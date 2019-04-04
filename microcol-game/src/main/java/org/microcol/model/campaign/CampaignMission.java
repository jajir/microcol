package org.microcol.model.campaign;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Abstract campaign mission. Holds basic info about campaign mission.
 */
public final class CampaignMission {

    private final MissionName missionName;

    private final Integer orderNo;

    private boolean isFinished;

    CampaignMission(final MissionName missionName, final Integer orderNo) {
        this.missionName = Preconditions.checkNotNull(missionName);
        this.orderNo = Preconditions.checkNotNull(orderNo);
        setFinished(false);
    }

    public String getName() {
        return missionName.getName();
    }

    public Integer getOrderNo() {
        return orderNo;
    }

    public boolean isFinished() {
        return isFinished;
    }

    /**
     * @param isFinished
     *            the isFinished to set
     */
    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("name", missionName.getName())
                .add("isFinished", isFinished).add("orderNo", orderNo).toString();
    }

    public String getClassPathFile() {
        return missionName.getClassPathFile();
    }

    public MissionName getMissionName() {
        return missionName;
    }

}
