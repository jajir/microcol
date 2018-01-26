package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConstructionTypeConstantTest {

	@Test
	public void test_source_type_test() throws Exception {
		assertEquals(GoodType.GOOD_TYPES.size(),
				ConstructionType.SOURCE_1.size() + ConstructionType.SOURCE_2.size() + ConstructionType.SOURCE_3.size());
	
		assertTrue(ConstructionType.SOURCE_1.contains(GoodType.BELL));
		assertTrue(ConstructionType.SOURCE_1.contains(GoodType.CROSS));

		assertTrue(ConstructionType.SOURCE_2.contains(GoodType.TOOLS));
		assertTrue(ConstructionType.SOURCE_2.contains(GoodType.RUM));

		assertTrue(ConstructionType.SOURCE_3.contains(GoodType.MUSKET));
	}
	
}
