package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class TerrainTypeTest {

    private final static int NUMBER_OF_TERRAINS = 14;

    /**
     * It just print out production matrix
     *
     * @throws Exception Could throws any exception
     */
    @Test
    public void test_production_matrix() throws Exception {
        assertEquals(NUMBER_OF_TERRAINS, TerrainType.TERRAINS.size());
    }

    @Test
    public void test_duplicated_terrainType_code() throws Exception {
        Set<String> codes = TerrainType.TERRAINS.stream()
                .map(terrainType -> terrainType.getCode())
                .collect(Collectors.toSet());

        assertEquals(NUMBER_OF_TERRAINS, codes.size());
    }

    /**
     * Every legal bare/forested terrain cell of the aid-card defense column.
     */
    @ParameterizedTest
    @CsvSource({ "GRASSLAND,true,0,50", "PRAIRIE,true,0,50",
            "SAVANNAH,true,0,50", "DESERT,true,0,50", "PLAINS,true,0,50",
            "MARSH,true,25,50", "SWAMP,true,25,75", "TUNDRA,true,0,50",
            "ARCTIC,false,0,0", "HILL,false,100,100", "MOUNTAIN,false,150,150",
            "OCEAN,false,0,0", "LAKE,false,0,0", "HIGH_SEA,false,0,0" })
    public void every_terrain_has_documented_forest_and_defense_cells(
            final String name, final boolean canHaveTree, final int bare,
            final int forested) {
        final TerrainType type = TerrainType.valueOf(name);
        assertEquals(canHaveTree, type.isCanHaveTree());
        assertEquals(bare, type.getDefenseBonus());
        assertEquals(forested, type.getDefenseBonusWithTree());
    }

    @Test
    public void every_terrain_round_trips_through_name_and_code() {
        for (final TerrainType type : TerrainType.TERRAINS) {
            assertSame(type, TerrainType.valueOf(type.name()));
            assertSame(type, TerrainType.valueOfCode(type.getCode()));
        }
    }

    @Test
    public void unknown_name_and_code_are_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> TerrainType.valueOf("VOLCANO"));
        assertThrows(IllegalArgumentException.class,
                () -> TerrainType.valueOfCode("?"));
    }

    @Test
    public void lake_is_distinct_water_with_ordinary_ocean_rules() {
        assertNotEquals(TerrainType.OCEAN, TerrainType.LAKE);
        assertNotEquals(TerrainType.HIGH_SEA, TerrainType.LAKE);
        assertTrue(TerrainType.LAKE.isSea());
        assertTrue(TerrainType.LAKE.isOrdinaryWater());
        assertTrue(TerrainType.OCEAN.isOrdinaryWater());
        assertFalse(TerrainType.HIGH_SEA.isOrdinaryWater());
        assertTrue(TerrainType.UNIT_CAN_SAIL_AT.contains(TerrainType.LAKE));
        assertFalse(TerrainType.UNIT_CAN_WALK_AT.contains(TerrainType.LAKE));
        assertEquals(TerrainType.OCEAN.getMoveCost(),
                TerrainType.LAKE.getMoveCost());
        assertEquals(3, TerrainType.LAKE.getBaseProduction(GoodsType.CORN)
                .orElseThrow().getBase());
        assertEquals(
                TerrainType.OCEAN.getBaseProduction(GoodsType.CORN)
                        .orElseThrow().getBase(),
                TerrainType.LAKE.getBaseProduction(GoodsType.CORN).orElseThrow()
                        .getBase());
    }

    @Test
    public void forest_is_legal_only_where_terrain_allows_it() {
        for (final TerrainType type : TerrainType.TERRAINS) {
            final Terrain terrain = new Terrain(Location.of(1, 1), type);
            if (type.isCanHaveTree()) {
                terrain.setHasTrees(true);
                assertTrue(terrain.isHasTrees());
            } else {
                assertThrows(IllegalArgumentException.class,
                        () -> terrain.setHasTrees(true), type.name());
                assertFalse(terrain.isHasTrees());
            }
        }
    }

}
