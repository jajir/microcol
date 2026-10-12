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

For Java tasks, include the requirements from `docs/code-formatting.md` and make
`.vscode/eclipse-formatter.xml` accessible in the worktree. The caller applies
that profile to changed Java files after unloading Qwen and before verification
and acceptance; specify the scoped formatter command in the task. Do not rely on
VS Code save hooks for worker edits.

```text
Task ID (shared across retries):
Attempt ID and prior attempt evidence:
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
For a repair: previous failure evidence, diagnosed cause and targeted correction:
Original failing command/check and unchanged acceptance criteria:

Available tools and who applies the changes:
Output mode: direct edits or text-only replacements (no worker tools/writes):
For replacements: exact current text and expected match counts; worker proposes new text:
Verification owner: caller runner (Maven and required test runs).
Java formatting profile and caller ownership:
Any explicitly allowed lightweight worker checks:
Java formatting command and scoped files (or not applicable with reason):
Caller verification commands and working directories:
Intended test names/selection, expected counts, and required checks:
Output locations for the patch, logs, and test reports:

Assigned local runtime/model identity and how the caller verifies it:
Context, output, concurrency, and memory limits:
Repair limit after the first implementation:
Per-attempt limit, cumulative task budget and worker time already used:
Effective attempt deadline and caller cancellation procedure:
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
- Java formatting: files, profile, executed command/runner or pending caller work.

Unverified:
- Checks that did not run, why, and any commands merely proposed.
- Explicitly identify Maven/tests left for the caller runner.

Blockers:
- Concrete failure, missing context, or decision required from the main agent.

Failure and recovery (not applicable only if no failure occurred):
- Original error/check and evidence; diagnosed cause or remaining uncertainty.
- Targeted correction and owner; actual retry and before/after verification.
- Outcome: verified recovery | partial | unresolved; reason if no retry ran.
- Any reusable prevention and its owning instruction/tool.

Run data, when available:
- Actual runtime and exact local model identity, including digest if exposed.
- Number of attempts and repair rounds; elapsed time and token usage.
- Cumulative worker time and remaining task budget, including previous attempts.
- Separate model loading, input processing, and generation times if reported.
- Unavailable measurements explicitly marked unavailable.
```
