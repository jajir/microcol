package org.microcol.model.store;

/**
 * Saved paid mercenary group (R-0169); every field is required when present.
 */
public final class MercenaryDeliveryPo {

    private Integer portColonyId;

    private Integer price;

    private Boolean delivered;

    public Integer getPortColonyId() {
        return portColonyId;
    }

    public void setPortColonyId(final Integer portColonyId) {
        this.portColonyId = portColonyId;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(final Integer price) {
        this.price = price;
    }

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(final Boolean delivered) {
        this.delivered = delivered;
    }
}
