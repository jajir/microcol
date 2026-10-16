package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AtlanticVoyageDurationTest {
    @Test
    void everyWeatherEdgeDirectionAndShipSpeedUsesTheBoundedFormula() {
        final int[] slowEastOut = { 1, 2, 3 };
        final int[] slowWestOutOrInbound = { 2, 3, 4 };
        final int[] fastEastOut = { 1, 1, 2 };
        final int[] fastWestOutOrInbound = { 1, 2, 3 };
        final UnitType[] ships = { UnitType.CARAVEL, UnitType.MERCHANTMAN,
                UnitType.GALLEON, UnitType.PRIVATEER, UnitType.FRIGATE,
                UnitType.MAN_OF_WAR };
        for (final UnitType ship : ships) {
            for (int index = 0; index < 3; index++) {
                final int weather = index - 1;
                for (final boolean toEurope : new boolean[] { false, true }) {
                    for (final Direction edge : new Direction[] {
                            Direction.east, Direction.west }) {
                        final boolean eastOut = toEurope
                                && edge == Direction.east;
                        final int[] table = ship.getSpeed() >= 6
                                ? eastOut ? fastEastOut : fastWestOutOrInbound
                                : eastOut ? slowEastOut : slowWestOutOrInbound;
                        final int expected = table[index];
                        assertEquals(expected,
                                AtlanticVoyageDuration.calculate(weather,
                                        toEurope, edge, ship.getSpeed()),
                                ship + " weather=" + weather + " toEurope="
                                        + toEurope + " edge=" + edge);
                    }
                }
            }
        }
    }
}
