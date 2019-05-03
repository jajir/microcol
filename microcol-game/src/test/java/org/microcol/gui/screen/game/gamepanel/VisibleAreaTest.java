package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.Location;
import org.microcol.model.WorldMap;

public class VisibleAreaTest {

    private final WorldMap worldMap = mock(WorldMap.class);

    @Test
    void test_setMaxMapSize() throws Exception {
        final VisibleArea visibleArea = new VisibleArea();
        when(worldMap.getMaxLocation()).thenReturn(Location.of(79, 77));
        visibleArea.setWorldMap(worldMap);

        assertNotNull(visibleArea.getMaxMapSize());
        assertEquals(3555, visibleArea.getMaxMapSize().getX());
        assertEquals(3465, visibleArea.getMaxMapSize().getY());
    }

}
