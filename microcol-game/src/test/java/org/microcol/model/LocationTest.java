package org.microcol.model;

import org.junit.Assert;
import org.junit.Test;

public class LocationTest {
	@Test
	public void testCreation() {
		final String message = "Test of creation failed: ";

		Location location = new Location(0, 0);
		Assert.assertEquals(message + "[0, 0]", 0, location.getX());
		Assert.assertEquals(message + "[0, 0]", 0, location.getY());

		location = new Location(2, 3);
		Assert.assertEquals(message + "[2, 3]", 2, location.getX());
		Assert.assertEquals(message + "[2, 3]", 3, location.getY());

		location = new Location(-2, 3);
		Assert.assertEquals(message + "[-2, 3]", -2, location.getX());
		Assert.assertEquals(message + "[-2, 3]", 3, location.getY());

		location = new Location(2, -3);
		Assert.assertEquals(message + "[2, -3]", 2, location.getX());
		Assert.assertEquals(message + "[2, -3]", -3, location.getY());

		location = new Location(-2, -3);
		Assert.assertEquals(message + "[-2, -3]", -2, location.getX());
		Assert.assertEquals(message + "[-2, -3]", -3, location.getY());
	}

	@Test
	public void testCreationStatic() {
		final String message = "Test of creation failed: ";

		Location location = Location.make(0, 0);
		Assert.assertEquals(message + "[0, 0]", 0, location.getX());
		Assert.assertEquals(message + "[0, 0]", 0, location.getY());

		location = Location.make(2, 3);
		Assert.assertEquals(message + "[2, 3]", 2, location.getX());
		Assert.assertEquals(message + "[2, 3]", 3, location.getY());

		location = Location.make(-2, 3);
		Assert.assertEquals(message + "[-2, 3]", -2, location.getX());
		Assert.assertEquals(message + "[-2, 3]", 3, location.getY());

		location = Location.make(2, -3);
		Assert.assertEquals(message + "[2, -3]", 2, location.getX());
		Assert.assertEquals(message + "[2, -3]", -3, location.getY());

		location = Location.make(-2, -3);
		Assert.assertEquals(message + "[-2, -3]", -2, location.getX());
		Assert.assertEquals(message + "[-2, -3]", -3, location.getY());
	}

	@Test
	public void testAdjacent() {
		final String message = "Test of isAdjacent failed: ";

		Assert.assertTrue(message + "[5, 5] - [4, 4]", new Location(5, 5).isAdjacent(new Location(4, 4)));
		Assert.assertTrue(message + "[5, 5] - [5, 4]", new Location(5, 5).isAdjacent(new Location(5, 4)));
		Assert.assertTrue(message + "[5, 5] - [6, 4]", new Location(5, 5).isAdjacent(new Location(6, 4)));
		Assert.assertTrue(message + "[5, 5] - [4, 5]", new Location(5, 5).isAdjacent(new Location(4, 5)));
		Assert.assertTrue(message + "[5, 5] - [6, 5]", new Location(5, 5).isAdjacent(new Location(6, 5)));
		Assert.assertTrue(message + "[5, 5] - [4, 6]", new Location(5, 5).isAdjacent(new Location(4, 6)));
		Assert.assertTrue(message + "[5, 5] - [5, 6]", new Location(5, 5).isAdjacent(new Location(5, 6)));
		Assert.assertTrue(message + "[5, 5] - [6, 6]", new Location(5, 5).isAdjacent(new Location(6, 6)));

		// Same location is not considered adjacent.
		Assert.assertFalse(message + "[5, 5] - [5, 5]", new Location(5, 5).isAdjacent(new Location(5, 5)));

		Assert.assertFalse(message + "[5, 5] - [3, 4]", new Location(5, 5).isAdjacent(new Location(3, 4)));
		Assert.assertFalse(message + "[5, 5] - [4, 3]", new Location(5, 5).isAdjacent(new Location(4, 3)));
		Assert.assertFalse(message + "[5, 5] - [6, 3]", new Location(5, 5).isAdjacent(new Location(6, 3)));
		Assert.assertFalse(message + "[5, 5] - [7, 4]", new Location(5, 5).isAdjacent(new Location(7, 4)));
		Assert.assertFalse(message + "[5, 5] - [3, 6]", new Location(5, 5).isAdjacent(new Location(3, 6)));
		Assert.assertFalse(message + "[5, 5] - [4, 7]", new Location(5, 5).isAdjacent(new Location(4, 7)));
		Assert.assertFalse(message + "[5, 5] - [6, 7]", new Location(5, 5).isAdjacent(new Location(6, 7)));
		Assert.assertFalse(message + "[5, 5] - [7, 6]", new Location(5, 5).isAdjacent(new Location(7, 6)));
	}

	@Test(expected = NullPointerException.class)
	public void testAdjacentNull() {
		new Location(5, 5).isAdjacent(null);
	}

	@Test
	public void testHashCode() {
		final String message = "Test of hasCode failed:";

		Assert.assertEquals(message, 0, new Location(0, 0).hashCode());
		Assert.assertEquals(message, 1, new Location(1, 0).hashCode());
		Assert.assertEquals(message, -1, new Location(-1, 0).hashCode());
		Assert.assertEquals(message, 65536, new Location(0, 1).hashCode());
		Assert.assertEquals(message, -65536, new Location(0, -1).hashCode());
		Assert.assertEquals(message, 196610, new Location(2, 3).hashCode());
		Assert.assertEquals(message, 196606, new Location(-2, 3).hashCode());
		Assert.assertEquals(message, -196606, new Location(2, -3).hashCode());
		Assert.assertEquals(message, -196610, new Location(-2, -3).hashCode());
		Assert.assertEquals(message, 131075, new Location(3, 2).hashCode());
		Assert.assertEquals(message, 131069, new Location(-3, 2).hashCode());
		Assert.assertEquals(message, -131069, new Location(3, -2).hashCode());
		Assert.assertEquals(message, -131075, new Location(-3, -2).hashCode());
	}

	@Test
	public void testEquals() {
		final String message = "Test of equals failed: ";

		Assert.assertTrue(message + "[0, 0]", new Location(0, 0).equals(new Location(0, 0)));
		Assert.assertTrue(message + "[2, 3]", new Location(2, 3).equals(new Location(2, 3)));
		Assert.assertTrue(message + "[3, 2]", new Location(3, 2).equals(new Location(3, 2)));

		Assert.assertFalse(message + "[2, 3] - null", new Location(2, 3).equals(null));
		Assert.assertFalse(message + "[2, 3] - 10", new Location(2, 3).equals(10));
		Assert.assertFalse(message + "[2, 3] - [3, 2]", new Location(2, 3).equals(new Location(3, 2)));
	}
}
