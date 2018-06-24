package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CalendarInvalidCreationTest {
	@Parameters(name = "{index}: startYear = {0}, endYear = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ 1750,  1590},
			{ 1590,  1590},
			{-1590, -1750},
			{-1590, -1590},
		});
	}

	@Parameter(0)
	public int startYear;

	@Parameter(1)
	public int endYear;

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidCreation() {
		new Calendar(startYear, startYear, 0);
	}
}
