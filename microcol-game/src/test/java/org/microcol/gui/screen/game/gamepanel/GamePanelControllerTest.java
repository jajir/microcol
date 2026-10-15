package org.microcol.gui.screen.game.gamepanel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.event.TurnStartedEvent;
import org.microcol.model.event.UnitMovedStepStartedEvent;
import org.microcol.model.event.UnitMoveFinishedEvent;
import org.microcol.model.event.ColonyWasCapturedEvent;
import com.google.common.eventbus.EventBus;

public class GamePanelControllerTest {
    private GameModelController gateway;
    private Model model;
    private Player human;
    private Player computer;
    private GamePanelController controller;
    private EventBus bus;

    @BeforeEach
    void setUp() {
        gateway = mock(GameModelController.class);
        model = mock(Model.class);
        human = mock(Player.class);
        computer = mock(Player.class);

        when(gateway.isGameModelReady()).thenReturn(true);
        when(gateway.getModel()).thenReturn(model);
        when(model.isGameRunning()).thenReturn(true);
        when(gateway.getRealCurrentPlayer()).thenReturn(human);
        when(human.isHuman()).thenReturn(true);
        // computer.isHuman() defaults false

        bus = new EventBus();
        controller = new GamePanelController(gateway);
        bus.register(controller);
    }

    @Test
    void noLiveModelDoesNotEnableInput() {
        when(gateway.isGameModelReady()).thenReturn(false);
        bus.post(new TurnStartedEvent(model, human, true));
        assertFalse(controller.isMouseEnabled());

        when(gateway.isGameModelReady()).thenReturn(true);
        when(model.isGameRunning()).thenReturn(false);
        bus.post(new TurnStartedEvent(model, human, true));
        assertFalse(controller.isMouseEnabled());
    }

    @Test
    void queriesFollowCurrentModelNotLastEvent() {
        assertTrue(controller.isMouseEnabled());

        when(gateway.getRealCurrentPlayer()).thenReturn(computer);
        assertFalse(controller.isMouseEnabled());

        when(gateway.getRealCurrentPlayer()).thenReturn(human);
        assertTrue(controller.isMouseEnabled());
    }

    @Test
    void delayedNotificationsHandledDeterministically() {
        when(gateway.getRealCurrentPlayer()).thenReturn(human);
        bus.post(new TurnStartedEvent(model, human, true));
        bus.post(new TurnStartedEvent(model, computer, true));
        assertTrue(controller.isMouseEnabled());

        when(gateway.getRealCurrentPlayer()).thenReturn(computer);
        bus.post(new TurnStartedEvent(model, human, true));
        assertFalse(controller.isMouseEnabled());
    }

    @Test
    void retainedAnimationStateRespondsToEvents() {
        assertFalse(controller.isUnitMoving());

        org.microcol.model.Unit unit = mock(org.microcol.model.Unit.class);
        org.microcol.model.Location from = org.microcol.model.Location.of(1, 1);
        org.microcol.model.Location to = org.microcol.model.Location.of(2, 1);

        UnitMovedStepStartedEvent stepStart = new UnitMovedStepStartedEvent(
                model, unit, from, to, org.microcol.model.Direction.east);
        bus.post(stepStart);
        assertTrue(controller.isUnitMoving());

        UnitMoveFinishedEvent finish = new UnitMoveFinishedEvent(model, unit,
                org.microcol.model.Path.of(java.util.List.of(from, to)));
        bus.post(finish);
        assertFalse(controller.isUnitMoving());

        bus.post(stepStart);
        assertTrue(controller.isUnitMoving());

        ColonyWasCapturedEvent capture = new ColonyWasCapturedEvent(model, unit,
                mock(org.microcol.model.Colony.class));
        bus.post(capture);
        assertFalse(controller.isUnitMoving());
    }
}
