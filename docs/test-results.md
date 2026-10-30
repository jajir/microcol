# Main test results

The repository badges show two groups from the latest full nightly or manually
requested regression of `main`: model/JUnit and full UI/support. The UI count is
the complete `microcol-test` selection, not the small pre-commit smoke selection.
Java compatibility checks still run separately on relevant pushes; their
[workflow status](https://github.com/jajir/microcol/actions/workflows/java-compatibility.yml?query=branch%3Amain)
is also visible in the repository README. Their repeated platform executions
are not added to the nightly model count.

Green requires complete passing reports for the displayed commit. Failed tests
and errors are red; skipped tests, missing reports, cancellation and a newer
`main` are amber. The badge's failed count includes assertion failures and test
errors; the report separates them. Missing results use “unknown”, never zero.
Counts in an incomplete run describe only the available reports.

Every Pages build retrieves the latest attempted full run, including failures.
Skipped no-change nights retain the previous result. Full regression start and
completion also refresh Pages. A failed Pages deployment or image caching may
delay an update: use the linked Actions run for the authoritative current state.
No test-result commits are written to `main`.

Reports retain the compact counts and failed-case list for 90 days. Raw XML,
logs and UI screenshots are downloadable from the linked Actions run for 14 days;
downloading Actions artifacts requires signing in to GitHub. Expired or missing
summaries are explicitly unknown. See [nightly execution](development-builds.md#ci-filtering).

The live section below is generated during Pages publication from Actions
artifacts. A local documentation build contains this explanation only.

---
