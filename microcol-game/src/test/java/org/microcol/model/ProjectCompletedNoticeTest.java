package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.turnevent.TurnEventProjectCompleted;

/** A finished head project gives its owner one routine notice per update. */
class ProjectCompletedNoticeTest {
    private Model model;
    private Colony colony;
    private ColonyBuildingQueue queue;

    @BeforeEach
    void prepare() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        colony = model.getColonyByName("Delft").orElseThrow();
        queue = colony.getColonyBuildingQueue();
        final Construction hall = colony
                .getConstructionByType(ConstructionType.TOWN_HALL);
        for (int slot = 0; slot < 3; slot++) {
            model.addUnitOutSideColony(colony)
                    .placeToColonyStructureSlot(hall.getSlotAt(slot));
        }
        assertTrue(queue.getBuildingQueue().isEmpty());
    }

    private List<TurnEventProjectCompleted> completedEvents() {
        return model.getTurnEventsLocalizedMessages(colony.getOwner()).stream()
                .filter(TurnEventProjectCompleted.class::isInstance)
                .map(TurnEventProjectCompleted.class::cast)
                .collect(Collectors.toList());
    }

    @Test
    void finishedConstructionIsReportedOnceWithItsColony() {
        final ColonyBuildingItemProgressConstruction shop = queue
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 64));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 20));

        queue.startTurn();

        assertTrue(colony.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_SHOP));
        assertFalse(queue.getBuildingQueue().contains(shop));
        final List<TurnEventProjectCompleted> events = completedEvents();
        assertEquals(1, events.size());
        final TurnEventProjectCompleted event = events.get(0);
        assertEquals(colony.getId(), event.getColonyId());
        assertEquals("Delft", event.getColonyName());
        assertEquals(QueueItemType.CONSTRUCTION, event.getProjectKind());
        assertEquals(ConstructionType.BLACKSMITHS_SHOP,
                event.getConstructionType().orElseThrow());

        // An update with an empty queue finishes nothing and repeats nothing.
        queue.startTurn();
        assertEquals(1, completedEvents().size());
    }

    @Test
    void savedGameKeepsTheNoticeOnceAndLoadingAddsNoOther() {
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 64));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 20));
        queue.startTurn();
        assertEquals(1, completedEvents().size());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());

        final Colony delft = restored.getColonyByName("Delft").orElseThrow();
        final List<TurnEventProjectCompleted> events = restored
                .getTurnEventsLocalizedMessages(delft.getOwner()).stream()
                .filter(TurnEventProjectCompleted.class::isInstance)
                .map(TurnEventProjectCompleted.class::cast)
                .collect(Collectors.toList());
        assertEquals(1, events.size());
        assertEquals(delft.getId(), events.get(0).getColonyId());
        assertEquals(ConstructionType.BLACKSMITHS_SHOP,
                events.get(0).getConstructionType().orElseThrow());
    }

}
