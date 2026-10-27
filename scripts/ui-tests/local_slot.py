#!/usr/bin/env python3
"""One local Docker test job at a time, shared by all worktrees of this user."""
import fcntl
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
from urllib.parse import urlsplit

LOCK_PATH = Path.home() / ".cache/microcol/docker-tests.lock"
OWNER = str(os.getuid())
LABEL = "org.microcol.local-test=" + OWNER
CLEANUP_SECONDS = 20


def event(log, phase, state, **fields):
    print("local-test " + json.dumps(dict(
        time=time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        phase=phase, state=state, pid=os.getpid(), **fields)), file=log, flush=True)


def excerpt(value, command=()):
    if isinstance(value, bytes):
        value = value.decode(errors="replace")
    if command[:2] == ["ps", "-axo"]:
        value = "\n".join(line for line in (value or "").splitlines()
                          if "docker" in line.lower() or "containerd" in line.lower())
    # Docker connection errors can contain credentials from a host override.
    value = re.sub(r"(\w+://)[^/\s@]+@", r"\1[redacted]@", value or "")
    return re.sub(r"(\w+://[^\s?#]+)[?#]\S*", r"\1[redacted-parameters]", value)[-4096:]


def command_step(log, phase, command, timeout, stream=False):
    started = time.monotonic()
    event(log, phase, "start", command=command, timeout_seconds=timeout)
    try:
        output = dict(stdout=log, stderr=subprocess.STDOUT) if stream else dict(capture_output=True)
        result = subprocess.run(command, text=True, timeout=timeout,
                                start_new_session=True, check=False, **output)
    except subprocess.TimeoutExpired as error:
        error.phase = phase
        event(log, phase, "timeout", command=command,
              elapsed_seconds=round(time.monotonic() - started, 3),
              stdout=excerpt(error.stdout, command), stderr=excerpt(error.stderr))
        raise
    except OSError as error:
        event(log, phase, "error", command=command, error=excerpt(str(error)))
        raise
    event(log, phase, "end", command=command, exit_code=result.returncode,
          elapsed_seconds=round(time.monotonic() - started, 3),
          stdout=excerpt(result.stdout, command), stderr=excerpt(result.stderr))
    return result


def diagnose_timeout(log, deadline):
    # Evidence collection shares the job's remaining budget and never retries tests.
    deadline = min(time.monotonic() + 8, deadline) if deadline is not None else time.monotonic() + 8
    host = urlsplit(os.environ.get("DOCKER_HOST", ""))
    event(log, "diagnostic", "context", docker=shutil.which("docker"),
          host_transport=host.scheme or "configured-context", host=host.hostname,
          context_override=os.environ.get("DOCKER_CONTEXT"), load_average=os.getloadavg())
    commands = [
        ["ps", "-axo", "pid=,stat=,%cpu=,rss=,comm="],
        ["docker", "--version"],
        ["docker", "context", "show"],
        ["docker", "version", "--format", "{{.Server.Version}}"],
    ]
    for command in commands:
        timeout = min(2, deadline - time.monotonic())
        if timeout <= 0:
            event(log, "diagnostic", "budget_exhausted")
            break
        try:
            result = command_step(log, "diagnostic", command, timeout)
            if command[1] == "version" and result.returncode == 0:
                commands.append(["docker", "ps", "-aq", "--filter", "label=" + LABEL])
        except (OSError, subprocess.SubprocessError):
            pass


def process_start(pid):
    result = subprocess.run(["ps", "-p", str(pid), "-o", "lstart="],
                            capture_output=True, text=True, check=False, timeout=5)
    return result.stdout.strip()


def group_alive(pgid):
    result = subprocess.run(["ps", "-axo", "pgid=,stat="],
                            capture_output=True, text=True, check=True, timeout=5)
    return any(fields[0] == str(pgid) and not fields[1].startswith("Z")
               for line in result.stdout.splitlines() if len(fields := line.split()) == 2)


def stop_group(record):
    if not record.get("pgid"):
        return
    pgid = record["pgid"]
    if not isinstance(pgid, int) or pgid <= 1 or pgid == os.getpgrp():
        raise RuntimeError("Invalid recorded test process group")
    identity = process_start(pgid)
    if identity and identity != record["process_start"]:
        raise RuntimeError("Recorded test PID was reused; refusing to stop it")
    for sig in (signal.SIGTERM, signal.SIGKILL):
        if not group_alive(pgid):
            return
        try:
            os.killpg(pgid, sig)
        except ProcessLookupError:
            return
        until = time.monotonic() + 5
        while group_alive(pgid) and time.monotonic() < until:
            time.sleep(0.1)
    if group_alive(pgid):
        raise RuntimeError("Previous test process group is still running")


def cleanup_containers(log, phase="preparation", deadline=None):
    command = ["docker", "ps", "-aq", "--filter", "label=" + LABEL]
    def run(command, stream=False):
        try:
            return command_step(log, phase, command, 20, stream)
        except subprocess.TimeoutExpired:
            try:
                diagnose_timeout(log, deadline)
            except (OSError, ValueError, subprocess.SubprocessError) as error:
                event(log, "diagnostic", "error", error=excerpt(str(error)))
            raise
    def containers():
        result = run(command)
        result.check_returncode()
        return result.stdout.split()
    ids = containers()
    if ids:
        print("Recovering owned test containers: " + ", ".join(ids), file=log, flush=True)
        for container in ids:
            run(["docker", "logs", container], stream=True)
        run(["docker", "rm", "--force", *ids])
        until = time.monotonic() + CLEANUP_SECONDS
        remaining = containers()
        while remaining and time.monotonic() < until:
            time.sleep(0.1)
            remaining = containers()
        if remaining:
            raise RuntimeError("Owned test containers remain; no new test may start")


class LocalSlot:
    def __init__(self, log, path=LOCK_PATH):
        self.log, self.path = log, path
        self.file = None
        self.process = None
        self.record = {}
        self.wait_seconds = 0
        self.acquired_at = None
        self.budget = None
        self.deadline = None
        self.phase = "lock_wait"
        self.command_started_at = None

    def save(self, record):
        self.file.seek(0)
        self.file.truncate()
        json.dump(record, self.file)
        self.file.flush()
        os.fsync(self.file.fileno())
        self.record = record

    def acquire(self):
        self.path.parent.mkdir(parents=True, exist_ok=True)
        self.file = self.path.open("a+")
        started = time.monotonic()
        event(self.log, "lock_wait", "start")
        try:
            try:
                fcntl.flock(self.file, fcntl.LOCK_EX | fcntl.LOCK_NB)
            except BlockingIOError:
                print("Waiting for the local Docker test lock", file=self.log, flush=True)
                fcntl.flock(self.file, fcntl.LOCK_EX)
            self.acquired_at = time.monotonic()
            self.wait_seconds = round(self.acquired_at - started, 3)
            self.deadline = None if self.budget is None else self.acquired_at + self.budget
            event(self.log, "lock_wait", "end", elapsed_seconds=self.wait_seconds)
            self.phase = "preparation"
            self.file.seek(0)
            previous = self.file.read()
            self.record = json.loads(previous) if previous else {}
            event(self.log, "preparation", "start",
                  previous_owner_pid=self.record.get("owner_pid"), previous_pgid=self.record.get("pgid"))
            stop_group(self.record)
            cleanup_containers(self.log, self.phase, self.deadline)
            event(self.log, "preparation", "end",
                  elapsed_seconds=round(time.monotonic() - self.acquired_at, 3))
            self.save({"owner_pid": os.getpid()})
            print("Local Docker test lock acquired (wait %.3f s)" % self.wait_seconds,
                  file=self.log, flush=True)
        except BaseException as error:
            error.phase = getattr(error, "phase", self.phase)
            self.file.close()
            self.file = None
            raise

    def start(self, command, **kwargs):
        self.phase = "execution"
        self.command_started_at = time.monotonic()
        event(self.log, self.phase, "start", command=command)
        read_fd, write_fd = os.pipe()
        try:
            env = dict(os.environ, MICROCOL_LOCAL_TEST_OWNER=OWNER,
                       MICROCOL_TEST_PROCESS_GROUP="1")
            self.process = subprocess.Popen(
                [sys.executable, str(Path(__file__).resolve()), "--gated", str(read_fd), *command],
                pass_fds=(read_fd,), start_new_session=True, env=env, **kwargs)
            self.save({"owner_pid": os.getpid(), "pgid": self.process.pid,
                       "process_start": process_start(self.process.pid)})
            # A child may execute only after its recovery identity is durable.
            os.write(write_fd, b"1")
            return self.process
        finally:
            os.close(read_fd)
            os.close(write_fd)

    def close(self):
        if self.file is None:
            return
        try:
            stop_group(self.record)
            if self.process is not None:
                self.process.wait(timeout=5)
                event(self.log, "execution", "end", exit_code=self.process.returncode,
                      elapsed_seconds=round(time.monotonic() - self.command_started_at, 3))
            self.phase = "cleanup"
            cleanup_containers(self.log, self.phase, self.deadline)
            self.save({})
        finally:
            self.file.close()
            self.file = None


def main(arguments):
    if not arguments or arguments == ["--"]:
        raise ValueError("A test command is required")
    if arguments[0] == "--gated":
        fd = int(arguments[1])
        ready = os.read(fd, 1)
        os.close(fd)
        if ready != b"1":
            return 125
        os.execvp(arguments[2], arguments[2:])
    budget = None
    if arguments[0] == "--budget":
        if len(arguments) < 3:
            raise ValueError("Budget and test command are required")
        budget = float(arguments[1])
        if not math.isfinite(budget) or budget <= 0:
            raise ValueError("Budget must be finite and positive")
        arguments = arguments[2:]
    command = arguments[1:] if arguments[0] == "--" else arguments
    if not command:
        raise ValueError("A test command is required")
    def cancel(*_):
        raise InterruptedError("Local test cancelled")
    for sig in (signal.SIGINT, signal.SIGTERM):
        signal.signal(sig, cancel)
    slot = LocalSlot(sys.stdout)
    slot.budget = budget
    try:
        slot.acquire()
        timeout = None if budget is None else budget - (time.monotonic() - slot.acquired_at)
        if timeout is not None and timeout <= 0:
            raise TimeoutError("Local test budget exhausted during recovery")
        result = slot.start(command).wait(timeout=timeout)
    finally:
        for sig in (signal.SIGINT, signal.SIGTERM):
            signal.signal(sig, signal.SIG_IGN)
        slot.close()
    if budget is not None and time.monotonic() - slot.acquired_at > budget:
        raise TimeoutError("Local test budget exceeded including cleanup")
    return result


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv[1:]))
    except (OSError, RuntimeError, ValueError, subprocess.SubprocessError) as error:
        print("Local Docker test: " + str(error), file=sys.stderr)
        sys.exit(1)
