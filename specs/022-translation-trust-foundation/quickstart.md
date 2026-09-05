# Quickstart: execute the translation stack safely

## PR 1 — stable profile identity

```bash
cd /home/notroot/Documents/Code/personal/fasttimes-022-translation-trust-foundation

export ANDROID_HOME=/nix/store/gangfkmhmwyb50zbzbhnnsa7lfxsp4hg-androidsdk/libexec/android-sdk
export JAVA_HOME=/nix/store/102gxd1lf8cniz9zzsxn7mdmnar8w0jz-openjdk-21.0.12+2
export PATH="$JAVA_HOME/bin:$PATH"

# Before implementation: proves the existing floor is green.
./scripts/verify-translation-foundation.sh --baseline

# During/after implementation: intentionally fails on current main, and turns
# green only when schema 7, stable identity, compatibility, and scope guards pass.
./scripts/verify-translation-foundation.sh
```

Expected PR 1 user-visible result: English presentation is unchanged. The value is internal correctness and migration safety. The final verifier's pre-implementation failure is required falsifier evidence, not a baseline defect.

## Review evidence

- Room migration 6→7 fixture includes bundled, legacy, custom-collision, favorite, and history records.
- Old JSON export fixture decodes and round-trips.
- No production business comparison uses localized display copy.
- No non-English resource directory exists.
- `.github/workflows/` has no diff.
- Existing deterministic Gradle floor remains green.

## Later catalog PR

Before adding a real locale:

```bash
python3 scripts/localization/check_literals.py
python3 scripts/localization/check_placeholders.py app/src/main/res
./gradlew lintFossDebug testFossDebugUnitTest
# Execute en-XA/ar-XB Compose/instrumentation or screenshot gates.
```

## Real-locale release checklist

A locale is not stable until:

- every catalog entry is translated and structurally valid;
- UI, notifications, semantics, channels, and store metadata are complete;
- `en-XA` and `ar-XB` gates pass;
- native in-context review, read-aloud review, and glossary review are signed;
- health-copy subset has named elevated review;
- rollback is one revert PR and fallback-to-English is verified.
