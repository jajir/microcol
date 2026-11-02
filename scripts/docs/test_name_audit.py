"""Check which occurrences of the former name the audit accepts."""

import unittest

import name_audit

OLD = name_audit.LEGACY_NAME
REPO = "https://github.com/" + name_audit.REPOSITORY


class NameAuditTest(unittest.TestCase):
    def words(self, text):
        return [word for _, word in name_audit.residuals(text)]

    def test_brand_paths_and_misspelling_are_reported(self):
        self.assertEqual(self.words(f"Welcome to {OLD.title()}"), [OLD.title()])
        self.assertEqual(self.words(f"{OLD}-game/pom.xml"), [OLD])
        self.assertEqual(self.words(f"{OLD.upper()}_DEV_ROOT"), [OLD.upper()])
        misspelled = OLD[0] + "o" + OLD[2:]
        self.assertEqual(self.words(misspelled), [misspelled])

    def test_repository_identity_and_pages_base_are_accepted(self):
        for text in (f"{REPO}/issues", f"{REPO}.git", "gh -R jajir/" + OLD,
                     "https://api.github.com/repos/jajir/" + OLD + "/releases",
                     "https://jajir.github.io/" + OLD + "/use-cases.html"):
            with self.subTest(text=text):
                self.assertEqual(self.words(text), [])

    def test_identifier_spans_do_not_cover_suffixed_names(self):
        self.assertEqual(self.words(f"jajir/{OLD}-game"), [OLD])
        self.assertEqual(self.words(f"https://jajir.github.io/{OLD}-game/"), [OLD])
        self.assertEqual(self.words(f"https://jajir.github.io/{OLD}/{OLD}-game/"), [OLD])
        self.assertEqual(self.words(f"{REPO}/blob/main/README.md"), [])

    def test_historical_paths_are_accepted_only_inside_pinned_links(self):
        pinned = f"{REPO}/blob/062f8ce9/{OLD}-game/A.java"
        self.assertEqual(self.words(f"[A]({pinned})"), [])
        self.assertEqual(self.words(f"[A]({REPO}/blob/main/{OLD}-game/A.java)"),
                         [OLD])
        self.assertEqual(self.words(f"[A]({pinned}) in {OLD}-game"), [OLD])

    def test_only_historical_release_assets_keep_their_names(self):
        old = f"{REPO}/releases/download/dev-54-13001e6ac6b8/{OLD.title()}.zip"
        current = f"{REPO}/releases/download/dev/{OLD.title()}-Dev-macos.zip"
        self.assertEqual(self.words(old), [])
        self.assertEqual(self.words(current), [OLD.title()])


if __name__ == "__main__":
    unittest.main()
