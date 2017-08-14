package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.panelview.MouseOverTileChangedController;
import org.microcol.gui.panelview.MouseOverTileChangedEvent;
import org.microcol.gui.panelview.ViewState;
import org.microcol.model.Location;

import mockit.Expectations;
import mockit.Mocked;

public class ViewStateTest {

	private ViewState viewState;

	private @Mocked MouseOverTileChangedController motcController;

	@Test
	public void test_getInitialValues() throws Exception {
		assertFalse(viewState.getMouseOverTile().isPresent());
		assertFalse(viewState.getSelectedTile().isPresent());
	}

	@Test
	public void test_setMouseOverTile() throws Exception {
		Location loc = Location.of(10, 23);
		new Expectations() {{
			motcController.fireEvent(new MouseOverTileChangedEvent(loc));
		}};
		viewState.setMouseOverTile(Optional.of(loc));

		assertTrue(viewState.getMouseOverTile().isPresent());
		assertEquals(loc, viewState.getMouseOverTile().get());
	}

	@Test
	public void test_setMouseOverTile_moreThanOnce() throws Exception {
		Location loc = Location.of(10, 23);
		new Expectations() {{
		motcController.fireEvent(new MouseOverTileChangedEvent(loc));
		}};
		viewState.setMouseOverTile(Optional.of(loc));
		viewState.setMouseOverTile(Optional.of(loc));
		viewState.setMouseOverTile(Optional.of(loc));

		assertTrue(viewState.getMouseOverTile().isPresent());
		assertEquals(loc, viewState.getMouseOverTile().get());
	}

	@Test
	public void test_change_value_of_mouseOverTile() throws Exception {
		Location loc1 = Location.of(10, 23);
		Location loc2 = Location.of(3, 7);
		new Expectations() {{
			motcController.fireEvent(new MouseOverTileChangedEvent(loc1));
			motcController.fireEvent(new MouseOverTileChangedEvent(loc2));
		}};
		viewState.setMouseOverTile(Optional.of(loc1));
		viewState.setMouseOverTile(Optional.of(loc2));

		assertTrue(viewState.getMouseOverTile().isPresent());
		assertEquals(loc2, viewState.getMouseOverTile().get());
	}

	@Before
	public void setUp() {
		viewState = new ViewState(motcController);
	}

	@After
	public void tearDown() {
		viewState = null;
	}

}
