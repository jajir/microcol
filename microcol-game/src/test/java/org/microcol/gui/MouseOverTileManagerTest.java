package org.microcol.gui;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.microcol.gui.screen.game.gamepanel.MouseOverTileChangedEvent;
import org.microcol.gui.screen.game.gamepanel.MouseOverTileManager;
import org.microcol.model.Location;

import com.google.common.eventbus.EventBus;

public class MouseOverTileManagerTest {

    private MouseOverTileManager viewState;

    private EventBus eventBus = mock(EventBus.class);

    @Test
    public void test_getInitialValues() throws Exception {
        assertFalse(viewState.getMouseOverTile().isPresent());
    }

    @Test
    public void test_setMouseOverTile() throws Exception {
        final Location loc = Location.of(10, 23);
        doNothing().when(eventBus).post(new MouseOverTileChangedEvent(loc));
        viewState.setMouseOverTile(loc);

        assertTrue(viewState.getMouseOverTile().isPresent());
        assertEquals(loc, viewState.getMouseOverTile().get());
    }

    @Test
    public void test_setMouseOverTile_moreThanOnce() throws Exception {
        final Location loc = Location.of(10, 23);

        doNothing().when(eventBus).post(new MouseOverTileChangedEvent(loc));

        viewState.setMouseOverTile(loc);
        viewState.setMouseOverTile(loc);
        viewState.setMouseOverTile(loc);

        assertTrue(viewState.getMouseOverTile().isPresent());
        assertEquals(loc, viewState.getMouseOverTile().get());
    }

    @Test
    public void test_change_value_of_mouseOverTile() throws Exception {
        final Location loc1 = Location.of(10, 23);
        final Location loc2 = Location.of(3, 7);

        doNothing().when(eventBus).post(new MouseOverTileChangedEvent(loc1));
        doNothing().when(eventBus).post(new MouseOverTileChangedEvent(loc2));

        viewState.setMouseOverTile(loc1);
        viewState.setMouseOverTile(loc2);

        assertTrue(viewState.getMouseOverTile().isPresent());
        assertEquals(loc2, viewState.getMouseOverTile().get());
    }

    @BeforeEach
    public void setUp() {
        viewState = new MouseOverTileManager(eventBus);
    }

    @AfterEach
    public void tearDown() {
        viewState = null;
    }

}
