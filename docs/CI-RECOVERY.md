# CI recovery — 01/10/2026

Base: upstream `b5ee61ec73a0d5060a6b5d6842e610d324d9b624`.

## Diagnosis and smallest repair

- [Android CI 33961592165](https://github.com/tom-murphy-development/FastTimes/actions/runs/33961592165)
  reports `Object_1_Proxy cannot be cast to Boolean` in dashboard rendering.
  Commit `5b31cd2` added collection of `DashboardViewModel.useExpressiveTheme`;
  all three dashboard test fixtures omitted this generic `StateFlow<Boolean>`.
  Stub a real `MutableStateFlow(false)` in each fixture, matching their theme.
  Existing dashboard and responsive assertions remain unchanged.
- [CodeQL 33961592148](https://github.com/tom-murphy-development/FastTimes/actions/runs/33961592148)
  restored both Kotlin and Java compilation `FROM-CACHE`, then exited 32 because
  no source was extracted. Disable build-cache reuse and rerun tasks for the
  traced build only; normal Android build caching remains enabled.
- The local quality gate checked for SDK 35 although `compileSdk` is 37. Align
  this preflight with compilation; API 35 remains the instrumentation runtime.

## Plan and acceptance

1. Repair missing fixture state, traced compilation, and SDK preflight.
2. Run formatting, lint, unit tests, APK build, parser checks, and connected tests.
3. Obtain successful Android CI and CodeQL results on the exact new head before
   claiming recovery. Upstream workflow approval and merge require a maintainer.

```bash
./gradlew --parallel --max-workers="$(nproc)" spotlessCheck lintFossDebug \
  testFossDebugUnitTest assembleFossDebug assembleFossDebugAndroidTest
scripts/connected-tests.sh --self-test
# Isolated emulator only, never an implicit physical-device target:
ANDROID_SERIAL=emulator-5554 scripts/connected-tests.sh
```

## Validation limitation

Local SDK installation on 01/10/2026 returned
`Failed to find package 'platforms;android-37'`; the available package listing
ends at API 36. Do not lower `compileSdk` or count this as successful Android
acceptance. No connected tests or full CodeQL extraction have run for this fix.
The CI failure logs and source comparison establish the defects, but these
patches remain runtime-unverified until the acceptance above succeeds.
The local Gradle acceptance command was also bounded at 240 seconds and timed
out before task output; it did not produce a test, lint, or build pass.

Verified locally:

- `git diff --check`, shell syntax, and instrumentation-parser self-tests pass.
- `actionlint` and `zizmor --pedantic` report no workflow findings.
- Source checks confirm every dashboard-collected flow is explicitly stubbed in
  all three fixtures (including the sibling responsive tests).
- An isolated preflight check with a stub Java/build executable refuses an
  API-35-only SDK and reaches the build with API 37. This is not Android acceptance.

No production source, dependency, permission, assertion, or health copy changed.
Rollback after a squash merge: `git revert <merge-commit>` via a normal PR.
