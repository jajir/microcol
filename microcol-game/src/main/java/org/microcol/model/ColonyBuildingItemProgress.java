package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * A project in the colony's construction queue.
 *
 * @param <I> type of item being built
 */
public abstract class ColonyBuildingItemProgress<I extends ColonyBuildingItem> {

    private final int id;

    private final I item;

    private boolean visible;

    ColonyBuildingItemProgress(final I item, final int id) {
        this.item = Preconditions.checkNotNull(item);
        this.id = id;
        visible = true;
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
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visible to set
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
