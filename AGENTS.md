# AGENTS.md

Guidance for AI coding agents working on this repository. For human contribution
rules (CLA, PR process, AI-assisted contribution policy), see [CONTRIBUTING.md](CONTRIBUTING.md).

## Project overview

The Maps SDK for Android Utility Library, split into focused submodules so
apps can depend only on what they need. `maps-utils` is the aggregator
artifact that pulls in all submodules; see [MIGRATION.md](MIGRATION.md) for
the 4.x to modular layout mapping.

| Module | Purpose |
| --- | --- |
| `library` | Core utilities shared by the other modules |
| `clustering` | Marker clustering |
| `heatmaps` | Heatmap tile overlays |
| `data` | GeoJSON and KML parsing/rendering |
| `ui` | UI helpers (icon generators, bubbles) |
| `maps-utils` | Aggregator artifact, transitively includes the modules above |
| `lint-checks` | Custom Lint rules shipped with the library |
| `demo` | Demo app exercising the utilities |
| `visual-testing` | Screenshot/visual regression test harness |

Shared Gradle conventions are in `build-logic/` (included build).

## Building and testing

```bash
./gradlew assembleDebug                       # build everything
./gradlew :clustering:testDebugUnitTest       # unit tests for one module
./gradlew test                                # all unit tests
./gradlew lint                                # Android Lint (includes lint-checks rules)
```

Running the `demo` app requires a Maps API key in `secrets.properties` at the
repo root (see `local.defaults.properties` for the template). Never hardcode
or commit API keys.

## Code style

- Match the language and idiom of the file you are editing; new code is
  Kotlin unless it extends an existing Java API surface.
- Formatting follows `.editorconfig`.
- KDoc/Javadoc on all public classes and methods; this is a widely consumed
  library and its docs are published.
- Public API changes must be additive and backward compatible; deprecate
  before removing. Breaking changes only in a major release with a
  MIGRATION.md entry.
- Put a change in the most specific submodule, not `library`, unless it is
  genuinely shared.

## Pull requests

- Use Conventional Commit messages (`feat:`, `fix:`, `docs:`, ...).
  release-please parses them to generate versions and CHANGELOG.md; a wrong
  prefix causes a wrong release bump.
- Never edit CHANGELOG.md or `.release-please-manifest.json` by hand.
- Every behavior change needs a unit test in the affected module.
- Run the module's tests and `lint` before declaring work done, and report
  actual results.
- Do not add dependencies to library modules without discussion in an issue
  first.
- AI tools must not be listed as authors or co-authors on commits or PRs, and
  unsolicited bot-generated PRs are prohibited (see CONTRIBUTING.md).
