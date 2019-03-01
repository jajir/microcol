package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class ConstructionTypeConstantTest {

    @Test
    public void test_soource_levest_are_computed_correctly() throws Exception {
        assertEquals(GoodType.GOOD_TYPES.size(), ConstructionType.SOURCE_1.size()
                + ConstructionType.SOURCE_2.size() + ConstructionType.SOURCE_3.size());

        assertTrue(ConstructionType.SOURCE_1.contains(GoodType.BELL));
        assertTrue(ConstructionType.SOURCE_1.contains(GoodType.CROSS));

        assertTrue(ConstructionType.SOURCE_2.contains(GoodType.TOOLS));
        assertTrue(ConstructionType.SOURCE_2.contains(GoodType.RUM));

        assertTrue(ConstructionType.SOURCE_3.contains(GoodType.MUSKET));
    }

    @Test
    public void test_requiredFrom_isCorrect_FUR_FACTORY() throws Exception {
        final Optional<ConstructionType> oType = ConstructionType.FUR_FACTORY.getUpgradeFrom();
        assertTrue(oType.isPresent());
        assertEquals(ConstructionType.FUR_TRADERS_HOUSE, oType.get());
    }

    @Test
    public void test_requiredFrom_isCorrect_FUR_TRADERS_HOUSE() throws Exception {
        final Optional<ConstructionType> oType = ConstructionType.FUR_TRADERS_HOUSE
                .getUpgradeFrom();
        assertTrue(oType.isPresent());
        assertEquals(ConstructionType.FUR_TRADING_POST, oType.get());
    }

    @Test
    public void test_getUpgradeChain_FUR_TRADING_POST() {
        final List<ConstructionType> list = ConstructionType.FUR_TRADING_POST.getUpgradeChain();
        assertTrue(list.contains(ConstructionType.FUR_TRADING_POST));
        assertTrue(list.contains(ConstructionType.FUR_TRADERS_HOUSE));
        assertTrue(list.contains(ConstructionType.FUR_FACTORY));
        assertEquals(3, list.size());
    }

    @Test
    public void test_getUpgradeChain_FUR_TRADERS_HOUSE() {
        final List<ConstructionType> list = ConstructionType.FUR_TRADERS_HOUSE.getUpgradeChain();
        assertTrue(list.contains(ConstructionType.FUR_TRADERS_HOUSE));
        assertTrue(list.contains(ConstructionType.FUR_FACTORY));
        assertEquals(2, list.size());
    }

}
