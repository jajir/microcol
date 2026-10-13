package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.QueueItemType;

import com.google.gson.Gson;

/** Real model regressions for queue mutations and mixed-order persistence. */
class ConstructionQueueIntegrityTest {
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
        // The real fixture has one resident and the initial warehouse.
        queue = colony.getColonyBuildingQueue();
        assertEquals(1, colony.getUnitsInsideColony().size());
        assertTrue(queue.getBuildingQueue().isEmpty());
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    @Test
    void missingInsertionAnchorCannotAddAProjectOrConsumeItsId() {
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addBeforeItem(ConstructionType.WAREHOUSE, 999));
        assertEquals(before, snapshot());
        assertEquals(0, queue.addAtEnd(ConstructionType.WAREHOUSE).getId());
    }

    @Test
    void missingMoveAnchorPreservesMembershipProgressAndOrder() {
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        item.addHammers(17);
        final ColonyBuildingItemProgressConstruction foreign = new ColonyBuildingItemProgressConstruction(
                new ColonyBuildingItemConstruction(ConstructionType.DOCK), 999);
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.moveItemBefore(item, foreign));
        assertEquals(before, snapshot());
        assertThrows(IllegalArgumentException.class,
                () -> queue.moveItemAtTheEnd(foreign));
        assertEquals(before, snapshot());
    }

    @Test
    void duplicateProjectIsAnExplicitRejectionWithoutMutation() {
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        item.addHammers(17);
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.WAREHOUSE));
        assertEquals(before, snapshot());
    }

    @Test
    void mixedQueueRetainsOrderIdentityAndProgressOnReload() {
        final ColonyBuildingItemProgressUnit wagon = queue
                .addAtEnd(UnitType.WAGON);
        final ColonyBuildingItemProgressConstruction warehouse = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        wagon.addHammers(7);
        warehouse.addHammers(17);
        assertEquals(List.of(QueueItemType.UNIT, QueueItemType.CONSTRUCTION),
                queue.save().stream().map(item -> item.getType())
                        .collect(Collectors.toList()));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final List<ColonyBuildingItemProgress<?>> items = restored
                .getColonyByName("Delft").orElseThrow().getColonyBuildingQueue()
                .getBuildingQueue();
        assertEquals(2, items.size());
        assertEquals(List.of(wagon.getId(), warehouse.getId()), items.stream()
                .map(item -> item.getId()).collect(Collectors.toList()));
        assertEquals(List.of(7, 17),
                items.stream().map(item -> item.getBuildHammers())
                        .collect(Collectors.toList()));
        assertEquals(UnitType.WAGON,
                ((ColonyBuildingItemProgressUnit) items.get(0)).getUnitType());
        assertEquals(ConstructionType.WAREHOUSE,
                ((ColonyBuildingItemProgressConstruction) items.get(1))
                        .getConstructionType());
    }

    private void growToFourResidents() {
        final Construction hall = colony
                .getConstructionByType(ConstructionType.TOWN_HALL);
        for (int slot = 0; slot < 3; slot++) {
            model.addUnitOutSideColony(colony)
                    .placeToColonyStructureSlot(hall.getSlotAt(slot));
        }
        assertEquals(4, colony.getUnitsInsideColony().size());
    }

    @Test
    void visitingColonistsDoNotSatisfyThePopulationGate() {
        for (int count = 0; count < 3; count++) {
            model.addUnitOutSideColony(colony);
        }
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP));
        assertEquals(before, snapshot());
        assertEquals(1, colony.getUnitsInsideColony().size());
    }

    @Test
    void admittedProjectWaitsForItsLastToolButSurvivesPopulationLoss() {
        growToFourResidents();
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        final ColonyBuildingItemProgressConstruction following = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        following.addHammers(17);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 64));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 19));
        queue.startTurn();
        assertEquals(64, item.getBuildHammers());
        assertEquals(19,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertTrue(colony.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_HOUSE));
        assertEquals(List.of(item, following), queue.getBuildingQueue());
        final Unit resident = colony
                .getConstructionByType(ConstructionType.TOWN_HALL).getSlotAt(0)
                .getUnit();
        resident.placeToLocation(colony.getLocation());
        assertEquals(3, colony.getUnitsInsideColony().size());
        colony.getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, 1));
        queue.startTurn();
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertFalse(colony.getConstructions().stream()
                .anyMatch(construction -> construction
                        .getType() == ConstructionType.BLACKSMITHS_HOUSE));
        assertEquals(1,
                colony.getConstructions().stream()
                        .filter(construction -> construction
                                .getType() == ConstructionType.BLACKSMITHS_SHOP)
                        .count());
        assertEquals(List.of(following), queue.getBuildingQueue());
        assertEquals(17, following.getBuildHammers());
        final String completed = snapshot();
        queue.startTurn();
        assertEquals(completed, snapshot());
    }

    @Test
    void missingLivePredecessorCannotConsumeToolsOrLoseTheQueueEntry() {
        growToFourResidents();
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        item.addHammers(64);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 20));
        colony.getConstructions().remove(colony
                .getConstructionByType(ConstructionType.BLACKSMITHS_HOUSE));
        final String before = snapshot();
        assertDoesNotThrow(() -> queue.startTurn());
        assertEquals(before, snapshot());
        assertEquals(List.of(item), queue.getBuildingQueue());
    }

    @Test
    void reorderAndRemovalPreserveOtherProjectProgress() {
        final ColonyBuildingItemProgressUnit wagon = queue
                .addAtEnd(UnitType.WAGON);
        final ColonyBuildingItemProgressConstruction warehouse = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        wagon.addHammers(7);
        warehouse.addHammers(17);
        queue.moveItemBefore(warehouse, wagon);
        assertEquals(List.of(warehouse, wagon), queue.getBuildingQueue());
        final String before = snapshot();
        queue.moveItemBefore(warehouse, warehouse);
        assertEquals(before, snapshot());
        queue.moveItemAtTheEnd(warehouse);
        assertEquals(List.of(wagon, warehouse), queue.getBuildingQueue());
        queue.removeItem(wagon);
        assertEquals(List.of(warehouse), queue.getBuildingQueue());
        assertEquals(17, warehouse.getBuildHammers());
        final String removed = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.removeItem(wagon));
        assertEquals(removed, snapshot());
    }

    @Test
    void invalidUnitAdmissionAndInsertionDoNotChangeTheQueue() {
        final String before = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd((UnitType) null));
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(UnitType.COLONIST));
        assertThrows(IllegalArgumentException.class,
                () -> queue.addBeforeItem(UnitType.WAGON, 999));
        assertEquals(before, snapshot());
        colony.getConstructions().remove(
                colony.getConstructionByType(ConstructionType.LUMBER_MILL));
        final String withoutMill = snapshot();
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(UnitType.WAGON));
        assertEquals(withoutMill, snapshot());
    }

    @Test
    void exhaustedQueueIdentityRejectsBeforeInsertion() {
        final ColonyBuildingItemProgressConstruction item = new ColonyBuildingItemProgressConstruction(
                new ColonyBuildingItemConstruction(ConstructionType.WAREHOUSE),
                Integer.MAX_VALUE);
        final ColonyBuildingQueue fullIds = new ColonyBuildingQueue(model,
                colony, List.of(item));
        item.addHammers(17);
        assertThrows(IllegalArgumentException.class,
                () -> fullIds.addAtEnd(UnitType.WAGON));
        assertEquals(List.of(item), fullIds.getBuildingQueue());
        assertEquals(17, item.getBuildHammers());
        assertEquals(Integer.MAX_VALUE, item.getId());
    }

    @Test
    void progressDisplayIncludesWorkAlreadyAssignedToTheProject() {
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        item.addHammers(70);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 10));
        final BuildingStatus<ColonyBuildingItem> status = queue
                .getActuallyBuildingStat().orElseThrow();
        assertEquals(80, status.getHammers().getAlreadyHave());
        assertEquals(java.util.Optional.of(0), status.getTurnsToComplete());
        assertEquals(70, item.getBuildHammers());
        assertEquals(10,
                colony.getWarehouse().getGoods(GoodsType.HAMMERS).getAmount());
    }

    @Test
    void targetBuiltElsewhereLeavesItsQueuedProjectWaiting() {
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        item.addHammers(80);
        colony.createConstruction(ConstructionType.WAREHOUSE);
        final String before = snapshot();
        assertEquals(ColonyBuildingQueue.UnavailableReason.ALREADY_BUILT,
                queue.getCompletionUnavailableReason().orElseThrow());
        queue.startTurn();
        assertEquals(before, snapshot());
        assertEquals(List.of(item), queue.getBuildingQueue());
    }

    @Test
    void abandonedColonyCannotSpendResourcesOnItsOldQueue() {
        final ColonyBuildingItemProgressConstruction item = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        item.addHammers(80);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 20));
        model.destroyColony(colony);
        final String before = snapshot();
        assertEquals(ColonyBuildingQueue.UnavailableReason.COLONY_UNAVAILABLE,
                queue.getCompletionUnavailableReason().orElseThrow());
        queue.startTurn();
        assertEquals(before, snapshot());
        assertEquals(List.of(item), queue.getBuildingQueue());
        assertEquals(20,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
    }

    @Test
    void unitProjectWaitsIfItsRequiredMillDisappears() {
        final ColonyBuildingItemProgressUnit item = queue
                .addAtEnd(UnitType.WAGON);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 40));
        colony.getConstructions().remove(
                colony.getConstructionByType(ConstructionType.LUMBER_MILL));
        final String before = snapshot();
        assertEquals(ColonyBuildingQueue.UnavailableReason.MISSING_PREDECESSOR,
                queue.getCompletionUnavailableReason().orElseThrow());
        queue.startTurn();
        assertEquals(before, snapshot());
        assertEquals(List.of(item), queue.getBuildingQueue());
    }

}
