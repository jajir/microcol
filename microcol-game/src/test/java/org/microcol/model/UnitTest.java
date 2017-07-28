package org.microcol.model;

import org.junit.Test;

 import static org.junit.Assert.*;
import mockit.Tested;
import mockit.Verifications;

public class UnitTest {

	@Tested	Unit unit;

	@Test
	public void testGetHold() {
		assertNotNull(unit);
		CargoHold cargo = unit.getHold();
		new Verifications() {
			{
				times = 1;
			}
		};
	}

}
