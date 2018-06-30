package org.microcol.model.campaign;

import java.util.function.Supplier;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Abstract campaign mission. Holds basic info about campaign mission.
 */
public class DefaultCampaignMission implements CampaignMission {

    private final String name;

    private final Integer orderNo;

    private final Supplier<Mission> missionSupplier;

    private boolean isFinished;

    DefaultCampaignMission(final String name, final Integer orderNo,
            final Supplier<Mission> missionSupplier) {
        this.name = Preconditions.checkNotNull(name);
        this.orderNo = Preconditions.checkNotNull(orderNo);
        this.missionSupplier = Preconditions.checkNotNull(missionSupplier);
        setFinished(false);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getOrderNo() {
        return orderNo;
    }

    @Override
    public boolean isFinished() {
        return isFinished;
    }

    /**
     * @param isFinished
     *            the isFinished to set
     */
    @Override
    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("name", name)
                .add("isFinished", isFinished).add("orderNo", orderNo).toString();
    }

    @Override
    public Mission makeMission() {
        return missionSupplier.get();
    }

}
