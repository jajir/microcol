package org.microcol.gui.screen.game.gamepanel;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.ArrayList;
import java.util.Collections;
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
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.Terrain;
import org.microcol.model.Unit;

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

    private final static Point STEP_ICON_SIXE = Point.of(25, 25);

    private final static Point STEP_ICON_POSITION = Tile.TILE_SIZE.substract(STEP_ICON_SIXE)
            .divide(2);

    private final ImageProvider imageProvider;

    private final GameModelController gameModelController;

    private final GamePreferences gamePreferences;

    private final PaintService paintService;

    @Inject
    public GamePaintService(final GameModelController gameModelController,
            final ImageProvider imageProvider, final PaintService paintService,
            final GamePreferences gamePreferences) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.paintService = Preconditions.checkNotNull(paintService);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
    }

    /**
     * Draw main game tiles.
     * 
     * @param graphics
     *            required {@link GraphicsContext}
     * @param area
     *            required visible area description
     * @param oneTurnMoveHighlighter
     *            required service that helps highlight possible move for
     *            current turn
     * @param coord
     *            required object holding information about canvas in world map
     *            coordinates (Location)
     * @param gameTick
     *            required long representing how many game ticks was already
     *            done. It allows to time animations.
     */
    public void paintTerrain(final GraphicsContext graphics, final Area area,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter, final CanvasInMapCoordinates coord,
            final long gameTick) {
        final Player player = gameModelController.getHumanPlayer();
        for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight().getX(); i++) {
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight().getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToCanvasPoint(location);
                if (player.isVisible(location)) {
                    final Terrain terrain = gameModelController.getModel().getMap()
                            .getTerrainAt(location);
                    paintService.paintTerrainOnTile(graphics, point, location, terrain,
                            oneTurnMoveHighlighter.isItHighlighted(location), gameTick);
                } else {
                    final Image imageHidden = imageProvider
                            .getImage(ImageLoaderTerrain.IMG_TILE_HIDDEN);
                    graphics.drawImage(imageHidden, 0, 0, TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX,
                            point.getX(), point.getY(), TILE_WIDTH_IN_PX, TILE_WIDTH_IN_PX);
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
     * @param graphics
     *            required {@link GraphicsContext}
     * @param model
     *            required game model
     * @param area
     *            visible area description
     * @param excludePainting
     *            required service that will provide list of excluded locations
     *            and units
     * @param oSelectedUnit
     *            required object with {@link Optional} selected unit
     */
    public void paintUnits(final GraphicsContext graphics, final Model model, final Area area,
            final ExcludePainting excludePainting, final Optional<Unit> oSelectedUnit) {
        final Map<Location, List<Unit>> ships = model.getUnitsAt();
        final Player player = gameModelController.getHumanPlayer();

        final Map<Location, List<Unit>> units = ships.entrySet().stream()
                .filter(entry -> area.isLocationVisible(entry.getKey()))
                .filter(entry -> player.isVisible(entry.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        units.forEach((location, list) -> paintUnits(graphics, area.convertToCanvasPoint(location),
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
     * @param graphics
     *            required graphics object
     * @param point
     *            required point where will be unit painted on canvas
     * @param units
     *            required list of units to paint
     * @param excludePainting
     *            required object containing units that will be skipped in
     *            drawing.
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
     * @param units
     *            required list of units
     * @param oSelectedUnit
     *            required {@link Optional} object with selected unit
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

    public void paintColonies(final GraphicsContext graphics, final Model model, final Area area) {
        final Player player = gameModelController.getHumanPlayer();
        final Map<Location, Colony> colonies = model.getColoniesAt().entrySet().stream()
                .filter(entry -> area.isLocationVisible(entry.getKey()))
                .filter(entry -> player.isVisible(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        colonies.forEach((location, colony) -> {
            final Point point = area.convertToCanvasPoint(location);
            paintService.paintColony(graphics, point, colony, true);
        });

    }

    public void paintGrid(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord) {
        if (gamePreferences.isGridShown()) {
            graphics.setStroke(Color.LIGHTGREY);
            graphics.setLineWidth(1);
            for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight().getX(); i++) {
                final Location l_1 = Location.of(i, coord.getTopLeft().getY());
                final Location l_2 = Location.of(i, coord.getBottomRight().getY() + 1);
                drawNetLine(graphics, area, l_1, l_2);
            }
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight().getY(); j++) {
                final Location l_1 = Location.of(coord.getTopLeft().getX(), j);
                final Location l_2 = Location.of(coord.getBottomRight().getX() + 1, j);
                drawNetLine(graphics, area, l_1, l_2);
            }
        }
    }

    private void drawNetLine(final GraphicsContext graphics, final Area area, final Location l_1,
            final Location l_2) {
        final Point p_1 = area.convertToCanvasPoint(l_1).add(-1, -1);
        final Point p_2 = area.convertToCanvasPoint(l_2).add(-1, -1);
        graphics.strokeLine(p_1.getX(), p_1.getY(), p_2.getX(), p_2.getY());
    }

    public void paintSelectedTile(final GraphicsContext graphics, final Area area,
            final Optional<Location> selectedTileLocation) {
        if (selectedTileLocation.isPresent()) {
            graphics.setStroke(Color.GREY);
            graphics.setLineWidth(2);
            paintCursor(graphics, area, selectedTileLocation.get());
        }
    }

    /**
     * Draw highlight of some tile.
     * 
     * @param graphics
     *            required {@link GraphicsContext}
     * @param area
     *            required displayed area
     * @param location
     *            required tiles where to draw cursor
     */
    public void paintCursor(final GraphicsContext graphics, final Area area,
            final Location location) {
        final Point p = area.convertToCanvasPoint(location);
        graphics.strokeLine(p.getX(), p.getY(), p.getX() + TILE_WIDTH_IN_PX, p.getY());
        graphics.strokeLine(p.getX(), p.getY(), p.getX(), p.getY() + TILE_WIDTH_IN_PX);
        graphics.strokeLine(p.getX() + TILE_WIDTH_IN_PX, p.getY(), p.getX() + TILE_WIDTH_IN_PX,
                p.getY() + TILE_WIDTH_IN_PX);
        graphics.strokeLine(p.getX(), p.getY() + TILE_WIDTH_IN_PX, p.getX() + TILE_WIDTH_IN_PX,
                p.getY() + TILE_WIDTH_IN_PX);
    }

    /**
     * Draw image of steps on tile. Image is part of highlighted path.
     * 
     * @param graphics
     *            required {@link GraphicsContext}
     * @param point
     *            required point where to draw image
     * @param stepCounter
     *            required step counter
     * @param moveMode
     *            required move mode definition
     */
    public void paintStep(final GraphicsContext graphics, final Point point,
            final StepCounter stepCounter, final MoveMode moveMode) {
        final Image image = imageProvider
                .getImage(moveMode.getImageForStep(stepCounter.canMakeMoveInSameTurn(1)));
        graphics.drawImage(image, point.getX() + STEP_ICON_POSITION.getX(),
                point.getY() + STEP_ICON_POSITION.getY());
    }

}
