package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.microcol.gui.Point;
import org.microcol.model.Location;

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
}
