package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.store.ModelPo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

/**
 * Each player performs turns. When all players perform one turn it's one round.
 */
final class GameManager {

    private final static Logger LOGGER = LoggerFactory.getLogger(GameManager.class);

    private final GameOverEvaluator gameOverEvaluator;
    private final Model model;
    private boolean started;
    private Player currentPlayer;

    GameManager(final Model model, final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final boolean alreadyStarted, final Player currentPlayer) {
        gameOverEvaluator = new GameOverEvaluator(gameOverEvaluators);
        this.started = alreadyStarted;
        this.currentPlayer = currentPlayer;
        this.model = Preconditions.checkNotNull(model);
    }

    public static GameManager make(final Model model, final ModelPo modelPo,
            final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final PlayerStore playerStore) {
        return new GameManager(model, gameOverEvaluators, modelPo.getGameManager().isGameStarted(),
                findPlayer(modelPo, playerStore));
    }

    private static Player findPlayer(final ModelPo modelPo, final PlayerStore playerStore) {
        final String playerName = modelPo.getGameManager().getCurrentPlayer();
        if (Strings.isNullOrEmpty(playerName)) {
            return null;
        } else {
            return playerStore.getPlayerByName(playerName);
        }
    }

    boolean isStarted() {
        return started;
    }

    boolean isRunning() {
        return started && !isFinished();
    }

    boolean isFinished() {
        return model.getCalendar().isFinished();
    }

    void checkGameRunning() {
        if (!isRunning()) {
            throw new IllegalStateException("Game must be running.");
        }
    }

    void checkCurrentPlayer(final Player player) {
        Preconditions.checkNotNull(player);

        if (!player.equals(currentPlayer)) {
            throw new IllegalStateException(String
                    .format("This player (%s) is not current player (%s).", player, currentPlayer));
        }
    }

    Player getCurrentPlayer() {
        Preconditions.checkState(started, "Game must be started.");

        return currentPlayer;
    }

    void startGame() {
        Preconditions.checkState(!started, "Game was already started.");
        started = true;
        currentPlayer = model.getPlayers().get(0);
        model.fireGameStarted();
        model.fireRoundStarted();
        currentPlayer.startTurn();
        model.fireTurnStarted(currentPlayer, true);
        model.getStatistics().countNextTurn(model);

    }

    /**
     * When game is loaded from file this should start it.
     */
    void continueGame() {
        Preconditions.checkState(started, "Game can't continue because wasn't started.");
        Preconditions.checkNotNull(currentPlayer, "Current player is null.");
        model.fireGameStarted();
        model.fireRoundStarted();
        model.fireTurnStarted(currentPlayer, false);
    }

    /**
     * Finish current players turn and start of next player. Ordering of players
     * should be:
     * <ul>
     * <li>human</li>
     * <li>AI</li>
     * <li>Natives</li>
     * </ul>
     */
    void endTurn() {
        checkGameRunning();
        LOGGER.debug("End turn was called, current player is {}", currentPlayer);
        model.fireTurnFinished(currentPlayer);
        model.getTurnEventStore().clearTurnEventsForPlayer(currentPlayer);
        final int index = model.getPlayers().indexOf(currentPlayer);
        if (index < model.getPlayers().size() - 1) {
            currentPlayer = model.getPlayers().get(index + 1);
            currentPlayer.startTurn();
            model.fireTurnStarted(currentPlayer, true);
        } else {
            model.getCalendar().endRound();
            final Optional<GameOverResult> oGameOverResult = gameOverEvaluator.evaluate(model);
            if (oGameOverResult.isPresent()) {
                model.fireGameFinished(oGameOverResult.get());
                started = false;
            } else {
                currentPlayer = model.getPlayers().get(0);
                model.getStatistics().countNextTurn(model);
                model.fireRoundStarted();
                currentPlayer.startTurn();
                model.fireTurnStarted(currentPlayer, true);
            }
        }
    }

    void save(final ModelPo modelPo) {
        modelPo.getGameManager().setGameStarted(isStarted());
        modelPo.getGameManager().setCurrentPlayer(getCurrentPlayer().getName());
    }

}
