package org.microcol.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

public class ConstructionTest {

	@Test
	public void test_church_verify_base_production() throws Exception {
		Construction church = new Construction(ConstructionType.CHURCH);

		assertEquals(1, church.getProductionPerTurn());
	}

	@Test
	public void test_blacksmith_verify_base_production_per_turn() throws Exception {
		Construction blacksmith = new Construction(ConstructionType.BLACKSMITHS_HOUSE);

		assertEquals(0, blacksmith.getProductionPerTurn());
	}

	@Test
	public void test_getOrderedSlots_noUnits() throws Exception {
		Construction blacksmith = new Construction(ConstructionType.BLACKSMITHS_HOUSE);

		List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

		assertEquals(3, slots.size());
		assertTrue(slots.get(0).isEmpty());
		assertTrue(slots.get(1).isEmpty());
		assertTrue(slots.get(2).isEmpty());
	}

	@Test
	public void test_getOrderedSlots_one_freeColonists(@Mocked final Unit colonist) {
		Construction blacksmith = new Construction(ConstructionType.BLACKSMITHS_HOUSE);
		blacksmith.placeWorker(1, colonist);
		new Expectations() {{
				colonist.getType(); result = UnitType.COLONIST;
		}};

		List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

		assertEquals(3, slots.size());
		assertFalse(slots.get(0).isEmpty());
		assertTrue(slots.get(1).isEmpty());
		assertTrue(slots.get(2).isEmpty());
	}

	@Test
	public void test_getOrderedSlots_one_freeColonists_one_ExpertBlacksmith(@Mocked final Unit colonist1, @Mocked final Unit colonist2) {
		Construction blacksmith = new Construction(ConstructionType.BLACKSMITHS_HOUSE);
		blacksmith.placeWorker(1, colonist1);
		blacksmith.placeWorker(2, colonist2);
		new Expectations() {{
			colonist1.getType(); result = UnitType.EXPERT_ORE_MINER;
			colonist2.getType(); result = UnitType.COLONIST;
		}};

		List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

		assertEquals(3, slots.size());
		assertFalse(slots.get(0).isEmpty());
		assertFalse(slots.get(1).isEmpty());
		assertTrue(slots.get(2).isEmpty());
		assertSame(colonist1, slots.get(0).getUnit());
		assertSame(colonist2, slots.get(1).getUnit());
	}

	@Test
	public void test_blacksmith_produce(@Mocked ColonyWarehouse colonyWarehouse, @Mocked final Unit colonist1, @Mocked final Unit colonist2) throws Exception {
		Construction blacksmith = new Construction(ConstructionType.BLACKSMITHS_HOUSE);
		blacksmith.placeWorker(1, colonist1);
		blacksmith.placeWorker(2, colonist2);
		new Expectations() {{
			colonist1.getType(); result = UnitType.EXPERT_ORE_MINER;
			colonist2.getType(); result = UnitType.COLONIST;
			
			colonyWarehouse.getGoodAmmount(GoodType.ORE); result = 10;
		}};

		new Expectations() {{
			colonyWarehouse.removeFromWarehouse(GoodType.ORE, 3);
			colonyWarehouse.addToWarehouse(GoodType.TOOLS, 3);
		}};

		blacksmith.produce(colonyWarehouse);
		
		//TODO finish verify section
		new Verifications() {{
				// a "verification block"
				// Verifies an expected invocation:
				// anotherMock.save(any); times = 1;
			}};
	}

}
