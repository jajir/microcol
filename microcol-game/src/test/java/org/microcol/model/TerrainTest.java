package org.microcol.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class TerrainTest {
	
	@Test
	public void test_setHasTrees() {
		assertTrue(TerrainType.GRASSLAND.isCanHaveTree());

		Terrain t = new Terrain(Location.of(1, 1), TerrainType.GRASSLAND);
		assertFalse(t.isHasTrees());

		t.setHasTrees(true);
		assertTrue(t.isHasTrees());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_setHasTrees_varify_that_terrainTypeDoesntSupportTrees() {
		assertFalse(TerrainType.ARCTIC.isCanHaveTree());

		Terrain t = new Terrain(Location.of(1, 1), TerrainType.ARCTIC);
		assertFalse(t.isHasTrees());

		t.setHasTrees(true);
	}

}
