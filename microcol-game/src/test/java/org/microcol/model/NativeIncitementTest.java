package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.microcol.model.NativeRelationsScene.DUTCH;
import static org.microcol.model.NativeRelationsScene.FRANCE;
import static org.microcol.model.NativeRelationsScene.at;
import static org.microcol.model.NativeRelationsScene.island;
import static org.microcol.model.NativeRelationsScene.settlement;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeTribalAngerPo;
import org.microcol.model.store.PlaceNativeMissionPo;
import org.microcol.model.store.UnitPo;

/**
 * The paid incitement (R-0166): the exact price and chance, the target list,
 * the single saved draw, the payment on success and failure, the order that a
 * success installs and a failure keeps, the refusals that change nothing, Save
 * and Continue without a replay, and the twenty-turn expiry. Anger is read for
 * both the requesting nation and the selected target, so the isolation of the
 * raise is checked as well.
 */
class NativeIncitementTest {

    /** Dutch missionary beside the settlement: the first unit of the scene. */
    private static final int VISITOR_ID = 200;

    /** Native turns of an order after a success, as the rules state. */
    private static final int ORDER_TURNS = 20;

    private static final double EPSILON = 1e-9;

    /** Step of the saved random stream, as the model advances it. */
    private static final long DRAW_STEP = 0x9E3779B97F4A7C15L;

    /** Peaceful decay of one European update, as the relations apply it. */
    private static final double DECAY_TOLERANCE = 0.1;

    private static final Consumer<UnitPo> ORDINARY = unit -> unit
            .setMissionary(true);

    private static final Consumer<UnitPo> NOTHING = unit -> {
    };

    /** Jesuit: a missionary specialist whose role flag stays false. */
    private static final Consumer<UnitPo> JESUIT = unit -> {
        unit.setWorkUpdates(0);
        unit.setMissionary(false);
    };

    /**
     * Dutch ordinary missionary and a French colonist beside the settlement, so
     * France is a selectable power of this game. Dutch gold is 1000.
     */
    private static NativeRelationsScene scene() {
        return island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), ORDINARY)
                .unit(FRANCE, UnitType.COLONIST, at(-1, 0), NOTHING)
                .gold(DUTCH, 1000);
    }

    private static Model withSeed(final NativeRelationsScene scene,
            final long seed) {
        scene.toPo().setCombatRandomState(seed);
        return scene.toModel();
    }

    private static Unit visitor(final Model model) {
        return model.getUnitById(VISITOR_ID);
    }

    private static NativeIncitementQuote quote(final Model model) {
        return model.getNativeIncitementQuote(visitor(model), settlement(model),
                EuropeanNation.FRENCH).orElseThrow();
    }

    private static int gold(final Model model) {
        return visitor(model).getOwner().getGold();
    }

    /**
     * Tribal anger of the Aztecs toward one European, read by the name of that
     * European's player, so the requester and the target are told apart.
     */
    private static double anger(final Model model, final String owner) {
        return model.getNativeTribalAnger(model.getPlayerByName(owner),
                NativeNation.AZTEC);
    }

    private static long randomState(final Model model) {
        return model.save().getCombatRandomState();
    }

    private static Model reload(final ModelPo po) {
        return Model.make(po, MissionImpl.getPredefinedGameOverEvaluators());
    }

    /**
     * Smallest saved state whose first draw gives the outcome at this chance.
     * Nothing draws before the confirmed attempt, so that draw is the first.
     */
    private static long seedFor(final NativeIncitementOutcome outcome,
            final double chance) {
        for (long seed = 1;; seed++) {
            if (NativeIncitementRules.outcome(chance,
                    Model.combatRandomAfter(seed)) == outcome) {
                return seed;
            }
        }
    }

    @Test
    void priceIsTheCeilingOfTheExactValue() {
        assertEquals(500, NativeIncitementRules.price(0, 0));
        assertEquals(1000, NativeIncitementRules.price(100, 0));
        // Ten missions halve the price: 500 / 2 = 250.
        assertEquals(250, NativeIncitementRules.price(0, 10));
        // One mission: 500 / 1.1 = 454.5..., rounded up once to 455.
        assertEquals(455, NativeIncitementRules.price(0, 1));
        // 500 * 1.5 / 1.1 = 681.8..., so 682, never 681.
        assertEquals(682, NativeIncitementRules.price(50, 1));
    }

    @Test
    void chanceIsAQuarterForOrdinaryAndAHalfForExperts() {
        assertEquals(0.25, NativeIncitementRules.chance(false), EPSILON);
        assertEquals(0.5, NativeIncitementRules.chance(true), EPSILON);
    }

    @Test
    void rollBelowTheChanceSucceeds() {
        assertEquals(NativeIncitementOutcome.SUCCESS,
                NativeIncitementRules.outcome(0.25, 0.2499));
        assertEquals(NativeIncitementOutcome.FAILURE,
                NativeIncitementRules.outcome(0.25, 0.25));
    }

    @Test
    void quoteReadsTheLiveTermsAndPrice() {
        final Model model = scene().toModel();
        final NativeIncitementQuote terms = quote(model);
        assertEquals(VISITOR_ID, terms.getVisitorId());
        assertEquals(settlement(model).getId(), terms.getSettlementId());
        assertEquals(NativeNation.AZTEC, terms.getNation());
        assertEquals(EuropeanNation.FRENCH, terms.getTarget());
        assertEquals(500, terms.getPrice());
        assertEquals(1000, terms.getGold());
        assertEquals(0, terms.getMissions());
        assertEquals(0.25, terms.getChance(), EPSILON);
        assertFalse(terms.isVisitorExpert());
    }

    /** The visitor's own nation is never a target; only France is offered. */
    @Test
    void targetsExcludeTheVisitorsOwnNation() {
        final Model model = scene().toModel();
        assertEquals(List.of(EuropeanNation.FRENCH), model
                .getNativeIncitementTargets(visitor(model), settlement(model)));
    }

    /**
     * A success charges the price once and raises only the target's anger to
     * 75; the requester's own anger toward the tribe stays at zero.
     */
    @Test
    void successPaysOnceAndInstallsTheTwentyTurnOrder() {
        final Model model = withSeed(scene(),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        final NativeIncitementOutcome outcome = model.incite(visitor(model),
                settlement(model), quote(model));
        assertEquals(NativeIncitementOutcome.SUCCESS, outcome);
        assertEquals(500, gold(model));
        assertEquals(75, anger(model, FRANCE), EPSILON);
        assertEquals(0, anger(model, DUTCH), EPSILON);
        final NativeIncitementOrder order = model
                .getNativeIncitementOrder(NativeNation.AZTEC).orElseThrow();
        assertEquals(EuropeanNation.FRENCH, order.getTarget());
        assertEquals(ORDER_TURNS, order.getRemainingTurns());
    }

    @Test
    void failurePaysButInstallsNoOrderAndKeepsAnger() {
        final Model model = withSeed(scene(),
                seedFor(NativeIncitementOutcome.FAILURE, 0.25));
        final NativeIncitementOutcome outcome = model.incite(visitor(model),
                settlement(model), quote(model));
        assertEquals(NativeIncitementOutcome.FAILURE, outcome);
        assertEquals(500, gold(model));
        assertEquals(0, anger(model, FRANCE), EPSILON);
        assertEquals(0, anger(model, DUTCH), EPSILON);
        assertTrue(
                model.getNativeIncitementOrder(NativeNation.AZTEC).isEmpty());
    }

    /**
     * A failed attempt keeps the order of an earlier success, with its
     * remaining turns: five native turns pass, then the failure leaves them at
     * fifteen and the target's anger at 75.
     */
    @Test
    void failureKeepsThePreviousOrderAndItsRemainingTurns() {
        final Model first = withSeed(scene(),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        first.incite(visitor(first), settlement(first), quote(first));
        final ModelPo po = first.save();
        po.setCombatRandomState(seedFor(NativeIncitementOutcome.FAILURE, 0.25));
        final Model model = reload(po);
        for (int turn = 0; turn < 5; turn++) {
            model.advanceNativeIncitement(NativeNation.AZTEC);
        }
        assertEquals(NativeIncitementOutcome.FAILURE,
                model.incite(visitor(model), settlement(model), quote(model)));
        assertEquals(0, gold(model));
        assertEquals(75, anger(model, FRANCE), EPSILON);
        assertEquals(0, anger(model, DUTCH), EPSILON);
        assertEquals(15, model.getNativeIncitementOrder(NativeNation.AZTEC)
                .orElseThrow().getRemainingTurns());
    }

    /**
     * A quote from before the success is stale: the purse and the order
     * revision changed. The refusal changes neither gold, order nor random
     * state.
     */
    @Test
    void staleQuoteIsRefusedWithoutChange() {
        final Model model = withSeed(scene(),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        final NativeIncitementQuote shown = quote(model);
        model.incite(visitor(model), settlement(model), shown);
        final long state = randomState(model);
        assertThrows(IllegalStateException.class,
                () -> model.incite(visitor(model), settlement(model), shown));
        assertEquals(500, gold(model));
        assertEquals(state, randomState(model));
    }

    /** Insufficient gold is refused before the draw; nothing changes. */
    @Test
    void insufficientGoldChangesNothing() {
        final Model model = scene().gold(DUTCH, 499).toModel();
        final NativeIncitementQuote shown = quote(model);
        assertEquals(500, shown.getPrice());
        final long state = randomState(model);
        assertThrows(IllegalStateException.class,
                () -> model.incite(visitor(model), settlement(model), shown));
        assertEquals(499, gold(model));
        assertEquals(0, anger(model, FRANCE), EPSILON);
        assertEquals(state, randomState(model));
        assertTrue(
                model.getNativeIncitementOrder(NativeNation.AZTEC).isEmpty());
    }

    /**
     * Save and Continue of a success keeps the order, its exact remaining
     * turns, the gold and the random state: loading draws nothing and ticks
     * nothing.
     */
    @Test
    void continueKeepsTheOrderWithoutAReplayOrTick() {
        final Model model = withSeed(scene(),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        model.incite(visitor(model), settlement(model), quote(model));
        model.advanceNativeIncitement(NativeNation.AZTEC);
        final ModelPo po = model.save();
        final Model loaded = reload(po);
        assertEquals(500, gold(loaded));
        assertEquals(ORDER_TURNS - 1,
                loaded.getNativeIncitementOrder(NativeNation.AZTEC)
                        .orElseThrow().getRemainingTurns());
        assertEquals(po.getCombatRandomState(), randomState(loaded));
    }

    /**
     * The target starts at anger 100, so no relation change can end the order
     * and only the clock is measured. Every one of the twenty native turns
     * finds the order, the nineteenth included, and the twentieth is its last:
     * after it the order is gone.
     */
    @Test
    void orderActsOnTwentyNativeTurnsThenExpires() {
        final Model model = withSeed(
                scene().tribal(NativeNation.AZTEC, EuropeanNation.FRENCH, 100),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        model.incite(visitor(model), settlement(model), quote(model));
        for (int turn = 1; turn <= ORDER_TURNS; turn++) {
            model.checkNativeIncitement(NativeNation.AZTEC);
            assertTrue(
                    model.getNativeIncitementOrder(NativeNation.AZTEC)
                            .isPresent(),
                    "no order during native turn " + turn);
            model.advanceNativeIncitement(NativeNation.AZTEC);
        }
        model.checkNativeIncitement(NativeNation.AZTEC);
        assertTrue(
                model.getNativeIncitementOrder(NativeNation.AZTEC).isEmpty());
    }

    /**
     * A replacement success resets the clock to twenty turns, even when the old
     * order had only a few left.
     */
    @Test
    void replacementSuccessResetsTheClock() {
        final Model model = withSeed(
                scene().tribal(NativeNation.AZTEC, EuropeanNation.FRENCH, 100),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        model.incite(visitor(model), settlement(model), quote(model));
        for (int turn = 0; turn < ORDER_TURNS - 3; turn++) {
            model.advanceNativeIncitement(NativeNation.AZTEC);
        }
        final ModelPo po = model.save();
        po.setCombatRandomState(seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        final Model replaced = reload(po);
        replaced.incite(visitor(replaced), settlement(replaced),
                quote(replaced));
        assertEquals(ORDER_TURNS,
                replaced.getNativeIncitementOrder(NativeNation.AZTEC)
                        .orElseThrow().getRemainingTurns());
    }

    /**
     * Peaceful decay is not exempt: one European update takes the target's
     * anger from 75 to just under it, and the next native turn ends the order.
     */
    @Test
    void peacefulDecayBelowSeventyFiveEndsTheOrderAtTheNextNativeTurn() {
        final Model model = withSeed(scene(),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        model.incite(visitor(model), settlement(model), quote(model));
        assertEquals(75, anger(model, FRANCE), EPSILON);
        model.updateNativeRelations(model.getPlayerByName(FRANCE));
        assertEquals(74, anger(model, FRANCE), DECAY_TOLERANCE);
        assertTrue(anger(model, FRANCE) < 75);
        model.checkNativeIncitement(NativeNation.AZTEC);
        assertTrue(
                model.getNativeIncitementOrder(NativeNation.AZTEC).isEmpty());
    }

    /**
     * Anger below 75 at the native turn start drops the order, because the
     * relation is no longer hostile enough to pursue the target.
     */
    /**
     * A second Dutch missionary stationed at the settlement is one own mission
     * in the tribe, read live: the price falls from 500 to ceil(500 / 1.1).
     */
    @Test
    void ownMissionInTheTribeLowersThePriceWithOneCeiling() {
        final NativeRelationsScene scene = scene().unit(DUTCH,
                UnitType.COLONIST, at(0, 1), ORDINARY);
        final ModelPo po = scene.toPo();
        final UnitPo holder = po.getUnits().stream()
                .filter(unit -> unit.getId() == 202).findFirst().orElseThrow();
        holder.setPlaceMap(null);
        final PlaceNativeMissionPo place = new PlaceNativeMissionPo();
        place.setSettlementId(0);
        holder.setPlaceNativeMission(place);
        po.getNativeSettlements().get(0).setMissionaryId(202);
        po.getNativeSettlements().get(0).setMissionUpdates(0);
        final NativeIncitementQuote terms = quote(scene.toModel());
        assertEquals(1, terms.getMissions());
        assertEquals(455, terms.getPrice());
    }

    /**
     * A confirmed attempt draws the saved randomness exactly once and pays the
     * price exactly once, whichever way the draw falls.
     */
    @Test
    void aConfirmedAttemptDrawsOnceAndPaysOnce() {
        for (final NativeIncitementOutcome wanted : NativeIncitementOutcome
                .values()) {
            final long seed = seedFor(wanted, 0.25);
            final Model model = withSeed(scene(), seed);
            assertEquals(wanted, model.incite(visitor(model), settlement(model),
                    quote(model)));
            assertEquals(seed + DRAW_STEP, randomState(model));
            assertEquals(500, gold(model));
        }
    }

    /**
     * Save and Continue of a confirmed attempt cannot replay it: the quote that
     * was confirmed no longer matches the purse and the order, so it is refused
     * without a payment or a draw.
     */
    @Test
    void aConfirmedQuoteCannotBeReplayedAfterContinue() {
        final Model model = withSeed(scene(),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        final NativeIncitementQuote confirmed = quote(model);
        model.incite(visitor(model), settlement(model), confirmed);
        final ModelPo po = model.save();
        final Model loaded = reload(po);
        assertThrows(IllegalStateException.class, () -> loaded
                .incite(visitor(loaded), settlement(loaded), confirmed));
        assertEquals(500, gold(loaded));
        assertEquals(po.getCombatRandomState(), randomState(loaded));
    }

    /**
     * Only a missionary may incite, and only against another power that exists
     * in this game: a colonist who is no missionary, the requester's own nation
     * and a power with no player all give no quote, so nothing can be
     * confirmed.
     */
    @Test
    void ineligibleRequestsGiveNoQuoteAndChangeNothing() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.COLONIST, at(1, 0), ORDINARY)
                .unit(DUTCH, UnitType.COLONIST, at(0, 1), NOTHING)
                .unit(FRANCE, UnitType.COLONIST, at(-1, 0), NOTHING)
                .gold(DUTCH, 1000).toModel();
        final long state = randomState(model);
        final Unit missionary = model.getUnitById(VISITOR_ID);
        final Unit plain = model.getUnitById(VISITOR_ID + 1);
        assertTrue(model.getNativeIncitementQuote(plain, settlement(model),
                EuropeanNation.FRENCH).isEmpty());
        assertTrue(model.getNativeIncitementTargets(plain, settlement(model))
                .isEmpty());
        assertTrue(model.getNativeIncitementQuote(missionary, settlement(model),
                EuropeanNation.DUTCH).isEmpty());
        assertTrue(model.getNativeIncitementQuote(missionary, settlement(model),
                EuropeanNation.ENGLISH).isEmpty());
        assertEquals(1000, gold(model));
        assertEquals(state, randomState(model));
        assertTrue(
                model.getNativeIncitementOrder(NativeNation.AZTEC).isEmpty());
    }

    /**
     * A Jesuit is an effective expert by its own role, so the chance is one
     * half; the missionary role flag stays false, and the expertise counts
     * once.
     */
    @Test
    void jesuitIncitesWithTheExpertHalfChance() {
        final Model model = island(NativeNation.AZTEC)
                .unit(DUTCH, UnitType.JESUIT_MISSIONARY, at(1, 0), JESUIT)
                .unit(FRANCE, UnitType.COLONIST, at(-1, 0), NOTHING)
                .gold(DUTCH, 1000).toModel();
        final NativeIncitementQuote terms = quote(model);
        assertTrue(terms.isVisitorExpert());
        assertEquals(0.5, terms.getChance(), EPSILON);
    }

    /**
     * Jean de Brebeuf makes an ordinary missionary an effective expert, so the
     * chance is one half for it too, without a second multiplier.
     */
    @Test
    void brebeufMembershipGivesAnOrdinaryMissionaryTheExpertHalfChance() {
        final Model model = scene().brebeufMember(DUTCH).toModel();
        final NativeIncitementQuote terms = quote(model);
        assertTrue(terms.isVisitorExpert());
        assertEquals(0.5, terms.getChance(), EPSILON);
        assertEquals(500, terms.getPrice());
    }

    @Test
    void orderEndsWhenTheTribalAngerFallsBelowSeventyFive() {
        final Model model = withSeed(scene(),
                seedFor(NativeIncitementOutcome.SUCCESS, 0.25));
        model.incite(visitor(model), settlement(model), quote(model));
        final ModelPo po = model.save();
        final NativeTribalAngerPo french = po.getNativeTribalAnger().stream()
                .filter(entry -> entry.getNation() == NativeNation.AZTEC
                        && entry.getEuropean() == EuropeanNation.FRENCH)
                .findFirst().orElseThrow();
        french.setValue(74.0);
        final Model lowered = reload(po);
        lowered.checkNativeIncitement(NativeNation.AZTEC);
        assertTrue(
                lowered.getNativeIncitementOrder(NativeNation.AZTEC).isEmpty());
    }

}
