package org.microcol.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class ConstructionTest {

	@Test
	public void test_getOrderedSlots_noUnits(final @Mocked Colony colony) throws Exception {
		Construction blacksmith = Construction.build(colony, ConstructionType.BLACKSMITHS_HOUSE);

		List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

		assertEquals(3, slots.size());
		assertTrue(slots.get(0).isEmpty());
		assertTrue(slots.get(1).isEmpty());
		assertTrue(slots.get(2).isEmpty());
	}

	@Test
	public void test_getOrderedSlots_one_freeColonists(final @Mocked Colony colony, @Mocked final Unit colonist) {
		Construction blacksmith = Construction.build(colony, ConstructionType.BLACKSMITHS_HOUSE);
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
	public void test_getOrderedSlots_one_freeColonists_one_ExpertBlacksmith(final @Mocked Colony colony, @Mocked final Unit colonist1, @Mocked final Unit colonist2) {
		Construction blacksmith = Construction.build(colony, ConstructionType.BLACKSMITHS_HOUSE);
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

}
