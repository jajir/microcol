package org.microcol.model;

import java.util.Optional;

import org.microcol.model.store.CalendarPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Calendar {

    private final static int YEAR_WHEN_START_COUNT_SEASONS = 1600;

    private final int startYear;

    private final int endYear;

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

    Calendar(final int startYear, final int endYear, final int numberOfPlayedTurns) {
        Preconditions.checkArgument(startYear < endYear,
                "Start year (%s) must be less than end year (%s).", startYear, endYear);
        this.startYear = startYear;
        this.endYear = endYear;
        this.numberOfPlayedTurns = numberOfPlayedTurns;
    }

    static Calendar make(final CalendarPo cal) {
        return new Calendar(cal.getStartYear(), cal.getEndYear(), cal.getNumberOfPlayedTurns());
    }

    private int getTurnWhenStartCountsSeasons() {
        return YEAR_WHEN_START_COUNT_SEASONS - startYear;
    }

    public int getStartYear() {
        return startYear;
    }

    public int getEndYear() {
        return endYear;
    }

    public int getCurrentYear() {
        if (numberOfPlayedTurns > getTurnWhenStartCountsSeasons()) {
            return YEAR_WHEN_START_COUNT_SEASONS
                    + (numberOfPlayedTurns - getTurnWhenStartCountsSeasons()) / 4;
        } else {
            return startYear + numberOfPlayedTurns;
        }
    }

    public Optional<Season> getCurrentSeason() {
        if (numberOfPlayedTurns < getTurnWhenStartCountsSeasons()) {
            return Optional.empty();
        } else {
            final int index = (numberOfPlayedTurns - getTurnWhenStartCountsSeasons()) % 4;
            return Optional.of(Season.values()[index]);
        }
    }

    boolean isFinished() {
        return getCurrentYear() >= endYear;
    }

    void endRound() {
        Preconditions.checkState(!isFinished(), "End year (%s) already reached.", endYear);

        numberOfPlayedTurns++;
    }

    public int getYearForTurnNo(int turnNo) {
        return startYear + turnNo;
    }

    public CalendarPo save() {
        final CalendarPo out = new CalendarPo();
        out.setStartYear(startYear);
        out.setEndYear(endYear);
        out.setNumberOfPlayedTurns(numberOfPlayedTurns);
        return out;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("startYear", startYear).add("endYear", endYear)
                .add("currentYear", getCurrentYear())
                .add("numberOfPlayedTurns", numberOfPlayedTurns).toString();
    }

    /**
     * @return the numberOfPlayedTurns
     */
    public int getNumberOfPlayedTurns() {
        return numberOfPlayedTurns;
    }

}
