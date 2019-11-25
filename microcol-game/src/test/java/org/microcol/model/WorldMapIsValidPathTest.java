package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test method isValid from {@link WorldMap}.
 */
public class WorldMapIsValidPathTest extends AbstractMapTest {

    private final static String TEST_SMALL_MAP = "/maps/test-map-ocean-10x10.json";

    private final WorldMap map = loadPredefinedWorldMap(TEST_SMALL_MAP);

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                /*
                 * Valid paths.
                 */
                arguments(Arrays.asList(Location.of(1, 1), Location.of(2, 2), Location.of(3, 3)),
                        true),
                arguments(Arrays.asList(Location.of(10, 10), Location.of(9, 9), Location.of(8, 8)),
                        true),
                arguments(Arrays.asList(Location.of(4, 6), Location.of(5, 5), Location.of(6, 4)),
                        true),
                /*
                 * Invalid paths.
                 */
                arguments(Arrays.asList(Location.of(2, 2), Location.of(1, 1), Location.of(0, 0)),
                        false),
                arguments(
                        Arrays.asList(Location.of(9, 9), Location.of(10, 10), Location.of(11, 11)),
                        false));
    }

    @ParameterizedTest(name = "{index}: path isValid= {1}, locations = {0}")
    @MethodSource("dataProvider")
    public void testValidPath(final List<Location> locations, final boolean isValid) {
        final Path path = Path.of(locations);
        if (isValid) {
            assertTrue(map.isValid(path), "Path should be valid but was evaluated as invalid");
        } else {
            assertFalse(map.isValid(path), "Path should be invalid but was evaluated as valid");
        }
    }

    @Test
    void test_isValid_null() throws Exception {
        assertThrows(NullPointerException.class, () -> map.isValid((Path) null));
    }
}
