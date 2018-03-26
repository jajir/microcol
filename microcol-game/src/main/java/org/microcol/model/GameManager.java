package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.Preconditions;

class GameManager {

    private final GameOverEvaluator gameOverEvaluator;
    private final Model model;
    private boolean started;
    private Player currentPlayer;

    GameManager(final Model model, final List<Function<Model, GameOverResult>> gameOverEvaluators) {
        gameOverEvaluator = new GameOverEvaluator(gameOverEvaluators);
        this.model = Preconditions.checkNotNull(model);
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
        model.fireTurnStarted(currentPlayer);
    }

    void endTurn() {
        checkGameRunning();
        // TODO JJ ordering of players should be human, AI, Natives
        final int index = model.getPlayers().indexOf(currentPlayer);
        if (index < model.getPlayers().size() - 1) {
            currentPlayer = model.getPlayers().get(index + 1);
            currentPlayer.startTurn();
            model.fireTurnStarted(currentPlayer);
        } else {
            model.getCalendar().endRound();
            final Optional<GameOverResult> oGameOverResult = gameOverEvaluator.evaluate(model);
            if (oGameOverResult.isPresent()) {
                model.fireGameFinished(oGameOverResult.get());
            } else {
                currentPlayer = model.getPlayers().get(0);
                model.fireRoundStarted();
                currentPlayer.startTurn();
                model.fireTurnStarted(currentPlayer);
            }
        }
    }

}
