package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microcol.model.NativeRelationsScene.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementPo;

/**
 * Model rules of paying for one native-claimed square. The Dutch colonist on
 * the tile east of the city gives the player sight of the claim owner, so the
 * claim is known; the city capital claims the tile at one square.
 */
class NativeLandPurchaseTest {

    private static final Location TILE = at(1, 0);

    private static NativeRelationsScene dutchOnTile() {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, TILE, UNARMED).gold(DUTCH, 500);
    }

    private static NativeLandQuote quote(final Model model, final String owner,
            final Location at) {
        return model.quoteNativeLand(model.getPlayerByName(owner), at)
                .orElseThrow();
    }

    @Test
    void capitalPriceFollowsDifficultyAndDoublesForTheCapital() {
        final Difficulty[] levels = { Difficulty.DISCOVERER,
                Difficulty.EXPLORER, Difficulty.CONQUISTADOR,
                Difficulty.GOVERNOR, Difficulty.VICEROY };
        final int[] prices = { 150, 180, 200, 230, 260 };
        for (int i = 0; i < levels.length; i++) {
            final NativeRelationsScene scene = dutchOnTile();
            scene.toPo().setDifficulty(levels[i]);
            assertEquals(prices[i],
                    quote(scene.toModel(), DUTCH, TILE).getPrice());
        }
    }

    @Test
    void buyDebitsThePlayerAndCreditsTheClaimantExactly() {
        final Model model = dutchOnTile().toModel();
        final NativeLandQuote offer = quote(model, DUTCH, TILE);
        assertEquals(NativeLandStatus.UNPAID, offer.getStatus());
        assertTrue(model.canBuyNativeLand(offer));
        model.buyNativeLand(offer);
        assertEquals(300, model.getPlayerByName(DUTCH).getGold());
        assertEquals(2200, settlement(model).getTreasury());
        final NativeLandQuote after = quote(model, DUTCH, TILE);
        assertEquals(NativeLandStatus.PAID, after.getStatus());
        assertEquals(0, after.getPrice());
    }

    @Test
    void insufficientFundsRefusesWithoutAnyChange() {
        final Model model = dutchOnTile().gold(DUTCH, 199).toModel();
        final NativeLandQuote offer = quote(model, DUTCH, TILE);
        assertFalse(model.canBuyNativeLand(offer));
        assertThrows(IllegalArgumentException.class,
                () -> model.buyNativeLand(offer));
        assertEquals(199, model.getPlayerByName(DUTCH).getGold());
        assertEquals(2000, settlement(model).getTreasury());
        assertEquals(NativeLandStatus.UNPAID,
                quote(model, DUTCH, TILE).getStatus());
    }

    @Test
    void staleQuoteIsRefusedAfterThePaymentOfAnotherQuote() {
        final Model model = dutchOnTile().toModel();
        final NativeLandQuote first = quote(model, DUTCH, TILE);
        final NativeLandQuote second = quote(model, DUTCH, TILE);
        model.buyNativeLand(first);
        assertFalse(model.canBuyNativeLand(second));
        assertThrows(IllegalArgumentException.class,
                () -> model.buyNativeLand(second));
        assertEquals(300, model.getPlayerByName(DUTCH).getGold());
        assertEquals(2200, settlement(model).getTreasury());
    }

    @Test
    void anotherCurrentPlayerCannotPayForTheQuote() {
        final Model model = dutchOnTile().currentPlayer(FRANCE).toModel();
        final NativeLandQuote offer = quote(model, DUTCH, TILE);
        assertFalse(model.canBuyNativeLand(offer));
        assertThrows(IllegalArgumentException.class,
                () -> model.buyNativeLand(offer));
        assertEquals(500, model.getPlayerByName(DUTCH).getGold());
        assertEquals(2000, settlement(model).getTreasury());
    }

    @Test
    void paidRightBelongsOnlyToTheEuropeanWhoPaid() {
        final Model model = dutchOnTile()
                .unit(FRANCE, UnitType.COLONIST, at(1, 1), UNARMED).toModel();
        model.buyNativeLand(quote(model, DUTCH, TILE));
        assertEquals(NativeLandStatus.PAID,
                quote(model, DUTCH, TILE).getStatus());
        assertEquals(NativeLandStatus.UNPAID,
                quote(model, FRANCE, TILE).getStatus());
    }

    @Test
    void minuitWaivesThePriceAndTheQuoteIsNotPayable() {
        final Model model = dutchOnTile().minuitMember(DUTCH).toModel();
        final NativeLandQuote offer = quote(model, DUTCH, TILE);
        assertEquals(NativeLandStatus.WAIVED, offer.getStatus());
        assertEquals(0, offer.getPrice());
        assertFalse(model.canBuyNativeLand(offer));
        assertEquals(500, model.getPlayerByName(DUTCH).getGold());
    }

    @Test
    void ordinarySettlementChargesTheBasePriceAndCreditsOnlyItself() {
        final Model model = dutchOnTile().ordinaryWithCapitalAt(at(4, 4))
                .toModel();
        final NativeLandQuote offer = quote(model, DUTCH, TILE);
        assertFalse(offer.getClaimant().isCapital());
        // Conquistador base 100, not doubled; the capital is not involved.
        assertEquals(100, offer.getPrice());
        model.buyNativeLand(offer);
        assertEquals(400, model.getPlayerByName(DUTCH).getGold());
        assertEquals(1100, model.getNativeSettlements().get(0).getTreasury());
        assertEquals(2000, model.getNativeSettlements().get(1).getTreasury());
    }

    @Test
    void claimantStaysHiddenUntilItsIdentityIsLearned() {
        // No Dutch unit is near, so neither the square nor the claimant is
        // known and the quote reveals nothing.
        final Model model = island(NativeNation.AZTEC).gold(DUTCH, 500)
                .toModel();
        assertTrue(model.quoteNativeLand(model.getPlayerByName(DUTCH), TILE)
                .isEmpty());
    }

    @Test
    void treasuryOverflowIsRefusedBeforeAnyChange() {
        final NativeRelationsScene scene = dutchOnTile();
        // 100 below the maximum cannot take a 200 payment.
        scene.toPo().getNativeSettlements().get(0)
                .setTreasury(Integer.MAX_VALUE - 100);
        final Model model = scene.toModel();
        final NativeLandQuote offer = quote(model, DUTCH, TILE);
        assertFalse(model.canBuyNativeLand(offer));
        assertThrows(IllegalArgumentException.class,
                () -> model.buyNativeLand(offer));
        assertEquals(500, model.getPlayerByName(DUTCH).getGold());
        assertEquals(Integer.MAX_VALUE - 100, settlement(model).getTreasury());
        assertEquals(0, model.save().getNativeLandRights().size());
        assertEquals(NativeLandStatus.UNPAID,
                quote(model, DUTCH, TILE).getStatus());
    }

    @Test
    void paidRightSurvivesWhenTheClaimantNoLongerClaimsTheSquare() {
        final Model model = dutchOnTile().toModel();
        model.buyNativeLand(quote(model, DUTCH, TILE));
        final ModelPo saved = model.save();
        // The only settlement moves away, so nothing claims the square now.
        saved.getNativeSettlements().get(0).setLocation(at(-3, 0));
        final Model loaded = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertTrue(loaded.quoteNativeLand(loaded.getPlayerByName(DUTCH), TILE)
                .isEmpty());
        // The right is neither lost nor rejected at load.
        final ModelPo again = loaded.save();
        assertEquals(1, again.getNativeLandRights().size());
        assertEquals(TILE, again.getNativeLandRights().get(0).getLocation());
        assertEquals(300, loaded.getPlayerByName(DUTCH).getGold());
    }

    @Test
    void paidRightHoldsWhenAnotherSettlementBecomesTheClaimant() {
        final Model model = dutchOnTile().toModel();
        model.buyNativeLand(quote(model, DUTCH, TILE));
        final ModelPo saved = model.save();
        // The first settlement moves off and a Tupi camp one square the other
        // way now owns the claim on the same square.
        saved.getNativeSettlements().get(0).setLocation(at(-3, 0));
        final NativeSettlementPo camp = new NativeSettlementPo();
        camp.setId(1);
        camp.setNation(NativeNation.TUPI);
        camp.setSettlementClass(NativeNation.TUPI.getSettlementClass());
        camp.setCapital(true);
        camp.setTreasury(2000);
        camp.setTeachableSkill(UnitType.EXPERT_FARMER.name());
        camp.setOfferedGoods(List.of());
        camp.setWantedGoods(List.of());
        camp.setTrade(List.of());
        camp.setLocation(at(2, 0));
        saved.getNativeSettlements().add(camp);
        saved.setNativeLocalAlarm(
                NativeRelations.freshLocalAlarm(saved.getNativeSettlements()));
        final Model loaded = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        final NativeLandQuote after = quote(loaded, DUTCH, TILE);
        // One purchase satisfies the obligation: no hidden second claim.
        assertEquals(NativeLandStatus.PAID, after.getStatus());
        assertEquals(1, after.getClaimant().getId());
        assertEquals(0, after.getPrice());
        assertEquals(300, loaded.getPlayerByName(DUTCH).getGold());
        assertEquals(2000, loaded.getNativeSettlements().get(1).getTreasury());
    }

    @Test
    void paidRightAndTreasurySurviveSaveAndLoadWithoutSideEffects() {
        final Model model = dutchOnTile().toModel();
        model.buyNativeLand(quote(model, DUTCH, TILE));
        final Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(300, loaded.getPlayerByName(DUTCH).getGold());
        assertEquals(2200, settlement(loaded).getTreasury());
        assertEquals(NativeLandStatus.PAID,
                quote(loaded, DUTCH, TILE).getStatus());
    }

}
