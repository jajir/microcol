package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Colony;
import org.microcol.model.Direction;
import org.microcol.model.EuropeTradeQuote;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.RivalTradeFixtures;
import org.microcol.model.RivalTradeObjective;
import org.microcol.model.RivalTradeObjective.Stage;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.Ship;

import com.google.common.eventbus.EventBus;

class RivalTradeCycleTest {
    private static final String RIVAL = "France";
    private static final int FOUNDER_ID = 4;
    private static final int SHIP_ID = 5;
    private static final int SECOND_SHIP_ID = 6;
    private static final int DUTCH_FORCE_ID = 0;
    private static final int DUTCH_SHIP_ID = 3;
    private static final Location PORT = Location.of(10, 10);
    private static final Location COLONY = Location.of(11, 10);
    /** Land beside the colony: room for a worker and for an attacker. */
    private static final Location SHORE = Location.of(12, 10);
    /** The default return square of a ship that never sailed out. */
    private static final Location ARRIVAL = Location.of(49, 10);
    private static final Location SECOND_SHIP_START = Location.of(10, 11);
    private static final Location OPEN_SEA = Location.of(30, 20);
    /** A one-square island far from the first colony and the sea lanes. */
    private static final Location SECOND_COLONY = Location.of(11, 20);
    private static final Location THIRD_SHIP_START = Location.of(30, 25);
    private static final int SECOND_FOUNDER_ID = 7;
    private static final int THIRD_SHIP_ID = 8;
    private static final int TAX_RATE = 25;
    private static final int GOLD_RESERVE = 200;

    private Model model;

    private SimpleAiPlayer ai;

    /** Tools in the hold of an extra French ship; null creates no such ship. */
    private Integer secondShipTools;

    /** Where the extra ship starts; it is not on land before the colony. */
    private Location secondShipAt = OPEN_SEA;

    /** Adds a second colony site, its French founder and a third empty ship. */
    private boolean twoColonies;

    @AfterEach
    void stopRobot() {
        if (ai != null) {
            ai.stop();
        }
    }

    /**
     * Open ocean with a western high-sea lane for the outbound voyage and an
     * eastern one that gives the default return square for a ship that never
     * sailed out. The lanes and a north-south land wall can be left out, and
     * the French saved player can be changed before the model is built.
     */
    private Model world(final boolean lanes, final boolean wall,
            final Consumer<PlayerPo> france) {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer(RIVAL);
        final Map<Location, TerrainType> terrain = new HashMap<>();
        final Set<Location> known = new HashSet<>();
        for (int x = 1; x <= 50; x++) {
            for (int y = 1; y <= 30; y++) {
                terrain.put(Location.of(x, y), TerrainType.OCEAN);
                known.add(Location.of(x, y));
            }
        }
        terrain.put(COLONY, TerrainType.GRASSLAND);
        terrain.put(SHORE, TerrainType.GRASSLAND);
        if (twoColonies) {
            terrain.put(SECOND_COLONY, TerrainType.GRASSLAND);
        }
        if (lanes) {
            terrain.put(Location.of(1, 10), TerrainType.HIGH_SEA);
            terrain.put(Location.of(2, 10), TerrainType.HIGH_SEA);
            terrain.put(ARRIVAL, TerrainType.HIGH_SEA);
            terrain.put(Location.of(50, 10), TerrainType.HIGH_SEA);
        }
        if (wall) {
            for (int y = 1; y <= 30; y++) {
                terrain.put(Location.of(5, y), TerrainType.GRASSLAND);
            }
        }
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(Set.of());
        final PlayerPo rival = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals(RIVAL))
                .findFirst().orElseThrow();
        rival.getVisible().setVisibility(known, 50, 30);
        france.accept(rival);
        final UnitPo ship = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                .orElseThrow();
        ship.getPlaceMap().setLocation(PORT);
        if (secondShipTools != null || twoColonies) {
            final List<UnitPo> units = new java.util.ArrayList<>(
                    data.getModel().getUnits());
            if (secondShipTools != null) {
                units.add(extraShip(ship, SECOND_SHIP_ID, secondShipAt,
                        secondShipTools));
            }
            if (twoColonies) {
                units.add(extraShip(ship, THIRD_SHIP_ID, THIRD_SHIP_START, 0));
                units.add(secondFounder(ship));
            }
            data.getModel().setUnits(units);
        }
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** An extra empty-or-tooled French galleon at sea. */
    private UnitPo extraShip(final UnitPo template, final int id,
            final Location at, final int tools) {
        final UnitPo extra = new UnitPo();
        extra.setId(id);
        extra.setType(UnitType.GALLEON);
        extra.setOwnerId(RIVAL);
        extra.setAction(template.getAction());
        extra.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        extra.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int index = 0; index < template.getCargo().getSlots()
                .size(); index++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        if (tools > 0) {
            cargo.getSlots().get(0).setGoodsType(GoodsType.TOOLS);
            cargo.getSlots().get(0).setAmount(tools);
        }
        extra.setCargo(cargo);
        return extra;
    }

    /** A French colonist standing on the second colony site. */
    private UnitPo secondFounder(final UnitPo template) {
        final UnitPo founder = new UnitPo();
        founder.setId(SECOND_FOUNDER_ID);
        founder.setType(UnitType.COLONIST);
        founder.setOwnerId(RIVAL);
        founder.setAction(template.getAction());
        founder.setWorkUpdates(0);
        founder.setMissionary(false);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(SECOND_COLONY);
        place.setOrientation(Direction.west);
        founder.setPlaceMap(place);
        founder.setAvailableMoves(1);
        return founder;
    }

    /**
     * Save and load with the saved colony list reversed, so the higher colony
     * comes first in the model's collection. Nothing else changes.
     */
    private void reverseSavedColonyOrder() {
        ai.stop();
        final ModelPo saved = model.save();
        final List<ColonyPo> colonies = new ArrayList<>(saved.getColonies());
        Collections.reverse(colonies);
        saved.setColonies(colonies);
        model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        ai = new SimpleAiPlayer(model, france(),
                new AnimationManager(new EventBus()));
    }

    /** The rival founds its colony from its own founder on the coast. */
    private void prepareRival() {
        prepareRival(true, false, rival -> {
        });
    }

    private void prepareRival(final boolean lanes, final boolean wall,
            final Consumer<PlayerPo> france) {
        model = world(lanes, wall, france);
        final Unit founder = model.getUnitById(FOUNDER_ID);
        founder.placeToLocation(COLONY);
        founder.startTurn();
        model.buildColony(france(), founder, "Rouen");
        ai = new SimpleAiPlayer(model, france(),
                new AnimationManager(new EventBus()));
    }

    /** Continue: the saved game is loaded into a new model and robot. */
    private void continueGame() {
        ai.stop();
        model = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        ai = new SimpleAiPlayer(model, france(),
                new AnimationManager(new EventBus()));
    }

    /** Moves the ship into its own colony port, using ordinary movement. */
    private void moveShipToColony() {
        moveToColony((Ship) model.getUnitById(SHIP_ID));
    }

    private void moveToColony(final Ship moving) {
        moving.startTurn();
        if (moving.getLocation().equals(COLONY)) {
            return;
        }
        model.moveUnit(moving, Path.of(List.of(COLONY)));
    }

    /**
     * One owner turn of the rival: the voyage counts down, the carrier regains
     * its movement, then the robot plays its turn. A carrier that no longer
     * exists is simply not advanced.
     */
    private void rivalTurn() {
        model.tryGetUnitById(SHIP_ID).filter(
                unit -> unit instanceof Ship && unit.getOwner() == france())
                .map(Ship.class::cast).ifPresent(carrier -> {
                    carrier.advanceVoyage();
                    carrier.startTurn();
                });
        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> ai.turnStarted());
    }

    private Player france() {
        return model.getPlayerByName(RIVAL);
    }

    private Ship ship() {
        return (Ship) model.getUnitById(SHIP_ID);
    }

    private Colony colony() {
        return model.getColonies(france()).get(0);
    }

    private int holdAmount(final Ship carrier, final GoodsType type) {
        return carrier.getCargo().getSlots().stream()
                .flatMap(slot -> slot.getGoods().stream())
                .filter(goods -> goods.getType().equals(type))
                .mapToInt(Goods::getAmount).sum();
    }

    private int holdAmount(final GoodsType type) {
        return holdAmount(ship(), type);
    }

    private int lumberStock() {
        return colony().getWarehouse().getGoods(GoodsType.LUMBER).getAmount();
    }

    private int toolStock() {
        return colony().getWarehouse().getGoods(GoodsType.TOOLS).getAmount();
    }

    private void setLumber(final int amount) {
        colony().getWarehouse().removeGoods(
                colony().getWarehouse().getGoods(GoodsType.LUMBER));
        if (amount > 0) {
            colony().getWarehouse()
                    .addGoods(Goods.of(GoodsType.LUMBER, amount));
        }
    }

    private void setTools(final int amount) {
        colony().getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, amount));
    }

    private Optional<Stage> stage() {
        return france().getRivalTradeObjective()
                .map(RivalTradeObjective::getStage);
    }

    private int colonyId() {
        return colony().getId();
    }

    private void objective(final Stage stage) {
        france().setRivalTradeObjective(
                new RivalTradeObjective(colonyId(), SHIP_ID, stage));
    }

    private int toolsPrice(final int amount) {
        return france()
                .quoteEuropeTrade(Goods.of(GoodsType.TOOLS, amount), true)
                .getTotal();
    }

    @Test
    void fortyNineSurplusLumberWaitsAndNothingMoves() {
        prepareRival();
        moveShipToColony();
        setLumber(99);
        rivalTurn();
        assertEquals(COLONY, ship().getLocation());
        assertEquals(0, holdAmount(GoodsType.LUMBER));
        assertEquals(99, lumberStock());
        assertEquals(Optional.of(Stage.TO_EUROPE), stage());
    }

    @Test
    void fiftySurplusLumberLoadsOnlyTheSurplusAndRetainsTheReserve() {
        prepareRival();
        moveShipToColony();
        setLumber(100);
        rivalTurn();
        assertEquals(50, holdAmount(GoodsType.LUMBER));
        assertEquals(50, lumberStock());
    }

    @Test
    void unavailableLumberStockLoadsNothingAndKeepsTheObjective() {
        prepareRival();
        moveShipToColony();
        setLumber(0);
        rivalTurn();
        assertEquals(0, holdAmount(GoodsType.LUMBER));
        assertEquals(0, lumberStock());
        assertEquals(Optional.of(Stage.TO_EUROPE), stage());
        assertEquals(COLONY, ship().getLocation());
    }

    @Test
    void carrierWithCargoAboardIsNotChosenAndItsCargoIsUntouched() {
        prepareRival();
        moveShipToColony();
        setLumber(100);
        ship().getCargo().getSlots().get(0).storeFromColonyWarehouse(
                Goods.of(GoodsType.LUMBER, 10), colony());
        rivalTurn();
        assertTrue(france().getRivalTradeObjective().isEmpty());
        assertEquals(10, holdAmount(GoodsType.LUMBER));
        assertEquals(90, lumberStock());
    }

    @Test
    void fullHoldsLoadNothingAndWaitWithTheirCargo() {
        prepareRival();
        moveShipToColony();
        setLumber(100);
        objective(Stage.TO_EUROPE);
        for (final var slot : ship().getCargo().getSlots()) {
            setTools(100);
            slot.storeFromColonyWarehouse(Goods.of(GoodsType.TOOLS, 100),
                    colony());
        }
        final int toolsAboard = holdAmount(GoodsType.TOOLS);
        rivalTurn();
        assertEquals(0, holdAmount(GoodsType.LUMBER));
        assertEquals(100, lumberStock());
        assertEquals(toolsAboard, holdAmount(GoodsType.TOOLS));
        assertEquals(Optional.of(Stage.TO_EUROPE), stage());
    }

    @Test
    void partlyFilledHoldTakesOnlyTheSurplusThatFitsAndLeavesPort() {
        prepareRival();
        moveShipToColony();
        setLumber(100);
        objective(Stage.TO_EUROPE);
        // Slot 0 already carries 30 lumber; the other holds are full of tools.
        ship().getCargo().getSlots().get(0).storeFromColonyWarehouse(
                Goods.of(GoodsType.LUMBER, 30), colony());
        for (int index = 1; index < ship().getCargo().getSlots()
                .size(); index++) {
            setTools(100);
            ship().getCargo().getSlots().get(index).storeFromColonyWarehouse(
                    Goods.of(GoodsType.TOOLS, 100), colony());
        }
        assertEquals(70, lumberStock());
        rivalTurn();
        // Only the 20 lumber above the 50 reserve moves, into the same hold.
        assertEquals(50, holdAmount(GoodsType.LUMBER));
        assertEquals(50, lumberStock());
        assertNotEquals(COLONY, ship().getLocation(),
                "A full shipment sails at once");
    }

    @Test
    void topologyWithoutASeaLaneKeepsTheShipmentAndTheObjective() {
        prepareRival(false, false, rival -> {
        });
        moveShipToColony();
        setLumber(100);
        rivalTurn();
        assertEquals(50, holdAmount(GoodsType.LUMBER));
        assertEquals(50, lumberStock());
        assertEquals(Optional.of(Stage.TO_EUROPE), stage());
    }

    @Test
    void lowestEligibleCarrierIsChosenAndOnlyOneObjectiveExists() {
        secondShipTools = 0;
        secondShipAt = SECOND_SHIP_START;
        prepareRival();
        moveShipToColony();
        moveToColony((Ship) model.getUnitById(SECOND_SHIP_ID));
        setLumber(100);
        rivalTurn();
        final RivalTradeObjective chosen = france().getRivalTradeObjective()
                .orElseThrow();
        assertEquals(SHIP_ID, chosen.getCarrierId());
        assertEquals(colonyId(), chosen.getColonyId());
        // The other ship is neither loaded nor moved by the cycle.
        final Ship other = (Ship) model.getUnitById(SECOND_SHIP_ID);
        assertEquals(0, holdAmount(other, GoodsType.LUMBER));
        assertEquals(COLONY, other.getLocation());
        assertEquals(50, holdAmount(GoodsType.LUMBER));
    }

    @Test
    void lumberSoldInEuropeCreditsTheTaxedQuoteExactlyOnce() {
        prepareRival();
        setTools(50);
        model.getEurope().getPort().placeShipToPort(ship());
        france().setGold(1000);
        ship().getCargo().getSlots().get(0)
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.LUMBER, 50));
        final int before = france().getGold();
        final int proceeds = france()
                .quoteEuropeTrade(Goods.of(GoodsType.LUMBER, 50), false)
                .getTotal();
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertEquals(before + proceeds, france().getGold());
        assertEquals(0, holdAmount(GoodsType.LUMBER));
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        assertTrue(ship().isAtHighSea());
        rivalTurn();
        assertEquals(before + proceeds, france().getGold(),
                "The sale must not repeat during the voyage");
    }

    @Test
    void taxedLumberSaleCreditsTheNetQuoteOnceAcrossRestoreAndVoyage() {
        prepareRival(true, false, rival -> rival.getTax().setRate(TAX_RATE));
        // The colony already has its 50 tools, so only the sale moves gold.
        setTools(50);
        model.getEurope().getPort().placeShipToPort(ship());
        france().setGold(1000);
        ship().getCargo().getSlots().get(0)
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.LUMBER, 50));
        final EuropeTradeQuote quote = france()
                .quoteEuropeTrade(Goods.of(GoodsType.LUMBER, 50), false);
        // The live quote is taxed: the net is the gross less a positive tax.
        assertEquals(TAX_RATE, france().getTaxRate());
        assertEquals(TAX_RATE, quote.getTaxPercentage());
        assertEquals(50 * quote.getUnitPrice(), quote.getGross());
        assertTrue(quote.getTax() > 0, "A nonzero rate takes a tax");
        assertEquals(quote.getGross() - quote.getTax(), quote.getTotal());
        assertTrue(quote.getTotal() < quote.getGross());
        final int before = france().getGold();
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertEquals(before + quote.getTotal(), france().getGold(),
                "Only the net proceeds are credited");
        assertEquals(0, holdAmount(GoodsType.LUMBER));
        assertEquals(0, holdAmount(GoodsType.TOOLS), "The target was met");
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        assertTrue(ship().isAtHighSea());
        final int afterSale = france().getGold();

        // A saved restore keeps the rate and the leg; the voyage home and the
        // following owner turns never sell or buy again.
        continueGame();
        assertEquals(TAX_RATE, france().getTaxRate());
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        for (int turn = 0; turn < 12; turn++) {
            rivalTurn();
            assertEquals(afterSale, france().getGold(),
                    "Treasury changed after restore, turn " + turn);
            assertEquals(0, holdAmount(GoodsType.LUMBER));
            assertEquals(0, holdAmount(GoodsType.TOOLS));
        }
    }

    @Test
    void lowestEligibleColonyWinsAndUsesItsLowestIdleShipInAnyListOrder() {
        secondShipTools = 0;
        twoColonies = true;
        prepareRival();
        final Unit secondFounder = model.getUnitById(SECOND_FOUNDER_ID);
        secondFounder.startTurn();
        final int higherId = model.buildColony(france(), secondFounder, "Lyon")
                .getId();
        final int lowestId = model.getColonyAt(COLONY).orElseThrow().getId();
        assertTrue(lowestId < higherId);
        // The lowest ship waits in the higher colony; the lowest colony has
        // two idle ships, 6 and 8. Both colonies have the same lumber.
        model.getUnitById(SHIP_ID).placeToLocation(SECOND_COLONY);
        model.getUnitById(SECOND_SHIP_ID).placeToLocation(COLONY);
        model.getUnitById(THIRD_SHIP_ID).placeToLocation(COLONY);
        model.getColonyById(lowestId).orElseThrow().getWarehouse()
                .addGoods(Goods.of(GoodsType.LUMBER, 100));
        model.getColonyById(higherId).orElseThrow().getWarehouse()
                .addGoods(Goods.of(GoodsType.LUMBER, 100));
        reverseSavedColonyOrder();
        assertEquals(higherId, model.getColonies(france()).get(0).getId(),
                "The collection lists the higher colony first");

        rivalTurn();
        final RivalTradeObjective chosen = france().getRivalTradeObjective()
                .orElseThrow();
        assertEquals(lowestId, chosen.getColonyId());
        assertEquals(SECOND_SHIP_ID, chosen.getCarrierId());
        final Ship carrier = (Ship) model.getUnitById(SECOND_SHIP_ID);
        assertEquals(50, holdAmount(carrier, GoodsType.LUMBER));
        assertEquals(50, model.getColonyById(lowestId).orElseThrow()
                .getWarehouse().getGoods(GoodsType.LUMBER).getAmount());
        // The other colony and the other ships are untouched.
        assertEquals(100, model.getColonyById(higherId).orElseThrow()
                .getWarehouse().getGoods(GoodsType.LUMBER).getAmount());
        final Ship lowestShip = ship();
        final Ship thirdShip = (Ship) model.getUnitById(THIRD_SHIP_ID);
        assertEquals(SECOND_COLONY, lowestShip.getLocation());
        assertEquals(COLONY, thirdShip.getLocation());
        assertTrue(lowestShip.getCargo().getSlots().stream()
                .allMatch(slot -> slot.isEmpty()));
        assertTrue(thirdShip.getCargo().getSlots().stream()
                .allMatch(slot -> slot.isEmpty()));
    }

    @Test
    void toolPurchaseStopsAtTheGoldReserveAndTheAffordableQuantity() {
        prepareRival();
        model.getEurope().getPort().placeShipToPort(ship());
        final int affordable = 7;
        france().setGold(GOLD_RESERVE + toolsPrice(affordable));
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertEquals(affordable, holdAmount(GoodsType.TOOLS));
        assertEquals(GOLD_RESERVE, france().getGold());
        assertTrue(ship().isAtHighSea());
    }

    @Test
    void oneGoldBelowTheReserveCostsOneTool() {
        prepareRival();
        model.getEurope().getPort().placeShipToPort(ship());
        final int wanted = 7;
        // Quotes are read before the purchase, because a purchase moves them.
        final int seven = toolsPrice(wanted);
        final int six = toolsPrice(wanted - 1);
        france().setGold(GOLD_RESERVE + seven - 1);
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertEquals(wanted - 1, holdAmount(GoodsType.TOOLS));
        assertTrue(france().getGold() >= GOLD_RESERVE,
                "The reserve is never spent");
        assertEquals(GOLD_RESERVE + seven - 1 - six, france().getGold());
    }

    @Test
    void toolsAlreadyAboardCountTowardTheColonyTarget() {
        prepareRival();
        model.getEurope().getPort().placeShipToPort(ship());
        france().setGold(10000);
        ship().getCargo().getSlots().get(0)
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.TOOLS, 20));
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertEquals(50, holdAmount(GoodsType.TOOLS));
    }

    @Test
    void toolsOnAnotherOwnShipAndInTheWarehouseCountTowardTheTarget() {
        secondShipTools = 30;
        prepareRival();
        model.getEurope().getPort().placeShipToPort(ship());
        setTools(5);
        france().setGold(10000);
        objective(Stage.TO_EUROPE);
        rivalTurn();
        // 50 wanted, 30 on the other ship and 5 stored: only 15 are bought.
        assertEquals(15, holdAmount(GoodsType.TOOLS));
        assertEquals(30, holdAmount((Ship) model.getUnitById(SECOND_SHIP_ID),
                GoodsType.TOOLS));
    }

    @Test
    void boycottedLumberKeepsTheCargoTheLegAndTheTreasuryInEurope() {
        prepareRival(true, false,
                rival -> rival.getTax().getArrears().put("LUMBER", 100));
        // A lumber boycott also closes buying it, so the cargo comes from the
        // colony warehouse before the ship goes to Europe.
        moveShipToColony();
        setLumber(50);
        ship().getCargo().getSlots().get(0).storeFromColonyWarehouse(
                Goods.of(GoodsType.LUMBER, 50), colony());
        model.getEurope().getPort().placeShipToPort(ship());
        france().setGold(1000);
        final int gold = france().getGold();
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertTrue(ship().isAtEuropePort());
        assertEquals(50, holdAmount(GoodsType.LUMBER));
        assertEquals(gold, france().getGold());
        assertEquals(0, holdAmount(GoodsType.TOOLS));
        assertEquals(Optional.of(Stage.TO_EUROPE), stage());
        rivalTurn();
        assertEquals(50, holdAmount(GoodsType.LUMBER));
        assertEquals(gold, france().getGold());
    }

    @Test
    void boycottedToolsAreSkippedButTheLumberIsStillSoldOnce() {
        prepareRival(true, false,
                rival -> rival.getTax().getArrears().put("TOOLS", 100));
        model.getEurope().getPort().placeShipToPort(ship());
        france().setGold(1000);
        ship().getCargo().getSlots().get(0)
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.LUMBER, 50));
        final int before = france().getGold();
        final int proceeds = france()
                .quoteEuropeTrade(Goods.of(GoodsType.LUMBER, 50), false)
                .getTotal();
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertEquals(before + proceeds, france().getGold());
        assertEquals(0, holdAmount(GoodsType.TOOLS));
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        assertTrue(ship().isAtHighSea(), "It returns without supplies");
    }

    @Test
    void declaredIndependenceKeepsTheShipAtEuropeWithoutAnyPurchase() {
        prepareRival(true, false, rival -> rival.setDeclaredIndependence(true));
        model.getEurope().getPort().placeShipToPort(ship());
        setTools(50);
        france().setGold(1000);
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertTrue(ship().isAtEuropePort(),
                "A declared nation has no home market to trade in");
        assertEquals(1000, france().getGold());
        assertEquals(0, holdAmount(GoodsType.TOOLS));
        assertEquals(Optional.of(Stage.TO_EUROPE), stage());
    }

    @Test
    void blockedReturnRouteKeepsTheShipInEuropeAndRetriesEachTurn() {
        prepareRival(true, true, rival -> {
        });
        model.getEurope().getPort().placeShipToPort(ship());
        ship().rememberDeparture(Location.of(2, 10), Direction.west);
        setTools(50);
        france().setGold(1000);
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertTrue(ship().isAtEuropePort(), "The return route is walled off");
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        final int gold = france().getGold();
        rivalTurn();
        assertTrue(ship().isAtEuropePort());
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        assertEquals(gold, france().getGold(), "No second trade on a retry");
    }

    @Test
    void occupiedArrivalSquareKeepsCargoAndObjectiveUntilItIsFree() {
        prepareRival();
        model.getEurope().getPort().placeShipToPort(ship());
        ship().getCargo().getSlots().get(0)
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.TOOLS, 20));
        objective(Stage.TO_COLONY);
        // A foreign ship stands on the square where the voyage ends.
        final Unit blocker = model.getUnitById(DUTCH_SHIP_ID);
        blocker.placeToLocation(ARRIVAL);
        for (int turn = 0; turn < 6; turn++) {
            rivalTurn();
        }
        assertTrue(ship().isAtHighSea(), "The arrival square is occupied");
        assertEquals(20, holdAmount(GoodsType.TOOLS));
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        // Once the square is free the next owner turn lands the ship.
        blocker.placeToLocation(OPEN_SEA);
        rivalTurn();
        assertFalse(ship().isAtHighSea());
        assertEquals(20, holdAmount(GoodsType.TOOLS) + toolStock());
    }

    @Test
    void destroyedColonyRetiresTheObjectiveAndKeepsTheCarrierCargo() {
        prepareRival();
        moveShipToColony();
        setLumber(100);
        rivalTurn();
        assertEquals(50, holdAmount(GoodsType.LUMBER));
        assertTrue(france().getRivalTradeObjective().isPresent());
        final Colony target = colony();
        target.getUnitsInsideColony()
                .forEach(worker -> worker.placeToLocation(SHORE));
        RivalTradeFixtures.destroyColony(model, target);
        rivalTurn();
        assertTrue(france().getRivalTradeObjective().isEmpty());
        assertEquals(50, holdAmount(GoodsType.LUMBER),
                "Retiring never removes cargo");
    }

    @Test
    void capturedColonyRetiresTheObjective() {
        prepareRival();
        moveShipToColony();
        setLumber(100);
        objective(Stage.TO_EUROPE);
        RivalTradeFixtures.captureColony(model, colony(),
                model.getUnitById(DUTCH_FORCE_ID));
        assertNotSame(france(),
                model.getColonyAt(COLONY).orElseThrow().getOwner());
        rivalTurn();
        assertTrue(france().getRivalTradeObjective().isEmpty());
    }

    @Test
    void destroyedCarrierRetiresTheObjectiveAndKeepsTheColonyStock() {
        prepareRival();
        moveShipToColony();
        setLumber(100);
        objective(Stage.TO_EUROPE);
        RivalTradeFixtures.destroyUnit(model, ship());
        rivalTurn();
        assertTrue(france().getRivalTradeObjective().isEmpty());
        assertEquals(100, lumberStock());
    }

    @Test
    void staleCarrierReferenceRetiresAtTheOwnersTurn() {
        prepareRival();
        france().setRivalTradeObjective(
                new RivalTradeObjective(colonyId(), 999, Stage.TO_EUROPE));
        rivalTurn();
        assertTrue(france().getRivalTradeObjective().isEmpty());
    }

    @Test
    void staleColonyReferenceRetiresAtTheOwnersTurn() {
        prepareRival();
        france().setRivalTradeObjective(
                new RivalTradeObjective(999, SHIP_ID, Stage.TO_EUROPE));
        rivalTurn();
        assertTrue(france().getRivalTradeObjective().isEmpty());
    }

    @Test
    void staleSavedReferencesLoadAndRetireAfterContinue() {
        prepareRival();
        france().setRivalTradeObjective(
                new RivalTradeObjective(999, 998, Stage.TO_COLONY));
        continueGame();
        assertEquals(Optional.of(Stage.TO_COLONY), stage(),
                "A stale reference loads; it is retired by the owner turn");
        rivalTurn();
        assertTrue(france().getRivalTradeObjective().isEmpty());
    }

    @Test
    void savedCycleKeepsItsLegAndCargoAfterContinue() {
        prepareRival();
        model.getEurope().getPort().placeShipToPort(ship());
        france().setGold(10000);
        ship().getCargo().getSlots().get(0)
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.TOOLS, 20));
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertTrue(ship().isAtHighSea());
        final int colonyId = colonyId();
        continueGame();
        assertEquals(Optional.of(colonyId), france().getRivalTradeObjective()
                .map(RivalTradeObjective::getColonyId));
        assertEquals(Optional.of(Stage.TO_COLONY), stage());
        assertTrue(ship().isAtHighSea());
        assertEquals(50, holdAmount(GoodsType.TOOLS));
    }

    @Test
    void midVoyageContinueNeverRepeatsASaleOrAPurchase() {
        prepareRival();
        model.getEurope().getPort().placeShipToPort(ship());
        france().setGold(1000);
        ship().getCargo().getSlots().get(0)
                .buyAndStoreFromEuropePort(Goods.of(GoodsType.LUMBER, 50));
        objective(Stage.TO_EUROPE);
        rivalTurn();
        assertTrue(ship().isAtHighSea());
        final int gold = france().getGold();
        final int tools = holdAmount(GoodsType.TOOLS);
        assertTrue(tools > 0);
        continueGame();
        for (int turn = 0; turn < 8; turn++) {
            rivalTurn();
            assertEquals(gold, france().getGold(),
                    "Treasury changed after Continue, turn " + turn);
            assertEquals(tools, holdAmount(GoodsType.TOOLS) + toolStock(),
                    "Tools were bought or lost after Continue, turn " + turn);
            assertEquals(0, holdAmount(GoodsType.LUMBER));
        }
    }
}
