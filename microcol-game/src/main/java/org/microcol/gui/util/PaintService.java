package org.microcol.gui.util;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.MapManager;
import org.microcol.model.Colony;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Contains methods for painting particular objects.
 */
public final class PaintService {

    /**
     * Unit's flag width.
     */
    private static final int FLAG_WIDTH = 7;

    /**
     * Unit's flag height.
     */
    private static final int FLAG_HEIGHT = 12;

    /**
     * Position of unit flag from top left tile corner.
     */
    private final Point OWNERS_FLAG_POSITION = Point.of(1, 5);

    /**
     * Position of unit image from top left tile corner.
     */
    private final Point UNIT_IMAGE_POSITION = Point.of(2, 4);

    private final ImageProvider imageProvider;

    private final MapManager mapManager;

    private final Font colonyFont;

    @Inject
    public PaintService(final ImageProvider imageProvider, final MapManager mapManager,
            final FontService fontService) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.mapManager = Preconditions.checkNotNull(mapManager);
        this.colonyFont = fontService.getDefault(16);
    }

    /**
     * Draw unit to tile.
     *
     * @param graphics
     *            required graphics to draw
     * @param point
     *            required point of top left corner or tile where to draw unit
     * @param unit
     *            required unit to draw
     */
    public void paintUnit(final GraphicsContext graphics, final Point point, final Unit unit) {
        paintUnit(graphics, point, unit, unit.getPlaceLocation().getOrientation());
    }

    public void paintUnit(final GraphicsContext graphics, final Point point, final Unit unit,
            final Direction orientation) {
        final Point p = point.add(UNIT_IMAGE_POSITION);
        graphics.drawImage(imageProvider.getUnitImage(unit, orientation), p.getX(), p.getY());
        painFlagWithAction(graphics, point.add(OWNERS_FLAG_POSITION), unit);
    }

    public void paintColony(final GraphicsContext graphics, final Point point, final Colony colony,
            final boolean drawColonyName) {
        final Point p = point.add(UNIT_IMAGE_POSITION);
        graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_TILE_TOWN), p.getX(), p.getY());
        if (drawColonyName) {
            paintOwnersFlag(graphics, point.add(OWNERS_FLAG_POSITION), colony.getOwner());
            graphics.setFont(colonyFont);
            graphics.setTextAlign(TextAlignment.CENTER);
            graphics.setTextBaseline(VPos.CENTER);
            graphics.setFill(Color.BLACK);
            graphics.fillText(colony.getName(), p.getX() + 20, p.getY() + 55);
        }
    }

    private void painFlagWithAction(final GraphicsContext graphics, final Point point,
            final Unit unit) {
        paintOwnersFlag(graphics, point, unit.getOwner());
        final String sign = unit.getAction().getType().getSign();
        if (!Strings.isNullOrEmpty(sign)) {
            graphics.setFont(colonyFont);
            graphics.setTextAlign(TextAlignment.CENTER);
            graphics.setTextBaseline(VPos.CENTER);
            graphics.setFill(Color.BLACK);
            graphics.fillText(sign, point.getX() + 2, point.getY() + 5);
        }
    }

    /**
     * All units have flag containing color of owner. Method draw this flag.
     * 
     * @param graphics
     *            required graphics
     * @param point
     *            required point where will be flag placed
     * @param player
     *            required player
     */
    public void paintOwnersFlag(final GraphicsContext graphics, final Point point,
            final Player player) {
        graphics.setStroke(Color.LIGHTGREY);
        graphics.setLineWidth(1.5F);
        graphics.strokeRect(point.getX(), point.getY(), FLAG_WIDTH, FLAG_HEIGHT);
        // TODO move colors to player's object
        if (player.isHuman()) {
            graphics.setFill(Color.YELLOW);
        } else {
            switch (player.getName().hashCode() % 4) {
            case 0:
                graphics.setFill(Color.RED);
                break;
            case 1:
                graphics.setFill(Color.GREEN);
                break;
            case 2:
                graphics.setFill(Color.MAGENTA);
                break;
            case 3:
                graphics.setFill(Color.BLUE);
                break;
            default:
                graphics.setFill(Color.GRAY);
                break;
            }
        }
        graphics.fillRect(point.getX() + 1, point.getY() + 1, FLAG_WIDTH - 2, FLAG_HEIGHT - 2);
    }

    /**
     * Paint one tile on game space. Including additional symbols and pictures
     * like fish.
     * 
     * @param graphics
     *            required graphics where will be tile drawn
     * @param point
     *            required point where will be painted
     * @param location
     *            required map location that will be drawn
     * @param terrain
     *            required terrain object
     * @param isHighlighted
     *            when it's <code>true</code> than tile is highlighted
     */
    public void paintTerrainOnTile(final GraphicsContext graphics, final Point point,
            final Location location, final Terrain terrain, final boolean isHighlighted) {
        // terrain tile
        final Image imageBackground = mapManager.getTerrainImage(terrain.getTerrainType(),
                location);
        graphics.drawImage(imageBackground, 0, 0, Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX, point.getX(),
                point.getY(), Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);

        // prechody
        final Image imageCoast = mapManager.getCoatsImageAt(location);
        graphics.drawImage(imageCoast, 0, 0, Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX, point.getX(),
                point.getY(), Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);

        if (terrain.isHasTrees()) {
            graphics.drawImage(mapManager.getTreeImage(location), point.getX(), point.getY());
        }

        if (terrain.isHasField()) {
            graphics.drawImage(imageProvider.getImage(ImageProvider.IMG_FIELD), point.getX(),
                    point.getY());
        }
        if (isHighlighted) {
            graphics.setFill(new Color(0.95, 0.75, 0.90, 0.4F));
            graphics.fillRect(point.getX(), point.getY(), Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);
        }
        final Image hiddenCoast = mapManager.getHiddenImageCoast(location);
        if (hiddenCoast != null) {
            graphics.drawImage(hiddenCoast, 0, 0, Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX, point.getX(),
                    point.getY(), Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);
        }
    }

}
