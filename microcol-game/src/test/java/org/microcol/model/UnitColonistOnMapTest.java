package org.microcol.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;

public class UnitColonistOnMapTest extends AbstractUnitTest {

	@Mocked
	private PlaceLocation placeLocation;

	private final List<Location> locations = Lists.newArrayList(Location.of(7, 5), Location.of(7, 6), Location.of(7, 7));
	
	private final Location unitLoc = Location.of(7, 4);
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_inHarbor(final @Mocked PlaceEuropePier placeEuropePier) throws Exception {
		makeUnit(cargo, model, 23, placeEuropePier, unitType, owner, 10);
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_gameIsNotRunning() throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		new Expectations() {{
			model.checkGameRunning(); result = new IllegalStateException();
		}};
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_invalid_currentPlayer() throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		new Expectations() {{
			model.checkCurrentPlayer(owner); result = new IllegalStateException();
		}};
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = NullPointerException.class)
	public void test_moveOneStep_moveTo_isNull() throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		
		unit.moveOneStep(null);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_isNotNeighbor() throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		
		unit.moveOneStep(Location.of(10, 10));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_isInvalid(final @Mocked WorldMap map) throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(Location.of(7, 5)); result = false;
		}};
		
		unit.moveOneStep(Location.of(7, 5));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_invalid_terrainType(final @Mocked WorldMap map) throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.HIGH_SEA;
			unitType.canMoveAtTerrain(TerrainType.HIGH_SEA); result = false;
		}};
		
		unit.moveOneStep(moveAt);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_moveOneStep_moveTo_isAttack(final @Mocked WorldMap map) throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.GRASSLAND;
			unitType.canMoveAtTerrain(TerrainType.GRASSLAND); result = true;
			owner.getEnemyUnitsAt(moveAt); result = Lists.newArrayList("d");
		}};
		
		unit.moveOneStep(moveAt);
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_moveOneStep_moveTo_notEnough_availableMoves(final @Mocked WorldMap map) throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 0);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.GRASSLAND;
			unitType.canMoveAtTerrain(TerrainType.GRASSLAND); result = true;
			owner.getEnemyUnitsAt(moveAt); result = Lists.newArrayList();
		}};
		
		unit.moveOneStep(moveAt);
	}
	
	@Test
	public void test_moveOneStep_moveTo(final @Mocked WorldMap map) throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 10);
		final Location moveAt = Location.of(7, 5);
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.getMap(); result = map;
			map.isValid(moveAt); result = true;
			map.getTerrainTypeAt(moveAt); result = TerrainType.GRASSLAND;
			unitType.canMoveAtTerrain(TerrainType.GRASSLAND); result = true;
			owner.getEnemyUnitsAt(moveAt); result = Lists.newArrayList();
		}};
		
		unit.moveOneStep(moveAt);
		assertEquals(9, unit.getAvailableMoves());
		assertTrue(unit.isAtPlaceLocation());
		//TODO assert new location, it's not easy because PlaceLocation is alwais mock.
	}
	
	@Test
	public void test_moveTo_availableMoves_1_path_length_3() throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 1);
		
		Path path = Path.of(locations);
		
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.isValid(path); result = true;
			
			//is possible to move method mocking
			model.getMap(); result = worldMap;
			worldMap.isValid((Location)any); result = true;
			unitType.canMoveAtTerrain((TerrainType)any); result = true;
		}};
		
		unit.moveTo(path);
		
		new Verifications() {{
			Path p;
			model.fireUnitMoved(unit, Location.of(7, 4), p = withCapture());
			assertEquals(1, p.getLocations().size());
			p.getLocations().contains(Location.of(7, 5));
		}};
		
		assertEquals(0, unit.getAvailableMoves());
	}

	@Test
	public void test_moveTo_availableMoves_3_path_length_3() throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 3);

		Path path = Path.of(locations);
		
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.isValid(path); result = true;
			
			//is possible to move method mocking
			model.getMap(); result = worldMap;
			worldMap.isValid((Location)any); result = true;
			unitType.canMoveAtTerrain((TerrainType)any); result = true;
		}};
		
		unit.moveTo(path);
		
		new Verifications() {{
			Path p;
			model.fireUnitMoved(unit, Location.of(7, 4), p = withCapture());
			assertEquals(3, p.getLocations().size());
			p.getLocations().contains(Location.of(7, 5));
			p.getLocations().contains(Location.of(7, 6));
			p.getLocations().contains(Location.of(7, 7));
		}};
		
		assertEquals(0, unit.getAvailableMoves());
	}

	@Test
	public void test_moveTo_availableMoves_3_path_length_1() throws Exception {
		makeUnit(cargo, model, 23, placeLocation, unitType, owner, 3);

		Path path = Path.of(Lists.newArrayList(Location.of(7, 5)));
		
		new Expectations() {{
			placeLocation.getLocation(); result = unitLoc;
			model.isValid(path); result = true;
			
			//is possible to move method mocking
			model.getMap(); result = worldMap;
			worldMap.isValid((Location)any); result = true;
			unitType.canMoveAtTerrain((TerrainType)any); result = true;
		}};
		
		unit.moveTo(path);
		
		new Verifications() {{
			Path p;
			model.fireUnitMoved(unit, Location.of(7, 4), p = withCapture());
			assertEquals(1, p.getLocations().size());
			p.getLocations().contains(Location.of(7, 5));
		}};
		
		assertEquals(2, unit.getAvailableMoves());
	}


}