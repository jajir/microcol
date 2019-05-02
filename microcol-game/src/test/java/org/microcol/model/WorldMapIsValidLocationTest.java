package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test method isValid from {@link WorldMap}.
 */
public class WorldMapIsValidLocationTest extends AbstractMapTest {

    private final static String TEST_SMALL_MAP = "/maps/test-map-ocean-10x10.json";

    private final WorldMap map = loadPredefinedWorldMap(TEST_SMALL_MAP);

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                /*
                 * Invalid locations
                 */
                arguments(Location.of(0, 0), false), arguments(Location.of(0, 5), false),
                arguments(Location.of(5, 0), false), arguments(Location.of(11, 5), false),
                arguments(Location.of(5, 11), false), arguments(Location.of(11, 11), false),
                arguments(Location.of(-1, -1), false), arguments(Location.of(-5, -5), false),
                /*
                 * Valid locations
                 */
                arguments(Location.of(1, 1), true), arguments(Location.of(10, 1), true),
                arguments(Location.of(10, 10), true), arguments(Location.of(1, 10), true),
                arguments(Location.of(5, 5), true));
    }

    @ParameterizedTest(name = "{index}: location = {0}, isValid= {1}")
    @MethodSource("dataProvider")
    public void test_isValid(final Location location, final boolean isValid) {
        if (isValid) {
            assertTrue(map.isValid(location), String.format(
                    "Location '%s' should be valid but was evaluated as invalid", location));
        } else {
            assertFalse(map.isValid(location), String.format(
                    "Location '%s' should be invalid but was evaluated as valid", location));
        }
    }

    @Test
    void test_isValid_null() throws Exception {
        assertThrows(NullPointerException.class, () -> map.isValid((Location) null));
    }
}
