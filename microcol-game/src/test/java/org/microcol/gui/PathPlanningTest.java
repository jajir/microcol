package org.microcol.gui;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class PathPlanningTest {

	private final Logger logger = LoggerFactory.getLogger(PathPlanningTest.class);

	private PathPlanning pathPlanning;

	private @Mocked GamePreferences gamePreferences;
	
	private @Mocked PathPlanning.WhatToDoWithPointInPath whatToDo;

	@Test
	public void test_simple() throws Exception {
		new Expectations() {{
			whatToDo.pathPoint(Point.of(3, 5));
			whatToDo.pathPoint(Point.of(4, 5));
			whatToDo.pathPoint(Point.of(5, 5));
			whatToDo.pathPoint(Point.of(5, 6));
			whatToDo.pathPoint(Point.of(6, 6));
			gamePreferences.getAnimationSpeed(); result = 4;
		}};

		pathPlanning.paintPath(Point.of(2, 4), Point.of(7, 6), whatToDo);
	}

	@Test
	public void test_diagonal_move() throws Exception {
		new Expectations() {{
			whatToDo.pathPoint((Point)any); times = 6;
			gamePreferences.getAnimationSpeed(); result = 4;
		}};
		pathPlanning.paintPath(Point.of(2, 2), Point.of(6, 6), whatToDo);
	}

	@Test
	public void test_horizontal_move() throws Exception {
		new Expectations() {{
			whatToDo.pathPoint(Point.of(10, 2));
			whatToDo.pathPoint(Point.of(9, 2));
			whatToDo.pathPoint(Point.of(8, 2));
			whatToDo.pathPoint(Point.of(8, 2));
			whatToDo.pathPoint(Point.of(7, 2));
			whatToDo.pathPoint(Point.of(6, 2));
			gamePreferences.getAnimationSpeed(); result = 4;
		}};
		pathPlanning.paintPath(Point.of(11, 2), Point.of(6, 2), whatToDo);
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
		new Expectations() {{
			gamePreferences.getAnimationSpeed(); result = 9;
		}};
		pathPlanning.paintPath(Point.of(5, 5), Point.of(5, 5), whatToDo);
	}

	@Test
	public void test_first_step_is_skipped() throws Exception {
		new Expectations() {{
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
			gamePreferences.getAnimationSpeed(); result = 3;
		}};
		pathPlanning.paintPath(Point.of(5, 5), Point.of(11, 0), whatToDo);
	}

	@Test
	public void test_move_to_lover_values() throws Exception {
		new Expectations() {{
			whatToDo.pathPoint(Point.of(6, 6));
			whatToDo.pathPoint(Point.of(5, 5));
			whatToDo.pathPoint(Point.of(5, 5));
			whatToDo.pathPoint(Point.of(5, 4));
			whatToDo.pathPoint(Point.of(5, 4));
			whatToDo.pathPoint(Point.of(4, 3));
			gamePreferences.getAnimationSpeed(); result = 4;
		}};
		pathPlanning.paintPath(Point.of(6, 7), Point.of(4, 3), whatToDo);
	}

	@Before
	public void setUp() {
		pathPlanning = new PathPlanning(gamePreferences);
	}

	@After
	public void tearDown() {
		pathPlanning = null;
	}

}
