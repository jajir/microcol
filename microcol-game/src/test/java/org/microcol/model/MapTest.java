package org.microcol.model;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class MapTest {
	@Test
	public void testValidLocation() {
		final String message = "Test of isValid location failed: ";

		final Map map = new Map(10, 10);

		Assert.assertTrue(message + "[1, 1]", map.isValid(Location.of(1, 1)));
		Assert.assertTrue(message + "[10, 1]", map.isValid(Location.of(10, 1)));
		Assert.assertTrue(message + "[10, 10]", map.isValid(Location.of(10, 10)));
		Assert.assertTrue(message + "[1, 10]", map.isValid(Location.of(1, 10)));
		Assert.assertTrue(message + "[5, 5]", map.isValid(Location.of(5, 5)));

		Assert.assertFalse(message + "[0, 0]", map.isValid(Location.of(0, 0)));
		Assert.assertFalse(message + "[0, 5]", map.isValid(Location.of(0, 5)));
		Assert.assertFalse(message + "[5, 0]", map.isValid(Location.of(5, 0)));
		Assert.assertFalse(message + "[11, 5]", map.isValid(Location.of(11, 5)));
		Assert.assertFalse(message + "[5, 11]", map.isValid(Location.of(5, 11)));
		Assert.assertFalse(message + "[11, 11]", map.isValid(Location.of(11, 11)));
	}

	@Test
	public void testValidPath() {
		final String message = "Test of isValid path failed: ";

		final Map map = new Map(10, 10);

		Path path = Path.of(Arrays.asList(Location.of(1, 1), Location.of(2, 2), Location.of(3, 3)));
		Assert.assertTrue(message + path, map.isValid(path));

		path = Path.of(Arrays.asList(Location.of(10, 10), Location.of(9, 9), Location.of(8, 8)));
		Assert.assertTrue(message + path, map.isValid(path));

		path = Path.of(Arrays.asList(Location.of(4, 6), Location.of(5, 5), Location.of(6, 4)));
		Assert.assertTrue(message + path, map.isValid(path));

		path = Path.of(Arrays.asList(Location.of(2, 2), Location.of(1, 1), Location.of(0, 0)));
		Assert.assertFalse(message + path, map.isValid(path));

		path = Path.of(Arrays.asList(Location.of(9, 9), Location.of(10, 10), Location.of(11, 11)));
		Assert.assertFalse(message + path, map.isValid(path));
	}
}
