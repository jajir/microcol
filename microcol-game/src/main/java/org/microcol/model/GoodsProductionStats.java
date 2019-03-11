package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Hold information about production of some good and about stock in warehouse
 * in one turn.
 */
public final class GoodsProductionStats {

    private final GoodsType type;
    private int rowProduction;
    private int consumed;

    /**
     * How many goods items are blocked by missing source goods.
     */
    private int blockedProduction;

    private int inWarehouseBefore;

    GoodsProductionStats(final GoodsType type) {
        this.type = Preconditions.checkNotNull(type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass()).add("type", type)
                .add("rowProduction", rowProduction).add("consumed", consumed)
                .add("blockedProduction", blockedProduction)
                .add("inWarehouseBefore", inWarehouseBefore).toString();
    }

    public void addRowProduction(final int add) {
        rowProduction += add;
    }

    /**
     * Return change of amount after next turn.
     * <p>
     * If it's negative than next turn amount goods will be lower.
     * </p>
     * <p>
     * If it's positive than next turn amount goods will be higher.
     * </p>
     *
     * @return Difference between next run amount of goods and current turn.
     */
    public int getDiff() {
        return getInWarehouseAfter() - getInWarehouseBefore();
    }

    /**
     * @return the rowProduction
     */
    public int getRowProduction() {
        return rowProduction;
    }

    /**
     * @param rowProduction
     *            the rowProduction to set
     */
    public void setRowProduction(final int rowProduction) {
        this.rowProduction = rowProduction;
    }

    /**
     * Net turn production. it's row production minus consumed goods. Could be
     * less than 0, because missing goods is taken from warehouse.
     * 
     * @return the netProduction
     */
    public int getNetProduction() {
        return rowProduction - consumed;
    }

    /**
     * @return the consumed
     */
    public int getConsumed() {
        return consumed;
    }

    /**
     * @param consumed
     *            the consumed to set
     */
    public void addConsumed(int consumed) {
        this.consumed += consumed;
    }

    /**
     * @return the blockedProduction
     */
    public int getBlockedProduction() {
        return blockedProduction;
    }

    /**
     * @param blockedProduction
     *            the blockedProduction to set
     */
    public void setBlockedProduction(int blockedProduction) {
        this.blockedProduction = blockedProduction;
    }

    /**
     * @return the inWarehouseBefore
     */
    public int getInWarehouseBefore() {
        return inWarehouseBefore;
    }

    /**
     * @param inWarehouseBefore
     *            the inWarehouseBefore to set
     */
    public void setInWarehouseBefore(int inWarehouseBefore) {
        this.inWarehouseBefore = inWarehouseBefore;
    }

    /**
     * @return the inWarehouseAfter
     */
    public int getInWarehouseAfter() {
        return getInWarehouseBefore() + getNetProduction();
    }

    /**
     * @return the type
     */
    public GoodsType getType() {
        return type;
    }

}