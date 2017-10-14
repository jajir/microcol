package org.microcol.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

/**
 * Verify that {@link GoodType#BELL} type of production is correctly computed.
 */
public class ConstructionType_TOWNHALL_test {

	@Test
	public void test_colony_no_booster(final @Mocked Colony colony) throws Exception {
		new Expectations() {{
			colony.isContainsConstructionByType(ConstructionType.PRINTING_PRESS); result = false; times = 1;
			colony.isContainsConstructionByType(ConstructionType.NEWSPAPER); result = false; times = 1;
		}};

		ConstructionProduction prod = ConstructionType.TOWN_HALL.getConstructionProduction(colony);
		
		assertEquals(0, prod.getConsumptionPerTurn());
		assertEquals(3, prod.getProductionPerTurn());
	}

	@Test
	public void test_colony_with_printing_press(final @Mocked Colony colony) throws Exception {
		new Expectations() {{
			colony.isContainsConstructionByType(ConstructionType.PRINTING_PRESS); result = true; times = 1;
			colony.isContainsConstructionByType(ConstructionType.NEWSPAPER); result = false; times = 1;
		}};

		ConstructionProduction prod = ConstructionType.TOWN_HALL.getConstructionProduction(colony);
		
		assertEquals(0, prod.getConsumptionPerTurn());
		assertEquals(9, prod.getProductionPerTurn());
	}

	@Test
	public void test_colony_with_newspapers(final @Mocked Colony colony) throws Exception {
		new Expectations() {{
			colony.isContainsConstructionByType(ConstructionType.PRINTING_PRESS); result = false; times = 1;
			colony.isContainsConstructionByType(ConstructionType.NEWSPAPER); result = true; times = 1;
		}};

		ConstructionProduction prod = ConstructionType.TOWN_HALL.getConstructionProduction(colony);
		
		assertEquals(0, prod.getConsumptionPerTurn());
		assertEquals(6, prod.getProductionPerTurn());
	}
	
	
}
