package org.microcol.model.store;

public final class CalendarPo {

    private int startYear;
    private int endYear;
    private int numberOfPlayedTurns;

    /**
     * @return the startYear
     */
    public int getStartYear() {
        return startYear;
    }

    /**
     * @param startYear
     *            the startYear to set
     */
    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    /**
     * @return the endYear
     */
    public int getEndYear() {
        return endYear;
    }

    /**
     * @param endYear
     *            the endYear to set
     */
    public void setEndYear(int endYear) {
        this.endYear = endYear;
    }

    /**
     * @return the numberOfPlayedTurns
     */
    public int getNumberOfPlayedTurns() {
        return numberOfPlayedTurns;
    }

    /**
     * @param numberOfPlayedTurns
     *            the numberOfPlayedTurns to set
     */
    public void setNumberOfPlayedTurns(int numberOfPlayedTurns) {
        this.numberOfPlayedTurns = numberOfPlayedTurns;
    }

}
