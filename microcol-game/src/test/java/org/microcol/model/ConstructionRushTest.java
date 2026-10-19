package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.event.RoundStartedEvent;

import com.google.gson.Gson;

/**
 * Rush quotes use the same warehouse pool and queue head as normal completion.
 */
class ConstructionRushTest {
    private Model model;
    private Colony colony;
    private ColonyBuildingQueue queue;
    private Player owner;

    @BeforeEach
    void prepare() {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        colony = model.getColonyByName("Delft").orElseThrow();
        queue = colony.getColonyBuildingQueue();
        owner = colony.getOwner();
        assertSame(owner, model.getCurrentPlayer());
        assertTrue(model.isGameRunning());
    }

    private String snapshot() {
        return new Gson().toJson(model.save());
    }

    private void growToFourResidents() {
        final Construction hall = colony
                .getConstructionByType(ConstructionType.TOWN_HALL);
        for (int slot = 0; slot < 3; slot++) {
            model.addUnitOutSideColony(colony)
                    .placeToColonyStructureSlot(hall.getSlotAt(slot));
        }
    }

    @Test
    void exactTaxedQuoteCompletesOnlyHeadAndPersistsOneCharge() {
        growToFourResidents();
        final ColonyBuildingItemProgressConstruction head = queue
                .addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        final ColonyBuildingItemProgressConstruction next = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 34));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 15));
        model.getEurope()
                .getGoodsTradeForType(GoodsType.TOOLS, EuropeanNation.DUTCH)
                .setBuyPrice(3);
        model.getPlayerStore().getKingForPlayer(owner).orElseThrow()
                .setKingsTaxPercentage(10);
        owner.setGold(100);
        final String before = snapshot();
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        assertSame(head.getItem(), quote.getItem());
        assertEquals(34, quote.getHammersUsed());
        assertEquals(15, quote.getToolsUsed());
        assertEquals(30, quote.getMissingHammers());
        assertEquals(5, quote.getMissingTools());
        assertEquals(76, quote.getPrice());
        assertEquals(24, quote.getGoldAfter());
        assertEquals(before, snapshot());
        queue.rush(quote);
        assertEquals(24, owner.getGold());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertTrue(colony.isContainsConstructionByType(
                ConstructionType.BLACKSMITHS_SHOP));
        assertEquals(List.of(next), queue.getBuildingQueue());
        final String completed = snapshot();
        assertThrows(IllegalStateException.class, () -> queue.rush(quote));
        assertEquals(completed, snapshot());
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(completed, new Gson().toJson(restored.save()));
        restored.startGame();
        assertEquals(completed, new Gson().toJson(restored.save()));
    }

    private GoodsTrade tools(final EuropeanNation nation) {
        return model.getEurope().getGoodsTradeForType(GoodsType.TOOLS, nation);
    }

    private void prepareRushNeedingFiveImportedTools() {
        growToFourResidents();
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 34));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 15));
    }

    @Test
    void rushMovesToolPressureOnlyForImportedToolsAfterSettlement() {
        prepareRushNeedingFiveImportedTools();
        owner.setGold(1000);
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        assertEquals(5, quote.getMissingTools());
        // Previewing and quoting twice only read the market.
        queue.getRushQuote().orElseThrow();
        assertEquals(0, tools(EuropeanNation.DUTCH).getPressure());
        assertFalse(tools(EuropeanNation.FRENCH).isTradedSinceUpdate());

        final int goldBefore = owner.getGold();
        queue.rush(quote);

        // Charged at the quoted price; then 5 purchased tools (stocked ones
        // are not purchases) push the Dutch market by -20 (half of -40) and the
        // others by -10 eighths, and mark every market as affected.
        assertEquals(goldBefore - quote.getPrice(), owner.getGold());
        assertEquals(-20, tools(EuropeanNation.DUTCH).getPressure());
        assertEquals(-10, tools(EuropeanNation.FRENCH).getPressure());
        assertEquals(-10, tools(EuropeanNation.ENGLISH).getPressure());
        assertEquals(-10, tools(EuropeanNation.SPANISH).getPressure());
        assertTrue(tools(EuropeanNation.SPANISH).isTradedSinceUpdate());
        assertEquals(1, tools(EuropeanNation.DUTCH).getSellPrice());
        // Hammers are not a market commodity and nothing else moved.
        assertEquals(0, model.getEurope()
                .getGoodsTradeForType(GoodsType.LUMBER, EuropeanNation.DUTCH)
                .getPressure());
    }

    @Test
    void staleAndUnaffordableRushesMoveNoMarket() {
        prepareRushNeedingFiveImportedTools();
        owner.setGold(1000);
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        queue.rush(quote);
        final String completed = snapshot();
        // The same quote cannot be replayed: nothing is charged or pressed.
        assertThrows(IllegalStateException.class, () -> queue.rush(quote));
        assertEquals(completed, snapshot());

        // A rush the player cannot afford is rejected before any mutation.
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 0));
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 0));
        owner.setGold(1);
        final ConstructionRushQuote costly = queue.getRushQuote().orElseThrow();
        final String beforeRejection = snapshot();
        assertThrows(NotEnoughtGoldException.class, () -> queue.rush(costly));
        assertEquals(beforeRejection, snapshot());
    }

    @Test
    void fullStockZeroPricePreservesSurplusAndFinishesOneHead() {
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        queue.addAtEnd(ConstructionType.STABLES);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 160));
        final int gold = owner.getGold();
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        assertEquals(80, quote.getHammersUsed());
        assertEquals(0, quote.getMissingHammers());
        assertEquals(0, quote.getPrice());
        queue.rush(quote);
        assertEquals(gold, owner.getGold());
        assertEquals(80,
                colony.getWarehouse().getGoods(GoodsType.HAMMERS).getAmount());
        assertEquals(1, queue.getBuildingQueue().size());
        assertFalse(
                colony.isContainsConstructionByType(ConstructionType.STABLES));
    }

    @Test
    void unitRushCreatesOneUnitWithoutStoringBoughtTools() {
        queue.addAtEnd(UnitType.WAGON);
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        colony.getWarehouse().setGoods(Goods.of(GoodsType.TOOLS, 10));
        owner.setGold(1000);
        final int units = model.getAllUnits().size();
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        assertEquals(0, quote.getMissingHammers());
        assertEquals(30, quote.getMissingTools());
        queue.rush(quote);
        assertEquals(units + 1, model.getAllUnits().size());
        assertEquals(0,
                colony.getWarehouse().getGoods(GoodsType.TOOLS).getAmount());
        assertEquals(1, queue.getBuildingQueue().size());
    }

    @Test
    void insufficientGoldAndChangedStocksRejectWithoutPartialEffects() {
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        owner.setGold(1);
        final ConstructionRushQuote poor = queue.getRushQuote().orElseThrow();
        assertTrue(poor.getGoldAfter() < 0);
        final String before = snapshot();
        assertThrows(NotEnoughtGoldException.class, () -> queue.rush(poor));
        assertEquals(before, snapshot());
        owner.setGold(1000);
        final ConstructionRushQuote stale = queue.getRushQuote().orElseThrow();
        colony.getWarehouse().setGoods(Goods.of(GoodsType.HAMMERS, 1));
        final String changed = snapshot();
        assertThrows(IllegalStateException.class, () -> queue.rush(stale));
        assertEquals(changed, snapshot());
    }

    @Test
    void orderRoundTripAndForeignQuoteAreRejected() {
        final ColonyBuildingItemProgressConstruction first = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        final ColonyBuildingItemProgressConstruction second = queue
                .addAtEnd(ConstructionType.STABLES);
        owner.setGold(1000);
        final ConstructionRushQuote stale = queue.getRushQuote().orElseThrow();
        queue.moveItemBefore(second, first);
        queue.moveItemBefore(first, second);
        final String before = snapshot();
        assertThrows(IllegalStateException.class, () -> queue.rush(stale));
        assertEquals(before, snapshot());
        final Colony other = model.getColonies().stream()
                .filter(candidate -> candidate != colony).findFirst()
                .orElseThrow();
        assertThrows(IllegalStateException.class,
                () -> other.getColonyBuildingQueue().rush(stale));
        assertEquals(before, snapshot());
    }

    @Test
    void marketTaxAndPredecessorChangesInvalidateQuote() {
        growToFourResidents();
        queue.addAtEnd(ConstructionType.BLACKSMITHS_SHOP);
        owner.setGold(1000);
        final ConstructionRushQuote priced = queue.getRushQuote().orElseThrow();
        model.getPlayerStore().getKingForPlayer(owner).orElseThrow()
                .setKingsTaxPercentage(20);
        final String changedTax = snapshot();
        assertThrows(IllegalStateException.class, () -> queue.rush(priced));
        assertEquals(changedTax, snapshot());
        final ConstructionRushQuote blocked = queue.getRushQuote()
                .orElseThrow();
        colony.getConstructions().remove(colony
                .getConstructionByType(ConstructionType.BLACKSMITHS_HOUSE));
        final String changedGate = snapshot();
        assertTrue(queue.getRushQuote().isEmpty());
        assertThrows(IllegalStateException.class, () -> queue.rush(blocked));
        assertEquals(changedGate, snapshot());
    }

    @Test
    void reusedQueueIdDoesNotReviveAnOldQuote() {
        final ColonyBuildingItemProgressConstruction old = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        final ConstructionRushQuote quote = queue.getRushQuote().orElseThrow();
        queue.removeItem(old);
        final ColonyBuildingItemProgressConstruction replacement = queue
                .addAtEnd(ConstructionType.WAREHOUSE);
        assertEquals(old.getId(), replacement.getId());
        final String before = snapshot();
        assertThrows(IllegalStateException.class, () -> queue.rush(quote));
        assertEquals(before, snapshot());
    }

    @Test
    void treasuryAndMarketPriceChangesRequireFreshQuotes() {
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        final ConstructionRushQuote treasury = queue.getRushQuote()
                .orElseThrow();
        owner.setGold(owner.getGold() + 1);
        final String changedGold = snapshot();
        assertThrows(IllegalStateException.class, () -> queue.rush(treasury));
        assertEquals(changedGold, snapshot());

        queue.removeItem(queue.getBuildingQueue().get(0));
        queue.addAtEnd(UnitType.WAGON);
        final ConstructionRushQuote market = queue.getRushQuote().orElseThrow();
        model.getEurope()
                .getGoodsTradeForType(GoodsType.TOOLS, EuropeanNation.DUTCH)
                .setBuyPrice(7);
        final String changedMarket = snapshot();
        assertThrows(IllegalStateException.class, () -> queue.rush(market));
        assertEquals(changedMarket, snapshot());
    }

    @Test
    void unavailableEuropeAndOverflowNeverOfferAQuote() {
        queue.addAtEnd(UnitType.WAGON);
        model.getEurope()
                .getGoodsTradeForType(GoodsType.TOOLS, EuropeanNation.DUTCH)
                .setBuyPrice(Integer.MAX_VALUE);
        final String overflow = snapshot();
        assertTrue(queue.getRushQuote().isEmpty());
        assertEquals(overflow, snapshot());

        final GameModelPo independentData = new GameModelPoDao()
                .loadFromClassPath(
                        "/maps/default-campaign-mission-2-thrive.microcol");
        independentData.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().setDeclaredIndependence(true);
        final Model independent = Model.make(independentData.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final ColonyBuildingQueue blocked = independent.getColonyByName("Delft")
                .orElseThrow().getColonyBuildingQueue();
        blocked.addAtEnd(ConstructionType.WAREHOUSE);
        final String before = new Gson().toJson(independent.save());
        assertTrue(blocked.getRushQuote().isEmpty());
        assertEquals(before, new Gson().toJson(independent.save()));
    }

    @Test
    void turnPreparationHidesOfferAndRejectsConfirmation() {
        queue.addAtEnd(ConstructionType.WAREHOUSE);
        final ConstructionRushQuote offer = queue.getRushQuote().orElseThrow();
        final String before = snapshot();
        final boolean[] checked = { false };
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onRoundStarted(final RoundStartedEvent event) {
                assertFalse(model.isGameRunning());
                assertTrue(queue.getRushQuote().isEmpty());
                assertThrows(TurnPreparationException.class,
                        () -> queue.rush(offer));
                checked[0] = true;
            }
        });
        model.startGame();
        assertTrue(checked[0]);
        assertEquals(before, snapshot());
    }
}
