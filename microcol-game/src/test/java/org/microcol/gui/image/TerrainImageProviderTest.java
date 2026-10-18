package org.microcol.gui.image;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.TerrainType;

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

        assertThrows(NullPointerException.class,
                () -> provider.getTerrainImage(null));
    }

    @Test
    void test_every_terrain_type_has_an_image() {
        when(imageCache.getImage(anyString())).thenReturn(image);
        TerrainImageProvider provider = new TerrainImageProvider(imageCache);

        for (final TerrainType type : TerrainType.TERRAINS) {
            assertNotNull(provider.getTerrainImage(type), type.name());
        }
    }

}
