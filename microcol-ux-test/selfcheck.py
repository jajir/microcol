"""Live X11 and MCP contract check, with a purpose-built desktop fixture."""
import asyncio
import base64
import json
import os
from pathlib import Path
import subprocess
import sys
import time

RESULTS = Path(os.environ["UX_RESULTS"])


def fixture():
    import tkinter as tk
    root = tk.Tk()
    root.geometry("800x600+100+100")
    root.configure(background="white")
    text = tk.StringVar()
    state = {"text": "", "drag": None, "return": False, "scroll": 0}
    def save(*args):
        state["text"] = text.get()
        (RESULTS / "fixture.json").write_text(json.dumps(state))
    entry = tk.Entry(root, textvariable=text)
    entry.place(x=20, y=20, width=500, height=35)
    text.trace_add("write", save)
    def enter(event):
        state["return"] = True
        save()
    entry.bind("<Return>", enter)
    canvas = tk.Canvas(root, background="lightblue")
    canvas.place(x=20, y=100, width=500, height=350)
    canvas.create_rectangle(20, 20, 100, 100, fill="yellow")
    def dragged(event):
        state["drag"] = [event.x_root, event.y_root]
        save()
    def scrolled(event):
        state["scroll"] += 1
        save()
    canvas.bind("<B1-Motion>", dragged)
    canvas.bind("<Button-5>", scrolled)
    def ready():
        # Use the mapped client origin; Openbox owns placement and input focus.
        state["origin"] = [root.winfo_rootx(), root.winfo_rooty()]
        save()
    root.after(200, ready)
    root.mainloop()


async def check_javafx_drag():
    from desktop import Desktop
    log_path = RESULTS / "javafx-drag.log"
    with log_path.open("w") as log:
        process = subprocess.Popen([
            "java", "-Dprism.order=sw", "-cp", "/opt/microcol/game.jar",
            str(Path(__file__).parent / "fixtures" / "DragFixture.java")],
            stdout=log, stderr=log)
        try:
            for _ in range(200):
                ready = next((line for line in log_path.read_text().splitlines()
                              if line.startswith("READY ")), None)
                if ready:
                    break
                assert process.poll() is None, "JavaFX fixture exited; inspect javafx-drag.log"
                await asyncio.sleep(0.05)
            else:
                raise RuntimeError("JavaFX fixture failed to start")
            x, y, to_x, to_y = map(int, ready.split()[1:])
            Desktop(RESULTS).perform("drag", x=x, y=y, to_x=to_x, to_y=to_y)
            assert "DROPPED" in log_path.read_text().splitlines(), "JavaFX drop not delivered"
        finally:
            process.terminate()
            process.wait(timeout=5)


async def check():
    from mcp import ClientSession, StdioServerParameters
    from mcp.client.stdio import stdio_client
    process = subprocess.Popen([sys.executable, __file__, "fixture"])
    checks = []
    try:
        for _ in range(100):
            if (RESULTS / "fixture.json").exists():
                break
            await asyncio.sleep(0.05)
        else:
            raise RuntimeError("Desktop fixture failed to start")
        x, y = json.loads((RESULTS / "fixture.json").read_text())["origin"]
        parameters = StdioServerParameters(command=sys.executable, args=["/opt/ux/server.py"], env=dict(os.environ))
        async with stdio_client(parameters) as streams:
            async with ClientSession(*streams) as client:
                await client.initialize()
                listed = await client.list_tools()
                assert len(listed.tools) == 9
                checks.append("MCP initialization and nine desktop tools")
                async def call(name, **arguments):
                    result = await client.call_tool(name, arguments)
                    assert not result.isError, str(result)
                    return result
                result = await call("screenshot")
                image = next(c for c in result.content if c.type == "image")
                assert base64.b64decode(image.data).startswith(b"\x89PNG")
                checks.append("MCP returns actual PNG pixels")
                invalid = await client.call_tool("click", {"x": -1, "y": 0})
                assert invalid.isError
                checks.append("out-of-screen input rejected")
                await call("click", x=x + 60, y=y + 35)
                await call("type_text", text="Příliš žluťoučký 🦊")
                await call("key", combo="Return")
                await call("drag", x=x + 60, y=y + 140, to_x=x + 200, to_y=y + 240)
                await call("scroll", x=x + 200, y=y + 240, amount=2)
                await call("move", x=400, y=400)
                await call("wait", seconds=0.1)
                state = json.loads((RESULTS / "fixture.json").read_text())
                assert state["text"] == "Příliš žluťoučký 🦊", state
                assert state["return"], state
                assert state["drag"] == [x + 200, y + 240], state
                assert state["scroll"] == 2, state
                checks += ["focus and Unicode clipboard", "keyboard Return", "native drag", "native scroll"]
                await call("record_note", goal="self-check", expected="native inputs", observed=str(state), source="fixture")
                events = [json.loads(line) for line in (RESULTS / "actions.jsonl").read_text().splitlines()]
                assert any(e["kind"] == "note" for e in events)
                for event in events:
                    for field in ("frame", "before", "after"):
                        if field in event:
                            assert (RESULTS / "screenshots" / event[field]).is_file()
                checks.append("durable notes and linked screenshot files")
    finally:
        process.terminate()
        process.wait(timeout=5)
    await check_javafx_drag()
    checks.append("JavaFX native drag-and-drop delivers payload")
    with (RESULTS / "game.log").open("w") as log:
        game = subprocess.Popen(["java", "-Duser.home=/home/player", "-Dprism.order=sw", "-DsoundEnabled=false",
                                 "-jar", "/opt/microcol/game.jar"], stdout=log, stderr=log)
        try:
            await asyncio.sleep(5)
            assert game.poll() is None, "Packaged game exited; inspect game.log"
            from desktop import Desktop
            metadata, _ = Desktop(RESULTS).observe()
            (RESULTS / "game-frame.json").write_text(json.dumps(metadata))
            checks.append("packaged game remains running and captured")
        finally:
            game.terminate()
            game.wait(timeout=5)
    (RESULTS / "selfcheck.json").write_text(json.dumps({"checks": checks, "failures": 0}, indent=2) + "\n")
    print("PASS: " + str(len(checks)) + " live X11/MCP checks", flush=True)


if __name__ == "__main__":
    if sys.argv[1:] == ["fixture"]:
        fixture()
    else:
        asyncio.run(check())
