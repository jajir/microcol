package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.microcol.model.store.GamePo;
import org.microcol.model.store.WorldMapPo;

@RunWith(Parameterized.class)
public class WorldMapCreationTest {
	@Parameters(name = "{index}: fileName = {0}, maxX = {1}, maxY = {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{"/maps/test-map-2islands-15x10.txt", 15, 10},
			{"/maps/test-map-2islands-15x10.txt", 34, 29},
		});
	}

	@Parameter(0)
	public String fileName;

	@Parameter(1)
	public int maxX;

	@Parameter(2)
	public int maxY;

	@Test
	public void testCreation() {
		WorldMapPo mapPo = new WorldMapPo();
		mapPo.setMaxX(maxX);
		mapPo.setMaxY(maxY);
		mapPo.setTerrainType(new HashMap<>());
		GamePo gamePo = new GamePo();
		gamePo.setMap(mapPo);
		final WorldMap map = new WorldMap(gamePo);

		Assert.assertEquals("Test of maxX failed.", maxX, map.getMaxX());
		Assert.assertEquals("Test of maxY failed.", maxY, map.getMaxY());
	}
}
