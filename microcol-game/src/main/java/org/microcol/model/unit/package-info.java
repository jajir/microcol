/**
 * Concrete unit types, their factory and persistent actions such as plowing.
 * Shared Unit, AbstractUnit and UnitType classes remain in model. AI strategy
 * and animation belong to their respective packages. Colonist equipment exposes
 * read-only availability and inventory changes, then validates the complete
 * transaction before mutating equipment, colony stocks or European-market gold.
 *
 * @see <a href="https://jajir.github.io/microcol/architecture.html">Package
 *      responsibilities</a>
 */
package org.microcol.model.unit;
