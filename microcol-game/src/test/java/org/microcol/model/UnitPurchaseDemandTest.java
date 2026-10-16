package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.ReferenceExpedition;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceEuropePortPo;
import org.microcol.model.store.UnitActionNoActionPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

class UnitPurchaseDemandTest {

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    private GameModelPo scenario() {
        return dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model load(final GameModelPo data) {
        return Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void eachShipTypeEscalatesIndependentlyAndSpecialistPriceStaysFixed() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(20_000);
        final String beforeQuote = snapshot(model);
        assertEquals(1_000, buyer.getUnitPurchasePrice(UnitType.CARAVEL));
        assertTrue(buyer.canBuyUnit(UnitType.CARAVEL));
        assertEquals(1_000, buyer.getUnitPurchasePrice(UnitType.CARAVEL));
        assertEquals(beforeQuote, snapshot(model));

        final int firstId = model.getAllUnits().stream().mapToInt(Unit::getId)
                .max().orElseThrow() + 1;
        buyer.buy(UnitType.CARAVEL, 1_000);
        assertSame(UnitType.CARAVEL, model.getUnitById(firstId).getType());
        assertEquals(1_100, buyer.getUnitPurchasePrice(UnitType.CARAVEL));
        assertEquals(2_000, buyer.getUnitPurchasePrice(UnitType.MERCHANTMAN));
        buyer.buy(UnitType.CARAVEL, 1_100);
        assertEquals(1_200, buyer.getUnitPurchasePrice(UnitType.CARAVEL));
        buyer.buy(UnitType.MERCHANTMAN, 2_000);
        assertEquals(2_200, buyer.getUnitPurchasePrice(UnitType.MERCHANTMAN));
        assertEquals(1_200, buyer.getUnitPurchasePrice(UnitType.CARAVEL));
        assertEquals(2_200,
                buyer.getUnitPurchasePrice(UnitType.MASTER_BLACKSMITH));
        buyer.buy(UnitType.MASTER_BLACKSMITH, 2_200);
        buyer.buy(UnitType.MASTER_BLACKSMITH, 2_200);
        assertEquals(2_200,
                buyer.getUnitPurchasePrice(UnitType.MASTER_BLACKSMITH));
        assertEquals(Map.of("CARAVEL", 2, "MERCHANTMAN", 1),
                buyer.save().getUnitPurchaseCounts());
    }

    @Test
    void staleQuoteAndOneGoldShortRejectWithoutAnyChange() {
        final Model model = load(scenario());
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(5_000);
        final int oldQuote = buyer.getUnitPurchasePrice(UnitType.CARAVEL);
        buyer.buy(UnitType.CARAVEL, oldQuote);
        final String afterFirst = snapshot(model);
        assertThrows(IllegalStateException.class,
                () -> buyer.buy(UnitType.CARAVEL, oldQuote));
        assertEquals(afterFirst, snapshot(model));

        final int current = buyer.getUnitPurchasePrice(UnitType.CARAVEL);
        buyer.setGold(current - 1);
        final String oneShort = snapshot(model);
        assertFalse(buyer.canBuyUnit(UnitType.CARAVEL));
        assertThrows(NotEnoughtGoldException.class,
                () -> buyer.buy(UnitType.CARAVEL, current));
        assertEquals(oneShort, snapshot(model));
    }

    @Test
    void diskContinueKeepsPerTypeDemandWithoutReapplyingPurchase() {
        final GameModelPo data = scenario();
        final Model model = load(data);
        final Player buyer = model.getPlayerByName("Dutch");
        buyer.setGold(5_000);
        buyer.buy(UnitType.CARAVEL, 1_000);
        final int nextPrice = buyer.getUnitPurchasePrice(UnitType.CARAVEL);
        final int units = model.getAllUnits().size();
        data.setModel(model.save());
        final Path file = directory.resolve("ship-demand.microcol");
        dao.saveToFile(file.toString(), data);
        final Model restored = load(dao.loadFromFile(file.toFile()));
        final Player restoredBuyer = restored.getPlayerByName("Dutch");
        restored.startGame();
        assertEquals(nextPrice,
                restoredBuyer.getUnitPurchasePrice(UnitType.CARAVEL));
        assertEquals(Map.of("CARAVEL", 1),
                restoredBuyer.save().getUnitPurchaseCounts());
        assertEquals(units, restored.getAllUnits().size());
    }

    @Test
    void anotherNationHasItsOwnSparseCounts() {
        final GameModelPo data = new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
        data.setModel(ReferenceExpedition.create(data.getModel().getMap(),
                EuropeanNation.ENGLISH, Difficulty.CONQUISTADOR));
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "England".equals(player.getName()))
                .findFirst().orElseThrow()
                .setUnitPurchaseCounts(Map.of("CARAVEL", 2));
        final Model model = load(data);
        assertEquals(1_200, model.getPlayerByName("England")
                .getUnitPurchasePrice(UnitType.CARAVEL));
        assertEquals(1_000, model.getPlayerByName("Dutch")
                .getUnitPurchasePrice(UnitType.CARAVEL));
        assertTrue(model.getPlayerByName("Dutch").save().getUnitPurchaseCounts()
                .isEmpty());
    }

    @Test
    void everyPurchasableShipUsesItsOwnExistingBasePrice() {
        for (final UnitType type : new UnitType[] { UnitType.CARAVEL,
                UnitType.MERCHANTMAN, UnitType.GALLEON, UnitType.PRIVATEER,
                UnitType.FRIGATE }) {
            final GameModelPo data = scenario();
            data.getModel().getGameManager().getPlayers().stream()
                    .filter(player -> "Dutch".equals(player.getName()))
                    .findFirst().orElseThrow()
                    .setUnitPurchaseCounts(Map.of(type.name(), 1));
            final Player buyer = load(data).getPlayerByName("Dutch");
            final int base = type.getEuropePrice();
            assertEquals(base + (base + 9) / 10,
                    buyer.getUnitPurchasePrice(type));
        }
    }

    @Test
    void malformedCountsAndCurrentPriceOverflowRejectOnLoad() {
        final GameModelPo missing = scenario();
        missing.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().setUnitPurchaseCounts(null);
        assertThrows(IllegalArgumentException.class, () -> load(missing));
        for (final Map<String, Integer> invalid : List.of(Map.of("UNKNOWN", 1),
                Map.of("COLONIST", 1), Map.of("MAN_OF_WAR", 1),
                Map.of("CARAVEL", -1), Map.of("CARAVEL", 21_474_827))) {
            final GameModelPo data = scenario();
            data.getModel().getGameManager().getPlayers().stream()
                    .filter(player -> "Dutch".equals(player.getName()))
                    .findFirst().orElseThrow().setUnitPurchaseCounts(invalid);
            assertThrows(IllegalArgumentException.class, () -> load(data));
        }
        final Map<String, Integer> nullCount = new HashMap<>();
        nullCount.put("CARAVEL", null);
        final GameModelPo nullData = scenario();
        nullData.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow().setUnitPurchaseCounts(nullCount);
        assertThrows(IllegalArgumentException.class, () -> load(nullData));
        final GameModelPo king = scenario();
        king.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getWhosKingThisPlayerIs() != null)
                .findFirst().orElseThrow()
                .setUnitPurchaseCounts(Map.of("CARAVEL", 1));
        assertThrows(IllegalArgumentException.class, () -> load(king));
    }

    @Test
    void nextQuoteOverflowAndExhaustedUnitIdsRejectBeforeMutation() {
        final GameModelPo nextPriceOverflow = scenario();
        nextPriceOverflow.getModel().getGameManager().getPlayers().stream()
                .filter(player -> "Dutch".equals(player.getName())).findFirst()
                .orElseThrow()
                .setUnitPurchaseCounts(Map.of("CARAVEL", 21_474_826));
        final Model priced = load(nextPriceOverflow);
        final Player pricedBuyer = priced.getPlayerByName("Dutch");
        pricedBuyer.setGold(Integer.MAX_VALUE);
        final int quote = pricedBuyer.getUnitPurchasePrice(UnitType.CARAVEL);
        assertEquals(2_147_483_600, quote);
        final String pricedBefore = snapshot(priced);
        assertFalse(pricedBuyer.canBuyUnit(UnitType.CARAVEL));
        assertThrows(IllegalStateException.class,
                () -> pricedBuyer.buy(UnitType.CARAVEL, quote));
        assertEquals(pricedBefore, snapshot(priced));

        final GameModelPo exhausted = scenario();
        final UnitPo finalId = new UnitPo();
        finalId.setId(Integer.MAX_VALUE - 1);
        finalId.setOwnerId("Dutch");
        finalId.setType(UnitType.COLONIST);
        finalId.setWorkUpdates(0);
        finalId.setAction(new UnitActionNoActionPo());
        finalId.setAvailableMoves(1);
        finalId.setPlaceEuropePort(new PlaceEuropePortPo());
        exhausted.getModel().getUnits().add(finalId);
        final Model noIds = load(exhausted);
        final Player noIdsBuyer = noIds.getPlayerByName("Dutch");
        noIdsBuyer.setGold(1_000);
        final String noIdsBefore = snapshot(noIds);
        assertFalse(noIdsBuyer.canBuyUnit(UnitType.CARAVEL));
        assertThrows(IllegalStateException.class,
                () -> noIdsBuyer.buy(UnitType.CARAVEL, 1_000));
        assertEquals(noIdsBefore, snapshot(noIds));
    }
}
