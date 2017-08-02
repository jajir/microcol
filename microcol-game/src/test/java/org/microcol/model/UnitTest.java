package org.microcol.model;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Before;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitTest {

	@Tested(availableDuringSetup=true)
	Unit unit;

	@Injectable
	UnitType type = UnitType.COLONIST;

	@Injectable
	Player owner;

	@Injectable()
	Location location = Location.of(4, 3);

	@Mocked Model model;
	
	@Test
	public void testInitialization() {
		assertNotNull(unit);
		assertEquals(Location.of(4, 3), unit.getLocation());
		assertEquals(owner, unit.getOwner());
	}
	
	@Test
	public void testIsMovable() throws Exception {
		final Location loc =Location.of(10, 12);
		new Expectations() {{
			model.getMap().isValid(loc); result=true;
			model.getMap().getTerrainAt(loc); result=Terrain.CONTINENT;
		}};
		assertTrue(unit.isMoveable(loc));
	}
	
	@Before
	public void setup() {
		new Expectations() {{
			model.getMap().isValid(location); result=true;
			model.getMap().getTerrainAt(location); result=Terrain.CONTINENT;
		}};
		unit.setModel(model);
	}
	
}
