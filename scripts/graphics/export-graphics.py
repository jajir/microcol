#!/usr/bin/env python3
"""Export maintained SVG graphics listed in the export manifest.

By default the selected groups (all when none are named) are rendered into a
new temporary directory. --output selects a new or empty directory outside the
source and runtime trees. --check compares fresh exports with the committed
runtime images without writing them; with --output the fresh exports are kept
for inspection. --install replaces the selected runtime images only after
every selected export passed validation.
"""
import argparse
import fnmatch
import json
from pathlib import Path, PurePosixPath
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / "akropia-game/src/graphics/export-manifest.json"
WITH_FONTS = Path(__file__).with_name("with-fonts.py")
SVG = "{http://www.w3.org/2000/svg}"
RENDERS = ("native", "size", "zoom", "viewbox", "crop", "atlas", "preserved")
ROOT_TAG = re.compile(r"<svg(?=[\s>/])[^>]*>")
FRAME = re.compile(r"""\s(?:width|height|viewBox|preserveAspectRatio)=(?:"[^"]*"|'[^']*')""")


def relative(value, field):
    if not isinstance(value, str) or not value or "\\" in value:
        raise ValueError(f"Manifest {field} must be a relative POSIX path: {value!r}")
    path = PurePosixPath(value)
    if path.is_absolute() or ".." in path.parts:
        raise ValueError(f"Unsafe manifest path in {field}: {value}")
    return value


def integers(value, count, field, minimum=1):
    if (not isinstance(value, list) or len(value) != count
            or not all(type(v) is int and v >= minimum for v in value)):
        raise ValueError(f"Manifest {field} must be {count} integers >= {minimum}: {value!r}")
    return value


def check_entry(group, entry):
    if not isinstance(entry, dict):
        raise ValueError(f"Manifest group {group} has a non-object entry")
    where = f"{group}/{entry.get('output')}"
    output = relative(entry.get("output"), where + " output")
    render = entry.get("render")
    if render not in RENDERS:
        raise ValueError(f"Manifest entry {where} has unknown render {render!r}")
    for field in ("consumer", "display", "stroke"):
        if not isinstance(entry.get(field), str) or not entry[field].strip():
            raise ValueError(f"Manifest entry {where} needs {field}")
    width, height = integers(entry.get("size"), 2, where + " size")
    pattern = entry["source"] if isinstance(entry.get("source"), str) else ""
    if ("*" in output) != ("*" in pattern) or output.count("*") > 1:
        raise ValueError(f"Manifest entry {where} must pair one * in source and output")
    if "*" in output and render not in ("native", "size", "zoom"):
        raise ValueError(f"Manifest entry {where} cannot expand a pattern with {render}")
    if render == "preserved":
        if entry.get("format") not in ("png", "gif") or not str(entry.get("reason", "")).strip():
            raise ValueError(f"Manifest entry {where} needs format png/gif and a reason")
    elif render == "crop":
        relative(entry.get("of"), where + " of")
        if integers(entry.get("crop"), 4, where + " crop", 0)[2:] != [width, height]:
            raise ValueError(f"Manifest entry {where} crop size differs from size")
    elif render == "atlas":
        sources = entry.get("sources")
        if not isinstance(sources, list) or not sources:
            raise ValueError(f"Manifest entry {where} needs sources")
        for source in sources:
            relative(source, where + " sources")
        integers([entry.get("sourceCell")], 1, where + " sourceCell")
        if not isinstance(entry.get("atlas"), dict):
            raise ValueError(f"Manifest entry {where} needs atlas geometry")
    else:
        relative(entry.get("source"), where + " source")
    if render == "zoom" and not (type(entry.get("zoom")) is int and entry["zoom"] > 0):
        raise ValueError(f"Manifest entry {where} needs a positive integer zoom")
    if render == "viewbox":
        box = entry.get("viewBox")
        if (not isinstance(box, list) or len(box) != 4
                or not all(type(v) in (int, float) for v in box) or min(box[2:]) <= 0):
            raise ValueError(f"Manifest entry {where} needs viewBox [x, y, width, height]")
    atlas = entry.get("atlas")
    if atlas is not None:
        if not isinstance(atlas, dict) or not str(atlas.get("aliases", "")).strip():
            raise ValueError(f"Manifest entry {where} atlas needs geometry and aliases")
        columns, rows, step = integers(
            [atlas.get("columns"), atlas.get("rows"), atlas.get("step")], 3, where + " atlas")
        if integers([atlas.get("cell", step)], 1, where + " atlas cell")[0] > step:
            raise ValueError(f"Manifest entry {where} atlas cell exceeds its step")
        if [columns * step, rows * step] != [width, height]:
            raise ValueError(f"Manifest entry {where} atlas {columns}x{rows} step {step}"
                             f" does not match size {width}x{height}")
        if render == "atlas" and len(entry["sources"]) > columns * rows:
            raise ValueError(f"Manifest entry {where} has more sources than atlas cells")


def load(path=None):
    path = Path(MANIFEST if path is None else path)
    try:
        manifest = json.loads(path.read_text(encoding="utf-8"))
    except FileNotFoundError:
        raise ValueError(f"Missing graphics export manifest: {path}") from None
    except json.JSONDecodeError as error:
        raise ValueError(f"Invalid graphics export manifest {path}: {error}") from None
    if not isinstance(manifest, dict):
        raise ValueError(f"Invalid graphics export manifest {path}: expected an object")
    for field in ("sourceRoot", "runtimeRoot"):
        relative(manifest.get(field), field)
    groups = manifest.get("groups")
    if not isinstance(groups, dict) or not groups:
        raise ValueError("Manifest has no export groups")
    owners = {}
    for name, group in groups.items():
        entries = group.get("outputs") if isinstance(group, dict) else None
        if not isinstance(entries, list) or not entries:
            raise ValueError(f"Manifest group {name} has no outputs")
        for entry in entries:
            check_entry(name, entry)
            if entry["output"] in owners:
                raise ValueError(f"Manifest output {entry['output']} is owned by both"
                                 f" {owners[entry['output']]} and {name}")
            owners[entry["output"]] = name
        by_output = {entry["output"]: entry for entry in entries}
        for entry in entries:
            if entry["render"] == "crop":
                parent = by_output.get(entry["of"])
                if parent is None or parent["render"] != "viewbox":
                    raise ValueError(f"Crop {entry['output']} needs a viewbox output"
                                     f" {entry['of']} in group {name}")
                x, y, width, height = entry["crop"]
                if x + width > parent["size"][0] or y + height > parent["size"][1]:
                    raise ValueError(f"Crop {entry['output']} lies outside {entry['of']}")
    return manifest


def vector(source, output):
    if not source.is_file():
        raise ValueError(f"Missing SVG source for {output}: {source}")
    try:
        tree = ET.parse(source).getroot()
    except ET.ParseError as error:
        raise ValueError(f"Invalid SVG source {source}: {error}") from None
    if tree.tag not in ("svg", SVG + "svg"):
        raise ValueError(f"Source {source} is not an SVG document")
    if any(node.tag in ("image", SVG + "image") for node in tree.iter()):
        raise ValueError(f"Source {source} embeds or links a bitmap;"
                         " it is not a maintained vector source")


def number(value):
    return f"{value + 0.0:.6f}".rstrip("0").rstrip(".")


def plan(manifest, root, names):
    groups = manifest["groups"]
    unknown = [name for name in names if name not in groups]
    if unknown:
        raise ValueError(f"Unknown export group(s): {', '.join(unknown)};"
                         f" available: {', '.join(groups)}")
    source_root = root / manifest["sourceRoot"]
    runtime = root / manifest["runtimeRoot"]
    jobs = []
    for name in names or list(groups):
        entries = {entry["output"]: entry for entry in groups[name]["outputs"]}
        for entry in entries.values():
            job = {"output": entry["output"], "entry": entry, "sources": []}
            if entry["render"] == "crop":
                parent = entries[entry["of"]]
                x, y, width, height = parent["viewBox"]
                scale_x, scale_y = width / parent["size"][0], height / parent["size"][1]
                left, top, crop_width, crop_height = entry["crop"]
                job["sources"] = [source_root / parent["source"]]
                job["frame"] = ([x + left * scale_x, y + top * scale_y,
                                 crop_width * scale_x, crop_height * scale_y],
                                parent.get("preserveAspectRatio"))
            elif entry["render"] == "viewbox":
                job["sources"] = [source_root / entry["source"]]
                job["frame"] = (entry["viewBox"], entry.get("preserveAspectRatio"))
            elif entry["render"] == "atlas":
                job["sources"] = [source_root / source for source in entry["sources"]]
            elif "source" in entry:
                job["sources"] = [source_root / entry["source"]]
            if "*" in entry["output"]:
                jobs += expand(job, source_root, runtime)
            else:
                jobs.append(job)
    for job in jobs:
        for source in job["sources"]:
            vector(source, job["output"])
    return jobs, runtime


def expand(job, source_root, runtime):
    entry = job["entry"]
    sources = sorted(source_root.glob(entry["source"]))
    if not sources:
        raise ValueError(f"No SVG sources match {entry['source']}")
    jobs = [dict(job, output=entry["output"].replace("*", source.stem), sources=[source])
            for source in sources]
    expected = {expanded["output"] for expanded in jobs}
    orphans = sorted(path.relative_to(runtime).as_posix()
                     for path in runtime.glob(entry["output"])
                     if path.relative_to(runtime).as_posix() not in expected)
    if orphans:
        raise ValueError("SVG and runtime inventories differ; runtime images without"
                         " an SVG source: " + ", ".join(orphans))
    return jobs


def reframe(text, size, box, aspect):
    match = ROOT_TAG.search(text)
    if not match:
        raise ValueError("SVG root element not found")
    rest = FRAME.sub("", match.group())[len("<svg"):]
    values = {"width": size[0], "height": size[1], "viewBox": " ".join(map(number, box))}
    if aspect:
        values["preserveAspectRatio"] = aspect
    attributes = "".join(f' {name}="{value}"' for name, value in values.items())
    return text[:match.start()] + "<svg" + attributes + rest + text[match.end():]


def atlas(job):
    entry, namespace = job["entry"], SVG[1:-1]
    columns, rows, cell = entry["atlas"]["columns"], entry["atlas"]["rows"], entry["sourceCell"]
    ET.register_namespace("", namespace)
    root = ET.Element(SVG + "svg", width=str(entry["size"][0]), height=str(entry["size"][1]),
                      viewBox=f"0 0 {columns * cell} {rows * cell}")
    for index, source in enumerate(job["sources"]):
        drawing = ET.parse(source).getroot().find(SVG + "g")
        if drawing is None:
            raise ValueError(f"Atlas source {source} has no top-level SVG group")
        group = ET.SubElement(root, SVG + "g", transform=(
            f"translate({index % columns * cell} {index // columns * cell})"))
        group.append(drawing)
    return ET.tostring(root, encoding="unicode")


def image_info(path):
    with open(path, "rb") as image:
        head = image.read(26)
    if head[:8] == b"\x89PNG\r\n\x1a\n" and head[12:16] == b"IHDR" and len(head) == 26:
        width, height = struct.unpack(">II", head[16:24])
        return "png", width, height, head[25] in (4, 6)
    if head[:6] in (b"GIF87a", b"GIF89a") and len(head) >= 10:
        width, height = struct.unpack("<HH", head[6:10])
        return "gif", width, height, None
    return None, None, None, None


def validate(job, path):
    entry = job["entry"]
    kind, width, height, alpha = image_info(path)
    expected = entry.get("format", "png")
    if kind != expected:
        raise ValueError(f"{job['output']}: expected {expected} content, found {kind or 'unknown'}")
    if [width, height] != entry["size"]:
        raise ValueError(f"{job['output']}: expected {entry['size'][0]}x{entry['size'][1]},"
                         f" found {width}x{height}")
    if entry.get("transparent") and alpha is False:
        raise ValueError(f"{job['output']}: expected an alpha channel")


def render(job, directory, helpers):
    entry = job["entry"]
    target = directory / job["output"]
    target.parent.mkdir(parents=True, exist_ok=True)
    source = job["sources"][0]
    if entry["render"] in ("viewbox", "crop", "atlas"):
        text = atlas(job) if entry["render"] == "atlas" else reframe(
            source.read_text(encoding="utf-8"), entry["size"], *job["frame"])
        source = helpers / (job["output"].replace("/", "__") + ".svg")
        source.write_text(text, encoding="utf-8")
    command = ["rsvg-convert"]
    if entry["render"] == "size":
        command += ["--width", str(entry["size"][0]), "--height", str(entry["size"][1])]
    elif entry["render"] == "zoom":
        command += ["--zoom", str(entry["zoom"])]
    command += ["--output", str(target), str(source)]
    if entry.get("fonts"):
        command = [sys.executable, str(WITH_FONTS)] + command
    subprocess.run(command, check=True)
    validate(job, target)


def require(jobs):
    tools = {"rsvg-convert"}
    if any(job["entry"].get("fonts") for job in jobs):
        tools.add("fc-match")
    missing = sorted(tool for tool in tools if shutil.which(tool) is None)
    if missing:
        raise ValueError("Missing graphics export prerequisite(s): " + ", ".join(missing)
                         + "; install librsvg (rsvg-convert) and Fontconfig (fc-match)."
                         " This tool installs nothing.")


def destination(output, root, manifest):
    if output is None:
        return Path(tempfile.mkdtemp(prefix="akropia-graphics-export-"))
    path = Path(output).resolve()
    for field in ("sourceRoot", "runtimeRoot"):
        protected = (root / manifest[field]).resolve()
        if path == protected or protected in path.parents or path in protected.parents:
            raise ValueError(f"Output {path} overlaps the {field} {protected};"
                             " choose a separate directory")
    if path.exists() and (not path.is_dir() or any(path.iterdir())):
        raise ValueError(f"Output {path} must be a new or empty directory")
    path.mkdir(parents=True, exist_ok=True)
    return path


def run(groups=(), output=None, check=False, install=False, manifest=None, root=None):
    """Return (exported count, stale descriptions, kept output directory or None)."""
    if install and (check or output is not None):
        raise ValueError("--install cannot be combined with --check or --output")
    root = Path(ROOT if root is None else root)
    manifest = load(manifest)
    jobs, runtime = plan(manifest, root, list(groups))
    exports = [job for job in jobs if job["entry"]["render"] != "preserved"]
    if exports:
        require(exports)
    keep = not install and (output is not None or not check)
    work = None if keep else tempfile.TemporaryDirectory(prefix="akropia-graphics-check-")
    directory = destination(output, root, manifest) if keep else Path(work.name)
    stale = []
    try:
        with tempfile.TemporaryDirectory(prefix="akropia-graphics-helpers-") as helpers:
            for job in exports:
                render(job, directory, Path(helpers))
        if check:
            for job in jobs:
                committed = runtime / job["output"]
                if not committed.is_file():
                    stale.append(f"{job['output']}: missing runtime image")
                elif job["entry"]["render"] == "preserved":
                    try:
                        validate(job, committed)
                    except ValueError as error:
                        stale.append(str(error))
                elif committed.read_bytes() != (directory / job["output"]).read_bytes():
                    stale.append(f"{job['output']}: differs from the fresh export")
        if install:
            for job in exports:
                target = runtime / job["output"]
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes((directory / job["output"]).read_bytes())
    finally:
        if work:
            work.cleanup()
    return len(exports), stale, directory if keep else None


def unlisted(manifest, root):
    runtime = root / manifest["runtimeRoot"]
    patterns = [entry["output"] for group in manifest["groups"].values()
                for entry in group["outputs"]]
    files = sorted(path.relative_to(runtime).as_posix()
                   for path in runtime.rglob("*") if path.is_file())
    return [name for name in files
            if not any(fnmatch.fnmatchcase(name, pattern) for pattern in patterns)]


def main():
    parser = argparse.ArgumentParser(description=__doc__,
                                     formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("groups", nargs="*", help="manifest groups (default: all)")
    parser.add_argument("--output", help="new or empty directory for the exports")
    parser.add_argument("--check", action="store_true",
                        help="compare with committed runtime images without writing them")
    parser.add_argument("--install", action="store_true",
                        help="replace the selected runtime images after all exports pass")
    parser.add_argument("--manifest", help="alternative manifest path")
    parser.add_argument("--list", action="store_true",
                        help="list groups and runtime images outside the manifest")
    args = parser.parse_args()
    try:
        if args.list:
            manifest = load(args.manifest)
            for name, group in manifest["groups"].items():
                print(f"{name}: {len(group['outputs'])} entries; {group.get('family', '')}")
            uncovered = unlisted(manifest, ROOT)
            print(f"Runtime images outside the manifest: {len(uncovered)}")
            for name in uncovered:
                print("  " + name)
            return 0
        if shutil.which("rsvg-convert"):
            version = subprocess.run(["rsvg-convert", "--version"], capture_output=True,
                                     text=True).stdout.strip()
            print(f"Renderer: {version}")
        count, stale, directory = run(args.groups, args.output, args.check, args.install,
                                      args.manifest)
    except (ValueError, subprocess.CalledProcessError) as error:
        print(f"error: {error}", file=sys.stderr)
        return 2
    if args.install:
        print(f"Installed {count} runtime images")
    elif not args.check:
        print(f"Exported {count} images into {directory}")
    else:
        print(f"Checked {count} exports" + (f"; fresh exports in {directory}" if directory else ""))
        for line in stale:
            print("Stale: " + line)
        return 1 if stale else 0
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
