# Feature Specification: Trustworthy translation foundation

**Feature Branch**: `022-translation-trust-foundation`
**Created**: 2026-08-11
**Status**: Ready for review
**Input**: Prepare the work for a translation PR whose language feels human and earns trust.

## Scope decision

This specification defines a **stack**, not one oversized PR.

- **PR 1 is the stable built-in profile identity migration described by upstream #24.** It makes future translation safe without changing visible English or claiming support for another locale.
- Source-catalog extraction, locale plumbing, notification/formatting work, and the first real locale follow as dependency-ordered PRs.
- No real locale is advertised or shipped while the app can still expose unintended English on any user-facing surface.

The first real locale is not selected by this PR. Brazilian Portuguese is a strong candidate because a native reviewer is available, but the upstream maintainer owns that product decision.

## User Scenarios & Testing

### User Story 1 - Language changes presentation, not behavior (Priority: P1)

A user who changes FastTimes' language sees translated built-in profile names and descriptions in a later locale PR, while existing fasts, favorites, custom profiles, imports, and open-fast behavior retain exactly the same meaning.

**Why this priority**: Today English display names are database identity and business-rule inputs. Translating before fixing this can silently change behavior or strand existing data.

**Independent Test**: Create a database and export using the current English schema, migrate it, then resolve built-in presentation in another locale. Stable identities, durations, favorites, custom names, and open-fast behavior remain unchanged while built-in labels can change.

**Acceptance Criteria**:

- WHEN a version-6 database containing all built-in profiles, legacy open-fast names, custom profiles, favorites, and history is upgraded THEN every known built-in receives the correct stable identity and all user data remains byte-for-byte equivalent except for the added identity field.
- WHEN a pre-change JSON export without the new identity field is imported THEN it retains the same fast history and obtains or derives the same semantic identity where possible.
- WHEN the app compares or branches on a built-in profile THEN it uses stable identity rather than localized display text.
- WHEN PR 1 is installed in English THEN every visible built-in name and description is unchanged from the current release.

---

### User Story 2 - Contributors translate a complete, contextual catalog (Priority: P2)

A translator can find every visible and spoken source string in one reviewed catalog, understand where it appears, follow a stable glossary, and see which health strings need elevated review.

**Why this priority**: A translation platform connected to a partial catalog invites volunteer work that cannot produce a complete experience.

**Independent Test**: Run the literal/resource inventory and pseudolocale build. Every visual, notification, validation, and accessibility string resolves from the source catalog, and each dynamic string has placeholder and translator context.

**Acceptance Criteria**:

- WHEN the literal scanner runs THEN it reports zero unapproved user-facing literals in production source.
- WHEN `en-XA` runs through every screen and notification fixture THEN all source copy is pseudolocalized; no unintended English remains.
- WHEN a translator opens the catalog THEN dynamic placeholders, length constraints, domain context, and health-review requirements are documented.
- WHEN the same concept appears on multiple screens THEN the source uses one canonical glossary term.

---

### User Story 3 - A user experiences one natural, complete locale (Priority: P3)

A user selecting a supported locale receives natural, consistent language across UI, errors, notifications, accessibility semantics, built-in profiles, and store metadata, without stronger health claims or broken formatting.

**Why this priority**: User trust is earned by complete in-context quality, not by the number of locale directories.

**Independent Test**: Install the real-locale build, complete the core start/watch/end/history/settings journey with TalkBack, and compare screenshots/read-aloud output to the reviewed glossary and source intent.

**Acceptance Criteria**:

- WHEN a supported locale is selected THEN no unintended English appears in the core journey, notifications, Android notification settings, TalkBack output, or store metadata.
- WHEN quantities, dates, times, percentages, durations, or user names appear THEN grammar, order, digits, direction, and placeholders follow that locale.
- WHEN health copy is translated THEN every hedge and scope limitation from the approved source remains present and no claim is stronger.
- WHEN linguistic review encounters a literal but unnatural translation THEN the locale cannot be marked complete until native in-context review approves revised wording.

## Edge Cases

- A built-in profile name is identical to a user-created custom profile name.
- A database contains legacy `No Goal` or `Manual` history values.
- An old export lacks stable identity; a new export is opened by an older app.
- A user-supplied profile name mixes Arabic/Hebrew and Latin text, emoji, quotes, or digits.
- A quantity uses zero, one, two, few, many, or other grammatical categories.
- The app locale differs from the device locale and from the 12/24-hour setting.
- A translated health term has no safe natural equivalent; the reviewer must preserve meaning and uncertainty rather than improvise a claim.
- A locale is mechanically complete but lacks named linguistic or health-copy sign-off.

## Requirements

### Functional Requirements

- **FR-001**: Built-in profiles and fast history MUST have locale-independent semantic identity before their display copy becomes localizable.
- **FR-002**: Migration and import compatibility MUST preserve existing custom names, durations, favorites, history, ratings, timestamps, and open-fast behavior.
- **FR-003**: PR 1 MUST preserve current visible English exactly and MUST NOT add or advertise a non-English locale.
- **FR-004**: All user-facing production copy MUST ultimately live in a translatable catalog or a reviewed non-translatable allowlist.
- **FR-005**: Translator guidance MUST define tone, glossary terms, placeholders, bidi handling, accessibility context, and elevated health-copy review.
- **FR-006**: Simple grammatical counts MUST use locale-aware quantity selection; complex selection/multi-quantity messages MUST preserve every locale's grammatical choices.
- **FR-007**: Dates, times, numbers, percentages, units, and durations MUST follow the active app locale while respecting the user's 12/24-hour preference.
- **FR-008**: Notification channels, notification content/actions, validation/errors, and accessibility semantics MUST use the same active locale as the app where the platform permits.
- **FR-009**: User-entered names MUST remain verbatim and bidi-safe; translations MUST NOT mutate them.
- **FR-010**: Health-related translation MUST NOT strengthen the approved source claim and MUST require named reviewer sign-off.
- **FR-011**: A locale MUST NOT be declared complete until mechanical completeness and named native linguistic review both pass.
- **FR-012**: Every stack PR MUST be independently reviewable and state which remaining dependency prevents a real locale release.

### Key Entities

- **Built-in profile identity**: Stable semantic key for a bundled fasting profile, independent of any language.
- **Built-in profile presentation**: Locale-specific name and description resolved only for display.
- **Custom profile**: User-authored profile whose name and description remain verbatim and never masquerade as a built-in.
- **Source message**: Canonical English message with context, placeholders, constraints, and review class.
- **Locale message**: Reviewed target-language expression of one source message.
- **Glossary entry**: One canonical domain concept and its approved source/target forms.
- **Review sign-off**: Named evidence that a locale or health-copy subset passed required linguistic review.

## Success Criteria

### Measurable Outcomes

- **SC-001**: A version-6 migration fixture and old export fixture pass with zero lost or changed user data beyond additive stable identity; partial, shifted, edited, and single-row custom collisions remain unclassified (`null`).
- **SC-002**: PR 1 produces zero intentional visible-copy changes and contains no non-English resource directory.
- **SC-003**: Before the first locale PR, the production literal scanner reports zero unapproved user-facing literals and pseudolocales expose no unintended English in the tested core journey.
- **SC-004**: Every formatted message passes placeholder parity; every quantity test covers all grammatical categories required by the target locale.
- **SC-005**: Every first-locale screen and notification fixture has named native-speaker in-context sign-off; every health-copy message has named elevated review.
- **SC-006**: A locale can be removed with one revert PR and the app safely falls back to English without corrupting persisted data.

## Assumptions

- American English (`en-US`) remains the canonical source locale.
- Upstream prefers small correctness PRs over a single translation mega-diff.
- Hosted translation-platform integration follows source readiness rather than preceding it.
- A distinct human native reviewer is required for a stable locale; model review is supplementary.
- Existing Android native tests, resource lint, and deterministic scripts are the acceptance layer; Gherkin is not introduced.
