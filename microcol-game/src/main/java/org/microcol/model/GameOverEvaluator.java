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

    /**
     * Declared rebellion losses (R-0192), in the priority order of the
     * conditions: no surviving colony, all surviving colonies Crown-held, all
     * coastal ones Crown-held or none surviving, and 90 percent of residents.
     */
    public final static String REASON_REBELLION_NO_COLONIES = "REBELLION_NO_COLONIES";

    public final static String REASON_REBELLION_ALL_COLONIES_CROWN = "REBELLION_ALL_COLONIES_CROWN";

    public final static String REASON_REBELLION_COASTAL_CROWN = "REBELLION_COASTAL_CROWN";

    public final static String REASON_REBELLION_RESIDENTS_CROWN = "REBELLION_RESIDENTS_CROWN";

    /** Declared rebellion won by the rebels (R-0191); never also a loss. */
    public final static String REASON_REBELLION_VICTORY = "REBELLION_VICTORY";

    /**
     * @param reason any game over reason code
     * @return whether it is one of the four rebellion loss reasons
     */
    public static boolean isRebellionLoss(final String reason) {
        return REASON_REBELLION_NO_COLONIES.equals(reason)
                || REASON_REBELLION_ALL_COLONIES_CROWN.equals(reason)
                || REASON_REBELLION_COASTAL_CROWN.equals(reason)
                || REASON_REBELLION_RESIDENTS_CROWN.equals(reason);
    }

    /**
     * @param reason any game over reason code
     * @return whether it is the victory reason of a declared rebellion
     */
    public static boolean isRebellionVictory(final String reason) {
        return REASON_REBELLION_VICTORY.equals(reason);
    }

    private final static int TURNS_WHEN_COLONY_LOST_IS_NOT_COUNTED = 15;

    private final ChainOfCommandOptionalStrategy<Model, GameOverResult> conditions;

    public GameOverEvaluator(
            final List<Function<Model, GameOverResult>> gameOverEvaluators) {
        Preconditions.checkNotNull(gameOverEvaluators,
                "Game over evaluators are null.");
        Preconditions.checkArgument(!gameOverEvaluators.isEmpty(),
                "Game over evaluators are empty.");
        this.conditions = new ChainOfCommandOptionalStrategy<>(
                gameOverEvaluators);
    }

    public Optional<GameOverResult> evaluate(final Model model) {
        return conditions.apply(model);
    }

    public static final Function<Model, GameOverResult> GAMEOVER_CONDITION_CALENDAR = (
            model) -> {
        if (model.getCalendar().isFinished()) {
            return new GameOverResult(REASON_TIME_IS_UP);
        } else {
            return null;
        }
    };

    public static final Function<Model, GameOverResult> GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES = (
            model) -> {
        if (model.getCalendar()
                .getNumberOfPlayedTurns() > TURNS_WHEN_COLONY_LOST_IS_NOT_COUNTED) {
            for (final Player player : model.getPlayerStore().getPlayers()) {
                if (player.isHuman() && model.getColonies(player).isEmpty()) {
                    return new GameOverResult(REASON_NO_COLONIES);
                }
            }
        }
        return null;
    };

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode())
                .toString();
    }

}
