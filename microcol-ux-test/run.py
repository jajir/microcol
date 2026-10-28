#!/usr/bin/env python3
"""Build and run an isolated UX player; never launch the regression runner."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import shutil
import signal
import subprocess
import sys
import uuid

ROOT = Path(__file__).resolve().parent.parent


def command(*args, **kwargs):
    return subprocess.run(args, check=True, **kwargs)


def snapshot(destination):
    names = subprocess.check_output(["git", "ls-files", "-z", "--cached", "--others", "--exclude-standard"], cwd=ROOT).split(b"\0")
    paths = sorted({os.fsdecode(n) for n in names if n})
    hashes = {}
    for name in paths:
        if not (name.endswith("pom.xml") or name.startswith(("microcol-game/", "microcol-module/", "microcol-dist/", "microcol-ux-test/", "scripts/ui-tests/"))):
            continue
        source = ROOT / name
        if source.is_symlink():
            raise ValueError("Source symlinks are not supported: " + name)
        data = source.read_bytes()
        target = destination / name
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        hashes[name] = hashlib.sha256(data).hexdigest()
    for name, digest in hashes.items():
        if hashlib.sha256((ROOT / name).read_bytes()).hexdigest() != digest:
            raise ValueError("Source changed while preparing image: " + name)
    return hashes


def parse(argv):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("mode", choices=("check", "play"))
    parser.add_argument("--container", required=True)
    parser.add_argument("--image", help="Reuse an already built, identified UX image")
    parser.add_argument("--model")
    parser.add_argument("--reasoning")
    parser.add_argument("--seconds", type=int, default=7200)
    parser.add_argument("--actions", type=int, default=1000)
    args = parser.parse_args(argv)
    if not 1 <= args.seconds <= 7200 or not 1 <= args.actions <= 1000:
        parser.error("Use 1–7200 seconds and 1–1000 actions")
    if args.mode == "play" and (not args.model or not args.reasoning):
        parser.error("Specify the user's selected --model and --reasoning")
    return args


def main(argv):
    args = parse(argv)
    identifier = datetime.now(timezone.utc).strftime("%Y%m%dT%H%M%SZ-") + uuid.uuid4().hex[:8]
    results = ROOT / "microcol-ux-test/runs" / identifier
    results.mkdir(parents=True)
    print("UX results: " + str(results), flush=True)
    manifest = {"revision": subprocess.check_output(["git", "rev-parse", "HEAD"], cwd=ROOT, text=True).strip(),
                "mode": args.mode, "model": args.model, "reasoning": args.reasoning,
                "seconds": args.seconds, "max_actions": args.actions, "container": args.container}
    output = results / "manifest.json"
    output.write_text(json.dumps(manifest, indent=2) + "\n")
    def interrupted(signum, frame):
        raise InterruptedError("UX run interrupted")
    signal.signal(signal.SIGTERM, interrupted)
    signal.signal(signal.SIGINT, interrupted)
    owned = False
    try:
        image = args.image
        if not image:
            context = ROOT / "target/ux-build" / identifier
            manifest["sources"] = snapshot(context)
            source_hash = hashlib.sha256(json.dumps(manifest["sources"], sort_keys=True).encode()).hexdigest()
            base = "microcol-ux-base:" + identifier.lower()
            command("docker", "build", "--platform", "linux/amd64", "-t", base, str(context / "scripts/ui-tests"))
            image = "microcol-ux:" + identifier.lower()
            command("docker", "build", "--platform", "linux/amd64", "--build-arg", "UI_IMAGE=" + base,
                    "--build-arg", "SOURCE_REVISION=" + manifest["revision"],
                    "--build-arg", "SOURCE_SHA256=" + source_hash,
                    "-f", str(context / "microcol-ux-test/Dockerfile"), "-t", image, str(context))
        manifest["image"] = subprocess.check_output(["docker", "image", "inspect", "--format", "{{.Id}}", image], text=True).strip()
        labels = json.loads(subprocess.check_output(["docker", "image", "inspect", "--format", "{{json .Config.Labels}}", image])) or {}
        manifest["image_revision"] = labels.get("org.microcol.ux.revision")
        manifest["image_source_sha256"] = labels.get("org.microcol.ux.source-sha256")
        if not manifest["image_revision"] or not manifest["image_source_sha256"]:
            raise ValueError("UX image lacks build provenance; rebuild it before playing")
        output.write_text(json.dumps(manifest, indent=2) + "\n")
        # JavaFX loads extracted native libraries from the fresh home directory.
        invocation = ["docker", "create", "--init", "--platform", "linux/amd64", "--name", args.container,
                      "--user", str(os.getuid()) + ":" + str(os.getgid()),
                      "--tmpfs", "/home/player:uid=" + str(os.getuid()) + ",gid=" + str(os.getgid()) + ",mode=700,exec",
                      "--label", "org.microcol.local-test=" + os.environ.get("MICROCOL_LOCAL_TEST_OWNER", str(os.getuid())),
                      "--memory", "4g", "--shm-size", "256m", "--cap-drop", "ALL",
                      "--security-opt", "no-new-privileges", "-v", str(results) + ":/results",
                      "-e", "UX_SECONDS=" + str(args.seconds), "-e", "UX_MAX_ACTIONS=" + str(args.actions)]
        if args.mode == "play":
            auth = Path(os.environ.get("CODEX_HOME", str(Path.home() / ".codex"))) / "auth.json"
            if not auth.is_file():
                raise ValueError("Codex auth.json is unavailable; authenticate on the host first")
            invocation += ["-v", str(auth) + ":/auth/auth.json:ro", "-e", "UX_MODEL=" + args.model,
                           "-e", "UX_REASONING=" + args.reasoning]
        invocation += [manifest["image"], args.mode]
        command(*invocation)
        owned = True
        command("docker", "start", "--attach", args.container)
        state = json.loads(subprocess.check_output(["docker", "inspect", "--format", "{{json .State}}", args.container]))
        if state["ExitCode"] != 0 or state["OOMKilled"]:
            raise RuntimeError("UX container did not complete successfully: " + str(state["ExitCode"]))
        return 0
    finally:
        signal.signal(signal.SIGTERM, signal.SIG_IGN)
        signal.signal(signal.SIGINT, signal.SIG_IGN)
        if owned:
            subprocess.run(["docker", "stop", "--time", "15", args.container], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=25)
            command("docker", "rm", "--force", args.container, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, timeout=20)


if __name__ == "__main__":
    try:
        sys.exit(main(sys.argv[1:]))
    except (OSError, ValueError, RuntimeError, InterruptedError, subprocess.SubprocessError) as error:
        print("UX run failed: " + str(error), file=sys.stderr)
        sys.exit(1)
