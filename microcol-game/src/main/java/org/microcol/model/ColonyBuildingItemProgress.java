package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Hold how far is building of some item.
 */
public abstract class ColonyBuildingItemProgress<I extends ColonyBuildingItem> {

    private final int id;

    private final I item;

    private int buildHammers;

    private boolean visible;

    ColonyBuildingItemProgress(final I item, final int id) {
        this.item = Preconditions.checkNotNull(item);
        this.id = id;
        visible = true;
    }

    public void addHammers(final int hammers) {
        buildHammers += hammers;
    }

    public boolean isHammersProvided() {
        return buildHammers >= getRequiredHammers();
    }

    public String getName() {
        return item.getName();
    }

    public int getRequiredHammers() {
        return item.getRequiredHammers();
    }

    public int getRequiredTools() {
        return item.getRequiredTools();
    }

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
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * @return the item
     */
    public I getItem() {
        return item;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

}