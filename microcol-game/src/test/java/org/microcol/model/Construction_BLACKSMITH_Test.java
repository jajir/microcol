package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Mocked;

/**
 * Test construct blacksmith shop and try to produce goods. Tests verify that
 * number of consumed, produced and blocked goods are correct.
 */
public class Construction_BLACKSMITH_Test {
	
	private @Mocked ConstructionSlot slot1;

	private @Mocked ConstructionSlot slot2;

	private @Mocked ConstructionSlot slot3;

	private @Mocked Colony colony;
	
	private Construction blacksmith;
	
	@Test
	public void test_getProduction_no_workers() throws Exception {
		
		new Expectations() {{
			slot1.isEmpty(); result = true; times = 1;
			slot2.isEmpty(); result = true; times = 1;
			slot3.isEmpty(); result = true; times = 1;
			slot1.getProductionModifier(GoodType.TOOLS); result = 1; times = 0; // force test to fail when it's called
		}};

		ConstructionTurnProduction ret = blacksmith.getProduction(0);
		
		assertEquals(0, ret.getConsumedGoods());
		assertEquals(0, ret.getProducedGoods());
		assertEquals(0, ret.getBlockedGoods());
	}
	
	
	@Test
	public void test_getProduction_1_worker() throws Exception {

		new Expectations() {{
			slot1.isEmpty(); result = false;
			slot2.isEmpty(); result = true;
			slot3.isEmpty(); result = true;
			slot1.getProductionModifier(GoodType.TOOLS); result = 1; times = 1;
		}};

		ConstructionTurnProduction ret = blacksmith.getProduction(0);
		
		assertEquals(0, ret.getConsumedGoods());
		assertEquals(0, ret.getProducedGoods());
		assertEquals(5, ret.getBlockedGoods());
	}
	
	@Test
	public void test_getProduction_1_worker_limitedSource() throws Exception {
		
		new Expectations() {{
			slot1.isEmpty(); result = false;
			slot2.isEmpty(); result = true;
			slot3.isEmpty(); result = true;
			slot1.getProductionModifier(GoodType.TOOLS); result = 1; times = 1;
		}};

		ConstructionTurnProduction ret = blacksmith.getProduction(3);
		
		assertEquals(3, ret.getConsumedGoods());
		assertEquals(3, ret.getProducedGoods());
		assertEquals(2, ret.getBlockedGoods());
	}
	
	@Test
	public void test_getProduction_2_worker_limitedSource(
			) throws Exception {
		
		new Expectations() {{
			slot1.isEmpty(); result = true;
			slot2.isEmpty(); result = true;
			slot3.isEmpty(); result = false;
			slot3.getProductionModifier(GoodType.TOOLS); result = 1; times = 1;
		}};

		ConstructionTurnProduction ret = blacksmith.getProduction(3);
		
		assertEquals(3, ret.getConsumedGoods());
		assertEquals(3, ret.getProducedGoods());
		assertEquals(2, ret.getBlockedGoods());
	}
	
	@Before
	public void setup() {
		blacksmith = new Construction(colony, ConstructionType.BLACKSMITHS_SHOP,
				construction -> Lists.newArrayList(slot1, slot2, slot3));
	}
	
	@After
	public void tearDown() {
		blacksmith = null;
	}
	
}
