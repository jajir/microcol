#!/usr/bin/env python3
"""Exercise SIGKILL recovery against Docker while the outer test job holds its slot."""
import json
from pathlib import Path
import subprocess
import sys
import tempfile
import time
import uuid

import local_slot


def configure(token):
    local_slot.OWNER = token
    local_slot.LABEL = "org.microcol.local-test=" + token


def holder(directory, image, token):
    configure(token)
    with (directory / "holder.log").open("w") as log:
        slot = local_slot.LocalSlot(log, directory / "test.lock")
        try:
            slot.acquire()
            command = ("import subprocess,time; from pathlib import Path; "
                       "subprocess.run(%r,check=True); Path(%r).touch(); time.sleep(60)" % (
                           ["docker", "run", "-d", "--platform", "linux/amd64", "--label", local_slot.LABEL,
                            "--name", token, "--entrypoint", "/bin/sh", image,
                            "-c", "echo retained-orphan-log; sleep 60"], str(directory / "ready")))
            slot.start([sys.executable, "-c", command], stdout=log, stderr=log).wait()
        finally:
            slot.close()


def check(image):
    directory = Path(tempfile.mkdtemp(prefix="lock-recovery-", dir="target/docker-ui-runner-checks")).resolve()
    token = "microcol-slot-check-" + uuid.uuid4().hex[:12]
    foreign = token + "-unlabelled"
    configure(token)
    owner = None
    try:
        subprocess.run(["docker", "create", "--platform", "linux/amd64", "--name", foreign, "--entrypoint", "/bin/true", image],
                       check=True, stdout=subprocess.DEVNULL, timeout=20)
        owner = subprocess.Popen([sys.executable, str(Path(__file__).resolve()), "--holder",
                                  str(directory), image, token])
        deadline = time.monotonic() + 20
        while not (directory / "ready").exists() and time.monotonic() < deadline:
            if owner.poll() is not None:
                raise RuntimeError("Holder failed; inspect " + str(directory / "holder.log"))
            time.sleep(0.05)
        assert (directory / "ready").exists(), "Container did not start"
        pgid = json.loads((directory / "test.lock").read_text())["pgid"]
        owner.kill()
        owner.wait(timeout=5)
        assert local_slot.group_alive(pgid), "The child must survive its holder for this check"
        with (directory / "recovery.log").open("w") as log:
            recovery = local_slot.LocalSlot(log, directory / "test.lock")
            try:
                recovery.acquire()
                assert not local_slot.group_alive(pgid)
                ids = subprocess.check_output(["docker", "ps", "-aq", "--filter",
                                               "label=" + local_slot.LABEL], text=True, timeout=20)
                assert not ids.strip(), "Orphan container remains"
                subprocess.run(["docker", "inspect", foreign], check=True,
                               stdout=subprocess.DEVNULL, timeout=20)
            finally:
                recovery.close()
        assert "retained-orphan-log" in (directory / "recovery.log").read_text()
        (directory / "result.json").write_text(json.dumps({"status": "passed", "foreign_preserved": True}) + "\n")
        print("PASS: SIGKILL releases lock, child group and Docker orphan recovered, foreign container preserved: " + str(directory))
    finally:
        if owner is not None and owner.poll() is None:
            owner.kill()
            owner.wait(timeout=5)
        state = directory / "test.lock"
        if state.exists():
            local_slot.stop_group(json.loads(state.read_text() or "{}"))
        subprocess.run(["docker", "rm", "-f", token, foreign], stdout=subprocess.DEVNULL,
                       stderr=subprocess.DEVNULL, timeout=20)


if __name__ == "__main__":
    if sys.argv[1] == "--holder":
        holder(Path(sys.argv[2]), sys.argv[3], sys.argv[4])
    else:
        check(sys.argv[1])
