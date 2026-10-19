import base64
import importlib.util
import io
from pathlib import Path
import re
import subprocess
import sys
import unittest
import xml.etree.ElementTree as ET

from fontTools.ttLib import TTFont

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('svg_font', HERE / 'svg-font.py')
svg_font = importlib.util.module_from_spec(spec)
spec.loader.exec_module(svg_font)


class SvgFontTest(unittest.TestCase):
    def test_embedded_face_preserves_letters_license_and_repeatability(self):
        source = ('<svg xmlns="http://www.w3.org/2000/svg"><defs/>'
                  '<text style="font-family:sans-serif;font-weight:bold;'
                  'font-style:italic">Příliš Ĳ €</text></svg>')
        result = svg_font.update(source)
        self.assertEqual(result, svg_font.update(result))
        self.assertIn("font-family:'Playpen Sans Light'", result)
        self.assertNotIn('font-weight:bold', result)
        self.assertNotIn('font-style:italic', result)
        ET.fromstring(result)
        data = base64.b64decode(re.search(r'base64,([A-Za-z0-9+/=]+)', result)[1])
        font = TTFont(io.BytesIO(data))
        self.assertEqual(300, font['OS/2'].usWeightClass)
        self.assertTrue(all(ord(c) in font.getBestCmap() for c in 'PřílišĲ€'))
        self.assertTrue(any('Open Font License' in n.toUnicode()
                            for n in font['name'].names if n.nameID == 13))

    def test_no_text_is_unchanged_and_missing_glyph_fails(self):
        source = '<svg xmlns="http://www.w3.org/2000/svg"><defs/><path d="M0 0"/></svg>'
        self.assertEqual(source, svg_font.update(source))
        with self.assertRaises(ValueError):
            svg_font.update('<svg xmlns="http://www.w3.org/2000/svg"><defs/><text>漢</text></svg>')

    def test_export_environment_contains_only_bundled_face(self):
        command = [sys.executable, str(HERE / 'with-fonts.py')]
        files = subprocess.check_output(command + ['fc-list', '-f', '%{file}\n'], text=True)
        self.assertEqual([str(svg_font.FONT)], files.splitlines())
        probe = 'import os,sys; sys.exit(7 if os.environ["PANGOCAIRO_BACKEND"] == "fc" else 8)'
        self.assertEqual(7, subprocess.run(command + [sys.executable, '-c', probe]).returncode)


if __name__ == '__main__':
    unittest.main()
