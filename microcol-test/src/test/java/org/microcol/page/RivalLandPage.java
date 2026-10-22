package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.Colony;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Unit;

public final class RivalLandPage extends AbstractScreen {
    /** The shared attacker win probability an attack needs. */
    private static final double MINIMUM_WIN_PROBABILITY = 0.6;
    private static final double EPSILON = 1e-9;

    public RivalLandPage(final TestContext context) {
        super(context);
    }

    public int readRound() {
        return getModel().getCalendar().getNumberOfPlayedTurns();
    }

    public void waitForHumanRound(final int round) throws Exception {
        org.testfx.util.WaitForAsyncUtils.waitFor(60,
                java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().getCalendar().getNumberOfPlayedTurns() == round
                        && getModel().getCurrentPlayer().isHuman());
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
    }

    /** Whether the rival armed unit stands on the threatened colony square. */
    public boolean isGarrisoned(final int rivalId, final Location colony) {
        final Unit rival = getModel().getUnitById(rivalId);
        return rival.isAtPlaceLocation() && rival.getLocation().equals(colony);
    }

    /**
     * Before the first French turn: France holds the colony, the Dutch human
     * stands two squares from it, and the two nations are at war.
     */
    public void verifyThreatReady(final int colonyId,
            final Location colonyLocation, final int rivalId,
            final Location rivalStart, final int humanId,
            final Location humanStart) {
        final Player human = getModel().getPlayerByName("Dutch");
        final Player rival = getModel().getPlayerByName("France");
        final Colony colony = getModel().getColonyById(colonyId).orElseThrow();
        assertEquals(colonyLocation, colony.getLocation());
        assertSame(rival, colony.getOwner());
        assertEquals(rivalStart, getModel().getUnitById(rivalId).getLocation());
        assertEquals(humanStart, getModel().getUnitById(humanId).getLocation());
        assertSame(human, getModel().getUnitById(humanId).getOwner());
        assertEquals(EuropeanRelationState.WAR,
                getModel().getEuropeanRelation(human, rival));
        assertSame(human, getModel().getCurrentPlayer());
    }

    /**
     * After each French turn: the game runs, control is back with the human,
     * and the armed unit that was threatened is never fighting the human. Each
     * round is described in the message so a stalled turn is explained.
     */
    public void verifyThreatTurn(final String leg, final int rivalId,
            final int humanId) {
        final String situation = leg + ": rival at " + describeRival(rivalId)
                + ", human at " + getModel().getUnitById(humanId).getLocation();
        assertFalse(getModel().isGameFinished(),
                "The game ended before the threat did; " + situation);
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each French turn; "
                        + situation);
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getUnitById(humanId).getOwner(), situation);
    }

    /** The rival armed unit reached and stood on the colony square in time. */
    public void verifyGarrisonWithinRounds(final int rivalId,
            final Location colony, final int rounds) {
        assertTrue(isGarrisoned(rivalId, colony),
                "The armed colonist did not reach the colony within " + rounds
                        + " rounds; rival at " + describeRival(rivalId));
    }

    /** Where the rival stands, and what it is doing, for reports. */
    public String describeRival(final int rivalId) {
        final Unit rival = getModel().getUnitById(rivalId);
        if (!rival.isAtPlaceLocation()) {
            return "not on the map";
        }
        return rival.getLocation() + " with action "
                + rival.getAction().getType() + ", movement "
                + rival.getMovementThirds() + " thirds";
    }

    /** Save and Continue keeps the garrison exactly as it was. */
    public void verifyGarrisonUnchanged(final String expected,
            final int rivalId) {
        assertEquals(expected, describeRival(rivalId));
    }

    /**
     * The owned colony keeps its owner and its armed garrison, and the human
     * still holds the game's colony.
     */
    public void verifyOwnedColonyDefended(final int colonyId,
            final Location colonyLocation, final int rivalId) {
        final Colony colony = getModel().getColonyById(colonyId).orElseThrow();
        assertEquals(colonyLocation, colony.getLocation());
        assertSame(getModel().getPlayerByName("France"), colony.getOwner());
        assertTrue(isGarrisoned(rivalId, colonyLocation),
                "The armed colonist left the colony; rival at "
                        + describeRival(rivalId));
        assertFalse(getModel().getColonies(getModel().getPlayerByName("Dutch"))
                .isEmpty(), "The human lost its colony");
    }

    /** The saved combat draw state; it changes only when a battle is fought. */
    public String readCombatState() {
        return String.valueOf(getModel().save().getCombatRandomState());
    }

    /** Whether a battle has drawn from the combat state since the read. */
    public boolean isBattleFought(final String before) {
        return !readCombatState().equals(before);
    }

    /**
     * Before the first French turn: the mounted French colonist and the Dutch
     * colonist stand next to each other at war, and the shared attacker win
     * probability of that attack is exactly the 60 percent threshold.
     */
    public void verifyOffenceReady(final int rivalId, final int humanId) {
        final Unit rival = getModel().getUnitById(rivalId);
        final Unit human = getModel().getUnitById(humanId);
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
        assertSame(getModel().getPlayerByName("France"), rival.getOwner());
        assertEquals(EuropeanRelationState.WAR, getModel()
                .getEuropeanRelation(human.getOwner(), rival.getOwner()));
        assertTrue(rival.getLocation().isNeighbor(human.getLocation()));
        assertEquals(MINIMUM_WIN_PROBABILITY,
                shareWinProbability(rival, human.getLocation()), EPSILON);
    }

    private double shareWinProbability(final Unit attacker,
            final Location target) {
        // Quote the rival's coming turn in a detached copy, never mutate the UI
        // game.
        final org.microcol.model.store.ModelPo saved = getModel().save();
        saved.getGameManager().setCurrentPlayer(attacker.getOwner().getName());
        final org.microcol.model.Model copy = org.microcol.model.Model
                .make(saved, org.microcol.model.campaign.MissionImpl
                        .getPredefinedGameOverEvaluators());
        return copy.getUnitById(attacker.getId()).prepareAttack(target, false)
                .flatMap(order -> order.getCombatStrengths())
                .map(strengths -> strengths.getAttackerWinProbability())
                .orElse(-1.0);
    }

    /**
     * After each French turn: the game runs and control is back with the human.
     */
    public void verifyOffenceTurn(final String leg, final int rivalId,
            final int humanId) {
        final String situation = leg + ": rival " + describeRival(rivalId)
                + ", human " + describeRival(humanId);
        assertFalse(getModel().isGameFinished(),
                "The game ended before the attack did; " + situation);
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer(), situation);
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each French turn; "
                        + situation);
    }

    /**
     * The attack came within the bound and ended with the expected fate of both
     * colonists: each still lives on its own square under its owner, the
     * attacker spent its movement, and each holds muskets only as expected. A
     * rival that only moved, or a battle with another result, fails here.
     */
    public void verifyOffenceBattle(final int rivalId,
            final Location expectedRivalSquare, final int humanId,
            final Location expectedHumanSquare, final boolean fought,
            final int rounds, final boolean expectedHumanArmed,
            final boolean expectedRivalArmed) {
        final String situation = "rival " + describeRival(rivalId) + ", human "
                + describeRival(humanId);
        assertTrue(fought,
                "No attack within " + rounds + " French rounds; " + situation);
        final Unit rival = getModel().tryGetUnitById(rivalId).orElse(null);
        final Unit human = getModel().tryGetUnitById(humanId).orElse(null);
        assertNotNull(rival, "The attacker was destroyed; " + situation);
        assertNotNull(human, "The defender was destroyed; " + situation);
        assertSame(getModel().getPlayerByName("France"), rival.getOwner());
        assertSame(getModel().getPlayerByName("Dutch"), human.getOwner());
        assertEquals(expectedRivalSquare, rival.getLocation(), situation);
        assertEquals(expectedHumanSquare, human.getLocation(), situation);
        assertEquals(0, rival.getActionPoints(), situation);
        assertEquals(expectedHumanArmed,
                ((org.microcol.model.unit.UnitFreeColonist) human)
                        .isHoldingGuns(),
                "Defender muskets after the battle; " + situation);
        assertEquals(expectedRivalArmed,
                ((org.microcol.model.unit.UnitFreeColonist) rival)
                        .isHoldingGuns(),
                "Attacker muskets after the battle; " + situation);
    }

    /** Start of the no-target scenario: the armed colonist stands alone. */
    public void verifyQuietStart(final int rivalId, final Location rivalStart,
            final Location humanStart, final int humanId) {
        assertEquals(rivalStart, getModel().getUnitById(rivalId).getLocation());
        assertEquals(humanStart, getModel().getUnitById(humanId).getLocation());
        assertEquals(EuropeanRelationState.WAR,
                getModel().getEuropeanRelation(
                        getModel().getPlayerByName("Dutch"),
                        getModel().getPlayerByName("France")));
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
    }

    /**
     * Without a visible target the armed colonist stays on its square, no
     * battle draws, the game keeps running and control returns to the human.
     */
    public void verifyQuietTurn(final String leg, final int rivalId,
            final int humanId, final Location rivalStart,
            final String combatBefore) {
        final String situation = leg + ": rival " + describeRival(rivalId);
        assertFalse(getModel().isGameFinished(),
                "The game ended before the quiet turn did; " + situation);
        assertEquals(rivalStart, getModel().getUnitById(rivalId).getLocation(),
                situation);
        assertEquals(combatBefore, readCombatState(), situation);
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each French turn; "
                        + situation);
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer(), situation);
    }
}
