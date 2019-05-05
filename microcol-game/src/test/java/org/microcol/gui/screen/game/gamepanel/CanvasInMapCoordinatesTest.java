package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.microcol.gui.Point;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

public class CanvasInMapCoordinatesTest {

    private final VisibleAreaService visibleArea = mock(VisibleAreaService.class);
    private final WorldMap worldMap = mock(WorldMap.class);
    private CanvasInMapCoordinates mapping;

    @Test
    void test_make_missing_worldMap() throws Exception {
        assertThrows(NullPointerException.class,
                () -> CanvasInMapCoordinates.make(visibleArea, null));
    }

    @Test
    void test_make_missing_visibleAreaService() throws Exception {
        assertThrows(NullPointerException.class,
                () -> CanvasInMapCoordinates.make(null, worldMap));
    }

    @Test
    void test_make_60_100() throws Exception {
        init(Point.of(60, 150), Point.of(220, 270), Location.of(80, 40));
        mapping = CanvasInMapCoordinates.make(visibleArea, worldMap);

        assertEquals(Location.of(2, 4), mapping.getTopLeft());
        assertEquals(Location.of(5, 7), mapping.getBottomRight());
    }

    @Test
    void test_make_0_0() throws Exception {
        init(Point.of(0, 0), Point.of(160, 120), Location.of(80, 40));
        mapping = CanvasInMapCoordinates.make(visibleArea, worldMap);

        assertEquals(Location.of(1, 1), mapping.getTopLeft());
        assertEquals(Location.of(4, 3), mapping.getBottomRight());
    }

    @Test
    void test_make__10__10() throws Exception {
        init(Point.of(-10, -10), Point.of(160, 120), Location.of(80, 40));
        mapping = CanvasInMapCoordinates.make(visibleArea, worldMap);

        assertEquals(Location.of(1, 1), mapping.getTopLeft());
        assertEquals(Location.of(4, 3), mapping.getBottomRight());
    }

    @Test
    void test_make_1000_1000() throws Exception {
        init(Point.of(1000, 1000), Point.of(3600, 1800), Location.of(80, 40));
        mapping = CanvasInMapCoordinates.make(visibleArea, worldMap);

        assertEquals(Location.of(23, 23), mapping.getTopLeft());
        assertEquals(Location.of(80, 40), mapping.getBottomRight());
    }

    @Test
    void test_make_1000_1000_max() throws Exception {
        init(Point.of(1000, 1000), Point.of(3800, 2000), Location.of(80, 40));
        mapping = CanvasInMapCoordinates.make(visibleArea, worldMap);

        assertEquals(Location.of(23, 23), mapping.getTopLeft());
        assertEquals(Location.of(80, 40), mapping.getBottomRight());
    }

    private void init(final Point topLeft, final Point bottomRight, final Location maxMapsSize) {
        when(visibleArea.getTopLeft()).thenReturn(topLeft);
        when(visibleArea.getBottomRight()).thenReturn(bottomRight);
        when(worldMap.getMaxLocationX()).thenReturn(maxMapsSize.getX());
        when(worldMap.getMaxLocationY()).thenReturn(maxMapsSize.getY());
    }

}
