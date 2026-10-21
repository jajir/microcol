package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.microcol.model.Location;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.NativeNation;
import org.microcol.model.Player;
import org.microcol.model.UnitType;
import org.microcol.model.event.RoundStartedEvent;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.unit.UnitNativeBrave;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Observes native braves and the native turn callbacks of a real round. It only
 * reads the live model; the player's actions go through the ordinary controls
 * of the other pages.
 */
public final class NativeTurnsPage extends AbstractScreen {

    private static final int FULL_FOOT_MOVEMENT_THIRDS = 3;

    private final Map<String, Integer> turnStarts = new ConcurrentHashMap<>();
    private final AtomicInteger roundStarts = new AtomicInteger();

    public NativeTurnsPage(final TestContext context) {
        super(context);
    }

    /** Count every turn start and round start from now on. */
    public void observeCallbacks() {
        turnStarts.clear();
        roundStarts.set(0);
        getRobot().interact(
                () -> getModel().addListener(new ModelListenerAdapter() {
                    @Override
                    public void onTurnStarted(final TurnStartedEvent event) {
                        turnStarts.merge(event.getPlayer().getName(), 1,
                                Integer::sum);
                    }

                    @Override
                    public void onRoundStarted(final RoundStartedEvent event) {
                        roundStarts.incrementAndGet();
                    }
                }));
    }

    private Player dutch() {
        return getModel().getPlayerByName("Dutch");
    }

    /** A wrong unit type fails here with a class cast, not with a verdict. */
    private UnitNativeBrave brave(final int unitId) {
        return (UnitNativeBrave) getModel().getUnitById(unitId);
    }

    /**
     * Each named native nation received exactly one callback of the finished
     * round, the human's own turn started once, and the round began once.
     */
    public void verifyNativeCallbacksOncePerRound(final String... nations)
            throws Exception {
        // Turn-started callbacks arrive on their own threads; wait for every
        // expected one before judging the exact counts.
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> getModel()
                .isGameRunning() && getModel().getCurrentPlayer().isHuman()
                && turnStarts.containsKey("Dutch")
                && Arrays.stream(nations).allMatch(turnStarts::containsKey));
        WaitForAsyncUtils.waitForFxEvents();
        for (final String nation : nations) {
            assertEquals(Integer.valueOf(1), turnStarts.get(nation),
                    nation + " must receive exactly one turn callback");
        }
        assertEquals(Integer.valueOf(1), turnStarts.get("Dutch"),
                "The human turn must start once");
        assertEquals(1, roundStarts.get(), "The round must start once");
    }

    /** The unit is a real brave with its own nation and explicit home. */
    public void verifyBraveIdentity(final int unitId, final NativeNation nation,
            final Integer homeSettlementId) {
        final UnitNativeBrave brave = brave(unitId);
        assertEquals(UnitType.BRAVE, brave.getType());
        assertTrue(brave.getOwner().isNative());
        assertEquals(nation.name(), brave.getOwner().getName());
        assertEquals(nation, brave.getNation());
        assertEquals(Optional.ofNullable(homeSettlementId),
                brave.getHomeSettlementId());
    }

    public void verifyBraveCount(final int expected) {
        assertEquals(expected, getModel().getAllUnits().stream()
                .filter(unit -> unit instanceof UnitNativeBrave).count(),
                "Braves must not be duplicated or lost");
    }

    /** The brave stands on the given square. */
    public void verifyBraveLocation(final int unitId, final Location expected) {
        assertEquals(expected, brave(unitId).getLocation());
    }

    /**
     * The native turn prepared the brave once, giving full foot movement, and
     * the brave spent none of it.
     */
    public void verifyBravePrepared(final int unitId) {
        assertEquals(FULL_FOOT_MOVEMENT_THIRDS,
                brave(unitId).getMovementThirds());
    }

    /**
     * The native turn prepared the brave and it spent the whole allowance on
     * one home-patrol step: it stands on the expected square with no movement
     * left. A brave saved without movement and never prepared cannot have
     * moved, and every land step costs at least the whole foot allowance.
     */
    public void verifyBravePatrolledTo(final int unitId,
            final Location expected) {
        assertEquals(expected, brave(unitId).getLocation());
        assertEquals(0, brave(unitId).getMovementThirds());
    }

    /** Whether the Dutch currently see this brave on the given square. */
    public void verifyBraveSeen(final Location where, final int unitId,
            final boolean expected) {
        assertEquals(expected,
                dutch().getObservedUnitsAt(where).stream()
                        .anyMatch(unit -> unit.getId() == unitId),
                () -> "Brave " + unitId + " seen at " + where);
    }

    /** Whether the Dutch have learned the brave's home settlement. */
    public void verifyBraveHomeKnown(final int unitId, final boolean expected) {
        assertEquals(expected,
                getModel().getNativeBraveHomeKnownTo(dutch(), brave(unitId))
                        .isPresent(),
                () -> "Home of brave " + unitId + " known");
    }

}
