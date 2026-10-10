# Branch workflow

Use `develop` for ongoing development and integration unless the user requests
another branch. Base isolated local-worker worktrees on the agreed `develop`
revision and bring their accepted changes back to `develop`.

# Local worker

When a local worker is configured and available, the main agent uses the
[local-worker skill](.agents/skills/local-worker/SKILL.md) for suitable bounded
implementation and test tasks, and owns their specification and acceptance.
Delegated local workers complete their assigned scope without further delegation.
Check [operational status](docs/ai/local-qwen.md) before use; an unavailable worker
does not trigger setup. Keep setup details and operating values in that document.
