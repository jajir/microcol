package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GoodsTradeTest {

    static Stream<Arguments> dataProvider() {
        return Stream.of(
                // [0, 0, 0, 0]
                arguments(9, 12, 10000, 100),
                arguments(9, 12, 120, 10),
                arguments(9, 12, 131, 10));
    }

    @ParameterizedTest(name = "{index}: sellPrice = {0}, buyPrice = {1}, availableGold = {2}, expectedAmount = {3}")
    @MethodSource("dataProvider")
    public void test_getAvailableAmountFor(final int sellPrice, final int buyPrice,
            final int availableGold, final int expectedAmount) throws Exception {
        final GoodsTrade gt = new GoodsTrade(GoodsType.CIGARS, sellPrice, buyPrice);

        final Goods ga = gt.getAvailableAmountFor(availableGold);

        assertEquals(GoodsType.CIGARS, ga.getType());
        assertEquals(expectedAmount, ga.getAmount());
    }

}
