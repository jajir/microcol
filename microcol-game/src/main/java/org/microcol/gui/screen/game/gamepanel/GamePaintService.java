package org.microcol.gui.screen.game.gamepanel;

import static org.microcol.gui.Tile.TILE_WIDTH_IN_PX;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.microcol.gui.Point;
import org.microcol.gui.StepCounter;
import org.microcol.gui.event.model.GameModelController;
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
public final class GamePaintService {

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
     *            required service that helps highlight possible move fo current
     *            turn
     */
    public void paintTerrain(final GraphicsContext graphics, final Area area,
            final OneTurnMoveHighlighter oneTurnMoveHighlighter) {
        final Player player = gameModelController.getCurrentPlayer();
        for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
            for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToPoint(location);
                if (player.isVisible(location)) {
                    final Terrain terrain = gameModelController.getModel().getMap()
                            .getTerrainAt(location);
                    paintService.paintTerrainOnTile(graphics, point, location, terrain,
                            oneTurnMoveHighlighter.isItHighlighted(location));
                } else {
                    final Image imageHidden = imageProvider.getImage(ImageProvider.IMG_TILE_HIDDEN);
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
     */
    public void paintUnits(final GraphicsContext graphics, final Model model, final Area area,
            final ExcludePainting excludePainting) {
        final Map<Location, List<Unit>> ships = model.getUnitsAt();
        final Player player = gameModelController.getCurrentPlayer();

        final Map<Location, List<Unit>> ships2 = ships.entrySet().stream()
                .filter(entry -> area.isLocationVisible(entry.getKey()))
                .filter(entry -> player.isVisible(entry.getKey()))
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        ships2.forEach((location, list) -> {
            final Unit unit = list.stream().findFirst().get();
            final Point point = area.convertToPoint(location);
            if (excludePainting.isUnitIncluded(unit)) {
                paintService.paintUnit(graphics, point, unit);
            }
        });
    }

    public void paintColonies(final GraphicsContext graphics, final Model model, final Area area) {
        final Player player = gameModelController.getCurrentPlayer();
        final Map<Location, Colony> colonies = model.getColoniesAt().entrySet().stream()
                .filter(entry -> area.isLocationVisible(entry.getKey()))
                .filter(entry -> player.isVisible(entry.getKey()))
                .collect(Collectors.toMap(entry -> entry.getKey(), entry -> entry.getValue()));

        colonies.forEach((location, colony) -> {
            final Point point = area.convertToPoint(location);
            paintService.paintColony(graphics, point, colony, true);
        });

    }

    public void paintGrid(final GraphicsContext graphics, final Area area) {
        if (gamePreferences.isGridShown()) {
            graphics.setStroke(Color.LIGHTGREY);
            graphics.setLineWidth(1);
            for (int i = area.getTopLeft().getX(); i <= area.getBottomRight().getX(); i++) {
                final Location l_1 = Location.of(i, area.getTopLeft().getY());
                final Location l_2 = Location.of(i, area.getBottomRight().getY() + 1);
                drawNetLine(graphics, area, l_1, l_2);
            }
            for (int j = area.getTopLeft().getY(); j <= area.getBottomRight().getY(); j++) {
                final Location l_1 = Location.of(area.getTopLeft().getX(), j);
                final Location l_2 = Location.of(area.getBottomRight().getX() + 1, j);
                drawNetLine(graphics, area, l_1, l_2);
            }
        }
    }

    private void drawNetLine(final GraphicsContext graphics, final Area area, final Location l_1,
            final Location l_2) {
        final Point p_1 = area.convertToPoint(l_1).add(-1, -1);
        final Point p_2 = area.convertToPoint(l_2).add(-1, -1);
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
        final Point p = area.convertToPoint(location);
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
        graphics.drawImage(image, point.getX(), point.getY());
    }

}
