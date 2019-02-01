package org.microcol.gui;

import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.ConstructionType;

/**
 * Define keys for construction type names.
 */
public enum ConstructionTypes implements MessageKeyResource {

    TOWN_HALL_name,
    LUMBER_MILL_name,
    CARPENTERS_SHOP_name,
    CARPENTERS_STAND_name,
    IRON_WORKS_name,
    BLACKSMITHS_SHOP_name,
    BLACKSMITHS_HOUSE_name,
    FORTRESS_name,
    FORT_name,
    STOCKADE_name,
    CIGAR_FACTORY_name,
    TOBACCONISTS_SHOP_name,
    TOBACCONISTS_HOUSE_name,
    TEXTILE_MILL_name,
    WEAVERS_SHOP_name,
    WEAVERS_HOUSE_name,
    RUM_FACTORY_name,
    RUM_DISTILLERY_name,
    RUM_DISTILLERS_HOUSE_name,
    FUR_FACTORY_name,
    FUR_TRADING_POST_name,
    FUR_TRADERS_HOUSE_name,
    ARSENAL_name,
    MAGAZINE_name,
    ARMORY_name,
    SHIPYARD_name,
    DRYDOCK_name,
    DOCK_name,
    UNIVERSITY_name,
    COLLEGE_name,
    SCHOOLHOUSE_name,
    WAREHOUSE_EXPANSION_name,
    WAREHOUSE_name,
    BASIC_WAREHOUSE_name,
    STABLES_name,
    CATHEDRAL_name,
    CHURCH_name,
    NEWSPAPER_name,
    PRINTING_PRESS_name,
    CUSTOM_HOUSE_name
    ;

    private static final String CONSTRUCTION_SUFFIX_NAME = "_name";

    public static ConstructionTypes getConstructionName(final ConstructionType constructionType) {
        final String key = constructionType.name() + CONSTRUCTION_SUFFIX_NAME;
        return valueOf(key);
    }

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }
}
