package org.microcol.model.store;

import org.microcol.model.GoodsType;
import org.microcol.model.Location;

public final class ColonyFieldPo {

    private Location direction;

    private Integer workerId;

    private GoodsType producedGoodsType;

    /**
     * @return the direction
     */
    public Location getDirection() {
        return direction;
    }

    /**
     * @param direction
     *            the direction to set
     */
    public void setDirection(Location direction) {
        this.direction = direction;
    }

    /**
     * @return the workerId
     */
    public Integer getWorkerId() {
        return workerId;
    }

    /**
     * @param workerId
     *            the workerId to set
     */
    public void setWorkerId(Integer workerId) {
        this.workerId = workerId;
    }

    /**
     * @return the producedGoodsType
     */
    public GoodsType getProducedGoodsType() {
        return producedGoodsType;
    }

    /**
     * @param producedGoodsType
     *            the producedGoodsType to set
     */
    public void setProducedGoodsType(GoodsType producedGoodsType) {
        this.producedGoodsType = producedGoodsType;
    }

}
