package org.microcol.model;

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

}
