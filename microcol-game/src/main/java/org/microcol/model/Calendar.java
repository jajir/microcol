package org.microcol.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

// TODO JKA Documentation
// TODO JKA Tests
public class Calendar {
	private final int startYear;
	private final int endYear;

	private int currentYear;

	public Calendar(final int startYear, final int endYear) {
		Preconditions.checkArgument(startYear < endYear, "Start year (%s) must be less than end year (%s).", startYear, endYear);

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
		Preconditions.checkState(!isFinished(), "Current year (%s) must be less than or equal to end year (%s).", currentYear, endYear);

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
