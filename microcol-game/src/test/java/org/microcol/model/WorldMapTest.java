package org.microcol.model;

import org.junit.Test;

public class WorldMapTest {
	@Test(expected = IllegalArgumentException.class)
	public void testCreationEmpty() {
		new WorldMap("/maps/test-map-invalid-0x0.txt");
	}

	@Test(expected = NullPointerException.class)
	public void testValidLocationNull() {
		final WorldMap map = new WorldMap("/maps/test-map-empty-10x10.txt");

		map.isValid((Location) null);
	}

	@Test(expected = NullPointerException.class)
	public void testValidPathNull() {
		final WorldMap map = new WorldMap("/maps/test-map-empty-10x10.txt");

		map.isValid((Path) null);
	}
}
