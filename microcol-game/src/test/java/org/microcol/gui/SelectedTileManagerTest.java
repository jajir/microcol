package org.microcol.gui;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.panelview.SelectedTileManager;
import org.microcol.gui.panelview.TileWasSelectedController;

import mockit.Mocked;

public class SelectedTileManagerTest {

	private SelectedTileManager viewState;

	@Mocked
	private TileWasSelectedController tileWasSelectedController;
	
	@Mocked
	private GameModelController gameModelController;
	

	@Test
	public void test_getInitialValues() throws Exception {
		assertFalse(viewState.getSelectedTile().isPresent());
	}

	@Before
	public void setUp() {
		viewState = new SelectedTileManager(tileWasSelectedController, gameModelController);
	}

	@After
	public void tearDown() {
		viewState = null;
	}

}
