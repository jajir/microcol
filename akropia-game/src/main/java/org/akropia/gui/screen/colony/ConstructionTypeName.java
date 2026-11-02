package org.akropia.gui.screen.colony;

import java.util.Arrays;

import org.akropia.i18n.MessageKeyResource;
import org.akropia.model.ConstructionType;

import com.google.common.base.Preconditions;

/**
 * Map {@link ConstructionType} to enumeration value,
 */
public enum ConstructionTypeName implements MessageKeyResource {

    townHall(ConstructionType.TOWN_HALL),
    lubmerMill(ConstructionType.LUMBER_MILL),
    carpenterStand(ConstructionType.CARPENTERS_STAND),
    ironWorks(ConstructionType.IRON_WORKS),
    blacksmithShop(ConstructionType.BLACKSMITHS_SHOP),
    blacksmithHouse(ConstructionType.BLACKSMITHS_HOUSE),
    fortress(ConstructionType.FORTRESS), fort(ConstructionType.FORT),
    stockade(ConstructionType.STOCKADE),
    largeWinery(ConstructionType.LARGE_WINERY), winery(ConstructionType.WINERY),
    smallWinery(ConstructionType.SMALL_WINERY),
    textilMill(ConstructionType.TEXTILE_MILL),
    weaversShop(ConstructionType.WEAVERS_SHOP),
    weaversHouse(ConstructionType.WEAVERS_HOUSE),
    largeOilPress(ConstructionType.LARGE_OIL_PRESS),
    oilPress(ConstructionType.OIL_PRESS),
    smallOilPress(ConstructionType.SMALL_OIL_PRESS),
    largePottery(ConstructionType.LARGE_POTTERY),
    smallPottery(ConstructionType.SMALL_POTTERY),
    pottery(ConstructionType.POTTERY), arsenal(ConstructionType.ARSENAL),
    magazine(ConstructionType.MAGAZINE), armory(ConstructionType.ARMORY),
    shipyard(ConstructionType.SHIPYARD), drydock(ConstructionType.DRYDOCK),
    dock(ConstructionType.DOCK), university(ConstructionType.UNIVERSITY),
    college(ConstructionType.COLLEGE),
    schoolhouse(ConstructionType.SCHOOLHOUSE),
    warehouseExpansion(ConstructionType.WAREHOUSE_EXPANSION),
    warehouse(ConstructionType.WAREHOUSE),
    warehouseBasic(ConstructionType.WAREHOUSE_BASIC),
    largeStables(ConstructionType.LARGE_STABLES),
    stables(ConstructionType.STABLES), cathedral(ConstructionType.CATHEDRAL),
    church(ConstructionType.CHURCH), chapel(ConstructionType.CHAPEL),
    newspaper(ConstructionType.NEWSPAPER),
    printingPress(ConstructionType.PRINTING_PRESS),
    customHouse(ConstructionType.CUSTOM_HOUSE);

    private final ConstructionType goodsType;

    ConstructionTypeName(final ConstructionType goodsType) {
        this.goodsType = Preconditions.checkNotNull(goodsType);
    }

    public static ConstructionTypeName getNameForType(
            final ConstructionType goodsType) {
        return Arrays.stream(values())
                .filter(gtName -> gtName.goodsType == goodsType).findFirst()
                .orElseThrow((() -> new IllegalArgumentException()));
    }

}
