package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Sea-lane entry and direction are separate from the eventual crossing. */
class HighSeaPolicyTest {
    private final Model model = mock(Model.class);
    private final WorldMap map = mock(WorldMap.class);
    private final Map<Location, TerrainType> terrain = new HashMap<>();
    private final HighSea sea = new HighSea(model);
    private int width = 10;
    private int height = 7;

    @BeforeEach
    void configureBothCoasts() {
        when(model.getMap()).thenReturn(map);
        when(map.getMaxLocationX()).thenAnswer(call -> width);
        when(map.getMaxLocationY()).thenAnswer(call -> height);
        when(map.isValid(any(Location.class))).thenAnswer(call -> {
            Location at = call.getArgument(0);
            return at.getX() >= 1 && at.getX() <= width && at.getY() >= 1
                    && at.getY() <= height;
        });
        when(map.getTerrainTypeAt(any(Location.class)))
                .thenAnswer(call -> terrain.getOrDefault(call.getArgument(0),
                        TerrainType.OCEAN));
        for (int y = 1; y <= height; y++) {
            for (int x : new int[] { 1, 2, 9, 10 }) {
                terrain.put(Location.of(x, y), TerrainType.HIGH_SEA);
            }
        }
    }

    @Test
    void departureSideIsNearestHorizontalEdgeWithEasternTie() {
        assertEquals(Direction.west,
                sea.getDepartureDirection(Location.of(2, 4)));
        assertEquals(Direction.east,
                sea.getDepartureDirection(Location.of(9, 4)));
        width = 9;
        terrain.put(Location.of(5, 4), TerrainType.HIGH_SEA);
        assertEquals(Direction.east,
                sea.getDepartureDirection(Location.of(5, 4)));
    }

    @Test
    void invalidOriginIsRejected() {
        assertThrows(NullPointerException.class,
                () -> sea.getDepartureDirection(null));
        assertThrows(IllegalArgumentException.class,
                () -> sea.getDepartureDirection(Location.of(11, 4)));
        assertThrows(IllegalArgumentException.class,
                () -> sea.getDepartureDirection(Location.of(6, 4)));
    }

    static Stream<Arguments> gestures() {
        return Stream.of(Arguments.of(8, 4, 9, 4, false),
                Arguments.of(3, 4, 2, 4, false),
                Arguments.of(9, 4, 9, 5, false),
                Arguments.of(2, 4, 2, 3, false),
                Arguments.of(9, 4, 8, 4, false),
                Arguments.of(2, 4, 3, 4, false),
                Arguments.of(9, 4, 10, 4, true), Arguments.of(2, 4, 1, 4, true),
                Arguments.of(9, 4, 10, 5, true), Arguments.of(2, 4, 1, 3, true),
                Arguments.of(10, 4, 11, 4, false),
                Arguments.of(9, 4, 9, 6, false),
                Arguments.of(9, 4, 9, 4, false));
    }

    @ParameterizedTest
    @MethodSource("gestures")
    void onlyOutwardMovementFromALaneStartsDeparture(int x, int y, int targetX,
            int targetY, boolean expected) {
        Unit ship = mock(Unit.class);
        when(ship.isShip()).thenReturn(true);
        when(ship.isAtPlaceLocation()).thenReturn(true);
        when(ship.getLocation()).thenReturn(Location.of(x, y));
        assertEquals(expected,
                sea.isDepartureMove(ship, Location.of(targetX, targetY)));
    }

    @Test
    void colonistsAndOffMapShipsDoNotDepartAgain() {
        Unit unit = mock(Unit.class);
        assertFalse(sea.isDepartureMove(unit, Location.of(10, 4)));
        when(unit.isShip()).thenReturn(true);
        assertFalse(sea.isDepartureMove(unit, Location.of(10, 4)));
        verify(unit, never()).getLocation();
    }

    @Test
    void defaultArrivalIsInnerEasternLaneNearestVerticalCentre() {
        assertEquals(Optional.of(Location.of(9, 4)),
                sea.getDefaultArrivalLocation());
        // The profile choice stays fixed; changing occupants cannot select a
        // new row.
        when(model.getUnitsAt(Location.of(9, 4)))
                .thenReturn(java.util.List.of(mock(Unit.class)));
        assertEquals(Optional.of(Location.of(9, 4)),
                sea.getDefaultArrivalLocation());
    }

    @Test
    void equalDistancesChooseLowerRowThenColumn() {
        height = 6;
        terrain.clear();
        terrain.put(Location.of(9, 4), TerrainType.HIGH_SEA);
        terrain.put(Location.of(9, 3), TerrainType.HIGH_SEA);
        terrain.put(Location.of(7, 3), TerrainType.HIGH_SEA);
        assertEquals(Optional.of(Location.of(7, 3)),
                sea.getDefaultArrivalLocation());
    }

    @Test
    void noEasternLaneAdjacentToOceanHasNoDefault() {
        terrain.clear();
        assertEquals(Optional.empty(), sea.getDefaultArrivalLocation());
        terrain.put(Location.of(2, 4), TerrainType.HIGH_SEA);
        assertEquals(Optional.empty(), sea.getDefaultArrivalLocation());
        for (int x = 1; x <= width; x++) {
            for (int y = 1; y <= height; y++)
                terrain.put(Location.of(x, y), TerrainType.HIGH_SEA);
        }
        assertEquals(Optional.empty(), sea.getDefaultArrivalLocation());
    }
}
