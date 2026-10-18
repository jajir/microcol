# Completion callbacks for Codex

[R-0243](../requirements/done/R-0243.md) adds one host-side
[runner](../../scripts/codex/run.py) for long worker, Docker and CI commands.
It uses the already installed Python, Codex CLI, Docker and GitHub CLI. It does
not install a daemon, change a model, or put Codex credentials inside a container.

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
   names/counts/skips, required full suites, formatting, independent review,
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
python3 scripts/codex/run.py --thread TASK_UUID --timeout 3600 docker
```

For an already justified targeted selection, forward the same Maven arguments:

```sh
python3 scripts/codex/run.py --thread TASK_UUID --timeout 3600 docker -- \
  -Dtest=PageVerificationContractTest -Dsurefire.failIfNoSpecifiedTests=false
```

The 60-minute watchdog matches the [full functional CI budget](../development-builds.md#full-functional-regression-budget). It does not justify accepting
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

## GitHub Actions

After push, discover the runs for the **full pushed commit SHA**, for example:

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

1. [R-0247](../requirements/R-0247.md) follows two timed-out attempts of
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
   Live full-suite acceptance and publication are still pending for the repair.
