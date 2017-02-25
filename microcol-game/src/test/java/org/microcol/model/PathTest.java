package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PathTest {
	@Test
	public void testCreationOne() {
		final List<Location> locations = new ArrayList<>();
		locations.add(new Location(2, 3));

		final Path path = new Path(locations);
		Assert.assertEquals("Test of creation failed:", locations, path.getLocations());
	}

	@Test
	public void testCreationMore() {
		final List<Location> locations = new ArrayList<>();
		locations.add(new Location(2, 3));
		locations.add(new Location(3, 3));
		locations.add(new Location(3, 4));
		locations.add(new Location(3, 5));
		locations.add(new Location(4, 5));

		final Path path = new Path(locations);
		Assert.assertEquals("Test of creation failed:", locations, path.getLocations());
	}

	@Test(expected = NullPointerException.class)
	public void testCreationNull() {
		new Path(null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreationNullElement() {
		final List<Location> locations = new ArrayList<>();
		locations.add(new Location(2, 3));
		locations.add(null);
		locations.add(new Location(3, 2));

		new Path(locations);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationEmpty() {
		new Path(new ArrayList<>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalidSame() {
		final List<Location> locations = new ArrayList<>();
		locations.add(new Location(2, 3));
		locations.add(new Location(2, 3));

		new Path(locations);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalidGap() {
		final List<Location> locations = new ArrayList<>();
		locations.add(new Location(2, 3));
		locations.add(new Location(4, 3));

		new Path(locations);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testImmutable() {
		final List<Location> locations = new ArrayList<>();
		locations.add(new Location(2, 3));

		final Path path = new Path(locations);
		path.getLocations().add(new Location(3, 2));
	}
}
