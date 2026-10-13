package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class EuropeTradeQuoteTest {

    @Test
    void purchaseAndSaleUseExactIntegerTotals() {
        EuropeTradeQuote purchase = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 50), true, 3, 17);
        assertEquals(150, purchase.getGross());
        assertEquals(25, purchase.getTax());
        assertEquals(175, purchase.getTotal());
        assertEquals(Goods.of(GoodsType.MUSKET, 50), purchase.getGoods());
        assertTrue(purchase.isPurchase());
        assertEquals(3, purchase.getUnitPrice());
        assertEquals(17, purchase.getTaxPercentage());

        EuropeTradeQuote sale = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 50), false, 2, 17);
        assertEquals(100, sale.getGross());
        assertEquals(17, sale.getTax());
        assertEquals(83, sale.getTotal());

        EuropeTradeQuote smallPurchase = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 1), true, 3, 17);
        assertEquals(3, smallPurchase.getGross());
        assertEquals(0, smallPurchase.getTax());
        assertEquals(3, smallPurchase.getTotal());

        EuropeTradeQuote smallSale = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 1), false, 3, 17);
        assertEquals(3, smallSale.getGross());
        assertEquals(1, smallSale.getTax());
        assertEquals(2, smallSale.getTotal());
    }

    @Test
    void zeroAndTaxBoundaries() {
        EuropeTradeQuote zeroQty = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 0), true, 5, 17);
        assertEquals(0, zeroQty.getGross());
        assertEquals(0, zeroQty.getTax());
        assertEquals(0, zeroQty.getTotal());

        EuropeTradeQuote zeroPrice = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 5), true, 0, 17);
        assertEquals(0, zeroPrice.getGross());
        assertEquals(0, zeroPrice.getTax());
        assertEquals(0, zeroPrice.getTotal());

        EuropeTradeQuote taxZero = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 10), true, 5, 0);
        assertEquals(50, taxZero.getGross());
        assertEquals(0, taxZero.getTax());
        assertEquals(50, taxZero.getTotal());

        EuropeTradeQuote highTaxPurchase = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 1), true, 50, 100);
        assertEquals(50, highTaxPurchase.getGross());
        assertEquals(50, highTaxPurchase.getTax());
        assertEquals(100, highTaxPurchase.getTotal());

        EuropeTradeQuote highTaxSale = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 10), false, 5, 100);
        assertEquals(50, highTaxSale.getGross());
        assertEquals(50, highTaxSale.getTax());
        assertEquals(0, highTaxSale.getTotal());
        assertEquals(50, new EuropeTradeQuote(Goods.of(GoodsType.MUSKET, 10),
                false, 5, 0).getTotal());
    }

    @Test
    void invalidAndOverflowPrices() {
        assertThrows(NullPointerException.class,
                () -> new EuropeTradeQuote(null, true, 5, 17));

        assertThrows(IllegalArgumentException.class,
                () -> new EuropeTradeQuote(Goods.of(GoodsType.MUSKET, 1), true,
                        -1, 17));
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeTradeQuote(Goods.of(GoodsType.MUSKET, 1), true,
                        5, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new EuropeTradeQuote(Goods.of(GoodsType.MUSKET, 1), true,
                        5, 101));

        assertThrows(ArithmeticException.class,
                () -> new EuropeTradeQuote(
                        Goods.of(GoodsType.MUSKET, Integer.MAX_VALUE), true, 2,
                        17));

        assertThrows(ArithmeticException.class,
                () -> new EuropeTradeQuote(Goods.of(GoodsType.MUSKET, 1), true,
                        Integer.MAX_VALUE, 1));
    }

    @Test
    void equalOffersRetainEveryPricingInput() {
        Goods g1a = Goods.of(GoodsType.MUSKET, 1);
        Goods g1b = Goods.of(GoodsType.MUSKET, 1);
        assertEquals(g1a, g1b);
        assertEquals(g1a.hashCode(), g1b.hashCode());

        assertNotEquals(g1a, null);
        assertNotEquals(g1a, new Object());

        EuropeTradeQuote quoteA = new EuropeTradeQuote(g1a, true, 10, 15);
        EuropeTradeQuote quoteB = new EuropeTradeQuote(g1b, true, 10, 15);
        assertEquals(quoteA, quoteB);
        assertEquals(quoteA.hashCode(), quoteB.hashCode());
        assertNotEquals(quoteA, null);
        assertNotEquals(quoteA, new Object());

        Goods g2 = Goods.of(GoodsType.TOOLS, 1);
        EuropeTradeQuote quoteDifferentGoods = new EuropeTradeQuote(g2, true,
                10, 15);
        assertNotEquals(quoteA, quoteDifferentGoods);

        EuropeTradeQuote quoteDifferentQty = new EuropeTradeQuote(
                Goods.of(GoodsType.MUSKET, 2), true, 10, 15);
        assertNotEquals(quoteA, quoteDifferentQty);

        EuropeTradeQuote quoteDifferentDirection = new EuropeTradeQuote(g1a,
                false, 10, 15);
        assertNotEquals(quoteA, quoteDifferentDirection);

        EuropeTradeQuote quoteDifferentUnitPrice = new EuropeTradeQuote(g1a,
                true, 11, 15);
        assertNotEquals(quoteA, quoteDifferentUnitPrice);

        EuropeTradeQuote quoteDifferentTax = new EuropeTradeQuote(g1a, true, 10,
                20);
        assertNotEquals(quoteA, quoteDifferentTax);

        // A rounded total alone cannot identify a still-current offer.
        EuropeTradeQuote taxOne = new EuropeTradeQuote(g1a, true, 1, 1);
        EuropeTradeQuote taxTwo = new EuropeTradeQuote(g1a, true, 1, 2);
        assertEquals(1, taxOne.getTotal());
        assertEquals(1, taxTwo.getTotal());
        assertNotEquals(taxOne, taxTwo);
    }
}