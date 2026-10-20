package org.microcol.model;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

/**
 * Immutable terms of one disbanding: the equipment the unit carries, the part a
 * receiving colony keeps and the part that is lost. Confirming requires the
 * offer the player saw to equal the terms offered now, including the place and
 * the warehouse capacity.
 */
public final class DisbandOffer {

    private final int unitId;

    private final Place place;

    private final Colony receivingColony;

    private final Map<GoodsType, Integer> carried;

    private final Map<GoodsType, Integer> retained;

    private final Map<GoodsType, Integer> lost;

    private final Map<GoodsType, Integer> capacity;

    DisbandOffer(final int unitId, final Place place,
            final Colony receivingColony, final Map<GoodsType, Integer> carried,
            final Map<GoodsType, Integer> retained,
            final Map<GoodsType, Integer> lost,
            final Map<GoodsType, Integer> capacity) {
        this.unitId = unitId;
        this.place = Preconditions.checkNotNull(place);
        this.receivingColony = receivingColony;
        this.carried = ImmutableMap.copyOf(carried);
        this.retained = ImmutableMap.copyOf(retained);
        this.lost = ImmutableMap.copyOf(lost);
        this.capacity = ImmutableMap.copyOf(capacity);
    }

    /** @return the disbanded unit */
    public int getUnitId() {
        return unitId;
    }

    /**
     * @return the colony that receives the retained equipment, empty when all
     *         equipment is lost
     */
    public Optional<Colony> getReceivingColony() {
        return Optional.ofNullable(receivingColony);
    }

    /** @return equipment amount the unit carries */
    public int getCarried(final GoodsType type) {
        return carried.getOrDefault(type, 0);
    }

    /** @return equipment amount the receiving warehouse keeps */
    public int getRetained(final GoodsType type) {
        return retained.getOrDefault(type, 0);
    }

    /** @return equipment amount that is lost */
    public int getLost(final GoodsType type) {
        return lost.getOrDefault(type, 0);
    }

    /** @return the receiving warehouse limit, or 0 without a colony */
    public int getCapacity(final GoodsType type) {
        return capacity.getOrDefault(type, 0);
    }

    /**
     * @return whether a full receiving warehouse makes the player lose part of
     *         the equipment, which must be acknowledged before confirming
     */
    public boolean hasExcessLoss() {
        return receivingColony != null
                && lost.values().stream().anyMatch(amount -> amount > 0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(unitId, place, receivingColony, carried, retained,
                lost, capacity);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DisbandOffer)) {
            return false;
        }
        final DisbandOffer other = (DisbandOffer) obj;
        return unitId == other.unitId && place == other.place
                && Objects.equals(receivingColony, other.receivingColony)
                && carried.equals(other.carried)
                && retained.equals(other.retained) && lost.equals(other.lost)
                && capacity.equals(other.capacity);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("unitId", unitId)
                .add("place", place).add("receivingColony", receivingColony)
                .add("carried", carried).add("retained", retained)
                .add("lost", lost).add("capacity", capacity).toString();
    }
}
