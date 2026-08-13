#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(git rev-parse --show-toplevel)
cd "$ROOT_DIR"

validate_instrumentation_output() {
    local output_file=$1
    local transport_status=${2:-0}

    if (( transport_status != 0 )); then
        echo "Instrumentation transport failed with status $transport_status." >&2
        return 1
    fi
    if grep -Eq '^(INSTRUMENTATION_FAILED|INSTRUMENTATION_ABORTED):|^INSTRUMENTATION_STATUS_CODE: -[12]$|FAILURES!!!|Process crashed|shortMsg=Process crashed' "$output_file"; then
        echo "Instrumentation runner reported a failure." >&2
        return 1
    fi
    if ! grep -Eq '^OK \([1-9][0-9]* tests?\)$' "$output_file"; then
        echo "Instrumentation did not report a non-empty successful JUnit run." >&2
        return 1
    fi
    if ! grep -Eq '^INSTRUMENTATION_CODE: -1$' "$output_file"; then
        echo "Instrumentation did not report the Android success code." >&2
        return 1
    fi
}

self_test_parser() {
    local fixtures
    fixtures=$(mktemp -d)

    cat > "$fixtures/pass.txt" <<'EOF'
INSTRUMENTATION_RESULT: stream=
Time: 0.123

OK (2 tests)

INSTRUMENTATION_CODE: -1
EOF
    cat > "$fixtures/false-green.txt" <<'EOF'
Time: 0.123
There was 1 failure:
FAILURES!!!
Tests run: 1,  Failures: 1
INSTRUMENTATION_CODE: -1
EOF

    validate_instrumentation_output "$fixtures/pass.txt" 0
    if validate_instrumentation_output "$fixtures/false-green.txt" 0; then
        echo "Parser accepted a runner failure with a zero transport status." >&2
        return 1
    fi
    if validate_instrumentation_output "$fixtures/pass.txt" 42; then
        echo "Parser accepted a failed transport." >&2
        return 1
    fi
    rm -rf "$fixtures"
    echo "Instrumentation parser self-test passed."
}

case "${1:-}" in
    --self-test)
        self_test_parser
        exit
        ;;
    --check-output)
        [[ $# -ge 2 ]] || { echo "Usage: $0 --check-output FILE [TRANSPORT_STATUS]" >&2; exit 2; }
        validate_instrumentation_output "$2" "${3:-0}"
        exit
        ;;
esac

: "${ANDROID_SERIAL:?Set ANDROID_SERIAL to the one emulator or device that may run the tests.}"
ADB=${ADB:-adb}
INSTRUMENTATION_TARGET=${INSTRUMENTATION_TARGET:-com.tmdev.fasttimes.debug.test/androidx.test.runner.AndroidJUnitRunner}
workers=${QUALITY_GATE_WORKERS:-$(nproc 2>/dev/null || echo 2)}

"$ADB" -s "$ANDROID_SERIAL" get-state | grep -qx device || {
    echo "Android target $ANDROID_SERIAL is not ready." >&2
    exit 1
}

./gradlew --parallel --max-workers="$workers" --console=plain \
    assembleFossDebug assembleFossDebugAndroidTest

app_apk=$(find app/build/outputs/apk/foss/debug -type f -name '*.apk' -print -quit)
test_apk=$(find app/build/outputs/apk/androidTest/foss/debug -type f -name '*.apk' -print -quit)
[[ -n "$app_apk" && -n "$test_apk" ]] || {
    echo "Expected FOSS debug app and instrumentation APKs were not produced." >&2
    exit 1
}

"$ADB" -s "$ANDROID_SERIAL" install -r -t "$app_apk"
"$ADB" -s "$ANDROID_SERIAL" install -r -t "$test_apk"

result_file=$(mktemp)
trap 'rm -f "$result_file"' EXIT
set +e
"$ADB" -s "$ANDROID_SERIAL" shell am instrument -w -r "$INSTRUMENTATION_TARGET" 2>&1 | tee "$result_file"
transport_status=${PIPESTATUS[0]}
set -e

validate_instrumentation_output "$result_file" "$transport_status"
echo "Connected instrumentation passed on $ANDROID_SERIAL."
