# MicroCol

[![Development builds](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml/badge.svg?branch=main)](https://github.com/jajir/microcol/actions/workflows/dev-builds.yml)
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
