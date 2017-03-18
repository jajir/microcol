package org.microcol.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PathTest {
	@Test(expected = NullPointerException.class)
	public void testCreationNull() {
		Path.of(null);
	}

	@Test(expected = NullPointerException.class)
	public void testCreationNullElement() {
		Path.of(Arrays.asList(Location.of(2, 3), null, Location.of(3, 2)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationEmpty() {
		Path.of(new ArrayList<>());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalidSame() {
		Path.of(Arrays.asList(Location.of(2, 3), Location.of(2, 3)));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalidAdjacent() {
		Path.of(Arrays.asList(Location.of(2, 3), Location.of(4, 3)));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void testImmutable() {
		final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

		path.getLocations().add(Location.of(3, 2));
	}

	@Test
	public void testContains() {
		final List<Location> locations = Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(3, 4));
		final Path path = Path.of(locations);

		for (Location location : locations) {
			Assert.assertTrue(String.format("%s not found.", location), path.contains(location));
		}
	}

	@Test(expected = NullPointerException.class)
	public void testContainsNull() {
		final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

		path.contains(null);
	}

	@Test
	public void testContainsAny() {
		final Path path = Path.of(Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(3, 4)));
		final List<Location> locations = Arrays.asList(Location.of(1, 1), Location.of(2, 2), Location.of(3, 3));

		Assert.assertTrue(path.containsAny(locations));
	}

	@Test(expected = NullPointerException.class)
	public void testContainsAnyNull() {
		final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

		path.containsAny(null);
	}

	@Test(expected = NullPointerException.class)
	public void testContainsAnyNullInside() {
		final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

		path.containsAny(Arrays.asList(Location.of(2, 3), null, Location.of(3, 2)));
	}
}
