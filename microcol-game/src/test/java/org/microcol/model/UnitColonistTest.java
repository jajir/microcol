package org.microcol.model;

import org.junit.Test;

import static org.junit.Assert.*;

import org.junit.Before;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitColonistTest {

	@Tested(availableDuringSetup=true)
	private Unit unit;

	@Injectable
	private UnitType type = UnitType.COLONIST;

	@Injectable
	private Player owner;

	@Injectable()
	private Location location = Location.of(4, 3);

	private @Mocked Model model;
	
	@Test(expected=IllegalArgumentException.class)
	public void test_placeToHighSeas() throws Exception {
		unit.placeToHighSeas(true);
		
		assertTrue(unit.isAtMap());
	}
	
	@Before
	public void setup() {
		unit.setModel(model);
		unit.startTurn();
	}
	
}