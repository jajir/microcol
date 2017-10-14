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
public class LocationNotEqualsTest {
	
	@Parameters(name = "{index}: location = {0}, object = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{Location.of(2, 3), null},
			{Location.of(2, 3), 10},
			{Location.of(2, 3), "test"},
			{Location.of(2, 3), Location.of( 3,  2)},
			{Location.of(2, 3), Location.of(-2,  3)},
			{Location.of(2, 3), Location.of( 2, -3)},
			{Location.of(2, 3), Location.of(-2, -3)},
		});
	}

	@Parameter(0)
	public Location location;

	@Parameter(1)
	public Object object;

	@Test
	public void testNotEquals() {
		Assert.assertFalse(location.equals(object));
	}
}
