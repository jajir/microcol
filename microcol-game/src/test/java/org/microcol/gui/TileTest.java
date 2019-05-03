package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.Location;

public class TileTest {

    @Test
    public void test_ofLocation_0_0() throws Exception {
        final Tile t = Tile.ofLocation(Location.of(1, 1));

        assertEquals(0, t.getTopLeftCorner().getX());
        assertEquals(0, t.getTopLeftCorner().getY());
    }

    @Test
    public void test_ofLocation_3_7() throws Exception {
        final Tile t = Tile.ofLocation(Location.of(3, 7));

        assertEquals(90, t.getTopLeftCorner().getX());
        assertEquals(270, t.getTopLeftCorner().getY());
    }

    static Stream<Arguments> dataProvider() {
        return Stream.of(arguments(Point.of(12, 12), Location.of(1, 1)),
                arguments(Point.of(TILE_WIDTH_IN_PX / 2, TILE_WIDTH_IN_PX * 3 / 2),
                        Location.of(1, 2)),
                arguments(Point.of(0, 0), Location.of(1, 1)),
                arguments(Point.of(1000, -6), Location.of(23, 0)),
                arguments(Point.of(-55, -55), Location.of(-1, -1)));
    }

    @ParameterizedTest(name = "{index}: point = {0} will be converted to location = {1}")
    @MethodSource("dataProvider")
    public void test_toLocation(final Point point, final Location expectedLocation)
            throws Exception {
        final Tile tile = Tile.of(point);

        assertEquals(expectedLocation, tile.toLocation());
    }

}
