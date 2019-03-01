package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class PathTest {

    @Test
    public void testCreationNull() {
        assertThrows(NullPointerException.class, () -> {
            Path.of(null);
        });
    }

    @Test
    public void testCreationNullElement() {
        assertThrows(NullPointerException.class, () -> {
            Path.of(Arrays.asList(Location.of(2, 3), null, Location.of(3, 2)));
        });

    }

    @Test
    public void testCreationEmpty() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    Path.of(new ArrayList<>());
                });

        assertTrue(exception.getMessage().contains("Path cannot be empty."),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void testCreationInvalidSame() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    Path.of(Arrays.asList(Location.of(2, 3), Location.of(2, 3)));
                });

        assertTrue(exception.getMessage().contains("Locations are not neighbors:"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void testCreationInvalidAdjacent() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    Path.of(Arrays.asList(Location.of(2, 3), Location.of(4, 3)));
                });

        assertTrue(exception.getMessage().contains("Locations are not neighbors:"),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }

    @Test
    public void testImmutable() {
        final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

        assertThrows(UnsupportedOperationException.class, () -> {
            path.getLocations().add(Location.of(3, 2));
        });
    }

    @Test
    public void testContains() {
        final List<Location> locations = Arrays.asList(Location.of(2, 3), Location.of(3, 3),
                Location.of(3, 4));
        final Path path = Path.of(locations);

        for (Location location : locations) {
            assertTrue(path.contains(location), String.format("%s not found.", location));
        }
    }

    @Test
    public void testContainsNull() {
        final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

        assertThrows(NullPointerException.class, () -> {
            path.contains(null);
        });

    }

    @Test
    public void testContainsAny() {
        final Path path = Path
                .of(Arrays.asList(Location.of(2, 3), Location.of(3, 3), Location.of(3, 4)));
        final List<Location> locations = Arrays.asList(Location.of(1, 1), Location.of(2, 2),
                Location.of(3, 3));

        assertTrue(path.containsAny(locations));
    }

    @Test
    public void testContainsAnyNull() {
        final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

        assertThrows(NullPointerException.class, () -> {
            path.containsAny(null);
        });
    }

    @Test
    public void testContainsAnyNullInside() {
        final Path path = Path.of(Arrays.asList(Location.of(2, 3)));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    path.containsAny(Arrays.asList(Location.of(2, 3), null, Location.of(3, 2)));
                });

        assertTrue(exception.getMessage().contains("contain null element."),
                String.format("Invalid exception message '%s'.", exception.getMessage()));
    }
}
