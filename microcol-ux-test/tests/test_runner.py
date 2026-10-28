import json
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import run


class RunnerTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.root = Path(temporary.name)
        self.arguments = ["check", "--container", "microcol-ux-test-owned", "--image", "sha256:tested"]
        self.outputs = ["a" * 40, "sha256:tested", json.dumps({
            "org.microcol.ux.revision": "b" * 40,
            "org.microcol.ux.source-sha256": "c" * 64})]

    def test_failed_create_never_removes_an_existing_container(self):
        with patch.object(run, "ROOT", self.root), patch.object(run.signal, "signal"), \
                patch.object(run.subprocess, "check_output", side_effect=self.outputs), \
                patch.object(run, "command", side_effect=subprocess.CalledProcessError(1, "create")) as command, \
                patch.object(run.subprocess, "run") as cleanup:
            with self.assertRaises(subprocess.CalledProcessError):
                run.main(self.arguments)
            self.assertEqual(1, command.call_count)
            cleanup.assert_not_called()

    def test_container_exit_code_is_checked_and_evidence_keeps_image_identity(self):
        with patch.object(run, "ROOT", self.root), patch.object(run.signal, "signal"), \
                patch.object(run.subprocess, "check_output", side_effect=self.outputs + [json.dumps({"ExitCode": 7, "OOMKilled": False})]), \
                patch.object(run, "command") as command, patch.object(run.subprocess, "run"):
            with self.assertRaisesRegex(RuntimeError, "7"):
                run.main(self.arguments)
        self.assertEqual(("docker", "rm", "--force", "microcol-ux-test-owned"), command.call_args.args)
        manifest = json.loads(next(self.root.glob("microcol-ux-test/runs/*/manifest.json")).read_text())
        self.assertEqual("b" * 40, manifest["image_revision"])
        self.assertEqual("a" * 40, manifest["revision"])

    def test_image_without_provenance_cannot_be_played(self):
        with patch.object(run, "ROOT", self.root), patch.object(run.signal, "signal"), \
                patch.object(run.subprocess, "check_output", side_effect=self.outputs[:2] + ["{}"]), \
                patch.object(run, "command") as command:
            with self.assertRaisesRegex(ValueError, "provenance"):
                run.main(self.arguments)
            command.assert_not_called()


if __name__ == "__main__":
    unittest.main()
