package org.microcol.model.store;

import java.util.List;

import org.microcol.model.ConstructionType;
import org.microcol.model.Goods;
import org.microcol.model.NativeNation;
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
    private String goToKind;
    private Integer goToUnitId;
    private UnitType goToUnitType;
    private String goToDestinationName;

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

    public String getGoToKind() {
        return goToKind;
    }

    public void setGoToKind(String value) {
        goToKind = value;
    }

    public Integer getGoToUnitId() {
        return goToUnitId;
    }

    public void setGoToUnitId(Integer value) {
        goToUnitId = value;
    }

    public UnitType getGoToUnitType() {
        return goToUnitType;
    }

    public void setGoToUnitType(UnitType value) {
        goToUnitType = value;
    }

    public String getGoToDestinationName() {
        return goToDestinationName;
    }

    public void setGoToDestinationName(String value) {
        goToDestinationName = value;
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

    private Integer clearedX;
    private Integer clearedY;

    public Integer getClearedX() {
        return clearedX;
    }

    public void setClearedX(final Integer value) {
        clearedX = value;
    }

    public Integer getClearedY() {
        return clearedY;
    }

    public void setClearedY(final Integer value) {
        clearedY = value;
    }

    private Founder founder;

    private Integer income;

    private String rivalName;
    private Integer peacePrice;
    private Integer interventionTarget;
    private Boolean interventionEarned;
    private String diplomacyKind;
    private String diplomacyVictimName;

    public String getDiplomacyKind() {
        return diplomacyKind;
    }

    public void setDiplomacyKind(final String value) {
        diplomacyKind = value;
    }

    public String getDiplomacyVictimName() {
        return diplomacyVictimName;
    }

    public void setDiplomacyVictimName(final String value) {
        diplomacyVictimName = value;
    }

    /**
     * Selected patrol and its slowdown or stop, for its own event type only.
     */
    private Integer patrolId;
    private Boolean patrolStopped;

    public Integer getPatrolId() {
        return patrolId;
    }

    public void setPatrolId(final Integer value) {
        patrolId = value;
    }

    public Boolean isPatrolStopped() {
        return patrolStopped;
    }

    public void setPatrolStopped(final Boolean value) {
        patrolStopped = value;
    }

    /**
     * Coastal battery that fired, the ship it fired at and whether it hit, for
     * its own event type only (R-0177).
     */
    private Integer coastalColonyId;
    private Integer coastalShipId;
    private Boolean coastalHit;

    public Integer getCoastalColonyId() {
        return coastalColonyId;
    }

    public void setCoastalColonyId(final Integer value) {
        coastalColonyId = value;
    }

    public Integer getCoastalShipId() {
        return coastalShipId;
    }

    public void setCoastalShipId(final Integer value) {
        coastalShipId = value;
    }

    public Boolean isCoastalHit() {
        return coastalHit;
    }

    public void setCoastalHit(final Boolean value) {
        coastalHit = value;
    }

    /** Lost-city rumor result, required for its own event type only. */
    private String rumorOutcome;
    private Integer rumorExplorerId;
    private Integer rumorGold;
    private NativeNation rumorNation;
    private Boolean rumorDeSotoAverted;

    public String getRumorOutcome() {
        return rumorOutcome;
    }

    public void setRumorOutcome(final String value) {
        rumorOutcome = value;
    }

    public Integer getRumorExplorerId() {
        return rumorExplorerId;
    }

    public void setRumorExplorerId(final Integer value) {
        rumorExplorerId = value;
    }

    public Integer getRumorGold() {
        return rumorGold;
    }

    public void setRumorGold(final Integer value) {
        rumorGold = value;
    }

    public NativeNation getRumorNation() {
        return rumorNation;
    }

    public void setRumorNation(final NativeNation value) {
        rumorNation = value;
    }

    public Boolean isRumorDeSotoAverted() {
        return rumorDeSotoAverted;
    }

    public void setRumorDeSotoAverted(final Boolean value) {
        rumorDeSotoAverted = value;
    }

    /**
     * Kind of a native teaching notice, a TurnEventNativeTeaching.Kind name.
     */
    private String nativeTeachingKind;

    /** Skill of a native teaching notice, a UnitType name. */
    private String nativeTeachingSkill;

    public String getNativeTeachingKind() {
        return nativeTeachingKind;
    }

    public void setNativeTeachingKind(final String value) {
        nativeTeachingKind = value;
    }

    public String getNativeTeachingSkill() {
        return nativeTeachingSkill;
    }

    public void setNativeTeachingSkill(final String value) {
        nativeTeachingSkill = value;
    }

    /** Kind of a native convert notice, a TurnEventNativeConvert.Kind name. */
    private String nativeConvertKind;

    public String getNativeConvertKind() {
        return nativeConvertKind;
    }

    public void setNativeConvertKind(final String value) {
        nativeConvertKind = value;
    }

    /** Settlement ID of a native conquest notice (R-0174). */
    private Integer nativeConquestSettlementId;
    /** Nation name of a native conquest notice, a NativeNation name. */
    private String nativeConquestNation;
    private Integer nativeConquestX;
    private Integer nativeConquestY;
    private Boolean nativeConquestCapital;
    /** Defenders overcome in the conquest, at least one. */
    private Integer nativeConquestDefeated;

    public Integer getNativeConquestSettlementId() {
        return nativeConquestSettlementId;
    }

    public void setNativeConquestSettlementId(final Integer value) {
        nativeConquestSettlementId = value;
    }

    public String getNativeConquestNation() {
        return nativeConquestNation;
    }

    public void setNativeConquestNation(final String value) {
        nativeConquestNation = value;
    }

    public Integer getNativeConquestX() {
        return nativeConquestX;
    }

    public void setNativeConquestX(final Integer value) {
        nativeConquestX = value;
    }

    public Integer getNativeConquestY() {
        return nativeConquestY;
    }

    public void setNativeConquestY(final Integer value) {
        nativeConquestY = value;
    }

    public Boolean getNativeConquestCapital() {
        return nativeConquestCapital;
    }

    public void setNativeConquestCapital(final Boolean value) {
        nativeConquestCapital = value;
    }

    /** Lost pupil or missionary of a conquered settlement (R-0174). */
    private Integer nativeConquestLossUnitId;
    /** Type the lost unit had, a UnitType name. */
    private String nativeConquestLossUnitType;
    /** Whether the lost unit was a pupil, otherwise a missionary. */
    private Boolean nativeConquestLossPupil;

    public Integer getNativeConquestLossUnitId() {
        return nativeConquestLossUnitId;
    }

    public void setNativeConquestLossUnitId(final Integer value) {
        nativeConquestLossUnitId = value;
    }

    public String getNativeConquestLossUnitType() {
        return nativeConquestLossUnitType;
    }

    public void setNativeConquestLossUnitType(final String value) {
        nativeConquestLossUnitType = value;
    }

    public Boolean getNativeConquestLossPupil() {
        return nativeConquestLossPupil;
    }

    public void setNativeConquestLossPupil(final Boolean value) {
        nativeConquestLossPupil = value;
    }

    public Integer getNativeConquestDefeated() {
        return nativeConquestDefeated;
    }

    public void setNativeConquestDefeated(final Integer value) {
        nativeConquestDefeated = value;
    }

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

    public Integer getInterventionTarget() {
        return interventionTarget;
    }

    public void setInterventionTarget(final Integer value) {
        interventionTarget = value;
    }

    public Boolean getInterventionEarned() {
        return interventionEarned;
    }

    public void setInterventionEarned(final Boolean value) {
        interventionEarned = value;
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
