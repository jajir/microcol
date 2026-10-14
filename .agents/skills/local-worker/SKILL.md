---
name: local-worker
description: Delegate a small implementation, test, or repair task to a configured local worker, then review its changes and runner evidence. Use for bounded work with clear acceptance criteria; this skill does not install or configure the worker.
---

# Local worker

The main agent owns the task definition, required behavior, and acceptance of the
result. The worker implements the assigned task without further delegation.

Apply the project's cloud-token priority in `AGENTS.md`: prefer suitable local
work over unnecessary parallel cloud agents, keep the handoff and returned
evidence concise, and retain all required checks. Start new ordinary jobs with
medium/64k; follow the operating document for a diagnosed high/64k escalation
within the same job and cumulative time/repair budget.

The selected route is `codex exec` supervised by the shared
[launcher](../../../scripts/local-worker/worker.py), not a copied per-job runner.
Read [the operating document](../../../docs/ai/local-qwen.md) for the
verified invocation, current status, host, model, resource limits, deadline,
repair limit, and test commands. Treat proposed settings as proposals, not working
services. Keep project-specific operating details there rather than duplicating
them here.

## Select and prepare a task

1. Choose a small task whose outcome can be checked against explicit behavior.
   Keep architecture decisions, unclear game rules, and work requiring broad
   context with the main agent. Do very small changes directly when preparing
   and reviewing a delegation would cost more than the work.
2. Check that the documented CLI, local runtime and model are available on the
   intended host. The child process edits files; the caller owns the actual test
   runs. If the worker is unavailable, report that and continue directly where
   appropriate; this skill grants no permission to install a runtime or build
   another integration. Never silently substitute a cloud model for a failed
   local run or change the main agent's model settings.
3. Inspect applicable project instructions and the relevant working-tree state.
   Prepare an isolated worktree at the agreed baseline and record any relevant
   uncommitted changes included in it. Do not silently omit those changes or let
   the worker edit the caller's checkout.
4. Read [the task and result contract](references/worker-instructions.md) and fill
   the template with a complete, limited task. Set `model_instructions_file` to
   the accessible [worker role file](references/worker-role.md), and explicitly
   pass the completed task and relevant project rules. Reading instructions in
   the main agent does not deliver them to the worker. Make referenced files
   accessible in the assigned worktree, or include their necessary contents.
5. Supply exact worktree-relative paths and current relevant methods/call sites,
   expected behavior, edge cases and acceptance checks. Include a test example
   when needed, not whole files or conversation history by default. Identify
   what may change and what must stay unchanged. Budget for instructions, input,
   tool history and output together; split work that does not fit rather than
   dropping requirements.
   Trace callers of changed shared capabilities before freezing the contract;
   resolve any conflict between the prescribed design and preserved behavior,
   and include the affected source and acceptance boundary in the handoff.
6. For small edits whose source is already known, prefer text replacements for
   the caller to apply instead of asking the worker to rediscover files and script
   edits. Use the [focused handoff contract](references/worker-instructions.md#focused-handoff)
   to specify the output mode and exact match counts. Keep direct editing for
   tasks that actually need it; do not impose replacement mode on all work.

## Execute and receive

The caller prepares the documented job manifest, prompt and role, then invokes
the launcher. It validates preflight, freezes the profile, captures output/exit
status and checks actual model identity. Text-only mode disables command
execution and uses a read-only sandbox. Keep only one worker request active. Enforce
both the per-attempt limit and cumulative task budget from the operating document.
Before each dispatch, record prior worker elapsed time and cap the attempt by the
remaining task budget; retries and new attempt directories do not reset it.
Follow the documented cancellation procedure on either limit and preserve partial
changes and logs. An isolated worktree separates changes but does not itself
restrict filesystem access; use the documented
execution restrictions as well.

The worker may fix ordinary mistakes within its assigned scope and repair limit.
Changes to required behavior, interfaces, or scope return to the main agent for a
decision. Exhausted repair limits, insufficient context, and runtime or resource
failures return a partial or blocked result with the available changes and logs.
Do not start an unbounded retry loop.

Require the result format in the task contract. Keep full process logs and
reports accessible; bring only the result and relevant failures into the main
conversation. After the worker finishes, inspect its diff and unload the local
model before Maven. The caller runs the assigned verification commands against
that worktree, records actual exit codes and reports, and supplies any relevant
failures for a bounded repair. Distinguish worker-proposed checks from tests the
caller actually executed.

For replacement output, use the launcher's separate `apply` command. It parses
the response, validates every ID/path, original file hash and exact anchor count
before writing the batch. Caller-owned anchors let new tasks request only IDs
and new text. A single outer `json` Markdown fence is removed and recorded;
extra prose, multiple blocks or invalid JSON still fail parsing. Inspect
application success before formatting or testing; a rejected
batch is not an applied patch. Apply only the validated edits,
then review, format changed Java files with the shared project profile, and run
the same acceptance checks as for direct worker edits. Record generated text
separately from caller application, formatting and verification.

## Diagnose, repair and rerun

After a failed or incomplete attempt, inspect the actual error, commands, diff
and remaining budget before deciding the next step. A timeout or exit code alone
is not a diagnosis; distinguish caller setup, tool/code errors, missing context,
resource limits and acceptance failures. Record uncertainty when the cause is
not established.

Fix caller-owned setup issues before redispatch. For a simple in-scope worker
error, send the worker the specific failure, current relevant code/diff and unchanged
acceptance check, then require a targeted repair while budgets permit. Do not
replace repairable worker work with a main-agent rewrite by default or replay
the same failing prompt without a concrete correction.

After the repair, the caller reruns the failed command/check and required
acceptance checks. Preserve before/after evidence and distinguish a verified
repair from a partial result. If a retry is unsafe, out of scope, budget-exhausted
or blocked by an unavailable runtime, record the reason and hand back the
remaining work; do not reset the budget or claim recovery. Capture a reusable
prevention in the owning instructions/tool when the evidence warrants it.

## Review and accept

Inspect the actual diff against the assigned baseline and current checkout.
Check scope, required behavior, and whether tests assert the intended rules.
Match the report to runner output, including exit codes, intended test names,
counts, skips, and report paths. A successful build with no intended tests run
does not verify the task.

Use the repair loop before taking over repairable worker work. If direct
completion is necessary, record why another worker repair was not appropriate and
separate main-agent repairs from worker output. Run additional checks only to
resolve remaining uncertainty. Bring accepted changes into the caller's checkout
after checking its current state and preserving unrelated edits. Report accepted changes,
verification, omissions, and available local-run metrics; do not claim a fully
verified task while required checks are missing. Record measured operating
findings in the operating document without turning a single successful task into
an unsupported savings estimate.
