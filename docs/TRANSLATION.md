# Translating FastTimes

FastTimes translations should feel written for the person reading them, not converted from English. A language is supported only when the complete user experience has been reviewed in context.

## Current status

American English (`en-US`) is the source locale. The translation stack is being prepared in dependency order. Do not add a locale directory until stable built-in identity, complete source extraction, formatting, notifications, accessibility semantics, and locale QA gates are ready.

## Voice

- **Interface:** concise, calm, direct. Use familiar platform language.
- **Errors:** say what happened and what the person can do next. Never blame.
- **Destructive actions:** name the consequence clearly; do not soften deletion or replacement.
- **Progress:** describe the data without forced praise or judgment.
- **Notifications:** informational and actionable, not chatty.
- **Health information:** cautious and scoped. Preserve `may`, `can`, and `is associated with`. Never introduce diagnosis, treatment, guaranteed benefit, or weight-loss promises.

Translate complete ideas, not English fragments. Natural target-language order wins over source word order as long as meaning, action, uncertainty, and placeholders are preserved.

## Canonical terminology

| Concept | English source term | Notes |
|---|---|---|
| One period of fasting | **fast** | Avoid “session” unless needed for clarity. |
| Named reusable target | **profile** | Do not alternate with plan/program/protocol. |
| No target duration | **Open Fast** | A profile/state, not the command “open a fast.” |
| Target duration achieved | **goal met** | Distinct from manually ending a fast. |
| Manual stop action | **end fast** | Do not use cancel; the history still exists. |
| Preferred profile | **favorite** | US English is canonical; do not mix “favourite.” |
| Consecutive fasting days | **streak** | Keep consistent across Dashboard and Statistics. |
| Time between fasts | **eating window** | Do not introduce diet/calorie tracking concepts. |

Brand/platform names such as FastTimes, Material 3, F-Droid, Obtainium, GitHub, Google Play, and GNU GPL are not translated unless an official localized brand form exists.

## Health terminology

Autophagy, ketosis, mitophagy, glycogen, insulin, mTOR, stem cells, and Human Growth Hormone are reviewer-gated terms. Translator comments must identify their scientific context. A translation must never strengthen the approved English claim.

Current fasting-phase source copy requires a separate evidence and safety review before a stable locale may ship. A faithful translation of an overstated source is still unsafe.

## Context and placeholders

Every dynamic resource must include:

- a translator comment describing screen, speaker, action, and length constraint;
- positional placeholders with types and examples;
- the full sentence or phrase so the translator controls word order;
- quantity resources for grammatical counts;
- bidi isolation for user-supplied names embedded in running text;
- a review class when the message is accessibility or health related.

Do not concatenate translated fragments such as `Today, ` + date, number + `h`, or profile name + English suffix.

## Review workflow

1. Freeze and review the English source and glossary.
2. A native translator writes or revises the target string in context.
3. A different native linguistic reviewer checks meaning, tone, glossary, placeholders, and naturalness.
4. Review screenshots and read the core journey aloud on a device.
5. Spot-check back-translation, weighted toward health and destructive copy.
6. A named elevated reviewer signs health/physiology strings.
7. Mechanical locale gates and the named sign-offs must both pass.

Machine translation and LLMs may suggest wording. They are not reviewers, and their output never ships without distinct human review. The model or service that generated a suggestion must not be its only automated reviewer either.

## Falsifiers

Reject a translation when any of these is true:

- It adds praise, urgency, blame, diagnosis, or benefit not present in the approved source.
- It drops uncertainty such as “may” or “can.”
- It translates **Open Fast** as an imperative action.
- It uses inconsistent words for profile, goal met, end fast, or fasting phase.
- It copies English singular grammar for a plural quantity.
- It drops/retypes a placeholder or mutates a user-entered name.
- It reads grammatically but sounds like literal machine translation when spoken in context.
- Any core screen, notification, TalkBack label, channel, or store listing falls back to unintended English.

## Completion levels

- **Mechanically ready:** resource/literal/placeholder/plural/pseudolocale gates pass.
- **Preview:** mechanically ready and explicitly labeled as awaiting full linguistic review.
- **Stable:** mechanically ready plus named native in-context, read-aloud, glossary, and required health-copy sign-off.

Only stable locales may be advertised as supported without qualification.
