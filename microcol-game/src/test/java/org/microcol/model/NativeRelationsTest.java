package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.NativeLocalAlarmPo;
import org.microcol.model.store.NativeSettlementPo;
import org.microcol.model.store.NativeTribalAngerPo;

/**
 * Native relations toward each European: fresh zeros, the peaceful update
 * formula, national and founder multipliers, the once-only arrival clear, and
 * the saved tables that are rejected when they are incomplete or invalid.
 */
class NativeRelationsTest {

    private static final EuropeanNation FRANCE = EuropeanNation.FRENCH;

    private static final EuropeanNation DUTCH = EuropeanNation.DUTCH;

    private static NativeSettlementPo po(final int id,
            final NativeNation nation, final int x, final int y) {
        final NativeSettlementPo po = new NativeSettlementPo();
        po.setId(id);
        po.setNation(nation);
        po.setSettlementClass(nation.getSettlementClass());
        po.setCapital(true);
        po.setTreasury(2000);
        po.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        po.setOfferedGoods(List.of());
        po.setWantedGoods(List.of());
        po.setTrade(List.of());
        po.setLocation(Location.of(x, y));
        return po;
    }

    /**
     * An Arawak village and a Tupi camp, each the only capital of its nation.
     */
    private static List<NativeSettlementPo> savedSettlements() {
        return List.of(po(0, NativeNation.ARAWAK, 3, 3),
                po(1, NativeNation.TUPI, 6, 6));
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

    private static NativeTribalAngerPo tribal(final NativeNation nation,
            final EuropeanNation european, final double value) {
        final NativeTribalAngerPo po = new NativeTribalAngerPo();
        po.setNation(nation);
        po.setEuropean(european);
        po.setValue(value);
        return po;
    }

    private static NativeLocalAlarmPo alarm(final int settlementId,
            final EuropeanNation european, final double value) {
        final NativeLocalAlarmPo po = new NativeLocalAlarmPo();
        po.setSettlementId(settlementId);
        po.setEuropean(european);
        po.setValue(value);
        return po;
    }

    @Test
    void hostilityBandsAreHalfOpenFromZeroToOneHundred() {
        assertEquals(NativeHostility.CONTENT, NativeHostility.of(0));
        assertEquals(NativeHostility.CONTENT, NativeHostility.of(24.999));
        assertEquals(NativeHostility.UNEASY, NativeHostility.of(25));
        assertEquals(NativeHostility.UNEASY, NativeHostility.of(49.999));
        assertEquals(NativeHostility.ANGRY, NativeHostility.of(50));
        assertEquals(NativeHostility.ANGRY, NativeHostility.of(74.999));
        assertEquals(NativeHostility.HOSTILE, NativeHostility.of(75));
        assertEquals(NativeHostility.HOSTILE, NativeHostility.of(100));
    }

    @Test
    void freshPairsStartAtZeroAndHostilityReadsTheLargerMeasure() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE));
        assertEquals(0, relations.getLocalAlarm(arawak, FRANCE));
        assertEquals(NativeHostility.CONTENT,
                relations.getHostility(arawak, FRANCE));
        relations.addTribalAnger(NativeNation.ARAWAK, FRANCE, 30);
        assertEquals(NativeHostility.UNEASY,
                relations.getHostility(arawak, FRANCE));
        relations.addLocalAlarm(arawak, FRANCE, 80, 1);
        assertEquals(NativeHostility.HOSTILE,
                relations.getHostility(arawak, FRANCE));
    }

    @Test
    void quietUpdateDecaysBothMeasuresAndAddsMeanAlarmToAnger() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.addTribalAnger(NativeNation.ARAWAK, DUTCH, 3);
        relations.addLocalAlarm(arawak, DUTCH, 3, 1);
        relations.applyEuropeanUpdate(DUTCH, Map.of(), 1);
        // Local alarm 3 -> 2; the mean of the Arawak alarms is 2, so anger
        // 3 -> 2 gains 2/20 = 0.1.
        assertEquals(2, relations.getLocalAlarm(arawak, DUTCH), 1e-9);
        assertEquals(2.1, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
    }

    @Test
    void decayStopsAtZeroAndQuietEmptyStateStaysZero() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.addLocalAlarm(arawak, DUTCH, 0.5, 1);
        relations.applyEuropeanUpdate(DUTCH, Map.of(), 1);
        assertEquals(0, relations.getLocalAlarm(arawak, DUTCH), 1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
    }

    @Test
    void localPressureAddsToAlarmAndItsMeanReachesOnlyItsTribe() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        final NativeSettlement tupi = registry.getAll().get(1);
        relations.applyEuropeanUpdate(DUTCH, Map.of(0, 40.0), 1);
        assertEquals(40, relations.getLocalAlarm(arawak, DUTCH), 1e-9);
        // The Arawak mean is 40; a single settlement gives 40/20 = 2 anger.
        assertEquals(2, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
        assertEquals(0, relations.getLocalAlarm(tupi, DUTCH), 1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.TUPI, DUTCH),
                1e-9);
    }

    @Test
    void alarmAboveHundredIsClamped() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.applyEuropeanUpdate(DUTCH, Map.of(0, 500.0), 1);
        assertEquals(100, relations.getLocalAlarm(arawak, DUTCH), 1e-9);
        // The clamped alarm 100 adds 100/20 = 5 to the single Arawak anger.
        assertEquals(5, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
    }

    @Test
    void frenchFactorHalvesPositiveAlarmAndFractionsAreKept() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.applyEuropeanUpdate(FRANCE, Map.of(0, 10.0), 0.5);
        assertEquals(5, relations.getLocalAlarm(arawak, FRANCE), 1e-9);
        assertEquals(0.25,
                relations.getTribalAnger(NativeNation.ARAWAK, FRANCE), 1e-9);
    }

    @Test
    void frenchAndPocahontasStackToOneQuarterOfPositiveAlarm() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.applyEuropeanUpdate(FRANCE, Map.of(0, 10.0), 0.25);
        assertEquals(2.5, relations.getLocalAlarm(arawak, FRANCE), 1e-9);
        assertEquals(0.125,
                relations.getTribalAnger(NativeNation.ARAWAK, FRANCE), 1e-9);
    }

    @Test
    void directTribalAngerIsNeverHalvedButLocalTypedAlarmIs() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.addTribalAnger(NativeNation.ARAWAK, FRANCE, 10);
        relations.addLocalAlarm(arawak, FRANCE, 10, 0.25);
        assertEquals(10, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(2.5, relations.getLocalAlarm(arawak, FRANCE), 1e-9);
    }

    @Test
    void updatingOneEuropeanLeavesOtherPairsUnchanged() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.applyEuropeanUpdate(FRANCE, Map.of(0, 10.0), 0.5);
        assertEquals(0, relations.getLocalAlarm(arawak, DUTCH), 1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.TUPI, FRANCE),
                1e-9);
    }

    @Test
    void arrivalClearsOnlyThatEuropeanPositiveMeasuresOnce() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.addTribalAnger(NativeNation.ARAWAK, FRANCE, 30);
        relations.addLocalAlarm(arawak, FRANCE, 20, 1);
        relations.addTribalAnger(NativeNation.ARAWAK, DUTCH, 5);
        relations.clearEuropean(FRANCE);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
        assertEquals(0, relations.getLocalAlarm(arawak, FRANCE), 1e-9);
        assertEquals(5, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
    }

    @Test
    void savedFractionsReloadExactlyAndLaterChangesAreKept() {
        final NativeSettlements registry = registry();
        final NativeRelations relations = fresh(registry);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.applyEuropeanUpdate(FRANCE, Map.of(0, 10.0), 0.25);
        final NativeRelations reloaded = NativeRelations.make(registry,
                relations.saveTribalAnger(), relations.saveLocalAlarm(),
                relations.saveArmsSales(), relations.saveAppliedOutcomes());
        assertEquals(2.5, reloaded.getLocalAlarm(arawak, FRANCE), 1e-9);
        assertEquals(relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                reloaded.getTribalAnger(NativeNation.ARAWAK, FRANCE), 0);
        reloaded.addLocalAlarm(arawak, FRANCE, 4, 1);
        assertEquals(6.5, reloaded.getLocalAlarm(arawak, FRANCE), 1e-9);
    }

    @Test
    void missingOrIncompleteTablesAreRejected() {
        final NativeSettlements registry = registry();
        final List<NativeTribalAngerPo> anger = NativeRelations
                .freshTribalAnger();
        final List<NativeLocalAlarmPo> alarm = NativeRelations
                .freshLocalAlarm(savedSettlements());
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, null, alarm, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, anger, null, List.of(), List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NativeRelations.make(registry,
                        anger.subList(0, anger.size() - 1), alarm, List.of(),
                        List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> NativeRelations.make(registry, anger,
                        alarm.subList(0, alarm.size() - 1), List.of(),
                        List.of()));
    }

    @Test
    void duplicateUnknownAndInvalidEntriesAreRejected() {
        final NativeSettlements registry = registry();
        final List<NativeTribalAngerPo> anger = new ArrayList<>(
                NativeRelations.freshTribalAnger());
        final List<NativeLocalAlarmPo> alarm = new ArrayList<>(
                NativeRelations.freshLocalAlarm(savedSettlements()));
        final List<NativeTribalAngerPo> duplicateAnger = new ArrayList<>(anger);
        duplicateAnger.set(duplicateAnger.size() - 1,
                tribal(NativeNation.ARAWAK, FRANCE, 0));
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, duplicateAnger, alarm, List.of(), List.of()));
        final List<NativeLocalAlarmPo> unknownAlarm = new ArrayList<>(alarm);
        unknownAlarm.set(0, alarm(9, DUTCH, 0));
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, anger, unknownAlarm, List.of(), List.of()));
        final List<NativeTribalAngerPo> nullNation = new ArrayList<>(anger);
        nullNation.set(0, tribal(null, DUTCH, 0));
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, nullNation, alarm, List.of(), List.of()));
        for (final double bad : List.of(-1.0, 100.5, Double.NaN,
                Double.POSITIVE_INFINITY)) {
            final List<NativeLocalAlarmPo> invalid = new ArrayList<>(alarm);
            invalid.set(0, alarm(0, DUTCH, bad));
            assertThrows(IllegalArgumentException.class, () -> NativeRelations
                    .make(registry, anger, invalid, List.of(), List.of()));
        }
        final List<NativeLocalAlarmPo> missingValue = new ArrayList<>(alarm);
        final NativeLocalAlarmPo blank = alarm(0, DUTCH, 0);
        blank.setValue(null);
        missingValue.set(0, blank);
        assertThrows(IllegalArgumentException.class, () -> NativeRelations
                .make(registry, anger, missingValue, List.of(), List.of()));
    }

    @Test
    void boundaryValuesZeroAndOneHundredLoad() {
        final NativeSettlements registry = registry();
        final List<NativeTribalAngerPo> anger = new ArrayList<>(
                NativeRelations.freshTribalAnger());
        anger.set(0, tribal(NativeNation.ARAWAK, DUTCH, 100));
        final NativeRelations relations = NativeRelations.make(registry, anger,
                NativeRelations.freshLocalAlarm(savedSettlements()), List.of(),
                List.of());
        assertEquals(100, relations.getTribalAnger(NativeNation.ARAWAK, DUTCH),
                1e-9);
        assertEquals(0, relations.getTribalAnger(NativeNation.ARAWAK, FRANCE),
                1e-9);
    }

}
