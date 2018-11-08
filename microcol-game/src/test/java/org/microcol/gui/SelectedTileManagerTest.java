package org.microcol.gui;

import static org.junit.Assert.assertFalse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.gamepanel.SelectedTileManager;
import org.microcol.gui.util.Listener;

import com.google.common.eventbus.EventBus;

import mockit.Mocked;

@Listener
public class SelectedTileManagerTest {

    private SelectedTileManager viewState;

    @Mocked
    private EventBus eventBus;

    @Mocked
    private GameModelController gameModelController;

    @Test
    public void test_getInitialValues() throws Exception {
        assertFalse(viewState.getSelectedTile().isPresent());
    }

    @Before
    public void setUp() {
        viewState = new SelectedTileManager(eventBus);
    }

    @After
    public void tearDown() {
        viewState = null;
    }

}
