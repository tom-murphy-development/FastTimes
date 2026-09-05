# Implementation Plan: Trustworthy translation foundation

**Branch**: `022-translation-trust-foundation` | **Date**: 2026-08-11 | **Spec**: `spec.md`

## Summary

Deliver translation safely as stacked PRs. PR 1 introduces additive, locale-independent identity for bundled profiles and history, migrates current data, preserves old exports, and leaves all visible English unchanged. Subsequent PRs externalize the complete catalog, fix locale-sensitive formatting/notifications/accessibility, add locale plumbing and pseudolocale gates, then add one human-reviewed real locale and Weblate.

## Technical Context

**Language/Version**: Kotlin 2.3.21; Java 21 toolchain
**Primary Dependencies**: Jetpack Compose, Material 3, Room 2.8.4, DataStore, kotlinx.serialization, Hilt
**Storage**: Room database v6 plus JSON import/export
**Testing**: JUnit 4, MockK, Turbine, AndroidX Compose instrumentation, Android Lint, Spotless
**Target Platform**: Android API 30–36
**Project Type**: Single Android mobile application with FOSS and Play Store flavors
**Performance Goals**: No measurable startup/timer regression; localization formatting off hot animation paths
**Constraints**: Offline-first, no analytics/accounts, user-visible English unchanged in PR 1, no real locale until completeness gates pass
**Scale/Scope**: 81 main Kotlin files; translation debt spans approximately 24 production files

## Constitution Check

### Pre-design gate

| Principle | Result | Evidence |
|---|---|---|
| Honest Completeness | PASS | PR 1 adds no locale; stack blocks first locale on catalog/notification/a11y completeness |
| Meaning Before Literal Equivalence | PASS | Voice/glossary and distinct native review are release requirements |
| Stable Domain Identity | PASS | PR 1 exists specifically to remove display-copy identity |
| Truthful Health Framing | PASS WITH BLOCKER | No health copy changes in PR 1; first locale remains blocked until source/reviewer gate exists |
| Accessibility/Direction/Expansion | PASS BY PLAN | Catalog and locale PRs require semantics, `en-XA`, `ar-XB`, and 200% font checks |
| Proof Before Done | PASS | Native tests/lint/scanners, no Gherkin, no self-certification |

### Post-design gate

PASS. The additive identity model preserves presentation and compatibility. Generated LocaleConfig, AppCompat, literal lint, and real translation are correctly deferred to their owning PRs rather than hidden in PR 1.

## Project Structure

```text
app/
├── build.gradle.kts
├── schemas/com.tmdev.fasttimes.data.AppDatabase/
├── src/main/java/com/tmdev/fasttimes/
│   ├── data/
│   │   ├── AppDatabase.kt
│   │   ├── DefaultFastingProfile.kt
│   │   ├── fast/Fast.kt
│   │   └── profile/FastingProfile.kt
│   ├── ui/dashboard/DashboardViewModel.kt
│   ├── ui/dashboard/FastingSummaryModal.kt
│   └── ui/history/DailyFastDetailsSheet.kt
├── src/test/java/com/tmdev/fasttimes/
└── src/androidTest/java/com/tmdev/fasttimes/data/

docs/
└── TRANSLATION.md

scripts/
└── localization/                    # introduced in catalog/gate PR, not PR 1

specs/022-translation-trust-foundation/
├── spec.md
├── research.md
├── data-model.md
├── plan.md
├── quickstart.md
├── tasks.md
└── checklists/
```

**Structure Decision**: Keep the existing single Android app. Add no runtime service or translation dependency in PR 1.

## PR Stack and Dependency DAG

```text
PR 1: stable built-in identity (#24)
  ├── PR 2a: complete source catalog + voice/glossary + Compose literal gate
  │     ├── PR 2b: locale-aware quantities/date/number formatting (#26)
  │     ├── PR 2c: notification localization (#25)
  │     └── PR 2d: accessibility semantics + RTL/bidi resilience (#28/#29)
  └──────────────────────────────────────────────────────────┐
PR 3: locale capability (generated LocaleConfig; API 30–32 decision; pseudo QA)
                                                              │
PR 4: first real locale + store metadata + named reviews + Weblate
```

PRs 2b–2d may proceed in parallel after 2a when they do not edit the same catalog block. PR 4 is blocked by all preceding runtime/completeness work.

## PR 1 Detailed Scope

### Included

- Add stable nullable key to bundled profiles and history.
- Upgrade Room v6→v7 with additive migration and exported schema.
- Backfill bundled profile rows only when the complete six-row original seed cohort agrees (expected IDs plus exact bundled duration/name/description); partial, shifted, edited, or ambiguous cohorts remain custom (`null`). Schema v6 has no provenance marker, so the theoretical exact-cohort custom collision is documented and tested as the irreducible limit. Backfill history only when the legacy name, target-duration shape, and creation-note shape jointly identify a bundled/open fast; ambiguous rows retain their snapshot name and `null` identity.
- Write stable keys on all newly created built-in/open fasts.
- Replace display-name behavior comparisons with stable-key comparisons.
- Preserve old JSON import through nullable/default fields and fixtures.
- Add migration, behavior, compatibility, collision, and visible-English regression tests.
- Commit the spec artifacts and translation constitution.

### Excluded

- Non-English resources or store listing.
- Complete string extraction or source-copy edits.
- Health-claim wording changes.
- Notifications, plural/date/number formatting, accessibility, RTL, or layout changes.
- Generated LocaleConfig, AppCompat dependency/activity changes, or language picker.
- Weblate/Crowdin configuration.
- GitHub Actions edits.
- Fixing the separate dead quick-select display-name filter (#30).

## PR 1 Verification Contract

The checked-in `scripts/verify-translation-foundation.sh` runs:

1. `spotlessCheck`
2. `lintFossDebug`
3. `testFossDebugUnitTest`
4. migration instrumentation/managed-device test when an emulator is available
5. `assembleFossDebug`
6. deterministic identity-comparison and no-real-locale guards
7. diff guards proving `.github/workflows/` is untouched and visible English fixtures match

The script supports `--baseline` (green on current main) and final mode (intentionally red until PR 1 is implemented). It is immutable to the implementation agent once the evaluator approves it. A failure is fixed in code/tests, never by weakening the script.

## Later PR Resource Rules

| Message shape | Resource contract |
|---|---|
| Static label/title/body | `<string>` + `stringResource`/`Context.getString` |
| Simple grammatical count | `<plurals>` + quantity API |
| Nested select or multiple quantities | API-24+ ICU MessageFormat wrapper with placeholder parity tests |
| Rich phase description | CDATA/annotated resource, translator context, health-review class |
| Dynamic visual phrase | One complete formatted resource, never fragments concatenated in English order |
| Accessibility action/state | Dedicated resource/quantity with semantics tests |
| Notification channel/action/body | Context resource resolved under app locale, fixture-tested |
| User-entered text | Verbatim placeholder with bidi isolation, never translated |

## Complexity Tracking

No constitution violation is accepted. The Room schema addition is necessary because current presentation copy is load-bearing domain identity; a documentation-only or catalog-only alternative cannot make translation behavior-safe.
