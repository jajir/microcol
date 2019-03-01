package org.microcol.gui;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.GamePanelView;
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

    @Test
    public void test_getCenterAreaTo_middle_of_map() throws Exception {
        final Area area = makeArea(222, 222, 800, 600, 100 * GamePanelView.TILE_WIDTH_IN_PX,
                100 * GamePanelView.TILE_WIDTH_IN_PX);

        when(visibleArea.getCanvasWidth()).thenReturn(800);
        when(visibleArea.getCanvasHeight()).thenReturn(600);
        when(visibleArea.scrollToPoint(Point.of(1850, 1950))).thenReturn(Point.of(1351, 1451));

        Point po = area.getCenterToLocation(Location.of(50, 50));

        assertEquals(1351, po.getX());
        assertEquals(1451, po.getY());
    }

    private Area makeArea(final int viewTopLeftCornerX, final int viewTopLeftCornerY,
            final int viewWidth, final int viewHeight, final int maxMapLocationX,
            final int maxMapLocationY) {
        final Point topLeft = Point.of(viewTopLeftCornerX, viewTopLeftCornerY);
        final Point bottomRight = topLeft.add(viewWidth, viewHeight);

        when(visibleArea.getTopLeft()).thenReturn(topLeft);
        when(visibleArea.getBottomRight()).thenReturn(bottomRight);
        when(map.getMaxX()).thenReturn(maxMapLocationX);
        when(map.getMaxY()).thenReturn(maxMapLocationY);

        final Area out = new Area(visibleArea, map);
        assertNotNull(out);
        return out;
    }

}
