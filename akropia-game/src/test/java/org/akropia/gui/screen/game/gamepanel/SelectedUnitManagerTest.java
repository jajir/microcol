package org.akropia.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.i18n.I18n;
import org.akropia.model.Location;
import org.akropia.model.Model;
import org.akropia.model.Unit;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;

import com.google.common.eventbus.EventBus;

class SelectedUnitManagerTest {
    private static final Location FOCUS = Location.of(45, 15);
    private static final Location EMPTY = Location.of(45, 14);

    private final EventBus events = new EventBus();
    private final GameModelController game = mock(GameModelController.class);
    private final SelectedTileManager tiles = new SelectedTileManager(events);
    private final SelectedUnitManager selection = new SelectedUnitManager(game,
            tiles, events, new GamePanelController(game), mock(I18n.class),
            new TradeRoutePlanning(game));
    private Model model;

    @BeforeEach
    void setUp() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/original-world.akropia");
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
    void keepsTheUnitForAdjacentInspectionButClearsItForADistantEmptyTile() {
        tiles.setSelectedTile(FOCUS, ScrollToFocusedTile.skip);
        final Unit selected = selection.getSelectedUnit().orElseThrow();
        final int movement = selected.getMovementThirds();
        tiles.setSelectedTile(EMPTY, ScrollToFocusedTile.skip);
        assertSame(selected, selection.getSelectedUnit().orElseThrow());
        assertEquals(EMPTY, tiles.getSelectedTile().orElseThrow());
        assertEquals(FOCUS, selected.getLocation());
        assertEquals(movement, selected.getMovementThirds());
        tiles.setSelectedTile(Location.of(45, 12), ScrollToFocusedTile.skip);
        assertTrue(selection.getSelectedUnit().isEmpty());
    }

    @Test
    void panelSelectionRequiresReadyTurnAndKeepsTheExactUnit() {
        final Unit ship = model.getUnitById(3);
        final int movement = ship.getMovementThirds();
        selection.selectFromPanel(ship);
        assertTrue(selection.getSelectedUnit().isEmpty());
        when(game.isHumanTurnReady()).thenReturn(true);
        selection.selectFromPanel(ship);
        assertSame(ship, selection.getSelectedUnit().orElseThrow());
        assertEquals(ship.getLocation(), tiles.getSelectedTile().orElseThrow());
        assertEquals(movement, ship.getMovementThirds());
        final Unit foreign = model.getAllUnits().stream()
                .filter(unit -> !unit.getOwner().equals(ship.getOwner()))
                .findFirst().orElseThrow();
        selection.selectFromPanel(foreign);
        assertSame(ship, selection.getSelectedUnit().orElseThrow());
    }

}
