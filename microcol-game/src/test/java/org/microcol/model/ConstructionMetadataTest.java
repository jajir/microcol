package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Admission metadata from the original manual Building Chart, PDF page 134. */
class ConstructionMetadataTest {

    // The final three rows preserve current internal scaffolding/extension
    // data.
    @ParameterizedTest(name = "{0}: {1} hammers, {2} tools, population {3}, predecessor {4}")
    @CsvSource({ "TOWN_HALL,0,0,1,-", "CARPENTERS_STAND,0,0,1,-",
            "LUMBER_MILL,52,0,3,CARPENTERS_STAND", "BLACKSMITHS_HOUSE,0,0,1,-",
            "BLACKSMITHS_SHOP,64,20,4,BLACKSMITHS_HOUSE",
            "IRON_WORKS,240,100,8,BLACKSMITHS_SHOP", "STOCKADE,64,0,3,-",
            "FORT,120,100,4,STOCKADE", "FORTRESS,320,200,8,FORT",
            "TOBACCONISTS_HOUSE,0,0,1,-",
            "TOBACCONISTS_SHOP,64,20,4,TOBACCONISTS_HOUSE",
            "CIGAR_FACTORY,160,100,8,TOBACCONISTS_SHOP",
            "WEAVERS_HOUSE,0,0,1,-", "WEAVERS_SHOP,64,20,4,WEAVERS_HOUSE",
            "TEXTILE_MILL,160,100,8,WEAVERS_SHOP",
            "RUM_DISTILLERS_HOUSE,0,0,1,-",
            "RUM_DISTILLERY,64,20,4,RUM_DISTILLERS_HOUSE",
            "RUM_FACTORY,160,100,8,RUM_DISTILLERY", "FUR_TRADERS_HOUSE,0,0,1,-",
            "FUR_TRADING_POST,56,20,3,FUR_TRADERS_HOUSE",
            "FUR_FACTORY,160,100,6,FUR_TRADING_POST", "ARMORY,52,0,1,-",
            "MAGAZINE,120,50,8,ARMORY", "ARSENAL,240,100,8,MAGAZINE",
            "DOCK,52,0,1,-", "DRYDOCK,80,50,6,DOCK",
            "SHIPYARD,240,100,8,DRYDOCK", "SCHOOLHOUSE,64,0,4,-",
            "COLLEGE,160,50,8,SCHOOLHOUSE", "UNIVERSITY,200,100,10,COLLEGE",
            "WAREHOUSE,80,0,1,BASIC_WAREHOUSE",
            "WAREHOUSE_EXPANSION,80,20,1,WAREHOUSE", "STABLES,64,0,1,-",
            "CHURCH,52,0,3,CHAPEL", "CATHEDRAL,176,100,8,CHURCH",
            "PRINTING_PRESS,80,0,1,-", "NEWSPAPER,120,50,4,PRINTING_PRESS",
            "CUSTOM_HOUSE,160,50,0,-", "BASIC_WAREHOUSE,0,0,1,-",
            "CHAPEL,0,0,3,-", "LARGE_STABLES,149,80,1,STABLES" })
    void projectMetadataMatchesItsAgreedRow(final String name,
            final int hammers, final int tools, final int population,
            final String predecessor) {
        final ConstructionType type = ConstructionType.valueOf(name);
        assertEquals(hammers, type.getRequiredHammers());
        assertEquals(tools, type.getRequiredTools());
        assertEquals(population, type.getRequiredColonyPopulation());
        assertEquals(predecessor,
                type.getUpgradeFrom().map(ConstructionType::name).orElse("-"));
        assertEquals(hammers > 0 || tools > 0, type.canBeConstructed());
        assertEquals(hammers == 0 && tools == 0, type.isBuildFromFounding());
    }
}
