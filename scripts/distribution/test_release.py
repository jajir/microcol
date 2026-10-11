"""Protect the update contract and promotion ordering without publishing anything."""

import copy
import hashlib
from pathlib import Path
import tempfile
import tarfile
import unittest
import xml.etree.ElementTree as ET
import zipfile

import release
import stage


SHA = "a" * 40
BUILD = "dev-10-" + SHA[:12]


def create_assets(directory, build=BUILD):
    for name in release.required_names(build):
        (directory / name).write_bytes(("test bytes: " + name).encode("ascii"))


class FakeGitHub(release.GitHub):
    def __init__(self):
        super().__init__("jajir/microcol")
        self.releases = {}
        self.events = []
        self.main_sha = SHA
        self.fail_upload = False
        self.advance_main_after_bootstrap = False

    def release(self, tag):
        item = self.releases.get(tag)
        if item is None:
            return None
        return {"tag_name": tag, "draft": item["draft"], "assets": [
            {"name": name, "digest": "sha256:" + hashlib.sha256(data).hexdigest()}
            for name, data in item["files"].items()]}

    def api(self, path, missing_ok=False):
        if path == "git/ref/heads/main":
            return {"object": {"sha": self.main_sha}}
        raise AssertionError(path)

    def download(self, tag, name, output):
        output.write_bytes(self.releases[tag]["files"][name])

    def command(self, operation, tag, *arguments):
        self.events.append((operation, tag, arguments))
        if operation == "create":
            self.releases[tag] = {"draft": "--draft" in arguments, "files": {}}
        elif operation == "upload":
            if self.fail_upload:
                raise RuntimeError("simulated interrupted upload")
            for argument in arguments:
                if not argument.startswith("--"):
                    path = Path(argument)
                    self.releases[tag]["files"][path.name] = path.read_bytes()
            if tag == "dev" and release.MANIFEST not in self.releases[tag]["files"] and self.advance_main_after_bootstrap:
                self.main_sha = "b" * 40
        elif operation == "edit":
            if "--draft=false" in arguments:
                self.releases[tag]["draft"] = False
        elif operation == "download":
            output = Path(arguments[arguments.index("--dir") + 1])
            for name, data in self.releases[tag]["files"].items():
                (output / name).write_bytes(data)
        else:
            raise AssertionError(operation)
        return ""


class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        create_assets(self.directory)

    def test_manifest_round_trip_requires_all_platforms_and_exact_filenames(self):
        release.prepare_assets(self.directory, BUILD, SHA)
        text = (self.directory / release.MANIFEST).read_text()
        build, sha, assets = release.parse_manifest(text)
        self.assertEqual((build, sha), (BUILD, SHA))
        self.assertEqual(set(assets), set(release.PLATFORMS))
        invalid = [text.replace("macos-x86_64\t", "macos-arm64\t"),
                   text.replace("MicroCol-macos-arm64-", "../MicroCol-macos-arm64-"),
                   "\n".join(text.splitlines()[:-1]) + "\n",
                   text.replace("a" * 40, "b" * 40)]
        for candidate in invalid:
            with self.subTest(candidate=candidate), self.assertRaises(ValueError):
                release.parse_manifest(candidate)

    def test_missing_platform_or_extra_asset_prevents_publication(self):
        (self.directory / release.archive_name("linux-x86_64", BUILD)).unlink()
        with self.assertRaises(ValueError):
            release.prepare_assets(self.directory, BUILD, SHA)

    def test_pointer_is_last_and_references_a_complete_published_release(self):
        github = FakeGitHub()
        self.assertTrue(release.publish(self.directory, BUILD, SHA, github))
        self.assertFalse(github.releases[BUILD]["draft"])
        self.assertEqual(set(github.releases[BUILD]["files"]),
                         release.required_names(BUILD) | {release.MANIFEST, "SHA256SUMS"})
        self.assertEqual(github.releases["dev"]["files"][release.MANIFEST],
                         github.releases[BUILD]["files"][release.MANIFEST])
        uploads = [event for event in github.events if event[0] == "upload"]
        self.assertEqual(uploads[-1][1], "dev")
        self.assertEqual(Path(uploads[-1][2][0]).name, release.MANIFEST)

    def test_failed_upload_never_creates_channel_manifest(self):
        github = FakeGitHub()
        github.fail_upload = True
        with self.assertRaises(RuntimeError):
            release.publish(self.directory, BUILD, SHA, github)
        self.assertNotIn("dev", github.releases)
        self.assertTrue(github.releases[BUILD]["draft"])

    def test_obsolete_revision_cannot_change_channel(self):
        github = FakeGitHub()
        github.main_sha = "b" * 40
        self.assertFalse(release.publish(self.directory, BUILD, SHA, github))
        self.assertNotIn("dev", github.releases)
        self.assertFalse(github.releases[BUILD]["draft"])

    def test_newer_promoted_run_cannot_be_rolled_back(self):
        previous = release.manifest_text(self.directory, BUILD, SHA)
        self.assertFalse(release.should_promote("dev-9-" + SHA[:12], SHA, SHA, previous))
        self.assertFalse(release.should_promote(BUILD, SHA, SHA, previous))
        self.assertTrue(release.should_promote("dev-11-" + SHA[:12], SHA, SHA, previous))

    def test_main_change_during_upload_prevents_pointer_change(self):
        github = FakeGitHub()
        github.advance_main_after_bootstrap = True
        self.assertFalse(release.publish(self.directory, BUILD, SHA, github))
        self.assertNotIn(release.MANIFEST, github.releases["dev"]["files"])

    def test_rerun_keeps_original_immutable_bytes(self):
        github = FakeGitHub()
        release.publish(self.directory, BUILD, SHA, github)
        original = copy.deepcopy(github.releases)
        (self.directory / release.archive_name("macos-arm64", BUILD)).write_bytes(b"new timestamp bytes")
        self.assertFalse(release.publish(self.directory, BUILD, SHA, github))
        self.assertEqual(github.releases, original)

    def test_corrupt_existing_release_is_not_promoted(self):
        github = FakeGitHub()
        release.publish(self.directory, BUILD, SHA, github)
        del github.releases["dev"]
        github.releases[BUILD]["files"][release.archive_name("macos-arm64", BUILD)] = b"corrupt"
        with self.assertRaises(ValueError):
            release.publish(self.directory, BUILD, SHA, github)
        self.assertNotIn("dev", github.releases)

    def test_missing_skipped_and_failed_test_reports_reject_build(self):
        reports = self.directory / "reports"
        reports.mkdir()
        with self.assertRaises(ValueError):
            stage.check_reports(reports)
        for tests, skipped, failures in [(0, 0, 0), (2, 2, 0), (2, 0, 1)]:
            root = ET.Element("testsuite", tests=str(tests), skipped=str(skipped), failures=str(failures))
            ET.ElementTree(root).write(reports / "TEST-fixture.xml")
            with self.assertRaises(ValueError):
                stage.check_reports(reports)
        ET.ElementTree(ET.Element("testsuite", tests="601", failures="0", skipped="0")).write(reports / "TEST-fixture.xml")
        self.assertEqual(stage.check_reports(reports)["tests"], 601)

    def test_unsafe_or_link_archive_members_reject_build(self):
        bundle = self.directory / "app.zip"
        for name, mode in [("../outside", 0o100644), ("/absolute", 0o100644),
                           ("App/link", 0o120777), ("App/control\n", 0o100644)]:
            member = zipfile.ZipInfo(name)
            member.external_attr = mode << 16
            with zipfile.ZipFile(bundle, "w") as archive:
                archive.writestr(member, b"target")
            with self.subTest(name=name), self.assertRaises(ValueError):
                stage.check_archive(bundle)
        tar = self.directory / "app.tar.gz"
        with tarfile.open(tar, "w:gz") as archive:
            link = tarfile.TarInfo("App/link")
            link.type = tarfile.SYMTYPE
            link.linkname = "../../outside"
            archive.addfile(link)
        with self.assertRaises(ValueError):
            stage.check_archive(tar)


if __name__ == "__main__":
    unittest.main()
