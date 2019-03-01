package org.microcol.gui;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.SelectedTileManager;

import com.google.common.eventbus.EventBus;

public class SelectedTileManagerTest {

    private SelectedTileManager viewState;

    private final EventBus eventBus = mock(EventBus.class);

    @Test
    public void test_getInitialValues() throws Exception {
        assertFalse(viewState.getSelectedTile().isPresent());
    }

    @BeforeEach
    public void setUp() {
        viewState = new SelectedTileManager(eventBus);
    }

    @AfterEach
    public void tearDown() {
        viewState = null;
    }

}
