package org.microcol.gui.screen.colony;

import java.util.Arrays;
import java.util.ResourceBundle;

import org.microcol.i18n.MessageKeyResource;
import org.microcol.i18n.ResourceBundleControlBuilder;
import org.microcol.i18n.ResourceBundleFormat;
import org.microcol.model.ConstructionType;

import com.google.common.base.Preconditions;

/**
 * Map {@link ConstructionType} to enumeration value,
 */
enum ConstructionTypeName implements MessageKeyResource {

    townHall(ConstructionType.TOWN_HALL),
    lubmerMill(ConstructionType.LUMBER_MILL),
    carpenterShop(ConstructionType.CARPENTERS_SHOP),
    carpenterStand(ConstructionType.CARPENTERS_STAND),
    ironWorks(ConstructionType.IRON_WORKS),
    blacksmithShop(ConstructionType.BLACKSMITHS_SHOP),
    blacksmithHouse(ConstructionType.BLACKSMITHS_HOUSE),
    fortress(ConstructionType.FORTRESS),
    fort(ConstructionType.FORT),
    stockade(ConstructionType.STOCKADE),
    cigarFactory(ConstructionType.CIGAR_FACTORY),
    tobacconistShop(ConstructionType.TOBACCONISTS_SHOP),
    tobacconistHouse(ConstructionType.TOBACCONISTS_HOUSE),
    textilMill(ConstructionType.TEXTILE_MILL),
    weaversShop(ConstructionType.WEAVERS_SHOP),
    weaversHouse(ConstructionType.WEAVERS_HOUSE),
    rumFactory(ConstructionType.RUM_FACTORY),
    rumDistillery(ConstructionType.RUM_DISTILLERY),
    rumDistillersHouse(ConstructionType.RUM_DISTILLERS_HOUSE),
    furFactory(ConstructionType.FUR_FACTORY),
    furTradersHouse(ConstructionType.FUR_TRADERS_HOUSE),
    furTradingPost(ConstructionType.FUR_TRADING_POST),
    arsenal(ConstructionType.ARSENAL),
    magazine(ConstructionType.MAGAZINE),
    armory(ConstructionType.ARMORY),
    shipyard(ConstructionType.SHIPYARD),
    drydock(ConstructionType.DRYDOCK),
    dock(ConstructionType.DOCK),
    university(ConstructionType.UNIVERSITY),
    college(ConstructionType.COLLEGE),
    schoolhouse(ConstructionType.SCHOOLHOUSE),
    warehouseExpansion(ConstructionType.WAREHOUSE_EXPANSION),
    warehouse(ConstructionType.WAREHOUSE),
    warehouseBasic(ConstructionType.WAREHOUSE_BASIC),
    largeStables(ConstructionType.LARGE_STABLES),
    stables(ConstructionType.STABLES),
    cathedral(ConstructionType.CATHEDRAL),
    church(ConstructionType.CHURCH),
    chapel(ConstructionType.CHAPEL),
    newspaper(ConstructionType.NEWSPAPER),
    printingPress(ConstructionType.PRINTING_PRESS),
    customHouse(ConstructionType.CUSTOM_HOUSE);

    private final ConstructionType goodsType;

    ConstructionTypeName(final ConstructionType goodsType) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
    }

    public static ConstructionTypeName getNameForType(final ConstructionType goodsType) {
        return Arrays.stream(values()).filter(gtName -> gtName.goodsType == goodsType).findFirst()
                .orElseThrow((() -> new IllegalArgumentException()));
    }

    @Override
    public ResourceBundle.Control getResourceBundleControl() {
        return new ResourceBundleControlBuilder().setPredefinedFormat(ResourceBundleFormat.xml)
                .build();
    }

}
