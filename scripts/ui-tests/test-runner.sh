#!/bin/bash
set -euo pipefail
if [[ "${1:-}" != --locked ]]; then
  exec python3 "$(dirname "$0")/local_slot.py" -- bash "$0" --locked
fi
shift
cd "$(dirname "$0")/../.."
docker compose -f compose.ui-tests.yml build
image=$(docker compose -f compose.ui-tests.yml config --images)
mkdir -p target/docker-ui-runner-checks
check_dir=$(mktemp -d "$PWD/target/docker-ui-runner-checks/run-XXXXXX")
mkdir -p "$check_dir/source/.hidden" "$check_dir/source/nested/target"
touch "$check_dir/source/.hidden/keep" "$check_dir/source/nested/target/stale"

cat > "$check_dir/mvn" <<'STUB'
#!/bin/bash
set -euo pipefail
test -f /workspace/.hidden/keep
test ! -e /workspace/nested/target
test "${DISPLAY:-}" != ""
found=0
for argument do
    if [[ "$argument" == '-Dsample=two words' ]]; then found=1; fi
done
[[ "$found" == 1 ]]
mkdir -p microcol-game/target/surefire-reports microcol-test/target/surefire-reports
echo model > microcol-game/target/surefire-reports/model.txt
echo ui > microcol-test/target/surefire-reports/ui.txt
echo 'Simulated Maven output'
if [[ "$CASE" == export-failure ]]; then
    touch "$RESULT_DIR/microcol-game"
fi
exit "$MAVEN_EXIT"
STUB
chmod +x "$check_dir/mvn"

for scenario in success test-failure export-failure; do
    mkdir -p "$check_dir/$scenario"
    expected=0
    maven_exit=0
    if [[ "$scenario" == test-failure ]]; then expected=7; maven_exit=7; fi
    if [[ "$scenario" == export-failure ]]; then expected=1; fi
    actual=0
    docker run --label "org.microcol.local-test=${MICROCOL_LOCAL_TEST_OWNER:?}" --rm --platform linux/amd64 \
        -v "$check_dir/source:/source:ro" \
        -v "$check_dir/mvn:/test-bin/mvn:ro" \
        -v "$check_dir/$scenario:/results" \
        -e PATH=/test-bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
        -e CASE="$scenario" -e MAVEN_EXIT="$maven_exit" \
        "$image" '-Dsample=two words' > "$check_dir/$scenario.log" 2>&1 || actual=$?
    [[ "$actual" == "$expected" ]]
    result=("$check_dir/$scenario"/run-*)
    [[ "$(cat "${result[0]}/exit-code.txt")" == "$expected" ]]
    test -s "${result[0]}/maven.log"
    [[ "$(cat "${result[0]}/microcol-test/surefire-reports/ui.txt")" == ui ]]
    if [[ "$scenario" != export-failure ]]; then
        [[ "$(cat "${result[0]}/microcol-game/surefire-reports/model.txt")" == model ]]
    fi
    echo "PASS: $scenario (exit $actual, logs and reports retained)"
done
test ! -d "$check_dir/source/microcol-game"
echo "PASS: read-only source, clean snapshot and quoted argument forwarding"

# Docker Desktop bind mounts do not reproduce Linux result permissions.
docker run --label "org.microcol.local-test=${MICROCOL_LOCAL_TEST_OWNER:?}" --rm --platform linux/amd64 --entrypoint /bin/bash \
    -v "$check_dir/source:/source:ro" -v "$check_dir/mvn:/test-bin/mvn:ro" \
    -e PATH=/test-bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin \
    -e CASE=success -e MAVEN_EXIT=0 "$image" -ec '
    mkdir /results
    microcol-ui-tests "-Dsample=two words"
    su nobody -s /bin/bash -c "cat /results/run-*/exit-code.txt \
        /results/run-*/microcol-test/surefire-reports/ui.txt > /dev/null"
' > "$check_dir/linux-permissions.log" 2>&1
echo "PASS: reports readable by an unprivileged Linux user"
python3 scripts/ui-tests/check-local-slot.py "$image"
