package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.SEA_ONE;
import static org.microcol.model.NativeRelationsScene.UNARMED;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.NativeMarketView.Admission;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeExpeditionPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.UnitPo;

import com.google.gson.Gson;

/**
 * Native market of a settlement (R-0159): the offer and demand rules, the
 * treasury and restock bounds of owner updates, the admission gates with their
 * reasons, the land introduction, the carrier's expedition from its square to
 * its departure, the village exception for repeated goods, and the saved form.
 */
class NativeMarketTest {

    /** Wanted goods of the scenes; each is tradable and never offered. */
    private static final List<GoodsType> WANTED = List.of(GoodsType.MUSKET,
            GoodsType.TOOLS, GoodsType.HORSE);

    /** Goods slots holding the given goods, one slot each. */
    private static Consumer<UnitPo> carrying(final GoodsType... goods) {
        return unit -> {
            for (final GoodsType held : goods) {
                final CargoSlotPo slot = new CargoSlotPo();
                slot.setGoodsType(held);
                slot.setAmount(50);
                unit.getCargo().getSlots().add(slot);
            }
        };
    }

    private static final Consumer<UnitPo> LOADED = carrying(GoodsType.CORN);

    private static final int WAGON = 200;

    private static final int SHIP = 201;

    private static NativeMarketView view(final Model model, final int unitId) {
        return model.getNativeMarketView(model.getUnitById(unitId),
                settlement(model)).orElseThrow();
    }

    private static List<Terrain> grassland() {
        final List<Terrain> out = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                out.add(new Terrain(at(dx, dy), TerrainType.GRASSLAND));
            }
        }
        return out;
    }

    /** Assert the one saved expedition: its carrier and its anchor square. */
    private static void assertExpeditionSaved(final Model model,
            final int carrierId, final Location anchor) {
        final NativeExpeditionPo expedition = model.save()
                .getNativeSettlements().get(0).getTrade().get(0)
                .getExpeditions().get(0);
        assertEquals(carrierId, expedition.getCarrierId().intValue());
        assertEquals(anchor, expedition.getAnchor());
    }

    /** Most European turns one round may take before the owner is on turn. */
    private static final int MAX_ROUNDS = 8;

    /** End turns until the owner's next turn is prepared; bounded. */
    private static void ownerRound(final Model model, final Player owner) {
        int rounds = 0;
        do {
            model.endTurn();
            assertTrue(++rounds <= MAX_ROUNDS,
                    "The owner's next turn never came");
        } while (model.getCurrentPlayer() != owner);
    }

    /** The saved unit with this identity, wherever it sits in the save. */
    private static UnitPo unitPo(final ModelPo po, final int id) {
        return po.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst().orElseThrow();
    }

    @Test
    void offeredGoodsRankByNaturalPotentialThenNameAndNeverPad() {
        final List<Terrain> around = grassland();
        final List<GoodsType> offered = NativeMarketRules.offeredGoods(around);
        long producible = GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(goods -> NativeMarketRules.potential(around, goods) > 0)
                .count();
        assertEquals(Math.min(NativeMarketRules.OFFER_SLOTS, producible),
                offered.size());
        for (int i = 0; i < offered.size(); i++) {
            assertTrue(NativeMarketRules.potential(around, offered.get(i)) > 0,
                    offered.get(i).name());
            if (i > 0) {
                final GoodsType before = offered.get(i - 1);
                final GoodsType after = offered.get(i);
                final int byPotential = Integer.compare(
                        NativeMarketRules.potential(around, after),
                        NativeMarketRules.potential(around, before));
                assertTrue(
                        byPotential < 0 || byPotential == 0
                                && before.name().compareTo(after.name()) < 0,
                        before + " before " + after);
            }
        }
    }

    @Test
    void noSurroundingOutputMeansNoOffer() {
        final List<Terrain> barren = new ArrayList<>();
        barren.add(new Terrain(at(0, 0), TerrainType.ARCTIC));
        final long producible = GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(goods -> NativeMarketRules.potential(barren, goods) > 0)
                .count();
        assertEquals(Math.min(NativeMarketRules.OFFER_SLOTS, producible),
                NativeMarketRules.offeredGoods(barren).size());
    }

    @Test
    void wantedGoodsAreTheLowestPotentialWithNameTies() {
        final List<Terrain> around = grassland();
        final List<GoodsType> wanted = NativeMarketRules.wantedGoods(around);
        assertEquals(NativeMarketRules.WANTED_SLOTS, wanted.size());
        final GoodsType last = wanted.get(wanted.size() - 1);
        for (final GoodsType goods : GoodsType.BUYABLE_GOOD_TYPES) {
            if (!wanted.contains(goods)) {
                final int byPotential = Integer.compare(
                        NativeMarketRules.potential(around, goods),
                        NativeMarketRules.potential(around, last));
                assertTrue(
                        byPotential > 0 || byPotential == 0
                                && goods.name().compareTo(last.name()) > 0,
                        goods + " should not precede " + last);
            }
        }
    }

    @Test
    void treasuryGainsAndCapsButNeverLosesGold() {
        assertEquals(1020, NativeMarketRules.treasuryAfterUpdate(1000, false));
        assertEquals(2000, NativeMarketRules.treasuryAfterUpdate(1990, false));
        assertEquals(2000, NativeMarketRules.treasuryAfterUpdate(2000, false));
        // A higher treasury from land payments is kept, not confiscated.
        assertEquals(2500, NativeMarketRules.treasuryAfterUpdate(2500, false));
        assertEquals(2040, NativeMarketRules.treasuryAfterUpdate(2000, true));
        assertEquals(4000, NativeMarketRules.treasuryAfterUpdate(3990, true));
        assertEquals(4500, NativeMarketRules.treasuryAfterUpdate(4500, true));
    }

    @Test
    void restockAddsTenAndStopsAtTheCap() {
        assertEquals(110, NativeMarketRules.stockAfterUpdate(100));
        assertEquals(200, NativeMarketRules.stockAfterUpdate(195));
        assertEquals(200, NativeMarketRules.stockAfterUpdate(200));
    }

    @Test
    void onlyAVillageRefusesTheSameGoodExceptMuskets() {
        assertTrue(NativeMarketRules.repeatsPreviousGood(
                NativeSettlementClass.VILLAGE, GoodsType.CORN, GoodsType.CORN));
        assertFalse(NativeMarketRules.repeatsPreviousGood(
                NativeSettlementClass.CAMP, GoodsType.CORN, GoodsType.CORN));
        assertFalse(NativeMarketRules.repeatsPreviousGood(
                NativeSettlementClass.CITY, GoodsType.CORN, GoodsType.CORN));
        assertFalse(NativeMarketRules.repeatsPreviousGood(
                NativeSettlementClass.VILLAGE, GoodsType.MUSKET,
                GoodsType.MUSKET));
        assertFalse(NativeMarketRules.repeatsPreviousGood(
                NativeSettlementClass.VILLAGE, GoodsType.CORN,
                GoodsType.TOBACCO));
        assertFalse(NativeMarketRules.repeatsPreviousGood(
                NativeSettlementClass.VILLAGE, null, GoodsType.CORN));
    }

    @Test
    void unintroducedWagonIsRefusedAndSeesNoMarketDetails() {
        final Model model = island(NativeNation.AZTEC)
                .market(100, List.of(GoodsType.CORN), WANTED)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED).toModel();
        final NativeMarketView view = view(model, WAGON);
        assertFalse(view.isContacted());
        assertTrue(view.isIntroductionOffered());
        assertEquals(Admission.NO_CONTACT, view.getAdmission());
        assertTrue(view.getMarket().isEmpty());
        assertFalse(view.isExpeditionOpen());
    }

    @Test
    void landIntroductionAdmitsLoadedWagonAndChangesNothingElse() {
        final Model model = island(NativeNation.AZTEC)
                .market(100, List.of(GoodsType.CORN, GoodsType.TOBACCO), WANTED)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED).toModel();
        final Location before = model.getUnitById(WAGON).getLocation();
        final int gold = model.getPlayerByName(DUTCH).getGold();
        model.introduceNativeContact(model.getUnitById(WAGON),
                settlement(model));
        assertTrue(model.isNativeContacted(EuropeanNation.DUTCH,
                NativeNation.AZTEC));
        assertEquals(before, model.getUnitById(WAGON).getLocation());
        assertEquals(gold, model.getPlayerByName(DUTCH).getGold());
        final NativeMarketView view = view(model, WAGON);
        assertEquals(Admission.ADMITTED, view.getAdmission());
        final NativeMarketView.Market market = view.getMarket().orElseThrow();
        assertEquals(2, market.getOffers().size());
        assertEquals(1, market.getUnavailableSlots());
        assertEquals(100, market.getOffers().get(0).getStock());
        assertEquals(WANTED, market.getWanted());
        assertEquals(2000, market.getTreasury());
    }

    @Test
    void introductionIsWholeTribeNationBasedAndRepeatable() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED).toModel();
        model.introduceNativeContact(model.getUnitById(WAGON),
                settlement(model));
        model.introduceNativeContact(model.getUnitById(WAGON),
                settlement(model));
        assertTrue(model.isNativeContacted(EuropeanNation.DUTCH,
                NativeNation.AZTEC));
        assertFalse(model.isNativeContacted(EuropeanNation.FRENCH,
                NativeNation.AZTEC));
        assertFalse(model.isNativeContacted(EuropeanNation.DUTCH,
                NativeNation.CHEROKEE));
        assertEquals(1, model.save().getNativeContacts().size());
    }

    @Test
    void shipCannotIntroduceAndIsRefusedBeforeContact() {
        // The caravel is the only unit added, so it has the first identity.
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.CARAVEL, SEA_ONE, LOADED).toModel();
        final NativeMarketView view = view(model, WAGON);
        assertFalse(view.isIntroductionOffered());
        assertEquals(Admission.NO_CONTACT, view.getAdmission());
        assertThrows(IllegalStateException.class,
                () -> model.introduceNativeContact(model.getUnitById(WAGON),
                        settlement(model)));
        assertFalse(model.isNativeContacted(EuropeanNation.DUTCH,
                NativeNation.AZTEC));
    }

    @Test
    void emptyAndPassengerOnlyCarriersAreRefusedForLackOfGoods() {
        final Model empty = island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), UNARMED).toModel();
        assertEquals(Admission.NO_GOODS, view(empty, WAGON).getAdmission());

        // A coastal caravel beside the capital, with no goods in its hold. Its
        // identity is the first unit added, so it is WAGON; the colonist is not
        // a goods carrier and is placed in the hold as a passenger.
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .unit(DUTCH, UnitType.CARAVEL, SEA_ONE, UNARMED)
                .unit(DUTCH, UnitType.COLONIST, at(-2, 0), UNARMED);
        final ModelPo po = scene.toPo();
        final UnitPo passenger = unitPo(po, WAGON + 1);
        passenger.setPlaceMap(null);
        passenger.setPlaceCargoSlot(new PlaceCargoSlotPo());
        final CargoSlotPo seat = new CargoSlotPo();
        seat.setUnitId(passenger.getId());
        unitPo(po, WAGON).getCargo().getSlots().add(seat);
        final Model carrying = Model.make(po,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(Admission.NO_GOODS, view(carrying, WAGON).getAdmission());
    }

    @Test
    void angryOrHostileRefusesBothAndUneasyRefusesOnlyShips() {
        final Model content = trading(10);
        assertEquals(Admission.ADMITTED, view(content, WAGON).getAdmission());
        assertEquals(Admission.ADMITTED, view(content, SHIP).getAdmission());

        final Model uneasy = trading(30);
        assertEquals(Admission.ADMITTED, view(uneasy, WAGON).getAdmission());
        assertEquals(Admission.SHIP_RELATION_UNEASY,
                view(uneasy, SHIP).getAdmission());

        for (final double anger : List.of(60d, 80d)) {
            final Model tense = trading(anger);
            assertEquals(Admission.RELATION_TENSE,
                    view(tense, WAGON).getAdmission(), "anger " + anger);
            assertEquals(Admission.RELATION_TENSE,
                    view(tense, SHIP).getAdmission(), "anger " + anger);
        }
    }

    /** A contacted tribe with a loaded wagon and a loaded ship beside it. */
    private static Model trading(final double anger) {
        return island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .market(100, List.of(GoodsType.CORN), WANTED)
                .tribal(NativeNation.AZTEC, EuropeanNation.DUTCH, anger)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED)
                .unit(DUTCH, UnitType.CARAVEL, SEA_ONE, LOADED).toModel();
    }

    @Test
    void enteringTheMarketOpensOneExpeditionAndReopeningReusesIt() {
        final Model model = trading(10);
        final Unit wagon = model.getUnitById(WAGON);
        model.enterNativeMarket(wagon, settlement(model));
        assertTrue(view(model, WAGON).isExpeditionOpen());
        model.enterNativeMarket(wagon, settlement(model));
        assertTrue(view(model, WAGON).isExpeditionOpen());
        assertEquals(1,
                model.save().getNativeSettlements().get(0).getTrade().size());
        assertExpeditionSaved(model, WAGON, at(-1, 0));
    }

    @Test
    void refusedCarrierCannotOpenTheMarket() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED).toModel();
        final Unit wagon = model.getUnitById(WAGON);
        assertThrows(IllegalStateException.class,
                () -> model.enterNativeMarket(wagon, settlement(model)));
        assertTrue(model.save().getNativeSettlements().get(0).getTrade()
                .isEmpty());
    }

    @Test
    void departureEndsTheExpeditionButClosingAndOwnerUpdatesDoNot() {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED)
                .withMovesForLastUnit();
        final Model model = scene.toModel();
        final Unit wagon = model.getUnitById(WAGON);
        model.enterNativeMarket(wagon, settlement(model));
        // A closed chooser and the owner update leave the carrier in place.
        model.updateNativeMarkets(NativeNation.AZTEC);
        assertTrue(view(model, WAGON).isExpeditionOpen());
        // A step to another square beside the settlement is a departure.
        wagon.moveOneStep(at(-1, -1));
        assertFalse(view(model, WAGON).isExpeditionOpen());
        model.enterNativeMarket(wagon, settlement(model));
        assertTrue(view(model, WAGON).isExpeditionOpen());
        assertExpeditionSaved(model, WAGON, at(-1, -1));
    }

    /**
     * The village refuses the goods the previous delivery brought, except
     * muskets. Tobacco is carried but not on offer, so it must still count: the
     * rule concerns what the village receives from the player, not its stock.
     */
    @Test
    void villageRefusesTheGoodsOfThePreviousDeliveryExceptMuskets() {
        final Model model = island(NativeNation.CHEROKEE)
                .contact(EuropeanNation.DUTCH, NativeNation.CHEROKEE)
                .market(100, List.of(GoodsType.CORN, GoodsType.MUSKET), WANTED)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0),
                        carrying(GoodsType.TOBACCO))
                .withMovesForLastUnit()
                .unit(DUTCH, UnitType.WAGON, at(0, -1),
                        carrying(GoodsType.TOBACCO, GoodsType.MUSKET))
                .unit(DUTCH, UnitType.WAGON, at(0, 1),
                        carrying(GoodsType.TOBACCO, GoodsType.MUSKET))
                .toModel();
        final Unit first = model.getUnitById(WAGON);
        final Unit second = model.getUnitById(WAGON + 1);
        final Unit third = model.getUnitById(WAGON + 2);
        final NativeSettlement village = settlement(model);

        // The first delivery brings tobacco, which is not offered: accepted.
        model.enterNativeMarket(first, village);
        model.recordNativeGoodsReceived(first, village, GoodsType.TOBACCO);
        assertFalse(model.isRepeatedReceivedGoods(first, village,
                GoodsType.TOBACCO));
        first.moveOneStep(at(-1, -1));

        // The next expedition refuses tobacco but accepts muskets.
        model.enterNativeMarket(second, village);
        assertTrue(model.isRepeatedReceivedGoods(second, village,
                GoodsType.TOBACCO));
        assertThrows(IllegalStateException.class, () -> model
                .recordNativeGoodsReceived(second, village, GoodsType.TOBACCO));
        assertFalse(model.isRepeatedReceivedGoods(second, village,
                GoodsType.MUSKET));
        model.recordNativeGoodsReceived(second, village, GoodsType.MUSKET);

        // After muskets, tobacco is allowed again and muskets stay allowed.
        model.enterNativeMarket(third, village);
        assertFalse(model.isRepeatedReceivedGoods(third, village,
                GoodsType.TOBACCO));
        assertFalse(model.isRepeatedReceivedGoods(third, village,
                GoodsType.MUSKET));
    }

    @Test
    void deliveryNeedsAnOpenExpeditionAndGoodsTheCarrierHolds() {
        final Model model = trading(10);
        final Unit wagon = model.getUnitById(WAGON);
        assertThrows(IllegalStateException.class,
                () -> model.recordNativeGoodsReceived(wagon, settlement(model),
                        GoodsType.CORN));
        model.enterNativeMarket(wagon, settlement(model));
        assertThrows(IllegalStateException.class,
                () -> model.recordNativeGoodsReceived(wagon, settlement(model),
                        GoodsType.TOBACCO));
    }

    @Test
    void expeditionAndContactSurviveSaveAndContinueWithoutRestock() {
        final Model model = trading(10);
        model.enterNativeMarket(model.getUnitById(WAGON), settlement(model));
        model.recordNativeGoodsReceived(model.getUnitById(WAGON),
                settlement(model), GoodsType.CORN);
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(restored.isNativeContacted(EuropeanNation.DUTCH,
                NativeNation.AZTEC));
        assertTrue(view(restored, WAGON).isExpeditionOpen());
        assertEquals(Optional.of(GoodsType.CORN),
                settlement(restored).findTrade(EuropeanNation.DUTCH)
                        .orElseThrow().getLastReceived());
        assertEquals(100, settlement(restored).getStock(GoodsType.CORN));
        assertEquals(2000, settlement(restored).getTreasury());
    }

    /**
     * Two carriers open expeditions at once. Opening the second one neither
     * replaces the first nor resets its refusal; reopening the first returns
     * the same expedition, so the refusal fixed when it began stays.
     */
    @Test
    void twoCarriersKeepTheirOwnOpenExpeditions() {
        final Model model = island(NativeNation.CHEROKEE)
                .contact(EuropeanNation.DUTCH, NativeNation.CHEROKEE)
                .market(100, List.of(GoodsType.CORN), WANTED)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED)
                .unit(DUTCH, UnitType.WAGON, at(0, -1), LOADED).toModel();
        final Unit first = model.getUnitById(WAGON);
        final Unit second = model.getUnitById(WAGON + 1);
        final NativeSettlement village = settlement(model);

        model.enterNativeMarket(first, village);
        model.recordNativeGoodsReceived(first, village, GoodsType.CORN);
        // The second carrier opens after the first delivered corn: its refusal
        // is corn, and the first carrier's expedition stays open.
        model.enterNativeMarket(second, village);
        assertTrue(view(model, WAGON).isExpeditionOpen());
        assertTrue(view(model, WAGON + 1).isExpeditionOpen());
        assertTrue(
                model.isRepeatedReceivedGoods(second, village, GoodsType.CORN));
        // Reopening the first carrier reuses its expedition: its refusal did
        // not
        // move to corn, and nothing it began was reset.
        model.enterNativeMarket(first, village);
        assertFalse(
                model.isRepeatedReceivedGoods(first, village, GoodsType.CORN));
        assertEquals(2, model.save().getNativeSettlements().get(0).getTrade()
                .get(0).getExpeditions().size());
        assertTrue(view(model, WAGON).isExpeditionOpen());
    }

    /**
     * A carrier that leaves and comes back to its square is not an open
     * expedition. Saving does not revive it, and Continue binds nothing to it.
     * An expedition still standing on its square survives Save and Continue.
     */
    @Test
    void leavingAndReturningEndsTheExpeditionThroughSaveAndContinue() {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                .market(100, List.of(GoodsType.CORN), WANTED)
                .unit(DUTCH, UnitType.WAGON, at(-1, 0), LOADED)
                .withMovesForLastUnit();
        final Model model = scene.toModel();
        final Unit wagon = model.getUnitById(WAGON);
        model.enterNativeMarket(wagon, settlement(model));
        wagon.moveOneStep(at(-1, -1));
        // The step spent the whole allowance; the owner's next turn renews it
        // legally, then the wagon returns without entering the market again.
        ownerRound(model, model.getPlayerByName(DUTCH));
        wagon.moveOneStep(at(-1, 0));
        assertFalse(view(model, WAGON).isExpeditionOpen());

        // Saving twice writes the same live state and changes no query.
        final Gson gson = new Gson();
        final String saved = gson.toJson(model.save());
        assertEquals(saved, gson.toJson(model.save()));
        assertFalse(view(model, WAGON).isExpeditionOpen());

        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertFalse(view(restored, WAGON).isExpeditionOpen());
        assertTrue(restored.save().getNativeSettlements().get(0).getTrade()
                .get(0).getExpeditions().isEmpty());

        // Entering again at the same square starts a fresh expedition.
        restored.enterNativeMarket(restored.getUnitById(WAGON),
                settlement(restored));
        assertTrue(view(restored, WAGON).isExpeditionOpen());
        final Model again = Model.make(restored.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(view(again, WAGON).isExpeditionOpen());
    }

    @Test
    void ownerUpdateRestocksOnceAndCapsWithoutConfiscation() {
        final Model model = island(NativeNation.AZTEC)
                .market(195, List.of(GoodsType.CORN), WANTED).toModel();
        model.updateNativeMarkets(NativeNation.AZTEC);
        assertEquals(200, settlement(model).getStock(GoodsType.CORN));
        assertEquals(2040, settlement(model).getTreasury());
        model.updateNativeMarkets(NativeNation.AZTEC);
        assertEquals(200, settlement(model).getStock(GoodsType.CORN));
        assertEquals(2080, settlement(model).getTreasury());
        // Another nation's update changes nothing here.
        model.updateNativeMarkets(NativeNation.CHEROKEE);
        assertEquals(2080, settlement(model).getTreasury());

        final Model rich = island(NativeNation.AZTEC).treasuryOfFirst(4500)
                .toModel();
        rich.updateNativeMarkets(NativeNation.AZTEC);
        assertEquals(4500, settlement(rich).getTreasury());
    }

    @Test
    void askingChangesNothingAndViewsStayPure() {
        final Model model = trading(10);
        final Gson gson = new Gson();
        final String before = gson.toJson(model.save());
        view(model, WAGON);
        view(model, SHIP);
        assertEquals(before, gson.toJson(model.save()));
    }

    @Test
    void invalidMarketSavesAreRejectedBeforeTheModelIsBuilt() {
        assertThrows(IllegalArgumentException.class,
                () -> island(NativeNation.AZTEC).market(100,
                        List.of(GoodsType.CORN, GoodsType.TOBACCO,
                                GoodsType.SUGAR, GoodsType.COTTON),
                        WANTED).toModel(),
                "more than three offered goods");
        assertThrows(IllegalArgumentException.class,
                () -> island(NativeNation.AZTEC)
                        .market(100, List.of(GoodsType.HAMMERS), WANTED)
                        .toModel(),
                "goods that cannot be bought");
        assertThrows(IllegalArgumentException.class,
                () -> island(NativeNation.AZTEC)
                        .market(201, List.of(GoodsType.CORN), WANTED).toModel(),
                "stock above the cap");
        assertThrows(IllegalArgumentException.class,
                () -> island(NativeNation.AZTEC)
                        .market(100, List.of(GoodsType.CORN),
                                List.of(GoodsType.MUSKET, GoodsType.MUSKET))
                        .toModel(),
                "duplicate wanted goods");
        assertThrows(IllegalArgumentException.class,
                () -> island(NativeNation.AZTEC)
                        .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                        .contact(EuropeanNation.DUTCH, NativeNation.AZTEC)
                        .toModel(),
                "duplicate contact");
    }

}
