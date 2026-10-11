#!/usr/bin/env python3
"""Open the actual portable game in Xvfb, keeping user data isolated."""

import argparse
import os
from pathlib import Path
import re
import subprocess
import sys
import tarfile
import tempfile
import time

from stage import check_archive


def open_game(launcher, evidence):
    with tempfile.TemporaryDirectory(prefix="microcol-smoke-home-") as temporary:
        home = Path(temporary)
        environment = os.environ.copy()
        environment.pop("JAVA_HOME", None)
        environment["HOME"] = str(home)
        environment["JAVA_TOOL_OPTIONS"] = ("-DsoundEnabled=false -Duser.home=" + str(home) +
                                            " -Djava.util.prefs.userRoot=" + str(home / "prefs"))
        log_path = evidence / "game.log"
        with log_path.open("w", encoding="utf-8") as log:
            process = subprocess.Popen([str(launcher)], cwd=home, env=environment,
                                       stdout=log, stderr=subprocess.STDOUT)
            try:
                deadline = time.monotonic() + 60
                while time.monotonic() < deadline:
                    if process.poll() is not None:
                        raise RuntimeError("Packaged game exited before opening a window: " + log_path.read_text())
                    windows = subprocess.run(["xwininfo", "-root", "-tree"], check=True,
                                             text=True, capture_output=True).stdout
                    (evidence / "windows.txt").write_text(windows, encoding="utf-8")
                    game_windows = re.findall(r'"[^"\n]*microcol[^"\n]*"[^\n]*?\b(\d+)x(\d+)\+', windows, re.IGNORECASE)
                    if any(int(width) > 100 and int(height) > 100 for width, height in game_windows):
                        # A window plus startup evidence catches missing native JavaFX libraries/resources.
                        time.sleep(2)
                        contents = log_path.read_text(encoding="utf-8", errors="replace")
                        if process.poll() is None and "started in production mode" in contents:
                            print("Portable Linux game opened its window using the bundled runtime.")
                            return
                    time.sleep(0.5)
                raise RuntimeError("Packaged game did not open a usable window; see " + str(evidence))
            finally:
                if process.poll() is None:
                    process.terminate()
                    try:
                        process.wait(timeout=10)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait()


def smoke(archive, evidence):
    evidence.mkdir(parents=True, exist_ok=True)
    root = check_archive(archive)
    with tempfile.TemporaryDirectory(prefix="microcol-smoke-app-") as temporary:
        with tarfile.open(archive, "r:gz") as bundle:
            # check_archive already rejected links, devices and paths outside the single root.
            bundle.extractall(temporary)
        launcher = Path(temporary) / root / "bin/MicroCol"
        subprocess.run(["xvfb-run", "-a", "--server-args=-screen 0 1280x1024x24",
                        sys.executable, str(Path(__file__).resolve()), "--display-child",
                        str(launcher), str(evidence.resolve())], check=True)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--display-child", action="store_true", help=argparse.SUPPRESS)
    parser.add_argument("archive_or_launcher", type=Path)
    parser.add_argument("evidence", type=Path)
    args = parser.parse_args()
    if args.display_child:
        open_game(args.archive_or_launcher, args.evidence)
    else:
        smoke(args.archive_or_launcher, args.evidence)
