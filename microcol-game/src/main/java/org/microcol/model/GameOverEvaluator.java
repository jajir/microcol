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

    /** Free play reached spring 1850 (R-0193); all play ends. */
    public final static String REASON_FREE_PLAY_ENDED = "FREE_PLAY_ENDED";

    /**
     * The human nation confirmed its retirement in scored free play (R-0194).
     * The frozen result is the one that the retirement took or kept.
     */
    public final static String REASON_RETIRED = "RETIRED";

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

    /**
     * A declared rebellion still unresolved at spring 1850 is lost (R-0193). It
     * is judged only at that boundary, after the rebellion's own conditions.
     */
    public final static String REASON_REBELLION_UNRESOLVED = "REBELLION_UNRESOLVED";

    /** Declared rebellion won by the rebels (R-0191); never also a loss. */
    public final static String REASON_REBELLION_VICTORY = "REBELLION_VICTORY";

    /**
     * @param reason any game over reason code
     * @return whether it is one of the rebellion loss reasons
     */
    public static boolean isRebellionLoss(final String reason) {
        return REASON_REBELLION_NO_COLONIES.equals(reason)
                || REASON_REBELLION_ALL_COLONIES_CROWN.equals(reason)
                || REASON_REBELLION_COASTAL_CROWN.equals(reason)
                || REASON_REBELLION_RESIDENTS_CROWN.equals(reason)
                || REASON_REBELLION_UNRESOLVED.equals(reason);
    }

    /**
     * @param reason any game over reason code
     * @return whether it is the victory reason of a declared rebellion
     */
    public static boolean isRebellionVictory(final String reason) {
        return REASON_REBELLION_VICTORY.equals(reason);
    }

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
            return new GameOverResult(
                    model.getCalendar().isScoredPlay() ? REASON_FREE_PLAY_ENDED
                            : REASON_TIME_IS_UP);
        } else {
            return null;
        }
    };

    /**
     * Losing the last colony ends play only from spring 1600 on (R-0193);
     * before that a zero-colony position is not a game end and no turn count
     * replaces the year.
     */
    public static final Function<Model, GameOverResult> GAMEOVER_CONDITION_HUMAN_LOST_ALL_COLONIES = (
            model) -> {
        if (model.getCalendar()
                .hasReachedYear(Calendar.YEAR_WHEN_LAST_COLONY_ENDS_GAME)) {
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
