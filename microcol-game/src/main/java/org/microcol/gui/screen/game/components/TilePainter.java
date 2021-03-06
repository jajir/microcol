package org.microcol.gui.screen.game.components;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.List;

import org.microcol.gui.Point;
import org.microcol.gui.util.PaintService;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Holds small canvas of fixed size of one tile. Class provide method to paint
 * selected tile.
 */
class TilePainter {

    private final PaintService paintService;

    private final Canvas canvas;

    @Inject
    public TilePainter(final PaintService paintService) {
        this.paintService = Preconditions.checkNotNull(paintService);
        canvas = new Canvas(TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
    }

    void setTerrain(final Terrain terrain, final Location location) {
        paintService.paintTerrainOnTile(canvas.getGraphicsContext2D(), Point.ZERO, location,
                terrain, false);
    }

    void paintUnit(final List<Unit> units) {
        final Unit unit = units.stream().findFirst().get();
        paintService.paintUnit(canvas.getGraphicsContext2D(), Point.ZERO, unit);
    }

    void paintColony(final Colony colony) {
        paintService.paintColony(canvas.getGraphicsContext2D(), Point.ZERO, colony, true);
    }

    void setImage(final Image image) {
        canvas.getGraphicsContext2D().drawImage(image, 0, 0);
    }

    void clear() {
        final GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(new Color(0, 0, 0, 0.34));
        g.fillRect(0, 0, TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
    }

    /**
     * @return the canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

}
