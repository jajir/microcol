package org.microcol.model;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;

public class UnitFrigateTest {

	private Unit unit;

	private UnitType type = UnitType.FRIGATE;

	@Injectable
	private Player owner;

	@Injectable()
	private Location location = Location.of(4, 3);

	private @Mocked Model model;
	
	@Test
	public void test_placeToHighSeas() throws Exception {
		unit.placeToHighSeas(true);
		
		assertTrue(unit.isAtHighSea());
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_placeToEuropePortPier() throws Exception {
		unit.placeToEuropePortPier();
	}
	
	@Before
	public void setup() {
		unit = new Unit(type, owner, location);
		new Expectations() {{
			model.getMap().getTerrainAt(location); result = Terrain.OCEAN;
			model.getMap().isValid(location); result = true;
		}};
		
		unit.setModel(model);
		unit.startTurn();
	}
	
}