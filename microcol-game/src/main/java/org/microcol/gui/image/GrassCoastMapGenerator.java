package org.microcol.gui.image;

import org.microcol.model.TerrainType;
import org.microcol.model.WorldMap;
import org.microcol.model.Location;
import org.microcol.gui.Tile;

import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import com.google.inject.Inject;

public final class GrassCoastMapGenerator extends AbstractCoastMapGenerator {

    private final ImageProvider images;

    @Inject
    GrassCoastMapGenerator(final ImageProvider imageProvider) {
        super(imageProvider);
        images = imageProvider;
    }

    @Override
    public String getPrefix() {
        return "grass-";
    }

    @Override
    public boolean isVoid(final InfoHolder infoHolder) {
        return infoHolder.tt().isSea();
    }

    @Override
    public boolean isMass(final InfoHolder infoHolder) {
        return infoHolder.tt().isLand();
    }

    @Override
    public boolean skipp(final InfoHolder infoHolder) {
        return false;
    }

    @Override
    protected Image prepareImage(final Image image, final WorldMap world,
            final Location location) {
        final int width = Tile.TILE_WIDTH_IN_PX;
        final TerrainMapUtil terrains = new TerrainMapUtil(world);
        final Color[][] neighbours = new Color[3][3];
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                final TerrainType type = terrains.getTerrainTypeAt(Location
                        .of(location.getX() + dx, location.getY() + dy));
                if (type.isLand()) {
                    neighbours[dx + 1][dy + 1] = images.getTerrainColor(type);
                }
            }
        }
        final WritableImage result = new WritableImage(width, width);
        final Color original = Color.web("#c5c18a");
        for (int y = 0; y < width; y++) {
            for (int x = 0; x < width; x++) {
                final Color pixel = image.getPixelReader().getColor(x, y);
                if (pixel.getOpacity() == 0) {
                    continue;
                }
                double distance = Double.MAX_VALUE;
                Color land = original;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (neighbours[dx + 1][dy + 1] == null) {
                            continue;
                        }
                        // Distance to the square keeps shared edges continuous.
                        final int px = Math.max(dx * width - x,
                                Math.max(0, x - ((dx + 1) * width - 1)));
                        final int py = Math.max(dy * width - y,
                                Math.max(0, y - ((dy + 1) * width - 1)));
                        final double candidate = px * px + py * py;
                        if (candidate < distance) {
                            distance = candidate;
                            land = neighbours[dx + 1][dy + 1];
                        }
                    }
                }
                result.getPixelWriter().setColor(x, y,
                        new Color(
                                Math.min(1,
                                        pixel.getRed() * land.getRed()
                                                / original.getRed()),
                                Math.min(1,
                                        pixel.getGreen() * land.getGreen()
                                                / original.getGreen()),
                                Math.min(1,
                                        pixel.getBlue() * land.getBlue()
                                                / original.getBlue()),
                                pixel.getOpacity()));
            }
        }
        return result;
    }

}
