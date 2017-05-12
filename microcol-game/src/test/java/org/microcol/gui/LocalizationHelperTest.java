package org.microcol.gui;

import static org.junit.Assert.assertEquals;

import org.easymock.EasyMock;
import org.junit.Test;
import org.microcol.gui.util.Text;
import org.microcol.model.Terrain;

/**
 * Test LocalizationHelper.
 */
public class LocalizationHelperTest {

	@Test
	public void test_getTerrain_verify_that_correct_key_is_loaded() throws Exception {
		final Text text = EasyMock.createMock(Text.class);
		EasyMock.expect(text.get("terrain.CONTINENT.name")).andReturn("continent");
		EasyMock.expect(text.get("terrain.OCEAN.name")).andReturn("ocean");
		EasyMock.replay(text);
		final LocalizationHelper helper = new LocalizationHelper(text);

		assertEquals("continent", helper.getTerrainName(Terrain.CONTINENT));
		assertEquals("ocean", helper.getTerrainName(Terrain.OCEAN));

		EasyMock.verify(text);
	}

}
