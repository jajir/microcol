package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Function;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitColonistAtLocationTest {

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
	public void testInitialization() {
		new Expectations() {{
			placeMap.getLocation(); result = Location.of(4, 3);
		}};
		
		assertNotNull(unit);
		assertEquals(Location.of(4, 3), unit.getLocation());
		assertEquals(owner, unit.getOwner());
	}
	
	@Test(expected = IllegalStateException.class)
	public void testPlaceAtMap() throws Exception {
		unit.unload(Location.of(12, 12));
	}
	
	@Test
	public void testIsMovable() throws Exception {
		final Location loc = Location.of(10, 12);
		new Expectations() {{
			model.getMap().isValid(loc); result = true;
			model.getMap().getTerrainTypeAt(loc); result = TerrainType.GRASSLAND;
			unitType.canMoveAtTerrain(TerrainType.GRASSLAND); result = true;
			owner.getEnemyUnitsAt(loc); result = Lists.newArrayList();
		}};
		
		assertTrue(unit.isMoveable(loc));
	}
	
	@Test
	public void testIsMovable_thereAreEnemyUnit() throws Exception {
		final Location loc = Location.of(10, 12);
		new Expectations() {{
			model.getMap().isValid(loc); result = true;
			model.getMap().getTerrainTypeAt(loc); result = TerrainType.GRASSLAND;
			unitType.canMoveAtTerrain(TerrainType.GRASSLAND); result = true;
			owner.getEnemyUnitsAt(loc); result = Lists.newArrayList(unit);
		}};
		
		assertFalse(unit.isMoveable(loc));
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
	
	@After
	public void teardown() {
		unit = null;
	}
	
}