package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.store.ModelDao;

import com.google.gson.JsonSyntaxException;

public class ModelDaoTest {

    private ModelDao dao;

    @Test
    public void test_invalid_file_format() {
        assertThrows(JsonSyntaxException.class, () -> {
            dao.loadPredefinedModel("/maps/test-map-invalid-0x0.json");
        });
    }

    @Test
    public void test_load_missingFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            dao.loadPredefinedModel("/maps/test-map-missing-0x0.json");
        });
    }

    @Test
    public void testValidLocationNull() {
        WorldMap map = dao.loadPredefinedWorldMap("/maps/test-map-ocean-10x10.json");

        assertThrows(NullPointerException.class, () -> {
            map.isValid((Location) null);
        });
    }

    @Test
    public void testValidPathNull() {
        WorldMap map = dao.loadPredefinedWorldMap("/maps/test-map-ocean-10x10.json");

        assertThrows(NullPointerException.class, () -> {
            map.isValid((Path) null);
        });
    }

    @BeforeEach
    public void before() {
        dao = new ModelDao();
    }

    @AfterEach
    public void after() {
        dao = null;
    }
}
