#!/bin/sh
set -eu

# Keep the old entry point, but let Maven own versions, JDK selection and outputs.
project_root=$(CDPATH= cd -- "$(dirname -- "$0")/../../../.." && pwd)
cd "$project_root"
exec mvn -pl microcol-dist-macos -am package "$@"
