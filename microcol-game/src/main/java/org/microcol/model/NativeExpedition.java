package org.microcol.model;

import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * One carrier's trading expedition at a native settlement (R-0159). It is open
 * only while the same carrier stands on the anchor square in the same
 * {@link Place} object it had when the expedition began. Any move, embarkation,
 * voyage or loss replaces or removes that place, so the expedition ends at
 * physical departure without a hook in the movement code. The place is
 * runtime-only and is bound again when a save is loaded.
 *
 * <p>
 * The village refusal is fixed when the expedition starts, from the goods the
 * village last received before it, and buying or receiving inside this
 * expedition does not change it.
 */
final class NativeExpedition {

    private final int carrierId;

    private final Location anchor;

    private final GoodsType blockedGoods;

    private Place place;

    /**
     * Goods of the one haggle of this expedition, or null before any haggle.
     * The draw is kept with it, so reopening never rerolls.
     */
    private GoodsType haggledGoods;

    /** Result of the haggle drawn for {@link #haggledGoods}. */
    private boolean haggleAccepted;

    /**
     * Revision of the confirmed sale or gift that opened the second stage
     * (R-0161), or null when no purchase is pending. Reopening never changes
     * it.
     */
    private Integer purchaseRevision;

    /**
     * Result of the one request of the pending purchase; {@code OFFERED} until
     * it is drawn, and meaningless without {@link #purchaseRevision}.
     */
    private NativeSaleQuote.Haggle purchaseHaggle = NativeSaleQuote.Haggle.OFFERED;

    /**
     * Goods the one request of the pending purchase was made for, or null
     * before the request. This is the offer the player was shown, kept so a
     * reopened or continued chooser shows the same offer; it is not a purchase.
     */
    private GoodsType purchaseGoods;

    /** Pieces of {@link #purchaseGoods} the request was made for, or null. */
    private Integer purchaseQuantity;

    /**
     * Pieces of {@link #haggledGoods} the sale haggle was made for (R-0160
     * offer kept for the reopened chooser), or null when none was recorded.
     */
    private Integer haggledQuantity;

    NativeExpedition(final int carrierId, final Location anchor,
            final GoodsType blockedGoods) {
        this.carrierId = carrierId;
        this.anchor = Preconditions.checkNotNull(anchor);
        this.blockedGoods = blockedGoods;
    }

    /**
     * @return goods the one haggle of this expedition was drawn for, empty
     *         before any haggle
     */
    Optional<GoodsType> getHaggledGoods() {
        return Optional.ofNullable(haggledGoods);
    }

    /**
     * @return whether the drawn haggle succeeded; meaningful only with
     *         {@link #getHaggledGoods()} present
     */
    boolean isHaggleAccepted() {
        return haggleAccepted;
    }

    /**
     * Record the one haggle of this expedition, once.
     *
     * @param goods    required goods the haggle was drawn for
     * @param accepted whether the drawn haggle succeeded
     */
    void recordHaggle(final GoodsType goods, final boolean accepted,
            final int quantity) {
        Preconditions.checkState(haggledGoods == null,
                "Native haggle was already drawn in this expedition");
        Preconditions.checkArgument(quantity > 0,
                "Native haggle offer needs positive pieces");
        restoreHaggle(goods, accepted);
        haggledQuantity = quantity;
    }

    /**
     * Restore a saved haggle; the caller has validated it.
     *
     * @param goods    required goods of the saved haggle
     * @param accepted saved result
     */
    void restoreHaggle(final GoodsType goods, final boolean accepted) {
        this.haggledGoods = Preconditions.checkNotNull(goods);
        this.haggleAccepted = accepted;
    }

    /**
     * @return revision of the sale or gift that opened the pending purchase,
     *         empty when no purchase is pending
     */
    Optional<Integer> getPurchaseRevision() {
        return Optional.ofNullable(purchaseRevision);
    }

    /**
     * @return result of the one request of the pending purchase
     */
    NativeSaleQuote.Haggle getPurchaseHaggle() {
        return purchaseHaggle;
    }

    /**
     * Open the second stage after a confirmed sale or gift (R-0161). A new sale
     * or gift replaces an earlier stage, and its request is open again.
     *
     * @param revision transaction revision that the sale or gift produced
     */
    void openPurchase(final int revision) {
        purchaseRevision = revision;
        purchaseHaggle = NativeSaleQuote.Haggle.OFFERED;
        purchaseGoods = null;
        purchaseQuantity = null;
    }

    /**
     * @return goods the request of the pending purchase was made for, empty
     *         before the request
     */
    Optional<GoodsType> getPurchaseGoods() {
        return Optional.ofNullable(purchaseGoods);
    }

    /**
     * @return pieces the request of the pending purchase was made for, empty
     *         before the request
     */
    Optional<Integer> getPurchaseQuantity() {
        return Optional.ofNullable(purchaseQuantity);
    }

    /**
     * @return pieces the sale haggle was made for, empty when none was recorded
     */
    Optional<Integer> getHaggledQuantity() {
        return Optional.ofNullable(haggledQuantity);
    }

    /**
     * Draw the one request of the pending purchase, once.
     *
     * @param accepted whether the drawn request succeeded
     * @param goods    required goods of the offer the request was made for
     * @param quantity positive pieces of that offer
     */
    void recordPurchaseHaggle(final boolean accepted, final GoodsType goods,
            final int quantity) {
        Preconditions.checkNotNull(goods);
        Preconditions.checkArgument(quantity > 0,
                "Native purchase offer needs positive pieces");
        Preconditions.checkState(
                purchaseRevision != null
                        && purchaseHaggle == NativeSaleQuote.Haggle.OFFERED,
                "Native purchase request is not open");
        purchaseGoods = goods;
        purchaseQuantity = quantity;
        purchaseHaggle = accepted ? NativeSaleQuote.Haggle.ACCEPTED
                : NativeSaleQuote.Haggle.REFUSED;
    }

    /**
     * End the pending purchase, after it was bought or declined.
     */
    void closePurchase() {
        purchaseRevision = null;
        purchaseGoods = null;
        purchaseQuantity = null;
        purchaseHaggle = NativeSaleQuote.Haggle.OFFERED;
    }

    /**
     * Restore a saved pending purchase; the caller has validated it.
     *
     * @param revision saved revision that opened the stage
     * @param haggle   required saved request result
     * @param goods    goods of the offer the request was made for, or null
     * @param quantity pieces of that offer, or null; present exactly with goods
     */
    void restorePurchase(final int revision,
            final NativeSaleQuote.Haggle haggle, final GoodsType goods,
            final Integer quantity) {
        purchaseRevision = revision;
        purchaseHaggle = Preconditions.checkNotNull(haggle);
        purchaseGoods = goods;
        purchaseQuantity = quantity;
    }

    /**
     * Restore the pieces of a saved sale haggle; the caller has validated it.
     *
     * @param quantity positive saved pieces, or null when none was recorded
     */
    void restoreHaggledQuantity(final Integer quantity) {
        haggledQuantity = quantity;
    }

    /**
     * @return identity of the carrier whose expedition this is
     */
    int getCarrierId() {
        return carrierId;
    }

    /**
     * @return square the carrier stood on when the expedition began
     */
    Location getAnchor() {
        return anchor;
    }

    /**
     * @return goods a village refuses in this expedition, empty when none
     */
    Optional<GoodsType> getBlockedGoods() {
        return Optional.ofNullable(blockedGoods);
    }

    /**
     * Bind a restored expedition to the live place of its carrier.
     *
     * @param boundPlace required current place of the carrier
     */
    void bindPlace(final Place boundPlace) {
        this.place = Preconditions.checkNotNull(boundPlace);
    }

    /**
     * Remember the place the carrier stands in when the expedition begins.
     *
     * @param startPlace required place of the carrier now
     */
    void startAt(final Place startPlace) {
        this.place = Preconditions.checkNotNull(startPlace);
    }

    /**
     * @param carrier required unit
     * @return whether this expedition is still open for that carrier now
     */
    boolean isOpenFor(final Unit carrier) {
        return carrier.getId() == carrierId && place != null
                && carrier.isAtPlaceLocation() && carrier.getPlace() == place
                && carrier.getLocation().equals(anchor);
    }

}
