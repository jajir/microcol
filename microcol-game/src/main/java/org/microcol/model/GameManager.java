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
    /**
     * The single terminal result; null while open or when none was recorded.
     */
    private volatile GameOverResult gameOverResult;
    private volatile Player currentPlayer;

    private volatile boolean awaitingImmigrationChoice;

    private volatile boolean awaitingTaxDecision;

    GameManager(final Model model,
            final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final boolean alreadyStarted, final boolean alreadyFinished,
            final String savedGameOverReason, final Player currentPlayer) {
        gameOverEvaluator = new GameOverEvaluator(gameOverEvaluators);
        this.started = alreadyStarted;
        this.finished = alreadyFinished;
        Preconditions.checkArgument(
                savedGameOverReason == null || alreadyFinished,
                "Game over reason is saved without a finished game");
        this.gameOverResult = savedGameOverReason == null ? null
                : new GameOverResult(savedGameOverReason);
        this.currentPlayer = currentPlayer;
        this.model = Preconditions.checkNotNull(model);
        final List<Player> pending = model.getPlayers().stream()
                .filter(Player::hasFrozenEarnedChoice)
                .collect(java.util.stream.Collectors.toList());
        Preconditions.checkArgument(
                pending.size() <= 1 && (pending.isEmpty() || alreadyStarted
                        && !alreadyFinished && pending.get(0) == currentPlayer),
                "Invalid saved immigration choice boundary");
        pending.forEach(Player::validateFrozenEarnedChoice);
        awaitingImmigrationChoice = !pending.isEmpty();
        final List<Player> taxPending = model.getPlayers().stream()
                .filter(Player::hasPendingTaxDemand)
                .collect(java.util.stream.Collectors.toList());
        // A tax demand only follows a completed immigration phase, so the two
        // saved choices may never overlap; without a tax demand there is
        // nothing to reject here.
        Preconditions.checkArgument(
                taxPending.isEmpty()
                        || taxPending.size() == 1 && !awaitingImmigrationChoice
                                && alreadyStarted && !alreadyFinished
                                && taxPending.get(0) == currentPlayer,
                "Invalid saved tax decision boundary");
        taxPending.forEach(Player::validateFrozenTaxDemand);
        awaitingTaxDecision = !taxPending.isEmpty();
    }

    public static GameManager make(final Model model, final ModelPo modelPo,
            final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final PlayerStore playerStore) {
        return new GameManager(model, gameOverEvaluators,
                modelPo.getGameManager().isGameStarted(),
                modelPo.getGameManager().isGameFinished(),
                modelPo.getGameManager().getGameOverReason(),
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
        players.sort(Comparator.comparingInt(GameManager::nativeRank)
                .thenComparing(Player::isKing)
                .thenComparingInt(player -> nationRank(nationKey(player)))
                .thenComparing(GameManager::nationKey)
                .thenComparing(Player::getName));
        return players;
    }

    /**
     * Native nations take their turns first, in their stable enum order and
     * independently of serialized order; every European actor follows them.
     */
    private static int nativeRank(final Player player) {
        return player.isNative()
                ? ((PlayerNative) player).getNativeNation().ordinal()
                : NativeNation.values().length;
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
        return started && !stopped && !isFinished() && !preparing
                && !awaitingImmigrationChoice && !awaitingTaxDecision;
    }

    boolean isAwaitingImmigrationChoice() {
        return started && !stopped && !isFinished() && awaitingImmigrationChoice
                && !preparing;
    }

    boolean isAwaitingTaxDecision() {
        return started && !stopped && !isFinished() && awaitingTaxDecision
                && !preparing;
    }

    void checkTurnPreparationComplete() {
        if (preparing || awaitingImmigrationChoice || awaitingTaxDecision) {
            throw new TurnPreparationException();
        }
    }

    void checkSaveBoundary() {
        if (preparing) {
            throw new TurnPreparationException();
        }
    }

    void stop() {
        stopped = true;
    }

    /**
     * @return the recorded terminal result, empty while the game is open
     */
    Optional<GameOverResult> getGameOverResult() {
        return Optional.ofNullable(gameOverResult);
    }

    /**
     * @return whether the game is started, not stopped and not yet finished, so
     *         a declared rebellion may still end it (R-0192)
     */
    boolean isOpen() {
        return started && !stopped && !finished;
    }

    /**
     * Record a rebellion loss or victory found at a completed boundary (R-0192,
     * R-0191). The result is recorded once and announced once; the caller has
     * already confirmed that the game is open.
     *
     * @param result required rebellion result
     */
    void finishByRebellion(final GameOverResult result) {
        Preconditions.checkState(isOpen(), "Only an open game can end");
        finish(result);
    }

    private void finish(final GameOverResult result) {
        gameOverResult = result;
        finished = true;
        model.fireGameFinished(result);
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
        final boolean completed = startTurnOf(currentPlayer);
        model.getStatistics().countNextTurn(model);
        preparing = false;
        announcePreparation(completed);
    }

    /**
     * When game is loaded from file this should start it.
     */
    void continueGame() {
        Preconditions.checkState(
                isRunning() || isAwaitingImmigrationChoice()
                        || isAwaitingTaxDecision(),
                "Only an active game can continue.");
        Preconditions.checkNotNull(currentPlayer, "Current player is null.");
        checkCurrentPlayer(currentPlayer);
        preparing = true;
        model.fireGameStarted();
        model.fireRoundStarted();
        preparing = false;
        if (awaitingImmigrationChoice) {
            model.fireImmigrationChoiceRequired(currentPlayer);
        } else if (awaitingTaxDecision) {
            model.fireTaxDemandRequired(currentPlayer);
        } else {
            model.fireTurnStarted(currentPlayer, false);
            announceMercenaryOffer();
        }
    }

    /**
     * A human owner whose completed update left a mercenary offer must answer
     * it (R-0169). The offer stays in the model, so a loaded game asks again.
     */
    private void announceMercenaryOffer() {
        if (currentPlayer.isHuman()
                && currentPlayer.getMercenaryOffer().isPresent()) {
            model.fireMercenaryOfferRequired(currentPlayer);
        }
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
        // An unanswered mercenary offer expires with its turn on every route
        // that ends it, so a later update can open a new one (R-0169).
        currentPlayer.clearMercenaryOffer();
        if (index < players.size() - 1) {
            currentPlayer = players.get(index + 1);
            final boolean completed = startTurnOf(currentPlayer);
            preparing = false;
            announcePreparation(completed);
        } else {
            model.getCalendar().endRound();
            // Once per complete round, before the next player's preparation;
            // start, continue and load never reach this boundary.
            model.processCrownWar();
            // The round is complete. A declared rebellion is judged before
            // every ordinary condition, so only one terminal result is ever
            // recorded (R-0192); either end leaves no preparation pending.
            preparing = false;
            model.judgeRebellions();
            final Optional<GameOverResult> oGameOverResult = finished
                    ? Optional.empty()
                    : gameOverEvaluator.evaluate(model);
            if (finished) {
                return;
            } else if (oGameOverResult.isPresent()) {
                finish(oGameOverResult.get());
            } else {
                preparing = true;
                currentPlayer = players.get(0);
                model.getStatistics().countNextTurn(model);
                model.fireRoundStarted();
                final boolean completed = startTurnOf(currentPlayer);
                preparing = false;
                announcePreparation(completed);
            }
        }
    }

    /**
     * Begin the player's own turn preparation. The player's coastal battery
     * shots from earlier rounds expire here, and no other owner's shots do
     * (R-0177). Continue and load never begin a preparation.
     */
    private boolean startTurnOf(final Player player) {
        model.renewCoastalBatteryShots(player);
        return player.startTurn();
    }

    private void announcePreparation(final boolean completed) {
        // A completed update is judged as a whole, after its last step and
        // before anything of the new turn (R-0192). A paused preparation is
        // not complete; it is judged when the player's choice completes it, so
        // a finished game never keeps a pending choice.
        if (completed && endsWithRebellionResult()) {
            return;
        }
        awaitingTaxDecision = !completed && currentPlayer.hasPendingTaxDemand();
        awaitingImmigrationChoice = !completed && !awaitingTaxDecision;
        if (completed) {
            advanceGoToOrders(currentPlayer);
            // A standing order's walk can itself end the rebellion.
            if (finished) {
                return;
            }
            model.fireTurnStarted(currentPlayer, true);
            announceMercenaryOffer();
        } else if (awaitingTaxDecision) {
            model.fireTaxDemandRequired(currentPlayer);
        } else {
            model.fireImmigrationChoiceRequired(currentPlayer);
        }
    }

    void beginImmigrationChoice(final Player player) {
        Preconditions.checkState(
                isAwaitingImmigrationChoice() && currentPlayer == player,
                "Immigration choice is not current");
        preparing = true;
    }

    void continueImmigrationChoice() {
        Preconditions.checkState(preparing && awaitingImmigrationChoice,
                "Immigration choice is not in progress");
        preparing = false;
        model.fireImmigrationChoiceRequired(currentPlayer);
    }

    void finishImmigrationChoice(final Player player) {
        Preconditions.checkState(preparing && awaitingImmigrationChoice
                && currentPlayer == player && !player.hasFrozenEarnedChoice(),
                "Immigration choice is not complete");
        final boolean completed = player.continueAfterImmigration();
        preparing = false;
        awaitingImmigrationChoice = false;
        announcePreparation(completed);
    }

    void beginTaxChoice(final Player player) {
        Preconditions.checkState(
                isAwaitingTaxDecision() && currentPlayer == player,
                "Tax decision is not current");
        preparing = true;
    }

    void finishTaxChoice(final Player player) {
        Preconditions.checkState(
                preparing && awaitingTaxDecision && currentPlayer == player
                        && !player.hasPendingTaxDemand(),
                "Tax decision is not complete");
        player.finishTurnPreparation();
        preparing = false;
        awaitingTaxDecision = false;
        if (endsWithRebellionResult()) {
            return;
        }
        advanceGoToOrders(player);
        if (finished) {
            return;
        }
        model.fireTurnStarted(player, true);
    }

    /**
     * Judge the rebellions at the end of a completed update, with no
     * preparation pending (R-0192, R-0191).
     *
     * @return whether the game is now finished, so nothing of the new turn may
     *         be announced
     */
    private boolean endsWithRebellionResult() {
        model.judgeRebellions();
        return finished;
    }

    /**
     * Continue the go-to orders and trade route assignments of the player whose
     * owner turn has just begun. It runs once per turn after preparation is
     * complete, so each step is an ordinary move; start, continue and load
     * never reach this boundary. A unit has at most one of the two orders.
     */
    private void advanceGoToOrders(final Player player) {
        for (final Unit unit : new ArrayList<>(player.getAllUnits())) {
            ((AbstractUnit) unit).advanceGoTo();
        }
        model.advanceTradeRouteAssignments(player);
    }

    void save(final ModelPo modelPo) {
        modelPo.getGameManager().setGameStarted(isStarted());
        modelPo.getGameManager().setGameFinished(isFinished());
        modelPo.getGameManager().setGameOverReason(gameOverResult == null ? null
                : gameOverResult.getGameOverReason());
        modelPo.getGameManager().setCurrentPlayer(getCurrentPlayer().getName());
    }

}
