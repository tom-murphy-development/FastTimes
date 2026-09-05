# FastTimes Localization Constitution

This constitution governs translation and localization work prepared on branch
`022-translation-trust-foundation`. It supplements the existing GPL-3.0 project
without changing FastTimes' product scope.

## Core Principles

### I. Honest Completeness

A locale MUST NOT be presented as supported while any user-visible surface in
that locale falls back to English unintentionally. The completeness boundary
includes Compose UI, dialogs, errors, toasts and snackbars, notifications and
notification channels, accessibility semantics, built-in profile copy, and
store metadata. Deliberately untranslated brand names and user-entered text
MUST be documented in a reviewed allowlist.

**Rationale:** A mixed-language health app signals neglect. Honest scope builds
more trust than a larger but misleading language list.

### II. Meaning Before Literal Equivalence

Translations MUST preserve user intent, action, emotional register, and safety,
not English word order. One glossary term MUST name each domain concept across
the entire app. Source and target strings MUST be read in context; short labels
MUST be judged beside their screen, not in isolation.

Machine translation or an LLM MAY produce a suggestion, but that suggestion
MUST NOT be the only linguistic judgment. A real locale requires a distinct
linguistic reviewer, in-context screenshots, a read-aloud pass, and explicit
sign-off on glossary consistency. The author of a translation cannot be its
sole reviewer.

**Rationale:** Fluency is not trust. Consistent, context-correct language is.

### III. Stable Domain Identity

Localized display text MUST NOT be used as a database key, enum identity,
business-rule discriminator, route, or serialized semantic identifier.
Built-in content MUST have locale-independent identity. Schema and export
changes MUST preserve existing custom profiles, history, favorites, and old
imports through tested migration paths.

**Rationale:** Changing language must change presentation only, never meaning
or behavior.

### IV. Truthful Health Framing

A translation MUST NOT strengthen a physiological, medical, or risk-reduction
claim. Health-related strings MUST retain uncertainty and scope from the
approved source, use the glossary, preserve citations or reviewer notes, and
receive named health-copy review before release. FastTimes MUST remain a timer
and educational tracker, not imply diagnosis, treatment, or guaranteed benefit.

**Rationale:** Natural wording that overstates evidence is more dangerous than
awkward wording.

### V. Accessibility, Direction, and Expansion Are First-Class

Visual copy and accessibility copy are one catalog. Non-decorative semantics,
custom actions, charts, notification actions, and state descriptions MUST be
localized with the same quality bar as visible text. User-entered names MUST
remain verbatim and bidi-safe. Every changed surface MUST tolerate RTL,
pseudolocale expansion, and 200% font scale without hiding the task or control.

**Rationale:** A translation that excludes screen-reader, RTL, or large-text
users is not complete.

### VI. Proof Before “Done”

Localization work MUST have externalized, reproducible proof:

- Android Lint and formatting gates pass without weakening or baselining checks.
- Resource keys, placeholders, plurals, and formatted arguments have parity.
- A reviewed static scan reports zero unapproved user-facing literals.
- Native unit/Compose/instrumentation checks cover the changed behavior.
- `en-XA` and `ar-XB` exercise expansion and direction before a real locale.
- A real locale cannot be called complete without named linguistic sign-off.

Gherkin MUST NOT be introduced unless a maintained Android Gherkin runner and a
required CI gate first exist. Native Android tests and measurable acceptance
checks are the project contract.

A failing test, lint rule, screenshot, or linguistic falsifier MUST NOT be
weakened, skipped, re-baselined, or deleted to make a PR green.

## Delivery Workflow

Translation work lands as small, dependency-ordered PRs. Each PR MUST provide a
complete value that can be reverted with one commit and MUST state what it does
not yet make safe to release. Infrastructure, domain migration, source-catalog
extraction, first-locale translation, and hosted-platform integration SHOULD be
separate when combining them would hide review risk.

Every PR is reviewed through three independent lenses:

1. Android correctness and backward compatibility;
2. localization QA, accessibility, and locale completeness;
3. linguistic trust, terminology, and health-claim fidelity.

The implementation author is not the final judge. Cross-model review can find
technical and consistency defects but does not replace named linguistic
sign-off for a real locale.

## Governance

Changing a principle requires a written rationale and impact review. Any
exception MUST be explicit in the implementation plan, time-bounded, and must
prevent the affected locale from being advertised as complete.

**Version**: 1.0.0 | **Ratified**: 2026-08-11 | **Last Amended**: 2026-08-11
