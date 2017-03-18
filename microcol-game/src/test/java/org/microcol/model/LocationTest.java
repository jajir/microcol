package org.microcol.model;

import org.junit.Assert;
import org.junit.Test;

public class LocationTest {
	@Test
	public void testCreation() {
		final String message = "Test of creation failed: ";

		Location location = Location.of(0, 0);
		Assert.assertEquals(message + "[0, 0]", 0, location.getX());
		Assert.assertEquals(message + "[0, 0]", 0, location.getY());

		location = Location.of(2, 3);
		Assert.assertEquals(message + "[2, 3]", 2, location.getX());
		Assert.assertEquals(message + "[2, 3]", 3, location.getY());

		location = Location.of(-2, 3);
		Assert.assertEquals(message + "[-2, 3]", -2, location.getX());
		Assert.assertEquals(message + "[-2, 3]", 3, location.getY());

		location = Location.of(2, -3);
		Assert.assertEquals(message + "[2, -3]", 2, location.getX());
		Assert.assertEquals(message + "[2, -3]", -3, location.getY());

		location = Location.of(-2, -3);
		Assert.assertEquals(message + "[-2, -3]", -2, location.getX());
		Assert.assertEquals(message + "[-2, -3]", -3, location.getY());
	}

	@Test
	public void testAdjacent() {
		final String message = "Test of isAdjacent failed: ";

		Assert.assertTrue(message + "[5, 5] - [4, 4]", Location.of(5, 5).isAdjacent(Location.of(4, 4)));
		Assert.assertTrue(message + "[5, 5] - [5, 4]", Location.of(5, 5).isAdjacent(Location.of(5, 4)));
		Assert.assertTrue(message + "[5, 5] - [6, 4]", Location.of(5, 5).isAdjacent(Location.of(6, 4)));
		Assert.assertTrue(message + "[5, 5] - [4, 5]", Location.of(5, 5).isAdjacent(Location.of(4, 5)));
		Assert.assertTrue(message + "[5, 5] - [6, 5]", Location.of(5, 5).isAdjacent(Location.of(6, 5)));
		Assert.assertTrue(message + "[5, 5] - [4, 6]", Location.of(5, 5).isAdjacent(Location.of(4, 6)));
		Assert.assertTrue(message + "[5, 5] - [5, 6]", Location.of(5, 5).isAdjacent(Location.of(5, 6)));
		Assert.assertTrue(message + "[5, 5] - [6, 6]", Location.of(5, 5).isAdjacent(Location.of(6, 6)));

		// Same location is not considered adjacent.
		Assert.assertFalse(message + "[5, 5] - [5, 5]", Location.of(5, 5).isAdjacent(Location.of(5, 5)));

		Assert.assertFalse(message + "[5, 5] - [3, 4]", Location.of(5, 5).isAdjacent(Location.of(3, 4)));
		Assert.assertFalse(message + "[5, 5] - [4, 3]", Location.of(5, 5).isAdjacent(Location.of(4, 3)));
		Assert.assertFalse(message + "[5, 5] - [6, 3]", Location.of(5, 5).isAdjacent(Location.of(6, 3)));
		Assert.assertFalse(message + "[5, 5] - [7, 4]", Location.of(5, 5).isAdjacent(Location.of(7, 4)));
		Assert.assertFalse(message + "[5, 5] - [3, 6]", Location.of(5, 5).isAdjacent(Location.of(3, 6)));
		Assert.assertFalse(message + "[5, 5] - [4, 7]", Location.of(5, 5).isAdjacent(Location.of(4, 7)));
		Assert.assertFalse(message + "[5, 5] - [6, 7]", Location.of(5, 5).isAdjacent(Location.of(6, 7)));
		Assert.assertFalse(message + "[5, 5] - [7, 6]", Location.of(5, 5).isAdjacent(Location.of(7, 6)));
	}

	@Test(expected = NullPointerException.class)
	public void testAdjacentNull() {
		Location.of(5, 5).isAdjacent(null);
	}

	@Test
	public void testAdd() {
		final String message = "Test of add failed: ";

		Assert.assertEquals(message + "[3, 2] + [3, 2]", Location.of(6, 4), Location.of(3, 2).add(Location.of(3, 2)));
		Assert.assertEquals(message + "[3, 2] + [-3, 2]", Location.of(0, 4), Location.of(3, 2).add(Location.of(-3, 2)));
		Assert.assertEquals(message + "[3, 2] + [3, -2]", Location.of(6, 0), Location.of(3, 2).add(Location.of(3, -2)));
		Assert.assertEquals(message + "[3, 2] + [-3, -2]", Location.of(0, 0), Location.of(3, 2).add(Location.of(-3, -2)));
		Assert.assertEquals(message + "[-3, 2] + [-3, 2]", Location.of(-6, 4), Location.of(-3, 2).add(Location.of(-3, 2)));
		Assert.assertEquals(message + "[3, -2] + [3, -2]", Location.of(6, -4), Location.of(3, -2).add(Location.of(3, -2)));
		Assert.assertEquals(message + "[-3, -2] + [-3, -2]", Location.of(-6, -4), Location.of(-3, -2).add(Location.of(-3, -2)));
	}

	@Test(expected = NullPointerException.class)
	public void testAddNull() {
		Location.of(5, 5).add(null);
	}

	@Test
	public void testHashCode() {
		final String message = "Test of hasCode failed:";

		Assert.assertEquals(message, 0, Location.of(0, 0).hashCode());
		Assert.assertEquals(message, 1, Location.of(1, 0).hashCode());
		Assert.assertEquals(message, -1, Location.of(-1, 0).hashCode());
		Assert.assertEquals(message, 65536, Location.of(0, 1).hashCode());
		Assert.assertEquals(message, -65536, Location.of(0, -1).hashCode());
		Assert.assertEquals(message, 196610, Location.of(2, 3).hashCode());
		Assert.assertEquals(message, 196606, Location.of(-2, 3).hashCode());
		Assert.assertEquals(message, -196606, Location.of(2, -3).hashCode());
		Assert.assertEquals(message, -196610, Location.of(-2, -3).hashCode());
		Assert.assertEquals(message, 131075, Location.of(3, 2).hashCode());
		Assert.assertEquals(message, 131069, Location.of(-3, 2).hashCode());
		Assert.assertEquals(message, -131069, Location.of(3, -2).hashCode());
		Assert.assertEquals(message, -131075, Location.of(-3, -2).hashCode());
	}

	@Test
	public void testEquals() {
		final String message = "Test of equals failed: ";

		Assert.assertTrue(message + "[0, 0]", Location.of(0, 0).equals(Location.of(0, 0)));
		Assert.assertTrue(message + "[2, 3]", Location.of(2, 3).equals(Location.of(2, 3)));
		Assert.assertTrue(message + "[3, 2]", Location.of(3, 2).equals(Location.of(3, 2)));

		Assert.assertFalse(message + "[2, 3] - null", Location.of(2, 3).equals(null));
		Assert.assertFalse(message + "[2, 3] - 10", Location.of(2, 3).equals(10));
		Assert.assertFalse(message + "[2, 3] - [3, 2]", Location.of(2, 3).equals(Location.of(3, 2)));
	}
}
