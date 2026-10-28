"""Only screen-level tools are exposed to the player; MCP uses stdio."""
import json
import os
from mcp.server.fastmcp import FastMCP, Image
from desktop import Desktop

desktop = Desktop(os.environ["UX_RESULTS"], int(os.environ.get("UX_MAX_ACTIONS", "100")))
mcp = FastMCP("MicroCol desktop")


def result(value):
    metadata, data = value
    return [json.dumps(metadata), Image(data=data, format="png")]


@mcp.tool()
def screenshot():
    """See the actual X11 screen. All tool coordinates use these unscaled pixels."""
    return result(desktop.observe())


@mcp.tool()
def click(x: int, y: int, button: str = "left"):
    """Click a visible screen location and return the resulting screenshot."""
    return result(desktop.perform("click", x=x, y=y, button=button))


@mcp.tool()
def move(x: int, y: int):
    """Move the pointer to inspect a visible control or its tooltip."""
    return result(desktop.perform("move", x=x, y=y))


@mcp.tool()
def drag(x: int, y: int, to_x: int, to_y: int):
    """Drag with the left button between visible locations."""
    return result(desktop.perform("drag", x=x, y=y, to_x=to_x, to_y=to_y))


@mcp.tool()
def scroll(x: int, y: int, amount: int):
    """Scroll at a location: negative up, positive down, at most ten notches."""
    return result(desktop.perform("scroll", x=x, y=y, amount=amount))


@mcp.tool()
def key(combo: str):
    """Press an X11 key/chord, such as Escape, Return, Right or ctrl+s."""
    return result(desktop.perform("key", combo=combo))


@mcp.tool()
def type_text(text: str):
    """Paste Unicode text into the focused text field using the X11 clipboard."""
    return result(desktop.perform("text", text=text))


@mcp.tool()
def wait(seconds: float = 1):
    """Wait up to five seconds for a transition, then inspect a new screenshot."""
    return result(desktop.perform("wait", seconds=seconds))


@mcp.tool()
def record_note(goal: str, expected: str, observed: str, source: str, finding: str = ""):
    """Record concise player observations with a fresh evidence frame. Distinguish guesses."""
    return desktop.note(goal, expected, observed, source, finding)


if __name__ == "__main__":
    mcp.run(transport="stdio")
