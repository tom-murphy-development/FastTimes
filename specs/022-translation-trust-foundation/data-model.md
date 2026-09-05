# Data model: stable built-in profile identity

## Existing problem

`displayName` and `profileName` are presentation copy but currently act as semantic identity. They are persisted in `fasting_profiles`, denormalized into `fasts`, serialized in exports, and compared by behavior.

## Target entities

### BuiltInProfileKey

A locale-independent identifier for one bundled profile.

Candidate values are stable machine identifiers such as:

- `open`
- `sixteen_eight`
- `fourteen_ten`
- `twelve_twelve`
- `eighteen_six`
- `twenty_four`

The serialized/database value is a stable string rather than an enum ordinal. Renaming a display label never changes this key.

### FastingProfile

Existing fields remain. Add:

- `builtInKey: String?`

Rules:

- Bundled profiles have a recognized non-null key.
- Custom profiles have `null`, even when their visible name matches a bundled profile.
- Display name and description remain persisted during PR 1 to preserve current UI and compatibility; a later catalog PR may resolve bundled presentation from resources.
- Favorite state, duration, and custom copy are untouched by migration.

### Fast

Existing fields remain. Add:

- `builtInProfileKey: String?`

Rules:

- New fasts started from a bundled profile persist its stable key.
- New custom-profile fasts persist `null` and retain the user's `profileName` verbatim.
- Open-fast behavior branches on the stable key, not the visible `profileName`.
- Existing history receives a stable key only when migration can identify a known bundled semantic record without guessing.

### Export record

PR 1 preserves compatibility with current JSON:

- Missing identity fields decode as `null`.
- New exports include nullable stable identity.
- Import may derive a built-in identity only using the same conservative rule as migration.
- User-entered names are never rewritten to a translation.

## Migration 6 → 7

1. Add nullable stable-key columns to `fasting_profiles` and `fasts`.
2. Backfill a seeded profile only when its expected original seed ID and its exact original name, duration, and description all match. This is a conservative historical heuristic, not proof of provenance: a custom row can theoretically reproduce that entire signature. The migration therefore also checks that the complete six-row seed cohort exists with the original IDs/signatures; otherwise it backfills no profile rows. A row edited by the user, inserted out of seed order, or otherwise ambiguous remains `null` and is treated as custom.
3. Backfill history only when legacy name, target-duration shape, and creation-note shape jointly identify a bundled record. Dedicated open fasts are distinguishable from user-created open-duration profiles because the dedicated path historically wrote `notes = null`, while profile starts wrote `Started <name> fast`. Ambiguous/imported rows keep their snapshot name and `null` identity.
4. Preserve every old column and value.
5. Export schema 7 and run migration validation.

## Invariants

- A locale switch cannot modify database rows.
- A translated label cannot change open-fast behavior.
- Two profiles may share visible text but remain semantically distinct.
- A custom profile never becomes built-in merely because its text matches; profile migration additionally requires the complete original six-row seed cohort. The remaining theoretical exact-cohort collision is recorded as a known migration limitation because schema v6 contains no provenance column; tests prove the migration fails closed for partial, shifted, edited, and single-row collisions.
- Old import data remains readable.
- New identity values are stable across future source-copy edits.

## Falsifiers

- Restoring any production equality comparison against `DefaultFastingProfile.*.displayName` fails the identity guard.
- Removing default/null handling for new JSON fields fails old-export fixture import.
- Changing a visible English name or description in PR 1 fails the English presentation fixture.
- Backfilling a custom collision as built-in fails the migration collision fixture.
- Dropping or rewriting any existing field fails migration row-equivalence checks.
