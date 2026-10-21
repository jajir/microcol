package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

import org.microcol.model.Difficulty;
import org.microcol.model.Location;

import com.google.common.base.Preconditions;

public class ModelPo {

    private Long combatRandomState;

    public Long getCombatRandomState() {
        return combatRandomState;
    }

    public void setCombatRandomState(final Long combatRandomState) {
        this.combatRandomState = combatRandomState;
    }

    /** Last complete round already considered by the Crown-war event. */
    private Integer crownWarRound;

    public Integer getCrownWarRound() {
        return crownWarRound;
    }

    public void setCrownWarRound(final Integer crownWarRound) {
        this.crownWarRound = crownWarRound;
    }

    private Difficulty difficulty;

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(final Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    private List<EuropeanRelationPo> europeanRelations;

    public List<EuropeanRelationPo> getEuropeanRelations() {
        return europeanRelations;
    }

    public void setEuropeanRelations(
            final List<EuropeanRelationPo> europeanRelations) {
        this.europeanRelations = europeanRelations;
    }

    private Integer nextColonyId;

    public Integer getNextColonyId() {
        return nextColonyId;
    }

    public void setNextColonyId(final Integer nextColonyId) {
        this.nextColonyId = nextColonyId;
    }

    /** Required: the next route ID never reused, even after a rejected save. */
    private Integer nextTradeRouteId;

    public Integer getNextTradeRouteId() {
        return nextTradeRouteId;
    }

    public void setNextTradeRouteId(final Integer nextTradeRouteId) {
        this.nextTradeRouteId = nextTradeRouteId;
    }

    /** Required, possibly empty; a missing list is rejected, not defaulted. */
    private List<TradeRoutePo> tradeRoutes;

    public List<TradeRoutePo> getTradeRoutes() {
        return tradeRoutes;
    }

    public void setTradeRoutes(final List<TradeRoutePo> tradeRoutes) {
        this.tradeRoutes = tradeRoutes;
    }

    private CalendarPo calendar = new CalendarPo();

    private List<UnitPo> units = new ArrayList<>();

    private List<ColonyPo> colonies = new ArrayList<>();

    /**
     * Native settlements. No default: the current schema requires an explicit
     * list, even an empty one.
     */
    private List<NativeSettlementPo> nativeSettlements;

    /**
     * Native settlements each player has learned. Absent means nothing is
     * learned yet; no settlement identity is guessed from it.
     */
    private List<NativeSettlementKnowledgePo> nativeSettlementKnowledge;

    public List<NativeSettlementKnowledgePo> getNativeSettlementKnowledge() {
        return nativeSettlementKnowledge;
    }

    public void setNativeSettlementKnowledge(
            final List<NativeSettlementKnowledgePo> nativeSettlementKnowledge) {
        this.nativeSettlementKnowledge = nativeSettlementKnowledge;
    }

    /**
     * Tribal anger for every native nation and European pair. No default: the
     * current schema requires the complete explicit table, zeros included.
     */
    private List<NativeTribalAngerPo> nativeTribalAnger;

    /**
     * Local alarm for every native settlement and European pair. No default:
     * the current schema requires the complete explicit table, zeros included.
     */
    private List<NativeLocalAlarmPo> nativeLocalAlarm;

    /**
     * Paid land rights of Europeans on native-claimed squares. No default: the
     * current schema requires the list, empty when nothing has been paid.
     */
    private List<NativeLandRecordPo> nativeLandRights;

    /**
     * Unpaid land takings already charged once, per European and square. No
     * default: the list is required, empty when nothing has been taken.
     */
    private List<NativeLandRecordPo> nativeLandTakings;

    public List<NativeLandRecordPo> getNativeLandRights() {
        return nativeLandRights;
    }

    public void setNativeLandRights(
            final List<NativeLandRecordPo> nativeLandRights) {
        this.nativeLandRights = nativeLandRights;
    }

    public List<NativeLandRecordPo> getNativeLandTakings() {
        return nativeLandTakings;
    }

    public void setNativeLandTakings(
            final List<NativeLandRecordPo> nativeLandTakings) {
        this.nativeLandTakings = nativeLandTakings;
    }

    public List<NativeTribalAngerPo> getNativeTribalAnger() {
        return nativeTribalAnger;
    }

    public void setNativeTribalAnger(
            final List<NativeTribalAngerPo> nativeTribalAnger) {
        this.nativeTribalAnger = nativeTribalAnger;
    }

    public List<NativeLocalAlarmPo> getNativeLocalAlarm() {
        return nativeLocalAlarm;
    }

    public void setNativeLocalAlarm(
            final List<NativeLocalAlarmPo> nativeLocalAlarm) {
        this.nativeLocalAlarm = nativeLocalAlarm;
    }

    private List<TurnEventPo> turnEvents = new ArrayList<>();

    private WorldMapPo map = new WorldMapPo();

    private EuropePo europe = new EuropePo();

    private Location focusedField;

    private StatisticsPo statistics = new StatisticsPo();

    private GameManagerPo gameManager = new GameManagerPo();

    public UnitPo getUnitWithUnitInCargo(final Integer idUnitInCargo) {
        Preconditions.checkState(idUnitInCargo != null,
                "IdUnitInCargo is null");
        return units.stream().filter(
                unit -> unit.getCargo().containsUnitInCargo(idUnitInCargo))
                .findAny()
                .orElseThrow(() -> new IllegalStateException(String.format(
                        "Unable to find unit containing unit '%s' in cargo",
                        idUnitInCargo)));
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

    public List<NativeSettlementPo> getNativeSettlements() {
        return nativeSettlements;
    }

    public void setNativeSettlements(
            final List<NativeSettlementPo> nativeSettlements) {
        this.nativeSettlements = nativeSettlements;
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
     * @param calendar the calendar to set
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
     * @param europe the europe to set
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
     * @param focusedField the focusedField to set
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
