#!/usr/bin/env bash
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
#
# Commits and pushes the coverage history to its own branch.
#
# Usage: push_coverage_history.sh "<commit message>"
#
# The history deliberately does not live on main. Branch protection there uses
# strict required status checks, so every commit pushed to main marks all open
# pull requests out-of-date and forces contributors to update their branch and
# re-run CI.
#
# If the branch moved under us, the update is redone on top of the new tip
# rather than rebased. Both sides append to the end of history.csv, so a rebase
# conflicts every time; the rows are independent records, so the right
# resolution is to keep both and regenerate the report.

set -euo pipefail

MESSAGE="${1:?commit message required}"
DATA_DIR="${DATA_DIR:-coverage-data}"
DATA_BRANCH="${DATA_BRANCH:-coverage-history}"
SCRIPT="${SCRIPT:-$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/coverage_history.py}"

# Every generated file published on the data branch. COVERAGE.md is derived
# from history.csv, so it is regenerated, not merged.
FILES=(history.csv COVERAGE.md)

cd "$DATA_DIR"

git config user.name 'googlemaps-bot'
git config user.email 'googlemaps-bot@google.com'

# Stage first and compare the index: a newly generated file is untracked, and
# "git diff" does not see untracked files, so checking before staging would
# skip the commit the first time a new output appears.
git add -- "${FILES[@]}"

if git diff --cached --quiet -- "${FILES[@]}"; then
  echo "Coverage history unchanged; nothing to commit."
  exit 0
fi

git commit -m "$MESSAGE"

for attempt in 1 2 3; do
  if git push origin "HEAD:$DATA_BRANCH"; then
    echo "Pushed to $DATA_BRANCH."
    exit 0
  fi

  echo "Push rejected (attempt $attempt); redoing the update on the new tip."

  # Keep our rows, take the branch as it now stands, then put ours back.
  OURS="${RUNNER_TEMP:-/tmp}/coverage-history-ours.csv"
  cp history.csv "$OURS"

  git fetch origin "$DATA_BRANCH"
  git reset --hard "origin/$DATA_BRANCH"

  python3 "$SCRIPT" merge --csv history.csv --ours "$OURS"
  python3 "$SCRIPT" render --csv history.csv --out COVERAGE.md

  git add -- "${FILES[@]}"

  if git diff --cached --quiet -- "${FILES[@]}"; then
    echo "Our rows are already on $DATA_BRANCH; nothing left to push."
    exit 0
  fi

  git commit -m "$MESSAGE"
done

echo "Could not push to $DATA_BRANCH after 3 attempts." >&2
exit 1
