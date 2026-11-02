import importlib.util
from pathlib import Path
import struct
import unittest
import zlib

HERE = Path(__file__).resolve().parent
spec = importlib.util.spec_from_file_location('legacy_icons', HERE / 'export-legacy-icons.py')
legacy_icons = importlib.util.module_from_spec(spec)
spec.loader.exec_module(legacy_icons)


def png(width, height):
    def chunk(kind, body):
        return (struct.pack('>I', len(body)) + kind + body
                + struct.pack('>I', zlib.crc32(kind + body)))
    rows = b''.join(b'\0' + b'\0' * 4 * width for _ in range(height))
    return (legacy_icons.PNG_SIGNATURE
            + chunk(b'IHDR', struct.pack('>IIBBBBB', width, height, 8, 6, 0, 0, 0))
            + chunk(b'IDAT', zlib.compress(rows)) + chunk(b'IEND', b''))


class LegacyIconTest(unittest.TestCase):
    def test_ico_directory_points_at_each_png(self):
        images = [png(256, 256), png(16, 16)]
        data = legacy_icons.ico_bytes(images)
        self.assertEqual((0, 1, 2), struct.unpack('<3H', data[:6]))
        for index, image in enumerate(images):
            width, height, _, _, planes, bits, size, offset = struct.unpack(
                '<4B2H2I', data[6 + 16 * index:22 + 16 * index])
            side = legacy_icons.png_size(image)[0]
            self.assertEqual((side % 256, side % 256, 1, 32), (width, height, planes, bits))
            self.assertEqual(image, data[offset:offset + size])

    def test_non_square_or_non_png_images_are_rejected(self):
        with self.assertRaises(ValueError):
            legacy_icons.ico_bytes([png(32, 16)])
        with self.assertRaises(ValueError):
            legacy_icons.ico_bytes([png(512, 512)])
        with self.assertRaises(ValueError):
            legacy_icons.png_size(b'GIF89a' + b'\0' * 20)

    def test_icns_set_uses_the_standard_iconutil_sizes(self):
        self.assertEqual((16, 32, 128, 256, 512), legacy_icons.ICNS_SIZES)
        self.assertTrue(legacy_icons.SOURCE.is_file())


if __name__ == '__main__':
    unittest.main()
