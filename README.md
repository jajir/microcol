# MicroCol

[![Model / JUnit counts](https://jajir.github.io/microcol/test-results-model.svg)](https://jajir.github.io/microcol/test-results.html)
[![Full UI test counts](https://jajir.github.io/microcol/test-results-ui.svg)](https://jajir.github.io/microcol/test-results.html)

Counts describe the latest full regression of `main`, including failures and skips.
Click either badge for the tested commit, date, failed tests and run evidence.
Amber means a newer `main`, an incomplete run or unavailable results; it is not a pass.
[Run the full tests manually](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml): select `main` and leave game publication unchecked.
[![Push model checks](https://github.com/jajir/microcol/actions/workflows/java-compatibility.yml/badge.svg?branch=main)](https://github.com/jajir/microcol/actions/workflows/java-compatibility.yml?query=branch%3Amain)

[![Full regression tests](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml/badge.svg?branch=main)](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml)
[![Documentation](https://github.com/jajir/microcol/actions/workflows/docs.yml/badge.svg?branch=main)](https://github.com/jajir/microcol/actions/workflows/docs.yml)

Colonisation remake

[Current documentation](https://jajir.github.io/microcol/) is maintained in
[`docs/`](docs/index.md) and published automatically from `main`.
Development, integration and publication use `main`.
The [`microcol-site`](microcol-site/) source module remains in the repository as
a development reference; it is excluded from website publication.
See [modules and build outputs](docs/modules.md) and
[writing and publishing documentation](docs/publishing.md).

Build self-contained macOS and Linux packages with Maven and JDK 17; see
[distribution build commands and outputs](docs/distribution.md).

For everyday testing, install [MicroCol Dev](docs/development-builds.md) once.
It downloads the latest successful development build when opened; published
installers are also available from the [development channel](https://github.com/jajir/microcol/releases/tag/dev).
