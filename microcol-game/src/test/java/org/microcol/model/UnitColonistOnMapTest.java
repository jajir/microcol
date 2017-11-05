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