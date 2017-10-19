package org.microcol.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
 import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;

public class PlaceBuilderTest {

	private PlaceBuilderImpl placeBuilder;

	@Test
	public void testSetLocation(@Mocked Unit unit) throws Exception {
		placeBuilder.setLocation(Location.of(3, 2));
		
		Place ret = placeBuilder.build(unit);
		assertNotNull(ret);
		assertTrue(ret instanceof PlaceLocation);
		assertEquals(Location.of(3, 2), ((PlaceLocation)ret).getLocation());
	}

	@Test(expected=IllegalStateException.class)
	public void testSetLocation_incomingToColonies() throws Exception {
		placeBuilder.setLocation(Location.of(3, 2));
		placeBuilder.setShipIncomingToColonies(3);
	}

	@Test(expected=IllegalStateException.class)
	public void testSetLocation_incomingToEurope() throws Exception {
		placeBuilder.setLocation(Location.of(3, 2));
		placeBuilder.setShipIncomingToEurope(3);
	}

	@Test
	public void test_setShipIncomingToEurope(@Mocked Unit unit) throws Exception {
		placeBuilder.setShipIncomingToEurope(4);
		new Expectations() {{
			unit.getType(); result=UnitType.FRIGATE;
		}};
		
		Place ret = placeBuilder.build(unit);
		assertNotNull(ret);
		assertTrue(ret instanceof PlaceHighSea);
		assertEquals(4, ((PlaceHighSea)ret).getRemainigTurns());
		assertEquals(true, ((PlaceHighSea)ret).isTravelToEurope());
	}

	@Test
	public void test_setShipIncomingToColonies(@Mocked Unit unit) throws Exception {
		placeBuilder.setShipIncomingToColonies(2);
		new Expectations() {{
			unit.getType(); result=UnitType.FRIGATE;
		}};
		
		Place ret = placeBuilder.build(unit);
		assertNotNull(ret);
		assertTrue(ret instanceof PlaceHighSea);
		assertEquals(2, ((PlaceHighSea)ret).getRemainigTurns());
		assertEquals(false, ((PlaceHighSea)ret).isTravelToEurope());
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_setShipIncomingToColonies_invalidUnitType(@Mocked Unit unit) throws Exception {
		placeBuilder.setShipIncomingToColonies(2);
		new Expectations() {{
			unit.getType(); result=UnitType.COLONIST;
		}};
		
		Place ret = placeBuilder.build(unit);
		assertNotNull(ret);
		assertTrue(ret instanceof PlaceHighSea);
		assertEquals(2, ((PlaceHighSea)ret).getRemainigTurns());
		assertEquals(false, ((PlaceHighSea)ret).isTravelToEurope());
	}

	@Test
	public void test_setShipToEuropePortPier(@Mocked Unit unit) throws Exception {
		placeBuilder.setUnitToEuropePortPier();
		new Expectations() {{
			unit.getType(); result=UnitType.COLONIST;
		}};
		
		Place ret = placeBuilder.build(unit);
		assertNotNull(ret);
		assertTrue(ret instanceof PlaceEuropePier);
	}

	@Test(expected=IllegalArgumentException.class)
	public void test_setShipToEuropePortPier_invalidUnitType(@Mocked Unit unit) throws Exception {
		placeBuilder.setUnitToEuropePortPier();
		new Expectations() {{
			unit.getType(); result=UnitType.FRIGATE;
		}};
		
		placeBuilder.build(unit);
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_noTargetPlace(@Mocked Unit unit) throws Exception {
		placeBuilder.build(unit);
	}

	@Before
	public void setUp() {
		placeBuilder = new PlaceBuilderImpl();
	}

	@After
	public void yearDown() {
		placeBuilder = null;
	}

}
