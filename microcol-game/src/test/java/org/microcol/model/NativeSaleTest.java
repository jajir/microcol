package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeTradePo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.unit.UnitWithCargo;

import com.google.gson.Gson;

/**
 * Native sale and gift of carried goods (R-0160): the exact price rule with its
 * floor and factors, the cargo and treasury caps, the atomic commit with its
 * stale-terms refusal, the once-only haggle drawn from the saved random state,
 * the gift reductions and the arms remainder, and the saved form.
 */
class NativeSaleTest {

    /** Wanted goods of the scene; each is tradable and never offered. */
    private static final List<GoodsType> WANTED = List.of(GoodsType.MUSKET,
            GoodsType.TOOLS, GoodsType.HORSE);

    private static final int WAGON = 200;

    /** Goods slot holding one amount of one goods type. */
    private static Consumer<UnitPo> carrying(final GoodsType goods,
            final int amount) {
        return unit -> {
            final CargoSlotPo slot = new CargoSlotPo();
            slot.setGoodsType(goods);
            slot.setAmount(amount);
            unit.getCargo().getSlots().add(slot);
        };
    }

    /** A contacted tribe, a capital of 2000 gold, and a wagon with 50 corn. */
    private static NativeRelationsScene wagonScene(final double anger) {
        return wagonScene(NativeNation.AZTEC, anger);
    }

    /** Same scene for another tribe, for example a village-class one. */
    private static NativeRelationsScene wagonScene(final NativeNation nation,
            final double anger) {
        return island(nation).contact(EuropeanNation.DUTCH, nation)
                .market(100, List.of(GoodsType.CORN), WANTED)
                .tribal(nation, EuropeanNation.DUTCH, anger).unit(DUTCH,
                        UnitType.WAGON, at(-1, 0),
                        carrying(GoodsType.CORN, 50));
    }

    /** Same tribe with a wagon holding fifty muskets. */
    private static NativeRelationsScene musketScene() {
        return island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .market(100, List.of(GoodsType.CORN), WANTED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, 10)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0),
                        carrying(GoodsType.MUSKET, 50));
    }

    private static Unit wagon(final Model model) {
        return model.getUnitById(WAGON);
    }

    private static NativeSaleQuote quote(final Model model,
            final GoodsType goods, final int quantity, final boolean gift) {
        return model.getNativeSaleQuote(wagon(model), settlement(model), goods,
                quantity, gift).orElseThrow();
    }

    /** Pieces of one goods type that the wagon holds now. */
    private static int held(final Model model, final GoodsType goods) {
        return ((UnitWithCargo) wagon(model)).getCargo().getSlots().stream()
                .flatMap(slot -> slot.getGoods()
                        .filter(carried -> carried.getType().equals(goods))
                        .stream())
                .mapToInt(Goods::getAmount).sum();
    }

    private static int gold(final Model model) {
        return model.getPlayerByName(DUTCH).getGold();
    }

    private static double tribalAnger(final Model model) {
        return model.save().getNativeTribalAnger().stream()
                .filter(entry -> entry.getNation() == NativeNation.AZTEC
                        && entry.getEuropean() == EuropeanNation.DUTCH)
                .findFirst().orElseThrow().getValue();
    }

    private static double localAlarm(final Model model) {
        return model.save().getNativeLocalAlarm().stream()
                .filter(entry -> entry.getSettlementId() == settlement(model)
                        .getId() && entry.getEuropean() == EuropeanNation.DUTCH)
                .findFirst().orElseThrow().getValue();
    }

    @Test
    void priceFloorsOnceAfterTheBaseAndTheFactors() {
        // The base is three quarters of the bid, floored, and never below one.
        assertEquals(1, NativeMarketRules.buyBasePrice(1));
        assertEquals(1, NativeMarketRules.buyBasePrice(2));
        assertEquals(8, NativeMarketRules.buyBasePrice(11));
        assertEquals(3, NativeMarketRules.saleTotal(1, 4, false, false, false));
        assertEquals(0, NativeMarketRules.saleTotal(0, 4, true, true, true));
        // Wanted ×1.5 and ship ×0.8 on ten pieces of base three: 30×1.2 = 36.
        assertEquals(36, NativeMarketRules.saleTotal(10, 4, true, true, false));
        // One piece: 3×1.2 = 3.6, floored once to 3.
        assertEquals(3, NativeMarketRules.saleTotal(1, 4, true, true, false));
        // Haggle +10% on ten pieces: 30×1.1 = 33.
        assertEquals(33,
                NativeMarketRules.saleTotal(10, 4, false, false, true));
        // Wanted and haggled on one piece: 3×1.5×1.1 = 4.95, floored to 4.
        assertEquals(4, NativeMarketRules.saleTotal(1, 4, true, false, true));
    }

    @Test
    void treasuryAndCargoCapTheQuantityWithoutAnyOtherLimit() {
        assertEquals(10, NativeMarketRules.maxSaleQuantity(50, 10, 2, false,
                false, false));
        assertEquals(50, NativeMarketRules.maxSaleQuantity(50, 1000, 2, false,
                false, false));
        assertEquals(0, NativeMarketRules.maxSaleQuantity(50, 0, 2, false,
                false, false));
        // Ten pieces cost 36 and eleven cost 39, so 36 gold buys exactly ten.
        assertEquals(10, NativeMarketRules.maxSaleQuantity(50, 36, 4, true,
                true, false));
        assertEquals(9, NativeMarketRules.maxSaleQuantity(50, 35, 4, true, true,
                false));
    }

    @Test
    void haggleSucceedsAtFiftyPercentWhenContentAndTwentyFivePercentWhenUneasy() {
        assertTrue(NativeMarketRules.haggleSucceeds(NativeHostility.CONTENT,
                0.49));
        assertFalse(
                NativeMarketRules.haggleSucceeds(NativeHostility.CONTENT, 0.5));
        assertTrue(
                NativeMarketRules.haggleSucceeds(NativeHostility.UNEASY, 0.24));
        assertFalse(
                NativeMarketRules.haggleSucceeds(NativeHostility.UNEASY, 0.25));
        assertFalse(
                NativeMarketRules.haggleSucceeds(NativeHostility.ANGRY, 0.0));
        assertFalse(
                NativeMarketRules.haggleSucceeds(NativeHostility.HOSTILE, 0.0));
    }

    @Test
    void quotesExistOnlyForAnOpenExpeditionAndWithinTheLimit() {
        final Model model = wagonScene(10).toModel();
        final NativeSettlement village = settlement(model);
        assertTrue(model.getNativeSaleQuote(wagon(model), village,
                GoodsType.CORN, 50, false).isEmpty());
        model.enterNativeMarket(wagon(model), village);
        final NativeMarketView.Market market = model
                .getNativeMarketView(wagon(model), village).orElseThrow()
                .getMarket().orElseThrow();
        assertEquals(List.of(GoodsType.CORN),
                market.getCarried().stream()
                        .map(NativeMarketView.Carried::getGoods)
                        .collect(Collectors.toList()));
        assertEquals(50, market.getCarried().get(0).getAmount());
        assertTrue(model.getNativeSaleQuote(wagon(model), village,
                GoodsType.CORN, 51, false).isEmpty());
        assertTrue(model.getNativeSaleQuote(wagon(model), village,
                GoodsType.CORN, 0, false).isEmpty());
        assertTrue(model.getNativeSaleQuote(wagon(model), village,
                GoodsType.TOBACCO, 1, false).isEmpty());
    }

    @Test
    void saleMovesGoodsAndGoldExactlyOnceAndRecordsTheVillage() {
        final Model model = wagonScene(10).toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        final NativeSaleQuote sale = quote(model, GoodsType.CORN, 50, false);
        assertEquals(NativeSaleQuote.Haggle.OFFERED, sale.getHaggle());
        assertEquals(NativeMarketRules.saleTotal(50, sale.getEuropeBid(), false,
                false, false), sale.getGold());
        final int gold = gold(model);
        final int treasury = village.getTreasury();

        model.sellToNative(wagon(model), village, sale);

        assertEquals(gold + sale.getGold(), gold(model));
        assertEquals(treasury - sale.getGold(), village.getTreasury());
        assertEquals(0, held(model, GoodsType.CORN));
        assertEquals(50, village.getReceivedStock().get(GoodsType.CORN));
        assertEquals(Optional.of(GoodsType.CORN),
                village.findTrade(EuropeanNation.DUTCH).orElseThrow()
                        .getLastReceived());
        assertEquals(1, model.save().getNativeRelationOutcomeIds().size());
    }

    @Test
    void aReplayedOrStaleConfirmationChangesNothing() {
        final Model model = wagonScene(10).toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        final NativeSaleQuote wholeLot = quote(model, GoodsType.CORN, 50,
                false);
        final NativeSaleQuote part = quote(model, GoodsType.CORN, 20, false);
        model.sellToNative(wagon(model), village, part);
        final int gold = gold(model);
        final int treasury = village.getTreasury();

        assertThrows(NativeTermsChangedException.class,
                () -> model.sellToNative(wagon(model), village, wholeLot));
        assertThrows(NativeTermsChangedException.class,
                () -> model.sellToNative(wagon(model), village, part));

        assertEquals(gold, gold(model));
        assertEquals(treasury, village.getTreasury());
        assertEquals(30, held(model, GoodsType.CORN));
        assertEquals(20, village.getReceivedStock().get(GoodsType.CORN));
        assertEquals(1, model.save().getNativeRelationOutcomeIds().size());
    }

    @Test
    void aGoldOverflowRefusesTheSaleBeforeAnyChange() {
        final Model model = wagonScene(10).gold(DUTCH, Integer.MAX_VALUE)
                .toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        final NativeSaleQuote sale = quote(model, GoodsType.CORN, 50, false);
        final int treasury = village.getTreasury();

        assertThrows(IllegalStateException.class,
                () -> model.sellToNative(wagon(model), village, sale));

        assertEquals(50, held(model, GoodsType.CORN));
        assertEquals(treasury, village.getTreasury());
        assertTrue(village.getReceivedStock().isEmpty());
        assertEquals(Integer.MAX_VALUE, gold(model));
    }

    @Test
    void theNativeTreasuryCapsTheSaleAndNeverGoesNegative() {
        final Model model = wagonScene(10).treasuryOfFirst(10).toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        final int bid = model.getEurope()
                .getGoodsTradeForType(GoodsType.CORN, EuropeanNation.DUTCH)
                .getSellPrice();
        final int most = NativeMarketRules.maxSaleQuantity(50, 10, bid, false,
                false, false);
        assertEquals(most, model.getNativeMaxSaleQuantity(wagon(model), village,
                GoodsType.CORN, false));
        assertTrue(model.getNativeSaleQuote(wagon(model), village,
                GoodsType.CORN, most + 1, false).isEmpty());

        model.sellToNative(wagon(model), village,
                quote(model, GoodsType.CORN, most, false));

        assertEquals(10
                - NativeMarketRules.saleTotal(most, bid, false, false, false),
                village.getTreasury());
        assertTrue(village.getTreasury() >= 0);
    }

    @Test
    void aGiftPaysNoGoldAndLowersTheCapitalsAlarmAndAngerProportionally() {
        final Model model = wagonScene(10).alarm(EuropeanNation.DUTCH, 10)
                .toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        final NativeSaleQuote gift = quote(model, GoodsType.CORN, 50, true);
        assertEquals(0, gift.getGold());
        assertEquals(NativeSaleQuote.Haggle.UNAVAILABLE, gift.getHaggle());
        final int gold = gold(model);
        final int treasury = village.getTreasury();

        assertThrows(IllegalArgumentException.class,
                () -> model.sellToNative(wagon(model), village, gift));
        model.giftToNative(wagon(model), village, gift);

        assertEquals(gold, gold(model));
        assertEquals(treasury, village.getTreasury());
        assertEquals(0, held(model, GoodsType.CORN));
        assertEquals(50, village.getReceivedStock().get(GoodsType.CORN));
        // Fifty pieces reduce by 5×50/100 = 2.5, doubled at a capital.
        assertEquals(5.0, localAlarm(model));
        assertEquals(5.0, tribalAnger(model));
    }

    @Test
    void aPartialGiftKeepsItsFractionalReductionExactly() {
        final Model model = wagonScene(10).alarm(EuropeanNation.DUTCH, 10)
                .toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        // Thirty-three pieces lower by 5×33/100 = 1.65, doubled at the capital.
        model.giftToNative(wagon(model), village,
                quote(model, GoodsType.CORN, 33, true));
        assertEquals(17, held(model, GoodsType.CORN));
        assertEquals(10 - 3.3, localAlarm(model), 1e-9);
        assertEquals(10 - 3.3, tribalAnger(model), 1e-9);
    }

    /** Saved arms remainder of the Dutch toward the Aztecs; zero if none. */
    private static int armsRemainder(final Model model) {
        return model.save().getNativeArmsSales().stream()
                .filter(entry -> entry.getNation() == NativeNation.AZTEC
                        && entry.getEuropean() == EuropeanNation.DUTCH)
                .mapToInt(entry -> entry.getQuantity()).sum();
    }

    @Test
    void soldMusketsCountTowardFiftyWhileGiftedMusketsAddNoArmsAnger() {
        final Model model = musketScene().toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);

        // The Aztec settlement is a capital: every completed trade lowers both
        // the local alarm and the tribal anger by 2 doubled = 4. The arms rise
        // of +5 per complete 50 pieces comes on top, after that trade.
        model.sellToNative(wagon(model), village,
                quote(model, GoodsType.MUSKET, 49, false));
        assertEquals(6.0, tribalAnger(model),
                "10 - 4 for the trade; 49 pieces are no complete step yet");
        assertEquals(49, armsRemainder(model));
        model.sellToNative(wagon(model), village,
                quote(model, GoodsType.MUSKET, 1, false));
        assertEquals(7.0, tribalAnger(model),
                "6 - 4 for the trade + 5 for the fiftieth piece");
        assertEquals(0, armsRemainder(model));
        assertEquals(0, held(model, GoodsType.MUSKET));

        final Model given = musketScene().toModel();
        final NativeSettlement giftVillage = settlement(given);
        given.enterNativeMarket(wagon(given), giftVillage);
        given.giftToNative(wagon(given), giftVillage,
                quote(given, GoodsType.MUSKET, 50, true));
        // Only the capital's gift reduction of 5 per 50 pieces doubled
        // applies, never the arms rise or a remainder.
        assertEquals(5.0, tribalAnger(given));
        assertEquals(0, armsRemainder(given));
    }

    @Test
    void haggleIsDrawnOnceFromTheSavedRandomStateAndKeepsItsResult() {
        int accepted = 0;
        for (long seed = 0; seed < 120; seed++) {
            final NativeRelationsScene scene = wagonScene(10);
            scene.toPo().setCombatRandomState(seed);
            final Model model = scene.toModel();
            final NativeSettlement village = settlement(model);
            model.enterNativeMarket(wagon(model), village);
            final NativeSaleQuote offered = quote(model, GoodsType.CORN, 50,
                    false);
            final boolean won = model.haggleNativeSale(wagon(model), village,
                    offered);
            if (won) {
                accepted++;
            }
            final NativeSaleQuote sale = quote(model, GoodsType.CORN, 50,
                    false);
            assertEquals(won ? NativeSaleQuote.Haggle.ACCEPTED
                    : NativeSaleQuote.Haggle.REFUSED, sale.getHaggle());
            assertEquals(NativeMarketRules.saleTotal(50, sale.getEuropeBid(),
                    false, false, won), sale.getGold());
            assertThrows(NativeTermsChangedException.class, () -> model
                    .haggleNativeSale(wagon(model), village, offered));
        }
        // Fair draws: 60 of 120 expected; the bounds are about four sigmas
        // wide.
        assertTrue(accepted >= 35 && accepted <= 85,
                "content haggles accepted " + accepted + " of 120");
    }

    @Test
    void theHaggleResultSurvivesSaveAndCannotBeRerolled() {
        final NativeRelationsScene scene = wagonScene(10);
        scene.toPo().setCombatRandomState(3L);
        final Model model = scene.toModel();
        model.enterNativeMarket(wagon(model), settlement(model));
        final NativeSaleQuote offered = quote(model, GoodsType.CORN, 50, false);
        final boolean won = model.haggleNativeSale(wagon(model),
                settlement(model), offered);

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.enterNativeMarket(wagon(restored), settlement(restored));
        assertEquals(
                won ? NativeSaleQuote.Haggle.ACCEPTED
                        : NativeSaleQuote.Haggle.REFUSED,
                quote(restored, GoodsType.CORN, 50, false).getHaggle());
        assertThrows(NativeTermsChangedException.class,
                () -> restored.haggleNativeSale(wagon(restored),
                        settlement(restored), offered));
    }

    @Test
    void theHaggledOfferKeepsItsGoodsAndPiecesThroughSaveAndReopening() {
        final NativeRelationsScene scene = wagonScene(10);
        scene.toPo().setCombatRandomState(3L);
        final Model model = scene.toModel();
        model.enterNativeMarket(wagon(model), settlement(model));
        NativeMarketView.Market before = model
                .getNativeMarketView(wagon(model), settlement(model))
                .orElseThrow().getMarket().orElseThrow();
        assertTrue(before.getHaggledGoods().isEmpty());
        assertEquals(0, before.getHaggledQuantity());

        model.haggleNativeSale(wagon(model), settlement(model),
                quote(model, GoodsType.CORN, 30, false));

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        for (final Model shown : List.of(model, restored)) {
            final NativeMarketView.Market market = shown
                    .getNativeMarketView(wagon(shown), settlement(shown))
                    .orElseThrow().getMarket().orElseThrow();
            assertEquals(GoodsType.CORN,
                    market.getHaggledGoods().orElseThrow());
            assertEquals(30, market.getHaggledQuantity());
        }
        assertEquals(30, tradePo(model.save()).getExpeditions().get(0)
                .getHaggledQuantity());
    }

    @Test
    void aSavedHaggledPiecesCountNeedsItsGoodsAndMustBePositive() {
        final NativeRelationsScene scene = wagonScene(10);
        scene.toPo().setCombatRandomState(3L);
        final Model model = scene.toModel();
        model.enterNativeMarket(wagon(model), settlement(model));
        model.haggleNativeSale(wagon(model), settlement(model),
                quote(model, GoodsType.CORN, 30, false));

        final ModelPo zero = model.save();
        tradePo(zero).getExpeditions().get(0).setHaggledQuantity(0);
        assertThrows(IllegalArgumentException.class, () -> Model.make(zero,
                MissionImpl.getPredefinedGameOverEvaluators()));

        final ModelPo orphan = enteredWagon(wagonScene(10).toModel()).save();
        tradePo(orphan).getExpeditions().get(0).setHaggledQuantity(30);
        assertThrows(IllegalArgumentException.class, () -> Model.make(orphan,
                MissionImpl.getPredefinedGameOverEvaluators()));
    }

    @Test
    void aSaleOnHaggledTermsPaysTheRaisedReceiptsOnce() {
        final NativeRelationsScene scene = wagonScene(10);
        scene.toPo().setCombatRandomState(0L);
        final Model model = scene.toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        final boolean won = model.haggleNativeSale(wagon(model), village,
                quote(model, GoodsType.CORN, 50, false));
        final NativeSaleQuote sale = quote(model, GoodsType.CORN, 50, false);
        final int gold = gold(model);

        model.sellToNative(wagon(model), village, sale);

        assertEquals(gold + NativeMarketRules.saleTotal(50, sale.getEuropeBid(),
                false, false, won), gold(model));
        assertEquals(0, held(model, GoodsType.CORN));
    }

    /** Whole saved state as JSON, so any changed field shows up. */
    private static String snapshot(final Model model) {
        return new Gson().toJson(model.save());
    }

    @Test
    void theVillageRoomLimitsSaleAndGiftAndAnOverflowChangesNothing() {
        final NativeSaleQuote sale = quote(
                enteredWagon(wagonScene(10).toModel()), GoodsType.CORN, 10,
                false);
        final NativeSaleQuote gift = quote(
                enteredWagon(wagonScene(10).toModel()), GoodsType.CORN, 10,
                true);

        // Room of exactly ten pieces: ten is legal, eleven is not, and the
        // settlement's stock stops at the largest storable amount.
        final Model tight = enteredWagon(wagonScene(10)
                .receivedOfFirst(GoodsType.CORN, Integer.MAX_VALUE - 10)
                .toModel());
        assertEquals(10, tight.getNativeMaxSaleQuantity(wagon(tight),
                settlement(tight), GoodsType.CORN, false));
        assertEquals(10, tight.getNativeMaxSaleQuantity(wagon(tight),
                settlement(tight), GoodsType.CORN, true));
        assertTrue(tight.getNativeSaleQuote(wagon(tight), settlement(tight),
                GoodsType.CORN, 11, false).isEmpty());
        tight.sellToNative(wagon(tight), settlement(tight),
                quote(tight, GoodsType.CORN, 10, false));
        assertEquals(Integer.MAX_VALUE,
                settlement(tight).getReceivedStock().get(GoodsType.CORN));

        // A full village refuses both at once, and a stale offer changes
        // nothing.
        final Model full = enteredWagon(wagonScene(10)
                .receivedOfFirst(GoodsType.CORN, Integer.MAX_VALUE).toModel());
        assertEquals(0, full.getNativeMaxSaleQuantity(wagon(full),
                settlement(full), GoodsType.CORN, false));
        assertEquals(0, full.getNativeMaxSaleQuantity(wagon(full),
                settlement(full), GoodsType.CORN, true));
        final String before = snapshot(full);
        assertThrows(NativeTermsChangedException.class,
                () -> full.sellToNative(wagon(full), settlement(full), sale));
        assertThrows(NativeTermsChangedException.class,
                () -> full.giftToNative(wagon(full), settlement(full), gift));
        assertEquals(before, snapshot(full));
        // Even the direct bookkeeping refuses to pass the largest amount.
        assertThrows(ArithmeticException.class,
                () -> settlement(full).addReceived(GoodsType.CORN, 1));
        assertEquals(before, snapshot(full));
    }

    @Test
    void aRefusedHaggleDrawsNothingAndChangesNothing() {
        // The mood turns uneasy after the offer was shown: the offer is stale.
        final Model mood = enteredWagon(wagonScene(10).toModel());
        final NativeSaleQuote shown = quote(mood, GoodsType.CORN, 50, false);
        mood.addNativeTribalAnger(NativeNation.AZTEC,
                mood.getPlayerByName(DUTCH), 20);
        final String moodBefore = snapshot(mood);
        assertThrows(NativeTermsChangedException.class, () -> mood
                .haggleNativeSale(wagon(mood), settlement(mood), shown));
        assertEquals(moodBefore, snapshot(mood));
        // The fresh uneasy offer is still open, and its haggle is available.
        final NativeSaleQuote uneasy = quote(mood, GoodsType.CORN, 50, false);
        assertEquals(NativeHostility.UNEASY, uneasy.getHostility());
        mood.haggleNativeSale(wagon(mood), settlement(mood), uneasy);

        // A sale changes the cargo: the earlier offer for fifty pieces is
        // stale.
        final Model cargo = enteredWagon(wagonScene(10).toModel());
        final NativeSaleQuote whole = quote(cargo, GoodsType.CORN, 50, false);
        cargo.sellToNative(wagon(cargo), settlement(cargo),
                quote(cargo, GoodsType.CORN, 20, false));
        final String cargoBefore = snapshot(cargo);
        assertThrows(NativeTermsChangedException.class, () -> cargo
                .haggleNativeSale(wagon(cargo), settlement(cargo), whole));
        assertEquals(cargoBefore, snapshot(cargo));

        // The treasury changes before the haggle: the offer is stale.
        final Model treasury = enteredWagon(wagonScene(10).toModel());
        final NativeSaleQuote paid = quote(treasury, GoodsType.CORN, 50, false);
        settlement(treasury).setTreasury(5);
        final String treasuryBefore = snapshot(treasury);
        assertThrows(NativeTermsChangedException.class, () -> treasury
                .haggleNativeSale(wagon(treasury), settlement(treasury), paid));
        assertEquals(treasuryBefore, snapshot(treasury));

        // The natives can pay the plain price but not the raised one: the
        // haggle is refused before any draw.
        final int bid = enteredWagon(wagonScene(10).toModel()).getEurope()
                .getGoodsTradeForType(GoodsType.CORN, EuropeanNation.DUTCH)
                .getSellPrice();
        final int plain = NativeMarketRules.saleTotal(50, bid, false, false,
                false);
        final Model poor = enteredWagon(
                wagonScene(10).treasuryOfFirst(plain).toModel());
        final NativeSaleQuote affordable = quote(poor, GoodsType.CORN, 50,
                false);
        final String poorBefore = snapshot(poor);
        assertThrows(IllegalStateException.class, () -> poor
                .haggleNativeSale(wagon(poor), settlement(poor), affordable));
        assertEquals(poorBefore, snapshot(poor));
    }

    @Test
    void aHaggleIsOnceOnlyAndAGiftCannotBeHaggled() {
        final Model model = enteredWagon(wagonScene(10).toModel());
        final NativeSaleQuote offered = quote(model, GoodsType.CORN, 50, false);
        model.haggleNativeSale(wagon(model), settlement(model), offered);
        final NativeSaleQuote settled = quote(model, GoodsType.CORN, 50, false);
        assertNotEquals(NativeSaleQuote.Haggle.OFFERED, settled.getHaggle());
        final String before = snapshot(model);
        assertThrows(IllegalStateException.class, () -> model
                .haggleNativeSale(wagon(model), settlement(model), settled));
        assertThrows(NativeTermsChangedException.class, () -> model
                .haggleNativeSale(wagon(model), settlement(model), offered));
        assertThrows(IllegalArgumentException.class,
                () -> model.haggleNativeSale(wagon(model), settlement(model),
                        quote(model, GoodsType.CORN, 50, true)));
        assertEquals(before, snapshot(model));
    }

    @Test
    void aRepeatedGoodsRefusalMakesEveryOfferUnavailable() {
        // Cherokee settlements are villages, the only class that refuses the
        // same goods on consecutive expeditions.
        final Model model = wagonScene(NativeNation.CHEROKEE, 10).unit(DUTCH,
                UnitType.WAGON, at(1, 0), carrying(GoodsType.CORN, 50))
                .toModel();
        assertEquals(NativeSettlementClass.VILLAGE,
                settlement(model).getSettlementClass());
        final NativeSettlement village = settlement(model);
        final Unit first = model.getUnitById(WAGON);
        final Unit second = model.getUnitById(WAGON + 1);
        model.enterNativeMarket(first, village);
        final NativeSaleQuote earlierSale = model
                .getNativeSaleQuote(first, village, GoodsType.CORN, 50, false)
                .orElseThrow();
        final NativeSaleQuote earlierGift = model
                .getNativeSaleQuote(first, village, GoodsType.CORN, 50, true)
                .orElseThrow();
        model.sellToNative(first, village, earlierSale);
        // The second expedition starts after corn was received: corn is
        // refused, for sale and gift alike, and no offer exists.
        model.enterNativeMarket(second, village);
        assertEquals(0, model.getNativeMaxSaleQuantity(second, village,
                GoodsType.CORN, false));
        assertEquals(0, model.getNativeMaxSaleQuantity(second, village,
                GoodsType.CORN, true));
        assertTrue(model
                .getNativeSaleQuote(second, village, GoodsType.CORN, 1, false)
                .isEmpty());
        assertTrue(model
                .getNativeSaleQuote(second, village, GoodsType.CORN, 1, true)
                .isEmpty());
        final String before = snapshot(model);
        assertThrows(NativeTermsChangedException.class,
                () -> model.haggleNativeSale(second, village, earlierSale));
        assertThrows(NativeTermsChangedException.class,
                () -> model.sellToNative(second, village, earlierSale));
        assertThrows(NativeTermsChangedException.class,
                () -> model.giftToNative(second, village, earlierGift));
        assertEquals(before, snapshot(model));

        // A city does not refuse: the same two-wagon sequence leaves the
        // second wagon's corn sellable.
        final Model city = wagonScene(10).unit(DUTCH, UnitType.WAGON, at(1, 0),
                carrying(GoodsType.CORN, 50)).toModel();
        city.enterNativeMarket(city.getUnitById(WAGON), settlement(city));
        city.sellToNative(city.getUnitById(WAGON), settlement(city),
                city.getNativeSaleQuote(city.getUnitById(WAGON),
                        settlement(city), GoodsType.CORN, 50, false)
                        .orElseThrow());
        city.enterNativeMarket(city.getUnitById(WAGON + 1), settlement(city));
        assertEquals(50,
                city.getNativeMaxSaleQuantity(city.getUnitById(WAGON + 1),
                        settlement(city), GoodsType.CORN, true));
    }

    /** Saved trade of the only settlement toward the Dutch. */
    private static NativeTradePo tradePo(final ModelPo po) {
        return po.getNativeSettlements().get(0).getTrade().get(0);
    }

    private static UnitPo unitPo(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    @Test
    void anOldOfferStaysDeadWhenEveryValueIsRestoredButTheRevisionAdvanced() {
        final Model model = enteredWagon(wagonScene(10).toModel());
        final NativeSaleQuote old = quote(model, GoodsType.CORN, 20, false);
        final ModelPo original = model.save();
        model.sellToNative(wagon(model), settlement(model), old);

        // Rebuild from the saved game after the sale, then put back every
        // value the offer names: cargo, treasury, received goods, history and
        // mood. Only the transaction revision stays advanced.
        final ModelPo po = model.save();
        final int revision = tradePo(po).getRevision();
        assertTrue(revision > tradePo(original).getRevision());
        unitPo(po, WAGON).getCargo().getSlots().get(0).setAmount(50);
        po.getNativeSettlements().get(0).setTreasury(
                original.getNativeSettlements().get(0).getTreasury());
        po.getNativeSettlements().get(0).setReceivedGoods(new ArrayList<>());
        tradePo(po).setLastReceivedGoods(null);
        po.getNativeTribalAnger().forEach(entry -> {
            if (entry.getNation() == NativeNation.AZTEC
                    && entry.getEuropean() == EuropeanNation.DUTCH) {
                entry.setValue(10.0);
            }
        });
        final Model replayed = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        final NativeSaleQuote fresh = quote(replayed, GoodsType.CORN, 20,
                false);
        // Every term but the revision reads as in the old offer.
        assertEquals(old.getGold(), fresh.getGold());
        assertEquals(old.getHeld(), fresh.getHeld());
        assertEquals(old.getTreasury(), fresh.getTreasury());
        assertEquals(old.getRoom(), fresh.getRoom());
        assertEquals(old.getHostility(), fresh.getHostility());
        assertEquals(old.getHaggle(), fresh.getHaggle());
        assertEquals(revision, fresh.getRevision());
        assertNotEquals(old, fresh);

        final String before = snapshot(replayed);
        assertThrows(NativeTermsChangedException.class, () -> replayed
                .sellToNative(wagon(replayed), settlement(replayed), old));
        assertThrows(NativeTermsChangedException.class, () -> replayed
                .haggleNativeSale(wagon(replayed), settlement(replayed), old));
        assertEquals(before, snapshot(replayed));
        // The fresh offer of the same values is still usable once.
        replayed.sellToNative(wagon(replayed), settlement(replayed), fresh);
        assertEquals(30, held(replayed, GoodsType.CORN));
    }

    @Test
    void aNewExpeditionWithTheSameValuesCannotReviveAnOldOffer() {
        final Model model = enteredWagon(wagonScene(10).toModel());
        final NativeSaleQuote old = quote(model, GoodsType.CORN, 50, false);

        // The carrier leaves and returns: the saved expedition is gone and the
        // market is entered again, with cargo, treasury and mood unchanged.
        final ModelPo po = model.save();
        tradePo(po).setExpeditions(new ArrayList<>());
        final Model returned = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        returned.enterNativeMarket(wagon(returned), settlement(returned));
        final NativeSaleQuote fresh = quote(returned, GoodsType.CORN, 50,
                false);
        assertEquals(old.getGold(), fresh.getGold());
        assertEquals(old.getHeld(), fresh.getHeld());
        assertEquals(old.getTreasury(), fresh.getTreasury());
        assertTrue(fresh.getRevision() > old.getRevision());
        assertNotEquals(old, fresh);

        final String before = snapshot(returned);
        assertThrows(NativeTermsChangedException.class, () -> returned
                .sellToNative(wagon(returned), settlement(returned), old));
        assertThrows(NativeTermsChangedException.class, () -> returned
                .haggleNativeSale(wagon(returned), settlement(returned), old));
        assertEquals(before, snapshot(returned));
    }

    @Test
    void anExhaustedRevisionRefusesEveryStepBeforeAnyChangeOrDraw() {
        final Model model = enteredWagon(wagonScene(10).toModel());
        final ModelPo po = model.save();
        tradePo(po).setRevision(Integer.MAX_VALUE);
        final Model maxed = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        final NativeSaleQuote shown = quote(model, GoodsType.CORN, 50, false);

        // The open expedition offers nothing, and nothing can be confirmed.
        assertEquals(0, maxed.getNativeMaxSaleQuantity(wagon(maxed),
                settlement(maxed), GoodsType.CORN, false));
        assertEquals(0, maxed.getNativeMaxSaleQuantity(wagon(maxed),
                settlement(maxed), GoodsType.CORN, true));
        assertTrue(maxed.getNativeSaleQuote(wagon(maxed), settlement(maxed),
                GoodsType.CORN, 50, false).isEmpty());
        final String before = snapshot(maxed);
        assertThrows(NativeTermsChangedException.class, () -> maxed
                .sellToNative(wagon(maxed), settlement(maxed), shown));
        assertThrows(NativeTermsChangedException.class, () -> maxed
                .haggleNativeSale(wagon(maxed), settlement(maxed), shown));
        assertEquals(before, snapshot(maxed));

        // A new expedition cannot start either, and the refusal leaves no
        // expedition behind.
        final ModelPo departed = maxed.save();
        tradePo(departed).setExpeditions(new ArrayList<>());
        final Model away = Model.make(departed,
                MissionImpl.getPredefinedGameOverEvaluators());
        final String awayBefore = snapshot(away);
        assertThrows(IllegalStateException.class,
                () -> away.enterNativeMarket(wagon(away), settlement(away)));
        assertEquals(awayBefore, snapshot(away));
    }

    @Test
    void everyCompletedTransactionRaisesTheRevisionSoOldOffersStayDead() {
        final Model model = enteredWagon(wagonScene(10).toModel());
        final NativeSaleQuote before = quote(model, GoodsType.CORN, 20, false);
        model.sellToNative(wagon(model), settlement(model), before);
        final NativeSaleQuote after = quote(model, GoodsType.CORN, 30, false);
        assertTrue(after.getRevision() > before.getRevision());
        // Saved and continued, the revision and the open offers come back.
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(after, quote(restored, GoodsType.CORN, 30, false));
        assertNotEquals(before, quote(restored, GoodsType.CORN, 20, false));
    }

    /**
     * Open the market of the wagon beside the settlement; returns the model.
     */
    private static Model enteredWagon(final Model model) {
        model.enterNativeMarket(wagon(model), settlement(model));
        return model;
    }

    @Test
    void theSavedFormKeepsTheReceivedGoodsAndTheHaggleTogether() {
        final NativeRelationsScene scene = wagonScene(10);
        scene.toPo().setCombatRandomState(0L);
        final Model model = scene.toModel();
        final NativeSettlement village = settlement(model);
        model.enterNativeMarket(wagon(model), village);
        model.haggleNativeSale(wagon(model), village,
                quote(model, GoodsType.CORN, 50, false));
        model.sellToNative(wagon(model), village,
                quote(model, GoodsType.CORN, 20, false));

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final NativeSettlement restoredVillage = settlement(restored);
        assertEquals(20,
                restoredVillage.getReceivedStock().get(GoodsType.CORN));
        assertEquals(30, held(restored, GoodsType.CORN));
        assertEquals(restoredVillage.getTreasury(), village.getTreasury());
    }

}
