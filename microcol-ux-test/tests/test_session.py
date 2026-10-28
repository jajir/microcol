from pathlib import Path
import json
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import Mock, patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import session


class SessionTest(unittest.TestCase):
    def test_report_without_observation_is_not_a_completed_session(self):
        with tempfile.TemporaryDirectory() as directory:
            results = Path(directory)
            with self.assertRaisesRegex(RuntimeError, "without a report"):
                session.verify_play_evidence(results)
            (results / "report.md").write_text("The desktop tools were unavailable.")
            with self.assertRaisesRegex(RuntimeError, "never observed"):
                session.verify_play_evidence(results)
            (results / "actions.jsonl").write_text('{"kind":"observe","frame":"screen.png"}\n')
            session.verify_play_evidence(results)

    def test_turn_checkpoints_require_consecutive_notes_and_screenshots(self):
        with tempfile.TemporaryDirectory() as directory:
            results = Path(directory)
            (results / "report.md").write_text("Playthrough report")
            frames = results / "screenshots"
            frames.mkdir()
            (frames / "turn.png").write_bytes(b"fixture")
            notes = [{"kind": "observe"}] + [
                {"kind": "note", "goal": f"Turn {turn:02}/40", "frame": "turn.png",
                 "observed": "Next turn", "source": "Visible calendar"} for turn in range(1, 41)]
            ledger = results / "actions.jsonl"
            ledger.write_text("\n".join(json.dumps(e) for e in notes if e.get("goal") != "Turn 03/40"))
            self.assertEqual(2, session.verify_play_evidence(results))
            ledger.write_text("\n".join(json.dumps(e) for e in notes))
            self.assertEqual(40, session.verify_play_evidence(results))
            (frames / "turn.png").unlink()
            self.assertEqual(0, session.verify_play_evidence(results))

    def test_timeout_preserves_evidence_and_excludes_credentials(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            results = root / "results"
            results.mkdir()
            home = root / "player"
            (home / ".microcol").mkdir(parents=True)
            (home / ".microcol/save").write_text("saved game")
            processes = [Mock(pid=100 + n) for n in range(4)]
            for process in processes:
                process.poll.return_value = None
                process.wait.return_value = 0
            processes[-1].wait.side_effect = [subprocess.TimeoutExpired("codex", 1), 0]
            original = Path.read_text
            def read(path, *args, **kwargs):
                return "player task" if str(path) == "/opt/ux/player.md" else original(path, *args, **kwargs)
            copyfile = session.shutil.copyfile
            def auth(source, destination, **kwargs):
                if str(source) == "/auth/auth.json":
                    destination.write_text("sensitive credentials")
                else:
                    return copyfile(source, destination, **kwargs)
            environment = {"DISPLAY": ":99", "XAUTHORITY": "/tmp/xauth", "UX_MODEL": "selected-model",
                           "UX_REASONING": "high", "UX_SECONDS": "1", "UX_MAX_ACTIONS": "10"}
            with patch.object(session, "RESULTS", results), patch.object(session.os, "environ", environment), \
                    patch.object(Path, "home", return_value=home), patch.object(Path, "read_text", read), \
                    patch.object(session.sys, "argv", ["session.py", "play"]), patch.object(session.time, "sleep"), \
                    patch.object(session.signal, "signal"), patch.object(session.os, "killpg"), \
                    patch.object(session.shutil, "copyfile", side_effect=auth), \
                    patch.object(session.subprocess, "Popen", side_effect=processes):
                self.assertEqual(124, session.main())
            self.assertIn('"status": "incomplete"', (results / "session.json").read_text())
            self.assertEqual("saved game", (results / "profile/.microcol/save").read_text())
            self.assertFalse((results / "profile/.codex").exists())
            config = (home / ".codex/config.toml").read_text()
            self.assertIn('model = "selected-model"', config)
            self.assertIn('DISPLAY = ":99"', config)
            self.assertIn('sandbox_mode = "read-only"', config)
            self.assertIn('shell_tool = false', config)
            self.assertIn('default_tools_approval_mode = "approve"', config)
            self.assertNotIn("sensitive credentials", config)


if __name__ == "__main__":
    unittest.main()
