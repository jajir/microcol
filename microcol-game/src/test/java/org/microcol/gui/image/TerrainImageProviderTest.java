package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import javafx.scene.image.Image;

public class TerrainImageProviderTest {

    private final Image image = mock(Image.class);

    private final ImageCache imageCache = mock(ImageCache.class);

    /**
     * Verify that get image for null terrain type throws null pointer
     * exception.
     * 
     * @throws Exception
     */
    @Test
    void test_getTerrainImage_null() throws Exception {
        when(imageCache.getImage(anyString())).thenReturn(image);
        TerrainImageProvider provider = new TerrainImageProvider(imageCache);

        assertThrows(NullPointerException.class, () -> provider.getTerrainImage(null));
    }

}
