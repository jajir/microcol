package org.microcol.model.campaign;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class AbstractMission implements Mission {

    private final String name;

    /**
     * Class path related mission model file name. It's something like
     * <i>/maps/03-before-game-over.json</i>.
     */
    private final String modelFileName;

    private final Integer orderNo;

    private boolean isFinished;

    AbstractMission(final String name, final Integer orderNo, final String modelFileName) {
        this.name = Preconditions.checkNotNull(name);
        this.orderNo = Preconditions.checkNotNull(orderNo);
        this.modelFileName = Preconditions.checkNotNull(modelFileName);
        setFinished(false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Mission#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Mission#getOrderNo()
     */
    @Override
    public Integer getOrderNo() {
        return orderNo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.microcol.model.campaign.Mission#getModelFileName()
     */
    @Override
    public String getModelFileName() {
        return modelFileName;
    }

    @Override
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
        return MoreObjects.toStringHelper(getClass()).add("name", name).add("orderNo", orderNo)
                .add("modelFileName", modelFileName).toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (isFinished ? 1231 : 1237);
        result = prime * result + ((modelFileName == null) ? 0 : modelFileName.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((orderNo == null) ? 0 : orderNo.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractMission other = (AbstractMission) obj;
        if (isFinished != other.isFinished)
            return false;
        if (modelFileName == null) {
            if (other.modelFileName != null)
                return false;
        } else if (!modelFileName.equals(other.modelFileName))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (orderNo == null) {
            if (other.orderNo != null)
                return false;
        } else if (!orderNo.equals(other.orderNo))
            return false;
        return true;
    }

}
