# FastTimes agent delivery contract

## Translation stack

Read these before translation work:

1. `.specify/memory/constitution.md`
2. `specs/022-translation-trust-foundation/spec.md`
3. `specs/022-translation-trust-foundation/plan.md`
4. `specs/022-translation-trust-foundation/tasks.md`
5. `docs/TRANSLATION.md`
6. `RUBRIC.md`

## Rules

- One worktree per PR. Never edit the main worktree.
- Implement the first unchecked dependency-safe task only.
- Native Android tests are the acceptance layer; do not introduce Gherkin without a maintained runner and required CI gate.
- Write a falsifying test/gate before implementation and show it fails for the intended reason.
- Never weaken, skip, delete, re-baseline, or edit a gate to green the implementation.
- Do not add a real locale before the complete catalog, formatting, notification, accessibility, RTL, and locale-capability dependencies pass.
- Do not use localized display text as identity or business state.
- Machine/LLM translations are suggestions only and require a different native linguistic reviewer.
- Health/physiology strings require elevated named review and may never become stronger than the approved source.
- The implementation author cannot be the only reviewer. Use a different model family for technical review; human sign-off remains mandatory for stable real locales.

## Baseline verification

```bash
./gradlew spotlessCheck lintFossDebug testFossDebugUnitTest assembleFossDebug \
  --parallel --max-workers="$(nproc)" --console=plain
```

PR-specific `verify.sh` scripts add to this floor; they never replace it.
