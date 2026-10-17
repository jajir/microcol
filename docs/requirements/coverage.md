# Documented scope and requirement coverage

1. Scope baseline: `f902be8a`, inspected on 2026-10-17. The user requested
   requirements for all remaining outcomes in the current project documentation,
   followed by a consolidated list of open points. This is preparation only:
   newly captured requirements remain `new`; existing implementation authorizations
   and requirement statuses are not extended by this inventory.
2. Preparation is in progress. The existence of a rule link or requirement does
   not prove implementation, acceptance or publication. This page records scope
   ownership; the individual requirements own behavior, dependencies and questions.
3. Reuse [existing requirements](backlog.md) and the
   [migration history](migration-2026-10-14.md). Preserve completed slices and
   distinguish missing behavior, missing acceptance and unselected specification
   proposals. IDs are identifiers, not an implementation order.

## Sources and completion checks

| Source | Required coverage evidence |
| --- | --- |
| [Rule catalogue](../gap-analysis.md#rule-catalogue) | Each of the 230 stable rule IDs has a named owner or a justified existing/cross-cutting disposition; partial ownership includes its remaining outcomes. |
| [Use cases](../use-cases.md) | All 119 UC IDs retain their Hra/Nastavení/Regrese categories and all mandatory variants, including integrated journeys UC-115–118. |
| [Mandatory matrices and reports](../use-cases.md#povinne-matice-a-obsah-prehledu) | Every matrix and report row has ownership beyond a single representative test or supported unit type. |
| [Implementation plan](../implementation-plan.md#seznam-pracovnich-baliku) | All P01–P32 outputs, including shared quality and delivery work, have explicit ownership or an ongoing contract. |
| [UX](../ux.md), [graphics](../graphics.md), [audio](../audio.md) | Feature integration retains these contracts; separate missing shared capabilities are captured once, without inventing an asset count or redesign. |
| [Distribution](../distribution.md#distribution-scope-and-remaining-release-work), [development delivery](../development-builds.md), [estimate](../implementation-estimate.md) | Credit delivered packaging/CI/launcher work; distinguish remaining release outcomes from historical budgets and optional scenarios. |
| [Requirement workflow](README.md) | Small outcomes, four numbered chapters, stable IDs, explicit dependencies and genuine unanswered questions; strict documentation validation and publication. |

## Working coverage map

1. The existing five pioneer captures [R-0118](R-0118.md)–[R-0122](R-0122.md)
   own tool admission, plowing, clearing, road construction and road production.
   Their linked shared decisions remain authoritative.
2. The next A05 captures assign terrain identity and missing defense cells to
   [R-0123](R-0123.md), baseline output to [R-0124](R-0124.md), exact movement
   cost/path/range to [R-0125](R-0125.md), rivers to [R-0126](R-0126.md), and
   special-resource overlays to [R-0127](R-0127.md). Existing R-0093 defense
   corrections are preserved. Lake output/travel and generator placement are
   explicit open points, not inferred ocean behavior.
3. [R-0128](R-0128.md) owns finite mineral depletion, [R-0129](R-0129.md)
   one-time rumor outcomes, [R-0130](R-0130.md) persistent go-to and its
   cancellation, and [R-0131](R-0131.md) player disband. MOVE-008 is traced
   through each order owner rather than duplicated as a universal rewrite:
   sentry R-0073, fortify R-0106, work R-0119–R-0121, go-to R-0130 and
   the forthcoming route-edit/cancel capture.
4. Remaining area-by-area ownership is being reconciled with existing work.
   This incomplete map is not a claim that the documented scope is covered.

## Preparation checkpoints

| Area | Capture progress | Remaining reconciliation |
| --- | --- | --- |
| A05 world and units | R-0118–R-0131 plus existing sight, ship, sentry and combat owners | Final unit/terrain matrix and cross-area dependency audit |
| A06 transport and Europe | Existing bounded transport and purchase work retained | Cargo residuals, routes, market prices, quotes and accounts |
| A07 economy | Existing food, work, construction and education owners retained | Terrain-independent production, specialists, native learning/converts and complete matrices |
| A08 relations and AI | Existing diplomacy and rival colony owners retained | Native state/interactions, missions, trade and remaining actor policies |
| A09 combat | Delivered ordinary/naval slices retained | Native/royal roles, patrol, coastal fire and national modifier |
| A10 politics and end | Existing support, Congress and declaration owners retained | Remaining founders, revolution, dates and scoring |
| A01–A04 and shared work | Existing setup, menus, save and turn work retained | Reports/help, nine mandatory matrices, four integrated journeys, UX/assets/audio and release scope |

This table is a preparation checkpoint, not completed coverage or an implementation
priority. The final consolidated open-point list follows reconciliation of all areas.
