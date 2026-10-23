package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.microcol.model.store.*;
import org.microcol.model.unit.UnitTreasure;
import org.microcol.model.unit.UnitWithCargo;

class TreasureRedemptionTest {
    private static final int TRAIN = 7900;
    private static final int SHIP = 7901;

    private Model world(final boolean europe) {
        ModelPo po = NativeTurnScene.island()
                .rawUnit(TRAIN, UnitType.TREASURE_TRAIN, "Dutch", null, null,
                        NativeTurnScene.at(4, 0))
                .rawUnit(7902, UnitType.COLONIST, "Dutch", null, null,
                        NativeTurnScene.at(4, 0))
                .po();
        UnitPo train = po.getUnits().stream().filter(u -> u.getId() == TRAIN)
                .findFirst().orElseThrow();
        train.setTreasureValue(501);
        po.getUnits().stream().filter(u -> u.getId() == 7902).findFirst()
                .orElseThrow().setMovementThirds(MovementPoints.ofPoints(1));
        if (europe) {
            UnitPo ship = NativeTurnScene.island()
                    .rawUnit(SHIP, UnitType.GALLEON, "Dutch", null, null,
                            NativeTurnScene.at(5, 0))
                    .po().getUnits().stream().filter(u -> u.getId() == SHIP)
                    .findFirst().orElseThrow();
            ship.setPlaceMap(null);
            ship.setPlaceEuropePort(new PlaceEuropePortPo());
            for (int i = 0; i < 6; i++) {
                CargoSlotPo slot = new CargoSlotPo();
                slot.setUnitId(TRAIN);
                ship.getCargo().getSlots().add(slot);
            }
            po.getUnits().add(ship);
            train.setPlaceMap(null);
            train.setPlaceCargoSlot(new PlaceCargoSlotPo());
        }
        po.getGameManager().setGameStarted(true);
        po.getGameManager().setCurrentPlayer("Dutch");
        Model model = NativeTurnScene.make(po);
        model.buildColony(model.getPlayerByName("Dutch"),
                model.getUnitById(7902), "Treasure Port");
        return model;
    }

    private UnitTreasure train(final Model model) {
        return (UnitTreasure) model.getUnitById(TRAIN);
    }

    private Colony port(final Model model) {
        return model.getColonyAt(train(model).getLocation()).orElseThrow();
    }

    @Test
    void europePaysTaxOnlyOnceAndReleasesEveryHold() {
        Model model = world(true);
        Player owner = train(model).getOwner();
        int gold = owner.getGold();
        TreasureRedemptionQuote quote = model
                .quoteTreasureRedemption(train(model), null).orElseThrow();
        assertEquals(0, quote.getTransport());
        assertEquals(501L * (100 - owner.getTaxRate()) / 100, quote.getNet());
        model.redeemTreasure(quote);
        assertEquals(gold + quote.getNet(), owner.getGold());
        assertTrue(model.tryGetUnitById(TRAIN).isEmpty());
        assertTrue(((UnitWithCargo) model.getUnitById(SHIP)).getCargo()
                .getSlots().stream().allMatch(CargoSlot::isEmpty));
        assertThrows(IllegalStateException.class,
                () -> model.redeemTreasure(quote));
        Model loaded = NativeTurnScene.make(model.save());
        assertEquals(owner.getGold(),
                loaded.getPlayerByName("Dutch").getGold());
        assertTrue(loaded.tryGetUnitById(TRAIN).isEmpty());
    }

    @Test
    void royalPickupWaitsAcrossReloadAndPaysConfirmedNetOnlyOnce() {
        Model model = world(false);
        Player owner = train(model).getOwner();
        int gold = owner.getGold();
        TreasureRedemptionQuote quote = model
                .quoteTreasureRedemption(train(model), port(model))
                .orElseThrow();
        assertEquals(250, quote.getTransport());
        assertEquals(251L * (100 - owner.getTaxRate()) / 100, quote.getNet());
        model.redeemTreasure(quote);
        assertEquals(gold, owner.getGold());
        assertTrue(model.tryGetUnitById(TRAIN).isPresent());
        assertThrows(IllegalStateException.class,
                () -> model.redeemTreasure(quote));
        Model loaded = NativeTurnScene.make(model.save());
        Player loadedOwner = loaded.getPlayerByName("Dutch");
        assertEquals(gold, loadedOwner.getGold());
        loaded.collectRoyalTreasure(loadedOwner);
        assertEquals(gold + quote.getNet(), loadedOwner.getGold());
        assertTrue(loaded.tryGetUnitById(TRAIN).isEmpty());
        loaded.collectRoyalTreasure(loadedOwner);
        assertEquals(gold + quote.getNet(), loadedOwner.getGold());
    }

    @Test
    void quotingAndCancelChangeNothingAndStaleTreasuryRejectsBeforeRemoval() {
        Model model = world(true);
        UnitTreasure train = train(model);
        Player owner = train.getOwner();
        int gold = owner.getGold();
        TreasureRedemptionQuote quote = model
                .quoteTreasureRedemption(train, null).orElseThrow();
        assertEquals(gold, owner.getGold());
        assertSame(train, model.getUnitById(TRAIN));
        owner.setGold(gold + 1);
        assertThrows(IllegalStateException.class,
                () -> model.redeemTreasure(quote));
        assertSame(train, model.getUnitById(TRAIN));
        assertEquals(gold + 1, owner.getGold());
        owner.setGold(Integer.MAX_VALUE);
        assertTrue(model.quoteTreasureRedemption(train, null).isEmpty());
    }

    @Test
    void independenceClosesBothRoutesAndCancelsPendingCollectionWithoutPayment() {
        Model model = world(false);
        Player owner = train(model).getOwner();
        model.redeemTreasure(
                model.quoteTreasureRedemption(train(model), port(model))
                        .orElseThrow());
        int gold = owner.getGold();
        ModelPo saved = model.save();
        RebellionFixtures.declare(saved,
                saved.getGameManager().getPlayers().stream()
                        .filter(p -> p.getName().equals("Dutch")).findFirst()
                        .orElseThrow());
        ClosedEuropeFixtures.seizeHomeEurope(saved);
        Model loaded = NativeTurnScene.make(saved);
        Player rebel = loaded.getPlayerByName("Dutch");
        loaded.collectRoyalTreasure(rebel);
        assertEquals(gold, rebel.getGold());
        assertNotNull(train(loaded));
        assertNull(train(loaded).getPickupColonyId());
        assertTrue(loaded.quoteTreasureRedemption(train(loaded), port(loaded))
                .isEmpty());
        Model overseas = world(true);
        ModelPo overseasPo = overseas.save();
        RebellionFixtures.declare(overseasPo,
                overseasPo.getGameManager().getPlayers().stream()
                        .filter(p -> p.getName().equals("Dutch")).findFirst()
                        .orElseThrow());
        ClosedEuropeFixtures.seizeHomeEurope(overseasPo);
        Model closed = NativeTurnScene.make(overseasPo);
        assertTrue(
                closed.quoteTreasureRedemption(train(closed), null).isEmpty());
    }

    @Test
    void lossBeforeCollectionNeverPays() {
        Model model = world(false);
        Player owner = train(model).getOwner();
        int gold = owner.getGold();
        model.redeemTreasure(
                model.quoteTreasureRedemption(train(model), port(model))
                        .orElseThrow());
        model.destroyUnit(train(model));
        model.collectRoyalTreasure(owner);
        assertEquals(gold, owner.getGold());
    }

    @Test
    void movedTrainCancelsPickupWithoutPaymentAndCanBeOfferedAgainAtPort() {
        Model model = world(false);
        Player owner = train(model).getOwner();
        int gold = owner.getGold();
        model.redeemTreasure(
                model.quoteTreasureRedemption(train(model), port(model))
                        .orElseThrow());
        ModelPo saved = model.save();
        saved.getUnits().stream().filter(u -> u.getId() == TRAIN).findFirst()
                .orElseThrow().getPlaceMap()
                .setLocation(NativeTurnScene.at(3, 0));
        Model loaded = NativeTurnScene.make(saved);
        loaded.collectRoyalTreasure(loaded.getPlayerByName("Dutch"));
        assertEquals(gold, loaded.getPlayerByName("Dutch").getGold());
        assertNull(train(loaded).getPickupColonyId());
        assertEquals(NativeTurnScene.at(3, 0), train(loaded).getLocation());
        assertTrue(loaded
                .quoteTreasureRedemption(train(loaded), loaded
                        .getColonyAt(NativeTurnScene.at(4, 0)).orElseThrow())
                .isEmpty());
    }

    @Test
    void overflowingTreasuryAtPickupCancelsWithoutLosingTrain() {
        Model model = world(false);
        Player owner = train(model).getOwner();
        model.redeemTreasure(
                model.quoteTreasureRedemption(train(model), port(model))
                        .orElseThrow());
        owner.setGold(Integer.MAX_VALUE);
        model.collectRoyalTreasure(owner);
        assertEquals(Integer.MAX_VALUE, owner.getGold());
        assertNotNull(train(model));
        assertNull(train(model).getPickupColonyId());
    }

    @Test
    void incompleteOrExcessiveSavedPickupIsRejected() {
        Model model = world(false);
        ModelPo saved = model.save();
        UnitPo train = saved.getUnits().stream().filter(u -> u.getId() == TRAIN)
                .findFirst().orElseThrow();
        train.setTreasurePickupColonyId(port(model).getId());
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(saved));
        train.setTreasurePickupNet(502);
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(saved));
        train.setTreasurePickupNet(0);
        train.setTreasurePickupColonyId(-1);
        assertThrows(IllegalArgumentException.class,
                () -> NativeTurnScene.make(saved));
    }

    @Test
    void changedTaxRejectsUnconfirmedOfferButPreservesAlreadyConfirmedPickup() {
        Model model = world(false);
        Player owner = train(model).getOwner();
        TreasureRedemptionQuote old = model
                .quoteTreasureRedemption(train(model), port(model))
                .orElseThrow();
        owner.setTaxRate(37);
        assertThrows(IllegalStateException.class,
                () -> model.redeemTreasure(old));
        assertNull(train(model).getPickupColonyId());
        TreasureRedemptionQuote fresh = model
                .quoteTreasureRedemption(train(model), port(model))
                .orElseThrow();
        model.redeemTreasure(fresh);
        int gold = owner.getGold();
        owner.setTaxRate(40);
        model.collectRoyalTreasure(owner);
        assertEquals(gold + fresh.getNet(), owner.getGold());
    }
}
