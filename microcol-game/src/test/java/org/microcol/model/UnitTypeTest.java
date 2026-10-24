package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class UnitTypeTest {

    @Test
    public void test_valueOf_noSuchUnitType() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> {
            UnitType.valueOf("NoSuchUnitType");
        });
    }

    @Test
    public void test_forValue() throws Exception {
        UnitType unitType = UnitType.valueOf("COLONIST");

        assertNotNull(unitType);
        assertSame(UnitType.COLONIST, unitType);
    }

    @Test
    public void test_equals() throws Exception {
        assertTrue(UnitType.COLONIST.equals(UnitType.COLONIST));
        assertFalse(UnitType.COLONIST.equals(UnitType.FRIGATE));
    }

}
