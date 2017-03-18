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
public class LocationHashCodeTest {
	@Parameters(name = "{index}: location = {0}, expected = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{Location.of( 0,  0),       0},
			{Location.of( 1,  0),       1},
			{Location.of(-1,  0),      -1},
			{Location.of( 0,  1),   65536},
			{Location.of( 0, -1),  -65536},
			{Location.of( 1,  1),   65537},
			{Location.of(-1, -1),  -65537},
			{Location.of( 2,  3),  196610},
			{Location.of(-2,  3),  196606},
			{Location.of( 2, -3), -196606},
			{Location.of(-2, -3), -196610},
			{Location.of( 3,  2),  131075},
			{Location.of(-3,  2),  131069},
			{Location.of( 3, -2), -131069},
			{Location.of(-3, -2), -131075},
		});
	}

	@Parameter(0)
	public Location location;

	@Parameter(1)
	public int expected;

	@Test
	public void testHashCode() {
		Assert.assertEquals(expected, location.hashCode());
	}
}
