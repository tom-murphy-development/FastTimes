# Research: trustworthy translation foundation

**Date**: 2026-08-11
**Snapshot**: `cb95125`
**Upstream goals**: [translation contributions #22](https://github.com/tom-murphy-development/FastTimes/issues/22) · [stable profile identity #24](https://github.com/tom-murphy-development/FastTimes/issues/24)

## Decisions

### D1 — Land stable profile identity before translating profile copy

**Decision:** The first implementation PR is upstream issue #24: add stable semantic identity for bundled profiles and fast history, migrate existing data additively, and preserve visible English.

**Why:** `DefaultFastingProfile.displayName` is currently treated as an ID (`DefaultFastingProfile.kt:68-70`), copied into Room (`DefaultFastingProfileProvider.kt:28-34`), denormalized into history (`Fast.kt:25-35`), exported (`DataManagementUseCase.kt:47`), and compared in behavior (`DashboardViewModel.kt:436`, `FastingSummaryModal.kt:92`, `DailyFastDetailsSheet.kt:177`). Translation before identity decoupling is unsafe.

**Rejected:**
- Extract all strings first: creates a huge conflict-prone diff and leaves profile translation unsafe.
- Combine migration and catalog extraction: couples schema risk to a mechanical mega-diff and weakens rollback/review.
- Infrastructure/docs only: does not remove the first correctness blocker.

### D2 — Do not ship a real locale in PR 1

**Decision:** PR 1 contains no non-English resource directory and advertises no new locale.

**Why:** Current source has only a small XML catalog and many user-facing Kotlin literals. A partial locale would be a mixed-language experience and an untruthful support claim.

### D3 — Defer generated LocaleConfig and AppCompat to a locale-capability PR

**Decision:** Add AGP-generated locale configuration only after source extraction is complete and immediately before the first real locale. Decide separately whether API 30–32 receives AppCompat per-app language support or documented device-language behavior.

**Technical target:** AGP's `androidResources { generateLocaleConfig = true }` plus `app/src/main/res/resources.properties` with `unqualifiedResLocale=en-US` when introduced. Do not combine a hand-authored locale config with generated configuration. If AppCompat's setter is selected for Compose, the hosting activity must extend `AppCompatActivity`; locale persistence (`autoStoreLocales` versus app-owned storage) is an explicit design choice, not an incidental manifest entry.

**Why:** FastTimes currently has no non-English locale to advertise. AppCompat also requires a dependency/activity/persistence decision for API 30–32 and deserves its own revert boundary.

### D4 — Use native Android acceptance checks, not Gherkin

**Decision:** Migration tests, JSON compatibility fixtures, JVM/Compose/instrumentation tests, Android Lint, pseudolocales, and deterministic static scanners form the acceptance layer.

**Why:** The repo has no maintained Android Gherkin runner or CI gate. Adding one would introduce a dead translation layer between criteria and tests.

### D5 — Use a custom literal gate after the source catalog is extracted

**Decision:** Android's XML hardcoded-text detector is not sufficient for Compose. Add a deterministic Compose-aware source scanner or custom lint rule in the catalog-extraction PR, with a reviewed narrow allowlist. Wire it through an existing Gradle task so the first stack need not edit GitHub Actions.

**Why:** Existing CI already executes `lintFossDebug` and `testFossDebugUnitTest`. A custom lint dependency or JVM test can become load-bearing without changing workflow files. A global zero-literal gate in PR 1 would require a meaningless baseline because the current tree intentionally still contains the debt.

### D6 — Human linguistic sign-off is a release gate

**Decision:** MT/LLM output is suggestion-only. Stable locale release requires a different native reviewer, in-context screenshots, read-aloud review, glossary consistency, and elevated review for health/physiology copy.

**Why:** Placeholder/resource checks can prove structure, not natural language or claim fidelity. A model must not self-certify its own translation.

### D7 — Hosted Weblate follows source readiness

**Decision:** Prefer hosted Weblate after catalog extraction, formatting, notification, accessibility, and locale-completeness gates exist. Crowdin remains an acceptable alternative if the maintainer prefers its workflow.

**Why:** Platform-first integration would expose a tiny partial catalog and waste volunteer effort. Weblate aligns with GPL-3.0, is Git-native, and preserves a self-hosted exit path.

## Current verified baseline

- `minSdk 30`, `targetSdk 36`, `compileSdk 36` (`app/build.gradle.kts`).
- AGP 9.1.1 and Compose/Material 3 project (`gradle/libs.versions.toml`).
- No real locale resource directory, locale config, AppCompat locale setter, screenshot framework, or localization-specific lint configuration.
- Baseline local gate passed on 11/08/2026: `spotlessCheck lintFossDebug testFossDebugUnitTest assembleFossDebug` — BUILD SUCCESSFUL.
- Coordinator scanner found 143 conservative user-output sink lines across 24 production files; prior broad audit found 232 candidate literal-bearing lines. The difference is scanner scope, not a contradiction. Neither number is a final catalog count.

## Risks and mitigations

| Risk | Mitigation |
|---|---|
| Room schema migration cannot be rolled back safely after a released database upgrade | Additive columns only; migration fixtures; land early in a release cycle; never bundle with unrelated copy changes |
| Old JSON lacks stable identity | Nullable/default compatibility plus fixture import and conservative derivation where safe |
| Custom profile has the same text as a built-in | Migrate only records with a defensible bundled-profile signature; preserve ambiguous/custom rows and document gaps |
| Source extraction introduces semantic copy changes | Preserve source English in extraction PR; isolate deliberate source-copy/health changes in reviewed commits or PRs |
| Locale mechanically complete but unnatural | Named native linguistic sign-off remains blocking |
| Health translation strengthens claims | Freeze/review source claims first; preserve hedges; require elevated reviewer sign-off |
| Scanner can be gamed by concatenation or broad allowlist | Flag literals adjacent to resource calls; line-granular reviewed allowlist; adversarial mutation tests |

## Open decisions

- **[GAP] First real locale:** `pt-BR` is recommended only if a distinct native reviewer is committed and the upstream maintainer accepts it.
- **[GAP] API 30–32 app-language UX:** AppCompat picker versus device-language-only behavior requires maintainer preference and install-base context.
- **[H] Profile migration ambiguity:** the selected fail-closed rule requires the complete original six-row seed cohort (IDs plus exact names, durations, and descriptions); partial/edited/shifted/single-row collisions remain custom. Schema v6 cannot disprove a malicious/theoretical custom database recreating the exact full cohort because it has no provenance marker. Record this residual limitation in the PR and release notes rather than claiming perfect derivation.
- **[GAP] Health reviewer:** no named medically informed reviewer is assigned yet; therefore health-copy changes and a stable translated locale remain release-blocked.

## Primary references

- Android per-app languages: https://developer.android.com/guide/topics/resources/app-languages
- Android string/plural resources: https://developer.android.com/guide/topics/resources/string-resource
- Android pseudolocales: https://developer.android.com/guide/topics/resources/pseudolocales
- ICU MessageFormat: https://developer.android.com/reference/android/icu/text/MessageFormat
- Compose screenshot testing status: https://developer.android.com/studio/preview/compose-screenshot-testing
- Weblate: https://weblate.org/en/
