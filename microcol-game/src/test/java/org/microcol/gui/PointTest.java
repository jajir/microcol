package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PointTest {

    @Test
    public void test_simple() throws Exception {

        for (int i = 40; i > -40; i--) {
            final Point p = Point.of(10, i);

            assertEquals(10, p.getX());
            assertEquals(i, p.getY());
        }
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
