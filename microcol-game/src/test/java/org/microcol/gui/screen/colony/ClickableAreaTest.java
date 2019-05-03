package org.microcol.gui.screen.colony;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.gui.Point;
import org.microcol.model.Direction;
import org.microcol.model.Location;

public class ClickableAreaTest {

    private final ClickableArea clickableArea = new ClickableArea();

    static Stream<Arguments> dataProvider_for_isLocationVisible() {
        return Stream.of(
                /*
                 * Empty
                 */
                arguments(Point.of(-1, -1), Optional.empty()),
                arguments(Point.of(50, 50), Optional.empty()),
                arguments(Point.of(136, 136), Optional.empty()),
                /*
                 * Visible locations
                 */
                arguments(Point.of(15, 15), Optional.of(Direction.northWest)),
                arguments(Point.of(60, 15), Optional.of(Direction.north)),
                arguments(Point.of(105, 15), Optional.of(Direction.northEast)),
                arguments(Point.of(15, 60), Optional.of(Direction.west)),
                arguments(Point.of(60, 60), Optional.empty()),
                arguments(Point.of(105, 60), Optional.of(Direction.east)),
                arguments(Point.of(15, 105), Optional.of(Direction.southWest)),
                arguments(Point.of(105, 105), Optional.of(Direction.southEast)),
                arguments(Point.of(60, 105), Optional.of(Direction.south)));
    }

    @ParameterizedTest(name = "{index}: location = {0}, isVisible= {1}")
    @MethodSource("dataProvider_for_isLocationVisible")
    public void test_isLocationVisible(final Point point, final Optional<Location> oLocation)
            throws Exception {
        Optional<Direction> oRet = clickableArea.getDirection(point);

        assertEquals(oLocation, oRet);
    }

}
