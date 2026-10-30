#!/usr/bin/env python3
"""Export the canonical building SVGs, or check their committed PNG exports."""
import argparse
from pathlib import Path
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "microcol-game/src/graphics/colony-buildings"
TARGET = ROOT / "microcol-game/src/main/resources/images/colony-buildings"


def export(check=False):
    sources = sorted(SOURCE.glob("*.svg"))
    if not sources:
        raise ValueError("No building SVG sources found")
    expected = {source.with_suffix(".png").name for source in sources}
    if expected != {path.name for path in TARGET.glob("*.png")}:
        raise ValueError("Building SVG and runtime PNG inventories differ")
    with tempfile.TemporaryDirectory(prefix="microcol-building-export-") as directory:
        pending = []
        for source in sources:
            rendered = Path(directory) / source.with_suffix(".png").name
            subprocess.run(["rsvg-convert", "--width", "320", "--height", "160",
                            "--output", str(rendered), str(source)], check=True)
            target = TARGET / rendered.name
            if check and rendered.read_bytes() != target.read_bytes():
                raise ValueError(f"Stale building export: {target.name}")
            pending.append((rendered, target))
        if not check:
            for rendered, target in pending:
                target.write_bytes(rendered.read_bytes())
    return len(sources)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Compare without changing files")
    arguments = parser.parse_args()
    print(f"{'Checked' if arguments.check else 'Exported'} {export(arguments.check)} building images")
