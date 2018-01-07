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
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.WorldMapPo;

@RunWith(Parameterized.class)
public class WorldMapCreationTest {
	@Parameters(name = "{index}: maxX = {0}, maxY = {1}, seed = {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{15, 10, 128},
			{34, 29, 256},
		});
	}

	@Parameter(0)
	public int maxX;

	@Parameter(1)
	public int maxY;

	@Parameter(2)
	public Integer seed;

	@Test
	public void testCreation() {
		WorldMapPo mapPo = new WorldMapPo();
		mapPo.setMaxX(maxX);
		mapPo.setMaxY(maxY);
		mapPo.setSeed(seed);
		mapPo.setTerrainType(new HashMap<>());
		ModelPo gamePo = new ModelPo();
		gamePo.setMap(mapPo);
		final WorldMap map = new WorldMap(gamePo);

		Assert.assertEquals("Test of maxX failed.", maxX, map.getMaxX());
		Assert.assertEquals("Test of maxY failed.", maxY, map.getMaxY());
		Assert.assertEquals("Test of seed failed.", seed, map.getSeed());
	}
}
