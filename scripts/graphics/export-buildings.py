#!/usr/bin/env python3
"""Export the canonical building SVGs, or check their committed PNG exports.

Shortcut for the manifest group ``buildings`` of export-graphics.py; without
--check it installs the fresh exports into the runtime resources.
"""
import argparse
import importlib.util
from pathlib import Path

spec = importlib.util.spec_from_file_location("export_graphics",
                                              Path(__file__).with_name("export-graphics.py"))
graphics = importlib.util.module_from_spec(spec)
spec.loader.exec_module(graphics)


def export(check=False):
    count, stale, _ = graphics.run(["buildings"], check=check, install=not check)
    if stale:
        raise ValueError("Stale building export: " + ", ".join(stale))
    return count


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Compare without changing files")
    arguments = parser.parse_args()
    print(f"{'Checked' if arguments.check else 'Exported'} {export(arguments.check)} building images")
