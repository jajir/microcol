package org.microcol.model.store;

import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;

public class ColonyBuildingQueueItemPo {

    private QueueItemType type;

    private int id;

    private int buildHammers;

    private UnitType unitType;

    private ConstructionType constructionType;

    /**
     * @return the type
     */
    public QueueItemType getType() {
        return type;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(QueueItemType type) {
        this.type = type;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the buildHammers
     */
    public int getBuildHammers() {
        return buildHammers;
    }

    /**
     * @param buildHammers
     *            the buildHammers to set
     */
    public void setBuildHammers(int buildHammers) {
        this.buildHammers = buildHammers;
    }

    /**
     * @return the unitType
     */
    public UnitType getUnitType() {
        return unitType;
    }

    /**
     * @param unitType
     *            the unitType to set
     */
    public void setUnitType(UnitType unitType) {
        this.unitType = unitType;
    }

    /**
     * @return the constructionType
     */
    public ConstructionType getConstructionType() {
        return constructionType;
    }

    /**
     * @param constructionType
     *            the constructionType to set
     */
    public void setConstructionType(ConstructionType constructionType) {
        this.constructionType = constructionType;
    }

}
