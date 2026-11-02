import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from desktop import Desktop
import run


class DesktopTest(unittest.TestCase):
    def setUp(self):
        temporary = tempfile.TemporaryDirectory()
        self.addCleanup(temporary.cleanup)
        self.desktop = Desktop(temporary.name, max_actions=1)
        self.frame = {"frame": "fixture.png", "width": 800, "height": 600}

    def test_invalid_actions_do_not_inject_input(self):
        for action, values in [("click", dict(x=-1, y=0, button="left")),
                               ("click", dict(x=800, y=0, button="left")),
                               ("key", dict(combo="Return;exit")),
                               ("text", dict(text="\0")),
                               ("wait", dict(seconds=float("nan"))),
                               ("scroll", dict(x=0, y=0, amount=11)),
                               ("drag", dict(x=0, y=0, to_x=800, to_y=0))]:
            with self.subTest(action=action, values=values), patch.object(self.desktop, "capture", return_value=(self.frame, b"png")), patch.object(self.desktop, "command") as command:
                with self.assertRaises(ValueError):
                    self.desktop.perform(action, **values)
                command.assert_not_called()
                self.assertEqual(0, self.desktop.actions)

    def test_delivered_action_has_before_after_and_budget(self):
        with patch.object(self.desktop, "capture", return_value=(self.frame, b"png")), patch.object(self.desktop, "command"), patch("desktop.time.sleep"):
            metadata, data = self.desktop.perform("click", x=10, y=20, button="left")
            self.assertEqual(0, metadata["actions_remaining"])
            with self.assertRaisesRegex(ValueError, "budget"):
                self.desktop.perform("click", x=10, y=20, button="left")
        events = self.events()
        self.assertEqual(["started", "delivered"], [e["status"] for e in events])
        self.assertEqual("fixture.png", events[-1]["after"])

    def test_failed_drag_releases_button_and_preserves_error(self):
        with patch.object(self.desktop, "capture", return_value=(self.frame, b"png")), patch.object(self.desktop, "command", side_effect=[RuntimeError("input failed"), b""]) as command:
            with self.assertRaisesRegex(RuntimeError, "input failed"):
                self.desktop.perform("drag", x=0, y=0, to_x=10, to_y=20)
        self.assertEqual(("xdotool", "mouseup", "1"), command.call_args.args)
        self.assertEqual("error", self.events()[-1]["status"])

    def test_notes_preserve_unicode_and_source(self):
        with patch.object(self.desktop, "capture", return_value=(self.frame, b"png")):
            self.desktop.note("Kolonie", "Příjem", "Nic", "guess", "Nejasné")
        self.assertEqual("Příjem", self.events()[0]["expected"])
        self.assertEqual("guess", self.events()[0]["source"])

    def test_mcp_restart_does_not_reset_action_budget(self):
        self.desktop.log({"kind": "action", "number": 1, "status": "started"})
        restarted = Desktop(self.desktop.directory, max_actions=1)
        with self.assertRaisesRegex(ValueError, "budget"):
            restarted.perform("click", x=0, y=0, button="left")

    def test_invalid_capture_rejected(self):
        with patch.object(self.desktop, "command"), patch.object(Path, "read_bytes", return_value=b"bad image"):
            with self.assertRaisesRegex(ValueError, "PNG"):
                self.desktop.capture()

    def test_player_requires_explicit_model_and_reasoning(self):
        with self.assertRaises(SystemExit):
            run.parse(["play", "--container", "test"])
        args = run.parse(["play", "--container", "test", "--model", "selected-model", "--reasoning", "high"])
        self.assertEqual("selected-model", args.model)

    def events(self):
        return [json.loads(line) for line in (self.desktop.directory / "actions.jsonl").read_text().splitlines()]


if __name__ == "__main__":
    unittest.main()
