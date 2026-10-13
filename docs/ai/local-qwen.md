# Codex and a local Qwen worker

Status as of **2026-10-11: direct `codex exec` ready for bounded tasks; Qwen is
the default for suitable development assignments**. Astra supervises a separate
Codex CLI process using local Qwen, runs tests and reviews the changes. The test
pilot and subsequent implementation and packaging tasks below establish a
working path, with main-agent corrections needed on some tasks. No custom runner
service or MCP server was developed. The accepted pilot test changes were
incorporated into `develop`, the branch selected for development at that time. Development
now uses `main`; follow the current [branch workflow](../../AGENTS.md) when
selecting a baseline and accepting new worker changes. The isolated worktree and
artifacts retain the original evidence. These bounded tasks do not establish
broad reliability or measured savings.

The arrangement is **cloud Astra as the main agent and local Qwen3.5-9B Q4_K_M
on the 16 GB Mac mini**. Preserve the main agent's model and highest supported
reasoning setting. The goal is more correctly completed work within the same
Codex budget; no savings percentage has been measured.

The user approved revised [time budgets](#time-budget-2026-10-12) on **2026-10-12**:
five minutes per attempt and ten minutes of cumulative worker time per task.
The [failure analysis](#failure-analysis-2026-10-12) below reviews the earlier
attempts. A subsequent [recovery check](#recovery-check-2026-10-12) used the new
limits and verified a targeted repair, not a general success-rate improvement.

## Instruction ownership

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
| [Worker Modelfile](../../.codex/local-worker/Modelfile) | Local alias using the installed weights with the pilot context/output limits. |
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
   was attempted. Acceptance belongs to [R-0011](../requirements/R-0011.md).

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
   before Maven; Ollama remains running as requested. Actual player evidence and
   publication belong to the [functional audit](../functional-tests.md#free-play-audit-2026-10-13).

## Default development workflow — 2026-10-11

User and main agent agree player goal, rules, edge cases, cancellation/failure
behavior, and observable acceptance criteria before implementation. Main agent
decomposes larger features into small complete tasks and prefers local Qwen for
suitable implementation, behavior tests, bug fixes, repetitive refactoring, and
documentation/configuration edits within agreed interfaces. No extra user
approval per suitable task.

Main agent keeps architecture, unresolved game rules, scope, actual test
execution/evidence, review, integration and publication. Directly handle
trivial edits when delegation costs more; explain other skipped/degraded
delegations briefly.

Send compact English task contracts and relevant code only; do not include full
chat history or duplicate implementation in parallel. Qwen edits its isolated
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

Broader features are sequences of bounded jobs, not proof that current 8k
worker can own whole features. Expand assignments from accepted evidence.
UI changes require the caller verification plan in the
[functional test contract](../functional-tests.md). The dated plowing task below
records local execution and its environment limits; the complete suite is a
required Linux/Xvfb publication check.

For each result report what Qwen did, main-agent repairs, actual checks and
pending checks. Record attempts/time/available usage; no measured savings
percentage yet.

Every failed or incomplete attempt follows the mandatory
[failure and recovery loop](#failure-and-recovery-loop). Simple repairable errors
go back to Qwen while budgets remain; taking over the implementation is not the
default response to the first failure.

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

### Operator commands

Use `brew services run ollama` to run the service without registering it at login,
and `brew services stop ollama` to stop it. If the user later requests login
autostart, `brew services start ollama` enables it; that option has not been used.
These service behaviors are described in the [Homebrew manual](https://docs.brew.sh/Manpage#services-subcommand).

Inspect the installed model with `ollama list` and
`ollama show qwen3.5:9b-q4_K_M`; use `ollama --version` for the runtime and
`ollama ps` for loaded models. A direct interactive request uses
`ollama run qwen3.5:9b-q4_K_M`; it is not a delegated agent task.
`ollama stop qwen3.5:9b-q4_K_M` unloads that model while leaving the server
running. For the current worker alias, unload with
`ollama stop microcol-qwen:9b-q4_K_M-8k` before Maven.

## Selected direct CLI workflow

Use the installed Homebrew Codex CLI as the worker and the caller's existing
shell tools to supervise it. MCP and a custom in-session subagent are not needed
for this route. No persistent user Codex configuration has been changed.

### Operating profile — bounded assignments

| Setting | Current choice and limit |
| --- | --- |
| Runtime/provider | Ollama 0.40.2 on loopback, Homebrew Codex CLI 0.142.3, `--oss --local-provider ollama`. |
| Model | Base `qwen3.5:9b-q4_K_M`; local alias `microcol-qwen:9b-q4_K_M-8k` uses the same installed weights without another download. Alias digest `dfe6877b76ae37d766653b62909426fc769288fc42575cb5069c2b9b366a79a2`. |
| Model allocation | [Modelfile](../../.codex/local-worker/Modelfile): `num_ctx 8192`, `num_predict 2048`. Loaded-model evidence confirmed context 8192 in the accepted pilot. |
| Codex metadata | `model_context_window=8192` describes the window to Codex; it does not set Ollama's allocation. Confirm actual context and identity in Ollama evidence. |
| Reasoning | Non-thinking with the three CLI settings shown below; the accepted pilot reported zero reasoning events/tokens. Do not copy Astra's `ultra` setting to the worker. |
| Concurrency | One active local request, serialized across sessions using a host-wide lock. Unload the worker model before Maven; no concurrent heavy tests. |
| Limits | **300 seconds (5 minutes) per attempt**, **600 seconds (10 minutes) cumulative worker time per task**, with at most **two repair rounds** and only while budget remains. The caller enforces both limits; see the accounting rules below. |
| Language/context | English code and project instructions; Czech user requests remain welcome. Give the worker a small complete task, inline relevant rules and code, without full skill/app catalogs or conversation history. Split tasks that do not fit. |

The alias was created from the project Modelfile with:

```sh
ollama create microcol-qwen:9b-q4_K_M-8k -f .codex/local-worker/Modelfile
```

Do not run setup on every delegation. The base and alias both report weight blob
`sha256-02d45dc1cf451ba2475ac33b301c2dd8f985abe4c182ce04a1f2f5bf0260278d`.
This alias changes operating parameters, not weights or the main configuration.
The small context remains an experiment: [Ollama's Codex guidance](https://docs.ollama.com/integrations/codex)
recommends substantially more context, while the 8k installation checks above
already caused swapping on this machine. Keep task size and memory use bounded.

### Time budget — 2026-10-12

These are the approved limits for **future dispatches**, replacing the previous
180-second attempt limit. Historical job scripts and reports remain unchanged.
There is no persistent runner or Ollama setting enforcing wall-clock deadlines:
the caller supervising each direct `codex exec` process must enforce them.
Do not copy the old `180` constant from archived per-job runners.

- Before the first attempt, assign a stable logical task ID and start its
  cumulative worker time at zero. Record each attempt's elapsed time using a
  monotonic clock, including process startup/model loading, tool calls and model
  generation. Charge failed starts, cancelled attempts and repairs too.
- Before every attempt, subtract previously used worker time from **600 seconds**.
  Do not start if none remains. The attempt's allowed runtime is the smaller of
  **300 seconds** and the remaining task budget. Record these values with the
  task input and executed command so the worker and caller share the same limit.
- Repairs, restarted processes and differently named attempt directories retain
  that same task budget. The existing maximum of two repair rounds is an upper
  bound, not an entitlement to another full attempt. Two 300-second attempts
  exhaust the task budget; a third attempt is not allowed.
- On either deadline, stop the owned process/request, unload the model and keep
  the partial diff and logs. Cleanup may finish slightly after the deadline;
  record and charge the actual elapsed time before considering another attempt.
  If prior elapsed-time evidence is missing, reconstruct it before dispatching
  again rather than assuming a fresh budget.
- Caller preparation, review, Maven/tests and publication are outside this
  **worker-runtime** budget. Do not run heavy verification concurrently with Qwen.
  Repeated reads or errors without progress can justify stopping earlier; more
  time is not a reason to continue an unchanged failing loop.

For example, 180 seconds already used leaves a 300-second attempt available;
480 used leaves only 120 seconds; 600 used permits no further attempt. This is
a caller-enforced workflow change, not a claim that the longer budget improves
success rates. The model, 8k context, output limit and concurrency are unchanged.

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
   For a simple in-scope worker error, give Qwen the relevant failing fragment,
   traceback/current diff and a focused repair task. Keep required behavior and
   checks unchanged. Do not dump the full previous conversation or repeat an
   unchanged failing prompt. Do not silently replace Qwen's implementation.
3. **Rerun within the same budget.** Dispatch the corrected setup or repair with
   the same logical task ID, remaining cumulative runtime and repair count.
   The caller must attempt a feasible, safe repair while those limits permit it;
   no extra user approval is needed. A new directory is not a new allowance.
   If the runtime is unavailable, scope is unclear, retry would be unsafe, or
   budgets are exhausted, record that concrete reason rather than retrying
   indefinitely. Main-agent takeover must explain why another worker repair
   was not appropriate.
4. **Verify the original failure and the result.** After unloading Qwen, the
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
   independently successful Qwen task.

This is enforced by the main agent's dispatch and acceptance procedure through
`AGENTS.md`, the worker skill and task/result contract. It is **not** an unattended
retry service or a guarantee of model correctness. The evidence and explicit
dispositions make compliance reviewable; they do not replace actual verification.

### Prepare, invoke, verify

1. Read the [local-worker skill](../../.agents/skills/local-worker/SKILL.md), current
   Git state and relevant code. Choose a unique task ID. Record the baseline and
   include any relevant uncommitted inputs deliberately; a worktree of `HEAD`
   alone does not include them. Create a detached isolated worktree under
   `~/Library/Application Support/Microcol/local-worker/worktrees/<task-id>`.
2. Store task input, copied `worker-role.md`, command, events, result and test
   reports under `~/Library/Logs/Microcol/local-qwen/jobs/<task-id>/`. Fill the
   existing task template, including scope, expected behavior, both time budgets,
   previous attempts' elapsed time and test ownership. Create the child-specific
   `codex-home` directory before dispatch. Inline applicable project rules; the
   child must receive them even when the source documents are untracked in the
   main checkout.
3. Claim `~/Library/Application Support/Microcol/local-worker/active.lock` with an
   atomic directory creation before starting. If it already exists, inspect its
   owning task/process; do not start a second job or remove another caller's
   lock. Record the task and owned process there. Hold the lock through worker
   execution and caller verification, then release your lock after cleanup.
4. Run the command below in the assigned worktree. Capture stdout events, stderr,
   final response and exit code. Enforce the effective attempt deadline and shared
   task budget from the calling session using the accounting rules above;
   this command has no built-in timeout wrapper. On timeout or interruption,
   stop the owned worker and any still-active local request, unload its model,
   and preserve the partial diff and logs. Never replace failed local inference
   with a cloud model silently.
5. Inspect the actual diff against scope, unload the alias, and run the specified
   Maven command on that worktree. Require a fresh Surefire report with the
   intended tests and exact counts, exit codes, failures and skips. Send concrete
   failures back only within the repair limit and remaining task budget. The
   worker must report its tests as pending caller verification, not claim they
   passed.
6. Astra reviews behavior and test meaning, checks the main checkout's current
   state, and integrates accepted changes within the assigned scope. The worker
   does not commit, merge or push its result. The main agent records repairs and
   uncertainty, then completes the authorized checks, commit, push to `main` and
   publication verification from [AGENTS.md](../../AGENTS.md). An explicitly
   isolated pilot may stay isolated when that is the agreed scope.

Example invocation, after setting `worker_tree` and `worker_job` to the prepared
absolute paths. The quoted, fail-fast preflight creates the child home and checks
the required inputs before dispatch. These variables are caller-local;
`CODEX_HOME` below applies only to the child process:

```sh
test -d "$worker_tree" && test -r "$worker_job/task.md" \
  && test -r "$worker_job/worker-role.md" \
  && mkdir -p "$worker_job/codex-home" || exit 1

env CODEX_HOME="$worker_job/codex-home" \
  /opt/homebrew/bin/codex -a never exec \
  --oss --local-provider ollama -m microcol-qwen:9b-q4_K_M-8k \
  --ignore-user-config --ephemeral -s workspace-write -C "$worker_tree" \
  --json --color never -o "$worker_job/response.md" \
  -c "model_instructions_file=\"$worker_job/worker-role.md\"" \
  -c model_context_window=8192 \
  -c features.multi_agent=false -c skills.include_instructions=false \
  -c features.plugins=false -c features.apps=false -c 'web_search="disabled"' \
  -c 'model_reasoning_effort="none"' \
  -c model_supports_reasoning_summaries=true \
  -c 'model_reasoning_summary="none"' \
  -c tool_output_token_limit=1000 - \
  < "$worker_job/task.md" > "$worker_job/events.jsonl" 2> "$worker_job/stderr.log"
```

Preserve all three reasoning settings together. A wire check found that without
`model_supports_reasoning_summaries=true`, this CLI sent `reasoning: null` despite
requesting effort `none`; the full set sends the intended reasoning controls.
This compatibility detail belongs to the checked CLI version, not to Astra.
`workspace-write` supplies the execution restriction; an isolated worktree and a
post-run diff check alone are not a filesystem sandbox.
The CLI emitted fallback model-metadata warnings for the local model. This is
an observed metadata limitation, not model substitution: the local model and
digest were checked through Ollama's runtime evidence.

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
original broad discovery task. Keep the 8k context for a comparable trial and
record useful edits, accepted results, caller repair time and budget consumption
for the next 3–5 assignments under the new limits. This initial read-only diagnosis
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
| Accepted retry | `retry-8k/command.json` records the exact command above. CLI exit **0**, **69.0 s**. Qwen changed only `GoodsTradeTest.java`: six assigned parameter rows and explanatory comments; original assertions and cases remained intact. |
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

A completed delegation requires **Astra task → verified local Qwen call → actual
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
