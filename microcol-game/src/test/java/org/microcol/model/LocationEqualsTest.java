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
public class LocationEqualsTest {
	@Parameters(name = "{index}: location1 = {0}, location2 = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{Location.of( 0,  0), Location.of( 0,  0)},
			{Location.of( 1,  1), Location.of( 1,  1)},
			{Location.of( 3,  2), Location.of( 3,  2)},
			{Location.of(-3,  2), Location.of(-3,  2)},
			{Location.of( 3, -2), Location.of( 3, -2)},
			{Location.of(-3, -2), Location.of(-3, -2)},
		});
	}

	@Parameter(0)
	public Location location1;

	@Parameter(1)
	public Location location2;

	@Test
	public void testEquals() {
		Assert.assertTrue("Test of location1 and location2 failed.", location1.equals(location2));
		Assert.assertTrue("Test of location2 and location1 failed.", location2.equals(location1));
	}
}
