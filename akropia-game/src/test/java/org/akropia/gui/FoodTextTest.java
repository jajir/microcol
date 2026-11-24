package org.akropia.gui;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.akropia.gui.util.StreamReader;
import org.akropia.i18n.I18n;
import org.akropia.model.*;

class FoodTextTest {
    @Test
    void waterAndRiversProduceFishingFoodWhileOrdinaryLandUsesFarming() {
        for (final TerrainType type : new TerrainType[] { TerrainType.OCEAN,
                TerrainType.LAKE }) {
            final Terrain terrain = new Terrain(Location.of(1, 1), type);
            assertEquals(FoodText.fishing, FoodText.source(terrain));
            assertEquals(3, terrain.canProduceAmmount(GoodsType.GRAIN));
        }
        final Terrain land = new Terrain(Location.of(1, 1),
                TerrainType.GRASSLAND);
        assertEquals(FoodText.farming, FoodText.source(land));
        land.setRiverSize(RiverSize.SMALL);
        assertEquals(FoodText.fishing, FoodText.source(land));
        assertEquals(3, land.canProduceAmmount(GoodsType.GRAIN));
        land.setRiverSize(RiverSize.LARGE);
        assertEquals(FoodText.fishing, FoodText.source(land));
        assertEquals(4, land.canProduceAmmount(GoodsType.GRAIN));
    }

    @Test
    void foodNamesAndSourceHelpWorkInBothLanguages() {
        final I18n en = I18n.builder().setStreamReader(new StreamReader())
                .setDefaultLocale(Locale.ROOT).build();
        final I18n cs = I18n.builder().setStreamReader(new StreamReader())
                .setDefaultLocale(new Locale("cs", "CZ")).build();
        assertEquals("Food", en.get(GoodsTypeName.grain));
        assertEquals("Jídlo", cs.get(GoodsTypeName.grain));
        assertEquals("jídla", cs.get(GoodsTypeName.grain_dc2_pl));
        assertEquals("jídlem", cs.get(GoodsTypeName.grain_dc7_pl));
        assertEquals("Fishing: provides 3 food per turn.",
                en.get(FoodText.fishing, 3));
        assertEquals("Rybolov: jídlo za tah — 3.", cs.get(FoodText.fishing, 3));
        assertEquals("Zemědělství: jídlo za tah — 1.",
                cs.get(FoodText.farming, 1));
    }
}
