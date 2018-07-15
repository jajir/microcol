package org.microcol.model.unit;

import org.microcol.model.Cargo;

/**
 * Identify units that could hold cargo. It could be naval and land units.
 */
public interface CargoHolder {

    /**
     * Provide cargo object.
     *
     * @return cargo object.
     */
    Cargo getCargo();

}
