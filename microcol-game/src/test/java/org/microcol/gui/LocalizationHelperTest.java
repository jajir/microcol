package org.microcol.gui;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.i18n.I18n;
import org.microcol.model.TerrainType;

/**
 * Test LocalizationHelper.
 */
public class LocalizationHelperTest {

    private final I18n i18n = mock(I18n.class);

    @Test
    public void test_getTerrain_verify_that_correct_key_is_loaded() throws Exception {
        when(i18n.get(TerrainTypes.GRASSLAND_name)).thenReturn("grassland");
        when(i18n.get(TerrainTypes.OCEAN_name)).thenReturn("ocean");

        final LocalizationHelper helper = new LocalizationHelper(i18n);

        assertEquals("grassland", helper.getTerrainName(TerrainType.GRASSLAND));
        assertEquals("ocean", helper.getTerrainName(TerrainType.OCEAN));
    }

}
