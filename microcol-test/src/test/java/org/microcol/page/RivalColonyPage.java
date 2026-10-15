package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.unit.UnitWithCargo;

public final class RivalColonyPage extends AbstractScreen {
    public RivalColonyPage(final TestContext context) {
        super(context);
    }

    public void verifyStartingExpeditions(final String expectedHumanName,
            final Location expectedHumanShip, final Location expectedRivalShip,
            final int expectedFounderId, final int expectedRivalShipId,
            final boolean expectedShoreVisible) {
        final Player human = getModel().getPlayerByName("Dutch");
        final Player rival = getModel().getPlayerByName("France");
        final Unit founder = getModel().getUnitById(expectedFounderId);
        final UnitWithCargo ship = (UnitWithCargo) getModel()
                .getUnitById(expectedRivalShipId);
        assertSame(human, getModel().getCurrentPlayer());
        assertEquals(expectedHumanName, human.getDisplayName());
        assertTrue(rival.isComputer());
        assertFalse(rival.isKing());
        assertEquals(6, getModel().getAllUnits().size());
        assertTrue(getModel().getColonies().isEmpty());
        assertEquals(0, getModel().save().getNextColonyId());
        assertSame(rival, founder.getOwner());
        assertSame(rival, ship.getOwner());
        assertSame(ship, founder.getPlaceCargoSlot().getOwnerUnit());
        assertSame(founder,
                ship.getCargo().getSlotByIndex(0).getUnit().orElseThrow());
        assertEquals(expectedHumanShip,
                getModel().getUnitById(3).getLocation());
        assertEquals(expectedRivalShip, ship.getLocation());
        assertTrue(human.isVisible(expectedRivalShip));
        assertTrue(rival.isVisible(expectedRivalShip));
        assertEquals(expectedShoreVisible,
                rival.isVisible(Location.of(26, 15)));
        assertEquals(EuropeanRelationState.PEACE,
                getModel().getEuropeanRelation(human, rival));
    }

    public void verifyFoundedResident(final int expectedColonyId,
            final int expectedFounderId, final String expectedName,
            final Location expectedSite, final boolean expectedFoodField) {
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(expectedColonyId).orElseThrow();
        final Unit founder = getModel().getUnitById(expectedFounderId);
        assertEquals(expectedName, colony.getName());
        assertEquals(expectedSite, colony.getLocation());
        assertEquals(1, getModel().getColonies().size());
        assertEquals(expectedColonyId + 1, getModel().save().getNextColonyId());
        assertSame(founder.getOwner(), colony.getOwner());
        assertEquals(java.util.List.of(founder), colony.getUnitsInsideColony());
        assertEquals(expectedFoodField, colony.getColonyFields().stream()
                .anyMatch(field -> field.getUnit().orElse(null) == founder));
        assertTrue(colony.getColonyFields().stream()
                .filter(field -> field.getUnit().isPresent())
                .allMatch(field -> getModel().getMap()
                        .isValid(field.getLocation())));
    }

    public void verifyFoundingRejected(final int expectedFounderId,
            final Location expectedSite, final int expectedColonyCount,
            final int expectedNextId) {
        assertEquals(expectedColonyCount, getModel().getColonies().size());
        assertEquals(expectedNextId, getModel().save().getNextColonyId());
        assertEquals(expectedSite,
                getModel().getUnitById(expectedFounderId).getLocation());
        assertFalse(getNodeFinder().lookup("#colonyNameInput").tryQuery()
                .isPresent());
    }

    public void waitForHumanRound(final int round) throws Exception {
        org.testfx.util.WaitForAsyncUtils.waitFor(60,
                java.util.concurrent.TimeUnit.SECONDS,
                () -> getModel().getCalendar().getNumberOfPlayedTurns() == round
                        && getModel().getCurrentPlayer().isHuman());
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
    }

    public String captureHumanExpedition() {
        return new com.google.gson.Gson().toJson(getModel()
                .getPlayerByName("Dutch").getAllUnits().stream().map(Unit::save)
                .collect(java.util.stream.Collectors.toList()));
    }

    public void verifyHumanExpeditionUnchanged(final String expectedUnits,
            final int expectedGold) {
        assertEquals(expectedUnits, captureHumanExpedition());
        assertEquals(expectedGold,
                getModel().getPlayerByName("Dutch").getGold());
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
    }

    public void verifyRivalTravel(final int expectedRound,
            final Location expectedShip, final boolean expectedAboard,
            final Location expectedFounderSite) {
        assertEquals(expectedRound,
                getModel().getCalendar().getNumberOfPlayedTurns());
        assertEquals(6, getModel().getAllUnits().size());
        assertTrue(getModel().getColonies().isEmpty());
        final Unit founder = getModel().getUnitById(4);
        final Unit ship = getModel().getUnitById(5);
        assertSame(getModel().getPlayerByName("France"), founder.getOwner());
        assertSame(founder.getOwner(), ship.getOwner());
        assertEquals(expectedShip, ship.getLocation());
        assertEquals(expectedAboard, founder.isAtCargoSlot());
        if (expectedAboard) {
            assertSame(ship, founder.getPlaceCargoSlot().getOwnerUnit());
            assertSame(founder, ((UnitWithCargo) ship).getCargo()
                    .getSlotByIndex(0).getUnit().orElseThrow());
        } else {
            assertTrue(founder.isAtPlaceLocation());
            assertEquals(expectedFounderSite, founder.getLocation());
            assertEquals(0, founder.getActionPoints());
            assertTrue(((UnitWithCargo) ship).getCargo().getSlotByIndex(0)
                    .isEmpty());
        }
    }

    public void verifyRivalColonyViable(final int expectedRound,
            final int expectedColonyId, final Location expectedSite) {
        assertEquals(expectedRound,
                getModel().getCalendar().getNumberOfPlayedTurns());
        verifyFoundedResident(expectedColonyId, 4, "New Amsterdam",
                expectedSite, true);
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(expectedColonyId).orElseThrow();
        assertSame(getModel().getPlayerByName("France"), colony.getOwner());
        assertTrue(colony.getGoodsStats()
                .getStatsByType(org.microcol.model.GoodsType.CORN)
                .getNetProduction() >= 0);
        assertEquals(6, getModel().getAllUnits().size());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "A rival founding must leave the human on the game map");
        assertFalse(getNodeFinder().lookup("#colonyNameInput").tryQuery()
                .isPresent());
    }

}
