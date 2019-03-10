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

        assertTrue(SOURCE_1.contains(GoodsType.BELL));
        assertTrue(SOURCE_1.contains(GoodsType.CROSS));

        assertTrue(SOURCE_2.contains(GoodsType.TOOLS));
        assertTrue(SOURCE_2.contains(GoodsType.RUM));

        assertTrue(SOURCE_3.contains(GoodsType.MUSKET));
    }

    @Test
    public void test_requiredFrom_isCorrect_FUR_FACTORY() throws Exception {
        final Optional<ConstructionType> oType = FUR_FACTORY.getUpgradeFrom();
        assertTrue(oType.isPresent());
        assertEquals(FUR_TRADERS_HOUSE, oType.get());
    }

    @Test
    public void test_requiredFrom_isCorrect_FUR_TRADERS_HOUSE() throws Exception {
        final Optional<ConstructionType> oType = FUR_TRADERS_HOUSE.getUpgradeFrom();
        assertTrue(oType.isPresent());
        assertEquals(FUR_TRADING_POST, oType.get());
    }

    @Test
    public void test_getUpgradeChain_FUR_TRADING_POST() {
        final List<ConstructionType> list = FUR_TRADING_POST.getUpgradeChain();
        assertTrue(list.contains(FUR_TRADING_POST));
        assertTrue(list.contains(FUR_TRADERS_HOUSE));
        assertTrue(list.contains(FUR_FACTORY));
        assertEquals(3, list.size());
    }

    @Test
    public void test_getUpgradeChain_FUR_TRADERS_HOUSE() {
        final List<ConstructionType> list = FUR_TRADERS_HOUSE.getUpgradeChain();
        assertTrue(list.contains(FUR_TRADERS_HOUSE));
        assertTrue(list.contains(FUR_FACTORY));
        assertEquals(2, list.size());
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
    public void test_CARPENTERS_SHOP_production_source_values() throws Exception {
        assertTrue(CARPENTERS_SHOP.getConsumptionPerTurn().isPresent());
        assertTrue(CARPENTERS_SHOP.getProductionPerTurn().isPresent());
        assertEquals(1, CARPENTERS_SHOP.getProductionRatio());

        final Goods consumption = CARPENTERS_SHOP.getConsumptionPerTurn().get();
        assertEquals(GoodsType.LUMBER, consumption.getType());
        assertEquals(6, consumption.getAmount());

        final Goods production = CARPENTERS_SHOP.getProductionPerTurn().get();
        assertEquals(GoodsType.HAMMERS, production.getType());
        assertEquals(6, production.getAmount());
    }

    @Test
    public void test_CHURCH_production_source_values() throws Exception {
        assertFalse(CHURCH.getConsumptionPerTurn().isPresent());
        assertTrue(CHURCH.getProductionPerTurn().isPresent());
        assertEquals(1, CHURCH.getProductionRatio());

        final Goods production = CHURCH.getProductionPerTurn().get();
        assertEquals(GoodsType.CROSS, production.getType());
        assertEquals(3, production.getAmount());
    }

}
