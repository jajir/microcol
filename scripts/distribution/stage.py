#!/usr/bin/env python3
"""Turn one successful native Maven build into uniquely named release assets."""

import argparse
import platform
import shutil
import stat
import subprocess
from pathlib import Path, PurePosixPath
import tarfile
import xml.etree.ElementTree as ET
import zipfile

from release import PLATFORMS, validate_build


def check_reports(directory):
    reports = sorted(directory.glob("TEST-*.xml"))
    if not reports:
        raise ValueError("No game test reports: refusing to publish an untested build")
    totals = {name: 0 for name in ("tests", "failures", "errors", "skipped")}
    for report in reports:
        suite = ET.parse(report).getroot()
        for name in totals:
            totals[name] += int(suite.attrib.get(name, "0"))
    if totals["failures"] or totals["errors"] or totals["tests"] <= totals["skipped"]:
        raise ValueError("Game test reports did not contain passing executed tests: " + str(totals))
    print("Verified game tests: " + str(totals))
    return totals


def single(directory, pattern):
    matches = list(directory.glob(pattern))
    if len(matches) != 1:
        raise ValueError("Expected exactly one " + str(directory / pattern))
    return matches[0]


def check_archive(path):
    """Match the launcher's extraction contract before offering an update."""
    if path.suffix == ".zip":
        with zipfile.ZipFile(path) as archive:
            entries = [(item.filename, stat.S_IFMT(item.external_attr >> 16) in (0, stat.S_IFREG, stat.S_IFDIR))
                       for item in archive.infolist()]
    else:
        with tarfile.open(path, "r:gz") as archive:
            entries = [(item.name, item.isfile() or item.isdir()) for item in archive.getmembers()]
    roots = set()
    for name, regular in entries:
        parts = PurePosixPath(name)
        if not regular or parts.is_absolute() or ".." in parts.parts or any(ord(c) < 32 for c in name):
            raise ValueError("Unsafe or unsupported archive member: " + name)
        if parts.parts:
            roots.add(parts.parts[0])
    if len(roots) != 1:
        raise ValueError("Game archive must contain exactly one application directory")
    return next(iter(roots))


def stage(target_platform, build, sha, output):
    validate_build(build, sha)
    expected_host = {"macos-arm64": ("Darwin", "arm64"),
                     "macos-x86_64": ("Darwin", "x86_64"),
                     "linux-x86_64": ("Linux", "x86_64")}[target_platform]
    if (platform.system(), platform.machine()) != expected_host:
        raise ValueError("Refusing to label assets for a different host architecture")
    check_reports(Path("microcol-game/target/surefire-reports"))
    output.mkdir(parents=True, exist_ok=True)
    if any(output.iterdir()):
        raise ValueError("Asset output directory must be empty")
    stem = "MicroCol-" + target_platform + "-" + build
    if target_platform.startswith("macos-"):
        module = Path("microcol-dist-macos/target")
        java = module / "app-image/MicroCol.app/Contents/runtime/Contents/Home/bin/java"
        # ditto retains bundle permissions and symlinks, including the bundled JVM.
        subprocess.run(["ditto", "-c", "-k", "--keepParent", "--norsrc",
                        str(module / "app-image/MicroCol.app"), str(output / (stem + ".zip"))], check=True)
        shutil.copy2(single(module / "packages", "*.dmg"), output / (stem + ".dmg"))
        check_archive(output / (stem + ".zip"))
    else:
        module = Path("microcol-dist-linux/target")
        java = module / "app-image/MicroCol/lib/runtime/bin/java"
        shutil.copy2(single(module, "MicroCol-*.tar.gz"), output / (stem + ".tar.gz"))
        shutil.copy2(single(module / "packages", "*.deb"), output / (stem + ".deb"))
        check_archive(output / (stem + ".tar.gz"))
    modules = subprocess.run([str(java), "--list-modules"], check=True, text=True, capture_output=True).stdout
    if "microcol.game" not in modules or "java.desktop@" not in modules:
        raise ValueError("Packaged Java runtime is missing required application modules")
    print("Verified packaged runtime and updater archive for " + target_platform)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("platform", choices=PLATFORMS)
    parser.add_argument("build")
    parser.add_argument("sha")
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    stage(args.platform, args.build, args.sha, args.output)
