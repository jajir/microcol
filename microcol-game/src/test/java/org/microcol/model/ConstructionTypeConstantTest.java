package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.ConstructionType.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ConstructionTypeConstantTest {

    @Test
    public void test_soource_levest_are_computed_correctly() throws Exception {
        assertEquals(GoodsType.GOOD_TYPES.size(),
                SOURCE_1.size() + SOURCE_2.size() + SOURCE_3.size());

        assertTrue(SOURCE_1.contains(GoodsType.OWLS));
        assertTrue(SOURCE_1.contains(GoodsType.TORCHES));

        assertTrue(SOURCE_2.contains(GoodsType.TOOLS));
        assertTrue(SOURCE_2.contains(GoodsType.OLIVE_OIL));

        assertTrue(SOURCE_3.contains(GoodsType.WEAPONS));
    }

    @Test
    public void test_requiredFrom_isCorrect_LARGE_POTTERY() throws Exception {
        final Optional<ConstructionType> oType = LARGE_POTTERY.getUpgradeFrom();
        assertTrue(oType.isPresent());
        assertEquals(POTTERY, oType.get());
    }

    @Test
    public void test_requiredFrom_isCorrect_SMALL_POTTERY() throws Exception {
        final Optional<ConstructionType> oType = SMALL_POTTERY.getUpgradeFrom();
        assertFalse(oType.isPresent());
    }

    @Test
    public void test_getUpgradeChain_POTTERY() {
        final List<ConstructionType> list = POTTERY.getUpgradeChain();
        assertTrue(list.contains(POTTERY));
        assertFalse(list.contains(SMALL_POTTERY));
        assertTrue(list.contains(LARGE_POTTERY));
        assertEquals(2, list.size());
    }

    @Test
    public void test_getUpgradeChain_SMALL_POTTERY() {
        final List<ConstructionType> list = SMALL_POTTERY.getUpgradeChain();
        assertTrue(list.contains(SMALL_POTTERY));
        assertTrue(list.contains(POTTERY));
        assertTrue(list.contains(LARGE_POTTERY));
        assertEquals(3, list.size());
    }

    @Test
    public void test_IRON_WORKS_production_source_values() throws Exception {
        assertTrue(IRON_WORKS.getConsumptionPerTurn().isPresent());
        assertTrue(IRON_WORKS.getProductionPerTurn().isPresent());
        assertEquals(3F / 2, IRON_WORKS.getProductionRatio());

        final Goods consumption = IRON_WORKS.getConsumptionPerTurn().get();
        assertEquals(GoodsType.ORE, consumption.getType());
        assertEquals(6, consumption.getAmount());

        final Goods production = IRON_WORKS.getProductionPerTurn().get();
        assertEquals(GoodsType.TOOLS, production.getType());
        assertEquals(9, production.getAmount());
    }

    @Test
    public void test_LUMBER_MILL_production_source_values() throws Exception {
        assertTrue(LUMBER_MILL.getConsumptionPerTurn().isPresent());
        assertTrue(LUMBER_MILL.getProductionPerTurn().isPresent());
        assertEquals(1, LUMBER_MILL.getProductionRatio());

        final Goods consumption = LUMBER_MILL.getConsumptionPerTurn().get();
        assertEquals(GoodsType.LUMBER, consumption.getType());
        assertEquals(6, consumption.getAmount());

        final Goods production = LUMBER_MILL.getProductionPerTurn().get();
        assertEquals(GoodsType.HAMMERS, production.getType());
        assertEquals(6, production.getAmount());
    }

    @Test
    public void test_CHURCH_production_source_values() throws Exception {
        assertFalse(CHURCH.getConsumptionPerTurn().isPresent());
        assertTrue(CHURCH.getProductionPerTurn().isPresent());
        assertEquals(1, CHURCH.getProductionRatio());

        final Goods production = CHURCH.getProductionPerTurn().get();
        assertEquals(GoodsType.TORCHES, production.getType());
        assertEquals(3, production.getAmount());
    }

}
