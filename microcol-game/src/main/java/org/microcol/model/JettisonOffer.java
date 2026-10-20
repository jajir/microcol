package org.microcol.model;

import java.util.Optional;

import org.microcol.model.unit.Ship;

import com.google.common.base.Preconditions;

/**
 * Immutable terms of one jettison: the ship, its place and voyage state, the
 * goods hold and the goods it held when the player was asked. Confirming
 * requires that these terms still hold.
 */
public final class JettisonOffer {

    final Model model;
    final Ship ship;
    final int slotIndex;
    final Goods goods;
    private final Place place;
    private final int voyagePosition;
    private final int voyageRevision;

    JettisonOffer(final Model model, final Ship ship, final int slotIndex,
            final Goods goods) {
        this.model = Preconditions.checkNotNull(model);
        this.ship = Preconditions.checkNotNull(ship);
        this.slotIndex = slotIndex;
        this.goods = Preconditions.checkNotNull(goods);
        this.place = ship.getPlace();
        if (place instanceof PlaceHighSea) {
            voyagePosition = ((PlaceHighSea) place).getPosition();
            voyageRevision = ((PlaceHighSea) place).getRevision();
        } else {
            voyagePosition = -1;
            voyageRevision = -1;
        }
    }

    /** @return the ship that may discard the goods */
    public Ship getShip() {
        return ship;
    }

    /** @return zero-based index of the goods hold */
    public int getSlotIndex() {
        return slotIndex;
    }

    /** @return the goods the hold held when offered, never zero */
    public Goods getGoods() {
        return goods;
    }

    /**
     * @return whether the same ship, place, voyage and hold still hold the
     *         offered goods
     */
    boolean isCurrent() {
        if (ship.getPlace() != place) {
            return false;
        }
        if (place instanceof PlaceHighSea) {
            final PlaceHighSea voyage = (PlaceHighSea) place;
            if (voyage.getPosition() != voyagePosition
                    || voyage.getRevision() != voyageRevision) {
                return false;
            }
        }
        return ship.getCargo().getSlotByIndex(slotIndex).getGoods()
                .equals(Optional.of(goods));
    }
}
