package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.i18n.I18n;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Unit;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.common.eventbus.EventBus;

class SelectedUnitManagerTest {
    private static final Location FOCUS = Location.of(45, 15);
    private static final Location EMPTY = Location.of(45, 14);

    private final EventBus events = new EventBus();
    private final GameModelController game = mock(GameModelController.class);
    private final SelectedTileManager tiles = new SelectedTileManager(events);
    private final SelectedUnitManager selection = new SelectedUnitManager(game,
            tiles, events, new GamePanelController(game), mock(I18n.class));
    private Model model;

    @BeforeEach
    void setUp() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/original-world.microcol");
        data.getModel().getGameManager().setGameStarted(false);
        data.getModel().getGameManager().setCurrentPlayer(null);
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        model.startGame();
        when(game.getModel()).thenReturn(model);
        when(game.isGameModelReady()).thenReturn(true);
        when(game.getHumanPlayer()).thenReturn(model.getPlayerByName("Dutch"));
        events.register(tiles);
        events.register(selection);
    }

    @Test
    void selectsTheHumanShipWhileTheComputerOwnsTheTurn() {
        final Unit ship = model.getUnitById(3);
        final int movement = ship.getMovementThirds();
        assertFalse(model.getCurrentPlayer().isHuman());
        assertTrue(model.getFirstSelectableUnitAt(FOCUS).isEmpty());

        tiles.setSelectedTile(FOCUS, ScrollToFocusedTile.skip);

        assertSame(ship, selection.getSelectedUnit().orElseThrow());
        assertEquals(FOCUS, ship.getLocation());
        assertEquals(movement, ship.getMovementThirds());
        assertFalse(model.getCurrentPlayer().isHuman());
    }

    @Test
    void leavesAnEmptyFocusedTileWithoutASelectedUnit() {
        tiles.setSelectedTile(FOCUS, ScrollToFocusedTile.skip);
        assertTrue(selection.getSelectedUnit().isPresent());

        tiles.setSelectedTile(EMPTY, ScrollToFocusedTile.skip);

        assertTrue(selection.getSelectedUnit().isEmpty());
        assertEquals(EMPTY, tiles.getSelectedTile().orElseThrow());
    }
}
