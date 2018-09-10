package org.microcol.model;

import com.google.common.base.Preconditions;

public class ColonyBuildingItemConstruction extends ColonyBuildingItem {

    private final ConstructionType constructionType;

    ColonyBuildingItemConstruction(final ConstructionType constructionType) {
        super(constructionType.getRequiredTools(), constructionType.getRequiredHammers());
        this.constructionType = Preconditions.checkNotNull(constructionType);
    }

    @Override
    public String getName() {
        return constructionType.name();
    }

}
