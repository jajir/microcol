package org.microcol.gui.screen.game.gamepanel;

import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.model.Location;

/**
 * Tests for {@link Area}.
 */
public class AreaTest {

    private final VisibleAreaService visibleArea = mock(VisibleAreaService.class);

    @Test
    public void test_isLocationVisible() throws Exception {
        final Area area = makeArea(222, 222, 800, 600);
        final Location loc = Location.of(12, 4);
        final Tile tile = Tile.ofLocation(loc);
        when(visibleArea.isVisibleMapPoint(tile.getTopLeftCorner())).thenReturn(true);
        when(visibleArea.isVisibleMapPoint(tile.getBottomRightCorner())).thenReturn(true);
        
        assertTrue(area.isLocationVisible(loc));
    }

    @Test
    public void test_getCenterToLocation_middle_of_map() throws Exception {
        final Area area = makeArea(222, 222, 800, 600);

        when(visibleArea.getCanvasSize()).thenReturn(Point.of(800, 600));
        when(visibleArea.computeNewTopLeftConnerOfCanvas(Point.of(1805, 1905)))
                .thenReturn(Point.of(1351, 1451));

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
        final Area area = makeArea(222, 222, 800, 600);

        final Point ret = area.convertToCanvasPoint(loc);

        assertEquals(expectedPoint, ret);
    }

    private Area makeArea(final int viewTopLeftCornerX, final int viewTopLeftCornerY,
            final int viewWidth, final int viewHeight) {
        final Point topLeft = Point.of(viewTopLeftCornerX, viewTopLeftCornerY);
        final Point bottomRight = topLeft.add(viewWidth, viewHeight);

        when(visibleArea.getTopLeft()).thenReturn(topLeft);
        when(visibleArea.getBottomRight()).thenReturn(bottomRight);

        final Area out = new Area(visibleArea);
        assertNotNull(out);
        return out;
    }

}
