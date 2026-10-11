"""Exercise downloads and cache transitions without network access or launching UI."""

import hashlib
import importlib.util
import io
import json
import os
from pathlib import Path
import plistlib
import shutil
import subprocess
import tarfile
import tempfile
import time
import unittest
import zipfile


SOURCE = Path(__file__).resolve().parents[1]
LAUNCHER = SOURCE / "microcol-dev"
PLATFORMS = ("macos-arm64", "macos-x86_64", "linux-x86_64")


class LauncherTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix="microcol launcher test ")
        self.addCleanup(self.temporary.cleanup)
        self.base = Path(self.temporary.name)
        self.fixtures = self.base / "fixtures"
        self.fixtures.mkdir()
        self.commands = self.base / "commands"
        self.commands.mkdir()
        self.cache = self.base / "cache with spaces"
        self.env = dict(os.environ)
        self.env.update({
            "PATH": str(self.commands) + os.pathsep + os.environ["PATH"],
            "MICROCOL_DEV_ROOT": str(self.cache),
            "FAKE_CURL_FIXTURES": str(self.fixtures),
            "GAME_LOG": str(self.base / "game.log"),
            "OPEN_LOG": str(self.base / "open.log"),
            "XATTR_LOG": str(self.base / "xattr.log"),
            "SIMULATED_OS": "Linux",
            "SIMULATED_ARCH": "x86_64",
        })
        self.command("uname", """import os, sys
print(os.environ['SIMULATED_OS'] if sys.argv[1] == '-s' else os.environ['SIMULATED_ARCH'])
""")
        self.command("sha256sum", """import hashlib, pathlib, sys
print(hashlib.sha256(pathlib.Path(sys.argv[1]).read_bytes()).hexdigest() + '  ' + sys.argv[1])
""")
        self.command("curl", """import os, pathlib, sys, time
a = sys.argv[1:]
assert a[a.index('--proto') + 1] == '=https'
assert a[a.index('--proto-redir') + 1] == '=https'
assert '--max-time' in a and '--connect-timeout' in a
url = a[-1]
assert url.startswith('https://github.com/jajir/microcol/releases/download/')
out = pathlib.Path(a[a.index('--output') + 1])
name = url.rsplit('/', 1)[1]
fixtures = pathlib.Path(os.environ['FAKE_CURL_FIXTURES'])
if name == 'manifest.tsv' and os.environ.get('FAIL_MANIFEST') == '1':
    sys.exit(28)
source = fixtures / name
if not source.is_file():
    sys.exit(22)
if name != 'manifest.tsv':
    (fixtures / 'asset-started').write_text('started')
    if os.environ.get('PAUSE_ASSET'):
        time.sleep(float(os.environ['PAUSE_ASSET']))
    if os.environ.get('PARTIAL_ASSET') == '1':
        out.write_bytes(source.read_bytes()[:16])
        sys.exit(18)
out.write_bytes(source.read_bytes())
""")
        self.command("open", """import json, os, pathlib, sys
pathlib.Path(os.environ['OPEN_LOG']).write_text(json.dumps(sys.argv[1:]))
""")
        self.command("xattr", """import json, os, pathlib, sys
pathlib.Path(os.environ['XATTR_LOG']).write_text(json.dumps(sys.argv[1:]))
""")

    def command(self, name, source):
        path = self.commands / name
        path.write_text("#!/usr/bin/env python3\n" + source)
        path.chmod(0o755)

    def run_launcher(self, *arguments, extra_env=None):
        env = dict(self.env)
        env.update(extra_env or {})
        return subprocess.run([str(LAUNCHER), *arguments], env=env,
                              capture_output=True, text=True, timeout=25)

    def make_release(self, number, bad_path=False, symlink=False):
        commit = (f"{number:x}" * 40)[:40]
        build = f"dev-{number}-{commit[:12]}"
        rows = ["microcol-dev-v1", f"build\t{build}\t{commit}"]
        for platform in PLATFORMS:
            if platform.startswith("macos"):
                filename = f"MicroCol-{platform}-{build}.zip"
                files = {
                    "MicroCol.app/Contents/MacOS/MicroCol": (b"#!/bin/sh\nexit 0\n", 0o755),
                    "MicroCol.app/Contents/runtime/Contents/Home/lib/modules": (build.encode(), 0o644),
                }
                with zipfile.ZipFile(self.fixtures / filename, "w") as archive:
                    for path, (data, mode) in files.items():
                        entry = zipfile.ZipInfo(path)
                        entry.create_system = 3
                        entry.external_attr = (0o100000 | mode) << 16
                        archive.writestr(entry, data)
                    if bad_path:
                        entry = zipfile.ZipInfo("../escaped")
                        entry.create_system = 3
                        entry.external_attr = 0o100644 << 16
                        archive.writestr(entry, b"unexpected")
                    if symlink:
                        entry = zipfile.ZipInfo("MicroCol.app/escape")
                        entry.create_system = 3
                        entry.external_attr = 0o120777 << 16
                        archive.writestr(entry, b"/tmp")
            else:
                filename = f"MicroCol-{platform}-{build}.tar.gz"
                base = "MicroCol-0.7.0-SNAPSHOT-linux-amd64"
                files = {
                    f"{base}/bin/MicroCol": (
                        f'#!/bin/sh\nprintf "%s\\n" "{build}" >> "$GAME_LOG"\n'.encode(), 0o755),
                    f"{base}/lib/runtime/lib/modules": (build.encode(), 0o644),
                }
                with tarfile.open(self.fixtures / filename, "w:gz") as archive:
                    for path, (data, mode) in files.items():
                        entry = tarfile.TarInfo(path)
                        entry.size = len(data)
                        entry.mode = mode
                        archive.addfile(entry, io.BytesIO(data))
                    if bad_path:
                        entry = tarfile.TarInfo("../escaped")
                        entry.size = 10
                        archive.addfile(entry, io.BytesIO(b"unexpected"))
                    if symlink:
                        entry = tarfile.TarInfo(f"{base}/escape")
                        entry.type = tarfile.SYMTYPE
                        entry.linkname = "/tmp"
                        archive.addfile(entry)
            checksum = hashlib.sha256((self.fixtures / filename).read_bytes()).hexdigest()
            rows.append(f"asset\t{platform}\t{filename}\t{checksum}")
        (self.fixtures / "manifest.tsv").write_text("\n".join(rows) + "\n")
        return build

    def state(self):
        return (self.cache / "state.tsv").read_text().strip().split("\t")

    def install(self, number):
        build = self.make_release(number)
        result = self.run_launcher("--update-only")
        self.assertEqual(result.returncode, 0, result.stdout + result.stderr)
        return build

    def test_check_does_not_install_then_valid_update_and_launch(self):
        build = self.make_release(1)
        result = self.run_launcher("--check")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn(build, result.stdout)
        self.assertFalse((self.cache / "state.tsv").exists())
        result = self.run_launcher()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.state(), [build, "-", "-"])
        self.assertEqual((self.base / "game.log").read_text().strip(), build)

    def test_manifest_rejects_duplicate_platform_and_arbitrary_asset_url(self):
        self.make_release(1)
        manifest = self.fixtures / "manifest.tsv"
        original = manifest.read_text()
        manifest.write_text(original.replace("asset\tmacos-x86_64\t", "asset\tmacos-arm64\t"))
        self.assertNotEqual(self.run_launcher("--update-only").returncode, 0)
        manifest.write_text(original.replace("MicroCol-linux-x86_64-", "https://attacker.invalid/"))
        self.assertNotEqual(self.run_launcher("--update-only").returncode, 0)
        self.assertFalse((self.cache / "state.tsv").exists())

    def test_checksum_mismatch_preserves_current_version(self):
        previous = self.install(1)
        self.make_release(2)
        manifest = self.fixtures / "manifest.tsv"
        rows = manifest.read_text().splitlines()
        fields = rows[-1].split("\t")
        fields[-1] = "0" * 64
        rows[-1] = "\t".join(fields)
        manifest.write_text("\n".join(rows) + "\n")
        result = self.run_launcher("--update-only")
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("checksum", result.stderr)
        self.assertEqual(self.state()[0], previous)
        self.assertEqual(len(list((self.cache / "versions").iterdir())), 1)

    def test_partial_download_preserves_current_and_cleans_work(self):
        previous = self.install(1)
        self.make_release(2)
        result = self.run_launcher("--update-only", extra_env={"PARTIAL_ASSET": "1"})
        self.assertNotEqual(result.returncode, 0)
        self.assertEqual(self.state()[0], previous)
        self.assertFalse(list(self.cache.glob("work.*")))
        self.assertFalse((self.cache / "update.lock").exists())

    def test_path_traversal_and_links_are_rejected_before_extraction(self):
        for operating_system, architecture in (("Linux", "x86_64"), ("Darwin", "arm64")):
            self.env.update(SIMULATED_OS=operating_system, SIMULATED_ARCH=architecture)
            for archive_options in ({"bad_path": True}, {"symlink": True}):
                with self.subTest(system=operating_system, **archive_options):
                    self.make_release(1, **archive_options)
                    result = self.run_launcher("--update-only")
                    self.assertNotEqual(result.returncode, 0)
                    self.assertIn("Unsafe or damaged", result.stderr)
                    self.assertFalse((self.cache / "state.tsv").exists())
                    self.assertFalse((self.cache / "escaped").exists())

    def test_offline_fallback_and_first_offline_failure(self):
        offline = {"FAIL_MANIFEST": "1"}
        result = self.run_launcher(extra_env=offline)
        self.assertNotEqual(result.returncode, 0)
        self.assertIn("No usable cached game", result.stderr)
        current = self.install(1)
        result = self.run_launcher(extra_env=offline)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual((self.base / "game.log").read_text().strip(), current)
        self.assertNotEqual(self.run_launcher("--update-only", extra_env=offline).returncode, 0)

    def test_rollback_holds_rejected_build_until_next_release(self):
        first = self.install(1)
        second = self.install(2)
        self.assertEqual(self.state(), [second, first, "-"])
        result = self.run_launcher("--rollback")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual(self.state(), [first, second, second])
        self.assertEqual(self.run_launcher().returncode, 0)
        self.assertEqual(self.state()[0], first)
        third = self.install(3)
        self.assertEqual(self.state(), [third, first, "-"])
        self.assertTrue((self.cache / "versions" / second).is_dir())

    def test_stale_manifest_does_not_downgrade_current_game(self):
        current = self.install(2)
        self.make_release(1)
        result = self.run_launcher("--update-only")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn("Ignoring stale release", result.stderr)
        self.assertEqual(self.state()[0], current)

    def test_concurrent_updates_serialize_without_overwriting_current(self):
        first = self.install(1)
        old_game = self.cache / "versions" / first / "app/bin/MicroCol"
        old_bytes = old_game.read_bytes()
        second = self.make_release(2)
        (self.fixtures / "asset-started").unlink(missing_ok=True)
        env = dict(self.env, PAUSE_ASSET="1.5")
        process = subprocess.Popen([str(LAUNCHER), "--update-only"], env=env,
                                   stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        self.addCleanup(lambda: process.poll() is None and process.kill())
        deadline = time.monotonic() + 5
        while not (self.fixtures / "asset-started").exists():
            self.assertLess(time.monotonic(), deadline)
            time.sleep(0.02)
        self.assertEqual(self.state()[0], first)
        other = self.run_launcher("--update-only")
        stdout, stderr = process.communicate(timeout=10)
        self.assertEqual(process.returncode, 0, stdout + stderr)
        self.assertEqual(other.returncode, 0, other.stderr)
        self.assertEqual(self.state(), [second, first, "-"])
        self.assertEqual(old_game.read_bytes(), old_bytes)
        self.assertFalse((self.cache / "update.lock").exists())

    def test_dead_owner_lock_recovers(self):
        self.make_release(1)
        lock = self.cache / "update.lock"
        lock.mkdir(parents=True)
        # PID is deliberately outside both macOS and Linux's supported range.
        (lock / "pid").write_text("999999999\n")
        result = self.run_launcher("--update-only")
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertFalse(lock.exists())

    def test_macos_uses_quarantine_and_open_without_runtime_execution(self):
        self.env.update(SIMULATED_OS="Darwin", SIMULATED_ARCH="arm64")
        build = self.install(1)
        quarantine = json.loads((self.base / "xattr.log").read_text())
        self.assertEqual(quarantine[:2], ["-w", "com.apple.quarantine"])
        self.assertTrue(quarantine[2].startswith("0083;"))
        result = self.run_launcher()
        self.assertEqual(result.returncode, 0, result.stderr)
        opened = json.loads((self.base / "open.log").read_text())
        self.assertEqual(opened, [str(self.cache / "versions" / build / "MicroCol.app"), "--args"])
        self.assertFalse((self.base / "game.log").exists())

    def test_bootstrap_archives_preserve_executability_and_install_without_root(self):
        output = self.base / "packages"
        result = subprocess.run([os.sys.executable, str(SOURCE / "package.py"),
                                 "--output-dir", str(output)], capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stderr)
        with zipfile.ZipFile(output / "MicroCol-Dev-macos.zip") as archive:
            entry = archive.getinfo("MicroCol Dev.app/Contents/MacOS/MicroCol Dev")
            self.assertEqual((entry.external_attr >> 16) & 0o777, 0o755)
            info = plistlib.loads(archive.read("MicroCol Dev.app/Contents/Info.plist"))
            self.assertEqual(info["CFBundleExecutable"], "MicroCol Dev")
        extract = self.base / "linux-bootstrap"
        extract.mkdir()
        with tarfile.open(output / "MicroCol-Dev-linux.tar.gz") as archive:
            self.assertEqual(archive.getmember("microcol-dev/install.sh").mode, 0o755)
            archive.extractall(extract)
        data = self.base / "xdg data"
        env = dict(self.env, XDG_DATA_HOME=str(data))
        result = subprocess.run([str(extract / "microcol-dev/install.sh")], env=env,
                                capture_output=True, text=True)
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertTrue(os.access(self.cache / "launcher/microcol-dev", os.X_OK))
        self.assertIn('Terminal=true', (data / "applications/microcol-dev.desktop").read_text())


if __name__ == "__main__":
    unittest.main()
