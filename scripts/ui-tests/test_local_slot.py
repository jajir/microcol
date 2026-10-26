"""Real process/lock tests with a fake Docker daemon; no desktop or Docker needed."""
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import threading
import time
import unittest
from unittest.mock import patch

import local_slot

SCRIPT = Path(local_slot.__file__).resolve()
LAUNCH = """
import sys
from pathlib import Path
sys.path.insert(0, sys.argv[1])
import local_slot
original = local_slot.LocalSlot
local_slot.CLEANUP_SECONDS = 0.2
local_slot.LocalSlot = lambda log: original(log, Path(sys.argv[2]))
sys.exit(local_slot.main(sys.argv[3:]))
"""
DOCKER = """
import json, os, sys, time
from pathlib import Path
p = Path(os.environ['SLOT_DOCKER_STATE'])
state = json.loads(p.read_text())
a = sys.argv[1:]
if a[0] == 'ps':
    counter = p.with_suffix('.count')
    count = int(counter.read_text()) if counter.exists() else 0
    counter.write_text(str(count + 1))
    if count >= int(os.getenv('SLOT_DELAY_AFTER', '0')):
        time.sleep(float(os.getenv('SLOT_PS_DELAY', '0')))
    assert a[1:3] == ['-aq', '--filter']
    assert a[3] == 'label=org.microcol.local-test=' + str(os.getuid())
    print('owned' if state.get('owned') else '')
elif a[0] == 'logs':
    assert a[1] == 'owned'
    print('previous test log')
elif a[0] == 'rm':
    assert a == ['rm', '--force', 'owned']
    if not state.get('refuse'):
        state.pop('owned', None)
        p.write_text(json.dumps(state))
else:
    raise AssertionError(a)
"""


class LocalSlotTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory(prefix="microcol slot ")
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.lock = self.root / "shared.lock"
        self.state = self.root / "docker.json"
        self.state.write_text('{"foreign": true}')
        docker = self.root / "docker"
        docker.write_text("#!" + sys.executable + "\n" + DOCKER)
        docker.chmod(0o755)
        self.env = dict(os.environ, PATH=str(self.root) + os.pathsep + os.environ["PATH"],
                        SLOT_DOCKER_STATE=str(self.state))
        self.processes = []
        self.addCleanup(self.cleanup)

    def cleanup(self):
        for process in self.processes:
            if process.poll() is None:
                process.terminate()
            process.wait(timeout=10)
        if self.lock.exists():
            record = json.loads(self.lock.read_text() or '{}')
            local_slot.stop_group(record)

    def start(self, name, code, budget=None):
        cwd = self.root / name
        cwd.mkdir()
        log = (cwd / "log").open("w")
        self.addCleanup(log.close)
        options = [] if budget is None else ["--budget", str(budget), "--"]
        process = subprocess.Popen(
            [sys.executable, "-c", LAUNCH, str(SCRIPT.parent), str(self.lock),
             *options, sys.executable, "-c", code], cwd=cwd, env=self.env, stdout=log, stderr=log)
        self.processes.append(process)
        return process, cwd / "log"

    def wait_for(self, predicate):
        until = time.monotonic() + 5
        while not predicate() and time.monotonic() < until:
            time.sleep(0.02)
        self.assertTrue(predicate())

    def holder(self):
        ready = self.root / "ready"
        release = self.root / "release"
        code = ("import time\nfrom pathlib import Path\nPath(%r).touch()\n"
                "while not Path(%r).exists(): time.sleep(.02)\n" % (str(ready), str(release)))
        process, log = self.start("first", code)
        self.wait_for(ready.exists)
        return process, log, release

    def test_worktrees_wait_and_lock_file_is_reusable(self):
        first, _, release = self.holder()
        marker = self.root / "second-started"
        second, log = self.start("second", "from pathlib import Path; Path(%r).touch()" % str(marker))
        self.wait_for(lambda: "Waiting" in log.read_text())
        self.assertFalse(marker.exists())
        release.touch()
        self.assertEqual(first.wait(timeout=10), 0)
        self.assertEqual(second.wait(timeout=10), 0)
        self.assertTrue(marker.exists())
        self.assertEqual(json.loads(self.lock.read_text()), {})
        self.assertEqual(json.loads(self.state.read_text()), {"foreign": True})

    def test_waiting_cancellation_does_not_touch_holder(self):
        first, _, release = self.holder()
        second, log = self.start("second", "raise AssertionError('must not start')")
        self.wait_for(lambda: "Waiting" in log.read_text())
        second.terminate()
        self.assertNotEqual(second.wait(timeout=5), 0)
        self.assertIsNone(first.poll())
        self.assertEqual(json.loads(self.lock.read_text())["owner_pid"], first.pid)
        release.touch()
        self.assertEqual(first.wait(timeout=10), 0)

    def test_sigkill_reclaims_child_group_and_only_owned_containers(self):
        first, _, _ = self.holder()
        pgid = json.loads(self.lock.read_text())["pgid"]
        self.state.write_text('{"owned": true, "foreign": true}')
        first.kill()
        first.wait(timeout=5)
        self.assertTrue(local_slot.group_alive(pgid))
        second, log = self.start("second", "print('new test')")
        self.assertEqual(second.wait(timeout=10), 0, log.read_text())
        self.assertFalse(local_slot.group_alive(pgid))
        self.assertIn("previous test log", log.read_text())
        self.assertEqual(json.loads(self.state.read_text()), {"foreign": True})

    def test_cleanup_failure_blocks_execution(self):
        self.state.write_text('{"owned": true, "foreign": true, "refuse": true}')
        marker = self.root / "must-not-start"
        process, _ = self.start("attempt", "from pathlib import Path; Path(%r).touch()" % str(marker))
        self.assertNotEqual(process.wait(timeout=10), 0)
        self.assertFalse(marker.exists())
        self.assertTrue(json.loads(self.state.read_text())["owned"])

    def test_child_does_not_execute_without_durable_registration(self):
        with patch.dict(os.environ, self.env), (self.root / "log").open("w") as log:
            slot = local_slot.LocalSlot(log, self.lock)
            slot.acquire()
            marker = self.root / "must-not-start"
            try:
                with patch.object(slot, "save", side_effect=OSError("disk error")):
                    with self.assertRaises(OSError):
                        slot.start([sys.executable, "-c", "from pathlib import Path; Path(%r).touch()" % str(marker)])
                self.assertEqual(slot.process.wait(timeout=5), 125)
                self.assertFalse(marker.exists())
            finally:
                slot.close()

    def test_reused_pid_is_not_killed(self):
        with patch.object(local_slot, "process_start", return_value="different"), \
                patch.object(local_slot.os, "killpg") as kill:
            with self.assertRaisesRegex(RuntimeError, "reused"):
                local_slot.stop_group({"pgid": 999999, "process_start": "old"})
            kill.assert_not_called()

    def test_budget_includes_recovery_before_command_start(self):
        self.env["SLOT_PS_DELAY"] = "0.2"
        marker = self.root / "must-not-start"
        process, log = self.start("attempt", "from pathlib import Path; Path(%r).touch()" % str(marker), budget=0.1)
        self.assertNotEqual(process.wait(timeout=5), 0)
        self.assertFalse(marker.exists())
        self.assertIn("exhausted during recovery", log.read_text())

    def test_success_cannot_exceed_budget_during_final_cleanup(self):
        self.env.update(SLOT_PS_DELAY="0.4", SLOT_DELAY_AFTER="1")
        process, log = self.start("attempt", "print('command passed')", budget=0.3)
        self.assertNotEqual(process.wait(timeout=5), 0)
        self.assertIn("command passed", log.read_text())
        self.assertIn("exceeded including cleanup", log.read_text())

    def test_wait_is_excluded_from_completion_watchdog(self):
        sys.path.insert(0, str(SCRIPT.parents[1] / "codex"))
        import run
        first, _, release = self.holder()
        directory = self.root / "completion"
        directory.mkdir()
        record = {"kind": "verify", "timeout_seconds": 0.5, "cwd": str(self.root),
                  "revision": "test", "command": [sys.executable, "-c", "print('finished')"]}
        (directory / "request.json").write_text(json.dumps(record))
        timer = threading.Timer(0.8, release.touch)
        timer.start()
        try:
            with patch.dict(os.environ, self.env), patch.object(run, "notify"), patch.object(
                    run, "LocalSlot", side_effect=lambda log: local_slot.LocalSlot(log, self.lock)):
                result = run.supervise(directory)
            self.assertEqual(result["status"], "success", result)
            self.assertGreater(result["wait_seconds"], 0.5)
            self.assertLess(result["elapsed_seconds"], 0.5)
            self.assertEqual(first.wait(timeout=10), 0)
        finally:
            timer.join()


if __name__ == "__main__":
    unittest.main()
