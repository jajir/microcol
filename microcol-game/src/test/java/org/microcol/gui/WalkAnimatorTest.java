package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.panelview.WalkAnimator;
import org.microcol.model.Location;
import org.microcol.model.Ship;

public class WalkAnimatorTest {

	private PathPlanning pathPlanning;

	private Ship unit;

	@Test
	public void test_first_steps() throws Exception {
		List<Location> path = Arrays.asList(Location.of(1, 2), Location.of(2, 2));
		WalkAnimator wa = new WalkAnimator(pathPlanning, path, unit);

		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
		/**
		 * Multiple call should not change result.
		 */
		assertEquals(Point.of(36, 70), wa.getNextCoordinates());
		assertEquals(Point.of(36, 70), wa.getNextCoordinates());
		wa.countNextAnimationLocation();

		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
		assertEquals(Point.of(37, 70), wa.getNextCoordinates());
		assertEquals(Point.of(37, 70), wa.getNextCoordinates());
		wa.countNextAnimationLocation();

		assertEquals("Verify that list size is not changed", 2, path.size());
		assertEquals(Location.of(2, 2), wa.getTo());
	}

	@Test
	public void test_multi_tiles_steps() throws Exception {
		List<Location> path = Arrays.asList(Location.of(1, 2), Location.of(2, 2), Location.of(3, 4));
		WalkAnimator wa = new WalkAnimator(pathPlanning, path, unit);

		wa.countNextAnimationLocation();
		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
		while (wa.isNextAnimationLocationAvailable()) {
			wa.countNextAnimationLocation();
		}

		assertFalse("Animation step should not be available", wa.isNextAnimationLocationAvailable());

		/**
		 * Even after animation counting response should be consisstent.
		 */
		wa.countNextAnimationLocation();
		assertFalse("Animation step should not be available", wa.isNextAnimationLocationAvailable());
	}

	@Before
	public void setup() {
		pathPlanning = new PathPlanning();
		unit = EasyMock.createMock(Ship.class);
	}

	@After
	public void tearDown() {
		pathPlanning = null;
		unit = null;
	}

}
