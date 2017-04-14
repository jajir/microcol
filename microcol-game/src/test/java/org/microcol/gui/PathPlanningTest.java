package org.microcol.gui;

import static org.junit.Assert.assertTrue;

import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathPlanningTest {

	private final Logger logger = LoggerFactory.getLogger(PathPlanningTest.class);

	private PathPlanning pathPlanning;

	private GamePreferences gamePreferences;

	@Test
	public void test_simple() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Point.of(3, 5));
		whatToDo.pathPoint(Point.of(4, 5));
		whatToDo.pathPoint(Point.of(5, 5));
		whatToDo.pathPoint(Point.of(5, 6));
		whatToDo.pathPoint(Point.of(6, 6));
		EasyMock.expect(gamePreferences.getAnimationSpeed()).andReturn(4);
		EasyMock.replay(whatToDo, gamePreferences);
		pathPlanning.paintPath(Point.of(2, 4), Point.of(7, 6), whatToDo);

		EasyMock.verify(whatToDo, gamePreferences);
	}

	@Test
	public void test_diagonal_move() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(EasyMock.anyObject());
		EasyMock.expectLastCall().anyTimes();
		EasyMock.expect(gamePreferences.getAnimationSpeed()).andReturn(4);
		EasyMock.replay(whatToDo, gamePreferences);
		pathPlanning.paintPath(Point.of(2, 2), Point.of(6, 6), whatToDo);

		EasyMock.verify(whatToDo, gamePreferences);
	}

	@Test
	public void test_horizontal_move() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Point.of(10, 2));
		whatToDo.pathPoint(Point.of(9, 2));
		whatToDo.pathPoint(Point.of(8, 2));
		whatToDo.pathPoint(Point.of(8, 2));
		whatToDo.pathPoint(Point.of(7, 2));
		whatToDo.pathPoint(Point.of(6, 2));
		EasyMock.expect(gamePreferences.getAnimationSpeed()).andReturn(4);
		EasyMock.replay(whatToDo, gamePreferences);
		pathPlanning.paintPath(Point.of(11, 2), Point.of(6, 2), whatToDo);

		EasyMock.verify(whatToDo, gamePreferences);
	}

	@Test
	public void test_countStepSize_positive_value_verify_step_size_decreasing() throws Exception {
		float previousValue = 0F;
		final int diff = 20;
		for (int speed = PathPlanning.ANIMATION_SPEED_MIN_VALUE; speed < PathPlanning.ANIMATION_SPEED_MAX_VALUE; speed++) {
			final float speedSize = pathPlanning.countStepSize(20, speed);
			logger.debug("'" + diff + "' steps in speed '" + speed + "' make step incrememnet '" + speedSize + "'");
			assertTrue(previousValue < speedSize);
			previousValue = speedSize;
		}
	}

	@Test
	public void test_countStepSize_negative_value_verify_step_size_increasing() throws Exception {
		float previousValue = 0F;
		final int diff = 20;
		for (int speed = PathPlanning.ANIMATION_SPEED_MIN_VALUE; speed < PathPlanning.ANIMATION_SPEED_MAX_VALUE; speed++) {
			final float speedSize = pathPlanning.countStepSize(20, speed);
			logger.debug("'" + diff + "' steps in speed '" + speed + "' make step incrememnet '" + speedSize + "'");
			assertTrue(previousValue < speedSize);
			previousValue = speedSize;
		}
	}

	@Test
	public void test_from_and_destiny_points_are_same() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		EasyMock.expect(gamePreferences.getAnimationSpeed()).andReturn(9);
		EasyMock.replay(whatToDo, gamePreferences);
		pathPlanning.paintPath(Point.of(5, 5), Point.of(5, 5), whatToDo);
		EasyMock.verify(whatToDo, gamePreferences);
	}

	@Test
	public void test_first_step_is_skipped() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Point.of(5, 4));
		whatToDo.pathPoint(Point.of(6, 4));
		whatToDo.pathPoint(Point.of(6, 3));
		whatToDo.pathPoint(Point.of(7, 3));
		whatToDo.pathPoint(Point.of(7, 3));
		whatToDo.pathPoint(Point.of(8, 2));
		whatToDo.pathPoint(Point.of(8, 2));
		whatToDo.pathPoint(Point.of(9, 1));
		whatToDo.pathPoint(Point.of(9, 1));
		whatToDo.pathPoint(Point.of(10, 1));
		whatToDo.pathPoint(Point.of(10, 0));
		EasyMock.expect(gamePreferences.getAnimationSpeed()).andReturn(3);
		EasyMock.replay(whatToDo, gamePreferences);
		pathPlanning.paintPath(Point.of(5, 5), Point.of(11, 0), whatToDo);

		EasyMock.verify(whatToDo, gamePreferences);
	}

	@Test
	public void test_move_to_lover_values() throws Exception {
		PathPlanning.WhatToDoWithPointInPath whatToDo = EasyMock.createMock(PathPlanning.WhatToDoWithPointInPath.class);
		whatToDo.pathPoint(Point.of(6, 6));
		whatToDo.pathPoint(Point.of(5, 5));
		whatToDo.pathPoint(Point.of(5, 5));
		whatToDo.pathPoint(Point.of(5, 4));
		whatToDo.pathPoint(Point.of(5, 4));
		whatToDo.pathPoint(Point.of(4, 3));
		EasyMock.expect(gamePreferences.getAnimationSpeed()).andReturn(4);
		EasyMock.replay(whatToDo, gamePreferences);
		pathPlanning.paintPath(Point.of(6, 7), Point.of(4, 3), whatToDo);

		EasyMock.verify(whatToDo, gamePreferences);
	}

	@Before
	public void setUp() {
		gamePreferences = EasyMock.createMock(GamePreferences.class);
		pathPlanning = new PathPlanning(gamePreferences);
	}

	@After
	public void tearDown() {
		pathPlanning = null;
	}

}
