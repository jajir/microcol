package org.microcol.gui.screen.game.gamepanel;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.model.Location;

import javafx.geometry.Rectangle2D;

/** A full-world, aspect-preserving projection shared by painting and input. */
public final class MinimapProjection {
    private final double scale;
    private final Rectangle2D bounds;

    public MinimapProjection(final Location worldSize, final double width,
            final double height) {
        final double worldWidth = worldSize.getX() * Tile.TILE_WIDTH_IN_PX;
        final double worldHeight = worldSize.getY() * Tile.TILE_WIDTH_IN_PX;
        scale = Math.min((width - 8) / worldWidth, (height - 8) / worldHeight);
        bounds = new Rectangle2D((width - worldWidth * scale) / 2,
                (height - worldHeight * scale) / 2, worldWidth * scale,
                worldHeight * scale);
    }

    public Rectangle2D getBounds() {
        return bounds;
    }

    public double getTileSize() {
        return Tile.TILE_WIDTH_IN_PX * scale;
    }

    public Point toMap(final double x, final double y) {
        return Point.of((int) Math.round((x - bounds.getMinX()) / scale),
                (int) Math.round((y - bounds.getMinY()) / scale));
    }

    public Rectangle2D viewport(final Point topLeft, final Point size) {
        final double x = Math.max(0, topLeft.getX() * scale);
        final double y = Math.max(0, topLeft.getY() * scale);
        final double right = Math.min(bounds.getWidth(),
                (topLeft.getX() + size.getX()) * scale);
        final double bottom = Math.min(bounds.getHeight(),
                (topLeft.getY() + size.getY()) * scale);
        return new Rectangle2D(bounds.getMinX() + x, bounds.getMinY() + y,
                Math.max(0, right - x), Math.max(0, bottom - y));
    }
}
