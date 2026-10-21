package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.NativeArmsSalePo;
import org.microcol.model.store.NativeLocalAlarmPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.NativeTribalAngerPo;

/**
 * Completed native-relation outcomes: exact deltas, capital and factor rules,
 * arms-sale remainders, clamps, pair isolation, refused input without partial
 * change, once-only delivery and Save/Continue persistence.
 */
class NativeRelationOutcomeTest {

    private static final EuropeanNation FRANCE = EuropeanNation.FRENCH;

    private static final EuropeanNation DUTCH = EuropeanNation.DUTCH;

    private static NativeSettlementPo po(final int id,
            final NativeNation nation, final boolean capital, final int x,
            final int y) {
        final NativeSettlementPo po = new NativeSettlementPo();
        po.setId(id);
        po.setNation(nation);
        po.setSettlementClass(nation.getSettlementClass());
        po.setCapital(capital);
        po.setTreasury(capital ? 2000 : 1000);
        po.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        po.setOfferedGoods(List.of());
        po.setWantedGoods(List.of());
        po.setTrade(List.of());
        po.setLocation(Location.of(x, y));
        return po;
    }

    /**
     * Arawak capital (index 0), an ordinary Arawak village (index 1) and the
     * Tupi capital (index 2).
     */
    private static List<NativeSettlementPo> savedSettlements() {
        return List.of(po(0, NativeNation.ARAWAK, true, 3, 3),
                po(1, NativeNation.ARAWAK, false, 6, 3),
                po(2, NativeNation.TUPI, true, 9, 9));
    }

    private static NativeSettlements registry() {
        return NativeSettlements.make(savedSettlements(), location -> true,
                Set.of());
    }

    private static NativeRelations fresh(final NativeSettlements registry) {
        return NativeRelations.make(registry,
                NativeRelations.freshTribalAnger(),
                NativeRelations.freshLocalAlarm(savedSettlements()), List.of(),
                List.of());
    }

    private static NativeRelations reload(final NativeRelations relations,
            final NativeSettlements registry) {
        return NativeRelations.make(registry, relations.saveTribalAnger(),
                relations.saveLocalAlarm(), relations.saveArmsSales(),
                relations.saveAppliedOutcomes());
    }

    private static NativeArmsSalePo arms(final NativeNation nation,
            final EuropeanNation european, final int quantity) {
        final NativeArmsSalePo po = new NativeArmsSalePo();
        po.setNation(nation);
        po.setEuropean(european);
        po.setQuantity(quantity);
        return po;
    }

    private static int remainder(final NativeRelations relations,
            final EuropeanNation european, final NativeNation nation) {
        return relations.saveArmsSales().stream().filter(
                po -> po.getEuropean() == european && po.getNation() == nation)
                .mapToInt(NativeArmsSalePo::getQuantity).sum();
    }

    /** Every saved value, so a refused outcome can be compared exactly. */
    private static List<Object> snapshot(final NativeRelations relations) {
        final List<Object> out = new ArrayList<>();
        relations.saveTribalAnger().forEach(po -> out.add(po.getValue()));
        relations.saveLocalAlarm().forEach(po -> out.add(po.getValue()));
        relations.saveArmsSales().forEach(po -> out.add(po.getQuantity()));
        out.addAll(relations.saveAppliedOutcomes());
        return out;
    }

    @Test
    void unitAttackAddsTenToItsNationAndHomeAlarmOnly() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.unitAttack(1,
                NativeNation.ARAWAK, village), 1);
        assertEquals(10, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(10, relations.getLocalAlarm(village, FRANCE), 1e-9);
        assertEquals(0,
                relations.getLocalAlarm(registry.getAll().get(0), FRANCE),
                1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.TUPI, FRANCE),
                1e-9);
    }

    @Test
    void homelessBraveChangesTribalAngerOnly() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.unitAttack(2, NativeNation.TUPI, null),
                1);
        assertEquals(10, relations.getTribalAnger(NativeNation.TUPI, FRANCE),
                1e-9);
        assertEquals(0,
                relations.getLocalAlarm(registry.getAll().get(2), FRANCE),
                1e-9);
    }

    @Test
    void settlementAttackDoublesItsIncrementsOnlyAtCapital() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        final NativeSettlement capital = registry.getAll().get(0);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.settlementAttack(3, village, false), 1);
        assertEquals(20, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(20, relations.getLocalAlarm(village, FRANCE), 1e-9);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.settlementAttack(4, capital, false), 1);
        assertEquals(60, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(40, relations.getLocalAlarm(capital, FRANCE), 1e-9);
    }

    @Test
    void destructionAddsTwentyMoreAndDoublesAtCapital() {
        final NativeSettlements registry = registry();
        final NativeSettlement village = registry.getAll().get(1);
        final NativeRelations ordinary = fresh(registry);
        ordinary.applyOutcome(FRANCE,
                NativeRelationOutcome.settlementAttack(5, village, true), 1);
        assertEquals(40, ordinary.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(40, ordinary.getLocalAlarm(village, FRANCE), 1e-9);
        final NativeRelations capital = fresh(registry);
        capital.applyOutcome(FRANCE, NativeRelationOutcome.settlementAttack(6,
                registry.getAll().get(0), true), 1);
        assertEquals(80, capital.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(80,
                capital.getLocalAlarm(registry.getAll().get(0), FRANCE), 1e-9);
    }

    @Test
    void burialAddsThirtyToTheResolvedNationOnly() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.burial(7, NativeNation.TUPI), 1);
        assertEquals(30, relations.getTribalAnger(NativeNation.TUPI, FRANCE),
                1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(0,
                relations.getLocalAlarm(registry.getAll().get(1), FRANCE),
                1e-9);
    }

    @Test
    void tributeAddsTenToNationAndSettlement() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.tribute(8, village), 1);
        assertEquals(10, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(10, relations.getLocalAlarm(village, FRANCE), 1e-9);
    }

    @Test
    void tradeReducesOnlyLocalAlarmByTwoAtOrdinarySettlement() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.addTribalAnger(NativeNation.ARAWAK, FRANCE, 10);
        relations.addLocalAlarm(village, FRANCE, 10, 1);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.trade(9, village),
                1);
        assertEquals(8, relations.getLocalAlarm(village, FRANCE), 1e-9);
        assertEquals(10, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
    }

    @Test
    void tradeAtCapitalDoublesReductionAndAlsoLowersTribalAnger() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement capital = registry.getAll().get(0);
        relations.addTribalAnger(NativeNation.ARAWAK, FRANCE, 10);
        relations.addLocalAlarm(capital, FRANCE, 10, 1);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.trade(10, capital),
                1);
        assertEquals(6, relations.getLocalAlarm(capital, FRANCE), 1e-9);
        assertEquals(6, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
    }

    @Test
    void giftReductionIsFiveTenthsPerHundredGoodsAndKeepsFractions() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.addLocalAlarm(village, FRANCE, 10, 1);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.gift(11, village, 1), 1);
        assertEquals(9.95, relations.getLocalAlarm(village, FRANCE), 1e-9);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.gift(12, village, 50), 1);
        assertEquals(7.45, relations.getLocalAlarm(village, FRANCE), 1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
    }

    @Test
    void giftAtCapitalDoublesReductionAndAlsoLowersTribalAnger() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement capital = registry.getAll().get(0);
        relations.addTribalAnger(NativeNation.ARAWAK, FRANCE, 10);
        relations.addLocalAlarm(capital, FRANCE, 10, 1);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.gift(13, capital, 50), 1);
        assertEquals(5, relations.getLocalAlarm(capital, FRANCE), 1e-9);
        assertEquals(5, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
    }

    @Test
    void reductionsClampAtZero() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.addLocalAlarm(village, FRANCE, 1, 1);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.trade(14, village),
                1);
        assertEquals(0, relations.getLocalAlarm(village, FRANCE), 1e-9);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.gift(15, village, 250), 1);
        assertEquals(0, relations.getLocalAlarm(village, FRANCE), 1e-9);
    }

    @Test
    void armsSalesAddFiveAtEachCompleteFiftyAndKeepTheRemainder() {
        final NativeRelations relations = fresh(registry());
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(16, NativeNation.ARAWAK, 49), 1);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(49, remainder(relations, FRANCE, NativeNation.ARAWAK));
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(17, NativeNation.ARAWAK, 1), 1);
        assertEquals(5, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(0, remainder(relations, FRANCE, NativeNation.ARAWAK));
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(18, NativeNation.ARAWAK, 51), 1);
        assertEquals(10, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(1, remainder(relations, FRANCE, NativeNation.ARAWAK));
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(19, NativeNation.ARAWAK, 49), 1);
        assertEquals(15, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(0, remainder(relations, FRANCE, NativeNation.ARAWAK));
    }

    @Test
    void armsRemaindersAreKeptPerEuropeanAndNation() {
        final NativeRelations relations = fresh(registry());
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(20, NativeNation.ARAWAK, 49), 1);
        relations.applyOutcome(DUTCH,
                NativeRelationOutcome.armsSale(21, NativeNation.ARAWAK, 49), 1);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(22, NativeNation.ARAWAK, 1), 1);
        assertEquals(5, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
        assertEquals(49, remainder(relations, DUTCH, NativeNation.ARAWAK));
        assertEquals(0, remainder(relations, FRANCE, NativeNation.ARAWAK));
        assertEquals(0, remainder(relations, FRANCE, NativeNation.TUPI));
    }

    @Test
    void armsRemainderSurvivesSaveAndContinue() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(23, NativeNation.ARAWAK, 30), 1);
        final NativeRelations continued = reload(relations, registry);
        assertEquals(30, remainder(continued, FRANCE, NativeNation.ARAWAK));
        continued.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(24, NativeNation.ARAWAK, 20), 1);
        assertEquals(5, continued.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(0, remainder(continued, FRANCE, NativeNation.ARAWAK));
    }

    @Test
    void directTribalChangesAndReductionsIgnoreTheLocalFactor() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.settlementAttack(25, village, false),
                0.25);
        assertEquals(20, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(5, relations.getLocalAlarm(village, FRANCE), 1e-9);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.tribute(26, village), 0.25);
        assertEquals(30, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(7.5, relations.getLocalAlarm(village, FRANCE), 1e-9);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.trade(27, village),
                0.25);
        assertEquals(5.5, relations.getLocalAlarm(village, FRANCE), 1e-9);
    }

    @Test
    void raisesAreClampedToOneHundred() {
        final NativeRelations relations = fresh(registry());
        relations.addTribalAnger(NativeNation.ARAWAK, DUTCH, 95);
        relations.applyOutcome(DUTCH,
                NativeRelationOutcome.unitAttack(28, NativeNation.ARAWAK, null),
                1);
        assertEquals(100, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
    }

    @Test
    void outcomeOfOneEuropeanLeavesOtherPairsUnchanged() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.settlementAttack(29, village, true), 1);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
        assertEquals(0, relations.getLocalAlarm(village, DUTCH), 1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.TUPI, FRANCE),
                1e-9);
        assertEquals(0,
                relations.getLocalAlarm(registry.getAll().get(2), FRANCE),
                1e-9);
    }

    @Test
    void distinctActionsApplyAgainAndRepeatedIdIsRefused() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.unitAttack(30,
                NativeNation.ARAWAK, village), 1);
        assertThrows(IllegalArgumentException.class,
                () -> relations.applyOutcome(FRANCE, NativeRelationOutcome
                        .unitAttack(30, NativeNation.ARAWAK, village), 1));
        assertEquals(10, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.unitAttack(31,
                NativeNation.ARAWAK, village), 1);
        assertEquals(20, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(20, relations.getLocalAlarm(village, FRANCE), 1e-9);
    }

    @Test
    void appliedOutcomeIdsSurviveSaveAndContinue() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.unitAttack(32,
                NativeNation.ARAWAK, village), 1);
        final NativeRelations continued = reload(relations, registry);
        assertThrows(IllegalArgumentException.class,
                () -> continued.applyOutcome(FRANCE, NativeRelationOutcome
                        .unitAttack(32, NativeNation.ARAWAK, village), 1));
        assertEquals(10, continued.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        continued.applyOutcome(FRANCE, NativeRelationOutcome.unitAttack(33,
                NativeNation.ARAWAK, village), 1);
        assertEquals(20, continued.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
    }

    @Test
    void refusedOutcomesLeaveNoPartialChange() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement village = registry.getAll().get(1);
        relations.applyOutcome(FRANCE,
                NativeRelationOutcome.armsSale(34, NativeNation.ARAWAK, 1), 1);
        relations.applyOutcome(FRANCE, NativeRelationOutcome.unitAttack(35,
                NativeNation.ARAWAK, village), 1);
        final List<Object> before = snapshot(relations);
        final NativeSettlements other = NativeSettlements.make(
                List.of(po(0, NativeNation.ARAWAK, true, 3, 3)),
                location -> true, Set.of());
        assertThrows(IllegalArgumentException.class,
                () -> relations.applyOutcome(FRANCE,
                        NativeRelationOutcome.unitAttack(36,
                                NativeNation.ARAWAK, other.getAll().get(0)),
                        1));
        assertThrows(IllegalArgumentException.class,
                () -> relations.applyOutcome(FRANCE, NativeRelationOutcome
                        .unitAttack(37, NativeNation.TUPI, village), 1));
        assertThrows(IllegalArgumentException.class,
                () -> relations.applyOutcome(FRANCE, NativeRelationOutcome
                        .settlementAttack(38, other.getAll().get(0), false),
                        1));
        assertThrows(ArithmeticException.class,
                () -> relations
                        .applyOutcome(FRANCE,
                                NativeRelationOutcome.armsSale(39,
                                        NativeNation.ARAWAK, Integer.MAX_VALUE),
                                1));
        assertEquals(before, snapshot(relations));
    }

    @Test
    void invalidOutcomeArgumentsAreRefusedBeforeCreation() {
        final NativeSettlements registry = registry();
        final NativeSettlement village = registry.getAll().get(1);
        assertThrows(IllegalArgumentException.class, () -> NativeRelationOutcome
                .unitAttack(0, NativeNation.ARAWAK, village));
        assertThrows(IllegalArgumentException.class,
                () -> NativeRelationOutcome.burial(-1, NativeNation.TUPI));
        assertThrows(IllegalArgumentException.class,
                () -> NativeRelationOutcome.gift(40, village, -1));
        assertThrows(IllegalArgumentException.class, () -> NativeRelationOutcome
                .armsSale(41, NativeNation.TUPI, -1));
        assertThrows(NullPointerException.class,
                () -> NativeRelationOutcome.trade(42, null));
        assertThrows(NullPointerException.class,
                () -> NativeRelationOutcome.burial(43, null));
    }

    @Test
    void savedArmsAndOutcomeIdsAreValidatedWhenLoaded() {
        final NativeSettlements registry = registry();
        final List<NativeTribalAngerPo> anger = NativeRelations
                .freshTribalAnger();
        final List<NativeLocalAlarmPo> alarm = NativeRelations
                .freshLocalAlarm(savedSettlements());
        assertThrows(IllegalArgumentException.class,
                () -> NativeRelations.make(registry, anger, alarm,
                        List.of(arms(NativeNation.ARAWAK, FRANCE, 50)),
                        List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NativeRelations.make(registry, anger, alarm,
                        List.of(arms(NativeNation.ARAWAK, FRANCE, -1)),
                        List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NativeRelations.make(registry, anger, alarm,
                        List.of(arms(NativeNation.ARAWAK, FRANCE, 4),
                                arms(NativeNation.ARAWAK, FRANCE, 5)),
                        List.of()));
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, anger, alarm, List.of(), List.of(0L)));
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, anger, alarm, List.of(), List.of(7L, 7L)));
        final NativeRelations absent = NativeRelations.make(registry, anger,
                alarm, null, null);
        assertTrue(absent.saveArmsSales().isEmpty());
        assertTrue(absent.saveAppliedOutcomes().isEmpty());
    }

}
