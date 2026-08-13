#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(git rev-parse --show-toplevel)
cd "$ROOT_DIR"

readonly SEMGREP_VERSION=1.172.0
semgrep_command=()

if command -v semgrep >/dev/null 2>&1 && [[ $(semgrep --version | head -n 1) == "$SEMGREP_VERSION" ]]; then
    semgrep_command=(semgrep)
elif command -v uvx >/dev/null 2>&1; then
    semgrep_command=(uvx --from "semgrep==$SEMGREP_VERSION" semgrep)
else
    cache_dir=${XDG_CACHE_HOME:-$HOME/.cache}/fasttimes/semgrep-$SEMGREP_VERSION
    if [[ ! -x "$cache_dir/bin/semgrep" ]]; then
        python3 -m venv "$cache_dir"
        "$cache_dir/bin/python" -m pip install --disable-pip-version-check "semgrep==$SEMGREP_VERSION"
    fi
    semgrep_command=("$cache_dir/bin/semgrep")
fi

actual_version=$("${semgrep_command[@]}" --version | head -n 1)
[[ "$actual_version" == "$SEMGREP_VERSION" ]] || {
    echo "Expected Semgrep $SEMGREP_VERSION, got $actual_version." >&2
    exit 1
}

# Semgrep 1.172 requires test fixtures to be co-located with their rules;
# `semgrep test DIRECTORY` discovers both without a remote registry lookup.
"${semgrep_command[@]}" test "$ROOT_DIR/.semgrep"

exec "${semgrep_command[@]}" scan \
    --config "$ROOT_DIR/.semgrep" \
    --strict \
    --error \
    --metrics=off \
    --disable-version-check \
    --jobs "${QUALITY_GATE_WORKERS:-$(nproc 2>/dev/null || echo 2)}" \
    "$ROOT_DIR/app/src"
