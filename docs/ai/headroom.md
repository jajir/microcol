# Headroom for Codex and Claude Code

[R-0244](../requirements/done/R-0244.md) records the user's **2026-10-18** decision
to use Headroom by default on the development Mac mini. This is a host setup,
not a dependency of the game, Docker tests or GitHub Actions. Required tests,
review, repair budgets and [completion callbacks](completion-callbacks.md) stay
in place.

## Configured scope

1. Headroom **0.41.0**, installed with the `proxy,code` extras in
   `~/.local/share/microcol/headroom/venv`, serves only `127.0.0.1:8787`.
   `~/.local/bin/headroom` points into that environment. Its native persistent
   service profile is `microcol`; macOS launchd starts it at login and restarts
   it after exit. No Docker service, cloud proxy or shell-wide API override was
   added.
2. `~/.codex/config.toml` has this **root-level** setting:

   ```toml
   openai_base_url = "http://127.0.0.1:8787/v1"
   ```

   The built-in `openai` provider, existing model/reasoning preferences and login
   remain unchanged. We deliberately do not use Headroom's Codex provider
   installer, which changes provider identity and retags existing threads.
   This user setting applies to supported clients that load this Codex home;
   a separate `CODEX_HOME`, explicit provider/URL override or remote host can
   bypass it. Already running desktop tasks are not proven to reload it.
3. The native installer adds the following entries to the existing
   `~/.claude/settings.json` environment, preserving other settings:

   ```json
   {
     "ANTHROPIC_BASE_URL": "http://127.0.0.1:8787",
     "ENABLE_TOOL_SEARCH": "true"
   }
   ```

   Tool search preserves Claude Code's deferred tool schemas with a custom
   endpoint. Existing claude.ai subscription login and per-task model/effort
   selection remain intact; no API key or credential helper was introduced.
   Claude web/desktop, connected review services and unrelated SDK callers
   are outside this verified scope.
4. The configured local Ollama worker keeps its own provider and URL. Do not
   route local inference through Headroom merely because cloud clients use it.
   This setup does not change who implements a task or which reviewer it needs.

The provider settings follow the official
[Codex configuration reference](https://developers.openai.com/codex/config-reference)
and [Claude Code gateway authentication](https://code.claude.com/docs/en/llm-gateway).
The service uses [Headroom's persistent installer](https://docs.headroomlabs.ai/docs/persistent-installs).

## Service settings and operation

The pinned Python installation uses **cache mode**, CPU/ONNX compression and the
already downloaded tokenizer/model caches. Shared memory, output shaping,
traffic-learning service, telemetry uploads, OpenTelemetry export and full-message
logging are disabled. No Headroom hooks, MCP server or instruction-learning
workflow was installed. Built-in compression/cache feedback counters may still
operate locally; this is not a claim that every adaptive mechanism is disabled.

The applied native installation command was:

```sh
headroom install apply --preset persistent-service --runtime python \
  --scope provider --providers manual --target claude --profile microcol \
  --port 8787 --mode cache --backend anthropic --no-telemetry --no-rate-limit \
  --env HEADROOM_OUTPUT_SHAPER=0 --env HEADROOM_MEMORY_ENABLED=0 \
  --env HEADROOM_BEACON=off --env DO_NOT_TRACK=1 \
  --env HF_HOME="$HOME/.local/share/microcol/headroom/huggingface-cache" \
  --env TIKTOKEN_CACHE_DIR="$HOME/.local/share/microcol/headroom/tiktoken-cache"
```

This command configures Claude; the native Codex setting above is managed
separately. The installation manifest is
`~/.headroom/deploy/microcol/manifest.json`, with a generated launcher in that
directory and `~/Library/LaunchAgents/com.headroom.microcol.plist`.
Do not copy credentials into project configuration or commit host backups.

```sh
headroom install status --profile microcol
headroom install restart --profile microcol
curl --fail --silent http://127.0.0.1:8787/health
```

Restart only when client requests are idle. Recovery restores the service; it
does not safely replay an interrupted edit or prove a task completed. Diagnose
the saved client/service error and inspect the working tree before continuing.
Keep raw files, diffs, test XML and logs as the acceptance source. When omitted
context matters, reread the relevant source or use the direct path below;
compression is not proof that all original information reached the model.

### Direct bypass and rollback

For one Codex CLI session using the current ChatGPT login:

```sh
codex -c 'openai_base_url="https://chatgpt.com/backend-api/codex"'
```

For one Claude Code session using the current subscription login:

```sh
claude --settings '{"env":{"ANTHROPIC_BASE_URL":"https://api.anthropic.com"}}'
```

These explicit URLs were checked with the installed clients. They are not a
silent fallback or permission to repeat a command that may already have edited
files. Existing model/effort choices still apply.

To disable persistently, first remove **only** the root `openai_base_url` entry
that points to this proxy from `~/.codex/config.toml`. Then use
`headroom install stop --profile microcol` to stop the service and restore its
managed Claude settings; `headroom install remove --profile microcol` removes
the service installation. Confirm settings before opening new client sessions.
The installer does not own our native Codex entry and cannot remove it for us.
Do not overwrite newer settings with a whole old backup. A later service start
reapplies the managed Claude routing; re-enabling Codex remains a separate edit.

## First A/B pair — 2026-10-18

The R-0101 neutral-politics fixture repair was run in two isolated copies of
historical commit `d829993d293fa6cbab138f38a271c7f4b7cd847d`, with identical
prompts, Codex **0.162.1**, **gpt-6-astra/xhigh**, fresh sessions and no local
worker. A ran directly; B used Headroom 0.41.0 cache mode. The experiment changed
neither `main` nor global client settings.

| Measurement | A: direct | B: Headroom |
| --- | ---: | ---: |
| Input tokens | 1,253,721 | 912,109 |
| Cached input tokens | 1,169,664 | 850,048 |
| Uncached input tokens | 84,057 | 62,061 |
| Output tokens | 8,729 | 8,285 |
| Total input + output | 1,262,450 | 920,394 |
| Agent elapsed seconds | 218.070 | 240.155 |
| Agent + Docker + docs seconds | 391.028 | 410.685 |
| Targeted checks, failures/errors/skips | 45; 0/0/0 | 45; 0/0/0 |

Both produced the same four fixture-value repairs, retained every assertion and
passed nine documentation tests plus strict MkDocs. Caller review accepted both;
the review was **not blinded**. Per-response provider usage sums match each CLI
total. Reasoning tokens are already within output, not added again.

B used **27.1% fewer total tokens** and **26.2% fewer uncached input tokens**,
but took **10.1% longer for the agent** and **5.0% longer overall**. The proxy's
22,730-token direct compression estimate is a different measurement from the
cumulative A/B difference. No dashboard dollar estimate or exact subscription
quota saving is claimed.

This is one pair, in A-then-B order, with nondeterminism, provider caching and
host-load effects. Initial installation/model preparation took about 686 seconds
and overlapped A; a prewarm exit hang was diagnosed and stopped before B's
measured attempt. No measured repair attempt was discarded. Timings exclude
coordinator preparation/review and smoke requests. These historical copies did
not run full game publication. The initial recommendation was more balanced
pairs; the user subsequently chose general adoption with these limitations.

Local evidence: `~/Library/Logs/Microcol/headroom-ab/r0101-pair1/`, including
`protocol.json`, `comparison.json`, both review records and raw test reports.

## Rollout verification — 2026-10-18

1. Host settings were backed up locally. Semantic comparison preserved all
   pre-existing Codex preferences; the only native Codex change is its base URL.
   Claude's existing theme/login/model choices were retained. The service
   manifest selects loopback, cache mode, disabled memory/telemetry and the
   explicit environment above. Runtime statistics confirm no telemetry shipping,
   traffic learner, output shaping or full-message logging.
2. Actual Codex and Claude Code requests read a 400-line file with three rare
   error markers and returned all three correctly. Proxy records identify
   `gpt-6-astra` and `claude-opus-5-5`, with no failed provider requests. Codex's
   tool output was compressed; Claude's file-read output was protected by the
   router while its tool schema was compacted. This verifies useful routing,
   not that every tool result is compressed or every task becomes cheaper.
3. The first smoke harness rejected Codex's additional required Ponytail footer
   despite the correct answer. The raw failure remains recorded. The corrected
   check compares all marker identities and their order, allowing unrelated
   personal footer text; it does not change game checks or accept missing markers.
4. After SIGTERM of the owned service process, launchd restored a healthy
   replacement in **4.344 seconds**. Both clients then repeated the file test
   successfully. Both explicit direct-provider commands also succeeded, with
   **zero proxy requests** during those bypass checks. Runtime socket inspection
   confirms only `127.0.0.1:8787` is listening. Client settings still match the
   intended changes after recovery. The 46.958-second verification completed
   successfully; its saved callback record names the originating task UUID.
5. The currently open desktop task is not a live-routing proof: new sessions or
   an app restart may be needed for settings to load. No app restart is forced
   during ongoing work. Login startup is configured by launchd; actual logout,
   reboot and sleep/wake were not tested.
6. All **49 portable checks** passed: 19 completion-runner checks, 21 worker
   launcher checks without inference and nine documentation checks. Strict
   MkDocs passed. Caller review checked the actual host settings, installed
   lifecycle behavior, raw smoke results and the documentation diff. No Java,
   game fixture, gameplay assertion, CI workflow or required gate changed.

Local rollout evidence and private backups are in
`~/Library/Logs/Microcol/headroom-rollout/2026-10-18/`. This change updates host
tooling and documentation only. Publication checks Pages and intentional
[docs-only game-job skips](../development-builds.md#ci-filtering); it does not
request a new game distribution.
