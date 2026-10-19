#!/bin/bash
# Finder provides no terminal, so record a fixed failure line and show a native
# failure dialog. Console output of the launcher and the game may contain player
# or save text, so it is discarded (never kept, never a blocking pipe). The
# record joins the game's diagnostic set in ~/Library/Logs/MicroCol: at most
# three files (microcol.log, microcol.1.log, microcol.2.log) of at most 5 MiB.
set -u
script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
root=${MICROCOL_DEV_ROOT:-$HOME/Library/Application Support/MicroCol Dev}
log_dir=$HOME/Library/Logs/MicroCol
limit=5242880
mkdir -p "$root" || exit 1
# An earlier wrapper kept a raw console copy here; it is no longer wanted.
rm -f -- "$root/launcher.log"

record_failure() {
    # Never fails the launch: the log is a convenience, the dialog is the feedback.
    local status=$1 line size active=$log_dir/microcol.log
    [[ $status =~ ^[0-9]{1,3}$ ]] || status=unknown
    line="$(date -u +%Y-%m-%dT%H:%M:%SZ) os=macOS arch=$(uname -m) event=LAUNCHER_FAILED status=$status"
    mkdir -p "$log_dir" 2>/dev/null || return 0
    size=0
    [ -f "$active" ] && size=$(wc -c < "$active" 2>/dev/null | tr -d '[:space:]')
    [[ $size =~ ^[0-9]+$ ]] || size=0
    if [ $((size + ${#line} + 1)) -gt "$limit" ]; then
        rm -f -- "$log_dir/microcol.2.log"
        [ -f "$log_dir/microcol.1.log" ] && mv -f -- "$log_dir/microcol.1.log" "$log_dir/microcol.2.log"
        mv -f -- "$active" "$log_dir/microcol.1.log"
    fi
    printf '%s\n' "$line" >> "$active" 2>/dev/null || true
    return 0
}

"$script_dir/../Resources/microcol-dev" "$@" > /dev/null 2>&1
status=$?
if [ "$status" -ne 0 ]; then
    record_failure "$status"
    # MICROCOL_OSASCRIPT lets build tests replace the native dialog.
    "${MICROCOL_OSASCRIPT:-/usr/bin/osascript}" -e 'display dialog "MicroCol Dev could not start. Connect to the internet for the first download. If macOS blocked the game, review its normal security prompt. A short failure record is in ~/Library/Logs/MicroCol/microcol.log. The included .command launcher also shows errors in Terminal." with title "MicroCol Dev" buttons {"OK"} default button "OK" with icon caution' >/dev/null 2>&1 || true
    exit 1
fi
