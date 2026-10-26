package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class RiverProductionTest {
    private Terrain river(final TerrainType type, final RiverSize size) {
        final Terrain terrain = new Terrain(Location.of(1, 1), type);
        terrain.setRiverSize(size);
        return terrain;
    }

    @Test
    void fishingReplacesOrdinaryOutputsAndRoadsDoNotIncreaseIt() {
        for (final RiverSize size : new RiverSize[] { RiverSize.SMALL,
                RiverSize.LARGE }) {
            for (final TerrainType type : TerrainType.TERRAINS) {
                if (!type.isLand())
                    continue;
                final Terrain terrain = river(type, size);
                terrain.setHasRoad(true);
                for (final GoodsType goods : GoodsType.GOOD_TYPES) {
                    final int expected = goods == GoodsType.CORN
                            && type.getBaseProduction(goods)
                                    .map(p -> p.getBase() > 0).orElse(false)
                                            ? (size == RiverSize.SMALL ? 3 : 4)
                                            : 0;
                    assertEquals(expected, terrain.canProduceAmmount(goods),
                            type + "/" + size + "/" + goods);
                }
            }
        }
    }

    @Test
    void depositsAreSmallFiniteAndIndependentOfFishingAndRoads() {
        for (final ResourceOverlay overlay : new ResourceOverlay[] {
                ResourceOverlay.SILVER, ResourceOverlay.ORE }) {
            final Terrain terrain = river(TerrainType.GRASSLAND,
                    RiverSize.LARGE);
            terrain.setResourceOverlay(overlay);
            terrain.setHasRoad(true);
            final GoodsType goods = overlay == ResourceOverlay.SILVER
                    ? GoodsType.SILVER
                    : GoodsType.ORE;
            assertEquals(overlay == ResourceOverlay.SILVER ? 2 : 1,
                    terrain.canProduceAmmount(goods));
            terrain.setMineralReserve(MineralReserve.EMPTY);
            assertEquals(0, terrain.canProduceAmmount(goods));
            assertEquals(4, terrain.canProduceAmmount(GoodsType.CORN));
        }
    }

    @Test
    void forestsPlowingAndOtherResourcesAreRejectedInEitherOrder() {
        final Terrain terrain = river(TerrainType.GRASSLAND, RiverSize.SMALL);
        assertThrows(IllegalArgumentException.class,
                () -> terrain.setHasTrees(true));
        assertThrows(IllegalArgumentException.class,
                () -> terrain.setHasField(true));
        for (final ResourceOverlay overlay : ResourceOverlay.values()) {
            if (overlay == ResourceOverlay.NONE
                    || overlay == ResourceOverlay.ORE
                    || overlay == ResourceOverlay.SILVER)
                continue;
            assertThrows(IllegalArgumentException.class,
                    () -> terrain.setResourceOverlay(overlay));
            if (!overlay.canBeOn(TerrainType.GRASSLAND))
                continue;
            final Terrain plain = river(TerrainType.GRASSLAND, RiverSize.NONE);
            plain.setResourceOverlay(overlay);
            assertThrows(IllegalArgumentException.class,
                    () -> plain.setRiverSize(RiverSize.SMALL));
        }
        final Terrain forest = river(TerrainType.GRASSLAND, RiverSize.NONE);
        forest.setHasTrees(true);
        assertThrows(IllegalArgumentException.class,
                () -> forest.setRiverSize(RiverSize.LARGE));
        final Terrain field = river(TerrainType.GRASSLAND, RiverSize.NONE);
        field.setHasField(true);
        assertThrows(IllegalArgumentException.class,
                () -> field.setRiverSize(RiverSize.SMALL));
        assertTrue(forest.isHasTrees());
        assertTrue(field.isHasField());
        assertEquals(RiverSize.NONE, field.getRiverSize());
    }

    @Test
    void fishermanRatherThanFarmerReceivesRiverFoodExpertise() {
        final Terrain river = river(TerrainType.GRASSLAND, RiverSize.SMALL);
        assertEquals(2, UnitType.EXPERT_FISHERMAN
                .getFieldProductionModifier(GoodsType.CORN, river));
        assertEquals(1, UnitType.EXPERT_FARMER
                .getFieldProductionModifier(GoodsType.CORN, river));
        final Terrain land = river(TerrainType.GRASSLAND, RiverSize.NONE);
        assertEquals(2, UnitType.EXPERT_FARMER
                .getFieldProductionModifier(GoodsType.CORN, land));
        assertEquals(1, UnitType.EXPERT_FISHERMAN
                .getFieldProductionModifier(GoodsType.CORN, land));
    }

    @Test
    void riverOnWaterAndUnknownCodeAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> river(TerrainType.OCEAN, RiverSize.SMALL));
        assertThrows(IllegalArgumentException.class,
                () -> river(TerrainType.LAKE, RiverSize.LARGE));
        assertEquals(RiverSize.SMALL, RiverSize.valueOfCode("s"));
        assertEquals(RiverSize.LARGE, RiverSize.valueOfCode("l"));
        assertThrows(IllegalArgumentException.class,
                () -> RiverSize.valueOfCode("x"));
    }

    @Test
    void nativeNaturalOutputAndTeachingUseRiverFishing() {
        final Terrain terrain = river(TerrainType.GRASSLAND, RiverSize.SMALL);
        assertEquals(3, terrain.getNaturalOutput(GoodsType.CORN));
        assertEquals(0, terrain.getNaturalOutput(GoodsType.TOBACCO));
        assertEquals(UnitType.EXPERT_FISHERMAN, NativeTeachableSkills
                .choose(java.util.List.of(terrain), new java.util.Random(290)));
    }
}
