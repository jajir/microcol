package org.microcol.gui.screen.game.gamepanel;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.Tile;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.game.gamepanel.MoveModeSupport.MoveMode;
import org.microcol.gui.util.PaintService;
import org.microcol.gui.util.RiverShape;
import org.microcol.model.ColonySighting;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.NativeSettlement;
import org.microcol.model.Player;
import org.microcol.model.RiverSize;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.WorldMap;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Service that draw parts of MicroCol main view. Class should not know about
 * UI.
 */
@Singleton
final class GamePaintService {

    /** Opacity of colonies remembered from an earlier observation. */
    private final static double STALE_OPACITY = 0.55;

    private final static Point STEP_ICON_SIXE = Point.of(25, 25);

    private final static Point STEP_ICON_POSITION = Tile.TILE_SIZE
            .substract(STEP_ICON_SIXE).divide(2);

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final GamePreferences gamePreferences;

    private final PaintService paintService;

    @Inject
    public GamePaintService(final GameModelController gameModelController,
            final ImageProvider imageProvider, final PaintService paintService,
            final GamePreferences gamePreferences) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    /**
     * Draw main game tiles.
     *
     * @param graphics required {@link GraphicsContext}
     * @param area     required visible area description
     * @param coord    required object holding information about canvas in world
     *                 map coordinates (Location)
     * @param gameTick required long representing how many game ticks was
     *                 already done. It allows to time animations.
     */
    public void paintTerrain(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord, final long gameTick) {
        final Player player = gameModelController.getHumanPlayer();
        for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight()
                .getX(); i++) {
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight()
                    .getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToCanvasPoint(location);
                if (player.isVisible(location)) {
                    paintService.paintTerrainBaseOnTile(
                            graphics, point, location, gameModelController
                                    .getModel().getMap().getTerrainAt(location),
                            gameTick);
                } else {
                    paintService.paintHiddenTile(graphics, point);
                }
            }
        }
    }

    /** Draw resource markers and current-sight rumors after every river. */
    public void paintTerrainOverlays(final GraphicsContext graphics,
            final Area area,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter,
            final CanvasInMapCoordinates coord) {
        final Player player = gameModelController.getHumanPlayer();
        final WorldMap map = gameModelController.getModel().getMap();
        for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight()
                .getX(); i++) {
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight()
                    .getY(); j++) {
                final Location location = Location.of(i, j);
                if (player.isVisible(location)) {
                    final Point point = area.convertToCanvasPoint(location);
                    paintService.paintTerrainOverlayOnTile(graphics, point,
                            map.getTerrainAt(location),
                            oneTurnMoveHighlighter.isItHighlighted(location));
                    if (player.isCurrentlyVisible(location)
                            && map.hasLostCityRumorAt(location)) {
                        paintService.paintLostCityRumor(graphics, point);
                    }
                }
            }
        }
    }

    /**
     * Draw river courses over known terrain. A course is shown only toward a
     * neighbour the player also knows, so unseen squares never reveal a river.
     *
     * @param graphics required {@link GraphicsContext}
     * @param area     required visible area description
     * @param coord    required object holding information about canvas in world
     *                 map coordinates (Location)
     */
    public void paintRivers(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord) {
        final Player player = gameModelController.getHumanPlayer();
        final WorldMap map = gameModelController.getModel().getMap();
        final Map<Location, RiverShape> courses = new LinkedHashMap<>();
        // Include mouths extending from just outside the viewport.
        for (int i = Math.max(Location.MAP_MIN_X,
                coord.getTopLeft().getX() - 1); i <= Math.min(
                        map.getMaxLocationX(),
                        coord.getBottomRight().getX() + 1); i++) {
            for (int j = Math.max(Location.MAP_MIN_Y,
                    coord.getTopLeft().getY() - 1); j <= Math.min(
                            map.getMaxLocationY(),
                            coord.getBottomRight().getY() + 1); j++) {
                final Location location = Location.of(i, j);
                final RiverSize size = map.getRiverSizeAt(location);
                if (size != RiverSize.NONE && player.isVisible(location)) {
                    final Map<Location, RiverSize> joined = map
                            .getRiverLinksAt(location).stream()
                            .filter(player::isVisible)
                            .collect(Collectors.toMap(neighbour -> neighbour,
                                    map::getRiverSizeAt));
                    final RiverShape river = new RiverShape(location, size,
                            joined);
                    courses.put(location, river);
                    paintService.paintRiver(graphics,
                            area.convertToCanvasPoint(location), river);
                    if (joined.size() == 1) {
                        final Map<Location, TerrainType> knownTerrain = location
                                .getNeighbors().stream().filter(map::isValid)
                                .filter(player::isVisible).collect(
                                        Collectors.toMap(neighbour -> neighbour,
                                                map::getTerrainTypeAt));
                        final Direction mouth = RiverShape.mouthDirection(
                                location, joined.keySet(), knownTerrain);
                        if (mouth != null) {
                            paintService.paintRiverMouth(graphics,
                                    area.convertToCanvasPoint(location), river,
                                    mouth, map.getTerrainTypeAt(location),
                                    knownTerrain.get(
                                            location.add(mouth.getVector())));
                        }
                    }
                }
            }
        }
        courses.forEach((location, river) -> {
            if (map.getTerrainAt(location).isHasRoad()) {
                paintService.paintRiverBridge(graphics,
                        area.convertToCanvasPoint(location), river);
            }
        });
    }

    /** Cover overlays which can cross from explored into unknown tiles. */
    public void paintFog(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord) {
        final Player player = gameModelController.getHumanPlayer();
        for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight()
                .getX(); i++) {
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight()
                    .getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToCanvasPoint(location);
                if (!player.isVisible(location)) {
                    paintService.paintHiddenTile(graphics, point);
                    paintService.paintUnknownMark(graphics, point, location);
                } else {
                    paintService.paintHiddenCoast(graphics, point, location);
                }
            }
        }
    }

    /**
     * Draw units.
     * <p>
     * Methods iterate through all location with ships, select first ship and
     * draw it.
     * </p>
     *
     * @param graphics        required {@link GraphicsContext}
     * @param model           required game model
     * @param area            visible area description
     * @param excludePainting required service that will provide list of
     *                        excluded locations and units
     * @param oSelectedUnit   required object with {@link Optional} selected
     *                        unit
     */
    public void paintUnits(final GraphicsContext graphics, final Model model,
            final Area area, final ExcludePainting excludePainting,
            final Optional<Unit> oSelectedUnit) {
        final Map<Location, List<Unit>> ships = model.getUnitsAt();
        final Player player = gameModelController.getHumanPlayer();

        final Map<Location, List<Unit>> units = ships.entrySet().stream()
                .filter(entry -> area.isLocationVisible(entry.getKey()))
                // Explored terrain shows no foreign unit outside present sight.
                .filter(entry -> player.isCurrentlyVisible(entry.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        units.forEach((location, list) -> paintUnits(graphics,
                area.convertToCanvasPoint(location),
                sortBySelecedUnit(list, oSelectedUnit), excludePainting));
    }

    /**
     * Paint list of units at specific point at canvas. Method count with:
     * <ul>
     * <li>Selected unit should be painted at the to of all units.</li>
     * <li>Moving unit should be in excluded units. So not will be drawn at
     * all.</li>
     * </ul>
     *
     * @param graphics        required graphics object
     * @param point           required point where will be unit painted on
     *                        canvas
     * @param units           required list of units to paint
     * @param excludePainting required object containing units that will be
     *                        skipped in drawing.
     */
    private void paintUnits(final GraphicsContext graphics, final Point point,
            final List<Unit> units, final ExcludePainting excludePainting) {
        final Unit unitToPaint = units.stream().findFirst().get();
        if (excludePainting.isUnitIncluded(unitToPaint)) {
            paintService.paintUnit(graphics, point, unitToPaint);
        } else {
            if (units.size() > 1) {
                paintService.paintUnit(graphics, point, units.get(1));
            }
        }
    }

    /**
     * Sort given list of unit. When selected unit is present and selected unit
     * is in list than will be listed as first element. In other cases rest of
     * list stay unmodified.
     *
     * @param units         required list of units
     * @param oSelectedUnit required {@link Optional} object with selected unit
     * @return unmodifiable list of units.
     */
    private List<Unit> sortBySelecedUnit(final List<Unit> units,
            final Optional<Unit> oSelectedUnit) {
        if (oSelectedUnit.isPresent()) {
            final Unit selectedUnit = oSelectedUnit.get();
            final int pos = units.indexOf(selectedUnit);
            if (pos > 0) {
                final List<Unit> out = new ArrayList<Unit>(units);
                out.remove(pos);
                out.add(0, selectedUnit);
                return Collections.unmodifiableList(out);
            }
        }
        return units;
    }

    public void paintColonies(final GraphicsContext graphics, final Model model,
            final Area area) {
        final Player player = gameModelController.getHumanPlayer();
        // Known colonies are live inside present sight and remembered
        // snapshots outside it; stale ones are drawn faded.
        for (final ColonySighting sighting : player.getKnownColonies()) {
            final Location location = sighting.getLocation();
            if (!area.isLocationVisible(location)
                    || !player.isVisible(location)) {
                continue;
            }
            final boolean stale = !player.isCurrentlyVisible(location);
            final Point point = area.convertToCanvasPoint(location);
            if (stale) {
                graphics.setGlobalAlpha(STALE_OPACITY);
            }
            paintService.paintColony(graphics, point, sighting, true);
            graphics.setGlobalAlpha(1.0);
        }
        // Native settlements appear only in present sight, like foreign units.
        for (final NativeSettlement settlement : model
                .getNativeSettlementsKnownTo(player)) {
            final Location location = settlement.getLocation();
            if (area.isLocationVisible(location)) {
                // Learned identity outside present sight is drawn faded.
                final boolean stale = !player.isCurrentlyVisible(location);
                if (stale) {
                    graphics.setGlobalAlpha(STALE_OPACITY);
                }
                paintService.paintNativeSettlement(graphics,
                        area.convertToCanvasPoint(location), settlement,
                        model.getNativeRelationView(player, settlement));
                graphics.setGlobalAlpha(1.0);
            }
        }
    }

    public void paintGrid(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord) {
        if (!gamePreferences.isGridShown()) {
            return;
        }
        final Player player = gameModelController.getHumanPlayer();
        graphics.save();
        try {
            graphics.setStroke(Color.web("#746E60", 0.55));
            graphics.setLineWidth(1);
            graphics.setLineDashes(4, 5);
            graphics.setLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            for (int x = coord.getTopLeft().getX(); x <= coord.getBottomRight()
                    .getX(); x++) {
                for (int y = coord.getTopLeft().getY(); y <= coord
                        .getBottomRight().getY(); y++) {
                    final Location at = Location.of(x, y);
                    if (!player.isVisible(at)) {
                        continue;
                    }
                    drawNetLine(graphics, area, at, Location.of(x + 1, y));
                    drawNetLine(graphics, area, at, Location.of(x, y + 1));
                    if (!player.isVisible(Location.of(x + 1, y))) {
                        drawNetLine(graphics, area, Location.of(x + 1, y),
                                Location.of(x + 1, y + 1));
                    }
                    if (!player.isVisible(Location.of(x, y + 1))) {
                        drawNetLine(graphics, area, Location.of(x, y + 1),
                                Location.of(x + 1, y + 1));
                    }
                }
            }
        } finally {
            graphics.restore();
        }
    }

    private void drawNetLine(final GraphicsContext graphics, final Area area,
            final Location from, final Location to) {
        final Point a = area.convertToCanvasPoint(from);
        final Point b = area.convertToCanvasPoint(to);
        // A fixed slight bend keeps the pen character stable through scrolling
        // and repainting.
        graphics.strokePolyline(
                new double[] { a.getX(), (a.getX() + b.getX()) / 2.0 + 0.2,
                        b.getX() },
                new double[] { a.getY(), (a.getY() + b.getY()) / 2.0 + 0.2,
                        b.getY() },
                3);
    }

    public void paintSelectedTile(final GraphicsContext graphics,
            final Area area, final Optional<Location> selectedTileLocation) {
        if (selectedTileLocation.isPresent()) {
            graphics.setStroke(Color.GREY);
            graphics.setLineWidth(2);
            paintCursor(graphics, area, selectedTileLocation.get());
        }
    }

    /**
     * Draw highlight of some tile.
     *
     * @param graphics required {@link GraphicsContext}
     * @param area     required displayed area
     * @param location required tiles where to draw cursor
     */
    public void paintCursor(final GraphicsContext graphics, final Area area,
            final Location location) {
        final Point p = area.convertToCanvasPoint(location);
        graphics.strokeLine(p.getX(), p.getY(), p.getX() + TILE_WIDTH_IN_PX,
                p.getY());
        graphics.strokeLine(p.getX(), p.getY(), p.getX(),
                p.getY() + TILE_WIDTH_IN_PX);
        graphics.strokeLine(p.getX() + TILE_WIDTH_IN_PX, p.getY(),
                p.getX() + TILE_WIDTH_IN_PX, p.getY() + TILE_WIDTH_IN_PX);
        graphics.strokeLine(p.getX(), p.getY() + TILE_WIDTH_IN_PX,
                p.getX() + TILE_WIDTH_IN_PX, p.getY() + TILE_WIDTH_IN_PX);
    }

    /**
     * Draw image of steps on tile. Image is part of highlighted path.
     *
     * @param graphics    required {@link GraphicsContext}
     * @param point       required point where to draw image
     * @param stepCounter required step counter
     * @param moveMode    required move mode definition
     * @param costThirds  required entry cost of this step in movement thirds
     */
    public void paintStep(final GraphicsContext graphics, final Point point,
            final StepCounter stepCounter, final MoveMode moveMode,
            final int costThirds) {
        final Image image = imageProvider.getImage(moveMode.getImageForStep(
                stepCounter.canMakeMoveInSameTurn(costThirds)));
        graphics.drawImage(image, point.getX() + STEP_ICON_POSITION.getX(),
                point.getY() + STEP_ICON_POSITION.getY());
    }

}
