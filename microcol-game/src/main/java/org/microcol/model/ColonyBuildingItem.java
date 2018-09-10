package org.microcol.model;

/**
 * Item that could be build. There should be extension for building and for
 * units.
 */
public abstract class ColonyBuildingItem {
    
    private final int requiredTools;

    private final int requiredHammers;

    ColonyBuildingItem(final int requiredTools, final int requiredHammers) {
        this.requiredTools = requiredTools;
        this.requiredHammers = requiredHammers;
    }

    public abstract String getName();

    /**
     * @return the requiredHammers
     */
    public int getRequiredHammers() {
        return requiredHammers;
    }

    /**
     * @return the requiredTools
     */
    public int getRequiredTools() {
        return requiredTools;
    }
}
