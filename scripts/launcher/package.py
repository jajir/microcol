#!/usr/bin/env python3
"""Create dependency-free launcher downloads on any CI host using Python stdlib."""

import argparse
import io
from pathlib import Path
import plistlib
import tarfile
import zipfile


SOURCE = Path(__file__).resolve().parent
PROJECT = SOURCE.parent.parent


def mac_entries():
    contents = "MicroCol Dev.app/Contents"
    info = {
        "CFBundleDevelopmentRegion": "English",
        "CFBundleExecutable": "MicroCol Dev",
        "CFBundleIdentifier": "org.microcol.devlauncher",
        "CFBundleName": "MicroCol Dev",
        "CFBundleDisplayName": "MicroCol Dev",
        "CFBundlePackageType": "APPL",
        "CFBundleShortVersionString": "1.0.0",
        "CFBundleVersion": "1.0.0",
        "CFBundleIconFile": "MicroCol.icns",
        "NSHighResolutionCapable": True,
    }
    return [
        (f"{contents}/Info.plist", plistlib.dumps(info), 0o644),
        (f"{contents}/MacOS/MicroCol Dev", (SOURCE / "macos-wrapper.sh").read_bytes(), 0o755),
        (f"{contents}/Resources/microcol-dev", (SOURCE / "microcol-dev").read_bytes(), 0o755),
        (f"{contents}/Resources/MicroCol.icns", (PROJECT / "microcol-dist/src/icon.icns").read_bytes(), 0o644),
        ("MicroCol Dev.command", (SOURCE / "microcol-dev").read_bytes(), 0o755),
        ("README.txt", (SOURCE / "README.txt").read_bytes(), 0o644),
    ]


def linux_entries():
    return [
        ("microcol-dev/microcol-dev", (SOURCE / "microcol-dev").read_bytes(), 0o755),
        ("microcol-dev/install.sh", (SOURCE / "install-linux.sh").read_bytes(), 0o755),
        ("microcol-dev/MicroCol.png", (PROJECT / "microcol-dist/src/icon.png").read_bytes(), 0o644),
        ("microcol-dev/README.txt", (SOURCE / "README.txt").read_bytes(), 0o644),
    ]


def package(output_dir):
    output_dir.mkdir(parents=True, exist_ok=True)
    mac = output_dir / "MicroCol-Dev-macos.zip"
    with zipfile.ZipFile(mac, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name, data, mode in mac_entries():
            entry = zipfile.ZipInfo(name, date_time=(2026, 1, 1, 0, 0, 0))
            entry.create_system = 3
            entry.external_attr = (0o100000 | mode) << 16
            entry.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(entry, data)
    linux = output_dir / "MicroCol-Dev-linux.tar.gz"
    with tarfile.open(linux, "w:gz", format=tarfile.PAX_FORMAT) as archive:
        for name, data, mode in linux_entries():
            entry = tarfile.TarInfo(name)
            entry.size = len(data)
            entry.mode = mode
            entry.mtime = 0
            archive.addfile(entry, io.BytesIO(data))
    return mac, linux


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()
    for artifact in package(args.output_dir):
        print(artifact)
