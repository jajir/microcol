package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ConstructionSlotTest {

    private Model model = mock(Model.class);

    private Unit unit = mock(Unit.class);

    private PlaceConstructionSlot placeConstructionSlot = mock(PlaceConstructionSlot.class);

    private ConstructionType constructionType = mock(ConstructionType.class);

    private Construction construction = mock(Construction.class);

    private ConstructionSlot slot;

    @Test
    void test_getProductionModifier_empty() throws Exception {

        assertEquals(0, slot.getProductionModifier());

    }

    @Test
    void test_getProductionModifier_noProducedType() throws Exception {
        slot.set(placeConstructionSlot);
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.empty());

        assertEquals(0, slot.getProductionModifier());
    }

    @Test
    void test_getProductionModifier_different_expertise_than_produced() throws Exception {
        slot.set(placeConstructionSlot);
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.RUM));
        when(placeConstructionSlot.getUnit()).thenReturn(unit);
        when(unit.getType()).thenReturn(UnitType.MASTER_BLACKSMITH);

        assertEquals(1, slot.getProductionModifier());
    }

    @Test
    void test_getProductionModifier_with_expert() throws Exception {
        slot.set(placeConstructionSlot);
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.TOOLS));
        when(placeConstructionSlot.getUnit()).thenReturn(unit);
        when(unit.getType()).thenReturn(UnitType.MASTER_BLACKSMITH);

        assertEquals(2, slot.getProductionModifier());
    }

    @Test
    void test_getProduction_noConsumption_constriction_doesnt_produce_any_goods() throws Exception {
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.empty());

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.getProduction());

        assertEquals("Construction doesn't produce any goods.", exception.getMessage());
    }

    @Test
    void test_getProduction_noConsumption_constriction_consume_goods() throws Exception {
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.RUM));
        when(constructionType.getConsumed()).thenReturn(Optional.of(GoodsType.SUGAR));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.getProduction());

        assertEquals("Construction consume some goods.", exception.getMessage());
    }

    @Test
    void test_getProduction_noConsumption_noUnit() throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.BELL, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.BELL));
        when(constructionType.getConsumed()).thenReturn(Optional.empty());
        when(constructionType.getProductionPerTurn()).thenReturn(production);

        final ConstructionTurnProduction ret = slot.getProduction();

        assertNotNull(ret);
        assertFalse(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());

        assertEquals(GoodsType.BELL, ret.getProducedGoods().get().getType());
        assertEquals(0, ret.getProducedGoods().get().getAmount());
    }

    @Test
    void test_getProduction_noConsumption_withFreeColonist() throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.BELL, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.BELL));
        when(constructionType.getConsumed()).thenReturn(Optional.empty());
        when(constructionType.getProductionPerTurn()).thenReturn(production);
        mockUnitForProductionModifier(UnitType.COLONIST);

        final ConstructionTurnProduction ret = slot.getProduction();

        assertNotNull(ret);
        assertFalse(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());

        assertEquals(GoodsType.BELL, ret.getProducedGoods().get().getType());
        assertEquals(5, ret.getProducedGoods().get().getAmount());
    }

    @Test
    void test_getProduction_noConsumption_with_oreExpertMiner() throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.ORE, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.ORE));
        when(constructionType.getConsumed()).thenReturn(Optional.empty());
        when(constructionType.getProductionPerTurn()).thenReturn(production);
        mockUnitForProductionModifier(UnitType.EXPERT_ORE_MINER);

        final ConstructionTurnProduction ret = slot.getProduction();

        assertNotNull(ret);
        assertFalse(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(GoodsType.ORE, ret.getProducedGoods().get().getType());
        assertEquals(10, ret.getProducedGoods().get().getAmount());
    }

    @Test
    void test_getProduction_withConsumption_with_noUnit() throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.ORE, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.TOOLS));
        when(constructionType.getProductionPerTurn()).thenReturn(production);
        when(constructionType.getConsumed()).thenReturn(Optional.of(GoodsType.ORE));
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.of(Goods.of(GoodsType.ORE, 5)));

        final ConstructionTurnProduction ret = slot.getProduction(Goods.of(GoodsType.ORE, 78));

        assertNotNull(ret);
        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(GoodsType.ORE, ret.getConsumedGoods().get().getType());
        assertEquals(0, ret.getConsumedGoods().get().getAmount());
        assertEquals(GoodsType.TOOLS, ret.getProducedGoods().get().getType());
        assertEquals(0, ret.getProducedGoods().get().getAmount());
    }

    @Test
    void test_getProduction_withConsumption_with_freeColonist() throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.TOOLS, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.TOOLS));
        when(constructionType.getProductionPerTurn()).thenReturn(production);
        when(constructionType.getConsumed()).thenReturn(Optional.of(GoodsType.ORE));
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.of(Goods.of(GoodsType.ORE, 5)));
        mockUnitForProductionModifier(UnitType.COLONIST);

        final ConstructionTurnProduction ret = slot.getProduction(Goods.of(GoodsType.ORE, 78));

        assertNotNull(ret);
        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(GoodsType.ORE, ret.getConsumedGoods().get().getType());
        assertEquals(5, ret.getConsumedGoods().get().getAmount());
        assertEquals(GoodsType.TOOLS, ret.getProducedGoods().get().getType());
        assertEquals(5, ret.getProducedGoods().get().getAmount());
    }

    @Test
    void test_getProduction_withConsumption_with_freeColonist_notEnoughResources()
            throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.TOOLS, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.TOOLS));
        when(constructionType.getProductionPerTurn()).thenReturn(production);
        when(constructionType.getConsumed()).thenReturn(Optional.of(GoodsType.ORE));
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.of(Goods.of(GoodsType.ORE, 5)));
        when(constructionType.getProductionRatio()).thenReturn(1F);
        mockUnitForProductionModifier(UnitType.COLONIST);

        final ConstructionTurnProduction ret = slot.getProduction(Goods.of(GoodsType.ORE, 2));

        assertNotNull(ret);
        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertTrue(ret.getBlockedGoods().isPresent());
        assertEquals(GoodsType.ORE, ret.getConsumedGoods().get().getType());
        assertEquals(2, ret.getConsumedGoods().get().getAmount());
        assertEquals(GoodsType.TOOLS, ret.getProducedGoods().get().getType());
        assertEquals(2, ret.getProducedGoods().get().getAmount());
        assertEquals(GoodsType.TOOLS, ret.getBlockedGoods().get().getType());
        assertEquals(3, ret.getBlockedGoods().get().getAmount());
    }

    @Test
    void test_getProduction_withConsumption_with_masterBlacksmith() throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.TOOLS, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.TOOLS));
        when(constructionType.getProductionPerTurn()).thenReturn(production);
        when(constructionType.getConsumed()).thenReturn(Optional.of(GoodsType.ORE));
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.of(Goods.of(GoodsType.ORE, 5)));
        mockUnitForProductionModifier(UnitType.MASTER_BLACKSMITH);

        final ConstructionTurnProduction ret = slot.getProduction(Goods.of(GoodsType.ORE, 78));

        assertNotNull(ret);
        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertFalse(ret.getBlockedGoods().isPresent());
        assertEquals(GoodsType.ORE, ret.getConsumedGoods().get().getType());
        assertEquals(5, ret.getConsumedGoods().get().getAmount());
        assertEquals(GoodsType.TOOLS, ret.getProducedGoods().get().getType());
        assertEquals(10, ret.getProducedGoods().get().getAmount());
    }

    @Test
    void test_getProduction_withConsumption_with_masterBlacksmith_notEnoughResources()
            throws Exception {
        final Optional<Goods> production = Optional.of(Goods.of(GoodsType.TOOLS, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProduce()).thenReturn(Optional.of(GoodsType.TOOLS));
        when(constructionType.getProductionPerTurn()).thenReturn(production);
        when(constructionType.getConsumed()).thenReturn(Optional.of(GoodsType.ORE));
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.of(Goods.of(GoodsType.ORE, 5)));
        when(constructionType.getProductionRatio()).thenReturn(1F);
        mockUnitForProductionModifier(UnitType.MASTER_BLACKSMITH);

        final ConstructionTurnProduction ret = slot.getProduction(Goods.of(GoodsType.ORE, 3));

        assertNotNull(ret);
        assertTrue(ret.getConsumedGoods().isPresent());
        assertTrue(ret.getProducedGoods().isPresent());
        assertTrue(ret.getBlockedGoods().isPresent());
        assertEquals(GoodsType.ORE, ret.getConsumedGoods().get().getType());
        assertEquals(3, ret.getConsumedGoods().get().getAmount());
        assertEquals(GoodsType.TOOLS, ret.getProducedGoods().get().getType());
        assertEquals(6, ret.getProducedGoods().get().getAmount());
        assertEquals(GoodsType.TOOLS, ret.getBlockedGoods().get().getType());
        assertEquals(4, ret.getBlockedGoods().get().getAmount());
    }

    private void mockUnitForProductionModifier(final UnitType unitType) {
        slot.set(placeConstructionSlot);
        when(construction.getType()).thenReturn(constructionType);
        when(placeConstructionSlot.getUnit()).thenReturn(unit);
        when(unit.getType()).thenReturn(unitType);
    }

    @BeforeEach
    void beforeEach() {
        slot = new ConstructionSlot(model, construction);
    }

    @AfterEach
    void afterEach() {
        slot = null;
    }

}
