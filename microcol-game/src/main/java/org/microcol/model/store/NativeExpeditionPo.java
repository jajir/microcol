package org.microcol.model.store;

import org.microcol.model.Location;

/**
 * One open trading expedition of a carrier at a settlement. The carrier and the
 * anchor square are required; the refused goods are null when none applies. A
 * recorded expedition whose carrier is no longer on that square is not saved.
 */
public class NativeExpeditionPo {

    /** Identity of the carrier. */
    private Integer carrierId;

    /** Square the carrier stood on when the expedition began. */
    private Location anchor;

    /** Goods name a village refuses in this expedition, or null. */
    private String blockedGoods;

    /**
     * Goods name of the one haggle drawn in this expedition, or null; present
     * only with {@link #haggleAccepted}.
     */
    private String haggledGoods;

    /** Saved result of the haggle, or null when none was drawn. */
    private Boolean haggleAccepted;

    /**
     * Revision of the sale or gift that opened the pending purchase (R-0161),
     * or null when no purchase is pending.
     */
    private Integer purchaseRevision;

    /**
     * Name of the pending purchase's one request state, OFFERED, ACCEPTED or
     * REFUSED; null exactly when no purchase is pending.
     */
    private String purchaseHaggle;

    /**
     * Goods name of the offer the pending purchase's request was made for, or
     * null before the request; present exactly with {@link #purchaseQuantity}.
     */
    private String purchaseGoods;

    /** Pieces of that offer as they were shown, or null before the request. */
    private Integer purchaseQuantity;

    /**
     * Pieces of the sale offer the haggle was made for, or null when none was
     * recorded; only valid with {@link #haggledGoods}.
     */
    private Integer haggledQuantity;

    public Integer getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(final Integer carrierId) {
        this.carrierId = carrierId;
    }

    public Location getAnchor() {
        return anchor;
    }

    public void setAnchor(final Location anchor) {
        this.anchor = anchor;
    }

    public String getBlockedGoods() {
        return blockedGoods;
    }

    public String getHaggledGoods() {
        return haggledGoods;
    }

    public void setHaggledGoods(final String haggledGoods) {
        this.haggledGoods = haggledGoods;
    }

    public Boolean getHaggleAccepted() {
        return haggleAccepted;
    }

    public void setHaggleAccepted(final Boolean haggleAccepted) {
        this.haggleAccepted = haggleAccepted;
    }

    public Integer getPurchaseRevision() {
        return purchaseRevision;
    }

    public void setPurchaseRevision(final Integer purchaseRevision) {
        this.purchaseRevision = purchaseRevision;
    }

    public String getPurchaseGoods() {
        return purchaseGoods;
    }

    public void setPurchaseGoods(final String purchaseGoods) {
        this.purchaseGoods = purchaseGoods;
    }

    public Integer getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(final Integer purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }

    public Integer getHaggledQuantity() {
        return haggledQuantity;
    }

    public void setHaggledQuantity(final Integer haggledQuantity) {
        this.haggledQuantity = haggledQuantity;
    }

    public String getPurchaseHaggle() {
        return purchaseHaggle;
    }

    public void setPurchaseHaggle(final String purchaseHaggle) {
        this.purchaseHaggle = purchaseHaggle;
    }

    public void setBlockedGoods(final String blockedGoods) {
        this.blockedGoods = blockedGoods;
    }

}
