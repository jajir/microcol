"""Runner contract tests with a fake Docker executable; no engine needed."""
import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import tempfile
import threading
import time
import unittest
from unittest.mock import patch
import xml.etree.ElementTree as ET

import verify


POM = '''<project xmlns="http://maven.apache.org/POM/4.0.0"><build><plugins><plugin>
<artifactId>maven-surefire-plugin</artifactId><configuration><includes>
<include>**/*Test.java</include></includes><excludes><exclude>sample/**.java</exclude></excludes>
</configuration></plugin></plugins></build></project>'''

DOCKER = r'''
import json, os, sys, time
from pathlib import Path
import xml.etree.ElementTree as E
a = sys.argv[1:]
with open(os.environ['DOCKER_EVENTS'], 'a') as f:
    f.write(json.dumps({'args': a, 'time': time.monotonic()}) + '\n')
if a[0] == 'info':
    print('{}')
elif a[:2] == ['image', 'inspect']:
    print('sha256:fixture')
elif 'run' in a:
    out = Path(a[a.index('-v') + 1].removesuffix(':/results')) / 'run-export'
    out.mkdir()
    mode = os.environ.get('DOCKER_MODE', '')
    is_ui = 'MICROCOL_TEST_MODULE=microcol-test' in a
    if mode == 'hang' and is_ui:
        time.sleep(20)
    time.sleep(0.2)
    if mode == 'bad-exit':
        raise SystemExit(7)
    if mode != 'missing-export':
        (out / 'exit-code.txt').write_text('0')
    names = next(x for x in a if x.startswith('-Dtest=')).split('=', 1)[1].split(',')
    for selection in names:
        name, separator, selected = selection.partition('#')
        methods = selected.split('+') if separator else ['test_run']
        if mode == 'missing-suite':
            continue
        suite = E.Element('testsuite', name=name, tests=str(len(methods)), errors='0', failures='0', skipped='0', time='0.2')
        for method in methods:
            E.SubElement(suite, 'testcase', classname=name, name=method)
        E.ElementTree(suite).write(out / ('TEST-' + name + '.xml'))
    with open(os.environ['DOCKER_EVENTS'], 'a') as f:
        f.write(json.dumps({'done': a[a.index('--name') + 1], 'time': time.monotonic()}) + '\n')
'''


class VerificationTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory(prefix="verification test ")
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.reports = self.root / "reports"
        self.reports.mkdir()

    def suite(self, name="sample.ExampleTest", methods=("test_run",), **attrs):
        suite = ET.Element("testsuite", name=name, tests=str(len(methods)),
                           failures="0", errors="0", skipped="0", time="1")
        suite.attrib.update(attrs)
        for method in methods:
            ET.SubElement(suite, "testcase", classname=name, name=method)
        path = self.reports / ("TEST-" + name + ".xml")
        ET.ElementTree(suite).write(path)
        return path

    def source(self, module, name, body):
        path = self.root / module / "src/test/java/sample" / (name + ".java")
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text("package sample; " + body)
        (self.root / module / "pom.xml").write_text(POM)
        return path

    def setup_project(self):
        for module, names in (("microcol-game", ["ModelTest"]),
                              ("microcol-test", ["FirstTest", "SecondTest"])):
            for name in names:
                self.source(module, name, "class " + name + " { @Test void test_run() {} }")
        subprocess.run(["git", "init", "-q", str(self.root)], check=True)
        subprocess.run(["git", "add", "."], cwd=self.root, check=True)
        subprocess.run(["git", "-c", "user.name=Test", "-c", "user.email=test@example.com",
                        "commit", "-qm", "fixture"], cwd=self.root, check=True)
        binary = self.root / "bin"
        binary.mkdir()
        docker = binary / "docker"
        docker.write_text("#!" + sys.executable + "\n" + DOCKER)
        docker.chmod(0o755)
        self.events = self.root / "events.jsonl"
        # Engine state is outside the source inventory, like real Docker state.
        (self.root / ".gitignore").write_text("target/\nevents.jsonl\n")
        environment = patch.dict(os.environ, {"PATH": str(binary) + os.pathsep + os.environ["PATH"],
                                              "DOCKER_EVENTS": str(self.events)})
        environment.start()
        self.addCleanup(environment.stop)

    def test_source_inventory_inheritance_literals_and_new_tests(self):
        self.source("microcol-game", "BaseTest", "abstract class BaseTest { @Test void inherited() {} }")
        self.source("microcol-game", "ChildTest", '''class ChildTest extends BaseTest {
            // @Test void commented() {}
            String example = "@Test void quoted() {}";
            @ParameterizedTest @ValueSource(ints={1,2}) void parameter(int n) {}
        }''')
        self.source("microcol-game", "HelperTest", "class HelperTest {}")
        self.assertEqual(verify.inventory(self.root, "microcol-game"),
                         {"sample.ChildTest": ["inherited", "parameter"]})
        self.source("microcol-game", "NewTest", "class NewTest { @Test void added() {} }")
        self.assertIn("sample.NewTest", verify.inventory(self.root, "microcol-game"))
        self.source("microcol-game", "DynamicTest", "class DynamicTest { @TestFactory Object generated() {} }")
        with self.assertRaisesRegex(ValueError, "Unsupported"):
            verify.inventory(self.root, "microcol-game")

    def test_balancing_covers_all_classes_once(self):
        groups = verify.partition(["a", "b", "c", "d"], 2, {"a": 10, "b": 5, "c": 3, "d": 2})
        self.assertEqual(groups, [["a"], ["b", "c", "d"]])
        self.assertEqual(verify.partition(["only"], 2, {}), [["only"]])

    def test_parameterized_reports_and_method_coverage(self):
        self.suite(methods=("test_run{int}[1]", "test_run{int}[2]"))
        result = verify.validate_reports(self.reports, {"sample.ExampleTest": ["test_run"]})
        self.assertEqual(result["counts"]["tests"], 2)
        with self.assertRaisesRegex(ValueError, "method mismatch"):
            verify.validate_reports(self.reports, {"sample.ExampleTest": ["test_run", "missing"]})

    def test_missing_duplicate_unexpected_empty_and_malformed_reports_fail(self):
        expected = {"sample.ExampleTest": ["test_run"]}
        with self.assertRaisesRegex(ValueError, "Missing"):
            verify.validate_reports(self.reports, expected)
        path = self.suite()
        other = self.reports / "TEST-copy.xml"
        other.write_bytes(path.read_bytes())
        with self.assertRaisesRegex(ValueError, "Duplicate"):
            verify.validate_reports(self.reports, expected)
        other.unlink()
        with self.assertRaisesRegex(ValueError, "unexpected"):
            verify.validate_reports(self.reports, {})
        self.suite(methods=())
        with self.assertRaisesRegex(ValueError, "empty"):
            verify.validate_reports(self.reports, expected)
        path.write_text("<broken")
        with self.assertRaises(ET.ParseError):
            verify.validate_reports(self.reports, expected)

    def test_failed_skipped_and_truncated_cases_fail(self):
        for attribute, element in (("errors", "error"), ("failures", "failure"), ("skipped", "skipped")):
            path = self.suite(**{attribute: "1"})
            tree = ET.parse(path)
            ET.SubElement(tree.getroot().find("testcase"), element)
            tree.write(path)
            with self.assertRaisesRegex(ValueError, "Unsuccessful"):
                verify.validate_reports(self.reports, {"sample.ExampleTest": ["test_run"]})
        self.suite(tests="2")
        with self.assertRaisesRegex(ValueError, "Incomplete"):
            verify.validate_reports(self.reports, {"sample.ExampleTest": ["test_run"]})
        self.suite(methods=("test_run", "test_run"))
        with self.assertRaisesRegex(ValueError, "Duplicate test"):
            verify.validate_reports(self.reports, {"sample.ExampleTest": ["test_run"]})

    def test_snapshot_preserves_dirty_untracked_sources_and_rejects_edits(self):
        self.setup_project()
        path = self.root / "microcol-game/pom.xml"
        path.write_text(POM + "\n")
        (self.root / "new.txt").write_text("untracked")
        identity = verify.snapshot(self.root, self.root / "target/source")
        self.assertEqual((self.root / "target/source/new.txt").read_text(), "untracked")
        self.assertIn("microcol-game/pom.xml", identity["files"])
        copy = verify.shutil.copy2

        def change_during_copy(source, destination):
            copy(source, destination)
            if source == path:
                path.write_text(POM + "\n\n")

        with patch.object(verify.shutil, "copy2", side_effect=change_during_copy):
            with self.assertRaisesRegex(ValueError, "Source changed"):
                verify.snapshot(self.root, self.root / "target/changed")

    def test_complete_run_waits_for_both_groups_and_keeps_runs_separate(self):
        self.setup_project()
        with patch.object(verify, "ROOT", self.root):
            self.assertEqual(verify.main([]), 0)
            self.assertEqual(verify.main(["--workers", "1"]), 0)
        runs = list((self.root / "target/verification").glob("run-*"))
        self.assertEqual(len(runs), 2)
        results = [json.loads((p / "summary.json").read_text()) for p in runs]
        self.assertEqual(sorted(len(r["groups"]) for r in results), [2, 3])
        for result in results:
            self.assertEqual(sum(g["counts"]["tests"] for g in result["groups"]), 3)
        events = [json.loads(line) for line in self.events.read_text().splitlines()]
        ui_starts = [e["time"] for e in events if "MICROCOL_TEST_MODULE=microcol-test" in e.get("args", [])]
        ui_ends = [e["time"] for e in events if "-ui-" in e.get("done", "")]
        self.assertLess(max(ui_starts[:2]), min(ui_ends[:2]))

    def setup_smoke(self):
        self.setup_project()
        selections = []
        for index in range(10):
            name = "TC_%02d_Test" % index
            self.source("microcol-test", name, "class " + name
                        + " { @Test void test_run() {} @Test void slower_test() {} }")
            selections.append("sample." + name + "#test_run")
        path = self.root / "scripts/ui-tests/smoke.json"
        path.parent.mkdir(parents=True)
        path.write_text(json.dumps(selections))
        return path

    def test_smoke_keeps_all_model_and_support_tests_and_only_selected_ui_methods(self):
        self.setup_smoke()
        with patch.object(verify, "ROOT", self.root):
            self.assertEqual(verify.main(["--smoke"]), 0)
        directory = next((self.root / "target/verification").glob("run-*"))
        summary = json.loads((directory / "summary.json").read_text())
        self.assertEqual(summary["scope"], "precommit")
        manifest = json.loads((directory / "manifest.json").read_text())
        self.assertEqual(manifest["timeout_seconds"], 360)
        self.assertEqual(summary["workers"], 1)
        self.assertEqual(len(summary["groups"]), 1)
        group = summary["groups"][0]
        self.assertEqual(group["counts"]["tests"], 13)
        self.assertIn("sample.ModelTest", group["expected"])
        self.assertIn("sample.FirstTest", group["expected"])
        self.assertEqual(group["expected"]["sample.TC_00_Test"], ["test_run"])

    def test_smoke_rejects_duplicate_or_unknown_scenarios(self):
        path = self.setup_smoke()
        selections = json.loads(path.read_text())
        expected = verify.inventory(self.root, "microcol-test")
        path.write_text(json.dumps([selections[0]] * 10))
        with self.assertRaisesRegex(ValueError, "distinct"):
            verify.smoke_inventory(self.root, expected)
        selections[0] = "sample.TC_00_Test#missing"
        path.write_text(json.dumps(selections))
        with self.assertRaisesRegex(ValueError, "Unknown"):
            verify.smoke_inventory(self.root, expected)

    def test_smoke_accepts_six_minutes_but_rejects_an_overrun(self):
        self.setup_smoke()
        for elapsed, expected_exit in ((301, 0), (360, 0), (361, 1)):
            with self.subTest(seconds=elapsed), patch.object(verify, "ROOT", self.root), \
                    patch.object(verify, "time") as clock:
                clock.monotonic.return_value = 0
                def completed(runner, *args):
                    self.assertEqual(runner.deadline, 360)
                    clock.monotonic.return_value = elapsed
                    return {"status": "passed"}
                with patch.object(verify.Runner, "group", new=completed):
                    self.assertEqual(verify.main(["--smoke"]), expected_exit)
        summaries = [json.loads(p.read_text()) for p in
                     (self.root / "target/verification").glob("run-*/summary.json")]
        failed = [s for s in summaries if s["status"] == "failed"]
        self.assertEqual(len(failed), 1)
        self.assertIn("exceeded 360 seconds", failed[0]["error"])

    def test_expired_total_budget_does_not_start_another_command(self):
        runner = verify.Runner([], 5400, threading.Event(), deadline=-1)
        with patch.object(verify.subprocess, "Popen") as process:
            with self.assertRaises(TimeoutError):
                runner.command(["docker", "info"], self.reports / "preflight.log", 120)
            process.assert_not_called()

    def test_failed_process_missing_export_and_missing_suite_are_not_success(self):
        self.setup_project()
        for mode in ("bad-exit", "missing-export", "missing-suite"):
            with patch.object(verify, "ROOT", self.root), patch.dict(os.environ, {"DOCKER_MODE": mode}):
                self.assertEqual(verify.main([]), 1)
        for path in (self.root / "target/verification").glob("run-*/summary.json"):
            summary = json.loads(path.read_text())
            self.assertEqual(summary["status"], "failed")
            self.assertEqual(len(summary["groups"]), 1)

    def test_preflight_timeout_records_failure_without_starting_tests(self):
        self.setup_project()
        with patch.object(verify, "ROOT", self.root), patch.object(
                verify.Runner, "command", side_effect=TimeoutError("Docker startup timed out")):
            self.assertEqual(verify.main([]), 1)
        summary = json.loads(next((self.root / "target/verification").glob("run-*/summary.json")).read_text())
        self.assertEqual(summary["groups"], [])
        self.assertEqual(summary["status"], "failed")
        self.assertIn("startup timed out", summary["error"])

    def test_managed_group_timeout_cleans_up_both_owned_ui_containers(self):
        with patch.dict(os.environ, {"MICROCOL_TEST_PROCESS_GROUP": "1"}):
            self.test_timeout_cleans_up_both_owned_ui_containers()

    def test_timeout_cleans_up_both_owned_ui_containers(self):
        self.setup_project()
        with patch.object(verify, "ROOT", self.root), patch.dict(os.environ, {"DOCKER_MODE": "hang"}):
            self.assertEqual(verify.main(["--timeout", "1"]), 1)
        events = [json.loads(line) for line in self.events.read_text().splitlines()]
        stops = [e["args"][-1] for e in events if e.get("args", [])[:1] == ["stop"]]
        self.assertEqual(len(stops), 2)
        self.assertTrue(all("-ui-" in name for name in stops))

    def test_sigterm_waits_for_owned_container_cleanup(self):
        self.setup_project()
        code = ("import sys; from pathlib import Path; sys.path.insert(0, "
                + repr(str(Path(verify.__file__).parent)) + "); import verify; verify.ROOT=Path("
                + repr(str(self.root)) + "); sys.exit(verify.main([]))")
        with patch.dict(os.environ, {"DOCKER_MODE": "hang"}):
            process = subprocess.Popen([sys.executable, "-c", code], stdout=subprocess.DEVNULL)
        try:
            deadline = time.monotonic() + 5
            while time.monotonic() < deadline:
                if self.events.exists() and self.events.read_text().count('MICROCOL_TEST_MODULE=microcol-test') == 2:
                    break
                time.sleep(0.02)
            else:
                self.fail("Both UI groups did not start")
            process.send_signal(signal.SIGTERM)
            self.assertEqual(process.wait(timeout=5), 1)
            summary = json.loads(next((self.root / "target/verification").glob("run-*/summary.json")).read_text())
            self.assertEqual(summary["status"], "failed")
            self.assertEqual(len(summary["groups"]), 3)
        finally:
            if process.poll() is None:
                process.kill()
                process.wait()


if __name__ == "__main__":
    unittest.main()
