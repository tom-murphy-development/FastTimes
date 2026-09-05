#!/usr/bin/env bash
set -euo pipefail

ROOT=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
MODE=final
if [[ ${1:-} == "--baseline" ]]; then
  MODE=baseline
elif [[ $# -gt 0 ]]; then
  echo "usage: $0 [--baseline]" >&2
  exit 2
fi

cd "$ROOT"

WORKERS=${GRADLE_MAX_WORKERS:-}
if [[ -z "$WORKERS" ]]; then
  WORKERS=$(nproc 2>/dev/null || getconf _NPROCESSORS_ONLN 2>/dev/null || printf '1')
fi

fail() {
  printf 'FAIL: %s\n' "$*" >&2
  exit 1
}

printf '== scope guards (%s) ==\n' "$MODE"
if ! git diff --quiet origin/main -- .github/workflows; then
  fail "PR 1 must not change .github/workflows"
fi

locale_dirs=$(python3 - <<'PY'
from pathlib import Path
import re
for path in sorted(Path('app/src/main/res').glob('values-*')):
    qualifier = path.name.removeprefix('values-')
    if qualifier.startswith('b+') or re.match(r'^[a-z]{2,3}(?:-r[A-Z]{2})?(?:-|$)', qualifier):
        print(path)
PY
)
[[ -z "$locale_dirs" ]] || fail "PR 1 must not ship a real locale: $locale_dirs"

printf '== deterministic Gradle floor ==\n'
./gradlew \
  spotlessCheck \
  lintFossDebug \
  testFossDebugUnitTest \
  assembleFossDebug \
  --parallel \
  --max-workers="$WORKERS" \
  --console=plain

if [[ "$MODE" == baseline ]]; then
  printf 'PASS: baseline is green; final identity guards intentionally not evaluated\n'
  exit 0
fi

printf '== final identity guards ==\n'
[[ -f app/schemas/com.tmdev.fasttimes.data.AppDatabase/7.json ]] || \
  fail "Room schema 7 is missing"

python3 - <<'PY'
from pathlib import Path
import re

root = Path('app/src/main/java')
violations = []
patterns = [
    re.compile(r'profileName\s*[!=]=\s*DefaultFastingProfile\.[A-Z_]+\.displayName'),
    re.compile(r'DefaultFastingProfile\.[A-Z_]+\.displayName\s*[!=]=\s*[^\n]*profileName'),
    re.compile(r'displayName\s*[!=]=\s*DefaultFastingProfile\.[A-Z_]+\.displayName'),
]
for path in root.rglob('*.kt'):
    for number, line in enumerate(path.read_text(errors='ignore').splitlines(), 1):
        if any(pattern.search(line) for pattern in patterns):
            violations.append(f'{path}:{number}:{line.strip()}')
if violations:
    raise SystemExit('localized display copy remains load-bearing:\n' + '\n'.join(violations))
PY

python3 - <<'PY'
import json
from pathlib import Path
schema = json.loads(Path('app/schemas/com.tmdev.fasttimes.data.AppDatabase/7.json').read_text())
entities = {entity['tableName']: entity for entity in schema['database']['entities']}
expected = {
    'fasting_profiles': 'builtInKey',
    'fasts': 'builtInProfileKey',
}
for table, column in expected.items():
    fields = {field['columnName']: field for field in entities[table]['fields']}
    field = fields.get(column)
    if field is None:
        raise SystemExit(f'{table}.{column} missing from Room schema 7')
    if field.get('notNull', False):
        raise SystemExit(f'{table}.{column} must remain nullable for compatibility')
PY

if command -v adb >/dev/null 2>&1 && adb devices | awk 'NR>1 && $2 == "device" { found=1 } END { exit !found }'; then
  printf '== connected migration/instrumentation tests ==\n'
  ./gradlew connectedFossDebugAndroidTest --parallel --max-workers="$WORKERS" --console=plain
else
  printf 'NOTE: no connected Android device; JVM migration/compatibility gates ran, connected tests deferred\n'
fi

printf 'PASS: translation foundation verification complete\n'
