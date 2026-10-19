#!/usr/bin/env python3
"""Run a Fontconfig-aware renderer with only the project's bundled Light font."""
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET


def main():
    if len(sys.argv) < 2:
        raise SystemExit("Usage: with-fonts.py COMMAND [ARG ...]")
    root = Path(__file__).resolve().parents[2]
    font = root / 'microcol-game/src/main/resources/fonts/PlaypenSans/PlaypenSans-Light.ttf'
    if not font.is_file():
        raise SystemExit("Missing bundled Playpen Sans Light font")
    with tempfile.TemporaryDirectory(prefix='microcol-fonts-') as tmp:
        config = ET.Element('fontconfig')
        ET.SubElement(config, 'dir').text = str(font.parent)
        ET.SubElement(config, 'cachedir').text = str(Path(tmp) / 'cache')
        path = Path(tmp) / 'fonts.conf'
        ET.ElementTree(config).write(path, encoding='utf-8', xml_declaration=True)
        # macOS Pango otherwise selects CoreText and ignores the private font set.
        env = dict(os.environ, FONTCONFIG_FILE=str(path), FONTCONFIG_PATH=tmp,
                   PANGOCAIRO_BACKEND='fc')
        selected = subprocess.check_output(
            ['fc-match', '-f', '%{file}', 'Playpen Sans Light'], env=env, text=True)
        if Path(selected).resolve() != font.resolve():
            raise SystemExit("Renderer did not select the bundled Playpen Sans Light font")
        return subprocess.call(sys.argv[1:], env=env)


if __name__ == '__main__':
    raise SystemExit(main())
