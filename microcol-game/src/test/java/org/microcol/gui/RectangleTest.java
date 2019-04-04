package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Preconditions;

public class RectangleTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                arguments(Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 0, 0, Boolean.FALSE),
                arguments(Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 100, 100,
                        Boolean.FALSE),
                arguments(Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 10, 10, Boolean.TRUE),
                arguments(Rectangle.of(Point.of(10, 10), Point.of(30, 30)), 20, 20, Boolean.TRUE));
    }

    @ParameterizedTest(name = "{index}: Reactangle={0}, Point (x = {1}, y = {2}) should be inside {3} rectangle")
    @MethodSource("dataProvider")
    public void testName(final Rectangle rectangle, final int x, final int y,
            final boolean shouldBeIn) throws Exception {
        Preconditions.checkNotNull(rectangle);
        Preconditions.checkNotNull(shouldBeIn);
        assertEquals(shouldBeIn, rectangle.isIn(Point.of(x, y)));
    }

}
