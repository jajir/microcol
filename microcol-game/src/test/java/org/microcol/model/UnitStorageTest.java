package org.microcol.model;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import mockit.Mocked;

public class UnitStorageTest {

	private UnitStorage unitStorage;

	@Mocked
	private Unit unit;

	@Test(expected=NullPointerException.class)
	public void test_addUnit_nullUnit() throws Exception {
		unitStorage.addUnit(null);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_addUnit_unitWasAlreadyInserted() throws Exception {
		unitStorage.addUnit(unit);
		unitStorage.addUnit(unit);
	}
	
	@Test
	public void test_thatEmptyStorageDoesntReturnNull() throws Exception {
		assertNotNull(unitStorage.getUnits());
		assertEquals(0, unitStorage.getUnits().size());
	}
	

	@Test
	public void test_addUnit() throws Exception {
		unitStorage.addUnit(unit);

		assertNotNull(unitStorage.getUnits());
		assertEquals(1, unitStorage.getUnits().size());
	}

	@Before
	public void setup() {
		unitStorage = new UnitStorage();
	}

}
