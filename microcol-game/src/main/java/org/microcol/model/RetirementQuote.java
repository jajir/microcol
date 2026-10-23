package org.microcol.model;

import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Preconditions;

/**
 * Preview of one retirement of the human colonial nation (R-0194). It is a
 * read-only snapshot of the score that the confirmation would record. A live
 * retirement previews the nation's current score, and the frozen identity is
 * taken only on confirmation. A retirement during an unscored continuation
 * previews the frozen result, whose identity and components the confirmation
 * keeps. The model compares a confirmed preview with the live state again and
 * never recalculates a differing score silently.
 */
public final class RetirementQuote {

    private final String playerName;

    private final String identity;

    private final BaseScore base;

    private final ResultScore score;

    private RetirementQuote(final String playerName, final String identity,
            final BaseScore base, final ResultScore score) {
        this.playerName = playerName;
        this.identity = identity;
        this.base = base;
        this.score = score;
    }

    /**
     * Preview a retirement before any score is frozen in free play.
     *
     * @param owner required human colonial nation
     * @return immutable preview of its current score
     */
    static RetirementQuote live(final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        return new RetirementQuote(owner.getName(), null, owner.getBaseScore(),
                owner.getResultScore());
    }

    /**
     * Preview a retirement during the unscored continuation.
     *
     * @param frozen required frozen result that the continuation keeps
     * @return immutable preview of the frozen result
     */
    static RetirementQuote continued(final GameResult frozen) {
        Preconditions.checkNotNull(frozen, "Frozen result is required");
        return new RetirementQuote(frozen.getPlayerName(), frozen.getIdentity(),
                frozen.getBaseScore(), frozen.getScore());
    }

    /**
     * @return the internal name of the nation that would retire
     */
    public String getPlayerName() {
        return playerName;
    }

    /**
     * @return whether the retirement keeps an existing frozen result
     */
    public boolean isContinued() {
        return identity != null;
    }

    /**
     * @return the identity of the frozen result that is kept, empty for a live
     *         retirement that will freeze a new one
     */
    public Optional<String> getIdentity() {
        return Optional.ofNullable(identity);
    }

    /**
     * @return the base score that the confirmation would record
     */
    public BaseScore getBaseScore() {
        return base;
    }

    /**
     * @return the calculated result that the confirmation would record
     */
    public ResultScore getScore() {
        return score;
    }

    /**
     * @param live the preview that the current state offers
     * @return whether a confirmed preview still describes the live state
     *         exactly
     */
    boolean matches(final RetirementQuote live) {
        return playerName.equals(live.playerName)
                && Objects.equals(identity, live.identity)
                && sameBase(base, live.base) && sameScore(score, live.score);
    }

    private static boolean sameBase(final BaseScore a, final BaseScore b) {
        return a.getServants() == b.getServants()
                && a.getColonists() == b.getColonists()
                && a.getExperts() == b.getExperts()
                && a.getFathers() == b.getFathers()
                && a.getGold() == b.getGold()
                && a.getSupportPercent() == b.getSupportPercent();
    }

    private static boolean sameScore(final ResultScore a, final ResultScore b) {
        return a.getTotal() == b.getTotal()
                && a.getDestroyedSettlements() == b.getDestroyedSettlements()
                && a.getBellsAfterTarget() == b.getBellsAfterTarget()
                && a.getOrderBonusPercent() == b.getOrderBonusPercent()
                && a.getEarlyPoints() == b.getEarlyPoints()
                && a.getDifficultyPercent() == b.getDifficultyPercent();
    }

}
