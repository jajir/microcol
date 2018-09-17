package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class ColonyBuildingItemConstruction extends ColonyBuildingItem {

    private final ConstructionType constructionType;

    public ColonyBuildingItemConstruction(final ConstructionType constructionType) {
        this.constructionType = Preconditions.checkNotNull(constructionType);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("constructionType", constructionType)
                .add("requiredTools", getRequiredTools())
                .add("requiredHammers", getRequiredHammers()).toString();
    }

    @Override
    public String getName() {
        return constructionType.name();
    }

    /**
     * @return the constructionType
     */
    public ConstructionType getConstructionType() {
        return constructionType;
    }

    @Override
    public int getRequiredHammers() {
        return constructionType.getRequiredHammers();
    }

    @Override
    public int getRequiredTools() {
        return constructionType.getRequiredTools();
    }

}
