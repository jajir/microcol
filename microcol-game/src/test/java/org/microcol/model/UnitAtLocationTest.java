package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class UnitAtLocationTest {

	private Unit unit;

	private UnitType type = UnitType.COLONIST;

	private @Mocked Player owner;

	private Location location = Location.of(4, 3);

	private @Mocked Model model;
	
	@Test
	public void testInitialization() {
		assertNotNull(unit);
		assertEquals(Location.of(4, 3), unit.getLocation());
		assertEquals(owner, unit.getOwner());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testPlaceAtMap() throws Exception {
		unit.unload(Location.of(12, 12));
	}
	
	@Test
	public void testIsMovable() throws Exception {
		final Location loc = Location.of(10, 12);
		new Expectations() {{
			model.getMap().isValid(loc); result = true;
			model.getMap().getTerrainTypeAt(loc); result = TerrainType.GRASSLAND;
		}};
		
		assertTrue(unit.isMoveable(loc));
	}
	
	@Before
	public void setup() {
		unit = new Unit(type, owner, location);
		new Expectations() {{
			model.getMap().isValid(location); result = true;
			model.getMap().getTerrainTypeAt(location); result = TerrainType.GRASSLAND;
		}};

		unit.setModel(model);
		unit.startTurn();
	}
	
	@After
	public void teardown() {
		unit = null;
	}
	
}