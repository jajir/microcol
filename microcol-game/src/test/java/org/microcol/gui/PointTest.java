package org.microcol.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.microcol.gui.gamepanel.GamePanelView;
import org.microcol.model.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PointTest {

	private final Logger logger = LoggerFactory.getLogger(PointTest.class);

	@Test
	public void test_simple() throws Exception {

		for (int i = 40; i > -40; i--) {
			Point p = Point.of(10, i);
			logger.debug(p + " -->  " + p.toLocation());
		}
	}

	@Test
	public void test_toLocation_simple_case() throws Exception {
		Point p1 = Point.of(GamePanelView.TILE_WIDTH_IN_PX / 2, GamePanelView.TILE_WIDTH_IN_PX * 3 / 2);

		Location l1 = p1.toLocation();

		assertEquals(1, l1.getX());
		assertEquals(2, l1.getY());
	}

	@Test
	public void test_toLocation_positive_close_to_0() throws Exception {
		Point p1 = Point.of(10, 10);

		Location l1 = p1.toLocation();

		assertEquals(1, l1.getX());
		assertEquals(1, l1.getY());
	}

	@Test
	public void test_toLocation_0() throws Exception {
		Point p1 = Point.of(0, 0);

		Location l1 = p1.toLocation();

		assertEquals(1, l1.getX());
		assertEquals(1, l1.getY());
	}

	@Test
	public void test_toLocation_negative_close_to_0() throws Exception {
		Point p1 = Point.of(1000, -6);

		Location l1 = p1.toLocation();

		assertEquals(23, l1.getX());
		assertEquals(0, l1.getY());
	}

	@Test
	public void test_toLocation_negative_close_to_1() throws Exception {
		Point p1 = Point.of(-55, -55);

		Location l1 = p1.toLocation();

		assertEquals(-1, l1.getX());
		assertEquals(-1, l1.getY());
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
