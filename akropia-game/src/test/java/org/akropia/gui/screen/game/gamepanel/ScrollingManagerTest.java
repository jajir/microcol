package org.akropia.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.akropia.gui.Point;
import org.akropia.gui.WasdController;
import org.akropia.gui.util.PathPlanning;
import org.akropia.model.Location;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.common.eventbus.EventBus;

class ScrollingManagerTest {
    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void manualAndAutomaticStepsUpdateCoordinatesAndTileBounds(
            final boolean automatic) {
        final VisibleAreaService view = new VisibleAreaService(true);
        final Location mapSize = Location.of(60, 40);
        view.setMapSize(mapSize);
        view.setCanvasWidth(450);
        view.setCanvasHeight(450);
        view.setTopLeftPosionOfCanvas(Point.of(44, 90));
        final WasdController keys = mock(WasdController.class);
        when(keys.isScrolling()).thenReturn(!automatic);
        when(keys.getDiff()).thenReturn(Point.of(18, 0));
        final ScrollingManager scroll = new ScrollingManager(new EventBus(),
                keys, view);
        if (automatic) {
            final PathPlanning path = mock(PathPlanning.class);
            when(path.getPathLimitSteps(Point.of(44, 90), Point.of(62, 90), 10))
                    .thenReturn(new java.util.ArrayList<>(
                            List.of(Point.of(62, 90))));
            scroll.addAnimation(new AnimatonScreenScroll(new ScreenScrolling(
                    path, Point.of(44, 90), Point.of(62, 90))));
        }

        scroll.paint();

        assertEquals(Point.of(62, 90), view.getTopLeft());
        assertEquals(Point.of(28, 0),
                new Area(view).convertToCanvasPoint(Location.of(3, 3)));
        assertEquals(Location.of(2, 3),
                CanvasInMapCoordinates.make(view, mapSize).getTopLeft());

        // Completing an automatic animation or releasing WASD must leave
        // the same camera ready for the next frame, without another step.
        when(keys.isScrolling()).thenReturn(false);
        scroll.paint();
        assertEquals(Point.of(62, 90), view.getTopLeft());
    }
}
