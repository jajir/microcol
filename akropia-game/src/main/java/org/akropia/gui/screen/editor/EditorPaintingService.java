package org.akropia.gui.screen.editor;

import org.akropia.gui.Point;
import org.akropia.gui.Tile;
import org.akropia.gui.image.ImageLoaderTerrain;
import org.akropia.gui.image.ImageProvider;
import org.akropia.gui.screen.game.gamepanel.Area;
import org.akropia.gui.screen.game.gamepanel.CanvasInMapCoordinates;
import org.akropia.gui.screen.game.gamepanel.VisibleAreaService;
import org.akropia.gui.util.PaintService;
import org.akropia.model.Location;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.TerrainType;
import org.akropia.model.store.NativeSettlementPo;
import org.akropia.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Simplified map preview of the draft: fixed terrain variants, forests, fields,
 * river courses, bonuses, colonies, native settlements, the first unit of each
 * square and the selected square. Code-drawn marks use black contours about two
 * pixels wide with flat fills.
 */
@Singleton
public class EditorPaintingService {

    private final VisibleAreaService visibleAreaService;

    private final ModelService modelService;

    private final ImageProvider imageProvider;

    private final PaintService paintService;

    @Inject
    EditorPaintingService(final ModelService modelService,
            final @Named("editor") VisibleAreaService visibleAreaService,
            final ImageProvider imageProvider,
            final PaintService paintService) {
        this.modelService = Preconditions.checkNotNull(modelService);
        this.visibleAreaService = Preconditions
                .checkNotNull(visibleAreaService);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        this.paintService = Preconditions.checkNotNull(paintService);
    }

    void paint(final GraphicsContext graphics) {
        final Area area = getArea();
        final CanvasInMapCoordinates canvasInMapCoordinates = CanvasInMapCoordinates
                .make(visibleAreaService, modelService.getMapSize());
        paintTerrain(graphics, area, canvasInMapCoordinates);
        // The map's river renderer, drawn with the editor's pen.
        paintService.paintRivers(graphics, modelService.getWorldMap(),
                location -> true, canvasInMapCoordinates.getTopLeft(),
                canvasInMapCoordinates.getBottomRight(),
                area::convertToCanvasPoint, PaintService.PEN);
        paintObjects(graphics, area, canvasInMapCoordinates);
        modelService.getSelected().ifPresent(location -> {
            final Point point = area.convertToCanvasPoint(location);
            graphics.setStroke(PaintService.PEN.getColor());
            graphics.setLineWidth(PaintService.PEN.getWidth());
            graphics.strokeRect(point.getX() + 1, point.getY() + 1,
                    Tile.TILE_WIDTH_IN_PX - 2, Tile.TILE_WIDTH_IN_PX - 2);
        });
    }

    private void paintTerrain(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord) {
        for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight()
                .getX(); i++) {
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight()
                    .getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToCanvasPoint(location);
                final TerrainType terrainType = modelService
                        .getTerrainTypeAt(location);
                final Image img = imageProvider.getTerrainImage(terrainType);
                graphics.drawImage(img, 0, 0, Tile.TILE_WIDTH_IN_PX,
                        Tile.TILE_WIDTH_IN_PX, point.getX(), point.getY(),
                        Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);
                if (modelService.hasTrees(location)) {
                    graphics.drawImage(
                            imageProvider
                                    .getImage(ImageLoaderTerrain.IMG_TREE_1),
                            point.getX(), point.getY());
                }
                if (modelService.hasField(location)) {
                    graphics.drawImage(
                            imageProvider
                                    .getImage(ImageLoaderTerrain.IMG_FIELD),
                            point.getX(), point.getY());
                }
            }
        }
    }

    private void paintObjects(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord) {
        for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight()
                .getX(); i++) {
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight()
                    .getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToCanvasPoint(location);
                final ResourceOverlay resource = modelService
                        .getResourceAt(location);
                if (resource != ResourceOverlay.NONE) {
                    PaintService.paintResource(graphics, point, resource,
                            imageProvider);
                }
                if (modelService.hasColony(location)) {
                    graphics.drawImage(
                            imageProvider
                                    .getImage(ImageLoaderTerrain.IMG_TILE_TOWN),
                            point.getX(), point.getY());
                }
                modelService.getDraft().getSettlementAt(location)
                        .ifPresent(settlement -> paintSettlement(graphics,
                                point, settlement));
                modelService.getDraft().getUnitsAt(location).stream()
                        .findFirst()
                        .ifPresent(unit -> paintUnit(graphics, point, unit));
            }
        }
    }

    private void paintSettlement(final GraphicsContext graphics,
            final Point point, final NativeSettlementPo settlement) {
        paintService.paintNativeSettlement(graphics, point,
                settlement.getNation(),
                settlement.getNation().getSettlementClass(),
                Boolean.TRUE.equals(settlement.getCapital()), PaintService.PEN);
    }

    private void paintUnit(final GraphicsContext graphics, final Point point,
            final UnitPo unit) {
        graphics.drawImage(imageProvider.getUnitImage(unit.getType()),
                point.getX(), point.getY());
    }

    private Area getArea() {
        return new Area(visibleAreaService);
    }

}
