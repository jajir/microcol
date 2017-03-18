package org.microcol.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PathTest {
	@Test
	public void testCreationOne() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));

		final Path path = new Path(locations);
		Assert.assertEquals("Test of creation failed:", locations, path.getLocations());
	}

	@Test
	public void testCreationMore() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));
		locations.add(Location.of(3, 3));
		locations.add(Location.of(3, 4));
		locations.add(Location.of(3, 5));
		locations.add(Location.of(4, 5));

		final Path path = new Path(locations);
		Assert.assertEquals("Test of creation failed:", locations, path.getLocations());
	}

	@Test
	public void testCreationCircle() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));
		locations.add(Location.of(3, 3));
		locations.add(Location.of(3, 4));
		locations.add(Location.of(2, 4));
		locations.add(Location.of(2, 3));

		final Path path = new Path(locations);
		Assert.assertEquals("Test of creation failed:", locations, path.getLocations());
	}

	@Test
	public void testCreationBack() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));
		locations.add(Location.of(3, 3));
		locations.add(Location.of(4, 3));
		locations.add(Location.of(3, 3));
		locations.add(Location.of(2, 3));

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
		locations.add(Location.of(2, 3));
		locations.add(null);
		locations.add(Location.of(3, 2));

		new Path(locations);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationEmpty() {
		new Path(new ArrayList<>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalidSame() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));
		locations.add(Location.of(2, 3));

		new Path(locations);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalidGap() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));
		locations.add(Location.of(4, 3));

		new Path(locations);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testImmutable() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));

		final Path path = new Path(locations);
		path.getLocations().add(Location.of(3, 2));
	}

	public void testContains() {
		final String message = "Test contains failed: ";

		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));
		locations.add(Location.of(3, 3));
		locations.add(Location.of(3, 4));
		locations.add(Location.of(3, 5));
		locations.add(Location.of(4, 5));

		final Path path = new Path(locations);
		Assert.assertTrue(message + "[2, 3]", path.contains(Location.of(2, 3)));		
		Assert.assertTrue(message + "[3, 4]", path.contains(Location.of(2, 3)));		
		Assert.assertTrue(message + "[4, 5]", path.contains(Location.of(2, 3)));
		// TODO JKA
	}

	@Test(expected = NullPointerException.class)
	public void testContainsNull() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));

		final Path path = new Path(locations);
		path.contains(null);
	}

	public void testContainsAny() {
		
	}

	public void testContainsAnyNull() {
		final List<Location> locations = new ArrayList<>();
		locations.add(Location.of(2, 3));

		final Path path = new Path(locations);
		path.contains(null);
	}

	public void testGetFirstLocation() {
		
	}
}
