package org.microcol.model;

import static org.junit.Assert.*;

import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Preconditions;

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
	
	@Test
	public void test_visibleArea_visibility_1() throws Exception {
		new Expectations() {{
			placeMap.getLocation(); result = Location.of(30, 30);
			unitType.getSpeed(); result = 0;
		}};
		
		final List<Location> visible = unit.getVisibleLocations();
		
		Preconditions.checkNotNull(visible);
		assertEquals(9, visible.size());
	}
	
	@Test
	public void test_visibleArea_visibility_2() throws Exception {
		new Expectations() {{
			placeMap.getLocation(); result = Location.of(30, 30);
			unitType.getSpeed(); result = 1;
		}};
		
		final List<Location> visible = unit.getVisibleLocations();
		
		Preconditions.checkNotNull(visible);
		assertEquals(25, visible.size());
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