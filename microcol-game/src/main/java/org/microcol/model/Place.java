package org.microcol.model;

import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

/**
 * Just hold information about some place.
 */
interface Place {

    /**
     * Return unit which place is described by this instance;
     * 
     * @return described unit
     */
    Unit getUnit();

    /**
     * Get name of place type.
     * 
     * @return place type name
     */
    String getName();

    /**
     * When place is not longer valid than in some cases other side of relation
     * should be notified. Method consequently destroy related objects.
     */
    void destroy();

    /**
     * When place is not longer valid than in some cases other side of relation
     * should be notified. Method doesn't perform any validations. Just destroy
     * place.
     */
    void destroySimple();

    /**
     * Save place internal state to {@link PlacePo} object that could be stored.
     * 
     * @param unitPo
     *            required unit persistent object where will be place stored
     * @return return saved state to object
     */
    PlacePo save(UnitPo unitPo);

}
