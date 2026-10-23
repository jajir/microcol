package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.CalendarPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Calendar {

    private final static int YEAR_WHEN_START_COUNT_SEASONS = 1600;

    /** From spring of this year, losing the last colony ends ordinary play. */
    public final static int YEAR_WHEN_LAST_COLONY_ENDS_GAME = 1600;

    /** Spring of this year is the scoring boundary of free play (R-0193). */
    public final static int YEAR_OF_SCORING_BOUNDARY = 1800;

    /**
     * Spring of this year ends all scored free play (R-0193). It is the end
     * year a scored calendar must carry; an unscored calendar, such as a
     * mission, may use any deadline including this one.
     */
    public final static int YEAR_OF_FREE_PLAY_END = 1850;

    private final int startYear;

    private final int endYear;

    /**
     * Explicit free-play score policy (R-0193), saved with the calendar. It is
     * not derived from the end year: a mission may end in any year, and only a
     * calendar saved as scored runs the frozen-result rules.
     */
    private final boolean scoredPlay;

    /**
     * Enum define seasons.
     * <p>
     * To determine current season is used ordinal value. So don't change order.
     * </p>
     */
    public static enum Season {

        spring("spring"), summer("summer"), autumn("autumn"), winter("winter");

        private final String key;

        Season(final String key) {
            this.key = Preconditions.checkNotNull(key);
        }

        /**
         * Return source key code.
         *
         * @return the key
         */
        public String getKey() {
            return key;
        }

    }

    /**
     * How many game turns passed. It's different from year.
     */
    private int numberOfPlayedTurns;

    Calendar(final int startYear, final int endYear,
            final int numberOfPlayedTurns) {
        this(startYear, endYear, numberOfPlayedTurns, false);
    }

    Calendar(final int startYear, final int endYear,
            final int numberOfPlayedTurns, final boolean scoredPlay) {
        Preconditions.checkArgument(startYear < endYear,
                "Start year (%s) must be less than end year (%s).", startYear,
                endYear);
        Preconditions.checkArgument(numberOfPlayedTurns >= 0,
                "Number of played turns must not be negative.");
        Preconditions.checkArgument(
                !scoredPlay || endYear == YEAR_OF_FREE_PLAY_END,
                "Scored play ends in %s, not in %s.", YEAR_OF_FREE_PLAY_END,
                endYear);
        this.startYear = startYear;
        this.endYear = endYear;
        this.numberOfPlayedTurns = numberOfPlayedTurns;
        this.scoredPlay = scoredPlay;
    }

    public static Calendar make(final CalendarPo cal) {
        return new Calendar(cal.getStartYear(), cal.getEndYear(),
                cal.getNumberOfPlayedTurns(), cal.isScoredPlay());
    }

    private int getTurnWhenStartCountsSeasons() {
        final int out = YEAR_WHEN_START_COUNT_SEASONS - startYear;
        return out < 0 ? 0 : out;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public int getCurrentYear() {
        return getYearForTurnNo(numberOfPlayedTurns);
    }

    public Optional<Season> getCurrentSeason() {
        if (numberOfPlayedTurns < getTurnWhenStartCountsSeasons()) {
            return Optional.empty();
        } else {
            final int index = (numberOfPlayedTurns
                    - getTurnWhenStartCountsSeasons()) % 4;
            return Optional.of(Season.values()[index]);
        }
    }

    boolean isFinished() {
        return getCurrentYear() >= endYear;
    }

    /**
     * @return whether this game was saved with the free-play score policy
     *         (R-0193): the frozen result at spring 1800 and the end at spring
     *         1850. Missions, even with an 1850 deadline, return false.
     */
    public boolean isScoredPlay() {
        return scoredPlay;
    }

    /**
     * @param year calendar year
     * @return whether the current year has reached the given one; the year
     *         changes in spring, so this is true from that spring on
     */
    public boolean hasReachedYear(final int year) {
        return getCurrentYear() >= year;
    }

    void endRound() {
        Preconditions.checkState(!isFinished(),
                "End year (%s) already reached.", endYear);

        numberOfPlayedTurns++;
    }

    public int getYearForTurnNo(final int turnNo) {
        Preconditions.checkArgument(turnNo >= 0,
                "Turn number must not be negative.");
        final int annualTurns = getTurnWhenStartCountsSeasons();
        if (turnNo <= annualTurns) {
            return startYear + turnNo;
        }
        return Math.max(startYear, YEAR_WHEN_START_COUNT_SEASONS)
                + (turnNo - annualTurns) / 4;
    }

    public CalendarPo save() {
        final CalendarPo out = new CalendarPo();
        out.setStartYear(startYear);
        out.setEndYear(endYear);
        out.setScoredPlay(scoredPlay);
        out.setNumberOfPlayedTurns(numberOfPlayedTurns);
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("startYear", startYear)
                .add("endYear", endYear).add("currentYear", getCurrentYear())
                .add("numberOfPlayedTurns", numberOfPlayedTurns).toString();
    }

    /**
     * @return the numberOfPlayedTurns
     */
    public int getNumberOfPlayedTurns() {
        return numberOfPlayedTurns;
    }

}
