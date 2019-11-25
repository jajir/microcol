package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.UnitPo;

import com.google.common.collect.Lists;

public class IdManagerTest {

    private UnitPo unit = mock(UnitPo.class);
    private ModelPo modelPo = mock(ModelPo.class);

    @Test
    public void test_constructor_zero() throws Exception {
        IdManager idm = new IdManager(0);

        assertEquals(0, idm.nextId());
    }

    @Test
    public void test_constructor_invalid_value() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    new IdManager(-1);
                });

        assertEquals("lastUsedId have to by equals or greater than 0.", exception.getMessage());
    }

    @Test
    public void test_makeFromModelPo_no_units() throws Exception {
        when(modelPo.getUnits()).thenReturn(new ArrayList<>());
        IdManager idm = IdManager.makeFromModelPo(modelPo);

        assertNotNull(idm);
        assertEquals(0, idm.nextId());
    }

    @Test
    public void test_makeFromModelPo_4_units() throws Exception {
        when(modelPo.getUnits()).thenReturn(Lists.newArrayList(unit, unit, unit));
        when(unit.getId()).thenReturn(0, 1, 3);
        IdManager idm = IdManager.makeFromModelPo(modelPo);

        assertNotNull(idm);
        assertEquals(4, idm.nextId());
    }

    @Test
    public void test_nextId() throws Exception {
        IdManager idm = new IdManager(6);

        assertEquals(6, idm.nextId());
        assertEquals(7, idm.nextId());
        assertEquals(8, idm.nextId());
        assertEquals(9, idm.nextId());
    }

}
