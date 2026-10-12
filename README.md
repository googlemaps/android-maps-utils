# Coverage history

Generated data. Do not edit by hand, and do not open pull requests against
this branch.

| File | Contents |
| --- | --- |
| `history.csv` | One row per module per merged commit (unit tests). |
| `COVERAGE.md` | Human-readable report, regenerated from `history.csv`. |

Both are written by `.github/scripts/coverage_history.py` on `main`, run by the
`Record coverage history` workflow after every merge to `main`.

This data lives on its own branch rather than on `main` on purpose. Branch
protection on `main` uses strict required status checks, so every commit
pushed there marks all open pull requests out-of-date and forces contributors
to update their branch and re-run CI. Keeping coverage data here means
recording it costs contributors nothing.
