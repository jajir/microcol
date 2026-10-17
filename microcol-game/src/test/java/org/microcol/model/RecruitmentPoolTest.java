package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.RecruitmentCandidatePo;
import org.microcol.model.store.RecruitmentPo;

class RecruitmentPoolTest {

    private RecruitmentPo state(final UnitType first) {
        final RecruitmentPo po = new RecruitmentPo();
        po.setCandidates(List.of(candidate(1, first),
                candidate(2, UnitType.INDENTURED_SERVANT),
                candidate(3, UnitType.PETTY_CRIMINAL)));
        po.setNextCandidateId(4);
        po.setPaidPassages(0);
        po.setCrosses(0);
        po.setEarnedImmigrants(0);
        po.setPendingEarnedChoice(false);
        return po;
    }

    private RecruitmentCandidatePo candidate(final int id,
            final UnitType type) {
        final RecruitmentCandidatePo po = new RecruitmentCandidatePo();
        po.setId(id);
        po.setType(type);
        return po;
    }

    @Test
    void weightedBoundaryAndUniformExpertIndexAreExact() {
        assertSame(UnitType.COLONIST, RecruitmentPool.draw(0, 0));
        assertSame(UnitType.COLONIST, RecruitmentPool.draw(59, 0));
        assertSame(UnitType.INDENTURED_SERVANT, RecruitmentPool.draw(60, 0));
        assertSame(UnitType.INDENTURED_SERVANT, RecruitmentPool.draw(79, 0));
        assertSame(UnitType.PETTY_CRIMINAL, RecruitmentPool.draw(80, 0));
        assertSame(UnitType.PETTY_CRIMINAL, RecruitmentPool.draw(89, 0));
        final Set<UnitType> experts = new HashSet<>();
        for (int i = 0; i < 19; i++) {
            final UnitType type = RecruitmentPool.draw(90, i);
            assertSame(UnitType.EUROPEAN_SPECIALISTS.get(i), type);
            experts.add(type);
        }
        assertEquals(19, experts.size());
        assertThrows(IllegalArgumentException.class,
                () -> RecruitmentPool.draw(100, 0));
    }

    @Test
    void brewsterDrawExcludesBothLaborerClassesAtEveryBoundary() {
        assertSame(UnitType.COLONIST, RecruitmentPool.draw(0, 0, true));
        assertSame(UnitType.COLONIST, RecruitmentPool.draw(89, 0, true));
        final Set<UnitType> experts = new HashSet<>();
        for (int i = 0; i < 19; i++) {
            final UnitType type = RecruitmentPool.draw(90, i, true);
            assertSame(UnitType.EUROPEAN_SPECIALISTS.get(i), type);
            experts.add(type);
        }
        assertEquals(19, experts.size());
    }

    @Test
    void brewsterJoinReplacesOnlyExcludedCandidateSlotsWithNewTokens() {
        final RecruitmentPool pool = new RecruitmentPool(
                state(UnitType.COLONIST));
        pool.applyBrewster();
        final RecruitmentPo saved = pool.save();
        assertEquals(1, saved.getCandidates().get(0).getId());
        assertEquals(4, saved.getCandidates().get(1).getId());
        assertEquals(5, saved.getCandidates().get(2).getId());
        assertEquals(6, saved.getNextCandidateId());
        pool.validateBrewsterPool(true);
        assertEquals(0, saved.getPaidPassages());
        assertEquals(0, saved.getCrosses());
        assertEquals(0, saved.getEarnedImmigrants());
    }

    @Test
    void pricesUseEveryDifficultyAndEnglishTargetRounding() {
        final int[] factors = { 75, 90, 100, 115, 130 };
        final int[] bases = { 150, 180, 200, 230, 260 };
        final int[] targets = { 75, 90, 100, 115, 130 };
        final RecruitmentPool pool = new RecruitmentPool(
                state(UnitType.COLONIST));
        for (int i = 0; i < Difficulty.values().length; i++) {
            final Difficulty difficulty = Difficulty.values()[i];
            assertEquals(bases[i], pool.price(difficulty, "Dutch"));
            assertEquals(targets[i],
                    RecruitmentPool.target(difficulty, "Dutch", 0));
            assertEquals((2 * targets[i] + 2) / 3,
                    RecruitmentPool.target(difficulty, "England", 0));
            assertEquals(factors[i], targets[i]);
        }
        final RecruitmentPo progressed = state(UnitType.COLONIST);
        progressed.setPaidPassages(1);
        progressed.setCrosses(25);
        progressed.setEarnedImmigrants(1);
        final RecruitmentPool later = new RecruitmentPool(progressed);
        assertEquals(125,
                RecruitmentPool.target(Difficulty.CONQUISTADOR, "Dutch", 1));
        assertEquals(84,
                RecruitmentPool.target(Difficulty.CONQUISTADOR, "England", 1));
        assertEquals(176, later.price(Difficulty.CONQUISTADOR, "Dutch"));
        assertEquals(155, later.price(Difficulty.CONQUISTADOR, "England"));
        progressed.setCrosses(124);
        assertEquals(50, new RecruitmentPool(progressed)
                .price(Difficulty.CONQUISTADOR, "Dutch"));
    }

    @Test
    void malformedPoolsRejectMissingCountersAndCandidateIdentity() {
        final RecruitmentPo po = state(UnitType.COLONIST);
        po.setNextCandidateId(null);
        assertThrows(IllegalArgumentException.class,
                () -> new RecruitmentPool(po));
        po.setNextCandidateId(4);
        po.setCrosses(-1);
        assertThrows(IllegalArgumentException.class,
                () -> new RecruitmentPool(po));
        po.setCrosses(0);
        po.setCandidates(List.of(candidate(1, UnitType.COLONIST),
                candidate(1, UnitType.COLONIST),
                candidate(3, UnitType.PETTY_CRIMINAL)));
        assertThrows(IllegalArgumentException.class,
                () -> new RecruitmentPool(po));
        po.setCandidates(List.of(candidate(1, UnitType.COLONIST),
                candidate(2, UnitType.COLONIST),
                candidate(4, UnitType.COLONIST)));
        assertThrows(IllegalArgumentException.class,
                () -> new RecruitmentPool(po));
        po.setCandidates(List.of(candidate(1, UnitType.COLONIST),
                candidate(2, UnitType.COLONIST),
                candidate(3, UnitType.MERCHANTMAN)));
        assertThrows(IllegalArgumentException.class,
                () -> new RecruitmentPool(po));
        assertFalse(UnitType.INDENTURED_SERVANT.getEuropePrice() > 0);
        assertTrue(UnitType.EUROPEAN_SPECIALISTS
                .contains(RecruitmentPool.draw(99, 18)));
    }
}
