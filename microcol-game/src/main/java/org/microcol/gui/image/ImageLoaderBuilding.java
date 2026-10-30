package org.microcol.gui.image;

import org.microcol.model.ConstructionType;

/** All building levels have their own transparent colony illustration. */
public final class ImageLoaderBuilding implements ImageLoader {
    @Override
    public void preload(final ImageCache imageCache) {
        for (final ConstructionType type : ConstructionType.ALL) {
            imageCache.registerImage("building_" + type.name(), imageCache
                    .getRawImage("colony-buildings/" + type.name() + ".png"));
        }
    }
}
