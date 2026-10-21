#!/bin/bash
set -euo pipefail

RESULT_DIR=$(mktemp -d /results/run-XXXXXX)
chmod 755 "$RESULT_DIR"
export RESULT_DIR
echo "Result directory: $RESULT_DIR"

export_results() {
    local status=$?
    trap - EXIT
    set +e
    local source parent
    for source in microcol-game/target/surefire-reports \
        microcol-test/target/surefire-reports microcol-test/target/warehouse-qa; do
        if [[ -d "$source" ]]; then
            parent=${source%/*}
            parent=${parent/\/target/}
            if ! mkdir -p "$RESULT_DIR/$parent" \
                || ! cp -a "$source" "$RESULT_DIR/$parent/"; then
                if ((status == 0)); then status=1; fi
            fi
        fi
    done
    if ! printf '%s\n' "$status" > "$RESULT_DIR/exit-code.txt"; then
        if ((status == 0)); then status=1; fi
    fi
    exit "$status"
}
trap export_results EXIT
trap 'exit 130' INT
trap 'exit 143' TERM

cd /workspace
tar -C /source --exclude=.git --exclude=target -cf - . \
    | tar -C /workspace -xf -

xvfb-run -e "$RESULT_DIR/xvfb.log" -a \
    --server-args='-screen 0 1600x1200x24' bash -euc '
    openbox > "$RESULT_DIR/openbox.log" 2>&1 &
    pid=$!
    trap "kill $pid 2>/dev/null || true" EXIT
    mvn -B -ntp -pl "${MICROCOL_TEST_MODULE:-microcol-test}" -am clean test -P ci "$@" \
        -Dtestfx.robot=glass -Dtestfx.headless=false -Dprism.order=sw \
        -DsoundEnabled=false
' bash "$@" 2>&1 | tee "$RESULT_DIR/maven.log"
