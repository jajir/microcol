package org.microcol.model;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

/**
 * Trade of one settlement with one European nation (R-0159): the goods the
 * village received most recently from a carrier, and the open expeditions of
 * each carrier. Several carriers may hold expeditions at once; a new one never
 * replaces another carrier's expedition, and reopening an expedition reuses it.
 */
final class NativeTradeState {

    /** Goods the village received most recently from a carrier, or null. */
    private GoodsType lastReceived;

    /** Expeditions by carrier identity, in identity order. */
    private final Map<Integer, NativeExpedition> expeditions = new TreeMap<>();

    /**
     * Transaction revision (R-0160): counts every expedition start, haggle draw
     * and completed sale or gift of this trade. Offers carry it, so an offer is
     * dead once any of these has happened, even when the values read the same.
     */
    private int revision;

    NativeTradeState(final GoodsType lastReceived) {
        this.lastReceived = lastReceived;
    }

    /**
     * @return the transaction revision, zero before any change
     */
    int getRevision() {
        return revision;
    }

    /**
     * @return whether one more transaction can be counted; the callers check
     *         this before their first change, so counting never fails midway
     */
    boolean canAdvanceRevision() {
        return revision < Integer.MAX_VALUE;
    }

    /**
     * Count one transaction of this trade; the caller has validated it.
     */
    void advanceRevision() {
        revision = Math.incrementExact(revision);
    }

    /**
     * Restore the saved revision.
     *
     * @param savedRevision nonnegative saved count
     */
    void restoreRevision(final int savedRevision) {
        Preconditions.checkArgument(savedRevision >= 0,
                "Native trade revision cannot be negative");
        revision = savedRevision;
    }

    /**
     * @return goods the village received most recently, empty before any
     */
    Optional<GoodsType> getLastReceived() {
        return Optional.ofNullable(lastReceived);
    }

    /**
     * @return every recorded expedition in carrier identity order, unmodifiable
     */
    Map<Integer, NativeExpedition> getExpeditions() {
        return Collections.unmodifiableMap(expeditions);
    }

    /**
     * @param carrier required unit
     * @return the expedition of that carrier, when it is still open for it
     */
    Optional<NativeExpedition> openExpedition(final Unit carrier) {
        return Optional.ofNullable(expeditions.get(carrier.getId()))
                .filter(expedition -> expedition.isOpenFor(carrier));
    }

    /**
     * Start the expedition of a carrier at its square. The refused goods are
     * the ones the village last received, fixed for this expedition.
     *
     * @param carrier required carrier standing on its square
     * @return the new expedition, which replaces only that carrier's older
     *         record
     */
    NativeExpedition startExpedition(final Unit carrier) {
        Preconditions.checkState(canAdvanceRevision(),
                "The native trade revision is exhausted");
        final NativeExpedition started = new NativeExpedition(carrier.getId(),
                carrier.getLocation(), lastReceived);
        started.startAt(carrier.getPlace());
        expeditions.put(carrier.getId(), started);
        advanceRevision();
        return started;
    }

    /**
     * Record that the village received goods from a carrier.
     *
     * @param goods required goods received
     */
    void recordReceived(final GoodsType goods) {
        lastReceived = goods;
    }

    /**
     * Restore one saved expedition; its place is bound later.
     *
     * @param expedition required restored expedition
     */
    void restoreExpedition(final NativeExpedition expedition) {
        expeditions.put(expedition.getCarrierId(), expedition);
    }

    /**
     * Forget one expedition, for example one whose carrier has already left.
     *
     * @param carrierId identity of the carrier
     */
    void removeExpedition(final int carrierId) {
        expeditions.remove(carrierId);
    }

}
