package org.microcol.model;

import org.junit.Before;
import org.junit.Test;
import org.microcol.model.store.UnitPo;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitColonistTest {

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
	private PlaceLocation placeLocation;

	@Test(expected = IllegalArgumentException.class)
	public void test_placeToHighSeas_invalid_place_type() throws Exception {
		new Expectations() {{
			unitType.isShip(); result = false;
		}};
		unit.placeToHighSeas(true);
	}

	@Test(expected = IllegalStateException.class)
	public void test_placeToEuropePortPier_unit_in_not_in_cargo() throws Exception {
		
		unit.placeToEuropePortPier();
	}

	@Before
	public void setup() {
		/*
		 * Following expectations will be used for unit constructior
		 */
		new Expectations() {{
			placeBuilder.build((Unit)any); result = placeLocation;
		}};
	}


}