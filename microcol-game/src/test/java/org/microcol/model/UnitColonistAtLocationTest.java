package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Mocked;

public class UnitColonistAtLocationTest extends AbstractUnitFreeColonistTest {

    @Mocked
    protected PlaceLocation placeLocation;

	@Test
	public void testInitialization() {
		makeColonist(model, 23, placeLocation, owner, 1);
		
		new Expectations() {{
			placeLocation.getLocation(); result = Location.of(4, 3);
		}};
		
		assertNotNull(unit);
		assertEquals(Location.of(4, 3), unit.getLocation());
		assertEquals(owner, unit.getOwner());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testPlaceAtMap() throws Exception {
		makeColonist(model, 23, placeLocation, owner, 1);
		unit.disembark(Location.of(12, 12));
	}
	
	@Test
	public void testIsMovable() throws Exception {
		makeColonist(model, 23, placeLocation, owner, 1);
		
		final Location loc = Location.of(10, 12);
		new Expectations() {{
			model.getMap().isValid(loc); result = true;
			model.getMap().getTerrainTypeAt(loc); result = TerrainType.GRASSLAND;
			owner.getEnemyUnitsAt(loc); result = Lists.newArrayList();
		}};
		
		assertTrue(unit.isPossibleToMoveAt(loc));
	}
	
	@Test
	public void testIsMovable_thereAreEnemyUnit() throws Exception {
		makeColonist(model, 23, placeLocation, owner, 1);
		
		final Location loc = Location.of(10, 12);
		new Expectations() {{
			model.getMap().isValid(loc); result = true;
			model.getMap().getTerrainTypeAt(loc); result = TerrainType.GRASSLAND;
			owner.getEnemyUnitsAt(loc); result = Lists.newArrayList(unit);
		}};
		
		assertFalse(unit.isPossibleToMoveAt(loc));
	}
	
}