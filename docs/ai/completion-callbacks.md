# Completion callbacks for Codex

[R-0243](../requirements/done/R-0243.md) adds one host-side
[runner](../../scripts/codex/run.py) for long worker, Docker and CI commands.
It uses the already installed Python, Codex CLI, Docker and GitHub CLI. It does
not install a daemon or change a model. The independent UX player mounts host
authentication only for its container session; credentials are never baked into
an image or exported with results.

## Local Docker test lock

1. [R-0292](../requirements/done/R-0292.md), agreed on 2026-10-26, serializes local
   test **jobs** for the current user across worktrees. `docker` and `verify`
   completion commands acquire `~/.cache/microcol/docker-tests.lock` before
   starting their execution watchdog. Direct `verify.sh` and `test-runner.sh`
   use the same [OS-lock helper](../../scripts/ui-tests/local_slot.py).
   Full verification may still run its two configured UI groups inside one job.
   This is a cooperative lock, not a FIFO scheduler or a Docker-wide limit.
   An explicit user request may use the isolated
   [parallel-run procedure](#user-requested-parallel-runs) below.
2. A waiting run logs `Waiting for the local Docker test lock`; `result.json`
   records `wait_seconds` separately from `elapsed_seconds`. Only waiting is
   excluded: both execution and acceptance budgets start when the lock is acquired,
   including recovery and final cleanup. The smoke wrapper also rejects a command
   that passed internally but exceeded 420 seconds during cleanup. Recovery
   failure blocks the new command. Cancellation while waiting leaves the holder
   untouched. Worker and remote-CI modes do not take this lock.
3. `flock` releases when its holder exits, including SIGKILL. The lock descriptor
   is not inherited. Before allowing a child to execute, the helper durably
   records its separate process group and start identity. After a crash, the next
   holder terminates that recorded group before checking Docker. A reused PID,
   corrupt recovery record or unsuccessful cleanup fails closed. Never remove
   the lock file to bypass a live lock; file existence is not lock ownership.
4. Managed containers carry `org.microcol.local-test` with the local user's UID.
   Recovery retains their Docker logs in the next command log and removes only
   those containers, then confirms none remain. Unlabelled/foreign containers
   are untouched. If Docker is unavailable, no new test command starts. Existing
   per-run reports and failed evidence remain in place.
5. Keep the selected checkout stable while waiting. Request revision/status are
   dispatch-time metadata; the verifier's manifest identifies the source captured
   after admission and is authoritative for acceptance. Targeted Docker tests
   retain their existing container-time source copy. Raw Docker commands or
   direct internal `verify.py` calls bypass the lock and are not supported local
   entry points. For a foreground targeted run, use:

   ```sh
   python3 scripts/ui-tests/local_slot.py -- \
     docker compose -f compose.ui-tests.yml run --build --rm ui-tests \
     -Dtest=PageVerificationContractTest -Dsurefire.failIfNoSpecifiedTests=false
   ```

6. Serialization removes competition between cooperating local test jobs; it
   does not guarantee that an individual run fits seven minutes. Serialization itself
   does not change test selection or the acceptance budget; the current limit is
   documented below. No remote regression is watched.

## User-requested parallel runs

1. **Agreed on 2026-10-28, [R-0333](../requirements/done/R-0333.md):** a brief explicit
   request such as "run this in parallel", "pusť to paralelně" or "use a separate
   Docker image" authorizes an isolated concurrent run for that task. Do not ask
   for the same approval or setup details again. Without that request, retain the
   default shared lock. This permission concerns local test jobs, not cloud-agent
   delegation or remote CI.
2. Record the existing runs' identities and a brief CPU, memory and disk snapshot.
   Prepare the new run without stopping, restarting or cleaning up any other job.
   Shared resource use alone is not a reason to request permission again; report
   a concrete execution blocker if one is observed. Resource contention can still
   cause a timeout or failure, which must remain visible in the evidence.
3. Give the run a unique image tag, container name, source snapshot and result
   directory. The verifier already builds a per-run image and pins its immutable
   image ID. For targeted Compose runs, use a task-specific project/image identity
   as well. Preserve other jobs' images, volumes and ports; do not use global
   prune or project-wide teardown on shared resources.
4. Isolate both the OS lock and cleanup owner. Use a task-specific lock path and
   `org.microcol.local-test` value, propagated through `MICROCOL_LOCAL_TEST_OWNER`.
   Never delete the default lock, reuse its owner label or launch a raw command
   that skips supervision. A separate image alone cannot prevent the default
   recovery logic from removing another job's containers.
5. The current runner has no parallel-mode CLI flag. Use a task-local copy of
   `scripts/codex/run.py` and `scripts/ui-tests/local_slot.py` under ignored
   `target/`: set the copy's repository root to the chosen checkout, import its
   copied lock helper, and give that helper a unique `LOCK_PATH` and `OWNER`.
   Keep its callback, process-group recovery, deadlines and cleanup logic.
   Retain per-run image isolation for verification and add the unique Compose
   project/image identity for targeted runs. Check that cleanup selects only
   this task's owner and preserves a foreign owner before starting it. Keep these
   operational copies out of the task's commit; do not change the shared runner
   merely to enable one requested run.
6. Dispatch with the exact originating task UUID and explicit timeout. Record
   the run directory and supervisor PID, then resume from the completion callback.
   Preserve the required model → targeted UI → smoke order within this task,
   source identity, complete reports and the 420-second smoke budget. After
   cleanup, verify that other runs were left intact. If replacing this task's
   already queued run, cancel only that identified waiting job and retain its
   cancellation evidence.
7. The successful [R-0332 run](../functional-tests.md#r-0332-congress-screen-2026-10-28)
   demonstrated isolated model, targeted UI and smoke execution alongside an
   existing UX test. This is evidence that concurrency is possible, not a measured
   speedup guarantee or evidence of unlimited capacity. This instruction change
   adds no runner option and launches no additional tests.

## Dispatch and resume

1. Use the **exact originating desktop task UUID**, not its title, the worker's
   child session ID, or a different task. Keep the desktop app and Mac available.
2. Start the command below from the selected checkout. The runner returns its
   unique evidence directory and supervisor PID after starting a detached local
   process. Record them in the task. Do independent work or end the current turn;
   do not repeatedly query a running process, sleep and ask the model to check it,
   or create another cloud agent to monitor it.
3. The local supervisor waits without model calls. It saves the result before
   attempting one `codex queue --thread` message on success, failure, cancellation
   or timeout. A message arriving during other work belongs to the same task;
   reconcile it with that work rather than starting duplicate verification.
4. On the callback, inspect that run's `result.json`, `command.log` and referenced
   worker/test/CI evidence. Completion is **not acceptance**. Preserve actual test
   names/counts/skips, required selected suites, formatting, independent review,
   bounded worker repair and publication checks. Reuse evidence only for the
   same checked source, environment and selection.

The source identity records HEAD and working-tree status at dispatch. A dirty
checkout is not an immutable snapshot: keep relevant source stable while tests
run and check the actual diff before reusing their evidence. Docker retains its
existing clean copy and report export; this wrapper does not change test inputs.

## Worker

Use the same job directory, profile and repair budget as the
[worker operating contract](local-qwen.md#prepare-invoke-verify):

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 7300 worker "$worker_job"
```

Pass an agreed `--profile` and diagnosed `--escalate-reason` after the job when
needed. The original launcher still controls the two-hour attempt, four-hour
cumulative budget, model identity, lock and unload. The outer 7,300-second limit
is a failsafe with cleanup allowance, not extra inference time. A callback points
to the new attempt; a preflight failure must not be mistaken for an older success.
Application of replacements, review, tests and lock release remain caller-owned.

## Docker tests

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 5400 docker
```

For an already justified targeted selection, forward the same Maven arguments:

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 3600 docker -- \
  -Dtest=PageVerificationContractTest -Dsurefire.failIfNoSpecifiedTests=false
```

The 90-minute watchdog matches the [full functional CI budget](../development-builds.md#full-functional-regression-budget). It does not justify accepting
partial results or shrinking a required suite. The runner owns one uniquely named
container. Timeout/cancellation stops that container as well as its Docker client;
other containers, images and volumes are untouched. Available reports remain in
`target/docker-ui/`; `command.log` identifies the new `run-...` directory. Forced
termination may leave incomplete reports, which remain a verification blocker.

The deadline ends command execution; bounded cleanup follows before notification.
It allows 45 seconds for graceful process exit, then up to 20 seconds each for
Docker stop and removal. Delivery has its own 30-second limit.

The original foreground command remains available to human operators and CI.
The [functional contract](../functional-tests.md#docker-ui-tests), named scenarios
and container runner checks are unchanged.

## Independent UX sessions

1. The `ux` command dispatches the separate [UX playtesting runner](../ux-playtesting.md), for example `python3 scripts/codex/run.py --thread TASK_UUID --timeout 1800 ux -- check`.
2. It uses the same local Docker admission/cleanup lock and originating-task callback, but invokes no UI regression selection. Results remain in `microcol-ux-test/runs/` and are not Surefire reports or gameplay acceptance.
3. The caller records the selected model/reasoning and inner play budget when dispatching `play`. Outer completion is separate from the player's goals and evidence review.

## Pre-commit gate

For a commit requiring game tests, run the [local acceptance phases](../functional-tests.md#pre-commit-smoke)
in order: all model/JUnit tests, targeted UI for changed behavior, then smoke.
Documentation-only commits are exempt under the [local acceptance policy](../publishing.md#local-acceptance-policy);
run documentation consistency checks and strict MkDocs instead, without starting this runner.

Start the model phase, inspect its callback and complete passing reports, then
run the justified targeted selection with the Docker command above. For a
tooling-only change, run its tooling tests; no unrelated targeted UI is needed.
Finish with smoke:

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 600 verify -- --model
# After acceptance of model and any required targeted UI results:
python3 scripts/codex/run.py --thread TASK_UUID --timeout 480 verify -- --smoke
```

The model phase explicitly selects all `microcol-game` tests, including
`ConnectorTest`, and no UI tests. Smoke selects only the existing UI/support
checks and enforces a **420-second total acceptance budget**; it does not repeat
model tests. [R-0303](../requirements/done/R-0303.md) records the 2026-10-26 agreement.
Historical results keep their original limits and selections. The outer
480-second smoke watchdog allows startup, bounded cleanup and notification;
it does not extend acceptance. The model command's 600-second watchdog is a
separate execution failsafe, not part of the smoke allowance.

Record each returned directory and PID, continue independent work, then inspect
its callback's `result.json`, `command.log`, manifest and reports. Every required
phase needs complete passing reports for the source being committed. Reuse model
and targeted results while the tested source and selection stay unchanged.
A failed or incomplete phase blocks the commit. No phase starts a remote CI watcher.

## Complete local verification

Only when the user explicitly requests full regression or its repair, dispatch the [full-suite runner](../functional-tests.md#uplne-overeni-v-izolovanych-skupinach)
once; it owns both UI containers and aggregates their results:

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 16400 verify -- --workers 2 --timeout 5400
```

The outer deadline covers image preparation, one model phase, one concurrent UI
phase and cleanup. Optional `--timings PATH` forwards previous Surefire XML for
balancing. Record the returned supervisor PID and evidence directory. On callback,
`command.log` identifies `target/verification/run-*/summary.json`; inspect that
result, source manifest and reports before acceptance. SIGTERM lets the runner
stop/remove only its owned containers. The handoff recipient must use their own
originating chat UUID. A successful callback is not acceptance or publication.

## GitHub Actions

Only when the user requests CI work or verified publication, discover the runs
for the **full pushed commit SHA**, for example. Ordinary task completion does not
start CI watchers; see the [local acceptance policy](../publishing.md#local-acceptance-policy).

```sh
gh run list --repo jajir/microcol --commit FULL_COMMIT_SHA --event push \
  --json databaseId,workflowName,headSha,status,url
python3 scripts/codex/run.py --thread TASK_UUID --timeout 7200 ci RUN_ID \
  --commit FULL_COMMIT_SHA --repo jajir/microcol
```

Start one watcher for each required run: development delivery, Java dependency
compatibility and Pages. Bind each to its actual run ID and commit; do not watch
whatever happens to be latest. If a run is not registered yet, retry discovery
locally with a bounded wait, not a repeated model loop. The watcher validates
identity before and after `gh run watch --exit-status --interval 30` and retains
CI metadata. GitHub is still polled locally every 30 seconds, without model calls.

A watcher timeout or network error means verification is incomplete; it neither
cancels remote CI nor proves that CI itself failed. There is no automatic rerun.
On completion inspect jobs and artifacts, including intentional documentation-only
skips, and verify the published build/manifest and download link under the
[delivery contract](../development-builds.md#ci-filtering). Three successful watch
commands alone do not establish correct publication.

## Evidence, cancellation and delivery failures

Each run keeps `request.json`, `supervisor.json` (PID), `command.log`, `supervisor.log`, `result.json` and
`notification.json` in `~/Library/Logs/Microcol/completions/`. Notification status
is separate from command outcome. Cancel an owned supervisor with `kill -TERM PID`
using its recorded PID; cleanup and a cancellation callback then run. Do not kill
the worker model or arbitrary containers by name guessing.

The queue attempt is bounded to 30 seconds. If it fails, inspect
`notification.json` and the target task before manually delivering the saved
result with `codex queue --thread TASK_UUID --message ...`. Do not blindly retry:
an interrupted queue command may already have accepted the message. A successful
queue exit proves enqueueing, not that the model read the message or accepted work.

This is a local process, not a persistent service. Host shutdown, supervisor
SIGKILL, app closure and sleep are not guaranteed delivery cases. Preserve the
saved evidence and reconcile it on return. Do not add recurring model polling as
an implicit fallback or claim success when completion/delivery is unverified.

## Verification

Portable checks use temporary stub executables, never the real cloud account,
Docker containers or worker lock:

```sh
python3 -m unittest discover -s scripts/codex -p 'test_*.py'
```

The documentation workflow runs these alongside the existing worker and
repository-documentation tests. Dated live results and local-worker recovery are
recorded in [worker operations](local-qwen.md#completion-callbacks-2026-10-18).
The isolated pre-implementation desktop probe on 2026-10-18 verified both installed
CLI versions and a callback after exit 7. Production-runner acceptance is recorded
separately; this probe does not prove the later integration.

## Full-suite timeout diagnosis — 2026-10-19

1. [R-0247](../requirements/done/R-0247.md) follows two timed-out attempts of
   [development #178](https://github.com/jajir/microcol/actions/runs/39165485395)
   for `39ed716c3ec9cc2b5ffd990c2f7b3546b5082089`. GitHub annotated both with
   the 45-minute job deadline. Tests continued up to cancellation: completed-suite
   logs contain 2,691 checks in attempt 1 and 2,696 in attempt 2, with no reported
   failures/errors/skips. These partial log counts are not full-suite acceptance;
   cancellation also prevented complete report export.
2. [Development #177](https://github.com/jajir/microcol/actions/runs/39164417874)
   passed 2,720 checks on the identical game code in about 44 minutes. Changes
   between that source and #178 only concerned documentation and agent guidance.
   Attempt 1's shared UI suites were 9.2% slower at the median. One bounded rerun
   retained all tests and the deadline; its second timeout justified increasing
   the full-suite budget to 60 minutes. No third run of the old workflow is used
   as a substitute for the repair.
3. The correction changes no scenario, assertion, test selection, native-package
   deadline, targeted stability campaign or publication gate. Completion remains
   separate from acceptance. Local diagnostic files are under `target/r245/`;
   the original completion records and their failed results remain intact.
4. Local verification on 2026-10-19 passed 62 existing checks: 19 completion,
   21 worker-launcher tests without inference, 13 change-classifier tests and nine
   documentation tests, plus strict MkDocs. Parsed before/after workflow comparison
   confirms that only `jobs.functional.timeout-minutes` changes from 45 to 60.
   Full-suite acceptance and publication were pending at that local checkpoint.
5. [Development #181](https://github.com/jajir/microcol/actions/runs/39176254760)
   for `07bb1e0203442bd46ff88f7868fbc7e5489639c8` then passed **2,723 full
   regression checks** in **47 minutes 51 seconds**, within the new deadline.
   Downloaded XML reports verify zero failures/errors/skips, 32 build-tool checks
   and 2,381 checks on each of macOS ARM64, macOS Intel and Linux. Java 11/17 on
   Linux and Windows each passed 2,348 checks; Pages build and deployment passed
   on the same commit. The callback arrived in the originating task and its saved
   result was checked against the exact source and actual reports.
6. The immutable release
   [dev-181-07bb1e020344](https://github.com/jajir/microcol/releases/tag/dev-181-07bb1e020344)
   is published. Its manifest matches the downloaded current `dev` channel;
   all three platform archive digests match `SHA256SUMS`, and the downloaded
   macOS launcher's checksum and ZIP integrity pass. Evidence is retained under
   `target/r247/`, including `verified-test-counts.json` and
   `publication-verified.json`. This closes the R-0245 publication blocker and
   accepts R-0247; neither failed attempt of #178 is reclassified as passing.


## R-0292 local lock evidence — 2026-10-26

1. Accepted design is the [local Docker test lock](#local-docker-test-lock), with
   no FIFO service or acceptance-budget change. Implementation was integrated
   with main `93840745` before the final gate; Java/game behavior is unchanged by
   R-0292. Existing work in the primary checkout remains separate.
2. Tooling checks passed: 25 UI-verifier/lock process tests and 19 completion-runner
   tests. These cover mutual exclusion across directories, waiting cancellation,
   SIGKILL with a surviving child, PID reuse refusal, failed container recovery,
   startup-registration failure, managed verifier timeout cleanup and execution
   watchdog accounting after a longer wait, recovery time and final cleanup. Earlier fixture-only failures
   (nested Python quoting and a test wait shorter than the cleanup deadline) were
   repaired and their logs retained under `target/r0292-ui-tooling-attempt-*.log`.
3. Actual Docker runner attempts `20261026T074919Z-0e75f2651006` (PID 13775) and
   `20261026T075511Z-d30be23d96f2` (PID 14790) failed during admission: Docker's
   labelled-container query exceeded its 20-second timeout. No test command
   started. Foreground probes found Docker Desktop running and no containers;
   neither the engine nor acceptance deadlines were reset. Admission/recovery
   Docker commands now use their own process sessions. These attempts remain
   failed and their completion evidence is retained.
4. Retry `20261026T080131Z-320fc90be5b2` (PID 15604, 180-second outer limit)
   passed in 4.639 seconds, exit 0. The real Docker/Xvfb runner contract passed
   report export, failure-code, source isolation, quoting and Linux permissions
   checks. `target/docker-ui-runner-checks/lock-recovery-tcz6b8hn/` additionally
   proves SIGKILL recovery of the surviving process group and detached Docker
   container, retained orphan logs, and preservation of an unlabelled fixture
   container. All three completion deliveries above are confirmed queued.
5. The earlier candidate passed fixed smoke gate `20261026T080428Z-bf5da9efa3de`
   (PID 16321): 4,770 tests in 352 suites, zero failures/errors/skips, 291.325
   seconds including supervision. This predates the final whole-job budget
   checks and is not acceptance of the final source. Its hashes remain in
   `target/r0292-acceptance.json`.
6. Final Docker runner/recovery check `20261026T082635Z-325a2abc7d5d`
   (PID 20981) passed in 5.968 seconds, exit 0. Final fixed smoke gate
   `20261026T082657Z-5a142d60ca41` (PID 21077) passed in **290.459 seconds**
   including recovery and cleanup (verifier 290.312, container group 287.906).
   The 420-second outer setting was capped at 300 seconds for smoke. All 352
   fresh XML reports contain **4,770 tests, zero failures/errors/skips**, including
   all model/JUnit and support checks and the unchanged ten smoke scenarios in
   eight TC classes. Reports are in `target/verification/run-en70yanx/`.
7. The final manifest and pre-dispatch hashes match every executable/config
   source in the accepted checkout, based on `93840745`; only documentation
   status/evidence was closed afterward. Source hashes, dispatch and independently
   counted report results are in `target/r0292-final-acceptance.json`. Review
   checked all local entry points, child/lock descriptor ownership, failure cleanup
   and preservation of foreign containers. Nine documentation consistency tests
   and strict MkDocs passed after closure. R-0292 is accepted locally. No remote
   CI or publication verification was requested.

## Timeout evidence (2026-10-27)

1. [R-0317](../requirements/done/R-0317.md) adds structured `local-test` JSON lines
   to the existing `command.log`. Each record includes UTC time, runner PID,
   phase and event. Queue waiting, preparation, execution (build/tests) and
   cleanup are distinct. Docker steps record their command, deadline, elapsed
   time, return code and output; timeout records retain partial output.
   Existing container-log streaming is preserved. Recorded output excerpts are
   limited to 4,096 characters per stream.
2. A timed-out preparation or cleanup Docker command triggers read-only
   diagnostics: selected client path/context and host transport, load averages,
   Docker process state/CPU/RSS, client version, selected context and server
   version. Only a successful server-version probe permits one diagnostic repeat
   of the owned-container query. A successful probe does not retry tests,
   change the original outcome, restart Docker or remove additional containers.
3. Collection has an eight-second maximum, with at most two seconds per probe,
   also bounded by the remaining job budget. Existing 20-second recovery command
   limits and the 420-second smoke acceptance budget remain unchanged. Cleanup
   still runs when a job has exhausted its budget; additional diagnostic probes
   then have no time allowance. Probe failures are logged without replacing the
   original timeout. Whole environment/configuration dumps and unrelated process
   listings are excluded; URL credentials and URL query parameters are redacted.
4. Local `result.json` adds `command_started` and, for failed runs,
   `failure_phase`; a caught timeout also records
   `step_timeout_seconds`. Existing result statuses and the notification's
   evidence link format remain unchanged. A preparation timeout notification
   explicitly says the test command did not start; `execution` includes build
   preparation and does not prove that individual tests began.
5. Initial tooling verification passed 27 UI-verifier/lock checks but exposed
   three completion-fixture failures: inserting the phase after the evidence
   path broke the fixture's link parser. Moving details before the evidence
   link preserves that existing format. Logs are retained under
   `target/timeout-diagnostics/initial-*-tooling.log`. Final local acceptance
   is pending. The recurring Docker delay itself is not diagnosed or repaired
   by adding this evidence collection.
6. Final tooling checks passed 35 verifier/lock/diagnostic tests in 13.244 s
   and 22 completion-runner tests in 6.552 s, with no failures or skips.
   Tests cover partial timeout output, probe budgets, original-failure
   preservation, credential/process filtering, preparation and cleanup phases,
   and the existing notification link parser. Logs and source hashes are in
   `target/timeout-diagnostics/`. Model dispatch
   `20261027T124439Z-ecd738502eb4`, PID 62913, timeout 600 s, checks the frozen
   source based on `f9dc601ef2c7967a2c002a4b3e85d5f1458f5051`.
7. That first model dispatch naturally reproduced the preparation timeout:
   the container query took 20.009 s without output; no test command started.
   New diagnostics completed within the budget, with the whole failed attempt
   taking 22.082 s. The Docker client version returned in 0.013 s and context
   `desktop-linux` in 0.017 s, while the server-version probe exceeded 2 s.
   Its result retains `timed_out`, `failure_phase: preparation`,
   `step_timeout_seconds: 20` and `command_started: false`.
8. A read-only follow-up of Docker Desktop's existing host log correlates this
   attempt with VM wake-up: at 12:44:39 UTC the container-list request moved
   Docker from idle to busy and started its VM after 1h10m56s idle; the busy
   interval ended about 31.3 s later. A selected excerpt is retained in
   `target/timeout-diagnostics/docker-wake-evidence.log`. This identifies a
   concrete cold-start delay for this attempt, not every older timeout. No
   Docker setting, test deadline or automatic retry policy was changed.
   After a bounded read-only check confirmed server 29.8.2 was responding,
   model acceptance was dispatched again as `20261027T124723Z-0838d932f9e3`,
   PID 63787, timeout 600 s, with unchanged executable sources.
9. Nine documentation consistency tests and strict MkDocs passed before
   evidence closure. Final model/smoke acceptance and the final documentation
   closure are pending.
10. Model retry `run-atlk_02r` passed 4,776 checks with zero failures,
    errors or skips in 134.116 s. Final review then added an edge-case test
    and preserved the original execution phase/timeout when cleanup also
    fails. The unchanged verifier/lock diagnostics and updated completion
    runner passed 35 tests in 13.697 s and 23 tests in 7.760 s respectively,
    with no failures or skips. Final-source model dispatch is
    `20261027T125149Z-95961652a03e`, PID 64872, timeout 600 s; smoke follows
    successful model acceptance. Source hashes were refreshed after this
    review correction; no further executable changes are planned.
11. Model run `run-wkjqfpah` passed all 4,776 checks with zero failures,
    errors or skips; completion `20261027T125149Z-95961652a03e` took
    131.683 s. Smoke was dispatched as `20261027T125536Z-37bf1f48190d`,
    PID 65700, timeout 480 s. A final review found that a timed-out process
    listing also needs the unrelated-process filter already used for complete
    output. The shared excerpt filter and a regression test now cover both
    paths. Final tooling checks passed 36 verifier/lock/diagnostic tests in
    13.840 s and 23 completion-runner tests in 7.261 s, without failures or
    skips. Source hashes were refreshed; model dispatch
    `20261027T125715Z-345e3e9cd3a7`, PID 66591, timeout 600 s, and a subsequent
    smoke run must cover this correction before acceptance.
12. The earlier smoke `run-kegmn5k8` passed all 21 checks with zero failures,
    errors or skips in 200.046 s. After the output-filter correction, final
    model run `run-ze3mdwct` passed all 4,776 checks with zero failures, errors
    or skips in 133.954 s; its separate 101.430 s lock wait was excluded from
    execution time. Source hashes still match the reviewed candidate.
    Final smoke was dispatched as `20261027T130126Z-faf0e1dc1d9a`, PID 68271,
    with a 480 s supervisor timeout and the unchanged 420 s acceptance budget.
13. Final smoke `run-o9p1tf4c` passed all 21 fixed smoke/support checks with
    zero failures, errors or skips; completion `20261027T130126Z-faf0e1dc1d9a`
    took 202.298 s including preparation and cleanup, within 420 s. The final
    four executable/test file hashes still match `target/timeout-diagnostics/source.json`,
    based on `f9dc601ef2c7967a2c002a4b3e85d5f1458f5051`. Review covered callers,
    budgets, failure preservation, notification compatibility and bounded output
    filtering. No game code or player behavior changed, so additional targeted
    gameplay scenarios and Java formatting do not apply. Local acceptance is
    59 tooling tests, 4,776 model/JUnit checks and 21 smoke/support checks,
    all passing without skips. Nine documentation consistency checks and strict
    MkDocs passed at closure. R-0317 is done. Historical failures remain failed;
    no remote CI, publication or Docker configuration changes were requested.
