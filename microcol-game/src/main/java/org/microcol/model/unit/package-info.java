/**
 * Concrete unit types, their factory and persistent actions such as plowing.
 * Shared Unit, AbstractUnit and UnitType classes remain in model. AI strategy
 * and animation belong to their respective packages. Colonist equipment exposes
 * read-only availability and inventory changes, then validates the complete
 * transaction before mutating equipment, colony stocks or European-market gold.
 * Concrete specialist colonists retain their type through equipment and place
 * changes; the factory and UnitPo preserve that identity. Ships retain their
 * departure coast across places and resolve arrival on the owner's turn; an
 * occupied return square keeps the same voyage waiting. Ship.advanceVoyage runs
 * before colony processing; inherited unit activation refreshes movement
 * afterward without advancing the voyage again. Its shared arrival operation
 * also resolves a model-validated zero-distance reversal without another turn.
 * Continuing actions report completion to AbstractUnit without invoking a
 * guarded player command.
 *
 * Persistent sentry suppresses automatic activation until explicit activation,
 * boarding or visible adjacent foreign movement.
 *
 * Servant and criminal types preserve their class through equipment and cargo;
 * production penalties remain in the shared model job calculation. Artillery
 * preserves its damage state independently of its purchase type; shared model
 * combat owns strength calculation and loss resolution. Laborer promotion
 * changes its type on the same registered unit; specialist promotion retains
 * its type and persists veteran status independently of equipment.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model.unit;
