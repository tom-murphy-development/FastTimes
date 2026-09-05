# Requirements Checklist: Trustworthy translation foundation

**Purpose**: Validate that the spec, plan, and tasks define an honest, reviewable translation stack before implementation.
**Created**: 2026-08-11
**Feature**: `../spec.md`

## Scope and dependency integrity

- [x] CHK001 PR 1 states one independently revertible value and explicitly excludes real locales, catalog mega-diff, locale picker, health copy changes, and Weblate. Evidence: `plan.md` PR 1 Included/Excluded.
- [x] CHK002 Stable domain identity is a prerequisite for translated built-in profile presentation. Evidence: `spec.md` FR-001 and `data-model.md`.
- [x] CHK003 Each later PR has a clear dependency and does not claim completion early. Evidence: `plan.md` dependency DAG and `tasks.md` dependencies.
- [x] CHK004 The first real locale remains an upstream product decision with named reviewer availability as a blocker. Evidence: `spec.md` Scope decision and `research.md` Open decisions.

## Data safety

- [x] CHK005 Migration criteria include every built-in, legacy open-fast names, custom-name collision, favorite state, and history. Evidence: `tasks.md` T004/T009.
- [x] CHK006 Existing columns/values are preserved and new identity fields are additive. Evidence: `data-model.md` migration steps and invariants.
- [x] CHK007 Old JSON without identity remains importable. Evidence: `spec.md` FR-002 and `tasks.md` T005/T008.
- [x] CHK008 User-entered names/descriptions remain verbatim and ambiguous records fail closed. Evidence: `data-model.md` complete-cohort rule and residual limitation.
- [x] CHK009 Migration and rollback limitations are explicit. Evidence: `research.md` Risks/Open decisions and `tasks.md` T014.

## Linguistic trust

- [x] CHK010 Voice rules cover labels, errors, destructive actions, notifications, progress, accessibility, and health copy. Evidence: `docs/TRANSLATION.md` Voice.
- [x] CHK011 One glossary term names every load-bearing domain concept. Evidence: `docs/TRANSLATION.md` Canonical terminology.
- [x] CHK012 Near-miss examples discriminate natural/contextual translation from literal or inflated wording. Evidence: `docs/TRANSLATION.md` Falsifiers and `RUBRIC.md`.
- [x] CHK013 Machine translation/LLM output requires a distinct native linguistic reviewer. Evidence: constitution Principle II and `AGENTS.md`.
- [x] CHK014 Health claims preserve hedges and require elevated named review. Evidence: constitution Principle IV and `docs/TRANSLATION.md` Health terminology.
- [x] CHK015 Completeness includes store metadata, notifications, semantics, and channels, not only XML keys. Evidence: constitution Principle I.

## Mechanical proof

- [x] CHK016 Every acceptance criterion names an observable result and deterministic oracle or named human sign-off. Evidence: `spec.md` criteria and `tasks.md` test-first tasks.
- [x] CHK017 Native Android tests are used; no dead Gherkin layer is introduced. Evidence: constitution Principle VI and `AGENTS.md`.
- [x] CHK018 Literal/placeholder allowlists are narrow, reviewed, and mutation-tested. Evidence: `tasks.md` T019 and `RUBRIC.md` falsifier.
- [x] CHK019 `en-XA`, `ar-XB`, RTL, and large-font gates are assigned to concrete stack PRs. Evidence: `tasks.md` T021/T025/T029.
- [x] CHK020 Existing tests/gates cannot be weakened, skipped, deleted, or broadly baselined to green the work. Evidence: constitution Principle VI and `tasks.md` anti-gaming rules.
- [x] CHK021 PR 1 visibly preserves current English and contains no non-English locale directory. Evidence: `spec.md` FR-003 and verifier scope guard.

## Upstream reviewability

- [x] CHK022 PR 1 can be explained as a correctness/data-model fix without requiring acceptance of the entire translation roadmap. Evidence: `spec.md` Scope decision and upstream #24.
- [x] CHK023 T014 requires the PR body to state every remaining blocker before any locale may ship. Evidence: `tasks.md` T014.
- [x] CHK024 GitHub Actions remain untouched in PR 1. Evidence: verifier workflow-diff guard and `plan.md` Excluded.
- [x] CHK025 T014 requires the PR body to state the additive Room migration's release timing and non-downgrade risk. Evidence: `tasks.md` T014.

## Result

All planning items passed static review. Coordinator execution evidence: verifier baseline passed and final mode failed on the intended preimplementation falsifier (`Room schema 7 is missing`). A Codex planning gate first BLOCKED five setup defects; they were repaired. A subsequent independent read-only review returned ALLOW. Human linguistic and health sign-offs remain future release gates, not claims made by this setup PR.
