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
public class LocationDistanceTest {
	@Parameters(name = "{index}: location1 = {0}, location2 = {1}, expected = {2}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			// [0, 0]
			{Location.of( 0,  0), Location.of( 0,  0), 0},
			// [1, 1]
			{Location.of( 1,  1), Location.of( 1,  1), 0},
			// [3, 2]
			{Location.of(3, 2), Location.of( 3,  2),  0},
			{Location.of(3, 2), Location.of(-3,  2),  6},
			{Location.of(3, 2), Location.of( 3, -2),  4},
			{Location.of(3, 2), Location.of(-3, -2), 7},
			// [-3, 2]
			{Location.of(-3, 2), Location.of( 3,  2),  6},
			{Location.of(-3, 2), Location.of(-3,  2),  0},
			{Location.of(-3, 2), Location.of( 3, -2), 7},
			{Location.of(-3, 2), Location.of(-3, -2),  4},
			// [3, -2]
			{Location.of(3, -2), Location.of( 3,  2),  4},
			{Location.of(3, -2), Location.of(-3,  2), 7},
			{Location.of(3, -2), Location.of( 3, -2),  0},
			{Location.of(3, -2), Location.of(-3, -2),  6},
			// [-3, -2]
			{Location.of(-3, -2), Location.of( 3,  2), 7},
			{Location.of(-3, -2), Location.of(-3,  2),  4},
			{Location.of(-3, -2), Location.of( 3, -2),  6},
			{Location.of(-3, -2), Location.of(-3, -2),  0},
		});
	}

	@Parameter(0)
	public Location location1;

	@Parameter(1)
	public Location location2;

	@Parameter(2)
	public int expected;

	@Test
	public void testDistance() {
		Assert.assertEquals("Test of location1 and location2 failed.", expected, location1.getDistance(location2));
		Assert.assertEquals("Test of location2 and location1 failed.", expected, location2.getDistance(location1));
	}
}
