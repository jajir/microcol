package org.microcol.gui.util;

import java.util.List;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.MapManager;
import org.microcol.model.Colony;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Place;
import org.microcol.model.PlaceLocation;
import org.microcol.model.Player;
import org.microcol.model.RiverSize;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeLineCap;
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

    /**
     * Road surface and its darker edge; both contrast with grass and sand.
     */
    public static final Color ROAD_COLOR = Color.web("#d8b26e");

    private static final Color ROAD_EDGE_COLOR = Color.web("#4a3118");

    /** River water; a blue that stands out on grass, sand and snow. */
    public static final Color RIVER_COLOR = Color.web("#2f6fd0");

    /** Width of a small river course, a share of one square. */
    private static final double SMALL_RIVER_WIDTH = Tile.TILE_WIDTH_IN_PX
            * 0.12;

    /** Width of a large river course, a share of one square. */
    private static final double LARGE_RIVER_WIDTH = Tile.TILE_WIDTH_IN_PX
            * 0.28;

    private final ImageProvider imageProvider;

    private final MapManager mapManager;

    private final Font colonyFont;

    @Inject
    public PaintService(final ImageProvider imageProvider,
            final MapManager mapManager, final FontService fontService) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.mapManager = Preconditions.checkNotNull(mapManager);
        this.colonyFont = fontService.getDefault(16);
    }

    /**
     * Draw a road as a dark-edged tan band across the square. It is code
     * drawing, so the overlay needs no image asset.
     *
     * @param graphics required canvas
     * @param point    required top-left corner of the tile
     */
    private void paintRoad(final GraphicsContext graphics, final Point point) {
        final double size = Tile.TILE_WIDTH_IN_PX;
        graphics.setFill(ROAD_EDGE_COLOR);
        graphics.fillRect(point.getX(), point.getY() + size * 0.36, size,
                size * 0.28);
        graphics.setFill(ROAD_COLOR);
        graphics.fillRect(point.getX(), point.getY() + size * 0.44, size,
                size * 0.12);
    }

    /**
     * Draw the half of each river course that leaves a square. Every course
     * reaches the midpoint between its two squares, and its width shows the
     * size of this square. A square without a joined neighbour gets a round
     * dot.
     *
     * @param graphics required canvas
     * @param point    required top-left corner of the tile
     * @param size     required river size of the square
     * @param joined   required top-left corners of the joined neighbours
     */
    public void paintRiver(final GraphicsContext graphics, final Point point,
            final RiverSize size, final List<Point> joined) {
        final double width = size == RiverSize.LARGE ? LARGE_RIVER_WIDTH
                : SMALL_RIVER_WIDTH;
        final double half = Tile.TILE_WIDTH_IN_PX / 2.0;
        final double centerX = point.getX() + half;
        final double centerY = point.getY() + half;
        // The grid is stroked later with the same canvas state, so restore it.
        final Paint previousStroke = graphics.getStroke();
        final Paint previousFill = graphics.getFill();
        final double previousWidth = graphics.getLineWidth();
        final StrokeLineCap previousCap = graphics.getLineCap();
        graphics.setStroke(RIVER_COLOR);
        graphics.setFill(RIVER_COLOR);
        graphics.setLineWidth(width);
        // A butt cap ends each half at its midpoint; the default square cap
        // would reach half a width into the neighbour square.
        graphics.setLineCap(StrokeLineCap.BUTT);
        for (final Point neighbour : joined) {
            graphics.strokeLine(centerX, centerY,
                    (centerX + neighbour.getX() + half) / 2,
                    (centerY + neighbour.getY() + half) / 2);
        }
        graphics.fillOval(centerX - width / 2, centerY - width / 2, width,
                width);
        graphics.setStroke(previousStroke);
        graphics.setFill(previousFill);
        graphics.setLineWidth(previousWidth);
        graphics.setLineCap(previousCap);
    }

    /**
     * Draw unit to tile.
     *
     * @param graphics required graphics to draw
     * @param point    required point of top left corner or tile where to draw
     *                 unit
     * @param unit     required unit to draw
     */
    public void paintUnit(final GraphicsContext graphics, final Point point,
            final Unit unit) {
        // A frame's unit list can outlive the unit's departure from the map.
        final Place place = unit.getPlace();
        if (place instanceof PlaceLocation) {
            paintUnit(graphics, point, unit,
                    ((PlaceLocation) place).getOrientation());
        }
    }

    public void paintUnit(final GraphicsContext graphics, final Point point,
            final Unit unit, final Direction orientation) {
        paintUnitWithoutFlag(graphics, point, unit, orientation);
        painFlagWithAction(graphics, point.add(OWNERS_FLAG_POSITION), unit);
    }

    public void paintUnitWithoutFlag(final GraphicsContext graphics,
            final Point point, final Unit unit) {
        paintUnitWithoutFlag(graphics, point, unit,
                unit.getDefaultOrintation());
    }

    public void paintUnitWithoutFlag(final GraphicsContext graphics,
            final Point point, final Unit unit, final Direction orientation) {
        final Point p = point.add(UNIT_IMAGE_POSITION);
        graphics.drawImage(imageProvider.getUnitImage(unit, orientation),
                p.getX(), p.getY());
    }

    public void paintColony(final GraphicsContext graphics, final Point point,
            final Colony colony, final boolean drawColonyName) {
        paintColony(graphics, point, colony.getOwner(), colony.getName(),
                drawColonyName);
    }

    /**
     * Paint a colony from an observed owner and name, so remembered colonies do
     * not need the live colony object.
     */
    public void paintColony(final GraphicsContext graphics, final Point point,
            final Player owner, final String colonyName,
            final boolean drawColonyName) {
        final Point p = point.add(UNIT_IMAGE_POSITION);
        graphics.drawImage(
                imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_TOWN),
                p.getX(), p.getY());
        if (drawColonyName) {
            paintOwnersFlag(graphics, point.add(OWNERS_FLAG_POSITION), owner);
            graphics.setFont(colonyFont);
            graphics.setTextAlign(TextAlignment.CENTER);
            graphics.setTextBaseline(VPos.CENTER);
            graphics.setFill(Color.BLACK);
            graphics.fillText(colonyName, p.getX() + 20, p.getY() + 55);
        }
    }

    private void painFlagWithAction(final GraphicsContext graphics,
            final Point point, final Unit unit) {
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
     * @param graphics required graphics
     * @param point    required point where will be flag placed
     * @param player   required player
     */
    public void paintOwnersFlag(final GraphicsContext graphics,
            final Point point, final Player player) {
        graphics.setStroke(Color.LIGHTGREY);
        graphics.setLineWidth(1.5F);
        graphics.strokeRect(point.getX(), point.getY(), FLAG_WIDTH,
                FLAG_HEIGHT);
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
        graphics.fillRect(point.getX() + 1, point.getY() + 1, FLAG_WIDTH - 2,
                FLAG_HEIGHT - 2);
    }

    /**
     * Paint one tile on game space. Including additional symbols and pictures
     * like fish.
     *
     * @param graphics      required graphics where will be tile drawn
     * @param point         required point where will be painted
     * @param location      required map location that will be drawn
     * @param terrain       required terrain object
     * @param isHighlighted when it's <code>true</code> than tile is highlighted
     * @param gameTick      required long representing how many game ticks was
     *                      already done. It allows to time animations.
     */
    public void paintTerrainOnTile(final GraphicsContext graphics,
            final Point point, final Location location, final Terrain terrain,
            final boolean isHighlighted, final long gameTick) {
        // terrain tile
        final Image imageBackground = mapManager
                .getTerrainImage(terrain.getTerrainType(), location, gameTick);
        graphics.drawImage(imageBackground, 0, 0, Tile.TILE_WIDTH_IN_PX,
                Tile.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
                Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);

        // prechody
        final Image imageCoast = mapManager.getCoatsImageAt(location);
        graphics.drawImage(imageCoast, 0, 0, Tile.TILE_WIDTH_IN_PX,
                Tile.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
                Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);

        if (terrain.isHasTrees()) {
            graphics.drawImage(mapManager.getTreeImage(location), point.getX(),
                    point.getY());
        }

        if (terrain.isHasField()) {
            graphics.drawImage(
                    imageProvider.getImage(ImageLoaderTerrain.IMG_FIELD),
                    point.getX(), point.getY());
        }
        if (terrain.isHasRoad()) {
            paintRoad(graphics, point);
        }
        if (isHighlighted) {
            graphics.setFill(new Color(0.95, 0.75, 0.90, 0.4F));
            graphics.fillRect(point.getX(), point.getY(), Tile.TILE_WIDTH_IN_PX,
                    Tile.TILE_WIDTH_IN_PX);
        }
        final Image hiddenCoast = mapManager.getHiddenImageCoast(location);
        if (hiddenCoast != null) {
            graphics.drawImage(hiddenCoast, 0, 0, Tile.TILE_WIDTH_IN_PX,
                    Tile.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
                    Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);
        }
    }

    public void paintTerrainOnTile(final GraphicsContext graphics,
            final Point point, final Location location, final Terrain terrain,
            final boolean isHighlighted) {
        paintTerrainOnTile(graphics, point, location, terrain, isHighlighted,
                0);
    }

}
