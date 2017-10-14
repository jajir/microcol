package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.microcol.model.store.WorldMapDao;

@RunWith(Parameterized.class)
public class WorldMapValidLocationTest {
	@Parameters(name = "{index}: fileName = {0}, location = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{"/maps/test-map-ocean-10x10.json", Location.of( 1,  1)},
			{"/maps/test-map-ocean-10x10.json", Location.of(10,  1)},
			{"/maps/test-map-ocean-10x10.json", Location.of(10, 10)},
			{"/maps/test-map-ocean-10x10.json", Location.of( 1, 10)},
			{"/maps/test-map-ocean-10x10.json", Location.of( 5,  5)},
		});
	}

	@Parameter(0)
	public String fileName;

	@Parameter(1)
	public Location location;

	@Test
	public void testValidLocation() {
		final WorldMapDao dao = new WorldMapDao();
		final WorldMap map = dao.loadMap(fileName);

		Assert.assertTrue(map.isValid(location));
	}
}
