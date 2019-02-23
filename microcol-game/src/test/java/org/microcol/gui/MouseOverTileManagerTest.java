package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.screen.game.gamepanel.MouseOverTileChangedEvent;
import org.microcol.gui.screen.game.gamepanel.MouseOverTileManager;
import org.microcol.model.Location;

import com.google.common.eventbus.EventBus;

import mockit.Expectations;
import mockit.Mocked;

public class MouseOverTileManagerTest {

	private MouseOverTileManager viewState;

	private @Mocked EventBus eventBus;

	@Test
	public void test_getInitialValues() throws Exception {
		assertFalse(viewState.getMouseOverTile().isPresent());
	}

	@Test
	public void test_setMouseOverTile() throws Exception {
		Location loc = Location.of(10, 23);
		new Expectations() {{
			eventBus.post(new MouseOverTileChangedEvent(loc));
		}};
		viewState.setMouseOverTile(loc);

		assertTrue(viewState.getMouseOverTile().isPresent());
		assertEquals(loc, viewState.getMouseOverTile().get());
	}

	@Test
	public void test_setMouseOverTile_moreThanOnce() throws Exception {
		Location loc = Location.of(10, 23);
		new Expectations() {{
		    eventBus.post(new MouseOverTileChangedEvent(loc));
		}};
		viewState.setMouseOverTile(loc);
		viewState.setMouseOverTile(loc);
		viewState.setMouseOverTile(loc);

		assertTrue(viewState.getMouseOverTile().isPresent());
		assertEquals(loc, viewState.getMouseOverTile().get());
	}

	@Test
	public void test_change_value_of_mouseOverTile() throws Exception {
		Location loc1 = Location.of(10, 23);
		Location loc2 = Location.of(3, 7);
		new Expectations() {{
		    eventBus.post(new MouseOverTileChangedEvent(loc1));
		    eventBus.post(new MouseOverTileChangedEvent(loc2));
		}};
		viewState.setMouseOverTile(loc1);
		viewState.setMouseOverTile(loc2);

		assertTrue(viewState.getMouseOverTile().isPresent());
		assertEquals(loc2, viewState.getMouseOverTile().get());
	}

	@Before
	public void setUp() {
		viewState = new MouseOverTileManager(eventBus);
	}

	@After
	public void tearDown() {
		viewState = null;
	}

}
