package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * Per-job political production change of one colony: a rebel support bonus and
 * a Tory penalty, both derived from exact integers. An instance is an immutable
 * snapshot; the colony captures one at the start of a production update and
 * passes it to every calculation of that update, so bells credited during the
 * update affect the next snapshot only.
 */
public final class PoliticalProduction {

    /** No political change, e.g. for tests or colonies without residents. */
    public static final PoliticalProduction NONE = new PoliticalProduction(
            RebelSupport.NONE, 0, 0, 0);

    /** Support (percent) that earns the first bonus point. */
    public static final int FIRST_BONUS_PERCENT = 50;

    /** Support (percent) that earns the second bonus point. */
    public static final int SECOND_BONUS_PERCENT = 100;

    private final RebelSupport support;

    private final int bonus;

    private final int toryCount;

    private final int toryThreshold;

    private PoliticalProduction(final RebelSupport support, final int bonus,
            final int toryCount, final int toryThreshold) {
        this.support = support;
        this.bonus = bonus;
        this.toryCount = toryCount;
        this.toryThreshold = toryThreshold;
    }

    /**
     * @param support    exact support captured for the colony
     * @param difficulty selected difficulty, which defines the Tory threshold
     * @return immutable snapshot
     */
    public static PoliticalProduction of(final RebelSupport support,
            final Difficulty difficulty) {
        Preconditions.checkNotNull(support);
        Preconditions.checkNotNull(difficulty);
        final int bonus = support.isAtLeastPercent(SECOND_BONUS_PERCENT) ? 2
                : support.isAtLeastPercent(FIRST_BONUS_PERCENT) ? 1 : 0;
        // N - floor(N * numerator / (200 * N)) = N - floor(numerator / 200)
        final long population = support.getPopulation();
        final long rebels = support.getNumerator()
                / RebelSupport.POINTS_PER_RESIDENT;
        return new PoliticalProduction(support, bonus,
                Math.toIntExact(population - rebels),
                getToryThreshold(difficulty));
    }

    /**
     * @param difficulty selected difficulty
     * @return Tory count at which the penalty starts
     */
    public static int getToryThreshold(final Difficulty difficulty) {
        switch (difficulty) {
        case DISCOVERER:
            return 10;
        case EXPLORER:
            return 9;
        case CONQUISTADOR:
            return 8;
        case GOVERNOR:
            return 7;
        case VICEROY:
            return 6;
        default:
            throw new IllegalArgumentException(
                    "Unknown difficulty " + difficulty);
        }
    }

    /** @return exact support behind this snapshot */
    public RebelSupport getSupport() {
        return support;
    }

    /** @return residents counted in this snapshot */
    public long getPopulation() {
        return support.getPopulation();
    }

    /** @return 0, 1 or 2 per eligible job */
    public int getBonus() {
        return bonus;
    }

    /** @return {@code N - floor(N * support)} */
    public int getToryCount() {
        return toryCount;
    }

    /**
     * @return Tory count at which the penalty starts; zero for {@link #NONE}
     */
    public int getToryThreshold() {
        return toryThreshold;
    }

    /** @return 0 or -1 per eligible job */
    public int getPenalty() {
        return toryThreshold > 0 && toryCount >= toryThreshold ? -1 : 0;
    }

    /** @return bonus and penalty combined once, per eligible job */
    public int getNetChange() {
        return bonus + getPenalty();
    }

    /**
     * Apply the combined change to one staffed job, after flat and profession
     * effects and before percentage effects.
     *
     * @param baseYield positive base yield of the job without any worker
     *                  effect; zero base never receives a change
     * @param jobOutput output after flat and profession effects
     * @return output changed once and never below zero
     */
    public int apply(final int baseYield, final int jobOutput) {
        if (baseYield <= 0) {
            return jobOutput;
        }
        return Math.max(0, jobOutput + getNetChange());
    }

    /**
     * Same as {@link #apply(int, int)} but for a whole goods amount.
     *
     * @param base      base goods of the job
     * @param jobOutput output goods after flat and profession effects
     * @return output goods changed once
     */
    public Goods apply(final Goods base, final Goods jobOutput) {
        return jobOutput
                .setAmount(apply(base.getAmount(), jobOutput.getAmount()));
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PoliticalProduction)) {
            return false;
        }
        final PoliticalProduction other = (PoliticalProduction) obj;
        return support.equals(other.support) && bonus == other.bonus
                && toryCount == other.toryCount
                && toryThreshold == other.toryThreshold;
    }

    @Override
    public int hashCode() {
        return support.hashCode() * 31 + bonus * 17 + toryCount * 7
                + toryThreshold;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("support", support)
                .add("bonus", bonus).add("toryCount", toryCount)
                .add("toryThreshold", toryThreshold).toString();
    }
}
