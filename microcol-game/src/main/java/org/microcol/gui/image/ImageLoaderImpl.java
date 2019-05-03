package org.microcol.gui.image;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Class loads all images at once. Already loaded images speed up further image
 * showing. Class should be instantiated at then begging of application run.
 */
public class ImageLoaderImpl {

    /**
     * Allows to register image loaders. It's ordered list of loaders.
     */
    private final List<ImageLoader> STARTUP_IMAGE_LOADERS = Lists.newArrayList(
            new ImageLoaderTerrain(), new GrassCoastImageLoader(), new IceCoastImageLoader(),
            new HiddenCoastImageLoader(), new ImageLoaderGoods(), new ImageLoaderBuilding(),
            new ImageLoaderButtons(), new ImageLoaderExtra(), new ImageLoaderUnit());

    @Inject
    ImageLoaderImpl(final ImageCache imageCache) {
        Preconditions.checkNotNull(imageCache);
        STARTUP_IMAGE_LOADERS.forEach(loader -> loader.preload(imageCache));
    }

}
