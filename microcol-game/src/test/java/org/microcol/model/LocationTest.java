package org.microcol.model;

import org.junit.Test;

public class LocationTest {

    @Test(expected = NullPointerException.class)
    public void testAddNull() {
	Location.of(5, 5).add(null);
    }

    @Test(expected = NullPointerException.class)
    public void testNeighborNull() {
	Location.of(5, 5).isNeighbor(null);
    }

}
