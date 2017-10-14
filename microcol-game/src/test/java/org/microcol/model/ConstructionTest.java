package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Mocked;

public class ConstructionTest {

	@Test
	public void test_church_verify_base_production(final @Mocked Colony colony) throws Exception {
		Construction church = Construction.build(ConstructionType.CHURCH);

		assertEquals(1, church.getProductionPerTurn(colony));
	}

	@Test
	public void test_blacksmith_verify_base_production_per_turn(final @Mocked Colony colony) throws Exception {
		Construction blacksmith = Construction.build(ConstructionType.BLACKSMITHS_HOUSE);

		assertEquals(0, blacksmith.getProductionPerTurn(colony));
	}

	@Test
	public void test_getOrderedSlots_noUnits() throws Exception {
		Construction blacksmith = Construction.build(ConstructionType.BLACKSMITHS_HOUSE);

		List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

		assertEquals(3, slots.size());
		assertTrue(slots.get(0).isEmpty());
		assertTrue(slots.get(1).isEmpty());
		assertTrue(slots.get(2).isEmpty());
	}

	@Test
	public void test_getOrderedSlots_one_freeColonists(@Mocked final Unit colonist) {
		Construction blacksmith = Construction.build(ConstructionType.BLACKSMITHS_HOUSE);
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
		Construction blacksmith = Construction.build(ConstructionType.BLACKSMITHS_HOUSE);
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
	public void test_blacksmith_produce(
			@Mocked ColonyWarehouse colonyWarehouse,
			@Mocked final ConstructionSlot slot1,
			@Mocked final ConstructionSlot slot2,
			@Mocked final ConstructionSlot slot3,
			@Mocked final ConstructionType buildingType,
			@Mocked final ConstructionProduction prod,
			@Mocked final Colony colony
			) throws Exception {
		
		final Construction blacksmith = new Construction(buildingType,
				Lists.newArrayList(slot1, slot2, slot3));
		
		new Expectations() {{
			buildingType.getProduce(); result = Optional.of(GoodType.TOOLS);
			buildingType.getConstructionProduction(colony); result = prod; times = 3;
			slot1.getProductionModifier(GoodType.TOOLS); result = 1; times = 1;
			slot2.getProductionModifier(GoodType.TOOLS); result = 1; times = 1;
			slot3.getProductionModifier(GoodType.TOOLS); result = 1; times = 1;
			prod.multiply(1); times = 3;
			prod.limit(colonyWarehouse); times = 3;
		}};

		blacksmith.produce(colony, colonyWarehouse);
	}

}
