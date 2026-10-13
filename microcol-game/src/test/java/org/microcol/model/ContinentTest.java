package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class ContinentTest {

    @Test
    void containsAddedLocations() {
        final Continent continent = new Continent(mock(Model.class));
        final Location first = Location.of(22, 12);
        final Location second = Location.of(23, 17);
        continent.add(first);
        continent.add(second);
        assertTrue(continent.contains(first));
        assertTrue(continent.contains(second));
    }
}
