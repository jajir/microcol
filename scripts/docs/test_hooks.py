"""Check source links that must survive moving Markdown onto GitHub Pages."""

import tempfile
import unittest
from pathlib import Path

from hooks import repository_url


class RepositoryLinksTest(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.addCleanup(self.tmp.cleanup)
        self.root = Path(self.tmp.name)
        self.repo = self.root / "repo"
        self.docs = self.repo / "docs"
        (self.docs / "ai").mkdir(parents=True)
        (self.repo / "game").mkdir()
        (self.repo / "pom.xml").touch()
        (self.repo / "game" / "a b.svg").touch()
        (self.repo / "game" / "Main.java").touch()

    def rewrite(self, href, page="index.md", **kwargs):
        return repository_url(
            href,
            source_file=self.docs / page,
            docs_dir=self.docs,
            repo_dir=self.repo,
            repo_url="https://github.com/jajir/microcol/",
            revision="abc123",
            **kwargs,
        )

    def test_web_urls_anchors_and_docs_assets_are_unchanged(self):
        for href in (
            "https://example.com/a?q=1#x", "mailto:jan@example.com",
            "//example.com/a", "/microcol/legacy/", "#section", "?q=1",
            "modules.md#module-overview", "images/a%20b.png", "",
        ):
            with self.subTest(href=href):
                self.assertEqual(self.rewrite(href), href)
        self.assertEqual(self.rewrite("../modules.md", "ai/worker.md"), "../modules.md")

    def test_repository_source_from_root_and_nested_pages(self):
        self.assertEqual(
            self.rewrite("../pom.xml"),
            "https://github.com/jajir/microcol/blob/abc123/pom.xml",
        )
        self.assertEqual(
            self.rewrite("../../game/Main.java?plain=1#L7", "ai/worker.md"),
            "https://github.com/jajir/microcol/blob/abc123/game/Main.java?plain=1#L7",
        )

    def test_directory_uses_tree_url(self):
        self.assertEqual(
            self.rewrite("../game/"),
            "https://github.com/jajir/microcol/tree/abc123/game",
        )

    def test_image_uses_raw_content_and_preserves_encoding(self):
        self.assertEqual(
            self.rewrite("../game/a%20b.svg?raw=1#view", image=True),
            "https://raw.githubusercontent.com/jajir/microcol/abc123/game/a%20b.svg?raw=1#view",
        )

    def test_missing_repository_file_fails(self):
        with self.assertRaises(ValueError):
            self.rewrite("../game/Missing.java")

    def test_outside_repository_and_symlink_escape_fail(self):
        outside = self.root / "outside.txt"
        outside.touch()
        (self.repo / "escape.txt").symlink_to(outside)
        for href in ("../../outside.txt", "../escape.txt"):
            with self.subTest(href=href), self.assertRaises(ValueError):
                self.rewrite(href)


if __name__ == "__main__":
    unittest.main()
