package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.gui.Point;
import org.microcol.model.Location;

public class VisibleAreaServiceTest {

    private VisibleAreaService visibleArea;

    @Test
    public void test_setMapSize_canvas_is_not_ready() throws Exception {
        final VisibleAreaService visibleArea = new VisibleAreaService();
        visibleArea.setMapSize(Location.of(79, 77));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(3555, visibleArea.getMapSize().getX());
        assertEquals(3465, visibleArea.getMapSize().getY());
        assertEquals(Point.of(0, 0), visibleArea.getTopLeft());
        assertEquals(Point.of(-1, -1), visibleArea.getCanvasSize());
    }

    @Test
    @DisplayName("test setMapSize canvas is ready and map is greater than canvas")
    public void test_setMapSize_canvas_is_ready_and_map_is_greater_than_canvas() throws Exception {
        final VisibleAreaService visibleArea = new VisibleAreaService();
        visibleArea.setCanvasWidth(800);
        visibleArea.setCanvasHeight(600);
        visibleArea.setMapSize(Location.of(79, 77));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(3555, visibleArea.getMapSize().getX());
        assertEquals(3465, visibleArea.getMapSize().getY());
        assertEquals(Point.of(0, 0), visibleArea.getTopLeft());
        assertEquals(Point.of(800, 600), visibleArea.getCanvasSize());
    }

    @Test
    @DisplayName("test setMapSize canvas is ready and map is smaller than canvas")
    public void test_setMapSize_canvas_is_ready_and_map_is_smaller_than_canvas() throws Exception {
        final VisibleAreaService visibleArea = new VisibleAreaService();
        visibleArea.setCanvasWidth(800);
        visibleArea.setCanvasHeight(600);
        visibleArea.setMapSize(Location.of(10, 10));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(450, visibleArea.getMapSize().getX());
        assertEquals(450, visibleArea.getMapSize().getY());
        assertEquals(Point.of(-175, -75), visibleArea.getTopLeft());
        assertEquals(Point.of(800, 600), visibleArea.getCanvasSize());
    }

    @Test
    public void test_initialization_setWorldMap_set_bigger_canvas_than_map() throws Exception {
        visibleArea.setCanvasWidth(800);
        visibleArea.setCanvasHeight(600);

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(-175, -75), visibleArea.getTopLeft());
        assertEquals(Point.of(800, 600), visibleArea.getCanvasSize());
    }

    @Test
    public void test_initialization_setWorldMap_set_smaller_canvas_than_map() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(0, 0), visibleArea.getTopLeft());
        assertEquals(Point.of(160, 120), visibleArea.getCanvasSize());
    }

    @Test
    public void test_setTopLeftPosionOfCanvas_null() throws Exception {
        assertThrows(NullPointerException.class, () -> visibleArea.setTopLeftPosionOfCanvas(null));
    }

    @Test
    public void test_setTopLeftPosionOfCanvas_canvas_is_smaller_move_at_10_10() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);

        visibleArea.setTopLeftPosionOfCanvas(Point.of(10, 10));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(10, 10), visibleArea.getTopLeft());
        assertEquals(Point.of(160, 120), visibleArea.getCanvasSize());
    }

    @Test
    public void test_setTopLeftPosionOfCanvas_canvas_is_smaller_move_at_minus_values()
            throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);

        visibleArea.setTopLeftPosionOfCanvas(Point.of(-10, -10));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(0, 0), visibleArea.getTopLeft());
        assertEquals(Point.of(160, 120), visibleArea.getCanvasSize());
    }

    @Test
    public void test_setTopLeftPosionOfCanvas_canvas_is_smaller_move_at_290_330() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);

        visibleArea.setTopLeftPosionOfCanvas(Point.of(290, 330));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(290, 330), visibleArea.getTopLeft());
        assertEquals(Point.of(160, 120), visibleArea.getCanvasSize());
    }

    @Test
    public void test_setTopLeftPosionOfCanvas_canvas_is_smaller_move_at_291_331() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);

        visibleArea.setTopLeftPosionOfCanvas(Point.of(291, 331));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(290, 330), visibleArea.getTopLeft());
        assertEquals(Point.of(160, 120), visibleArea.getCanvasSize());
    }

    @Test
    public void test_setTopLeftPosionOfCanvas_canvas_is_smaller_move_at_450_450() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);

        visibleArea.setTopLeftPosionOfCanvas(Point.of(450, 450));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(290, 330), visibleArea.getTopLeft());
        assertEquals(Point.of(160, 120), visibleArea.getCanvasSize());
    }

    @Test
    public void test_setTopLeftPosionOfCanvas_canvas_is_smaller_move_at_451_451() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);

        visibleArea.setTopLeftPosionOfCanvas(Point.of(451, 451));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(Point.of(290, 330), visibleArea.getTopLeft());
        assertEquals(Point.of(160, 120), visibleArea.getCanvasSize());
    }

    static Stream<Arguments> isVisibleMapPoint_dataProvider() {
        return Stream.of(arguments(Point.ZERO, false), arguments(Point.of(-1, -1), false),
                arguments(Point.of(30, 50), true), arguments(Point.of(29, 49), false),
                arguments(Point.of(190, 170), true), arguments(Point.of(189, 169), true),
                arguments(Point.of(191, 171), false));
    }

    @ParameterizedTest(name = "{index}: point = {0} is valid {1} on map")
    @MethodSource("isVisibleMapPoint_dataProvider")
    public void test_isVisibleMapPoint(final Point point, final boolean shouldBeValid)
            throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);
        visibleArea.setTopLeftPosionOfCanvas(Point.of(30, 50));

        assertEquals(shouldBeValid, visibleArea.isVisibleMapPoint(point));
    }

    @Test
    public void test_isVisibleCanvasPoint() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);
        visibleArea.setTopLeftPosionOfCanvas(Point.of(30, 50));

        assertTrue(visibleArea.isVisibleCanvasPoint(Point.of(160, 120)));
        assertTrue(visibleArea.isVisibleCanvasPoint(Point.of(160, 120)));
        assertTrue(visibleArea.isVisibleCanvasPoint(Point.of(80, 60)));

        assertFalse(visibleArea.isVisibleCanvasPoint(Point.of(-1, -1)));
        assertFalse(visibleArea.isVisibleCanvasPoint(Point.of(161, 121)));
    }

    @Test
    public void test_isMapPointValid() throws Exception {
        visibleArea.setCanvasWidth(160);
        visibleArea.setCanvasHeight(120);
        visibleArea.setTopLeftPosionOfCanvas(Point.of(30, 50));

        assertTrue(visibleArea.isMapPointValid(Point.ZERO));
        assertTrue(visibleArea.isMapPointValid(Point.of(450, 450)));
        assertTrue(visibleArea.isMapPointValid(Point.of(200, 200)));
        assertFalse(visibleArea.isMapPointValid(Point.of(-1, -1)));
        assertFalse(visibleArea.isMapPointValid(Point.of(451, 451)));
    }

    @Test
    public void test_isMapPointValid_null() throws Exception {
        assertThrows(NullPointerException.class, () -> visibleArea.isMapPointValid(null));
    }

    @BeforeEach
    private void beforeEach() {
        visibleArea = new VisibleAreaService();
        visibleArea.setMapSize(Location.of(10, 10));

        assertNotNull(visibleArea.getMapSize());
        assertEquals(450, visibleArea.getMapSize().getX());
        assertEquals(450, visibleArea.getMapSize().getY());
        assertEquals(Point.of(0, 0), visibleArea.getTopLeft());
        assertEquals(CanvasArea.NOT_READY, visibleArea.getCanvasSize());
    }

    @AfterEach
    private void afterEach() {
        visibleArea = null;
    }
}
