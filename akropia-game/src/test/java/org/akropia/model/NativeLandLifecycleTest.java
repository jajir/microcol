package org.akropia.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.akropia.model.NativeRelationsScene.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.store.ModelPo;
import org.akropia.model.unit.UnitActionType;

/**
 * Continuous land returns through the real movement, combat and save
 * boundaries.
 */
class NativeLandLifecycleTest {
    private static Model scene(final int favour) {
        final ModelPo saved = island(NativeNation.AZTEC).unit(DUTCH,
                UnitType.COLONIST, at(1, 0), unit -> unit.setMovementThirds(3))
                .toPo();
        saved.getNativeFriendships().stream()
                .filter(row -> row.getNation() == NativeNation.AZTEC)
                .forEach(row -> {
                    row.getFavour().put(EuropeanNation.DUTCH, favour);
                    row.setFriend(favour >= 8 ? EuropeanNation.DUTCH : null);
                });
        return Model.make(saved, MissionImpl.getPredefinedGameOverEvaluators());
    }

    private static void attack(final Model model, final long id) {
        model.applyNativeRelationOutcome(model.getPlayerByName(DUTCH),
                NativeRelationOutcome.settlementAttack(id, settlement(model),
                        false));
    }

    @Test
    void anotherNationCannotSellTheRetainedOriginalClaim() {
        final ModelPo saved = scene(5).save();
        final org.akropia.model.store.LandOwnershipPo claim = new org.akropia.model.store.LandOwnershipPo();
        claim.setLocation(at(1, 0));
        claim.setNativeClaim(NativeNation.CHEROKEE);
        saved.getLandOwnership()
                .removeIf(row -> row.getLocation().equals(at(1, 0)));
        saved.getLandOwnership().add(claim);
        final Model model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        assertEquals(Optional.of(NativeNation.CHEROKEE),
                model.getNativeLandClaim(at(1, 0)));
        assertTrue(model.quoteNativeLand(model.getPlayerByName(DUTCH), at(1, 0))
                .isEmpty());
    }

    @Test
    void startingPioneerWorkAcquiresFriendlyLand() {
        final ModelPo saved = scene(8).save();
        saved.getUnits().stream().filter(unit -> unit.getId() == 200)
                .forEach(unit -> unit.setTools(80));
        final Model model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player owner = model.getPlayerByName(DUTCH);
        model.getUnitById(200).setActionType(UnitActionType.plowField);
        assertEquals(Optional.of(owner), model.getLandOwner(at(1, 0)));
        attack(model, 10);
        assertEquals(Optional.of(owner), model.getLandOwner(at(1, 0)));
        model.destroyUnit(model.getUnitById(200));
        assertFalse(model.canUseLand(owner, at(1, 0)));
    }

    @Test
    void freeLandStaysOccupiedThenReturnsOnDepartureAndAfterContinue() {
        final Model model = scene(8);
        final Player owner = model.getPlayerByName(DUTCH);
        final Unit unit = model.getUnitById(200);
        model.recordNativeLandUse(owner, at(1, 0));
        attack(model, 1);
        assertEquals(5, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
        assertEquals(Optional.of(owner), model.getLandOwner(at(1, 0)));
        final Model restored = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        restored.moveUnit(restored.getUnitById(200),
                Path.of(java.util.List.of(at(2, 0))));
        assertFalse(restored.getLandOwner(at(1, 0))
                .equals(Optional.of(restored.getPlayerByName(DUTCH))));
        assertEquals(Optional.of(NativeNation.AZTEC),
                restored.getNativeLandClaim(at(1, 0)));
        assertFalse(
                restored.canUseLand(restored.getPlayerByName(DUTCH), at(1, 0)));
        assertTrue(model.tryGetUnitById(unit.getId()).isPresent());
    }

    @Test
    void destroyingLastOccupantReturnsFreeLand() {
        final Model model = scene(8);
        model.recordNativeLandUse(model.getPlayerByName(DUTCH), at(1, 0));
        attack(model, 1);
        model.destroyUnit(model.getUnitById(200));
        assertFalse(model.canUseLand(model.getPlayerByName(DUTCH), at(1, 0)));
        assertEquals(Optional.of(NativeNation.AZTEC),
                model.getNativeLandClaim(at(1, 0)));
    }

    @Test
    void paidLandSurvivesHostilityAndDuplicateAttackDoesNotSubtractAgain() {
        final Model model = scene(5);
        final Player owner = model.getPlayerByName(DUTCH);
        model.buyNativeLand(
                model.quoteNativeLand(owner, at(1, 0)).orElseThrow());
        attack(model, 1);
        attack(model, 1);
        assertEquals(2, model.getNativeFavour(NativeNation.AZTEC,
                EuropeanNation.DUTCH));
        model.getUnitById(200).placeToLocation(at(3, 0));
        assertEquals(Optional.of(owner), model.getLandOwner(at(1, 0)));
        assertTrue(model.canUseLand(owner, at(1, 0)));
        assertFalse(model.canAcquireLand(owner, at(0, 1)));
        final NativeLandQuote hostile = model.quoteNativeLand(owner, at(0, 1))
                .orElseThrow();
        final int gold = owner.getGold();
        assertThrows(IllegalArgumentException.class,
                () -> model.buyNativeLand(hostile));
        assertEquals(gold, owner.getGold());
    }
}
