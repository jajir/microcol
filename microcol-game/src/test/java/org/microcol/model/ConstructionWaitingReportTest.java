package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.TurnEventPo;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventConstructionWaitingForTools;

import com.google.gson.Gson;

/** A missing-tools reminder for every owner update blocked only by tools. */
class ConstructionWaitingReportTest {
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
        assertEquals(4, colony.getUnitsInsideColony().size());
        assertTrue(queue.getBuildingQueue().isEmpty());
        assertTrue(waitingEvents().isEmpty());
    }

    private List<TurnEventConstructionWaitingForTools> waitingEvents() {
        return waitingEvents(model);
    }

    private static List<TurnEventConstructionWaitingForTools> waitingEvents(
            final Model model) {
        final Player owner = model.getColonyByName("Delft").orElseThrow()
                .getOwner();
        return model.getTurnEventsLocalizedMessages(owner).stream()
                .filter(TurnEventConstructionWaitingForTools.class::isInstance)
                .map(TurnEventConstructionWaitingForTools.class::cast)
                .collect(Collectors.toList());
    }

    private void stock(final int hammers, final int tools) {
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, hammers));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, tools));
    }

    private int amount(final GoodsType type) {
        return colony.getWarehouse().getGoods(type).getAmount();
    }

    private void nextOwnerUpdate() {
        // The player's Next turn clears the previous report before updating.
        model.getTurnEventStore().clearTurnEventsForPlayer(colony.getOwner());
        queue.startTurn();
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    @Test
    void oneToolShortReportsColonyProjectAndShortageWithoutCompletion() {
        final ColonyBuildingItemProgressConstruction shop = queue
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        stock(64, 19);
        queue.startTurn();
        assertEquals(64, amount(GoodsType.HAMMERS));
        assertEquals(19, amount(GoodsType.TOOLS));
        assertFalse(colony.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_SHOP));
        assertEquals(List.of(shop), queue.getBuildingQueue());
        final List<TurnEventConstructionWaitingForTools> events = waitingEvents();
        assertEquals(1, events.size());
        final TurnEventConstructionWaitingForTools event = events.get(0);
        assertEquals(colony.getOwner().getName(), event.getPlayerName());
        assertEquals(colony.getId(), event.getColonyId());
        assertEquals("Delft", event.getColonyName());
        assertEquals(QueueItemType.CONSTRUCTION, event.getProjectKind());
        assertEquals(Optional.of(ConstructionType.BLACKSMITHS_SHOP),
                event.getConstructionType());
        assertEquals(Optional.empty(), event.getUnitType());
        assertEquals(1, event.getMissingTools());
    }

    @Test
    void missingHammersOrEnoughToolsDoNotReport() {
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        // One hammer short is a hammer wait, even while tools are also short.
        stock(63, 19);
        queue.startTurn();
        assertTrue(waitingEvents().isEmpty());
        assertEquals(63, amount(GoodsType.HAMMERS));
        assertEquals(19, amount(GoodsType.TOOLS));
        // Exact hammers and tools complete once and consume exactly the cost.
        stock(64, 20);
        queue.startTurn();
        assertTrue(waitingEvents().isEmpty());
        assertTrue(colony.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_SHOP));
        assertTrue(queue.getBuildingQueue().isEmpty());
        assertEquals(0, amount(GoodsType.HAMMERS));
        assertEquals(0, amount(GoodsType.TOOLS));
    }

    @Test
    void everyOwnerUpdateRemindsWithItsCurrentShortageUntilCompletion() {
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        final ColonyBuildingItemProgressConstruction following = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        stock(70, 5);
        queue.startTurn();
        assertEquals(1, waitingEvents().size());
        assertEquals(15, waitingEvents().get(0).getMissingTools());
        // The next update repeats the reminder with the current shortage.
        stock(75, 12);
        nextOwnerUpdate();
        assertEquals(1, waitingEvents().size());
        assertEquals(8, waitingEvents().get(0).getMissingTools());
        stock(75, 12);
        nextOwnerUpdate();
        assertEquals(1, waitingEvents().size());
        assertEquals(8, waitingEvents().get(0).getMissingTools());
        assertEquals(75, amount(GoodsType.HAMMERS));
        assertEquals(12, amount(GoodsType.TOOLS));
        // Enough tools complete the shop once without a reminder.
        stock(75, 21);
        nextOwnerUpdate();
        assertTrue(waitingEvents().isEmpty());
        assertTrue(colony.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_SHOP));
        assertEquals(11, amount(GoodsType.HAMMERS));
        assertEquals(1, amount(GoodsType.TOOLS));
        assertEquals(List.of(following), queue.getBuildingQueue());
    }

    @Test
    void eachUpdateAppendsExactlyOneReminderForTheCurrentHead() {
        final ColonyBuildingItemProgressConstruction shop = queue
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        final ColonyBuildingItemProgressConstruction weavers = queue
                .addAtEnd(ConstructionType.WEAVERS_SHOP);
        stock(64, 19);
        queue.startTurn();
        queue.moveItemBefore(weavers, shop);
        stock(64, 10);
        queue.startTurn();
        assertEquals(2, waitingEvents().size());
        assertEquals(Optional.of(ConstructionType.BLACKSMITHS_SHOP),
                waitingEvents().get(0).getConstructionType());
        assertEquals(1, waitingEvents().get(0).getMissingTools());
        assertEquals(Optional.of(ConstructionType.WEAVERS_SHOP),
                waitingEvents().get(1).getConstructionType());
        assertEquals(10, waitingEvents().get(1).getMissingTools());
        assertEquals(List.of(weavers, shop), queue.getBuildingQueue());
        assertEquals(64, amount(GoodsType.HAMMERS));
        assertEquals(10, amount(GoodsType.TOOLS));
    }

    @Test
    void unavailableHeadOrEmptyQueueDoesNotRemind() {
        final ColonyBuildingItemProgressConstruction shop = queue
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        colony.createConstruction(ConstructionType.BLACKSMITHS_SHOP);
        stock(64, 19);
        queue.startTurn();
        assertTrue(waitingEvents().isEmpty());
        assertEquals(List.of(shop), queue.getBuildingQueue());
        assertEquals(64, amount(GoodsType.HAMMERS));
        assertEquals(19, amount(GoodsType.TOOLS));
        queue.removeItem(shop);
        queue.startTurn();
        assertTrue(waitingEvents().isEmpty());
        assertEquals(64, amount(GoodsType.HAMMERS));
        assertEquals(19, amount(GoodsType.TOOLS));
    }

    @Test
    void viewsSavesAndRejectedMutationsAppendNoReminder() {
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        stock(64, 19);
        queue.startTurn();
        final String before = snapshot();
        queue.getActuallyBuildingStat();
        queue.getCompletionUnavailableReason();
        queue.getBuildingQueue();
        queue.getRushQuote();
        model.getTurnEventsLocalizedMessages(colony.getOwner());
        assertThrows(IllegalArgumentException.class,
                () -> queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP));
        final ColonyBuildingItemProgressConstruction foreign = new ColonyBuildingItemProgressConstruction(
                new ColonyBuildingItemConstruction(ConstructionType.DOCK), 999);
        assertThrows(IllegalArgumentException.class,
                () -> queue.removeItem(foreign));
        assertEquals(before, snapshot());
        assertEquals(1, waitingEvents().size());
    }

    @Test
    void savedReminderKeepsItsSnapshotWhenStockChanges() {
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        stock(64, 19);
        queue.startTurn();
        final TurnEventConstructionWaitingForTools event = waitingEvents()
                .get(0);
        stock(64, 2);
        assertEquals(1, event.getMissingTools());
        assertEquals(Integer.valueOf(1), event.save().getMissingTools());
        assertEquals(ConstructionType.BLACKSMITHS_SHOP,
                event.save().getProjectConstructionType());
        assertEquals("Delft", event.save().getColonyName());
    }

    @Test
    void unitProjectReportsItsTypedIdentity() {
        // Delft already has the lumber mill that admits wagons.
        assertTrue(colony
                .isContainsConstructionByType(ConstructionType.LUMBER_MILL));
        final ColonyBuildingItemProgressUnit wagon = queue
                .addAtEnd(UnitType.WAGON);
        final int hammers = UnitType.WAGON.getRequiredHammers();
        final int tools = UnitType.WAGON.getRequiredTools();
        stock(hammers, tools - 3);
        queue.startTurn();
        assertEquals(List.of(wagon), queue.getBuildingQueue());
        assertEquals(1, waitingEvents().size());
        final TurnEventConstructionWaitingForTools event = waitingEvents()
                .get(0);
        assertEquals(QueueItemType.UNIT, event.getProjectKind());
        assertEquals(Optional.of(UnitType.WAGON), event.getUnitType());
        assertEquals(Optional.empty(), event.getConstructionType());
        assertEquals(3, event.getMissingTools());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(Optional.of(UnitType.WAGON),
                waitingEvents(restored).get(0).getUnitType());
        // Supplying the tools completes the wagon instead of reminding.
        stock(hammers, tools);
        nextOwnerUpdate();
        assertTrue(waitingEvents().isEmpty());
        assertTrue(queue.getBuildingQueue().isEmpty());
        assertEquals(0, amount(GoodsType.TOOLS));
    }

    @Test
    void loadAndContinueRestoreTheReportWithoutAppendingAReminder() {
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        stock(64, 19);
        queue.startTurn();
        final ModelPo saved = model.save();
        final Model restored = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        // Loading neither runs construction nor adds another reminder.
        assertEquals(1, waitingEvents(restored).size());
        assertEquals(1, waitingEvents(restored).get(0).getMissingTools());
        restored.startGame();
        assertEquals(1, waitingEvents(restored).size());
        final Colony restoredColony = restored.getColonyByName("Delft")
                .orElseThrow();
        assertEquals(64, restoredColony.getWarehouse()
                .getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(19, restoredColony.getWarehouse().getGoods(GoodsType.TOOLS)
                .getAmount());
        assertFalse(restoredColony.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_SHOP));
        // The following owner update reminds again with the current shortage.
        restored.getTurnEventStore()
                .clearTurnEventsForPlayer(restoredColony.getOwner());
        restoredColony.getColonyBuildingQueue().startTurn();
        assertEquals(1, waitingEvents(restored).size());
        assertEquals(1, waitingEvents(restored).get(0).getMissingTools());
    }

    private static TurnEventPo validEvent() {
        final TurnEventPo po = new TurnEventPo();
        po.setType(TurnEventConstructionWaitingForTools.class.getSimpleName());
        po.setPlayerName("Dutch");
        po.setColonyId(3);
        po.setColonyName("Delft");
        po.setProjectKind(QueueItemType.CONSTRUCTION);
        po.setProjectConstructionType(ConstructionType.BLACKSMITHS_SHOP);
        po.setMissingTools(1);
        return po;
    }

    static Stream<Consumer<TurnEventPo>> malformedEvents() {
        return Stream.of(po -> po.setPlayerName(null),
                po -> po.setColonyId(null), po -> po.setColonyId(-1),
                po -> po.setColonyName(null), po -> po.setColonyName(" "),
                po -> po.setProjectKind(null),
                po -> po.setProjectConstructionType(null),
                po -> po.setProjectConstructionType(ConstructionType.TOWN_HALL),
                po -> po.setProjectUnitType(UnitType.WAGON), po -> {
                    po.setProjectKind(QueueItemType.UNIT);
                    po.setProjectConstructionType(null);
                }, po -> {
                    po.setProjectKind(QueueItemType.UNIT);
                    po.setProjectConstructionType(null);
                    po.setProjectUnitType(UnitType.COLONIST);
                }, po -> po.setMissingTools(null), po -> po.setMissingTools(0),
                po -> po.setMissingTools(-1), po -> po.setMissingTools(21));
    }

    @Test
    void validStoredEventLoadsWithItsFrozenSnapshot() {
        final TurnEventConstructionWaitingForTools event = TurnEventConstructionWaitingForTools
                .tryLoad(validEvent());
        assertEquals("Delft", event.getColonyName());
        assertEquals(3, event.getColonyId());
        assertEquals(1, event.getMissingTools());
        assertEquals(ConstructionType.BLACKSMITHS_SHOP,
                ((ColonyBuildingItemConstruction) event.getProject())
                        .getConstructionType());
        final TurnEventPo resaved = event.save();
        assertEquals(new Gson().toJson(validEvent()),
                new Gson().toJson(resaved));
        final TurnEventPo other = validEvent();
        other.setType("TurnEventColonyWasLost");
        assertNull(TurnEventConstructionWaitingForTools.tryLoad(other));
    }

    @ParameterizedTest
    @MethodSource("malformedEvents")
    void malformedStoredEventIsRejected(final Consumer<TurnEventPo> damage) {
        final TurnEventPo po = validEvent();
        damage.accept(po);
        assertThrows(IllegalArgumentException.class,
                () -> TurnEventConstructionWaitingForTools.tryLoad(po));
    }

    @Test
    void malformedStoredEventRejectsTheWholeModel() {
        final ModelPo saved = model.save();
        final TurnEventPo po = validEvent();
        po.setPlayerName(colony.getOwner().getName());
        po.setMissingTools(0);
        final List<TurnEventPo> events = new java.util.ArrayList<>(
                saved.getTurnEvents());
        events.add(po);
        saved.setTurnEvents(events);
        assertThrows(IllegalArgumentException.class, () -> Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators()));
        final List<TurnEvent> unchanged = model
                .getTurnEventsLocalizedMessages(colony.getOwner());
        assertTrue(unchanged.isEmpty());
    }
}
