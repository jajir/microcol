package org.akropia.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.akropia.model.turnevent.TurnEventProvider;
import org.akropia.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

/**
 * One processing of a route stop for a carrier that {@link Model} has already
 * validated. Entries run unload first, then load, each in the fixed goods
 * order. A carrier of a route in waiting mode loads only once every goods
 * marked for unloading is off board, so loading never takes the place of an
 * unfinished unloading. Every transfer is a call to the existing atomic cargo,
 * warehouse or European trade operation, so a refused call changes nothing by
 * itself. The carrier's visit is marked as soon as the first quantity is
 * settled, so no later failure can let the stop repeat a transfer that was
 * already paid. In an automatic visit, every entry that settled a quantity,
 * including one rejected part-way, adds one turn notice for the carrier's owner
 * in processing order (R-0392); a stop the player processes explicitly shows
 * its result directly instead.
 */
final class TradeRouteStopProcessor {

    /** Quantity and treasury change settled so far by the current entry. */
    private static final class Tally {
        private int amount;
        private int gold;
    }

    private final Model model;
    private final UnitWithCargo carrier;
    private final TradeRoute route;
    /** Whether the route navigator, not the player, runs this visit. */
    private final boolean automatic;
    private final TradeRouteStop stop;
    /** Position of the stop in the itinerary, as reported to the owner. */
    private final int stopIndex;
    private final TradeRouteVisit visit;
    private final List<TradeRouteTransfer> transfers = new ArrayList<>();

    TradeRouteStopProcessor(final Model model, final UnitWithCargo carrier,
            final TradeRoute route, final int stopIndex,
            final TradeRouteStop stop, final TradeRouteVisit visit,
            final boolean automatic) {
        this.model = Preconditions.checkNotNull(model);
        this.carrier = Preconditions.checkNotNull(carrier);
        this.route = Preconditions.checkNotNull(route);
        this.automatic = automatic;
        this.stop = Preconditions.checkNotNull(stop);
        this.stopIndex = stopIndex;
        this.visit = Preconditions.checkNotNull(visit);
    }

    TradeRouteStopResult run() {
        boolean halted = false;
        for (final GoodsType type : stop.getUnloadGoods()) {
            halted = step(true, type, halted);
        }
        final boolean loadNow = !automatic
                || route.getMode() == TradeRouteMode.TOUCH_AND_GO
                || isUnloadComplete(carrier, stop);
        if (loadNow) {
            for (final GoodsType type : stop.getLoadGoods()) {
                halted = step(false, type, halted);
            }
        }
        // A processing that moved nothing is still a finite, reported visit.
        markVisit();
        return TradeRouteStopResult.processed(transfers);
    }

    /**
     * @return whether processing stopped, because this or an earlier entry was
     *         rejected; later entries are then reported as not attempted
     */
    private boolean step(final boolean unload, final GoodsType type,
            final boolean halted) {
        if (halted) {
            transfers.add(new TradeRouteTransfer(unload, type, 0, 0,
                    TradeRouteTransferReason.NOT_ATTEMPTED, null));
            return true;
        }
        final Tally tally = new Tally();
        final TradeRouteTransfer done;
        try {
            done = execute(unload, type, tally);
        } catch (final IllegalArgumentException | IllegalStateException
                | ArithmeticException | AkropiaException rejected) {
            // Quantities settled before the refusal stay reported; the visit
            // was already marked when the first of them was settled.
            settle(new TradeRouteTransfer(unload, type, tally.amount,
                    tally.gold, TradeRouteTransferReason.REJECTED,
                    String.valueOf(rejected.getMessage())));
            return true;
        }
        settle(done);
        return false;
    }

    /**
     * Keeps the entry result and, in an automatic visit, reports the quantity
     * it actually moved to the owner; an entry that moved nothing is not a
     * transaction.
     */
    private void settle(final TradeRouteTransfer transfer) {
        transfers.add(transfer);
        if (automatic && transfer.getAmount() > 0) {
            model.addTurnEvent(TurnEventProvider.getTradeRouteTransfer(route,
                    stopIndex, carrier,
                    stop.isEurope() ? null
                            : model.getColonyById(stop.getColonyId())
                                    .orElseThrow(),
                    transfer));
        }
    }

    private TradeRouteTransfer execute(final boolean unload,
            final GoodsType type, final Tally tally) {
        if (stop.isEurope()) {
            final Player owner = carrier.getOwner();
            if (!owner.canTradeInEurope()) {
                return entry(unload, type, tally,
                        TradeRouteTransferReason.EUROPE_CLOSED);
            }
            if (owner.isBoycotted(type)) {
                return entry(unload, type, tally,
                        TradeRouteTransferReason.BOYCOTTED);
            }
            return unload ? sellToEurope(type, tally)
                    : buyFromEurope(type, tally);
        }
        final Colony colony = model.getColonyById(stop.getColonyId())
                .orElseThrow();
        return unload ? unloadToWarehouse(colony, type, tally)
                : loadFromWarehouse(colony, type, tally);
    }

    /**
     * Why a carrier in waiting mode must stay at its stop after processing, or
     * empty when the stop is complete. Unloading is complete when no goods
     * marked for unloading is on board; loading when no hold has room for any
     * goods marked for loading. Ignored goods and passengers keep their holds.
     *
     * @param carrier   required carrier at the stop
     * @param stop      required stop with the instructions of this visit
     * @param transfers results of the latest processing, to tell missing funds
     *                  from missing stock
     * @return the waiting reason, or empty when the carrier may continue
     */
    static Optional<TradeRoutePause> getWaitingReason(
            final UnitWithCargo carrier, final TradeRouteStop stop,
            final List<TradeRouteTransfer> transfers) {
        if (!isUnloadComplete(carrier, stop)) {
            return Optional.of(TradeRoutePause.WAITING_UNLOAD);
        }
        final boolean loadComplete = stop.getLoadGoods().stream()
                .allMatch(type -> room(Model.roomForGoods(carrier, type)) == 0);
        if (loadComplete) {
            return Optional.empty();
        }
        final boolean funds = stop.isEurope() && transfers.stream()
                .anyMatch(transfer -> !transfer.isUnload() && (transfer
                        .getReason() == TradeRouteTransferReason.INSUFFICIENT_FUNDS
                        || transfer
                                .getReason() == TradeRouteTransferReason.PARTIAL_FUNDS));
        return Optional.of(funds ? TradeRoutePause.WAITING_FUNDS
                : TradeRoutePause.WAITING_LOAD);
    }

    private static boolean isUnloadComplete(final UnitWithCargo carrier,
            final TradeRouteStop stop) {
        return stop.getUnloadGoods().stream()
                .allMatch(type -> Model.holdingGoods(carrier, type).isEmpty());
    }

    private static TradeRouteTransfer entry(final boolean unload,
            final GoodsType type, final Tally tally,
            final TradeRouteTransferReason reason) {
        return new TradeRouteTransfer(unload, type, tally.amount, tally.gold,
                reason, null);
    }

    private static int total(final List<CargoSlot> slots) {
        return slots.stream()
                .mapToInt(
                        slot -> slot.getGoods().map(Goods::getAmount).orElse(0))
                .sum();
    }

    private static int room(final List<CargoSlot> slots) {
        return slots.stream().mapToInt(CargoSlot::getAvailableCapacity).sum();
    }

    private void markVisit() {
        if (carrier.getProcessedTradeRouteVisit().isEmpty()) {
            carrier.markProcessedTradeRouteVisit(visit);
        }
    }

    private TradeRouteTransfer unloadToWarehouse(final Colony colony,
            final GoodsType type, final Tally tally) {
        final ColonyWarehouse warehouse = colony.getWarehouse();
        final List<CargoSlot> holds = Model.holdingGoods(carrier, type);
        final int held = total(holds);
        for (final CargoSlot slot : holds) {
            final Goods transfer = warehouse
                    .getUnloadableGoods(slot.getGoods().get(), slot);
            if (!transfer.isZero()) {
                warehouse.moveToWarehouse(transfer, slot);
                tally.amount += transfer.getAmount();
                markVisit();
            }
        }
        return entry(true, type, tally, unloadReason(held, tally.amount));
    }

    private TradeRouteTransfer sellToEurope(final GoodsType type,
            final Tally tally) {
        final Player owner = carrier.getOwner();
        final List<CargoSlot> holds = Model.holdingGoods(carrier, type);
        final int held = total(holds);
        for (final CargoSlot slot : holds) {
            final Goods goods = slot.getGoods().get();
            // The quote is read per hold, because each sale moves the price.
            final EuropeTradeQuote quote = owner.quoteEuropeTrade(goods, false);
            final int before = owner.getGold();
            model.sellGoods(slot, goods, quote);
            tally.amount += goods.getAmount();
            tally.gold += owner.getGold() - before;
            markVisit();
        }
        return entry(true, type, tally, unloadReason(held, tally.amount));
    }

    private static TradeRouteTransferReason unloadReason(final int held,
            final int moved) {
        if (held == 0) {
            return TradeRouteTransferReason.NOTHING_ON_BOARD;
        }
        if (moved == held) {
            return TradeRouteTransferReason.MOVED;
        }
        return moved == 0 ? TradeRouteTransferReason.DESTINATION_FULL
                : TradeRouteTransferReason.PARTIAL_DESTINATION_FULL;
    }

    private TradeRouteTransfer loadFromWarehouse(final Colony colony,
            final GoodsType type, final Tally tally) {
        final List<CargoSlot> holds = Model.roomForGoods(carrier, type);
        final int stock = colony.getWarehouse().getGoods(type).getAmount();
        for (final CargoSlot slot : holds) {
            final int amount = Math.min(
                    colony.getWarehouse().getGoods(type).getAmount(),
                    slot.getAvailableCapacity());
            if (amount > 0) {
                slot.storeFromColonyWarehouse(Goods.of(type, amount), colony);
                tally.amount += amount;
                markVisit();
            }
        }
        final TradeRouteTransferReason reason;
        if (stock == 0) {
            reason = TradeRouteTransferReason.SOURCE_EMPTY;
        } else if (tally.amount == stock) {
            reason = TradeRouteTransferReason.MOVED;
        } else {
            reason = tally.amount == 0 ? TradeRouteTransferReason.HOLD_FULL
                    : TradeRouteTransferReason.PARTIAL_HOLD_FULL;
        }
        return entry(false, type, tally, reason);
    }

    private TradeRouteTransfer buyFromEurope(final GoodsType type,
            final Tally tally) {
        final Player owner = carrier.getOwner();
        final List<CargoSlot> holds = Model.roomForGoods(carrier, type);
        final int room = room(holds);
        for (final CargoSlot slot : holds) {
            // Affordability and price are read again for every hold, because
            // an earlier purchase changed both the treasury and the market.
            final int affordable = owner.getMaxBuyableGoods(type).getAmount();
            final int amount = Math.min(affordable,
                    slot.getAvailableCapacity());
            if (amount > 0) {
                final Goods goods = Goods.of(type, amount);
                final EuropeTradeQuote quote = owner.quoteEuropeTrade(goods,
                        true);
                final int before = owner.getGold();
                final Goods bought = slot.buyAndStoreFromEuropePort(goods,
                        quote);
                tally.amount += bought.getAmount();
                tally.gold += owner.getGold() - before;
                markVisit();
            }
        }
        final TradeRouteTransferReason reason;
        if (room == 0) {
            reason = TradeRouteTransferReason.HOLD_FULL;
        } else if (tally.amount == room) {
            reason = TradeRouteTransferReason.MOVED;
        } else {
            reason = tally.amount == 0
                    ? TradeRouteTransferReason.INSUFFICIENT_FUNDS
                    : TradeRouteTransferReason.PARTIAL_FUNDS;
        }
        return entry(false, type, tally, reason);
    }
}
