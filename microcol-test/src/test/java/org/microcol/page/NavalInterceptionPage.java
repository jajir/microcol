package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.turnevent.TurnEventCoastalBattery;
import org.microcol.model.turnevent.TurnEventPatrolInterception;
import org.microcol.model.unit.Ship;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Explicit observations of a ship's patrol checks: its position, the movement
 * left for the turn, its cargo and the saved random stream that decides the
 * next check.
 */
public final class NavalInterceptionPage extends AbstractScreen {
    private static final int SHIP_ID = 0;

    public NavalInterceptionPage(TestContext context) {
        super(context);
    }

    public void verifyShipAt(final Location expected) {
        assertEquals(expected, getModel().getUnitById(SHIP_ID).getLocation());
    }

    /**
     * @param expected movement left for the turn, in thirds of a point
     */
    public void verifyMovementThirds(final int expected) {
        assertEquals(expected,
                ((Ship) getModel().getUnitById(SHIP_ID)).getMovementThirds());
    }

    public void verifyCargoIntact() {
        final Ship ship = (Ship) getModel().getUnitById(SHIP_ID);
        assertEquals(Goods.of(GoodsType.ORE, 95),
                ship.getCargo().getSlotByIndex(0).getGoods().orElseThrow());
    }

    /**
     * @param expected the saved random state that the next check will read
     */
    public void verifyCombatRandomState(final long expected) {
        assertEquals(expected,
                getModel().save().getCombatRandomState().longValue());
    }

    /**
     * An unseen patrol leaves no interception notice for the player's turn.
     */
    public void verifyNoPatrolNotice() {
        assertTrue(
                getModel()
                        .getTurnEventsLocalizedMessages(
                                getModel().getPlayerByName("Dutch"))
                        .stream().noneMatch(
                                TurnEventPatrolInterception.class::isInstance),
                "No patrol notice may be recorded");
    }

    /** A step that met no patrol opens no turn report. */
    public void verifyNoReportShown() {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(
                getNodeFinder().lookup("#closeButtonId").tryQuery().isPresent(),
                "No report may be shown");
    }

    /** A battery hit leaves the ship damaged and off the map until repair. */
    public void verifyShipDamagedOffMap() {
        final Ship ship = (Ship) getModel().getUnitById(SHIP_ID);
        assertTrue(ship.isDamaged(), "Ship must be damaged");
        assertFalse(ship.isAtPlaceLocation(), "Ship must leave the map");
    }

    /**
     * @param expected number of coastal battery notices the Dutch player has
     */
    public void verifyCoastalBatteryNoticeCount(final int expected) {
        assertEquals(expected, getModel()
                .getTurnEventsLocalizedMessages(
                        getModel().getPlayerByName("Dutch"))
                .stream().filter(TurnEventCoastalBattery.class::isInstance)
                .count());
    }
}
