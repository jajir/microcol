package org.akropia.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.akropia.gui.Point;
import org.akropia.model.Location;

import javafx.geometry.Rectangle2D;

class MinimapProjectionTest {
    @Test
    void entireLandscapeWorldFitsWithEqualScaleOnBothAxes() {
        final MinimapProjection projection = new MinimapProjection(
                Location.of(100, 50), 220, 180);
        assertEquals(new Rectangle2D(4, 37, 212, 106), projection.getBounds());
        assertEquals(Point.of(0, 0), projection.toMap(4, 37));
        assertEquals(Point.of(4500, 2250), projection.toMap(216, 143));
        assertEquals(Point.of(2250, 1125), projection.toMap(110, 90));
    }

    @Test
    void portraitWorldFitsAndViewportUsesTheSameProjection() {
        final MinimapProjection projection = new MinimapProjection(
                Location.of(20, 80), 220, 180);
        assertEquals(new Rectangle2D(88.5, 4, 43, 172), projection.getBounds());
        final Rectangle2D view = projection.viewport(Point.of(450, 900),
                Point.of(450, 900));
        assertEquals(new Rectangle2D(110, 47, 21.5, 43), view);
    }

    @Test
    void viewLargerThanWorldOutlinesOnlyThePlayableExtent() {
        final MinimapProjection projection = new MinimapProjection(
                Location.of(4, 3), 220, 180);
        assertEquals(projection.getBounds(),
                projection.viewport(Point.of(-360, -232), Point.of(900, 600)));
    }

    @Test
    void manualNavigationKeepsSmallWorldCenteredAndClampsLargeWorld() {
        final CanvasArea area = new CanvasArea();
        area.setCanvasWidth(900, 180);
        area.setCanvasHeight(600, 135);
        area.setTopLeft(Point.of(2000, -2000), Point.of(180, 135));
        assertEquals(Point.of(-360, -232), area.getTopLeft());
        area.setTopLeft(Point.of(2000, -2000), Point.of(1800, 1350));
        assertEquals(Point.of(900, 0), area.getTopLeft());
    }

    @Test
    void overscrollClampsCameraCenterToWorldEvenWhenWorldIsSmaller() {
        final CanvasArea area = new CanvasArea(true);
        area.setCanvasWidth(900, 180);
        area.setCanvasHeight(600, 135);
        area.setTopLeft(Point.of(-2000, -2000), Point.of(180, 135));
        assertEquals(Point.of(-450, -300), area.getTopLeft());
        area.setTopLeft(Point.of(2000, 2000), Point.of(180, 135));
        assertEquals(Point.of(-270, -165), area.getTopLeft());
        area.setCanvasWidth(600, 180);
        area.setCanvasHeight(400, 135);
        assertEquals(Point.of(-120, -165), area.getTopLeft());
        area.setTopLeft(Point.of(2000, 2000), Point.of(1800, 1350));
        assertEquals(Point.of(1500, 1150), area.getTopLeft());
    }

    @Test
    void viewportClipsEachCornerWithoutChangingWorldBounds() {
        final MinimapProjection p = new MinimapProjection(Location.of(100, 50),
                220, 180);
        final Rectangle2D world = p.getBounds();
        for (int x : new int[] { 0, 4500 }) {
            for (int y : new int[] { 0, 2250 }) {
                final Rectangle2D view = p.viewport(Point.of(x - 450, y - 300),
                        Point.of(900, 600));
                assertEquals(21.2, view.getWidth(), .001);
                assertEquals(300 * 212.0 / 4500, view.getHeight(), .001);
                assertTrue(world.contains(view));
            }
        }
        assertEquals(new Rectangle2D(4, 37, 212, 106), p.getBounds());
    }

    @Test
    void pixelsImmediatelyOutsideWorldCannotSelectBorderTiles() {
        final VisibleAreaService view = new VisibleAreaService(true);
        view.setMapSize(Location.of(10, 10));
        view.setCanvasWidth(800);
        view.setCanvasHeight(600);
        view.setTopLeftPosionOfCanvas(Point.of(-400, -300));
        final Area area = new Area(view);
        assertEquals(Location.of(0, 1),
                area.convertToLocation(Point.of(399, 300)));
        assertEquals(Location.of(1, 0),
                area.convertToLocation(Point.of(400, 299)));
        assertEquals(Location.of(1, 1),
                area.convertToLocation(Point.of(400, 300)));
        assertEquals(Location.of(11, 11),
                area.convertToLocation(Point.of(850, 750)));
    }

}
