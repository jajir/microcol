#!/bin/bash
# Finder provides no terminal, so keep a log and show a native failure dialog.
set -u
script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
root=${MICROCOL_DEV_ROOT:-$HOME/Library/Application Support/MicroCol Dev}
mkdir -p "$root" || exit 1
if ! "$script_dir/../Resources/microcol-dev" "$@" > "$root/launcher.log" 2>&1; then
    /usr/bin/osascript -e 'display dialog "MicroCol Dev could not start. Connect to the internet for the first download. If macOS blocked the game, review its normal security prompt. Details are in ~/Library/Application Support/MicroCol Dev/launcher.log. The included .command launcher also shows errors in Terminal." with title "MicroCol Dev" buttons {"OK"} default button "OK" with icon caution' >/dev/null 2>&1 || true
    exit 1
fi
