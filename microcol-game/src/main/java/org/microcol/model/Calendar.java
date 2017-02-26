package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class Calendar {
	private final int startYear;
	private final int endYear;

	private int currentYear;

	public Calendar(final int startYear, final int endYear) {
		Preconditions.checkArgument(startYear < endYear);

		this.startYear = startYear;
		this.endYear = endYear;

		this.currentYear = startYear;
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

	protected boolean isFinished() {
		return currentYear >= endYear;
	}

	protected void endRound() {
		Preconditions.checkState(!isFinished(), "Current year (%s) cannot be greater than end year (%s).", currentYear, endYear);

		currentYear++;
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
