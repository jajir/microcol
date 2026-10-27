package org.microcol.gui.util;

import java.util.List;
import java.util.Optional;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageLoaderColonies;
import org.microcol.model.ColonySighting;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.MapManager;
import org.microcol.model.Colony;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.NativeHostility;
import org.microcol.model.NativeNation;
import org.microcol.model.NativeRelationView;
import org.microcol.model.NativeSettlement;
import org.microcol.model.Place;
import org.microcol.model.PlaceLocation;
import org.microcol.model.Player;
import org.microcol.model.PlayerNative;
import org.microcol.model.ResourceOverlay;
import org.microcol.model.Terrain;
import org.microcol.model.TerrainType;
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
import javafx.scene.shape.StrokeLineJoin;
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
    private final Point UNIT_IMAGE_POSITION = Point.of(0, 0);

    /**
     * Road surface and its darker edge; both contrast with grass and sand.
     */
    public static final Color ROAD_COLOR = Color.web("#d8b26e");

    private static final Color ROAD_EDGE_COLOR = Color.web("#4a3118");

    /** River water; a blue that stands out on grass, sand and snow. */
    public static final Color RIVER_COLOR = Color.web("#426F70");

    /** Ink outline of a special-resource marker, the same as the grid ink. */
    private static final Color MARKER_INK = Color.web("#292824");

    /** Share of a square's width used by a lost-city rumor marker. */
    private static final double RESOURCE_MARKER_SHARE = 0.28;

    /** Share of a square's width used by a native settlement silhouette. */
    private static final double NATIVE_SETTLEMENT_SHARE = 0.56;

    /** Gold frame that marks a nation's capital, drawn over the square. */
    private static final Color CAPITAL_FRAME_COLOR = Color.web("#FFD60A");

    /** Tint behind a settlement whose visible rival provokes it. */
    private static final Color PROVOKED_BACKGROUND = Color.web("#7B1FA2", 0.35);

    /** Side of the attitude badge in the lower right corner of the square. */
    private static final double ATTITUDE_BADGE_SIZE = 12;

    private final ImageProvider imageProvider;

    private final MapManager mapManager;

    private final Font colonyFont;

    @Inject
    public PaintService(final ImageProvider imageProvider,
            final MapManager mapManager, final FontService fontService) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.mapManager = Preconditions.checkNotNull(mapManager);
        this.colonyFont = fontService.getDefault(14);
    }

    public void paintUnknownMark(final GraphicsContext graphics,
            final Point point, final Location location) {
        if (mapManager.hasUnknownMark(location)) {
            graphics.save();
            graphics.translate(point.getX(), point.getY());
            graphics.setStroke(Color.BLACK);
            graphics.setLineWidth(2);
            graphics.setLineCap(StrokeLineCap.ROUND);
            graphics.setLineJoin(StrokeLineJoin.ROUND);
            graphics.beginPath();
            graphics.appendSVGPath("M17 17 C16.7 13.5 20.3 11.9 23.6 12.4 "
                    + "C27.6 12.7 29 16.4 27.1 19 "
                    + "C25.6 21 22.8 21.8 22.5 24.2 L22.4 26 "
                    + "M22.2 31 L22.3 31.5");
            graphics.stroke();
            graphics.restore();
        }
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
     * Paint the selected resource picture over a known square. The atlas keeps
     * its surroundings transparent and positions the motif in the upper right.
     */
    public static void paintResource(final GraphicsContext graphics,
            final Point point, final ResourceOverlay overlay,
            final ImageProvider images) {
        graphics.drawImage(images.getResourceImage(overlay), point.getX(),
                point.getY());
    }

    /** Fill of an unresolved lost-city rumor marker. */
    private static final Color RUMOR_MARKER_COLOR = Color.web("#E8B84A");

    /**
     * Draw an unresolved lost-city rumor as an outlined triangle in the
     * bottom-left corner, clear of the resource marker. Only the caller decides
     * whether the square is currently visible; this method draws what it is
     * given. Graphics state is restored afterwards.
     *
     * @param graphics required canvas
     * @param point    required top-left corner of the tile
     */
    public void paintLostCityRumor(final GraphicsContext graphics,
            final Point point) {
        final double size = Tile.TILE_WIDTH_IN_PX * RESOURCE_MARKER_SHARE;
        final double left = point.getX() + Tile.TILE_WIDTH_IN_PX * 0.04;
        final double top = point.getY() + Tile.TILE_WIDTH_IN_PX * 0.62;
        final double[] xs = { left, left + size, left + size / 2 };
        final double[] ys = { top + size, top + size, top };
        final Paint previousFill = graphics.getFill();
        final Paint previousStroke = graphics.getStroke();
        final double previousWidth = graphics.getLineWidth();
        graphics.setFill(RUMOR_MARKER_COLOR);
        graphics.setStroke(MARKER_INK);
        graphics.setLineWidth(1.5);
        graphics.fillPolygon(xs, ys, 3);
        graphics.strokePolygon(xs, ys, 3);
        graphics.setFill(previousFill);
        graphics.setStroke(previousStroke);
        graphics.setLineWidth(previousWidth);
    }

    /** Draw the existing road across the local river's tangent. */
    public void paintRiverBridge(final GraphicsContext graphics,
            final Point point, final RiverShape river) {
        graphics.save();
        graphics.translate(point.getX() + river.bridgeX,
                point.getY() + river.bridgeY);
        graphics.rotate(river.bridgeAngle);
        final double x = -13.5;
        final double y = 0;
        graphics.setFill(ROAD_COLOR);
        graphics.fillRect(x, y - 5, 27, 10);
        graphics.setStroke(ROAD_EDGE_COLOR);
        graphics.setLineWidth(1.5);
        graphics.strokeLine(x, y - 5, x + 27, y - 5);
        graphics.strokeLine(x, y + 5, x + 27, y + 5);
        for (int plank = 3; plank < 27; plank += 5) {
            graphics.strokeLine(x + plank, y - 4, x + plank, y + 4);
        }
        graphics.restore();
    }

    /** Open the coast with two water arms around a small decorative island. */
    public void paintRiverMouth(final GraphicsContext graphics,
            final Point point, final RiverShape river,
            final Direction direction, final TerrainType land,
            final TerrainType water) {
        final double half = Tile.TILE_WIDTH_IN_PX / 2.0;
        final double length = Tile.TILE_WIDTH_IN_PX * .75;
        final double radius = river.width / 2;
        final double opening = river.width * 1.1;
        graphics.save();
        graphics.translate(point.getX() + half, point.getY() + half);
        graphics.rotate(Math.toDegrees(Math.atan2(direction.getVector().getY(),
                direction.getVector().getX())));
        graphics.setFill(imageProvider.getTerrainColor(water));
        graphics.beginPath();
        graphics.moveTo(0, -radius);
        graphics.bezierCurveTo(length * .35, -radius, length * .55, -opening,
                length, -opening);
        graphics.lineTo(length, opening);
        graphics.bezierCurveTo(length * .55, opening, length * .35, radius, 0,
                radius);
        graphics.closePath();
        graphics.fill();
        // A pointed flat-colour transition avoids a dark cap in the sea.
        graphics.setFill(RIVER_COLOR);
        graphics.beginPath();
        graphics.moveTo(0, -radius);
        graphics.bezierCurveTo(length * .25, -radius, length * .38,
                -radius * .5, length * .42, 0);
        graphics.bezierCurveTo(length * .38, radius * .5, length * .25, radius,
                0, radius);
        graphics.closePath();
        graphics.fill();
        final double islandRadius = Math.max(2.2, river.width * .3);
        graphics.setFill(imageProvider.getTerrainColor(land));
        graphics.setStroke(MARKER_INK);
        graphics.setLineWidth(1.6);
        graphics.beginPath();
        graphics.moveTo(length * .55, 0);
        graphics.bezierCurveTo(length * .75, -islandRadius, length * .95,
                -islandRadius, length * .95, 0);
        graphics.bezierCurveTo(length * .95, islandRadius, length * .75,
                islandRadius, length * .55, 0);
        graphics.closePath();
        graphics.fill();
        graphics.stroke();
        graphics.restore();
    }

    /** Draw a stable hand-drawn course in tile-local coordinates. */
    public void paintRiver(final GraphicsContext graphics, final Point point,
            final RiverShape river) {
        graphics.save();
        graphics.translate(point.getX(), point.getY());
        graphics.setFill(RIVER_COLOR);
        for (final RiverShape.Band band : river.bands) {
            graphics.fillPolygon(band.x, band.y, band.x.length);
        }
        if (river.roundCenter) {
            final double edge = (Tile.TILE_WIDTH_IN_PX - river.width) / 2;
            graphics.fillOval(edge, edge, river.width, river.width);
        }
        graphics.restore();
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
                colony.getPopulation(), colony.getFortificationLevel(),
                drawColonyName);
    }

    public void paintColony(final GraphicsContext graphics, final Point point,
            final ColonySighting sighting, final boolean drawColonyName) {
        paintColony(graphics, point, sighting.getOwner(),
                sighting.getColonyName(), sighting.getPopulation(),
                sighting.getFortificationLevel(), drawColonyName);
    }

    private void paintColony(final GraphicsContext graphics, final Point point,
            final Player owner, final String colonyName, final int population,
            final int fortificationLevel, final boolean drawColonyName) {
        graphics.drawImage(
                imageProvider.getImage(ImageLoaderColonies.colonyKey(population,
                        fortificationLevel)),
                point.getX() - 10, point.getY() - 10, 64, 64);
        graphics.drawImage(
                imageProvider.getImage(
                        ImageLoaderColonies.flagKey(owner.getEuropeanNation())),
                point.getX() - 10, point.getY() - 10, 64, 64);
        if (drawColonyName) {
            graphics.setFont(colonyFont);
            graphics.setTextAlign(TextAlignment.CENTER);
            graphics.setTextBaseline(VPos.CENTER);
            graphics.setFill(Color.BLACK);
            graphics.fillText(colonyName, point.getX() + 22.5,
                    point.getY() + 57);
        }
    }

    /**
     * Draw a native settlement on its square. The silhouette carries the class,
     * the fill carries the nation and a gold frame marks a capital, so no
     * single colour decides what the settlement is.
     *
     * The attitude badge and the provoked tint come from the model's view, so
     * the badge shape carries the attitude band and the tint marks a visible
     * rival, without colour alone.
     *
     * @param graphics   required canvas
     * @param point      required top-left corner of the tile
     * @param settlement required settlement already known to the player
     * @param relation   required view of the settlement, empty when the player
     *                   has no view
     */
    public void paintNativeSettlement(final GraphicsContext graphics,
            final Point point, final NativeSettlement settlement,
            final Optional<NativeRelationView> relation) {
        final double size = Tile.TILE_WIDTH_IN_PX * NATIVE_SETTLEMENT_SHARE;
        final double left = point.getX() + (Tile.TILE_WIDTH_IN_PX - size) / 2;
        final double top = point.getY() + Tile.TILE_WIDTH_IN_PX * 0.3;
        final Paint previousFill = graphics.getFill();
        final Paint previousStroke = graphics.getStroke();
        final double previousWidth = graphics.getLineWidth();
        if (relation.isPresent()
                && relation.get().getProvokingRival().isPresent()) {
            graphics.setFill(PROVOKED_BACKGROUND);
            graphics.fillRect(point.getX(), point.getY(), Tile.TILE_WIDTH_IN_PX,
                    Tile.TILE_WIDTH_IN_PX);
        }
        graphics.setFill(nativeNationColor(settlement.getNation()));
        graphics.setStroke(MARKER_INK);
        graphics.setLineWidth(1.5);
        switch (settlement.getSettlementClass()) {
        case CAMP:
            final double[] tentX = { left, left + size, left + size / 2 };
            final double[] tentY = { top + size, top + size, top };
            graphics.fillPolygon(tentX, tentY, 3);
            graphics.strokePolygon(tentX, tentY, 3);
            break;
        case VILLAGE:
            final double[] houseX = { left, left + size / 2, left + size,
                    left + size, left };
            final double[] houseY = { top + size * 0.4, top, top + size * 0.4,
                    top + size, top + size };
            graphics.fillPolygon(houseX, houseY, 5);
            graphics.strokePolygon(houseX, houseY, 5);
            break;
        case CITY:
            graphics.fillRect(left, top, size, size);
            graphics.strokeRect(left, top, size, size);
            break;
        default:
            break;
        }
        if (settlement.isCapital()) {
            graphics.setStroke(CAPITAL_FRAME_COLOR);
            graphics.setLineWidth(3);
            graphics.strokeRect(point.getX() + 2, point.getY() + 2,
                    Tile.TILE_WIDTH_IN_PX - 4, Tile.TILE_WIDTH_IN_PX - 4);
        }
        relation.ifPresent(view -> paintAttitude(graphics, point, view));
        graphics.setFill(previousFill);
        graphics.setStroke(previousStroke);
        graphics.setLineWidth(previousWidth);
    }

    /**
     * Attitude badge in the lower right corner: the colour and the shape both
     * name the band (circle content, triangle uneasy, square angry, diamond
     * hostile). Exclamation marks above it show the rate of alarm change.
     */
    private void paintAttitude(final GraphicsContext graphics,
            final Point point, final NativeRelationView view) {
        final double size = ATTITUDE_BADGE_SIZE;
        final double left = point.getX() + Tile.TILE_WIDTH_IN_PX - size - 2;
        final double top = point.getY() + Tile.TILE_WIDTH_IN_PX - size - 2;
        graphics.setFill(attitudeColor(view.getAttitude()));
        graphics.setStroke(MARKER_INK);
        graphics.setLineWidth(1);
        switch (view.getAttitude()) {
        case CONTENT:
            graphics.fillOval(left, top, size, size);
            graphics.strokeOval(left, top, size, size);
            break;
        case UNEASY:
            final double[] triangleX = { left, left + size, left + size / 2 };
            final double[] triangleY = { top + size, top + size, top };
            graphics.fillPolygon(triangleX, triangleY, 3);
            graphics.strokePolygon(triangleX, triangleY, 3);
            break;
        case ANGRY:
            graphics.fillRect(left, top, size, size);
            graphics.strokeRect(left, top, size, size);
            break;
        case HOSTILE:
            final double[] diamondX = { left + size / 2, left + size,
                    left + size / 2, left };
            final double[] diamondY = { top, top + size / 2, top + size,
                    top + size / 2 };
            graphics.fillPolygon(diamondX, diamondY, 4);
            graphics.strokePolygon(diamondX, diamondY, 4);
            break;
        default:
            break;
        }
        final int marks = view.getExclamationMarks();
        if (marks > 0) {
            graphics.setFont(colonyFont);
            graphics.setFill(MARKER_INK);
            graphics.fillText("!".repeat(marks), left, top - 2);
        }
    }

    private static Color attitudeColor(final NativeHostility attitude) {
        switch (attitude) {
        case CONTENT:
            return Color.web("#2E7D32");
        case UNEASY:
            return Color.web("#F9A825");
        case ANGRY:
            return Color.web("#E65100");
        case HOSTILE:
            return Color.web("#B71C1C");
        default:
            throw new IllegalArgumentException(
                    "No colour for attitude " + attitude);
        }
    }

    private static Color nativeNationColor(final NativeNation nation) {
        switch (nation) {
        case ARAWAK:
            return Color.web("#B56576");
        case AZTEC:
            return Color.web("#2A9D8F");
        case INCA:
            return Color.web("#6D597A");
        case TUPI:
            return Color.web("#E76F51");
        case CHEROKEE:
            return Color.web("#588157");
        case IROQUOIS:
            return Color.web("#3D5A80");
        case SIOUX:
            return Color.web("#BC6C25");
        case APACHE:
            return Color.web("#8D99AE");
        default:
            throw new IllegalArgumentException(
                    "No colour for nation " + nation);
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
        if (player.isNative()) {
            // Same nation fill as the settlements of that nation.
            graphics.setFill(nativeNationColor(
                    ((PlayerNative) player).getNativeNation()));
        } else if (player.isHuman()) {
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
        paintTerrainOnTile(graphics, point, location, terrain, isHighlighted,
                gameTick, true);
    }

    private void paintTerrainOnTile(final GraphicsContext graphics,
            final Point point, final Location location, final Terrain terrain,
            final boolean isHighlighted, final long gameTick,
            final boolean animated) {
        paintTerrainBaseOnTile(graphics, point, location, terrain, gameTick,
                animated);
        paintTerrainOverlayOnTile(graphics, point, terrain, isHighlighted);
    }

    /** Paint the map background before rivers and meaningful overlays. */
    public void paintTerrainBaseOnTile(final GraphicsContext graphics,
            final Point point, final Location location, final Terrain terrain,
            final long gameTick) {
        paintTerrainBaseOnTile(graphics, point, location, terrain, gameTick,
                true);
    }

    private void paintTerrainBaseOnTile(final GraphicsContext graphics,
            final Point point, final Location location, final Terrain terrain,
            final long gameTick, final boolean animated) {
        graphics.setFill(
                imageProvider.getTerrainColor(terrain.getTerrainType()));
        graphics.fillRect(point.getX(), point.getY(), Tile.TILE_WIDTH_IN_PX,
                Tile.TILE_WIDTH_IN_PX);
        final Image background = mapManager.getTerrainBackground(location,
                terrain.getTerrainType());
        if (background != null) {
            graphics.drawImage(background, point.getX(), point.getY());
        }
        if (mapManager.isTerrainDecorated(location, terrain, gameTick)) {
            graphics.drawImage(
                    animated ? mapManager.getTerrainDecoration(
                            terrain.getTerrainType(), location, gameTick)
                            : imageProvider.getTerrainDecoration(
                                    terrain.getTerrainType()),
                    point.getX(), point.getY());
        }
        final Image imageCoast = mapManager.getCoatsImageAt(location);
        if (imageCoast != null) {
            graphics.drawImage(imageCoast, point.getX(), point.getY());
        }
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
    }

    /**
     * Resources stay above rivers; movement tint keeps its existing priority.
     */
    public void paintTerrainOverlayOnTile(final GraphicsContext graphics,
            final Point point, final Terrain terrain,
            final boolean isHighlighted) {
        if (terrain.getResourceOverlay() != ResourceOverlay.NONE) {
            paintResource(graphics, point, terrain.getResourceOverlay(),
                    imageProvider);
        }
        if (isHighlighted) {
            graphics.setFill(new Color(0.95, 0.75, 0.90, 0.4F));
            graphics.fillRect(point.getX(), point.getY(), Tile.TILE_WIDTH_IN_PX,
                    Tile.TILE_WIDTH_IN_PX);
        }
    }

    /** Draw the existing rounded fog edge after map overlays. */
    public void paintHiddenCoast(final GraphicsContext graphics,
            final Point point, final Location location) {
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
        paintTerrainOnTile(graphics, point, location, terrain, isHighlighted, 0,
                false);
        paintHiddenCoast(graphics, point, location);
    }

}
