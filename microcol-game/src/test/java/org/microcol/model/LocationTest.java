package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class LocationTest {

    @Test
    public void testAddNull() {
        assertThrows(NullPointerException.class, () -> {
            Location.of(5, 5).add(null);
        });
    }

    @Test
    public void testNeighborNull() {
        assertThrows(NullPointerException.class, () -> {
            Location.of(5, 5).isNeighbor(null);
        });
    }

}
