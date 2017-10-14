package org.microcol.gui;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.microcol.gui.util.Text;
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
	public void test_getTerrain_verify_that_correct_key_is_loaded(final @Mocked Text text) throws Exception {
		new Expectations() {{
			text.get("terrain.GRASSLAND.name");result="grassland";times=1;
			text.get("terrain.OCEAN.name");result="ocean";times=1;
		}};
		final LocalizationHelper helper = new LocalizationHelper(text);

		assertEquals("grassland", helper.getTerrainName(TerrainType.GRASSLAND));
		assertEquals("ocean", helper.getTerrainName(TerrainType.OCEAN));
	}

}
