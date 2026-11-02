package org.akropia.gui.screen.statistics;

import org.akropia.gui.GoodsTypeName;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.screen.colony.ConstructionTypeName;
import org.akropia.gui.screen.game.components.StatusBarMessageService;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.LaborLocation;
import org.akropia.model.Location;
import org.akropia.model.NativeSettlement;

/**
 * Localized description of where a labor entry stands (R-0198). The model
 * supplies every fact; this class only names them.
 */
final class LaborLocationText {

    private LaborLocationText() {
    }

    /**
     * @param i18n     required translator
     * @param location required place of a person
     * @return the place, with the carrier's type and stable ID first when the
     *         person is aboard
     */
    static String describe(final I18n i18n, final LaborLocation location) {
        final String place = place(i18n, location);
        if (location.getCarrierType().isEmpty()) {
            return place;
        }
        return i18n.get(LaborText.aboard,
                i18n.get(
                        UnitTypes.getUnitName(location.getCarrierType().get())),
                location.getCarrierId().orElseThrow(), place);
    }

    private static String place(final I18n i18n, final LaborLocation location) {
        switch (location.getKind()) {
        case COLONY:
            return colony(i18n, location);
        case MAP:
            final Location tile = location.getLocation().orElseThrow();
            return i18n.get(LaborText.map, tile.getX(), tile.getY());
        case EUROPE:
            return i18n.get(LaborText.europe);
        case AT_SEA:
            return i18n.get(LaborText.atSea);
        case NATIVE_COURSE:
            return nativeSite(i18n, location, LaborText.nativeCourse,
                    LaborText.nativeCourseUnknown);
        case NATIVE_MISSION:
            return nativeSite(i18n, location, LaborText.nativeMission,
                    LaborText.nativeMissionUnknown);
        case REPAIR:
            return i18n.get(LaborText.repair);
        default:
            return i18n.get(LaborText.other);
        }
    }

    private static String colony(final I18n i18n,
            final LaborLocation location) {
        final String name = location.getColony().map(Colony::getName)
                .orElse("");
        if (location.getFieldGoods().isPresent()) {
            return i18n.get(LaborText.colonyField, name, i18n.get(GoodsTypeName
                    .getNameForGoodsType(location.getFieldGoods().get())));
        }
        if (location.getConstruction().isPresent()) {
            return i18n.get(LaborText.colonyBuilding, name,
                    i18n.get(ConstructionTypeName
                            .getNameForType(location.getConstruction().get())));
        }
        return i18n.get(LaborText.colony, name);
    }

    /**
     * A learned settlement is named by its nation and tile; an unlearned one
     * shows only that the course or mission is at a native settlement.
     */
    private static String nativeSite(final I18n i18n,
            final LaborLocation location, final LaborText known,
            final LaborText unknown) {
        final NativeSettlement site = location.getNativeSettlement()
                .orElse(null);
        if (site == null) {
            return i18n.get(unknown);
        }
        final Location tile = site.getLocation();
        return i18n.get(known,
                i18n.get(StatusBarMessageService.nationLabel(site.getNation())),
                tile.getX(), tile.getY());
    }
}
