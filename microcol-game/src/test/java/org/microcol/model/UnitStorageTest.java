package org.microcol.model;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnitStorageTest {

    private UnitStorage unitStorage;

    private final Unit unit = mock(Unit.class);

    private final IdManager idManager = mock(IdManager.class);

    @Test
    public void test_addUnit_nullUnit() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            unitStorage.addUnit(null);
        });
    }

    @Test
    public void test_addUnit_unitWasAlreadyInserted() throws Exception {
        unitStorage.addUnit(unit);
        assertThrows(IllegalArgumentException.class, () -> {
            unitStorage.addUnit(unit);
        });
    }

    @Test
    public void test_thatEmptyStorageDoesntReturnNull() throws Exception {
        assertNotNull(unitStorage.getUnits());
        assertEquals(0, unitStorage.getUnits().size());
    }

    @Test
    public void test_addUnit() throws Exception {
        unitStorage.addUnit(unit);

        assertNotNull(unitStorage.getUnits());
        assertEquals(1, unitStorage.getUnits().size());
    }

    @Test
    public void mapQueryKeepsMembershipSnapshotWhileUnitsAreReplaced()
            throws Exception {
        final Unit first = mock(Unit.class);
        final Unit second = mock(Unit.class);
        final Player owner = mock(Player.class);
        final Location firstLocation = Location.of(1, 1);
        final Location secondLocation = Location.of(2, 2);
        when(first.getId()).thenReturn(1);
        when(second.getId()).thenReturn(2);
        when(first.getLocation()).thenReturn(firstLocation);
        when(second.getLocation()).thenReturn(secondLocation);
        when(first.getPlace()).thenReturn(
                new PlaceLocation(first, firstLocation, Direction.east));
        when(second.getPlace()).thenReturn(
                new PlaceLocation(second, secondLocation, Direction.east));
        when(first.getOwner()).thenReturn(owner);
        when(second.getOwner()).thenReturn(owner);
        when(first.isAtPlaceLocation()).thenReturn(true);
        when(second.isAtPlaceLocation()).thenReturn(true);
        unitStorage.addUnit(first);

        final CountDownLatch readerEntered = new CountDownLatch(1);
        final CountDownLatch releaseReader = new CountDownLatch(1);
        when(first.getPlace()).thenAnswer(invocation -> {
            readerEntered.countDown();
            assertTrue(releaseReader.await(5, TimeUnit.SECONDS));
            return new PlaceLocation(first, firstLocation, Direction.east);
        });
        final ExecutorService readerExecutor = Executors
                .newSingleThreadExecutor();
        try {
            final Future<Map<Location, List<Unit>>> result = readerExecutor
                    .submit(() -> unitStorage.getUnitsAt());
            assertTrue(readerEntered.await(5, TimeUnit.SECONDS));
            unitStorage.remove(first);
            unitStorage.addUnit(second);
            releaseReader.countDown();
            assertEquals(Map.of(firstLocation, List.of(first)),
                    result.get(5, TimeUnit.SECONDS));
            assertEquals(Map.of(secondLocation, List.of(second)),
                    unitStorage.getUnitsAt());
        } finally {
            releaseReader.countDown();
            readerExecutor.shutdownNow();
        }
    }

    @Test
    void mapQueriesUseOnePlaceSnapshotWhilePassengerBoards() {
        final Player owner = mock(Player.class);
        final Player other = mock(Player.class);
        final Location location = Location.of(1, 1);
        final PlaceLocation mapPlace = new PlaceLocation(unit, location,
                Direction.east);
        final Place cargoPlace = mock(PlaceCargoSlot.class);
        when(unit.getOwner()).thenReturn(owner);
        unitStorage.addUnit(unit);
        // A fresh unit location query would now fail: only the captured place
        // is safe.
        when(unit.getLocation())
                .thenThrow(new IllegalStateException("Already aboard"));
        when(unit.getPlace()).thenReturn(mapPlace, cargoPlace);
        assertEquals(List.of(unit), unitStorage.getUnitsAt(location));
        assertTrue(unitStorage.getUnitsAt(location).isEmpty());
        when(unit.getPlace()).thenReturn(mapPlace, cargoPlace);
        assertEquals(List.of(unit), unitStorage.getUnitsAt(owner, location));
        assertTrue(unitStorage.getUnitsAt(owner, location).isEmpty());
        when(unit.getPlace()).thenReturn(mapPlace, cargoPlace);
        assertEquals(List.of(unit),
                unitStorage.getEnemyUnitsAt(other, location));
        assertTrue(unitStorage.getEnemyUnitsAt(other, location).isEmpty());
        when(unit.getPlace()).thenReturn(mapPlace, cargoPlace);
        assertEquals(Map.of(location, List.of(unit)), unitStorage.getUnitsAt());
        assertTrue(unitStorage.getUnitsAt().isEmpty());
        when(unit.getPlace()).thenReturn(mapPlace, cargoPlace);
        assertEquals(Map.of(location, List.of(unit)),
                unitStorage.getUnitsAt(owner));
        assertTrue(unitStorage.getUnitsAt(owner).isEmpty());
        when(unit.getPlace()).thenReturn(mapPlace, cargoPlace);
        assertEquals(Map.of(location, List.of(unit)),
                unitStorage.getEnemyUnitsAt(other));
        assertTrue(unitStorage.getEnemyUnitsAt(other).isEmpty());
    }

    @BeforeEach
    public void setup() {
        unitStorage = new UnitStorage(idManager);
    }

    @AfterEach
    public void tearDown() {
        unitStorage = null;
    }

}
