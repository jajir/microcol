#!/usr/bin/env python3
"""Verify the pre-commit gate or complete model/UI suites in isolated Docker."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import fnmatch
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import signal
import subprocess
import sys
import tempfile
import threading
import time
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
MODULES = ("microcol-game", "microcol-test")
NS = {"m": "http://maven.apache.org/POM/4.0.0"}


def write_json(path, value):
    temporary = path.with_suffix(".tmp")
    temporary.write_text(json.dumps(value, indent=2, sort_keys=True) + "\n")
    temporary.replace(path)


def git(root, *args):
    return subprocess.check_output(["git", *args], cwd=root)


def source_files(root):
    names = git(root, "ls-files", "-z", "--cached", "--others", "--exclude-standard")
    return sorted({os.fsdecode(name) for name in names.split(b"\0") if name
                   and "target" not in Path(os.fsdecode(name)).parts
                   and (root / os.fsdecode(name)).exists()})


def hashes(root, names):
    result = {}
    for name in names:
        path = root / name
        if path.is_symlink():
            raise ValueError("Source symlinks need explicit snapshot support: " + name)
        result[name] = hashlib.sha256(path.read_bytes()).hexdigest()
    return result


def snapshot(root, destination):
    revision = git(root, "rev-parse", "HEAD").decode().strip()
    names = source_files(root)
    before = hashes(root, names)
    for name in names:
        target = destination / name
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(root / name, target)
    if (names != source_files(root) or before != hashes(root, names)
            or before != hashes(destination, names)
            or revision != git(root, "rev-parse", "HEAD").decode().strip()):
        raise ValueError("Source changed during snapshot; retry after edits finish")
    return {"revision": revision, "files": before,
            "working_tree_status": git(root, "status", "--short").decode()}


def inventory(source, module):
    """Match Surefire's source conventions and explicit/inherited JUnit methods."""
    pom = ET.parse(source / module / "pom.xml")
    plugin = next(p for p in pom.findall(".//m:build/m:plugins/m:plugin", NS)
                  if p.findtext("m:artifactId", namespaces=NS) == "maven-surefire-plugin")
    includes = [n.text for n in plugin.findall("m:configuration/m:includes/m:include", NS)]
    includes = includes or ["**/Test*.java", "**/*Test.java", "**/*Tests.java", "**/*TestCase.java"]
    classes = {}
    for path in sorted((source / module / "src/test/java").rglob("*.java")):
        text = path.read_text()
        # Remove comments and literals so examples in contract tests are not tests.
        text = re.sub(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'', " ", text)
        declaration = re.search(r"\b(abstract\s+)?class\s+" + re.escape(path.stem)
                                + r"\b(?:\s+extends\s+([\w.]+))?", text)
        if not declaration:
            continue
        package = re.search(r"\bpackage\s+([\w.]+)\s*;", text).group(1)
        name = package + "." + path.stem
        if re.search(r"@(?:[\w.]+\.)?(TestFactory|TestTemplate|Nested)\b", text):
            raise ValueError("Unsupported dynamic/nested inventory; extend discovery first: " + name)
        methods = set(re.findall(r"@(?:[\w.]+\.)?(?:Test|ParameterizedTest|RepeatedTest)\b"
                                 r"[\s\S]*?\bvoid\s+(\w+)\s*\(", text))
        parent = declaration.group(2)
        if parent and "." not in parent:
            imported = re.search(r"\bimport\s+([\w.]+\." + re.escape(parent) + r")\s*;", text)
            parent = imported.group(1) if imported else package + "." + parent
        classes[name] = (methods, parent, bool(declaration.group(1)),
                         path.relative_to(source / module / "src/test/java").as_posix())

    def inherited(name, seen=()):
        if name not in classes:
            return set()
        if name in seen:
            raise ValueError("Cyclic test inheritance: " + name)
        methods, parent, _, _ = classes[name]
        return methods | inherited(parent, (*seen, name))

    def matches(path, patterns):
        return any(fnmatch.fnmatchcase(path, p) or
                   (p.startswith("**/") and fnmatch.fnmatchcase(path, p[3:])) for p in patterns)

    result = {name: sorted(inherited(name)) for name, (_, _, abstract, path) in classes.items()
              if not abstract and matches(path, includes)
              and inherited(name)}
    if not result:
        raise ValueError("Empty test inventory: " + module)
    return result


def partition(classes, workers, timings):
    groups = [[] for _ in range(workers)]
    loads = [0.0] * workers
    default = sum(timings.values()) / len(timings) if timings else 1.0
    for name in sorted(classes, key=lambda n: (-timings.get(n, default), n)):
        index = min(range(workers), key=lambda i: loads[i])
        groups[index].append(name)
        loads[index] += timings.get(name, default)
    return [sorted(group) for group in groups if group]


def read_timings(directory):
    timings = {}
    if directory:
        for path in sorted(directory.rglob("TEST-*.xml")):
            suite = ET.parse(path).getroot()
            seconds = float(suite.get("time", "0"))
            if seconds > 0:
                timings[suite.attrib["name"]] = seconds
    return timings


def validate_reports(directory, expected):
    counts = dict(tests=0, failures=0, errors=0, skipped=0)
    actual = {}
    for path in sorted(directory.rglob("TEST-*.xml")):
        suite = ET.parse(path).getroot()
        name = suite.attrib["name"]
        if name in actual or name not in expected:
            raise ValueError("Duplicate or unexpected suite: " + name)
        cases = suite.findall("testcase")
        if int(suite.attrib["tests"]) != len(cases) or not cases:
            raise ValueError("Incomplete or empty suite: " + name)
        identities = [(case.get("classname"), case.get("name")) for case in cases]
        if any(classname != name or not method for classname, method in identities):
            raise ValueError("Invalid test case identity: " + name)
        if len(set(identities)) != len(identities):
            raise ValueError("Duplicate test case: " + name)
        methods = {re.split(r"[({\[]", case.attrib["name"], maxsplit=1)[0] for case in cases}
        if methods != set(expected[name]):
            raise ValueError("Test method mismatch in " + name + ": expected "
                             + str(expected[name]) + ", got " + str(sorted(methods)))
        actual[name] = {"tests": len(cases), "seconds": float(suite.get("time", "0"))}
        counts["tests"] += len(cases)
        for key, element in (("failures", "failure"), ("errors", "error"), ("skipped", "skipped")):
            reported = int(suite.attrib.get(key, "0"))
            observed = sum(case.find(element) is not None for case in cases)
            if reported != observed:
                raise ValueError("Inconsistent " + key + " in " + name)
            counts[key] += observed
    if set(actual) != set(expected):
        raise ValueError("Missing suites: " + ", ".join(sorted(set(expected) - set(actual))))
    if any(counts[k] for k in ("failures", "errors", "skipped")):
        raise ValueError("Unsuccessful tests: " + str(counts))
    return {"counts": counts, "suites": actual}


class Runner:
    def __init__(self, compose, timeout, cancelled, deadline=None):
        self.compose = compose
        self.timeout = timeout
        self.cancelled = cancelled
        self.deadline = deadline

    def command(self, command, log, timeout=None):
        started = time.monotonic()
        timeout = timeout or self.timeout
        if self.deadline is not None:
            timeout = min(timeout, self.deadline - started)
        if timeout <= 0:
            raise TimeoutError("Command deadline exceeded: " + str(log))
        with log.open("w") as output:
            process = subprocess.Popen(command, stdout=output, stderr=subprocess.STDOUT,
                                       stdin=subprocess.DEVNULL,
                                       start_new_session=not os.getenv("MICROCOL_TEST_PROCESS_GROUP"))
            try:
                while True:
                    if self.cancelled.is_set():
                        raise InterruptedError("Verification cancelled")
                    if time.monotonic() - started > timeout:
                        raise TimeoutError("Command deadline exceeded: " + str(log))
                    try:
                        return process.wait(timeout=0.25)
                    except subprocess.TimeoutExpired:
                        pass
            finally:
                if process.poll() is None:
                    if os.getenv("MICROCOL_TEST_PROCESS_GROUP"):
                        process.terminate()
                    else:
                        os.killpg(process.pid, signal.SIGTERM)
                    try:
                        process.wait(timeout=5)
                    except subprocess.TimeoutExpired:
                        if os.getenv("MICROCOL_TEST_PROCESS_GROUP"):
                            process.kill()
                        else:
                            os.killpg(process.pid, signal.SIGKILL)
                        process.wait()

    def group(self, directory, module, expected, container, selection=None):
        directory.mkdir()
        started = time.monotonic()
        result = {"status": "failed", "container": container, "module": module,
                  "expected": expected}
        command = self.compose + ["run", "--rm", "--no-deps", "--name", container,
                                  "-v", str(directory) + ":/results", "-e",
                                  "MICROCOL_TEST_MODULE=" + module, "ui-tests"]
        # Explicit complete selection also includes the legacy-excluded ConnectorTest.
        command += ["-Dtest=" + ",".join(selection if selection is not None else expected),
                    "-Dsurefire.failIfNoSpecifiedTests=false"]
        result["command"] = command
        write_json(directory / "request.json", result)
        try:
            code = self.command(command, directory / "console.log")
            result["exit_code"] = code
            exports = list(directory.glob("run-*/exit-code.txt"))
            if code != 0 or len(exports) != 1 or exports[0].read_text().strip() != "0":
                raise ValueError("Process/export failed; inspect console.log and run-*/maven.log")
            result.update(validate_reports(directory, expected))
            result["status"] = "passed"
        except (OSError, ValueError, KeyError, ET.ParseError) as error:
            result["error"] = str(error)
        finally:
            # Compose client termination does not reliably stop its container.
            with (directory / "cleanup.log").open("w") as log:
                if "exit_code" not in result:
                    try:
                        subprocess.run(["docker", "stop", "--time", "10", container], stdout=log,
                                       stderr=subprocess.STDOUT, timeout=15)
                    except (OSError, subprocess.TimeoutExpired) as error:
                        result["stop_error"] = str(error)
                try:
                    removal = subprocess.run(["docker", "rm", "-f", container], stdout=log,
                                             stderr=subprocess.STDOUT, timeout=20)
                    if removal.returncode and "No such container" not in (directory / "cleanup.log").read_text():
                        result.update(status="failed", cleanup_error="Container removal failed")
                except (OSError, subprocess.TimeoutExpired) as error:
                    result.update(status="failed", cleanup_error=str(error))
            result["seconds"] = round(time.monotonic() - started, 3)
            write_json(directory / "result.json", result)
            print(directory.name + ": " + result["status"] + " (" + str(result["seconds"]) + " s)", flush=True)
        return result


def smoke_inventory(source, expected):
    selections = json.loads((source / "scripts/ui-tests/smoke.json").read_text())
    if len(selections) != 10 or len(set(selections)) != 10:
        raise ValueError("Smoke manifest must contain ten distinct scenarios")
    selected = {name: methods for name, methods in expected.items()
                if not name.rsplit(".", 1)[-1].startswith("TC_")}
    for selection in selections:
        name, method = selection.split("#")
        if method not in expected.get(name, []):
            raise ValueError("Unknown smoke scenario: " + selection)
        selected.setdefault(name, []).append(method)
    return selected


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--workers", type=int, choices=(1, 2), default=2)
    parser.add_argument("--timeout", type=int, default=5400, help="Seconds per build/test group")
    parser.add_argument("--total-timeout", type=int, help="Seconds for the complete verification, including cleanup")
    parser.add_argument("--timings", type=Path, help="Previous Surefire XML directory for balancing")
    scope = parser.add_mutually_exclusive_group()
    scope.add_argument("--model", action="store_true", help="All model/JUnit tests only")
    scope.add_argument("--smoke", action="store_true",
                       help="Support tests and ten UI scenarios; 420 seconds total; no model tests")
    args = parser.parse_args(argv)
    if args.timeout <= 0 or (args.total_timeout is not None and args.total_timeout <= 0) or (args.timings and not args.timings.is_dir()):
        parser.error("Positive timeout and an existing timings directory are required")
    output = ROOT / "target/verification"
    output.mkdir(parents=True, exist_ok=True)
    directory = Path(tempfile.mkdtemp(prefix="run-", dir=output))
    print("Verification directory: " + str(directory), flush=True)
    started = time.monotonic()
    total_timeout = min(420, args.total_timeout or 420) if args.smoke else args.total_timeout
    summary = {"status": "failed", "groups": [],
               "workers": 1 if args.smoke or args.model else args.workers,
               "scope": "precommit" if args.smoke else "model" if args.model else "full"}
    cancelled = threading.Event()
    old_handlers = {sig: signal.signal(sig, lambda *_: cancelled.set())
                    for sig in (signal.SIGTERM, signal.SIGINT)}
    try:
        source = directory / "source"
        identity = snapshot(ROOT, source)
        modules = ("microcol-test",) if args.smoke else ("microcol-game",) if args.model else MODULES
        expected = {module: inventory(source, module) for module in modules}
        if args.smoke:
            expected["microcol-test"] = smoke_inventory(source, expected["microcol-test"])
        allocations = partition(expected.get("microcol-test", {}), summary["workers"], read_timings(args.timings))
        manifest = {"source": identity, "expected": expected, "ui_groups": allocations,
                    "workers": summary["workers"], "timeout_seconds": 420 if args.smoke else args.timeout,
                    "timings": str(args.timings.resolve()) if args.timings else None}
        manifest["scope"] = summary["scope"]
        manifest["total_timeout_seconds"] = total_timeout
        write_json(directory / "manifest.json", manifest)
        summary["revision"] = identity["revision"]
        # A unique image tag prevents another invocation's build replacing ours.
        override = directory / "compose.json"
        write_json(override, {"services": {"ui-tests": {"image": "microcol-ui-verify:" + directory.name}}})
        compose = ["docker", "compose", "-f", str(source / "compose.ui-tests.yml"), "-f", str(override)]
        runner = Runner(compose, args.timeout, cancelled, started + total_timeout if total_timeout else None)
        if runner.command(["docker", "info", "--format", "{{json .}}"], directory / "docker-info.json", 120):
            raise ValueError("Docker unavailable; inspect docker-info.json")
        if runner.command(compose + ["build", "ui-tests"], directory / "build.log"):
            raise ValueError("Image build failed; inspect build.log")
        if runner.command(["docker", "image", "inspect", "--format", "{{.Id}}",
                           "microcol-ui-verify:" + directory.name], directory / "image-id.txt", 30):
            raise ValueError("Cannot identify built image")
        manifest["image_id"] = (directory / "image-id.txt").read_text().strip()
        write_json(directory / "manifest.json", manifest)
        write_json(override, {"services": {"ui-tests": {"image": manifest["image_id"]}}})
        if args.smoke:
            selected = expected["microcol-test"]
            selection = [name + "#" + "+".join(methods)
                         if name.rsplit(".", 1)[-1].startswith("TC_") else name
                         for name, methods in selected.items()]
            summary["groups"].append(runner.group(directory / "precommit", "microcol-test", selected,
                                                "microcol-verify-" + directory.name + "-precommit", selection))
        else:
            model = runner.group(directory / "model", "microcol-game", expected["microcol-game"],
                                 "microcol-verify-" + directory.name + "-model")
            summary["groups"].append(model)
            if model["status"] != "passed":
                raise ValueError("Model verification failed; UI groups were not started")
            if not args.model:
                with ThreadPoolExecutor(max_workers=args.workers) as executor:
                    futures = [executor.submit(runner.group, directory / ("ui-%02d" % (i + 1)),
                                               "microcol-test", {name: expected["microcol-test"][name] for name in names},
                                               "microcol-verify-" + directory.name + "-ui-%02d" % (i + 1))
                               for i, names in enumerate(allocations)]
                    summary["groups"].extend(future.result() for future in futures)
        if hashes(source, list(identity["files"])) != identity["files"]:
            raise ValueError("Frozen source changed during verification")
        if any(group["status"] != "passed" for group in summary["groups"]):
            raise ValueError("Incomplete/failed verification; inspect group result.json files")
        if cancelled.is_set():
            raise InterruptedError("Verification cancelled")
        if args.smoke and time.monotonic() - started > 420:
            raise ValueError("Pre-commit verification exceeded 420 seconds")
        if total_timeout and time.monotonic() - started > total_timeout:
            raise ValueError("Verification exceeded its total time budget")
        summary["status"] = "passed"
    except (OSError, ValueError, KeyError, ET.ParseError, subprocess.SubprocessError) as error:
        summary["error"] = str(error)
    finally:
        summary["seconds"] = round(time.monotonic() - started, 3)
        write_json(directory / "summary.json", summary)
        for sig, handler in old_handlers.items():
            signal.signal(sig, handler)
    print("Verification " + summary["status"] + ": " + str(directory / "summary.json"), flush=True)
    return 0 if summary["status"] == "passed" else 1


if __name__ == "__main__":
    sys.exit(main())
