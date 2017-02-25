package org.microcol.model;

import org.junit.Assert;
import org.junit.Test;

public class CalendarTest {
	@Test
	public void testCreation() {
		final Calendar calendar = new Calendar(1590, 1750);

		Assert.assertEquals(1590, calendar.getStartYear());
		Assert.assertEquals(1750, calendar.getEndYear());
		Assert.assertEquals(1590, calendar.getCurrentYear());
	}

	@Test
	public void testCreationValid() {
		new Calendar(1590, 1591);
		new Calendar(0, 1);
		new Calendar(-1, 0);
		new Calendar(-1590, -1589);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalid1() {
		new Calendar(1590, 1590);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalid2() {
		new Calendar(1750, 1590);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreationInvalid3() {
		new Calendar(-1590, -1750);
	}

	@Test
	public void testFinished() {
		final Calendar calendar = new Calendar(1590, 1593);
		Assert.assertFalse(calendar.isFinished());

		calendar.endRound();
		Assert.assertFalse(calendar.isFinished());

		calendar.endRound();
		Assert.assertFalse(calendar.isFinished());

		calendar.endRound();
		Assert.assertTrue(calendar.isFinished());
	}

	@Test
	public void testEndRound() {
		final Calendar calendar = new Calendar(1590, 1593);

		calendar.endRound();
		Assert.assertEquals(1590, calendar.getStartYear());
		Assert.assertEquals(1593, calendar.getEndYear());
		Assert.assertEquals(1591, calendar.getCurrentYear());

		calendar.endRound();
		Assert.assertEquals(1590, calendar.getStartYear());
		Assert.assertEquals(1593, calendar.getEndYear());
		Assert.assertEquals(1592, calendar.getCurrentYear());

		calendar.endRound();
		Assert.assertEquals(1590, calendar.getStartYear());
		Assert.assertEquals(1593, calendar.getEndYear());
		Assert.assertEquals(1593, calendar.getCurrentYear());
	}

	@Test(expected = IllegalStateException.class)
	public void testEndRoundException() {
		final Calendar calendar = new Calendar(1590, 1591);

		calendar.endRound();
		calendar.endRound();
	}
}
