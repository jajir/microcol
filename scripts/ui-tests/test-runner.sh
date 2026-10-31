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
    [[ "$argument" != clean ]]
done
[[ "$found" == 1 ]]
for module in microcol-game microcol-test; do
    [[ "$module/target/surefire-reports" -ef "$RESULT_DIR/$module/surefire-reports" ]]
done
echo model > microcol-game/target/surefire-reports/model.txt
echo ui > microcol-test/target/surefire-reports/ui.txt
mkdir -p microcol-test/target/warehouse-qa
echo screenshot > microcol-test/target/warehouse-qa/example.txt
echo 'Simulated Maven output'
if [[ "$CASE" == interrupted ]]; then
    echo '<testsuite name="completed" tests="1"><testcase name="passed"/></testsuite>' \
        > microcol-test/target/surefire-reports/TEST-completed.xml
    touch /results/ready
    sleep 60
fi
if [[ "$CASE" == export-failure ]]; then
    touch "$RESULT_DIR/microcol-test/warehouse-qa"
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
        [[ "$(cat "${result[0]}/microcol-test/warehouse-qa/example.txt")" == screenshot ]]
    fi
    echo "PASS: $scenario (exit $actual, logs and reports retained)"
done
test ! -d "$check_dir/source/microcol-game"
echo "PASS: read-only source, clean snapshot and quoted argument forwarding"

# Completed XML must survive SIGKILL, when neither shell EXIT trap can run.
container="microcol-report-check-$(basename "$check_dir" | tr '[:upper:]' '[:lower:]')"
trap 'docker rm -f "$container" >/dev/null 2>&1 || true' EXIT
mkdir -p "$check_dir/interrupted"
docker run -d --name "$container" --label "org.microcol.local-test=${MICROCOL_LOCAL_TEST_OWNER:?}" \
    --platform linux/amd64 -v "$check_dir/source:/source:ro" \
    -v "$check_dir/mvn:/test-bin/mvn:ro" -v "$check_dir/interrupted:/results" \
    -e PATH=/test-bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/bin \
    -e CASE=interrupted -e MAVEN_EXIT=0 "$image" '-Dsample=two words' > "$check_dir/interrupted-container.txt"
for ((attempt=0; attempt<150; attempt++)); do
    [[ -f "$check_dir/interrupted/ready" ]] && break
    sleep 0.2
done
test -f "$check_dir/interrupted/ready"
docker kill --signal KILL "$container" > /dev/null
[[ "$(docker wait "$container")" == 137 ]]
result=("$check_dir/interrupted"/run-*)
test -s "${result[0]}/microcol-test/surefire-reports/TEST-completed.xml"
test ! -e "${result[0]}/exit-code.txt"
docker rm "$container" > /dev/null
trap - EXIT
echo "PASS: completed XML survives forced termination without final export"

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
