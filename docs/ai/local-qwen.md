# Codex and a local development worker

Status as of **2026-10-14: new ordinary jobs use GPT-OSS 20B medium,
context 98304 and a 65536-token generation allowance**, under
[R-0039](../requirements/done/R-0039.md). After diagnosed medium failure, the caller
may escalate the same job to high/64k with unchanged time and repair budgets.
This is the user's operating policy, not evidence that medium/64k has better
coding quality. Earlier trials and their recommendations below remain dated
historical evidence; R-0039 supersedes their default-selection policy.
Qwen2.5-Coder 14B Instruct Q4_K_M is installed as another explicit profile. Astra remains the main
agent and supervises a separate local Codex CLI process, checks the result and
owns integration and publication. The current operating profile below replaces
Qwen3.5-9B for new assignments; dated Qwen results remain historical evidence.

The host is the **16 GB Mac mini**. Preserve the main agent's model and reasoning
settings. The goal is more correctly completed work within the same Codex budget;
no general reliability improvement or Codex-budget savings percentage has been
measured. The operating page and
log directory retain their `local-qwen` names to preserve existing links and
artifacts; they are not model selectors.

The default [time budgets](#time-budget-2026-10-14) are two hours per
attempt, four hours cumulative per task and at most two repairs across medium
and high combined. The [64k-output experiment](#explicit-gpt-oss-high-with-64k-output)
established the installed alias used by both reasoning levels.
The user explicitly accepts slower inference for this model trial. A completed
smoke test is not evidence of general Java coding quality or sufficient capacity
for larger assignments.

## Cloud-token priority and medium-to-high policy — 2026-10-14

1. [AGENTS.md](../../AGENTS.md#local-worker) owns the project priority: saving
   cloud tokens takes precedence over speed and unnecessary parallel cloud work.
   Apply it in the current task, not only future sessions. Use local workers for
   suitable bounded work; do trivial edits directly when delegation costs more.
   Additional cloud agents need a stated reason based on expected total-token
   savings or a concrete quality concern, not elapsed-time savings alone.
2. Reuse known context, send exact relevant code and acceptance criteria, and
   return concise diffs/results/failures rather than full logs. Avoid duplicate
   cloud implementation, repeated broad discovery and redundant reviews. Keep
   required checks and independent acceptance; local generation is not itself a
   measured saving once caller preparation/review/repair costs are included.
3. Start with the default medium/64k profile. Correct caller setup or unclear
   instructions first. Target simple code errors with a focused repair. If medium
   still cannot solve the task or exhausts generation, escalate with the command
   below before consuming the remaining repair/time allowance. No additional user
   approval is needed for this in-scope transition.
4. Escalation is a caller decision, not an unattended retry service. Preserve the
   same job, worktree, task scope and acceptance checks. The launcher allows only
   medium to high with all other profile fields unchanged, records the reason,
   checks the same installed digest and retains cumulative time/attempt counts.
   High repairs must keep selecting the high profile. No downgrade, unrelated
   profile change or new-directory budget reset is allowed.
5. R-0039 verification covers configuration, installed alias and launcher budget
   behavior; it does not rerun the two-hour coding benchmark. The main agent
   performs this small policy/configuration/launcher change directly because a
   separate handoff and review would add overhead. No extra cloud agents are used.

```sh
# First attempt and ordinary medium repair:
python3 scripts/local-worker/worker.py run "$worker_job"
# After diagnosis, retaining the same job and remaining allowance:
python3 scripts/local-worker/worker.py run "$worker_job" --profile .codex/local-worker/gpt-oss-high-64k --escalate-reason "Medium exhausted generation without a final answer"
# A subsequent high repair omits --escalate-reason but retains --profile:
python3 scripts/local-worker/worker.py run "$worker_job" --profile .codex/local-worker/gpt-oss-high-64k
```

The reason above is an example; record the actual observed failure. A high
attempt counts as a repair, leaving at most one further repair if escalation
follows the first medium attempt. Required tests still decide acceptance.

Verified on **2026-10-14**: all **21 launcher tests**, **9 documentation tests**
and the strict MkDocs build passed. Tests cover escalation, subsequent high
repair, missing reason, forbidden changes/downgrade and preserved time/repair
limits. The installed alias matches both profiles and supports medium/high;
the effective default command selects medium and context 98304. No model was
loaded and no new inference was run. `target/r39/profile-verification.json`
retains configuration evidence; publication is verified for the resulting commit.

## GPT-OSS high with a 64k generation allowance — 2026-10-14

1. [R-0038](../requirements/done/R-0038.md) adds the explicit 96k-context / 64k-output
   high profile described below. It replays the relation helper from R-0036 at
   `61789f8169248f7ec1987d48be4b750311dcc81f`. Independent review confirmed all
   five initial source/test hashes, role and replacement anchors match the
   original high attempt. Only task identifiers and runtime metadata differ.
2. The known contradictory attack handoff is not replayed. Its previous partial
   result remains in the paired-trial record; a corrected attack task would
   require a new contract and would not be an unchanged replay.
3. Evidence lives under `~/Library/Logs/Microcol/local-qwen/jobs/` in
   `2026-10-14-r37-high64-relations` and `2026-10-14-r37-comparison`. These retain
   the unpublished draft prefix; concurrent Guava work used R-0037, so the final
   requirement is R-0038. A caller
   preparation error initially left the owned running marker after alias
   creation. The launcher refused it before creating any model attempt. The
   caller verified the owner process had exited, archived the preflight logs
   and removed only its stale marker. This consumed zero model tokens and is
   recorded separately from coding failures.
4. The first attempt completed normally after **6987.117 seconds** (116.45
   minutes), reporting **2799 input / 61585 output tokens** and a 6864-byte final
   answer. Both the actual loaded context **98304** and the server's effective
   generation limit **65536** were verified. The run used the larger allowance:
   output exceeded the original 16384 cap, and a complete JSON replacement
   reached the caller. The 7200-second deadline was not hit; a contemplated longer
   replay was therefore unnecessary and was not run.
5. Application and shared Java formatting succeeded. Docker `run-43Prdm`
   compiled the candidate and ran **8 model tests with 1 failure, 0 errors/skips**;
   source-contract cases were not reached. `HashSet.contains()` used
   `Player.equals()` (name and computer flag), accepting an equal but unregistered
   player. This violates the original exact-object-identity contract. Independent
   review confirmed the same blocker and found no other concrete violation.
   A targeted worker repair changed only `validateEndpoints` to check both
   registered objects by reference identity. The caller did not rewrite Java.
6. The repair completed in **167.531 seconds**, using **2007 input / 864 output
   tokens**. Shared formatting and Docker `run-KbQtcx` passed **8
   EuropeanRelationsTest + 3 PageVerificationContractTest cases**, zero
   failures/errors/skips. Independent review confirmed the identity fix and the
   remaining contract. The isolated replay is **accepted after one repair**;
   no candidate Java or replay fixture is integrated into main.
7. Total worker time is **7154.648 seconds (119.24 minutes)** and total reported
   usage is **4806 input / 62449 output tokens**. Medium completed the same
   initial task with one repair in **393.789 seconds**, using **6030 output
   tokens**. This high profile took about **18.2 times the worker time and 10.4
   times the generated tokens**, reaching the same acceptance checks. The larger
   budget enabled a complete high answer in this run, but did not demonstrate
   better quality than medium. Keep medium as the ordinary default; high/64k is
   an explicit option, not an automatic fallback.
8. Both attempts loaded the same digest and actual **98304-token context**.
   Runtime allocation was **14,249,025,861 bytes total / 10,290,525,633 bytes
   GPU**; these are allocation reports, not host RAM or process RSS. The first
   run allocated **1224 MiB non-SWA + 9.56 MiB SWA KV cache**, using existing
   `q8_0` settings. The log reports 25/25 layers offloaded but also **2554.98 MiB
   CPU_REPACK** model storage, so that layer count does not mean every weight
   stayed on the GPU. First-run decoding averaged **8.92 tokens/second** and
   varied during the run. Host swap peaked at **13494.12 MiB** across both
   attempts, including unrelated processes. Models were unloaded before Docker
   and the owned lock was released after acceptance.
9. Context, generation allowance and deadlines change together, and sampling
   remains unseeded. The observed answer needed more than 16k tokens, but this
   single replay does not isolate the output limit's individual causal effect,
   establish general Java reliability or measure a Codex-budget saving. The
   provider reports zero separated reasoning tokens; that field does not mean
   reasoning was disabled. The generation total includes the final answer and
   any other generated content. Both attempts made zero tool calls.
10. Caller checks passed **18 launcher tests**, **9 documentation tests**, the
    strict MkDocs build, rendered-page/link review and `git diff --check`.
    Publication is checked separately against the final source commit. The
    main-branch change contains one explicit profile and its documentation.

## GPT-OSS high and Qwen2.5-Coder paired trial — 2026-10-14

1. [R-0036](../requirements/done/R-0036.md) compares the existing GPT-OSS at medium
   and high with the explicitly requested Qwen2.5-Coder 14B Instruct Q4_K_M.
   All use context 32768, output 16384 and the existing 1800/3600-second limits
   with at most two repairs. Qwen has no thinking level; its profile uses none.
2. The two historical main baselines are `61789f8169248f7ec1987d48be4b750311dcc81f`
   for the relation helper and `a43507d5fbd700195d4e5ea10f9e82a99d4b73bf` for
   equipment-aware attack roles. Six detached worktrees receive the same initial
   source, tests, role and task prompt per case. The comparison manifest records
   matching source/prompt hashes. Previously completed or rejected production
   implementations are not copied into these inputs.
3. The new handoff supplies whole methods for attack-role replacements and asks
   for IDs/new text instead of repeating old source. Medium is rerun with that
   same handoff so an input improvement is not mistaken for a model improvement.
   The caller applies validated responses, formats with the shared Java profile,
   reviews semantics and runs unchanged Docker acceptance after model unload.

| Profile / task | Worker seconds, including repairs | Result |
| --- | --- | --- |
| GPT-OSS medium / relations | 295.010 + 98.779 = **393.789** | Accepted after one worker repair; **8 model + 3 source-contract tests** passed, zero failures/errors/skips. |
| GPT-OSS high / relations | 920.118 + 916.083 = **1836.201** | Rejected: both attempts exhausted 16384 output tokens without a final message. No source applied; **0 tests run**. |
| Qwen / relations | 146.581 + 161.615 + 191.487 = **499.683** | Rejected after both repairs: last revision ran **8 model tests, 6 failures + 1 error**, zero skips; source-contract tests not reached. |
| GPT-OSS medium / attack | 334.946 + 213.597 + 176.282 = **724.825** | Rejected after both repairs: double-escaped Java line breaks and omitted required methods. No source applied; **0 tests run**. |
| GPT-OSS high / attack | **903.644** | Rejected: 16384 output tokens without a final answer. No source applied; **0 tests run**. |
| Qwen / attack | 124.933 + 124.350 + 55.873 = **305.156** | Partial: prescribed API implemented after two repairs; **40 tests passed**, zero failures/errors/skips. Full handoff not accepted because caller's capture-preservation constraint conflicts with its prescribed API; see below. |

4. Medium's first relation implementation compiled, but two of eight model cases
   rejected `NullPointerException` where the contract required
   `IllegalArgumentException`. A targeted worker repair corrected the constructor
   checks; the caller made no Java implementation rewrite. The successful rerun
   is Docker `run-PaebYW`. Total reported usage is **5253 input / 6030 output
   tokens**, including repair; these are local-run costs, not Codex savings.
5. High's first relation attempt produced no final answer after **2796 input /
   16384 output tokens**. Its 920.118 seconds remained below the attempt deadline;
   input plus generation remained below the 32768-token context. This is output
   budget exhaustion before a usable final response, not context overflow or
   repository wandering. The launcher rejected the empty response and unloaded
   the model. The targeted compact-output repair repeated the same failure with 3041
   input / 16384 output tokens. Total usage was **5837 / 32768**. One repair and
   1763.799 worker seconds remained; the caller skipped another attempt because
   the same generation-budget failure had repeated after a concrete correction,
   with no delivered code or specific source/tool error to repair. Another
   unchanged dispatch offered no concrete correction to that limit; another
   sampled answer might differ, but changing the profile would be a different
   experiment. No caller implementation takeover occurred.
6. Evidence is retained in `~/Library/Logs/Microcol/local-qwen/jobs/` under
   `2026-10-14-r36-{medium,high,qwen}-{relations,attack}` and the shared
   `2026-10-14-r36-comparison` directory. Per-attempt records distinguish model
   output, application, formatting, fresh test XML and acceptance. Replayed Java
   candidates remain isolated; main receives tooling, profiles and documentation.
7. This is one paired run per task/profile with bounded repairs, not a statistical
   Java benchmark. Sampling uses installed defaults without a fixed seed. The
   first medium run overlaps the final minutes of the Qwen weight download;
   wall time also includes loading and cleanup. Host swap includes unrelated
   processes and cannot be treated as model-exclusive memory use. No measured
   Codex-budget saving follows from this experiment.
8. The existing service uses `q8_0` KV cache for both models; no service settings
   changed for this trial. Qwen's first 32k run allocated **3264 MiB of KV cache**,
   offloaded **46/49 layers** to the GPU, and `/api/ps` reported **12,791,001,905
   bytes total / 11,465,004,154 bytes GPU**. These are runtime allocation reports,
   not total host RAM or process RSS. The configured 32k context actually loaded;
   a successful load alone is not coding acceptance.
9. Qwen repeatedly wrapped otherwise parseable JSON in a Markdown fence. The
   shared caller now strips exactly one outer `json` fence and records that
   transport normalization; source and path validation remain unchanged. The
   raw responses are preserved. This does not repair Java code: the first
   delivered relation candidate had missing validation and incorrect player
   identity handling; repair 1 failed compilation with incompatible
   `HashSet<Player>`/String types. Repair 2 removed the state and returned
   placeholder PEACE results. Docker `run-x9fGSM` confirmed six failures and one
   error in eight model tests. Both repairs were exhausted, with no caller Java
   takeover. Total reported usage was **11129 input / 3449 output tokens**.
10. Medium's attack response omitted the common `canAttack()` implementation,
    replaced the whole master declaration with a fragment and encoded literal
    backslash-n characters in Java. Repair 1 corrected those defects but omitted
    the interface declaration and equipment override; whole-batch application
    rejected its unchanged anchor before writing. The final repair again omitted
    the base method and returned literal backslash-n strings. Both repairs were
    exhausted, with no Java applied, no tests run and no caller takeover. Total
    reported usage was **9835 input / 12040 output tokens**. More explicit whole
    method anchors did not by themselves make this medium run succeed.
11. High's attack attempt repeated the same generation-limit failure after
    **3163 input / 16384 output tokens**, below both time and context limits.
    Two repairs and 2696.356 worker seconds remained. The caller explicitly
    skipped both retries because this was the third identical budget exhaustion
    across the high trial, including the earlier compact-output repair, with no
    delivered source to correct. No caller takeover occurred. Raising generation
    above 16384 requires a separate profile, subsequently tested under
    [R-0038](../requirements/done/R-0038.md); these results do not prove
    that high fails at every possible budget. The ordinary default returns to
    medium, while the exact high trial remains selectable explicitly.
12. Qwen's first attack response omitted three methods and invented a new speed
    for armed colonists. Repair 1 restored movement and most required edits but
    omitted `AbstractUnit.canAttack()`; Docker compilation failed for frigate,
    galleon and wagon before any test ran. The final targeted repair added only
    the missing base method. Docker `run-xT65HO` passed **10 AttackRoleTest,
    19 EuropeanHostilityTest, 8 EuropeanRelationAiTest and 3 source-contract
    cases**, with no caller Java rewrite. Total reported usage was **9665 input /
    1779 output tokens**. One outer JSON fence was normalized during application.
13. Independent review found a **caller handoff defect shared by all three
    attack inputs**: prescribing the master-blacksmith type filter change while
    requiring unchanged capture behavior is inconsistent with the historical
    `Colony.planCapture()` defender check, which still calls
    `unit.getType().canAttack()`. The resulting type-level change also affects
    capture eligibility. Qwen correctly followed the prescribed API; this is
    not a model deviation. The 40 passing tests do not cover this boundary, and
    no new capture case was executed. Initial API acceptance was therefore
    corrected to **partial, full handoff not accepted**, with both review records
    retained. Do not reuse this handoff for production: first resolve capture
    behavior and include its source and acceptance test. Changing only Qwen's
    task at the end would no longer compare identical initial inputs.
14. The result supports keeping medium as the ordinary default, retaining Qwen
    for explicitly bounded trials, and not promoting high at this output limit.
    Qwen delivered the specified attack API with repairs; medium delivered the
    complete relation helper with one repair. Neither result establishes
    dependable autonomous implementation. Every observed run made zero tool
    calls, so repository discovery did not cause these failures. All owned
    locks were released and models unloaded after caller disposition.
15. Caller verification passed **18 launcher tests**, **9 documentation tests**,
    skill validation, the strict MkDocs build, rendered comparison-table/link
    review and `git diff --check`. All three final profiles matched installed
    model metadata; the default is medium, with high and Qwen selected explicitly.
    Publication is verified separately for the final source commit. No gameplay
    code or replay test fixture is part of the main-branch change.

## Larger output and shared launcher verification — 2026-10-14

1. [R-0035](../requirements/done/R-0035.md) implements the user's requested trial:
   GPT-OSS 20B, context 32768, output 16384, medium reasoning, 1800-second
   attempts and a 3600-second task budget with two repairs. The installed alias's
   `/api/show` matches both parameters; its new digest is recorded in the
   [operating profile](#operating-profile-bounded-assignments). The weights were
   reused, not downloaded. Runtime samples matched that alias/digest/context.
2. The shared standard-library launcher replaces copied job scripts. It rejects
   bad baselines/paths/anchors before dispatch, disables command execution for
   text-only work, snapshots the effective profile and checks runtime identity.
   It keeps the existing host-wide lock through caller acceptance, charges
   previous attempts, handles owned-process cancellation/unload and validates an
   entire replacement batch before writing. New prompts request IDs/new text;
   original-prompt replays retain the historical output shape.
3. Two historical tasks were replayed in separate worktrees at their recorded
   main revisions. Their original role, coding instructions, source and acceptance
   inputs were retained; only execution paths, digest and approved budget values
   changed in the first prompts. Repairs retained the original acceptance.
   Both replays made **zero observed command/file/tool calls**. Inference and
   Docker acceptance were serialized, and both owned locks were released.

| Replay | Historical result | New profile result |
| --- | --- | --- |
| `2026-10-14-r35-attack-replay`, baseline `a43507d5` | First attempt empty at 4096 output tokens; original repair subsequently accepted after 384.633 s total. | **Unsuccessful coding replay.** Three attempts used 184.902 / 101.676 / 68.147 s (**354.725 s total**). First result omitted the base implementation and made a pure query throw. Repair 1 duplicated a guard body; final repair broke the remaining terrain condition and was rejected before application. Compilation of the last staged candidate failed, exit 1; **zero tests ran**. Both repairs exhausted. |
| `2026-10-14-r35-relations-replay`, baseline `61789f81` | Two truncated responses and final timeout; **600.216 s**, no worker code accepted. | **Original behavior acceptance passed on attempt 1:** 175.048 s, no manual code repairs. Shared formatting preserved code tokens and idempotence. Docker `run-agOZfU` passed **8 EuropeanRelationsTest cases and 3 PageVerificationContractTest cases**, zero failures/errors/skips, exit 0. |

4. The attack replay produced complete answers but retained coding defects. Its
   partial-method repair anchors proved fragile; no caller rewrite was needed
   because the real product already contains the completed feature. The isolated
   compilation failure is retained as a failed experiment, not a passing game
   change. The relation candidate implements the original behavior with a private
   pair-key class and verbose comments; passing its tests does not establish a
   preferred production design. **Neither replay changed main's gameplay code.**
5. The successful relation response used **2832 input / 2726 output tokens**;
   its 157 loaded runtime samples matched the configured model. Since its output
   stayed below the former 4096 limit, this comparison **does not isolate the
   benefit of the larger output allowance** from tool restrictions or sampling
   variation. The first attack attempt likewise used 3110 output tokens. There
   is one recovered historical task and one unsuccessful replay, not a general
   success-rate, throughput or cloud-token-saving measurement.
6. Full prompts, original prompts, response JSON, runtime samples, exact
   application records, diagnoses and Docker logs remain under the two job IDs
   in `~/Library/Logs/Microcol/local-qwen/jobs/`. The acceptance records explicitly
   distinguish worker output, caller formatting and failures. No failing candidate
   was integrated and no failed check was weakened or relabeled as passing.
7. Caller launcher verification passed **14 behavioral tests** without inference:
   budgets/repair exhaustion, incomplete history/profile drift, stale or escaping
   source paths, ambiguous anchors, invalid whole batches, JSON newlines, legacy
   replay anchors, mode-specific shell settings, foreign/concurrent locks, process
   timeout/cleanup, failed starts and runtime-parameter mismatch. One caller
   regression initially rejected macOS's `/var` symlink in temporary roots;
   canonicalizing the worktree root fixed it and the same tests passed. Diagnostics
   preserve original process errors rather than masking them with a missing-sample
   error. The documentation workflow now runs these portable tests.
8. Local caller checks also passed the **9 documentation tests**, skill validation,
   strict MkDocs build, rendered operating-page review and `git diff --check`.
   Publication is checked separately for the final source commit. No new game
   behavior, Java package or player scenario belongs to this tooling change.

## Instruction ownership

### Unit registry concurrency repair — 2026-10-13

1. Task `2026-10-13-r23-unit-registry`, baseline `b3ba0efc` plus 33 explicitly
   copied R-0023 inputs, followed the actual TC03 `ConcurrentModificationException`
   in the full Docker suite. The caller selected a standard
   `CopyOnWriteArrayList` for ordered, read-heavy membership snapshots; this does
   not make mutable unit fields or compound gameplay actions thread-safe.
2. Attempt 0 took **196.028 s** and returned matching replacement anchors, but
   its test nested a method inside setup, used string unit IDs and an unimported
   Mockito qualifier, installed the blocking callback before setup registration,
   and omitted finally cleanup. The caller rejected the test before application
   and sent these defects for targeted repair. An initial caller suspicion about
   JSON newline escaping was disproved by all three exact match counts of one;
   the review record corrects that observation.
3. Repair 1 took **300.343 s** and timed out after unnecessary source discovery
   despite the text-only contract. Repair 2 was narrowed to the single test
   method and used its remaining **103.859 s**, also without a final result.
   Total **600.230 s**, including cleanup; both repairs and the cumulative
   budget were exhausted. The caller then implemented the regression test.
   Only the two production replacements came from the first worker result.
4. Runtime samples confirmed the configured alias/digest and context **32768**;
   each attempt unloaded before caller Docker checks. Full evidence is in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r23-unit-registry/`.
   Actual red/green and integration checks remain caller-owned and are recorded
   in the [functional test evidence](../functional-tests.md#expert-purchase-implementation-2026-10-13).
   No savings or successful test-authoring claim follows from this attempt.


### Z02 name helper — 2026-10-12

Task `2026-10-12-z02-names`, baseline `8bef9928`, assigned only the
`ColonyNames` helper with explicit interfaces, Unicode validation, global
duplicates and deterministic suggestions. The caller retained architecture,
JSON integrity, dialogs, integration tests and publication. All attempts used
the documented alias/digest and context 8192, confirmed in runtime samples.

| Attempt | Seconds | Evidence and decision |
| --- | --- | --- |
| 0 | 300.122 | Timeout after repeated source/formatter reads despite text-only instructions; no completed source. Repair explicitly confirmed the new interfaces and prohibited further discovery. |
| 1 | 110.898 | Rejected: nonexistent `Character.isControl`, inaccessible constructor, missing NBSP trimming, incorrect validation order and incomplete surrogate check. Caller sent those concrete failures for repair. |
| 2 | 72.580 | Rejected: invalid stream construction and Java-11-incompatible APIs, no actual trimming/control validation, suggestions ignored occupied names. Both allowed repairs exhausted. |

Total worker time **483.600 s**. Completed attempts reported input/output
tokens 3677/1556 and 3754/924; the interrupted attempt has no completed-turn
usage report. No worker implementation was accepted. The main agent took over
only after both targeted repairs failed, then implemented the helper and its
normalization, boundary, duplicate and suggestion tests. No cloud worker fallback
or savings claim. The local model was unloaded before Maven and the owned lock
released. Full prompts, results and process evidence remain in
`~/Library/Logs/Microcol/local-qwen/jobs/2026-10-12-z02-names/`.

This failure does not establish that more time would help. A new interface
absent from the baseline must be explicitly identified in the initial handoff;
repeat source discovery was unnecessary. Tests, not exit code 0, determine
acceptance. Integration verification is recorded in the
[Z02 evidence](../functional-tests.md#z02-pojmenovani-a-identita-kolonie).

| Source | Responsibility |
| --- | --- |
| [AGENTS.md](../../AGENTS.md) | Short routing rule and responsibility for acceptance. |
| [local-worker skill](../../.agents/skills/local-worker/SKILL.md) | Reusable selection, handoff, repair and review workflow. |
| [Worker role](../../.agents/skills/local-worker/references/worker-role.md) | Single source of model instructions, supplied through `model_instructions_file`. |
| [Task and result contract](../../.agents/skills/local-worker/references/worker-instructions.md) | Completed task input and required result format. |
| [Worker Modelfile](../../.codex/local-worker/Modelfile) | Installed weights and the sole configured context/output limits. |
| [Launcher profile](../../.codex/local-worker/profile.json) | Alias, reasoning, attempt/task deadlines and repair limit. |
| [Launcher](../../scripts/local-worker/worker.py) | Preflight, local-only dispatch, tool restrictions, budgets, cleanup and validated application. |
| This document | Microcol environment, operating profile, integration status, verification and measurements. |
| [Rules gap analysis](../gap-analysis.md) | Existing gameplay findings and historical test commands/results. |

The user decides game behavior and priorities. Astra owns task scope, context,
architecture and acceptance. The child edits its assigned worktree; the caller
owns Maven/test execution and its evidence. Instructions do not trigger automatic
installation, configuration changes or a cloud fallback when the worker fails.

### ArchUnit availability check — 2026-10-13

1. For R-0011, preflight confirmed Codex CLI 0.142.3 but `ollama list`
   returned `could not connect to ollama server`. No inference request,
   worker edit or worker attempt was started; elapsed worker time is zero.
2. The main agent implemented the bounded change directly because the required
   local runtime was unavailable. No service setup or cloud worker substitution
   was attempted. Acceptance belongs to [R-0011](../requirements/done/R-0011.md).

### Free-play audit runtime — 2026-10-13

1. Initial preflight found Ollama stopped. The main agent prepared TC21/TC22 and a fixture; the first compile found three helper errors before any UI scenario ran.
2. The user explicitly requested starting Ollama and no further work without Qwen. `brew services start ollama` succeeded. `ollama list` found the existing worker alias, and `ollama ps` confirmed digest prefix `dfe6877b76ae`, context 8192 and GPU execution. No model download or cloud worker substitution occurred.
3. Four bounded tasks have completed their worker attempts; caller verification
   and integration are separate. Baseline was `23b2b013`; the isolated checkout
   was later advanced to `6ed5a8e1` (the independent validation cleanup) and moved
   to a path without spaces for JavaFX verification.

| Task | Attempt durations (seconds) | Review and recovery |
| --- | --- | --- |
| `2026-10-13-freeplay-audit-compile` | 75.787 / 47.928 / 12.241 | First results omitted a repair or used invalid Java. Both targeted repairs were used; three final replacements fixed compilation. Source-contract tests passed 3/3; UI then exposed a wagon-render fixture blocker. |
| `2026-10-13-freeplay-wagon-isolation` | 77.349 / 48.962 / 44.449 | First result referenced an undefined exception; repair fixed it. Caller UI run exposed a wrong image-provider overload supplied in the handoff, a wrong statistics selector/title and genuinely hidden construction controls. Last repair received an oversized caller prompt (8180 input / 12 output tokens) and returned an incomplete fragment. Both repairs exhausted; caller completed those specific corrections, retaining explicit gap observations. |
| `2026-10-13-atlantic-ui-audit` | 132.371 / 300.023 / 39.424 | First result invented APIs and omitted required setup/steps. First repair timed out with a compaction warning and no completed source; exact internal cause is not established. Last repair accepted only two read-only verifiers. Caller supplied fixture, UI sequence and navigation integration, and corrected a caller-provided `Integer` assumption where `getId()` actually returns `int`. |

| `2026-10-13-audit-save-fixtures` | 81.596 / 32.539 | First batch mixed assertions into TC, duplicated setup and missed the clean-profile deletion. Targeted repair supplied two correct isolated-home setup methods; caller added omitted imports and integrated the separate verifier/scenarios. |

4. These are partial delegation results, not autonomous successes. The caller
   also strengthened the purchase-roster assertion to compare the complete
   visible list. The clean-checkout test exposed an ignored source autosave
   influencing earlier local results; the separate task
   `2026-10-13-audit-save-fixtures` isolates startup states without changing
   production behavior. After preparation repair, 16/17 targeted checks passed; the remaining voyage scenario hit a native clipboard exception in Monocle, so final acceptance uses native JavaFX.
5. Keep handoffs below the available total context, including role and CLI
   instructions; do not send two complete classes for a three-fragment repair.
   The voyage task shows that even a text-only prompt can fail: accept only
   reviewed source and actual tests. Remaining time never resets the two-repair
   limit. No cloud worker fallback or token-savings claim was made.
6. Job directories under `~/Library/Logs/Microcol/local-qwen/jobs/` retain prompts,
   raw responses, elapsed times, reviews and caller corrections. Qwen was unloaded
   before Maven; Ollama remains running as requested. The final native CI passed 683 game tests and all 64 functional/control tests
   at `64927cde`, including all 12 new scenarios. Actual player evidence and
   publication belong to the [functional audit](../functional-tests.md#free-play-audit-2026-10-13).

## R-0012–R-0018 implementation — 2026-10-13

1. Baseline `85644389`; the configured `microcol-gpt-oss:20b-32k` worker runs in separate detached worktrees. Ollama, Codex CLI and Docker were available. The caller owns architecture, integration, actual checks and publication; no cloud worker substitution.
2. Each task retains the 300-second attempt, 600-second cumulative and two-repair limits. Generated text is validated against exact source matches before application. Model inference and Maven/Docker checks run separately.

| Task | Worker seconds by attempt | Accepted output and recovery |
| --- | --- | --- |
| `2026-10-13-r12-r18-menu-save` | 205.994 / 237.764 / 37.052 | First response made no edits and returned an incomplete patch with the wrong readiness API. First repair produced exact replacements but imported `PersistingTool` from the wrong package. Final focused repair corrected the import. Caller applied replacements, removed leftover blank lines, formatted six Java files and strengthened the player scenarios. Docker passed 13 selected checks, including fresh/existing autosave, cancelled load, settings return, navigation and campaign-complete start. |
| `2026-10-13-r14-player-name` | 162.190 / 300.198 / 114.569 | First draft used incorrect whitespace trimming and did not validate saved text. First repair ignored the text-only scope, repeatedly searched files and timed out. Final focused repair reused `ColonyNames.normalize` and validated stored display names. Caller’s first test run had one setup error from mocking final `PlayerStore`; using its real constructor fixed that test setup. All six unchanged boundary/persistence expectations then passed. |

3. These accepted results required review and repairs; CLI exit zero alone did not establish correctness. The first two tasks consumed 480.810 and 576.957 worker seconds respectively. No token-saving or autonomous-completion claim is made.
4. Equipment, unloading and complete integration checks passed. Final acceptance counts and delivery evidence are in the [functional record](../functional-tests.md#free-play-implementation-2026-10-13).
5. Prompts, responses, runtime samples, rejected results and caller evidence are retained under `~/Library/Logs/Microcol/local-qwen/jobs/` using the task IDs above. Production changes were not accepted until their assigned caller checks passed.

### R-0012–R-0018 additional local assignments — 2026-10-13

1. `2026-10-13-r14-r15-maps` used 263.787, 155.354 and 79.207 seconds
   (498.348 total). The first output was truncated; the first repair confused
   the JSON schema and map content. The final repair supplied usable terrain
   generation but allowed hills/prairie to overwrite water. After both repairs,
   the caller restricted those predicates to land and completed French content.
   This is partial reuse, not autonomous worker completion. Caller corrections
   also fixed the generator working directory and two test setup errors. Both
   real-model map tests then passed, including disk round trips and topology.
2. `2026-10-13-r16-colony-menu` used 136.007 and 102.294 seconds
   (238.301 total). The first result omitted selection-time ownership validation
   and the requested return type. One targeted repair corrected both. The caller
   applied the shared formatter and ran TC12, TC21, TC23 and the page contract in
   Docker: 13 checks passed with zero failures, errors or skips. The caller owns
   toolbar/key wiring and the player-path assertions.
3. `2026-10-13-r14-setup-screen` used 300.285, 170.005 and 129.915
   seconds (600.205 total, including cleanup). The first attempt repeatedly
   rediscovered caller-provided APIs and timed out. The second supplied the
   screen but used incorrect localization calls, a component cast and incomplete
   validation. The final exact-replacement repair timed out. After both repairs
   and the cumulative limit, the caller reused the layout and completed the
   listed fixes. A caller brace-edit error failed compilation and was repaired.
   Five setup journeys and three page-contract checks then passed after changing
   the screenshot helper to avoid unbounded hidden-screen geometry. Image review
   exposed an incomplete physical capture. JavaFX snapshot also re-laid out the
   scene using oversized preferred dimensions. The caller now temporarily pins
   the root preferred size to the live scene dimensions, restoring it in finally.
   All six setup cases passed; Czech and English snapshots were visually checked. This is partial reuse, not autonomous worker completion.
4. `2026-10-13-r17-equipment-preview` used 180.891, 88.509 and 55.874
   seconds (325.274 total). The first result reversed the tools-return conditions
   and returned a stock-based amount when unequipping. The first three-line
   repair retained those conditions and invented a field; the final repair
   corrected all three lines. Caller integration added preflight to all six
   actions and automatic unequipping, plus validation of work destinations before
   mutation. Caller tests initially treated `GoodsType` as an enum and mocked
   final `ColonyField`; both setup errors were corrected without weakening the
   expectations. All 696 isolated game tests then passed, including 13 new
   equipment checks. The fourteenth invalid-production regression also passed in the
   integrated run.
5. `2026-10-13-r17-equipment-dialog` used 197.630, 228.018 and 47.795
   seconds (473.443 total). The first class invented localization/model APIs and
   missed changed-cost confirmation. A focused repair supplied the correct class.
   UI verification then found retained Cancel focus, so the second repair set
   focus on the appropriate button when opening. All five TC25 cases passed,
   including Czech Enter confirmation. Caller setup corrections moved the F17
   ship into its colony and fixed keyboard menu indexing; these were not worker
   defects. Fourteen equipment model checks also passed. The separate caller-owned setup screenshot issue was subsequently fixed and
   verified without changing production layout.
6. `2026-10-13-r17-unload-refresh` used 160.483, 103.596 and 300.654
   seconds (564.733 total). The first two methods broke initial transfer behavior,
   reused stale cargo quantities or omitted goods-type validation; neither was
   applied. The final focused repair ignored the text-only scope, repeatedly
   searched already supplied source and timed out without a completed method.
   Both repair rounds were exhausted. The caller completed the small loop in
   the isolated tree, retaining one warning per rejected confirmation and an
   explicit fresh quantity confirmation. All 18 TC19 scenarios then passed, including refresh/cancel/reconfirmation and
   abandoned-colony recovery; the complete integration regression also passed.
7. Caller integration found a retained map animation after test-window closure:
   the manually started application was never stopped. A small application/
   presenter shutdown hook and fixture `AfterEach` cleanup fixed the lifecycle;
   the same targeted run then passed all 32 checks without increasing memory.
   Full regression also exposed an old identity test assuming the bundled world
   had no colonies. Its preparation now explicitly removes scenario opponents
   and colonies; original first-ID-zero/exhaustion assertions remain intact.
   Later native-dialog diagnostics showed a reopened window physically at
   (300,317) while JavaFX reported (0,0), explaining missed clicks in TC05/06/25.
   The shared dialog now centers after its native window is shown; offscreen
   show/hide priming was removed. Thirteen targeted checks passed after this
   root fix. Speculative test-click changes and diagnostic prints were reverted.
8. These assignments used the configured GPT-OSS alias at context 32768, unloaded
   before verification. Prompts, rejected results, runtime samples and acceptance
   reports remain under `~/Library/Logs/Microcol/local-qwen/jobs/` using the task
   names above. These intermediate results do not establish acceptance of all
   seven requirements or any measured cloud-token saving.
9. Across these eight assignments the recorded worker attempts total
   **3758.071 seconds** (about 62.6 minutes), including loading/cleanup. This is
   local worker runtime, not total task time or a token-saving measurement.


## Default development workflow — 2026-10-11

User and main agent agree player goal, rules, edge cases, cancellation/failure
behavior, and observable acceptance criteria before implementation. Main agent
decomposes larger features into small complete tasks and prefers the configured
local worker for suitable implementation, behavior tests, bug fixes, repetitive refactoring, and
documentation/configuration edits within agreed interfaces. No extra user
approval per suitable task.

Main agent keeps architecture, unresolved game rules, scope, actual test
execution/evidence, review, integration and publication. Directly handle
trivial edits when delegation costs more; explain other skipped/degraded
delegations briefly.

Send compact English task contracts and relevant code only; do not include full
chat history or duplicate implementation in parallel. The worker edits its isolated
worktree and does not delegate, commit or push. One worker at a time; preserve
existing limits and unload before Maven.

### Focused handoffs — 2026-10-12

The [worker skill](../../.agents/skills/local-worker/SKILL.md) and
[task contract](../../.agents/skills/local-worker/references/worker-instructions.md#focused-handoff)
now require exact paths, relevant current code/call sites, explicit acceptance
checks and a clear unchanged scope. Prefer small assignments over broad repository
investigation; do not send unnecessary history or entire files by default.

For simple edits with known source, prefer text-only replacements with exact
match counts. The worker proposes the changes without shell editing; the caller
validates the complete batch against allowed paths and current text before
application, then reviews, formats and tests it. Missing matches or syntax errors
go back with a specific diagnosis and unchanged checks, within the remaining
budget. Direct editing remains available when the task needs it.

This captures the lesson from the
[dock assertion recovery on main](https://github.com/jajir/microcol/blob/92a22efaf9c9b991dbc40f4c681b276af34d8423/docs/ai/local-qwen.md#dock-assertion-move-2026-10-12),
not a new Qwen benchmark or a claim that the earlier warehouse jobs succeeded.
It changes handoff/review instructions, not the runtime, model, time limits or
cloud-fallback policy, and does not install an automatic replacement service.

### Scope and acceptance

Broader features are sequences of bounded jobs, not proof that the current
worker can own whole features. Expand assignments from accepted evidence.
UI changes require the caller verification plan in the
[functional test contract](../functional-tests.md). The dated plowing task below
records local execution and its environment limits; the complete suite is a
required Linux/Xvfb publication check.

For each result report what the worker did, main-agent repairs, actual checks and
pending checks. Record attempts/time/available usage; no measured savings
percentage yet.

Every failed or incomplete attempt follows the mandatory
[failure and recovery loop](#failure-and-recovery-loop). Simple repairable errors
go back to the worker while budgets remain; taking over the implementation is not
the default response to the first failure.

If the runtime is unavailable, report the limitation and continue directly when
useful; do not install anything or silently switch to a cloud worker. The main
agent completes accepted tasks through the authorized `main` publication
workflow. This is a delegation policy, not an unattended background service.

## Host and repository snapshot

These observations describe the executor inspected during preparation on
2026-10-10, not every possible Codex session. Installation uses the same Mac mini
and checkout. Recheck the snapshot when changing host or checkout; the current
runtime and service state are recorded separately under installation verification.

| Item | Preparation snapshot on 2026-10-10 |
| --- | --- |
| Shell and filesystem | Commands execute directly on macOS, with checkout `/Users/jan/projects/microcol`; not in a cloud Linux container. |
| Hardware | `system_profiler SPHardwareDataType`: **Mac mini**, `Mac16,10`, Apple M4, 10 CPU cores, 16 GB memory. `sysctl hw.memsize`: 17,179,869,184 bytes (16 GiB). |
| OS and architecture | `sw_vers`: macOS 27.0, build 26A428; `uname -srm`: Darwin 27.0.0 arm64. |
| Git baseline | `origin` is `https://github.com/jajir/microcol.git`; branch `validation`, HEAD `2413a296535868d34c099523b7a26714f1fe8db1`. Working tree was clean before these documentation changes. |
| Existing instructions | No `AGENTS.md`, `AGENTS.override.md` or project skills found in the checkout before this change. No ancestor instructions or `~/.codex/AGENTS{,.override}.md` found. Existing Markdown documentation is in English. |
| Codex CLI | `/opt/homebrew/bin/codex`, `codex-cli 0.142.3`. This identifies the standalone CLI, not the desktop app's bundled runtime. |
| Main model configuration | The inspected user CLI config records `model = "gpt-6-astra"` and `model_reasoning_effort = "ultra"`. No model/provider/reasoning settings were changed. This config read does not independently establish effective settings in every app thread. |
| Ollama before installation | Not on `PATH`; no application in `/Applications/Ollama.app` or `~/Applications/Ollama.app`, no `~/.ollama`, no binary at the checked Homebrew or `/usr/local/bin` locations, and no listener found on TCP 11434. Historical absence, superseded by the installation and direct CLI setup below. |
| Java in shell | `java -version`: Homebrew OpenJDK 21.0.10, reached through a jenv shim. |
| Maven and its JVM | `mvn --version`: Maven 3.9.12 using Homebrew OpenJDK **25.0.2**. The shell Java version alone does not identify Maven's JVM. |
| Available JDK 11 | `/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home/bin/java -version`: OpenJDK 11.0.30. |
| Memory observation | One `sysctl vm.swapusage` snapshot showed 374.81 MiB swap used. No Qwen was running; this is not its memory cost or a capacity benchmark. Free memory, Memory Pressure and swap under combined Qwen/Java load remain unmeasured. |

The executor therefore has direct access to a Mac mini matching the requested
hardware and to its local checkout. Astra's inference runs in the cloud; command
execution and files are on this Mac. The installed Ollama service runs here;
the worker process and caller-run Maven/TestFX checks use this Mac too.
No cross-machine file transfer or network bridge is needed for this arrangement.
A `localhost:11434` request must originate on this Mac. If a later task executes
elsewhere, establish the specific connection, checkout synchronization and test host before using it;
installing Qwen in that other environment would not satisfy this target.

## Installation verification — 2026-10-10

The authorized installation uses the native [Homebrew Ollama formula](https://formulae.brew.sh/formula/ollama),
not an application bundle or Docker. The following command completed with exit 0:

```sh
HOMEBREW_NO_AUTO_UPDATE=1 HOMEBREW_NO_INSTALL_CLEANUP=1 brew install --formula ollama
```

| Check | Observed result |
| --- | --- |
| Runtime | Ollama **0.40.2**, matching both the client version and `GET /api/version`. |
| Service | `brew services run ollama` completed with exit 0. Homebrew reported `Running=true`, `Loaded=true`, `Schedulable=false`; no login autostart was registered. |
| Local endpoint | Listener bound only to `127.0.0.1:11434`; API base `http://127.0.0.1:11434`. |
| Server and model storage | Server log `/opt/homebrew/var/log/ollama.log`; model storage `/Users/jan/.ollama/models`. |
| Acceleration | Actual inference used the `llamacpp` runner and Apple M4 Metal GPU. `ollama ps` reported `100% GPU`, context 8192. |
| Service environment | Formula sets `OLLAMA_FLASH_ATTENTION=1` and `OLLAMA_KV_CACHE_TYPE=q8_0`. The latter controls the KV cache, **not** the model's Q4_K_M weight quantization. |
| Logged defaults | One parallel request (`NUM_PARALLEL=1`), context 4096, keep-alive 5 minutes, automatic maximum loaded-model count. These server defaults are distinct from the worker alias profile below. |
| Exact model download | `ollama pull qwen3.5:9b-q4_K_M` completed with exit 0 after SHA-256 verification. Local metadata reports `qwen35`, `9.0B`, `Q4_K_M`, package size **6,550,825,550 bytes**. |
| Model identity | Local digest `56671c2ab9385f9cfcb404638e32cd62d88e3501d44822208363c010179a3c90`, confirmed by `/api/tags` and the loaded-model `/api/ps` response. `ollama show` reports minimum runtime 0.30.0. |
| Model checks and measurements | During installation, three direct API requests completed with per-request context 8192. The later CLI pilot uses the separate alias documented below. |
| Loaded state at the end of installation checks | The final request used `keep_alive: 0`; `/api/ps` then returned an empty model list. The installed weights remain on disk and the service remains available. |
| Agent status at installation | No agent integration was configured at that point. Direct `codex exec` was subsequently selected; its current pilot is recorded below. |

Homebrew also installed `openssl@4` 4.0.3, `mlx` 0.32.3 and `mlx-c` 0.7.0,
and upgraded `ca-certificates` to 2026-09-25 and `python@3.14` to 3.14.8_2.
The install log records unlinking the old `openssl@3` command symlinks; the old
package was not removed. These are effects of dependency resolution for this
installation, not changes to Microcol's Java dependencies.

Installation evidence is stored outside the repository at
`/Users/jan/Library/Logs/Microcol/local-qwen/2026-10-10-install/`:
`brew-install.log`, `ollama-pull.log`, `installed-environment.json`,
`model-show.json` and `model-tags.json` record installation and identity.
The request/response JSON, `smoke-summary.json`, `czech-thinking-summary.json`,
`java-verification.json`, `memory-*.json*` and `unloaded-models.json` record actual
checks. `smoke-check.py`, `QwenSmoke.java` and `QwenSmokeCheck.java` retain the
initial check script, model-produced source and the caller's verification harness.

### Direct inference checks and measured limits

The caller sent three sequential `POST /api/chat` requests to the loopback API,
with `stream: false` and `options.num_ctx: 8192`. The first two used
`think: false`, output limits of 128/512 tokens and `keep_alive: "5m"`. The third
used `think: true`, a 768-token output limit and `keep_alive: 0` to unload.
Sampling values were left at the tag defaults. `/api/show` exposes thinking
values `false` and `true`, default `true`. These are Ollama controls, separate
from Astra's reasoning setting. See [Chat API](https://docs.ollama.com/api/chat)
and [model details](https://docs.ollama.com/api-reference/show-model-details).

| Check | Result | Load / input processing / generation | Generated tokens / rate | Client elapsed |
| --- | --- | --- | --- | --- |
| Czech, non-thinking | **Incorrect:** asked how much remains from 100 after buying three items at 12; answered 36 instead of 64 and used malformed Czech. | 6.371 s / 0.721 s / 1.587 s | 29 / 18.28 tokens/s | 8.691 s |
| Java 11, non-thinking | Produced `unitsAffordable(gold, unitPrice, capacity)` with the requested validation and cap. Caller inspected the source, compiled it on JDK 11 and passed **13 checks**: 9 results, 4 invalid inputs. Compilation and execution both exited 0. | 0.004 s / 0.620 s / 6.966 s | 127 / 18.23 tokens/s | 7.602 s |
| Same Czech question, thinking | **Correct:** returned 64. This is a second attempt on the same question, not independent evidence of broad Czech reliability. | 0.005 s / 0.095 s / 33.461 s | 561 / 16.77 tokens/s | 33.597 s |

All three API responses ended with `done: true`, `done_reason: "stop"`; that
only establishes completed generation, not answer correctness. Input counts were
59, 98 and 57 tokens; the third response reports 55 cached input tokens. Rates
are `eval_count / eval_duration` after converting nanoseconds to seconds; the
thinking run includes reasoning work rather than only the final sentence.
The cold load and cached/warm requests are not comparable load-time benchmarks.

Before the first inference, swap used was **374.81 MiB** and
`memory_pressure -Q` reported 49% system-wide memory free. During the first two
requests, two-second samples reached **3,487.19 MiB swap used** (about 3.04 GiB
more), a minimum reported free percentage of 19%, and kernel memory-pressure
level 2 versus baseline 1. One loaded-runner RSS snapshot was 6,442,288 KiB
(about 6.14 GiB). `/api/ps` reported **5,642,083,368 bytes** for both model size
and GPU allocation; this is not the total memory cost of the machine or process.

After unloading, `/api/ps` was empty, pressure level returned to 1 and the reported
free percentage rose to 68%; swap was still 3,479.19 MiB. This short observation
does not establish long-term memory behavior. No Maven/TestFX workload ran
alongside inference; the tiny Java compilation happened **after unloading**.
The current desktop workload plus this model at 8k therefore caused significant
swapping even without game tests. The later pilot therefore separates generation
and Maven execution; continue measuring the intended workload. Do not promote
16k or concurrent heavy tests on the basis of these checks.

These checks establish a usable local inference installation with the stated
quality and memory limits. Qwen only returned text: the caller wrote and tested
the isolated Java snippet outside the checkout. No game files were changed by
Qwen during these installation checks; they are not the delegated-worker acceptance chain.

### Historical Qwen worker profile — 2026-10-10 to 2026-10-13

The earlier tasks used `qwen3.5:9b-q4_K_M`, project alias
`microcol-qwen:9b-q4_K_M-8k`, digest
`dfe6877b76ae37d766653b62909426fc769288fc42575cb5069c2b9b366a79a2`.
Its weight blob was `sha256-02d45dc1cf451ba2475ac33b301c2dd8f985abe4c182ce04a1f2f5bf0260278d`.
The alias allocated context 8192 and generation limit 2048; Codex requested
reasoning `none`. References to the Qwen alias/digest in dated evidence below
mean this historical profile, not the replacement GPT-OSS profile. Archived
job commands remain the source for each past invocation and its actual limits.

### Operator commands

Ollama was started with `brew services start ollama` at the user's request on
2026-10-13; this also registers login autostart. This migration keeps the existing
service configuration. `brew services stop ollama` stops it;
`brew services run ollama` runs it without login registration. See the
[Homebrew manual](https://docs.brew.sh/Manpage#services-subcommand).

Inspect the installed worker using `ollama list`, `ollama show
microcol-gpt-oss:20b-32k`, `ollama --version` and `ollama ps`.
`ollama stop microcol-gpt-oss:20b-32k` unloads it before Maven while retaining the
server. The old Qwen weights/alias and the initial GPT-OSS 8k alias remain
installed for explicit comparison; do not select them automatically or load
multiple models concurrently.

## Selected direct CLI workflow

Use the installed Homebrew Codex CLI through the repository's small
[launcher](../../scripts/local-worker/worker.py). It replaces copied per-job scripts;
there is no background service, new MCP integration or persistent user Codex
configuration.

### GPT-OSS installation and worker verification — 2026-10-13

The user requested this replacement in [R-0034](../requirements/done/R-0034.md).
This initial installation test used the 8k profile; the later 32k verification
below supersedes its context and alias for new assignments.
`ollama pull gpt-oss:20b` completed with exit 0 and SHA-256 verification. The
local package is **13,793,441,566 bytes**, reports **20.9B / MXFP4** and uses the
`llamacpp` runner. The base digest is
`f38aa0c53da5f8c49d08c43a99df24ff53167fe68e24664a7777288e7656fdfe`;
the initial `microcol-gpt-oss:20b-8k` alias digest is
`855c7ef433e4a7ed323d53dba8c5ce5e3a57ab5398e3844af84a02047ef124a7`.
`/api/show` confirms `num_ctx 8192`, `num_predict 4096`, tool support and
thinking levels `low`, `medium`, `high` with default `medium`. Creating the
alias reused the same weights.

The real Codex CLI smoke task requested four exact instruction replacements
in the worker skill and task contract. The caller retained runtime setup and
operating documentation, validated the whole returned batch before applying it,
and did not repair the accepted replacement text manually.

| Attempt | Seconds | Caller verification |
| --- | --- | --- |
| 0 | 70.987 | Valid JSON with all four entries, but three new strings omitted the required `the`; the exact-diff check rejected the batch before any application. The initial instruction used an unquoted replacement phrase; the repair supplied all four complete new strings explicitly. |
| 1 | 74.318 | Accepted all four exact replacements in the two allowed files; no worker shell commands, file edits or extra changes. The caller reran the failed validation and applied the verified batch. |

Total worker time was **145.305 seconds**, within the unchanged task/repair
limits. Both processes exited 0 and unloaded the model successfully; exit status
alone did not determine acceptance. Reported input/output tokens were 2684/757
and 2885/812, with no cached input. The CLI reported zero separate reasoning
tokens; this does not establish that reasoning was disabled or expose its actual
token split. Codex emitted nonfatal unknown-model metadata and alias-telemetry
warnings; completion and acceptance are verified despite those warnings.

All 105 loaded-model samples matched the alias digest above and context 8192.
`ollama ps` reported **18% CPU / 82% GPU**; `/api/ps` reported allocation
13,968,500,323 bytes, including 11,450,754,006 bytes of VRAM. Whole-host swap
was already 6850.94 MiB before the first attempt, peaked at 13364.69 MiB during it
(an increase of about **6.36 GiB**), and reached 13574.62 MiB during the repair.
Both attempts briefly reached memory-pressure level 4 (critical). These are
whole-host observations, not model-only memory measurements. The final
`/api/ps` contained no loaded models. This host is under significant memory
pressure with GPT-OSS even at 8k: keep inference separate from heavy tests and
do not increase context on the strength of this smoke result.

The check establishes the actual local handoff and repair path. It does not
compare Java coding quality with Qwen or demonstrate reliable larger tasks.
After integrating with the current `main`, the caller passed all 9 documentation
tests, the strict MkDocs build, worker skill validation and `git diff --check`.
No Java or player behavior changed; local Maven/TestFX was not rerun for this
configuration and instruction change.

Download, setup, model metadata and subsequent worker evidence are retained in
`~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-gpt-oss-switch/`.
The previous Qwen task was still verifying Docker UI tests during setup; no
inference was started while its shared worker lock was held.

### 32k context verification — 2026-10-13

After the initial installation, the user requested context **32768** because
previous assignments exhausted 8k. The new alias `microcol-gpt-oss:20b-32k`
reuses the installed weights; its digest is
`425bbbff577bec88a59f86c437ff81eb369e011cfcd047121431a05951076fef`.
Both Ollama `num_ctx` and Codex `model_context_window` are 32768. Generation
allowance 4096, medium reasoning and the existing time/repair limits are unchanged.
The caller made this small runtime/configuration change directly and used the
actual local worker for the capacity check.

The tool-free Codex request contained five unchanged related Java page objects
(39,814 source characters) and requested three exact JSON answers from their
beginning, middle and end. The caller checked source hashes, all answers and
that the worker made no edits. One attempt passed in **119.541 seconds** with
**11162 input / 849 output tokens**, zero cached input, no reported compaction
or truncation, exit 0 and successful unload. All 100 loaded-model samples matched
the new digest and **context 32768**. This verifies a request larger than the
old 8192-token window, not the full 32k occupancy or general coding quality.

`ollama ps` reported **21% CPU / 79% GPU**. Runtime allocation was
14,070,306,565 bytes, including 11,126,869,850 bytes of VRAM. Whole-host swap
started at 9822.25 MiB and peaked at 12965.69 MiB; memory-pressure levels included
1, 2 and **4 (critical)**. After unload, no model remained loaded and a
snapshot returned pressure level 1. These observations are not a controlled
comparison with the earlier 8k run; other desktop memory use differs.

The 32k profile is now the default. Continue one worker at a time and unload
before heavy tests. A larger context raises the available window but does not fix
incorrect output or guarantee that long tasks finish inside the attempt budget.
The fallback-metadata and alias-telemetry warnings remain the same known CLI
limitation as in the initial trial. Evidence, source hashes, task, response,
validation and runtime samples are under
`~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-gpt-oss-32k/`.

Caller verification passed all **9 documentation tests**, a strict MkDocs build,
the rendered-page review and `git diff --check`. No Java or UI behavior changed;
local Maven/TestFX was not rerun for this configuration-only follow-up.

### Operating profile — bounded assignments

Updated on **2026-10-14** under [R-0039](../requirements/done/R-0039.md).
The existing
[Modelfile](../../.codex/local-worker/Modelfile) owns context/output values;
[profile.json](../../.codex/local-worker/profile.json) owns the remaining settings.
The launcher reads both, checks the installed alias, and snapshots their effective
values per attempt. Only the diagnosed medium-to-high transition described above
is allowed during a task; retries retain that task's original budget. Historical
jobs retain their original evidence and need their original frozen profile to resume.

| Setting | Current configured value |
| --- | --- |
| Runtime | Existing Ollama 0.40.2 and Codex CLI 0.142.3, local loopback only. |
| Model | `gpt-oss:20b`, alias `microcol-gpt-oss:20b-96k-output64k`; no cloud fallback. |
| Context / generation | `num_ctx 98304`, `num_predict 65536`; generation includes reasoning and final output. Input, tools and generated text share the context. |
| Reasoning | `medium`; installed GPT-OSS supports `low`, `medium`, `high`. This is a request setting, not a different set of weights. |
| Limits | 7200 seconds per attempt, 14400 cumulative seconds per task, at most two repairs across both reasoning levels. |
| Text-only mode | `features.shell_tool=false`, read-only sandbox; browser/computer/image generation, plugins, apps and delegation disabled. |
| Direct-edit mode | Command execution and workspace writes permitted in the isolated worktree, within the supplied scope. |
| Concurrency | One local worker; host-wide lock retained through caller verification. Unload before Maven/Docker. |

To change model or context/output limits deliberately, update these configuration
files and recreate the alias from the existing weights while holding the worker
lock. A reasoning-only change is passed per request and needs no alias rebuild:

```sh
ollama create microcol-gpt-oss:20b-96k-output64k -f .codex/local-worker/Modelfile
```

Changing the file alone does not update Ollama. The launcher rejects a mismatch
between the installed alias's context/output parameters and repository settings.
The model alias is configurable in the profile; model changes require an explicit
operator decision and verification. No launch installs or downloads a model.

The alias now has digest
`aa2f84a12eb2606aeb426045b0d256cff9ffe8f92b8ee6f11b5499a08c0a174f`.
The `b1630d...` digest identifies the historical 32k-context / 16k-output alias;
`425bbb...` identifies the older 4096-output profile.
The configured change is verified by `/api/show`; coding results are recorded
in the dated evidence, not inferred from configuration.

The historical [high profile](../../.codex/local-worker/gpt-oss-high/profile.json) and its
[Modelfile](../../.codex/local-worker/gpt-oss-high/Modelfile) preserve the exact
tested 16k-output settings with the older 32k alias. Select it only for
an explicit experiment; these runs provide no evidence that an unchanged retry
would remedy the observed generation-limit failures:

```sh
python3 scripts/local-worker/worker.py run "$worker_job" --profile .codex/local-worker/gpt-oss-high
```

### Explicit GPT-OSS high with 64k output

The larger-budget profile authorized under [R-0038](../requirements/done/R-0038.md)
on **2026-10-14** uses the same installed GPT-OSS 20B MXFP4 weights. Its
[Modelfile](../../.codex/local-worker/gpt-oss-high-64k/Modelfile) configures
**98304 context / 65536 generated tokens**; its
[profile](../../.codex/local-worker/gpt-oss-high-64k/profile.json) requests **high**,
**7200 seconds per attempt / 14400 cumulative seconds / at most two repairs**.
Input and generation share context, so a 65536-token context would not leave
room for a full 65536-token generation. Installed metadata and the
[official model documentation](https://developers.openai.com/api/docs/models/gpt-oss-20b)
report a 131072-token context limit; this does not guarantee sufficient host memory.

The separate alias is `microcol-gpt-oss:20b-96k-output64k`, digest
`aa2f84a12eb2606aeb426045b0d256cff9ffe8f92b8ee6f11b5499a08c0a174f`.
Its installed parameters match the files. Alias creation reuses existing weights;
service settings remain unchanged. R-0039 also selects this alias for the default
medium profile; reasoning is the only difference between the current two profiles.

```sh
# Hold the existing worker lock while recreating an explicitly changed alias.
ollama create microcol-gpt-oss:20b-96k-output64k -f .codex/local-worker/gpt-oss-high-64k/Modelfile
python3 scripts/local-worker/worker.py run "$worker_job" --profile .codex/local-worker/gpt-oss-high-64k
```

### Explicit Qwen2.5-Coder comparison profile

The authorized installation on **2026-10-14** uses exactly
`qwen2.5-coder:14b-instruct-q4_K_M`: local metadata reports 14.8B parameters and
Q4_K_M quantization. The separate
[Modelfile](../../.codex/local-worker/qwen2.5-coder-14b/Modelfile) and
[profile](../../.codex/local-worker/qwen2.5-coder-14b/profile.json) select alias
`microcol-qwen2.5-coder:14b-q4_K_M-32k`, context **32768**, output **16384**, and
the historical 1800/3600-second limits and two repairs. Its installed digest is
`9ea50088a1236ab5b3d379b1b266636db45a63eb7db2108becb47c394f8f8fa0`.
The model does not expose thinking capability; `reasoning: none` and disabled
reasoning summaries reflect that capability. There is no Qwen `high` level.

```sh
# Installation was explicitly authorized for this comparison; runs never install.
ollama pull qwen2.5-coder:14b-instruct-q4_K_M
ollama create microcol-qwen2.5-coder:14b-q4_K_M-32k -f .codex/local-worker/qwen2.5-coder-14b/Modelfile
python3 scripts/local-worker/worker.py run "$worker_job" --profile .codex/local-worker/qwen2.5-coder-14b
```

Pass the same `--profile` for every Qwen repair. The launcher checks actual model
capability before starting and rejects unsupported reasoning or unrelated profile
changes. Only GPT-OSS medium-to-high escalation with otherwise identical settings
is part of the ordinary policy. Apply/release commands are unchanged; there is no
unattended fallback or change to the main agent's model.

### Time budget — 2026-10-14

1. The launcher derives the next attempt number and elapsed time from all prior
   `attempt-*/execution.json` records. Missing/nonconsecutive evidence, a changed
   profile other than a recorded medium-to-high transition, exhausted time or
   exhausted repairs stops dispatch. R-0039 raises new-job limits to 7200/14400
   seconds; historical jobs preserve their original limits.
2. Each attempt gets `min(attempt_seconds, task_seconds - elapsed_seconds)`.
   Startup, failed execution, cancellation and cleanup count. For example, 14300
   seconds already used leaves 100 seconds, not a new two-hour attempt. Escalation
   does not renew time or the maximum two repairs.
3. On deadline, interruption or runtime identity failure, the launcher terminates
   its owned process group, escalates after five seconds if needed, unloads the
   model and records the result. Repeated discovery without progress can still
   justify cancellation before the deadline; the larger allowance is not a
   reason to repeat an unchanged failing investigation.
4. Caller preparation, review and tests remain outside the inference budget.
   A new directory is not permission to reset an unfinished logical task.

### Time budget — 2026-10-12

The historical limit was 300 seconds per attempt and 600 seconds per task, with
at most two repairs. The user superseded those time limits on 2026-10-14; use the
[current budget](#time-budget-2026-10-14). Archived runners are evidence, not
current invocation templates.

### Failure and recovery loop

**Required from 2026-10-12.** Previously the workflow allowed bounded repairs,
but did not require a diagnosed retry before main-agent completion. Recording a
failure without checking a feasible correction does not meet this contract.

1. **Inspect and classify.** After a failed command, incomplete/invalid result,
   timeout or caller acceptance failure, preserve the task, runtime identity,
   exact command/error, exit status, current diff and elapsed budget. Determine
   whether the cause is caller setup, a tool/script/code error, missing context,
   resource limits or a failed requirement. State unknown causes as unknown;
   a timeout alone does not establish that more time is the answer.
2. **Make a specific correction.** The caller fixes its own setup, such as a
   missing child `CODEX_HOME`, without installing a runtime or changing models.
   For a simple in-scope worker error, give the worker the relevant failing fragment,
   traceback/current diff and a focused repair task. Keep required behavior and
   checks unchanged. Do not dump the full previous conversation or repeat an
   unchanged failing prompt. Do not silently replace the worker's implementation.
3. **Rerun within the same budget.** Dispatch the corrected setup or repair with
   the same logical task ID, remaining cumulative runtime and repair count.
   The caller must attempt a feasible, safe repair while those limits permit it;
   no extra user approval is needed. A new directory is not a new allowance.
   If the runtime is unavailable, scope is unclear, retry would be unsafe, or
   budgets are exhausted, record that concrete reason rather than retrying
   indefinitely. Main-agent takeover must explain why another worker repair
   was not appropriate.
   For an eligible medium failure, follow the diagnosed high/64k transition above
   before exhausting the shared budget; keep the same job and verification contract.
4. **Verify the original failure and the result.** After unloading the local model, the
   caller reruns the failed command/check against the repaired state and then
   the assigned acceptance checks, including formatting for Java changes.
   A corrected variable name, successful CLI exit or passing syntax check alone
   does not establish a working feature. Preserve actual before/after results,
   intended test selection, counts and any remaining failures.
5. **Close the loop visibly.** The job's verification/acceptance record must say
   what failed, why, who corrected it, which retry/check ran, and whether recovery
   is verified, partial or unresolved. Record skipped-retry/takeover reasons and
   reusable prevention in its existing owning instructions or tool. Summarize
   relevant measured outcomes here; do not present caller-completed work as an
   independently successful worker task.

This is enforced by the main agent's dispatch and acceptance procedure through
`AGENTS.md`, the worker skill and task/result contract. It is **not** an unattended
retry service or a guarantee of model correctness. The evidence and explicit
dispositions make compliance reviewable; they do not replace actual verification.

### Prepare, invoke, verify

For agent-driven attempts, dispatch the existing launcher through the
[completion runner](completion-callbacks.md#worker). It reports completion to the
originating task without repeated cloud-model status checks. The steps below,
including actual acceptance and lock release, remain mandatory.

1. Prepare a detached worktree at the agreed `main` revision. For a historical
   replay, use its recorded historical main revision and explicitly copy the
   original acceptance inputs. Never give the worker the main checkout.
2. Create a unique job directory in
   `~/Library/Logs/Microcol/local-qwen/jobs/<task-id>/` containing `task.md`,
   `worker-role.md` and `job.json`. Copy the current role from the skill; a
   historical replay records use of its original role explicitly. Supply the
   applicable rules, exact source/APIs, unchanged acceptance checks and caller
   formatting/test ownership in the task. Verify APIs against the checkout before
   passing them; source-anchor validation cannot establish Java type correctness.
3. Declare every allowed source path and exact anchor as below. Paths are
   relative to the worktree; escaping paths, symlinks, missing sources, duplicate
   IDs and incorrect counts fail preflight. For new files, prepare an explicit
   skeleton first. Record copied/uncommitted inputs; the launcher captures the
   Git diff/status and allowed file hashes at dispatch.
4. Run the launcher. It creates the child-specific Codex home, claims the existing
   host-wide lock, validates the alias and records the prompt, role, command,
   effective profile, runtime identity samples and process outcome. An existing
   lock owned by another job or an active invocation prevents dispatch. Do not
   remove another caller's lock. A crash leaving incomplete evidence or a running
   marker requires diagnosis before retrying.
5. Review the returned code. For text-only output, run the separate `apply`
   action; it accepts the complete expected set of IDs, decodes JSON normally,
   checks original file hashes and validates every exact match before writing.
   One outer `json` Markdown fence may be removed and recorded in
   `application.json`; surrounding prose or multiple blocks remain invalid.
   Unknown, duplicate, missing or unchanged replacements fail. Original-prompt
   replays may return the historical full `file`/`old`/`new`/`expected_matches`
   objects only when they identify the same declared anchors exactly.
6. **Inspect the application exit status before formatting or tests.** Formatting
   is not evidence that a rejected patch applied. Use the shared formatter on
   changed Java files, review the diff and run the assigned acceptance after model
   unload. A nonempty response and exit zero do not establish acceptance.
7. For a repair, retain the job directory/history, update only the focused task
   and caller-owned anchors to the current reviewed source, and rerun within the
   remaining budget. Preserve correct fragments; do not ask for the whole file
   again when one fragment is wrong. Explain caller corrections and failure causes.
8. Integrate accepted changes only after checking current main state, then
   release this job's lock. The caller owns commit, push and publication. Replays
   retain their candidate only in the isolated tree; they do not rewrite gameplay.

Example `job.json` (replace the illustrative path, full revision and source):

```json
{
  "worktree": "/absolute/path/to/detached/worktree",
  "baseline": "full-git-commit-id",
  "mode": "text-only",
  "allowed_files": ["path/to/Example.java"],
  "anchors": [{
    "id": "method",
    "file": "path/to/Example.java",
    "old": "exact original source",
    "expected_matches": 1
  }]
}
```

The prompt supplies the source/context and requests only
`{"replacements":[{"id":"method","new":"replacement source"}]}`. It need not
repeat the original text, path or match count in its answer. Direct-edit jobs
select `"mode":"edit"`; anchors may be empty, while `allowed_files` and caller
review still define scope. The workspace sandbox is broader than that allowlist;
review the entire diff before accepting direct edits.

From the main checkout, after assigning the absolute `worker_job` path:

```sh
python3 scripts/local-worker/worker.py run "$worker_job"
# Review the candidate, then apply the explicitly selected attempt:
python3 scripts/local-worker/worker.py apply "$worker_job" --attempt 0
# Only after successful application: formatter, diff review, caller acceptance.
# After verification/integration (or documented rejection):
python3 scripts/local-worker/worker.py release "$worker_job"
```

These are separate operator steps, not an unconditional shell sequence. If an
application fails, stop the dependent steps and diagnose it. The launcher does
not run acceptance tests, auto-accept code, schedule retries, commit or publish.
Its output metadata says verification is pending caller work.

Portable launcher checks run without inference or the host lock:

```sh
python3 -m unittest discover -s scripts/local-worker -p 'test_*.py'
```

The documentation CI runs these tests alongside the existing documentation checks.
Actual coding acceptance requires the local model and the assigned Java checks.

### Failure analysis — 2026-10-12

This is a read-only review of **14 recorded attempts** from 10–11 October, not
14 independent tasks or a controlled model benchmark. It includes retries and
one startup failure. Sources are each job's `events.jsonl`, execution metadata,
`stderr.log`, and caller verification/acceptance records under
`~/Library/Logs/Microcol/local-qwen/jobs/`. The derived attempt inventory is in
`~/Library/Logs/Microcol/local-qwen/analysis/2026-10-12-timeouts/summary.json`.

| Recorded outcome | Attempts | Interpretation |
| --- | --- | --- |
| Completed model turn, process exit 0 | 5 | Goods-trade retry, build identity, plowing assertions, Linux assembly and launcher metadata. Four needed caller corrections or only partial acceptance; completion is not correctness. |
| Stopped at the old deadline | 7 | Documentation links, workflow policy, implementation-plan handoff, package descriptions, warehouse model, its repair and the warehouse fixture retry. Some produced reusable partial work. |
| Caller stopped before the deadline | 1 | Initial goods-trade pilot: context compaction and no edits after 145.73 seconds. |
| Failed before model execution | 1 | Warehouse fixture: missing child `CODEX_HOME`, exit 1 after 3.02 seconds. This was caller setup failure, not a Qwen generation failure. |

Observed causes overlap; a timeout is a stopping reason, not by itself a root
cause. The strongest evidence is:

| Finding | Evidence and interpretation |
| --- | --- |
| Reading/context churn | The warehouse-model attempt read the same 7,375-character Java file three times, made no edit and reported a compaction warning. Four attempts reported compaction warnings. The workflow-policy attempt read the whole growing operating document. This supports narrowing inputs, not simply waiting longer. |
| Oversized output and guessed data shape | The fixture retry printed 103,428 characters from the saved game, then treated colony data as a unit and failed with `TypeError: 'NoneType' object is not subscriptable`. Raw trace size is not proof that all those characters reached the model: tool output was capped. It does show an unhelpfully broad read followed by an incorrect schema assumption. |
| Failed editing mechanics | The warehouse repair reached an edit script, but `old_addgoods` was undefined and Python raised `NameError`. The documentation-link job used unquoted `Application Support` paths, spent calls diagnosing the wrong directories and left only an empty helper. These are concrete command/edit errors, not evidence that all work only needed more generation time. |
| Deadline/task size mismatch | The package-documentation attempt issued 16 commands and drafted six of eight files before cancellation at 180.25 seconds. Build identity completed at 180.48 seconds in the caller's recorded timing. More time plausibly helps such progressing attempts; the logs cannot establish that all timed-out work would finish correctly at 300 seconds. |
| Output quality still needs review | Even completed runs needed fixes: helper visibility/resource handling in build identity, XML structure/output paths in Linux assembly, and replacement of the launcher's Linux descriptor. A longer deadline does not remove the acceptance gate. |

The fallback-model-metadata warning appeared in all **13 attempts that started
a model turn**, including all five completed turns. It is a compatibility caveat,
not a demonstrated sole cause of failure. Codex documents the context-window and
compaction controls separately in its
[configuration reference](https://learn.chatgpt.com/docs/config-file/config-reference).
Do not disable compaction or increase context solely to hide the warning.
Earlier measurements showed memory pressure, but these logs do not isolate
swapping or model loading as the cause of the individual timeouts. Complete
generation timing and token totals are also unavailable for the interrupted runs.

**Recommended next checks, not yet measured improvements:** preflight the child
home and working directory; use relative worktree paths or quote absolute paths;
provide the exact method/data fragment and acceptance example instead of whole
documents or fixtures; split multi-file batches that leave little editing time.
On a repair, supply the actual error and current diff rather than repeating the
original broad discovery task. That Qwen comparison recommended keeping 8k and
recording useful edits, accepted results, caller repair time and budget consumption
for the next 3–5 assignments under the new limits. The later user-requested
GPT-OSS 32k profile supersedes that context choice for new work. This initial read-only diagnosis
included no live worker run or token-saving claim; the later recovery check is
recorded separately below.

### Recovery check — 2026-10-12

Task `2026-10-12-recovery-check` verified the failure loop on a detached worktree
at `5db07522ccf9dc1a731f276c88bce951a9aa441f`. It used two **controlled
reproductions** of observed failures: a deliberately missing child home and a
seven-line Python function with the same `old_method` / undefined `old_addgoods`
mismatch as the archived warehouse edit script. It did not resume or complete
the original warehouse feature, reset that task's budget, or modify game code.

| Step | Actual result |
| --- | --- |
| Original code check | Caller ran `python3 -m unittest -v test_recovery_probe`: 4 tests, 4 `NameError` errors, exit 1. |
| Missing-home reproduction | Attempt 0 exited 1 before inference because its child `CODEX_HOME` did not exist; 1.062 seconds including cleanup. |
| Caller setup correction | The documented quoted, fail-fast preflight created the child home. The retry retained the same task ID and charged the first attempt's elapsed time. |
| Targeted Qwen retry | Attempt 1 exited 0 in 48.509 seconds including cleanup. Qwen changed only the undefined reference to the already-defined `old_method`; no caller code repair was needed. |
| Runtime verification | Samples matched local alias `microcol-qwen:9b-q4_K_M-8k`, the documented digest and actual context 8192. The model was unloaded before caller checks. |
| Same acceptance check after repair | The identical command passed all 4 tests, with no failures, errors or skips. Caller checked that the test file hash and all tracked repository files were unchanged. |
| Budget and result | Total worker time 49.571 seconds, within the 300/600-second limits. Both reproduced failures have verified recovery; the repaired probe remains isolated. |

The four cases check replacement, unchanged surrounding text, unrelated content
and repeated occurrences. Qwen used three shell commands and left tests pending
caller verification. Its completed-turn usage was 14,013 input tokens (10,262
cached), 397 output tokens and zero reasoning output tokens across requests;
these are not a single prompt's size or measured cloud savings. The caller
prepared the reproduction, diagnosed both faults and ran the checks. This is
evidence of one supervised recovery, not proof of general autonomous reliability
or that a five-minute allowance is better than three minutes.

The documented preflight also passed six caller-run cases: missing/existing child
home, missing worktree, task or role, and a file blocking the home-directory path.
Paths contained spaces; existing home content was preserved. These checks are
recorded in `preflight-checks.json` and do not require model inference.

Task input, updated worker role, `diagnosis.json`, both attempt directories,
original/corrected probe, immutable tests, red/green logs and `verification.json`
are retained in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-12-recovery-check/`.
The isolated checkout remains under the standard worker worktree directory.

### Current Microcol pilot — 2026-10-10

Task `2026-10-10-goods-trade` uses worktree
`/Users/jan/Library/Application Support/Microcol/local-worker/worktrees/2026-10-10-goods-trade`
and artifacts in
`/Users/jan/Library/Logs/Microcol/local-qwen/jobs/2026-10-10-goods-trade/`.
The baseline, exact executed command, task and role are retained in the artifacts.

| Attempt/check | Recorded status |
| --- | --- |
| Baseline `GoodsTradeTest` | Caller verification passed **3 existing cases** before worker edits. See `baseline-maven.log` and `baseline.json`. |
| First worker attempt | Actual 4k context with default thinking. Caller stopped it after **145.73 s** because context compaction occurred before any edit. No changes were produced; this was not a successful implementation. |
| Accepted retry | `retry-8k/command.json` records the historical Qwen invocation. CLI exit **0**, **69.0 s**. Qwen changed only `GoodsTradeTest.java`: six assigned parameter rows and explanatory comments; original assertions and cases remained intact. |
| Caller verification | After unloading the model, Maven on **JDK 11** exited **0**. Fresh Surefire XML reports **9 tests, 0 failures, 0 errors, 0 skipped**, up from the 3-case baseline. |
| Review and delivery | Astra accepted the diff in the isolated worktree. When the user subsequently selected `develop` for ongoing development, the same patch was incorporated into the main checkout on that branch. The original worktree and artifacts retain the pilot evidence. |

`retry-8k/verification.json` records the command result, tests, runtime identity,
measurements and review; the copied `TEST-org.microcol.model.GoodsTradeTest.xml`
contains the nine cases. The model ran at 8192 context with no compaction or
truncation warnings and zero reasoning events. Observed prompt lengths were
2945, 3347 and 4011 tokens. Reported usage sums across requests: **10,303 input
tokens, including 6,284 cached, and 799 output tokens**; it is not a single
10k-token prompt.

Across 34 retry samples, swap remained **3,447.19 MiB** (no measured increase)
and pressure level remained **1**. After unloading, the model list was empty.
This describes one 69-second accepted attempt after the aborted 145.73-second
attempt; neither this retry nor the combined setup is a throughput or
savings benchmark. The earlier installation checks still show that significant
swap growth is possible under a different desktop workload.

### Build identity task — 2026-10-11

The same local alias completed the bounded `ApplicationInfo` build-ID task in
180.48 seconds (exit 0, verified digest and 8192 context). Main-agent review fixed
helper visibility, resource closing and whitespace/placeholder handling before
acceptance. The caller then ran all five new cases on JDK 11 successfully; the
integrated JDK 17 distribution build passed 601 game tests. The accepted files
were brought into `main`. Evidence, the worker's original output and the recorded
repairs are in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-11-build-identity/`.
This task required main-agent corrections and establishes no savings percentage.

### Workflow documentation check — 2026-10-11

A fresh availability check found Codex CLI 0.142.3, Ollama 0.40.2 and the
configured alias on this Mac. Job `2026-10-11-qwen-default-workflow` used the
verified alias digest above and an actual 8192-token context. The caller stopped
its single attempt at the 180-second deadline (181.20 seconds including cleanup,
process exit -15). Qwen produced the requested policy text but appended it at
the wrong location and returned no final report. The main agent accepted the
text only, moved and wrapped it, clarified verification/fallback wording, and
updated the root routing rule and older status/publication wording. There were
no worker repair rounds; this was a partial result requiring caller completion.

Samples recorded swap usage from 4553.75 to 4894.62 MiB and pressure levels 1
and 2 during this desktop workload; these observations do not isolate the model's
memory cost. After cleanup, Ollama reported no loaded models. Complete per-run
usage and loading/generation timing were unavailable. The main agent's final
checks passed six documentation hook tests and a strict MkDocs build; game tests
were not run locally for these documentation-only changes. Logs, the original
diff, runtime samples and acceptance evidence are retained under
`~/Library/Logs/Microcol/local-qwen/jobs/2026-10-11-qwen-default-workflow/`.
This check verifies the handoff path and the need for review, not unattended
reliability or measured Codex-budget savings.

### Implementation-plan handoff attempt — 2026-10-11

Task `2026-10-11-plan-worker-handoff` assigned a bounded Czech documentation
change at baseline `1fb920259987d216d54698188fd66aee1b9df986`: extend the
implementation-plan task template and describe readiness for delegation.
The caller stopped the first attempt at **180.84 seconds**; the owned process
exited **-15**. Qwen produced a temporary draft but made **no worktree changes**
and did not deliver a final report. Runtime samples confirmed the configured
alias digest and **8192** context; the events included a compaction warning.
The model was unloaded after cancellation. No repair attempt was dispatched.

The main agent completed the edit, repaired Czech wording and the draft's
incorrect whole-package blocking statement, and replaced documentation-specific
verification fields with a general caller verification contract. The caller
separately designed the package responsibility matrix. This was an incomplete
worker attempt with main-agent completion, not an independently accepted worker
implementation. Timing includes the complete supervised attempt; token usage
and savings were not established.

Input, command, runtime samples, draft, empty worker patch, caller repairs and
verification evidence are retained in
`~/Library/Logs/Microcol/local-qwen/jobs/2026-10-11-plan-worker-handoff/`.
The detached worktree remains under the standard worktree root with that task ID.

### Earlier native feasibility evidence — 2026-10-10

Before the implementation pilot, CLI 0.142.3 used the base model in a read-only
sandbox with isolated `CODEX_HOME`, `--ignore-user-config` and `--ephemeral`.
It executed `cat probe.txt` and obtained a random token absent from its prompt.
Ollama served two `/v1/responses` requests; loaded-model evidence matched the
base digest and actual context 4096. Exit was 0 after approximately 30 seconds.
The final answer **failed the requested format** by including the tool wrapper.
Reasoning appeared despite an effort-none request; that diagnostic did not
establish thinking-off. Its first invocation rejected `agents.enabled=false`
before inference; the retry used `features.multi_agent=false`.

Artifacts are in
`/Users/jan/Library/Logs/Microcol/local-qwen/2026-10-10-native-probe/`, including
`retry/command.json`, events, summary, samples and server evidence. No project
files were available for editing, and the model was unloaded afterwards.
A separate loader-only check of desktop runtime **0.154.0-alpha.6.2** accepted and
type-checked a custom agent's provider configuration, but did not spawn a model
turn or establish a mixed-provider child. Sanitized evidence is in `agent-loader/`.
That alternative and the earlier MCP design are not required for the selected
CLI route; no custom server, runner package or project MCP registration was added.

## Microcol verification and pilot scope

The inspected [root POM](../../pom.xml) still declares modules `microcol-game`,
`microcol-test`, `microcol-module`, `microcol-dist`, `microcol-site`, Java release
11, JavaFX 13, JUnit 5.5.2 and Surefire 3.0.0-M3. These match the consultation's
reference. This inspection originally used `validation`; the user subsequently
selected `develop` for ongoing development and integrated the existing work there.
No Java, JavaFX or test framework upgrade is part of this preparation.

Use the existing [test commands and historical results](../gap-analysis.md#commands-and-observed-results)
as the primary record: that assessment reported a failed game-module run on JDK
25 with two `ImageCacheTest` toolkit errors and a successful JDK 11 run of 458
model tests. Those are earlier results, not checks repeated during preparation or
the installation. The current pilot separately verifies its focused baseline.
The JDK 11 path is still present here; set the chosen JVM explicitly and verify
it with Maven when establishing the pilot baseline.

The [TestFX POM](../../microcol-test/pom.xml) has TestFX 4.0.16-alpha, Monocle
`jdk-11+26`, and Surefire `groups=${testedTags}`. The default `local` profile sets
`ci, local`; `ci` sets `ci-save` with a comment about disabling all tests. No
`ci-save` annotation was found in the inspected test sources. The historical
[Travis configuration](../../.travis.yml) skips tests during install and uses
that `ci` profile for its site step. A green Maven command is not evidence that
the intended test ran. The [game POM](../../microcol-game/pom.xml) also excludes
`org/microcol/integration/**.java`. Check the selected classes, cases, skipped
counts and fresh Surefire XML in the module's `target/surefire-reports/`.

Existing references checked during preparation:

- [TC_06_F06_buy_and_sell_goods_test](../../microcol-test/src/test/java/org/microcol/test/TC_06_F06_buy_and_sell_goods_test.java) covers a partial sale and buying into a full slot; do not duplicate these cases.
- [EuropePortScreen](../../microcol-test/src/test/java/org/microcol/page/EuropePortScreen.java) has sale and gold-check helpers.
- [SimpleOperationsTest](../../microcol-test/src/test/java/org/microcol/test/SimpleOperationsTest.java) includes language switching and historical headless limitations.
- [CargoSlotTest](../../microcol-game/src/test/java/org/microcol/model/CargoSlotTest.java) already checks selling all goods from a slot, emptying it and calling the player transaction. [PlayerTest](../../microcol-game/src/test/java/org/microcol/model/PlayerTest.java) separately checks sale proceeds. A full-cargo sale is not generally an uncovered model behavior.

**Completed pilot:** added missing money boundaries to
[GoodsTradeTest](../../microcol-game/src/test/java/org/microcol/model/GoodsTradeTest.java).
At a positive buy price of 12, available gold of 0, 11, 12, 1199, 1200 and 1201
should purchase 0, 0, 1, 99, 100 and 100 units respectively, using the current
100-unit slot capacity. The three baseline cases cover 10000, 120 and 131. Expected values follow the
assigned whole-unit affordability and capacity rules, independently of calling the
implementation under test. The pilot changed tests only. Bounded UI changes may
be delegated when the caller has a credible way to verify the affected journey
and failure states; execution of the legacy TestFX suite on this Mac remains
unverified and must not be claimed as coverage.

Caller verification from the assigned worktree root (baseline already passed):

```sh
JAVA_HOME=/opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk/Contents/Home \
  mvn -B -pl microcol-game -am test \
  -Dmaven.test.skip=false -DskipTests=false \
  -Dtest=GoodsTradeTest -Dsurefire.failIfNoSpecifiedTests=false
```

The last flag permits upstream reactor modules without this test. The runner
must still require a fresh
`microcol-game/target/surefire-reports/TEST-org.microcol.model.GoodsTradeTest.xml`
containing the intended cases. Adding the six assigned parameter rows to the three baseline rows requires
nine executed cases, zero failures/errors and zero skipped. Record the real count rather than assuming it from build success.

## Acceptance and further measurements

On **2026-10-11**, the Maven packaging migration delegated launcher metadata to
the local worker as job `2026-10-11-maven-launcher-metadata`. The worker used
`microcol-qwen:9b-q4_K_M-8k`, digest
`dfe6877b76ae37d766653b62909426fc769288fc42575cb5069c2b9b366a79a2`, with an
8192-token context. One attempt finished in 128.69 seconds with exit 0 and no
worker repair rounds. Review accepted its static macOS `Info.plist`; its Linux
assembly descriptor had incorrect paths and public filenames and was replaced
by the main agent. The worker did not run Maven. Caller verification passed XML
and plist validation, a clean Maven launcher package, a repeated package that
removed stale output, and archive layout, executable-mode and SHA-256 checks.
The model was unloaded before native builds. This is a **partially accepted**
delegation, not evidence of measured budget savings. The task, tool events, diff
and `verification.json` are retained in the local job log directory documented
above.

A completed delegation requires **Astra task → verified local worker call → actual
change → required caller verification → Astra review**. The accepted pilot above
records this chain. Installation, a text response or a tool diagnostic alone
would not establish it.

For each accepted task retain the baseline, diff, exact model/digest, command,
process exit, test selection/counts and fresh reports, attempts, timing, available
usage, main-agent repairs and acceptance decision in the job artifacts. Measure
Memory Pressure and swap across generation and the later Java run. Missing
measurements remain explicitly unavailable. Tune task size, context, thinking and
repair limits from several comparable accepted tasks.

The consultation's rough savings estimates were 10–25% for test writing, 25–50%
for tests plus bounded implementations (working goal about 40%), and optimistically
50–70% for repetitive prepared tasks. These are unmeasured planning estimates;
setup, review and rewrites can erase the saving. Test execution itself is not
model-generation usage. Compare equal-quality work with the same main model and
reasoning; separate user time, elapsed time and measured model usage rather than
converting raw tokens into a precise weekly-subscription saving. A 40% reduction
per task would permit about 1.67 times the work; four times the work for half the
budget would require 87.5% savings and is not the working plan.

The direct route is ready for similarly bounded tasks. Broader work still needs
evidence of repeatability and reliable limits under this machine's memory
constraints; GUI execution and precise Codex-budget savings remain unverified.


## Instruction validation

The project skill belongs in `.agents/skills/local-worker`, using `name` and
`description` frontmatter as specified by [Build skills](https://learn.chatgpt.com/docs/build-skills).
The short root rule follows [AGENTS.md discovery](https://learn.chatgpt.com/docs/agent-configuration/agents-md).
Validate the skill and its relative references after changes; check actual Codex
discovery rather than treating correct file placement as proof. If it does not
appear in a target client, refresh/restart that client and inspect disabled skill
settings. Do not create a second copy to work around an unverified discovery issue.

Preparation checks on 2026-10-10:

- The installed `skill-creator/scripts/quick_validate.py`, run with the existing
  `python3`, returned `Skill is valid!` (exit 0).
- All 20 relative file-link targets across the four new files exist. A Git
  whitespace check of each new file against `/dev/null` found no issues.
- Standalone Codex CLI 0.142.3 was started in stdio app-server mode solely for a
  read-only discovery check. After initialization, `skills/list` with
  `cwds: ["/Users/jan/projects/microcol"]` and `forceReload: true` returned exactly
  one `local-worker` at `.agents/skills/local-worker/SKILL.md`, `scope: "repo"`,
  `enabled: true`, with no repository skill errors. The temporary process was
  stopped; no model thread/turn was started. This verifies the installed CLI's
  discovery, not a Qwen run or a refresh of an already-open desktop skill picker.
  The request method is documented in [Codex App Server](https://learn.chatgpt.com/docs/app-server).
- An independent source review found no required corrections to the task scope,
  handoff contract or separation of proposals from observations. Game tests and
  live worker behavior were outside the preparation phase's validation.

Direct CLI rollout checks on the same date revalidated the updated skill, all
26 relative links across the five Markdown files, whitespace in those files and
the Modelfile, and the syntax of all four shell examples. An independent review
matched the accepted pilot's task, local model identity, tool events, diff, Maven
log and nine-case XML report. At that point the pilot test remained isolated;
the user's subsequent branch consolidation brought the accepted test patch into
`develop` without changing production code.


## Functional plowing regression — 2026-10-11

Task `2026-10-11-functional-plow-contract` used baseline
`94bd260e3538c4d632d76f108155fb28476a56cf` and the existing local alias at
context 8192. One attempt exited 0 after **136.581 seconds**, with no worker
repair round. Reported usage was 23,832 input tokens, 17,144 cached input tokens,
1,688 output tokens and zero reasoning output tokens across the CLI turn.
These are local-worker counts, not measured cloud savings.

Qwen added assertions for the selected colonist's plowing action, consumed
movement and unchanged position in TC18. The main agent restored the existing
format, added preconditions, corrected stale comments and shortened the JavaDoc.
The caller handled test-environment diagnosis, shared helper corrections,
coverage policy, CI integration and verification because those required
cross-module review and actual runtime evidence.

The caller passed the 603 game tests on JDK 17 and the targeted TC18/TC15
plowing checks using Monocle 17.0.10. The first full local headless run selected
27 tests, with two failures and one error: Monocle does not implement the LINK
drag needed by TC05/TC06, and mission XML used a DTD rejected by Java 17.
After correcting the two mission bundles, both language checks passed; TC03
also passed after removing a double-lookup race in its shared wait helper.
The two new language tests failed against the original bundles before the fix.

Native Glass on this macOS session did not successfully click Load Game, so
that attempt does not verify the native goods scenarios. The complete suite
uses Linux/Xvfb in the required `functional` job, with native LINK modifiers
handled by the existing page objects. The caller downloaded and inspected the
`tests-functional` artifact from [run 38156401646](https://github.com/jajir/microcol/actions/runs/38156401646),
commit `951e82ae824f55084f7af65edafbdc1948293997`: **27 functional tests and 603
game tests passed**, with zero failures, errors or skips. The complete Linux
result, rather than the partial local run, establishes this acceptance.
Checkout paths containing spaces also prevent the existing JavaFX font loader
from starting the unpacked game. See the [functional test contract](../functional-tests.md)
for commands and the environment limits.

Evidence is retained under
`/Users/jan/Library/Logs/Microcol/local-qwen/jobs/2026-10-11-functional-plow-contract/`:
worker input/result, runtime identity, patches, failed environment attempts,
`game-final.log`, `plow-final.log`, mission red/green logs and verification summary.
The worker model was unloaded before Maven. Source fixtures were unchanged by
the verified isolated test setup. No broad reliability or savings claim follows
from this single assignment.

## Package responsibility documentation — 2026-10-11

Task `2026-10-11-package-responsibilities` used baseline `dba286043459eef872f5ef561e0da753417cdb3e`
and the existing local alias. Runtime samples confirmed its documented digest
and context 8192. The caller stopped the single attempt at **180.25 seconds**
(exit -15). Six of eight assigned `package-info.java` files contained drafts;
there was no final worker result or completed-turn usage report. No repair round
was requested for the small remaining edits.

The main agent reviewed and reused those drafts, corrected the JavaDoc URL syntax,
added missing links, clarified turn-report lifetime, wrapped text and completed
the AI/GUI descriptions. The caller owns the package map, architecture decisions,
documentation links and agent guidance. After unloading the model, the caller
verified comment-only changes in all eight Java files, unchanged declarations,
all 37 production package links, six documentation checks, strict MkDocs and
JDK 17 Maven compilation with tests skipped. This documents no gameplay change;
the publication workflow independently runs its usual checks on push.

Input, raw worker patch, runtime samples and caller evidence are retained in
`/Users/jan/Library/Logs/Microcol/local-qwen/jobs/2026-10-11-package-responsibilities/`.
The partial result does not establish a measured saving or broader capability.


### Z01 attempt evidence — 11 October 2026

Baseline: `89354999954f8a1bfa4c9a6ac0030751056f63e2`. The caller prepared the
isolated worker tree `~/Library/Application Support/Microcol/local-worker/worktrees/2026-10-11-warehouse-unloading`
and the PR branch `codex/rules-warehouse-unloading` in a separate checkout.
The requested delivery is an open PR into `main`, so no direct main push or merge.

| Attempt | Actual result | Accepted worker edits |
| --- | --- | --- |
| `2026-10-11-warehouse-model` | Watchdog timeout, 180.47 s, exit -15; repeated reads. | None |
| `2026-10-11-warehouse-model-repair1` | Watchdog timeout, 180.50 s, exit -15; generated patch script failed with an undefined variable. | None |
| `2026-10-11-warehouse-fixture` | Caller runner preparation error: child CODEX_HOME directory missing, exit 1 after 3.02 s; no inference. | None |
| `2026-10-11-warehouse-fixture-retry` | After caller created the directory, watchdog timeout, 180.51 s, exit -15; incorrect JSON traversal. | None |

The compact repair and fixture invocation used `project_doc_max_bytes=0` to
avoid automatic duplicate repository instructions; applicable rules and scope
were explicitly supplied in the task. This did not make the task succeed and
is not evidence of a reliable new profile. Runtime samples confirmed the
configured local alias, digest and 8192 context. Each runner unloaded the model
before caller Maven verification; only the owned host lock was removed.

The caller reported the failure and completed the implementation, fixture,
tests and documentation under the main agent's control. During native verification,
a Codex collaborator authored the small settings synchronization fix and two
concurrency regressions; the main agent reviewed the changes and verified the
complete native test suite. No local Qwen code was accepted. The configured
local runner was not silently replaced or reconfigured. This is a failed local
delegation, not measured token savings.
Logs, commands, runtime samples and exit reports remain under
`~/Library/Logs/Microcol/local-qwen/jobs/` in the directories above. Actual
model and functional verification is recorded in the
[Z01 test evidence](../functional-tests.md#z01-bezpecne-vylozeni-nakladu).

## PR #44 dock assertion migration — 2026-10-12

Historical direction, superseded later that day by the user's
[named-verification rule](../functional-tests.md#stala-a-citelna-struktura-scenaru).
The evidence below describes the earlier revision, not the current assertion placement.

Task `2026-10-12-pr44-dock-selection` used clean PR head `0dc70bb8`, not
`main`, as explicitly requested. The caller reused the previously accepted
Qwen ship-count refactor from `main` (`92a22efa`) and assigned only the
PR-specific selection assertion/accessors and 18 TC19 call sites to Qwen.
The text-only handoff included exact snippets, paths and match counts; no
worker file discovery or edits were required. This task used the subsequently
agreed **300-second attempt / 600-second cumulative** limits and two repairs,
overriding the older 180-second profile still recorded on this PR branch.

| Attempt | Measured seconds | Actual result and recovery |
| --- | --- | --- |
| 0 | 25.233 | Returned a request for file inspection, not replacements. Caller clarified that supplied fragments had already been inspected. |
| 1 | 46.378 | Generated usable accessors and assertions, but included instruction prose in one old-text field and used match count 1 instead of 17. Caller rejected the batch and sent exact validation failures. |
| 2 | 29.265 | Corrected the repeated count but retained prose in the import match and over-escaped Java quotes. Repair limit reached. |

The caller reused attempt 1's implementation, corrected its import-match
field and repeated count, then verified every replacement against both the
worker baseline and integration checkout before applying it. Main-agent
takeover was limited to those two contract corrections because both allowed
repairs had failed; this was not an unattended worker success. Total worker
time was **100.876 seconds**, including runner cleanup. Runtime samples for
all attempts confirmed the documented model digest and context 8192. Reported
input/output tokens were 3630/22, 3782/460 and 3950/138; separate loading,
prompt-processing and generation timings were unavailable. No savings claim.

The model was unloaded before caller Maven checks. Caller review confirmed
an assertion-free dock component, three explicit count checks in TC01/TC06
and 18 immediate selection checks in TC19, with unchanged scenario IDs,
comments, fixtures, conditional click and FX wait. The shared formatter checked
all six changed Java files with unchanged tokens/comments and idempotent output.
Actual test evidence is recorded in the [functional contract](../functional-tests.md#pr-44-presun-aserci-doku).
Inputs, rejected results, runtime samples and exit reports remain under
`~/Library/Logs/Microcol/local-qwen/jobs/2026-10-12-pr44-dock-selection/`.

## PR #44 named page verifications — 2026-10-12

The revised rule keeps assertions in page methods named `verify...`, with
expected parameters, and removes direct assertions from TC classes. Two bounded
Qwen assignments used clean `ad7140fd` worktrees; the main agent owned the
cross-screen caller migration, source-contract test, documentation and verification.
Both used the agreed 300-second attempt / 600-second task budget and two repairs.

| Task / attempt | Seconds | Diagnosis and outcome |
| --- | --- | --- |
| `2026-10-12-pr44-dock-verifiers` / 0 | 69.636 | Removed getters still called by the proposed verifiers and omitted a wrapper. Rejected before application. Also performed four reads despite the text-only prompt. |
| dock / 1 | 45.412 | Restored getters, but added a guard silently skipping selection checks for missing ships. Rejected; targeted repair required unconditional verification. |
| dock / 2 | 44.363 | Corrected the guard; accepted after exact-match validation and review. Caller stripped JSON fences and applied the identical count delegate to EuropePortScreen. |
| `2026-10-12-pr44-ship-verifiers` / 0 | 55.440 | Wrong CargoSlot package and unused imports; rejected. One unnecessary file-read command failed due to path escaping. |
| ship / 1 | 49.385 | Corrected import and retained all required cargo/state checks. Accepted; caller added one local `final` modifier and normalized import grouping. |

Total worker durations were 159.411 and 104.825 seconds including cleanup.
All attempts exited 0; acceptance depended on source review, not exit status.
Runtime samples confirmed digest
`dfe6877b76ae37d766653b62909426fc769288fc42575cb5069c2b9b366a79a2` and context8192.
The unnecessary reads show that a text-only prompt alone does not disable tools;
no worker file edits occurred. Repairs used the concrete rejected output, not
a restarted investigation. No repairs were skipped and no savings are claimed.

The caller unloaded Qwen before Maven, applied the shared formatter and moved
the checks without changing fixture files or scenario IDs. The initial caller
compile exposed one missed TC09 caller after splitting a mixed action/verifier;
it was updated and the same check rerun successfully. The source-contract tests
then passed 2/2. Final functional evidence is kept in the
[test contract](../functional-tests.md#pr-44-pojmenovane-verifikace).
Inputs, raw results, token reports, model samples and process evidence remain in
`~/Library/Logs/Microcol/local-qwen/jobs/` under the two task IDs above.

## Dock assertion move — 2026-10-12

Historical main-branch work, superseded by the named-verification rule
integrated from PR #44. The checks below describe that earlier revision.

Task `2026-10-12-dock-assertions`, baseline `1b15f866`, moves three ship-count
expectations into TC01 and both TC06 scenarios. The dock and its two screen
wrappers expose the actual count without validating an expected value.
Scope is five Java files; other legacy page assertions are not migrated.

| Attempt | Worker seconds | Observed result |
| --- | ---: | --- |
| 0 | 224.693 | Caller stopped repeated broad reads/context compaction; no edits. |
| 1 | 300.112 | Targeted fragments did not prevent shell-editing errors: incompatible BSD `sed -i`, ineffective Perl replacement, then deletion of the method and JavaDoc opener. Timed out; malformed partial diff rejected. |
| 2 | 75.253 | Text-only patch request exhausted the remaining original budget without a completed response. |
| 3 | 115.871 | After a user-authorized extension, returned a patch with invented surrounding code and invalid syntax; `git apply --check` exited 128. Rejected without application. |
| 4 | 48.409 | Returned six exact replacement entries for the five files; old strings matched, but TC06's new assertion lacked a semicolon. |
| 5 | 21.201 | Qwen corrected that specific statement; caller applied the returned replacements and shared formatter without semantic changes. |

The first three attempts consumed **600.058 seconds including cleanup**.
The user then explicitly authorized **another 30 minutes** for this task,
including its targeted repairs: the extended cumulative ceiling was 2400.058
seconds, not a reset of prior time. The extension used **185.481 seconds**;
total worker time was **785.539 seconds**. This was a one-off exception, not
a change to the default five-/ten-minute limits or a general reliability claim.
The caller's provisional direct implementation was archived and rolled back
before the extended attempt; it was not supplied to Qwen or used as its output.

The accepted result is Qwen-generated replacement text applied by the caller,
not autonomous worker filesystem editing. Source checks verify no dock
assertions/verifications, no old ship-verification method, and three direct
expected-one assertions in unchanged TC steps. All five Java files passed the
shared formatter's token-preservation/idempotence checks. After unloading Qwen,
the caller reran TC01 on macOS/JDK 17/Monocle: **1 passed, no failures/errors/skips**.
Both TC06 scenarios require native Glass/LINK and the full Linux/Xvfb publication
gate; the local TC01 result does not replace that check.

Runtime samples confirmed the same local alias/digest and 8192 context.
Completed attempts 3/4/5 report respectively 3787/3424/3031 input tokens and
1707/522/70 output tokens, zero cached input and reasoning tokens. Interrupted
attempts have no completed-turn usage totals. More time alone is not an
established fix: output format and handoff changed too. The useful recovery
was exact replacement matching plus a targeted syntax repair, not blind retry.
Artifacts, failed patches, responses and caller verification are under
`/Users/jan/Library/Logs/Microcol/local-qwen/jobs/2026-10-12-dock-assertions/`.


## Docker UI runner — 2026-10-13

Task `2026-10-13-docker-ui-runner` used clean baseline `595038b3` and the
existing local alias/digest at context 8192. The worker owned only the proposed
Bash entrypoint; the caller owned the Docker/CI environment and verification.

| Attempt | Seconds | Result and recovery |
| --- | ---: | --- |
| 0 | 104.424 | Direct draft used wrong tar destination, inserted prompt prose into Maven arguments, masked exit status and waited indefinitely for Openbox. Rejected; supplied each concrete failure. |
| 1 | 70.053 | Text-only repair still copied the wrong path, used a non-pipefail shell, nested report directories and ignored copy errors. Rejected; requested a focused final repair. |
| 2 | 42.545 | Corrected snapshot command but retained ignored export failures and incorrect paths, expanded a quoted command as a string and did not start Openbox. Both permitted repairs exhausted. |

Total worker elapsed time was **217.022 seconds**. All turns exited zero, but
none provided an accepted complete implementation. The caller completed the
small runner after the two repair rounds and added runtime checks for success,
Maven failure, export failure, quoted arguments and a clean read-only snapshot.
The caller's first check harness used `compose images` before a container
existed; it was corrected to `compose config --images`, and all checks passed.
This caller setup correction is separate from the worker failures.

Reported input/output tokens were 22266/1257, 3924/816 and 3392/378; the first
turn reported 16879 cached input tokens, the others zero. Runtime samples
confirmed the documented local model and 8192 context. Loading and generation
timings are unavailable; no savings claim is made. Qwen was unloaded before
container builds and Maven. Inputs, rejected drafts, reviews and measurements
remain in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-docker-ui-runner/`.
Acceptance and publication belong to [R-0033](../requirements/done/R-0033.md).


## R-0019 fog overlays — 2026-10-13

1. Task `2026-10-13-r19-fog-rendering`, baseline `0fbd65b0`, assigned three
   existing renderer/preview classes. Runtime samples confirmed
   `microcol-gpt-oss:20b-32k`, digest
   `425bbbff577bec88a59f86c437ff81eb369e011cfcd047121431a05951076fef`, context
   32768. The caller owned the manual decision, fixture, regression tests,
   review and publication.
2. Attempt 0 used **170.478 seconds** and exited zero without edits or a
   completed response. It unnecessarily searched a wrong `Unit` path; the
   reason for the early end is not established. The first repair supplied
   exact source fragments and requested replacement text without tools.
3. Attempt 1 used **233.712 seconds** and returned two complete replacement
   objects followed by a truncated third. The caller retained only the two
   complete objects, then requested the missing small edit. Attempt 2 used
   **94.329 seconds** and returned the valid third object. All allowed paths
   and unique old-text matches were checked before applying the batch to the
   isolated checkout. Total worker time: **498.519 seconds**, both repairs used.
4. Accepted worker output restores fog after overlays and suppresses previews
   into unknown cells. The caller applied the shared formatter and added one
   explanatory comment. Ollama was unloaded before Maven/Docker. Exit zero and
   replacement generation alone did not constitute acceptance.
5. Caller test setup required an explicit predicate cast for TestFX, centering
   the actual viewport, reusing the protected scene snapshot, and returning a
   drag to its origin before release. The earlier Escape/release gesture moved
   the ship and was not a valid cancellation; that failure is not product
   evidence. A non-attacking galleon could not exercise attack-preview leakage;
   the final fixture uses a frigate and an enemy six cells away. On the original
   production code, the corrected test observed a FIGHT path to an unexplored
   enemy; the compositor probe independently exposed an animation over fog.
6. After restoration of the worker changes, the final targeted run passed
   one visibility model test, three TC26 scenarios and three page-contract
   checks. Full-regression acceptance belongs to the
   [functional record](../functional-tests.md#fog-implementation-2026-10-13).
   Prompts, responses, runtime samples and reviews remain in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r19-fog-rendering/`;
   caller logs are in `target/r19-r31/`. No token-saving claim is made.


## R-0020 shared trade quote — 2026-10-13

1. Task `2026-10-13-r20-trade-quote` uses baseline `f383ebfb` and the configured
   GPT-OSS 20B alias/digest with actual context 32768. The worker owns the new
   immutable price quote and its focused unit test; the caller owns pricing
   policy, Player/market integration and actual acceptance. No inference ran
   alongside Docker/Maven.
2. Attempt 0 used **270.426 seconds**: the production class was complete, but
   the test/outer JSON ended mid-expression. The caller extracted the complete
   object without applying an incomplete batch and requested just the test.
3. Attempt 1 used **171.112 seconds** and returned a complete test with an
   incorrect small-sale expectation and missing requested boundaries. The
   isolated Docker run selected four quote tests: three passed, one failed
   (`expected 3, actual 1`). The generated example used quantity 2, price 1;
   the requested case was quantity 1, price 3, yielding net 2 at 17% tax.
   Source review also found that the alleged equal-total comparison had
   different totals, and the 100% sale case actually used 0%.
4. The final targeted repair supplied five exact replacement blocks and the
   unchanged acceptance. It timed out after **158.816 seconds**, with a reasoning
   event but no completed response. Total **600.354 seconds including cleanup**;
   both repair rounds and the cumulative budget were exhausted. The caller
   then corrected those test cases, added the omitted getter/boundary checks,
   validated both new file paths and applied the shared formatter. The production
   quote remains worker output. This is partial reuse with caller repair, not
   autonomous completion.
5. After unloading the model, the caller reran the failing quote test together
   with Player integration: **4 quote and 11 Player tests passed**, zero failures,
   errors or skips. The same targeted run also passed three TC05 cargo scenarios, two TC06
   purchase/sale scenarios and three page-contract checks. The complete R-0020
   equipment journey remains pending; this pricing foundation does not complete
   pier equipment trading.
   Logs and reviews are in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r20-trade-quote/`
   and `target/r19-r31/pricing-*.log`. No savings claim is made.

### R-0020 pier equipment model — 2026-10-13

1. Task `2026-10-13-r20-pier-equipment-model`, baseline `f383ebfb`, received
   the caller's current `Player`/`EuropeTradeQuote` APIs in an isolated tree.
   Attempts used **98.340 / 243.131 / 101.307 seconds** (**442.778 total**).
   Initial placeholders were rejected. The first repair supplied the enum and
   quote but truncated validation. The final repair supplied both remaining
   operations; after both repairs were exhausted the caller narrowed an overly
   broad `catch (Exception)` to `ArithmeticException` and removed redundant
   comments. Existing colony equipment behavior was preserved.
2. Runtime samples confirmed digest
   `425bbbff577bec88a59f86c437ff81eb369e011cfcd047121431a05951076fef`, context
   32768 and the configured GPT-OSS alias. The runner unloaded before Docker.
   The first check failed to compile a caller-authored test using the nonexistent
   `getAvailableMoves()`; the caller corrected it to the actual
   `getActionPoints()`, preserving the movement assertion.
3. The same Docker acceptance command then passed **52 model tests and three
   page-contract checks**, with no failures, errors or skips. This includes all
   22 `EuropeEquipmentTest` cases, 14 colony-equipment regression cases,
   12 Player cases and four quote cases. The caller inspected fresh XML,
   formatted with the shared profile and integrated only `UnitFreeColonist`.
   Model evidence does not establish working pier UI or complete R-0020.
4. Logs and before/after results remain in the task's directory under
   `~/Library/Logs/Microcol/local-qwen/jobs/`; acceptance log is
   `target/r19-r31/equipment-model-fixed.log` in the caller checkout.
   No autonomous-success or token-saving claim is made.

### R-0020 equipment confirmation dialog — 2026-10-13

1. Task `2026-10-13-r20-pier-equipment-dialog` received the precise model API,
   localization keys and existing colony-dialog example. Attempts consumed
   **236.903 / 77.247 / 45.550 seconds** (**359.700 total**). Initial source
   was complete but incorrectly enabled confirmation for some rejected offers.
   The caller requested a one-line presence check, without changing criteria.
2. Repair one ignored text-only instructions and listed the tree, then failed
   after five reconnects with `stream closed before response.completed`.
   No response was produced; the disconnection's cause is not established.
   The caller placed the initial source in the isolated tree and made the final
   repair request self-contained. The final exact replacement correctly uses
   `confirm.setDisable(problemOpt.isPresent())`; the unique match was validated
   before application. The task used both allowed repairs, not a reset budget.
3. The first caller check found that the installed Gson lacks the test's
   `JsonParser.parseString`; caller fixture preparation now uses existing
   `Gson.fromJson`. The same Docker selection then passed **52 model tests,
   nine TC27 scenarios and three page-contract checks**, no failures, errors
   or skips. Fresh reports: isolated `target/docker-ui/run-jRkncS/`;
   caller log `target/r19-r31/equipment-ui-fixed.log`.
4. The caller accepted the dialog after formatting and these checks, reviewed
   English/Czech screenshots, and changed the goods label to the existing
   plural localization key. The caller also owns pier controls, translations,
   fixtures, player tests and the shared cargo-market restriction display.
   Those final integration changes receive separate regression checks.
5. Runtime samples use the same documented GPT-OSS alias/digest and context
   32768; model unload completed before checks. No token-saving claim. Source,
   prompts, errors, replacements and reports remain under the task's log
   directory. The owned worker lock was released after acceptance.

### R-0021 sea-lane policy — 2026-10-13

1. Task `2026-10-13-r21-sea-lane-policy`, baseline `f383ebfb`, assigned three
   pure methods in `HighSea`: departure direction, outward gesture and default
   arrival. The caller retained manual interpretation, persistence, movement,
   UI integration and actual tests. Runtime samples confirmed the configured
   GPT-OSS alias/digest and context 32768; inference did not overlap local tests.
2. Attempts used **171.025 / 300.229 / 126.816 seconds**, **598.070 total**.
   The first exited zero with an empty response after unnecessary reads. The
   first repair repeated source discovery and timed out. The final repair
   reduced the handoff to the exact APIs and three method contracts; it
   returned complete source. The empty first completion's cause is unknown;
   a metadata-fallback warning does not prove causation.
3. Final source used nonexistent uppercase `Direction.EAST/WEST` despite the
   supplied lowercase API and an unresolved `Optional` Javadoc link. Both
   repairs were exhausted. The caller corrected those names, removed a
   redundant comment, applied the shared formatter and retained the algorithm.
   This is partial worker reuse with caller correction, not autonomous success.
4. The first caller Docker selection passed its model tests but failed the
   test module because no functional/control tests were selected. The corrected
   command adds `PageVerificationContractTest`, preserving the no-tests gate:
   `-Dtest=HighSeaPolicyTest,HighSeaTest,ModelArchitectureTest,PageVerificationContractTest`.
   Fresh isolated XML `target/docker-ui/run-diqWLY/` prove **22 model/architecture
   and three source-contract tests**, no failures, errors or skips. The caller
   integrated only `HighSea` and its test, then released the owned lock.
5. Logs, all rejected responses and caller review remain under the task's
   directory in `~/Library/Logs/Microcol/local-qwen/jobs/`. Acceptance logs are
   `target/r19-r31/sea-lane-policy*.log` in the caller checkout. Full voyage
   integration remains pending; no savings or broader quality claim is made.

### R-0021 voyage arrival — 2026-10-13

1. Task `2026-10-13-r21-voyage-arrival` uses baseline `b989413b` plus recorded
   caller integration inputs. The worker owns only the new `Ship.startTurn`
   body. The caller owns the coast schema, departure validation, UI, fixtures
   and test journeys. Runtime samples for all three attempts confirmed the
   configured GPT-OSS digest and context 32768.
2. Attempts consumed **179.693 / 127.823 / 39.527 seconds**, **347.043 total**.
   The first method incorrectly fired New World arrival/reveal actions in the
   Europe branch and called a nonexistent report factory. The first targeted
   repair fixed those errors but fired the New World event before adding its
   report. The final repair returned the correct three-statement sequence.
   The caller checked the unique replacement and removed obsolete random
   arrival code, then applied the shared formatter. Both repair rounds used.
3. The initial caller test run selected 45 game/architecture cases; ten new
   voyage cases failed during setup because an empty game-over evaluator list
   is invalid. The caller reused the existing mission evaluators. The next
   run exposed five JSON-save failures: the optional direction field requires
   the existing Gson direction adapter's `nullSafe()` wrapper. The caller
   corrected this integration point; factory validation still rejects missing
   coast data for ships in transit and invalid direction/location pairs.
4. After model unload, the same Docker selection passed all **45 model checks
   and 34 functional/control cases**, including TC23, TC27, TC28, TC19 and the
   source contract. Fresh isolated XML `run-hPfVaK` contain no failures,
   errors or skips; log `target/r19-r31/voyage-integrated-json-fixed.log`.
   The caller accepted the method and removal of obsolete random-arrival code,
   integrated those files and released the owned lock. A subsequent visual
   review exposed a truncated waiting label; the caller's layout correction
   and additional occupied-outward-step model boundary passed a further
   11 model and five functional/control tests in main `run-ZBBq6H`. The whole
   regression and publication remain separate from this worker acceptance.
5. Prompts, source responses, model samples and before/after diagnoses remain
   in the task directory under `~/Library/Logs/Microcol/local-qwen/jobs/`.
   The isolated checkout separates worker verification from main integration.
   No local inference overlapped Maven/Docker and no savings claim is made.

### R-0022 passenger selector — 2026-10-13

1. Task `2026-10-13-r22-passenger-dialog` uses isolated baseline
   `88573a85ac0336307724b7da8bf06bc4bb159990`. The caller supplied the translated
   enum/XML keys and R-0022 contract; later model/controller/test inputs were
   copied explicitly for integration. Only `ChoosePassengerDialog.java` is
   worker-authored. Architecture, shared transfer validation and actual checks
   remain caller-owned because they span persistence, map, port and AI callers.
2. Attempt 0 timed out after **300.337 seconds** without a file change. Events
   show repeated source discovery, including a wrong enum path and broad GUI
   listing. Repair 1 provided exact API signatures inline and requested one
   complete source file without tool use; it completed in **111.946 seconds**.
3. Static review found access to the superclass's private `i18n`, missing radio
   IDs, enabled confirmation with no selected passenger, a premature accepted
   result, unused imports and an invalid generic Javadoc link. Repair 2 took
   **109.415 seconds**, fixing the compile/selection errors but removing default
   first selection and retaining the Javadoc link. Both repairs were exhausted;
   the caller restored initial selection and corrected the link, then used
   ordinary coordinates in the caption instead of the model object's `toString`.
4. Total worker time **521.698 seconds**, three attempts, two repairs. All actual
   runtime samples identify `microcol-gpt-oss:20b-32k`, context **32768**, digest
   `425bbbff577bec88a59f86c437ff81eb369e011cfcd047121431a05951076fef`.
   The runner unloaded after every attempt before Docker checks. No measured
   token saving or autonomous acceptance is claimed.
5. The caller applied the shared formatter to the integrated isolated inputs;
   token preservation and idempotence passed. Compilation passed. The first
   model run exposed stale departure fields in the caller's modified scenario;
   the second exposed two further test setup mistakes: `Model.stop()` does not
   change the saved game-start flag, and a foreign carrier cannot be created on
   the friendly carrier's occupied square. Corrected fixtures clear the old
   voyage origin, explicitly set the unstarted state, and use adjacent water.
   A further comparison error used whole-game save before the game had started;
   the corrected rejection test compares passenger/carrier snapshots and all
   cargo instead. The first UI load also exposed a tree left on new water in
   F21; this caller fixture was corrected without weakening map validation.
   The unchanged acceptance selection then passed in isolated `run-MZv8FT`:
   **10 model/architecture and six functional/control tests**, zero failures,
   errors or skips. TC01 retains its original cargo assertions through two
   choices; both TC29 selection/cancel/Continue/stale-target cases passed.
   The caller reviewed `passenger-selection.png`, accepted the selector,
   copied only its class into main and released the owned lock. All 20 current
   main Java changes then passed the shared formatter/token/idempotence check.
   Full R-0022 acceptance remains pending broader boundaries and regression.
6. Evidence: `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r22-passenger-dialog/`
   contains task revisions, generated source, runtime samples, execution budgets
   and caller review. Main-checkout logs are
   `target/r19-r31/passenger-model-first.log`, `passenger-integrated.log` and
   `passenger-integrated-fixture-fixed.log`. These failures concern caller test
   preparation; they are not reported as worker implementation defects.

### R-0022 frigate eligibility tests — 2026-10-13

1. Task `2026-10-13-r22-frigate-tests`, detached baseline
   `88573a85ac0336307724b7da8bf06bc4bb159990`, received the current R-0022
   inputs explicitly recorded in `caller-inputs.json`. Only
   `UnitFrigateEmbarkTest.java` was assigned. The caller owns real-model
   transfer fixtures, coastal boundaries, UI journeys and acceptance.
2. Attempts used **239.736 / 232.304 / 108.129 seconds**, **580.169 total**.
   The first response was truncated, retained the obsolete Europe exception
   and omitted the fifth test. The first repair was also truncated and
   invented `sharedPassengerPredicate`. Neither result was applied. The last
   repair returned four complete snippets using the actual passenger API.
3. The caller applied the last snippets only in the isolated tree. An initial
   exact-match check stopped before writing because the old file lacked a
   trailing newline; the application was corrected against the actual ending.
   The first compile then rejected `verifyNoInteractions`, an API explicitly
   supplied by the caller but absent from this project's Mockito version.
   The caller replaced it with existing `verifyZeroInteractions`, preserving
   the assertion. Both worker repairs were exhausted; this one-line caller
   setup correction did not justify another worker task or a budget reset.
4. All three attempts reported the configured digest
   `425bbbff577bec88a59f86c437ff81eb369e011cfcd047121431a05951076fef`
   and context 32768. The model was unloaded before formatting and Docker
   verification. No cloud fallback or savings claim. Full prompts, responses,
   execution metadata and caller reviews remain under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r22-frigate-tests/`.
5. The five assigned frigate tests and three page-contract tests passed in
   isolated `run-myTaDs`. Only the assigned class was integrated into `main`
   and the owned worker lock released. The complete selected model group
   passed 44 cases; the UI group had one caller-authored journey failure
   among 18 cases (incorrect assumed return location). That failure remains
   separate from the bounded worker acceptance. Additional caller inputs are
   recorded in `integration-inputs.json`; they are not worker-authored changes.
   Actual results belong to the [passenger evidence](../functional-tests.md#individual-passenger-transfers-2026-10-13).

### R-0023 specialist type — 2026-10-13

1. `2026-10-13-r23-specialist-type` used isolated baseline
   `a2a4ac22b9dbf1341fb1be81f3cadb84adb5176a` plus the explicit Ready R-0023
   analysis. Scope: remove `final` from the ordinary colonist, add the concrete
   blacksmith subtype, factory route and type registry entry. The caller owns
   purchase validation, UI, actual model/player tests and integration.
2. Attempts used **270.833 / 242.690 / 78.808 seconds**, **592.331 total**,
   both repair rounds used. Attempt 0 ended with truncated JSON at output 4096,
   a wrong registry path and an ambiguous insertion anchor; no text was applied.
   A focused repair provided exact unique anchors. Its valid JSON was applied
   in isolation, but source review found an invalid superclass constructor call
   and private inherited-field assignments. The final small repair forwards all
   nine constructor arguments to the existing superclass. No new fields or
   profession framework were introduced.
3. The caller supplied `Player` purchase guards and `ExpertPurchaseTest` as
   recorded integration inputs. The first Docker selection had 36 model cases
   with one caller fixture error: a saved warehouse map was immutable. Copying
   that map fixed the setup. The unchanged selection then passed **36 model and
   three structural cases**, no failures, errors or skips (`run-AfJ4fv`), exit 0.
   Six isolated Java files were formatted with the shared profile, preserving
   code tokens. Only the four assigned worker files were accepted into main;
   caller files retain separate ownership. Player journeys remain unverified.
4. All three runtime traces identify the configured GPT-OSS digest
   `425bbbff577bec88a59f86c437ff81eb369e011cfcd047121431a05951076fef`, context
   32768. The runner unloaded before each Docker check and released its owned
   lock after acceptance. Evidence remains in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r23-specialist-type/`,
   including rejected outputs, review/application records and `acceptance.json`.
   No token-saving or general quality claim is made from this assignment.

## Specialist workplace identity — 2026-10-13

1. Task `2026-10-13-r23-worker-identity` used baseline `b3ba0efc` plus 29
   explicitly copied caller inputs. Its only allowed edit was
   `AbstractPanelConstruction.getConstructionName`: retain the existing
   localized building name and append the localized names of occupied slots.
   The worker returned one valid exact replacement in **93.628 seconds**;
   no worker repair was needed. Runtime samples confirmed the configured
   GPT-OSS digest and context **32768**. The model was unloaded before Docker.
2. Caller acceptance first exposed a wrong expected building translation:
   `ConstructionTypes.xml` was used instead of the colony screen's owning
   `ConstructionTypeName.xml`. After correcting that expectation, the new
   departure gesture exposed an existing UI problem. Captured mouse events
   showed `id_tobacco` intercepting a click on the visible blacksmith below it
   because a translated production box enlarged its picking bounds.
3. The main agent applied the two-line picking correction directly: panel
   empty bounds no longer catch the mouse, and decorative production boxes
   are mouse-transparent. This is outside the worker's naming-only method;
   a new delegation for those two diagnosed lines would cost more than direct
   application/review. The caller preserved the original gesture coordinates
   and state/name expectations and removed temporary diagnostic logging.
4. The same isolated selection then passed **13 model and 14 functional/control
   tests**, zero failures/errors/skips (`run-CqcmQQ`, exit 0, 2:37). All six
   TC30 cases, five TC22 cases and three source-contract controls ran. The
   worker method and the two caller picking corrections were integrated;
   the owned worker lock was released. Shared Java formatting passed.
   The separate new caller test for replacing the entire game subsequently
   passed with three structural controls. Full regression and publication
   remain pending; this is not whole-R-0023 acceptance.
5. Prompts, runtime samples, generated output, exact application, caller
   failure diagnoses and `acceptance.json` remain in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r23-worker-identity/`.
   No throughput or token-saving claim is made.


### R-0024 wagon display — 2026-10-13

1. Task `2026-10-13-r24-wagon-display` used isolated baseline `2a464170`
   plus four recorded caller inputs (analysis, F23 fixture, page object and
   TC31). The worker owned seven production files: image registration,
   English/Czech unit names, colony carrier query and its dock consumers.
   The main agent retained manual interpretation, acceptance tests and review.
2. Attempts consumed **257.939 / 179.233 / 163.102 seconds**, **600.274 total**,
   both repairs exhausted. The first response was truncated at the 4096-token
   output limit and had an incorrect image-map anchor; no patch was applied.
   The first repair returned seven replacements. Its map insertion incorrectly
   used `request` inside the map builder and was rejected. Six valid changes
   were applied: instance image, colony import/query, removal of the raw cast
   and suppression, and ship-only carrier-icon dragging.
3. The final repair timed out while rereading image-provider source and
   searching insertion anchors, without a final response. The supervised
   limit was the remaining 162.828 seconds; cleanup accounts for the small
   overrun. The caller then completed the five remaining replacements:
   type image, enum keys, both resource bundles and query return documentation.
   This is partial worker reuse with caller completion, not autonomous success.
4. The runner unloaded the model before Docker tests. Shared formatting
   checked ten Java files, changed six, and passed token preservation and
   idempotence. Initial acceptance exposed a stale warehouse label in a caller-authored
   scenario. The caller added three lines using the existing repaint event
   before a refreshed amount dialog. The identical selection then passed
   **66 model and 34 functional/control cases**, no failures/errors/skips
   (`run-XUJtg1`). All eight TC31 cases ran. The caller integrated the
   14 source/test/fixture files after baseline checks and released the owned
   lock. The full main regression then passed 801 model and 113 functional/control
   cases without failures, errors or skips (`run-vD4k94`). Publication remains pending. The caller authored seven
   real-model checks and eight player scenarios and updated the existing
   TC22 artwork diagnostic, preserving its stable ID.
5. Prompts, rejected responses, execution records and caller application
   evidence remain in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r24-wagon-display/`.
   No token-saving claim. Inference did not overlap local Maven/Docker tests.


### R-0025 building metadata — 2026-10-13

1. Task `2026-10-13-r25-building-metadata` used isolated baseline `e43e32fc`
   plus the explicitly copied R-0025 analysis. The configured GPT-OSS 20B
   alias/digest at context 32768 was confirmed in runtime samples and unloaded
   before verification. Only `ConstructionType.java` belonged to the worker;
   the caller owned removed-tier consumers, fixtures and acceptance tests.
2. Attempts took **268.916, 266.676 and 61.648 seconds**, **597.240 total**.
   The first exhausted its 4096-token output allowance mid-JSON and supplied
   double-escaped source; nothing was applied. The first repair used plain
   source blocks and a shorter contract but exhausted output in reasoning,
   returning no final result. Exit code zero did not establish a usable patch.
3. With 64.408 seconds left, the final bounded repair returned two valid exact
   replacements: removing the obsolete registry entry and accepting paid
   projects with a zero-tool cost. Both matched once and were reviewed before
   application. The caller completed the other six metadata regions after
   explicitly taking over that scope; the final repair did not own those edits.
   Both repair rounds are exhausted. No cloud fallback or savings claim applies.
4. Caller adaptations removed obsolete UI/name references, used the two carpenter
   tiers in 16 saves, updated fur-chain expectations, and added 41 independent
   metadata rows. Eight changed Java files passed the shared formatter with
   token preservation and idempotence. Docker `run-h4Sn6S` passed **52 model
   and three page-contract cases**, zero failures, errors or skips, using
   `-Dtest=ConstructionMetadataTest,ConstructionTypeConstantTest,ColonyBuildingQueueTest_queue,ModelArchitectureTest,PageVerificationContractTest`
   with `-Dsurefire.failIfNoSpecifiedTests=false`.
5. Accepted 28 source/resource/test/fixture files were integrated after checking
   their original main blobs. The task lock was released. Evidence, rejected
   responses, runtime samples, caller completion and file hashes remain under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r25-building-metadata/`.
   This accepts only metadata; full R-0025 queue behavior and UI remain unfinished.

### R-0025 construction admission — 2026-10-13

1. Task `2026-10-13-r25-construction-admission` used baseline `13001e6a`
   with 34 explicitly recorded R-0025 inputs. The caller supplied the enum,
   method stub, query/addition integration and real-model boundary tests.
   The worker owned only `getUnavailableReason(ConstructionType)`, with the
   established reason priority and no mutations or resource spending.
2. Attempt 0 took **190.441 s**. Its conditions were mostly correct but used
   a generic cast for a non-generic class and treated an `Optional` predecessor
   as a nullable construction type. The caller rejected it before application.
   An initial caller diagnosis of double-escaped newlines was wrong: parsing
   confirmed one matching anchor. That mistaken warning had already entered
   the first repair prompt and was explicitly corrected in the evidence and
   final repair; it is not attributed to the worker.
3. Repair 1 took **259.439 s**. It changed the required reason ordering,
   confused zero-cost projects with population zero, omitted the reverse
   queued-upgrade conflict and returned a nonmatching escaped anchor. None of
   that revision was accepted. The caller staged the first candidate only in
   isolation and narrowed the last repair to its two Java type errors.
   Repair 2 took **74.941 s** and returned two matching replacements. Total
   **524.821 s**, both allowed repairs used; no budgets were reset.
4. The configured GPT-OSS alias/digest and actual context **32768** were
   verified from runtime samples. Inference did not overlap local Docker
   checks; the model was unloaded. After shared formatting, the isolated
   Docker run `run-k4ldgf` passed **125 model cases and three page-contract
   controls**, zero failures, errors or skips. This includes 60 admission
   cases, 41 metadata cases, 23 saved-queue cases and the architecture check.
5. The caller integrated the accepted admission method and its tests,
   removed a now-unused exact-type helper, and retained separate ownership
   of queue transactions, persistence and UI integration. This does not
   establish completion of R-0025. Full prompts, rejected outputs, corrections,
   runtime samples and acceptance evidence are in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r25-construction-admission/`.
   No token-saving claim follows from this result.

### R-0025 completion prerequisites — 2026-10-13

1. Task `2026-10-13-r25-completion-prerequisites` used baseline `13001e6a`
   plus 38 explicitly recorded inputs. The caller supplied the completion-query
   stub, a guard before the existing queue completion mutation, and three
   additional regressions. The worker owned only the read-only query; it must
   check live colony, existing target and exact predecessor without reapplying
   the population admission gate.
2. Attempt 0 reached its deadline after **300.400 s** without a final patch.
   It ignored the text-only handoff and made ten repository reads. All completed
   reads returned exit 0; the caller's initial suspicion about nested shell
   quoting was incorrect and was corrected in the diagnosis. Unnecessary
   discovery, not a demonstrated shell failure, consumed the attempt.
3. Repair 1 took **109.992 s** and supplied the required method, but placed
   `expected_matches` outside the JSON replacement object. Parsing failed, so
   no patch was applied. The final format-only repair took **116.701 s** and
   returned valid JSON with the same method. Total **527.093 s**, both repairs
   used; no time budget reset. The caller did not replace the implementation.
4. The configured GPT-OSS alias/digest and context **32768** were confirmed
   in runtime evidence. The model was unloaded before formatter and Docker
   checks. The scoped run `run-soSK6J` passed **144 model cases and three
   page-contract controls**, no failures, errors or skips. This includes the
   earlier missing-predecessor failure and checks for an already-built target,
   an abandoned colony and a unit whose required mill disappeared.
5. The caller integrated the two changed files after checking their recorded
   input hashes. The task evidence is in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-13-r25-completion-prerequisites/`.
   The caller subsequently ran the broader model selection and repaired two
   stale passenger tests with nonstandard discovery names; those results belong
   to the [construction evidence](../functional-tests.md#construction-model-implementation-2026-10-13).
   The construction UI and player journeys remain unfinished; this result
   alone does not complete R-0025.

### R-0025 queue controller — 2026-10-14

1. Task `2026-10-14-r25-queue-controller` used isolated baseline `13001e6a`
   plus explicitly copied R-0025 model, metadata and caller UI/test changes.
   The worker owned the bounded controller; the caller owned the picker,
   localization, workplace control, fixtures and player scenarios.
2. Attempts took **198.201 / 254.641 / 119.945 seconds**, **572.787 seconds**
   cumulative. The first candidate had an incorrect Model import, missing item
   imports, a silent foreign-item return and an unused helper. Repair 1 repeated
   the whole file and returned truncated JSON; the caller's retained whole-file
   example made the otherwise narrowed repair contract inconsistent. Repair 2
   returned an array and changed a required semicolon to a comma in its import
   anchor and replacement. Exact-match validation rejected that batch.
3. Both repair rounds were exhausted. The caller retained the first candidate's
   binding, model-first updates and reordering logic, repaired the listed small
   defects and added a stopped-model guard with its regression. This is partial
   worker reuse, not successful autonomous completion. An initial caller parser
   assumed an object; after inspecting the actual array, the nonmatching import
   anchor remained a separate substantive defect. No ambiguous patch was applied.
4. All attempts used the configured GPT-OSS alias/digest and context 32768,
   confirmed by runtime samples, and unloaded before Docker. Shared formatting
   passed on 27 changed Java files in the isolated checkout. The repaired controller passed six caller tests; the selected model cases and
   source contract also passed. After a separate caller Escape fix, all seven
   TC32 scenarios and the five TC22 scenarios passed. Exact runs and the earlier
   UI failures are in the [construction evidence](../functional-tests.md#construction-ui-implementation-2026-10-14). Full prompts, responses and
   diagnoses remain under `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r25-queue-controller/`.
5. Caller procedure correction: inspect the application result before dispatching
   the dependent formatter. Twice formatting ran after a parser rejection;
   it made no semantic repair, but should not be treated as evidence that the
   patch had applied. Final application and formatting were explicitly sequenced.

## R-0026 relation pairs — 2026-10-14

1. The isolated `2026-10-14-r26-relation-pairs` assignment at `61789f81`
   requested only the package-private bilateral-state helper. The caller supplied
   its interface, trivial state/PO classes, eight acceptance cases and the
   manual-based requirement. Model lifecycle, persistence integration, AI and
   GUI were outside that worker assignment and remain separate work.
2. Attempts took **262.801 / 249.641 / 87.774 seconds** (**600.216 seconds**
   including final timeout cleanup), with both repairs used. The first two CLI
   runs exited 0 but reached the reported 4096-token output limit with incomplete
   JSON; neither response was applied. A zero process exit was not acceptance.
   Their exact reasoning/final-output split is not established by the available
   usage counters.
3. Review of the first fragment found duplicated adjacency entries and a joined
   string pair key instead of one unordered record. The first repair required a
   compact map with immutable two-name keys. Its partial constructor still
   counted Crown players, accepted Crown endpoints and used null exceptions
   inconsistent with the validation contract. The final repair requested only
   that constructor, using the remaining 87.558-second allowance; it timed out
   without a completed response. No further retry was allowed by either limit.
4. The caller then implemented the small helper directly: one unordered-pair
   map, exact registered player identities, non-Crown completeness, atomic
   expected-state transitions and fresh saved DTOs. **No worker-generated code
   was accepted.** This takeover follows exhausted repairs/runtime, not a
   silent replacement of a still-repairable attempt. The original eight
   acceptance expectations remain unchanged. For subsequent assignments, split
   multi-method new-class output before requesting another whole-class response;
   the existing focused-handoff workflow owns that choice.
5. All loaded runtime samples matched the configured GPT-OSS digest and context
   32768; every attempt unloaded successfully. The shared formatter checked all
   four Java inputs with exact token preservation and idempotence. Targeted
   caller verification after unload passed **eight model cases and three
   page-contract controls**, zero failures, errors or skips (`run-p36aLJ`,
   Docker exit 0). The four formatted Java files were copied byte-for-byte into
   main; game lifecycle/save-schema wiring, AI, report UI and full R-0026
   acceptance remain pending. Evidence is in the job's `attempt-*/execution.json`,
   runtime samples, diagnoses and `acceptance.json`, with the caller log at
   `target/r26-draft/pairs-target.log`.


## R-0026 save validation cases — 2026-10-14

1. Task `2026-10-14-r26-save-cases` used baseline `61789f81` with explicitly
   copied R-0026 work. The caller owned Model/ModelPo wiring, lifecycle guards,
   fixture updates, the disk-test harness and live-model tests. The worker owned
   only the `malformedRelations()` provider with 16 specified invalid inputs.
2. Attempt 0 took **184.618 s**. Its JSON wrapper and replacement anchor were
   valid, but cases 4–14 returned objects instead of one-row arrays; those would
   test the wrong boundary. The Crown case also used a self-pair with state
   `KING`. Nothing was applied. The targeted repair described these concrete
   defects and retained the original acceptance criteria.
3. Repair 1 took **129.115 s**, producing all 16 intended defects. The caller
   checked the exact source anchor, permitted method body and decoded input
   semantics before applying one replacement. Total worker time **313.733 s**;
   one repair used. Runtime samples for both attempts matched the configured
   GPT-OSS digest and context **32768**; both unloaded before Docker checks.
4. Caller formatting checked 13 touched Java files with exact-token preservation
   and idempotence. In the isolated checkout, `run-PAfjpb` passed **44 model
   cases plus 3 source-contract cases**, zero failures, errors or skips. The
   17 disk cases comprise the worker's 16 malformed inputs and the caller's
   missing-list case; this is not complete R-0026 gameplay acceptance.
5. Separate caller reproduction `run-3zhffH` disabled only the new stop/game-over
   flags and ran two lifecycle tests: both failed because the old behavior
   still reported a running game. Restoring the flags made both pass in the
   targeted group. Thirteen formatted Java files were copied to main; the
   worker authored only the provider method. Evidence and acceptance remain in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r26-save-cases/`.
   The owned lock was released. No cloud fallback or token-savings claim.


## R-0026 hostility queries — 2026-10-14

1. Task `2026-10-14-r26-hostility-queries`, baseline `61789f81` plus copied
   R-0026 work, assigned three regions of `AbstractUnit`: foreign-colony movement
   permission, attack availability and hostile-target filtering. The caller
   supplied the shared `Model.areHostile` query, command/capture guards and tests.
2. Attempt 0 took **247.129 s**. All three old-text anchors had lost internal
   indentation and matched zero times. The attack query also omitted the peaceful
   colony-owner gate for a hostile third-party defender. Nothing was applied;
   the caller sent these concrete defects for a targeted repair.
3. Repair 1 took **130.229 s**, returning three matching replacements with both
   movement and attack gates. Total **377.358 s**, one repair. Caller review used
   the existing `Optional` import and final locals, removed a redundant self-owner
   filter and a comment repeating its conditional. All three query regions
   otherwise retain the worker's implementation. Both runtime traces matched
   the configured GPT-OSS digest/context **32768**, and unloaded before tests.
4. A separate caller reproduction, `run-qkkQKc`, failed both armed-capture tests
   with `Equipment change rejected: UNIT_UNAVAILABLE`: automatic field assignment
   tried to return weapons before colony ownership changed. Following manual
   pages 32/46, the caller removed that automatic assignment, required the force
   to be on the colony tile, and routed adjacent AI capture through ordinary
   movement. The capturer remains equipped on the map. The existing colony-ID
   test now places its capturing force at the actual colony instead of a remote
   coordinate; its ownership, identity and rename assertions remain intact.
5. Caller verification `run-n3Tff9` passed **68 model/AI cases plus 3 source-contract
   cases**, zero failures/errors/skips. It includes 13 new hostility-boundary
   cases and seven actual simple-AI planning/fighting/capture cases. Nineteen
   touched Java files passed shared formatting, token preservation and
   idempotence before integration. The owned lock was released.
6. Prompts, rejected response, diagnosis, accepted replacement and actual reports
   remain in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r26-hostility-queries/`.
   The caller owns the manual decisions, additional capture repair, test authoring,
   integration and wider checks. No cloud fallback or token-savings claim.

## R-0026 declaration review — 2026-10-14

1. Task `2026-10-14-r26-declaration-dialog` used baseline `61789f81` and recorded
   copied R-0026 inputs in an isolated worktree. The worker owned only the small
   `DeclarationDialog`; the caller owned report wiring, model guards, translation,
   fixture, scenarios, screenshots and actual verification.
2. Attempt 0 (**157.246 s**) returned malformed JSON, invented import packages
   and no visible title for the undecorated window. Nothing was applied. The
   caller clarified exact imports, which should have been supplied explicitly
   in the original tool-free handoff, and requested targeted repair.
3. Repair 1 (**128.298 s**) fixed JSON/imports but still omitted the visible title
   and retained an unused field. It was staged only in the isolated worktree.
   Repair 2 (**96.376 s**) supplied three exact replacements, but its insertion
   contained a literal backslash-n outside a Java string. The formatter preserved
   those tokens; compilation of `ui-first.log` rejected them before any test ran.
4. Both allowed repairs were exhausted at **381.920 s** cumulative worker time.
   The caller replaced that one escape with an actual newline and closed the
   one-shot dialog lifecycle after every modal dismissal, including a window
   close. Caller final locals and shared formatting are separate from worker
   output. Runtime traces contained **326** loaded-model samples of the configured
   digest and context **32768**; all attempts unloaded with exit 0 before Maven.
5. The subsequent main model selection passed **75 cases**, but front-end test
   compilation found a separate caller mistake: the page object read package-private
   `Colony.save()`. It now reads colony stock through public `Model.save()`.
   The subsequent `run-sFAN9W` passed all seven TC33 player scenarios and three
   source-contract checks, including actual English/Czech confirmation, keyboard
   cancellation and stale-state rejection. Broader acceptance remains in the
   [functional evidence](../functional-tests.md#european-hostility-and-political-report-2026-10-14).
6. Full prompts, generated text, diagnoses, runtime samples and caller application
   records remain in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r26-declaration-dialog/`.
   Valid JSON and exact anchors alone do not prove compilable Java: inspect inserted
   escapes before application. No cloud fallback or savings claim.

## R-0027 bounded movement — 2026-10-14

1. Task `2026-10-14-r27-bounded-navigation` used isolated baseline `432e96b0`.
   The worker owned only `SimpleAiPlayer.computeMoveLocation`; the caller owned
   three regression cases, architecture, manual decisions and actual checks.
   The four unrelated R-0026 publication-evidence updates stayed in main.
2. One attempt took **144.743 seconds**, with no repair. The generated method
   retains the preferred direction, scans the eight neighbours once if blocked,
   and returns when no legal continuation exists. All candidate moves still use
   the shared movement and diplomatic query. An initial caller suspicion about
   escaped newlines was disproved by decoding the JSON and checking its exact
   single source match; no correction was needed or dispatched.
3. Caller reproduction `run-Ujxgay` ran three movement cases against the original
   method: the surrounded-unit case failed with a two-second timeout. After
   worker application and shared formatting, isolated `run-sAeMOC` passed
   **11 AI cases and 3 page-contract controls**, zero failures/errors/skips.
   This proves the bounded-movement prerequisite, not completed R-0027 gameplay.
4. All **125** loaded-runtime samples matched the configured GPT-OSS digest and
   context **32768**. The model unloaded before tests; the two formatted Java
   files were integrated byte-for-byte and the owned lock released. Prompts,
   runtime evidence, caller review and `acceptance.json` remain under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r27-bounded-navigation/`.
   Full first-colony/UI acceptance and publication remain pending.

## R-0027 expedition map selection — 2026-10-14

1. Task `2026-10-14-r27-map-selection` used isolated baseline `432e96b0` plus
   recorded R-0027 inputs. The worker owned six bounded edits across the map
   enum, setup screen and English/Czech keys. The caller owned scenario content,
   tests, the first-colony architecture and actual acceptance.
2. Attempt 0 took **205.894 s** and returned seven entries: the setup list anchor
   was duplicated with `expected_matches: 1` twice, although its original source
   has two matches. The caller rejected the batch before application. A caller
   review assertion expected six entries, but its shell then launched the old
   prompt after the assertion failed. That unintended attempt was stopped after
   **12.732 s**, unloaded, and charged to both the runtime and repair budget.
   Subsequent preparation/dispatch uses `set -e` so review failure stops dispatch.
3. Final repair 2 took **154.799 s** and returned identical old/new text, adding
   no map choice. Both repair slots were exhausted at **373.425 s** cumulative
   runtime. The caller retained the original valid source edits and consolidated
   the duplicated setup edit into one replacement with an exact match count of
   two. This is caller completion after failed repair, not autonomous success.
4. Shared formatting checked ten changed Java files with exact token preservation
   and idempotence. Caller verification covers both original worlds, the new
   world's model/disk state, existing TC24 journeys and new TC34 setup/Continue
   and Czech selection-refresh cases. Isolated `run-mZrKO0` passed **4 model
   cases and 11 player/support cases**, zero failures/errors/skips; both new
   setup screenshots were inspected. All **323** loaded-runtime samples matched
   the configured GPT-OSS digest/context 32768, and all attempts unloaded before
   tests. Ten accepted source/test files were copied byte-for-byte to main and
   the owned lock released. These checks do not prove founding AI; full R-0027
   acceptance and publication remain pending.
5. Full inputs, generated text, caller setup diagnosis, bounded execution and
   application records remain in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r27-map-selection/`.
   No cloud fallback or token-savings claim.


### R-0027 shared founding queries — 2026-10-14

1. `2026-10-14-r27-founding-query` assigned only prospective and immediate
   founding queries in `Model` to the local worker. The caller retained atomic
   creation, equipment/food preflight, map-edge presentation, fixtures and tests.
2. Attempt zero took **265.168 s** and exhausted its output before completing
   the replacement JSON; its partial Java also invented a `SEA` constant.
   Nothing was applied. The first targeted repair took **251.698 s** and
   supplied complete code, but review found `getLocation()` before the on-map
   guard. The second repair took **76.494 s** and corrected that order.
   Total **593.360 s**, both repairs used. Caller edits only shortened the
   query Javadoc; production query logic is the repaired worker result.
3. All **526 loaded-model samples** matched the configured alias/digest and
   context **32768**; every attempt unloaded before caller checks. Shared
   formatting passed token preservation and idempotence. No cloud fallback
   or measured token-savings claim.
4. Caller checks first exposed a missing `map.isValid` stub in an existing
   equipment test; the caller repaired its fixture without changing the
   expected rejection. A newly authored UI drag check exposed native gesture
   completion after the FX queue; the caller reused the existing `DRAG_DONE`
   wait. These were caller test integration repairs, not further worker attempts.
   The latest selected class reports prove **52 model and 15 UI/control cases**
   passing across the recorded runs. Original red/green counts and commands
   belong to the [functional evidence](../functional-tests.md#rival-first-colony-preparation-2026-10-14).
5. Accepted sources were copied to main and the owned lock released. Prompts,
   failed outputs, runtime samples, diagnoses and `acceptance.json` remain in
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r27-founding-query/`.
   This accepts a shared-model prerequisite; first-colony AI, full regression
   and publication are still pending.


### R-0027 first-colony planner — 2026-10-14

1. `2026-10-14-r27-first-colony-planner` assigned a single read-only planner.
   Attempt zero ignored the text-only mode, performed repeated source discovery
   and used inconsistent relative working directories. It had no implementation
   after **232.315 s**; the caller stopped that owned process, recorded the
   actual failed commands and supplied a shorter explicit text-only repair.
2. Repair one took **284.519 s** but used the entire 4096-token output for
   reasoning and produced an empty final file. Exit zero did not signify success.
   With only **83.166 s** left, the last targeted repair requested just the small
   site/food scoring method. It timed out after **83.469 s** without a usable
   result. Total **600.303 s**, both repair slots exhausted, **zero worker code
   accepted**. The caller then implemented the planner and retained the full
   original acceptance criteria, along with the caller-owned action integration.
3. All **537 loaded-model samples** matched the configured alias/digest and
   context **32768**. Each attempt unloaded before verification. The caller
   implementation passed shared formatting and `run-tYgNmS` (**44 model and
   3 source-contract cases**); later real-turn and UI acceptance is recorded
   in the [functional evidence](../functional-tests.md#rival-first-colony-preparation-2026-10-14).
   The job's `acceptance.json` explicitly distinguishes failed delegation from
   accepted caller implementation. The owned lock was released after those checks.
4. Evidence, original and repair prompts, runtime samples, cancellation diagnosis
   and `caller-takeover.json` remain under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r27-first-colony-planner/`.
   A complete search algorithm proved too large for this worker's output/time
   allowance in this attempt. Future handoffs should isolate one supplied
   algorithmic step before dispatch; do not spend the allowance rediscovering
   APIs already supplied. This observation does not change configured limits
   or claim savings.


## R-0028 attack-role prerequisite — 2026-10-14

1. Assignment `2026-10-14-r28-attack-roles` used isolated main revision
   `a43507d5` with the caller's role tests and refined analysis copied explicitly.
   Its scope was four production files: instance attack capability, the shared
   query/command role guard and the specialist's potential land-target domain.
   The caller retained attack snapshots, live-state validation, UI and tests.
2. Attempt 0 ended after **252.863 s** with process exit 0 but an empty final
   response. Its 4096-token output went into reasoning about whitespace and
   serialization. No code was accepted. Repair 1 supplied the same five exact
   anchors and fixed requirements, omitting the unneeded formatting discussion;
   it completed in **131.770 s** with five valid JSON replacements. Total worker
   time was **384.633 s**, one repair, within the unchanged 600-second budget.
3. Across the two attempts, **346 loaded runtime samples** matched alias
   `microcol-gpt-oss:20b-32k`, digest
   `425bbbff577bec88a59f86c437ff81eb369e011cfcd047121431a05951076fef` and context
   32768. The model was unloaded before formatting and Maven. The shared formatter
   checked five Java files, changed two and passed token/idempotence checks.
4. The original caller run `run-qbwL79` had six failed role assertions among ten
   cases. Isolated acceptance `run-qmBniN` passed all ten and the unchanged
   hostility/AI controls: **37 model/AI and 3 source-contract cases**, no failures,
   errors or skips. The caller integrated only the four approved production files
   and released the owned lock. Main's later attack-order work is separate from
   this worker result; complete R-0028 player acceptance remains pending.
5. Commands, exact anchors, original empty response, repair, runtime samples,
   diagnosis and acceptance are retained under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-r28-attack-roles/`.
   No token-saving or full-feature success claim follows from this prerequisite.

### R-0028 live AI step integration — 2026-10-14

1. `2026-10-14-r28-ai-live-step` ran against `afcdb67d` with the caller's
   recorded R-0028 work copied into its isolated checkout. The mounted-wanderer
   reproduction kept seven units instead of six: the new current-position query
   could not validate an attack from a hypothetical later path position.
2. One local attempt took **138.386 seconds** and returned one exact replacement
   combining the two supplied anchors. It performed one unnecessary bounded read
   despite the text-only instruction; it wrote no files. The caller validated
   both changes and their exact source match before applying them. All 116 loaded
   runtime samples matched the configured GPT-OSS digest and context 32768.
3. Ordinary wandering now executes one real step, checks shared combat, then
   continues while the live turn and remaining movement permit. The founding
   branch, peace policy and shared combat rules remain authoritative. Caller
   formatting passed; isolated `run-7JPXpL` passed **89 model/AI and 3 source-contract
   cases**, zero failures/errors/skips, including the failed mounted case and
   twelve first-colony planner cases. Only the accepted AI file was integrated.
4. The model was unloaded and the owned lock released. UI integration remains
   caller-owned; this result does not establish full R-0028 acceptance or
   publication. Full logs are under the task ID in the documented jobs directory.

5. Caller review extended the same assignment with a stop-between-commands case.
   The initial handoff omitted the robot-local running flag. An initial test
   attempted nested listener removal during dispatch and errored; the corrected
   seam stops the robot immediately after the real model move returns.
   `run-M8uv3z` then ran 14 cases with one valid failure: the stopped robot still
   fought (six units remained instead of seven).
6. Repair 1 took **104.907 seconds** and added the cancellation gates, but omitted
   the explicitly required existing unit-owner guard. It was applied only in the
   isolated tree and not accepted. Final repair 2 took **65.564 seconds** and
   restored that exact predicate. Total worker time was **308.857 seconds** with
   both repairs used; all 250 loaded samples matched the configured digest and
   context. These are measured run costs, not a token-savings estimate.
7. After caller formatting, `run-7VaET0` passed **94 model/AI and 3 source-contract
   cases**, zero failures/errors/skips, exit 0. Both actual-step variants passed:
   a live robot fights after approaching; a stopped robot retains its one legal
   move and performs no further attack or embark command. The final AI file was
   integrated after checking the main source still matched its handoff baseline.
   The model was unloaded and the owned lock released. Full R-0028 regression
   and publication remain separate acceptance gates.


### Requirement catalogue checks — 2026-10-14

Under [R-0050](../requirements/done/R-0050.md), the main agent retains requirement
analysis and migration. The local worker received only a bounded Python
`unittest` catalogue-consistency task, with text replacements in an isolated
worktree based on `062f8ce9ec5e1d04f85bc2f182346417a2274f6b`. No game change,
Java formatting, worker commits or worker-side tests were authorized.

Job: `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-14-requirements-catalogue/`.
The installed GPT-OSS alias/digest matches the current operating profile.
Attempt 0 (medium/64k, **525.670 seconds**) returned applicable JSON but failed
caller acceptance: multiline heading/table patterns lacked multiline matching;
the origin pattern matched three digits instead of four, and duplicate metadata
and table rows could be silently dropped. The caller’s valid minimal catalogue
probe failed on missing headings, before any test method ran.

The targeted repair (medium/64k, **391.546 seconds**) fixed multiline matching
but retained the invalid origin pattern and other requested corrections. The
same probe then failed on the valid R-0050 → R-0040 origin. This was not accepted.
The caller escalated the last allowed repair to high/64k in the same job and
worktree, preserving **917.216 seconds** already spent and the shared time/repair
budget. The repair also corrects a caller contract issue: lineage to an old
requirement applies to this historical migration, not every unrelated future
request. Future catalogue growth must not require fictional predecessor links.

The high attempt was **canceled incomplete after 1,996.219 seconds**, with
SIGTERM handled by the launcher, child exit −15 and successful model unload.
Total worker runtime was **2,913.435 seconds**, three attempts, two repair slots.
CLI usage for the completed attempts reports 1,771 input / 2,997 output tokens
and 6,741 input / 2,502 output tokens (3,661 cached input in the second). The
interrupted high attempt has no completed usage report; its token total is unavailable.
This is not a timeout, an accepted repair or a model-unavailability claim.
The caller corrected its own scope: a new permanent checker was an unnecessary
addition to this documentation-only migration. Existing mandatory checks and a
direct audit of all catalogue records satisfy the requested deliverable without
adding this rejected prototype to the repository. No worker code was integrated,
no replacement cloud implementation was written, and no required check was waived.
No further retry is requested for the withdrawn task; the original failures remain
unresolved in the isolated candidate, rather than being reported as fixed.

The job retains `disposition.json`, attempt records and
`probe-before.txt` / `probe-after-repair1.txt`. The main-agent documentation audit
and passing project checks are recorded in [R-0050](../requirements/done/R-0050.md).
No measured cloud-token saving or general model-reliability conclusion follows
from this unsuccessful optional assignment. Future documentation-only tasks
should reuse existing validation and a focused audit before proposing new tooling.


## R-0112 diagnostic hypothesis — 2026-10-15

1. The caller delegated four event-entry tests as job
   `2026-10-15-r112-entry-regression`, baseline `99e6c14b`, medium/64k. Attempt 0
   took **710.207 s**, reported **3805 input / 3862 output tokens**, and returned
   malformed replacement JSON. Application was rejected without a source edit.
   Review also found invalid imports and missing exact-state assertions; the
   second case could incorrectly pass because it checked only after a subsequent
   movement event. A focused repair retained the job, worktree and budgets.
2. Two caller errors are retained in the job review: a test command was started
   despite rejected application (**5 existing MouseOverTileManager + 3 page-contract
   checks**, no generated test executed), and a review filename beginning with
   `attempt-` collided with the launcher's directory enumeration. Renaming that
   note corrected preflight before any repair inference. Neither is a successful
   worker verification or an application repair.
3. The native full-class trace then captured all **187 movement events delivered
   with input disabled**. The caller withdrew the missing-movement hypothesis
   and canceled repair 1 through the owned launcher (SIGTERM), after **575.475 s**;
   child exit **−15**, model unload **0**, cumulative **1285.682 s**. Interrupted
   token usage is unavailable. No worker code was applied or integrated, and no
   further retry/takeover of that withdrawn hypothesis was needed. The original
   output defects remain unresolved. `disposition.json` and all attempt evidence
   remain in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-15-r112-entry-regression/`.
4. This host check observed Codex CLI **0.162.1** and Ollama **0.40.2** with the
   configured model digest/context. CLI warnings about ignored reasoning-summary
   metadata did not prevent inference. No model/runtime setup was performed and
   no token savings are claimed. The confirmed turn-gate regression is a separate,
   narrowly specified task; actual acceptance is recorded after caller execution.

5. The confirmed-owner task `2026-10-15-r112-turn-gate-tests` used baseline
   `cc9d5cbf`, medium/64k, with caller-owned constructor wiring, application fix
   and native UI reproducer. Worker scope was only four unit cases. Attempt 0
   delivered valid replacements in **561.455 s**, **2165 input / 4322 output
   tokens**. The negative control exposed three expected owner failures and a
   caller-handoff error: Mockito inline was available in `microcol-test`, not
   `microcol-game`, so a mocked final movement event failed before that check.
6. The caller supplied exact real-event constructors for one-method repair.
   Repair 1 took **292.214 s**, **1543 input / 1380 output tokens**, preserving the
   same job/worktree/budget; cumulative **853.669 s**, one repair. Both batches
   applied through the validated launcher. Shared formatting preserved code tokens
   and idempotence. After model unload, caller Docker checks passed **4 new gate +
   5 existing hover tests, 3 page-contract checks and 4 player scenarios**, all
   without failures/errors/skips. The worker did not run tests or change application
   code. The reviewed test was integrated; wider R-0112 acceptance remains recorded
   in the [functional evidence](../functional-tests.md#r-0112-map-input-repair-2026-10-15).
   Full logs and `acceptance.json` remain under the job directory. No measured
   cloud-token savings are inferred from these runtimes.

## R-0113 CI filtering — 2026-10-15

1. Job `2026-10-15-r113-ci-filter` uses baseline `b2b3ec14`, GPT-OSS medium/64k
   and four assigned files: the change classifier, its Git-fixture tests and the
   two game workflows. The caller owns rules, documentation, review and all
   verification. [R-0113](../requirements/done/R-0113.md) retains the full acceptance.
2. Attempt 0 delivered no assigned source edits. Its CLI reported an idle SSE
   timeout and then a stream closed before `response.completed`; the inference
   server remained active. The underlying cause is unproven. After repeated
   failures without edits, the caller interrupted the owned launcher. Exit was
   `-15`, cleanup unloaded the model successfully, and **2108.077 seconds** count
   against the same task budget. This was a caller interruption before the
   deadline, not a successful result or a deadline timeout.
3. Repair 1 retains medium, the same job/worktree, original behavior and all
   checks. The corrected handoff requires small separate write calls and narrow
   source reads, testing the hypothesis that long buffered output contributed to
   the connection failures. No runtime or model setting changed. Repair 1 also delivered no source edits and reported repeated streams closed
   before completion. The caller verified unchanged source hashes and stopped
   the owned launcher after **595.370 seconds**; exit `-15`, clean unload. The
   cumulative worker time is **2703.447 seconds**. The hypothesis is unconfirmed,
   and no worker code has been accepted.
4. Final repair 2 uses the authorized high/64k profile and tool-free exact
   replacements for nine caller-owned anchors in the same four files. This
   removes write-tool calls from the attempted delivery path without changing
   behavior, tests, worktree, model digest or cumulative budget. The launcher
   records the diagnosed escalation. No repairs remain after this attempt;
   the caller stopped it after **3558.740 seconds** without a completed response
   or applicable patch, to fulfill the user's publication request and release
   resources for verification. Exit `-15`, successful model unload; cumulative
   **6262.187 seconds**. This was an intentional interruption, not a runtime
   crash or deadline timeout. No underlying stream failure was observed in this
   final attempt. Both repair slots were consumed. The caller implemented the
   four-file scope directly; no worker implementation was applied or accepted.
5. Prompts, stream errors, runtime observations and the caller diagnosis remain
   under `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-15-r113-ci-filter/`.
   The existing main checks passed 21 launcher and 9 documentation tests. Strict
   MkDocs passed after the caller implementation. Its 13 Git-fixture/CLI tests
   and workflow preservation check passed. These local checks do not prove live
   filtering/publication. Full logs remain local; no token savings are claimed.

## R-0052 ordering attempt and publication handoff — 2026-10-15

1. Job `2026-10-15-r52-national-order`, baseline `40faafe3`, assigned only
   `GameManager.java` through six exact replacement anchors. Medium/64k,
   GPT-OSS 20B with 96k context, returned valid JSON in **1141.720 seconds**,
   **3670 input / 9933 output tokens**; exit 0 and successful model unload.
   The caller applied the batch in the isolated worktree and formatted it.
2. The output was not accepted: it interleaved Crowns with nations, emitted
   turn-finished before rejecting a removed current actor, omitted registration
   from the shared actor guard and removed the resume round notification. The
   corrected-fixture isolated Docker run executed **35 model cases**, with
   **4 failures and 1 error**, no skips; the UI module did not execute.
   One failure was an old game-over fixture's assumption that Spain is last;
   the error was a caller fixture reversing an immutable saved-player list.
   The other three failures directly exposed the worker's ordering/guard errors.
   Review separately identified the missing resume notification.
3. Caller setup also needed correction: the first check ran from main instead
   of the worker checkout, and earlier roster mutations targeted the immutable
   `Model.getPlayers()` copy. These are not worker defects. The final caller
   fixture mutates `PlayerStore` for live-roster tests and copies the saved
   list before reversal. Handoff claims about mutable `Model.getPlayers()` and
   `PlayerPo` display-name access were inaccurate; future handoffs must verify
   the actual mutation/access APIs before dispatch.
4. The user then requested publication of all changes. The caller retained
   the completed attempt and rejected patch as evidence, released its lock,
   and took over the small ordering repair directly to finish that request
   without another long inference attempt. No repair was dispatched and no
   escalation occurred; two repair slots remained. This is an explicit
   publication-scope takeover, not a claim that the repair budget was exhausted.
   Main also owns the small Player/Ship stage split and all acceptance tests;
   none of the worker patch was copied into main.
5. Runtime logs and rejected output remain under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-15-r52-national-order/`.
   Caller before/after checks are in `target/r52/`; accepted results are recorded
   in [functional evidence](../functional-tests.md#r-0052-partial-turn-order-delivery-2026-10-15).
   No token savings are inferred. R-0052 remains open for preparation-time
   save/action exclusion and failure recovery.

## Turn-preparation boundary worker — 2026-10-15

1. Job `2026-10-15-r52-preparation-boundary`, baseline `57c7bb4f`, assigned
   only the Model/GameManager lifecycle and snapshot boundary. Medium/64k
   attempt 0 took **1121.613 s** (3244 input / 10592 output tokens). The
   replacement batch applied in its isolated checkout, but review rejected
   recursive BeforeEndTurn entry and a changed RoundStarted order. It also
   added unnecessary save state and catch/rethrow blocks. No test acceptance
   was claimed for that candidate.
2. Targeted repair took **502.045 s** (2401 / 4232 tokens), **1623.658 s**
   cumulative. Its Markdown response failed the JSON application contract;
   no repair text was applied. The caller's shortened repair prompt omitted
   the explicit JSON example, contributing format ambiguity. Source review
   also found forbidden finally-clearing of preparation after failure, an
   early readiness reset and removed actor validation. Future repairs must
   repeat the exact response schema and failure invariant.
3. To complete the user's publication request after these two rejected
   results, the caller corrected the small lifecycle methods directly. One
   repair slot and runtime budget remained; this was an explicit completion
   decision, not exhausted budgets. No high escalation or cloud worker was
   used. The accepted scope derives from attempt 0 plus caller corrections;
   attempt 1 contributed no source. Both attempts unloaded the model before
   Java verification. Main owns command guards, internal action completion,
   GUI recovery, tests, formatting and actual acceptance.
4. Full job records remain under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-15-r52-preparation-boundary/`;
   caller checks remain in `target/r52/boundary-*.log`. No token saving is
   inferred from these measurements. Verification is recorded with the
   [functional evidence](../functional-tests.md).


## Unit turn orders worker — 2026-10-15

1. Job `2026-10-15-r53-unit-orders`, baseline `398df486`, assigns only the
   model query/command, normal selection and their tests. Main owns requirement
   refinement, GUI integration and acceptance. Medium/64k attempt 0 took
   **559.222 s** and unloaded successfully. Its validated replacement batch
   applied in isolation; shared formatting preserved tokens and was idempotent.
2. Review rejected reversed `isNoAction` filters, owner equality instead of
   identity, and tests with post-state self-comparisons. Docker acceptance then
   failed compilation: missing Optional import, Unit passed to a Model-only
   snapshot helper, invented Model accessors and an invalid event constructor.
   These APIs and the intended assertions had been specified in the handoff.
3. The caller retained the same job/worktree and dispatched focused repair 1
   with exact current anchors, corrected APIs and unchanged acceptance. This
   escalates to high/64k under the normal policy because medium lost multiple
   semantic and test requirements. Prior elapsed time remains 559.222 s;
   the shared limits and two-repair budget are not reset. Evidence is under
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-15-r53-unit-orders/` and
   `target/r53/`. No token savings are claimed.

4. High repair 1 did not return a patch before the caller stopped its owned
   launcher to complete the explicit publication request. It ran **1781.944 s**,
   child exit **−15**, successful unload **0**, **2341.166 s** cumulative.
   This was a deliberate completion decision, not a timeout or exhausted budget;
   one repair slot remained. No high-attempt source was applied and its completed
   token usage is unavailable. Continued waiting had no reviewable output.
5. Main corrected the query identity/null checks, positive no-action filters and
   the invalid test methods in the isolated candidate, retaining the worker's
   command and usable test fragments. Main then copied its separately authored
   GUI integration into that checkout and ran combined model/UI acceptance after
   unloading and formatting. This is caller-completed work, not an independently
   accepted worker implementation. The job retains `disposition.json` and both
   attempts; actual check results belong in [functional evidence](../functional-tests.md).

6. First caller-completed run compiled and executed **40 model cases**, with
   **one error** and no failures/skips. The not-started fixture tried `Model.save`,
   which correctly rejects an unstarted game. This came from the caller's
   handoff/acceptance setup, not a gameplay defect. That case now compares the
   unit's persisted state and the non-running boundary directly; other rejected
   states retain full-model before/after comparison. The unchanged combined
   selection was rerun after shared formatting.

## Coastal movement acceptance worker — 2026-10-15

1. R-0054 assigns `CoastalPortMovementTest` to a single medium/64k local job
   based on `77dd824a`. The caller supplies a current-API fixture scaffold;
   allowed output is one validated full-file replacement, with no production
   changes. The caller owns refinement, F30 and TC38 player paths, review,
   formatting, actual Docker acceptance and publication.
2. Job and raw evidence: `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-15-r54-coastal-movement/`;
   isolated checkout: `~/Library/Application Support/Microcol/local-worker/worktrees/2026-10-15-r54-coastal-movement/`.
   The launcher verifies the configured alias and observed context 98304.
3. Medium completed in **866.448 seconds**, exit 0 and unload 0, reporting
   **7375 input / 4841 output tokens** (4258 cached input). Validated application
   and shared formatting succeeded, but Docker `run-mYtKW9` failed test
   compilation with **28 errors; zero tests ran**. The candidate omitted its
   package and invented movement APIs. Review also found cyclic live-object
   serialization, wrong moving actors, unpaired border cases, stale fixture
   reconstruction and tautological passenger comparisons. No model test was
   accepted or integrated into main.
4. The caller corrected a separate scaffold defect before repair: terrain changes
   could leave tobacco workers on water. The fixture now uses compatible corn
   production and removes trees/fields from its sole water square. This does
   not repair the worker's test methods. The focused repair supplies verified
   APIs and complete method anchors while preserving the original checks.
5. Attempt 1 completed at high/64k in **5044.344 seconds**, exit 0 and unload 0,
   reporting **9619 input / 42911 output tokens**. Cumulative worker time is
   **5910.792 seconds**. Validated application and shared formatting passed,
   but Docker `run-DyLrPw` again failed compilation: **17 errors; zero tests ran**.
   The repair retained nonexistent live-unit place APIs, confused file paths with
   movement paths and used the wrong `UnitType` package. The incorrect movement,
   snapshot and stale-state setups also remained. Neither candidate is accepted.
6. The final permitted repair was dispatched on **2026-10-15 at 21:11 UTC**
   in the same job. Its input removes
   the rejected test bodies as examples and supplies a clean fixture plus
   persistence, identity and rejection helpers. Those helpers are caller-owned;
   the worker must regenerate the same seven test methods (14 invocations).
   This is a concrete prompt correction, not a smaller acceptance scope. It
   retains high/64k, a 7200-second attempt cap and **8489.208 seconds** of the
   original cumulative budget. No budget or repair count is reset. The caller
   synchronized the five published game-screen/UI/F30 files from `8b44b3d5`
   into the existing isolated checkout before dispatch; worker edits remain
   limited to the model test. No token saving is claimed.
7. Attempt 2 completed in **4379.082 seconds**, exit 0 and unload 0, reporting
   **4261 input / 26870 output tokens**. Total worker time across three attempts
   is **10289.874 seconds**. Validated replacement application and shared
   formatting passed. Review found a reversed inland-sailing assertion; the
   original combined Docker selection is being run to expose any additional
   defects. Both permitted repairs are now exhausted, so any necessary caller
   corrections will be recorded separately and checked before integration.

8. Docker `run-GIhSyH` failed compilation with **one error; zero tests ran**: the
   caller-prescribed `Model.createUnit` is package-private and unavailable from
   the AI test package. The caller corrected that fixture by rebuilding from
   the current `model.save()` plus the enemy DTO, preserving the post-movement
   ship state and resolving all units from the new model. The caller also
   corrected the worker's reversed inland predicate assertion. The original
   selection is rerun; neither correction reduces acceptance scope.

9. Docker `run-VA0WGN` compiled and ran **39 model checks: 38 passed, one
   error, no failures/skips**; UI execution was not reached. The caller scaffold
   incorrectly gave a foot colonist two points despite its speed of one. The
   caller now uses one point for each of the eight outward steps, verifies that
   exhaustion rejects the return without mutation, then supplies a fresh legal
   allowance to verify the return costs one point too. Ground terrain/border
   setups also use the legal allowance. This corrects an impossible fixture,
   retains both directions and adds the exhausted-step boundary.

10. Docker `run-si7LcR` ran **39 model checks: 38 passed, one failure, no
    errors/skips**. The added exhaustion assertion expected the wrong exception
    type. `Model.moveUnit` documents `IllegalStateException`, and
    `AbstractUnit.moveOneStep` uses `checkState` for insufficient movement. The
    caller aligned that assertion with the existing contract; terrain/boundary
    assertions retain `IllegalArgumentException`. Full-state preservation stays
    required. The same combined selection is rerun again.

11. Docker `run-Zqjuc6` passed the unchanged combined selection: **39 model
    checks** (14 new coastal cases plus 25 existing checks) and **16 UI-module
    checks**, zero failures/errors/skips, exit 0. The caller reviewed and
    integrated the final test file after shared formatting. Accepted after
    worker generation plus the explicitly recorded caller corrections; full
    CI regression/publication of the added model tests subsequently passed in
    `df25024d` / `dev-94-df25024d4b3e`; see the
    [complete evidence](../functional-tests.md#r-0054-coastal-port-movement-2026-10-15).

## Shared-field persistence worker — 2026-10-16

1. R-0055 is selected and refined. The caller assigned its bounded saved-assignment validation and ten real-model persistence tests to `2026-10-16-r55-field-persistence`, based on main `03437f99`. The isolated tree explicitly includes the caller’s three requirement/rule edits and test scaffold. Live assignment and UI changes are outside this assignment.
2. Attempt 0 used the ordinary medium/64k profile and finished in **115.558 seconds**, exit 0 and successful unload. It returned the two literal descriptions from the caller’s JSON example instead of Java. The replacement validator applied the syntactically valid JSON, but caller diff review rejected both replacements before formatting or compilation: **0 tests ran**, and no production change was integrated.
3. The caller restored both exact source anchors and removed the ambiguous example values, explicitly requiring complete Java implementation and test bodies. The same job/profile is retrying with the original acceptance scope and cumulative budget. This is a caller prompt correction, not accepted worker code or evidence of gameplay completion. Subsequent application, formatting and test results remain pending.
4. Attempt 1 took **778.012 seconds**, exit 0 and successful unload; cumulative worker time was **893.570 seconds**. Caller source review rejected invalid Java boundaries/imports and invented APIs before compilation (**0 tests run**). More seriously, the draft required indoor workers to have outdoor places while omitting outdoor ownership and orphan-reference validation. The source and rejection diagnosis are retained in that attempt directory; no production change was integrated.
5. The caller restored the original anchors and used the final allowed repair with high/64k, explicitly recording the medium failure as the escalation reason. The handoff now supplies a single-pass validation algorithm, exact API spelling and an entire-test-file anchor to avoid duplicate class boundaries. The caller also fixed its own immutability helper to compare the parsed DTO actually passed to loading. Acceptance scope and the cumulative budget remain unchanged; the final repair and its checks are pending.
6. Future handoffs should describe the JSON shape without putting plausible literal prose in its `new` values. Source-anchor validation protects application identity and location; it does not prove that a returned value is usable source. Logs, prompts, rejected output and the caller review remain in `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-16-r55-field-persistence/`.

7. During the final repair the caller found another handoff error: the source scenario contains only Dutch and the Dutch king, not the claimed France player. The foreign-owner tests must use the registered king player, so their rejection proves the assignment rule rather than unknown-owner lookup. The caller will correct this fixture detail after generation and verify the original cases; the running attempt and its frozen prompt are retained.

8. While the separate persistence generation runs, the caller retained the short Load/Continue integration and the shared live assignment boundary (existing turn, identity, source-place and equipment preconditions) for direct architectural integration. The caller drafted four controller tests and two initial real-model assignment tests. This does not duplicate the worker’s saved-DTO validator or reset its repair budget. Formatting, compilation, remaining boundary tests and player acceptance are pending; these drafts are not attributed to the local worker.

9. The user then explicitly requested publication of all current changes. The caller stopped the owned launcher to prioritize verification of that checkpoint: attempt 2 ran **5547.786 seconds**, child exit **−15**, successful unload **0**, cumulative **6441.356 seconds**. This was deliberate cancellation, not a timeout or a completed high response. No attempt-2 output was applied; no worker implementation was accepted. Logs and the isolated tree remain available, and the owned lock was released. Saved-assignment validation and its acceptance remain unfinished under R-0055; no new job was created to reset the repair allowance.
10. For the checkpoint, the caller completed the small UI integration against its live model query, using the existing colony status bar and capturing the crop menu's expected worker. It added the initial TC_39 shared-square player path. The first caller test run failed compilation because this repository's Mockito version uses `verifyZeroInteractions`, not `verifyNoInteractions`; **zero tests ran**. The caller corrected that API and reran the checks. These changes are caller-owned, not worker output or evidence of full R-0055 completion.

11. Subsequent caller checks exposed a second test setup defect: this Mockito configuration cannot mock the final campaign/loader classes. The caller replaced those mocks with real campaign/loader construction and temporary files. All four load-controller cases then passed. UI compilation later exposed absent parameterized-test support in that module; two ordinary named tests preserved both paths without adding dependencies. Actual passing counts and remaining R-0055 scope are recorded in the linked functional evidence.

12. On goal continuation after the checkpoint publication, the caller takes over the unfinished persistence validator and its tests because both permitted repairs were consumed and no implementation was accepted. This does not restart the exhausted job. A separate medium/64k job, `2026-10-16-r55-live-boundaries`, based on `af77a0c5`, is limited to six new real-model tests of the already-published live assignment guards in `SharedColonyFieldBoundaryTest`. It does not duplicate persistence work. The caller owns saved-data validation, its input/file immutability tests, UI integration, review, formatting and actual verification; none of these newly drafted checks is yet a passed result.

13. The independent live-boundary worker's medium attempt 0 completed in **1057.247 seconds**, exit 0 and unload 0. Replacement application rejected the wrong `content` key; static review additionally found DTO/live-unit confusion, invented imports/accessors, cyclic live-object serialization and a false before/after comparison. **No worker source was applied and zero worker tests ran.** The caller renamed its scaffold's DTO helper to `savedUnit` and prepared a targeted high/64k repair with exact live-unit APIs and unchanged six-test acceptance. This repair retains the same worktree, baseline and cumulative budget. Caller-owned persistence/UI verification runs while the model is unloaded, before redispatch.

14. The user requested another publication checkpoint before dispatch of the prepared high repair. No second live-boundary attempt was started; its existing job, cumulative time and remaining repair allowance are preserved. Caller acceptance passed 108 model/controller and 12 UI-module checks as recorded in [functional evidence](../functional-tests.md#r-0055-completion-work-2026-10-16). Worker-generated coverage remains unfinished and is excluded from this checkpoint.

15. After verified checkpoint publication, the caller dispatched the prepared live-boundary repair with high/64k in the same job/worktree, retaining 1057.247 seconds of prior inference and both original limits. The recorded escalation reason is the diagnosed DTO/live-unit confusion and missing semantic assertions. The full local Docker regression had finished before dispatch; no Maven/Docker work runs concurrently with inference. Application, review and tests for this repair remain pending.

16. Live-boundary high repair 1 finished in **6182.077 seconds**, exit 0 and unload 0; cumulative inference is **7239.324 seconds**. Reported usage was 2777 input and 56651 output tokens; no savings claim follows from those figures. The replacement JSON applied successfully only in the isolated tree. Caller static review rejected over-escaped Java literals, a warehouse-stock assertion used instead of field yield, an unequipped positive gun/horse fixture, and a nonexistent `Model.getModel()` accessor. **Zero tests ran and no worker source was integrated.** The final allowed high repair targets six non-overlapping fragments, preserving correct cases and the same job/worktree. Its attempt is capped by the remaining **7160.676 seconds**; there is no budget reset.

17. Final live-boundary high repair 2 finished in **3176.702 seconds**, exit 0 and unload 0; cumulative inference is **10416.026 seconds**. Usage was 2707 input and 27866 output tokens. Its six replacements applied only in the isolated tree, and the shared formatter passed token preservation and idempotence. The selected Docker run `run-EqZh8p` failed test compilation: `ModelPo` has no `setCurrentPlayer`; the saved turn belongs to `getGameManager()`. **Zero tests executed.** The caller had missed that API error while narrowing the final repair anchors. Both repair allowances are consumed, so the caller takes over this one-line fixture correction in the same tree, preserves the rejected source and compiler evidence, and reruns the same acceptance selection. No worker source is accepted until those checks pass; the task budget is not reset.

18. After the caller's setter fix, isolated Docker `run-8Jrkdj` compiled and ran **43 model checks: zero failures, one error, zero skips**. The wrong-owner fixture failed before the action because it placed a king-owned worker on the same map square as Dutch units. The caller moves those other map occupants to the neighboring colony center before loading, retaining the foreign worker at the tested colony center and preserving the ownership-rejection and unchanged-world assertions. Descriptive test names replace the draft names; no acceptance case is removed. The same selection is rerun before integration.

19. Isolated Docker `run-Ki7iFq` passed all **43 model checks**, including the six worker boundary tests, with zero failures/errors/skips. The overall runner still failed because the caller's selection included no UI-module tests; this is a selection error, not a passing full command. The caller copies only `SharedColonyFieldBoundaryTest.java` to current `main` and reruns verification with the page source contract and affected player paths included, alongside the current saved-field and founding checks. No old production files are integrated from the worker baseline.

20. Current-main Docker `run-gAixgo` passed **106 model/controller and 12 UI-module checks**, zero failures/errors/skips, verified from XML. The six new boundary tests retain every intended case; caller changes fix the saved-turn receiver and invalid enemy-stack setup and improve test names. The shared formatter passed token preservation/idempotence. This completes local acceptance of the one test file; full CI/publication and R-0055 closure remain pending. The worker job ends with its original **10416.026-second** cumulative inference record and exhausted repair allowance; no new job resets this task.

### R-0056 indoor admission — 2026-10-16

1. After R-0055 publication, the caller dispatched the independent
   `2026-10-16-r56-indoor-boundaries` job from `main` revision
   `d0dbb37b1eba22f5a36b0806c32fc985d3daffd6`, using the default medium/64k
   profile and its unchanged 7200/14400-second limits and two-repair allowance.
   Seven caller-owned anchors cover four model files and one new real-model test
   class. Scope is shared indoor/outdoor worker admission and atomic equipment
   rejection, preserving founding and saved-job restoration. This is one part of
   [R-0056](../requirements/done/R-0056.md), not acceptance of its job picker.
2. Dispatch observed Codex CLI **0.162.1**, rather than the historical 0.142.3
   installation above. The CLI reports the retired
   `model_supports_reasoning_summaries` setting as ignored and emits telemetry
   warnings for the local alias. The expected local model digest and 98304
   context are loaded, and the request's output stream remains active. These
   warnings alone do not establish an inference failure; no restart or profile
   change was made. Preserve the actual outcome before deciding on a tooling
   repair.
3. Generation, caller review and model acceptance are pending. The caller
   separately prepared the small English/Czech picker text and traced its three
   UI entry points; these do not duplicate the worker's model implementation.
   Java formatting and Docker verification wait until inference unloads. Logs,
   frozen prompt, anchors and eventual result belong to
   `~/Library/Logs/Microcol/local-qwen/jobs/2026-10-16-r56-indoor-boundaries/`.
   No runtime or token-savings result is claimed for this unfinished attempt.

4. Attempt 0 finished with exit 0 and successful unload after **1030.897
   seconds**. Atomic application rejected the unchanged `colony-admission`
   replacement, so no worker source was applied. Static review also found
   missing shared predicates, a literal invalid `package-private` Java modifier,
   dropped field guards and calls to undefined test methods instead of the eight
   requested cases. **No Java tests ran on this rejected batch.** The failure is
   incomplete source generation, not established runtime failure. The caller
   preserved the response and diagnosis in `attempt-0/caller-review.txt`.
5. The first repair uses the documented medium-to-high transition in the same
   job, with exact source anchors and the same acceptance scope. Its focused
   prompt requires complete shared predicates, retained field/crop guards and
   eight real test methods; the caller again supplies the actual saved-turn and
   valid wrong-owner fixture APIs. Prior worker time remains **1030.897
   seconds**, leaving **13369.103 seconds** of the original task budget. The
   repair is capped at 7200 seconds, with one further repair available after it;
   no budget or baseline was reset. Application and verification remain pending.

6. Attempt 1 reached its 7200-second deadline without a response file. The
   launcher terminated it with exit -15 and unloaded successfully: **7200.745
   seconds**, cumulative **8231.642 seconds**. Events contain startup warnings
   and `turn.started`, but no completed response; the telemetry stream remained
   active until termination. There is no returned source to diagnose as a Java
   error, and the internal generation cause is unknown. No patch or Java test
   result was produced. The caller preserves this distinction in
   `attempt-1/caller-review.txt`.
7. The final repair narrows generation to the four production files and six
   anchors. The caller takes over the eight boundary tests because the combined
   source-and-test repair did not return within two hours; the final worker
   prompt explicitly excludes that test file, avoiding duplicate implementation.
   Test coverage and the original acceptance selection are unchanged. The same
   high profile, job and baseline retain **6168.358 seconds** of task budget,
   which caps this final attempt; no repair allowance remains after it. The
   caller has written all eight real-model cases in the isolated tree, with
   formatting and execution pending worker unload. This is authored source,
   not passing acceptance evidence.

8. Attempt 2 returned with exit 0 and successful unload after **5529.990
   seconds**, cumulative **13761.632 seconds**. Its six replacements passed
   atomic application, but static review found nonexistent `colony` variables in
   `Colony` and `ConstructionSlot`, an incorrect field wrapper in place of the
   shared actor predicate, and a missing place-kind check in the field identity
   helper. Application validation therefore did not establish usable Java or
   correct behavior. The generated diff and diagnosis are preserved in
   `attempt-2/generated.diff` and `attempt-2/caller-review.txt`.
9. Both repair allowances are exhausted. The caller corrects those specific
   source errors in the same isolated tree and retains its eight independent
   boundary tests. The shared formatter passes exact-token preservation and
   idempotence for all five changed/new Java files. The required Docker test
   selection is running; no passing Java acceptance is claimed yet. There is no
   new job or budget reset for this work, and no measured savings claim.

10. Caller verification passed in Docker after unload: the focused selection
    (`run-5jiemY`) ran **186 model and 3 page-contract tests**; the broader
    `-Dtest=*Test` regression (`run-Zy6A7s`) ran **1279 game and 9 UI/control
    tests**, all with zero failures, errors or skips and exit 0. All eight new
    indoor-boundary cases ran. The latter selection does not include `TC_...`
    player scenarios and is not acceptance of the picker. Reviewed and formatted
    changes in four production files and the new test were integrated into main;
    the owned lock was released. The caller retains the subsequent state-sensitive
    choice design/implementation because this boundary task exhausted both repairs
    with incomplete validation code; suitably bounded presentation work remains
    eligible for local delegation. Full R-0056 acceptance is still pending.


### R-0056 additional player scenarios — 2026-10-16, in progress

1. After the first seven picker scenarios passed, the caller dispatched
   `2026-10-16-r56-choice-player-scenarios` from main baseline `d0dbb37b` with the
   current uncommitted R-0056 sources, tests and docs explicitly copied into its
   isolated tree and recorded in `copied-inputs.json`. This distinct task authors
   four linear TC40 cases using fixed page APIs: unavailable outdoor work, full
   indoor building, current-job no-op and building-body drag fallback. It does
   not retry or reset the exhausted indoor-boundary job.
2. One replacement anchor permits only those four test methods. Production,
   page objects, fixture and existing tests are caller-owned; no duplicate
   implementation is assigned. Default medium/64k, 7200-second attempt,
   14400-second task and two repairs apply. Generation, review, formatting and
   actual acceptance remain pending. Java/Docker verification is serialized after
   inference unload; the caller continues documentation and source review.

3. Attempt 0 returned the four intended player scenarios after **905.722 seconds**,
   exit 0 and unload 0. Application rejected a missing final outer JSON brace
   (EOF at character 3728), so no generated source was applied and no Java
   acceptance ran. Diagnostic parsing confirmed complete Java bodies and normal
   newlines; the defect is the response envelope. The caller mistakenly invoked
   formatting after rejection; this formatted only the copied scaffold, not the
   generated patch. An initial stale-anchor repair preflight was rejected before
   dispatch; no attempt or inference was consumed. The caller corrected the full
   current method anchor. Repair 1 requests only a valid complete envelope with
   the same four methods, retaining medium/64k and the original job and limits.

4. Repair 1 completed after **395.005 seconds**, exit 0 and unload 0; cumulative
   worker time **1300.727 seconds**, one repair used. Atomic application succeeded.
   The Java differs from the initial response only by a trailing newline; all four
   scenarios remain intact. Shared formatting passed token preservation and
   idempotence (18 files checked, one changed). Caller Docker acceptance is running
   in the isolated tree; integration and final R-0056 acceptance remain pending.

5. Caller Docker acceptance `run-PuzAgH` passed **40 model and 14 UI tests**
   (11 TC40 player scenarios and three page-contract checks), zero failures,
   errors or skips, exit 0. Only worker-authored TC40/08–11 were integrated into
   main, preserving the caller's subsequent drag-race tests and source fixes.
   The owned lock was released. Main formatting passed; integration acceptance
   and complete R-0056 regression/publication remain separate checks. The local
   job used one repair and **1300.727 seconds** of inference; no measured
   cloud-token saving is claimed.


## Cloud Sol low batch — 2026-10-16

The user explicitly selected R-0057–R-0061 and replaced local inference with
cloud Sol for this batch. The main agent remained **Astra high**; every worker
used **gpt-6-sol / low**, sequentially in one isolated worktree. No medium/high
worker escalation or local model call was needed. This override does not change
the ordinary local medium/64k → high/64k policy.

| Assignment | Input tokens | Cached input (included) | Uncached input | Output tokens | Worker elapsed time |
| --- | ---: | ---: | ---: | ---: | ---: |
| R-0057 + R-0058, interactive | 11,373,434 | 11,216,256 | 157,178 | 31,173 | 998 s |
| R-0059, interactive | 9,179,147 | 9,078,016 | 101,131 | 28,140 | 566 s |
| R-0060, interactive | 16,006,459 | 15,871,744 | 134,715 | 36,016 | 724 s |
| R-0061, four text-only calls including context corrections and review repair | 143,341 | 4,224 | 139,117 | 11,517 | 119.315 s |

These are measured worker counters, not subscription charges or a comparison of
equivalent tasks. Cached input is part of input, not an additional total, and
is not assumed free. Interactive elapsed time spans first-to-last worker events
and includes its tools; text-only time measures calls and excludes main-agent
application, review and tests. Main-agent tokens and CI time are additional.
At the main-agent checkpoint **2026-10-16 13:20:12 UTC**, Astra's delta since the saved
baseline was **24,446,861 input** (24,176,256 cached; 270,605 uncached) and
**75,364 output tokens**. It includes main review, verification and CI
monitoring up to that checkpoint; initial reads and final documentation closure
are excluded. This is a lower bound for main orchestration, not a task bill.
Main-agent work is a material part of this experiment's cloud use.
There is no Astra-only control run, quota-per-task counter or measured saving
percentage. Other concurrent threads share account limits.

Sol low delivered useful implementation and tests but needed caller review and
targeted repairs. The interactive workers repeatedly carried large cached
contexts. R-0061's bounded text-only approach avoided that repeated interactive
history, but two initially incomplete caller packets cost calls without code:
one omitted permission to update contradictory existing assertions, another
omitted fixture and warehouse/statistics context. A complete packet produced the
patch; a short review repair centralized the input calculation. This supports
trying bounded Sol low assignments with explicit APIs, acceptance and limited
repair context; it does not establish that every new task will be cheaper.

[Functional evidence and diagnosed failures](../functional-tests.md#r-0057r-0061-cloud-sol-batch-2026-10-16)
own the acceptance results. Full integrated regression passed (1325 game and 187 UI checks, zero failures/errors/skips); the same counts passed in CI, and [dev-109-0b0cb0593df4](https://github.com/jajir/microcol/releases/tag/dev-109-0b0cb0593df4) was published and verified. Raw counters and CLI call evidence are retained
locally in `target/r57-r61/`; no new worker infrastructure or default setting was
committed for this experiment.


## Continuing cloud run — 2026-10-16

1. The user authorized all remaining R-0062–R-0111, adopting Astra medium with
   bounded Sol low implementation and hourly reports. The current main session
   still reported `gpt-6-astra`/`high` when inspected; the user was asked to switch
   the thread setting. Do not report medium usage until observed. The hourly
   thread heartbeat `microcol-hodinov-postup` is active.
2. R-0062 model work used `gpt-6-sol`/`low` in isolated worktree
   `2026-10-16-r62`, based on `f15adba0`. One worker was active; no local model
   was invoked. Caller-owned UI/fixture work did not duplicate its implementation.
   Two targeted review follow-ups strengthened worker-count, upgrade, national
   boundary and exact bell assertions; no execution failure occurred in the
   selected model checks. Actual caller verification is in the
   [functional evidence](../functional-tests.md#r-0062-automatic-cross-production-2026-10-16).
3. Token/quota savings for this run have not been measured. The workflow choice
   is an economy hypothesis, not a verified percentage saving.

4. R-0064 used one `gpt-6-sol`/`low` worker in `2026-10-16-r64`, based on
   `6cf31ea5`. Main handled fixtures, UI tests, requirements and verification.
   A targeted review follow-up made queued UI text use a captured model value
   and added bundled-default coverage. Worker model tests passed; caller test
   failures and repairs are separately recorded in the
   [R-0064 evidence](../functional-tests.md#r-0064-difficulty-profile-2026-10-16).

5. After the user confirmed the switch on 2026-10-16, the current session's
   `turn_context` was inspected and reported `gpt-6-astra`/`medium`. R-0108
   continues with this main setting and one bounded `gpt-6-sol`/`low` worker
   in `2026-10-16-r108`, based on `db946d43`. Main owns GUI and actual checks;
   worker owns the model roster and focused tests. A review follow-up added
   exact voyage/passenger and rejection coverage before acceptance.
6. R-0108's first worker test execution found a scenario-terrain setup error
   (six of seven roster tests passed). A targeted repair in the same worktree
   applied the terrain and exercised the model command; the complete model
   rerun passed. The caller separately repaired its unsupported UI parameter
   annotations. Details and remaining acceptance belong to the
   [R-0108 evidence](../functional-tests.md#r-0108-six-ship-roster-2026-10-16).
7. The R-0062 full-CI failure received one bounded `gpt-6-sol`/`low`
   diagnosis in `2026-10-16-ci-colony-jobs`, based on `db946d43`. The worker
   identified the first missing native drag completion and subsequent failures,
   but correctly retained uncertainty about the cause and made no speculative
   edits. Main owns the selected reproduction and any eventual repair.
8. Main reproduced the TC40 native-drag failure in Docker and sent the evidence
   back to the same Sol low worker. The worker proposed deferring construction
   repaint until the event returns; main integrated that one-line production
   change without changing the stale-state tests. Verification is pending.
   Caller-authored ship UI checks also needed corrections for existing closed
   crate placeholders and the three-turn voyage; those are separate from worker
   implementation failures.
9. The caller's Docker `run-uo1009` passed all 15 unchanged TC40 cases, the
   full specialist-to-smithy journey and three source-contract checks after
   the worker's deferred-repaint repair. Complete CI remains necessary;
   neither this local pass nor the adopted model setting measures quota savings.

8. R-0066 uses one bounded Sol low worker for model assembly and tests; the main
   Astra medium owns menu/DAO integration, UI checks and documentation. Review
   corrected missing initial actions/moves/focus and an unnecessary immediate
   ocean-neighbor restriction. Failed test attempts were returned for targeted
   repairs (start before save, preserve canonical England-first turn order).
   Main corrected its UI helper type and a misspelled test selector. Results are
   in the [R-0066 evidence](../functional-tests.md#r-0066-reference-expeditions-2026-10-16).
   No measured quota saving is claimed.

9. R-0063 reused one Sol low worker in isolated `2026-10-16-r63` based on
   `d407a393`. It owned the nation plumbing and focused model/UI tests; main
   retained architecture review, documentation, formatter and actual execution.
   The first test build used an unavailable Mockito method, returned as a
   targeted compatibility repair. Its next fixture tried to mock a final DAO;
   the targeted retry used the real DAO, preserving production boundaries.
   Main review kept AI-turn evolution separate from initial-roster assertions.
   See [actual results](../functional-tests.md#r-0063-nation-selection-2026-10-16).

10. Full CI at `57d0db52` confirmed all TC40 cases pass, but exposed two R-0108
    test integration omissions: the old TC22 purchase roster and TC26 frontier
    geometry under frigate sight. Main updates the explicit offer list; one
    bounded Sol low repair owns fog-fixture geometry in `2026-10-16-ci-fog`.
    This independent review is for concrete regression correctness, without
    duplicating the R-0067 worker's load-boundary implementation. See the
    [full-regression follow-up](../functional-tests.md#full-regression-follow-up-after-r-0108-2026-10-16).

11. R-0067 reused Sol low in `2026-10-16-r67` for the load boundary and focused
    recovery tests. Main review requested explicit empty-root/nested-location
    rejection and verified the existing required-relations contract. The first
    executed model test failed in fixture setup (no on-map unit); the targeted
    repair created that malformed placement. Main also required an actually
    active-game UI check instead of checking after the menu had stopped it.
    [Evidence](../functional-tests.md#r-0067-load-recovery-2026-10-16) records
    actual results; no measured cloud-token saving is claimed.

    Further targeted probes found two actual loader gaps (null cargo row and
    truncated terrain accepted as ocean). Worker repairs added explicit shared
    validation; a full run then exposed the malformed tenth-row label in one
    old 10×10 test fixture. Correcting that fixture yielded 1351 passing model
    checks and 24 passing selected UI/support checks. No acceptance checks were
    omitted to save tokens.

12. R-0068 uses the same Sol low worker in `2026-10-16-r68`, based on
    `188755b1`, with two bounded stages: atomic persistence/checkpoint policy,
    then menu/settings/listener integration and UI scenarios. Main reviewed
    and executed the first-stage checks (18 passing), and requested FX-thread
    dispatch for save-failure feedback before continuing integration. Actual
    verification is recorded in the [R-0068 evidence](../functional-tests.md#r-0068-recovery-checkpoints-2026-10-16).

    Main review also corrected failed menu-exit handling to preserve the active
    game, required full saved-state comparisons for both slots, and added Czech
    visual checks. The first integrated UI run exposed TC37's stale single-file
    expectation; this targeted test repair was returned to the same Sol low
    worker. Full model checks passed 1357; actual UI results remain in the
    linked evidence, without an unmeasured token-saving claim.

13. R-0069 uses the same Sol low worker in `2026-10-16-r69`, based on
    `3b518aac`. Main keeps architecture, checks and integration; the worker owns
    bounded sight/role changes and related tests. Main execution exposed stale
    expedition-route assertions, a warehouse-full test setup and an incomplete
    diagnostic-signature update; targeted repairs went back to the worker.
    [Evidence](../functional-tests.md#r-0069-explicit-sight-2026-10-16) distinguishes
    the observed new AI route from passing acceptance. No token savings are
    claimed without measurement.

    A further bounded Sol low assignment in `2026-10-16-ci-modal-save`
    diagnosed the first TC40 drag failure and the independent TC21 obsolete
    save-slot expectation from full CI. This separate repair avoids repeating
    the large CI log in the main implementation context; actual token savings
    are unmeasured. Main reviews, formats and executes all acceptance checks.
    R-0069 hover diagnostics remain with its original worker after two
    unsuccessful coordinate/panning repairs; failures are retained in evidence.

14. R-0071 is assigned to the same Sol low worker in `2026-10-16-r71`, based
    on `ba651749`, after main refined the agreed duration formula, persistence
    and acceptance boundaries. The worker owns bounded implementation and test
    authoring; main retains architecture, execution, formatting and integration.
    Early review explicitly checks royal-carrier voyage metadata as well as
    ordinary departures. Random one-turn crossings must not make UI tests
    assume that every voyage remains at sea after its first update. No local
    model or additional implementation agent is used for this assignment.

15. R-0072 uses one bounded Sol low assignment in `2026-10-16-r72`, based
    on `b1695549`, for route-position reversal, confirmation UI and regression
    authoring. Main refined cancellation/stale-offer semantics and keeps
    execution and integration. Early review identified the house yes/no
    dialog's retained result as a reuse risk; the new confirmation must reset
    its result on each opening and cancellation, with a confirm-then-Escape
    scenario. This is a task-specific requirement, not a parallel rewrite of
    the dialog framework.

    Main review also rejected an offer check based only on position and
    destination: two reversals can restore both and incorrectly revive an old
    confirmation. The worker must bind a voyage revision or replaced transit
    identity and cover that repeated-reversal case explicitly. Persisted
    direction/source/position must reject missing values and inconsistent
    derived remaining time.

    Both review issues were corrected and verified in model/UI checks. The
    Czech UI navigation failure was repaired by using the localized port title;
    its rerun passed. [R-0072 evidence](../functional-tests.md#r-0072-voyage-reversal-2026-10-16)
    records test counts; no quota-saving percentage was measured.

16. R-0073 uses one bounded Sol low model/test assignment in
    `2026-10-16-r73`, based on `79248aa4`. Main agreed the Sentry/Activate API
    before independently implementing GUI controls and player tests; the two
    scopes do not duplicate implementation. Main retains execution, formatting,
    review and publication. No token-saving percentage is inferred.

    Initial model failures were diagnosed as zero-movement passengers and an
    outdated five-hold Galleon fixture. The worker repaired those preconditions
    while retaining rejection checks, then strengthened the sole-candidate
    selection test after review. Main verified 1401 full model tests and eight
    focused cases. Main-owned UI fixture/helper failures and the final 31-pass
    Docker regression are recorded in [R-0073 evidence](../functional-tests.md#r-0073-repair-and-model-acceptance-2026-10-16).


17. R-0074 uses one bounded Sol low model/test assignment in
    `2026-10-16-r74`, based on `62b419d7`. Main owns GUI/localization and
    player scenarios; no duplicate model implementation or additional cloud
    reviewer was started. Review found saved-field identity validation and
    construction-time type access that needed updating for typed specialists.
    The worker repaired five old fixture errors after the first full model
    run; main verified the complete 1464-test rerun, 12 TC30 scenarios and
    three page-contract checks. Missing non-production role integration
    and full acceptance remain separately tracked in
    [R-0074 evidence](../functional-tests.md#r-0074-specialist-roster-2026-10-16).
    No measured cloud-token saving is claimed.


18. R-0075 uses one Sol low model/test assignment in `2026-10-16-r75`,
    based on `7717124e`; main independently owns GUI, fixture migration and
    functional scenarios. Main full-run evidence found plain-Gson candidate
    serialization errors and one old incomplete player fixture. The same
    worker repaired those causes; main's rerun passed 1473 tests.
    Review requested identity/counter boundaries and found a prospective-price
    overflow that could invalidate the next saved state, plus the shared gold
    subtraction overflow. The worker supplied narrow repairs and regression
    cases; main's final full run passed 1478 tests. TC46/TC30 plus the source
    contract passed 21 checks; the final arithmetic revision passed TC46 again.
    Main repaired stale TC22 menu/roster expectations and verified all five
    scenarios plus three source-contract cases. CI and dependent
    integration evidence remains in [R-0075](../functional-tests.md#r-0075-persistent-recruitment-2026-10-16).
    No token-saving percentage is inferred from these results.

19. R-0076 reuses the same Sol low worker in `2026-10-16-r76`, based on
    `11c53f15`, for bounded model accounting, earned arrivals and rule tests.
    Main owns the Europe progress display, player journey, formatting, actual
    checks and integration; implementation is not duplicated. The first actual
    full run executed 1485 tests with one failure and no errors/skips: the
    national-turn ordering test still relied on the removed turn-11 grant.
    Its fixture repair was returned to the same worker, preserving the
    arrival/birth/movement and Continue assertions. Final full model rerun
    passed 1488 tests; main-owned Docker selection passed 11 after repairing
    its own unsupported JUnit assertion. Review added worker-authored unit-ID,
    prospective target-overflow and computer/closed-Europe boundaries.
    [Execution evidence](../functional-tests.md#r-0076-national-cross-immigration-2026-10-16)
    retains full CI/publication gates; no token saving is claimed.

20. R-0077 reuses Sol low in `2026-10-16-r77` at `2a5dbac6` for the
    per-type purchase count model, confirmed-price transaction and model
    tests. Main prepares requirements, dialog integration, TC48/F39 and actual
    verification. Model suite passed 1495 tests; Docker passed 24 selected
    UI/support tests, with no failed attempts in this stage. No implementation
    is duplicated and no local model is used in this user-selected experiment.
    These outcomes do not establish token savings. Full acceptance, including the R-0095
    artillery dependency, remains in [R-0077](../requirements/done/R-0077.md).

21. R-0078 reuses Sol low in `2026-10-16-r78`, based on `d9e9615d`, for
    shared horse-breeding accounting and model tests. Main owns requirement
    refinement, TC49/F40 construction-to-breeding journeys and actual checks.
    The shared calculation must retain food-before-birth-before-breeding
    ordering, famine behavior and next-update stable activation. This is a
    bounded model assignment with no parallel duplicate implementation.
    Main review returned a transposed 201-food expected value to the worker
    before execution. The first full model run had one failure out of 1522:
    the last-resident fixture retained a food producer and did not meet its
    famine precondition. The same worker repaired the fixture; full rerun
    passed 1522. Main repaired its own UI setup compilation (package-private
    setter) with existing public stock operations; Docker then passed 17.
    [Execution evidence](../functional-tests.md#r-0078-horse-breeding-2026-10-16)
    distinguishes these repairs from the remaining CI/publication gates.


### Artillery and CI repair with Sol low — 2026-10-17

1. The continuing authorized cloud run reused the existing Sol low agent for
   a bounded R-0095 model assignment in `2026-10-16-r95`, based on `0d027e62`.
   Main owned refinement, GUI, artwork, player tests, review and verification.
   No local model and no parallel duplicate implementation were used.
2. Main review found omitted artillery attack +50%, loss handling that destroyed
   full artillery against an unsupported civilian opponent, insufficient saved
   cargo/placement validation, unsafe post-win occupation and an unjustified
   wagon target exclusion. Corrections went to the same worker with focused
   regressions. A proposed neutral-stack fixture was invalid under the existing
   world invariant and was removed; the runtime occupancy guard still checks
   every foreign occupant. Native combat was explicitly left pending, without
   inventing a native-role predicate.
3. Main's first UI tests exposed the missing attack bonus; later tests showed
   destruction feedback being overwritten during animation. Main corrected the
   notification timing and reran the affected player paths. Separately, the
   worker repaired TC21's canvas-to-screen coordinate conversion after full CI
   evidence, preserving the original assertions. These outcomes show usable
   bounded work plus necessary caller review, not measured quota savings.
   Counts and publication status belong to [execution evidence](../functional-tests.md#r-0095-artillery-lifecycle-2026-10-17).

### Cloud Sol follow-up R-0098 — 2026-10-17

1. The authorized run continued without the local worker. The existing Sol low
   agent implemented the bounded ship-repair model and model tests; the main
   agent owned requirements, GUI, player scenarios, review and acceptance.
   This division avoids duplicate implementation; no measured token saving
   is claimed.
2. Main review returned concrete gaps before acceptance: initial no-destination
   loss notification, a removed original coastal port, a distance-zero local
   yard, public progression gating, valid ship ID zero and typed Europe identity
   in saved messages. Sol corrected them and reported 12 focused tests passing.
3. Main full verification then ran 1705 model cases with zero assertion failures
   and two errors in existing `UnitTest` passenger tests: an unguarded new cast
   from a carrier to `Ship` rejected their `UnitWithCargo` test doubles. The same
   worker received the exact failing checks for targeted repair; this attempt
   is not recorded as passing. Final results belong in the
   [R-0098 evidence](../functional-tests.md#r-0098-ship-repair-2026-10-17).
4. Main-owned UI preparation also needed a caller correction: the Czech test
   used the English default Europe-screen expectation. The existing explicit
   Czech-name overload fixes the test without relaxing its assertions. A shell
   invocation initially left Maven's wildcard selector unquoted; zsh rejected
   it before Docker started. The corrected selector is quoted.

5. After the targeted carrier correction, main reran the complete model suite:
   1705 passed with zero failures/errors/skips. Final TC63 had four passing
   journeys; main corrected final modal closure between its own UI cases.
   The same Sol agent added failure-only diagnostics for an earlier TC53
   context-menu timeout. TC53 passed locally, but that is not a diagnosed fix.


### Cloud Sol follow-up R-0097 — 2026-10-17

1. This continuing run uses Astra medium for requirements, GUI, actual review
   and integration, with one bounded Sol low worker for naval model operations
   and tests. The local worker was not used. Reusing the model worker's context
   for two stale-input tests avoided repeating its fixture setup; this is a
   workflow choice, not a measured token-saving percentage.
2. Worker delivered nine focused passing cases and a full 1714-case passing
   model suite. Main requested two additional market/repair confirmation
   boundaries; the resulting focused suite passed 11/11 without production
   changes. Main inspected the report files: 174 XML suites currently represent
   1716 passing cases in aggregate, combining the full run with the later test
   supplement; this is not a separate fresh full-suite run.
3. Main's GUI acceptance caught its own invalid fixture goods identity,
   unescaped percent message and incorrect price-order expectation. These were
   corrected without blaming or rerunning the model implementation. Detailed
   attempts and publication status remain in [R-0097 evidence](../functional-tests.md#r-0097-naval-combat-2026-10-17).


### Cloud Sol follow-up R-0099 — 2026-10-17

1. Reused the naval model worker for one bounded privateer exception and
   stale-relation contract, while Astra medium owned GUI, player tests and
   acceptance. The local model was not used and no duplicate implementation
   ran in parallel. Main requested an explicit Crown boundary so the exception
   did not accidentally change existing king combat.
2. Worker focused checks passed 33/33. Main full model suite passed 1722/1722;
   Java formatter and package passed. The first GUI attempt exposed an off-FX
   TestFX visibility/layout race while closing the result. Main fixed that
   test helper and reran the affected journeys. Actual results and publication
   remain in [R-0099 evidence](../functional-tests.md#r-0099-privateer-hostilities-2026-10-17).
3. No comparative token savings have been measured for this individual task.

## Claude Code subscription trial — 2026-10-17

1. The user explicitly selected Claude Code through their Claude Pro subscription
   for one small backlog implementation trial, overriding the local-worker choice
   for this assignment only. The caller selected R-0086 after identifying that the
   sole `ready for development` item, R-0085, still needed the real boycott lifecycle.
   The user settled R-0086's notification policy before refinement and dispatch.
   This does not change ordinary worker defaults or start another requirement.
2. Baseline `3942b5de`; isolated managed worktree
   `~/.codex/worktrees/claude-r0086/microcol`. The unrelated main-checkout edit in
   R-0070 was excluded and preserved. Caller owns refinement, review, acceptance,
   integration and publication; Claude owns implementation and test authoring.
   No additional cloud subagents or local inference were used.
3. Preflight verified native Claude Code 2.1.302, `claude.ai` authentication and
   subscription type `pro`, with no API-key/auth-token/base-URL environment
   override. The default selected model reported `claude-opus-5-5`; the caller
   did not force a model. A first shell dispatch failed before inference because
   the worktree shell did not include `~/.local/bin` on PATH. The caller corrected
   it to the verified absolute executable path; this is a setup failure, not a
   model implementation failure.
4. The direct `claude -p` invocation used Read/Edit/Write/Glob/Grep/Bash with normal
   edit permission, explicit denial of commit/push/reset/clean tools and no MCP
   servers. The bounded task prohibited delegation, other checkouts, credential
   access, installations and native desktop control. Worktree isolation is not a
   filesystem sandbox. A caller process enforced 7200 seconds per invocation,
   14400 seconds cumulative and at most two caller-requested repairs. Logs and
   handoffs are under `~/Library/Logs/Microcol/claude/2026-10-17-r0086/`.
5. Initial invocation completed in **739.596 seconds**. Its first Docker run,
   `run-K2zc8M`, ran 78 model cases with one error and no skips; UI did not run.
   The new unit-project test directly replaced a construction while a worker
   retained its old slot, yielding an invalid saved fixture. Claude diagnosed and
   repaired its test setup, checked the model on the host without UI, then reran
   Docker. `run-q8X6le` passed **78 model + 10 UI/source-contract cases**, with
   zero failures/errors/skips. Caller review then requested missing Czech/mouse/
   screenshot and stale-navigation UI coverage in the same session, repair 1/2.
6. The initial invocation reported 122 uncached input, 140089 cache-creation input,
   5558263 cache-read input and 47746 output tokens (13116 reported thinking).
   These are provider-reported categories, not Codex tokens saved or a subscription
   quota percentage. JSON list-price cost estimates are not an API invoice.
   The following repair/final evidence keeps these categories separate.

7. The same-session caller-requested repair took **1118.692 seconds**, making
   **1858.288 seconds (30.97 minutes)** across both invocations. `run-aiaj3I`
   deadlocked in the new lost-colony UI fixture: capture was called on the FX
   thread and waited for the galleon's escape animation on that same thread.
   Caller captured the Java thread dump, requested a JVM dump and terminated
   only that stuck test JVM; Maven exported the failed run. Claude independently
   diagnosed the exported stack and moved the galleon out of the fixture port
   before simulated capture, retaining all rejection assertions. `run-ttXe0j`
   then passed 78 model + 13 UI/source-contract cases.
8. Screenshot review found a clipped Czech action label. An initial layout fix
   exposed real row overflow: `run-BFR9GO` passed 78 model cases but failed two
   of 13 UI/source-contract cases. Diagnostic TC32-only `run-uosIaN` passed ten
   cases, but its screenshot still showed the button outside the paper; it was
   not accepted as visual completion. Claude corrected the action-row CSS and
   retained text-width/report-width assertions. Final targeted `run-EmWEIf`
   passed **78 model + 13 UI/source-contract cases**, zero failures/errors/skips.
   Caller inspected the final Czech screenshot and reapplied the shared formatter:
   19 Java files, no changes, exact code tokens and idempotence passed.
9. Repair usage reported 82 uncached input, 60968 cache-creation input, 7112955
   cache-read input and 31607 output tokens. Its `modelUsage` is cumulative for
   the resumed session: 204 uncached input, 201057 cache-creation input,
   12671218 cache-read input, 79353 output and 26796 thinking tokens. Do not add
   the initial `modelUsage` to that cumulative total. Both invocations used the
   same default `claude-opus-5-5`; no permission denials or extra subagents were
   reported. This proves a supervised implementation/repair route, not a measured
   Codex quota saving or general autonomous reliability. Caller added no gameplay
   implementation or test repair; caller-authored work is refinement, docs,
   diagnosis, review, formatting, acceptance and integration.
10. Full-suite acceptance and publication are recorded in the
    [R-0086 functional evidence](../functional-tests.md#r-0086-construction-waiting-notice-2026-10-17).

11. While caller full regression `run-rYyVo5` was running, concurrent main commits
    `54fb81bc`/`f10e3f49` published other requirement decisions, including a
    conflicting per-turn policy for R-0086. The caller asked which choice was
    final; the user explicitly chose **every turn**. The caller canceled that
    superseded regression after 1853 game tests passed (UI incomplete); it is
    not final acceptance. Repair 2/2 in the same session removes the now-unneeded
    episode state and updates reminders/tests. This is a user scope correction,
    not an implementation failure. Concurrent unrelated documentation is retained.

12. Final-policy repair 2 completed in **280.502 seconds**, cumulative
    **2138.790 seconds (35.65 minutes)**. Fresh Docker `run-6Psd2R` passed
    **74 model + 13 UI/source-contract cases**, zero failures/errors/skips.
    Removing episode persistence returned `Colony`, `Model`, `ColonyPo` and
    obsolete `GamePage` helpers to baseline. The 26 waiting-report cases now
    verify reminders on successive updates, current shortage and load/view
    non-mutation; TC32/TC02 exercises actual consecutive turns and repeated
    report views after Continue. Caller reviewed the diff and screenshot,
    reapplied the shared formatter to 15 Java files (no changes, token equality
    and idempotence passed), and started full regression of this final policy.
13. Repair 2 usage: 26 uncached input, 26288 cache-creation input, 2858506
    cache-read input and 14062 output tokens. Final cumulative session totals:
    **230 uncached input, 227345 cache-creation input, 15529724 cache-read input,
    93415 output** (29403 reported thinking) tokens. Same model, no permission
    denials or subagents. These measured Claude categories establish execution,
    not a measured Codex quota saving; the reported list-price estimate is not
    a charge against an API account. All gameplay code and test changes came
    from Claude; the main agent retained analysis, review and acceptance.

14. The caller's complete Docker regression and independent Linux CI both passed
    **1,849 model + 292 player/support tests**, with no failures/errors/skips.
    The accepted code was committed to main as `3c0342c4` and published as
    `dev-158-3c0342c4260b`; the release and checksum verification, unchanged
    automatic channel and separate documentation closure are recorded in the
    [functional evidence](../functional-tests.md#r-0086-construction-waiting-notice-2026-10-17).
    The managed worktree was archived after preserving all trial reports in the
    primary checkout. This completes one supervised Claude Pro handoff through
    implementation, repair, review, full acceptance and publication.


## Claude model-selection batch — 2026-10-17

1. The user authorized implementation of 20 requirements through Claude Code,
   searching for a cheaper adequate model by stepping down and escalating when
   evidence warrants it. This supersedes the ordinary worker choice for this goal.
   Use bounded sequential assignments; no additional cloud subagents are needed.
2. Validation override: author and run affected model and targeted UI tests per
   requirement. Run the full visual/functional regression and wait for CI only
   after each batch of ten. Keep intermediate work in the isolated batch worktree
   so concurrent main documentation pushes do not publish an unfinished batch.
   The main agent owns review, formatting, local checkpoint commits, integration
   and each batch's verified publication; workers never commit or push.
3. Selected first batch order: R-0118, R-0133, R-0132, R-0179, R-0184, R-0147,
   R-0119, R-0120, R-0136 and R-0090. Second batch candidates: R-0070, R-0089,
   R-0096, R-0100, R-0101, R-0102, R-0103, R-0085, R-0105 and R-0123.
   Refine one at a time and recheck actual prerequisites; open gameplay questions
   are not permission to guess. Completed predecessor work remains intact.
4. Model experiment: start with explicit `claude-sonnet-5-5` at `medium`, then
   try `claude-haiku-5-5` on an appropriate bounded task after a successful
   Sonnet assignment. Reduce effort only after an adequate model baseline;
   change one setting at a time. Diagnose setup errors separately. A simple
   defect gets one targeted same-model repair; repeated logic failures or an
   unsuitable scope justify escalating effort/model before exhausting the task
   budget. Keep the worktree, task ID, elapsed time and repair counts on escalation.
   Limits per requirement remain 7200 seconds per attempt, 14400 cumulative,
   and two requested repairs. Model switching does not reset them.
5. Record exact model/effort, actual output changes, checks, repairs, wall time
   and usage categories per requirement. Compare total cost of accepted work
   including repairs; different tasks are observational evidence, not a controlled
   benchmark. Report list-price estimates separately from subscription usage;
   do not equate them to an invoice or percentage quota saved. Recommend a
   practical default and escalation rule after both batches, not a universal optimum.
6. Sonnet preflight returned OK with actual `claude-sonnet-5-5` identity. No
   global Claude settings were changed. Baseline, handoffs and run data are under
   `~/Library/Logs/Microcol/claude/2026-10-17-batch20/`; the managed worktree is
   `~/.codex/worktrees/claude-batch20/microcol`. The previous R-0086 Opus trial
   is historical comparison, not a same-task benchmark.

7. R-0118: actual `claude-sonnet-5-5`, medium, one attempt, 57.762 seconds,
   no requested repair. Caller review/formatting and all 54 targeted checks passed
   ([evidence](../functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17));
   full batch acceptance is pending. Usage: 24 uncached input, 35,083 cache-create,
   321,295 cache-read, 7,670 output tokens (1,044 thinking reported); list-price
   estimate $0.2492095, not subscription charges or measured quota savings.
   Next selected trial is Haiku medium for the bounded R-0133 carrier admission gap.

8. R-0133 first Haiku medium attempt was stopped by the caller after 301.387
   seconds (exit 143). It introduced `EnumSource` for non-enum `UnitType`; its
   unauthorized Maven attempt confirmed three compilation errors. It also
   searched the whole filesystem for a formatter despite the author-only task;
   the caller terminated that search before stopping the worker. No acceptance
   or successful token/cost total is claimed for this incomplete attempt. Logs
   and diagnosis remain in `R-0133/` under the batch log directory.
   Recovery retains the same task/worktree/budgets and Haiku medium for one
   targeted repair. The runner now exposes only Read/Edit/Write/Glob/Grep:
   command execution, formatting and actual verification remain with the caller.
   This is an instruction-following/code error, not a missing runtime.
9. R-0133 repair completed in 118.165 seconds on the same Haiku medium session,
   for 419.552 seconds across both attempts. Caller review and all 184 targeted
   checks passed; full batch acceptance remains pending. The resumed session
   reports 120 input, 134,982 cache-create, 5,117,526 cache-read and 58,540
   output tokens (33,766 thinking reported), list-price estimate $0.28575678.
   These are the resumed-session totals as reported, not added again to an
   inferred first-attempt total. The narrower tool set prevented more command
   execution. Keep Haiku available for a further small trial; select Sonnet
   medium for R-0132 because shared transfer admission also affects purchase
   charges and changed-state UI confirmation. This remains observational evidence.
10. R-0132 Sonnet medium first attempt took 128.369 seconds and produced the
    cap/admission and changed-purchase UI handling, but explicitly omitted
    required stale-purchase, real-model conservation and saved-roundtrip checks.
    Caller review returned this incomplete authoring to the same model for one
    bounded repair; a missing page helper is within the assignment, not a blocker.
    No successful acceptance is claimed for that attempt.
11. R-0132 first repair (same Sonnet medium) took 132.320 seconds. Actual
    Docker `run-cO7Ogt` compiled and passed all 104 selected model tests;
    UI/support ran 10 with two errors, zero assertion failures/skips. Both
    new stale-purchase cases queried `#labelGold` while the market screen
    was open, causing `EmptyNodeQueryException`. Review also found the requested
    Continue check still omitted. Final bounded repair escalates effort to high
    on the same Sonnet/session/worktree, retaining 260.689 elapsed seconds and
    the two-repair limit. Fix expected fixture gold, add Continue, and narrow
    invalid-restore assertions; then rerun the same targeted acceptance.
12. R-0132 final Sonnet-high repair took 84.176 seconds; total worker time
    is 344.865 seconds across three attempts. All 115 selected checks now pass
    ([evidence](../functional-tests.md#claude-batch-20-targeted-acceptance-2026-10-17));
    full batch acceptance remains pending. Final cumulative resumed-session
    totals: 84 input, 130,411 cache-create, 3,298,761 cache-read and 42,124 output
    tokens (12,783 thinking reported), list-price estimate $1.2729281. Do not
    sum earlier cumulative reports again. The high repair fixed the two actual
    UI errors and completed the repeated missing Continue check. Next R-0179
    uses Haiku medium for a smaller additive combat modifier; no Opus fallback
    has been needed in this batch so far.
13. R-0179 Haiku medium initial authoring took 182.063 seconds. Shared
    additive strength/breakdown and model cases were written, but the required
    UI, runtime stale-order and disk-roundtrip cases were omitted. Caller
    requested one same-model repair using concrete existing TC31 temporary
    fixture preparation, CongressTest membership and disk-save patterns. This
    was incomplete authoring, not an unavailable runtime. No acceptance is claimed
    until the full targeted selection passes.
14. R-0179 Haiku medium repair took 204.530 seconds, 386.593 total across
    both attempts. All 75 targeted checks passed. Final cumulative session
    reports 102 input, 173,659 cache-create, 4,350,405 cache-read and 82,128
    output tokens (54,173 thinking), list-price estimate $0.36277049. No runtime
    repair was needed after the missing authoring was completed. Next R-0184
    starts Sonnet high because founder join, population and capture must share
    an exactly-once rule; this scope has more state transitions than the
    successful small strength modifier and earlier medium work omitted checks.
15. R-0184 Sonnet high completed the initial authoring in 684.465 seconds.
    Caller review shortened one explanatory comment and applied shared
    formatting. Docker `run-WFtFHk` passed all 50 model tests, but both new
    TC72 cases dragged an empty carpenter slot and timed out (nine UI/support
    tests total, two errors, no assertion failures/skips). `assignCarpenter`
    drops at slot 1; the new cases tried slot 0. One same-model high repair
    changed only those two indices in 13.063 seconds, preserving all assertions
    and timeouts. The same targeted selection is being rerun.
16. R-0184 now passes all 59 targeted checks. Total worker time is 697.528
    seconds (684.465 initial + 13.063 repair). Final cumulative session totals:
    82 input, 298,389 cache-create, 6,819,218 cache-read and 104,727 output
    tokens (56,483 thinking), list-price estimate $2.9229118. Higher effort
    completed the requested authoring initially but did not prevent a small UI
    fixture mistake. For R-0147 return to Sonnet medium with concrete existing
    role, dialog and fixture patterns; escalate only on diagnosed need. This
    is a practical trial, not evidence that effort alone caused the time/cost gap.
17. R-0147 Sonnet medium authored the clear-specialty operation, confirmation
    and model/UI coverage in 427.134 seconds. Caller review found that its new
    public unit mutation could bypass colony validation. Docker `run-lpElKF`
    ran 108 model checks with three errors: saved field-worker validation omitted
    the three existing school-only planter roles. UI checks were not reached.
    The first same-model repair (62.513 seconds) closed the public mutation path,
    retained all role checks, corrected saved-role admission and added direct-entry,
    foreign-owner and real disk coverage. `run-GSs2a1` then passed 111 of 112
    model checks; its new foreign-owner fixture used raw Gson to clone a domain
    type containing a Predicate, causing deserialization failure. A second
    medium repair (12.667 seconds) reused the configured scenario loader instead,
    preserving assertions. Total worker time: 502.314 seconds; both repairs are
    used. These failures and the final rerun are retained in the task log folder.
18. R-0147 final `run-aZpHYU` passes all 123 targeted checks (112 model,
    eight TC68 scenarios and three source-contract checks), no failures, errors
    or skips. Shared formatting and strict documentation validation passed.
    Final cumulative Sonnet session: 106 input,
    177,511 cache-create, 5,979,966 cache-read,
    66,054 output (27,256 thinking), list-price estimate
    $1.96879260. These estimates are not an invoice or measured Pro quota.
    R-0119 starts Sonnet medium with the traced action, yield and fixture callers;
    escalate only after diagnosing an inadequate result.
19. R-0119 initial Sonnet medium authoring took 582.853 seconds. Shared
    formatting passed. Docker `run-YFvZ6F` ran 181 model checks with two failures
    and 30 errors; UI was not reached. Diagnosis: 19 specialist cargo reloads
    exposed R-0133's early `getType()` call during specialist construction; 11
    turn-boundary fixtures issued plow before the game started; one action
    fixture mutated placement inside the command expected to reject atomically;
    one new test expected an already-plowed reason while movement was still zero.
    Review additionally found missing required work history in the hardy UI
    fixture, an old work callback able to consume movement after cancellation,
    and no discoverable reason control for a partial 19-tool load. Escalated
    the same job/session to Sonnet high for repair, retaining its time and
    two-repair budget. No Opus or extra cloud worker was used. The repair must
    retain all acceptance assertions and rerun the affected cargo checks too.
20. Batch sequencing refinement: use R-0090 (bounded AI food-first/lumber
    workforce through existing job choices) as the tenth task and move R-0089
    (alliances, passage and Crown-war transitions) into its second-batch slot.
    The same 20 outcomes remain selected. This keeps the first integration
    boundary focused and leaves the larger diplomacy change for the next batch.
21. R-0119 first high repair took 191.413 seconds. `run-vGuYyP` passed all
    279 model checks and seven TC18 scenarios plus the three source-contract
    checks; one of the two TC15 cases failed because its selected UI unit was
    not model-list index zero. The last high repair (26.427 seconds)
    made both UI selections explicit and retained both identity/action/tool checks.
    Review also corrected foreign/wrong-unit callbacks so they cannot terminate
    the original valid order; a new test proves its later normal completion.
    Caller renamed the older callback test to reflect its actual no-effect check.
    Final `run-D7bgbV` passes **292 checks** (280 model, nine UI, three contract),
    zero failures/errors/skips. Shared formatter and strict docs validation pass.
    Total worker time 800.693 seconds. Final cumulative session: 182
    input, 239,660 cache-create, 12,471,460 cache-read,
    98,059 output (32,911 thinking), list estimate
    $3.18674000. Start R-0120 on Sonnet high because completion combines
    terrain, current recipient, warehouse capacity and persisted loss feedback;
    plan a cheaper Haiku trial for the narrower R-0136 arrival hook.
22. R-0120 Sonnet high was interrupted by the Claude Pro session limit on
    2026-10-17 at about 17:28 Europe/Prague. The CLI reports reset at 20:20
    that day. Exit 1, session `cafedc07-6f8f-4939-916f-8231bb8e9e8c`, elapsed
    878.070 seconds; this is an incomplete attempt, not accepted delivery.
    It authored model/map state, clearing controls, loss notifications and
    `ClearForestTest`, but no TC73/F65 player scenarios. Final reported usage:
    138 input, 337,442 cache-create, 14,431,124 cache-read, 111,661 output
    (45,190 thinking), list estimate $3.90976640. This is not a Pro charge.
    Preserve the same worktree/session and remaining two attempts; do not
    immediately retry against the exhausted quota or silently use this account
    for the missing implementation. After availability returns, resume the
    bounded UI/test completion on Sonnet medium. Its architecture is already
    written; high effort is not justified merely because the first attempt ended
    on an account limit. The caller is validating the existing partial change.
23. Partial R-0120 Docker `run-mIaLGe` exits 0 with 122 checks: 110 model,
    nine existing plow UI scenarios and three contract checks. No failures,
    errors or skips; missing new clearing UI acceptance remains an explicit
    blocker. Seven requirements are targeted-accepted, none yet full-batch
    accepted or published in this experiment. The provisional choice is Sonnet
    medium for bounded implementation, Haiku for small changes with concrete
    test patterns, and Sonnet high only for diagnosed complexity. The requested
    20-task trial and optimum assessment are not finished.
24. User resumed the batch on 2026-10-17 at 20:21 Europe/Prague. The same
    R-0120 session resumed on Sonnet medium; actual assistant responses and an
    `allowed` rate-limit event confirm availability returned. No new session,
    worktree or task budget was substituted. The remaining authoring is the
    clearing player path and narrow validation of the new saved map state.
25. R-0120 resumed Sonnet medium authoring completed in 95.984 seconds.
    Docker `run-ODgwPv` ran 130 checks: 129 passed and TC73_06 failed because
    it required the entire next-turn report to be absent, although F01 produces
    unrelated warehouse events. All 44 clearing model tests and the other five
    new UI cases passed. The final same-session medium repair (16.736 seconds)
    replaced that broad assumption with zero messages containing `lumber lost:`;
    prior single-notice, exact stock/tool and Continue checks remain. This is a
    test correction, not acceptance evidence until the rerun passes.
26. Final R-0120 `run-tfx9Qq` passes all 130 checks, zero failures/errors/skips.
    Eight requirements are targeted-accepted. Total worker time 990.790 seconds;
    final cumulative session usage: 164 input,
    708,675 cache-create, 18,769,905
    cache-read, 128,132 output (47,698 thinking),
    list estimate $5.99333850. This is not a Pro charge or quota estimate.
    R-0136 will trial Haiku low on the traced arrival hook and existing test
    patterns; diagnose and repair/escalate within the same job if inadequate.
27. R-0136 Haiku low authoring took 212.532 seconds. Review found missing
    post-callback movement validation and passenger checks after departure-sentry
    mutation; required stale/zero-point player coverage was incomplete. Docker
    `run-i3mvs5` ran 122 model checks: 121 passed and one new fixture errored by
    giving a speed-one colonist three action points; UI was not reached.
    Caller also corrected its TC29 selector from F23 to the actual F21.
    Escalate the same session to Haiku medium for the concrete code/fixture and
    coverage repair, retaining budgets and all assertions. Low is not accepted.
28. R-0136 medium repair took 171.362 seconds. `run-KOKfO7` passes 125 model,
    six TC29 and three source-contract checks; TC38 has one failure and one
    error among five cases. Diagnosis: the new empty-hold helper included F30's
    ore-filled slot zero, and reboarding was attempted while the colony UI was
    closed. The last same-session medium repair corrects these concrete setup
    errors and adds focused departure-sentry arrival coverage; no rollback
    framework or weaker acceptance is requested.
29. R-0136 final medium repair took 217.464 seconds; total worker time
    601.358 seconds across low, medium and medium. Final `run-t7AO4q` passes
    **141 checks** with no failures/errors/skips. Shared Java formatter passed.
    Cumulative session usage: 146 input,
    176,101 cache-create, 8,194,868
    cache-read, 101,991 output (70,363 thinking),
    list estimate $0.62215598; not an invoice or measured Pro quota.
    Haiku low needed both repairs to meet acceptance on this movement task.
    Keep Sonnet medium as the provisional default for broader behavior changes;
    cheap Haiku remains a candidate for narrower tasks with concrete fixtures.
    Nine requirements are targeted-accepted; full batch acceptance is pending.
30. R-0090 Sonnet medium authoring took 225.587 seconds. Review found an
    incorrect `canAttack()` worker filter (ordinary colonists also have that
    capability), incomplete food/job selection and missing spare-worker UI and
    shared-field/foreign-worker coverage. Docker `run-tsR1yN` ran 88 model checks:
    86 passed and two new fixtures failed because raw Gson cannot clone
    `UnitType` predicates. UI was not reached. Escalate the same session to
    Sonnet high for diagnosed policy and coverage gaps, preserving budgets;
    reuse the configured scenario loader instead of introducing a serializer.
31. R-0090 Sonnet high repair took 265.352 seconds and corrected worker
    eligibility/food choices plus missing scenarios. `run-wwKSyB` ran 92 model
    checks: 86 passed, six new fixtures errored in one shared helper by assuming
    every map coordinate is stored explicitly. Missing terrain entries represent
    ocean in the existing model. The final same-session repair uses medium for
    this narrow fixture correction and audits the new fixture boundaries;
    UI acceptance has not yet run. No assertions are removed.
32. Final R-0090 worker repair took 28.739 seconds. `run-8RXahW` ran 92
    model checks with three assertion failures: two boundary fixtures let the
    resident starve in the owner update before its first AI decision, and the
    foreign-unit snapshot compared initial zero movement with the normal next
    turn's one point. Both worker repairs are exhausted, so the caller takes
    over these small fixture corrections explicitly: starting food only in
    the focused reassignment/occupation fixtures and a normal initial movement
    allowance for the foreign unit. Ten-turn production references retain their
    original stocks; no runtime supplies or production changes are introduced.
    The foreign test additionally checks that its resident still exists.
    Required targeted acceptance will be rerun; this is not yet a pass.
33. Caller fixture correction first hit immutable saved warehouse maps:
    `run-0ieCze` had 92 model checks, three setup errors. Use a copied map and
    the existing PO setter; preserve the exact checks and rerun. This failure
    belongs to caller setup, not Claude model quality.
34. R-0090 final `run-aNzXVw` passes **104 checks** (92 model, nine TC34,
    three source-contract), zero failures/errors/skips. Shared formatter passes.
    Total worker authoring time 519.678 seconds; caller fixture repair is
    separately recorded above. Cumulative Claude usage: 102
    input, 156,482 cache-create,
    4,924,461 cache-read, 71,314 output
    (35,569 thinking), list estimate $1.83171810.
    This is not a Pro bill or measured quota savings. All first ten selected
    requirements now have targeted acceptance. Next: merge current main docs,
    complete shared batch regression, then publication; no full-batch acceptance
    or published build is claimed yet.
35. First full batch run found TC08's food stock still assuming the former
    doubled plow yield. F07/F08 have two ordinary workers on plowed grassland:
    each now makes five instead of eight corn, so net food is +3, not +9.
    Tobacco fields are unplowed and remain +9. Caller repairs the narrow old
    fixture/expectations because R-0119's worker repairs are exhausted: TC07
    expects 167/170/173 corn; F08 starts at 190 and TC08 checks
    190/193/196/199/2 with birth on the fourth update at the unchanged 200
    threshold. The full container uses its source snapshot and continues;
    these corrected scenarios must be rerun before batch acceptance.
36. The same full run exposed TC27_07's pre-R-0136 arrival expectation:
    it waited for one map unit and then manually unloaded the passenger. Real
    arrival now correctly leaves the ship and its passenger outside (two map
    units). With R-0136 repairs exhausted, caller updates that narrow player
    path: wait for two, verify the single outside passenger in the colony UI,
    retain all equipment, identity, empty cargo and Continue checks. Production
    behavior is unchanged. Rerun TC27 with the other affected legacy scenarios.
37. TC41's import/growth reference has the same two plowed corn fields.
    Its expected production becomes 10 rather than 16, consumption stays 16,
    and 230 imported food settles to 24 after one 200-food birth. Caller updates
    those two exact assertions only; cancellation, transfer totals, no immediate
    birth and newborn placement remain unchanged. Include TC41 in the rerun.
38. Source review during the same run found the identical manual-arrival
    assumption in TC30's purchased-expert voyage. Caller applies the same
    two-map-unit/one-outside-person expectation and keeps specialist identity,
    equipment, cargo, Continue and subsequent smithy assignment assertions.
    Other manual-disembark callers either start in port or land on ordinary
    shore and are retained. TC30 joins the targeted legacy rerun.
39. TC13's basic arrival case likewise expected only the carrier on the
    colony tile. Caller updates it to two map units and strengthens explicit
    checks for the same passenger, unchanged movement/tools, normal ship cost
    and the freed hold. Add TC13 to the legacy rerun; preserve its stable ID.
40. TC21_05's recovery-save check also assumed a carrier alone after its
    port arrival. Caller updates the count to two and explicitly verifies the
    same outside passenger and free hold after Continue; autosave-change and
    empty-origin assertions remain. Include TC21 in the targeted rerun.
41. TC17's newborn-focus fixture also used the former two plowed-field
    yields. Its net food becomes +9 instead of +15. Caller changes initial
    F13 corn from 189 to 195, retaining the same 204 before the 200-food birth.
    The scenario still checks first-update birth and immediate movement of the
    automatically focused newborn without reselection. Include TC17 in rerun.
42. TC01's ordinary-shore embark/disembark journey passes until its final
    return to the owned colony. Caller updates only that final arrival from one
    to three map units and asserts zero occupied holds. Both prior selective
    landings, boarding operations and cargo/shore checks remain. Add TC01 to
    the targeted legacy rerun.
43. The legacy rerun exposed a caller helper mismatch in the new TC13
    pre-arrival assertion: `PassengerTransferPage.verifyAboard` also requires
    seven ore in F21/F30's slot zero, absent in F12. Use the existing occupied-
    hold count for the initial ship instead; retain exact passenger identity,
    movement/tools and freed-slot checks after arrival. No gameplay change.
    Rerun TC13 and the source-contract check after this correction.
44. First-batch local acceptance is complete across full `run-hSFnT1`,
    corrected-scenario `run-Km1yji` and final TC13 `run-jfk9pd`; see the
    [integrated evidence](../functional-tests.md#claude-batch-1-integrated-acceptance-2026-10-17).
    All nine original full-run failures have passing reruns. Production code
    was unchanged during those legacy corrections. Complete requirement
    archival/link checks and publish on main; CI/publication remain pending.

45. Batch one was integrated and pushed as `333de9b6` on 2026-10-17.
    Publication remains pending. Batch two starts R-0070 with Sonnet high: its
    model-owned observation snapshots, persistence and GUI disclosure boundaries
    justify the higher effort directly. The worker uses the same bounded tool
    contract and cannot run tests or publish. No Opus is requested.

46. First-ten provisional model totals (final cumulative session usage counted
    once): Haiku handled three requirements in seven attempts / 1,408 worker
    seconds; Sonnet handled seven in 18 attempts / 3,914 seconds. CLI list-price
    estimates were $1.271 and $17.426 respectively, **not Pro charges or measured
    quota savings**. Task difficulty differs, so these are not controlled model
    benchmarks. R-0119/R-0136 legacy fixture corrections and R-0090 fixture
    repairs required caller takeover after repair budgets were exhausted. The
    caller's review/test effort is not included in worker time or price.

47. R-0070 Sonnet high attempt 1 completed in 894.143 seconds. Compilation
    and 130 selected model checks passed; TC26 passed six of seven scenarios
    and all three source-contract checks passed. TC26_07 timed out moving the
    player after a synthetic foreign-step start without the matching completion
    lifecycle. Review also found missing colony-only terrain exploration and
    foreign-fight disclosure when only one participant is in sight. Targeted
    repair 1 uses Sonnet medium in the same session and budget, with tests for
    both boundaries; no acceptance is claimed for the failed attempt.

48. R-0070 repair 1 completed on Sonnet medium in 111.509 seconds, same
    session. The worker balanced diagnostic movement events, explored owned
    colony surroundings and required both foreign fighters in sight. Docker
    `run-9WkN2C` passes all 142 selected checks; see
    [acceptance](../functional-tests.md#r-0070-current-sight-2026-10-17).
    No caller implementation takeover or Opus was needed.

49. R-0089 starts with Sonnet high after R-0070 targeted acceptance. Shared
    diplomacy, allied passage, attack atomicity and a persisted round event
    make this a higher-effort assignment; the bounded repair/tool contract
    remains unchanged. No Opus or additional cloud agent is used.

50. R-0089 attempt 1 was interrupted by the Claude Pro five-hour limit after
    1,183.421 seconds (exit 1, session
    `e8bcd909-6732-49a3-938d-4319ff87de9b`). Last reported utilization reached
    99%, then the CLI explicitly rejected further work until **2026-10-18
    01:20 Europe/Prague**. Overage remained disabled. Partial production/model/UI
    changes are retained in the isolated worktree and are not accepted or
    published. Caller runs a diagnostic targeted build before preparing the
    same-session medium continuation; no silent implementation fallback.
51. First-batch publication is independently verified: build
    `dev-171-333de9b6bf8f`, CI run `39066465318`, 2,072 model and 320
    functional/contract checks all passed. Documentation-only evidence commit
    `c20bf30e` passed Pages run `39069476049`; game run `39069476068` and
    compatibility run `39069476096` intentionally skipped their game jobs.
    Downloaded channel manifests before/after that documentation push were
    identical. Primary main contains those sources; R-0070 and later work
    remain isolated for the second batch.

52. Diagnostic Docker `run-4xcJ62` compiled the interrupted R-0089 code and
    ran 249 model checks: 248 passed, one error, zero skips.
    `alliedPassageUsesTheColonyTileWithoutOwnershipOrWorkforceAccess` fails
    loading a formerly shared tile after termination: `UnitStorage` rejects
    every mixed-owner map stack. This is a real passage/persistence integration
    gap, not permission to weaken the assertion. Existing overlap after alliance
    termination or forced war needs an explicit rule; the user was asked to
    choose retained positions with departure or automatic relocation. R-0089
    returns to waiting for refinement until that answer is incorporated. A
    separate UI diagnostic runs without treating the model failure as passed.

53. Interrupted R-0089 UI diagnostic `run-M5HEYR` ran 16 checks: eight of
    13 TC74 scenarios and all three source-contract checks passed; four failed
    and one errored. TC74_12 uses a no-owned-colony fixture at round 19, and
    the real end-round path correctly ends the game before its expected Crown
    report (`Game over, no colonies`), producing the timeout. The four later
    load/status-bar failures may be cascading from that game-over window; this
    is a diagnosis to verify, not a passing result. The continuation handoff
    requires a valid live colony fixture and reruns of every failed check plus
    relevant regression, in the same Claude session after quota reset.

54. Resumed on 2026-10-18 after the advertised quota reset. Claude Code
    2.1.302 remains authenticated through claude.ai Pro. The mixed-owner
    post-alliance placement decision is still pending and was asked again.
    To make independent progress, attempt 2 resumes the same R-0089 session
    on Sonnet medium for the TC74 Crown-event fixture only; production
    passage/persistence policy is explicitly excluded. Prior elapsed time
    1,183.421 seconds and the original three-attempt budget are retained.

55. R-0089 attempt 2 completed on Sonnet medium in 41.437 seconds, exit 0,
    without denied tools. Cumulative worker time is 1,224.858 seconds; one
    original repair attempt remains. It added a scoped long-game fixture,
    transferring Montreal and its corn worker to the Dutch together while
    preserving stored food. Main applied the shared formatter (24 Java files
    checked, one changed, code-token/idempotence checks passed). Docker
    `run-aH0VVY` then ran all 13 TC74 scenarios and three source-contract
    checks: 15 passed, one failed, zero errors/skips. TC74_12's Crown/Continue
    path and previous TC05/06/09 failures now pass. TC74_10 still incorrectly
    requires an empty movement preview towards an ally, conflicting with
    allowed passage; its repair must prove no attack/declaration while retaining
    legitimate movement, rather than dropping the gameplay check. This is not
    R-0089 acceptance: the mixed-owner save error and pending termination
    placement decision remain. The final original repair attempt should combine
    both fixes after that decision. No full regression or CI was started.

56. User approved retained positions on 2026-10-18. The authoritative relation
    rule and R-0089 now specify departure, no new non-allied entry, ordinary
    adjacent combat after separation and saved lawful overlap; R-0089 returns
    to ready for development. Attempt 3 resumes the same Sonnet medium session
    for the placement/save repair and TC74_10 correction, retaining 1,224.858
    seconds of prior work and the original cumulative budget. This is the final
    original attempt; acceptance remains pending targeted checks.

57. Attempt 3 completed on Sonnet medium in 185.140 seconds, exit 0, no
    denied tools; total R-0089 worker time is 1,409.998 seconds. It added
    pair/tile retained-overlap evidence, load validation and transition tests,
    corrected TC74_10 and added TC74_14 Continue coverage. Main review caught
    seven older `EuropeanRelationsTest` calls not adapted to the new save
    predicate. With the three-attempt budget exhausted, main updated those
    calls explicitly for their no-overlap fixtures instead of starting another
    worker attempt. The shared formatter checked 26 Java files, with token and
    idempotence checks passing. All 253 selected model tests passed in the
    subsequent Docker run; selected UI acceptance is still running.

58. R-0089 targeted acceptance completed in Docker `run-6JdvBs`: 253 model
    plus 34 functional/contract checks pass with zero failures/errors/skips,
    exit 0. All 14 TC74 scenarios and prior TC33/59/60 pass. This verifies the
    repaired cases, not merely the worker report; full second-batch acceptance
    remains pending. Sonnet medium handled both targeted repairs, with the
    caller's seven test-call adaptation explicitly separated above.

59. R-0100 starts on Sonnet medium after R-0089 targeted commit `3fc8f9bd`.
    It owns liberty, exact local/national support, occupation affiliation and
    reports; R-0096 will supply actual Revere battle placement. Main owns the
    repetitive current-schema fixture update after field names are fixed,
    avoiding worker rewrites of 68 existing colony-bearing scenario files.
    No R-0100 implementation or acceptance is yet claimed.

60. Main updated the fixed `liberty`/`lastColonialAffiliation` schema in 68
    existing scenario files containing 114 colonies. A JSON comparison against
    HEAD verifies only those two additions; existing liberty starts at zero,
    ordinary affiliation equals the stated colonial owner, and occupied New
    Amsterdam in F04/F06 explicitly retains Dutch affiliation. The caller's
    script first encountered the intentionally malformed `test-currupted-file`
    fixture and a directory named `.microcol`; its scan was corrected to leave
    malformed fixtures intact and process files only. These were caller setup
    issues, not Claude failures. The final field-only equivalence check passes.

61. R-0100 attempt 1 completed on Sonnet medium in 428.263 seconds, exit 0,
    no denied tools. It reported missing real cross-colony transfer acceptance.
    Main formatted 16 Java files (nine changed; token/idempotence checks pass).
    Docker `run-ZIKsKS` ran all 2,139 model tests: 2,136 passed, three failed,
    no errors/skips. Two old R-0089 movement tests stubbed the replaced enemy
    query instead of `Model.isPassageBlockedAt`; main adapted those two mocks,
    preserving rejection assertions, because R-0089's repair budget was exhausted.
    R-0100's conqueror fixture also failed its real capture precondition.
    Separate UI diagnostic `run-Qx6F1u` passes all 18 selected checks (TC75/36/39/56
    and source contract), exit 0. Attempt 2 resumes the same Sonnet medium
    session to repair the capture fixture and add actual two-colony transfer
    plus conqueror/Bolivar/Crown-chain acceptance; no requirement acceptance
    is claimed yet. Original time and repair budgets are retained.

62. R-0100 attempt 2 on Sonnet medium completed in 213.519 seconds, exit 0.
    Docker `run-KRtFY7` ran 2,141 model tests: 2,138 passed, two failures and
    one error, no skips. The three new fixture problems are an ineligible
    cross-colony worker during free-slot selection, Quebec's assumed two
    residents versus one actual resident, and incomplete Congress activation/
    category state when assigning Bolivar. Existing model regressions pass,
    including the two corrected movement mocks. Separate `run-l7vcIM` passes
    all six checks: three TC75 scenarios, including real cross-colony movement,
    plus three source-contract tests. Repeated medium fixture mistakes justify
    Sonnet high for final attempt 3 in the same session; prior 641.782 seconds
    and the original budget are retained. No production validation or expected
    reference values are relaxed.

63. R-0100 final attempt on Sonnet high completed in 70.765 seconds, exit 0,
    without denied tools. It changed only model-test fixtures, preserving the
    expected 2/8→1/9 values and 50/70% capture cases. Total worker time is
    712.547 seconds across three attempts. Docker `run-kPlLaY` passes all
    2,141 model tests and 12 selected functional/contract checks, zero failures,
    errors or skips. High repaired the diagnosed fixture mistakes; this does
    not prove it is always needed. R-0096 starts high because its interacting
    combat/worker/ownership/save boundaries are more complex; Opus remains unused.

64. Reusable fixture lesson from R-0100: inspect actual saved workers and jobs
    before assuming population from a scenario name. A cross-colony worker
    must complete exit and reach the destination square before a destination
    slot is eligible. Founder membership requires a complete valid Congress
    state (activation, generation, one remaining candidate per category and
    consistent selection), not a membership-list edit alone. Preserve these
    preconditions in future handoffs; never weaken model validation to make
    invented fixture states load.

65. R-0096 attempt 1 on Sonnet high completed in 1,297.483 seconds, exit 0,
    no denied tools. Formatter checked 21 Java files (11 changed; token and
    idempotence checks pass). Diagnostic `run-EcLW0T` passes 2,159 of 2,160
    model checks; `movedResidentMakesTheQuoteStale` errors because its opponent
    worker assignment is attempted during the attacking player's turn. Separate
    UI diagnostic `run-xxagH8` passes 36 checks (TC04/26/33/35/76 and source
    contract), zero failures/errors/skips, exit 0. This is not acceptance:
    worker-reported omissions include independent field-worker capture, a public
    direct-capture bypass, declaration against an otherwise empty peaceful
    colony, and resident/Revere promotion. Review also found an unapproved
    fortified-minimum exception to Revere's compulsory exit. The agreed rule
    excludes only the last resident; voluntary departure restrictions stay
    intact. Attempt 2 resumes Sonnet high with those exact completion criteria,
    preserving the original time/repair budget and existing passing assertions.

66. R-0096 attempt 2 was interrupted by Claude Pro's session limit after
    279.459 seconds, exit 1: advertised reset 2026-10-18 at 11:00 Europe/Prague.
    No extra usage was enabled. Cumulative worker time is 1,576.942 seconds;
    one original attempt remains in session
    `e7958573-e09b-4935-a9b2-10ecef11d267`. Partial production edits are retained,
    including changes to capture, field targeting, forced exit and promotion;
    the complete implementation/test repair was not delivered. Main formatted
    22 changed Java files (five changed; token/idempotence checks pass) and
    started a diagnostic build to record the exact continuation state. Earlier
    passing UI results do not verify these interrupted edits.

67. Interrupted-state diagnostic `run-mzg0Tn` exits 1 during production
    compilation, before any tests: `AbstractUnit` no longer implements
    `Unit.isPossibleToCaptureColonyAt(Location)`, while the interface and callers
    still require it. The compiler reports the same integration gap on nine
    concrete unit classes. The continuation handoff records this exact state,
    the unfinished caller/test adaptations and all remaining accepted behavior.
    No repair is claimed, no partial R-0096 change is committed/pushed, and
    no extra Claude attempt is spent before the advertised quota reset.

68. R-0096 resumed after the user confirmed Claude availability on 2026-10-18.
    Final original attempt 3 on Sonnet high completed in 518.782 seconds,
    exit 0 with no denied tools; cumulative worker time is 2,095.724 seconds.
    It completed the removed-interface callers and the five outstanding paths.
    Main formatter checked 37 Java files (seven changed; token/idempotence pass).
    Diagnostic `run-TIokit` compiled production and tests, then ran 2,175 model
    checks: 2,173 passed, one failure and one error, no skips. UI did not run
    because the model module failed. One test assumed two residents although
    its fixture has three; another expected a capture quote despite an armed
    third-party occupant blocking the atomic capture. Main corrected these
    two expectations, preserving unchanged-state assertions and adding an
    explicit armed-occupant assertion. Main takeover is limited to these
    diagnosed test mistakes because the original three-attempt budget is
    exhausted; no fourth worker attempt or budget reset was used. Acceptance
    requires the rerun and selected UI paths, not the worker's completion report.

69. R-0096 rerun `run-F6cEP6` passes all 2,175 model tests. Of 56 selected
    functional/contract checks, 51 pass and five TC33 cases fail (three
    failures, two errors; zero skips). The first error expects `buttonOk`
    after a map gesture against a peaceful colony, although the accepted
    behavior now opens the declaration/attack review. That open modal then
    blocks four following cases. Main changed TC33_02 to verify and cancel
    the ordinary attack review, then verify the complete world is unchanged
    before its existing explicit declaration and successful capture journey.
    Production code is unchanged. TC76 (10), TC26 (7), TC35 (12), TC32 (10),
    TC04 (1), basic/environment checks (6) and source contract (3) all pass.
    The TC33 repair was made after Docker had copied the tested sources;
    its separate rerun is required. No additional worker attempt was used.

70. The first main-owned TC33 repair rerun (`run-KOyAtj`) still fails five
    cases: TC33_02 reaches `AttackPage.verifyReview` without initializing its
    stored review window; four subsequent cases are blocked by the remaining
    modal. Source contract passes. This is a caller/test adaptation error,
    not a Claude attempt or a game failure. Main replaced both handwritten
    gestures with the existing `AttackPage.chooseAttack` operation, which
    performs the same player gesture and records the review window. Required
    assertions and production code are unchanged; a second focused rerun
    verifies the correction.

71. R-0096 final focused rerun `run-QeRl9J` passes all seven TC33 scenarios
    and the three source-contract checks, zero failures/errors/skips. Combined
    targeted acceptance is 2,231 distinct checks with production unchanged
    after the successful 2,175-model run. Final formatter checks 37 Java files
    with code-token/idempotence equality. The high/high/high session used
    2,095.724 worker seconds, including its quota interruption; main corrected
    the explicitly recorded test adaptations after the original budget was
    exhausted. This complex task does not justify high for every assignment.
    R-0101 starts on Sonnet medium; Opus remains unused.

72. R-0101 attempt 1 on Sonnet medium completed in 439.258 seconds, exit 0,
    with no denied tools. Main formatter checked 19 Java files (11 changed;
    token/idempotence checks pass). Docker `run-JU9QWf` compiled and ran 2,188
    model checks: 2,187 pass, one error, zero skips. `ColonyTest` uses a mock
    model without the difficulty required by the new political calculation;
    the repair must supply a real profile, not relax production validation.
    Separate UI diagnostic `run-SAOYhq` runs 22 checks: 20 pass, two TC67
    failures, no errors/skips. F58 has eight zero-liberty residents on
    Conquistador, so its new Tory penalty changes 3 hammers to 2; starting
    stock 49 now reaches 51 rather than the church's required 52. The unchanged
    chapel explains both failures. TC50/52/68/75/77 and source contract pass.
    Review also requires direct famine/snapshot acceptance, a negative-Tory
    player journey, and nonzero-support integration for unattended/breeding
    exclusions, percentage ordering and captured affiliation. Attempt 2 stays
    on Sonnet medium with this focused diagnosis and the original budget.

73. R-0101 attempt 2 on Sonnet medium completed in 251.967 seconds, exit 0,
    no denied tools; cumulative worker time is 691.225 seconds. Main formatter
    checked 24 Java files (four changed; token/idempotence pass). Model run
    `run-MhAec0` passes 2,206 of 2,209 checks, with three failures and no errors
    or skips. The new blacksmith tests begin with 100 tools at storage capacity
    100, then incorrectly expect 103/104 after the normal end-update trim.
    Final repair must create free storage while retaining expected output and
    input conservation. Famine and capture-affiliation checks pass. Review
    still needs a nonzero-support Penn integration, and random famine repeats
    must not be presented as guaranteed exhaustive victim coverage. The final
    original attempt escalates to Sonnet high for these repeated fixture and
    coverage misses; production has not changed since attempt 1.

74. R-0101 final attempt on Sonnet high completed in 37.396 seconds, exit 0,
    no denied tools; total worker time 728.621 seconds. It changed only model
    tests: free warehouse capacity, six real Penn/support combinations and
    one observed-victim famine check instead of six random repeats. Main
    formatter checked 24 Java files (one changed; token/idempotence pass).
    `run-97pdNe` passes 2,210 model and nine basic/contract checks. Unchanged
    selected UI evidence plus final `run-2ymlLP` establishes 2,239 distinct
    passing checks. Medium authored the implementation; high completed the
    bounded fixture/coverage repair. No main implementation takeover or Opus
    was used. Full batch acceptance/publication remains pending.

75. The final R-0101 documentation check caught a main-owned R-0096 catalogue
    edit missing the mandatory `I / V` prefix on UC-075. The earlier nine-test
    documentation check preceded that row edit; strict MkDocs alone did not
    detect it. Main restored `R / N` with the targeted evidence link and reran
    the catalogue tests and strict build. No game or worker change was needed.

76. R-0102 starts on Sonnet high because the agreed scope joins saved tax
    decisions, national-update resumption, market restrictions and UI. Attempt
    1 was interrupted by the Claude Pro session limit after 1,157.154 seconds,
    exit 1, no denied tools; advertised reset is 2026-10-18 at 16:00
    Europe/Prague. Session `c58cbe1f-fd86-430f-adc2-9cff54bd1737` retains two
    original attempts within the same 14,400-second budget. No extra usage
    was enabled. Model/GUI changes and three model-test classes are partial;
    TC78 and the related UI adaptations were not completed. No acceptance or
    publication is claimed.

77. Main applied the pre-agreed current-schema fixture update after the quota
    interruption: 72 tracked JSON files, 102 colonial tax states. It copies
    explicit old Crown rates into each matching colonial state, starts cadence
    counters at zero, removes the obsolete rate field and leaves the corrupt
    fixture untouched. Nine price-specific scenarios retain rates 17, 10 or
    25; ordinary game seeds remain zero. Dry-run and write-time self-checks
    confirm exact semantic equality apart from these fields, including Unicode
    preservation. Main formatter checked 36 Java files (nine changed; code-token
    and idempotence checks pass). An interrupted-state diagnostic is recorded
    separately; this mechanical work does not complete the worker assignment.

78. Interrupted R-0102 diagnostic `run-w0EKQp` compiled production and tests,
    then ran 2,245 model checks: 2,241 pass, one failure, three errors and no
    skips. UI did not run. Three Brewster save/load checks fail because the new
    GameManager tax-boundary condition rejects an immigration choice even when
    no tax demand exists. The protest conservation check differs by one corn
    after resuming production; repair must account for the protest's liberty
    effect without weakening stock-loss assertions. These are diagnosed open
    failures, not acceptance. The remaining worker scope includes TC78 and
    related UI coverage, gross bid-value selection, existing-boycott exclusion,
    shared AI acceptance and strict pending-state validation. Two original
    attempts remain; no main implementation takeover or publication occurred.

79. R-0102 resumed on 2026-10-18 after the advertised 16:00 Prague reset.
    Attempt 2 on Sonnet high completed in 521.981 seconds, exit 0, no denied
    tools; cumulative worker time is 1,679.135 seconds. Main formatter checked
    43 Java files (eight changed; token/idempotence pass). The caller's resume
    prompt named external evidence files without their absolute directory, so
    the worker could not find them and used the inline diagnosis and retained
    context. Future continuation prompts must contain exact absolute paths;
    this discovery overhead is a caller error, not a model-quality failure.
    Docker acceptance and review remain in progress; one original repair
    remains, and the full-regression/publication boundary is still requirement 20.

80. R-0102 run `run-czrcJC` passes all 2,248 model and 43 selected UI/basic/
    contract checks, with no failures, errors or skips. Review still found a
    weak custom-house proof: after protesting 100 of 200 ore, the remaining
    100 would not export even without a boycott. The final original repair
    strengthens it to 300 initial/200 remaining with exact gold, and selects
    the highest-value stock before constructing its bounded arrears quote, so
    overflow cannot silently substitute another commodity. Attempt 3 on Sonnet
    high completes in 96.862 seconds, exit 0, no denied tools; total worker
    time is 1,775.997 seconds. Main formatter checks 43 files (two changed;
    token/idempotence pass). Final affected acceptance is pending.

81. R-0103 starts on Haiku medium as a bounded payment flow over accepted
    R-0102 state. Attempt 1 completes in 325.904 seconds, exit 0, no denied
    tools. Formatter checks 12 Java files (six changed; token/idempotence pass).
    Docker `run-ZJdy4f` passes all 2,260 model checks and 22 of 25 UI/basic/
    contract checks, with one failure, two errors and no skips. All three TC79
    paths fail because `BackTaxDialog.refresh` calls `List.copyOf(...).contains`
    with a null initial selection, throwing on first opening; the missing-node
    errors are consequences. Review additionally requires removing one debt
    before emitting the gold-change event to prevent observable/reentrant partial
    payment, removing an unrequested human-only model restriction, and proving
    restored actual selling as well as buying. Attempt 2 stays on Haiku medium
    with the diagnosed fixes and original budget. Existing TC27/TC78 pass.

82. R-0103 attempt 2 on Haiku medium completes in 156.370 seconds, exit 0,
    no denied tools; cumulative worker time is 482.274 seconds. The worker
    guards the initial null selection, removes the debt before the synchronous
    gold event, adds a reentrant listener regression, removes the human-only
    restriction and extends TC79 to actual musket purchase/sale after a real
    protest and payment. Formatter checks 12 Java files (four changed;
    token/idempotence pass). All 2,261 model checks pass; UI acceptance is
    pending. No main implementation takeover or model escalation was needed.

83. R-0103 `run-IdGG0D` passes all 2,261 model and 24 of 25 UI/basic/
    contract checks, with one failure and no errors/skips. Real payment followed
    by purchase/sale and Continue passes, as does insufficient funds. TC79_03
    reveals stale port boycott styling after a rejected payment while another
    debt remains: `BackTaxDialog.reject` repaints Europe only when every debt
    is gone. The final original repair raises Haiku effort to high for this
    repeated UI-state omission, preserves all assertions, and repaints both
    branches. This is not a Sonnet/Opus escalation or a main takeover.

84. R-0103 final Haiku high repair completes in 11.484 seconds, exit 0,
    no denied tools; total worker time is 493.758 seconds. It changes only
    the dialog rejection repaint; formatter checks 12 Java files, none needing
    changes, with token/idempotence pass. Final `run-J1dNdH` passes all six
    TC79/source-contract checks. Combined unchanged model and UI evidence
    establishes 2,286 distinct passing checks. Haiku handled the bounded task
    with two repairs; this is an observed outcome, not a controlled cost
    comparison with Sonnet. Full batch acceptance/publication remains pending.

85. R-0085's remaining boycott/export proof completed on Haiku medium in one
    153.574-second attempt, exit 0, no denied tools. It changes only TC58 and
    its page fixture helpers, deriving the revolution case from an actual saved
    protest without reseeding debts or stock. Main formatter checks two Java
    files (one changed; token/idempotence pass). Docker `run-kp4OXZ` passes
    2,261 model and 15 UI/basic/contract checks with no skips. No main
    implementation takeover or production change was needed. The controlled
    revolution flag does not establish R-0105 declaration/muster acceptance.

86. R-0123 follows the user's 2026-10-18 lake decision: distinct identity,
    ordinary ocean ship access and fishing. Sonnet medium completes three
    attempts in 332.268, 140.895 and 43.416 seconds (516.579 total), exit 0
    with no denied tools. Initial 2,296 model checks expose one invalid foreign
    ship fixture assumption. Attempt 2 explicitly prepares the foreign ship
    and adds actual fishing-turn and combat-after-clearing evidence. Docker
    `run-PinZB3` passes all 2,300 model checks; 37 UI/basic/contract checks
    contain four failures: hidden terrain fixture, an unconfirmed Load-click
    problem, and two coastal construction cases. Attempt 3 reveals the scoped
    fixture tiles; it does not invent a cause for the Load failure. A focused
    rerun passes names/Continue and ships but repeats the pre-load failure.
87. The coastal F42 fixture has eight residents, zero liberty and Conquistador
    difficulty. R-0101 now correctly applies the eight-Tory production penalty,
    so the fixture's expected three hammers and fish no longer apply. After
    exhausting the original three worker attempts, main changes only its
    initial liberty to 200: one rebel, seven Tories, no bonus or penalty.
    This preserves the original expected output, construction and player
    assertions. The fixture repair and remaining startup issue require an
    actual rerun; no acceptance is claimed by this diagnosis.

88. The focused coastal rerun `run-3A9YzG` passes both TC51 scenarios after
    the F42 repair. TC80's third case still fails before loading. Two bounded
    focus experiments time out; diagnostic `run-bWDVWC` confirms the button
    is visible/enabled but its window cannot gain focus. Source tracing to
    the existing TC38 land-rejection journey finds the actual cause: TC80_02
    leaves the modal movement-rejection message open. Main removes the focus
    experiments and extends TC80_02 to verify and close that message through
    the existing `PassengerTransferPage`, preserving the ship-position check.
    This is a test lifecycle repair after worker attempts were exhausted, not
    a production workaround. Final TC80 and source-contract rerun is required.

89. Final `run-KPhjW4` passes TC80 (3) and the source contract (3), zero
    failures/errors/skips, including an actual fishing update and Continue.
    Unchanged model evidence and the targeted coastal/clearing/combat controls
    establish 2,337 distinct passing checks. Shared formatter checks all 13
    changed Java files with code-token/idempotence pass. Sonnet medium needed
    three attempts; main corrected the old F42 political fixture and the
    modal cleanup after the attempt budget. No Sonnet-high or Opus escalation
    occurred, and this mixed result is not evidence of autonomous completion
    or measured savings. R-0123 is targeted-accepted as item 19; full batch
    acceptance/publication and R-0105 remain pending.

90. R-0105 resumes after the user resolves royal/Continental infantry and
    cavalry defeat as destruction with equipment on 2026-10-18. Main records
    the shared rule in R-0178 and the affected active REF requirements and
    refines R-0105. Sonnet high is selected for declaration, exact muster,
    persistent roles and shared combat integration. Attempt 1 stops at the
    Claude Pro session limit after 1,063.654 seconds, exit 1, no denied tools;
    the reported reset is 21:00 Europe/Prague on 2026-10-18. This is a quota
    interruption, not a verified implementation failure. Two original attempts
    remain, with the same worktree and session; the total budget stays 14,400
    seconds. Partial implementation and model tests exist, but TC81/F73 has
    not been written. Main applies the formatter (27 Java files, 13 changed,
    token/idempotence pass) and runs a diagnostic model check. No main
    implementation takeover, full UI regression or publication is authorized
    by this partial result; the agreed batch remains 19 targeted-accepted.

91. Diagnostic Docker `run-PRMDVb` compiles the interrupted production/model
    tests and runs 2,335 checks: 2,334 pass, one fails, no errors/skips. The
    new weighted-support test expects 48%, but its actual colony populations
    are 5/1/6; seven residents at 78% and five at zero give 45.5%, displayed
    as 45%. The refusal below 50% remains correct. The UI module is not reached.
    Main prepares this arithmetic repair, completion of missing TC81/F73 and
    review of callback/pause and role-transition atomicity for the same worker
    session after quota reset. No failed check is called repaired, no partial
    R-0105 acceptance is recorded, and no new worker budget is started.


## Completion callbacks — 2026-10-18

1. [R-0243](../requirements/done/R-0243.md) adds host-side completion delivery;
   [operations](completion-callbacks.md) owns the commands and limits. The original
   worker launcher, model profiles, cumulative budgets and caller acceptance
   remain authoritative.
2. Local implementation job: `2026-10-18-r243-completion`, isolated baseline
   `288e3e91ca92d5b403c7bf3b8658b608556091e1`, medium/64k. The worker owns only
   the new runner and its portable tests; the caller owns requirements, workflow
   integration, documentation, actual checks and review. The candidate was not
   integrated; the caller subsequently implemented the runner directly.
3. Medium attempt 0 completed in 768.145 seconds (2,994 input / 5,568 output
   tokens), with the expected 98,304-token context and successful unload. It
   returned two code blocks rather than editing files. The caller matched the
   original skeleton hashes before extracting the blocks into the isolated tree.
   All six candidate tests failed; a direct CLI check raised `float.isfinite`
   `AttributeError`. Review also found an invalid queue option, blocking dispatch,
   ineffective test stubs, omitted Docker cleanup and CI identity/final-state
   validation. This result is rejected, not accepted from launcher exit zero.
4. Repair 1 used high/64k in the same job and worktree with unchanged budgets.
   The diagnosed omissions justify escalation; explicit text-only anchors remove
   the output-mode ambiguity. Actual checks and original acceptance remain
   unchanged. With no reviewable output after nearly an hour, the caller stopped
   the owned launcher with SIGTERM: 3,590.847 seconds, 4,358.992 cumulative worker
   seconds, exit -15 and successful model unload. The host lock was released.
   The broad multi-adapter assignment proved unsuitable for this worker; repeating
   it was not justified. `takeover-decision.json` preserves that diagnosis. The
   user then explicitly requested Codex for all remaining work. No further model
   inference or local-worker repair is used for this requirement.
5. Both attempt callbacks were enqueued to the originating desktop task; the
   first resumed it. The second reports cancellation, not an accepted repair.
   The caller's standard-library runner retains the original launchers and gates.
   A fresh review checked argument forwarding, process ownership, source/run/task
   identity, bounded cleanup and separate notification failure. The review changed
   cleanup to stop the Docker client before removing its owned container, avoiding
   a container-creation race; no test or gameplay assertion was weakened.
6. Production-runner live probes on 2026-10-18 were acknowledged by the exact
   isolated desktop task `01a15004-8364-7c93-baff-c384e0e07ebf`:
   worker preflight failure `20261018T184529Z-31438392f5e1` (exit 1, missing job
   metadata, no inference); Docker startup deadline `20261018T184529Z-62f86db22784`
   (15-second deadline, 31.467 seconds including cleanup, no leftover container);
   CI success `20261018T184549Z-0229709c7dc1` (existing Pages run 39124554783,
   exact commit `eb4422281e71c7abdbdf415c6bd04016785a6f7f`). Per-run evidence is
   retained under `~/Library/Logs/Microcol/completions/`. These prove callback
   delivery, not acceptance of the new source or a new publication.
7. The five existing Docker runner checks passed: success, test failure, export
   failure, read-only clean source with quoted argument forwarding, and report
   readability by an unprivileged Linux user. The final portable checks pass:
   19 completion-runner, 21 worker-launcher (stubs, no inference), 13 CI-classifier
   and 9 documentation tests. A first rerun exposed a 0.1-second stub-startup race
   under compilation load; the timeout fixture now permits startup and the
   detached-dispatch check uses explicit release-file synchronization. No required
   assertion was removed.
8. Successful production-runner Docker run `20261018T184842Z-f2fd9787ff77`
   finished in 84.631 seconds and its callback was acknowledged by the probe task.
   Report `run-P5uABi` contains 3 `PageVerificationContractTest` checks and 7
   `TC_18_F01_plow_field_test` checks, all passing with no errors or skips.
   No game source, fixture, page object or scenario assertion changed. The
   [functional evidence](../functional-tests.md#r-0243-completion-runner-2026-10-18)
   separates this targeted check from the unchanged full CI gate.
9. Live active-container timeout `20261018T185033Z-c6f3b02ec2d4` exposed a
   cleanup race: Docker's automatic removal was already in progress when explicit
   removal ran. The container disappeared, but the first version reported an
   error. The caller added a bounded retry for exactly that Docker response and
   case-insensitive missing-container recognition, plus a regression that preserves
   the command's failure result while checking successful cleanup. Replacement
   `20261018T185152Z-bb726d0b41ed` observed the real running container, reached its
   15-second deadline, completed cleanup in 25.467 seconds total, and confirmed
   the container absent before acceptance, with no cleanup errors. Its partial
   Maven log is retained; intentionally interrupted compilation is not a test pass.
10. Strict MkDocs and rendered-page/link inspection passed. The rejected worker
    worktree was archived with a recoverable snapshot; job/attempt evidence remains
    in the operator logs. Publication remains independently pending.
11. Commit `d829993d293fa6cbab138f38a271c7f4b7cd847d` was pushed to `main`.
    All three production CI callbacks reached the originating desktop task:
    Pages success (`20261018T185343Z-d877fb214b0a`), Java compatibility success
    (`20261018T185342Z-4633e4b64c4a`) and development failure
    (`20261018T185342Z-93631e663f79`). Pages build/deployment and the live page's
    source link match that commit. Each of the four Windows/Linux Java 11/17
    combinations passes 2,299 tests without failures/errors/skips. The failed
    callback also resumed this task with the exact run/commit identity retained.
12. Development #174 passes 32 launcher/publication tests and 2,332 tests on each
    of three native platforms, all without failures/errors/skips. Full Docker
    regression finds five older-fixture failures described in
    [R-0101's evidence](../functional-tests.md#r-0101-political-production-2026-10-18).
    CI correctly skips publication. The downloaded channel manifest still names
    `dev-171-333de9b6bf8f`; no #174 release exists. Codex owns the bounded fixture
    repair and reruns; no local worker is restarted and no gate is weakened.
13. The fixture repair passes all 45 targeted checks in Docker `run-LdfWrZ`
    and delivers its success callback to the original desktop task. Detailed
    counts and unchanged assertions are recorded with R-0101's functional
    evidence. Documentation checks and strict MkDocs pass; full CI on the
    repaired source remains required before publishing a new game build.
