"""Named player tasks; game knowledge belongs in the on-screen UI, not here."""
from pathlib import Path

PLAYERS = {"strategist": 2700, "ux-reviewer": 2700, "beginner": 2400}
REPORT_SECONDS = 300
PROTOCOL = "2"


def prompt_for(player, directory=None):
    if player not in PLAYERS:
        raise ValueError("Unknown player profile")
    directory = Path(directory) if directory else Path(__file__).resolve().parent
    return ((directory / "player.md").read_text() + "\n\n" +
            (directory / "players" / (player + ".md")).read_text())
