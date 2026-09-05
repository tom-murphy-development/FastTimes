# Tasks: Trustworthy translation foundation

**Input**: `spec.md`, `research.md`, `data-model.md`, `plan.md`
**Acceptance layer**: native Android tests, lint, deterministic scripts; no Gherkin

## Phase 0: PR setup and immutable evaluation contract

- [ ] T001 [PR1] Add `scripts/verify-translation-foundation.sh` with current Gradle floor and read-only identity/no-locale/workflow-diff guards; obtain evaluator approval before implementation and do not weaken it afterward.
- [ ] T002 [P] [PR1] Add v6 database and old JSON fixtures under `app/src/androidTest/assets/translation-foundation/` and `app/src/test/resources/translation-foundation/`.
- [ ] T003 [P] [PR1] Add stable identity invariant tests covering display-name comparisons and unchanged English presentation.

**Checkpoint**: Tests/guards fail against current code for the intended reason; current baseline Gradle suite remains green.

## Phase 1: PR 1 — stable built-in profile identity (#24)

### Migration tests first

- [ ] T004 [PR1] Add `androidx.room:room-testing` for instrumentation and `org.xerial:sqlite-jdbc` for the load-bearing host-JVM SQL oracle to `gradle/libs.versions.toml` and `app/build.gradle.kts`; add `AppDatabaseMigrationSqlTest.kt` and `AppDatabaseMigrationTest.kt` covering exact seeds, edited seeds, shifted IDs, legacy open-fast values, favorites, history, and custom-name collisions. Existing CI must execute the JVM oracle even when no emulator is attached.
- [ ] T005 [P] [PR1] Add old-export decode/round-trip tests in `app/src/test/java/com/tmdev/fasttimes/data/DataManagementUseCaseTest.kt`.
- [ ] T006 [P] [PR1] Add behavior regression tests in `app/src/test/java/com/tmdev/fasttimes/ui/dashboard/DashboardViewModelTest.kt` proving open-fast behavior ignores visible names.

### Additive identity model

- [ ] T007 [PR1] Define stable serialized profile keys in `app/src/main/java/com/tmdev/fasttimes/data/DefaultFastingProfile.kt` without changing visible English.
- [ ] T008 [P] [PR1] Add nullable stable identity to `app/src/main/java/com/tmdev/fasttimes/data/profile/FastingProfile.kt` and `app/src/main/java/com/tmdev/fasttimes/data/fast/Fast.kt`, preserving old JSON defaults.
- [ ] T009 [PR1] Upgrade Room to v7 and add conservative 6→7 migration in `app/src/main/java/com/tmdev/fasttimes/data/AppDatabase.kt`; profile backfill requires expected seed ID plus exact full seed signature, history backfill requires name/duration/note evidence, ambiguous rows remain null; export and commit schema 7.
- [ ] T010 [PR1] Seed stable keys for new bundled profiles in `app/src/main/java/com/tmdev/fasttimes/data/profile/DefaultFastingProfileProvider.kt` and for newly started fasts in `app/src/main/java/com/tmdev/fasttimes/ui/dashboard/DashboardViewModel.kt`.
- [ ] T011 [PR1] Replace production behavior comparisons in `DashboardViewModel.kt`, `FastingSummaryModal.kt`, and `DailyFastDetailsSheet.kt` with stable identity.
- [ ] T012 [PR1] Update export/import derivation conservatively in `DataManagementUseCase.kt` only where required by old fixtures; never reclassify ambiguous custom content.
- [ ] T013 [PR1] Update affected test fixtures without weakening assertions.
- [ ] T014 [PR1] Run `scripts/verify-translation-foundation.sh`; record baseline, falsifier, and final outputs. In the PR body, state that PR 1 does not add a locale and list every remaining release blocker (catalog extraction, locale-safe formatting, notifications, accessibility/RTL, locale capability, named linguistic/health review, and store metadata), plus the additive Room migration's release-timing and non-downgrade caveat.

**PR 1 checkpoint**: All migration/compatibility/identity tests pass; visible English and workflows are unchanged; no locale ships.

## Phase 2: PR 2a — complete source catalog and trust documentation

- [ ] T015 [PR2A] Reconcile the conservative and broad literal inventories into a reviewed catalog map by sink/category.
- [ ] T016 [P] [PR2A] Externalize static Compose/dialog/settings/history/statistics/profile copy into `app/src/main/res/values/strings.xml` without semantic English edits.
- [ ] T017 [P] [PR2A] Replace ViewModel errors/snackbars with typed message IDs and arguments; never pass translated `String` through domain state.
- [ ] T018 [PR2A] Add translator comments, positional placeholder contracts, `tools:locale="en"`, glossary links, and translatable=false reasons.
- [ ] T019 [PR2A] Add Compose-aware literal and placeholder scanners under `scripts/localization/` with line-granular reviewed allowlists and mutation tests.
- [ ] T020 [PR2A] Wire scanners through an existing Gradle verification task so `.github/workflows/android.yml` need not change.
- [ ] T021 [PR2A] Add `en-XA` resource-resolution/semantics and 200% font-scale coverage for every screen/dialog touched by catalog extraction; no essential label clips, overlaps, or becomes ambiguous.

**PR 2a checkpoint**: zero unapproved production literals; pseudolocale exposes no unintended English on tested surfaces.

## Phase 3: Parallel runtime localization PRs

- [ ] T022 [P] [PR2B] Replace manual quantity/unit/percent/date/time formatting under #26; cover CLDR categories and 12/24-hour behavior.
- [ ] T023 [P] [PR2C] Localize notification channels/content/actions under #25; cover open, 1h, 2h, and 90min fixtures.
- [ ] T024 [P] [PR2D] Localize accessibility semantics and add bidi/RTL resilience under #28/#29; cover mixed-script user names.
- [ ] T025 [PR2D] Add compact `en-XA`/`ar-XB`, RTL, and 200% font-scale tests for changed surfaces.

**Runtime checkpoint**: UI, notifications, formatting, and spoken semantics all derive from locale-safe resources.

## Phase 4: PR 3 — locale capability

- [ ] T026 [PR3] Add AGP-generated LocaleConfig and source-locale declaration after the catalog is complete.
- [ ] T027 [PR3] Resolve and document API 30–32 behavior: AppCompat picker or device-language-only; test the selected contract.
- [ ] T028 [PR3] Add explicit locale selection UI only if the selected API contract requires it.
- [ ] T029 [PR3] Run merged-manifest, restart-persistence, notification-locale, `en-XA`, and `ar-XB` gates.

## Phase 5: PR 4 — first human-reviewed locale and Weblate

- [ ] T030 [PR4] Obtain upstream maintainer decision on first locale and named native translator/reviewer assignments.
- [ ] T031 [PR4] Review and source health/physiology copy under #32 before translating it; assign elevated reviewer.
- [ ] T032 [PR4] Translate the complete app and Fastlane metadata using the glossary and reviewed source.
- [ ] T033 [PR4] Complete distinct native linguistic review, read-aloud pass, in-context screenshots, and weighted back-translation sample.
- [ ] T034 [PR4] Prove locale completeness mechanically and record named sign-offs in the PR.
- [ ] T035 [PR4] Connect hosted Weblate only after catalog/gates are stable; document sync direction, permissions, and rollback.

**Release checkpoint**: No locale is advertised until every mechanical and named human gate passes.

## Dependencies

- PR 1 blocks every profile translation.
- PR 2a depends on PR 1 and blocks PRs 2b–2d and locale capability.
- PRs 2b–2d may run in parallel if they do not edit the same source/catalog blocks.
- PR 3 depends on runtime/catalog completeness.
- PR 4 depends on all earlier PRs plus named reviewer availability and upstream locale choice.

## Anti-gaming rules

- Tests and verification scripts are written/evaluated before implementation and cannot be weakened in the same slice.
- No baseline suppresses new literal/resource defects.
- No non-English locale directory appears before PR 4.
- A model-generated translation never receives sole same-model review.
- A health string without named elevated review blocks stable release.
