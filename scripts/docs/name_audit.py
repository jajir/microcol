"""Report leftovers of the former product name in tracked paths and contents.

The GitHub repository keeps its former name until the owner renames it, so
the only accepted occurrences are spans that address that repository: its
identifier, the Pages base, and links that pin a historical revision or a
published historical release asset. Each occurrence is checked separately;
files and lines are never exempted as a whole.
"""

import re
import subprocess
import sys
from pathlib import Path

REPOSITORY = "jajir/microcol"
LEGACY_NAME = REPOSITORY.split("/")[1]
# The former name plus the one-letter misspelling once used in the parent POM.
LEGACY = re.compile(LEGACY_NAME[0] + "[io]" + LEGACY_NAME[2:], re.IGNORECASE)

# A repository or Pages segment must end at the name, so suffixed names such as
# local module paths after the identifier are still reported.
_REPO = re.escape(REPOSITORY) + r"(?![\w-])"
_TAIL = r"[^\s)\]\"'<>`|,;}]*"
ALLOWED = re.compile("|".join([
    r"github\.com/" + _REPO + r"/(?:blob|tree|raw|commit)/[0-9a-f]{6,40}/" + _TAIL,
    r"raw\.githubusercontent\.com/" + _REPO + r"/[0-9a-f]{6,40}/" + _TAIL,
    r"github\.com/" + _REPO + r"/releases/download/dev-\d+-[0-9a-f]{12}/" + _TAIL,
    re.escape(REPOSITORY.split("/")[0]) + r"\.github\.io/" + LEGACY_NAME + r"(?![\w-])",
    _REPO,
]))


def residuals(text):
    """Return (offset, word) for every occurrence outside an allowed span."""
    allowed = [match.span() for match in ALLOWED.finditer(text)]
    return [(match.start(), match.group(0))
            for match in LEGACY.finditer(text)
            if not any(start <= match.start() and match.end() <= end
                       for start, end in allowed)]


def audit(root):
    """Yield one report line per residual in the tracked, present files."""
    listed = subprocess.run(
        ["git", "ls-files", "-z", "--cached", "--others", "--exclude-standard"],
        cwd=root, check=True, capture_output=True).stdout.decode("utf-8")
    for name in sorted(set(filter(None, listed.split("\0")))):
        path = Path(root, name)
        if not path.is_file():
            continue
        for _, word in residuals(name):
            yield f"{name}: path contains {word}"
        text = path.read_bytes().decode("utf-8", errors="replace")
        for offset, word in residuals(text):
            line = text.count("\n", 0, offset) + 1
            yield f"{name}:{line}: {word}"


def main(argv):
    root = argv[1] if len(argv) > 1 else "."
    found = list(audit(root))
    for report in found:
        print(report)
    print(f"{len(found)} unexplained occurrence(s) of the former name")
    return 1 if found else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
