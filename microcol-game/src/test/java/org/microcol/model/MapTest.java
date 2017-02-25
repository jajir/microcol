package org.microcol.model;

import org.junit.Assert;
import org.junit.Test;

public class MapTest {
	@Test
	public void testValidLocation() {
		final String message = "Test of isValid location failed: ";

		final Map map = new Map(10, 10);

		Assert.assertTrue(message + "[0, 0]", map.isValid(new Location(0, 0)));
		Assert.assertTrue(message + "[10, 0]", map.isValid(new Location(10, 0)));
		Assert.assertTrue(message + "[10, 10]", map.isValid(new Location(10, 10)));
		Assert.assertTrue(message + "[0, 10]", map.isValid(new Location(0, 10)));
		Assert.assertTrue(message + "[5, 5]", map.isValid(new Location(5, 5)));

		Assert.assertFalse(message + "[-1, -1]", map.isValid(new Location(-1, -1)));
		Assert.assertFalse(message + "[-1, 5]", map.isValid(new Location(-1, 5)));
		Assert.assertFalse(message + "[5, -1]", map.isValid(new Location(5, -1)));
		Assert.assertFalse(message + "[11, 5]", map.isValid(new Location(11, 5)));
		Assert.assertFalse(message + "[5, 11]", map.isValid(new Location(5, 11)));
		Assert.assertFalse(message + "[11, 11]", map.isValid(new Location(11, 11)));
	}

	@Test
	public void testValidPath() {
		final String message = "Test of isValid path failed: ";

		final Map map = new Map(10, 10);

		Path path = new PathBuilder().add(0, 0).add(1, 1).add(2, 2).build();
		Assert.assertTrue(message + path, map.isValid(path));

		path = new PathBuilder().add(10, 10).add(9, 9).add(8, 8).build();
		Assert.assertTrue(message + path, map.isValid(path));

		path = new PathBuilder().add(4, 6).add(5, 5).add(6, 4).build();
		Assert.assertTrue(message + path, map.isValid(path));

		path = new PathBuilder().add(1, 1).add(0, 0).add(-1, -1).build();
		Assert.assertFalse(message + path, map.isValid(path));

		path = new PathBuilder().add(9, 9).add(10, 10).add(11, 11).build();
		Assert.assertFalse(message + path, map.isValid(path));
	}
}