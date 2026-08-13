#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR=$(git rev-parse --show-toplevel)
cd "$ROOT_DIR"

java_major=$(java -XshowSettings:properties -version 2>&1 | awk -F'= ' '/java.specification.version =/{print $2; exit}')
[[ "$java_major" == 21 ]] || {
    echo "FastTimes quality gate requires Java 21; found ${java_major:-unknown}." >&2
    exit 1
}

sdk_root=${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}
[[ -n "$sdk_root" && -f "$sdk_root/platforms/android-36/android.jar" ]] || {
    echo "Set ANDROID_SDK_ROOT or ANDROID_HOME to an SDK containing API 36." >&2
    exit 1
}

workers=${QUALITY_GATE_WORKERS:-$(nproc 2>/dev/null || echo 2)}
./gradlew --parallel --max-workers="$workers" --console=plain \
    spotlessCheck \
    lintFossDebug \
    testFossDebugUnitTest \
    assembleFossDebug \
    jacocoFossDebugReport

coverage_xml=app/build/reports/jacoco/jacocoFossDebugReport/jacocoFossDebugReport.xml
[[ -s "$coverage_xml" ]] || {
    echo "Coverage XML was not generated at $coverage_xml." >&2
    exit 1
}

scripts/semgrep.sh

if [[ ${RUN_CONNECTED_TESTS:-0} == 1 ]]; then
    scripts/connected-tests.sh
else
    echo "[SKIP] Connected tests require RUN_CONNECTED_TESTS=1 and an explicit ANDROID_SERIAL."
fi

sonar_values=0
for value in "${SONAR_HOST_URL:-}" "${SONAR_TOKEN:-}" "${SONAR_PROJECT_KEY:-}"; do
    [[ -n "$value" ]] && ((sonar_values += 1))
done

case $sonar_values in
    0)
        echo "[SKIP] SonarQube is not configured; no scan or quality-gate claim was made."
        ;;
    3)
        command -v sonar-scanner >/dev/null 2>&1 || {
            echo "sonar-scanner is required when SonarQube is configured." >&2
            exit 1
        }
        sonar-scanner \
            -Dsonar.projectKey="$SONAR_PROJECT_KEY" \
            -Dsonar.qualitygate.wait=true
        ;;
    *)
        echo "SonarQube is partially configured; set SONAR_HOST_URL, SONAR_TOKEN, and SONAR_PROJECT_KEY together." >&2
        exit 1
        ;;
esac

echo "FastTimes local quality gate passed."
