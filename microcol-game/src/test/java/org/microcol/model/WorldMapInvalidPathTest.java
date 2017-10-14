package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.microcol.model.store.WorldMapDao;

@RunWith(Parameterized.class)
public class WorldMapInvalidPathTest {
	@Parameters(name = "{index}: fileName = {0}, locations = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{"/maps/test-map-ocean-10x10.json", Arrays.asList(Location.of(2, 2), Location.of(1, 1), Location.of(0, 0))},
			{"/maps/test-map-ocean-10x10.json", Arrays.asList(Location.of(9, 9), Location.of(10, 10), Location.of(11, 11))},
		});
	}

	@Parameter(0)
	public String fileName;

	@Parameter(1)
	public List<Location> locations;

	@Test
	public void testInalidPath() {
		WorldMapDao dao = new WorldMapDao();
		WorldMap map = dao.loadMap(fileName);
		
		final Path path = Path.of(locations);

		Assert.assertFalse(map.isValid(path));
	}
}
