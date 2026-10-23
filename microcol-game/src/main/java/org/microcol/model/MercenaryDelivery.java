package org.microcol.model;

import org.microcol.model.store.MercenaryDeliveryPo;

import com.google.common.base.Preconditions;

/**
 * A mercenary group the owner has paid for (R-0169). The payment is made once
 * when accepted; the soldiers are placed once, at the first delivery that finds
 * the port open. Until then the group stays pending and is never paid again.
 */
public final class MercenaryDelivery {

    private final int portColonyId;

    private final int price;

    private boolean delivered;

    MercenaryDelivery(final int portColonyId, final int price,
            final boolean delivered) {
        Preconditions.checkArgument(portColonyId >= 0,
                "Port identity must be nonnegative");
        Preconditions.checkArgument(price > 0, "Price must be positive");
        this.portColonyId = portColonyId;
        this.price = price;
        this.delivered = delivered;
    }

    /**
     * Load a saved group; missing or incoherent identity data is rejected.
     *
     * @param po required saved group
     */
    MercenaryDelivery(final MercenaryDeliveryPo po) {
        this(validatedPortId(po), validatedPrice(po), po.getDelivered());
    }

    private static int validatedPortId(final MercenaryDeliveryPo po) {
        Preconditions.checkArgument(
                po != null && po.getPortColonyId() != null
                        && po.getDelivered() != null,
                "Incomplete mercenary delivery");
        return po.getPortColonyId();
    }

    private static int validatedPrice(final MercenaryDeliveryPo po) {
        Preconditions.checkArgument(po.getPrice() != null,
                "Incomplete mercenary delivery");
        return po.getPrice();
    }

    /** @return colony identity of the port the owner chose */
    public int getPortColonyId() {
        return portColonyId;
    }

    /** @return gold paid for this group */
    public int getPrice() {
        return price;
    }

    /** @return whether the soldiers were placed; never reverts */
    public boolean isDelivered() {
        return delivered;
    }

    /** Record the placement once every soldier exists. */
    void markDelivered() {
        Preconditions.checkState(!delivered,
                "The mercenary group is already delivered");
        delivered = true;
    }

    MercenaryDeliveryPo save() {
        final MercenaryDeliveryPo po = new MercenaryDeliveryPo();
        po.setPortColonyId(portColonyId);
        po.setPrice(price);
        po.setDelivered(delivered);
        return po;
    }
}
