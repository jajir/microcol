"""One bounded desktop session; always leave evidence in the mounted results."""
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import time
import zipfile
from evidence import verify_play_evidence, package_results
from profiles import REPORT_SECONDS

RESULTS = Path(os.environ.get("UX_RESULTS", "/results"))



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
                          "-jar", "/opt/akropia/game.jar"], "game")
            time.sleep(4)
            if game.poll() is not None:
                raise RuntimeError("Game exited during startup; inspect game.log")
            codex_home = Path.home() / ".codex"
            codex_home.mkdir(exist_ok=True, mode=0o700)
            shutil.copyfile("/auth/auth.json", codex_home / "auth.json")
            (codex_home / "auth.json").chmod(0o600)
            play_started = time.time()
            play_deadline = play_started + int(os.environ["UX_SECONDS"])
            outcome.update(player=os.environ["UX_PLAYER"], play_started_at=play_started,
                           play_seconds=int(os.environ["UX_SECONDS"]), report_seconds=REPORT_SECONDS)
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
                'UX_DEADLINE = ' + json.dumps(str(play_deadline)),
                'UX_REPORT_DEADLINE = ' + json.dumps(str(play_deadline + REPORT_SECONDS)),
            ]) + '\n'
            (codex_home / "config.toml").write_text(config)
            prompt = Path("/task/player.md").read_text()
            prompt += "\nSession limits: " + os.environ["UX_SECONDS"] + " seconds, " + os.environ["UX_MAX_ACTIONS"] + " input actions. Separate report allowance: " + str(REPORT_SECONDS) + " seconds."
            with (RESULTS / "codex-events.jsonl").open("w") as events, (RESULTS / "codex.log").open("w") as log:
                agent = subprocess.Popen(["codex", "exec", "--skip-git-repo-check", "--json",
                                          "--output-last-message", str(RESULTS / "report.md"), prompt],
                                         stdout=events, stderr=log, start_new_session=True)
                processes.append(agent)
                outcome["exit_code"] = agent.wait(timeout=int(os.environ["UX_SECONDS"]) + REPORT_SECONDS)
            if outcome["exit_code"] == 0:
                outcome.update(verify_play_evidence(RESULTS))
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
            for name in (".akropia", ".java", ".local/state/akropia"):
                source = Path.home() / name
                if source.exists():
                    shutil.copytree(source, RESULTS / "profile" / name, dirs_exist_ok=True)
        except OSError as error:
            outcome.update(status="failed", error="Profile export failed: " + str(error), exit_code=1)
        outcome["elapsed_seconds"] = round(time.monotonic() - began, 3)
        (RESULTS / "session.json").write_text(json.dumps(outcome, indent=2) + "\n")
        if sys.argv[1:] != ["check"]:
            try:
                package_results(RESULTS, outcome)
            except (OSError, ValueError, KeyError, zipfile.BadZipFile) as error:
                outcome.update(status="failed", export_error=str(error), exit_code=1)
                (RESULTS / "session.json").write_text(json.dumps(outcome, indent=2) + "\n")
    return outcome["exit_code"] or 0


if __name__ == "__main__":
    sys.exit(main())
