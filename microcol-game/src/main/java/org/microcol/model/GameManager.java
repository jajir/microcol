package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
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

    private final static Logger LOGGER = LoggerFactory
            .getLogger(GameManager.class);

    private final GameOverEvaluator gameOverEvaluator;
    private final Model model;
    private boolean started;
    private volatile boolean preparing;
    private volatile boolean stopped;
    private volatile boolean finished;
    private volatile Player currentPlayer;

    GameManager(final Model model,
            final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final boolean alreadyStarted, final boolean alreadyFinished,
            final Player currentPlayer) {
        gameOverEvaluator = new GameOverEvaluator(gameOverEvaluators);
        this.started = alreadyStarted;
        this.finished = alreadyFinished;
        this.currentPlayer = currentPlayer;
        this.model = Preconditions.checkNotNull(model);
    }

    public static GameManager make(final Model model, final ModelPo modelPo,
            final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final PlayerStore playerStore) {
        return new GameManager(model, gameOverEvaluators,
                modelPo.getGameManager().isGameStarted(),
                modelPo.getGameManager().isGameFinished(),
                findPlayer(modelPo, playerStore));
    }

    private static Player findPlayer(final ModelPo modelPo,
            final PlayerStore playerStore) {
        final String playerName = modelPo.getGameManager().getCurrentPlayer();
        if (Strings.isNullOrEmpty(playerName)) {
            return null;
        } else {
            return playerStore.getPlayerByName(playerName);
        }
    }

    private List<Player> turnOrder() {
        final List<Player> players = new ArrayList<>(model.getPlayers());
        players.sort(Comparator.comparing(Player::isKing)
                .thenComparingInt(player -> nationRank(nationKey(player)))
                .thenComparing(GameManager::nationKey)
                .thenComparing(Player::getName));
        return players;
    }

    private static String nationKey(final Player player) {
        return player.isKing()
                ? ((PlayerKing) player).getWhosKingThisPlayerIs().getName()
                : player.getName();
    }

    private static int nationRank(final String nation) {
        final int index = List.of("England", "France", "Spain", "Dutch")
                .indexOf(nation);
        return index < 0 ? 4 : index;
    }

    boolean isStarted() {
        return started;
    }

    boolean isPreparing() {
        return preparing;
    }

    boolean isRunning() {
        return started && !stopped && !isFinished() && !preparing;
    }

    void checkTurnPreparationComplete() {
        if (preparing) {
            throw new TurnPreparationException();
        }
    }

    void stop() {
        stopped = true;
    }

    boolean isFinished() {
        return finished || model.getCalendar().isFinished();
    }

    void checkGameRunning() {
        if (!isRunning()) {
            throw new IllegalStateException("Game must be running.");
        }
    }

    void checkCurrentPlayer(final Player player) {
        Preconditions.checkNotNull(player);

        if (player != currentPlayer || model.getPlayers().stream()
                .noneMatch(registered -> registered == player)) {
            throw new IllegalStateException(String.format(
                    "This player (%s) is not current player (%s).", player,
                    currentPlayer));
        }
    }

    Player getCurrentPlayer() {
        Preconditions.checkState(started, "Game must be started.");

        return currentPlayer;
    }

    void startGame() {
        Preconditions.checkState(!started && !stopped && !isFinished(),
                "Game was already started or stopped.");
        final List<Player> players = turnOrder();
        Preconditions.checkState(!players.isEmpty(), "No registered players.");
        preparing = true;
        started = true;
        currentPlayer = players.get(0);
        model.fireGameStarted();
        model.fireRoundStarted();
        currentPlayer.startTurn();
        model.getStatistics().countNextTurn(model);
        preparing = false;
        model.fireTurnStarted(currentPlayer, true);
    }

    /**
     * When game is loaded from file this should start it.
     */
    void continueGame() {
        Preconditions.checkState(isRunning(),
                "Only a running game can continue.");
        Preconditions.checkNotNull(currentPlayer, "Current player is null.");
        checkCurrentPlayer(currentPlayer);
        preparing = true;
        model.fireGameStarted();
        model.fireRoundStarted();
        preparing = false;
        model.fireTurnStarted(currentPlayer, false);
    }

    /** Finish one actor and advance through nations, then their Crowns. */
    void endTurn() {
        checkGameRunning();
        checkCurrentPlayer(currentPlayer);
        final List<Player> players = turnOrder();
        final int index = players.indexOf(currentPlayer);
        LOGGER.debug("End turn was called, current player is {}",
                currentPlayer);
        preparing = true;
        model.fireTurnFinished(currentPlayer);
        model.getTurnEventStore().clearTurnEventsForPlayer(currentPlayer);
        if (index < players.size() - 1) {
            currentPlayer = players.get(index + 1);
            currentPlayer.startTurn();
            preparing = false;
            model.fireTurnStarted(currentPlayer, true);
        } else {
            model.getCalendar().endRound();
            final Optional<GameOverResult> oGameOverResult = gameOverEvaluator
                    .evaluate(model);
            if (oGameOverResult.isPresent()) {
                finished = true;
                preparing = false;
                model.fireGameFinished(oGameOverResult.get());
            } else {
                currentPlayer = players.get(0);
                model.getStatistics().countNextTurn(model);
                model.fireRoundStarted();
                currentPlayer.startTurn();
                preparing = false;
                model.fireTurnStarted(currentPlayer, true);
            }
        }
    }

    void save(final ModelPo modelPo) {
        modelPo.getGameManager().setGameStarted(isStarted());
        modelPo.getGameManager().setGameFinished(isFinished());
        modelPo.getGameManager().setCurrentPlayer(getCurrentPlayer().getName());
    }

}
