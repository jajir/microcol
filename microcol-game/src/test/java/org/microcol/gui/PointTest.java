package org.microcol.gui;

import static org.junit.Assert.*;

import org.junit.Test;
import org.microcol.model.Location;

public class PointTest {

	@Test
	public void test_toLocation() throws Exception {
		Point p1 = Point.of(20, 40);

		Location l1 = p1.toLocation();

		assertEquals(1, l1.getX());
		assertEquals(2, l1.getY());
	}

	@Test
	public void test_constructor_of_verify_that_max_and_min_value_can_be_set() throws Exception {
		Point p1 = Point.of(Integer.MAX_VALUE, Integer.MIN_VALUE);

		assertEquals(Integer.MAX_VALUE, p1.getX());
		assertEquals(Integer.MIN_VALUE, p1.getY());
	}

	@Test
	public void test_constructor_of_verify_that_simple_value_can_be_set() throws Exception {
		Point p1 = Point.of(13, -4);

		assertEquals(13, p1.getX());
		assertEquals(-4, p1.getY());
	}

}
