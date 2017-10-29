package org.microcol.model;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.microcol.model.store.UnitPo;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitColonistTestInCargo {

	@Tested
	private Unit unit;
	
	@Injectable
	private UnitPo unitPo;
	
	@Injectable
	private Model model;
	
	@Injectable(value="4")
	private Integer id;
	
	@Injectable
	private PlaceBuilder placeBuilder;
	
	@Injectable
	private UnitType unitType;
	
	@Injectable
	private Player owner;
	
	@Injectable(value="3")
	private int availableMoves;
	
	@Mocked
	private PlaceCargoSlot placeCargoSlot;

	@Test
	public void test_placeToEuropePortPier() throws Exception {
		new Expectations() {{
			placeCargoSlot.isOwnerAtEuropePort(); result = true;
		}};
		
		unit.placeToEuropePortPier();

		assertTrue(unit.isAtEuropePier());
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_placeToEuropePortPier_holding_unit_is_not_in_europe_port() throws Exception {
		new Expectations() {{
			placeCargoSlot.isOwnerAtEuropePort(); result = false;
		}};
		unit.placeToEuropePortPier();
	}
	
	@Before
	public void setup() {
		/*
		 * Following expectations will be used for unit constructor
		 */
		new Expectations() {{
			placeBuilder.build((Unit)any); result = placeCargoSlot;
		}};
	}

}
