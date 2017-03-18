package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LocationCreationTest {
	@Parameters(name = "{index}: x = {0}, y = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{ 0,  0},
			{ 1,  1},
			{ 2,  3},
			{-2,  3},
			{ 2, -3},
			{-2, -3},
		});
	}

	@Parameter(0)
	public int x;

	@Parameter(1)
	public int y;

	@Test
	public void testCreation() {
		Location location = Location.of(x, y);

		Assert.assertEquals("Test of X-axis failed:", x, location.getX());
		Assert.assertEquals("Test of Y-axis failed:", y, location.getY());
	}
}
