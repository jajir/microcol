package org.microcol.gui.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.gui.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathPlanningTest {

    private final Logger logger = LoggerFactory.getLogger(PathPlanningTest.class);

    private final PathPlanning pathPlanning = new PathPlanning();

    @Test
    public void test_getPathLimitSteps() throws Exception {
        final List<Point> ret = pathPlanning.getPathLimitSteps(Point.of(2, 4), Point.of(7, 6), 5);

        assertEquals(5, ret.size());
        assertEquals(Point.of(3, 4), ret.get(0));
        assertEquals(Point.of(4, 5), ret.get(1));
        assertEquals(Point.of(5, 5), ret.get(2));
        assertEquals(Point.of(6, 6), ret.get(3));
        assertEquals(Point.of(7, 6), ret.get(4));
    }

    @Test
    public void test_getPathLimitSpeed() throws Exception {

        final List<Point> ret = pathPlanning.getPathLimitSpeed(Point.of(2, 4), Point.of(7, 6), 4);

        assertEquals(5, ret.size());
        assertEquals(Point.of(3, 5), ret.get(0));
        assertEquals(Point.of(4, 5), ret.get(1));
        assertEquals(Point.of(5, 5), ret.get(2));
        assertEquals(Point.of(5, 6), ret.get(3));
        assertEquals(Point.of(6, 6), ret.get(4));
    }

    @Test
    public void test_getPathLimitSpeed_diagonal_move() throws Exception {

        final List<Point> ret = pathPlanning.getPathLimitSpeed(Point.of(2, 2), Point.of(6, 6), 4);

        assertEquals(6, ret.size());
        assertEquals(Point.of(3, 2), ret.get(0));
        assertEquals(Point.of(3, 3), ret.get(1));
        assertEquals(Point.of(4, 3), ret.get(2));
        assertEquals(Point.of(4, 4), ret.get(3));
        assertEquals(Point.of(5, 5), ret.get(4));
        assertEquals(Point.of(6, 5), ret.get(5));
    }

    @Test
    public void test_getPathLimitSpeed_horizontal_move() throws Exception {

        final List<Point> ret = pathPlanning.getPathLimitSpeed(Point.of(11, 2), Point.of(6, 2), 4);

        assertEquals(6, ret.size());
        assertEquals(Point.of(10, 2), ret.get(0));
        assertEquals(Point.of(9, 2), ret.get(1));
        assertEquals(Point.of(8, 2), ret.get(2));
        assertEquals(Point.of(8, 2), ret.get(3));
        assertEquals(Point.of(7, 2), ret.get(4));
        assertEquals(Point.of(6, 2), ret.get(5));
    }

    @Test
    public void test_getPathLimitSpeed_from_and_destiny_points_are_same() throws Exception {

        final List<Point> ret = pathPlanning.getPathLimitSpeed(Point.of(5, 5), Point.of(5, 5), 9);

        assertEquals(0, ret.size());
    }

    @Test
    public void test_getPathLimitSpeed_first_step_is_skipped() throws Exception {

        final List<Point> ret = pathPlanning.getPathLimitSpeed(Point.of(5, 5), Point.of(11, 0), 3);

        assertEquals(11, ret.size());
        assertEquals(Point.of(5, 4), ret.get(0));
        assertEquals(Point.of(6, 4), ret.get(1));
        assertEquals(Point.of(6, 3), ret.get(2));
        assertEquals(Point.of(7, 3), ret.get(3));
        assertEquals(Point.of(7, 3), ret.get(4));
        assertEquals(Point.of(8, 2), ret.get(5));
        assertEquals(Point.of(8, 2), ret.get(6));
        assertEquals(Point.of(9, 1), ret.get(7));
        assertEquals(Point.of(9, 1), ret.get(8));
        assertEquals(Point.of(10, 1), ret.get(9));
        assertEquals(Point.of(10, 0), ret.get(10));
    }

    @Test
    public void test_getPathLimitSpeed_move_to_lover_values() throws Exception {

        final List<Point> ret = pathPlanning.getPathLimitSpeed(Point.of(6, 7), Point.of(4, 3), 4);

        assertEquals(6, ret.size());
        assertEquals(Point.of(6, 6), ret.get(0));
        assertEquals(Point.of(5, 5), ret.get(1));
        assertEquals(Point.of(5, 5), ret.get(2));
        assertEquals(Point.of(5, 4), ret.get(3));
        assertEquals(Point.of(4, 3), ret.get(4));
        assertEquals(Point.of(4, 3), ret.get(5));
    }

    @Test
    public void test_countStepSize_positive_value_verify_step_size_decreasing() throws Exception {
        float previousValue = 0F;
        final int diff = 20;
        for (int speed = PathPlanningService.ANIMATION_SPEED_MIN_VALUE; speed < PathPlanningService.ANIMATION_SPEED_MAX_VALUE; speed++) {
            final float speedSize = pathPlanning.countStepSize(40, 60, speed);
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
        for (int speed = PathPlanningService.ANIMATION_SPEED_MIN_VALUE; speed < PathPlanningService.ANIMATION_SPEED_MAX_VALUE; speed++) {
            final float speedSize = pathPlanning.countStepSize(40, 60, speed);
            logger.debug("'" + diff + "' steps in speed '" + speed + "' make step incrememnet '"
                    + speedSize + "'");
            assertTrue(previousValue < speedSize);
            previousValue = speedSize;
        }
    }

}
