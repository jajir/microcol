package org.microcol.model;

import java.math.BigInteger;

import com.google.common.base.Preconditions;

/**
 * Calculated result of one colonial nation (R-0196). It takes the base score
 * (R-0195), subtracts each native settlement the nation destroyed, adds the
 * bells produced beyond the earned intervention target, clamps that sum at
 * zero, and only then applies the successful independence bonus and early
 * points. The difficulty factor comes last and the total is floored once. Every
 * term is exact; nothing is rounded before the final floor. Reading the result
 * changes nothing and records no finished game.
 */
public final class ResultScore {

    /** Early declaration points are the years before this one. */
    static final int EARLY_DECLARATION_YEAR = 1780;

    /** Independence bonus in percent for the first to fourth victory. */
    private static final int[] ORDER_BONUS_PERCENT = { 100, 50, 25, 0 };

    private final long baseSubtotal;
    private final int destroyedSettlements;
    private final long pointsPerSettlement;
    private final long destructionDeduction;
    private final long bellsAfterTarget;
    private final long clampedBase;
    private final int orderBonusPercent;
    private final long earlyPoints;
    private final int difficultyPercent;
    private final long total;

    private ResultScore(final BaseScore base, final int destroyedSettlements,
            final long pointsPerSettlement, final long bellsAfterTarget,
            final int orderBonusPercent, final long earlyPoints,
            final int difficultyPercent) {
        baseSubtotal = base.getSubtotal();
        this.destroyedSettlements = destroyedSettlements;
        this.pointsPerSettlement = pointsPerSettlement;
        destructionDeduction = Math.multiplyExact((long) destroyedSettlements,
                pointsPerSettlement);
        this.bellsAfterTarget = bellsAfterTarget;
        clampedBase = Math.max(0L,
                Math.subtractExact(
                        Math.addExact(baseSubtotal, bellsAfterTarget),
                        destructionDeduction));
        this.orderBonusPercent = orderBonusPercent;
        this.earlyPoints = earlyPoints;
        this.difficultyPercent = difficultyPercent;
        // (B x (100 + bonus) / 100 + early) x factor / 100, floored once: the
        // numerator is kept exact and only the last division drops the rest.
        final BigInteger numerator = BigInteger.valueOf(clampedBase)
                .multiply(BigInteger.valueOf(100L + orderBonusPercent))
                .add(BigInteger.valueOf(earlyPoints)
                        .multiply(BigInteger.valueOf(100L)))
                .multiply(BigInteger.valueOf(difficultyPercent));
        total = numerator.divide(BigInteger.valueOf(10000L)).longValueExact();
    }

    /**
     * Calculate the result from its already known parts. Used by the nation
     * calculation and by tests of the arithmetic.
     *
     * @param base                 required base score of the nation
     * @param destroyedSettlements native settlements the nation destroyed,
     *                             nonnegative
     * @param difficulty           required selected difficulty
     * @param bellsBeyondTarget    bells produced after the earned intervention
     *                             target, nonnegative
     * @param victoryOrder         completion order of a successful
     *                             independence, one or more; null when it did
     *                             not succeed
     * @param declarationYear      calendar year of the declaration; required
     *                             for a successful independence
     * @return immutable calculated result
     */
    static ResultScore of(final BaseScore base, final int destroyedSettlements,
            final Difficulty difficulty, final long bellsBeyondTarget,
            final Integer victoryOrder, final Integer declarationYear) {
        Preconditions.checkNotNull(base, "Base score is required");
        Preconditions.checkNotNull(difficulty, "Difficulty is required");
        Preconditions.checkArgument(destroyedSettlements >= 0,
                "Negative destroyed settlements");
        Preconditions.checkArgument(bellsBeyondTarget >= 0,
                "Negative bells beyond target");
        Preconditions.checkArgument(victoryOrder == null || victoryOrder >= 1,
                "Invalid victory order");
        Preconditions.checkArgument(
                victoryOrder == null || declarationYear != null,
                "A successful independence needs its declaration year");
        final boolean successful = victoryOrder != null;
        final long pointsPerSettlement = difficulty.ordinal() + 1L;
        return new ResultScore(base, destroyedSettlements, pointsPerSettlement,
                bellsBeyondTarget,
                successful ? orderBonusPercent(victoryOrder) : 0,
                successful ? earlyPoints(declarationYear) : 0L,
                difficultyPercent(difficulty));
    }

    /**
     * @param order completion order, one for the first successful independence
     * @return independence bonus in percent; zero from the fourth on
     */
    static int orderBonusPercent(final int order) {
        return order <= ORDER_BONUS_PERCENT.length
                ? ORDER_BONUS_PERCENT[order - 1]
                : 0;
    }

    /**
     * @param declarationYear calendar year of a successful declaration
     * @return points for declaring before 1780, zero from 1780 on
     */
    static long earlyPoints(final int declarationYear) {
        return Math.max(0L, (long) EARLY_DECLARATION_YEAR - declarationYear);
    }

    /**
     * @param difficulty required selected difficulty
     * @return score factor of the difficulty in percent, 50 to 150
     */
    static int difficultyPercent(final Difficulty difficulty) {
        switch (Preconditions.checkNotNull(difficulty)) {
        case DISCOVERER:
            return 50;
        case EXPLORER:
            return 75;
        case CONQUISTADOR:
            return 100;
        case GOVERNOR:
            return 125;
        case VICEROY:
            return 150;
        default:
            throw new IllegalStateException("Unknown difficulty " + difficulty);
        }
    }

    /** @return base subtotal before every war and difficulty term */
    public long getBaseSubtotal() {
        return baseSubtotal;
    }

    /** @return native settlements destroyed by this nation */
    public int getDestroyedSettlements() {
        return destroyedSettlements;
    }

    /** @return points deducted per destroyed settlement, difficulty plus one */
    public long getPointsPerSettlement() {
        return pointsPerSettlement;
    }

    /** @return total points deducted for the destroyed settlements */
    public long getDestructionDeduction() {
        return destructionDeduction;
    }

    /** @return bells produced beyond the earned intervention target */
    public long getBellsAfterTarget() {
        return bellsAfterTarget;
    }

    /** @return base after the deductions and bells, never below zero */
    public long getClampedBase() {
        return clampedBase;
    }

    /** @return independence bonus in percent, zero without a victory */
    public int getOrderBonusPercent() {
        return orderBonusPercent;
    }

    /** @return early declaration points, zero without a victory */
    public long getEarlyPoints() {
        return earlyPoints;
    }

    /** @return difficulty factor in percent */
    public int getDifficultyPercent() {
        return difficultyPercent;
    }

    /** @return calculated result, floored once after the difficulty factor */
    public long getTotal() {
        return total;
    }

    @Override
    public String toString() {
        return "ResultScore[base=" + baseSubtotal + ", destroyed="
                + destroyedSettlements + ", deduction=" + destructionDeduction
                + ", bells=" + bellsAfterTarget + ", clamped=" + clampedBase
                + ", orderBonus=" + orderBonusPercent + "%, early="
                + earlyPoints + ", difficulty=" + difficultyPercent
                + "%, total=" + total + "]";
    }
}
