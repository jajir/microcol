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
public class PathCreationTest {
	@Parameters(name = "{index}: locations = {0}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{Arrays.asList(Location.of(2, 3))},
			{Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(3, 4), Location.of(3, 5), Location.of(4, 5))},
			// circle
			{Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(3, 4), Location.of(2, 4), Location.of(2, 3))},
			// back
			{Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(4, 3), Location.of(3, 3), Location.of(2, 3))},
		});
	}

	@Parameter(0)
	public List<Location> locations;

	@Test
	public void testCreation() {
		final Path path = Path.of(locations);

		Assert.assertEquals("Test of locations failed:", locations, path.getLocations());
		Assert.assertEquals("Test of start failed:", locations.get(0), path.getStart());
	}
}
