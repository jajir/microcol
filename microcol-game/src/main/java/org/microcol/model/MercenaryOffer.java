package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Paid bundle of veteran soldiers that a European nation offers the owner
 * (R-0169). The offer is a snapshot of the issuer and price; it lasts until the
 * owner declines, accepts or ends the turn.
 */
public final class MercenaryOffer {

    private final Player owner;

    private final Player issuer;

    private final int price;

    MercenaryOffer(final Player owner, final Player issuer, final int price) {
        this.owner = Preconditions.checkNotNull(owner, "Owner is null");
        this.issuer = Preconditions.checkNotNull(issuer, "Issuer is null");
        Preconditions.checkArgument(price > 0, "Price must be positive");
        this.price = price;
    }

    /** @return colonial nation that receives the bundle */
    public Player getOwner() {
        return owner;
    }

    /** @return European nation that offers the bundle */
    public Player getIssuer() {
        return issuer;
    }

    /** @return gold the owner pays for the whole bundle */
    public int getPrice() {
        return price;
    }

    /** @return number of veteran soldiers in the bundle */
    public int getSoldiers() {
        return MercenaryRules.SOLDIERS;
    }
}
