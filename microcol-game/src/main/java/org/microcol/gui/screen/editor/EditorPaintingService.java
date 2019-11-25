package org.microcol.gui.screen.editor;

import org.microcol.gui.Point;
import org.microcol.gui.Tile;
import org.microcol.gui.image.ImageLoaderTerrain;
import org.microcol.gui.image.ImageProvider;
import org.microcol.gui.screen.game.gamepanel.Area;
import org.microcol.gui.screen.game.gamepanel.CanvasInMapCoordinates;
import org.microcol.gui.screen.game.gamepanel.VisibleAreaService;
import org.microcol.model.Location;
import org.microcol.model.TerrainType;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

@Singleton
public class EditorPaintingService {

    private final VisibleAreaService visibleAreaService;

    private final ModelService modelService;

    private final ImageProvider imageProvider;

    @Inject
    EditorPaintingService(final ModelService modelService,
            final @Named("editor") VisibleAreaService visibleAreaService,
            final ImageProvider imageProvider) {
        this.modelService = Preconditions.checkNotNull(modelService);
        this.visibleAreaService = Preconditions.checkNotNull(visibleAreaService);
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
    }

    void paint(final GraphicsContext graphics) {
        final Area area = getArea();
        final CanvasInMapCoordinates canvasInMapCoordinates = CanvasInMapCoordinates
                .make(visibleAreaService, modelService.getMapSize());
        paintTerrain(graphics, area, canvasInMapCoordinates);
    }

    private void paintTerrain(final GraphicsContext graphics, final Area area,
            final CanvasInMapCoordinates coord) {
        for (int i = coord.getTopLeft().getX(); i <= coord.getBottomRight().getX(); i++) {
            for (int j = coord.getTopLeft().getY(); j <= coord.getBottomRight().getY(); j++) {
                final Location location = Location.of(i, j);
                final Point point = area.convertToCanvasPoint(location);
                final TerrainType terrainType = getTerrainType(location);
                final Image img = imageProvider.getTerrainImage(terrainType);
                graphics.drawImage(img, 0, 0, Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX,
                        point.getX(), point.getY(), Tile.TILE_WIDTH_IN_PX, Tile.TILE_WIDTH_IN_PX);
                if (modelService.hasTrees(location)) {
                    graphics.drawImage(imageProvider.getImage(ImageLoaderTerrain.IMG_TREE_1),
                            point.getX(), point.getY());
                }
                if (modelService.hasField(location)) {
                    graphics.drawImage(imageProvider.getImage(ImageLoaderTerrain.IMG_FIELD),
                            point.getX(), point.getY());
                }
                if (modelService.hasColony(location)) {
                    graphics.drawImage(imageProvider.getImage(ImageLoaderTerrain.IMG_TILE_TOWN),
                            point.getX(), point.getY());
                }
            }
        }
    }

    private TerrainType getTerrainType(final Location location) {
        final TerrainType out = modelService.getTerrainTypeAt(location);
        if (out == null) {
            return TerrainType.OCEAN;
        }
        return out;
    }

    private Area getArea() {
        return new Area(visibleAreaService);
    }

}
