package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.TaxPo;
import org.microcol.model.event.GoldWasChangedEvent;

import com.google.gson.Gson;

/**
 * Paying back taxes settles one frozen commodity debt exactly once, after the
 * live turn, identity, European access, debt and gold are all confirmed.
 */
class TaxArrearsPaymentTest {

    /** Demand generation the saved arrears were created in. */
    private static final int GENERATION = 2;

    private static PlayerPo player(final ModelPo model, final String name) {
        return model.getGameManager().getPlayers().stream()
                .filter(candidate -> name.equals(candidate.getName()))
                .findFirst().orElseThrow();
    }

    /**
     * The thrive scenario with Dutch holding the given gold and arrears; the
     * demand falls due after {@code updatesToDemand} more Dutch updates.
     */
    private GameModelPo data(final int gold, final Map<GoodsType, Integer> owed,
            final int updatesToDemand) {
        final GameModelPo data = new GameModelPoDao().loadFromClassPath(
                "/maps/default-campaign-mission-2-thrive.microcol");
        data.getModel().setDifficulty(Difficulty.CONQUISTADOR);
        final PlayerPo dutch = player(data.getModel(), "Dutch");
        dutch.setGold(gold);
        final TaxPo tax = TaxState.initial();
        tax.setActivated(true);
        tax.setUpdates(
                TaxState.interval(Difficulty.CONQUISTADOR) - updatesToDemand);
        tax.setIssued(GENERATION);
        owed.forEach(
                (type, amount) -> tax.getArrears().put(type.name(), amount));
        dutch.setTax(tax);
        return data;
    }

    private Model load(final ModelPo saved) {
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** The loaded game with the Dutch turn running and no demand waiting. */
    private Model runningDutchTurn(final GameModelPo data) {
        final Model model = load(data.getModel());
        model.endTurn();
        model.endTurn();
        assertEquals("Dutch", model.getCurrentPlayer().getName());
        assertTrue(model.isGameRunning());
        return model;
    }

    /** The loaded game paused at the Dutch demand. */
    private Model pausedDutchDemand(final GameModelPo data) {
        final Model model = load(data.getModel());
        model.endTurn();
        model.endTurn();
        assertTrue(model.isAwaitingTaxDecision());
        return model;
    }

    private static String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void quoteIsReadOnlyAndNamesOnlyTheBoycottedCommodity() {
        final Model model = runningDutchTurn(data(200,
                Map.of(GoodsType.RUM, 200, GoodsType.TOBACCO, 100), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        final String before = snapshot(model);

        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        assertSame(dutch, rum.getPlayer());
        assertEquals(GoodsType.RUM, rum.getGoods());
        assertEquals(200, rum.getAmount());
        assertTrue(dutch.canPayTaxArrears(rum));
        assertTrue(dutch.quoteTaxArrears(GoodsType.SILK).isEmpty());
        assertEquals(before, snapshot(model));
        assertEquals(200, dutch.getTaxArrears().get(GoodsType.RUM));
        assertEquals(200, dutch.getGold());
    }

    @Test
    void exactFundsPayOnceAndLiftOnlyTheSelectedBoycott() {
        final Model model = runningDutchTurn(data(200,
                Map.of(GoodsType.RUM, 200, GoodsType.TOBACCO, 100), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();

        dutch.payTaxArrears(rum);

        assertEquals(0, dutch.getGold());
        assertFalse(dutch.isBoycotted(GoodsType.RUM));
        assertTrue(dutch.isBoycotted(GoodsType.TOBACCO));
        assertEquals(Map.of(GoodsType.TOBACCO, 100), dutch.getTaxArrears());
        assertEquals(0, dutch.getTaxRate());
        assertFalse(dutch.canPayTaxArrears(rum));
    }

    @Test
    void oneGoldShortChangesNothing() {
        final Model model = runningDutchTurn(data(199,
                Map.of(GoodsType.RUM, 200, GoodsType.TOBACCO, 100), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        final String before = snapshot(model);

        assertFalse(dutch.canPayTaxArrears(rum));
        assertThrows(IllegalStateException.class,
                () -> dutch.payTaxArrears(rum));
        assertEquals(before, snapshot(model));
        assertEquals(199, dutch.getGold());
        assertTrue(dutch.isBoycotted(GoodsType.RUM));
    }

    @Test
    void repeatedConfirmationAfterPaymentDoesNotChargeAgain() {
        final Model model = runningDutchTurn(
                data(500, Map.of(GoodsType.RUM, 200), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        dutch.payTaxArrears(rum);
        final String after = snapshot(model);

        assertThrows(IllegalStateException.class,
                () -> dutch.payTaxArrears(rum));
        assertEquals(300, dutch.getGold());
        assertEquals(after, snapshot(model));
        assertTrue(dutch.quoteTaxArrears(GoodsType.RUM).isEmpty());
    }

    @Test
    void quoteFromAnEarlierDemandGenerationIsStaleForTheSameDebt() {
        final Model model = runningDutchTurn(
                data(500, Map.of(GoodsType.RUM, 200), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        // Identical debt and commodity, but created by an earlier demand.
        final TaxArrearsQuote earlier = new TaxArrearsQuote(dutch,
                GoodsType.RUM, 200, GENERATION - 1);
        final String before = snapshot(model);

        assertFalse(dutch.canPayTaxArrears(earlier));
        assertThrows(IllegalStateException.class,
                () -> dutch.payTaxArrears(earlier));
        assertEquals(before, snapshot(model));
        assertEquals(500, dutch.getGold());
        assertTrue(dutch.isBoycotted(GoodsType.RUM));
    }

    @Test
    void wrongNationChangesNothing() {
        final Model model = runningDutchTurn(
                data(500, Map.of(GoodsType.RUM, 200), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        final Player other = model.getPlayers().stream()
                .filter(candidate -> candidate != dutch).findFirst()
                .orElseThrow();
        final String before = snapshot(model);

        assertFalse(other.canPayTaxArrears(rum));
        assertThrows(IllegalStateException.class,
                () -> other.payTaxArrears(rum));
        assertEquals(before, snapshot(model));
        assertEquals(500, dutch.getGold());
    }

    @Test
    void pausedDemandRejectsPaymentEvenWithExactFunds() {
        final Model model = pausedDutchDemand(
                data(200, Map.of(GoodsType.RUM, 200), 1));
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        final String before = snapshot(model);

        assertFalse(dutch.canPayTaxArrears(rum));
        assertThrows(IllegalStateException.class,
                () -> dutch.payTaxArrears(rum));
        assertEquals(before, snapshot(model));
        assertEquals(200, dutch.getGold());
    }

    @Test
    void independentNationCannotPayBackTaxes() {
        final GameModelPo data = data(200, Map.of(GoodsType.RUM, 200), 5);
        player(data.getModel(), "Dutch").setDeclaredIndependence(true);
        final Model model = runningDutchTurn(data);
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        final String before = snapshot(model);

        assertFalse(dutch.canPayTaxArrears(rum));
        assertThrows(IllegalStateException.class,
                () -> dutch.payTaxArrears(rum));
        assertEquals(before, snapshot(model));
        assertEquals(200, dutch.getGold());
    }

    @Test
    void goldEventSeesCompletedPaymentAndReentrantConfirmationCannotChargeAgain() {
        final Model model = runningDutchTurn(
                data(500, Map.of(GoodsType.RUM, 200), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        final int[] events = { 0 };
        model.addListener(new ModelListenerAdapter() {
            @Override
            public void onGoldWasChanged(final GoldWasChangedEvent event) {
                if (event.getPlayer() != dutch || event.getNewValue() != 300) {
                    return;
                }
                events[0]++;
                // Both the gold and the lifted boycott are already complete.
                assertEquals(300, dutch.getGold());
                assertFalse(dutch.isBoycotted(GoodsType.RUM));
                assertFalse(dutch.canPayTaxArrears(rum));
                assertThrows(IllegalStateException.class,
                        () -> dutch.payTaxArrears(rum));
            }
        });

        dutch.payTaxArrears(rum);

        assertEquals(1, events[0]);
        assertEquals(300, dutch.getGold());
        assertFalse(dutch.isBoycotted(GoodsType.RUM));
    }

    @Test
    void frozenAmountIgnoresChangedMarketPriceAndKeepsOtherDebtsAndCongress() {
        final Model model = runningDutchTurn(
                data(1000, Map.of(GoodsType.RUM, 200, GoodsType.SILK, 100), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        final TaxArrearsQuote rum = dutch.quoteTaxArrears(GoodsType.RUM)
                .orElseThrow();
        final int bells = dutch.getCongressBells();
        model.getEurope()
                .getGoodsTradeForType(GoodsType.RUM, EuropeanNation.DUTCH)
                .setSellPrice(3);

        assertEquals(200, rum.getAmount());
        assertTrue(dutch.canPayTaxArrears(rum));
        dutch.payTaxArrears(rum);

        assertEquals(800, dutch.getGold());
        assertEquals(Map.of(GoodsType.SILK, 100), dutch.getTaxArrears());
        assertEquals(bells, dutch.getCongressBells());
        assertEquals(0, dutch.getTaxRate());
    }

    @Test
    void paymentRestoresBuyingOfThatCommodityOnlyAndSurvivesSaveAndLoad() {
        final Model model = runningDutchTurn(data(1_000_000,
                Map.of(GoodsType.RUM, 200, GoodsType.SILK, 100), 5));
        final Player dutch = model.getPlayerByName("Dutch");
        dutch.payTaxArrears(dutch.quoteTaxArrears(GoodsType.RUM).orElseThrow());

        assertFalse(dutch.isBoycotted(GoodsType.RUM));
        assertTrue(dutch.isBoycotted(GoodsType.SILK));
        assertFalse(dutch.getMaxBuyableGoods(GoodsType.RUM).isZero());
        assertTrue(dutch.getMaxBuyableGoods(GoodsType.SILK).isZero());
        assertDoesNotThrow(() -> dutch.buyGoods(Goods.of(GoodsType.RUM, 1)));
        assertThrows(IllegalStateException.class,
                () -> dutch.buyGoods(Goods.of(GoodsType.SILK, 1)));
        final int goldBeforeSale = dutch.getGold();
        final int saleTotal = dutch
                .quoteEuropeTrade(Goods.of(GoodsType.RUM, 1), false).getTotal();
        dutch.sellGoods(Goods.of(GoodsType.RUM, 1));
        assertEquals(goldBeforeSale + saleTotal, dutch.getGold());
        assertThrows(IllegalStateException.class,
                () -> dutch.sellGoods(Goods.of(GoodsType.SILK, 1)));

        final Model reloaded = load(model.save());
        reloaded.startGame();
        final Player restored = reloaded.getPlayerByName("Dutch");
        assertFalse(restored.isBoycotted(GoodsType.RUM));
        assertEquals(Map.of(GoodsType.SILK, 100), restored.getTaxArrears());
        assertTrue(restored.quoteTaxArrears(GoodsType.RUM).isEmpty());
        assertEquals(100, restored.quoteTaxArrears(GoodsType.SILK).orElseThrow()
                .getAmount());
    }
}
