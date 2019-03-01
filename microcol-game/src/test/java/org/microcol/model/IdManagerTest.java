package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.store.ModelDao;
import org.microcol.model.store.ModelPo;

public class IdManagerTest {

    private ModelDao modelDao;

    @Test
    public void start_from_zero() throws Exception {
        IdManager idm = new IdManager(0);

        assertEquals(0, idm.nextId());
    }

    @Test
    public void start_from_invalid_value() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    new IdManager(-1);
                });

        assertEquals("lastUsedId have to by equals or greater than 0.", exception.getMessage());
    }

    @Test
    public void start_from_modelPo() throws Exception {
        ModelPo modelPo = modelDao.loadPredefinedModel("/maps/test-idManager.microcol");

        IdManager idm = IdManager.makeFromModelPo(modelPo);

        assertNotNull(idm);
        assertEquals(4, idm.nextId());
    }

    @Test
    public void test_sequence() throws Exception {
        IdManager idm = new IdManager(6);

        assertEquals(6, idm.nextId());
        assertEquals(7, idm.nextId());
        assertEquals(8, idm.nextId());
        assertEquals(9, idm.nextId());
    }

    @BeforeEach
    public void setup() {
        modelDao = new ModelDao();
    }

    @AfterEach
    public void tearDown() {
        modelDao = null;

    }

}
