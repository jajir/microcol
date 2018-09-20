package org.microcol.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class ColonyBuildingQueueTest_queue {

    private ColonyBuildingQueue queue;

    private @Mocked Colony colony;

    private @Mocked Model model;

    private ArrayList<ColonyBuildingItemProgress<?>> initialBuildingQueue = new ArrayList<>();

    @Test
    public void test_that_buildingQueue_isReady() throws Exception {
        assertNotNull(queue.getBuildingQueue());
    }

    @Test
    public void test_that_getBuildigItemsConstruction_is_builded_correctly() throws Exception {
        new Expectations() {
            {
                colony.isContainsConstructionByType(ConstructionType.FUR_TRADING_POST);
                result = true;
            }
        };

        final List<ColonyBuildingItemConstruction> list = queue.getBuildigItemsConstruction();
        assertFalse("Building should not be in list because is already builded.",
                getByType(list, ConstructionType.FUR_TRADING_POST).isPresent());
        assertTrue("Building should be in list because is already builded.",
                getByType(list, ConstructionType.FUR_TRADERS_HOUSE).isPresent());
        assertFalse("Building should not be in list because required building is not builded.",
                getByType(list, ConstructionType.FUR_FACTORY).isPresent());
    }

    private Optional<ColonyBuildingItemConstruction> getByType(
            final List<ColonyBuildingItemConstruction> list,
            final ConstructionType constructionType) {
        return list.stream().filter(item -> item.getConstructionType().equals(constructionType))
                .findFirst();
    }

    @Before
    public void setup() {
        queue = new ColonyBuildingQueue(model, colony, initialBuildingQueue);
    }

}
