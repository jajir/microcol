#!/usr/bin/env python3
"""Export the legacy text icons of akropia-module from the game's icon.svg.

The distribution icon with the church (akropia-dist/src/icon.png|icns) is a
separate artwork and is not touched. Required host tools: python3,
rsvg-convert (librsvg, run through with-fonts.py with Fontconfig) and, for the
ICNS file, macOS iconutil. Pillow and other packages are not needed.
"""
import argparse
from pathlib import Path
import struct
import subprocess
import sys
import tempfile

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "akropia-game/src/graphics/icon.svg"
WITH_FONTS = ROOT / "scripts/graphics/with-fonts.py"
ICO_SIZES = (256, 128, 64, 48, 32, 16)
# iconutil names: icon_<size>x<size>.png and its @2x variant at double size.
ICNS_SIZES = (16, 32, 128, 256, 512)
PNG_SIGNATURE = b"\x89PNG\r\n\x1a\n"


def png_size(data):
    """Return (width, height) from the IHDR chunk of a PNG image."""
    if data[:8] != PNG_SIGNATURE or data[12:16] != b"IHDR":
        raise ValueError("Not a PNG image")
    return struct.unpack(">II", data[16:24])


def ico_bytes(images):
    """Build an ICO container holding square 32-bit PNG images."""
    header = struct.pack("<3H", 0, 1, len(images))
    entries, offset = b"", 6 + 16 * len(images)
    for data in images:
        width, height = png_size(data)
        if width != height or not 1 <= width <= 256:
            raise ValueError(f"ICO image must be square, 1-256 px: {width}x{height}")
        size = 0 if width == 256 else width  # 0 encodes 256 in the directory.
        entries += struct.pack("<4B2H2I", size, size, 0, 0, 1, 32, len(data), offset)
        offset += len(data)
    return header + entries + b"".join(images)


def render(size, directory):
    target = Path(directory) / f"{size}.png"
    subprocess.run([sys.executable, str(WITH_FONTS), "rsvg-convert", "--width", str(size),
                    "--height", str(size), "-o", str(target), str(SOURCE)], check=True)
    data = target.read_bytes()
    if png_size(data) != (size, size):
        raise ValueError(f"Unexpected rendered size for {size} px")
    return data


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", type=Path, default=ROOT / "akropia-module/src")
    args = parser.parse_args()
    with tempfile.TemporaryDirectory(prefix="akropia-icons-") as tmp:
        sizes = set(ICO_SIZES) | set(ICNS_SIZES) | {2 * s for s in ICNS_SIZES}
        rendered = {size: render(size, tmp) for size in sorted(sizes)}
        iconset = Path(tmp) / "icon.iconset"
        iconset.mkdir()
        for size in ICNS_SIZES:
            (iconset / f"icon_{size}x{size}.png").write_bytes(rendered[size])
            (iconset / f"icon_{size}x{size}@2x.png").write_bytes(rendered[2 * size])
        args.output.mkdir(parents=True, exist_ok=True)
        subprocess.run(["iconutil", "-c", "icns", str(iconset), "-o",
                        str(args.output / "icon.icns")], check=True)
    (args.output / "icon.ico").write_bytes(ico_bytes([rendered[s] for s in ICO_SIZES]))
    (args.output / "icon_square_64x64.ico").write_bytes(ico_bytes([rendered[64]]))
    print("Exported icon.icns, icon.ico and icon_square_64x64.ico to " + str(args.output))


if __name__ == "__main__":
    main()
