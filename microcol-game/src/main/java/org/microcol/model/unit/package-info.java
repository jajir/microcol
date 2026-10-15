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
 * afterward without advancing the voyage again. Continuing actions report
 * completion to AbstractUnit without invoking a guarded player command.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model.unit;
