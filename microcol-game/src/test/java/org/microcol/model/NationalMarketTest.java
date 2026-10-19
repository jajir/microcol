package org.microcol.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.EuropePo;

public class NationalMarketTest {

    private final Europe europe = new Europe(mock(Model.class));

    private GoodsTrade corn(final EuropeanNation nation) {
        return europe.getGoodsTradeForType(GoodsType.CORN, nation);
    }

    @Test
    public void test_everyNationStartsAtCatalogueQuote() {
        for (final EuropeanNation nation : EuropeanNation.values()) {
            assertEquals(2, corn(nation).getSellPrice());
            assertEquals(4, corn(nation).getBuyPrice());
            assertFalse(corn(nation).isTradedSinceUpdate());
        }
    }

    @Test
    public void test_ownSalesLowerBidOneStepPerHundredUnits() {
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 50);
        assertEquals(2, corn(EuropeanNation.ENGLISH).getSellPrice());
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 50);
        assertEquals(1, corn(EuropeanNation.ENGLISH).getSellPrice());
        assertEquals(3, corn(EuropeanNation.ENGLISH).getBuyPrice());
    }

    @Test
    public void test_crossMarketMovesOneEighthOfUnitPerSingleSale() {
        // One unit in another market is a quarter of a unit: two eighths.
        // Four hundred of them reach one full step of a hundred units.
        for (int sale = 0; sale < 399; sale++) {
            europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 1);
        }
        assertEquals(2, corn(EuropeanNation.FRENCH).getSellPrice());
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 1);
        assertEquals(1, corn(EuropeanNation.FRENCH).getSellPrice());
        // The four hundred own units reached the floor of the actor's market.
        assertEquals(1, corn(EuropeanNation.ENGLISH).getSellPrice());
    }

    @Test
    public void test_purchaseRaisesBidAndKeepsRemainder() {
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, -99);
        assertEquals(2, corn(EuropeanNation.ENGLISH).getSellPrice());
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, -1);
        assertEquals(3, corn(EuropeanNation.ENGLISH).getSellPrice());
        assertEquals(5, corn(EuropeanNation.ENGLISH).getBuyPrice());
    }

    @Test
    public void test_floorDiscardsOutwardPressureAtBound() {
        // 1001 units leave a positive remainder beyond the floor; it must not
        // bank, so a later purchase of 100 moves the bid back up from 1 to 2.
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 1001);
        assertEquals(1, corn(EuropeanNation.ENGLISH).getSellPrice());
        assertEquals(0, corn(EuropeanNation.ENGLISH).getPressure());
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, -100);
        assertEquals(2, corn(EuropeanNation.ENGLISH).getSellPrice());
    }

    @Test
    public void test_ceilingDiscardsOutwardPressureAtBound() {
        // Silver starts at bid 3; purchases raise it to 20. The remainder
        // beyond the ceiling is discarded, so one sale of 100 gives bid 19.
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.SILVER, -2001);
        assertEquals(20, europe
                .getGoodsTradeForType(GoodsType.SILVER, EuropeanNation.ENGLISH)
                .getSellPrice());
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.SILVER, 100);
        assertEquals(19, europe
                .getGoodsTradeForType(GoodsType.SILVER, EuropeanNation.ENGLISH)
                .getSellPrice());
    }

    @Test
    public void test_quietRecoveryNeedsNoAffectingTradeSinceLastUpdate() {
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 100);
        assertEquals(1, corn(EuropeanNation.ENGLISH).getSellPrice());
        // The trade affected this update, so the bid stays and the flag resets.
        europe.recoverMarkets(EuropeanNation.ENGLISH);
        assertEquals(1, corn(EuropeanNation.ENGLISH).getSellPrice());
        assertFalse(corn(EuropeanNation.ENGLISH).isTradedSinceUpdate());
        // A quiet update moves one step toward the initial bid.
        europe.recoverMarkets(EuropeanNation.ENGLISH);
        assertEquals(2, corn(EuropeanNation.ENGLISH).getSellPrice());
        assertEquals(4, corn(EuropeanNation.ENGLISH).getBuyPrice());
        europe.recoverMarkets(EuropeanNation.ENGLISH);
        assertEquals(2, corn(EuropeanNation.ENGLISH).getSellPrice());
    }

    @Test
    public void test_crossMarketTradeCountsAsAffectingRecovery() {
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 100);
        europe.recoverMarkets(EuropeanNation.ENGLISH);
        europe.applyTrade(EuropeanNation.FRENCH, GoodsType.CORN, 1);
        europe.recoverMarkets(EuropeanNation.ENGLISH);
        assertEquals(1, corn(EuropeanNation.ENGLISH).getSellPrice());
        europe.recoverMarkets(EuropeanNation.ENGLISH);
        assertEquals(2, corn(EuropeanNation.ENGLISH).getSellPrice());
    }

    @Test
    public void test_savedStateRoundTripKeepsPressureAndFlag() {
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 37);
        final EuropePo saved = europe.save();
        final Europe restored = new Europe(mock(Model.class), saved);
        final GoodsTrade trade = restored.getGoodsTradeForType(GoodsType.CORN,
                EuropeanNation.ENGLISH);
        assertEquals(2, trade.getSellPrice());
        assertEquals(296, trade.getPressure());
        assertTrue(trade.isTradedSinceUpdate());
        // The restored remainder continues exactly: 296 + 504 reaches one step.
        restored.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 63);
        assertEquals(1, trade.getSellPrice());
        assertEquals(0, trade.getPressure());
    }

    @Test
    public void test_malformedSavedStateIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 0, 0, 0 })));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 2, 800, 0 })));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 1, 8, 0 })));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 2, 0, 2 })));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class), missingNation("FRENCH")));
    }

    @Test
    public void test_pressureBoundsRejectOverflowingSavedValues() {
        // Math.abs(Integer.MIN_VALUE) is negative, so a bound written with it
        // would accept this value; the signed bound must reject it.
        for (final int pressure : new int[] { Integer.MIN_VALUE,
                Integer.MAX_VALUE, -800, 800 }) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Europe(mock(Model.class), withState("ENGLISH",
                            "CORN", new int[] { 2, pressure, 0 })));
        }
        // The nearest accepted values on either side of the step.
        final Europe edge = new Europe(mock(Model.class),
                withState("ENGLISH", "CORN", new int[] { 2, -799, 1 }));
        assertEquals(-799, edge
                .getGoodsTradeForType(GoodsType.CORN, EuropeanNation.ENGLISH)
                .getPressure());
        assertEquals(799,
                new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 2, 799, 0 }))
                        .getGoodsTradeForType(GoodsType.CORN,
                                EuropeanNation.ENGLISH)
                        .getPressure());
    }

    @Test
    public void test_savedBidAndOutwardPressureBoundaries() {
        for (final int bid : new int[] { Integer.MIN_VALUE, -1, 0, 21,
                Integer.MAX_VALUE }) {
            assertThrows(IllegalArgumentException.class, () -> new Europe(
                    mock(Model.class),
                    withState("ENGLISH", "CORN", new int[] { bid, 0, 0 })));
        }
        // Outward pressure can never be saved at a bound; inward can.
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 1, 1, 0 })));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 20, -1, 0 })));
        final Europe inward = new Europe(mock(Model.class),
                withState("ENGLISH", "CORN", new int[] { 1, -799, 0 }));
        assertEquals(1, inward
                .getGoodsTradeForType(GoodsType.CORN, EuropeanNation.ENGLISH)
                .getSellPrice());
        final Europe ceiling = new Europe(mock(Model.class),
                withState("ENGLISH", "CORN", new int[] { 20, 799, 0 }));
        assertEquals(20, ceiling
                .getGoodsTradeForType(GoodsType.CORN, EuropeanNation.ENGLISH)
                .getSellPrice());
    }

    @Test
    public void test_missingOrShortStateRejectsTheWholeMarket() {
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class), new EuropePo()));
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class),
                        withState("ENGLISH", "CORN", new int[] { 2, 0 })));
        final EuropePo missingGood = Europe.initialPo();
        missingGood.getMarkets().get("SPANISH").remove("MUSKET");
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class), missingGood));
        final EuropePo extraGood = Europe.initialPo();
        extraGood.getMarkets().get("SPANISH").put("HAMMERS",
                new int[] { 2, 0, 0 });
        assertThrows(IllegalArgumentException.class,
                () -> new Europe(mock(Model.class), extraGood));
    }

    @Test
    public void test_extremeTradesStayWithinBoundsWithoutOverflow() {
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN,
                Integer.MAX_VALUE);
        for (final EuropeanNation nation : EuropeanNation.values()) {
            assertEquals(1, corn(nation).getSellPrice());
            assertEquals(0, corn(nation).getPressure());
            assertTrue(corn(nation).isTradedSinceUpdate());
        }
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN,
                Integer.MIN_VALUE);
        for (final EuropeanNation nation : EuropeanNation.values()) {
            assertEquals(20, corn(nation).getSellPrice());
            assertEquals(0, corn(nation).getPressure());
        }
    }

    @Test
    public void test_zeroAndUnknownTradesChangeNothing() {
        europe.applyTrade(EuropeanNation.ENGLISH, GoodsType.CORN, 0);
        assertFalse(corn(EuropeanNation.FRENCH).isTradedSinceUpdate());
        assertThrows(IllegalArgumentException.class, () -> europe
                .applyTrade(EuropeanNation.ENGLISH, GoodsType.HAMMERS, 5));
        for (final EuropeanNation nation : EuropeanNation.values()) {
            assertEquals(0, corn(nation).getPressure());
            assertEquals(2, corn(nation).getSellPrice());
        }
    }

    private static EuropePo withState(final String nation, final String goods,
            final int[] state) {
        final EuropePo po = Europe.initialPo();
        po.getMarkets().get(nation).put(goods, state);
        return po;
    }

    private static EuropePo missingNation(final String nation) {
        final EuropePo po = Europe.initialPo();
        po.getMarkets().remove(nation);
        return po;
    }

}
