package org.microcol.model.store;

import java.util.List;

import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.UnitType;
import org.microcol.model.Founder;

public final class TurnEventPo {

    private String repairKind;
    private Integer repairShipId;
    private UnitType repairShipType;
    private String repairDestinationName;
    private Boolean repairEuropeDestination;
    private Integer repairLostPassengers;
    private List<Goods> repairLostGoods;

    public String getRepairKind() {
        return repairKind;
    }

    public void setRepairKind(String value) {
        repairKind = value;
    }

    public Integer getRepairShipId() {
        return repairShipId;
    }

    public void setRepairShipId(Integer value) {
        repairShipId = value;
    }

    public UnitType getRepairShipType() {
        return repairShipType;
    }

    public void setRepairShipType(UnitType value) {
        repairShipType = value;
    }

    public Boolean isRepairEuropeDestination() {
        return repairEuropeDestination;
    }

    public void setRepairEuropeDestination(Boolean value) {
        repairEuropeDestination = value;
    }

    public String getRepairDestinationName() {
        return repairDestinationName;
    }

    public void setRepairDestinationName(String value) {
        repairDestinationName = value;
    }

    public Integer getRepairLostPassengers() {
        return repairLostPassengers;
    }

    public void setRepairLostPassengers(Integer value) {
        repairLostPassengers = value;
    }

    public List<Goods> getRepairLostGoods() {
        return repairLostGoods;
    }

    public void setRepairLostGoods(List<Goods> value) {
        repairLostGoods = value;
    }

    private QueueItemType projectKind;
    private ConstructionType projectConstructionType;
    private UnitType projectUnitType;
    private Integer missingTools;

    public QueueItemType getProjectKind() {
        return projectKind;
    }

    public void setProjectKind(final QueueItemType value) {
        projectKind = value;
    }

    public ConstructionType getProjectConstructionType() {
        return projectConstructionType;
    }

    public void setProjectConstructionType(final ConstructionType value) {
        projectConstructionType = value;
    }

    public UnitType getProjectUnitType() {
        return projectUnitType;
    }

    public void setProjectUnitType(final UnitType value) {
        projectUnitType = value;
    }

    public Integer getMissingTools() {
        return missingTools;
    }

    public void setMissingTools(final Integer value) {
        missingTools = value;
    }

    private String playerName;

    private String type;

    private Goods goods;

    private String colonyName;

    private UnitType famineVictim;

    private Integer famineVictimId;

    private List<Goods> famineStocks;

    private Integer colonyId;
    private Integer lostShips;

    private Founder founder;

    private Integer income;

    private String rivalName;
    private Integer peacePrice;

    private Integer educationStudentId;
    private UnitType educationBeforeType;
    private UnitType educationAfterType;
    private Boolean educationWasVeteran;
    private Boolean educationVeteran;
    private Boolean educationWorkExperience;

    public Integer getEducationStudentId() {
        return educationStudentId;
    }

    public void setEducationStudentId(final Integer value) {
        educationStudentId = value;
    }

    public UnitType getEducationBeforeType() {
        return educationBeforeType;
    }

    public void setEducationBeforeType(final UnitType value) {
        educationBeforeType = value;
    }

    public UnitType getEducationAfterType() {
        return educationAfterType;
    }

    public void setEducationAfterType(final UnitType value) {
        educationAfterType = value;
    }

    public Boolean getEducationWasVeteran() {
        return educationWasVeteran;
    }

    public void setEducationWasVeteran(final Boolean value) {
        educationWasVeteran = value;
    }

    public Boolean getEducationVeteran() {
        return educationVeteran;
    }

    public void setEducationVeteran(final Boolean value) {
        educationVeteran = value;
    }

    public Boolean getEducationWorkExperience() {
        return educationWorkExperience;
    }

    public void setEducationWorkExperience(final Boolean value) {
        educationWorkExperience = value;
    }

    public String getRivalName() {
        return rivalName;
    }

    public void setRivalName(final String value) {
        rivalName = value;
    }

    public Integer getPeacePrice() {
        return peacePrice;
    }

    public void setPeacePrice(final Integer value) {
        peacePrice = value;
    }

    public Integer getIncome() {
        return income;
    }

    public void setIncome(final Integer income) {
        this.income = income;
    }

    public Founder getFounder() {
        return founder;
    }

    public void setFounder(final Founder founder) {
        this.founder = founder;
    }

    public Integer getLostShips() {
        return lostShips;
    }

    public void setLostShips(final Integer lostShips) {
        this.lostShips = lostShips;
    }

    public Integer getFamineVictimId() {
        return famineVictimId;
    }

    public void setFamineVictimId(final Integer famineVictimId) {
        this.famineVictimId = famineVictimId;
    }

    public UnitType getFamineVictim() {
        return famineVictim;
    }

    public void setFamineVictim(final UnitType famineVictim) {
        this.famineVictim = famineVictim;
    }

    public List<Goods> getFamineStocks() {
        return famineStocks;
    }

    public void setFamineStocks(final List<Goods> famineStocks) {
        this.famineStocks = famineStocks;
    }

    public Integer getColonyId() {
        return colonyId;
    }

    public void setColonyId(final Integer colonyId) {
        this.colonyId = colonyId;
    }

    /**
     * @return the playerName
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @param playerName the playerName to set
     */
    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public String getColonyName() {
        return colonyName;
    }

    public void setColonyName(String colonyName) {
        this.colonyName = colonyName;
    }

}
