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

@RunWith(Parameterized.class)
public class WorldMapValidPathTest {
	@Parameters(name = "{index}: fileName = {0}, locations = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{"/maps/test-map-ocean-10x10.txt", Arrays.asList(Location.of(1, 1), Location.of(2, 2), Location.of(3, 3))},
			{"/maps/test-map-ocean-10x10.txt", Arrays.asList(Location.of(10, 10), Location.of(9, 9), Location.of(8, 8))},
			{"/maps/test-map-ocean-10x10.txt", Arrays.asList(Location.of(4, 6), Location.of(5, 5), Location.of(6, 4))},
		});
	}

	@Parameter(0)
	public String fileName;

	@Parameter(1)
	public List<Location> locations;

	@Test
	public void testValidPath() {
		final WorldMap map = new WorldMap(fileName);
		final Path path = Path.of(locations);

		Assert.assertTrue(map.isValid(path));
	}
}
