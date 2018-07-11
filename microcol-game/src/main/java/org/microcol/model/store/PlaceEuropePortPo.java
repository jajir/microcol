package org.microcol.model.store;

import com.google.common.base.MoreObjects;

public final class PlaceEuropePortPo extends PlacePo {

    private boolean isOnPier = false;

    public boolean isOnPier() {
        return isOnPier;
    }

    public void setOnPier(boolean isOnPier) {
        this.isOnPier = isOnPier;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("isOnPier", isOnPier).toString();
    }

}
