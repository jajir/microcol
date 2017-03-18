package org.microcol.model;

import org.junit.Test;

public class LocationTest {
	@Test(expected = NullPointerException.class)
	public void testAdjacentNull() {
		Location.of(5, 5).isAdjacent(null);
	}

	@Test(expected = NullPointerException.class)
	public void testAddNull() {
		Location.of(5, 5).add(null);
	}
}
