package org.microcol.gui;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.preferences.GamePreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathPlanningTest {

    private final Logger logger = LoggerFactory.getLogger(PathPlanningTest.class);

    private PathPlanning pathPlanning;

    private final GamePreferences gamePreferences = mock(GamePreferences.class);

    private final PathPlanning.WhatToDoWithPointInPath whatToDo = mock(
            PathPlanning.WhatToDoWithPointInPath.class);

    @Test
    public void test_simple() throws Exception {
        doNothing().when(whatToDo).pathPoint(Point.of(3, 5));
        doNothing().when(whatToDo).pathPoint(Point.of(4, 5));
        doNothing().when(whatToDo).pathPoint(Point.of(5, 5));
        doNothing().when(whatToDo).pathPoint(Point.of(5, 6));
        doNothing().when(whatToDo).pathPoint(Point.of(6, 6));
        when(gamePreferences.getAnimationSpeed()).thenReturn(4);

        pathPlanning.paintPath(Point.of(2, 4), Point.of(7, 6), whatToDo);
    }

    @Test
    public void test_diagonal_move() throws Exception {
        doNothing().when(whatToDo).pathPoint(any());
        when(gamePreferences.getAnimationSpeed()).thenReturn(4);

        pathPlanning.paintPath(Point.of(2, 2), Point.of(6, 6), whatToDo);
    }

    @Test
    public void test_horizontal_move() throws Exception {
        doNothing().when(whatToDo).pathPoint(Point.of(10, 2));
        doNothing().when(whatToDo).pathPoint(Point.of(9, 2));
        doNothing().when(whatToDo).pathPoint(Point.of(8, 2));
        doNothing().when(whatToDo).pathPoint(Point.of(8, 2));
        doNothing().when(whatToDo).pathPoint(Point.of(7, 2));
        doNothing().when(whatToDo).pathPoint(Point.of(6, 2));
        when(gamePreferences.getAnimationSpeed()).thenReturn(4);

        pathPlanning.paintPath(Point.of(11, 2), Point.of(6, 2), whatToDo);
    }

    @Test
    public void test_countStepSize_positive_value_verify_step_size_decreasing() throws Exception {
        float previousValue = 0F;
        final int diff = 20;
        for (int speed = PathPlanning.ANIMATION_SPEED_MIN_VALUE; speed < PathPlanning.ANIMATION_SPEED_MAX_VALUE; speed++) {
            final float speedSize = pathPlanning.countStepSize(20, speed);
            logger.debug("'" + diff + "' steps in speed '" + speed + "' make step incrememnet '"
                    + speedSize + "'");
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
            logger.debug("'" + diff + "' steps in speed '" + speed + "' make step incrememnet '"
                    + speedSize + "'");
            assertTrue(previousValue < speedSize);
            previousValue = speedSize;
        }
    }

    @Test
    public void test_from_and_destiny_points_are_same() throws Exception {
        when(gamePreferences.getAnimationSpeed()).thenReturn(9);

        pathPlanning.paintPath(Point.of(5, 5), Point.of(5, 5), whatToDo);
    }

    @Test
    public void test_first_step_is_skipped() throws Exception {
        doNothing().when(whatToDo).pathPoint(Point.of(5, 4));
        doNothing().when(whatToDo).pathPoint(Point.of(6, 4));
        doNothing().when(whatToDo).pathPoint(Point.of(6, 3));
        doNothing().when(whatToDo).pathPoint(Point.of(7, 3));
        doNothing().when(whatToDo).pathPoint(Point.of(7, 3));
        doNothing().when(whatToDo).pathPoint(Point.of(8, 2));
        doNothing().when(whatToDo).pathPoint(Point.of(8, 2));
        doNothing().when(whatToDo).pathPoint(Point.of(9, 1));
        doNothing().when(whatToDo).pathPoint(Point.of(9, 1));
        doNothing().when(whatToDo).pathPoint(Point.of(10, 1));
        doNothing().when(whatToDo).pathPoint(Point.of(10, 0));
        when(gamePreferences.getAnimationSpeed()).thenReturn(3);

        pathPlanning.paintPath(Point.of(5, 5), Point.of(11, 0), whatToDo);
    }

    @Test
    public void test_move_to_lover_values() throws Exception {
        doNothing().when(whatToDo).pathPoint(Point.of(6, 6));
        doNothing().when(whatToDo).pathPoint(Point.of(5, 5));
        doNothing().when(whatToDo).pathPoint(Point.of(5, 5));
        doNothing().when(whatToDo).pathPoint(Point.of(5, 4));
        doNothing().when(whatToDo).pathPoint(Point.of(5, 4));
        doNothing().when(whatToDo).pathPoint(Point.of(4, 3));
        when(gamePreferences.getAnimationSpeed()).thenReturn(4);
        pathPlanning.paintPath(Point.of(6, 7), Point.of(4, 3), whatToDo);
    }

    @BeforeEach
    public void setUp() {
        pathPlanning = new PathPlanning(gamePreferences);
    }

    @AfterEach
    public void tearDown() {
        pathPlanning = null;
    }

}
