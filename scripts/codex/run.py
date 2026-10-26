#!/usr/bin/env python3
"""Run existing worker, Docker or CI commands locally and queue one completion."""

import argparse
from datetime import datetime, timezone
import json
import math
import os
from pathlib import Path
import re
import shutil
import signal
import subprocess
import sys
import time
import uuid


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "scripts/ui-tests"))
from local_slot import LocalSlot
LOG_ROOT = Path.home() / "Library/Logs/Microcol/completions"
GRACE_SECONDS = 45
QUEUE_SECONDS = 30


def write_json(path, value):
    temporary = path.with_suffix(".tmp")
    temporary.write_text(json.dumps(value, indent=2) + "\n")
    temporary.replace(path)


def installed(name):
    path = shutil.which(name)
    if not path:
        raise ValueError(name + " must already be installed")
    return path


def parse_args(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--thread", required=True)
    parser.add_argument("--timeout", required=True, type=float)
    modes = parser.add_subparsers(dest="kind", required=True)
    worker = modes.add_parser("worker")
    worker.add_argument("job", type=Path)
    worker.add_argument("--profile", type=Path)
    worker.add_argument("--escalate-reason")
    modes.add_parser("docker").add_argument("arguments", nargs=argparse.REMAINDER)
    modes.add_parser("verify").add_argument("arguments", nargs=argparse.REMAINDER)
    ci = modes.add_parser("ci")
    ci.add_argument("run_id")
    ci.add_argument("--commit", required=True)
    ci.add_argument("--repo", required=True)
    args = parser.parse_args(argv)
    args.thread = str(uuid.UUID(args.thread))
    if not math.isfinite(args.timeout) or args.timeout <= 0:
        raise ValueError("Timeout must be finite and positive")
    if args.kind == "worker" and not args.job.resolve().is_dir():
        raise ValueError("Worker job directory does not exist")
    if args.kind == "ci":
        if not re.fullmatch(r"[1-9][0-9]*", args.run_id):
            raise ValueError("CI run ID must be a positive integer")
        if not re.fullmatch(r"[0-9a-fA-F]{40}", args.commit):
            raise ValueError("CI commit must be a full SHA")
        if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9_.-]*/[A-Za-z0-9][A-Za-z0-9_.-]*", args.repo):
            raise ValueError("CI repository must be OWNER/REPO")
        args.commit = args.commit.lower()
    return args


def request(args, run_id):
    record = {"kind": args.kind, "thread": args.thread, "timeout_seconds": args.timeout,
              "cwd": str(ROOT), "codex": installed("codex"), "run_id": run_id,
              "revision": subprocess.check_output(
                  ["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip(),
              "working_tree_status": subprocess.check_output(
                  ["git", "status", "--short"], cwd=ROOT, text=True).strip()}
    if args.kind == "worker":
        job = args.job.resolve()
        numbers = [int(p.name[8:]) for p in job.glob("attempt-*")
                   if p.is_dir() and p.name[8:].isdigit()]
        record["worker_attempt"] = str(job / ("attempt-" + str(max(numbers, default=-1) + 1)))
        command = [sys.executable, str(ROOT / "scripts/local-worker/worker.py"), "run", str(job)]
        if args.profile is not None:
            command += ["--profile", str(args.profile.resolve())]
        if args.escalate_reason is not None:
            command += ["--escalate-reason", args.escalate_reason]
    elif args.kind == "docker":
        record["docker"] = installed("docker")
        record["container"] = "microcol-" + run_id
        record["reports"] = str(ROOT / "target/docker-ui")
        arguments = args.arguments[1:] if args.arguments[:1] == ["--"] else args.arguments
        command = [record["docker"], "compose", "-f", str(ROOT / "compose.ui-tests.yml"),
                   "run", "--build", "--rm", "--name", record["container"], "ui-tests"] + arguments
    elif args.kind == "verify":
        arguments = args.arguments[1:] if args.arguments[:1] == ["--"] else args.arguments
        if "--smoke" in arguments:
            record["acceptance_seconds"] = 300
        command = [sys.executable, str(ROOT / "scripts/ui-tests/verify.py")] + arguments
        record["reports"] = str(ROOT / "target/verification")
    else:
        record.update(gh=installed("gh"), ci_run=args.run_id, ci_commit=args.commit, ci_repo=args.repo)
        command = [record["gh"], "run", "watch", args.run_id, "--repo", args.repo,
                   "--exit-status", "--interval", "30"]
    record["command"] = command
    return record


def dispatch(record):
    directory = LOG_ROOT / record["run_id"]
    directory.mkdir(parents=True, exist_ok=False)
    write_json(directory / "request.json", record)
    with (directory / "supervisor.log").open("w") as log:
        try:
            process = subprocess.Popen([sys.executable, str(Path(__file__).resolve()),
                                        "--supervise", str(directory)],
                                       stdin=subprocess.DEVNULL, stdout=log, stderr=log,
                                       start_new_session=True, cwd=record["cwd"])
        except OSError as error:
            write_json(directory / "result.json", {"status": "failed", "exit_code": None,
                                                    "error": "Supervisor start: " + str(error)})
            notify(directory, record)
            raise
    write_json(directory / "supervisor.json", {"pid": process.pid})
    return {"directory": str(directory), "pid": process.pid}


class Cancelled(Exception):
    pass


def cancel(signum, frame):
    raise Cancelled("Supervisor received signal " + str(signum))


def remaining(deadline):
    seconds = deadline - time.monotonic()
    if seconds <= 0:
        raise subprocess.TimeoutExpired("completion watchdog", 0)
    return seconds


def ci_metadata(record, directory, phase, deadline, log):
    command = [record["gh"], "run", "view", record["ci_run"], "--repo", record["ci_repo"],
               "--json", "databaseId,headSha,status,conclusion,url"]
    result = subprocess.run(command, capture_output=True, text=True,
                            timeout=min(30, remaining(deadline)), cwd=record["cwd"])
    log.write(result.stderr)
    if result.returncode:
        raise ValueError("CI identity lookup failed; see command.log")
    data = json.loads(result.stdout)
    write_json(directory / ("ci-" + phase + ".json"), data)
    if str(data["databaseId"]) != record["ci_run"] or data["headSha"] != record["ci_commit"]:
        raise ValueError("CI run/commit identity mismatch")
    return data


def cleanup(record, process, log):
    errors = []
    if process is not None:
        try:
            os.killpg(process.pid, signal.SIGTERM)
        except ProcessLookupError:
            pass
        try:
            process.wait(timeout=GRACE_SECONDS)
        except subprocess.TimeoutExpired:
            errors.append("Command required forced termination")
        finally:
            try:
                os.killpg(process.pid, signal.SIGKILL)
            except ProcessLookupError:
                pass
            process.wait()
    if record["kind"] == "docker":
        # The client is stopped first so it cannot create a container after cleanup.
        for command in ([record["docker"], "stop", "--timeout", "10", record["container"]],
                        [record["docker"], "rm", "--force", record["container"]]):
            try:
                deadline = time.monotonic() + 20
                while True:
                    result = subprocess.run(command, capture_output=True, text=True,
                                            timeout=remaining(deadline))
                    log.write(result.stdout + result.stderr)
                    # Compose --rm can be removing the same stopped container already.
                    if command[1] != "rm" or "is already in progress" not in result.stderr:
                        break
                    time.sleep(0.25)
                missing = any(text in result.stderr.lower()
                              for text in ("no such container", "no such object"))
                if result.returncode and not missing:
                    errors.append("Container cleanup failed: " + result.stderr.strip())
            except (OSError, subprocess.TimeoutExpired) as error:
                errors.append(str(error))
    return errors


def notify(directory, record):
    result = json.loads((directory / "result.json").read_text())
    message = ("Microcol " + record["kind"] + " " + record["run_id"] + ": " + result["status"]
               + "; exit " + str(result["exit_code"]) + ". Evidence: " + str(directory / "result.json")
               + ". Verification and review remain pending; inspect evidence before continuing "
                 "the already authorized task.")
    delivery = {"status": "attempting", "thread": record["thread"], "message": message}
    write_json(directory / "notification.json", delivery)
    try:
        result = subprocess.run([record["codex"], "queue", "--thread", record["thread"],
                                 "--message", message], capture_output=True, text=True,
                                timeout=QUEUE_SECONDS, cwd=record["cwd"])
        delivery.update(status="queued" if result.returncode == 0 else "failed",
                        exit_code=result.returncode, stdout=result.stdout, stderr=result.stderr)
    except subprocess.TimeoutExpired:
        delivery.update(status="unknown", error="Queue timed out; inspect task before retrying")
    except OSError as error:
        delivery.update(status="failed", error=str(error))
    write_json(directory / "notification.json", delivery)


def supervise(directory):
    record = json.loads((directory / "request.json").read_text())
    started = time.monotonic()
    deadline = started + record["timeout_seconds"]
    process = None
    slot = None
    admitted = record["kind"] not in ("docker", "verify")
    result = {"status": "failed", "exit_code": None, "cleanup_errors": [],
              "request": str(directory / "request.json"), "revision": record["revision"],
              "timeout_seconds": record["timeout_seconds"]}
    previous = {sig: signal.signal(sig, cancel) for sig in (signal.SIGTERM, signal.SIGINT)}
    try:
        with (directory / "command.log").open("w") as log:
            try:
                if record["kind"] in ("docker", "verify"):
                    slot = LocalSlot(log)
                    slot.acquire()
                    admitted = True
                    started = slot.acquired_at
                    deadline = started + record["timeout_seconds"]
                    if record.get("acceptance_seconds"):
                        deadline = min(deadline, started + record["acceptance_seconds"])
                    result["wait_seconds"] = slot.wait_seconds
                    remaining(deadline)
                    process = slot.start(record["command"], cwd=record["cwd"],
                                         stdin=subprocess.DEVNULL, stdout=log, stderr=log)
                else:
                    if record["kind"] == "ci":
                        ci_metadata(record, directory, "before", deadline, log)
                    process = subprocess.Popen(record["command"], cwd=record["cwd"],
                                               stdin=subprocess.DEVNULL, stdout=log, stderr=log,
                                               start_new_session=True)
                process.wait(timeout=remaining(deadline))
                result["status"] = "success" if process.returncode == 0 else "failed"
                if record["kind"] == "ci":
                    data = ci_metadata(record, directory, "after", deadline, log)
                    if data["status"] != "completed" or data["conclusion"] != "success":
                        result["status"] = "failed"
                if record["kind"] == "worker":
                    evidence = Path(record["worker_attempt"]) / "execution.json"
                    if evidence.exists() and json.loads(evidence.read_text()).get("timed_out"):
                        result["status"] = "timed_out"
                if record["kind"] == "docker" and process.returncode != 0:
                    result["cleanup_errors"] = cleanup(record, process, log)
            except (Cancelled, subprocess.TimeoutExpired) as error:
                result.update(status="cancelled" if isinstance(error, Cancelled) else "timed_out",
                              error=str(error))
                for sig in previous:
                    signal.signal(sig, signal.SIG_IGN)
                if admitted:
                    result["cleanup_errors"] = cleanup(record, process, log)
            except (OSError, ValueError, KeyError, TypeError, RuntimeError, subprocess.SubprocessError) as error:
                result.update(status="failed", error=str(error))
            finally:
                for sig in previous:
                    signal.signal(sig, signal.SIG_IGN)
                if slot is not None:
                    try:
                        slot.close()
                    except (OSError, RuntimeError, ValueError, subprocess.SubprocessError) as error:
                        result["status"] = "failed"
                        result["cleanup_errors"].append(str(error))
                result["exit_code"] = process.returncode if process else None
                if slot is not None and slot.acquired_at is not None:
                    started, admitted = slot.acquired_at, True
                    result["wait_seconds"] = slot.wait_seconds
                elapsed = round(time.monotonic() - started, 3)
                if (result["status"] == "success" and record.get("acceptance_seconds")
                        and time.monotonic() - started > record["acceptance_seconds"]):
                    result.update(status="failed", error="Acceptance budget exceeded including cleanup")
                result["elapsed_seconds"] = elapsed if admitted else 0
                if not admitted:
                    result["wait_seconds"] = elapsed
                write_json(directory / "result.json", result)
        notify(directory, record)
    finally:
        for sig, handler in previous.items():
            signal.signal(sig, handler)
    return result


def main(argv):
    if len(argv) == 2 and argv[0] == "--supervise":
        supervise(Path(argv[1]))
        return
    args = parse_args(argv)
    run_id = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ-") + uuid.uuid4().hex[:12]
    print(json.dumps(dispatch(request(args, run_id))))


if __name__ == "__main__":
    try:
        main(sys.argv[1:])
    except (OSError, ValueError, subprocess.SubprocessError) as error:
        print("Completion runner: " + str(error), file=sys.stderr)
        sys.exit(1)
