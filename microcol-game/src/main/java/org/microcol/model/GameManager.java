package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.store.GameResultPo;
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
    /**
     * The frozen free-play score (R-0193); null while none is frozen. It is
     * separate from the terminal result above.
     */
    private volatile GameResult frozenResult;
    private volatile Player currentPlayer;

    private volatile boolean awaitingImmigrationChoice;

    private volatile boolean awaitingTaxDecision;

    GameManager(final Model model,
            final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final boolean alreadyStarted, final boolean alreadyFinished,
            final String savedGameOverReason, final Player currentPlayer,
            final GameResult savedFrozenResult) {
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
        frozenResult = savedFrozenResult;
        final boolean awaitingScore = awaitsScoreChoice();
        Preconditions.checkArgument(
                !awaitingScore || alreadyStarted && !alreadyFinished
                        && !awaitingImmigrationChoice && !awaitingTaxDecision
                        && currentPlayer == turnOrder().get(0),
                "Invalid saved score boundary");
        validateScoreHistory(alreadyStarted, alreadyFinished,
                savedGameOverReason);
    }

    /**
     * Check that the saved score history is one the rules can produce (R-0193).
     * A saved game is never repaired or guessed: an impossible combination of
     * calendar, frozen result, rebellion and terminal reason is rejected.
     */
    private void validateScoreHistory(final boolean alreadyStarted,
            final boolean alreadyFinished, final String reason) {
        final Calendar calendar = model.getCalendar();
        final boolean scoredReason = GameOverEvaluator.REASON_FREE_PLAY_ENDED
                .equals(reason)
                || GameOverEvaluator.REASON_REBELLION_UNRESOLVED.equals(reason)
                || GameOverEvaluator.REASON_RETIRED.equals(reason);
        if (!calendar.isScoredPlay()) {
            // A mission keeps its own deadline and keeps no frozen result.
            Preconditions.checkArgument(frozenResult == null && !scoredReason,
                    "Only scored free play keeps a frozen result");
            return;
        }
        Preconditions.checkArgument(reason == null || alreadyFinished,
                "A game over reason needs a finished game");
        final Optional<Player> human = model.getPlayers().stream()
                .filter(player -> player.isHuman() && player.isColonialNation())
                .findFirst();
        final boolean declared = human.isPresent()
                && human.get().isDeclaredIndependence();
        final boolean pastBoundary = calendar
                .hasReachedYear(Calendar.YEAR_OF_SCORING_BOUNDARY);
        if (frozenResult == null) {
            // Every end of a scored game with a human nation leaves exactly one
            // frozen result, and an undeclared nation past spring 1800 always
            // has one; a saved game never guesses it.
            Preconditions.checkArgument(human.isEmpty() || !alreadyFinished,
                    "A finished free-play game needs its frozen result");
            Preconditions.checkArgument(
                    human.isEmpty() || declared || !alreadyStarted
                            || !pastBoundary,
                    "Saved free play past spring 1800 needs its frozen result");
            checkReason(reason, null, human.isPresent());
            return;
        }
        Preconditions.checkArgument(human.isPresent(),
                "A frozen result needs the human colonial nation");
        final GameResult.Status status = frozenResult.getStatus();
        switch (status) {
        case AWAITING_CONTINUATION:
            Preconditions.checkArgument(
                    alreadyStarted && !alreadyFinished && pastBoundary
                            && !declared,
                    "An awaited score choice needs a running undeclared game"
                            + " past spring 1800");
            break;
        case CONTINUED_UNSCORED:
            Preconditions.checkArgument(
                    alreadyStarted && pastBoundary && !declared,
                    "An unscored continuation needs an undeclared game past"
                            + " spring 1800");
            break;
        case FINAL:
            // A final result is the end itself: a rebellion's victory or loss,
            // the ordinary colony loss of an undeclared nation, or a confirmed
            // retirement (R-0194), which a declared nation may also take while
            // its rebellion is still open.
            Preconditions.checkArgument(alreadyStarted && alreadyFinished,
                    "A final result needs a finished game");
            Preconditions.checkArgument(declared
                    ? GameOverEvaluator.isRebellionLoss(reason)
                            || GameOverEvaluator.isRebellionVictory(reason)
                            || GameOverEvaluator.REASON_RETIRED.equals(reason)
                    : GameOverEvaluator.REASON_NO_COLONIES.equals(reason)
                            || GameOverEvaluator.REASON_RETIRED.equals(reason),
                    "A final result needs the end that produced it");
            // A declared rebellion is decided by its own end; a retirement is
            // the only end that leaves it open.
            Preconditions.checkArgument(
                    !declared || GameOverEvaluator.REASON_RETIRED
                            .equals(reason) != human.get().isRebellionDecided(),
                    "A declared final result is decided by its rebellion or"
                            + " retired while it is open");
            break;
        default:
            throw new IllegalArgumentException("Unknown result status");
        }
        checkReason(reason, status, true);
    }

    /**
     * The calendar and the frozen result a scored-play terminal reason needs.
     */
    private void checkReason(final String reason,
            final GameResult.Status status, final boolean humanPresent) {
        final Calendar calendar = model.getCalendar();
        Preconditions.checkArgument(!GameOverEvaluator.REASON_FREE_PLAY_ENDED
                .equals(reason)
                || calendar.hasReachedYear(Calendar.YEAR_OF_FREE_PLAY_END)
                        && (status == GameResult.Status.CONTINUED_UNSCORED
                                || status == null && !humanPresent),
                "Free play end needs the unscored continuation");
        Preconditions.checkArgument(
                !GameOverEvaluator.REASON_REBELLION_UNRESOLVED.equals(reason)
                        || calendar
                                .hasReachedYear(Calendar.YEAR_OF_FREE_PLAY_END),
                "An unresolved rebellion is lost only at spring 1850");
        // A retirement keeps a scored result: it is taken at once or kept from
        // the unscored continuation, never from the pending spring 1800 choice.
        Preconditions.checkArgument(
                !GameOverEvaluator.REASON_RETIRED.equals(reason)
                        || status == GameResult.Status.FINAL
                        || status == GameResult.Status.CONTINUED_UNSCORED,
                "A retirement needs its scored result");
        Preconditions.checkArgument(
                !GameOverEvaluator.REASON_NO_COLONIES.equals(reason)
                        || calendar.hasReachedYear(
                                Calendar.YEAR_WHEN_LAST_COLONY_ENDS_GAME),
                "A colony loss ends play only from spring 1600");
    }

    public static GameManager make(final Model model, final ModelPo modelPo,
            final List<Function<Model, GameOverResult>> gameOverEvaluators,
            final PlayerStore playerStore) {
        return new GameManager(model, gameOverEvaluators,
                modelPo.getGameManager().isGameStarted(),
                modelPo.getGameManager().isGameFinished(),
                modelPo.getGameManager().getGameOverReason(),
                findPlayer(modelPo, playerStore),
                loadFrozenResult(model, modelPo));
    }

    /**
     * Load the frozen result, if one was saved, for the nation it names.
     *
     * @return the saved result, or null when none was saved
     */
    private static GameResult loadFrozenResult(final Model model,
            final ModelPo modelPo) {
        final GameResultPo saved = modelPo.getGameManager().getGameResult();
        if (saved == null) {
            return null;
        }
        final Player owner = model.getPlayers().stream().filter(
                player -> player.getName().equals(saved.getPlayerName()))
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "Frozen result names an unknown nation"));
        return GameResult.load(saved, owner, model.getDifficulty());
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
                && !awaitingImmigrationChoice && !awaitingTaxDecision
                && !awaitsScoreChoice();
    }

    boolean isAwaitingImmigrationChoice() {
        return started && !stopped && !isFinished() && awaitingImmigrationChoice
                && !preparing;
    }

    boolean isAwaitingTaxDecision() {
        return started && !stopped && !isFinished() && awaitingTaxDecision
                && !preparing;
    }

    /**
     * @return whether spring 1800 froze the score and the game waits for the
     *         unscored continuation (R-0193)
     */
    boolean isAwaitingScoreChoice() {
        return started && !stopped && !isFinished() && awaitsScoreChoice()
                && !preparing;
    }

    private boolean awaitsScoreChoice() {
        return frozenResult != null && frozenResult.isAwaitingContinuation();
    }

    /**
     * @return the frozen free-play score, empty while none has been frozen
     */
    Optional<GameResult> getFrozenResult() {
        return Optional.ofNullable(frozenResult);
    }

    void checkTurnPreparationComplete() {
        if (preparing || awaitingImmigrationChoice || awaitingTaxDecision
                || awaitsScoreChoice()) {
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
     * @param result      required rebellion result
     * @param finalResult frozen score of the rebel taken with the same end in
     *                    free play (R-0193); null in a mission
     */
    void finishByRebellion(final GameOverResult result,
            final GameResult finalResult) {
        Preconditions.checkState(isOpen(), "Only an open game can end");
        Preconditions.checkState(frozenResult == null,
                "A result is already frozen");
        frozenResult = finalResult;
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
                        || isAwaitingTaxDecision() || isAwaitingScoreChoice(),
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
        } else if (awaitsScoreChoice()) {
            model.fireScoreBoundaryRequired(scoredPlayer());
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
                freezeScoreAtOrdinaryEnd();
                finish(oGameOverResult.get());
            } else if (!freezeScoreAtBoundary()) {
                startRound();
            }
        }
    }

    /**
     * An ordinary game end of scored free play (R-0193), such as losing the
     * last colony from spring 1600 on, takes the undeclared nation's result as
     * final once, in the same atomic step as the end. A result frozen at spring
     * 1800 is kept unchanged, so a later unscored loss or the 1850 end never
     * replaces it.
     */
    private void freezeScoreAtOrdinaryEnd() {
        if (frozenResult != null || !model.getCalendar().isScoredPlay()) {
            return;
        }
        model.getPlayers().stream()
                .filter(player -> player.isHuman() && player.isColonialNation()
                        && !player.isDeclaredIndependence())
                .findFirst().ifPresent(player -> frozenResult = GameResult
                        .freeze(player, GameResult.Status.FINAL));
    }

    /**
     * Start the next round with its first actor. Continuing after the score
     * boundary runs the same steps.
     */
    private void startRound() {
        preparing = true;
        currentPlayer = turnOrder().get(0);
        model.getStatistics().countNextTurn(model);
        model.fireRoundStarted();
        final boolean completed = startTurnOf(currentPlayer);
        preparing = false;
        announcePreparation(completed);
    }

    /**
     * Spring 1800 entry (R-0193): an undeclared human colonial nation records
     * its score once and the game waits for the unscored continuation, so no
     * round starts until the player chooses. A declared rebellion stays scored
     * past 1800 and is not frozen here; a finished game never reaches this.
     *
     * @return whether the score was frozen and the game now waits
     */
    private boolean freezeScoreAtBoundary() {
        if (frozenResult != null || !model.getCalendar().isScoredPlay()
                || !model.getCalendar()
                        .hasReachedYear(Calendar.YEAR_OF_SCORING_BOUNDARY)) {
            return false;
        }
        final Optional<Player> scored = model.getPlayers().stream()
                .filter(player -> player.isHuman() && player.isColonialNation()
                        && !player.isDeclaredIndependence())
                .findFirst();
        if (scored.isEmpty()) {
            return false;
        }
        frozenResult = GameResult.freeze(scored.get(),
                GameResult.Status.AWAITING_CONTINUATION);
        currentPlayer = turnOrder().get(0);
        model.fireScoreBoundaryRequired(scored.get());
        return true;
    }

    /**
     * Continue past spring 1800 without scoring (R-0193). The frozen score is
     * kept as it is and the round starts exactly once.
     *
     * @param player required human colonial nation that owns the frozen score
     */
    void continueWithoutScoring(final Player player) {
        Preconditions.checkState(
                isAwaitingScoreChoice() && scoredPlayer() == player,
                "Score boundary is not awaiting this nation");
        frozenResult.continueUnscored();
        startRound();
    }

    /**
     * The human colonial nation whose score free play records, declared or not.
     *
     * @return the nation, empty when there is none
     */
    private Optional<Player> retiringNation() {
        return model.getPlayers().stream()
                .filter(player -> player.isHuman() && player.isColonialNation())
                .findFirst();
    }

    /**
     * The retirement the human nation may confirm now (R-0194). Only running
     * scored free play offers it, on the nation's own turn and with no choice
     * or preparation pending. A live retirement previews the current score; an
     * unscored continuation previews the frozen result. The pending spring 1800
     * choice, a finished game and a mission offer nothing.
     *
     * @return the preview, empty when no retirement is offered
     */
    Optional<RetirementQuote> retirementQuote() {
        if (!isRunning() || !model.getCalendar().isScoredPlay()) {
            return Optional.empty();
        }
        final Optional<Player> nation = retiringNation();
        if (nation.isEmpty() || nation.get() != currentPlayer
                || nation.get().isRebellionDecided()) {
            return Optional.empty();
        }
        if (frozenResult == null) {
            return Optional.of(RetirementQuote.live(nation.get()));
        }
        return frozenResult.getStatus() == GameResult.Status.CONTINUED_UNSCORED
                ? Optional.of(RetirementQuote.continued(frozenResult))
                : Optional.empty();
    }

    /**
     * Confirm a retirement that was previewed (R-0194). The live state must
     * still offer exactly the previewed result; otherwise nothing changes and
     * the player previews again. A live retirement freezes one final result,
     * and an unscored continuation keeps its frozen result unchanged. Either
     * way the game ends with the retirement reason and no second result is
     * recorded.
     *
     * @param quote required preview that the player confirmed
     * @throws IllegalStateException when no retirement is offered or the
     *                               preview no longer matches the live state
     */
    void retire(final RetirementQuote quote) {
        final RetirementQuote live = retirementQuote().orElseThrow(
                () -> new IllegalStateException("Retirement is not offered"));
        Preconditions.checkState(live.matches(quote),
                "Retirement preview is stale");
        if (frozenResult == null) {
            frozenResult = GameResult.freeze(retiringNation().orElseThrow(),
                    GameResult.Status.FINAL);
        }
        finish(new GameOverResult(GameOverEvaluator.REASON_RETIRED));
    }

    /**
     * @return the human colonial nation that owns the frozen score
     * @throws IllegalStateException when no score is frozen
     */
    private Player scoredPlayer() {
        Preconditions.checkState(frozenResult != null, "No score is frozen");
        final String name = frozenResult.getPlayerName();
        return model.getPlayers().stream()
                .filter(player -> player.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Frozen result names an unknown nation"));
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
        modelPo.getGameManager().setGameResult(
                frozenResult == null ? null : frozenResult.save());
        modelPo.getGameManager().setCurrentPlayer(getCurrentPlayer().getName());
    }

}
