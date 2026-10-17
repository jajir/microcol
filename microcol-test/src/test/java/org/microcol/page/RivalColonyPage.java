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
        assertFalse(human.isVisible(expectedRivalShip));
        assertTrue(rival.isVisible(expectedRivalShip));
        assertEquals(expectedShoreVisible, rival.isVisible(Location.of(21, 2)));
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
        assertTrue(getModel().getMap().getTerrainTypeAt(expectedSite).isLand());
        assertTrue(expectedSite.getNeighbors().stream()
                .filter(getModel().getMap()::isValid).anyMatch(at -> getModel()
                        .getMap().getTerrainTypeAt(at).isSea()));
        assertTrue(colony.getGoodsStats()
                .getStatsByType(org.microcol.model.GoodsType.CORN)
                .getNetProduction() >= 0);
        assertEquals(6, getModel().getAllUnits().size());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "A rival founding must leave the human on the game map");
        assertFalse(getNodeFinder().lookup("#colonyNameInput").tryQuery()
                .isPresent());
    }

    public int readRivalFood(final int colonyId) {
        return getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                .getGoods(org.microcol.model.GoodsType.CORN).getAmount();
    }

    public void verifyRivalColonyKeepsProducing(final int expectedRound,
            final int expectedColonyId, final Location expectedSite,
            final int expectedMinimumFood) {
        assertEquals(expectedRound,
                getModel().getCalendar().getNumberOfPlayedTurns());
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(expectedColonyId).orElseThrow();
        assertEquals(1, getModel().getColonies().size());
        assertEquals(expectedSite, colony.getLocation());
        assertSame(getModel().getPlayerByName("France"), colony.getOwner());
        assertEquals(java.util.List.of(getModel().getUnitById(4)),
                colony.getUnitsInsideColony());
        assertTrue(readRivalFood(expectedColonyId) >= expectedMinimumFood);
        assertEquals(0, colony.getWarehouse()
                .getGoods(org.microcol.model.GoodsType.LUMBER).getAmount());
        assertTrue(colony.getGoodsStats()
                .getStatsByType(org.microcol.model.GoodsType.CORN)
                .getNetProduction() >= 0);
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each rival turn");
    }

    public int readRound() {
        return getModel().getCalendar().getNumberOfPlayedTurns();
    }

    public int readRivalLumber(final int colonyId) {
        return getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                .getGoods(org.microcol.model.GoodsType.LUMBER).getAmount();
    }

    public void verifyScopedColonyWithIdleWorkers(final int expectedColonyId,
            final Location expectedSite, final int expectedFounderId,
            final int expectedFirstSpareId, final int expectedSpareCount) {
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(expectedColonyId).orElseThrow();
        assertEquals(expectedSite, colony.getLocation());
        assertSame(getModel().getPlayerByName("France"), colony.getOwner());
        assertEquals(
                java.util.List.of(getModel().getUnitById(expectedFounderId)),
                colony.getUnitsInsideColony());
        for (int i = 0; i < expectedSpareCount; i++) {
            final Unit spare = getModel().getUnitById(expectedFirstSpareId + i);
            assertSame(colony.getOwner(), spare.getOwner());
            assertTrue(spare.isAtPlaceLocation());
            assertEquals(expectedSite, spare.getLocation());
        }
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
    }

    public void verifyRivalWorkforceProducing(final int expectedRound,
            final int expectedColonyId, final int expectedFirstWorkerId,
            final int expectedWorkerCount, final int expectedMinimumFood,
            final int expectedMinimumLumber) {
        assertEquals(expectedRound, readRound());
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(expectedColonyId).orElseThrow();
        final java.util.List<Unit> residents = colony.getUnitsInsideColony();
        assertEquals(expectedWorkerCount, residents.size());
        long food = 0;
        long lumber = 0;
        for (final Unit worker : residents) {
            assertSame(colony.getOwner(), worker.getOwner());
            assertTrue(worker.getId() >= expectedFirstWorkerId);
            assertTrue(worker.isAtPlaceColonyField());
            final org.microcol.model.GoodsType goods = worker
                    .getPlaceColonyField().getProducedGoodsType();
            food += goods == org.microcol.model.GoodsType.CORN ? 1 : 0;
            lumber += goods == org.microcol.model.GoodsType.LUMBER ? 1 : 0;
        }
        assertTrue(food >= 1, "Food comes first");
        assertTrue(lumber >= 1, "Spare workers cut lumber");
        assertTrue(readRivalFood(expectedColonyId) >= expectedMinimumFood);
        assertTrue(readRivalLumber(expectedColonyId) >= expectedMinimumLumber);
        assertTrue(colony.getGoodsStats()
                .getStatsByType(org.microcol.model.GoodsType.CORN)
                .getNetProduction() >= 0);
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each rival turn");
    }

    public void verifyRivalColonyUndiscovered(final Location expectedSite) {
        final Player human = getModel().getPlayerByName("Dutch");
        assertFalse(human.isVisible(expectedSite));
        assertFalse(human.isVisible(getModel().getUnitById(5).getLocation()));
    }

}
