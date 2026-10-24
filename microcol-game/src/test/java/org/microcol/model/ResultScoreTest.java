package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Arithmetic of the calculated result (R-0196). The base is kept at or below
 * 100 points so that the support percentage alone sets it exactly.
 */
class ResultScoreTest {

    private static ResultScore score(final int base, final int destroyed,
            final Difficulty difficulty, final long bells, final Integer order,
            final Integer year) {
        return ResultScore.of(BaseScore.of(0, 0, 0, 0, 0L, base), destroyed,
                difficulty, bells, order, year);
    }

    @Test
    void allFiveDifficultiesMultiplyTheBaseWithoutAVictory() {
        assertEquals(50,
                score(100, 0, Difficulty.DISCOVERER, 0, null, null).getTotal());
        assertEquals(75,
                score(100, 0, Difficulty.EXPLORER, 0, null, null).getTotal());
        assertEquals(100, score(100, 0, Difficulty.CONQUISTADOR, 0, null, null)
                .getTotal());
        assertEquals(125,
                score(100, 0, Difficulty.GOVERNOR, 0, null, null).getTotal());
        assertEquals(150,
                score(100, 0, Difficulty.VICEROY, 0, null, null).getTotal());
    }

    @ParameterizedTest
    @CsvSource({ "DISCOVERER,1,48,53", "EXPLORER,2,46,76",
            "CONQUISTADOR,3,44,98", "GOVERNOR,4,42,117", "VICEROY,5,40,135" })
    void eachDifficultyDeductsItsSettlementPointsAndFloorsItsFactorOnce(
            final Difficulty difficulty, final long pointsPerSettlement,
            final long clampedBase, final long total) {
        // Base 40 with 10 bells, two destroyed settlements, the first order
        // and the declaration in 1770: 40 + 10 - 2 x points, then the bonus
        // and early points, then the factor, floored once.
        final ResultScore result = score(40, 2, difficulty, 10, 1, 1770);
        assertEquals(pointsPerSettlement, result.getPointsPerSettlement());
        assertEquals(clampedBase, result.getClampedBase());
        assertEquals(total, result.getTotal());
    }

    @Test
    void eachOrderGetsItsBonusAndLaterOrdersGetNone() {
        assertEquals(200,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 1, 1780).getTotal());
        assertEquals(150,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 2, 1780).getTotal());
        assertEquals(125,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 3, 1780).getTotal());
        assertEquals(100,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 4, 1780).getTotal());
        assertEquals(100,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 5, 1780).getTotal());
    }

    @Test
    void secondIndependenceDeclaredIn1770AtConquistadorGives160() {
        final ResultScore result = score(100, 0, Difficulty.CONQUISTADOR, 0, 2,
                1770);
        assertEquals(10, result.getEarlyPoints());
        assertEquals(50, result.getOrderBonusPercent());
        assertEquals(160, result.getTotal());
    }

    @Test
    void earlyPointsStopAtTheBoundaryYear() {
        assertEquals(201,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 1, 1779).getTotal());
        assertEquals(200,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 1, 1780).getTotal());
        assertEquals(200,
                score(100, 0, Difficulty.CONQUISTADOR, 0, 1, 1781).getTotal());
    }

    @Test
    void earlyPointsAreZeroWithoutASuccessfulIndependence() {
        assertEquals(100, score(100, 0, Difficulty.CONQUISTADOR, 0, null, 1700)
                .getTotal());
        assertEquals(0, score(100, 0, Difficulty.CONQUISTADOR, 0, null, 1700)
                .getEarlyPoints());
    }

    @Test
    void floorHappensOnceAfterTheDifficultyFactor() {
        // 3 x 1.5 = 4.5 and then 4.5 x 0.5 = 2.25, which floors to 2.
        assertEquals(2,
                score(3, 0, Difficulty.DISCOVERER, 0, 2, 1780).getTotal());
        // 2 x 0.75 = 1.5, which floors to 1.
        assertEquals(1,
                score(2, 0, Difficulty.EXPLORER, 0, null, null).getTotal());
    }

    @Test
    void destroyedSettlementsDeductDifficultyPlusOneEach() {
        assertEquals(1, score(10, 1, Difficulty.DISCOVERER, 0, null, null)
                .getPointsPerSettlement());
        assertEquals(5, score(10, 1, Difficulty.VICEROY, 0, null, null)
                .getPointsPerSettlement());
        // 30 - 2 x 5 = 20, and Viceroy multiplies that by 1.5.
        final ResultScore result = score(30, 2, Difficulty.VICEROY, 0, null,
                null);
        assertEquals(10, result.getDestructionDeduction());
        assertEquals(20, result.getClampedBase());
        assertEquals(30, result.getTotal());
    }

    @Test
    void negativeBaseIsClampedBeforeTheBonus() {
        // 10 - 5 x 5 = -15, clamped to 0; the early points still count once.
        final ResultScore result = score(10, 5, Difficulty.VICEROY, 0, 1, 1700);
        assertEquals(0, result.getClampedBase());
        assertEquals(120, result.getTotal());
    }

    @Test
    void postTargetBellsAddToTheBaseAndAreNotCountedTwice() {
        final ResultScore result = score(10, 0, Difficulty.CONQUISTADOR, 25,
                null, null);
        assertEquals(25, result.getBellsAfterTarget());
        assertEquals(35, result.getClampedBase());
        assertEquals(35, result.getTotal());
    }

    @Test
    void aLossOrDeclarationAloneGivesNoSuccessBonus() {
        final ResultScore lost = score(100, 0, Difficulty.CONQUISTADOR, 0, null,
                1700);
        assertEquals(0, lost.getOrderBonusPercent());
        assertEquals(0, lost.getEarlyPoints());
        assertEquals(100, lost.getTotal());
    }

    @Test
    void aSuccessfulIndependenceNeedsItsDeclarationYear() {
        assertThrows(IllegalArgumentException.class,
                () -> score(100, 0, Difficulty.CONQUISTADOR, 0, 1, null));
    }

    @Test
    void overflowOfAnyTermIsReportedNotWrapped() {
        assertThrows(ArithmeticException.class, () -> score(100, 0,
                Difficulty.CONQUISTADOR, Long.MAX_VALUE, null, null));
    }
}
