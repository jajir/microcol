package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Immutable terms of one quick load or unload. Confirming requires that the
 * terms offered now equal the terms the player saw: carrier, colony, place,
 * hold, goods, warehouse stock and room, hold contents and room, and for
 * loading the one-unit sale quote that ranked the goods.
 */
public final class QuickCargoOffer {

    private final UnitWithCargo carrier;

    private final Colony colony;

    private final QuickCargoAction action;

    private final Location place;

    private final int slotIndex;

    private final Goods goods;

    private final int warehouseStock;

    private final int warehouseRoom;

    private final int holdStock;

    private final int holdRoom;

    private final EuropeTradeQuote unitQuote;

    QuickCargoOffer(final UnitWithCargo carrier, final Colony colony,
            final QuickCargoAction action, final Location place,
            final int slotIndex, final Goods goods, final int warehouseStock,
            final int warehouseRoom, final int holdStock, final int holdRoom,
            final EuropeTradeQuote unitQuote) {
        this.carrier = Preconditions.checkNotNull(carrier);
        this.colony = Preconditions.checkNotNull(colony);
        this.action = Preconditions.checkNotNull(action);
        this.place = Preconditions.checkNotNull(place);
        this.slotIndex = slotIndex;
        this.goods = Preconditions.checkNotNull(goods);
        this.warehouseStock = warehouseStock;
        this.warehouseRoom = warehouseRoom;
        this.holdStock = holdStock;
        this.holdRoom = holdRoom;
        this.unitQuote = unitQuote;
    }

    UnitWithCargo getCarrier() {
        return carrier;
    }

    Colony getColony() {
        return colony;
    }

    /** @return whether goods move into the carrier or into the warehouse */
    public QuickCargoAction getAction() {
        return action;
    }

    /** @return zero-based index of the hold that the transfer uses */
    public int getSlotIndex() {
        return slotIndex;
    }

    /** @return exact goods and amount that move, never zero */
    public Goods getGoods() {
        return goods;
    }

    /**
     * @return one-unit tax-net sale quote that ranked the goods, present only
     *         for loading
     */
    public Optional<EuropeTradeQuote> getUnitQuote() {
        return Optional.ofNullable(unitQuote);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QuickCargoOffer)) {
            return false;
        }
        final QuickCargoOffer other = (QuickCargoOffer) obj;
        return carrier == other.carrier && colony == other.colony
                && action == other.action && place.equals(other.place)
                && slotIndex == other.slotIndex && goods.equals(other.goods)
                && warehouseStock == other.warehouseStock
                && warehouseRoom == other.warehouseRoom
                && holdStock == other.holdStock && holdRoom == other.holdRoom
                && Objects.equals(unitQuote, other.unitQuote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(action, place, slotIndex, goods, warehouseStock,
                warehouseRoom, holdStock, holdRoom, unitQuote);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("action", action)
                .add("place", place).add("slotIndex", slotIndex)
                .add("goods", goods).add("warehouseStock", warehouseStock)
                .add("warehouseRoom", warehouseRoom).add("holdStock", holdStock)
                .add("holdRoom", holdRoom).add("unitQuote", unitQuote)
                .toString();
    }
}
