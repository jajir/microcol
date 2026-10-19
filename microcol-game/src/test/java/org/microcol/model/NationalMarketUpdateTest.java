package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Real-model checks of the national market at the owner-update boundary and
 * across Save/Continue.
 */
class NationalMarketUpdateTest {

    @TempDir
    Path directory;

    private final GameModelPoDao dao = new GameModelPoDao();

    private GameModelPo fixture() {
        return dao.loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
    }

    private Model make(final GameModelPo data) {
        return Model.make(data.getModel(),
                List.of(GameOverEvaluator.GAMEOVER_CONDITION_CALENDAR));
    }

    /** The Dutch update, preceded by the Crown's turn. */
    private void nextOwnerTurn(final Model model) {
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
    }

    private GoodsTrade corn(final Model model, final EuropeanNation nation) {
        return model.getEurope().getGoodsTradeForType(GoodsType.CORN, nation);
    }

    @Test
    void quietRecoveryRunsOncePerOwnerUpdate() {
        final Model model = make(fixture());
        final Player dutch = model.getPlayerByName("Dutch");
        // 200 Dutch units press 800 eighths: one step from bid 2 to 1.
        dutch.sellGoods(Goods.of(GoodsType.CORN, 200));
        assertEquals(1, corn(model, EuropeanNation.DUTCH).getSellPrice());
        assertTrue(corn(model, EuropeanNation.DUTCH).isTradedSinceUpdate());

        // The first update only consumes the trade flag.
        nextOwnerTurn(model);
        assertEquals(1, corn(model, EuropeanNation.DUTCH).getSellPrice());
        assertFalse(corn(model, EuropeanNation.DUTCH).isTradedSinceUpdate());

        // A quiet update moves one step back; the next one stays at the start.
        nextOwnerTurn(model);
        assertEquals(2, corn(model, EuropeanNation.DUTCH).getSellPrice());
        assertEquals(4, corn(model, EuropeanNation.DUTCH).getBuyPrice());
        nextOwnerTurn(model);
        assertEquals(2, corn(model, EuropeanNation.DUTCH).getSellPrice());
    }

    @Test
    void anotherNationsTradeCountsAsAffectingTheOwnersUpdate() {
        final Model model = make(fixture());
        model.getPlayerByName("Dutch").sellGoods(Goods.of(GoodsType.CORN, 200));
        nextOwnerTurn(model);
        assertEquals(1, corn(model, EuropeanNation.DUTCH).getSellPrice());

        // A single French purchase is an eighth of a unit to the Dutch market
        // (-1 eighth, half of the ordinary quarter-unit -2), inward at the bid
        // floor so it is kept. It still affects the Dutch market and suppresses
        // this update's recovery.
        model.getEurope().applyTrade(EuropeanNation.FRENCH, GoodsType.CORN, -1);
        assertEquals(-1, corn(model, EuropeanNation.DUTCH).getPressure());
        assertTrue(corn(model, EuropeanNation.DUTCH).isTradedSinceUpdate());
        nextOwnerTurn(model);
        assertEquals(1, corn(model, EuropeanNation.DUTCH).getSellPrice());
        assertEquals(-1, corn(model, EuropeanNation.DUTCH).getPressure());

        // With no further trade the following update recovers one step.
        nextOwnerTurn(model);
        assertEquals(2, corn(model, EuropeanNation.DUTCH).getSellPrice());
    }

    @Test
    void openingAndSavingNeverAdvanceRecovery() {
        final Model model = make(fixture());
        model.getPlayerByName("Dutch").sellGoods(Goods.of(GoodsType.CORN, 200));
        nextOwnerTurn(model);
        assertEquals(1, corn(model, EuropeanNation.DUTCH).getSellPrice());
        // Reading prices, quoting and saving are not updates.
        model.getPlayerByName("Dutch")
                .quoteEuropeTrade(Goods.of(GoodsType.CORN, 5), true);
        model.save();
        model.save();
        assertEquals(1, corn(model, EuropeanNation.DUTCH).getSellPrice());
        nextOwnerTurn(model);
        assertEquals(2, corn(model, EuropeanNation.DUTCH).getSellPrice());
    }

    @Test
    void savedModelKeepsPricesPressureAndFlagWithoutReplay() {
        final GameModelPo data = fixture();
        final Model model = make(data);
        // 37 Dutch own units press 37 x 4 = 148 eighths, below one step.
        model.getPlayerByName("Dutch").sellGoods(Goods.of(GoodsType.CORN, 37));
        // Five English sugar units press the English market by 5 x 8 = -40 and
        // the Dutch sugar market by 5 x 1 = -5, a cross-market eighth per unit.
        model.getEurope().applyTrade(EuropeanNation.ENGLISH, GoodsType.SUGAR,
                -5);
        data.setModel(model.save());
        final Path file = directory.resolve("market.microcol");
        dao.saveToFile(file.toString(), data);

        final Model restored = make(dao.loadFromFile(file.toFile()));
        final GoodsTrade savedCorn = corn(restored, EuropeanNation.DUTCH);
        assertEquals(2, savedCorn.getSellPrice());
        assertEquals(148, savedCorn.getPressure());
        assertTrue(savedCorn.isTradedSinceUpdate());
        // Each French unit of the Dutch sale is a quarter unit: 37 x 2 = 74.
        assertEquals(74, corn(restored, EuropeanNation.FRENCH).getPressure());
        assertEquals(-40, restored.getEurope()
                .getGoodsTradeForType(GoodsType.SUGAR, EuropeanNation.ENGLISH)
                .getPressure());
        assertEquals(-5, restored.getEurope()
                .getGoodsTradeForType(GoodsType.SUGAR, EuropeanNation.DUTCH)
                .getPressure());

        // Continue resumes without recovery, export or any other replay.
        final JsonElement saved = new Gson().toJsonTree(restored.save());
        restored.startGame();
        assertEquals(saved, new Gson().toJsonTree(restored.save()));
        assertTrue(corn(restored, EuropeanNation.DUTCH).isTradedSinceUpdate());

        // The restored remainder continues exactly: 148 + 4 x 163 = 800
        // eighths,
        // so 163 more Dutch units finish the step that 37 units started.
        restored.getPlayerByName("Dutch")
                .sellGoods(Goods.of(GoodsType.CORN, 163));
        assertEquals(1, corn(restored, EuropeanNation.DUTCH).getSellPrice());
        assertEquals(0, corn(restored, EuropeanNation.DUTCH).getPressure());
    }

    @Test
    void malformedSavedMarketRejectsTheWholeModel() {
        final GameModelPo missing = fixture();
        missing.getModel().setEurope(new org.microcol.model.store.EuropePo());
        assertThrows(IllegalArgumentException.class, () -> make(missing));

        final GameModelPo overflow = fixture();
        overflow.getModel().getEurope().getMarkets().get("DUTCH").put("CORN",
                new int[] { 2, Integer.MIN_VALUE, 0 });
        assertThrows(IllegalArgumentException.class, () -> make(overflow));

        final GameModelPo outward = fixture();
        outward.getModel().getEurope().getMarkets().get("FRENCH").put("ORE",
                new int[] { 1, 5, 0 });
        assertThrows(IllegalArgumentException.class, () -> make(outward));

        // A rejected candidate leaves the shared fixture usable.
        assertEquals(2,
                corn(make(fixture()), EuropeanNation.DUTCH).getSellPrice());
    }

}
