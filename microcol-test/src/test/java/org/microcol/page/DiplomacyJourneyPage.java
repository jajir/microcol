package org.microcol.page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.microcol.model.Colony;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.TradeRoute;
import org.microcol.model.TradeRouteStop;
import org.testfx.util.WaitForAsyncUtils;

/**
 * Independent observations of the connected diplomacy journey (R-0219): the
 * purses, the Dutch and French relation, who owns a colony and which routes and
 * lists still name it. Everything reads the model; nothing is changed here.
 */
public final class DiplomacyJourneyPage extends AbstractScreen {

    private static final String DUTCH = "Dutch";
    private static final String FRANCE = "France";

    public DiplomacyJourneyPage(final TestContext context) {
        super(context);
    }

    private Player player(final String name) {
        return getModel().getPlayerByName(name);
    }

    private Colony colony(final int colonyId) {
        return getModel().getColonyById(colonyId).orElseThrow();
    }

    /** Gold of the Dutch now; a getter, nothing is asserted here. */
    public int dutchGold() {
        return player(DUTCH).getGold();
    }

    /** Gold of France now; a getter, nothing is asserted here. */
    public int frenchGold() {
        return player(FRANCE).getGold();
    }

    /** Pieces of one goods in a colony now; a getter, no assertion. */
    public int colonyStock(final int colonyId, final GoodsType type) {
        return colony(colonyId).getWarehouse().getGoods(type).getAmount();
    }

    public void verifyDutchGold(final int expected) {
        assertEquals(expected, dutchGold());
    }

    public void verifyFrenchGold(final int expected) {
        assertEquals(expected, frenchGold());
    }

    public void verifyColonyStock(final int colonyId, final GoodsType type,
            final int expected) {
        assertEquals(expected, colonyStock(colonyId, type),
                type + " in colony " + colonyId);
    }

    /** Both directions of the Dutch and French pair show this relation. */
    public void verifyRelation(final EuropeanRelationState expected) {
        assertEquals(expected,
                getModel().getEuropeanRelation(player(DUTCH), player(FRANCE)));
        assertEquals(expected,
                getModel().getEuropeanRelation(player(FRANCE), player(DUTCH)));
    }

    /** Peace holds, so no attack on the other nation is a legal hostility. */
    public void verifyTreatyKept() {
        verifyRelation(EuropeanRelationState.PEACE);
        assertFalse(getModel().areHostile(player(DUTCH), player(FRANCE)));
    }

    public void verifyColonyOwner(final int colonyId, final String owner) {
        assertEquals(owner, colony(colonyId).getOwner().getName());
    }

    /** Residents are counted, so a worker taken or lost shows here. */
    public void verifyColonyResidents(final int colonyId, final int expected) {
        assertEquals(expected, colony(colonyId).getUnitsInsideColony().size());
    }

    /** Every resident of the colony belongs to this owner. */
    public void verifyResidentsBelongTo(final int colonyId,
            final String owner) {
        assertTrue(
                colony(colonyId).getUnitsInsideColony().stream().allMatch(
                        unit -> owner.equals(unit.getOwner().getName())),
                "Residents of colony " + colonyId + " belong to " + owner);
    }

    /** Whether the owner's own colony list shows this colony. */
    public void verifyColonyListed(final String owner, final int colonyId,
            final boolean listed) {
        assertEquals(listed,
                getModel().getColonies(player(owner)).stream()
                        .anyMatch(colony -> colony.getId() == colonyId),
                "Colony " + colonyId + " in the list of " + owner);
    }

    /** The route is stored for the owner and names exactly these colonies. */
    public void verifyRouteStops(final String owner, final String routeName,
            final int... colonyIds) {
        assertEquals(toList(colonyIds), stops(owner, routeName));
    }

    /**
     * The route of the former owner still names the colony, but the colony no
     * longer belongs to the route owner. The route operations then treat the
     * stop as lost; that behavior is exercised by
     * {@code TradeRouteNavigationTest.capturedStopIsLostForTheFormerOwnerRouteWithoutMovingTheWagon}.
     */
    public void verifyRouteStopLost(final String owner, final String routeName,
            final int colonyId, final String newOwner) {
        assertTrue(stops(owner, routeName).contains(colonyId),
                "Route " + routeName + " names colony " + colonyId);
        assertNotEquals(owner, colony(colonyId).getOwner().getName());
        assertEquals(newOwner, colony(colonyId).getOwner().getName());
    }

    private List<Integer> stops(final String owner, final String routeName) {
        final TradeRoute route = getModel().getTradeRoutes(player(owner))
                .stream().filter(stored -> routeName.equals(stored.getName()))
                .findFirst().orElseThrow();
        final List<Integer> ids = new ArrayList<>();
        for (final TradeRouteStop stop : route.getStops()) {
            ids.add(stop.getColonyId());
        }
        return ids;
    }

    private static List<Integer> toList(final int[] values) {
        final List<Integer> out = new ArrayList<>();
        for (final int value : values) {
            out.add(value);
        }
        return out;
    }

    /** The unit was destroyed in a battle and is no longer in the game. */
    public void verifyUnitDestroyed(final int unitId) {
        assertTrue(getModel().tryGetUnitById(unitId).isEmpty(),
                "Unit " + unitId + " still exists");
    }

    public void verifyUnitAt(final int unitId, final Location expected) {
        assertEquals(expected, getModel().getUnitById(unitId).getLocation());
    }

    public void verifyUnitOwner(final int unitId, final String owner) {
        assertEquals(owner,
                getModel().getUnitById(unitId).getOwner().getName());
    }

    /** The unit spent its movement, for instance in a battle. */
    public void verifyMovementSpent(final int unitId) {
        assertEquals(0, getModel().getUnitById(unitId).getActionPoints());
    }

    /**
     * The colony changed hands in a won battle: its owner, its workers and the
     * attacker, which stands on the colony square. The battle runs in the
     * background, so the check waits for it.
     */
    public void verifyColonyCaptured(final int colonyId, final String owner,
            final int residents, final int attackerId, final Location colonyAt)
            throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> owner.equals(colony(colonyId).getOwner().getName()));
        GamePage.of(getContext()).waitWhileMoving();
        verifyColonyResidents(colonyId, residents);
        verifyResidentsBelongTo(colonyId, owner);
        verifyUnitAt(attackerId, colonyAt);
    }

    /**
     * A peace settled once: the price moved from the Dutch to France, the pair
     * is at peace and no proposal is pending.
     */
    public void verifyPeaceSettled(final int dutchBefore,
            final int frenchBefore, final int price) {
        verifyDutchGold(dutchBefore - price);
        verifyFrenchGold(frenchBefore + price);
        verifyRelation(EuropeanRelationState.PEACE);
        assertTrue(getModel()
                .getPendingPeaceOffer(player(DUTCH), player(FRANCE)).isEmpty());
    }
}
