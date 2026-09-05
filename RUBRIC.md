# FastTimes Translation PR Rubric

This rubric gates translation-stack PRs. Each item needs direct evidence; a missing oracle is not a pass.

| Item | Blocking oracle | Adversarial falsifier |
|---|---|---|
| Stable identity | Migration and behavior tests prove built-in semantics never depend on display copy | Restore one `profileName == translatedDisplayName` branch; gate must fail |
| Data compatibility | v6 database and current JSON fixtures retain custom profiles, favorites, history, ratings, and timestamps | Remove new-field default or misclassify a custom collision; fixture must fail |
| Honest scope | PR 1 contains no non-English locale directory and visible English fixtures are unchanged | Add `values-pt-rBR` or alter one built-in label; gate must fail |
| Catalog completeness | Compose-aware literal scan reports zero unapproved production outputs | Append a literal to `stringResource(...)`; scanner must still catch it |
| Placeholder/quantity correctness | Android format lint plus ICU/plural parity tests pass for locale categories | Drop/retype/reorder one placeholder or plural category; gate must fail |
| Locale formatting | Forced-locale tests cover date/time/number/percent/unit output and 12/24-hour behavior | Introduce one UI `ofPattern` or manual `h/%/days` suffix; gate must fail |
| Notifications and semantics | Non-English fixtures contain localized channels, bodies, actions, content descriptions, and chart/action semantics | Leave one channel/action or TalkBack label in English; gate must fail |
| RTL and expansion | `en-XA`/`ar-XB`, RTL, and 200% font core-journey checks pass | Restore physical swipe direction or fixed clipping; gate must fail |
| Linguistic trust | Different native reviewer signs in-context, read-aloud, glossary, and naturalness review | Literal but grammatical near-miss is rejected, not approved |
| Health fidelity | Named elevated reviewer confirms no stronger claim than approved source | Remove a hedge or introduce diagnosis/guarantee; release must block |
| Review independence | Generator is not sole technical or linguistic evaluator | Same-family self-review alone is insufficient |
| Gate integrity | No test/script/check threshold is weakened or broadly baselined | Diff touching gate to permit a known violation blocks review |

## Aggregate verdict

`PASS` requires every blocking item applicable to the current stack PR to pass with evidence. A first-locale PR additionally requires all human sign-offs. `UNVERIFIABLE` is not mergeable unless the PR explicitly remains infrastructure-only and the missing evidence is assigned to a later dependency before any locale release.
