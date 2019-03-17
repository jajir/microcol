package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonSyntaxException;

public class ModelDaoTest extends AbstractMapTest {

    @Test
    public void test_invalid_file_format() {
        assertThrows(JsonSyntaxException.class, () -> {
            getDao().loadFromClassPath("/maps/test-map-invalid-0x0.json");
        });
    }

    @Test
    public void test_load_missingFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            getDao().loadFromClassPath("/maps/test-map-missing-0x0.json");
        });
    }

    @Test
    public void testValidLocationNull() {
        WorldMap map = loadPredefinedWorldMap("/maps/test-map-ocean-10x10.json");

        assertThrows(NullPointerException.class, () -> {
            map.isValid((Location) null);
        });
    }

    @Test
    public void testValidPathNull() {
        WorldMap map = loadPredefinedWorldMap("/maps/test-map-ocean-10x10.json");

        assertThrows(NullPointerException.class, () -> {
            map.isValid((Path) null);
        });
    }

}
