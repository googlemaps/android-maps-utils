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

"""Track test coverage over time from JaCoCo-format XML reports.

Coverage here comes from unit tests: Kover reports produced by
``./gradlew koverXmlReportDebug`` at
``<module>/build/reports/kover/reportDebug.xml``.

The CSV carries a ``suite`` column and the script also understands AGP's
instrumentation reports, so emulator coverage can be recorded later without a
format change. This repository runs no emulator tests in CI today, so only the
``unit`` suite is populated. Commands:

  summary  print the parsed coverage as JSON (debugging / ad-hoc use)
  append   append one row per module to the append-only history CSV
  render   regenerate the human-readable COVERAGE.md from that CSV
  compare  render a markdown coverage diff against the last recorded entry,
           for posting on a pull request

Standard library only, so it runs on any CI image without extra setup.
"""

from __future__ import annotations

import argparse
import csv
import datetime
import glob
import json
import os
import sys
import xml.etree.ElementTree as ET

# JaCoCo counter types we record. The reports also carry METHOD and CLASS, but
# line and branch coverage are what people actually track; instructions are
# kept because they move more smoothly and make small changes visible.
COUNTERS = ("LINE", "BRANCH", "INSTRUCTION")

# Name used for the synthetic row holding the sum across all modules.
TOTAL = "TOTAL"

# Where each suite leaves its reports, relative to the repo root.
SUITES = {
    "unit": "*/build/reports/kover/reportDebug.xml",
    "instrumentation": "*/build/reports/coverage/androidTest/debug/connected/report.xml",
}

CSV_FIELDS = [
    "timestamp",
    "commit",
    "pr",
    "subject",
    "suite",
    "module",
    "lines_covered",
    "lines_total",
    "line_pct",
    "branches_covered",
    "branches_total",
    "branch_pct",
    "instructions_covered",
    "instructions_total",
    "instruction_pct",
]

# Modules the history tracks. This mirrors the module list that report.yml
# gates on, so the history and the pull request coverage check describe the
# same surface. Two published modules are deliberately left out: "maps-utils"
# is an umbrella artifact with no sources of its own, and "visual-testing" is
# test-support tooling. Set to None to record every module Kover reports.
MODULE_ALLOWLIST = ("library", "clustering", "data", "heatmaps", "ui")

DEFAULT_CSV = "coverage/history.csv"
DEFAULT_MARKDOWN = "coverage/COVERAGE.md"

# How many past commits the trend table in COVERAGE.md shows.
TREND_LIMIT = 30


def module_name(report_path: str) -> str:
    """Derive the Gradle module name from a report path.

    Two layouts are supported. In a Gradle build directory the module is the
    path component before ``build``::

        maps-compose/build/reports/kover/reportDebug.xml -> maps-compose

    Instrumentation reports are staged into a flat directory before being
    uploaded as a CI artifact, because actions/upload-artifact strips the
    common parent when only one file matches. There the file is named after
    its module::

        instrumentation-coverage/maps-app.xml -> maps-app
    """
    path = os.path.normpath(report_path)
    # Reports are normally globbed relative to the repo root. Re-anchor absolute
    # paths so the module name does not absorb the directories above the
    # checkout.
    if os.path.isabs(path):
        relative = os.path.relpath(path, os.getcwd())
        path = relative if not relative.startswith("..") else path

    parts = [p for p in path.split(os.sep) if p not in (".", "")]
    if "build" in parts:
        prefix = parts[: parts.index("build")]
        if not prefix:
            return "unknown"
        # Nested Gradle modules read as "features:foo". An absolute path that
        # could not be re-anchored contributes only its innermost directory.
        return ":".join(prefix) if not os.path.isabs(path) else prefix[-1]
    return os.path.splitext(parts[-1])[0]


def parse_report(report_path: str) -> dict[str, dict[str, int]]:
    """Read the report-level counters from one JaCoCo-format XML report."""
    root = ET.parse(report_path).getroot()
    counters: dict[str, dict[str, int]] = {}
    # findall on the root matches direct children only, which is exactly the
    # report-level total rather than the per-package or per-class counters.
    for counter in root.findall("counter"):
        kind = counter.get("type", "")
        if kind not in COUNTERS:
            continue
        covered = int(counter.get("covered", 0))
        missed = int(counter.get("missed", 0))
        counters[kind] = {"covered": covered, "total": covered + missed}
    for kind in COUNTERS:
        counters.setdefault(kind, {"covered": 0, "total": 0})
    return counters


def collect(
    pattern: str,
    required: bool = True,
    allow: tuple[str, ...] | None = MODULE_ALLOWLIST,
) -> dict[str, dict[str, dict[str, int]]]:
    """Parse every matching report and add a TOTAL across the tracked modules.

    ``allow`` restricts which modules are recorded; ``None`` records all of
    them. TOTAL is summed after filtering, so it reflects only what is tracked.
    """
    reports = sorted(glob.glob(pattern))
    if not reports:
        if required:
            sys.exit(f"No coverage reports matched '{pattern}'.")
        return {}

    modules = {module_name(path): parse_report(path) for path in reports}
    if allow is not None:
        missing = [name for name in allow if name not in modules]
        if missing:
            print(f"Warning: no report found for {', '.join(missing)}", file=sys.stderr)
        modules = {name: c for name, c in modules.items() if name in allow}
        if not modules:
            if required:
                sys.exit(f"No reports matched the tracked modules {list(allow)}.")
            return {}

    total = {kind: {"covered": 0, "total": 0} for kind in COUNTERS}
    for counters in modules.values():
        for kind in COUNTERS:
            total[kind]["covered"] += counters[kind]["covered"]
            total[kind]["total"] += counters[kind]["total"]
    modules[TOTAL] = total
    return modules


def pct(covered: int, total: int) -> float:
    """Coverage percentage, rounded to two decimals. Empty counts as 0%."""
    return round(covered / total * 100, 2) if total else 0.0


def sort_key(module: str) -> tuple[int, str]:
    """Order modules alphabetically but keep TOTAL last."""
    return (1, "") if module == TOTAL else (0, module)


def to_row(module: str, counters: dict[str, dict[str, int]], meta: dict) -> dict:
    line, branch, instr = counters["LINE"], counters["BRANCH"], counters["INSTRUCTION"]
    return {
        "timestamp": meta["timestamp"],
        "commit": meta["commit"],
        "pr": meta["pr"],
        "subject": meta["subject"],
        "suite": meta["suite"],
        "module": module,
        "lines_covered": line["covered"],
        "lines_total": line["total"],
        "line_pct": pct(line["covered"], line["total"]),
        "branches_covered": branch["covered"],
        "branches_total": branch["total"],
        "branch_pct": pct(branch["covered"], branch["total"]),
        "instructions_covered": instr["covered"],
        "instructions_total": instr["total"],
        "instruction_pct": pct(instr["covered"], instr["total"]),
    }


def read_history(csv_path: str) -> list[dict]:
    if not os.path.exists(csv_path):
        return []
    with open(csv_path, newline="", encoding="utf-8") as handle:
        return list(csv.DictReader(handle))


def latest_entry(rows: list[dict], suite: str) -> dict[str, dict]:
    """The most recently appended commit for one suite, as ``{module: row}``.

    The CSV is append-only, so the last block of rows sharing a commit is the
    newest entry. Walking backwards avoids depending on timestamp parsing.
    """
    rows = [row for row in rows if row.get("suite") == suite]
    if not rows:
        return {}
    newest = rows[-1]["commit"]
    return {row["module"]: row for row in rows if row["commit"] == newest}


def resolve_pattern(args: argparse.Namespace) -> str:
    """The report glob: an explicit --reports wins, else the suite default."""
    return args.reports or SUITES[args.suite]


def resolve_allow(args: argparse.Namespace) -> tuple[str, ...] | None:
    """The tracked module list: --modules overrides, "all" disables filtering."""
    if not args.modules:
        return MODULE_ALLOWLIST
    if args.modules.strip().lower() == "all":
        return None
    return tuple(m.strip() for m in args.modules.split(",") if m.strip())


def cmd_summary(args: argparse.Namespace) -> None:
    modules = collect(resolve_pattern(args), allow=resolve_allow(args))
    summary = {
        module: {
            "line_pct": pct(c["LINE"]["covered"], c["LINE"]["total"]),
            "branch_pct": pct(c["BRANCH"]["covered"], c["BRANCH"]["total"]),
            "instruction_pct": pct(
                c["INSTRUCTION"]["covered"], c["INSTRUCTION"]["total"]
            ),
            "counters": c,
        }
        for module, c in sorted(modules.items(), key=lambda kv: sort_key(kv[0]))
    }
    print(json.dumps({"suite": args.suite, "modules": summary}, indent=2))


def cmd_append(args: argparse.Namespace) -> None:
    modules = collect(
        resolve_pattern(args), required=not args.optional, allow=resolve_allow(args)
    )
    if not modules:
        print(f"No {args.suite} reports found; skipping (--optional).")
        return

    meta = {
        "timestamp": datetime.datetime.now(datetime.timezone.utc).strftime(
            "%Y-%m-%dT%H:%M:%SZ"
        ),
        "commit": args.commit[:7],
        "pr": args.pr or "",
        "subject": args.subject or "",
        "suite": args.suite,
    }

    existing = read_history(args.csv)
    # Each suite is appended by a separate step, so dedupe on the pair.
    if any(
        row["commit"] == meta["commit"] and row.get("suite") == args.suite
        for row in existing
    ):
        print(
            f"Commit {meta['commit']} already has {args.suite} coverage recorded; "
            "nothing to append."
        )
        return

    os.makedirs(os.path.dirname(args.csv) or ".", exist_ok=True)
    with open(args.csv, "a", newline="", encoding="utf-8") as handle:
        # csv defaults to CRLF; force LF so the file stays stable under git
        # and each CI append does not churn line endings.
        writer = csv.DictWriter(handle, fieldnames=CSV_FIELDS, lineterminator="\n")
        if not existing:
            writer.writeheader()
        for module, counters in sorted(modules.items(), key=lambda kv: sort_key(kv[0])):
            writer.writerow(to_row(module, counters, meta))
    print(
        f"Appended {len(modules)} {args.suite} rows for {meta['commit']} to {args.csv}"
    )


def bar(percentage: float, width: int = 20) -> str:
    """A fixed-width text meter, so the table scans at a glance."""
    filled = round(percentage / 100 * width)
    return "█" * filled + "░" * (width - filled)


def render_current(rows: list[dict], suite: str, heading: str, note: str) -> list[str]:
    latest = latest_entry(rows, suite)
    if not latest:
        return [f"### {heading}", "", "_No data recorded yet._", ""]

    head = next(iter(latest.values()))
    lines = [
        f"### {heading}",
        "",
        note,
        "",
        f"Measured at `{head['commit']}`"
        + (f" (#{head['pr']})" if head["pr"] else "")
        + f", recorded {head['timestamp']}.",
        "",
        "| Module | Lines | Line % | | Branches | Branch % |",
        "| --- | ---: | ---: | --- | ---: | ---: |",
    ]
    for module in sorted(latest, key=sort_key):
        row = latest[module]
        name = f"**{module}**" if module == TOTAL else f"`{module}`"
        line_pct = float(row["line_pct"])
        lines.append(
            f"| {name} "
            f"| {row['lines_covered']}/{row['lines_total']} "
            f"| {line_pct:.2f}% "
            f"| `{bar(line_pct)}` "
            f"| {row['branches_covered']}/{row['branches_total']} "
            f"| {float(row['branch_pct']):.2f}% |"
        )
    lines.append("")
    return lines


def cmd_render(args: argparse.Namespace) -> None:
    rows = read_history(args.csv)
    if not rows:
        sys.exit(f"{args.csv} has no entries to render.")

    # This repository records unit coverage only. The instrumentation sections
    # and columns appear automatically if emulator coverage is ever recorded.
    has_instrumentation = any(row.get("suite") == "instrumentation" for row in rows)

    lines = [
        "<!-- Generated by .github/scripts/coverage_history.py. Do not edit by hand. -->",
        "# Test coverage",
        "",
        "Raw data lives in [`history.csv`](history.csv), one row per module per",
        "suite per merged commit. This page is regenerated from it and should not",
        "be edited by hand.",
        "",
    ]
    if has_instrumentation:
        lines += [
            "The two suites are reported separately and are **not additive**. They",
            "instrument different code with different runners, and merging them",
            "would require combining the raw execution data rather than the XML",
            "reports.",
            "",
        ]
    lines += ["## Current", ""]

    tracked = ", ".join(f"`{m}`" for m in MODULE_ALLOWLIST) if MODULE_ALLOWLIST else ""
    lines += render_current(
        rows,
        "unit",
        "Unit tests",
        "JVM tests run by `./gradlew koverXmlReportDebug`"
        + (f", covering {tracked}." if tracked else ", covering all library modules."),
    )
    if has_instrumentation:
        lines += render_current(
            rows,
            "instrumentation",
            "Instrumentation tests",
            "Emulator tests run by `./gradlew createDebugCoverageReport`.",
        )

    # Trend: one line per recorded commit, newest first, with the change in
    # total line coverage for each suite against the commit before it.
    order: list[str] = []
    for row in rows:
        if row["commit"] not in order:
            order.append(row["commit"])

    def total_pct(commit: str, suite: str) -> float | None:
        for row in rows:
            if (
                row["commit"] == commit
                and row.get("suite") == suite
                and row["module"] == TOTAL
            ):
                return float(row["line_pct"])
        return None

    def cell(current: float | None, previous: float | None) -> tuple[str, str]:
        if current is None:
            return "n/a", "n/a"
        if previous is None:
            return f"{current:.2f}%", "n/a"
        difference = current - previous
        change = f"{difference:+.2f}" if abs(difference) >= 0.005 else "0.00"
        return f"{current:.2f}%", change

    lines += [
        f"## Trend (last {TREND_LIMIT} commits)",
        "",
        ("Total line coverage per suite, newest first." if has_instrumentation
         else "Total line coverage, newest first."),
        "",
        ("| Date | Commit | PR | Unit % | Change | Instr. % | Change | Subject |"
         if has_instrumentation
         else "| Date | Commit | PR | Line % | Change | Subject |"),
        ("| --- | --- | --- | ---: | ---: | ---: | ---: | --- |"
         if has_instrumentation
         else "| --- | --- | --- | ---: | ---: | --- |"),
    ]
    for index in range(len(order) - 1, max(len(order) - TREND_LIMIT, 0) - 1, -1):
        commit = order[index]
        previous = order[index - 1] if index > 0 else None
        meta = next(row for row in rows if row["commit"] == commit)
        unit, unit_change = cell(
            total_pct(commit, "unit"),
            total_pct(previous, "unit") if previous else None,
        )
        instr, instr_change = cell(
            total_pct(commit, "instrumentation"),
            total_pct(previous, "instrumentation") if previous else None,
        )
        pr = f"#{meta['pr']}" if meta["pr"] else "n/a"
        subject = meta["subject"].replace("|", "\\|") or "n/a"
        row_cells = f"| {meta['timestamp'][:10]} | `{commit}` | {pr} | {unit} | {unit_change} "
        if has_instrumentation:
            row_cells += f"| {instr} | {instr_change} "
        lines.append(row_cells + f"| {subject} |")
    lines.append("")

    os.makedirs(os.path.dirname(args.out) or ".", exist_ok=True)
    with open(args.out, "w", encoding="utf-8") as handle:
        handle.write("\n".join(lines))
    print(f"Wrote {args.out}")


def cmd_compare(args: argparse.Namespace) -> None:
    modules = collect(resolve_pattern(args), allow=resolve_allow(args))
    baseline = latest_entry(read_history(args.csv), args.suite)

    lines = [args.marker, f"## Coverage ({args.suite} tests)", ""]
    if baseline:
        head = next(iter(baseline.values()))
        lines += [
            f"Compared against `{head['commit']}` on `{args.base}`"
            + (f" (#{head['pr']})" if head["pr"] else "")
            + ".",
            "",
        ]
    else:
        lines += [
            f"No {args.suite} baseline recorded in `{args.csv}` yet, so this run "
            "only reports absolute numbers.",
            "",
        ]

    lines += [
        "| Module | Line % | Change | Branch % | Change |",
        "| --- | ---: | ---: | ---: | ---: |",
    ]

    def delta(current: float, module: str, field: str) -> str:
        if module not in baseline:
            return "new"
        difference = current - float(baseline[module][field])
        if abs(difference) < 0.005:
            return "no change"
        return f"{'🔺' if difference > 0 else '🔻'} {difference:+.2f}"

    for module, counters in sorted(modules.items(), key=lambda kv: sort_key(kv[0])):
        line_pct = pct(counters["LINE"]["covered"], counters["LINE"]["total"])
        branch_pct = pct(counters["BRANCH"]["covered"], counters["BRANCH"]["total"])
        name = f"**{module}**" if module == TOTAL else f"`{module}`"
        lines.append(
            f"| {name} | {line_pct:.2f}% | {delta(line_pct, module, 'line_pct')} "
            f"| {branch_pct:.2f}% | {delta(branch_pct, module, 'branch_pct')} |"
        )

    lines += [
        "",
        f"<sub>Line and branch coverage from {args.suite} test reports. History "
        f"is recorded in `coverage/history.csv` after each merge to "
        f"`{args.base}`.</sub>",
        "",
    ]

    body = "\n".join(lines)
    if args.out:
        with open(args.out, "w", encoding="utf-8") as handle:
            handle.write(body)
        print(f"Wrote {args.out}")
    else:
        print(body)


def add_common(parser: argparse.ArgumentParser) -> None:
    parser.add_argument(
        "--suite",
        choices=sorted(SUITES),
        default="unit",
        help="which test suite these reports came from (default: unit)",
    )
    parser.add_argument(
        "--reports",
        default="",
        help="glob matching the XML reports; defaults to the suite's usual path",
    )
    parser.add_argument(
        "--modules",
        default="",
        help='comma-separated modules to track, or "all" for every module '
        f"reported (default: {','.join(MODULE_ALLOWLIST)})",
    )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    subparsers = parser.add_subparsers(dest="command", required=True)

    summary = subparsers.add_parser("summary", help="print parsed coverage as JSON")
    add_common(summary)
    summary.set_defaults(func=cmd_summary)

    append = subparsers.add_parser("append", help="append an entry to the history CSV")
    add_common(append)
    append.add_argument("--csv", default=DEFAULT_CSV)
    append.add_argument("--commit", required=True, help="full commit SHA")
    append.add_argument("--pr", default="", help="pull request number, if known")
    append.add_argument("--subject", default="", help="commit subject line")
    append.add_argument(
        "--optional",
        action="store_true",
        help="skip quietly instead of failing when no reports are present",
    )
    append.set_defaults(func=cmd_append)

    render = subparsers.add_parser("render", help="regenerate COVERAGE.md from the CSV")
    render.add_argument("--csv", default=DEFAULT_CSV)
    render.add_argument("--out", default=DEFAULT_MARKDOWN)
    render.set_defaults(func=cmd_render)

    compare = subparsers.add_parser(
        "compare", help="render a markdown coverage diff against the last entry"
    )
    add_common(compare)
    compare.add_argument("--csv", default=DEFAULT_CSV)
    compare.add_argument("--base", default="main", help="branch the baseline came from")
    compare.add_argument("--out", default="", help="write here instead of stdout")
    compare.add_argument(
        "--marker",
        default="<!-- coverage-history-comment -->",
        help="hidden marker used to find and update the existing PR comment",
    )
    compare.set_defaults(func=cmd_compare)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
