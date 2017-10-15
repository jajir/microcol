package org.microcol.model;

import org.microcol.model.store.CalendarPo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Calendar {
	
	private final int startYear;
	
	private final int endYear;

	private int currentYear;
	
	Calendar(final int startYear, final int endYear, final int currentYear) {
		Preconditions.checkArgument(startYear < endYear, "Start year (%s) must be less than end year (%s).", startYear, endYear);
		this.startYear = startYear;
		this.endYear = endYear;
		this.currentYear = currentYear;
	}
	

	Calendar(final int startYear, final int endYear) {
		this(startYear, endYear, startYear);
	}
	
	static Calendar make(final CalendarPo cal) {
		return new Calendar(cal.getStartYear(), cal.getEndYear(), cal.getCurrentYear());
	}

	public int getStartYear() {
		return startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public int getCurrentYear() {
		return currentYear;
	}

	boolean isFinished() {
		return currentYear >= endYear;
	}

	void endRound() {
		Preconditions.checkState(!isFinished(), "End year (%s) already reached.", endYear);

		currentYear++;
	}
	
	public CalendarPo save(){
		final CalendarPo out = new CalendarPo();
		out.setStartYear(startYear);
		out.setEndYear(endYear);
		out.setCurrentYear(currentYear);
		return out;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("startYear", startYear)
			.add("endYear", endYear)
			.add("currentYear", currentYear)
			.toString();
	}
}
