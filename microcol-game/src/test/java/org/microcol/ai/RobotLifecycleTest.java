package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.microcol.model.Model;
import org.microcol.model.ModelListener;
import org.microcol.model.Player;
import org.microcol.model.Unit;
import org.microcol.model.event.TurnStartedEvent;
import org.mockito.ArgumentCaptor;

class RobotLifecycleTest {
    private final Model model = mock(Model.class);
    private final Player player = mock(Player.class);
    private final Unit first = mock(Unit.class);
    private final Unit second = mock(Unit.class);
    private final AtomicBoolean live = new AtomicBoolean(true);

    private Robot robot() {
        when(model.isGameRunning()).thenAnswer(invocation -> live.get());
        when(model.getCurrentPlayer()).thenReturn(player);
        when(player.getUnits()).thenReturn(List.of(first, second));
        when(first.isAtPlaceLocation()).thenReturn(true);
        when(second.isAtPlaceLocation()).thenReturn(true);
        return new Robot();
    }

    private ModelListener registeredListener() {
        final ArgumentCaptor<ModelListener> listener = ArgumentCaptor
                .forClass(ModelListener.class);
        verify(model).addListener(listener.capture());
        return listener.getValue();
    }

    @Test
    void stoppedRobotRejectsAnAlreadyQueuedTurnCallback() {
        final Robot robot = robot();
        final ModelListener callback = registeredListener();
        robot.stop();
        callback.onTurnStarted(new TurnStartedEvent(model, player, true));
        assertEquals(0, robot.starts);
        assertTrue(robot.moved.isEmpty());
        verify(player, never()).endTurn();
        verify(model).removeListener(callback);
    }

    @Test
    void stopDuringAnActionPreventsLaterUnitsAndEndTurn() {
        final Robot robot = robot();
        robot.afterMove = robot::stop;
        registeredListener()
                .onTurnStarted(new TurnStartedEvent(model, player, true));
        assertEquals(1, robot.starts);
        assertEquals(List.of(first), robot.moved);
        verify(player, never()).endTurn();
    }

    @Test
    void discardedModelPreventsLaterActionsEvenBeforeRobotDisposal() {
        final Robot robot = robot();
        robot.afterMove = () -> live.set(false);
        registeredListener()
                .onTurnStarted(new TurnStartedEvent(model, player, true));
        assertEquals(List.of(first), robot.moved);
        verify(player, never()).endTurn();
    }

    @Test
    void liveTurnStillMovesEveryUnitAndEndsOnce() {
        final Robot robot = robot();
        registeredListener()
                .onTurnStarted(new TurnStartedEvent(model, player, true));
        assertEquals(1, robot.starts);
        assertEquals(List.of(first, second), robot.moved);
        verify(player, times(1)).endTurn();
    }

    private final class Robot extends AbstractRobotPlayer {
        private int starts;
        private final List<Unit> moved = new ArrayList<>();
        private Runnable afterMove = () -> {
        };

        private Robot() {
            super(RobotLifecycleTest.this.model, RobotLifecycleTest.this.player,
                    () -> {
                    });
        }

        @Override
        void turnStarted() {
            starts++;
        }

        @Override
        void moveUnit(final Unit unit) {
            moved.add(unit);
            afterMove.run();
        }
    }
}
