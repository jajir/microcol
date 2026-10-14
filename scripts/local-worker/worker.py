#!/usr/bin/env python3
"""Run one local Codex attempt; validate/apply text separately from acceptance."""

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import signal
import subprocess
import sys
import time
import urllib.request


ROOT = Path(__file__).resolve().parents[2]
LOCK = Path.home() / "Library/Application Support/Microcol/local-worker/active.lock"


def read_json(path):
    return json.loads(path.read_text())


def write_json(path, value):
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_text(json.dumps(value, indent=2) + "\n")
    temporary.replace(path)


def require(condition, message):
    if not condition:
        raise ValueError(message)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def profile(root=ROOT):
    config = read_json(root / ".codex/local-worker/profile.json")
    model_file = (root / ".codex/local-worker/Modelfile").read_text()
    bases = re.findall(r"^FROM (\S+)$", model_file, re.M)
    require(len(bases) == 1, "Modelfile must select one installed base model")
    config["base_model"] = bases[0]
    for name in ("num_ctx", "num_predict"):
        values = re.findall(r"^PARAMETER " + name + r" (\d+)$", model_file, re.M)
        require(len(values) == 1, "Modelfile must define " + name + " exactly once")
        config[name] = int(values[0])
    for name in ("num_ctx", "num_predict", "attempt_seconds", "task_seconds"):
        require(type(config[name]) is int and config[name] > 0, "Invalid " + name)
    require(type(config["max_repairs"]) is int and config["max_repairs"] >= 0,
            "Invalid repair limit")
    require(config["num_predict"] < config["num_ctx"], "Leave room for input")
    require(config["reasoning"] in ("low", "medium", "high"), "Invalid reasoning")
    require(isinstance(config["model"], str) and config["model"].strip(), "Missing model")
    return config


def source_path(tree, relative):
    require(isinstance(relative, str) and relative and not Path(relative).is_absolute(),
            "Source path must be worktree-relative")
    path = tree.resolve() / relative
    require(path.resolve().is_relative_to(tree.resolve()), "Source escapes worktree")
    require(path.is_file() and path.resolve() == path.absolute(),
            "Missing or symlink source: " + relative)
    return path


def check_anchors(tree, contract):
    allowed = contract["allowed_files"]
    require(isinstance(allowed, list) and allowed and len(set(allowed)) == len(allowed),
            "Invalid allowed_files")
    hashes = {name: digest(source_path(tree, name)) for name in allowed}
    anchors = contract.get("anchors", [])
    require(contract["mode"] != "text-only" or anchors, "Text-only task needs anchors")
    ids = set()
    for anchor in anchors:
        require(isinstance(anchor["id"], str) and anchor["id"], "Missing anchor ID")
        require(anchor["id"] not in ids, "Duplicate anchor ID")
        ids.add(anchor["id"])
        require(anchor["file"] in allowed, "Anchor file is not allowed")
        require(isinstance(anchor["old"], str) and anchor["old"], "Empty anchor")
        count = anchor["expected_matches"]
        require(type(count) is int and count > 0, "Invalid match count")
        actual = source_path(tree, anchor["file"]).read_text().count(anchor["old"])
        require(actual == count, "Anchor mismatch: " + anchor["id"])
    return hashes


def budget(job, config):
    attempts = sorted(job.glob("attempt-*"), key=lambda p: int(p.name.split("-")[-1]))
    require([p.name for p in attempts] == [f"attempt-{n}" for n in range(len(attempts))],
            "Nonconsecutive attempt history")
    used = 0
    for attempt in attempts:
        require((attempt / "execution.json").is_file(), "Incomplete attempt needs diagnosis")
        elapsed = read_json(attempt / "execution.json")["elapsed_seconds"]
        require(isinstance(elapsed, (int, float)) and elapsed >= 0, "Invalid elapsed time")
        require(read_json(attempt / "profile.json") == config, "Profile changed during task")
        used += elapsed
    require(len(attempts) <= config["max_repairs"], "Repair limit exhausted")
    allowed = min(config["attempt_seconds"], config["task_seconds"] - used)
    require(allowed > 0, "Task time exhausted")
    return len(attempts), used, allowed


def git(tree, *args):
    return subprocess.check_output(["git", "-C", str(tree), *args], text=True).strip()


def preflight(job, config):
    contract = read_json(job / "job.json")
    require(contract["mode"] in ("text-only", "edit"), "Unknown output mode")
    require(Path(contract["worktree"]).is_absolute(), "Worktree path must be absolute")
    tree = Path(contract["worktree"]).resolve()
    require(tree != ROOT and tree.is_dir(), "Use an isolated worktree")
    require(Path(git(tree, "rev-parse", "--show-toplevel")) == tree, "Not a worktree root")
    require(git(tree, "rev-parse", "HEAD") == contract["baseline"], "Baseline changed")
    require(git(tree, "rev-parse", "--abbrev-ref", "HEAD") == "HEAD", "Worker must be detached")
    require(Path(git(tree, "rev-parse", "--path-format=absolute", "--git-common-dir")) ==
            Path(git(ROOT, "rev-parse", "--path-format=absolute", "--git-common-dir")),
            "Worktree belongs to another repository")
    for name in ("task.md", "worker-role.md"):
        require((job / name).is_file() and (job / name).read_text().strip(), "Missing " + name)
    return contract, tree, check_anchors(tree, contract), budget(job, config)


def api(route, body=None):
    request = urllib.request.Request(
        "http://127.0.0.1:11434/api/" + route,
        data=None if body is None else json.dumps(body).encode(),
        headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(request, timeout=5) as response:
        return json.load(response)


def model_info(config):
    info = api("show", {"model": config["model"]})
    require(info.get("details", {}).get("parent_model") == config["base_model"],
            "Ollama base model mismatch")
    for name in ("num_ctx", "num_predict"):
        actual = re.search(r"^" + name + r"\s+(\d+)\s*$", info["parameters"], re.M)
        require(actual and int(actual[1]) == config[name], "Ollama profile mismatch: " + name)
    models = [m for m in api("tags")["models"] if m["name"] == config["model"]]
    require(len(models) == 1, "Installed alias missing")
    return {"digest": models[0]["digest"], "parameters": info["parameters"]}


def command(config, contract, tree, attempt, codex):
    text_only = contract["mode"] == "text-only"
    cmd = [codex, "-a", "never", "exec", "--oss", "--local-provider", "ollama",
           "-m", config["model"], "--ignore-user-config", "--ephemeral", "-s",
           "read-only" if text_only else "workspace-write", "-C", str(tree),
           "--json", "--color", "never", "-o", str(attempt / "response.md")]
    settings = {
        "model_instructions_file": str(attempt / "worker-role.md"),
        "model_context_window": config["num_ctx"], "project_doc_max_bytes": 0,
        "features.shell_tool": not text_only, "features.multi_agent": False,
        "skills.include_instructions": False, "features.plugins": False,
        "features.apps": False, "features.browser_use": False,
        "features.computer_use": False, "features.image_generation": False,
        "web_search": "disabled", "model_reasoning_effort": config["reasoning"],
        "model_supports_reasoning_summaries": True, "model_reasoning_summary": "none",
        "tool_output_token_limit": 1000,
    }
    for key, value in settings.items():
        cmd += ["-c", key + "=" + json.dumps(value)]
    return cmd + ["-"]


def terminate(process):
    if process is not None and process.poll() is None:
        os.killpg(process.pid, signal.SIGTERM)
        try:
            process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            os.killpg(process.pid, signal.SIGKILL)
            process.wait()


def supervise(cmd, tree, attempt, environment, seconds, sample):
    process = None
    started = time.monotonic()
    result = {}
    try:
        with (attempt / "task.md").open() as source, \
                (attempt / "events.jsonl").open("w") as output, \
                (attempt / "stderr.log").open("w") as error:
            process = subprocess.Popen(cmd, cwd=tree, env=environment, stdin=source,
                                       stdout=output, stderr=error, start_new_session=True)
            write_json(attempt / "process.json", {"pid": process.pid})
            while process.poll() is None:
                if time.monotonic() - started >= seconds:
                    result["timed_out"] = True
                    break
                sample()
                time.sleep(min(1, max(0, seconds - (time.monotonic() - started))))
    except BaseException as error:
        result["error"] = str(error) or type(error).__name__
    finally:
        terminate(process)
        result["exit_code"] = process.returncode if process else None
    return result


def claim(job, lock=LOCK):
    lock.parent.mkdir(parents=True, exist_ok=True)
    try:
        lock.mkdir()
        write_json(lock / "owner.json", {"job": str(job), "runner_pid": None})
    except FileExistsError:
        owner = read_json(lock / "owner.json")
        require(owner.get("job") == str(job), "Worker lock belongs to another job")
    # Exclusive marker also prevents two callers from resuming the same job.
    (lock / "running").mkdir()
    write_json(lock / "owner.json", {"job": str(job), "runner_pid": os.getpid()})


def run(job):
    config = profile()
    contract, tree, hashes, (number, used, seconds) = preflight(job, config)
    codex, ollama = shutil.which("codex"), shutil.which("ollama")
    require(codex and ollama, "Codex and Ollama must already be installed")
    info = model_info(config)
    if number:
        require(read_json(job / "attempt-0/model.json")["digest"] == info["digest"],
                "Installed model changed during task")
    require(not api("ps")["models"], "Unload existing inference before starting")
    claim(job, LOCK)
    def interrupted(signum, frame):
        raise InterruptedError("Launcher received signal " + str(signum))
    previous_handler = signal.signal(signal.SIGTERM, interrupted)
    attempt = job / f"attempt-{number}"
    started = time.monotonic()
    result = {"allowed_seconds": seconds, "previous_worker_seconds": used}
    loaded = 0
    try:
        attempt.mkdir()
        for name in ("task.md", "worker-role.md", "job.json"):
            shutil.copyfile(job / name, attempt / name)
        write_json(attempt / "profile.json", config)
        write_json(attempt / "source-hashes.json", hashes)
        write_json(attempt / "model.json", info)
        (attempt / "input.diff").write_text(git(tree, "diff", "--binary", "HEAD"))
        (attempt / "input-status.txt").write_text(git(tree, "status", "--short"))
        cmd = command(config, contract, tree, attempt, codex)
        write_json(attempt / "command.json", cmd)
        child_home = job / "codex-home"
        child_home.mkdir(exist_ok=True)
        environment = dict(os.environ, CODEX_HOME=str(child_home))

        def sample():
            nonlocal loaded
            models = api("ps")["models"]
            with (attempt / "runtime.jsonl").open("a") as stream:
                stream.write(json.dumps({"seconds": time.monotonic() - started,
                                         "models": models}) + "\n")
            for model in models:
                require(model["name"] == config["model"] and model["digest"] == info["digest"]
                        and model["context_length"] == config["num_ctx"],
                        "Unexpected runtime model/context")
                loaded += 1

        result.update(supervise(cmd, tree, attempt, environment, seconds, sample))
        result["loaded_samples"] = loaded
        require(not result.get("error"), result.get("error"))
        require(not result.get("timed_out"), "Worker deadline exhausted")
        require(result.get("exit_code") == 0, "Codex exited " + str(result.get("exit_code")))
        require(loaded > 0, "No runtime identity samples")
        if contract["mode"] == "text-only":
            require(all(digest(source_path(tree, name)) == value for name, value in hashes.items()),
                    "Text-only worker changed source")
            forbidden = []
            for line in (attempt / "events.jsonl").read_text().splitlines():
                event = json.loads(line)
                item = event.get("item", {})
                if item.get("type") in ("command_execution", "file_change", "mcp_tool_call", "web_search"):
                    forbidden.append(item["type"])
            require(not forbidden, "Text-only worker used tools: " + str(forbidden))
        require((attempt / "response.md").is_file() and
                (attempt / "response.md").read_text().strip(), "Empty final response")
    except BaseException as error:
        result["error"] = str(error) or type(error).__name__
    finally:
        try:
            unloaded = subprocess.run([ollama, "stop", config["model"]], capture_output=True,
                                      text=True, timeout=30)
            (attempt / "unload.log").write_text(unloaded.stdout + unloaded.stderr)
            result["unload_exit_code"] = unloaded.returncode
            require(unloaded.returncode == 0 and not api("ps")["models"], "Model unload failed")
        except BaseException as error:
            result["cleanup_error"] = str(error)
        result["elapsed_seconds"] = round(time.monotonic() - started, 3)
        result["cumulative_worker_seconds"] = used + result["elapsed_seconds"]
        write_json(attempt / "execution.json", result)
        write_json(LOCK / "owner.json", {"job": str(job), "runner_pid": None})
        (LOCK / "running").rmdir()
        signal.signal(signal.SIGTERM, previous_handler)
    print(json.dumps(result, indent=2))
    return 0 if result.get("exit_code") == 0 and not any(
        result.get(key) for key in ("error", "cleanup_error", "timed_out")) else 1


def replacements(tree, contract, hashes, response):
    check_anchors(tree, contract)
    require(all(digest(source_path(tree, name)) == value for name, value in hashes.items()),
            "Source changed since dispatch")
    require(isinstance(response, dict) and isinstance(response.get("replacements"), list),
            "Expected a JSON object with replacements")
    anchors = {a["id"]: a for a in contract["anchors"]}
    edits = {}
    for item in response["replacements"]:
        require(isinstance(item, dict), "Replacement must be an object")
        key = item.get("id")
        if key is None:
            # Original-prompt replays still return the historical full anchor format.
            matches = [a["id"] for a in anchors.values() if all(
                item.get(k) == a[k] for k in ("file", "old", "expected_matches"))]
            require(len(matches) == 1, "Unknown or ambiguous legacy anchor")
            key = matches[0]
        require(key in anchors and key not in edits, "Unknown or duplicate replacement ID")
        require(isinstance(item.get("new"), str), "Replacement text must be a string")
        require(not any(k in item and item[k] != anchors[key][k]
                        for k in ("file", "old", "expected_matches")), "Conflicting anchor fields")
        require(item["new"] != anchors[key]["old"], "Unchanged replacement: " + key)
        edits[key] = item["new"]
    require(edits.keys() == anchors.keys(), "Missing replacements")
    planned = {}
    for key, anchor in anchors.items():
        path = source_path(tree, anchor["file"])
        text = planned.get(path, path.read_text())
        require(text.count(anchor["old"]) == anchor["expected_matches"], "Overlapping anchors")
        planned[path] = text.replace(anchor["old"], edits[key])
    return planned


def apply(job, number):
    attempt = job / f"attempt-{number}"
    execution = read_json(attempt / "execution.json")
    require(execution.get("exit_code") == 0 and not any(execution.get(k) for k in
            ("error", "cleanup_error", "timed_out")), "Attempt did not finish cleanly")
    contract = read_json(attempt / "job.json")
    require(contract["mode"] == "text-only", "Direct edits do not use replacement application")
    tree = Path(contract["worktree"])
    require(git(tree, "rev-parse", "HEAD") == contract["baseline"], "Baseline changed")
    planned = replacements(tree, contract, read_json(attempt / "source-hashes.json"),
                           read_json(attempt / "response.md"))
    originals = {path: path.read_bytes() for path in planned}
    try:
        for path, text in planned.items():
            path.write_text(text)
    except BaseException:
        for path, content in originals.items():
            path.write_bytes(content)
        raise
    write_json(attempt / "application.json", {"files": [str(p) for p in planned],
                                              "verification": "pending caller"})
    print("Applied validated replacements; formatting and tests remain caller-owned.")


def release(job, lock=LOCK):
    require(read_json(lock / "owner.json").get("job") == str(job), "Foreign worker lock")
    require(not (lock / "running").exists(), "Worker still running")
    require(not api("ps")["models"], "Model still loaded")
    (lock / "owner.json").unlink()
    lock.rmdir()


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("run", "apply", "release"))
    parser.add_argument("job", type=Path)
    parser.add_argument("--attempt", type=int)
    args = parser.parse_args()
    job = args.job.resolve()
    if args.action == "run":
        return run(job)
    if args.action == "apply":
        require(args.attempt is not None and args.attempt >= 0, "Select --attempt explicitly")
        apply(job, args.attempt)
    else:
        release(job)
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (ValueError, OSError, KeyError, TypeError, subprocess.SubprocessError) as error:
        print("Local worker stopped: " + str(error), file=sys.stderr)
        sys.exit(1)
