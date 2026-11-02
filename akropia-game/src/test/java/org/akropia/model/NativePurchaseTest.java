package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.akropia.model.NativeRelationsScene.DUTCH;
import static org.akropia.model.NativeRelationsScene.at;
import static org.akropia.model.NativeRelationsScene.island;
import static org.akropia.model.NativeRelationsScene.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.store.CargoSlotPo;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.NativeExpeditionPo;
import org.akropia.model.store.NativeTradePo;
import org.akropia.model.store.UnitPo;
import org.akropia.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * Second-stage native purchase (R-0161): the pending stage that a confirmed
 * sale or gift opens in the same expedition, the Europe ask without tax, the
 * one request kept with the stage, the stock, carrier and gold bounds, the
 * atomic purchase with its stale refusal, the decline, and the saved form.
 */
class NativePurchaseTest {

    /**
     * Wanted goods of the scene; none is offered, so none competes with grain.
     */
    private static final List<GoodsType> WANTED = List.of(GoodsType.WEAPONS,
            GoodsType.TOOLS, GoodsType.HORSE);

    private static final int WAGON = 200;

    /** Goods slot holding one amount of grain. */
    private static Consumer<UnitPo> carryingGrain(final int amount) {
        return unit -> {
            final CargoSlotPo slot = new CargoSlotPo();
            slot.setGoodsType(GoodsType.GRAIN);
            slot.setAmount(amount);
            unit.getCargo().getSlots().add(slot);
        };
    }

    /**
     * A contacted capital that offers grain at stock 100, and a wagon with
     * fifty grain beside it.
     */
    private static NativeRelationsScene wagonScene() {
        return island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .market(100, List.of(GoodsType.GRAIN), WANTED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), carryingGrain(50));
    }

    private static Unit wagon(final Model model) {
        return model.getUnitById(WAGON);
    }

    private static int gold(final Model model) {
        return model.getPlayerByName(DUTCH).getGold();
    }

    /** Europe ask of one piece of grain: the price the natives charge. */
    private static int ask(final Model model) {
        return model.getEurope()
                .getGoodsTradeForType(GoodsType.GRAIN, EuropeanNation.DUTCH)
                .getBuyPrice();
    }

    /** Pieces of grain that the wagon holds now. */
    private static int held(final Model model) {
        return ((UnitWithCargo) wagon(model)).getCargo().getSlots().stream()
                .flatMap(slot -> slot.getGoods()
                        .filter(carried -> carried.getType()
                                .equals(GoodsType.GRAIN))
                        .stream())
                .mapToInt(Goods::getAmount).sum();
    }

    private static NativeSaleQuote sale(final Model model, final int quantity) {
        return model.getNativeSaleQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, quantity, false).orElseThrow();
    }

    private static NativePurchaseQuote purchase(final Model model,
            final int quantity) {
        return model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, quantity).orElseThrow();
    }

    private static NativeMarketView.Purchase stage(final Model model) {
        return model.getNativeMarketView(wagon(model), settlement(model))
                .orElseThrow().getPurchase().orElseThrow();
    }

    private static boolean noStage(final Model model) {
        return model.getNativeMarketView(wagon(model), settlement(model))
                .orElseThrow().getPurchase().isEmpty();
    }

    /** Whole saved state as JSON, so any changed field shows up. */
    private static String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    /** Saved trade of the only settlement toward the Dutch. */
    private static NativeTradePo tradePo(final ModelPo po) {
        return po.getNativeSettlements().get(0).getTrade().get(0);
    }

    /**
     * Open the market of the wagon beside the settlement; returns the model.
     */
    private static Model enteredWagon(final Model model) {
        model.enterNativeMarket(wagon(model), settlement(model));
        return model;
    }

    /**
     * Open the market, then sell the given pieces of grain: the stage opens.
     */
    private static Model soldGrain(final int quantity) {
        final Model model = enteredWagon(wagonScene().toModel());
        model.sellToNative(wagon(model), settlement(model),
                sale(model, quantity));
        return model;
    }

    @Test
    void purchaseTotalRoundsAPositiveDiscountedTotalUpOnce() {
        assertEquals(5, NativeMarketRules.purchaseTotal(1, 5, false));
        // Ninety per cent of five is 4.5, rounded up to five.
        assertEquals(5, NativeMarketRules.purchaseTotal(1, 5, true));
        // Three at seven is 21; ninety per cent is 18.9, rounded up to 19.
        assertEquals(19, NativeMarketRules.purchaseTotal(3, 7, true));
        // Ten at four is 40; ninety per cent is exactly 36.
        assertEquals(36, NativeMarketRules.purchaseTotal(10, 4, true));
        assertEquals(1, NativeMarketRules.purchaseTotal(1, 1, true));
    }

    @Test
    void goldAndTheDiscountBoundTheQuantityByBisection() {
        assertEquals(9,
                NativeMarketRules.maxPurchaseQuantity(50, 36, 4, false));
        // Ten pieces cost 36 after the discount, eleven cost 40.
        assertEquals(10,
                NativeMarketRules.maxPurchaseQuantity(50, 36, 4, true));
        assertEquals(0, NativeMarketRules.maxPurchaseQuantity(50, 0, 4, true));
        assertEquals(3,
                NativeMarketRules.maxPurchaseQuantity(3, 1000, 4, false));
    }

    @Test
    void aConfirmedSaleOrGiftOpensTheSecondStageAndArrivalDoesNot() {
        final Model model = enteredWagon(wagonScene().toModel());
        assertTrue(noStage(model));
        assertTrue(model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, 5).isEmpty());
        assertEquals(0, model.getNativeMaxPurchaseQuantity(wagon(model),
                settlement(model), GoodsType.GRAIN));

        model.sellToNative(wagon(model), settlement(model), sale(model, 20));
        assertTrue(stage(model).isOpen());
        assertEquals(NativeSaleQuote.Haggle.OFFERED, stage(model).getHaggle());

        final Model given = enteredWagon(wagonScene().toModel());
        given.giftToNative(wagon(given), settlement(given),
                given.getNativeSaleQuote(wagon(given), settlement(given),
                        GoodsType.GRAIN, 20, true).orElseThrow());
        assertTrue(stage(given).isOpen());
    }

    @Test
    void anEmptiedCarrierBuysWithItsStageButAnotherEmptyCarrierNever() {
        final Model model = enteredWagon(
                wagonScene().unit(DUTCH, UnitType.WAGON, at(1, 0), unit -> {
                }).toModel());
        final Unit second = model.getUnitById(WAGON + 1);
        assertTrue(model.getNativeMarketView(second, settlement(model))
                .orElseThrow().getPurchase().isEmpty());

        model.sellToNative(wagon(model), settlement(model), sale(model, 50));
        assertEquals(0, held(model));
        assertEquals(NativeMarketView.Admission.NO_GOODS,
                model.getNativeMarketView(wagon(model), settlement(model))
                        .orElseThrow().getAdmission());
        // The emptied wagon still buys, and the trade offers it as available.
        assertTrue(stage(model).isOpen());
        assertEquals(NativeVisitChoice.Limit.AVAILABLE, model
                .getNativeVisitChoices(wagon(model), settlement(model)).stream()
                .filter(choice -> choice.getAction() == NativeVisitAction.TRADE)
                .findFirst().orElseThrow().getLimit());
        assertEquals(5, purchase(model, 5).getQuantity());

        // The second empty wagon has no expedition and no stage: refused.
        assertTrue(model.getNativePurchaseQuote(second, settlement(model),
                GoodsType.GRAIN, 1).isEmpty());
        assertEquals(0, model.getNativeMaxPurchaseQuantity(second,
                settlement(model), GoodsType.GRAIN));
    }

    @Test
    void buyingMovesTheStockGoldAndCargoExactlyOnceAndClosesTheStage() {
        final Model model = soldGrain(20);
        final int gold = gold(model);
        final int treasury = settlement(model).getTreasury();
        final NativePurchaseQuote shown = purchase(model, 5);
        assertEquals(NativeMarketRules.purchaseTotal(5, ask(model), false),
                shown.getGold());

        model.buyFromNative(wagon(model), settlement(model), shown);

        assertEquals(gold - shown.getGold(), gold(model));
        assertEquals(treasury + shown.getGold(),
                settlement(model).getTreasury());
        assertEquals(95, settlement(model).getStock(GoodsType.GRAIN));
        // Fifty pieces, twenty sold, five bought.
        assertEquals(35, held(model));
        // Bought stock never enters what the village received from the player.
        assertEquals(20,
                settlement(model).getReceivedStock().get(GoodsType.GRAIN));
        assertTrue(noStage(model));
        assertTrue(model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, 5).isEmpty());
        assertThrows(NativeTermsChangedException.class, () -> model
                .buyFromNative(wagon(model), settlement(model), shown));
    }

    @Test
    void aStaleOrReplayedPurchaseChangesNothing() {
        final Model model = soldGrain(20);
        final NativePurchaseQuote whole = purchase(model, 5);
        final NativePurchaseQuote part = purchase(model, 2);
        model.buyFromNative(wagon(model), settlement(model), part);
        final String before = snapshot(model);

        assertThrows(NativeTermsChangedException.class, () -> model
                .buyFromNative(wagon(model), settlement(model), whole));
        assertThrows(NativeTermsChangedException.class, () -> model
                .buyFromNative(wagon(model), settlement(model), part));
        assertEquals(before, snapshot(model));

        // A treasury that moved since the quote makes it stale too.
        final Model moved = soldGrain(20);
        final NativePurchaseQuote shown = purchase(moved, 5);
        settlement(moved).setTreasury(settlement(moved).getTreasury() + 1);
        final String movedBefore = snapshot(moved);
        assertThrows(NativeTermsChangedException.class, () -> moved
                .buyFromNative(wagon(moved), settlement(moved), shown));
        assertEquals(movedBefore, snapshot(moved));
    }

    @Test
    void stockGoldAndNativeTreasuryRoomBoundThePurchase() {
        final Model model = soldGrain(20);
        final int ask = ask(model);
        model.getPlayerByName(DUTCH).setGold(3 * ask);
        assertEquals(3, model.getNativeMaxPurchaseQuantity(wagon(model),
                settlement(model), GoodsType.GRAIN));
        assertTrue(model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, 4).isEmpty());

        // The village can take only one more piece's gold before its integer
        // treasury overflows.
        settlement(model).setTreasury(Integer.MAX_VALUE - ask);
        model.getPlayerByName(DUTCH).setGold(100 * ask);
        assertEquals(1, model.getNativeMaxPurchaseQuantity(wagon(model),
                settlement(model), GoodsType.GRAIN));

        // Stock limits the purchase once the treasury has room again.
        settlement(model).setTreasury(0);
        settlement(model).setStock(GoodsType.GRAIN, 2);
        assertEquals(2, model.getNativeMaxPurchaseQuantity(wagon(model),
                settlement(model), GoodsType.GRAIN));
        assertTrue(model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, 3).isEmpty());
    }

    @Test
    void theDiscountIsDrawnOnceAndKeepsItsResultThroughReopeningAndAnotherQuantity() {
        int accepted = 0;
        for (long seed = 0; seed < 60; seed++) {
            final NativeRelationsScene scene = wagonScene();
            scene.toPo().setCombatRandomState(seed);
            final Model model = enteredWagon(scene.toModel());
            model.sellToNative(wagon(model), settlement(model),
                    sale(model, 20));
            final NativePurchaseQuote shown = purchase(model, 5);
            final boolean won = model.haggleNativePurchase(wagon(model),
                    settlement(model), shown);
            if (won) {
                accepted++;
            }
            final NativePurchaseQuote after = purchase(model, 5);
            assertEquals(
                    won ? NativeSaleQuote.Haggle.ACCEPTED
                            : NativeSaleQuote.Haggle.REFUSED,
                    after.getHaggle());
            assertEquals(NativeMarketRules.purchaseTotal(5, ask(model), won),
                    after.getGold());
            // Changing the quantity draws nothing: the request result stays.
            assertEquals(after.getHaggle(), purchase(model, 4).getHaggle());
            assertThrows(NativeTermsChangedException.class,
                    () -> model.haggleNativePurchase(wagon(model),
                            settlement(model), shown));
            assertThrows(IllegalStateException.class,
                    () -> model.haggleNativePurchase(wagon(model),
                            settlement(model), after));
        }
        // Fair draws: 30 of 60 expected; the bounds are wide enough for chance.
        assertTrue(accepted >= 10 && accepted <= 50,
                "content requests accepted " + accepted + " of 60");
    }

    @Test
    void theSavedStageKeepsItsIdentityAndItsRequestAcrossSaveAndReopening() {
        final NativeRelationsScene scene = wagonScene();
        scene.toPo().setCombatRandomState(3L);
        final Model model = enteredWagon(scene.toModel());
        model.sellToNative(wagon(model), settlement(model), sale(model, 20));
        model.haggleNativePurchase(wagon(model), settlement(model),
                purchase(model, 5));
        final NativePurchaseQuote before = purchase(model, 5);

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(before, purchase(restored, 5));
        // Reopening the market draws nothing and keeps the same expedition.
        restored.enterNativeMarket(wagon(restored), settlement(restored));
        assertEquals(before, purchase(restored, 5));
        assertTrue(stage(restored).isOpen());
    }

    @Test
    void declineEndsTheStageAndOnlyANewRealSaleOpensAnother() {
        final Model model = soldGrain(20);
        model.rejectNativePurchase(wagon(model), settlement(model));
        assertTrue(noStage(model));
        assertTrue(model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, 5).isEmpty());
        assertThrows(IllegalStateException.class, () -> model
                .rejectNativePurchase(wagon(model), settlement(model)));

        // Reopening the same expedition does not reopen the declined stage.
        enteredWagon(model);
        assertTrue(noStage(model));

        // A new confirmed sale opens a fresh stage with a fresh request.
        model.sellToNative(wagon(model), settlement(model), sale(model, 10));
        assertEquals(NativeSaleQuote.Haggle.OFFERED, stage(model).getHaggle());
    }

    @Test
    void aTenseMarketKeepsThePendingStageDeclinableButNotBuyable() {
        final Model model = soldGrain(20);
        model.addNativeTribalAnger(NativeNation.AZTEC,
                model.getPlayerByName(DUTCH), 80);
        assertFalse(stage(model).isOpen());
        assertTrue(model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, 5).isEmpty());
        assertEquals(0, model.getNativeMaxPurchaseQuantity(wagon(model),
                settlement(model), GoodsType.GRAIN));

        model.rejectNativePurchase(wagon(model), settlement(model));
        assertTrue(noStage(model));
    }

    @Test
    void theStageEndsWhenTheCarrierLeavesItsExpedition() {
        final Model model = soldGrain(20);
        final ModelPo po = model.save();
        tradePo(po).setExpeditions(new ArrayList<>());
        final Model departed = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(noStage(departed));
    }

    /** Saved expedition of the only settlement toward the Dutch. */
    private static NativeExpeditionPo expeditionPo(final ModelPo po) {
        return tradePo(po).getExpeditions().get(0);
    }

    @Test
    void theStageShowsUpToThreeOfferedGoodsWithStockAndAskAndCountsTheRestUnavailable() {
        // One offered good leaves two slots unavailable, and its ask is the
        // Europe price with no tax.
        final Model one = soldGrain(20);
        final NativeMarketView.Purchase single = stage(one);
        assertEquals(1, single.getOffers().size());
        assertEquals(2, single.getUnavailableSlots());
        assertEquals(GoodsType.GRAIN, single.getOffers().get(0).getGoods());
        assertEquals(100, single.getOffers().get(0).getStock());
        assertEquals(ask(one), single.getOffers().get(0).getAskPrice());
        // Goods the settlement does not offer have no stock to buy.
        assertTrue(one.getNativePurchaseQuote(wagon(one), settlement(one),
                GoodsType.GRAPES, 1).isEmpty());
        assertEquals(0, one.getNativeMaxPurchaseQuantity(wagon(one),
                settlement(one), GoodsType.GRAPES));

        // Three offered goods fill every slot.
        final Model three = enteredWagon(island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .market(100,
                        List.of(GoodsType.GRAIN, GoodsType.GRAPES,
                                GoodsType.WEAPONS),
                        WANTED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), carryingGrain(50))
                .toModel());
        three.sellToNative(wagon(three), settlement(three), sale(three, 20));
        final NativeMarketView.Purchase full = stage(three);
        assertEquals(3, full.getOffers().size());
        assertEquals(0, full.getUnavailableSlots());
        assertEquals(
                List.of(GoodsType.GRAIN, GoodsType.GRAPES, GoodsType.WEAPONS),
                full.getOffers().stream()
                        .map(NativeMarketView.PurchaseOffer::getGoods)
                        .collect(Collectors.toList()));
    }

    /**
     * Same tribe with a wagon whose two holds are fifty grain and a full hold
     * of weapons; the wagon holds two slots in total.
     */
    private static NativeRelationsScene fullWagonScene() {
        return island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .market(100, List.of(GoodsType.GRAIN), WANTED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), unit -> {
                    carryingGrain(50).accept(unit);
                    final CargoSlotPo weapons = new CargoSlotPo();
                    weapons.setGoodsType(GoodsType.WEAPONS);
                    weapons.setAmount(CargoSlot.MAX_CARGO_SLOT_CAPACITY);
                    unit.getCargo().getSlots().add(weapons);
                });
    }

    @Test
    void aFullCarrierWithNoMergeRoomOffersNothingAndAStaleBuyChangesNothing() {
        final Model model = enteredWagon(fullWagonScene().toModel());
        model.sellToNative(wagon(model), settlement(model), sale(model, 20));
        final NativePurchaseQuote shown = purchase(model, 5);
        // The grain hold fills to its limit and the weapons hold stays full, so
        // no hold can take another piece of grain.
        ((UnitWithCargo) wagon(model)).getCargo().getSlots().get(0)
                .addGoods(Goods.of(GoodsType.GRAIN, 70));

        assertEquals(0, model.getNativeMaxPurchaseQuantity(wagon(model),
                settlement(model), GoodsType.GRAIN));
        assertTrue(model.getNativePurchaseQuote(wagon(model), settlement(model),
                GoodsType.GRAIN, 1).isEmpty());
        // The stage stays pending: only its room is gone.
        assertTrue(stage(model).isOpen());

        final String before = snapshot(model);
        final int gold = gold(model);
        assertThrows(NativeTermsChangedException.class, () -> model
                .buyFromNative(wagon(model), settlement(model), shown));
        assertEquals(before, snapshot(model));
        assertEquals(gold, gold(model));
        assertEquals(100, settlement(model).getStock(GoodsType.GRAIN));
        assertEquals(100, held(model));
    }

    @Test
    void aPurchasePartlyFillsTheCarrierSlotOfThatGoodsBeforeAnEmptyOne() {
        final Model model = soldGrain(20);
        model.buyFromNative(wagon(model), settlement(model),
                purchase(model, 5));
        // The grain slot holds the thirty kept pieces and the five bought: one
        // merged slot, not a second one.
        final CargoSlot first = ((UnitWithCargo) wagon(model)).getCargo()
                .getSlots().get(0);
        assertEquals(35, first.getGoods().orElseThrow().getAmount());
    }

    @Test
    void anExhaustedRevisionOffersNoPurchaseAndChangesNothing() {
        final Model model = soldGrain(20);
        final NativePurchaseQuote shown = purchase(model, 5);
        final ModelPo po = model.save();
        tradePo(po).setRevision(Integer.MAX_VALUE);
        final Model maxed = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());

        assertEquals(0, maxed.getNativeMaxPurchaseQuantity(wagon(maxed),
                settlement(maxed), GoodsType.GRAIN));
        assertTrue(maxed.getNativePurchaseQuote(wagon(maxed), settlement(maxed),
                GoodsType.GRAIN, 5).isEmpty());
        final String before = snapshot(maxed);
        assertThrows(NativeTermsChangedException.class, () -> maxed
                .buyFromNative(wagon(maxed), settlement(maxed), shown));
        assertThrows(NativeTermsChangedException.class, () -> maxed
                .haggleNativePurchase(wagon(maxed), settlement(maxed), shown));
        assertEquals(before, snapshot(maxed));
    }

    @Test
    void theRequestKeepsTheOfferItWasMadeForThroughSaveAndReopening() {
        final Model model = soldGrain(20);
        // Before the request no offer is kept: the chooser starts afresh.
        assertTrue(stage(model).getSelectedGoods().isEmpty());
        assertEquals(0, stage(model).getSelectedQuantity());

        model.haggleNativePurchase(wagon(model), settlement(model),
                purchase(model, 4));
        assertEquals(GoodsType.GRAIN,
                stage(model).getSelectedGoods().orElseThrow());
        assertEquals(4, stage(model).getSelectedQuantity());
        // Looking at another quantity draws and records nothing.
        purchase(model, 2);
        assertEquals(4, stage(model).getSelectedQuantity());

        final ModelPo saved = model.save();
        assertEquals("GRAIN", expeditionPo(saved).getPurchaseGoods());
        assertEquals(4, expeditionPo(saved).getPurchaseQuantity());
        final Model restored = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(GoodsType.GRAIN,
                stage(restored).getSelectedGoods().orElseThrow());
        assertEquals(4, stage(restored).getSelectedQuantity());
        restored.enterNativeMarket(wagon(restored), settlement(restored));
        assertEquals(4, stage(restored).getSelectedQuantity());
        assertEquals(stage(model).getHaggle(), stage(restored).getHaggle());
    }

    @Test
    void aBoughtOrDeclinedStageForgetsItsOfferAndANewStageStartsAfresh() {
        final Model bought = soldGrain(20);
        bought.haggleNativePurchase(wagon(bought), settlement(bought),
                purchase(bought, 4));
        bought.buyFromNative(wagon(bought), settlement(bought),
                purchase(bought, 4));
        assertTrue(noStage(bought));
        bought.sellToNative(wagon(bought), settlement(bought),
                sale(bought, 10));
        assertTrue(stage(bought).getSelectedGoods().isEmpty());
        assertEquals(0, stage(bought).getSelectedQuantity());
        assertEquals(NativeSaleQuote.Haggle.OFFERED, stage(bought).getHaggle());

        final Model declined = soldGrain(20);
        declined.haggleNativePurchase(wagon(declined), settlement(declined),
                purchase(declined, 4));
        declined.rejectNativePurchase(wagon(declined), settlement(declined));
        declined.sellToNative(wagon(declined), settlement(declined),
                sale(declined, 10));
        assertTrue(stage(declined).getSelectedGoods().isEmpty());
        assertEquals(0, stage(declined).getSelectedQuantity());
    }

    @Test
    void aMalformedSavedOfferIsRefusedNotGuessed() {
        final Model model = soldGrain(20);
        model.haggleNativePurchase(wagon(model), settlement(model),
                purchase(model, 4));

        final ModelPo noPieces = model.save();
        expeditionPo(noPieces).setPurchaseQuantity(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(noPieces,
                MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo noGoods = model.save();
        expeditionPo(noGoods).setPurchaseGoods(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(noGoods,
                MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo zero = model.save();
        expeditionPo(zero).setPurchaseQuantity(0);
        assertThrows(IllegalArgumentException.class, () -> Model.make(zero,
                MissionImpl.getPredefinedGameOverEvaluators()));

        // The settlement does not sell grapes in this scene.
        final ModelPo notSold = model.save();
        expeditionPo(notSold).setPurchaseGoods("GRAPES");
        assertThrows(IllegalArgumentException.class, () -> Model.make(notSold,
                MissionImpl.getPredefinedGameOverEvaluators()));

        // An offer before any request would be a phantom request.
        final ModelPo phantom = soldGrain(20).save();
        expeditionPo(phantom).setPurchaseGoods("GRAIN");
        expeditionPo(phantom).setPurchaseQuantity(4);
        assertThrows(IllegalArgumentException.class, () -> Model.make(phantom,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void aMalformedSavedStageIsRefusedNotGuessed() {
        final Model model = soldGrain(20);
        final int revision = tradePo(model.save()).getRevision();

        final ModelPo unavailable = model.save();
        expeditionPo(unavailable).setPurchaseHaggle("UNAVAILABLE");
        assertThrows(IllegalArgumentException.class,
                () -> Model.make(unavailable,
                        MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo noResult = model.save();
        expeditionPo(noResult).setPurchaseHaggle(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(noResult,
                MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo future = model.save();
        expeditionPo(future).setPurchaseRevision(revision + 5);
        assertThrows(IllegalArgumentException.class, () -> Model.make(future,
                MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo orphan = model.save();
        expeditionPo(orphan).setPurchaseRevision(null);
        assertThrows(IllegalArgumentException.class, () -> Model.make(orphan,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

}
