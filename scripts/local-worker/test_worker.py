"""Behavior checks without invoking a model or changing the host worker lock."""

import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

import worker


class WorkerTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.tree = self.root / "tree"
        self.tree.mkdir()
        (self.tree / "a.java").write_text("old A\n")
        (self.tree / "b.java").write_text("old B\n")
        self.job = self.root / "job"
        self.job.mkdir()
        self.config = dict(model="local:trial", base_model="gpt-oss:20b", reasoning="medium", num_ctx=32768,
                           num_predict=16384, attempt_seconds=1800,
                           task_seconds=3600, max_repairs=2)
        self.contract = dict(worktree=str(self.tree), baseline="abc", mode="text-only",
                             allowed_files=["a.java", "b.java"], anchors=[
                                 dict(id="a", file="a.java", old="old A\n", expected_matches=1),
                                 dict(id="b", file="b.java", old="old B\n", expected_matches=1)])
        self.hashes = worker.check_anchors(self.tree, self.contract)
        self.response = {"replacements": [{"id": "a", "new": "new A\n"},
                                           {"id": "b", "new": "new B\n"}]}

    def attempt(self, number, seconds):
        path = self.job / f"attempt-{number}"
        path.mkdir()
        worker.write_json(path / "execution.json", {"elapsed_seconds": seconds, "exit_code": 0})
        worker.write_json(path / "profile.json", self.config)
        return path

    def test_budget_charges_failed_and_cancelled_attempts(self):
        self.attempt(0, 1799.5)
        self.attempt(1, 1750)
        self.assertEqual(worker.budget(self.job, self.config), (2, 3549.5, 50.5))
        self.attempt(2, 1)
        with self.assertRaisesRegex(ValueError, "Repair limit"):
            worker.budget(self.job, self.config)

    def test_budget_does_not_reset_after_missing_record_or_profile_change(self):
        path = self.attempt(0, 3600.1)
        with self.assertRaisesRegex(ValueError, "Task time"):
            worker.budget(self.job, self.config)
        (path / "execution.json").unlink()
        with self.assertRaisesRegex(ValueError, "Incomplete attempt"):
            worker.budget(self.job, self.config)
        worker.write_json(path / "execution.json", {"elapsed_seconds": 1})
        worker.write_json(path / "profile.json", {})
        with self.assertRaisesRegex(ValueError, "Profile changed"):
            worker.budget(self.job, self.config)

    def test_batch_rejects_bad_last_edit_without_writing_first(self):
        path = self.attempt(0, 1)
        worker.write_json(path / "job.json", self.contract)
        worker.write_json(path / "source-hashes.json", self.hashes)
        self.response["replacements"][-1]["id"] = "unknown"
        worker.write_json(path / "response.md", self.response)
        with patch.object(worker, "git", return_value="abc"), self.assertRaises(ValueError):
            worker.apply(self.job, 0)
        self.assertEqual((self.tree / "a.java").read_text(), "old A\n")
        self.assertFalse((path / "application.json").exists())

    def test_batch_decodes_newlines_and_applies_all_valid_edits(self):
        path = self.attempt(0, 1)
        worker.write_json(path / "job.json", self.contract)
        worker.write_json(path / "source-hashes.json", self.hashes)
        worker.write_json(path / "response.md", self.response)
        with patch.object(worker, "git", return_value="abc"):
            worker.apply(self.job, 0)
        self.assertEqual((self.tree / "a.java").read_text(), "new A\n")
        self.assertEqual((self.tree / "b.java").read_text(), "new B\n")
        with patch.object(worker, "git", return_value="abc"), self.assertRaises(ValueError):
            worker.apply(self.job, 0)

    def test_stale_source_duplicate_and_unchanged_output_are_rejected(self):
        for replacements in [self.response["replacements"][:1],
                             self.response["replacements"] * 2,
                             [{"id": "a", "new": "old A\n"}, self.response["replacements"][1]]]:
            with self.subTest(replacements=replacements), self.assertRaises(ValueError):
                worker.replacements(self.tree, self.contract, self.hashes,
                                    {"replacements": replacements})
        (self.tree / "a.java").write_text("old A\ncaller addition\n")
        with self.assertRaisesRegex(ValueError, "Source changed"):
            worker.replacements(self.tree, self.contract, self.hashes, self.response)

    def test_legacy_response_requires_exact_anchor(self):
        legacy = {"replacements": [dict(a, new="new " + a["id"]) for a in self.contract["anchors"]]}
        for item in legacy["replacements"]:
            del item["id"]
        self.assertEqual(len(worker.replacements(self.tree, self.contract, self.hashes, legacy)), 2)
        legacy["replacements"][0]["old"] = "wrong"
        with self.assertRaisesRegex(ValueError, "legacy anchor"):
            worker.replacements(self.tree, self.contract, self.hashes, legacy)

    def test_paths_and_ambiguous_anchors_fail_preflight(self):
        outside = self.root / "outside"
        outside.write_text("old A\n")
        (self.tree / "link").symlink_to(outside)
        for path in ("../outside", str(outside), "link"):
            with self.subTest(path=path), self.assertRaises(ValueError):
                worker.source_path(self.tree, path)
        (self.tree / "a.java").write_text("old A\nold A\n")
        with self.assertRaisesRegex(ValueError, "Anchor mismatch"):
            worker.check_anchors(self.tree, self.contract)

    def test_text_only_command_has_no_shell_and_direct_edit_keeps_shell(self):
        for mode, sandbox, shell in [("text-only", "read-only", "false"),
                                     ("edit", "workspace-write", "true")]:
            self.contract["mode"] = mode
            cmd = worker.command(self.config, self.contract, self.tree, self.job, "codex")
            self.assertIn("features.shell_tool=" + shell, cmd)
            self.assertEqual(cmd[cmd.index("-s") + 1], sandbox)
            self.assertIn("--oss", cmd)
            self.assertEqual(cmd[cmd.index("-m") + 1], "local:trial")

    def test_foreign_lock_and_same_job_concurrency_are_rejected(self):
        lock = self.root / "active.lock"
        worker.claim(self.job, lock)
        with self.assertRaises(FileExistsError):
            worker.claim(self.job, lock)
        with self.assertRaisesRegex(ValueError, "another job"):
            worker.claim(self.root / "other", lock)
        with patch.object(worker, "api", return_value={"models": []}), self.assertRaises(ValueError):
            worker.release(self.job, lock)
        (lock / "running").rmdir()
        with patch.object(worker, "api", return_value={"models": []}):
            worker.release(self.job, lock)
        self.assertFalse(lock.exists())

    def test_timeout_terminates_owned_process_and_preserves_logs(self):
        (self.job / "task.md").write_text("test")
        result = worker.supervise([sys.executable, "-c", "import time; time.sleep(60)"],
                                  self.tree, self.job, os.environ.copy(), 0.1, lambda: None)
        self.assertTrue(result["timed_out"])
        self.assertEqual(result["exit_code"], -15)
        pid = worker.read_json(self.job / "process.json")["pid"]
        with self.assertRaises(ProcessLookupError):
            os.kill(pid, 0)
        self.assertTrue((self.job / "events.jsonl").is_file())

    def test_sampler_failure_cancels_owned_process(self):
        (self.job / "task.md").write_text("test")
        def fail():
            raise ValueError("Unexpected runtime model/context")
        result = worker.supervise([sys.executable, "-c", "import time; time.sleep(60)"],
                                  self.tree, self.job, os.environ.copy(), 60, fail)
        self.assertEqual(result["error"], "Unexpected runtime model/context")
        self.assertEqual(result["exit_code"], -15)

    def test_preflight_failure_never_launches_process(self):
        with patch.object(worker, "preflight", side_effect=ValueError("bad source")), \
                patch.object(subprocess, "Popen") as start:
            with self.assertRaisesRegex(ValueError, "bad source"):
                worker.run(self.job)
        start.assert_not_called()

    def test_installed_model_mismatch_stops_before_generation(self):
        good = {"details": {"parent_model": "gpt-oss:20b"},
                "parameters": "num_ctx 32768\nnum_predict 16384\n"}
        for info in [{**good, "parameters": "num_ctx 32768\nnum_predict 4096\n"},
                     {**good, "details": {"parent_model": "other"}}]:
            with self.subTest(info=info), patch.object(worker, "api", return_value=info), \
                    self.assertRaisesRegex(ValueError, "mismatch"):
                worker.model_info(self.config)

    def test_failed_start_records_budget_unloads_and_retains_verification_lock(self):
        for name in ("task.md", "worker-role.md"):
            (self.job / name).write_text("supplied input")
        worker.write_json(self.job / "job.json", self.contract)
        lock = self.root / "active.lock"
        with patch.object(worker, "profile", return_value=self.config), \
                patch.object(worker, "preflight", return_value=(
                    self.contract, self.tree, self.hashes, (0, 0, 1800))), \
                patch.object(worker, "LOCK", lock), \
                patch.object(worker, "model_info", return_value={"digest": "expected"}), \
                patch.object(worker, "api", return_value={"models": []}), \
                patch.object(worker, "git", return_value=""), \
                patch.object(worker.shutil, "which", return_value="/fake/tool"), \
                patch.object(worker, "supervise", return_value={
                    "error": "failed start", "exit_code": None}), \
                patch.object(subprocess, "run", return_value=subprocess.CompletedProcess(
                    [], 0, stdout="unloaded", stderr="")) as cleanup:
            self.assertEqual(worker.run(self.job), 1)
        evidence = worker.read_json(self.job / "attempt-0/execution.json")
        self.assertEqual(evidence["error"], "failed start")
        self.assertGreaterEqual(evidence["elapsed_seconds"], 0)
        self.assertEqual(evidence["unload_exit_code"], 0)
        cleanup.assert_called_once()
        self.assertTrue((lock / "owner.json").is_file())
        self.assertFalse((lock / "running").exists())


if __name__ == "__main__":
    unittest.main()
