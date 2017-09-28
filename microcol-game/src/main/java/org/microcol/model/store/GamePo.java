package org.microcol.model.store;

import java.util.ArrayList;
import java.util.List;

public class GamePo {

	private List<UnitPo> units = new ArrayList<>();
	
	private WorldMapPo map = new WorldMapPo();
	
	public GamePo() {
		// TODO Auto-generated constructor stub
	}

	public List<UnitPo> getUnits() {
		return units;
	}

	public WorldMapPo getMap() {
		return map;
	}

}
