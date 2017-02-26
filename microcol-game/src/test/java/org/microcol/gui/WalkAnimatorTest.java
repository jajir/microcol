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
import org.microcol.model.Location;
import org.microcol.model.Ship;

public class WalkAnimatorTest {

	private PathPlanning pathPlanning;

	private Ship unit;

	@Test
	public void test_first_steps() throws Exception {
		List<Location> path = Arrays.asList(Location.make(1, 2), Location.make(2, 2));
		WalkAnimator wa = new WalkAnimator(pathPlanning, path, unit);

		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
		/**
		 * Multiple call should not change result.
		 */
		assertEquals(Location.make(31, 62), wa.getNextCoordinates());
		assertEquals(Location.make(31, 62), wa.getNextCoordinates());
		wa.countNextAnimationLocation();

		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
		assertEquals(Location.make(32, 62), wa.getNextCoordinates());
		assertEquals(Location.make(32, 62), wa.getNextCoordinates());
		wa.countNextAnimationLocation();

		assertEquals("Verify that list size is not changed", 2, path.size());
		assertEquals(Location.make(2, 2), wa.getTo());
	}

	@Test
	public void test_multi_tiles_steps() throws Exception {
		List<Location> path = Arrays.asList(Location.make(1, 2), Location.make(2, 2), Location.make(3, 4));
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
