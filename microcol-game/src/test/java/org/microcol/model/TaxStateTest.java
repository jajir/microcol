package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.TaxDemandPo;
import org.microcol.model.store.TaxPo;
import org.microcol.model.store.TaxProtestPo;

/** Cadence, cap and strict persistence of the colonial tax state. */
class TaxStateTest {

    private static TaxPo pendingState() {
        final TaxPo po = TaxState.initial();
        po.setRate(4);
        po.setActivated(true);
        // The counter restarted when this demand fell due.
        po.setUpdates(0);
        po.setIssued(2);
        final TaxProtestPo protest = new TaxProtestPo();
        protest.setGoods(GoodsType.RUM);
        protest.setColonyId(1);
        protest.setQuantity(60);
        protest.setBid(11);
        final TaxDemandPo demand = new TaxDemandPo();
        demand.setProposedRate(6);
        demand.setProtest(protest);
        po.setPending(demand);
        final Map<String, Integer> arrears = new LinkedHashMap<>();
        arrears.put("TOBACCO", 150);
        po.setArrears(arrears);
        return po;
    }

    @Test
    void intervalFollowsDifficultyWithFloorOfFive() {
        assertEquals(20, TaxState.interval(Difficulty.DISCOVERER));
        assertEquals(17, TaxState.interval(Difficulty.EXPLORER));
        assertEquals(14, TaxState.interval(Difficulty.CONQUISTADOR));
        assertEquals(11, TaxState.interval(Difficulty.GOVERNOR));
        assertEquals(8, TaxState.interval(Difficulty.VICEROY));
    }

    @Test
    void noDemandBeforeAFirstColonyThenOneEveryInterval() {
        for (final Difficulty difficulty : Difficulty.values()) {
            final int interval = TaxState.interval(difficulty);
            final TaxState state = new TaxState(TaxState.initial(), difficulty);
            for (int update = 0; update < 3 * interval; update++) {
                assertFalse(state.advance(false, difficulty));
            }
            assertFalse(state.save().getActivated());
            assertEquals(0, state.save().getUpdates());
            for (int round = 0; round < 3; round++) {
                for (int update = 1; update < interval; update++) {
                    // The first colony may be gone later; counting continues.
                    assertFalse(state.advance(round == 0, difficulty),
                            difficulty + " update " + update);
                }
                assertTrue(state.advance(round == 0, difficulty),
                        difficulty + " round " + round);
                assertEquals(0, state.save().getUpdates());
            }
            assertTrue(state.save().getActivated());
        }
    }

    @Test
    void proposalIsTwoPointsCappedAtSeventyFive() {
        final TaxState state = new TaxState(TaxState.initial(),
                Difficulty.CONQUISTADOR);
        assertEquals(2, state.nextRate());
        state.setRate(73);
        assertEquals(75, state.nextRate());
        state.setRate(75);
        assertEquals(75, state.nextRate());
        assertThrows(IllegalStateException.class, state::issue);
        assertThrows(IllegalArgumentException.class, () -> state.setRate(76));
    }

    @Test
    void pendingDemandAndArrearsRoundTrip() {
        final TaxPo saved = new TaxState(pendingState(),
                Difficulty.CONQUISTADOR).save();
        assertEquals(4, saved.getRate());
        assertEquals(0, saved.getUpdates());
        assertEquals(2, saved.getIssued());
        assertEquals(6, saved.getPending().getProposedRate());
        assertEquals(GoodsType.RUM, saved.getPending().getProtest().getGoods());
        assertEquals(1, saved.getPending().getProtest().getColonyId());
        assertEquals(60, saved.getPending().getProtest().getQuantity());
        assertEquals(11, saved.getPending().getProtest().getBid());
        assertEquals(Map.of("TOBACCO", 150), saved.getArrears());
    }

    @Test
    void demandWithoutProtestRoundTrips() {
        final TaxPo po = pendingState();
        po.getPending().setProtest(null);
        final TaxState state = new TaxState(po, Difficulty.CONQUISTADOR);
        assertTrue(state.isPending());
        assertFalse(state.getPending().hasProtest());
        assertEquals(null, state.save().getPending().getProtest());
    }

    @Test
    void malformedStateIsRejectedInsteadOfDefaulted() {
        final Difficulty difficulty = Difficulty.CONQUISTADOR;
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(null, difficulty));
        final TaxPo noRate = TaxState.initial();
        noRate.setRate(null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(noRate, difficulty));
        final TaxPo noActivation = TaxState.initial();
        noActivation.setActivated(null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(noActivation, difficulty));
        final TaxPo noCounter = TaxState.initial();
        noCounter.setUpdates(null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(noCounter, difficulty));
        final TaxPo noIssued = TaxState.initial();
        noIssued.setIssued(null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(noIssued, difficulty));
        final TaxPo noArrears = TaxState.initial();
        noArrears.setArrears(null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(noArrears, difficulty));
        final TaxPo high = TaxState.initial();
        high.setRate(76);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(high, difficulty));
        final TaxPo negative = TaxState.initial();
        negative.setRate(-1);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(negative, difficulty));
        final TaxPo overdue = TaxState.initial();
        overdue.setActivated(true);
        overdue.setUpdates(TaxState.interval(difficulty));
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(overdue, difficulty));
        final TaxPo counterWithoutColony = TaxState.initial();
        counterWithoutColony.setUpdates(1);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(counterWithoutColony, difficulty));
    }

    @Test
    void contradictoryPendingDemandIsRejected() {
        final Difficulty difficulty = Difficulty.CONQUISTADOR;
        final TaxPo wrongProposal = pendingState();
        wrongProposal.getPending().setProposedRate(5);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(wrongProposal, difficulty));
        final TaxPo neverIssued = pendingState();
        neverIssued.setIssued(0);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(neverIssued, difficulty));
        final TaxPo atCap = pendingState();
        atCap.setRate(75);
        atCap.getPending().setProposedRate(75);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(atCap, difficulty));
        final TaxPo midInterval = pendingState();
        midInterval.setUpdates(3);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(midInterval, difficulty));
        final TaxPo alreadyBoycotted = pendingState();
        alreadyBoycotted.getArrears().put("RUM", 200);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(alreadyBoycotted, difficulty));
        final TaxPo inactive = pendingState();
        inactive.setActivated(false);
        inactive.setUpdates(0);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(inactive, difficulty));
        final TaxPo zero = pendingState();
        zero.getPending().getProtest().setQuantity(0);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(zero, difficulty));
        final TaxPo huge = pendingState();
        huge.getPending().getProtest().setQuantity(101);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(huge, difficulty));
        final TaxPo untradable = pendingState();
        untradable.getPending().getProtest().setGoods(GoodsType.HAMMERS);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(untradable, difficulty));
        final TaxPo noColony = pendingState();
        noColony.getPending().getProtest().setColonyId(null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(noColony, difficulty));
        final TaxPo noBid = pendingState();
        noBid.getPending().getProtest().setBid(null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(noBid, difficulty));
        final TaxPo overflow = pendingState();
        overflow.getPending().getProtest().setBid(Integer.MAX_VALUE);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(overflow, difficulty));
    }

    @Test
    void arrearsMustNameATradableCommodityAndOweAtLeastOneHundred() {
        final Difficulty difficulty = Difficulty.CONQUISTADOR;
        final TaxPo small = TaxState.initial();
        small.getArrears().put("RUM", 99);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(small, difficulty));
        final TaxPo unknown = TaxState.initial();
        unknown.getArrears().put("GOLD", 100);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(unknown, difficulty));
        final TaxPo untradable = TaxState.initial();
        untradable.getArrears().put("HAMMERS", 100);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(untradable, difficulty));
        final TaxPo nullOwed = TaxState.initial();
        nullOwed.getArrears().put("RUM", null);
        assertThrows(IllegalArgumentException.class,
                () -> new TaxState(nullOwed, difficulty));
        final TaxPo valid = TaxState.initial();
        valid.getArrears().put("RUM", 100);
        assertTrue(new TaxState(valid, difficulty).isBoycotted(GoodsType.RUM));
    }

    @Test
    void protestArrearsAreAtLeastOneHundredAndExactOtherwise() {
        assertEquals(100, TaxProtest.arrears(1, 11));
        assertEquals(100, TaxProtest.arrears(1, 50));
        assertEquals(1320, TaxProtest.arrears(60, 11));
        assertEquals(1800, TaxProtest.arrears(100, 9));
        assertThrows(ArithmeticException.class,
                () -> TaxProtest.arrears(100, Integer.MAX_VALUE));
    }
}
