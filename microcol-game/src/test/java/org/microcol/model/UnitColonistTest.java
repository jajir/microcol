package org.microcol.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class UnitColonistTest {

	private @Mocked Player owner;

	private @Mocked PlaceBuilder placeBuilder;

	private @Mocked PlaceCargoSlot place;

	@Test(expected = IllegalArgumentException.class)
	public void test_placeToHighSeas_invalid_place_type() throws Exception {
		Unit unit = new Unit(UnitType.COLONIST, owner, Location.of(4, 3));
		unit.placeToHighSeas(true);
	}

	@Test
	public void test_placeToEuropePortPier(@Mocked PlaceCargoSlot place) throws Exception {
		new Expectations() {{
			placeBuilder.build((Unit)any); result=place;
			place.isOwnerAtEuropePort(); result=true;
		}};
		
		Unit unit = new Unit(UnitType.COLONIST, owner, placeBuilder);
		unit.placeToEuropePortPier();

		assertTrue(unit.isAtEuropePier());
	}

	@Test(expected = IllegalStateException.class)
	public void test_placeToEuropePortPier_unit_in_not_in_cargo(@Mocked PlaceLocation place) throws Exception {
		new Expectations() {{
			placeBuilder.build((Unit)any); result=place;
		}};
		
		Unit unit =new Unit(UnitType.COLONIST, owner, placeBuilder);
		unit.placeToEuropePortPier();
	}

	@Test(expected = IllegalStateException.class)
	public void test_placeToEuropePortPier_holding_unit_is_not_in_europe_port(@Mocked PlaceCargoSlot place) throws Exception {
		new Expectations() {{
			placeBuilder.build((Unit)any); result=place;
			place.isOwnerAtEuropePort(); result=false;
		}};
		Unit unit = new Unit(UnitType.COLONIST, owner, placeBuilder);
		unit.placeToEuropePortPier();
	}


}