package org.microcol.model;

public class Calendar {
	private final int startYear;
	private final int endYear;

	private int currentYear;

	public Calendar(final int startYear, final int endYear) {
		// TODO JKA Validace startYear < maxYear
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

	boolean isFinished() {
		return currentYear >= endYear;
	}

	protected void endRound() {
		// TODO JKA Implement test currentYear >= endYear.
		currentYear++;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Calendar [startYear = ");
		builder.append(startYear);
		builder.append(", endYear = ");
		builder.append(endYear);
		builder.append(", currentYear = ");
		builder.append(currentYear);
		builder.append("]");

		return builder.toString();
	}
}
