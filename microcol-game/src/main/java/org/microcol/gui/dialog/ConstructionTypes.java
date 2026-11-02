package org.microcol.gui.dialog;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.model.ConstructionType;

/**
 * Define keys for construction type names.
 */
public enum ConstructionTypes implements MessageKeyResource {

    TOWN_HALL_name, LUMBER_MILL_name, CARPENTERS_STAND_name, IRON_WORKS_name,
    BLACKSMITHS_SHOP_name, BLACKSMITHS_HOUSE_name, FORTRESS_name, FORT_name,
    STOCKADE_name, LARGE_WINERY_name, WINERY_name, SMALL_WINERY_name,
    TEXTILE_MILL_name, WEAVERS_SHOP_name, WEAVERS_HOUSE_name,
    LARGE_OIL_PRESS_name, OIL_PRESS_name, SMALL_OIL_PRESS_name,
    LARGE_POTTERY_name, POTTERY_name, SMALL_POTTERY_name, ARSENAL_name,
    MAGAZINE_name, ARMORY_name, SHIPYARD_name, DRYDOCK_name, DOCK_name,
    UNIVERSITY_name, COLLEGE_name, SCHOOLHOUSE_name, WAREHOUSE_EXPANSION_name,
    WAREHOUSE_name, BASIC_WAREHOUSE_name, STABLES_name, CATHEDRAL_name,
    CHURCH_name, NEWSPAPER_name, PRINTING_PRESS_name, CUSTOM_HOUSE_name;

    private static final String CONSTRUCTION_SUFFIX_NAME = "_name";

    public static ConstructionTypes getConstructionName(
            final ConstructionType constructionType) {
        final String key = constructionType.name() + CONSTRUCTION_SUFFIX_NAME;
        return valueOf(key);
    }

}
