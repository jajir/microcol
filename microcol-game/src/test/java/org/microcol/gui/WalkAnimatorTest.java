package org.microcol.gui;

import static org.junit.Assert.assertNotNull;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.model.Ship;

public class WalkAnimatorTest {

	private PathPlanning pathPlanning;

	private Ship unit;

	@Test
	public void test_first_steps() throws Exception {
		assertNotNull(pathPlanning);
		assertNotNull(unit);
		//FIXME JJ I can't figure out how to test following code
//		List<Location> path = Arrays.asList(Location.of(1, 2), Location.of(2, 2));
//		Capture<WhatToDoWithPointInPath> capturedLambda = new Capture<>();
//		pathPlanning.paintPath(EasyMock.eq(Point.of(35, 70)), EasyMock.eq(Point.of(70, 70)),
//				EasyMock.and((WhatToDoWithPointInPath) EasyMock.capture(capturedLambda),
//						EasyMock.isA(WhatToDoWithPointInPath.class)));
//		EasyMock.replay(unit, pathPlanning);
//		WalkAnimator wa = new WalkAnimator(pathPlanning, path, unit);
//		capturedLambda.getValue().pathPoint(Point.of(45,70));
//		capturedLambda.getValue().pathPoint(Point.of(55,70));
//		capturedLambda.getValue().pathPoint(Point.of(65,70));
//
//		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
//		/**
//		 * Multiple call should not change result.
//		 */
//		assertEquals(Point.of(36, 70), wa.getNextCoordinates());
//		assertEquals(Point.of(36, 70), wa.getNextCoordinates());
//		wa.countNextAnimationLocation();
//
//		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
//		assertEquals(Point.of(37, 70), wa.getNextCoordinates());
//		assertEquals(Point.of(37, 70), wa.getNextCoordinates());
//		wa.countNextAnimationLocation();
//
//		assertEquals("Verify that list size is not changed", 2, path.size());
//		assertEquals(Location.of(2, 2), wa.getTo());
//		EasyMock.verify(unit, pathPlanning);
	}

	@Test
	public void test_multi_tiles_steps() throws Exception {
		assertNotNull(pathPlanning);
		assertNotNull(unit);
//		List<Location> path = Arrays.asList(Location.of(1, 2), Location.of(2, 2), Location.of(3, 4));
//		EasyMock.replay(unit, pathPlanning);
//		WalkAnimator wa = new WalkAnimator(pathPlanning, path, unit);
//
//		wa.countNextAnimationLocation();
//		assertTrue("first animation step should be available", wa.isNextAnimationLocationAvailable());
//		while (wa.isNextAnimationLocationAvailable()) {
//			wa.countNextAnimationLocation();
//		}
//
//		assertFalse("Animation step should not be available", wa.isNextAnimationLocationAvailable());
//
//		/**
//		 * Even after animation counting response should be consisstent.
//		 */
//		wa.countNextAnimationLocation();
//		assertFalse("Animation step should not be available", wa.isNextAnimationLocationAvailable());
//		EasyMock.verify(unit, pathPlanning);
	}

	@Before
	public void setup() {
		pathPlanning = EasyMock.createMock(PathPlanning.class);
		unit = EasyMock.createMock(Ship.class);
	}

	@After
	public void tearDown() {
		pathPlanning = null;
		unit = null;
	}

}
