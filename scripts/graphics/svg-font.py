#!/usr/bin/env python3
"""Refresh/check self-contained Playpen Sans subsets in tracked SVG text assets.

Authoring dependency: fontTools (the version used is in the bundled SOURCE.txt).
No font tools are needed to run the game or open the resulting SVGs.
"""
import argparse
import base64
import hashlib
import io
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

from fontTools import subset
from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parents[2]
FONT = ROOT / "microcol-game/src/main/resources/fonts/PlaypenSans/PlaypenSans-Light.ttf"
SVG = "{http://www.w3.org/2000/svg}"


def update(source):
    tree = ET.fromstring(source)
    text = "".join("".join(node.itertext()) for node in tree.iter()
                   if node.tag in (SVG + "text", SVG + "flowRoot"))
    if not text.strip():
        return source
    font = TTFont(FONT, recalcTimestamp=False)
    missing = {c for c in text if not c.isspace() and ord(c) not in font.getBestCmap()}
    if missing:
        raise ValueError("SVG characters missing from bundled font: " + repr(missing))
    options = subset.Options()
    options.name_IDs = ["*"]  # Preserve copyright and the OFL license in the subset.
    options.drop_tables += ["meta"]
    cutter = subset.Subsetter(options=options)
    cutter.populate(text=text)
    cutter.subset(font)
    font.flavor = "woff"
    output = io.BytesIO()
    font.save(output)
    payload = base64.b64encode(output.getvalue()).decode("ascii")
    digest = hashlib.sha256(FONT.read_bytes()).hexdigest()
    style = ('<style id="microcol-font" type="text/css" data-font-sha256="' + digest
             + '">@font-face{font-family:"Playpen Sans Light";font-style:normal;'
             + 'font-weight:300;src:url("data:font/woff;base64,' + payload
             + '") format("woff");}</style>\n\n  ')
    source = re.sub(r'<style id="microcol-font".*?</style>\s*', '', source, flags=re.S)
    source = re.sub(r'(?<![-\w])font-family:[^;"<]+', "font-family:'Playpen Sans Light'", source)
    source = re.sub(r'-inkscape-font-specification:[^;"<]+',
                    "-inkscape-font-specification:'Playpen Sans Light'", source)
    source = re.sub(r'(?<![-\w])font-weight:[^;"<]+', 'font-weight:300', source)
    source = re.sub(r'(?<![-\w])font-style:[^;"<]+', 'font-style:normal', source)
    source, count = re.subn(r'<defs\b', lambda match: style + match.group(), source, count=1)
    if count != 1:
        raise ValueError("Expected an SVG defs element")
    ET.fromstring(source)
    return source


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    files = subprocess.check_output(['git', 'ls-files', '*.svg'], cwd=ROOT, text=True).splitlines()
    changed = []
    for name in files:
        path = ROOT / name
        old = path.read_text()
        new = update(old)
        if new != old:
            changed.append(name)
            if not args.check:
                path.write_text(new)
    print(("Stale" if args.check else "Updated") + " SVG font assets: " + str(len(changed)))
    for name in changed:
        print(name)
    if args.check and changed:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
