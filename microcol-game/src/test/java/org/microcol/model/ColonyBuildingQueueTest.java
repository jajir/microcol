package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColonyBuildingQueueTest {

    private ColonyBuildingQueue queue;

    private final Colony colony = mock(Colony.class);

    private final Model model = mock(Model.class);

    private final ArrayList<ColonyBuildingItemProgress<?>> initialBuildingQueue = new ArrayList<>();

    @Test
    public void test_that_buildingQueue_isReady() throws Exception {
        assertNotNull(queue.getBuildingQueue());
    }

    @Test
    public void test_that_getBuildigItemsConstruction_is_builded_correctly()
            throws Exception {
        when(colony.isContainsConstructionByType(
                ConstructionType.FUR_TRADERS_HOUSE)).thenReturn(true);
        final Player owner = mock(Player.class);
        when(colony.isValid()).thenReturn(true);
        when(colony.getOwner()).thenReturn(owner);
        when(model.getCurrentPlayer()).thenReturn(owner);
        when(model.getPlayers()).thenReturn(List.of(owner));
        when(colony.getUnitsInsideColony())
                .thenReturn(java.util.Collections.nCopies(8, mock(Unit.class)));
        final Construction house = mock(Construction.class);
        when(house.getType()).thenReturn(ConstructionType.FUR_TRADERS_HOUSE);
        when(colony.getConstructions()).thenReturn(List.of(house));

        final List<ColonyBuildingItemConstruction> list = queue
                .getBuildigItemsConstruction();
        assertFalse(
                getByType(list, ConstructionType.FUR_TRADERS_HOUSE).isPresent(),
                "Building should not be in list because is already builded.");
        assertTrue(
                getByType(list, ConstructionType.FUR_TRADING_POST).isPresent(),
                "Upgrade should be offered because its predecessor is built.");
        assertFalse(getByType(list, ConstructionType.FUR_FACTORY).isPresent(),
                "Building should not be in list because required building is not builded.");
    }

    private Optional<ColonyBuildingItemConstruction> getByType(
            final List<ColonyBuildingItemConstruction> list,
            final ConstructionType constructionType) {
        return list.stream().filter(
                item -> item.getConstructionType().equals(constructionType))
                .findFirst();
    }

    @BeforeEach
    public void setup() {
        queue = new ColonyBuildingQueue(model, colony, initialBuildingQueue);
    }

    @AfterEach
    public void tearDown() {
        queue = null;
    }

}
