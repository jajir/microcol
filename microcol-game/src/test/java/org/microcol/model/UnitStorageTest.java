package org.microcol.model;

import static org.mockito.Mockito.*;

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

    @BeforeEach
    public void setup() {
        unitStorage = new UnitStorage(idManager);
    }

    @AfterEach
    public void tearDown() {
        unitStorage = null;
    }

}
