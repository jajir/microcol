package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Unit;

/**
 * Real-screen reads of the rival naval policy (R-0171). The French warship and
 * the Dutch carrier are checked after real end-turns; every assertion lives in
 * a verify method, while the reads only return values.
 */
public final class RivalNavalPage extends AbstractScreen {
    /** Patrol keeps within this many squares of the owned coastal port. */
    private static final int PATROL_RADIUS = 5;

    public RivalNavalPage(final TestContext context) {
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

    /** The saved combat draw state; it changes only when a battle is fought. */
    public String readCombatState() {
        return String.valueOf(getModel().save().getCombatRandomState());
    }

    /** Whether a battle has drawn from the combat state since the read. */
    public boolean isBattleFought(final String before) {
        return !readCombatState().equals(before);
    }

    /** Whether the unit is no longer on the map after its battle. */
    public boolean isRemoved(final int unitId) {
        return getModel().tryGetUnitById(unitId).isEmpty();
    }

    /** Where a unit stands and what it is doing, for reports. */
    public String describeUnit(final int unitId) {
        final Unit unit = getModel().tryGetUnitById(unitId).orElse(null);
        if (unit == null) {
            return "removed";
        }
        if (!unit.isAtPlaceLocation()) {
            return "not on the map";
        }
        return unit.getLocation() + " with action " + unit.getAction().getType()
                + ", movement " + unit.getMovementThirds() + " thirds";
    }

    private Player france() {
        return getModel().getPlayerByName("France");
    }

    private Player dutch() {
        return getModel().getPlayerByName("Dutch");
    }

    /**
     * Before the first French turn: the two nations are at war, the warship
     * stands on its square and the Dutch carrier is in its present sight.
     */
    public void verifyEncounterReady(final int warshipId,
            final Location warshipStart, final int carrierId,
            final Location carrierStart) {
        final Unit warship = getModel().getUnitById(warshipId);
        final Unit carrier = getModel().getUnitById(carrierId);
        assertEquals(warshipStart, warship.getLocation());
        assertEquals(carrierStart, carrier.getLocation());
        assertSame(france(), warship.getOwner());
        assertSame(dutch(), carrier.getOwner());
        assertEquals(EuropeanRelationState.WAR,
                getModel().getEuropeanRelation(dutch(), france()));
        assertTrue(france().isCurrentlyVisible(carrier.getLocation()),
                "The carrier must be in the warship's sight");
        assertSame(dutch(), getModel().getCurrentPlayer());
    }

    /**
     * After each French turn: the game runs and control is back with the human,
     * and the visible outcome is described in the message for a stalled turn.
     */
    public void verifyEncounterTurn(final String leg, final int warshipId,
            final int carrierId) {
        final String situation = leg + ": warship " + describeUnit(warshipId)
                + ", carrier " + describeUnit(carrierId);
        assertFalse(getModel().isGameFinished(),
                "The game ended before the encounter did; " + situation);
        assertSame(dutch(), getModel().getCurrentPlayer(), situation);
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each French turn; "
                        + situation);
    }

    /** Where a ship stands now, for a scenario to compare across rounds. */
    public Location readLocation(final int unitId) {
        return getModel().getUnitById(unitId).getLocation();
    }

    /**
     * Steps of the shortest route the owner knows from the ship to a sea square
     * beside the target, zero when it already stands beside it, -1 when no
     * square can be reached. It is the model's own route and changes nothing.
     */
    public int readApproachSteps(final int shipId, final Location target) {
        final Unit ship = getModel().getUnitById(shipId);
        int best = -1;
        for (final Location square : target.getNeighbors()) {
            int steps = -1;
            if (square.equals(ship.getLocation())) {
                steps = 0;
            } else if (getModel().getMap().isValid(square)
                    && ship.isPossibleToMoveAt(square)) {
                steps = ship.getKnownPath(square, false).map(List::size)
                        .orElse(-1);
            }
            if (steps >= 0 && (best < 0 || steps < best)) {
                best = steps;
            }
        }
        return best;
    }

    /**
     * Before the first French turn: reaching a square beside the walled carrier
     * costs at least a whole owner turn of movement (one point per step), so
     * the attack cannot happen before the second turn.
     */
    public void verifyApproachNeedsAWholeTurn(final int warshipId,
            final int steps) {
        final int speed = getModel().getUnitById(warshipId).getSpeed();
        assertTrue(steps >= speed,
                "The approach is " + steps + " steps but the turn allows "
                        + speed
                        + "; the encounter would not need a second turn");
    }

    /**
     * After the first French turn with no battle yet: the model's route to a
     * square beside the carrier is shorter than before, so the warship has
     * really pursued it, and the carrier is still afloat.
     */
    public void verifyFirstTurnApproach(final String leg, final int warshipId,
            final int carrierId, final Location carrierSquare,
            final int stepsBefore) {
        final String situation = leg + ": warship " + describeUnit(warshipId)
                + ", carrier " + describeUnit(carrierId);
        assertFalse(isRemoved(carrierId),
                "The carrier sank before the warship could reach it; "
                        + situation);
        final int stepsAfter = readApproachSteps(warshipId, carrierSquare);
        assertTrue(stepsAfter >= 0 && stepsAfter < stepsBefore,
                "The warship did not close in: " + stepsBefore + " steps, then "
                        + stepsAfter + "; " + situation);
    }

    /**
     * The warship sought the carrier and sank it within the bound. The sunk
     * carrier is removed from the map, the warship lives under France with its
     * movement spent, and the battle draw came from the saved state.
     */
    public void verifyEncounterBattle(final int warshipId, final int carrierId,
            final boolean fought, final int rounds) {
        final String situation = "warship " + describeUnit(warshipId)
                + ", carrier " + describeUnit(carrierId);
        assertTrue(fought, "No commerce encounter within " + rounds
                + " French rounds; " + situation);
        assertTrue(isRemoved(carrierId),
                "The Dutch carrier was not sunk; " + situation);
        final Unit warship = getModel().tryGetUnitById(warshipId).orElse(null);
        assertNotNull(warship, "The warship was lost; " + situation);
        assertSame(france(), warship.getOwner(), situation);
        assertEquals(0, warship.getActionPoints(), situation);
    }

    /**
     * The fight came on a later turn than the first, since the approach to the
     * walled carrier took the whole first turn.
     */
    public void verifyFoughtAfterApproach(final int fightTurn) {
        assertTrue(fightTurn >= 1, "The fight came on French turn index "
                + fightTurn + ", before the approach could be finished");
    }

    /** The peaceful start: the nations keep the peace and nothing has moved. */
    public void verifyPeaceReady(final int warshipId,
            final Location warshipStart, final int carrierId,
            final Location carrierStart) {
        assertEquals(warshipStart,
                getModel().getUnitById(warshipId).getLocation());
        assertEquals(carrierStart,
                getModel().getUnitById(carrierId).getLocation());
        assertEquals(EuropeanRelationState.PEACE,
                getModel().getEuropeanRelation(dutch(), france()));
        assertSame(dutch(), getModel().getCurrentPlayer());
    }

    /**
     * Under peace the warship neither fights nor declares war: the carrier
     * keeps its square and the draw state is unchanged after the turn.
     */
    public void verifyPeaceTurn(final String leg, final int warshipId,
            final int carrierId, final Location carrierStart,
            final String combatBefore) {
        final String situation = leg + ": warship " + describeUnit(warshipId)
                + ", carrier " + describeUnit(carrierId);
        assertFalse(getModel().isGameFinished(),
                "The game ended before the peaceful turn did; " + situation);
        assertEquals(combatBefore, readCombatState(), situation);
        assertEquals(carrierStart,
                getModel().getUnitById(carrierId).getLocation(), situation);
        assertEquals(EuropeanRelationState.PEACE,
                getModel().getEuropeanRelation(dutch(), france()), situation);
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each French turn; "
                        + situation);
    }

    /**
     * Before the first French turn with no carrier: the nations are at war and
     * the warship stands on its square, beside its owned coastal port.
     */
    public void verifyPatrolReady(final int warshipId,
            final Location warshipStart) {
        assertEquals(warshipStart,
                getModel().getUnitById(warshipId).getLocation());
        assertEquals(EuropeanRelationState.WAR,
                getModel().getEuropeanRelation(dutch(), france()));
        assertSame(dutch(), getModel().getCurrentPlayer());
    }

    /**
     * The warship with no visible target stays on known sea within five squares
     * of Quebec, no battle is drawn and control returns to the human.
     */
    public void verifyPatrolTurn(final String leg, final int warshipId,
            final Location port, final String combatBefore) {
        final String situation = leg + ": warship " + describeUnit(warshipId);
        assertFalse(getModel().isGameFinished(),
                "The game ended before the patrol did; " + situation);
        final Unit warship = getModel().getUnitById(warshipId);
        assertTrue(warship.isAtPlaceLocation(), situation);
        assertTrue(
                warship.getLocation()
                        .getDistanceChebyshev(port) <= PATROL_RADIUS,
                "The warship left the patrol band; " + situation);
        assertTrue(getModel().getMap().getTerrainTypeAt(warship.getLocation())
                .isSea(), "The warship left the sea; " + situation);
        assertEquals(combatBefore, readCombatState(), situation);
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each French turn; "
                        + situation);
    }

    /** The patrol ship changed its square in at least one bounded round. */
    public void verifyPatrolled(final boolean moved) {
        assertTrue(moved, "The warship never left its square while patrolling");
    }

    /** Save and Continue keeps the battle as it was, with no second one. */
    public void verifyContinueUnchanged(final String combatBefore,
            final int carrierId) {
        assertEquals(combatBefore, readCombatState());
        assertTrue(isRemoved(carrierId),
                "The sunk carrier came back after Continue");
    }
}
