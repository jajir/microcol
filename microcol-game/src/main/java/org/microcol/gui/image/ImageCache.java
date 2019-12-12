package org.microcol.gui.image;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.microcol.gui.util.StreamReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.image.Image;

/**
 * Class responsible for loading images by it's name and caching image
 * instances.
 * <p>
 * Class allows to register images. It speed up first image drawing.
 * </p>
 */
@Singleton
public class ImageCache {

    private static final String BASE_PACKAGE = "images";

    private final Map<String, Image> images = new HashMap<>();

    private final StreamReader streamReader;

    @Inject
    public ImageCache(final StreamReader streamReader) {
        this.streamReader = Preconditions.checkNotNull(streamReader);
    }

    /**
     * Return list of all registered image names.
     * 
     * @return Return list of all image names.
     */
    public List<String> getImageNames() {
        return ImmutableList.copyOf(images.keySet());
    }

    /**
     * Load image for name. If image is not in cache it try to load it from
     * image store.
     * 
     * @param name
     *            required image name
     * @return loaded image
     */
    public Image getImage(final String name) {
        Image img = images.get(name);
        if (img == null) {
            img = getRawImage(name);
            if (img == null) {
                throw new IllegalStateException(String.format("cant't lod image %s", name));
            }
            images.put(name, img);
        }
        return img;
    }

    /**
     * Simplify loading image from resource. Path should look like: <code>
     * org/microcol/images/unit-60x60.gif
     * </code>. Class suppose that all images are in directory <i>images</i>.
     * 
     * @param rawPath
     *            path at classpath where is stored image
     * @return return {@link javafx.scene.image.Image} object
     */
    public Image getRawImage(final String rawPath) {
        final String path = BASE_PACKAGE + "/" + rawPath;
        return new Image(streamReader.openStream(path));
    }

    /**
     * Allow to register new type of image.
     * 
     * @param name
     *            required unique image name
     * @param image
     *            required image object
     */
    public void registerImage(final String name, final Image image) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(image);
        if (images.containsKey(name)) {
            throw new IllegalArgumentException(
                    String.format("Image with name '%s' was already registerd", name));
        } else {
            images.put(name, image);
        }
    }

    /**
     * Allow to register image under new name. Image will be accessible under
     * both names. Image is physically still just one.
     * 
     * @param newImageName
     *            required new image name
     * @param imageName
     *            required old image name
     */
    public void registerImage(final String newImageName, final String imageName) {
        Preconditions.checkNotNull(newImageName);
        final Image image = getImage(imageName);
        registerImage(newImageName, image);
    }

    public boolean containsImage(final String imageName) {
        return images.containsKey(imageName);
    }

}
