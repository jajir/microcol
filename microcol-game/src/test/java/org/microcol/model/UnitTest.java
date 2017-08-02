package org.microcol.model;

import org.junit.Test;

import static org.junit.Assert.*;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

public class UnitTest {

	@Tested	Unit unit;

	@Injectable UnitType tyoe = UnitType.COLONIST;
	
	@Injectable Player owner;
	
	@Injectable Location location = Location.of(2, 5);
	
	@Test
	public void testGetHold() {
		assertNotNull(unit);
		CargoHold cargo = unit.getHold();
		
		assertNotNull(cargo);
	}
	
	@Test
	public void testIsPossible(@Mocked Model model) {
		unit.setModel(model);
		assertEquals(true, unit.isMoveable(Location.of(12, 4)));
	}
	
	

}
