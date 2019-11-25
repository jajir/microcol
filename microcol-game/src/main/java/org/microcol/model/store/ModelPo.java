package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.Location;

import com.google.common.base.Preconditions;

public class ModelPo {

    private CalendarPo calendar = new CalendarPo();

    private List<UnitPo> units = new ArrayList<>();

    private List<ColonyPo> colonies = new ArrayList<>();

    private List<TurnEventPo> turnEvents = new ArrayList<>();

    private WorldMapPo map = new WorldMapPo();

    private EuropePo europe = new EuropePo();

    private Location focusedField;
    
    private StatisticsPo statistics = new StatisticsPo();
    
    private GameManagerPo gameManager = new GameManagerPo();

    public UnitPo getUnitWithUnitInCargo(final Integer idUnitInCargo) {
        Preconditions.checkState(idUnitInCargo != null, "IdUnitInCargo is null");
        return units.stream().filter(unit -> unit.getCargo().containsUnitInCargo(idUnitInCargo))
                .findAny().orElseThrow(() -> new IllegalStateException(String.format(
                        "Unable to find unit containing unit '%s' in cargo", idUnitInCargo)));
    }

    public List<UnitPo> getUnits() {
        return units;
    }

    public WorldMapPo getMap() {
        return map;
    }

    public List<ColonyPo> getColonies() {
        return colonies;
    }

    public void setColonies(List<ColonyPo> colonies) {
        this.colonies = colonies;
    }

    public void setUnits(List<UnitPo> units) {
        this.units = units;
    }

    public void setMap(WorldMapPo map) {
        this.map = map;
    }

    /**
     * @return the calendar
     */
    public CalendarPo getCalendar() {
        return calendar;
    }

    /**
     * @param calendar
     *            the calendar to set
     */
    public void setCalendar(CalendarPo calendar) {
        this.calendar = calendar;
    }

    /**
     * @return the europe
     */
    public EuropePo getEurope() {
        return europe;
    }

    /**
     * @param europe
     *            the europe to set
     */
    public void setEurope(EuropePo europe) {
        this.europe = europe;
    }

    /**
     * @return the focusedField
     */
    public Location getFocusedField() {
        return focusedField;
    }

    /**
     * @param focusedField
     *            the focusedField to set
     */
    public void setFocusedField(Location focusedField) {
        this.focusedField = focusedField;
    }

    /**
     * @return the turnEvents
     */
    public List<TurnEventPo> getTurnEvents() {
        return turnEvents;
    }

    /**
     * @param turnEvents the turnEvents to set
     */
    public void setTurnEvents(List<TurnEventPo> turnEvents) {
        this.turnEvents = turnEvents;
    }

    /**
     * @return the statistics
     */
    public StatisticsPo getStatistics() {
        return statistics;
    }

    /**
     * @param statistics the statistics to set
     */
    public void setStatistics(StatisticsPo statistics) {
        this.statistics = statistics;
    }

    /**
     * @return the gameManagerPo
     */
    public GameManagerPo getGameManager() {
        return gameManager;
    }

    /**
     * @param gameManager the gameManagerPo to set
     */
    public void setGameManager(GameManagerPo gameManager) {
        this.gameManager = gameManager;
    }

}
