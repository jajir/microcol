package org.microcol.model.campaign;

import com.google.common.base.Preconditions;

public class AbstractMission implements Mission {

    private final String name;

    /**
     * Class path related mission model file name. It's something like
     * <i>/maps/03-before-game-over.json</i>.
     */
    private final String modelFileName;

    private final Integer orderNo;

    AbstractMission(final String name, final Integer orderNo, final String modelFileName) {
        this.name = Preconditions.checkNotNull(name);
        this.orderNo = Preconditions.checkNotNull(orderNo);
        this.modelFileName = Preconditions.checkNotNull(modelFileName);
    }

    /* (non-Javadoc)
     * @see org.microcol.model.campaign.Mission#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see org.microcol.model.campaign.Mission#getOrderNo()
     */
    @Override
    public Integer getOrderNo() {
        return orderNo;
    }

    /* (non-Javadoc)
     * @see org.microcol.model.campaign.Mission#getModelFileName()
     */
    @Override
    public String getModelFileName() {
        return modelFileName;
    }

}
