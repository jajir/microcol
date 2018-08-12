package org.microcol.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LocationNeighborsTest {
	@Parameters(name = "{index}: {0}, neighbors = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
			{Location.of(0, 0),  Direction.getVectors()},
			{Location.of(1, 1), Arrays.asList(
				Location.of(1, 0),
				Location.of(2, 0),
				Location.of(2, 1),
				Location.of(2, 2),
				Location.of(1, 2),
				Location.of(0, 2),
				Location.of(0, 1),
				Location.of(0, 0))},
			{Location.of(3, 2),  Arrays.asList(
				Location.of(3, 1),
				Location.of(4, 1),
				Location.of(4, 2),
				Location.of(4, 3),
				Location.of(3, 3),
				Location.of(2, 3),
				Location.of(2, 2),
				Location.of(2, 1))},
			{Location.of(-3, 2),  Arrays.asList(
				Location.of(-3, 1),
				Location.of(-2, 1),
				Location.of(-2, 2),
				Location.of(-2, 3),
				Location.of(-3, 3),
				Location.of(-4, 3),
				Location.of(-4, 2),
				Location.of(-4, 1))},
			{Location.of(3, -2),  Arrays.asList(
				Location.of(3, -3),
				Location.of(4, -3),
				Location.of(4, -2),
				Location.of(4, -1),
				Location.of(3, -1),
				Location.of(2, -1),
				Location.of(2, -2),
				Location.of(2, -3))},
			{Location.of(-3, -2),  Arrays.asList(
				Location.of(-3, -3),
				Location.of(-2, -3),
				Location.of(-2, -2),
				Location.of(-2, -1),
				Location.of(-3, -1),
				Location.of(-4, -1),
				Location.of(-4, -2),
				Location.of(-4, -3))},
		});
	}

	@Parameter(0)
	public Location location;

	@Parameter(1)
	public List<Location> neighbors;

	@Test
	public void testNeighbors() {
		Assert.assertEquals(neighbors, location.getNeighbors());
	}
}
