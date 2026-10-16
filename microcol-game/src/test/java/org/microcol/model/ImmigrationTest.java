package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.ReferenceExpedition;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.RecruitmentPo;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

class ImmigrationTest {

    private GameModelPo scenario() {
        return new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private RecruitmentPo savedDutchPool(final GameModelPo data) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().getRecruitment();
    }

    private long pierPeople(final Player player) {
        return player.getAllUnits().stream()
                .filter(unit -> unit.isAtEuropePier() && !unit.isShip())
                .count();
    }

    private int crossProduction(final Player player) {
        return player.getColonies().stream()
                .mapToInt(colony -> colony.getGoodsStats()
                        .getStatsByType(GoodsType.CROSS).getRowProduction())
                .sum();
    }

    private List<Integer> candidateIds(final Player player) {
        return player.save().getRecruitment().getCandidates().stream()
                .map(candidate -> candidate.getId())
                .collect(Collectors.toList());
    }

    private void advanceToNextDutchTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    @Test
    void thresholdMinusOneWaitsUntilNextTurnAndProductionCreditsOnce() {
        final GameModelPo data = scenario();
        savedDutchPool(data).setCrosses(99);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final int produced = crossProduction(dutch);
        final long pierBefore = pierPeople(dutch);
        final List<Integer> candidates = candidateIds(dutch);
        assertTrue(produced > 0);
        assertFalse(dutch.hasPendingImmigrant());
        advanceToNextDutchTurn(model);
        assertEquals(99 + produced, dutch.getImmigrationCrosses());
        assertTrue(dutch.hasPendingImmigrant());
        assertEquals(pierBefore, pierPeople(dutch));
        assertEquals(candidates, candidateIds(dutch));
        advanceToNextDutchTurn(model);
        assertEquals(pierBefore + 1, pierPeople(dutch));
        assertEquals(1, dutch.save().getRecruitment().getEarnedImmigrants());
        assertEquals(99 + produced * 2 - 100, dutch.getImmigrationCrosses());
    }

    @Test
    void exactAndSurplusThresholdsMaterializeOneChosenCandidate() {
        for (final int initialCrosses : new int[] { 100, 101 }) {
            final GameModelPo data = scenario();
            savedDutchPool(data).setCrosses(initialCrosses);
            final Model model = load(data);
            final Player dutch = model.getPlayerByName("Dutch");
            final int produced = crossProduction(dutch);
            final int gold = dutch.getGold();
            final long pierBefore = pierPeople(dutch);
            final List<Integer> ids = candidateIds(dutch);
            final List<UnitType> types = dutch.save().getRecruitment()
                    .getCandidates().stream()
                    .map(candidate -> candidate.getType())
                    .collect(Collectors.toList());
            final int nextUnitId = model.getAllUnits().stream()
                    .mapToInt(Unit::getId).max().orElseThrow() + 1;
            advanceToNextDutchTurn(model);
            assertEquals(pierBefore + 1, pierPeople(dutch));
            final Unit immigrant = model.getUnitById(nextUnitId);
            assertTrue(immigrant.isAtEuropePier());
            assertSame(dutch, immigrant.getOwner());
            final List<Integer> after = candidateIds(dutch);
            int changedSlot = -1;
            for (int slot = 0; slot < 3; slot++) {
                if (!ids.get(slot).equals(after.get(slot))) {
                    assertEquals(-1, changedSlot);
                    changedSlot = slot;
                    assertEquals(4, after.get(slot));
                }
            }
            assertTrue(changedSlot >= 0);
            assertSame(types.get(changedSlot), immigrant.getType());
            assertEquals(initialCrosses - 100 + produced,
                    dutch.getImmigrationCrosses());
            assertEquals(125, dutch.getImmigrationTarget());
            assertEquals(1,
                    dutch.save().getRecruitment().getEarnedImmigrants());
            assertEquals(0, dutch.save().getRecruitment().getPaidPassages());
            assertEquals(gold, dutch.getGold());
        }
    }

    @Test
    void twoEarnedArrivalsConsumeIncreasingTargetsAndResumeDoesNotReplay() {
        final GameModelPo data = scenario();
        savedDutchPool(data).setCrosses(230);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final int produced = crossProduction(dutch);
        final int gold = dutch.getGold();
        final long pierBefore = pierPeople(dutch);
        advanceToNextDutchTurn(model);
        assertEquals(pierBefore + 2, pierPeople(dutch));
        assertEquals(5 + produced, dutch.getImmigrationCrosses());
        assertEquals(150, dutch.getImmigrationTarget());
        assertEquals(2, dutch.save().getRecruitment().getEarnedImmigrants());
        assertEquals(0, dutch.save().getRecruitment().getPaidPassages());
        assertEquals(6, dutch.save().getRecruitment().getNextCandidateId());
        assertEquals(gold, dutch.getGold());

        final Model resumed = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player reloaded = resumed.getPlayerByName("Dutch");
        final long savedPier = pierPeople(reloaded);
        final int savedCrosses = reloaded.getImmigrationCrosses();
        final List<Integer> savedCandidates = candidateIds(reloaded);
        resumed.startGame();
        assertEquals(savedPier, pierPeople(reloaded));
        assertEquals(savedCrosses, reloaded.getImmigrationCrosses());
        assertEquals(savedCandidates, candidateIds(reloaded));
        assertEquals(2, reloaded.save().getRecruitment().getEarnedImmigrants());
    }

    @Test
    void twoColonyProductionCreditsOneSharedNationalBalance() {
        final Model model = load(scenario());
        final Player dutch = model.getPlayerByName("Dutch");
        final Colony first = model.getColonyById(0).orElseThrow();
        final Colony second = model.getColonyById(1).orElseThrow();
        first.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        second.getWarehouse().setGoods(Goods.of(GoodsType.CORN, 100));
        final int firstProduced = first.getGoodsStats()
                .getStatsByType(GoodsType.CROSS).getRowProduction();
        final int secondProduced = second.getGoodsStats()
                .getStatsByType(GoodsType.CROSS).getRowProduction();
        final int before = dutch.getImmigrationCrosses();
        assertTrue(firstProduced > 0);
        assertTrue(secondProduced > 0);
        first.startTurn();
        assertEquals(before + firstProduced, dutch.getImmigrationCrosses());
        second.startTurn();
        assertEquals(before + firstProduced + secondProduced,
                dutch.getImmigrationCrosses());
    }

    @Test
    void englandUsesReducedThresholdAtFirstFreshTurn() {
        final GameModelPo source = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        source.setModel(ReferenceExpedition.create(source.getModel().getMap(),
                EuropeanNation.ENGLISH, Difficulty.CONQUISTADOR));
        source.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "England".equals(player.getName()))
                .findFirst().orElseThrow().getRecruitment().setCrosses(67);
        final Model model = load(source);
        final Player england = model.getPlayerByName("England");
        assertEquals(67, england.getImmigrationTarget());
        assertTrue(england.hasPendingImmigrant());
        model.startGame();
        assertEquals(1, pierPeople(england));
        assertEquals(0, england.getImmigrationCrosses());
        assertEquals(84, england.getImmigrationTarget());
        assertEquals(1, england.save().getRecruitment().getEarnedImmigrants());
    }

    @Test
    void elapsedTurnElevenNoLongerGrantsAnUnrelatedColonist() {
        final GameModelPo data = scenario();
        data.getModel().getCalendar().setNumberOfPlayedTurns(10);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final long before = pierPeople(dutch);
        advanceToNextDutchTurn(model);
        assertEquals(11, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(before, pierPeople(dutch));
        assertEquals(0, dutch.save().getRecruitment().getEarnedImmigrants());
    }

    @Test
    void overflowingProductionAndExhaustedCandidateTokensDoNotPartiallyApply() {
        final GameModelPo overflow = scenario();
        savedDutchPool(overflow).setCrosses(Integer.MAX_VALUE);
        final Model producing = load(overflow);
        final Player dutch = producing.getPlayerByName("Dutch");
        final Colony colony = producing.getColonyById(0).orElseThrow();
        final String before = new Gson().toJson(producing.save());
        assertThrows(ArithmeticException.class, colony::startTurn);
        assertEquals(before, new Gson().toJson(producing.save()));
        assertEquals(Integer.MAX_VALUE, dutch.getImmigrationCrosses());

        final GameModelPo exhausted = scenario();
        savedDutchPool(exhausted).setCrosses(100);
        savedDutchPool(exhausted).setNextCandidateId(Integer.MAX_VALUE);
        final Model blocked = load(exhausted);
        final Player blockedDutch = blocked.getPlayerByName("Dutch");
        final long pierBefore = pierPeople(blockedDutch);
        final String poolBefore = new Gson()
                .toJson(blockedDutch.save().getRecruitment());
        assertThrows(IllegalStateException.class, blockedDutch::startTurn);
        assertEquals(pierBefore, pierPeople(blockedDutch));
        assertEquals(poolBefore,
                new Gson().toJson(blockedDutch.save().getRecruitment()));
    }

    @Test
    void exhaustedUnitIdsAndProspectiveTargetOverflowLeaveEarnedStateUntouched() {
        final GameModelPo noUnitIds = scenario();
        savedDutchPool(noUnitIds).setCrosses(100);
        final UnitPo finalId = new UnitPo();
        finalId.setId(Integer.MAX_VALUE - 1);
        finalId.setOwnerId("Dutch");
        finalId.setType(UnitType.COLONIST);
        finalId.setWorkUpdates(0);
        finalId.setAction(new UnitActionNoActionPo());
        finalId.setAvailableMoves(1);
        finalId.setPlaceEuropePort(new PlaceEuropePortPo());
        noUnitIds.getModel().getUnits().add(finalId);
        final Model full = load(noUnitIds);
        final Player fullDutch = full.getPlayerByName("Dutch");
        final long fullPier = pierPeople(fullDutch);
        final String fullPool = new Gson()
                .toJson(fullDutch.save().getRecruitment());
        assertThrows(IllegalStateException.class, fullDutch::startTurn);
        assertEquals(fullPier, pierPeople(fullDutch));
        assertEquals(fullPool,
                new Gson().toJson(fullDutch.save().getRecruitment()));

        final GameModelPo noNextTarget = scenario();
        savedDutchPool(noNextTarget).setEarnedImmigrants(85_899_341);
        savedDutchPool(noNextTarget).setCrosses(2_147_483_625);
        final Model threshold = load(noNextTarget);
        final Player thresholdDutch = threshold.getPlayerByName("Dutch");
        final long thresholdPier = pierPeople(thresholdDutch);
        final String thresholdPool = new Gson()
                .toJson(thresholdDutch.save().getRecruitment());
        assertThrows(IllegalStateException.class, thresholdDutch::startTurn);
        assertEquals(thresholdPier, pierPeople(thresholdDutch));
        assertEquals(thresholdPool,
                new Gson().toJson(thresholdDutch.save().getRecruitment()));
    }

    @Test
    void twoHundredThirtyCrossesSettleTwiceLeavingFiveWithoutProduction() {
        final RecruitmentPo initial = savedDutchPool(scenario());
        initial.setCrosses(230);
        final RecruitmentPool pool = new RecruitmentPool(initial);
        final int first = pool.checkCanSettleEarned(Difficulty.CONQUISTADOR,
                "Dutch");
        assertEquals(100, first);
        pool.settleEarned(pool.chooseEarnedSlot(), first);
        final int second = pool.checkCanSettleEarned(Difficulty.CONQUISTADOR,
                "Dutch");
        assertEquals(125, second);
        pool.settleEarned(pool.chooseEarnedSlot(), second);
        assertEquals(5, pool.getCrosses());
        assertEquals(150, pool.getTarget(Difficulty.CONQUISTADOR, "Dutch"));
        assertFalse(pool.hasReachedTarget(Difficulty.CONQUISTADOR, "Dutch"));
        assertEquals(2, pool.save().getEarnedImmigrants());
        assertEquals(0, pool.save().getPaidPassages());
    }

    @Test
    void computerColonialReceivesArrivalButClosedEuropeDoesNot() {
        for (final boolean independent : new boolean[] { false, true }) {
            final GameModelPo source = new GameModelPoDao()
                    .loadFromClassPath("/maps/free-play.microcol");
            source.setModel(
                    ReferenceExpedition.create(source.getModel().getMap(),
                            EuropeanNation.ENGLISH, Difficulty.CONQUISTADOR));
            source.getModel().getGameManager().getPlayers().stream()
                    .filter(player -> "Dutch".equals(player.getName()))
                    .findFirst().orElseThrow().getRecruitment().setCrosses(100);
            source.getModel().getGameManager().getPlayers().stream()
                    .filter(player -> "Dutch".equals(player.getName()))
                    .findFirst().orElseThrow()
                    .setDeclaredIndependence(independent);
            final Model model = load(source);
            final Player computer = model.getPlayerByName("Dutch");
            assertTrue(computer.isComputer());
            model.startGame();
            while (model.getCurrentPlayer() != computer) {
                model.endTurn();
            }
            assertEquals(independent ? 0 : 1, pierPeople(computer));
            assertEquals(independent ? 100 : 0,
                    computer.getImmigrationCrosses());
            assertEquals(independent ? 0 : 1,
                    computer.save().getRecruitment().getEarnedImmigrants());
        }
    }
}
