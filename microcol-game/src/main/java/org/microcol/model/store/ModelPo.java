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

    private List<PlayerPo> players = new ArrayList<>();

    private EuropePo europe = new EuropePo();

    private CampaignPo campaign = new CampaignPo();

    private Location focusedField;

    public PlayerPo getPlayerByName(final String name) {
        Preconditions.checkState(players != null, "Players are null");
        return players.stream().filter(player -> player.getName().equals(name)).findAny()
                .orElseThrow(() -> new IllegalStateException("Invalid owner name '" + name + "'"));
    }

    public UnitPo getUnitWithUnitInCargo(final Integer idUnitInCargo) {
        Preconditions.checkState(idUnitInCargo != null, "IdUnitInCargo is null");
        return units.stream().filter(unit -> unit.getCargo().containsUnitInCargo(idUnitInCargo))
                .findAny().orElseThrow(() -> new IllegalStateException(String.format(
                        "Unable to find unit containing unit '%s' in cargo", idUnitInCargo)));
    }

    public void addUnit(final UnitPo add) {
        units.stream().filter(unit -> unit.getId().equals(add.getId())).findFirst()
                .ifPresent(unit -> {
                    throw new IllegalArgumentException(
                            "unit " + unit + " is same asss added " + add);
                });
        units.add(add);
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
     * @return the players
     */
    public List<PlayerPo> getPlayers() {
        return players;
    }

    /**
     * @param players
     *            the players to set
     */
    public void setPlayers(List<PlayerPo> players) {
        this.players = players;
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
     * @return the campaign
     */
    public CampaignPo getCampaign() {
        return campaign;
    }

    /**
     * @param campaign
     *            the campaign to set
     */
    public void setCampaign(CampaignPo campaign) {
        this.campaign = campaign;
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

}
