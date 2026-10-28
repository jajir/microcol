"""One bounded desktop session; always leave evidence in the mounted results."""
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import time

RESULTS = Path(os.environ.get("UX_RESULTS", "/results"))


def verify_play_evidence(directory):
    report = directory / "report.md"
    if not report.is_file() or not report.stat().st_size:
        raise RuntimeError("Codex finished without a report")
    ledger = directory / "actions.jsonl"
    events = [json.loads(line) for line in ledger.read_text().splitlines()] if ledger.exists() else []
    if not any(e.get("kind") == "observe" or e.get("status") == "delivered" for e in events):
        raise RuntimeError("Codex never observed the desktop; inspect report.md and codex-events.jsonl")
    for turn in range(1, 41):
        if not any(e.get("kind") == "note" and e.get("goal") == f"Turn {turn:02}/40"
                   and e.get("observed") and e.get("source")
                   and (directory / "screenshots" / e.get("frame", "missing")).is_file()
                   for e in events):
            return turn - 1
    return 40


def main():
    if not os.environ.get("DISPLAY"):
        os.execvp("xvfb-run", ["xvfb-run", "-a", "-e", str(RESULTS / "xvfb.log"),
                              "--server-args=-screen 0 1600x1200x24 -nolisten tcp",
                              sys.executable, __file__, *sys.argv[1:]])
    processes = []
    streams = []

    def start(command, name):
        stream = (RESULTS / (name + ".log")).open("w")
        streams.append(stream)
        process = subprocess.Popen(command, stdout=stream, stderr=stream, start_new_session=True)
        processes.append(process)
        return process

    def interrupted(signum, frame):
        raise InterruptedError("Session interrupted")

    signal.signal(signal.SIGTERM, interrupted)
    signal.signal(signal.SIGINT, interrupted)
    outcome = {"status": "failed", "exit_code": None}
    began = time.monotonic()
    try:
        start(["openbox"], "openbox")
        start(["xcompmgr", "-n"], "compositor")
        time.sleep(0.5)
        if sys.argv[1:] == ["check"]:
            outcome["exit_code"] = subprocess.call([sys.executable, "/opt/ux/selfcheck.py"])
        else:
            game = start(["java", "-Duser.home=/home/player", "-Dprism.order=sw", "-DsoundEnabled=false",
                          "-jar", "/opt/microcol/game.jar"], "game")
            time.sleep(4)
            if game.poll() is not None:
                raise RuntimeError("Game exited during startup; inspect game.log")
            codex_home = Path.home() / ".codex"
            codex_home.mkdir(exist_ok=True, mode=0o700)
            shutil.copyfile("/auth/auth.json", codex_home / "auth.json")
            (codex_home / "auth.json").chmod(0o600)
            config = '\n'.join([
                'model = ' + json.dumps(os.environ["UX_MODEL"]),
                'model_reasoning_effort = ' + json.dumps(os.environ["UX_REASONING"]),
                'approval_policy = "never"',
                'sandbox_mode = "read-only"', 'web_search = "disabled"',
                '[tools]', 'view_image = false',
                '[features]', 'shell_tool = false', 'apps = false', 'plugins = false',
                'remote_plugin = false', 'multi_agent = false',
                '[mcp_servers.desktop]', 'command = "/opt/ux-venv/bin/python"',
                'args = ["/opt/ux/server.py"]', 'required = true',
                'default_tools_approval_mode = "approve"',
                'enabled_tools = ["screenshot", "click", "move", "drag", "scroll", "key", "type_text", "wait", "record_note"]',
                'tool_timeout_sec = 30',
                '[mcp_servers.desktop.env]', 'UX_RESULTS = "/results"',
                'UX_MAX_ACTIONS = ' + json.dumps(os.environ.get("UX_MAX_ACTIONS", "100")),
                'DISPLAY = ' + json.dumps(os.environ["DISPLAY"]),
                'XAUTHORITY = ' + json.dumps(os.environ["XAUTHORITY"]),
                'UX_DEADLINE = ' + json.dumps(str(time.time() + int(os.environ["UX_SECONDS"]))),
            ]) + '\n'
            (codex_home / "config.toml").write_text(config)
            prompt = Path("/opt/ux/player.md").read_text()
            prompt += "\nSession limits: " + os.environ["UX_SECONDS"] + " seconds, " + os.environ["UX_MAX_ACTIONS"] + " actions."
            with (RESULTS / "codex-events.jsonl").open("w") as events, (RESULTS / "codex.log").open("w") as log:
                agent = subprocess.Popen(["codex", "exec", "--skip-git-repo-check", "--json",
                                          "--output-last-message", str(RESULTS / "report.md"), prompt],
                                         stdout=events, stderr=log, start_new_session=True)
                processes.append(agent)
                outcome["exit_code"] = agent.wait(timeout=int(os.environ["UX_SECONDS"]))
            if outcome["exit_code"] == 0:
                outcome["documented_turns"] = verify_play_evidence(RESULTS)
                if outcome["documented_turns"] != 40:
                    outcome.update(status="incomplete", exit_code=2,
                                   error="Fewer than 40 consecutive turn checkpoints; inspect report and screenshots")
        if outcome["exit_code"] == 0:
            outcome["status"] = "completed"
    except subprocess.TimeoutExpired:
        outcome.update(status="incomplete", error="Session time budget reached", exit_code=124)
    except (OSError, RuntimeError, ValueError, InterruptedError) as error:
        outcome.update(status="failed", error=str(error), exit_code=1)
    finally:
        signal.signal(signal.SIGTERM, signal.SIG_IGN)
        signal.signal(signal.SIGINT, signal.SIG_IGN)
        for process in reversed(processes):
            if process.poll() is None:
                try:
                    os.killpg(process.pid, signal.SIGTERM)
                    process.wait(timeout=5)
                except subprocess.TimeoutExpired:
                    try:
                        os.killpg(process.pid, signal.SIGKILL)
                    except ProcessLookupError:
                        pass
                    process.wait()
                except ProcessLookupError:
                    pass
        for stream in streams:
            stream.close()
        try:
            for name in (".microcol", ".java", ".local/state/microcol"):
                source = Path.home() / name
                if source.exists():
                    shutil.copytree(source, RESULTS / "profile" / name, dirs_exist_ok=True)
        except OSError as error:
            outcome.update(status="failed", error="Profile export failed: " + str(error), exit_code=1)
        outcome["elapsed_seconds"] = round(time.monotonic() - began, 3)
        (RESULTS / "session.json").write_text(json.dumps(outcome, indent=2) + "\n")
    return outcome["exit_code"] or 0


if __name__ == "__main__":
    sys.exit(main())
