package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.Point;
import org.microcol.model.Location;

public class AreaBasicTest {

    private final VisibleAreaService visibleArea = mock(VisibleAreaService.class);

    private Area area;

    @Test
    public void test_convertToLocation() throws Exception {
        assertEquals(Location.of(2, 2), area.convertToLocation(Point.of(23, 23)));
        assertEquals(Location.of(8, 8), area.convertToLocation(Point.of(300, 300)));
    }

    @BeforeEach
    private void beforeEach() {
        when(visibleArea.getTopLeft()).thenReturn(Point.of(30, 50));
        when(visibleArea.getBottomRight()).thenReturn(Point.of(130, 150));

        area = new Area(visibleArea);
    }

    @AfterEach
    private void afterEach() {
        area = null;
    }

}
