package org.microcol.model.store;

import org.microcol.model.GoodType;
import org.microcol.model.Location;

public final class ColonyFieldPo {

    private Location direction;

    private Integer workerId;

    private GoodType producedGoodType;

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
     * @return the producedGoodType
     */
    public GoodType getProducedGoodType() {
        return producedGoodType;
    }

    /**
     * @param producedGoodType
     *            the producedGoodType to set
     */
    public void setProducedGoodType(GoodType producedGoodType) {
        this.producedGoodType = producedGoodType;
    }

}
