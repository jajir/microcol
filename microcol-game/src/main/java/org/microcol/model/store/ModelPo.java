package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

public class ModelPo {

	private CalendarPo calendar;
	
	private List<UnitPo> units = new ArrayList<>();

	private List<ColonyPo> colonies = new ArrayList<>();

	private WorldMapPo map = new WorldMapPo();

	private List<PlayerPo> players = new ArrayList<>();
	
	private EuropePo europe;

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

}
