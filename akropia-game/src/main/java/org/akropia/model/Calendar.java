package org.akropia.model;

import java.util.Optional;

import org.akropia.model.store.CalendarPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Calendar {

    public static final int DAYS_PER_MONTH = 7;
    public static final int MONTHS_PER_YEAR = 12;
    public static final int DAYS_PER_YEAR = DAYS_PER_MONTH * MONTHS_PER_YEAR;
    public static final int DAYS_PER_SEASON = 21;
    private static final String[] MONTH_NAMES = { "Hekatombaion",
            "Metageitnion", "Boedromion", "Pyanepsion", "Maimakterion",
            "Poseideon", "Gamelion", "Anthesterion", "Elaphebolion",
            "Mounichion", "Thargelion", "Skirophorion" };
    private int turnLimit;

    /** Legacy fixture horizon; scored free play has no calendar deadline. */
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
        this.startYear = startYear;
        this.endYear = endYear;
        this.numberOfPlayedTurns = numberOfPlayedTurns;
        this.scoredPlay = scoredPlay;
        this.turnLimit = Math.multiplyExact(endYear - startYear, DAYS_PER_YEAR);
    }

    public static Calendar make(final CalendarPo cal) {
        Preconditions.checkArgument(
                cal.getTurnLimit() != null && cal.getTurnLimit() > 0,
                "A positive mission turn limit is required.");
        final Calendar result = new Calendar(cal.getStartYear(),
                cal.getEndYear(), cal.getNumberOfPlayedTurns(),
                cal.isScoredPlay());
        result.turnLimit = cal.getTurnLimit();
        return result;
    }

    public int getDayOfSeason() {
        return numberOfPlayedTurns % DAYS_PER_SEASON + 1;
    }

    public Season getNextSeason() {
        return Season.values()[(numberOfPlayedTurns / DAYS_PER_SEASON + 2) % 4];
    }

    public int getDayOfMonth() {
        return numberOfPlayedTurns % DAYS_PER_MONTH + 1;
    }

    public int getMonthOfYear() {
        return numberOfPlayedTurns % DAYS_PER_YEAR / DAYS_PER_MONTH + 1;
    }

    public String getMonthName() {
        return MONTH_NAMES[getMonthOfYear() - 1];
    }

    public int getDaysUntilNextSeason() {
        return DAYS_PER_SEASON - numberOfPlayedTurns % DAYS_PER_SEASON;
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
        return Optional.of(Season
                .values()[(numberOfPlayedTurns / DAYS_PER_SEASON + 1) % 4]);
    }

    boolean isFinished() {
        return !scoredPlay && numberOfPlayedTurns >= turnLimit;
    }

    /**
     * @return whether this is free play, without a calendar deadline.
     */
    public boolean isScoredPlay() {
        return scoredPlay;
    }

    /**
     * @param year expedition year
     * @return whether the current year has reached the given one
     */
    public boolean hasReachedYear(final int year) {
        return getCurrentYear() >= year;
    }

    void endRound() {
        Preconditions.checkState(!isFinished(),
                "Mission turn limit (%s) already reached.", turnLimit);

        numberOfPlayedTurns++;
    }

    public int getYearForTurnNo(final int turnNo) {
        Preconditions.checkArgument(turnNo >= 0,
                "Turn number must not be negative.");
        return startYear + turnNo / DAYS_PER_YEAR;
    }

    public CalendarPo save() {
        final CalendarPo out = new CalendarPo();
        out.setStartYear(startYear);
        out.setTurnLimit(turnLimit);
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
