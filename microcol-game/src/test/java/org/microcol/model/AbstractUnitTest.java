package org.microcol.model;

import org.junit.After;
import org.junit.Before;

import mockit.Mocked;

public abstract class AbstractUnitTest {
	
	protected Unit unit;
		
	@Mocked
	protected Model model;
	
	@Mocked
	protected UnitType unitType;
	
	@Mocked
	protected Player owner;
	
	@Mocked
	protected PlaceLocation placeLocation;

	@Mocked
	protected Cargo cargo;
	
	@Mocked
	protected WorldMap worldMap;
	
	protected void makeUnit(final Cargo cargo, final Model model, final int id, final Place place, final UnitType unitType, final Player owner, final int availableMoves){
		unit = new Unit(unit -> cargo, model, id, unit -> place, unitType, owner, availableMoves);
	}
	
	@Before
	public void setup() {
	}

	@After
	public void tearDown(){
		unit = null;
	}

}
