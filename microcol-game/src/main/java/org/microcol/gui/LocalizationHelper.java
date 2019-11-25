package org.microcol.gui;

import org.microcol.i18n.I18n;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Helps localize messages.
 */
public final class LocalizationHelper {

    /**
     * Localization class.
     */
    private final I18n i18n;

    @Inject
    public LocalizationHelper(final I18n i18n) {
        this.i18n = Preconditions.checkNotNull(i18n);
    }

    public String getTerrainName(final TerrainType terrainType) {
        return i18n.get(TerrainTypes.getTerrainName(terrainType));
    }

    public String getUnitName(final UnitType unitType) {
        return i18n.get(UnitTypes.getUnitName(unitType));
    }

}
