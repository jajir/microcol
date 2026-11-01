import importlib.util
import json
from pathlib import Path
import struct
import subprocess
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("building_export", Path(__file__).with_name("export-buildings.py"))
exporter = importlib.util.module_from_spec(spec)
spec.loader.exec_module(exporter)
RENDERED = b"\x89PNG\r\n\x1a\n" + struct.pack(">I4sIIBB", 13, b"IHDR", 320, 160, 8, 6) + b"rendered"


class BuildingExportTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.addCleanup(self.directory.cleanup)
        self.root = Path(self.directory.name)
        self.source = self.root / "source/colony-buildings"
        self.target = self.root / "target/colony-buildings"
        self.source.mkdir(parents=True)
        self.target.mkdir(parents=True)
        (self.source / "TOWN_HALL.svg").write_text("<svg/>")
        (self.target / "TOWN_HALL.png").write_bytes(b"old")
        manifest = self.root / "manifest.json"
        manifest.write_text(json.dumps({"sourceRoot": "source", "runtimeRoot": "target", "groups": {
            "buildings": {"outputs": [{
                "output": "colony-buildings/*.png", "source": "colony-buildings/*.svg",
                "render": "size", "size": [320, 160], "transparent": True,
                "consumer": "loader", "display": "64", "stroke": "scaled"}]}}}))
        for name, value in [("ROOT", self.root), ("MANIFEST", manifest)]:
            patcher = patch.object(exporter.graphics, name, value)
            patcher.start()
            self.addCleanup(patcher.stop)
        patcher = patch.object(exporter.graphics.shutil, "which", return_value="/usr/bin/tool")
        patcher.start()
        self.addCleanup(patcher.stop)

    @staticmethod
    def render(command, check):
        assert check
        assert command[:5] == ["rsvg-convert", "--width", "320", "--height", "160"]
        Path(command[6]).write_bytes(RENDERED)

    def test_export_and_check_without_mutation(self):
        with patch.object(exporter.graphics.subprocess, "run", side_effect=self.render):
            with self.assertRaisesRegex(ValueError, "Stale"):
                exporter.export(check=True)
            self.assertEqual(b"old", (self.target / "TOWN_HALL.png").read_bytes())
            self.assertEqual(1, exporter.export())
            self.assertEqual(1, exporter.export(check=True))
            self.assertEqual(RENDERED, (self.target / "TOWN_HALL.png").read_bytes())

    def test_missing_source_is_rejected_before_rendering(self):
        (self.target / "FORT.png").write_bytes(b"old")
        with patch.object(exporter.graphics.subprocess, "run") as run:
            with self.assertRaisesRegex(ValueError, "inventories"):
                exporter.export()
            run.assert_not_called()

    def test_renderer_failure_preserves_all_exports(self):
        (self.source / "UNIVERSITY.svg").write_text("<svg/>")
        (self.target / "UNIVERSITY.png").write_bytes(b"old")

        def fail_second(command, check):
            if command[-1].endswith("UNIVERSITY.svg"):
                raise subprocess.CalledProcessError(1, command)
            self.render(command, check)

        with patch.object(exporter.graphics.subprocess, "run", side_effect=fail_second):
            with self.assertRaises(subprocess.CalledProcessError):
                exporter.export()
        self.assertEqual([b"old", b"old"], [p.read_bytes() for p in sorted(self.target.glob("*.png"))])
