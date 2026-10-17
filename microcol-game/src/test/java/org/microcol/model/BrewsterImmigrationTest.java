package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CongressPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.RecruitmentPo;
import org.microcol.model.store.ModelPo;

/** The earned choice interrupts, then resumes, one national preparation. */
class BrewsterImmigrationTest {

    private GameModelPo fixture(final int crosses) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        final PlayerPo dutch = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        final CongressPo congress = dutch.getCongress();
        congress.setMembers(List.of(Founder.WILLIAM_BREWSTER));
        congress.setSelected(null);
        dutch.getRecruitment().setCrosses(crosses);
        dutch.getRecruitment().getCandidates().forEach(candidate -> {
            if (candidate.getType() == UnitType.INDENTURED_SERVANT
                    || candidate.getType() == UnitType.PETTY_CRIMINAL) {
                candidate.setType(UnitType.COLONIST);
            }
        });
        return data;
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void nextDutchTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    private int peopleAtPier(final Player player) {
        return (int) player.getAllUnits().stream()
                .filter(unit -> unit.isAtEuropePier() && !unit.isShip())
                .count();
    }

    @Test
    void twoSequentialChoicesPauseBeforeProductionAndContinueDoesNotReplay() {
        final Model model = load(fixture(230));
        final Player dutch = model.getPlayerByName("Dutch");
        final int pierBefore = peopleAtPier(dutch);
        final int bellsBefore = dutch.getCongressBells();
        final int producedCrosses = dutch.getColonies().stream()
                .mapToInt(colony -> colony.getGoodsStats()
                        .getStatsByType(GoodsType.CROSS).getRowProduction())
                .sum();
        nextDutchTurn(model);
        assertTrue(model.isAwaitingImmigrationChoice());
        assertFalse(model.isGameRunning());
        assertEquals(230, dutch.getImmigrationCrosses());
        assertEquals(bellsBefore, dutch.getCongressBells());
        assertEquals(pierBefore, peopleAtPier(dutch));
        assertThrows(IllegalStateException.class, model::endTurn);
        final List<EarnedImmigrationOffer> first = dutch
                .getEarnedImmigrationOffers();
        assertEquals(3, first.size());
        assertEquals(100, first.get(0).getThreshold());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player owner = restored.getPlayerByName("Dutch");
        restored.startGame();
        assertTrue(restored.isAwaitingImmigrationChoice());
        assertEquals(230, owner.getImmigrationCrosses());
        assertEquals(pierBefore, peopleAtPier(owner));
        final EarnedImmigrationOffer chosen = owner.getEarnedImmigrationOffers()
                .get(0);
        owner.chooseEarnedImmigrant(chosen);
        assertTrue(restored.isAwaitingImmigrationChoice());
        assertEquals(130, owner.getImmigrationCrosses());
        assertEquals(125, owner.getImmigrationTarget());
        assertEquals(pierBefore + 1, peopleAtPier(owner));
        assertFalse(owner.canChooseEarnedImmigrant(chosen));

        final Model between = Model.make(restored.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player resumed = between.getPlayerByName("Dutch");
        between.startGame();
        assertTrue(between.isAwaitingImmigrationChoice());
        assertEquals(130, resumed.getImmigrationCrosses());
        resumed.chooseEarnedImmigrant(
                resumed.getEarnedImmigrationOffers().get(1));
        assertFalse(between.isAwaitingImmigrationChoice());
        assertTrue(between.isGameRunning());
        assertEquals(5 + producedCrosses, resumed.getImmigrationCrosses());
        assertEquals(2, resumed.save().getRecruitment().getEarnedImmigrants());
        assertEquals(pierBefore + 2, peopleAtPier(resumed));
        assertTrue(resumed.getCongressBells() > bellsBefore);
        final Model after = Model.make(between.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player finalOwner = after.getPlayerByName("Dutch");
        final int finalCrosses = finalOwner.getImmigrationCrosses();
        final int finalPier = peopleAtPier(finalOwner);
        after.startGame();
        assertFalse(after.isAwaitingImmigrationChoice());
        assertEquals(finalCrosses, finalOwner.getImmigrationCrosses());
        assertEquals(finalPier, peopleAtPier(finalOwner));
    }

    @Test
    void firstGameStartPausesBeforeColoniesAndResumesOnlyAfterChoice() {
        final GameModelPo data = fixture(100);
        data.getModel().getGameManager().setGameStarted(false);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final int bellsBefore = dutch.getCongressBells();
        final int peopleBefore = peopleAtPier(dutch);
        model.startGame();
        assertTrue(model.isAwaitingImmigrationChoice());
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        assertEquals(100, dutch.getImmigrationCrosses());
        assertEquals(bellsBefore, dutch.getCongressBells());
        assertEquals(peopleBefore, peopleAtPier(dutch));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player owner = restored.getPlayerByName("Dutch");
        restored.startGame();
        assertTrue(restored.isAwaitingImmigrationChoice());
        owner.chooseEarnedImmigrant(owner.getEarnedImmigrationOffers().get(0));
        assertFalse(restored.isAwaitingImmigrationChoice());
        assertTrue(restored.isGameRunning());
        assertEquals(1, owner.save().getRecruitment().getEarnedImmigrants());
        assertEquals(peopleBefore + 1, peopleAtPier(owner));
        assertTrue(owner.getCongressBells() > bellsBefore);
    }

    @Test
    void computerBrewsterSettlesTwoArrivalsWithoutHumanChoice() {
        final GameModelPo data = fixture(230);
        final PlayerPo dutchPo = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        dutchPo.setComputer(true);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final int peopleBefore = peopleAtPier(dutch);
        nextDutchTurn(model);
        assertTrue(model.isGameRunning());
        assertFalse(model.isAwaitingImmigrationChoice());
        assertFalse(dutch.save().getRecruitment().getPendingEarnedChoice());
        assertEquals(2, dutch.save().getRecruitment().getEarnedImmigrants());
        assertEquals(peopleBefore + 2, peopleAtPier(dutch));
        assertEquals(150, dutch.getImmigrationTarget());
        assertEquals(
                5 + dutch.getColonies().stream()
                        .mapToInt(colony -> colony.getGoodsStats()
                                .getStatsByType(GoodsType.CROSS)
                                .getRowProduction())
                        .sum(),
                dutch.getImmigrationCrosses());
    }

    @Test
    void staleAndForeignChoicesDoNotMaterializeAUnit() {
        final Model model = load(fixture(100));
        nextDutchTurn(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final Player foreign = model.getPlayers().stream()
                .filter(player -> player != dutch).findFirst().orElseThrow();
        final EarnedImmigrationOffer offer = dutch.getEarnedImmigrationOffers()
                .get(0);
        final int before = model.getAllUnits().size();
        assertFalse(foreign.canChooseEarnedImmigrant(offer));
        assertThrows(IllegalStateException.class,
                () -> foreign.chooseEarnedImmigrant(offer));
        assertEquals(before, model.getAllUnits().size());
        dutch.chooseEarnedImmigrant(offer);
        assertFalse(dutch.canChooseEarnedImmigrant(offer));
        assertThrows(IllegalStateException.class,
                () -> dutch.chooseEarnedImmigrant(offer));
        assertEquals(before + 1, model.getAllUnits().size());
    }

    @Test
    void settlementStageRejectsReentrantSaveUntilChoiceIsCoherent() {
        final Model model = load(fixture(100));
        nextDutchTurn(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final int before = dutch.getImmigrationCrosses();
        assertTrue(model.isAwaitingImmigrationChoice());
        model.beginImmigrationChoice(dutch);
        assertThrows(TurnPreparationException.class, model::save);
        assertThrows(IllegalStateException.class, model::endTurn);
        assertEquals(before, dutch.getImmigrationCrosses());
        model.continueImmigrationChoice();
        assertTrue(model.isAwaitingImmigrationChoice());
        assertEquals(before, dutch.getImmigrationCrosses());
        assertTrue(model.save().getGameManager().isGameStarted());
    }

    @Test
    void concurrentSnapshotSeesOnlyBeforeOrAfterACompleteChoice()
            throws Exception {
        final Model model = load(fixture(100));
        nextDutchTurn(model);
        final Player dutch = model.getPlayerByName("Dutch");
        final EarnedImmigrationOffer offer = dutch.getEarnedImmigrationOffers()
                .get(0);
        final int unitsBefore = model.getAllUnits().size();
        final ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            final CountDownLatch launched = new CountDownLatch(2);
            final Future<?> choice;
            final Future<ModelPo> snapshot;
            synchronized (model) {
                choice = workers.submit(() -> {
                    launched.countDown();
                    dutch.chooseEarnedImmigrant(offer);
                });
                snapshot = workers.submit(() -> {
                    launched.countDown();
                    return model.save();
                });
                assertTrue(launched.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class,
                        () -> choice.get(100, TimeUnit.MILLISECONDS));
                assertThrows(TimeoutException.class,
                        () -> snapshot.get(100, TimeUnit.MILLISECONDS));
            }
            choice.get(5, TimeUnit.SECONDS);
            final ModelPo saved = snapshot.get(5, TimeUnit.SECONDS);
            final Model restored = Model.make(saved,
                    MissionImpl.getPredefinedGameOverEvaluators());
            final int earned = restored.getPlayerByName("Dutch").save()
                    .getRecruitment().getEarnedImmigrants();
            assertTrue(earned == 0 || earned == 1);
            assertEquals(earned == 0 ? unitsBefore : model.getAllUnits().size(),
                    restored.getAllUnits().size());
            assertEquals(earned == 0, restored.isAwaitingImmigrationChoice());
        } finally {
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void malformedPendingMarkerAndExcludedMemberCandidateRejectOnLoad() {
        final GameModelPo bad = fixture(100);
        final PlayerPo dutch = bad.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        final RecruitmentPo pool = dutch.getRecruitment();
        pool.setPendingEarnedChoice(null);
        assertThrows(IllegalArgumentException.class, () -> load(bad));
        pool.setPendingEarnedChoice(false);
        pool.getCandidates().get(0).setType(UnitType.PETTY_CRIMINAL);
        assertThrows(IllegalArgumentException.class, () -> load(bad));
    }

    @Test
    void actualCongressJoinFiltersOnlyCurrentPoolSlots() {
        final GameModelPo data = fixture(0);
        final PlayerPo dutchPo = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> "Dutch".equals(player.getName()))
                .findFirst().orElseThrow();
        final CongressPo congress = dutchPo.getCongress();
        congress.setMembers(List.of());
        congress.setCandidates(List.of(Founder.JOHN_PAUL_JONES,
                Founder.THOMAS_JEFFERSON, Founder.ADAM_SMITH,
                Founder.WILLIAM_BREWSTER, Founder.FERDINAND_MAGELLAN));
        congress.setSelected(Founder.WILLIAM_BREWSTER);
        congress.setBells(99);
        final RecruitmentPo pool = dutchPo.getRecruitment();
        pool.getCandidates().get(1).setType(UnitType.INDENTURED_SERVANT);
        pool.getCandidates().get(2).setType(UnitType.PETTY_CRIMINAL);
        final Model model = load(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final int unitsBefore = dutch.getAllUnits().size();
        nextDutchTurn(model);
        assertTrue(
                dutch.getCongressMembers().contains(Founder.WILLIAM_BREWSTER));
        final RecruitmentPo after = dutch.save().getRecruitment();
        assertEquals(1, after.getCandidates().get(0).getId());
        assertEquals(4, after.getCandidates().get(1).getId());
        assertEquals(5, after.getCandidates().get(2).getId());
        assertEquals(6, after.getNextCandidateId());
        assertEquals(unitsBefore, dutch.getAllUnits().size());
        assertEquals(0, after.getPaidPassages());
        after.getCandidates().forEach(candidate -> assertFalse(
                candidate.getType() == UnitType.INDENTURED_SERVANT
                        || candidate.getType() == UnitType.PETTY_CRIMINAL));
    }
}
