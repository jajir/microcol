package org.microcol.model;

import com.google.common.base.MoreObjects;

/**
 * Object represents work done at some construction.
 */
public class ColonyBuildingItemProgressConstruction
        extends ColonyBuildingItemProgress<ColonyBuildingItemConstruction> {

    public ColonyBuildingItemProgressConstruction(final ColonyBuildingItemConstruction item,
            final int id) {
        super(item, id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("id", getId())
                .add("buildHammers", getBuildHammers()).add("visible", isVisible())
                .add("constructionType", getConstructionType()).toString();
    }

    public ConstructionType getConstructionType() {
        return getItem().getConstructionType();
    }

}
