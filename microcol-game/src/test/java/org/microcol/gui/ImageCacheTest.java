package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.microcol.gui.image.ImageCache;

import javafx.scene.image.Image;

public class ImageCacheTest {

    @Test
    public void test_existingImage() throws Exception {
        ImageCache imageProvider = new ImageCache();
        Image i1 = imageProvider.getImage("cursor-goto.png");
        assertNotNull(i1);
    }

    @Test
    public void test_two_getImage_returns_same_image_instance() throws Exception {
        ImageCache imageProvider = new ImageCache();
        Image i1 = imageProvider.getImage("cursor-goto.png");
        Image i2 = imageProvider.getImage("cursor-goto.png");
        assertNotNull(i1);
        assertSame(i1, i2);
    }

    @Test
    public void test_load_non_existing_image() throws Exception {
        ImageCache imageProvider = new ImageCache();

        assertThrows(MicroColException.class, () -> {
            imageProvider.getImage("doesn't exists.png");
        });
    }

}
