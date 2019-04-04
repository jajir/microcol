package org.microcol.model;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Evaluate game over conditions.
 */
public final class GameOverEvaluator {

    public final static String REASON_TIME_IS_UP = "TIME_IS_UP";

    public final static String REASON_NO_COLONIES = "NO_COLONIES";

    private final static int TURNS_WHEN_COLONY_LOST_IS_NOT_COUNTED = 15;

    private final ChainOfCommandOptionalStrategy<Model, GameOverResult> conditions;

    public GameOverEvaluator(final List<Function<Model, GameOverResult>> gameOverEvaluators) {
        Preconditions.checkNotNull(gameOverEvaluators, "Game over evaluators are null.");
        Preconditions.checkArgument(!gameOverEvaluators.isEmpty(),
                "Game over evaluators are empty.");
        this.conditions = new ChainOfCommandOptionalStrategy<>(gameOverEvaluators);
    }

    public Optional<GameOverResult> evaluate(final Model model) {
        return conditions.apply(model);
    }

    public static final Function<Model, GameOverResult> GAMEOVER_CONDITION_CALENDAR = (model) -> {
        if (model.getCalendar().isFinished()) {
            return new GameOverResult(REASON_TIME_IS_UP);
        } else {
            return null;
        }
    };

    public static final Function<Model, GameOverResult> GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES = (
            model) -> {
        if (model.getCalendar().getNumberOfPlayedTurns() > TURNS_WHEN_COLONY_LOST_IS_NOT_COUNTED) {
            for (final Player player : model.getPlayerStore().getPlayers()) {
                if (player.isHuman() && model.getColonies(player).isEmpty()) {
                    return new GameOverResult(REASON_NO_COLONIES);
                }
            }
        }
        return null;
    };

    void addEvaluator(final Function<Model, GameOverResult> evaluator) {
        conditions.addCommand(evaluator);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode()).toString();
    }

}
