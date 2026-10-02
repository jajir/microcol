# Remaining implementation estimate

Estimate date: 2026-10-02. Based on the [rules gap analysis](gap-analysis.md) at documentation commit [`f619e71c`](https://github.com/jajir/microcol/commit/f619e71cba9c7e7380c098e9b9cad20b59af01ed).

Completing the full gameplay scope in the gap analysis is estimated at **about 2,400 developer hours**. Use **3,000 hours as the planning budget**, including approximately 25% reserve. The broad optimistic-to-difficult range is **roughly 1,500–4,000 hours**; this is an initial engineering estimate, not a statistical confidence interval or a guaranteed upper limit. The 3,000-hour budget sits within that range; do not add the reserve to the range again.

## Calibration and assumptions

- The reference is the developer's estimate that implementing a small, understood behavior like **COL-006 would take four hours**. COL-006 already matches the stated rule, so it receives no new implementation hours.
- The working assumption is that those four hours include implementation and focused verification. This has not yet been calibrated against measured task completion times.
- The current inventory contains 228 entries: 20 Matches, 30 Partial, 40 Differs, 134 Missing and 4 Unclear. Entries differ substantially in size; they are not independent four-hour tasks.
- Existing production code, architecture and UI components are reused. Implemented behavior receives credit regardless of whether it has a UI test.
- Shared foundations are counted once. For example, Congress recruitment supplies common infrastructure, while individual founding fathers add their particular effects; trade routes reuse movement and cargo operations.
- Estimates represent developer effort. Calendar duration depends on productive hours available each week and is not a commitment to a release date.

## Work breakdown

| Remaining work | Approximate developer hours |
| --- | ---: |
| Rule decisions and shared unit/profession infrastructure | 130 |
| Setup, nationalities, difficulty, map generation and terrain | 260 |
| Movement, equipment, pioneers, cargo and trade routes | 370 |
| Colonies, production, construction, education and European markets | 340 |
| Native societies, diplomacy and rival AI | 500 |
| Combat, capture and naval damage/repair | 150 |
| Taxation, rebel sentiment, Congress and founding fathers | 200 |
| Revolution, victory conditions and scoring | 180 |
| Advisors, reports and autosave improvements | 60 |
| Whole-game integration, balancing and regression testing | 240 |
| **Total, rounded** | **2,400** |
| **Planning budget with reserve** | **3,000** |

Package figures are rounded individually. The underlying working estimate totals 2,408 hours; its 25% reserve produces 3,010 hours, rounded to the planning figures above. These figures express relative sizing, not that level of forecasting precision.

## Included scope and limits

The target is the full gameplay scope documented in the gap analysis, including all four nationalities and five difficulty levels. Each feature estimate includes its model changes, necessary player controls, saved-state changes and focused tests. The separate whole-game allowance covers cross-system integration, balancing, regression checks and complete playthroughs, rather than repeating the feature-level verification allowance.

Already matching behavior is not budgeted for reimplementation, although regression checks may exercise it. Rival AI is estimated for competent baseline play, not reproduction of the original game's exact decisions.

Excluded work includes new artwork, music, extensive visual redesign, strong compatibility with every historical save, and research to reproduce undocumented original-game behavior. Where the manual leaves formulas unspecified, the estimate assumes the project can make and document reasonable decisions promptly.

The largest uncertainties are new native systems, rival AI and diplomacy, cross-system political effects, and unresolved formulas for combat, markets, sentiment, recruitment and revolution. These account for much of the wide planning range.

## Calendar translation

For one developer using the **3,000-hour planning budget**:

| Productive development time | Approximate duration |
| --- | --- |
| 30 hours/week | 100 weeks — about 23 months |
| 20 hours/week | 150 weeks — about 35 months |
| 10 hours/week | 300 weeks — nearly 6 years |

These durations assume the stated productive hours are sustained; holidays and interruptions that reduce that average extend the schedule.

## Recalibration

Re-estimate after completing 5–10 representative tasks. Include a small rule correction, a persistent order, a feature connected to player controls, and part of a new subsystem. Record implementation, verification and integration time, compare actual effort with the four-hour reference, and update the affected packages and reserve. Timing only similarly small changes would not adequately calibrate the larger systems.
