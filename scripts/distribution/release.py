#!/usr/bin/env python3
"""Publish complete immutable build assets before changing the small dev pointer."""

import argparse
import hashlib
import os
from pathlib import Path
import re
import subprocess
import tempfile
import json


PLATFORMS = ("macos-arm64", "macos-x86_64", "linux-x86_64")
BOOTSTRAPS = ("MicroCol-Dev-macos.zip", "MicroCol-Dev-linux.tar.gz")
MANIFEST = "manifest.tsv"
BUILD_RE = re.compile(r"dev-([1-9][0-9]*)-([0-9a-f]{12})\Z")


def validate_build(build, sha):
    match = BUILD_RE.fullmatch(build)
    if not match or not re.fullmatch(r"[0-9a-f]{40}", sha) or match[2] != sha[:12]:
        raise ValueError("Invalid build identifier or source revision")
    return int(match[1])


def archive_name(platform, build):
    extension = ".tar.gz" if platform == "linux-x86_64" else ".zip"
    return "MicroCol-" + platform + "-" + build + extension


def required_names(build):
    result = set(BOOTSTRAPS)
    for platform in PLATFORMS:
        result.add(archive_name(platform, build))
        extension = ".deb" if platform == "linux-x86_64" else ".dmg"
        result.add("MicroCol-" + platform + "-" + build + extension)
    return result


def digest(path):
    checksum = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            checksum.update(block)
    return checksum.hexdigest()


def manifest_text(directory, build, sha):
    validate_build(build, sha)
    lines = ["microcol-dev-v1", "build\t" + build + "\t" + sha]
    for platform in PLATFORMS:
        filename = archive_name(platform, build)
        lines.append("\t".join(("asset", platform, filename, digest(directory / filename))))
    return "\n".join(lines) + "\n"


def parse_manifest(text):
    lines = text.splitlines()
    if len(lines) != 5 or lines[0] != "microcol-dev-v1":
        raise ValueError("Invalid development manifest header or row count")
    build_row = lines[1].split("\t")
    if len(build_row) != 3 or build_row[0] != "build":
        raise ValueError("Invalid development manifest build row")
    build, sha = build_row[1:]
    validate_build(build, sha)
    assets = {}
    for line in lines[2:]:
        row = line.split("\t")
        if len(row) != 4 or row[0] != "asset" or row[1] not in PLATFORMS:
            raise ValueError("Invalid development manifest asset row")
        _, platform, filename, checksum = row
        if platform in assets or filename != archive_name(platform, build):
            raise ValueError("Duplicate platform or unexpected asset filename")
        if not re.fullmatch(r"[0-9a-f]{64}", checksum):
            raise ValueError("Invalid asset checksum")
        assets[platform] = (filename, checksum)
    if set(assets) != set(PLATFORMS):
        raise ValueError("Development manifest is missing a platform")
    return build, sha, assets


def prepare_assets(directory, build, sha):
    validate_build(build, sha)
    existing = {p.name for p in directory.iterdir()}
    expected = required_names(build)
    if existing - {MANIFEST, "SHA256SUMS"} != expected:
        raise ValueError("Incomplete or unexpected release assets: " + str(existing ^ expected))
    for filename in expected:
        path = directory / filename
        if not path.is_file() or path.is_symlink() or not path.stat().st_size:
            raise ValueError("Release assets must be nonempty regular files: " + filename)
    (directory / MANIFEST).write_text(manifest_text(directory, build, sha), encoding="ascii")
    checksums = {name: digest(directory / name) for name in expected | {MANIFEST}}
    (directory / "SHA256SUMS").write_text(
        "".join(checksums[name] + "  " + name + "\n" for name in sorted(checksums)), encoding="ascii")
    checksums["SHA256SUMS"] = digest(directory / "SHA256SUMS")
    return checksums


def should_promote(build, sha, main_sha, previous_manifest):
    number = validate_build(build, sha)
    if sha != main_sha:
        return False
    if previous_manifest:
        previous_build, previous_sha, _ = parse_manifest(previous_manifest)
        # A rerun of an older workflow must never replace a newer successful build.
        return number > validate_build(previous_build, previous_sha)
    return True


class GitHub:
    def __init__(self, repository):
        if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repository):
            raise ValueError("Invalid GitHub repository")
        self.repository = repository

    def run(self, *arguments):
        result = subprocess.run(["gh", *arguments], text=True, capture_output=True)
        if result.returncode:
            raise RuntimeError(result.stderr.strip())
        return result.stdout

    def api(self, path, missing_ok=False):
        try:
            return json.loads(self.run("api", "repos/" + self.repository + "/" + path))
        except RuntimeError as error:
            if missing_ok and "HTTP 404" in str(error):
                return None
            raise

    def release(self, tag):
        result = self.api("releases/tags/" + tag, missing_ok=True)
        if result is not None:
            return result
        # REST's tag lookup only finds published releases. The CLI can resolve
        # drafts; fetch that release by ID so interrupted uploads can be repaired.
        try:
            metadata = json.loads(self.command("view", tag, "--json", "databaseId"))
        except RuntimeError as error:
            if str(error) == "release not found":
                return None
            raise
        return self.api("releases/" + str(metadata["databaseId"]))

    def command(self, *arguments):
        return self.run("release", *arguments, "--repo", self.repository)

    def download(self, tag, name, output):
        self.command("download", tag, "--pattern", name, "--output", str(output), "--clobber")

    def previous_manifest(self, directory):
        release = self.release("dev")
        if release is None or not any(a["name"] == MANIFEST for a in release["assets"]):
            return None
        output = directory / "previous-manifest.tsv"
        self.download("dev", MANIFEST, output)
        return output.read_text(encoding="ascii")

    def verify_uploads(self, release, checksums, directory):
        if release is None:
            raise ValueError("Uploaded draft release could not be found for verification")
        assets = {asset["name"]: asset for asset in release["assets"]}
        if set(assets) != set(checksums):
            raise ValueError("Remote release asset set is incomplete or unexpected")
        for name, checksum in checksums.items():
            if assets[name].get("digest") == "sha256:" + checksum:
                continue
            # Some GitHub API responses omit digest; verify the actual uploaded bytes.
            downloaded = directory / ("verify-" + name)
            self.download(release["tag_name"], name, downloaded)
            if digest(downloaded) != checksum:
                raise ValueError("Remote release checksum mismatch: " + name)
            downloaded.unlink()


def notes(build, sha, repository):
    return ("Development build `" + build + "` from `" + sha + "`.\n\n"
            "All three native builds and their game unit tests completed successfully. "
            "macOS packages are unsigned and are not notarized.\n\n"
            "Install the one-time **MicroCol Dev** launcher to receive the latest successful "
            "development build when starting the game. Download manual DMG/DEB installers from "
            "[this complete build release](https://github.com/" + repository + "/releases/tag/" + build + ").\n\n"
            "[Installation instructions](https://" + repository.split("/")[0] + ".github.io/" +
            repository.split("/")[1] + "/development-builds.html)\n")


def publish(directory, build, sha, github):
    checksums = prepare_assets(directory, build, sha)
    with tempfile.TemporaryDirectory(prefix="microcol-release-") as temporary:
        work = Path(temporary)
        description = work / "notes.md"
        description.write_text(notes(build, sha, github.repository), encoding="utf-8")
        release = github.release(build)
        if release and not release["draft"]:
            # A completed release is immutable by policy. Reruns use its original bytes.
            original = work / "original"
            original.mkdir()
            github.command("download", build, "--dir", str(original))
            original_manifest = (original / MANIFEST).read_text(encoding="ascii")
            original_build, original_sha, assets = parse_manifest(original_manifest)
            if (original_build, original_sha) != (build, sha):
                raise ValueError("Existing build tag belongs to another revision")
            for filename, checksum in assets.values():
                if digest(original / filename) != checksum:
                    raise ValueError("Existing release archive checksum mismatch")
            # Regenerate checksums only after verifying the downloaded manifest against its archives.
            original_checksums = (original / "SHA256SUMS").read_bytes()
            checksums = prepare_assets(original, build, sha)
            if (original / "SHA256SUMS").read_bytes() != original_checksums:
                raise ValueError("Existing release checksums do not match its assets")
            directory = original
        else:
            if release is None:
                github.command("create", build, "--target", sha, "--prerelease", "--draft",
                               "--title", "MicroCol " + build, "--notes-file", str(description))
            # Only drafts can be repaired in place; incomplete builds never reach the channel.
            github.command("upload", build, *[str(directory / name) for name in sorted(checksums)], "--clobber")
            github.verify_uploads(github.release(build), checksums, work)
            github.command("edit", build, "--draft=false", "--prerelease", "--latest=false")

        main_sha = github.api("git/ref/heads/main")["object"]["sha"]
        previous = github.previous_manifest(work)
        if not should_promote(build, sha, main_sha, previous):
            print("Built " + build + "; keeping the newer/current development channel unchanged.")
            return False
        if github.release("dev") is None:
            github.command("create", "dev", "--target", sha, "--prerelease", "--latest=false",
                           "--title", "MicroCol development channel", "--notes-file", str(description))
        github.command("upload", "dev", *[str(directory / name) for name in BOOTSTRAPS], "--clobber")
        # A main push may arrive during the large uploads. Check again before replacing the pointer.
        main_sha = github.api("git/ref/heads/main")["object"]["sha"]
        if not should_promote(build, sha, main_sha, github.previous_manifest(work)):
            print("Main advanced during publication; leaving the development manifest unchanged.")
            return False
        github.command("upload", "dev", str(directory / MANIFEST), "--clobber")
        github.command("edit", "dev", "--title", "MicroCol development channel",
                       "--notes-file", str(description), "--prerelease", "--latest=false")
        print("Promoted development channel to " + build)
        summary = os.environ.get("GITHUB_STEP_SUMMARY")
        if summary:
            with open(summary, "a", encoding="utf-8") as stream:
                stream.write("Published [" + build + "](https://github.com/" + github.repository +
                             "/releases/tag/" + build + ") and updated **MicroCol Dev**.\n")
        return True


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    parser.add_argument("build")
    parser.add_argument("sha")
    parser.add_argument("--repository", required=True)
    args = parser.parse_args()
    publish(args.directory, args.build, args.sha, GitHub(args.repository))
