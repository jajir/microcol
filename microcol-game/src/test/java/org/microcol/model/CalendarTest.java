package org.microcol.model;

import org.junit.Assert;
import org.junit.Test;

public class CalendarTest {
	@Test
	public void testEndRoundPositive() {
		final Calendar calendar = createCalendar(1590, 1593);

		calendar.endRound();
		Assert.assertEquals(1590, calendar.getStartYear());
		Assert.assertEquals(1593, calendar.getEndYear());
		Assert.assertEquals(1591, calendar.getCurrentYear());
		Assert.assertFalse(calendar.isFinished());

		calendar.endRound();
		Assert.assertEquals(1590, calendar.getStartYear());
		Assert.assertEquals(1593, calendar.getEndYear());
		Assert.assertEquals(1592, calendar.getCurrentYear());
		Assert.assertFalse(calendar.isFinished());

		calendar.endRound();
		Assert.assertEquals(1590, calendar.getStartYear());
		Assert.assertEquals(1593, calendar.getEndYear());
		Assert.assertEquals(1593, calendar.getCurrentYear());
		Assert.assertTrue(calendar.isFinished());
	}

	@Test
	public void testEndRoundNegative() {
		final Calendar calendar = createCalendar(-1593, -1590);

		calendar.endRound();
		Assert.assertEquals(-1593, calendar.getStartYear());
		Assert.assertEquals(-1590, calendar.getEndYear());
		Assert.assertEquals(-1592, calendar.getCurrentYear());
		Assert.assertFalse(calendar.isFinished());

		calendar.endRound();
		Assert.assertEquals(-1593, calendar.getStartYear());
		Assert.assertEquals(-1590, calendar.getEndYear());
		Assert.assertEquals(-1591, calendar.getCurrentYear());
		Assert.assertFalse(calendar.isFinished());

		calendar.endRound();
		Assert.assertEquals(-1593, calendar.getStartYear());
		Assert.assertEquals(-1590, calendar.getEndYear());
		Assert.assertEquals(-1590, calendar.getCurrentYear());
		Assert.assertTrue(calendar.isFinished());
	}

	@Test(expected = IllegalStateException.class)
	public void testEndRoundException() {
		final Calendar calendar = createCalendar(1590, 1591);

		calendar.endRound();
		calendar.endRound();
	}
	

	private Calendar createCalendar(final int startYear, final int endYear) {
		return new Calendar(startYear, endYear, startYear, 0);
	}

}
