package org.microcol.ai;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.microcol.model.CargoSlot;
import org.microcol.model.Colony;
import org.microcol.model.ColonyWarehouse;
import org.microcol.model.EuropeTradeQuote;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.MicroColException;
import org.microcol.model.Model;
import org.microcol.model.Path;
import org.microcol.model.Player;
import org.microcol.model.RivalExpansionObjective;
import org.microcol.model.RivalTradeObjective;
import org.microcol.model.RivalTradeObjective.Stage;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.unit.Ship;
import org.slf4j.LoggerFactory;

/**
 * A rival's single colony-to-Europe supply cycle. One colony and one carrier
 * are saved in the player's {@link RivalTradeObjective}. Every step revalidates
 * live ownership, cargo, capacity and trade permission, then uses the ordinary
 * model moves, cargo transfers and quoted Europe trades. A refused step changes
 * nothing and waits for the next owner turn, so the pass count is bounded.
 */
final class RivalTradeCycle {

    /** Lumber the colony keeps; only the surplus above it may be shipped. */
    private static final int LUMBER_RESERVE = 50;

    /** Least lumber a voyage carries. */
    private static final int LUMBER_SHIPMENT = 50;

    /** Tools wanted in the colony, counting those already on the way. */
    private static final int TOOLS_TARGET = 50;

    /** Gold kept back from every Europe purchase. */
    private static final int GOLD_RESERVE = 200;

    /**
     * Each pass moves, trades or changes the leg, so this is a safety bound.
     */
    private static final int MAX_PASSES = 64;

    private final AbstractRobotPlayer robot;

    RivalTradeCycle(final AbstractRobotPlayer robot) {
        this.robot = robot;
    }

    /**
     * @param unit required unit of this robot
     * @return whether the unit is the carrier of the active cycle, so the
     *         ordinary unit loop leaves it to this class
     */
    boolean isCarrier(final Unit unit) {
        return player().getRivalTradeObjective()
                .filter(objective -> objective.getCarrierId() == unit.getId())
                .isPresent();
    }

    /**
     * One owner-turn pass: retire a stale cycle, choose the lowest eligible
     * colony and carrier when none is active, then advance the cycle while each
     * pass makes progress.
     */
    void advance() {
        if (player().getRivalTradeObjective().isPresent()
                && resolve().isEmpty()) {
            player().clearRivalTradeObjective();
        }
        if (player().getRivalTradeObjective().isEmpty()) {
            choose();
        }
        boolean progressed = true;
        for (int pass = 0; pass < MAX_PASSES && progressed
                && robot.isActiveTurn(); pass++) {
            progressed = advanceOnce();
        }
    }

    private Model model() {
        return robot.getModel();
    }

    private Player player() {
        return robot.getPlayer();
    }

    /**
     * Live colony and carrier of the saved cycle; empty when either is gone.
     */
    private Optional<Leg> resolve() {
        final Optional<RivalTradeObjective> objective = player()
                .getRivalTradeObjective();
        if (objective.isEmpty()) {
            return Optional.empty();
        }
        final Optional<Colony> colony = model()
                .getColonyById(objective.get().getColonyId())
                .filter(Colony::isValid)
                .filter(candidate -> candidate.getOwner() == player());
        final Optional<Ship> carrier = model()
                .tryGetUnitById(objective.get().getCarrierId())
                .filter(unit -> unit instanceof Ship)
                .filter(unit -> unit.getOwner() == player())
                .map(Ship.class::cast);
        if (colony.isEmpty() || carrier.isEmpty()) {
            return Optional.empty();
        }
        return Optional
                .of(new Leg(objective.get(), colony.get(), carrier.get()));
    }

    /**
     * Lowest colony that has a lowest eligible ship in its port. Nothing is
     * chosen when no colony has one, so the rival waits finitely.
     */
    private void choose() {
        for (final Colony colony : model().getColonies(player()).stream()
                .sorted(Comparator.comparingInt(Colony::getId))
                .collect(Collectors.toList())) {
            if (!colony.isValid() || !model().isOwnedCoastalPort(player(),
                    colony.getLocation())) {
                continue;
            }
            final Optional<Ship> carrier = model()
                    .getUnitsAt(colony.getLocation()).stream()
                    .filter(unit -> unit instanceof Ship).map(Ship.class::cast)
                    .filter(this::isEligibleCarrier)
                    .min(Comparator.comparingInt(Ship::getId));
            if (carrier.isPresent()) {
                player().setRivalTradeObjective(
                        new RivalTradeObjective(colony.getId(),
                                carrier.get().getId(), Stage.TO_EUROPE));
                return;
            }
        }
    }

    /**
     * Own ship in its colony port, idle, undamaged and with empty holds. A ship
     * that carries an active second-colony founder belongs to that expansion.
     */
    private boolean isEligibleCarrier(final Ship ship) {
        return ship.getOwner() == player() && ship.isAtPlaceLocation()
                && ship.getAction().isNoAction() && !ship.isDamaged()
                && player().getRivalExpansionObjective()
                        .flatMap(RivalExpansionObjective::getCarrierId)
                        .map(carrierId -> carrierId != ship.getId())
                        .orElse(true)
                && ship.getCargo().getSlots().stream()
                        .allMatch(CargoSlot::isEmpty);
    }

    /** @return whether this pass moved, traded or changed the leg */
    private boolean advanceOnce() {
        final Optional<Leg> resolved = resolve();
        if (resolved.isEmpty()) {
            player().clearRivalTradeObjective();
            return false;
        }
        final Leg leg = resolved.get();
        if (leg.carrier.isAtEuropePort()) {
            return inEurope(leg);
        }
        if (!leg.carrier.isAtPlaceLocation()) {
            // A voyage runs its own countdown; the carrier waits for arrival.
            return false;
        }
        if (leg.carrier.getLocation().equals(leg.colony.getLocation())) {
            return atColony(leg);
        }
        return sail(leg);
    }

    private boolean atColony(final Leg leg) {
        if (leg.objective.getStage() == Stage.TO_COLONY) {
            return unloadTools(leg);
        }
        if (holdAmount(leg.carrier, GoodsType.LUMBER) < LUMBER_SHIPMENT) {
            loadLumber(leg);
            if (holdAmount(leg.carrier, GoodsType.LUMBER) < LUMBER_SHIPMENT) {
                return false;
            }
        }
        return sail(leg);
    }

    /**
     * Unload every tool the warehouse can take. Tools that stay aboard keep the
     * leg, so the next owner turn retries them.
     *
     * @return whether the carrier is empty of tools and the leg moved on
     */
    private boolean unloadTools(final Leg leg) {
        final ColonyWarehouse warehouse = leg.colony.getWarehouse();
        for (final CargoSlot slot : holdingSlots(leg.carrier,
                GoodsType.TOOLS)) {
            final Goods transfer = warehouse
                    .getUnloadableGoods(slot.getGoods().get(), slot);
            if (!transfer.isZero()) {
                warehouse.moveToWarehouse(transfer, slot);
            }
        }
        if (holdAmount(leg.carrier, GoodsType.TOOLS) > 0) {
            return false;
        }
        setStage(leg, Stage.TO_EUROPE);
        return true;
    }

    /**
     * Load the lumber above the reserve, within the free hold space, but only
     * when the load reaches a full shipment. Nothing moves below that.
     */
    private void loadLumber(final Leg leg) {
        final int surplus = warehouseAmount(leg.colony, GoodsType.LUMBER)
                - LUMBER_RESERVE;
        final int amount = Math.min(surplus,
                freeCapacity(leg.carrier, GoodsType.LUMBER));
        if (amount <= 0 || holdAmount(leg.carrier, GoodsType.LUMBER)
                + amount < LUMBER_SHIPMENT) {
            return;
        }
        int remaining = amount;
        for (final CargoSlot slot : freeSlots(leg.carrier, GoodsType.LUMBER)) {
            final int move = Math.min(remaining, slot.getAvailableCapacity());
            if (move > 0) {
                slot.storeFromColonyWarehouse(Goods.of(GoodsType.LUMBER, move),
                        leg.colony);
                remaining -= move;
            }
        }
    }

    /**
     * One ordinary step toward the leg's goal. At the departure square of the
     * outbound leg, the ship leaves for Europe.
     */
    private boolean sail(final Leg leg) {
        final Ship carrier = leg.carrier;
        if (!carrier.getAction().isNoAction()) {
            return false;
        }
        if (leg.objective.getStage() == Stage.TO_EUROPE
                && isDepartureSquare(carrier.getLocation())) {
            return depart(carrier);
        }
        final Predicate<Location> goal = leg.objective
                .getStage() == Stage.TO_COLONY
                        ? leg.colony.getLocation()::equals
                        : this::isDepartureSquare;
        return nextStep(carrier, goal).filter(carrier::canAffordStepTo)
                .map(step -> move(carrier, step)).orElse(false);
    }

    private boolean depart(final Ship carrier) {
        return carrier.getLocation().getNeighbors().stream()
                .filter(neighbor -> model().getHighSea()
                        .isDepartureMove(carrier, neighbor))
                .findFirst().filter(carrier::canAffordStepTo)
                .map(neighbor -> move(carrier, neighbor)).orElse(false);
    }

    private boolean move(final Ship carrier, final Location step) {
        try {
            model().moveUnit(carrier, Path.of(List.of(step)));
        } catch (final IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            // The shared rules refused the step; nothing moved.
            LoggerFactory.getLogger(RivalTradeCycle.class)
                    .debug("Rival trade step was refused", refused);
            return false;
        }
        robot.waitForAnimations();
        return true;
    }

    private boolean isDepartureSquare(final Location at) {
        return model().getMap().getTerrainTypeAt(at) == TerrainType.HIGH_SEA
                && at.getNeighbors().stream().anyMatch(neighbor -> model()
                        .getHighSea().isDepartureStep(at, neighbor));
    }

    /**
     * Breadth-first search over squares the player knows and the carrier may
     * enter. A departure step is never part of a route, so only the explicit
     * departure can leave the map.
     *
     * @return the first step toward the nearest goal square, or empty
     */
    private Optional<Location> nextStep(final Unit carrier,
            final Predicate<Location> goal) {
        final Location start = carrier.getLocation();
        final Map<Location, Location> firstStep = new HashMap<>();
        final ArrayDeque<Location> open = new ArrayDeque<>();
        firstStep.put(start, start);
        open.add(start);
        while (!open.isEmpty()) {
            final Location current = open.remove();
            for (final Location neighbor : current.getNeighbors()) {
                if (firstStep.containsKey(neighbor)
                        || !model().getMap().isValid(neighbor)
                        || !player().isVisible(neighbor)
                        || model().getHighSea().isDepartureStep(current,
                                neighbor)
                        || !carrier.isPossibleToMoveAt(neighbor)) {
                    continue;
                }
                firstStep.put(neighbor, current.equals(start) ? neighbor
                        : firstStep.get(current));
                if (goal.test(neighbor)) {
                    return Optional.of(firstStep.get(neighbor));
                }
                open.add(neighbor);
            }
        }
        return Optional.empty();
    }

    /**
     * Tools the gold reserve allows at the live price, up to the cap. A larger
     * purchase that is not affordable is never tried.
     */
    private int affordableTools(final int cap) {
        for (int amount = cap; amount > 0; amount--) {
            final EuropeTradeQuote quote = player()
                    .quoteEuropeTrade(Goods.of(GoodsType.TOOLS, amount), true);
            if (player().getGold() - quote.getTotal() >= GOLD_RESERVE) {
                return amount;
            }
        }
        return 0;
    }

    /**
     * Sell the carried lumber, then buy tools toward the colony target. A
     * refused sale keeps the cargo and the leg for the next owner turn.
     *
     * @return whether the Europe transactions finished and the leg moved on
     */
    private boolean trade(final Leg leg) {
        if (!player().canTradeInEurope() || !sellLumber(leg.carrier)) {
            return false;
        }
        buyTools(leg);
        setStage(leg, Stage.TO_COLONY);
        return true;
    }

    private boolean sellLumber(final Ship carrier) {
        if (holdAmount(carrier, GoodsType.LUMBER) > 0
                && player().isBoycotted(GoodsType.LUMBER)) {
            // A boycott closes the sale; the cargo and the leg wait in Europe.
            return false;
        }
        for (final CargoSlot slot : holdingSlots(carrier, GoodsType.LUMBER)) {
            final Goods goods = slot.getGoods().get();
            try {
                model().sellGoods(slot, goods,
                        player().quoteEuropeTrade(goods, false));
            } catch (final IllegalArgumentException | IllegalStateException
                    | MicroColException refused) {
                LoggerFactory.getLogger(RivalTradeCycle.class)
                        .debug("Rival lumber sale was refused", refused);
                return false;
            }
        }
        return true;
    }

    /**
     * Buy only as many tools as the target still needs, the gold reserve allows
     * and the holds can take. Tools already aboard count as in transit. A
     * refused purchase stops buying; the cycle still returns without supplies.
     */
    private void buyTools(final Leg leg) {
        final Ship carrier = leg.carrier;
        if (player().isBoycotted(GoodsType.TOOLS)) {
            // A boycott closes the purchase; the ship returns without supplies.
            return;
        }
        int remaining = TOOLS_TARGET
                - warehouseAmount(leg.colony, GoodsType.TOOLS)
                - toolsOnOwnShips();
        for (final CargoSlot slot : freeSlots(carrier, GoodsType.TOOLS)) {
            if (remaining <= 0) {
                return;
            }
            final int amount = affordableTools(
                    Math.min(remaining, slot.getAvailableCapacity()));
            if (amount == 0) {
                return;
            }
            final Goods tools = Goods.of(GoodsType.TOOLS, amount);
            final Goods bought;
            try {
                bought = slot.buyAndStoreFromEuropePort(tools,
                        player().quoteEuropeTrade(tools, true));
            } catch (final IllegalArgumentException | IllegalStateException
                    | MicroColException refused) {
                LoggerFactory.getLogger(RivalTradeCycle.class)
                        .debug("Rival tool purchase was refused", refused);
                return;
            }
            if (bought.isZero()) {
                return;
            }
            remaining -= bought.getAmount();
        }
    }

    /**
     * Tools in the holds of every ship the player owns, including the carrier.
     * They are on their way or waiting to be unloaded, so they count toward the
     * colony target and keep a second purchase from duplicating the supply.
     */
    private int toolsOnOwnShips() {
        return player().getAllUnits().stream()
                .filter(unit -> unit instanceof Ship).map(Ship.class::cast)
                .mapToInt(ship -> holdAmount(ship, GoodsType.TOOLS)).sum();
    }

    private boolean inEurope(final Leg leg) {
        if (leg.objective.getStage() == Stage.TO_EUROPE && !trade(leg)) {
            return false;
        }
        return departFromEurope(leg);
    }

    /**
     * Return to the saved sea-lane square, or to the default arrival lane. The
     * model's return-route rule must reach the colony first; otherwise the ship
     * stays in Europe with its cargo and the next owner turn retries.
     */
    private boolean departFromEurope(final Leg leg) {
        final Ship carrier = leg.carrier;
        if (!carrier.getAction().isNoAction() || !model()
                .isEuropeReturnReachable(carrier, leg.colony.getLocation())) {
            return false;
        }
        try {
            carrier.placeToHighSeas(false);
        } catch (final IllegalArgumentException | IllegalStateException
                | MicroColException refused) {
            LoggerFactory.getLogger(RivalTradeCycle.class)
                    .debug("Rival return voyage was refused", refused);
            return false;
        }
        robot.waitForAnimations();
        return true;
    }

    private void setStage(final Leg leg, final Stage stage) {
        player().setRivalTradeObjective(leg.objective.withStage(stage));
    }

    private static List<CargoSlot> holdingSlots(final Ship ship,
            final GoodsType type) {
        return ship.getCargo().getSlots().stream()
                .filter(slot -> slot.getGoods()
                        .map(goods -> goods.getType().equals(type))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    /** Empty holds and holds of this goods type that still have room. */
    private static List<CargoSlot> freeSlots(final Ship ship,
            final GoodsType type) {
        return ship.getCargo().getSlots().stream()
                .filter(slot -> slot.getAvailableCapacity() > 0)
                .filter(slot -> slot.getGoods()
                        .map(goods -> goods.getType().equals(type))
                        .orElse(true))
                .collect(Collectors.toList());
    }

    private static int holdAmount(final Ship ship, final GoodsType type) {
        return holdingSlots(ship, type).stream()
                .mapToInt(slot -> slot.getGoods().get().getAmount()).sum();
    }

    private static int freeCapacity(final Ship ship, final GoodsType type) {
        return freeSlots(ship, type).stream()
                .mapToInt(CargoSlot::getAvailableCapacity).sum();
    }

    private static int warehouseAmount(final Colony colony,
            final GoodsType type) {
        return colony.getWarehouse().getGoods(type).getAmount();
    }

    /** The saved cycle with its live colony and carrier. */
    private static final class Leg {
        private final RivalTradeObjective objective;
        private final Colony colony;
        private final Ship carrier;

        private Leg(final RivalTradeObjective objective, final Colony colony,
                final Ship carrier) {
            this.objective = objective;
            this.colony = colony;
            this.carrier = carrier;
        }
    }
}
