package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColonyBuildingQueueTest_queue {

    private ColonyBuildingQueue queue;

    private final Colony colony = mock(Colony.class);

    private final Model model = mock(Model.class);

    private final ArrayList<ColonyBuildingItemProgress<?>> initialBuildingQueue = new ArrayList<>();

    @Test
    public void test_that_buildingQueue_isReady() throws Exception {
        assertNotNull(queue.getBuildingQueue());
    }

    @Test
    public void test_that_getBuildigItemsConstruction_is_builded_correctly() throws Exception {
        when(colony.isContainsConstructionByType(ConstructionType.FUR_TRADING_POST))
                .thenReturn(true);

        final List<ColonyBuildingItemConstruction> list = queue.getBuildigItemsConstruction();
        assertFalse(getByType(list, ConstructionType.FUR_TRADING_POST).isPresent(),
                "Building should not be in list because is already builded.");
        assertTrue(getByType(list, ConstructionType.FUR_TRADERS_HOUSE).isPresent(),
                "Building should be in list because is already builded.");
        assertFalse(getByType(list, ConstructionType.FUR_FACTORY).isPresent(),
                "Building should not be in list because required building is not builded.");
    }

    private Optional<ColonyBuildingItemConstruction> getByType(
            final List<ColonyBuildingItemConstruction> list,
            final ConstructionType constructionType) {
        return list.stream().filter(item -> item.getConstructionType().equals(constructionType))
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
