"""X11 desktop actions and durable evidence, independent of the game model."""
import json
import os
from pathlib import Path
import struct
import subprocess
import time
import uuid
from datetime import datetime, timezone


class Desktop:
    def __init__(self, directory, max_actions=100):
        self.directory = Path(directory)
        self.frames = self.directory / "screenshots"
        self.frames.mkdir(parents=True, exist_ok=True)
        self.max_actions = max_actions
        ledger = self.directory / "actions.jsonl"
        self.actions = max((json.loads(line).get("number", 0)
                            for line in ledger.read_text().splitlines()), default=0) if ledger.exists() else 0

    def log(self, event):
        event = {"time": datetime.now(timezone.utc).isoformat(), **event}
        with (self.directory / "actions.jsonl").open("a") as stream:
            stream.write(json.dumps(event, ensure_ascii=False) + "\n")
            stream.flush()
            os.fsync(stream.fileno())

    def command(self, *args):
        return subprocess.run(args, check=True, capture_output=True,
                              timeout=15).stdout

    def capture(self):
        name = uuid.uuid4().hex + ".png"
        path = self.frames / name
        self.command("scrot", "--overwrite", str(path))
        data = path.read_bytes()
        if data[:8] != b"\x89PNG\r\n\x1a\n" or len(data) < 24:
            raise ValueError("Capture did not produce a PNG")
        width, height = struct.unpack(">II", data[16:24])
        metadata = {"frame": name, "width": width, "height": height}
        if os.environ.get("UX_DEADLINE"):
            metadata["seconds_remaining"] = max(0, round(float(os.environ["UX_DEADLINE"]) - time.time()))
        return metadata, data

    def observe(self):
        frame, data = self.capture()
        self.log({"kind": "observe", **frame})
        return frame, data

    @staticmethod
    def point(x, y, frame):
        if type(x) is not int or type(y) is not int:
            raise ValueError("Coordinates must be integers")
        if not (0 <= x < frame["width"] and 0 <= y < frame["height"]):
            raise ValueError("Coordinates must lie inside the screenshot")

    def perform(self, action, **values):
        if self.actions >= self.max_actions:
            raise ValueError("Action budget reached; record findings and finish")
        before, _ = self.capture()
        try:
            command = self.prepare(action, values, before)
        except (ValueError, KeyError, TypeError) as error:
            self.log({"kind": "rejected", "action": action, "arguments": values,
                      "before": before["frame"], "error": str(error)})
            raise
        self.actions += 1
        event = {"kind": "action", "number": self.actions, "action": action,
                 "arguments": values, "before": before["frame"]}
        self.log({**event, "status": "started"})
        try:
            if action == "text":
                self.paste(values["text"])
            elif action == "wait":
                time.sleep(values["seconds"])
            elif action == "drag":
                try:
                    self.command(*command)
                finally:
                    self.command("xdotool", "mouseup", "1")
            else:
                self.command(*command)
            time.sleep(0.25)
            after, data = self.capture()
            self.log({**event, "status": "delivered", "after": after["frame"]})
            return {**after, "actions_remaining": self.max_actions - self.actions}, data
        except Exception as error:
            self.log({**event, "status": "error", "error": str(error)})
            raise

    def prepare(self, action, values, frame):
        if action in ("click", "move", "drag", "scroll"):
            self.point(values["x"], values["y"], frame)
            # --sync waits forever when the pointer is already at the destination.
            command = ["xdotool", "mousemove", str(values["x"]), str(values["y"])]
            if action == "click":
                button = values["button"]
                if button not in ("left", "middle", "right"):
                    raise ValueError("Unknown mouse button")
                command += ["click", str({"left": 1, "middle": 2, "right": 3}[button])]
            elif action == "drag":
                self.point(values["to_x"], values["to_y"], frame)
                # JavaFX starts native DnD on motion; a later motion must enter the drop target.
                command += ["mousedown", "1", "sleep", "0.15", "mousemove",
                            str((values["x"] + values["to_x"]) // 2),
                            str((values["y"] + values["to_y"]) // 2),
                            "sleep", "0.15", "mousemove",
                            str(values["to_x"]), str(values["to_y"]),
                            "sleep", "0.15", "mouseup", "1"]
            elif action == "scroll":
                amount = values["amount"]
                if type(amount) is not int or not 1 <= abs(amount) <= 10:
                    raise ValueError("Scroll amount must be between -10 and 10, excluding zero")
                command += ["click", "--repeat", str(abs(amount)), "4" if amount < 0 else "5"]
            return command
        if action == "key":
            combo = values["combo"]
            if not combo or len(combo) > 80 or any(c not in "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_+" for c in combo):
                raise ValueError("Use an X11 key name/chord, for example Escape or ctrl+s")
            return ["xdotool", "key", "--clearmodifiers", combo]
        if action == "text":
            if not isinstance(values["text"], str) or not 1 <= len(values["text"]) <= 4096 or "\0" in values["text"]:
                raise ValueError("Text must contain 1–4096 characters without NUL")
            return []
        if action == "wait":
            if type(values["seconds"]) not in (int, float) or not 0 <= values["seconds"] <= 5:
                raise ValueError("Wait must be between zero and five seconds")
            return []
        raise ValueError("Unknown action")

    def paste(self, text):
        # Keep the clipboard owner alive until the focused application reads it.
        process = subprocess.Popen(["xclip", "-quiet", "-selection", "clipboard", "-in"],
                                   stdin=subprocess.PIPE, stdout=subprocess.DEVNULL,
                                   stderr=subprocess.DEVNULL)
        try:
            process.stdin.write(text.encode("utf-8"))
            process.stdin.close()
            time.sleep(0.15)
            self.command("xdotool", "key", "--clearmodifiers", "ctrl+v")
            time.sleep(0.25)
        finally:
            process.terminate()
            process.wait(timeout=5)

    def note(self, goal, expected, observed, source, finding=""):
        values = dict(goal=goal, expected=expected, observed=observed,
                      source=source, finding=finding)
        if any(not isinstance(v, str) or len(v) > 4000 for v in values.values()):
            raise ValueError("Each note field must be text of at most 4000 characters")
        frame, _ = self.capture()
        self.log({"kind": "note", **frame, **values})
        return frame
