import importlib.util
import json
from pathlib import Path
import re
import shutil
import struct
import tempfile
import unittest
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("export_graphics", Path(__file__).with_name("export-graphics.py"))
graphics = importlib.util.module_from_spec(spec)
spec.loader.exec_module(graphics)
SVG = '<svg xmlns="http://www.w3.org/2000/svg" width="{0}" height="{1}" viewBox="0 0 {0} {1}"><g><path d="M0 0h1"/></g></svg>'


def png(width, height, colour=6):
    return b"\x89PNG\r\n\x1a\n" + struct.pack(">I4sIIBB", 13, b"IHDR", width, height, 8, colour)


def entry(output, **fields):
    return dict({"output": output, "render": "native", "size": [10, 10], "transparent": True,
                 "consumer": "loader", "display": "10", "stroke": "native"}, **fields)


class GraphicsExportTest(unittest.TestCase):
    def setUp(self):
        directory = tempfile.TemporaryDirectory()
        self.addCleanup(directory.cleanup)
        self.root = Path(directory.name)
        (self.root / "src").mkdir()
        (self.root / "runtime").mkdir()
        (self.root / "src/icon.svg").write_text(SVG.format(10, 10))
        self.helpers = []
        patcher = patch.object(graphics.shutil, "which", return_value="/usr/bin/tool")
        self.which = patcher.start()
        self.addCleanup(patcher.stop)

    def manifest(self, *entries, **groups):
        groups = groups or {"icons": {"outputs": list(entries)}}
        path = self.root / "manifest.json"
        path.write_text(json.dumps({"sourceRoot": "src", "runtimeRoot": "runtime", "groups": groups}))
        return path

    def render(self, command, check):
        """Stand-in renderer: records the SVG it received and writes a PNG header."""
        output, source = Path(command[command.index("--output") + 1]), Path(command[-1])
        text = source.read_text()
        self.helpers.append((command, text))
        if "--width" in command:
            size = int(command[command.index("--width") + 1]), int(command[command.index("--height") + 1])
        else:
            root = re.search(r"<svg[^>]*>", text).group()
            size = tuple(int(re.search(f' {name}="([0-9]+)"', root)[1]) for name in ("width", "height"))
            if "--zoom" in command:
                size = tuple(value * int(command[command.index("--zoom") + 1]) for value in size)
        output.write_bytes(png(*size))

    def run_export(self, manifest, **options):
        with patch.object(graphics.subprocess, "run", side_effect=self.render):
            return graphics.run(manifest=manifest, root=self.root, **options)

    def test_default_export_uses_fresh_directory_with_only_mapped_outputs(self):
        manifest = self.manifest(entry("icon.png", source="icon.svg"),
                                 entry("big/icon.png", source="icon.svg", render="zoom", zoom=2, size=[20, 20]),
                                 entry("text.png", source="icon.svg", render="size", size=[5, 5], fonts=True))
        count, stale, directory = self.run_export(manifest)
        self.addCleanup(shutil.rmtree, directory)
        self.assertEqual((3, []), (count, stale))
        self.assertNotIn(self.root, directory.parents)
        self.assertEqual(["big/icon.png", "icon.png", "text.png"],
                         sorted(p.relative_to(directory).as_posix() for p in directory.rglob("*") if p.is_file()))
        self.assertEqual([], list((self.root / "runtime").iterdir()))
        self.assertTrue(self.helpers[2][0][1].endswith("with-fonts.py"))
        self.assertEqual("rsvg-convert", self.helpers[2][0][2])

    def test_output_must_be_new_or_empty_and_outside_protected_trees(self):
        manifest = self.manifest(entry("icon.png", source="icon.svg"))
        (self.root / "used").mkdir()
        (self.root / "used/old.png").write_bytes(b"keep")
        for output, message in [(self.root / "runtime", "overlaps"), (self.root / "src/out", "overlaps"),
                                (self.root, "overlaps"), (self.root / "used", "empty")]:
            with self.subTest(output=output), self.assertRaisesRegex(ValueError, message):
                self.run_export(manifest, output=output)
        self.assertEqual(b"keep", (self.root / "used/old.png").read_bytes())
        self.assertEqual([], self.helpers)
        self.run_export(manifest, output=self.root / "new/out")
        self.assertTrue((self.root / "new/out/icon.png").is_file())

    def test_malformed_manifests_fail_with_the_offending_field(self):
        cases = [
            ("Unsafe", [entry("../icon.png", source="icon.svg")]),
            ("Unsafe", [entry("/tmp/icon.png", source="icon.svg")]),
            ("owned by both", [entry("icon.png", source="icon.svg"), entry("icon.png", source="icon.svg")]),
            ("size", [entry("icon.png", source="icon.svg", size=[10])]),
            ("unknown render", [entry("icon.png", source="icon.svg", render="scale")]),
            ("needs stroke", [entry("icon.png", source="icon.svg", stroke="")]),
            ("does not match size", [entry("icon.png", source="icon.svg",
                                           atlas={"columns": 3, "rows": 1, "step": 5, "aliases": "a"})]),
            ("outside", [entry("big.png", source="icon.svg", render="viewbox", viewBox=[0, 0, 10, 10]),
                         entry("edge.png", render="crop", of="big.png", crop=[6, 0, 5, 5], size=[5, 5])]),
            ("viewbox output", [entry("edge.png", render="crop", of="missing.png", crop=[0, 0, 5, 5], size=[5, 5])]),
            ("reason", [entry("bird.png", render="preserved", format="gif")]),
        ]
        for message, entries in cases:
            with self.subTest(message), self.assertRaisesRegex(ValueError, message):
                graphics.load(self.manifest(*entries))
        (self.root / "broken.json").write_text("{")
        with self.assertRaisesRegex(ValueError, "Invalid"):
            graphics.load(self.root / "broken.json")
        with self.assertRaisesRegex(ValueError, "Missing graphics export manifest"):
            graphics.load(self.root / "absent.json")
        with self.assertRaisesRegex(ValueError, "Unknown export group"):
            self.run_export(self.manifest(entry("icon.png", source="icon.svg")), groups=["menu"])

    def test_missing_or_bitmap_sources_and_prerequisites_fail_before_rendering(self):
        (self.root / "src/photo.svg").write_text(
            '<svg xmlns="http://www.w3.org/2000/svg"><image href="photo.png"/></svg>')
        for source, message in [("absent.svg", "Missing SVG source"), ("photo.svg", "bitmap")]:
            with self.subTest(source), self.assertRaisesRegex(ValueError, message):
                self.run_export(self.manifest(entry("icon.png", source=source)))
        self.which.side_effect = lambda tool: None if tool == "fc-match" else "/usr/bin/" + tool
        with self.assertRaisesRegex(ValueError, "prerequisite.*fc-match"):
            self.run_export(self.manifest(entry("icon.png", source="icon.svg", fonts=True)))
        self.which.side_effect = lambda tool: None
        with self.assertRaisesRegex(ValueError, "prerequisite.*rsvg-convert"):
            self.run_export(self.manifest(entry("icon.png", source="icon.svg")))
        self.assertEqual([], self.helpers)

    def test_rendered_size_format_and_alpha_are_validated(self):
        manifest = self.manifest(entry("icon.png", source="icon.svg"))
        for content, message in [(png(9, 10), "expected 10x10"), (b"GIF89a" + bytes(20), "found gif"),
                                 (png(10, 10, colour=2), "alpha")]:
            def render(command, check, content=content):
                Path(command[command.index("--output") + 1]).write_bytes(content)
            with self.subTest(message), patch.object(graphics.subprocess, "run", side_effect=render):
                with self.assertRaisesRegex(ValueError, message):
                    graphics.run(manifest=manifest, root=self.root, output=self.root / message)

    def test_viewbox_crop_and_atlas_sources_are_composed_deterministically(self):
        (self.root / "src/scene.svg").write_text(
            '<?xml version="1.0"?>\n<svg xmlns="http://www.w3.org/2000/svg"'
            ' xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"\n   width="1000" height="500"'
            ' viewBox="0 0 264 132" inkscape:export-width="9"><rect width="5" height="5"/></svg>')
        manifest = self.manifest(
            entry("scene.png", source="scene.svg", render="viewbox", viewBox=[-10, 0, 100, 50],
                  preserveAspectRatio="none", size=[200, 100], transparent=False),
            entry("edge.png", render="crop", of="scene.png", crop=[0, 20, 50, 40], size=[50, 40]),
            entry("atlas.png", render="atlas", sources=["icon.svg", "icon.svg", "icon.svg"], sourceCell=10,
                  size=[20, 20], atlas={"columns": 2, "rows": 2, "step": 10, "aliases": "a, b, c"}))
        self.run_export(manifest, output=self.root / "first")
        first, self.helpers = self.helpers, []
        self.run_export(manifest, output=self.root / "second")
        self.assertEqual([text for _, text in first], [text for _, text in self.helpers])
        self.assertEqual((self.root / "first/edge.png").read_bytes(), (self.root / "second/edge.png").read_bytes())
        scene, edge, sheet = (text for _, text in first)
        self.assertIn('<svg width="200" height="100" viewBox="-10 0 100 50" preserveAspectRatio="none"'
                      ' xmlns="http://www.w3.org/2000/svg"', scene)
        self.assertIn(' inkscape:export-width="9"><rect', scene)
        self.assertNotIn('"1000"', scene)
        self.assertIn('width="50" height="40" viewBox="-10 10 25 20" preserveAspectRatio="none"', edge)
        self.assertIn('<rect width="5" height="5"/>', edge)
        self.assertEqual(['translate(0 0)', 'translate(10 0)', 'translate(0 10)'],
                         re.findall(r'transform="([^"]+)"', sheet))
        self.assertIn('viewBox="0 0 20 20"', sheet)
        self.assertFalse(list((self.root / "first").glob("*.svg")))

    def test_check_compares_without_writing_and_validates_preserved_rasters(self):
        manifest = self.manifest(
            entry("icon.png", source="icon.svg"), entry("new.png", source="icon.svg"),
            entry("bird.png", render="preserved", format="gif", size=[4, 3], reason="animation"),
            entry("swords.png", render="preserved", format="gif", size=[4, 3], reason="raster"))
        runtime = self.root / "runtime"
        (runtime / "icon.png").write_bytes(png(10, 10))
        gif = b"GIF89a" + struct.pack("<HH", 4, 3) + b"frames"
        (runtime / "bird.png").write_bytes(gif)
        (runtime / "swords.png").write_bytes(png(4, 3))
        count, stale, directory = self.run_export(manifest, check=True)
        self.assertEqual((2, None), (count, directory))
        self.assertEqual(["new.png: missing runtime image", "swords.png: expected gif content, found png"], stale)
        self.assertEqual(gif, (runtime / "bird.png").read_bytes())
        self.assertEqual(["bird.png", "icon.png", "swords.png"], sorted(p.name for p in runtime.iterdir()))
        (runtime / "icon.png").write_bytes(png(10, 10) + b"drift")
        _, stale, directory = self.run_export(manifest, groups=["icons"], check=True, output=self.root / "fresh")
        self.assertIn("icon.png: differs from the fresh export", stale)
        self.assertEqual(png(10, 10), (directory / "icon.png").read_bytes())
        with self.assertRaisesRegex(ValueError, "--install"):
            self.run_export(manifest, install=True, check=True)


if __name__ == "__main__":
    unittest.main()
