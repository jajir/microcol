package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.Point;

public class CanvasAreaTest {

    private CanvasArea canvasArea;

    @Test
    void test_setCanvasWidth_canvas_200_map_45000() throws Exception {
        canvasArea.setCanvasWidth(200, 45000);

        assertEquals(Point.of(200, 600), canvasArea.getCanvasSize());
        assertEquals(Point.of(400, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasWidth_canvas_200_map_600() throws Exception {
        canvasArea.setCanvasWidth(200, 600);

        assertEquals(Point.of(200, 600), canvasArea.getCanvasSize());
        /*
         * Previous center 500 = 800/2 + 100.
         */
        assertEquals(Point.of(400, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasWidth_canvas_200_map_510() throws Exception {
        canvasArea.setCanvasWidth(200, 510);

        assertEquals(Point.of(200, 600), canvasArea.getCanvasSize());
        /*
         * Previous center 500 = 800/2 + 100.
         */
        assertEquals(Point.of(310, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasWidth_canvas_200_map_180() throws Exception {
        canvasArea.setCanvasWidth(200, 180);

        assertEquals(Point.of(200, 600), canvasArea.getCanvasSize());
        assertEquals(Point.of(-10, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasWidth_canvas_200_map_600_previousCanvasPosition_20()
            throws Exception {
        canvasArea.setCanvasWidth(40,
                VisibleAreaService.MAX_CANVAS_SIDE_LENGTH);
        canvasArea.setTopLeft(Point.of(20, 100),
                VisibleAreaService.MAX_CANVAS_SIZE);
        assertEquals(Point.of(20, 100), canvasArea.getTopLeft());
        canvasArea.setCanvasWidth(200, 600);

        assertEquals(Point.of(200, 600), canvasArea.getCanvasSize());
        assertEquals(Point.of(0, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasWidth_canvas_200_map_200() throws Exception {
        canvasArea.setCanvasWidth(200, 200);

        assertEquals(Point.of(200, 600), canvasArea.getCanvasSize());
        assertEquals(Point.of(0, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasHeight_shrinking_keeps_top_row() throws Exception {
        // A status bar that wraps onto more lines takes height from the map;
        // the map keeps its top row, so the tile under the pointer stays.
        canvasArea.setCanvasHeight(554, 45000);

        assertEquals(Point.of(800, 554), canvasArea.getCanvasSize());
        assertEquals(Point.of(100, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasHeight_growing_keeps_top_row() throws Exception {
        canvasArea.setCanvasHeight(646, 45000);

        assertEquals(Point.of(800, 646), canvasArea.getCanvasSize());
        assertEquals(Point.of(100, 100), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasHeight_growing_at_map_bottom_stays_in_map()
            throws Exception {
        canvasArea.setCanvasHeight(600, 720);
        assertEquals(Point.of(100, 100), canvasArea.getTopLeft());

        canvasArea.setCanvasHeight(650, 720);

        assertEquals(Point.of(800, 650), canvasArea.getCanvasSize());
        assertEquals(Point.of(100, 70), canvasArea.getTopLeft());
    }

    @Test
    void test_setCanvasHeight_taller_than_map_is_centered() throws Exception {
        canvasArea.setCanvasHeight(600, 450);

        assertEquals(Point.of(800, 600), canvasArea.getCanvasSize());
        assertEquals(Point.of(100, -75), canvasArea.getTopLeft());
    }

    @Test
    void test_computeNewTopLeftConnerOfCanvas_at_0_0() throws Exception {
        final Point ret = canvasArea.computeNewTopLeftConnerOfCanvas(Point.ZERO,
                Point.of(1600, 1200));

        assertEquals(Point.of(0, 0), ret);
    }

    @Test
    void test_computeNewTopLeftConnerOfCanvas_at__10__10() throws Exception {
        final Point ret = canvasArea.computeNewTopLeftConnerOfCanvas(
                Point.of(-10, -10), Point.of(1600, 1200));

        assertEquals(Point.of(0, 0), ret);
    }

    @Test
    void test_computeNewTopLeftConnerOfCanvas_at_10_10() throws Exception {
        final Point ret = canvasArea.computeNewTopLeftConnerOfCanvas(
                Point.of(10, 10), Point.of(1600, 1200));

        assertEquals(Point.of(10, 10), ret);
    }

    @Test
    void test_computeNewTopLeftConnerOfCanvas_at_800_600() throws Exception {
        final Point ret = canvasArea.computeNewTopLeftConnerOfCanvas(
                Point.of(800, 600), Point.of(1600, 1200));

        assertEquals(Point.of(800, 600), ret);
    }

    @Test
    void test_computeNewTopLeftConnerOfCanvas_at_810_610() throws Exception {
        final Point ret = canvasArea.computeNewTopLeftConnerOfCanvas(
                Point.of(810, 610), Point.of(1600, 1200));

        assertEquals(Point.of(800, 600), ret);
    }

    @Test
    void test_initial_condition() throws Exception {
        assertEquals(Point.of(800, 600), canvasArea.getCanvasSize());
        assertEquals(Point.of(100, 100), canvasArea.getTopLeft());
    }

    @BeforeEach
    private void beforeEach() {
        canvasArea = new CanvasArea();
        canvasArea.setCanvasWidth(800,
                VisibleAreaService.MAX_CANVAS_SIDE_LENGTH);
        canvasArea.setCanvasHeight(600,
                VisibleAreaService.MAX_CANVAS_SIDE_LENGTH);
        canvasArea.setTopLeft(Point.of(100, 100),
                VisibleAreaService.MAX_CANVAS_SIZE);
    }

    @AfterEach
    private void afterEach() {
        canvasArea = null;
    }

}
