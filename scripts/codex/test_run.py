"""Portable process tests; no real Codex, Docker, GitHub or worker inference."""

import json
import os
from pathlib import Path
import signal
import subprocess
import sys
import tempfile
import time
import unittest
from unittest.mock import patch

import run
import local_slot


THREAD = "01a15004-8364-7c93-baff-c384e0e07ebf"
SHA = "a" * 40
SCRIPT = Path(run.__file__).resolve()


class CompletionTest(unittest.TestCase):
    def test_ux_dispatch_is_separate_from_regression(self):
        args = run.parse_args(["--thread", THREAD, "--timeout", "600", "ux", "--", "check"])
        with patch.object(run, "installed", side_effect=lambda name: "/bin/" + name), \
                patch.object(run.subprocess, "check_output", return_value=SHA):
            record = run.request(args, "20261028T000000Z-example")
        self.assertEqual("ux", record["kind"])
        self.assertEqual("/bin/docker", record["docker"])
        self.assertIn("microcol-ux-test/run.py", record["command"][1])
        self.assertEqual("check", record["command"][-1])
        self.assertNotIn("acceptance_seconds", record)

    def setUp(self):
        temporary = tempfile.TemporaryDirectory(prefix="completion test ")
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name).resolve()
        self.directory = self.root / "evidence"
        self.directory.mkdir()
        self.events = self.root / "events.jsonl"
        self.bin = self.root / "bin"
        self.bin.mkdir()
        self.env = patch.dict(os.environ, {"PROBE_EVENTS": str(self.events),
                                          "PATH": str(self.bin) + os.pathsep + os.environ["PATH"]})
        self.env.start()
        self.addCleanup(self.env.stop)
        self.codex = self.stub("codex", '''
assert sys.argv[1:4] == ['queue', '--thread', sys.argv[3]]
assert sys.argv[4] == '--message' and len(sys.argv) == 6
message = sys.argv[5]
result = Path(message.split('Evidence: ', 1)[1].split('. Verification', 1)[0])
event.update(thread=sys.argv[3], result_exists=result.is_file())
emit()
time.sleep(float(os.getenv('PROBE_QUEUE_SLEEP', '0')))
raise SystemExit(int(os.getenv('PROBE_QUEUE_EXIT', '0')))
''')
        self.record = {"kind": "worker", "thread": THREAD, "timeout_seconds": 3,
                       "cwd": str(self.root), "codex": str(self.codex), "run_id": "test-123",
                       "revision": SHA, "working_tree_status": "",
                       "worker_attempt": str(self.root / "attempt-0"),
                       "command": [sys.executable, "-c", "print('complete')"]}

    def stub(self, name, source):
        path = self.bin / name
        path.write_text("#!" + sys.executable + "\n" + '''
import json, os, sys, time
from pathlib import Path
event = {'tool': Path(sys.argv[0]).name, 'args': sys.argv[1:]}
def emit():
    with open(os.environ['PROBE_EVENTS'], 'a') as output:
        output.write(json.dumps(event) + '\\n')
''' + source)
        path.chmod(0o755)
        return path

    def observed(self):
        return [json.loads(line) for line in self.events.read_text().splitlines()]

    def execute(self, **changes):
        self.record.update(changes)
        run.write_json(self.directory / "request.json", self.record)
        with patch("local_slot.cleanup_containers"), patch.object(
                run, "LocalSlot", side_effect=lambda log: local_slot.LocalSlot(log, self.root / "slot.lock")):
            result = run.supervise(self.directory)
        calls = [e for e in self.observed() if e["tool"] == "codex"]
        self.assertEqual(len(calls), 1)
        self.assertEqual(calls[0]["thread"], THREAD)
        self.assertTrue(calls[0]["result_exists"])
        self.assertEqual(json.loads((self.directory / "result.json").read_text()), result)
        return result

    def test_preparation_timeout_notifies_without_starting_test_command(self):
        self.record.update(kind="verify", timeout_seconds=600)
        run.write_json(self.directory / "request.json", self.record)
        with patch("local_slot.cleanup_containers", side_effect=subprocess.TimeoutExpired(["docker", "ps"], 20)), \
                patch.object(run, "LocalSlot", side_effect=lambda log: local_slot.LocalSlot(log, self.root / "slot.lock")):
            result = run.supervise(self.directory)
        self.assertEqual(result["status"], "timed_out")
        self.assertEqual(result["failure_phase"], "preparation")
        self.assertEqual(result["step_timeout_seconds"], 20)
        self.assertFalse(result["command_started"])
        notice = json.loads((self.directory / "notification.json").read_text())
        self.assertIn("Phase: preparation. Step exceeded 20 s. Test command did not start.", notice["message"])
        self.assertEqual(notice["status"], "queued")

    def test_cleanup_timeout_records_phase_after_command_completion(self):
        self.record.update(kind="verify", timeout_seconds=600)
        run.write_json(self.directory / "request.json", self.record)
        with patch("local_slot.cleanup_containers", side_effect=[None, subprocess.TimeoutExpired(["docker", "ps"], 20)]), \
                patch.object(run, "LocalSlot", side_effect=lambda log: local_slot.LocalSlot(log, self.root / "slot.lock")):
            result = run.supervise(self.directory)
        self.assertEqual(result["status"], "failed")
        self.assertEqual(result["failure_phase"], "cleanup")
        self.assertEqual(result["step_timeout_seconds"], 20)
        self.assertTrue(result["command_started"])

    def test_cleanup_timeout_preserves_original_execution_failure(self):
        for code, budget, timed_out in [("raise SystemExit(7)", 3, False),
                                      ("import time; time.sleep(10)", 0.1, True)]:
            with self.subTest(timed_out=timed_out):
                self.record.update(kind="verify", timeout_seconds=budget,
                                   command=[sys.executable, "-c", code])
                run.write_json(self.directory / "request.json", self.record)
                with patch("local_slot.cleanup_containers", side_effect=[None, subprocess.TimeoutExpired(["docker", "ps"], 20)]), \
                        patch.object(run, "LocalSlot", side_effect=lambda log: local_slot.LocalSlot(log, self.root / "slot.lock")):
                    result = run.supervise(self.directory)
                self.assertEqual(result["failure_phase"], "execution")
                self.assertTrue(result["command_started"])
                self.assertTrue(result["cleanup_errors"])
                if timed_out:
                    self.assertLessEqual(result["step_timeout_seconds"], budget)
                else:
                    self.assertNotIn("step_timeout_seconds", result)

    def test_success_keeps_arguments_and_evidence(self):
        with patch.dict(os.environ, {"CODEX_THREAD": "wrong-task"}):
            result = self.execute(command=[sys.executable, "-c", "import sys; print(sys.argv[1])",
                                           "two words; $(never execute)"])
        self.assertEqual((result["status"], result["exit_code"]), ("success", 0))
        self.assertIn("two words; $(never execute)", (self.directory / "command.log").read_text())
        message = self.observed()[0]["args"][-1]
        self.assertIn("Verification and review remain pending", message)

    def test_nonzero_exit_is_not_success(self):
        result = self.execute(command=[sys.executable, "-c", "raise SystemExit(7)"])
        self.assertEqual((result["status"], result["exit_code"]), ("failed", 7))

    def test_start_failure_still_notifies(self):
        result = self.execute(command=[str(self.root / "missing-command")])
        self.assertEqual(result["status"], "failed")
        self.assertIsNone(result["exit_code"])
        self.assertIn("missing-command", result["error"])

    def test_timeout_terminates_child(self):
        result = self.execute(timeout_seconds=0.1,
                              command=[sys.executable, "-c", "import time; time.sleep(20)"])
        self.assertEqual(result["status"], "timed_out")
        self.assertLess(result["exit_code"], 0)
        self.assertLess(result["elapsed_seconds"], 2)

    def test_worker_internal_timeout_uses_only_new_attempt(self):
        new = Path(self.record["worker_attempt"])
        new.mkdir()
        run.write_json(new / "execution.json", {"timed_out": True})
        result = self.execute(command=[sys.executable, "-c", "raise SystemExit(1)"])
        self.assertEqual(result["status"], "timed_out")
        self.events.unlink()
        self.record["worker_attempt"] = str(self.root / "attempt-1")
        self.assertEqual(self.execute(command=[sys.executable, "-c", "raise SystemExit(1)"])
                         ["status"], "failed")

    def test_queue_failure_preserves_successful_command_outcome(self):
        with patch.dict(os.environ, {"PROBE_QUEUE_EXIT": "9"}):
            result = self.execute()
        self.assertEqual(result["status"], "success")
        delivery = json.loads((self.directory / "notification.json").read_text())
        self.assertEqual((delivery["status"], delivery["exit_code"]), ("failed", 9))

    def test_queue_timeout_is_ambiguous_without_retry(self):
        with patch.dict(os.environ, {"PROBE_QUEUE_SLEEP": "5"}), patch.object(run, "QUEUE_SECONDS", 0.5):
            self.execute()
        self.assertEqual(json.loads((self.directory / "notification.json").read_text())["status"],
                         "unknown")

    def test_cancellation_escalates_when_child_ignores_term(self):
        ready = self.root / "child-ready"
        self.record["command"] = [sys.executable, "-c",
                                  "import signal,time,os; from pathlib import Path; "
                                  "signal.signal(signal.SIGTERM,signal.SIG_IGN); "
                                  "Path(" + repr(str(ready)) + ").write_text(str(os.getpid())); "
                                  "time.sleep(20)"]
        run.write_json(self.directory / "request.json", self.record)
        program = ("import sys; from pathlib import Path; sys.path.insert(0, "
                   + repr(str(SCRIPT.parent)) + "); import run; run.GRACE_SECONDS=0.05; "
                   "run.supervise(Path(" + repr(str(self.directory)) + "))")
        with subprocess.Popen([sys.executable, "-c", program], stdout=subprocess.PIPE,
                              stderr=subprocess.PIPE) as process:
            deadline = time.monotonic() + 3
            while not ready.exists() and time.monotonic() < deadline:
                time.sleep(0.01)
            self.assertTrue(ready.exists())
            process.send_signal(signal.SIGTERM)
            stdout, stderr = process.communicate(timeout=4)
            self.assertEqual(process.returncode, 0, stderr)
        result = json.loads((self.directory / "result.json").read_text())
        self.assertEqual(result["status"], "cancelled")
        self.assertEqual(result["exit_code"], -signal.SIGKILL)
        self.assertEqual(len(self.observed()), 1)
        with self.assertRaises(ProcessLookupError):
            os.kill(int(ready.read_text()), 0)

    def ci(self):
        gh = self.stub("gh", '''
emit()
marker = Path(os.environ['PROBE_EVENTS'] + '.watched')
if sys.argv[2] == 'watch':
    marker.touch()
    raise SystemExit(int(os.getenv('PROBE_WATCH_EXIT', '0')))
assert sys.argv[2] == 'view'
print(json.dumps({'databaseId': 42, 'headSha': os.getenv('PROBE_SHA', 'a'*40),
                  'status': 'completed' if marker.exists() else 'in_progress',
                  'conclusion': os.getenv('PROBE_CONCLUSION', 'success') if marker.exists() else '',
                  'url': 'https://example.invalid/run/42'}))
''')
        self.record.update(kind="ci", gh=str(gh), ci_run="42", ci_commit=SHA, ci_repo="owner/repo",
                           command=[str(gh), "run", "watch", "42", "--repo", "owner/repo",
                                    "--exit-status", "--interval", "30"])

    def test_ci_success_checks_identity_before_and_after_watch(self):
        self.ci()
        self.assertEqual(self.execute()["status"], "success")
        self.assertEqual([e["args"][1] for e in self.observed() if e["tool"] == "gh"],
                         ["view", "watch", "view"])
        self.assertTrue((self.directory / "ci-after.json").is_file())

    def test_wrong_ci_commit_does_not_watch(self):
        self.ci()
        with patch.dict(os.environ, {"PROBE_SHA": "b" * 40}):
            result = self.execute()
        self.assertEqual(result["status"], "failed")
        self.assertIn("identity mismatch", result["error"])
        self.assertEqual([e["args"][1] for e in self.observed() if e["tool"] == "gh"], ["view"])

    def test_ci_cancelled_final_is_failure_even_when_watch_returns_zero(self):
        self.ci()
        with patch.dict(os.environ, {"PROBE_CONCLUSION": "cancelled"}):
            self.assertEqual(self.execute()["status"], "failed")

    def test_ci_watch_error_cannot_be_hidden_by_successful_metadata(self):
        self.ci()
        with patch.dict(os.environ, {"PROBE_WATCH_EXIT": "1"}):
            self.assertEqual(self.execute()["status"], "failed")

    def test_ci_deadline_never_cancels_remote_run(self):
        self.ci()
        self.stub("gh", "emit(); time.sleep(20)\n")
        # Allow the stub to start on a busy host before testing its timeout.
        result = self.execute(timeout_seconds=2)
        self.assertEqual(result["status"], "timed_out")
        self.assertEqual([e["args"][1] for e in self.observed() if e["tool"] == "gh"], ["view"])

    def test_docker_timeout_stops_only_owned_container(self):
        docker = self.stub("docker", '''
emit()
if sys.argv[1] == 'compose':
    time.sleep(20)
elif sys.argv[1] in ('stop', 'rm'):
    assert sys.argv[-1] == 'microcol-test-123'
else:
    raise SystemExit(10)
''')
        result = self.execute(kind="docker", docker=str(docker), container="microcol-test-123",
                              timeout_seconds=1, command=[str(docker), "compose"])
        self.assertEqual(result["status"], "timed_out")
        self.assertEqual(result["cleanup_errors"], [])
        calls = [e["args"] for e in self.observed() if e["tool"] == "docker"]
        self.assertEqual(calls, [["compose"], ["stop", "--timeout", "10", "microcol-test-123"],
                                 ["rm", "--force", "microcol-test-123"]])

    def test_docker_client_failure_also_cleans_up_and_records_errors(self):
        docker = self.stub("docker", "emit(); print('daemon unavailable', file=sys.stderr); raise SystemExit(1)\n")
        result = self.execute(kind="docker", docker=str(docker), container="microcol-owned",
                              command=[str(docker), "compose"])
        self.assertEqual(result["status"], "failed")
        self.assertEqual(len(result["cleanup_errors"]), 2)
        self.assertTrue(all(e["args"][-1] == "microcol-owned" for e in self.observed()
                            if e["tool"] == "docker" and e["args"][0] != "compose"))

    def test_docker_cleanup_waits_for_compose_automatic_removal(self):
        docker = self.stub("docker", '''
emit()
if sys.argv[1] == 'compose':
    raise SystemExit(7)
if sys.argv[1] == 'rm':
    marker = Path(os.environ['PROBE_EVENTS'] + '.removing')
    if not marker.exists():
        marker.touch()
        print('removal of container microcol-owned is already in progress', file=sys.stderr)
    else:
        print('error: no such object: microcol-owned', file=sys.stderr)
    raise SystemExit(1)
''')
        result = self.execute(kind="docker", docker=str(docker), container="microcol-owned",
                              command=[str(docker), "compose"])
        self.assertEqual((result["status"], result["exit_code"]), ("failed", 7))
        self.assertEqual(result["cleanup_errors"], [])
        self.assertEqual(sum(e["args"][0] == "rm" for e in self.observed()), 2)

    def test_validation_rejects_bad_timeout_and_identity(self):
        for value in ["0", "-1", "nan", "inf"]:
            with self.subTest(timeout=value), self.assertRaises(ValueError):
                run.parse_args(["--thread", THREAD, "--timeout", value, "docker"])
        for thread in ["", "not-a-uuid"]:
            with self.assertRaises(ValueError):
                run.parse_args(["--thread", thread, "--timeout", "1", "docker"])
        for run_id, commit, repo in [("0", SHA, "owner/repo"), ("42", "abc", "owner/repo"),
                                     ("42", SHA, "--repo")]:
            with self.assertRaises((ValueError, SystemExit)):
                run.parse_args(["--thread", THREAD, "--timeout", "1", "ci", run_id,
                                "--commit", commit, "--repo=" + repo])

    def test_adapters_preserve_paths_spaces_and_worker_attempt(self):
        job = self.root / "worker job"
        job.mkdir()
        (job / "attempt-0").mkdir()
        args = run.parse_args(["--thread", THREAD, "--timeout", "1", "worker", str(job),
                               "--profile", str(self.root / "a profile"),
                               "--escalate-reason", "specific diagnosis"])
        with patch.object(run.subprocess, "check_output", return_value=SHA):
            record = run.request(args, "unique-id")
        self.assertEqual(record["worker_attempt"], str(job / "attempt-1"))
        self.assertEqual(record["command"][-4:], ["--profile", str(self.root / "a profile"),
                                                  "--escalate-reason", "specific diagnosis"])
        self.stub("docker", "raise SystemExit(0)\n")
        args = run.parse_args(["--thread", THREAD, "--timeout", "1", "docker", "--", "-Dsample=two words"])
        with patch.object(run.subprocess, "check_output", return_value=SHA):
            record = run.request(args, "different-id")
        self.assertEqual(record["command"][-2:], ["ui-tests", "-Dsample=two words"])
        self.assertEqual(record["container"], "microcol-different-id")
        args = run.parse_args(["--thread", THREAD, "--timeout", "10800", "verify", "--",
                               "--workers", "2", "--timings", "path with spaces"])
        with patch.object(run.subprocess, "check_output", return_value=SHA):
            record = run.request(args, "verify-id")
        self.assertEqual(record["command"][-4:], ["--workers", "2", "--timings", "path with spaces"])
        self.assertEqual(Path(record["command"][1]).name, "verify.py")
        self.assertNotIn("container", record)

    def test_smoke_entry_points_share_seven_minute_acceptance_budget(self):
        args = run.parse_args(["--thread", THREAD, "--timeout", "480",
                               "verify", "--", "--smoke"])
        with patch.object(run.subprocess, "check_output", return_value=SHA):
            record = run.request(args, "smoke-id")
        self.assertEqual(record["acceptance_seconds"], 420)
        self.assertEqual(record["timeout_seconds"], 480)
        self.assertEqual(record["command"][-1], "--smoke")
        self.stub("python3", "emit()\n")
        wrapper = SCRIPT.parents[1] / "ui-tests/verify.sh"
        subprocess.run(["bash", str(wrapper), "--smoke"], check=True)
        arguments = self.observed()[0]["args"]
        self.assertEqual(Path(arguments[0]).name, "local_slot.py")
        self.assertEqual(arguments[1:4], ["--budget", "420", "--"])
        self.assertEqual(Path(arguments[-2]).name, "verify.py")
        self.assertEqual(arguments[-1], "--smoke")

    def test_dispatch_returns_before_command_finishes(self):
        release = self.root / "release-child"
        self.record["command"] = [sys.executable, "-c",
                                  "import time; from pathlib import Path\n"
                                  "while not Path(" + repr(str(release)) + ").exists(): time.sleep(0.02)"]
        program = ("import sys,json; from pathlib import Path; sys.path.insert(0, "
                   + repr(str(SCRIPT.parent)) + "); import run; run.LOG_ROOT=Path("
                   + repr(str(self.root / "runs")) + "); print(json.dumps(run.dispatch("
                   + repr(self.record) + ")))" )
        launched = subprocess.run([sys.executable, "-c", program], capture_output=True,
                                  text=True, timeout=2, check=True)
        dispatched = json.loads(launched.stdout)
        directory = Path(dispatched["directory"])
        self.assertFalse((directory / "result.json").exists())
        release.touch()
        deadline = time.monotonic() + 4
        while time.monotonic() < deadline:
            notification = directory / "notification.json"
            if notification.exists() and json.loads(notification.read_text())["status"] == "queued":
                break
            time.sleep(0.02)
        self.assertEqual(json.loads((directory / "result.json").read_text())["status"], "success")
        self.assertEqual(json.loads((directory / "notification.json").read_text())["status"], "queued")


if __name__ == "__main__":
    unittest.main()
