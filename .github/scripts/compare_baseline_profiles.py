#!/usr/bin/env python3
#
# Copyright 2026 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Compare regenerated Baseline Profiles with the committed ones.

Used by the Regenerate Baseline Profiles workflow. For each module it reads the
committed profile (copied to --before before regenerating) and the regenerated
one in the working tree, then:

- fails if a module lost most of its rules. A profile generated without a valid
  Maps API key, or from demos that close themselves, comes out nearly empty and
  must never be proposed.
- reports whether the change is large enough to be worth a pull request. Two
  runs never record exactly the same methods, so small differences are noise.

It writes a Markdown summary to --summary and `significant=true|false` to
$GITHUB_OUTPUT when that variable is set.
"""

import argparse
import os
import sys
from pathlib import Path

PROFILE = Path("src/main/generated/baselineProfiles/baseline-prof.txt")


def read_rules(path: Path) -> set[str]:
    if not path.is_file():
        return set()
    return {line.strip() for line in path.read_text().splitlines() if line.strip()}


def owner(rule: str) -> str:
    """The class a rule belongs to, without nested classes: HSPLcom/a/B$c;->m()V -> com.a.B."""
    start = rule.find("L")
    end = rule.find(";", start)
    if start < 0 or end < 0:
        return rule
    return rule[start + 1:end].split("$")[0].replace("/", ".")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--before", required=True, type=Path,
                        help="Directory with the committed profiles, laid out as <module>/baseline-prof.txt")
    parser.add_argument("--summary", required=True, type=Path, help="Markdown file to write")
    parser.add_argument("--min-ratio", type=float, default=0.5,
                        help="Fail if a module keeps fewer than this share of its committed rules")
    parser.add_argument("--threshold", type=float, default=0.05,
                        help="Share of changed rules above which the change is significant")
    parser.add_argument("modules", nargs="+")
    args = parser.parse_args()

    rows = []
    errors = []
    changed_total = 0
    before_total = 0
    details = []

    for module in args.modules:
        before = read_rules(args.before / module / "baseline-prof.txt")
        after = read_rules(Path(module) / PROFILE)
        added = after - before
        removed = before - after
        changed_total += len(added) + len(removed)
        before_total += len(before)
        rows.append(f"| `{module}` | {len(before)} | {len(after)} | +{len(added)} | -{len(removed)} |")

        if not after:
            errors.append(f"{module}: the regenerated profile is empty.")
        elif before and len(after) < len(before) * args.min_ratio:
            errors.append(
                f"{module}: {len(after)} rules, down from {len(before)}. "
                "Check that the Maps API key is valid and that the demos load their maps."
            )

        new_classes = sorted({owner(r) for r in added} - {owner(r) for r in before})
        gone_classes = sorted({owner(r) for r in removed} - {owner(r) for r in after})
        if new_classes or gone_classes:
            lines = [f"**`{module}`**"]
            lines += [f"- added `{c}`" for c in new_classes]
            lines += [f"- removed `{c}`" for c in gone_classes]
            details.append("\n".join(lines))

    significant = before_total == 0 or changed_total > before_total * args.threshold

    summary = [
        "| Module | Committed rules | Regenerated rules | Added | Removed |",
        "| --- | ---: | ---: | ---: | ---: |",
        *rows,
        "",
        f"{changed_total} of {before_total} committed rules changed "
        f"({'above' if significant else 'below'} the {args.threshold:.0%} threshold).",
    ]
    if details:
        summary += ["", "Classes added to or removed from the profiles:", "", *details]
    if errors:
        summary += ["", "**Errors:**", *[f"- {e}" for e in errors]]
    args.summary.write_text("\n".join(summary) + "\n")
    print("\n".join(summary))

    github_output = os.environ.get("GITHUB_OUTPUT")
    if github_output:
        with open(github_output, "a") as out:
            out.write(f"significant={'true' if significant and not errors else 'false'}\n")

    if errors:
        for error in errors:
            print(f"::error::{error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
