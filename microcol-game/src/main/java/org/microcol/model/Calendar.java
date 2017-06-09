package org.microcol.model;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public final class Calendar {
	private final int startYear;
	private final int endYear;

	private int currentYear;

	Calendar(final int startYear, final int endYear) {
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

	boolean isFinished() {
		return currentYear >= endYear;
	}

	void endRound() {
		Preconditions.checkState(!isFinished(), "End year (%s) already reached.", endYear);

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

	void save(final String name, final JsonGenerator generator) {
		generator.writeStartObject(name)
			.write("startYear", startYear)
			.write("endYear", endYear)
			.write("currentYear", currentYear)
			.writeEnd();
	}

	static Calendar load(final JsonParser parser) {
		parser.next(); // START_OBJECT
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_NUMBER
		final int startYear = parser.getInt();
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_NUMBER
		final int endYear = parser.getInt();
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_NUMBER
		final int currentYear = parser.getInt();
		parser.next(); // END_OBJECT

		Calendar calendar = new Calendar(startYear, endYear);
		calendar.currentYear = currentYear;

		return calendar;
	}
}
