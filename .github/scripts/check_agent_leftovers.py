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

"""Fail a pull request that carries leftovers from local or agent-assisted work.

Only what the pull request adds is checked: added lines of the diff, the
names of added files, the messages of its commits, and its title and body.
Existing content on the base branch is never flagged.

Things that are fine on the author's machine but wrong in a public repo:

  local paths       file:// URLs into home or temp directories, and home
                    directories themselves (/Users/x/, /home/x/,
                    /usr/local/google/, C:\\Users\\x\\)
  internal links    go/ short links, b/ bug IDs, corp and googleplex hosts
  secrets           Google API keys
  agent traces      AI co-author trailers, links to agent chat sessions
  local files       agent settings meant to stay local, merge leftovers

A line that legitimately needs one of these can opt out by containing
"agent-leftovers: ignore".

Usage, from the repository root:

  BASE_SHA=<base commit> [HEAD_SHA=<head, default HEAD>] [PR_TITLE=...] [PR_BODY=...] \\
      check_agent_leftovers.py

Findings are printed as GitHub Actions error annotations.
"""

import os
import re
import subprocess
import sys

IGNORE_MARKER = "agent-leftovers: ignore"

# This script and its workflow describe the patterns, so they would match them.
SKIPPED_FILES = {
    ".github/scripts/check_agent_leftovers.py",
    ".github/workflows/agent-leftovers.yml",
}

CONTENT_PATTERNS = [
    ("local file URL", re.compile(r"file:///(Users|home|usr/local/google|private/var|var/folders|[A-Za-z]:)")),
    ("local path", re.compile(r"/usr/local/google/")),
    ("local path", re.compile(r"(?<![\w.])/Users/[\w.-]+/")),
    ("local path", re.compile(r"(?<![\w.])/home/(?!runner/)[\w.-]+/")),
    ("local path", re.compile(r"\b[A-Za-z]:\\+Users\\+[\w.-]+")),
    ("internal short link", re.compile(r"(?<![\w./-])go/[A-Za-z0-9][\w.-]*")),
    ("internal short link", re.compile(r"https?://go/")),
    ("internal bug ID", re.compile(r"(?<![\w./-])b/\d{6,}")),
    ("internal host", re.compile(r"\b[\w.-]+\.corp\.google\.com\b")),
    ("internal host", re.compile(r"\bgoogleplex\.com\b")),
    ("Google API key", re.compile(r"AIza[0-9A-Za-z_-]{35}")),
    ("agent session link", re.compile(r"claude\.ai/(code/)?(chat|session)")),
    ("agent session link", re.compile(r"chatgpt\.com/(c|share)/")),
    ("agent session link", re.compile(r"gemini\.google\.com/(app|share)/")),
    ("agent attribution", re.compile(r"Generated with \[?(Claude|Gemini|Copilot|Codex|Cursor)", re.I)),
    (
        "AI co-author trailer",
        re.compile(r"Co-Authored-By:.*\b(Claude|Anthropic|Gemini|Copilot|Codex|Cursor|OpenAI)\b", re.I),
    ),
]

FILE_PATTERNS = [
    ("local agent settings", re.compile(r"(^|/)\.claude/settings\.local\.json$")),
    ("local agent memory", re.compile(r"(^|/)CLAUDE\.local\.md$")),
    ("merge leftover", re.compile(r"\.(orig|rej)$")),
]


def git(*args):
    return subprocess.run(
        ["git", *args], check=True, capture_output=True, text=True
    ).stdout


def scan_text(text):
    """Yield the description of every pattern found in text, line by line."""
    for line in text.splitlines():
        if IGNORE_MARKER in line:
            continue
        for name, pattern in CONTENT_PATTERNS:
            match = pattern.search(line)
            if match:
                yield f"{name}: {match.group(0)}"


def added_lines(base, head):
    """Yield (path, line number, text) for every line the diff adds."""
    diff = git("diff", "--unified=0", "--no-color", "--no-ext-diff", f"{base}...{head}")
    path = None
    line_no = 0
    for line in diff.splitlines():
        if line.startswith("+++ "):
            path = line[6:] if line.startswith("+++ b/") else None
        elif line.startswith("@@"):
            hunk = re.match(r"@@ -\S+ \+(\d+)", line)
            line_no = int(hunk.group(1)) if hunk else 0
        elif line.startswith("+") and path:
            yield path, line_no, line[1:]
            line_no += 1


def annotate(message, path=None, line=None):
    location = ""
    if path:
        location = f" file={path}" + (f",line={line}" if line else "")
    print(f"::error{location}::{message}")


def main():
    base = os.environ.get("BASE_SHA")
    head = os.environ.get("HEAD_SHA", "HEAD")
    if not base:
        print("BASE_SHA is not set.", file=sys.stderr)
        return 2

    findings = 0

    for path, line_no, text in added_lines(base, head):
        if path in SKIPPED_FILES:
            continue
        for finding in scan_text(text):
            annotate(finding, path, line_no)
            findings += 1

    for path in git("diff", "--name-only", "--diff-filter=A", f"{base}...{head}").splitlines():
        for name, pattern in FILE_PATTERNS:
            if pattern.search(path):
                annotate(f"{name}: {path} should not be committed", path)
                findings += 1

    for entry in git("log", "--no-merges", "--format=%h%x00%B%x1e", f"{base}..{head}").split("\x1e"):
        if not entry.strip():
            continue
        sha, _, message = entry.strip().partition("\x00")
        for finding in scan_text(message):
            annotate(f"commit {sha} message has {finding}")
            findings += 1

    for field in ("PR_TITLE", "PR_BODY"):
        for finding in scan_text(os.environ.get(field, "")):
            annotate(f"pull request {field[3:].lower()} has {finding}")
            findings += 1

    if findings:
        print(
            f"\nFound {findings} leftover(s). Remove them, or add "
            f'"{IGNORE_MARKER}" to a line that legitimately needs one.'
        )
        return 1
    print("No agent leftovers found.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
