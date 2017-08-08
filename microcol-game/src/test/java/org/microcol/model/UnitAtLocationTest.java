package org.microcol.model;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Before;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitAtLocationTest {

	@Tested(availableDuringSetup=true)
	private Unit unit;

	@Injectable
	private UnitType type = UnitType.COLONIST;

	@Injectable
	private Player owner;

	@Injectable()
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
			model.getMap().isValid(loc); result=true;
			model.getMap().getTerrainAt(loc); result=Terrain.CONTINENT;
		}};
		assertTrue(unit.isMoveable(loc));
	}
	
	@Before
	public void setup() {
		unit.setModel(model);
		unit.startTurn();
	}
	
}