# Worker task and result contract

The caller uses [worker-role.md](worker-role.md) as the single source of model
role instructions, supplied through `model_instructions_file`. Send a completed
task template, its required result format, and the relevant project instructions
as task input. Reading these files in the main agent does not send them to the
worker. Take actual operating values and the direct `codex exec` invocation from
[the operating document](../../../../docs/ai/local-qwen.md); do not treat proposals
there as configured services.

## Focused handoff

Keep the assignment small enough to implement and check within its budget. Give
exact worktree-relative paths, the current relevant fragments/callers, expected
behavior and explicit acceptance checks. Omit unrelated history and broad file
dumps; preserve required interfaces, edge cases and fixtures.

For a simple known edit, prefer text-only replacements over shell editing. State
that the worker must not run tools or write files in this mode. Request a JSON
object with a `replacements` array; each entry contains `file`, `old`, `new` and
`expected_matches` (a positive count, including deliberate repeated call sites).
Use literal current text, not invented surrounding code. The caller checks all
paths against the allowlist and all match counts before applying the batch; a
mismatch requires fresh context or a targeted repair, not a fuzzy replacement.
This is an output contract, not an installed automatic patch runner.

Repair input includes the actual error/check, current source or rejected output,
and the one correction needed. Keep acceptance criteria and remaining budget;
do not restart an unchanged broad prompt. The caller reviews, applies the shared
Java formatter and runs verification regardless of output mode. Model-proposed
checks remain pending until actually executed.

## Task template

Fill every field before dispatch. Use `not applicable` with a reason where
appropriate. Include file contents when the worker cannot read the referenced
paths. This template is independent of the chosen transport or agent runtime.

```text
Task ID:
Assigned isolated worktree and host:
Baseline revision and relevant current state (including existing changes):

Goal:
Acceptance criteria:
Allowed files and changes:
Excluded work:

Applicable project instructions (paths with accessible content, or inline):
Relevant code and context:
Exact current source fragments and relevant call sites:
Interfaces to preserve:
Existing test example:
Expected behavior and important edge cases:
For a repair: actual failure, current state, targeted correction and unchanged checks:

Available tools and who applies the changes:
Output mode: direct edits or text-only replacements (no worker tools/writes):
For replacements: exact current text and expected match counts; worker proposes new text:
Verification owner: caller runner (Maven and required test runs).
Java formatting profile, scoped command and caller ownership:
Any explicitly allowed lightweight worker checks:
Caller verification commands and working directories:
Intended test names/selection, expected counts, and required checks:
Output locations for the patch, logs, and test reports:

Assigned local runtime/model identity and how the caller verifies it:
Context, output, concurrency, and memory limits:
Repair limit after the first implementation:
Deadline and caller cancellation procedure:
Other agreed limits and stopping conditions:

Required result: use the result format below.
```

## Result format

```text
Task ID:
Status: completed | partially completed | blocked

Changes:
- Actual diff/patch or result location, baseline, and changed files.
- Brief description of the implemented behavior.
- For text-only output: generated replacements; application and checks pending caller.

Verification:
- For each executed command: working directory, command, runner, exit code.
- Intended test names, actual test selection, counts, failures, and skips.
- Accessible log and report paths.

Unverified:
- Checks that did not run, why, and any commands merely proposed.
- Explicitly identify Maven/tests left for the caller runner.

Blockers:
- Concrete failure, missing context, or decision required from the main agent.

Failure and recovery:
- Actual failed check, diagnosis, targeted correction, rerun and remaining issues.
- Explain any skipped retry or main-agent takeover; never claim an unrun check passed.

Run data, when available:
- Actual runtime and exact local model identity, including digest if exposed.
- Number of attempts and repair rounds; elapsed time and token usage.
- Separate model loading, input processing, and generation times if reported.
- Unavailable measurements explicitly marked unavailable.
```
