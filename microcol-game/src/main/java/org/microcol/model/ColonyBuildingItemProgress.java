package org.microcol.model;

/**
 * Hold how far is building of some item.
 */
abstract class ColonyBuildingItemProgress {

    private int buildHammers;

    private int buildTools;

    public abstract String getName();

    public abstract int getRequiredHammers();

    public abstract int getRequiredTools();

    /**
     * @return the buildHammers
     */
    public int getBuildHammers() {
        return buildHammers;
    }

    /**
     * @param buildHammers
     *            the buildHammers to set
     */
    public void setBuildHammers(int buildHammers) {
        this.buildHammers = buildHammers;
    }

    /**
     * @return the buildTools
     */
    public int getBuildTools() {
        return buildTools;
    }

    /**
     * @param buildTools
     *            the buildTools to set
     */
    public void setBuildTools(int buildTools) {
        this.buildTools = buildTools;
    }

}