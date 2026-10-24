package org.microcol.model.turnevent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.microcol.model.ColonyBuildingItemConstruction;
import org.microcol.model.ConstructionType;
import org.microcol.model.UnitType;
import org.microcol.model.store.QueueItemType;

public class TurnEventProjectCompletedTest {

    private final static String PLAYER_NAME = "Jan";

    @Test
    public void verify_finished_construction_is_routine_and_round_trips() {
        final TurnEventProjectCompleted event = new TurnEventProjectCompleted(
                PLAYER_NAME, 4, "Delft", QueueItemType.CONSTRUCTION,
                ConstructionType.BLACKSMITHS_SHOP, null);

        assertFalse(TurnEventCriticality.isCritical(event));
        assertTrue(
                event.getProject() instanceof ColonyBuildingItemConstruction);
        final TurnEventProjectCompleted loaded = TurnEventProjectCompleted
                .tryLoad(event.save());
        assertEquals(4, loaded.getColonyId());
        assertEquals("Delft", loaded.getColonyName());
        assertEquals(ConstructionType.BLACKSMITHS_SHOP,
                loaded.getConstructionType().orElseThrow());
        assertTrue(loaded.getUnitType().isEmpty());
    }

    @Test
    public void verify_finished_unit_round_trips() {
        final TurnEventProjectCompleted event = new TurnEventProjectCompleted(
                PLAYER_NAME, 4, "Delft", QueueItemType.UNIT, null,
                UnitType.WAGON);

        final TurnEventProjectCompleted loaded = TurnEventProjectCompleted
                .tryLoad(event.save());
        assertEquals(QueueItemType.UNIT, loaded.getProjectKind());
        assertEquals(UnitType.WAGON, loaded.getUnitType().orElseThrow());
        assertTrue(loaded.getConstructionType().isEmpty());
        assertFalse(TurnEventCriticality.isCritical(loaded));
    }

    @Test
    public void verify_construction_cannot_name_a_unit_as_well() {
        assertThrows(IllegalArgumentException.class,
                () -> new TurnEventProjectCompleted(PLAYER_NAME, 4, "Delft",
                        QueueItemType.CONSTRUCTION,
                        ConstructionType.BLACKSMITHS_SHOP, UnitType.GALLEON));
    }

    @Test
    public void verify_blank_colony_name_is_rejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new TurnEventProjectCompleted(PLAYER_NAME, 4, " ",
                        QueueItemType.CONSTRUCTION,
                        ConstructionType.BLACKSMITHS_SHOP, null));
    }

}
