package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * R-0169 rule values: the bundle price is ceil(1000 × k) for each difficulty,
 * and the interval opens on exactly every tenth update.
 */
class MercenaryRulesTest {

    @ParameterizedTest
    @CsvSource({ "DISCOVERER, 750", "EXPLORER, 900", "CONQUISTADOR, 1000",
            "GOVERNOR, 1150", "VICEROY, 1300" })
    void priceIsCeilingOfThousandTimesFactor(final Difficulty difficulty,
            final int expected) {
        assertEquals(expected, MercenaryRules.price(difficulty));
    }

    @Test
    void intervalOpensOnTheTenthUpdateOnly() {
        assertFalse(MercenaryRules.isIntervalUpdate(0));
        assertFalse(MercenaryRules.isIntervalUpdate(9));
        assertTrue(MercenaryRules.isIntervalUpdate(10));
        assertFalse(MercenaryRules.isIntervalUpdate(11));
        assertTrue(MercenaryRules.isIntervalUpdate(20));
    }

    @Test
    void peacetimeGateAndBundleAreTheAgreedValues() {
        assertEquals(5000, MercenaryRules.TREASURY_THRESHOLD);
        assertEquals(0.25, MercenaryRules.PEACETIME_CHANCE);
        assertEquals(2, MercenaryRules.SOLDIERS);
    }
}
