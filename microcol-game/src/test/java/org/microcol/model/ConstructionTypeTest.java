package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConstructionTypeTest {

	
	@Test
	public void test_verify_consumptionPerTurn_ARMORY() throws Exception {
		assertEquals(3, ConstructionType.ARMORY.getProductionPerTurn());
		assertEquals(3, ConstructionType.ARMORY.getConsumptionPerTurn());
	}
	
	@Test
	public void test_verify_consumptionPerTurn_IRON_WORKS() throws Exception {
		assertEquals(9, ConstructionType.IRON_WORKS.getProductionPerTurn());
		assertEquals(6, ConstructionType.IRON_WORKS.getConsumptionPerTurn());		
	}

}
