package org.microcol.model;

/**
 * Item that could be build. There should be extension for building and for
 * units.
 */
public abstract class ColonyBuildingItem {

    public abstract String getName();

    /**
     * @return the requiredHammers
     */
    public abstract int getRequiredHammers();

    /**
     * @return the requiredTools
     */
    public abstract int getRequiredTools();
}
