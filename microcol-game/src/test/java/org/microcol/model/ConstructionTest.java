package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ConstructionTest {

    private final Model model = mock(Model.class);

    private final Colony colony = mock(Colony.class);

    private final Unit colonist = mock(Unit.class);

    private final Unit colonist1 = mock(Unit.class);

    private final Unit colonist2 = mock(Unit.class);

    @Test
    public void test_getOrderedSlots_noUnits() throws Exception {
        Construction blacksmith = Construction.build(model, colony,
                ConstructionType.BLACKSMITHS_HOUSE);

        List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

        assertEquals(3, slots.size());
        assertTrue(slots.get(0).isEmpty());
        assertTrue(slots.get(1).isEmpty());
        assertTrue(slots.get(2).isEmpty());
    }

    @Test
    public void test_getOrderedSlots_one_freeColonists() {
        Construction blacksmith = Construction.build(model, colony,
                ConstructionType.BLACKSMITHS_HOUSE);
        blacksmith.placeWorker(1, colonist);

        when(colonist.getType()).thenReturn(UnitType.COLONIST);

        List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

        assertEquals(3, slots.size());
        assertFalse(slots.get(0).isEmpty());
        assertTrue(slots.get(1).isEmpty());
        assertTrue(slots.get(2).isEmpty());
    }

    @Test
    public void test_getOrderedSlots_one_freeColonists_one_ExpertBlacksmith() {
        Construction blacksmith = Construction.build(model, colony,
                ConstructionType.BLACKSMITHS_HOUSE);
        blacksmith.placeWorker(1, colonist1);
        blacksmith.placeWorker(2, colonist2);

        when(colonist1.getType()).thenReturn(UnitType.EXPERT_ORE_MINER);
        when(colonist2.getType()).thenReturn(UnitType.COLONIST);

        List<ConstructionSlot> slots = blacksmith.getOrderedSlots();

        assertEquals(3, slots.size());
        assertFalse(slots.get(0).isEmpty());
        assertFalse(slots.get(1).isEmpty());
        assertTrue(slots.get(2).isEmpty());
        assertSame(colonist1, slots.get(0).getUnit());
        assertSame(colonist2, slots.get(1).getUnit());
    }

}
