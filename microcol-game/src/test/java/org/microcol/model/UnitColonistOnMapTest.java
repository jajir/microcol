package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Mocked;

public class UnitColonistOnMapTest extends AbstractUnitFreeColonistTest {

	@Mocked
	private PlaceLocation placeLocation;

	private final Location unitLoc = Location.of(7, 4);
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_inHarbor(final @Mocked PlaceEuropePier placeEuropePier) throws Exception {
		makeColonist(model, 23, placeEuropePier, owner, 10);
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_gameIsNotRunning() throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		new Expectations() {{
			model.checkGameRunning(); result = new IllegalStateException();
		}};
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_invalid_currentPlayer() throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		new Expectations() {{
			model.checkCurrentPlayer(owner); result = new IllegalStateException();
		}};
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = NullPointerException.class)
	public void test_moveOneStep_moveTo_isNull() throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		
		unit.moveOneStep(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_isNotNeighbor() throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		
		unit.moveOneStep(Location.of(10, 10));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_isInvalid(final @Mocked WorldMap map) throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(Location.of(7, 5)); result = false;
		}};
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_invalid_terrainType(final @Mocked WorldMap map) throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.HIGH_SEA;
		}};
		
		unit.moveOneStep(moveAt);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_isAttack(final @Mocked WorldMap map) throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.GRASSLAND;
			owner.getEnemyUnitsAt(moveAt); result = Lists.newArrayList("d");
		}};
		
		unit.moveOneStep(moveAt);
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_moveTo_notEnough_availableMoves(final @Mocked WorldMap map) throws Exception {
		makeColonist(model, 23, placeLocation, owner, 0);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.GRASSLAND;
			owner.getEnemyUnitsAt(moveAt); result = Lists.newArrayList();
		}};
		
		unit.moveOneStep(moveAt);
	}
	
	@Test
	public void test_moveOneStep_moveTo(final @Mocked WorldMap map) throws Exception {
		makeColonist(model, 23, placeLocation, owner, 10);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.GRASSLAND;
			owner.getEnemyUnitsAt(moveAt); result = Lists.newArrayList();
		}};
		
		unit.moveOneStep(moveAt);
		assertEquals(9, unit.getActionPoints());
		assertTrue(unit.isAtPlaceLocation());
		//TODO assert new location, it's not easy because PlaceLocation is alwais mock.
	}


}