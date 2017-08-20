package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class UnitTypeTest {

	@Test(expected=IllegalArgumentException.class)
	public void test_valueOf_noSuchUnitType() throws Exception {
		UnitType.valueOf("NoSuchUnitType");
	}
	
	public void test_forValue() throws Exception {
		UnitType unitType = UnitType.valueOf("Colonist");
		
		assertNotNull(unitType);
		assertEquals("Colonist", unitType);
	}
	
	public void test_equals() throws Exception {
		assertTrue(UnitType.COLONIST.equals(UnitType.COLONIST));
		assertFalse(UnitType.COLONIST.equals(UnitType.FRIGATE));
	}
	
}
