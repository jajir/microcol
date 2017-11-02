package org.microcol.model;

import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitFrigateTest {

	@Tested
	private Unit unit;
	
	@Injectable
	private Function<Unit, Cargo> cargoProvider;
	
	@Injectable
	private Model model;
	
	@Injectable(value="4")
	private Integer id;
	
	@Injectable
	private Function<Unit, Place> placeBuilder;
	
	@Injectable
	private UnitType unitType;
	
	@Injectable
	private Player owner;
	
	@Injectable(value="3")
	private int availableMoves;
	
	@Mocked
	private PlaceLocation placeMap;

	@Mocked
	private Cargo cargo;
	
	@Test
	public void test_placeToHighSeas() throws Exception {
		new Expectations() {{
			unitType.isShip(); result = true;
		}};
		unit.placeToHighSeas(true);
		
		assertTrue(unit.isAtHighSea());
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_placeToEuropePortPier() throws Exception {
		unit.placeToEuropePortPier();
	}
	
	@Before
	public void setup() {
		/*
		 * Following expectations will be used for unit constructor
		 */
		new Expectations() {{
			cargoProvider.apply((Unit)any); result = cargo;
			placeBuilder.apply((Unit)any); result = placeMap;
		}};
	}
	
}