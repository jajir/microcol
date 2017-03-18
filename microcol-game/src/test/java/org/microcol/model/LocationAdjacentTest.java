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
public class LocationAdjacentTest {
	@Parameters(name = "{index}: location1 = {0}, location2 = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			// [0, 0]
			{Location.of(0, 0),  Location.of(-1, -1)},
			{Location.of(0, 0),  Location.of( 0, -1)},
			{Location.of(0, 0),  Location.of( 1, -1)},
			{Location.of(0, 0),  Location.of(-1,  0)},
			{Location.of(0, 0),  Location.of( 1,  0)},
			{Location.of(0, 0),  Location.of(-1,  1)},
			{Location.of(0, 0),  Location.of( 0,  1)},
			{Location.of(0, 0),  Location.of( 1,  1)},
			// [1, 1]
			{Location.of(1, 1),  Location.of(0, 0)},
			{Location.of(1, 1),  Location.of(1, 0)},
			{Location.of(1, 1),  Location.of(2, 0)},
			{Location.of(1, 1),  Location.of(0, 1)},
			{Location.of(1, 1),  Location.of(2, 1)},
			{Location.of(1, 1),  Location.of(0, 2)},
			{Location.of(1, 1),  Location.of(1, 2)},
			{Location.of(1, 1),  Location.of(2, 2)},
			// [5, 5]
			{Location.of(5, 5),  Location.of(4, 4)},
			{Location.of(5, 5),  Location.of(5, 4)},
			{Location.of(5, 5),  Location.of(6, 4)},
			{Location.of(5, 5),  Location.of(4, 5)},
			{Location.of(5, 5),  Location.of(6, 5)},
			{Location.of(5, 5),  Location.of(4, 6)},
			{Location.of(5, 5),  Location.of(5, 6)},
			{Location.of(5, 5),  Location.of(6, 6)},
			// [-5, -5]
			{Location.of(-5, -5),  Location.of(-4, -4)},
			{Location.of(-5, -5),  Location.of(-5, -4)},
			{Location.of(-5, -5),  Location.of(-6, -4)},
			{Location.of(-5, -5),  Location.of(-4, -5)},
			{Location.of(-5, -5),  Location.of(-6, -5)},
			{Location.of(-5, -5),  Location.of(-4, -6)},
			{Location.of(-5, -5),  Location.of(-5, -6)},
			{Location.of(-5, -5),  Location.of(-6, -6)},
		});
	}

	@Parameter(0)
	public Location location1;

	@Parameter(1)
	public Location location2;

	@Test
	public void testAdjacent() {
		Assert.assertTrue("Test of location1 and location2 failed.", location1.isAdjacent(location2));
		Assert.assertTrue("Test of location2 and location1 failed.", location2.isAdjacent(location1));
	}
}
