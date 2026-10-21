package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.Preconditions;

/**
 * One processing of a route stop for a carrier that {@link Model} has already
 * validated. Entries run unload first, then load, each list in its saved order.
 * Every transfer is a call to the existing atomic cargo, warehouse or European
 * trade operation, so a refused call changes nothing by itself. The carrier's
 * visit is marked as soon as the first quantity is settled, so no later failure
 * can let the stop repeat a transfer that was already paid.
 */
final class TradeRouteStopProcessor {

    /** Quantity and treasury change settled so far by the current entry. */
    private static final class Tally {
        private int amount;
        private int gold;
    }

    private final Model model;
    private final UnitWithCargo carrier;
    private final TradeRouteStop stop;
    private final TradeRouteVisit visit;
    private final List<TradeRouteTransfer> transfers = new ArrayList<>();

    TradeRouteStopProcessor(final Model model, final UnitWithCargo carrier,
            final TradeRouteStop stop, final TradeRouteVisit visit) {
        this.model = Preconditions.checkNotNull(model);
        this.carrier = Preconditions.checkNotNull(carrier);
        this.stop = Preconditions.checkNotNull(stop);
        this.visit = Preconditions.checkNotNull(visit);
    }

    TradeRouteStopResult run() {
        boolean halted = false;
        for (final GoodsType type : stop.getUnloadGoods()) {
            halted = step(true, type, halted);
        }
        for (final GoodsType type : stop.getLoadGoods()) {
            halted = step(false, type, halted);
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
        try {
            transfers.add(execute(unload, type, tally));
            return false;
        } catch (final IllegalArgumentException | IllegalStateException
                | ArithmeticException | MicroColException rejected) {
            // Quantities settled before the refusal stay reported; the visit
            // was already marked when the first of them was settled.
            transfers.add(new TradeRouteTransfer(unload, type, tally.amount,
                    tally.gold, TradeRouteTransferReason.REJECTED,
                    String.valueOf(rejected.getMessage())));
            return true;
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
