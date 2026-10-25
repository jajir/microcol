package org.microcol.gui.image;

import org.microcol.model.Location;
import org.microcol.model.WorldMap;
import com.google.common.base.Preconditions;
import javafx.scene.image.Image;

/** Seeded variation of the existing forest overlay. */
public final class ImageRandomProvider {
    private final ImageProvider imageProvider;
    private final WorldMapRandoms mapRandoms;

    public ImageRandomProvider(final ImageProvider imageProvider,
            final WorldMap worldMap) {
        this.imageProvider = Preconditions.checkNotNull(imageProvider);
        mapRandoms = new WorldMapRandoms(Preconditions.checkNotNull(worldMap));
    }

    public Image getTreeImage(final Location location) {
        final Integer rnd = mapRandoms.getRandomAt(location);
        return rnd % 3 == 0
                ? imageProvider.getImage(ImageLoaderTerrain.IMG_TREE_2)
                : imageProvider.getImage(ImageLoaderTerrain.IMG_TREE_1);
    }

}
