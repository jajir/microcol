package org.microcol.gui;

import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.VisibleArea;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

/**
 * Tests for {@link Area}.
 */
public class AreaTest {

    private final VisibleArea visibleArea = mock(VisibleArea.class);

    private final WorldMap map = mock(WorldMap.class);

    @Test
    public void test_small_map_huge_view() {
        Area area = makeArea(0, 0, 320, 237, 3, 3);

        assertEquals(1, area.getTopLeft().getX());
        assertEquals(1, area.getTopLeft().getY());

        assertEquals(3, area.getBottomRight().getX());
        assertEquals(3, area.getBottomRight().getY());
    }

    @Test
    public void test_constructor() {
        Area area = makeArea(222, 222, 800, 600, 500, 500);

        assertEquals(5, area.getTopLeft().getX());
        assertEquals(5, area.getTopLeft().getY());

        assertEquals(24, area.getBottomRight().getX());
        assertEquals(20, area.getBottomRight().getY());
    }

    static Stream<Arguments> dataProvider_for_isLocationVisible() {
        return Stream.of(
                /*
                 * Invisible locations.
                 */
                arguments(Location.of(1, 1), false), arguments(Location.of(4, 4), false),
                arguments(Location.of(24, 20), false), arguments(Location.of(4, 5), false),
                arguments(Location.of(5, 4), false),
                /*
                 * Visible locations
                 */
                arguments(Location.of(5, 5), true), arguments(Location.of(23, 19), true),
                arguments(Location.of(23, 6), true), arguments(Location.of(6, 19), true));
    }

    @ParameterizedTest(name = "{index}: location = {0}, isVisible= {1}")
    @MethodSource("dataProvider_for_isLocationVisible")
    public void test_isLocationVisible(final Location loc, final boolean isVisible)
            throws Exception {
        final Area area = makeArea(222, 222, 800, 600, 90, 90);

        if (isVisible) {
            assertTrue(area.isLocationVisible(loc));
        } else {
            assertFalse(area.isLocationVisible(loc));
        }
    }

    static Stream<Arguments> dataProvider_for_isVisibleCanvasPoint() {
        return Stream.of(
                /*
                 * Invisible locations.
                 */
                arguments(Point.of(1, 1), false), arguments(Point.of(221, 222), false),
                arguments(Point.of(222, 221), false), arguments(Point.of(1023, 822), false),
                arguments(Point.of(1022, 823), false),
                /*
                 * Visible locations
                 */
                arguments(Point.of(222, 222), true), arguments(Point.of(1022, 822), true),
                arguments(Point.of(300, 300), true));
    }

    @ParameterizedTest(name = "{index}: location = {0}, isVisible= {1}")
    @MethodSource("dataProvider_for_isVisibleCanvasPoint")
    public void test_isVisibleCanvasPoint(final Point point, final boolean isVisible)
            throws Exception {
        final Area area = makeArea(222, 222, 800, 600, 90, 90);
        /*
         * Top left point [222,222]
         * 
         * Bottom right [1022,822]
         */

        if (isVisible) {
            assertTrue(area.isVisibleCanvasPoint(point));
        } else {
            assertFalse(area.isVisibleCanvasPoint(point));
        }
    }

    @Test
    public void test_getCenterToLocation_middle_of_map() throws Exception {
        final Area area = makeArea(222, 222, 800, 600, 100 * TILE_WIDTH_IN_PX,
                100 * TILE_WIDTH_IN_PX);

        when(visibleArea.getCanvasWidth()).thenReturn(800);
        when(visibleArea.getCanvasHeight()).thenReturn(600);
        when(visibleArea.scrollToPoint(Point.of(1805, 1905))).thenReturn(Point.of(1351, 1451));

        Point po = area.getCenterToLocation(Location.of(50, 50));
        
        assertNotNull(po);
        assertEquals(1351, po.getX());
        assertEquals(1451, po.getY());
    }

    static Stream<Arguments> dataProvider_for_convertToPoint() {
        return Stream.of(arguments(Location.of(10, 10), Point.of(183, 183)),
                arguments(Location.of(23, 19), Point.of(768, 588)),
                arguments(Location.of(-1, -1), Point.of(-312, -312)));
    }

    @ParameterizedTest(name = "{index}: locatin = {0} is converted to point = {1}")
    @MethodSource("dataProvider_for_convertToPoint")
    public void test_convertToPoint(final Location loc, final Point expectedPoint)
            throws Exception {
        final Area area = makeArea(222, 222, 800, 600, 90, 90);

        final Point ret = area.convertToPoint(loc);

        assertEquals(expectedPoint, ret);
    }

    private Area makeArea(final int viewTopLeftCornerX, final int viewTopLeftCornerY,
            final int viewWidth, final int viewHeight, final int maxMapLocationX,
            final int maxMapLocationY) {
        final Point topLeft = Point.of(viewTopLeftCornerX, viewTopLeftCornerY);
        final Point bottomRight = topLeft.add(viewWidth, viewHeight);

        when(visibleArea.getTopLeft()).thenReturn(topLeft);
        when(visibleArea.getBottomRight()).thenReturn(bottomRight);
        when(map.getMaxLocationX()).thenReturn(maxMapLocationX);
        when(map.getMaxLocationY()).thenReturn(maxMapLocationY);

        final Area out = new Area(visibleArea, map);
        assertNotNull(out);
        return out;
    }

}
