package org.microcol.model.store;

import java.util.List;

import org.microcol.model.Goods;
import org.microcol.model.UnitType;
import org.microcol.model.Founder;

public final class TurnEventPo {

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
