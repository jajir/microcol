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

    private final Optional<Goods> PRODUCTION = Optional
            .of(Goods.of(GoodsType.TOOLS, 5));

    private final Optional<Goods> CONSUMPTION = Optional
            .of(Goods.of(GoodsType.ORE, 5));

    private Model model = mock(Model.class);

    private Unit unit = mock(Unit.class);

    private PlaceConstructionSlot placeConstructionSlot = mock(
            PlaceConstructionSlot.class);

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
        when(constructionType.getProductionPerTurn())
                .thenReturn(Optional.empty());
        assertEquals(0, slot.getProductionModifier());
    }

    @Test
    void test_getProductionModifier_different_expertise_than_produced()
            throws Exception {
        slot.set(placeConstructionSlot);
        Optional<Goods> altProduction = Optional
                .of(Goods.of(GoodsType.RUM, 10));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProductionPerTurn()).thenReturn(altProduction);
        when(placeConstructionSlot.getUnit()).thenReturn(unit);
        when(unit.getType()).thenReturn(UnitType.MASTER_BLACKSMITH);
        assertEquals(1, slot.getProductionModifier());
    }

    @Test
    void test_getProductionModifier_with_expert() throws Exception {
        slot.set(placeConstructionSlot);
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProductionPerTurn()).thenReturn(PRODUCTION);
        when(placeConstructionSlot.getUnit()).thenReturn(unit);
        when(unit.getType()).thenReturn(UnitType.MASTER_BLACKSMITH);
        assertEquals(2, slot.getProductionModifier());
    }

    @Test
    void test_getProduction_noConsumption_construction_doesnt_produce_any_goods()
            throws Exception {
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProductionPerTurn())
                .thenReturn(Optional.empty());
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> slot.getProduction());
        assertEquals("Construction doesn't produce any goods.",
                exception.getMessage());
    }

    @Test
    void test_getProduction_noConsumption_construction_consume_goods()
            throws Exception {
        final Optional<Goods> altProduction = Optional
                .of(Goods.of(GoodsType.RUM, 0));
        final Optional<Goods> altConsumption = Optional
                .of(Goods.of(GoodsType.SUGAR, 0));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProductionPerTurn()).thenReturn(altProduction);
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(altConsumption);
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> slot.getProduction());
        assertEquals("Construction consume some goods.",
                exception.getMessage());
    }

    @Test
    void test_getProduction_noConsumption_noUnit() throws Exception {
        final Optional<Goods> production = Optional
                .of(Goods.of(GoodsType.BELL, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.empty());
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
        final Optional<Goods> production = Optional
                .of(Goods.of(GoodsType.BELL, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.empty());
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
    void test_getProduction_noConsumption_with_oreExpertMiner()
            throws Exception {
        final Optional<Goods> production = Optional
                .of(Goods.of(GoodsType.ORE, 5));
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.empty());
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
        configureSmithy(5);
        final ConstructionTurnProduction ret = slot
                .getProduction(Goods.of(GoodsType.ORE, 78));
        assertNotNull(ret);
        assertEquals(Goods.of(GoodsType.ORE, 0),
                ret.getConsumedGoods().orElseThrow());
        assertEquals(Goods.of(GoodsType.TOOLS, 0),
                ret.getProducedGoods().orElseThrow());
        assertFalse(ret.getBlockedGoods().isPresent());
    }

    @Test
    void test_getProduction_withConsumption_with_freeColonist()
            throws Exception {
        configureSmithy(5);
        mockUnitForProductionModifier(UnitType.COLONIST);
        assertProduction(78, 5, 5, 0);
    }

    @Test
    void test_getProduction_withConsumption_with_freeColonist_notEnoughResources()
            throws Exception {
        configureSmithy(5);
        mockUnitForProductionModifier(UnitType.COLONIST);
        assertProduction(2, 2, 2, 3);
    }

    @Test
    void test_getProduction_withConsumption_with_masterBlacksmith()
            throws Exception {
        configureSmithy(5);
        mockUnitForProductionModifier(UnitType.MASTER_BLACKSMITH);
        assertProduction(78, 10, 10, 0);
    }

    @Test
    void test_getProduction_withConsumption_with_masterBlacksmith_notEnoughResources()
            throws Exception {
        configureSmithy(5);
        mockUnitForProductionModifier(UnitType.MASTER_BLACKSMITH);
        assertProduction(3, 3, 3, 7);
    }

    @Test
    void ordinarySmithHouseUsesNoMoreThanThreeOre() {
        configureSmithy(3);
        mockUnitForProductionModifier(UnitType.COLONIST);
        assertEquals(Goods.of(GoodsType.ORE, 3), slot.getPotentialInput(unit));
        assertProduction(0, 0, 0, 3);
        assertProduction(1, 1, 1, 2);
        assertProduction(2, 2, 2, 1);
        assertProduction(3, 3, 3, 0);
        assertProduction(6, 3, 3, 0);
    }

    @Test
    void masterBlacksmithPaysForEveryToolAtSmithHouse() {
        configureSmithy(3);
        mockUnitForProductionModifier(UnitType.MASTER_BLACKSMITH);
        assertEquals(Goods.of(GoodsType.ORE, 6), slot.getPotentialInput(unit));
        assertProduction(0, 0, 0, 6);
        assertProduction(3, 3, 3, 3);
        assertProduction(6, 6, 6, 0);
        assertProduction(9, 6, 6, 0);
    }

    @Test
    void wrongSourceTypeIsRejectedForOccupiedAndEmptySlots() {
        configureSmithy(3);
        assertThrows(IllegalArgumentException.class,
                () -> slot.getProduction(Goods.of(GoodsType.SUGAR, 3)));
        mockUnitForProductionModifier(UnitType.COLONIST);
        assertThrows(IllegalArgumentException.class,
                () -> slot.getProduction(Goods.of(GoodsType.SUGAR, 3)));
    }

    private void configureSmithy(final int perTurn) {
        when(construction.getType()).thenReturn(constructionType);
        when(constructionType.getProductionPerTurn())
                .thenReturn(Optional.of(Goods.of(GoodsType.TOOLS, perTurn)));
        when(constructionType.getConsumptionPerTurn())
                .thenReturn(Optional.of(Goods.of(GoodsType.ORE, perTurn)));
        when(constructionType.getProductionRatio()).thenReturn(1F);
    }

    private void assertProduction(final int available, final int consumed,
            final int produced, final int blocked) {
        final ConstructionTurnProduction result = slot
                .getProduction(Goods.of(GoodsType.ORE, available));
        assertEquals(Goods.of(GoodsType.ORE, consumed),
                result.getConsumedGoods().orElseThrow());
        assertEquals(Goods.of(GoodsType.TOOLS, produced),
                result.getProducedGoods().orElseThrow());
        if (blocked == 0) {
            assertFalse(result.getBlockedGoods().isPresent());
        } else {
            assertEquals(Goods.of(GoodsType.TOOLS, blocked),
                    result.getBlockedGoods().orElseThrow());
        }
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
        final Colony colony = mock(Colony.class);
        when(construction.getColony()).thenReturn(colony);
        when(colony.getPoliticalProduction())
                .thenReturn(PoliticalProduction.NONE);
        when(colony.getPoliticalProductionFor(unit))
                .thenReturn(PoliticalProduction.NONE);
    }

    @AfterEach
    void afterEach() {
        slot = null;
    }

}
