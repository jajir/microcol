package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.InterventionPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.turnevent.TurnEventIntervention;

/**
 * R-0189: the target is fixed at the first owner update after declaration and
 * announced once; only bells of that update onward count; the earning happens
 * once with its exact overflow; a saved state is validated before it is used.
 */
class InterventionTargetTest {

    private static final String MAP = "/maps/default-campaign-mission-2-thrive.microcol";

    private static GameModelPo data(final boolean declared) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(MAP);
        for (final PlayerPo player : data.getModel().getGameManager()
                .getPlayers()) {
            if (player.getWhosKingThisPlayerIs() != null) {
                player.setRoyalRoster(
                        RoyalRoster.initial(Difficulty.CONQUISTADOR));
            } else if (player.getName().equals("Dutch")) {
                RebellionFixtures.set(data.getModel(), player, declared);
            }
        }
        return data;
    }

    private static Model load(final ModelPo po) {
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static Player dutch(final Model model) {
        return model.getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow();
    }

    private static PlayerPo dutchPo(final GameModelPo data) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow();
    }

    private static InterventionPo po(final Integer target, final Long bells,
            final Boolean earned) {
        final InterventionPo po = new InterventionPo();
        po.setTarget(target);
        po.setBells(bells);
        po.setEarned(earned);
        po.setDelivered(false);
        return po;
    }

    private static InterventionTarget intervention(final Player player) {
        return player.getIntervention().orElseThrow();
    }

    private static List<TurnEventIntervention> notices(final Model model,
            final Player player) {
        return model.getTurnEventsLocalizedMessages(player).stream()
                .filter(TurnEventIntervention.class::isInstance)
                .map(TurnEventIntervention.class::cast)
                .collect(Collectors.toList());
    }

    private static long earnedNotices(final Model model, final Player player) {
        return notices(model, player).stream()
                .filter(TurnEventIntervention::isEarned).count();
    }

    /** Bells the colonies of this nation would produce in its next update. */
    private static int plannedBells(final Model model, final Player player) {
        return model.getColonies(player).stream()
                .mapToInt(colony -> Math.max(0, colony.getGoodsStats()
                        .getStatsByType(GoodsType.BELL).getRowProduction()))
                .sum();
    }

    @ParameterizedTest
    @CsvSource({ "DISCOVERER,375", "EXPLORER,450", "CONQUISTADOR,500",
            "GOVERNOR,575", "VICEROY,650" })
    void targetIsFiveHundredTimesTheDifficultyFactorRoundedUp(
            final Difficulty difficulty, final int expected) {
        assertEquals(expected, InterventionTarget.targetFor(difficulty));
    }

    @Test
    void noTargetBeforeDeclarationOrBeforeTheFirstOwnerUpdate() {
        final Model prewar = load(data(false).getModel());
        assertTrue(dutch(prewar).getIntervention().isEmpty());
        final Model awaiting = load(data(true).getModel());
        assertTrue(dutch(awaiting).getIntervention().isEmpty());
        assertTrue(notices(awaiting, dutch(awaiting)).isEmpty());
        // Reloading the declared but unstarted state keeps it unstarted.
        final Model reloaded = load(awaiting.save());
        assertTrue(dutch(reloaded).getIntervention().isEmpty());
        assertTrue(notices(reloaded, dutch(reloaded)).isEmpty());
    }

    @Test
    void firstOwnerUpdateFixesTheTargetAndAnnouncesItOnce() {
        final Model model = load(data(true).getModel());
        final Player dutch = dutch(model);
        final int planned = plannedBells(model, dutch);
        assertTrue(dutch.startTurn());
        final InterventionTarget state = intervention(dutch);
        final int target = InterventionTarget.targetFor(model.getDifficulty());
        assertEquals(target, state.getTarget());
        // Only this update's bells are counted, and none is earned yet.
        assertEquals(planned, state.getBells());
        assertFalse(state.isEarned());
        assertEquals(1, notices(model, dutch).size());
        assertFalse(notices(model, dutch).get(0).isEarned());
        assertEquals(target, notices(model, dutch).get(0).getTarget());
        // A later owner update keeps the target and announces nothing again.
        assertTrue(dutch.startTurn());
        assertEquals(target, intervention(dutch).getTarget());
        assertEquals(1, notices(model, dutch).size());
    }

    @Test
    void belowAtAndAboveTheTargetEarnOnceWithExactOverflow() {
        final Model model = load(data(true).getModel());
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());
        final InterventionTarget state = intervention(dutch);
        final long toTarget = state.getTarget() - state.getBells();
        // One bell short of the target does not earn.
        dutch.creditProducedBells(Math.toIntExact(toTarget - 1));
        assertFalse(state.isEarned());
        assertEquals(0L, earnedNotices(model, dutch));
        // Exactly the target earns once, with nothing above it yet.
        dutch.creditProducedBells(1);
        assertTrue(state.isEarned());
        assertEquals(0L, state.getBellsBeyondTarget());
        assertEquals(1L, earnedNotices(model, dutch));
        // Bells above the target are the overflow and never earn again.
        dutch.creditProducedBells(5);
        assertTrue(state.isEarned());
        assertEquals(5L, state.getBellsBeyondTarget());
        assertEquals(1L, earnedNotices(model, dutch));
    }

    @Test
    void bellsOfSeveralColoniesCountOnceEachAndZeroIsValid() {
        final Model model = load(data(true).getModel());
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());
        final InterventionTarget state = intervention(dutch);
        final long before = state.getBells();
        dutch.creditProducedBells(3);
        dutch.creditProducedBells(0);
        dutch.creditProducedBells(4);
        assertEquals(before + 7, state.getBells());
        assertFalse(state.isEarned());
    }

    @Test
    void invalidCreditIsRejectedBeforeAnyChange() {
        final Model model = load(data(true).getModel());
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());
        final InterventionTarget state = intervention(dutch);
        final long before = state.getBells();
        assertThrows(IllegalArgumentException.class,
                () -> state.checkCredit(-1));
        assertThrows(IllegalStateException.class,
                () -> new InterventionTarget(po(10, Long.MAX_VALUE, true))
                        .checkCredit(1));
        assertEquals(before, state.getBells());
    }

    @Test
    void savedProgressSurvivesReloadWithoutNewAnnouncementOrCredit() {
        final Model model = load(data(true).getModel());
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());
        dutch.creditProducedBells(7);
        final InterventionTarget state = intervention(dutch);
        final Model restored = load(model.save());
        final Player restoredDutch = dutch(restored);
        final InterventionTarget copy = intervention(restoredDutch);
        assertEquals(state.getTarget(), copy.getTarget());
        assertEquals(state.getBells(), copy.getBells());
        assertEquals(state.isEarned(), copy.isEarned());
        // Loading credits nothing; the next owner update adds no announcement.
        assertEquals(1, notices(restored, restoredDutch).size());
        assertTrue(restoredDutch.startTurn());
        assertEquals(1, notices(restored, restoredDutch).size());
        assertEquals(state.getTarget(),
                intervention(restoredDutch).getTarget());
    }

    @Test
    void earnedOutcomeSurvivesReloadAndEarnsNoSecondTime() {
        final Model model = load(data(true).getModel());
        final Player dutch = dutch(model);
        assertTrue(dutch.startTurn());
        final InterventionTarget state = intervention(dutch);
        dutch.creditProducedBells(
                Math.toIntExact(state.getTarget() - state.getBells()));
        assertTrue(state.isEarned());
        final Model restored = load(model.save());
        final Player restoredDutch = dutch(restored);
        assertTrue(intervention(restoredDutch).isEarned());
        assertEquals(1L, earnedNotices(restored, restoredDutch));
        restoredDutch.creditProducedBells(9);
        assertEquals(1L, earnedNotices(restored, restoredDutch));
        assertEquals(9L, intervention(restoredDutch).getBellsBeyondTarget());
    }

    @Test
    void incoherentSavedTargetsAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new InterventionTarget(po(null, 0L, false)));
        assertThrows(IllegalArgumentException.class,
                () -> new InterventionTarget(po(0, 0L, false)));
        assertThrows(IllegalArgumentException.class,
                () -> new InterventionTarget(po(500, -1L, false)));
        assertThrows(IllegalArgumentException.class,
                () -> new InterventionTarget(po(500, 10L, true)));
        assertThrows(IllegalArgumentException.class,
                () -> new InterventionTarget(po(500, 500L, false)));
    }

    @Test
    void targetOnAnUndeclaredNationIsRejectedOnLoad() {
        final GameModelPo data = data(false);
        dutchPo(data).setIntervention(po(500, 0L, false));
        assertThrows(IllegalArgumentException.class,
                () -> load(data.getModel()));
    }
}
