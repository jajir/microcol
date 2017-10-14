package org.microcol.model;

import org.microcol.model.store.PlacePo;
import org.microcol.model.store.UnitPo;

/**
 * Just hold information about some place.
 */
public interface Place {

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
	 * should be notified.
	 */
	void destroy();

	/**
	 * Save place internal state to {@link PlacePo} object that could be stored.
	 * 
	 * @param unitPo
	 *            required unit persistent object where will be place stored
	 * @return return saved state to object
	 */
	PlacePo save(UnitPo unitPo);

}
