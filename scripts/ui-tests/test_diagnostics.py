"""Bounded timeout evidence; all Docker responses are simulated."""
import io
import json
import os
import subprocess
import unittest
from unittest.mock import patch

import local_slot


class DiagnosticTest(unittest.TestCase):
    def setUp(self):
        self.log = io.StringIO()

    def events(self):
        return [json.loads(line[len("local-test "):]) for line in self.log.getvalue().splitlines()
                if line.startswith("local-test ")]

    def test_step_records_phase_output_and_nonzero_exit(self):
        result = subprocess.CompletedProcess(["docker", "ps"], 7, "out", "err")
        with patch.object(local_slot.subprocess, "run", return_value=result):
            self.assertIs(local_slot.command_step(self.log, "preparation", result.args, 20), result)
        start, end = self.events()
        self.assertEqual((start["phase"], start["timeout_seconds"]), ("preparation", 20))
        self.assertEqual((end["exit_code"], end["stdout"], end["stderr"]), (7, "out", "err"))
        self.assertIn("elapsed_seconds", end)

    def test_timeout_preserves_partial_output_and_original_exception(self):
        error = subprocess.TimeoutExpired(["docker", "ps"], 20, b"partial", b"tcp://user:secret@host?token=hidden")
        with patch.object(local_slot.subprocess, "run", side_effect=error):
            with self.assertRaises(subprocess.TimeoutExpired) as caught:
                local_slot.command_step(self.log, "preparation", error.cmd, 20)
        self.assertIs(caught.exception, error)
        self.assertEqual(error.phase, "preparation")
        self.assertEqual(self.events()[-1]["stdout"], "partial")
        self.assertNotIn("secret", self.log.getvalue())
        self.assertNotIn("hidden", self.log.getvalue())

    def test_successful_probe_never_reclassifies_original_timeout(self):
        error = subprocess.TimeoutExpired(["docker", "ps"], 20)
        commands = []
        def run(command, **kwargs):
            commands.append(command)
            if len(commands) == 1:
                raise error
            return subprocess.CompletedProcess(command, 0, "", "")
        with patch.object(local_slot.subprocess, "run", side_effect=run):
            with self.assertRaises(subprocess.TimeoutExpired) as caught:
                local_slot.cleanup_containers(self.log)
        self.assertIs(caught.exception, error)
        self.assertEqual(sum(command[1] == "ps" for command in commands), 2)
        self.assertEqual(self.events()[-1]["phase"], "diagnostic")

    def test_unavailable_server_does_not_repeat_container_query(self):
        commands = []
        def run(command, **kwargs):
            commands.append(command)
            return subprocess.CompletedProcess(command, 1, "", "offline")
        with patch.object(local_slot.subprocess, "run", side_effect=run):
            local_slot.diagnose_timeout(self.log, None)
        self.assertFalse(any(command[1] == "ps" for command in commands))

    def test_probes_share_remaining_job_budget(self):
        now, limits = [100], []
        def run(command, **kwargs):
            limits.append(kwargs["timeout"])
            now[0] += kwargs["timeout"]
            raise subprocess.TimeoutExpired(command, kwargs["timeout"])
        with patch.object(local_slot.time, "monotonic", side_effect=lambda: now[0]), \
                patch.object(local_slot.subprocess, "run", side_effect=run):
            local_slot.diagnose_timeout(self.log, 103)
        self.assertEqual(limits, [2, 1])
        self.assertEqual(self.events()[-1]["state"], "budget_exhausted")

    def test_context_and_process_output_omit_private_data(self):
        def run(command, **kwargs):
            return subprocess.CompletedProcess(command, 0,
                "1 S 0 10 /Applications/Docker.app\n2 S 0 20 /Applications/PrivateApp", "")
        with patch.dict(os.environ, {"DOCKER_HOST": "tcp://user:secret@engine:2376?token=hidden"}), \
                patch.object(local_slot.subprocess, "run", side_effect=run):
            local_slot.diagnose_timeout(self.log, None)
        context = self.events()[0]
        self.assertEqual((context["host_transport"], context["host"]), ("tcp", "engine"))
        process = next(e for e in self.events() if e.get("command", [])[:1] == ["ps"] and e["state"] == "end")
        self.assertIn("Docker.app", process["stdout"])
        self.assertNotIn("PrivateApp", process["stdout"])
        self.assertNotIn("secret", self.log.getvalue())
        self.assertNotIn("hidden", self.log.getvalue())

    def test_partial_process_output_also_omits_unrelated_apps(self):
        command = ["ps", "-axo", "pid=,stat=,%cpu=,rss=,comm="]
        error = subprocess.TimeoutExpired(command, 2,
            b"1 S 0 10 /Applications/Docker.app\n2 S 0 20 /Applications/PrivateApp")
        with patch.object(local_slot.subprocess, "run", side_effect=error), \
                self.assertRaises(subprocess.TimeoutExpired):
            local_slot.command_step(self.log, "diagnostic", command, 2)
        self.assertIn("Docker.app", self.events()[-1]["stdout"])
        self.assertNotIn("PrivateApp", self.log.getvalue())

    def test_collection_error_preserves_failed_preparation(self):
        error = subprocess.TimeoutExpired(["docker", "ps"], 20)
        with patch.object(local_slot.subprocess, "run", side_effect=error), \
                patch.object(local_slot, "diagnose_timeout", side_effect=ValueError("bad context")):
            with self.assertRaises(subprocess.TimeoutExpired) as caught:
                local_slot.cleanup_containers(self.log)
        self.assertIs(caught.exception, error)
        self.assertEqual(self.events()[-1]["state"], "error")

    def test_success_does_not_probe_and_excerpts_are_bounded(self):
        with patch.object(local_slot.subprocess, "run", return_value=subprocess.CompletedProcess([], 0, "", "")), \
                patch.object(local_slot, "diagnose_timeout") as diagnose:
            local_slot.cleanup_containers(self.log)
        diagnose.assert_not_called()
        self.assertEqual(len(local_slot.excerpt("x" * 10000)), 4096)
