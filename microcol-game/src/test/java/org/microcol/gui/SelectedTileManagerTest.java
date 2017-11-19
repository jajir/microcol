package org.microcol.gui;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.panelview.SelectedTileManager;
import org.microcol.gui.panelview.TileWasSelectedController;

import mockit.Mocked;

public class SelectedTileManagerTest {

	private SelectedTileManager viewState;

	@Mocked
	private TileWasSelectedController tileWasSelectedController;

	@Test
	public void test_getInitialValues() throws Exception {
		assertFalse(viewState.getSelectedTile().isPresent());
	}

	@Before
	public void setUp() {
		viewState = new SelectedTileManager(tileWasSelectedController);
	}

	@After
	public void tearDown() {
		viewState = null;
	}

}
