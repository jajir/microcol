package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.microcol.gui.screen.game.gamepanel.AnimationManager;
import org.microcol.model.Model;
import org.microcol.model.ModelListenerAdapter;
import org.microcol.model.Player;
import org.microcol.model.PlayerKing;
import org.microcol.model.RoyalScenarioFixtures;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.event.GameFinishedEvent;
import org.microcol.model.event.TurnStartedEvent;

import com.google.common.eventbus.EventBus;

/**
 * Real royal turns. The Crown's robot is attached to the model and acts when
 * the model starts the Crown's turn, on its own thread, exactly as in a game.
 * The human nation of the campaign only ends its turn; its next turn starts
 * after the Crown has finished, so the event of that start is the
 * synchronization point and nothing waits on time except the failure timeout. A
 * rebellion lost during the Crown's turn ends the game instead, and that
 * game-finished event is the synchronization point of that turn.
 */
final class RoyalTurns implements AutoCloseable {

    private static final long TIMEOUT_SECONDS = 20;

    final Model model;

    final PlayerKing king;

    private final KingRobotPlayer robot;

    /** Human turn starts (a {@link Player}) and game ends (a reason String). */
    private final BlockingQueue<Object> signals = new LinkedBlockingQueue<>();

    /**
     * Load the saved game, attach the robot and wait for the human's turn.
     *
     * @param data required saved game of a campaign with two nations
     */
    RoyalTurns(final GameModelPo data) throws InterruptedException {
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        king = (PlayerKing) model.getPlayerByName(RoyalScenarioFixtures.KING);
        robot = new KingRobotPlayer(model, king,
                new AnimationManager(new EventBus()));
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onTurnStarted(final TurnStartedEvent event) {
                if (event.getPlayer().isHuman()) {
                    signals.add(event.getPlayer());
                }
            }

            @Override
            public void onGameFinished(final GameFinishedEvent event) {
                signals.add(event.getGameOverResult().getGameOverReason());
            }
        });
        model.startGame();
        assertHumanTurn(awaitSignal());
    }

    /**
     * End the human's turn and wait until the Crown has played its whole turn
     * and the human's next turn has started.
     */
    void nextRoyalTurn() throws InterruptedException {
        final String loss = nextRoyalTurnOrLoss();
        assertNull(loss, "The game ended during the Crown's turn: " + loss);
    }

    /**
     * End the human's turn and wait until the Crown has played its turn.
     *
     * @return the reason of the rebellion loss that ended the game during that
     *         turn, or null when the human's next turn started
     */
    String nextRoyalTurnOrLoss() throws InterruptedException {
        model.getPlayerByName(RoyalScenarioFixtures.REBELS).endTurn();
        final Object signal = awaitSignal();
        if (signal instanceof String) {
            return (String) signal;
        }
        assertHumanTurn(signal);
        return null;
    }

    private Object awaitSignal() throws InterruptedException {
        final Object signal = signals.poll(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        assertNotNull(signal,
                "The human's turn did not start and the game did not end; "
                        + "running: " + model.isGameRunning());
        return signal;
    }

    private void assertHumanTurn(final Object signal) {
        assertSame(model.getPlayerByName(RoyalScenarioFixtures.REBELS), signal);
        assertSame(signal, model.getCurrentPlayer());
    }

    @Override
    public void close() {
        robot.stop();
        model.stop();
    }
}
