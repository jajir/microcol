package org.microcol.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.microcol.i18n.I18n;
import org.microcol.model.TerrainType;

import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

/**
 * Test LocalizationHelper.
 */
@RunWith(JMockit.class)
public class LocalizationHelperTest {

	@Test
	public void test_getTerrain_verify_that_correct_key_is_loaded(final @Mocked I18n i18n) throws Exception {
		new Expectations() {{
		        i18n.get(TerrainTypes.GRASSLAND_name);result="grassland";times=1;
		        i18n.get(TerrainTypes.OCEAN_name);result="ocean";times=1;
		}};
		final LocalizationHelper helper = new LocalizationHelper(i18n);

		assertEquals("grassland", helper.getTerrainName(TerrainType.GRASSLAND));
		assertEquals("ocean", helper.getTerrainName(TerrainType.OCEAN));
	}

}
