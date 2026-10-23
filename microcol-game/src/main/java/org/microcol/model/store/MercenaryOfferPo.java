package org.microcol.model.store;

/**
 * Saved pending mercenary offer (R-0169); the issuer name and price are
 * required.
 */
public final class MercenaryOfferPo {

    private String issuer;

    private Integer price;

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(final String issuer) {
        this.issuer = issuer;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(final Integer price) {
        this.price = price;
    }
}
