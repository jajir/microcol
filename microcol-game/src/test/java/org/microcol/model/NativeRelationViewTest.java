package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.NativeLocalAlarmPo;
import org.microcol.model.store.NativeSettlementKnowledgePo;
import org.microcol.model.store.NativeSettlementPo;

/**
 * Native settlement relation view: the rate boundaries, the alarm change kept
 * from the last peaceful update and from committed outcomes, pair isolation,
 * the learned gate, rival provocation only from visible sources, and a pure
 * read that survives save and load.
 */
class NativeRelationViewTest {

    private static final EuropeanNation DUTCH = EuropeanNation.DUTCH;

    private static final EuropeanNation FRANCE = EuropeanNation.FRENCH;

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

    private static List<NativeSettlementPo> savedSettlements() {
        return List.of(po(0, NativeNation.ARAWAK, 3, 3),
                po(1, NativeNation.TUPI, 6, 6));
    }

    private static NativeSettlements registry(
            final List<NativeSettlementPo> saved) {
        return NativeSettlements.make(saved, location -> true, Set.of());
    }

    private static NativeRelations fresh(final NativeSettlements registry,
            final List<NativeSettlementPo> saved) {
        return NativeRelations.make(registry,
                NativeRelations.freshTribalAnger(),
                NativeRelations.freshLocalAlarm(saved), List.of(), List.of());
    }

    @Test
    void exclamationMarksFollowTheAcceptedRateBoundaries() {
        assertEquals(0, NativeRelationView.exclamationMarksFor(-0.0));
        assertEquals(0, NativeRelationView.exclamationMarksFor(0));
        assertEquals(0, NativeRelationView.exclamationMarksFor(-3));
        assertEquals(1, NativeRelationView.exclamationMarksFor(0.0001));
        assertEquals(1, NativeRelationView.exclamationMarksFor(1.75));
        assertEquals(1, NativeRelationView.exclamationMarksFor(2));
        assertEquals(2, NativeRelationView.exclamationMarksFor(2.25));
        assertEquals(2, NativeRelationView.exclamationMarksFor(2.0001));
        assertEquals(2, NativeRelationView.exclamationMarksFor(5));
        assertEquals(3, NativeRelationView.exclamationMarksFor(5.25));
        assertEquals(3, NativeRelationView.exclamationMarksFor(5.0001));
    }

    @Test
    void trendReadsTheExactSignAndNegativeZeroIsStable() {
        final NativeRelationView rising = NativeRelationView
                .of(NativeHostility.CONTENT, 0.001, Optional.empty());
        final NativeRelationView falling = NativeRelationView
                .of(NativeHostility.CONTENT, -0.001, Optional.empty());
        final NativeRelationView stable = NativeRelationView
                .of(NativeHostility.CONTENT, -0.0, Optional.empty());
        assertEquals(NativeRelationView.AlarmTrend.RISING, rising.getTrend());
        assertEquals(NativeRelationView.AlarmTrend.FALLING, falling.getTrend());
        assertEquals(NativeRelationView.AlarmTrend.STABLE, stable.getTrend());
        assertEquals(1, rising.getExclamationMarks());
        assertEquals(0, falling.getExclamationMarks());
        assertEquals(0, stable.getExclamationMarks());
    }

    @Test
    void alarmChangeIsTheDifferenceFromTheLastPeacefulUpdateAndSurvivesSave() {
        final List<NativeSettlementPo> saved = savedSettlements();
        final NativeSettlements registry = registry(saved);
        final NativeRelations relations = fresh(registry, saved);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.observeLocalAlarm(DUTCH);
        relations.applyEuropeanUpdate(DUTCH, Map.of(0, 4.0), 1);
        assertEquals(4, relations.getLocalAlarmChange(arawak, DUTCH), 1e-9);
        final NativeRelations restored = NativeRelations.make(registry,
                relations.saveTribalAnger(), relations.saveLocalAlarm(),
                relations.saveArmsSales(), relations.saveAppliedOutcomes());
        assertEquals(4, restored.getLocalAlarmChange(arawak, DUTCH), 1e-9);
        assertEquals(2, NativeRelationView.exclamationMarksFor(
                restored.getLocalAlarmChange(arawak, DUTCH)));
        relations.observeLocalAlarm(DUTCH);
        relations.applyEuropeanUpdate(DUTCH, Map.of(), 1);
        assertEquals(-1, relations.getLocalAlarmChange(arawak, DUTCH), 1e-9);
        assertEquals(0, NativeRelationView.exclamationMarksFor(
                relations.getLocalAlarmChange(arawak, DUTCH)));
    }

    @Test
    void outcomeMovesTheChangeUntilTheNextUpdateAndOtherPairsStayApart() {
        final List<NativeSettlementPo> saved = savedSettlements();
        final NativeSettlements registry = registry(saved);
        final NativeRelations relations = fresh(registry, saved);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.observeLocalAlarm(DUTCH);
        relations.applyEuropeanUpdate(DUTCH, Map.of(0, 4.0), 1);
        relations.addLocalAlarm(arawak, DUTCH, 3, 1);
        assertEquals(7, relations.getLocalAlarmChange(arawak, DUTCH), 1e-9);
        assertEquals(0, relations.getLocalAlarmChange(arawak, FRANCE), 1e-9);
    }

    @Test
    void fractionalChangeKeepsItsExactValueAndEqualValuesReadStable() {
        final List<NativeSettlementPo> saved = savedSettlements();
        final NativeSettlements registry = registry(saved);
        final NativeRelations relations = fresh(registry, saved);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.observeLocalAlarm(DUTCH);
        relations.applyEuropeanUpdate(DUTCH, Map.of(0, 2.25), 1);
        assertEquals(2.25, relations.getLocalAlarmChange(arawak, DUTCH), 1e-9);
        // Above 2 through 5 is two marks, so a 2.25 rise reads as two.
        assertEquals(2, NativeRelationView.exclamationMarksFor(
                relations.getLocalAlarmChange(arawak, DUTCH)));
        final double stable = relations.getLocalAlarmChange(arawak, FRANCE);
        assertEquals(NativeRelationView.AlarmTrend.STABLE,
                NativeRelationView
                        .of(NativeHostility.CONTENT, stable, Optional.empty())
                        .getTrend());
    }

    @Test
    void savedLocalAlarmWithoutPreviousValueReadsStableUntilObserved() {
        final List<NativeSettlementPo> saved = savedSettlements();
        final NativeSettlements registry = registry(saved);
        final List<NativeLocalAlarmPo> alarms = NativeRelations
                .freshLocalAlarm(saved);
        alarms.forEach(entry -> {
            entry.setPreviousValue(null);
            if (entry.getSettlementId() == 0 && entry.getEuropean() == DUTCH) {
                entry.setValue(5.0);
            }
        });
        final NativeRelations relations = NativeRelations.make(registry,
                NativeRelations.freshTribalAnger(), alarms, List.of(),
                List.of());
        final NativeSettlement arawak = registry.getAll().get(0);
        assertEquals(5, relations.getLocalAlarm(arawak, DUTCH), 1e-9);
        assertEquals(0, relations.getLocalAlarmChange(arawak, DUTCH), 1e-9);
    }

    @Test
    void viewNeedsALearnedSettlementAndReadsTheOwnersAttitude() {
        final Model unseen = NativeRelationsScene.island(NativeNation.ARAWAK)
                .toModel();
        final NativeSettlement unseenSettlement = NativeRelationsScene
                .settlement(unseen);
        assertTrue(unseen.getNativeRelationView(
                unseen.getPlayerByName(NativeRelationsScene.DUTCH),
                unseenSettlement).isEmpty());

        final Model seen = NativeRelationsScene.island(NativeNation.ARAWAK)
                .unit(NativeRelationsScene.DUTCH, UnitType.COLONIST,
                        NativeRelationsScene.at(0, 1),
                        NativeRelationsScene.UNARMED)
                .toModel();
        final Player dutch = seen.getPlayerByName(NativeRelationsScene.DUTCH);
        final NativeRelationView view = seen.getNativeRelationView(dutch,
                NativeRelationsScene.settlement(seen)).orElseThrow();
        assertEquals(NativeHostility.CONTENT, view.getAttitude());
        assertEquals(0, view.getAlarmChange(), 1e-9);
        assertEquals(NativeRelationView.AlarmTrend.STABLE, view.getTrend());
        assertTrue(view.getProvokingRival().isEmpty());
    }

    @Test
    void rivalProvokesOnlyThroughSourcesTheViewerCanSee() {
        final Model visible = NativeRelationsScene.island(NativeNation.ARAWAK)
                .unit(NativeRelationsScene.DUTCH, UnitType.COLONIST,
                        NativeRelationsScene.at(0, 1),
                        NativeRelationsScene.UNARMED)
                .unit(NativeRelationsScene.FRANCE, UnitType.COLONIST,
                        NativeRelationsScene.at(1, 0),
                        NativeRelationsScene.SOLDIER)
                .toModel();
        final NativeRelationView seen = visible.getNativeRelationView(
                visible.getPlayerByName(NativeRelationsScene.DUTCH),
                NativeRelationsScene.settlement(visible)).orElseThrow();
        assertEquals(NativeRelationsScene.FRANCE,
                seen.getProvokingRival().orElseThrow().getName());

        final NativeRelationsScene remembered = NativeRelationsScene
                .island(NativeNation.ARAWAK)
                .unit(NativeRelationsScene.DUTCH, UnitType.COLONIST,
                        NativeRelationsScene.at(-4, 0),
                        NativeRelationsScene.UNARMED)
                .unit(NativeRelationsScene.FRANCE, UnitType.COLONIST,
                        NativeRelationsScene.at(1, 0),
                        NativeRelationsScene.SOLDIER);
        final NativeSettlementKnowledgePo known = new NativeSettlementKnowledgePo();
        known.setPlayerName(NativeRelationsScene.DUTCH);
        known.setSettlementIds(List.of(0));
        remembered.toPo()
                .setNativeSettlementKnowledge(new ArrayList<>(List.of(known)));
        final Model hidden = remembered.toModel();
        final Player dutch = hidden.getPlayerByName(NativeRelationsScene.DUTCH);
        assertFalse(dutch.isCurrentlyVisible(NativeRelationsScene.at(1, 0)));
        final NativeRelationView unknown = hidden.getNativeRelationView(dutch,
                NativeRelationsScene.settlement(hidden)).orElseThrow();
        assertTrue(unknown.getProvokingRival().isEmpty());
    }

    @Test
    void arrivalClearAfterTurnStartReadsAsAFallFromTheObservedAlarm() {
        final List<NativeSettlementPo> saved = savedSettlements();
        final NativeSettlements registry = registry(saved);
        final NativeRelations relations = fresh(registry, saved);
        final NativeSettlement arawak = registry.getAll().get(0);
        relations.addLocalAlarm(arawak, DUTCH, 30, 1);
        // Same order as a turn: observe, congress clears, then the update.
        relations.observeLocalAlarm(DUTCH);
        relations.clearEuropean(DUTCH);
        relations.applyEuropeanUpdate(DUTCH, Map.of(0, 2.5), 1);
        assertEquals(2.5, relations.getLocalAlarm(arawak, DUTCH), 1e-9);
        assertEquals(-27.5, relations.getLocalAlarmChange(arawak, DUTCH), 1e-9);
        assertEquals(NativeRelationView.AlarmTrend.FALLING,
                NativeRelationView.of(NativeHostility.CONTENT,
                        relations.getLocalAlarmChange(arawak, DUTCH),
                        Optional.empty()).getTrend());
    }

    @Test
    void readingTheViewChangesNoRelation() {
        final Model model = NativeRelationsScene.island(NativeNation.ARAWAK)
                .unit(NativeRelationsScene.DUTCH, UnitType.COLONIST,
                        NativeRelationsScene.at(0, 1),
                        NativeRelationsScene.UNARMED)
                .toModel();
        final Player dutch = model.getPlayerByName(NativeRelationsScene.DUTCH);
        final NativeSettlement settlement = NativeRelationsScene
                .settlement(model);
        final double alarm = model.getNativeLocalAlarm(dutch, settlement);
        final double anger = model.getNativeTribalAnger(dutch,
                settlement.getNation());
        final NativeRelationView first = model
                .getNativeRelationView(dutch, settlement).orElseThrow();
        final NativeRelationView second = model
                .getNativeRelationView(dutch, settlement).orElseThrow();
        assertEquals(alarm, model.getNativeLocalAlarm(dutch, settlement), 0);
        assertEquals(anger,
                model.getNativeTribalAnger(dutch, settlement.getNation()), 0);
        assertEquals(first.getAlarmChange(), second.getAlarmChange(), 0);
        assertEquals(first.getAttitude(), second.getAttitude());
    }

}
