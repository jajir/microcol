#!/bin/bash
# JavaFX runs in the akropia-game module, so this path stays repository-relative.
set -euo pipefail
exec bash "$(dirname "$0")/run-game.sh" \
    '-Djavafx.args=--demo-studio --demo-directory=src/main/resources/demos' "$@"
