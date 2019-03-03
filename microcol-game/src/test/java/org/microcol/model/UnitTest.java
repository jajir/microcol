package org.microcol.model;

import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.collect.Lists;

/**
 * Verify basic unit operations.
 * <p>
 * This test shows how to mock all unit's private final members.
 * </p>
 */
public class UnitTest extends AbstractUnitFreeColonistTest {

    private final PlaceEuropePier placeEuropePier = mock(PlaceEuropePier.class);
    
    private final Unit u1 = mock(Unit.class);
    
    private final Unit u2 = mock(Unit.class);

    @Test
    public void test_isSameOwner_emptyList() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertEquals(4, unit.getId());

        boolean ret = unit.isSameOwner(new ArrayList<>());

        assertTrue(ret);
    }

    @Test
    public void test_isSameOwner_oneUnit_different() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);
        
        assertNotNull(unit);
        assertNotNull(u1);
        
        when(u1.getOwner()).thenReturn(null);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1));

        assertFalse(ret);
    }

    @Test
    public void test_isSameOwner_oneUnit_sameOwner() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertNotNull(unit);
        assertNotNull(u1);
        when(u1.getOwner()).thenReturn(owner);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1));

        assertTrue(ret);
    }

    @Test
    public void test_isSameOwner_twoUnit_bothSameOwner() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertNotNull(unit);
        assertNotNull(u1);
        when(u1.getOwner()).thenReturn(owner);
        when(u2.getOwner()).thenReturn(owner);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1, u2));

        assertTrue(ret);
    }

    @Test
    public void test_isSameOwner_twoUnit_secondIsDifferentOwner() throws Exception {
        makeColonist(model, 4, placeEuropePier, owner, 10);

        assertNotNull(unit);
        assertNotNull(u1);
        when(u1.getOwner()).thenReturn(owner);
        when(u2.getOwner()).thenReturn(null);

        boolean ret = unit.isSameOwner(Lists.newArrayList(u1, u2));

        assertFalse(ret);
    }

}
