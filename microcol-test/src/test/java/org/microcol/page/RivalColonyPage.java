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

    /**
     * Lumber carried in the holds of a rival ship, read from the live model.
     */
    public int readRivalHoldLumber(final int shipId) {
        return ((UnitWithCargo) getModel().getUnitById(shipId)).getCargo()
                .getSlots().stream().flatMap(slot -> slot.getGoods().stream())
                .filter(goods -> goods.getType()
                        .equals(org.microcol.model.GoodsType.LUMBER))
                .mapToInt(org.microcol.model.Goods::getAmount).sum();
    }

    public void verifyRivalShipmentReady(final int expectedColonyId,
            final Location expectedSite, final int expectedShipId,
            final int expectedLumber) {
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(expectedColonyId).orElseThrow();
        final Unit ship = getModel().getUnitById(expectedShipId);
        assertSame(getModel().getPlayerByName("France"), colony.getOwner());
        assertEquals(expectedSite, colony.getLocation());
        assertEquals(expectedSite, ship.getLocation());
        assertSame(getModel().getPlayerByName("France"), ship.getOwner());
        assertEquals(expectedLumber, readRivalLumber(expectedColonyId));
        assertEquals(0, readRivalHoldLumber(expectedShipId));
        assertTrue(getModel().getPlayerByName("France").getRivalTradeObjective()
                .isEmpty());
    }

    /**
     * After the rival's turn the surplus left the colony in the ship, the
     * reserve stayed, and the objective names the same colony and ship.
     */
    public void verifyRivalShipmentLoaded(final int expectedRound,
            final int expectedColonyId, final int expectedShipId,
            final int expectedWarehouseLumber, final int expectedHoldLumber) {
        assertEquals(expectedRound, readRound());
        assertEquals(expectedWarehouseLumber,
                readRivalLumber(expectedColonyId));
        assertEquals(expectedHoldLumber, readRivalHoldLumber(expectedShipId));
        assertEquals(expectedColonyId, getModel().getPlayerByName("France")
                .getRivalTradeObjective().orElseThrow().getColonyId());
        assertEquals(expectedShipId, getModel().getPlayerByName("France")
                .getRivalTradeObjective().orElseThrow().getCarrierId());
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each rival turn");
    }

    /** Tools carried in the holds of a rival ship, read from the live model. */
    public int readRivalHoldTools(final int shipId) {
        return ((UnitWithCargo) getModel().getUnitById(shipId)).getCargo()
                .getSlots().stream().flatMap(slot -> slot.getGoods().stream())
                .filter(goods -> goods.getType()
                        .equals(org.microcol.model.GoodsType.TOOLS))
                .mapToInt(org.microcol.model.Goods::getAmount).sum();
    }

    /**
     * One France treasury event with the prices in force at that moment. The
     * model fires the event before it moves the market, so these are exactly
     * the prices the transaction was settled at.
     */
    private static final class LedgerEntry {
        private final int oldGold;
        private final int newGold;
        private final int lumberSellPrice;
        private final int toolBuyPrice;
        private final int taxPercentage;

        private LedgerEntry(final int oldGold, final int newGold,
                final int lumberSellPrice, final int toolBuyPrice,
                final int taxPercentage) {
            this.oldGold = oldGold;
            this.newGold = newGold;
            this.lumberSellPrice = lumberSellPrice;
            this.toolBuyPrice = toolBuyPrice;
            this.taxPercentage = taxPercentage;
        }

        @Override
        public String toString() {
            return oldGold + "->" + newGold + " (lumber sells at "
                    + lumberSellPrice + ", tools cost " + toolBuyPrice
                    + ", tax " + taxPercentage + "%)";
        }
    }

    /** Every France treasury event seen since the first observation. */
    private final java.util.List<LedgerEntry> ledger = new java.util.concurrent.CopyOnWriteArrayList<>();

    private volatile int ledgerStartGold = -1;

    /**
     * Start recording France's treasury events of the current model. The first
     * call also remembers the treasury it started from. A Continue creates a
     * new model, so the scenario calls this again after every Continue; the
     * recorded events are kept. It only listens and changes nothing.
     */
    public void observeRivalTreasury() {
        final org.microcol.model.Model model = getModel();
        final Player rival = model.getPlayerByName("France");
        if (ledgerStartGold < 0) {
            ledgerStartGold = rival.getGold();
        }
        model.addListener(new org.microcol.model.ModelListenerAdapter() {
            @Override
            public void onGoldWasChanged(
                    final org.microcol.model.event.GoldWasChangedEvent event) {
                if (event.getPlayer() != rival) {
                    return;
                }
                final org.microcol.model.EuropeanNation nation = rival
                        .getEuropeanNation();
                final org.microcol.model.Europe europe = model.getEurope();
                ledger.add(new LedgerEntry(event.getOldValue(),
                        event.getNewValue(),
                        europe.getGoodsTradeForType(
                                org.microcol.model.GoodsType.LUMBER, nation)
                                .getSellPrice(),
                        europe.getGoodsTradeForType(
                                org.microcol.model.GoodsType.TOOLS, nation)
                                .getBuyPrice(),
                        model.getKingsTaxForPlayer(rival)));
            }
        });
    }

    /**
     * The game is still running for the journey: it did not end, the rival
     * still follows its objective and the human still holds a colony. The
     * message names the leg, round, stage and ship place, so a stalled or ended
     * journey is explained instead of timing out in a turn wait.
     */
    public void verifyRivalJourneyRunning(final String leg, final int shipId) {
        final org.microcol.model.Model model = getModel();
        final Player rival = model.getPlayerByName("France");
        final String situation = leg + ": round " + readRound() + ", stage "
                + rival.getRivalTradeObjective()
                        .map(objective -> objective.getStage().name())
                        .orElse("none")
                + ", ship "
                + model.tryGetUnitById(shipId)
                        .map(unit -> String.valueOf(unit.getPlace()))
                        .orElse("gone")
                + ", France gold " + rival.getGold() + ", treasury events "
                + ledger;
        assertFalse(model.isGameFinished(),
                "The game ended before the journey did; " + situation);
        assertTrue(rival.getRivalTradeObjective().isPresent(),
                "The objective was retired; " + situation);
        assertFalse(model.getColonies(model.getPlayerByName("Dutch")).isEmpty(),
                "The human lost the colony that keeps the game going; "
                        + situation);
    }

    /**
     * France's treasury equals the start plus its recorded events, and the
     * expected number of events happened. Any other treasury movement fails
     * here with the events listed.
     */
    public void verifyRivalTreasury(final int expectedEvents) {
        final int gold = getModel().getPlayerByName("France").getGold();
        int accounted = ledgerStartGold;
        for (final LedgerEntry entry : ledger) {
            accounted += entry.newGold - entry.oldGold;
        }
        assertEquals(expectedEvents, ledger.size(),
                "Treasury events: " + ledger);
        assertEquals(accounted, gold, "Treasury events: " + ledger);
    }

    /** Whether the carried lumber has been sold and the leg has turned home. */
    public boolean isRivalLumberSold(final int shipId) {
        return readRivalHoldLumber(shipId) == 0
                && getModel().getPlayerByName("France").getRivalTradeObjective()
                        .map(objective -> objective.getStage().name())
                        .orElse("").equals("TO_COLONY");
    }

    /** Whether the colony warehouse now holds at least the expected tools. */
    public boolean isRivalToolsDelivered(final int colonyId,
            final int expectedTools) {
        return getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                .getGoods(org.microcol.model.GoodsType.TOOLS)
                .getAmount() >= expectedTools;
    }

    /**
     * The two treasury events of the Europe stop are exactly the taxed sale of
     * the carried lumber and the largest tool purchase the 200-gold reserve and
     * the 50-tool target allowed, each at the prices recorded when it settled.
     * Nothing else touched the treasury, and the stage turned home once.
     *
     * @param soldLumber      lumber that was aboard when the ship arrived
     * @param suppliedAlready tools already in the colony or in transit
     */
    public void verifyRivalEuropeSale(final int shipId, final int soldLumber,
            final int suppliedAlready) {
        assertEquals(2, ledger.size(),
                "The sale and the purchase are the only treasury events: "
                        + ledger);
        final LedgerEntry sale = ledger.get(0);
        final LedgerEntry purchase = ledger.get(1);
        assertEquals(ledgerStartGold, sale.oldGold,
                "Sale starts from the fixture treasury");
        assertEquals(
                sale.oldGold + saleTotal(soldLumber, sale.lumberSellPrice,
                        sale.taxPercentage),
                sale.newGold, "Taxed sale proceeds: " + sale);
        assertEquals(sale.newGold, purchase.oldGold,
                "The purchase starts from the gold the sale left");
        final int tools = affordableTools(purchase.oldGold,
                TOOLS_TARGET - suppliedAlready, purchase.toolBuyPrice,
                purchase.taxPercentage);
        assertTrue(tools > 0, "Some tools must be affordable: " + purchase);
        assertEquals(tools, readRivalHoldTools(shipId),
                "Largest affordable quantity: " + purchase);
        assertEquals(
                purchase.oldGold - purchaseTotal(tools, purchase.toolBuyPrice,
                        purchase.taxPercentage),
                purchase.newGold, "Taxed purchase cost: " + purchase);
        assertTrue(purchase.newGold >= 200, "The reserve is kept: " + purchase);
        assertEquals(0, readRivalHoldLumber(shipId));
        verifyRivalTreasury(2);
        assertEquals("TO_COLONY", getModel().getPlayerByName("France")
                .getRivalTradeObjective().orElseThrow().getStage().name());
    }

    /**
     * Save and Continue during the return voyage changes no cargo or gold and
     * replays no sale or purchase: the same bought tools are still aboard, the
     * treasury equals what the two events left, and the leg is still home.
     */
    public void verifyRivalVoyageUnchanged(final int shipId,
            final int expectedTools) {
        assertEquals(expectedTools, readRivalHoldTools(shipId));
        assertEquals(0, readRivalHoldLumber(shipId));
        verifyRivalTreasury(2);
        assertEquals("TO_COLONY", getModel().getPlayerByName("France")
                .getRivalTradeObjective().orElseThrow().getStage().name());
    }

    /**
     * The bought tools reached the colony warehouse, the hold is empty and the
     * colony keeps the reserve. The way home added no treasury event, so
     * nothing was sold or bought a second time.
     */
    public void verifyRivalToolsDelivered(final int colonyId, final int shipId,
            final int expectedTools, final int expectedLumber) {
        assertEquals(expectedTools,
                getModel().getColonyById(colonyId).orElseThrow().getWarehouse()
                        .getGoods(org.microcol.model.GoodsType.TOOLS)
                        .getAmount());
        assertEquals(0, readRivalHoldTools(shipId));
        assertEquals(0, readRivalHoldLumber(shipId));
        assertEquals(expectedLumber, readRivalLumber(colonyId));
        verifyRivalTreasury(2);
        assertEquals("TO_EUROPE", getModel().getPlayerByName("France")
                .getRivalTradeObjective().orElseThrow().getStage().name());
    }

    private static int saleTotal(final int amount, final int unitPrice,
            final int taxPercentage) {
        return amount * unitPrice * (100 - taxPercentage) / 100;
    }

    private static int purchaseTotal(final int amount, final int unitPrice,
            final int taxPercentage) {
        return amount * unitPrice * (100 + taxPercentage) / 100;
    }

    /** Largest amount up to the cap that keeps the reserve after the price. */
    private static int affordableTools(final int gold, final int cap,
            final int unitPrice, final int taxPercentage) {
        for (int amount = cap; amount > 0; amount--) {
            if (gold - purchaseTotal(amount, unitPrice, taxPercentage) >= 200) {
                return amount;
            }
        }
        return 0;
    }

    private static final int TOOLS_TARGET = 50;

    /** Lumber in the colony and the ship together never changes by trading. */
    public void verifyRivalLumberConserved(final int expectedRound,
            final int expectedColonyId, final int expectedShipId,
            final int expectedTotalLumber) {
        assertEquals(expectedRound, readRound());
        assertEquals(expectedTotalLumber, readRivalLumber(expectedColonyId)
                + readRivalHoldLumber(expectedShipId));
        assertTrue(getModel().getPlayerByName("France").getRivalTradeObjective()
                .isPresent());
    }

    /**
     * Colonists of France, however they are placed. The expansion keeps them.
     */
    private static final int CREW_SIZE = 4;

    /** Food in stock must cover this many turns of the remaining residents. */
    private static final int FOOD_TURNS = 5;

    /** The second colony must be founded within this many France turns. */
    private static final int MAX_OWNER_TURNS = 20;

    private int readRivalColonists() {
        final Player rival = getModel().getPlayerByName("France");
        return (int) getModel().getAllUnits().stream()
                .filter(unit -> unit.getOwner() == rival
                        && unit.getType().canBuildColony())
                .count();
    }

    /** French colonists born by ordinary colony growth since observation. */
    private final java.util.Set<Integer> rivalBirths = new java.util.concurrent.CopyOnWriteArraySet<>();

    /**
     * Start recording France's births of the current model. A Continue creates
     * a new model, so the scenario calls this again after every Continue; the
     * recorded births are kept. It only listens and changes nothing.
     */
    public void observeRivalBirths() {
        final org.microcol.model.Model model = getModel();
        final Player rival = model.getPlayerByName("France");
        model.addListener(new org.microcol.model.ModelListenerAdapter() {
            @Override
            public void onNewUnitWasBorn(
                    final org.microcol.model.event.NewUnitWasBornEvent event) {
                if (event.getUnit().getOwner() == rival) {
                    rivalBirths.add(event.getUnit().getId());
                }
            }
        });
    }

    private java.util.Set<Integer> readRivalColonistIds() {
        final Player rival = getModel().getPlayerByName("France");
        return getModel().getAllUnits().stream()
                .filter(unit -> unit.getOwner() == rival
                        && unit.getType().canBuildColony())
                .map(Unit::getId).collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Every original colonist survives under its stable ID, and the only other
     * French colonists are the births recorded from the colony growth events.
     */
    private void verifyRivalColonistsAccounted(
            final java.util.Set<Integer> crew, final String situation) {
        final java.util.Set<Integer> expected = new java.util.HashSet<>(crew);
        expected.addAll(rivalBirths);
        assertEquals(expected, readRivalColonistIds(),
                "Colonists are the original crew plus recorded births; "
                        + situation);
    }

    public int readRivalColonies() {
        return getModel().getColonies(getModel().getPlayerByName("France"))
                .size();
    }

    public boolean isRivalColonyFounded() {
        return readRivalColonies() == 2;
    }

    public boolean isRivalFounderReleased(final int colonyId,
            final int founderId) {
        return getModel().getColonyById(colonyId)
                .map(colony -> !colony.getUnitsInsideColony()
                        .contains(getModel().getUnitById(founderId)))
                .orElse(true);
    }

    /** The site the saved objective chose, or null while none exists. */
    public Location readRivalExpansionSite() {
        return getModel().getPlayerByName("France").getRivalExpansionObjective()
                .map(org.microcol.model.RivalExpansionObjective::getSite)
                .orElse(null);
    }

    /** Aboard a ship that has left the colony square. */
    public boolean isRivalFounderAwayFrom(final int founderId,
            final Location colonyLocation) {
        final Unit founder = getModel().getUnitById(founderId);
        return founder.isAtCargoSlot() && !founder.getPlaceCargoSlot()
                .getOwnerUnit().getLocation().equals(colonyLocation);
    }

    /**
     * Where the founder is, without asking a unit that works in a colony or
     * rides a ship for a map location, which it does not have.
     */
    private String describeFounderPlace(final Unit founder) {
        if (founder.isAtPlaceLocation()) {
            return "on the map at " + founder.getLocation();
        }
        if (founder.isAtCargoSlot()) {
            return "aboard the ship at "
                    + founder.getPlaceCargoSlot().getOwnerUnit().getLocation();
        }
        return "inside a colony";
    }

    /** The objective, founder and carrier placement, as text for reports. */
    public String describeRivalExpansion(final int founderId,
            final int shipId) {
        final Unit founder = getModel().getUnitById(founderId);
        return getModel().getPlayerByName("France").getRivalExpansionObjective()
                .map(org.microcol.model.RivalExpansionObjective::toString)
                .orElse("none") + "; founder " + describeFounderPlace(founder)
                + "; carrier at " + getModel().getUnitById(shipId).getLocation()
                + "; round " + readRound();
    }

    public void verifyRivalExpansionReady(final int colonyId,
            final Location colonyLocation, final int shipId,
            final int secondShipId, final int founderId) {
        final Player rival = getModel().getPlayerByName("France");
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(colonyId).orElseThrow();
        assertEquals(colonyLocation, colony.getLocation());
        assertSame(rival, colony.getOwner());
        assertEquals(1, readRivalColonies());
        assertEquals(java.util.List.of(getModel().getUnitById(founderId)),
                colony.getUnitsInsideColony());
        assertEquals(colonyLocation,
                getModel().getUnitById(shipId).getLocation());
        assertEquals(colonyLocation,
                getModel().getUnitById(secondShipId).getLocation());
        assertEquals(CREW_SIZE, readRivalColonists());
        assertTrue(rival.getRivalExpansionObjective().isEmpty());
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
    }

    /**
     * After each France turn: the game runs, control is back with the human,
     * the colonists are conserved, and an expansion that has released its
     * founder keeps its objective until the founding.
     */
    public void verifyRivalExpansionJourney(final String leg,
            final int colonyId, final int founderId, final int shipId,
            final java.util.Set<Integer> crew) {
        final org.microcol.model.Model model = getModel();
        final Player rival = model.getPlayerByName("France");
        final String situation = leg + ": "
                + describeRivalExpansion(founderId, shipId) + "; births "
                + rivalBirths;
        assertFalse(model.isGameFinished(),
                "The game ended before the expansion did; " + situation);
        verifyRivalColonistsAccounted(crew, situation);
        assertSame(model.getPlayerByName("Dutch"), model.getCurrentPlayer());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each rival turn");
        if (rival.getRivalExpansionObjective().isPresent()) {
            final org.microcol.model.RivalExpansionObjective objective = rival
                    .getRivalExpansionObjective().orElseThrow();
            assertEquals(colonyId, objective.getColonyId(), situation);
            assertEquals(founderId, objective.getFounderId(), situation);
            assertEquals(java.util.Optional.of(shipId),
                    objective.getCarrierId(), situation);
        }
        assertTrue(
                !isRivalFounderReleased(colonyId, founderId)
                        || isRivalColonyFounded()
                        || rival.getRivalExpansionObjective().isPresent(),
                "A released founder keeps its expansion until it founds; "
                        + situation);
    }

    /**
     * The release leaves the colony with the crew minus the founder, at least
     * two residents, a nonnegative food balance and food for five turns.
     */
    public void verifyRivalSourceAfterRelease(final int colonyId,
            final int founderId, final java.util.Set<Integer> crew) {
        final org.microcol.model.Colony colony = getModel()
                .getColonyById(colonyId).orElseThrow();
        final java.util.Set<Integer> expected = new java.util.HashSet<>(crew);
        expected.remove(founderId);
        final java.util.Set<Integer> residents = colony.getUnitsInsideColony()
                .stream().map(Unit::getId)
                .collect(java.util.stream.Collectors.toSet());
        final java.util.Set<Integer> born = new java.util.HashSet<>(residents);
        born.removeAll(expected);
        assertTrue(residents.containsAll(expected),
                "Every remaining crew member stays a resident: " + residents);
        assertTrue(rivalBirths.containsAll(born),
                "Other residents are only recorded births: " + residents
                        + ", births " + rivalBirths);
        assertTrue(colony.getUnitsInsideColony().size() >= 2,
                "Two residents stay");
        final int consumption = colony.getUnitsInsideColony().stream()
                .mapToInt(unit -> unit.getType().getAteFoodPerTurn()).sum();
        assertTrue(readRivalFood(colonyId) >= FOOD_TURNS * consumption,
                "Food covers five turns of the remaining residents");
        assertTrue(
                colony.getGoodsStats()
                        .getStatsByType(org.microcol.model.GoodsType.CORN)
                        .getNetProduction() >= 0,
                "The food balance is not negative");
    }

    /** Save and Continue midway: the same objective, founder and ship place. */
    public void verifyRivalExpansionUnchanged(final String expected,
            final int founderId, final int shipId) {
        assertEquals(expected, describeRivalExpansion(founderId, shipId));
    }

    /**
     * Exactly one new colony at the expected site with the founder as its only
     * resident. The source keeps its crew minus the founder, the objective is
     * retired, and the founding came within the owner-turn bound.
     */
    public void verifyRivalSecondColony(final int sourceColonyId,
            final Location expectedSite, final int founderId,
            final java.util.Set<Integer> crew, final int startRound,
            final boolean continuedMidJourney) {
        final Player rival = getModel().getPlayerByName("France");
        assertTrue(continuedMidJourney,
                "The Save and Continue midway never happened");
        assertEquals(2, readRivalColonies(), "Exactly one new colony");
        assertTrue(readRound() - startRound <= MAX_OWNER_TURNS,
                "Founding took more than twenty owner turns");
        final org.microcol.model.Colony source = getModel()
                .getColonyById(sourceColonyId).orElseThrow();
        final org.microcol.model.Colony second = getModel().getColonies(rival)
                .stream().filter(colony -> colony.getId() != sourceColonyId)
                .findFirst().orElseThrow();
        assertNotNull(expectedSite, "The expansion never chose a site");
        assertEquals(expectedSite, second.getLocation());
        assertSame(rival, second.getOwner());
        assertEquals(java.util.List.of(getModel().getUnitById(founderId)),
                second.getUnitsInsideColony());
        assertNotEquals(source.getName(), second.getName());
        assertTrue(rival.getRivalExpansionObjective().isEmpty(),
                "The expansion objective is retired after founding");
        verifyRivalSourceAfterRelease(sourceColonyId, founderId, crew);
        verifyRivalColonistsAccounted(crew, "Second colony");
        assertSame(getModel().getPlayerByName("Dutch"),
                getModel().getCurrentPlayer());
        assertTrue(getNodeFinder().lookup("#nextTurn").tryQuery().isPresent(),
                "Control must return to the human after each rival turn");
    }

}
