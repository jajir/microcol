package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.model.store.ModelDao;
import org.microcol.model.store.ModelPo;

public class IdManagerTest {

    private ModelDao modelDao;

    @Test
    public void start_from_zero() throws Exception {
        IdManager idm = new IdManager(0);

        assertEquals(0, idm.nextId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void start_from_invalid_value() throws Exception {
        new IdManager(-1);
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

    @Before
    public void setup() {
        modelDao = new ModelDao();
    }

    @After
    public void tearDown() {
        modelDao = null;

    }

}
