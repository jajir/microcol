package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.microcol.model.NativeRelationsScene.*;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;

/**
 * Unpaid claimed land as a local input. The Montreal fixtures of
 * {@link NativeRelationsScene} keep a French paid right unless a test removes
 * it with {@code withoutRights()}, so the resident and fort tests stay exact.
 */
class NativeLandPressureTest {

    private static final double EXACT = 1e-9;

    private static Model updated(final NativeRelationsScene scene,
            final String owner) {
        final Model model = scene.toModel();
        model.updateNativeRelations(model.getPlayerByName(owner));
        return model;
    }

    private static void assertRelations(final Model model, final String owner,
            final double alarm, final double anger) {
        final Player player = model.getPlayerByName(owner);
        assertEquals(alarm,
                model.getNativeLocalAlarm(player, settlement(model)), EXACT);
        assertEquals(anger,
                model.getNativeTribalAnger(player, NativeNation.AZTEC), EXACT);
    }

    @Test
    void unpaidColonySquareAddsTwoBeforeFranceHalvesIt() {
        // The worker's 0.25 resident input plus 2 unpaid, halved by France.
        assertRelations(
                updated(besideMontreal(NativeNation.AZTEC, 2).withoutRights(),
                        FRANCE),
                FRANCE, 1.125, 1.125 / 20);
    }

    @Test
    void paidColonySquareAddsNothingToUnpaidPressure() {
        assertRelations(updated(besideMontreal(NativeNation.AZTEC, 2), FRANCE),
                FRANCE, 0.125, 0.125 / 20);
    }

    @Test
    void pocahontasStacksWithFranceOnUnpaidPressure() {
        // Factors multiply: 0.5 × 0.5. Resident 0.25 and unpaid 2 give 0.5625.
        assertRelations(
                updated(besideMontreal(NativeNation.AZTEC, 2).withoutRights()
                        .pocahontasMember(FRANCE), FRANCE),
                FRANCE, 0.5625, 0.5625 / 20);
    }

    @Test
    void minuitRemovesOnlyUnpaidPressureNotResidents() {
        // The resident worker still counts: 0.25 halved by France.
        assertRelations(
                updated(besideMontreal(NativeNation.AZTEC, 2).withoutRights()
                        .minuitMember(FRANCE), FRANCE),
                FRANCE, 0.125, 0.125 / 20);
    }

    @Test
    void firstUnpaidUseTakesFivePerEuropeanOnceAndDoesNotRepeat() {
        final Model model = besideMontreal(NativeNation.AZTEC, 2)
                .withoutRights().toModel();
        final Player france = model.getPlayerByName(FRANCE);
        model.recordNativeLandUse(france, MONTREAL);
        model.recordNativeLandUse(france, MONTREAL);
        // Halved by France once: 5 × 0.5. No update has run, so no anger.
        assertEquals(2.5, model.getNativeLocalAlarm(france, settlement(model)),
                EXACT);
        assertEquals(0, model.getNativeTribalAnger(france, NativeNation.AZTEC),
                EXACT);
        assertEquals(1, model.save().getNativeLandTakings().size());
    }

    @Test
    void paidOrMinuitUseTakesNothing() {
        final Model paid = besideMontreal(NativeNation.AZTEC, 2).toModel();
        paid.recordNativeLandUse(paid.getPlayerByName(FRANCE), MONTREAL);
        assertEquals(0, paid.getNativeLocalAlarm(paid.getPlayerByName(FRANCE),
                settlement(paid)), EXACT);
        assertEquals(0, paid.save().getNativeLandTakings().size());

        final Model minuit = besideMontreal(NativeNation.AZTEC, 2)
                .withoutRights().minuitMember(FRANCE).toModel();
        minuit.recordNativeLandUse(minuit.getPlayerByName(FRANCE), MONTREAL);
        assertEquals(0, minuit.getNativeLocalAlarm(
                minuit.getPlayerByName(FRANCE), settlement(minuit)), EXACT);
    }

    /**
     * A square one step east of the city, three squares from Montreal. It lies
     * outside Montreal's ring of fields, so it can only ever be a taken square.
     */
    private static final Location TAKEN = Location.of(MONTREAL.getX() + 3,
            MONTREAL.getY());

    @Test
    void aTakenSquareKeepsAddingTwoEveryUpdateOnTopOfTheColonySquare() {
        final Model model = besideMontreal(NativeNation.AZTEC, 2).landAt(TAKEN)
                .withoutRights().toModel();
        final Player france = model.getPlayerByName(FRANCE);
        model.recordNativeLandUse(france, TAKEN);
        assertEquals(2.5, model.getNativeLocalAlarm(france, settlement(model)),
                EXACT);
        // Two distinct unpaid squares: Montreal and the taken one. The
        // resident 0.25 and 2 + 2 are halved by France: 2.5 - 1 + 2.125.
        model.updateNativeRelations(france);
        assertRelations(model, FRANCE, 3.625, 3.625 / 20);
        // The taking is not repeated; each update only adds the per-update 2s:
        // 3.625 - 1 + 2.125 and anger 0.18125 - 1 floored, then 4.75 / 20.
        model.updateNativeRelations(france);
        assertRelations(model, FRANCE, 4.75, 4.75 / 20);
    }

    @Test
    void theSameSquareUsedTwiceWaysIsCountedOnce() {
        final Model model = besideMontreal(NativeNation.AZTEC, 2)
                .withoutRights().toModel();
        final Player france = model.getPlayerByName(FRANCE);
        // Montreal is both the colony square and a taken square: one tile.
        model.recordNativeLandUse(france, MONTREAL);
        model.updateNativeRelations(france);
        // 2.5 - 1 + (0.25 + 2) / 2, not a second +2.
        assertRelations(model, FRANCE, 2.625, 2.625 / 20);
        model.updateNativeRelations(france);
        assertRelations(model, FRANCE, 2.75, 2.75 / 20);
    }

    @Test
    void payingForATakenSquareRemovesItsOngoingPressure() {
        final Model model = besideMontreal(NativeNation.AZTEC, 2).landAt(TAKEN)
                .withoutRights().paid(EuropeanNation.FRENCH, TAKEN).toModel();
        final Player france = model.getPlayerByName(FRANCE);
        // A paid right on the square means the use is never a taking.
        model.recordNativeLandUse(france, TAKEN);
        assertEquals(0, model.getNativeLocalAlarm(france, settlement(model)),
                EXACT);
        model.updateNativeRelations(france);
        // Only Montreal's unpaid square and the resident count.
        assertRelations(model, FRANCE, 1.125, 1.125 / 20);
    }

    /** Ends turns until the Dutch player is current again: one full round. */
    private static void endRound(final Model model, final Player dutch) {
        do {
            model.endTurn();
        } while (model.getCurrentPlayer() != dutch);
    }

    private static Model dutchSceneWithPaidAndTakenSquares(
            final boolean minuitCongress) {
        final NativeRelationsScene scene = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), UNARMED)
                .gold(DUTCH, 500);
        if (minuitCongress) {
            scene.dutchCongressReadyFor(Founder.PETER_MINUIT);
        }
        final Model model = Model.make(scene.toPo(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player dutch = model.getPlayerByName(DUTCH);
        // Pay for one square and take another unpaid.
        model.buyNativeLand(
                model.quoteNativeLand(dutch, at(1, 0)).orElseThrow());
        model.recordNativeLandUse(dutch, at(0, 1));
        return model;
    }

    @Test
    void realMinuitArrivalWaivesLandPressureButKeepsPaymentsAndOtherPressure() {
        final Model baseline = dutchSceneWithPaidAndTakenSquares(false);
        final Player baselineDutch = baseline.getPlayerByName(DUTCH);
        assertEquals(5, baseline.getNativeLocalAlarm(baselineDutch,
                settlement(baseline)), EXACT);
        endRound(baseline, baselineDutch);
        // Without Minuit: 5 - 1 + 0.25 resident + 2 for the unpaid taken
        // square.
        assertEquals(6.25, baseline.getNativeLocalAlarm(baselineDutch,
                settlement(baseline)), EXACT);

        final Model model = dutchSceneWithPaidAndTakenSquares(true);
        final Player dutch = model.getPlayerByName(DUTCH);
        dutch.chooseFounder(dutch.getFounderOffers().stream()
                .filter(offer -> offer.getFounder() == Founder.PETER_MINUIT)
                .findFirst().orElseThrow());
        // Choosing is not arrival: the taken square is still unpaid.
        assertEquals(NativeLandStatus.UNPAID, model
                .quoteNativeLand(dutch, at(0, 1)).orElseThrow().getStatus());
        endRound(model, dutch);
        assertTrue(dutch.getCongressMembers().contains(Founder.PETER_MINUIT));
        // The arrival update keeps the earlier 5 (decayed once) and the
        // resident 0.25, and adds nothing for the now-waived taken square.
        assertEquals(4.25, model.getNativeLocalAlarm(dutch, settlement(model)),
                EXACT);
        assertEquals(4.25 / 20,
                model.getNativeTribalAnger(dutch, NativeNation.AZTEC), EXACT);
        // No refund: the paid square keeps its status, gold and treasury; the
        // taken square is waived from now on.
        assertEquals(300, dutch.getGold());
        assertEquals(2200, settlement(model).getTreasury());
        assertEquals(NativeLandStatus.PAID, model
                .quoteNativeLand(dutch, at(1, 0)).orElseThrow().getStatus());
        assertEquals(NativeLandStatus.WAIVED, model
                .quoteNativeLand(dutch, at(0, 1)).orElseThrow().getStatus());

        // Loading replays neither the arrival nor any land effect.
        final Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player loadedDutch = loaded.getPlayerByName(DUTCH);
        assertEquals(4.25,
                loaded.getNativeLocalAlarm(loadedDutch, settlement(loaded)),
                EXACT);
        assertEquals(300, loadedDutch.getGold());
        assertEquals(1, loaded.save().getNativeLandRights().size());
        assertEquals(1, loaded.save().getNativeLandTakings().size());
    }

    @Test
    void takingSurvivesSaveAndLoadWithoutAnotherCharge() {
        final Model model = besideMontreal(NativeNation.AZTEC, 2)
                .withoutRights().toModel();
        model.recordNativeLandUse(model.getPlayerByName(FRANCE), MONTREAL);
        final Model loaded = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final Player france = loaded.getPlayerByName(FRANCE);
        loaded.recordNativeLandUse(france, MONTREAL);
        assertEquals(2.5,
                loaded.getNativeLocalAlarm(france, settlement(loaded)), EXACT);
    }

}
