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

## Instruction ownership

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

Broader features are sequences of bounded jobs, not proof that current 8k
worker can own whole features. Expand assignments from accepted evidence.
UI changes require the caller verification plan in the
[functional test contract](../functional-tests.md). The dated plowing task below
records local execution and its environment limits; the complete suite is a
required Linux/Xvfb publication check.

For each result report what Qwen did, main-agent repairs, actual checks and
pending checks. Record attempts/time/available usage; no measured savings
percentage yet.

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
| Limits | **180 seconds per attempt**, with at most **two repair rounds** after the first implementation. The caller enforces the deadline, stops the owned process and retains evidence. |
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

### Prepare, invoke, verify

1. Read the [local-worker skill](../../.agents/skills/local-worker/SKILL.md), current
   Git state and relevant code. Choose a unique task ID. Record the baseline and
   include any relevant uncommitted inputs deliberately; a worktree of `HEAD`
   alone does not include them. Create a detached isolated worktree under
   `~/Library/Application Support/Microcol/local-worker/worktrees/<task-id>`.
2. Store task input, copied `worker-role.md`, command, events, result and test
   reports under `~/Library/Logs/Microcol/local-qwen/jobs/<task-id>/`. Fill the
   existing task template, including scope, expected behavior, deadline and test
   ownership. Inline applicable project rules; the child must receive them even
   when the source documents are untracked in the main checkout.
3. Claim `~/Library/Application Support/Microcol/local-worker/active.lock` with an
   atomic directory creation before starting. If it already exists, inspect its
   owning task/process; do not start a second job or remove another caller's
   lock. Record the task and owned process there. Hold the lock through worker
   execution and caller verification, then release your lock after cleanup.
4. Run the command below in the assigned worktree. Capture stdout events, stderr,
   final response and exit code. Enforce the deadline from the calling session;
   this command has no built-in timeout wrapper. On timeout or interruption,
   stop the owned worker and any still-active local request, unload its model,
   and preserve the partial diff and logs. Never replace failed local inference
   with a cloud model silently.
5. Inspect the actual diff against scope, unload the alias, and run the specified
   Maven command on that worktree. Require a fresh Surefire report with the
   intended tests and exact counts, exit codes, failures and skips. Send concrete
   failures back only within the repair limit. The worker must report its tests
   as pending caller verification, not claim they passed.
6. Astra reviews behavior and test meaning, checks the main checkout's current
   state, and integrates accepted changes within the assigned scope. The worker
   does not commit, merge or push its result. The main agent records repairs and
   uncertainty, then completes the authorized checks, commit, push to `main` and
   publication verification from [AGENTS.md](../../AGENTS.md). An explicitly
   isolated pilot may stay isolated when that is the agreed scope.

Example invocation, after setting `worker_tree` and `worker_job` to the prepared
absolute paths and creating the job's `codex-home` directory. These variables are
caller-local; `CODEX_HOME` below applies only to the child process:

```sh
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
