package org.akropia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

public class GoodsTest {

    @Test
    void verify_of_with_no_goods() throws Exception {
        final Goods grain = Goods.of(GoodsType.GRAIN);

        assertEquals(GoodsType.GRAIN, grain.getType());
        assertEquals(0, grain.getAmount());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10, 100, 200, 10000 })
    void verify_parametrized_amount_of_grain(final int amount)
            throws Exception {
        final Goods grain = Goods.of(GoodsType.GRAIN, amount);

        assertEquals(GoodsType.GRAIN, grain.getType());
        assertEquals(amount, grain.getAmount());
        if (amount == 0) {
            assertTrue(grain.isZero());
            assertFalse(grain.isNotZero());
        } else {
            assertFalse(grain.isZero());
            assertTrue(grain.isNotZero());
        }
    }

    @Test
    void verify_of_minus_10_grain() throws Exception {
        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Goods.of(GoodsType.GRAIN, -10));

        assertEquals("Amount (-10) can't be less than zero",
                exception.getMessage());
    }

    static Stream<Arguments> substractData() {
        return Stream.of(arguments(0, 0, 0), arguments(1, 1, 0),
                arguments(37, 17, 20), arguments(100, 100, 0));
    }

    @ParameterizedTest(name = "{index}: Grain {0} minus {1} = {2}")
    @MethodSource("substractData")
    void verify_substract(int item1, int item2, int item) throws Exception {
        final Goods grain1 = Goods.of(GoodsType.GRAIN, item1);
        final Goods grain2 = Goods.of(GoodsType.GRAIN, item2);

        final Goods grain = grain1.substract(grain2);
        assertEquals(item, grain.getAmount());
    }

    @Test
    void verify_that_different_goods_cant_be_substract() throws Exception {
        final Goods grain1 = Goods.of(GoodsType.GRAIN, 34);
        final Goods grain2 = Goods.of(GoodsType.GRAPES, 5);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> grain1.substract(grain2));

        assertEquals(
                "Substracted goods have differet type (Goods{GoodsType=GRAPES, amount=5}) from current one (Goods{GoodsType=GRAIN, amount=34})",
                exception.getMessage());
    }

    static Stream<Arguments> addData() {
        return Stream.of(arguments(0, 0, 0), arguments(1, 1, 2),
                arguments(37, 17, 54), arguments(100, 100, 200));
    }

    @ParameterizedTest(name = "{index}: Grain {0} plus {1} = {2}")
    @MethodSource("addData")
    void verify_add(int item1, int item2, int item) throws Exception {
        final Goods grain1 = Goods.of(GoodsType.GRAIN, item1);
        final Goods grain2 = Goods.of(GoodsType.GRAIN, item2);

        final Goods grain = grain1.add(grain2);
        assertEquals(item, grain.getAmount());
    }

    @Test
    void foodAdditionRejectsUnrepresentableAmount() {
        assertEquals(Integer.MAX_VALUE,
                Goods.of(GoodsType.GRAIN, Integer.MAX_VALUE - 1)
                        .add(Goods.of(GoodsType.GRAIN, 1)).getAmount());
        assertThrows(IllegalArgumentException.class,
                () -> Goods.of(GoodsType.GRAIN, Integer.MAX_VALUE)
                        .add(Goods.of(GoodsType.GRAIN, 1)));
    }

    @Test
    void verify_that_different_goods_cant_be_add() throws Exception {
        final Goods grain1 = Goods.of(GoodsType.GRAIN, 34);
        final Goods grain2 = Goods.of(GoodsType.GRAPES, 5);

        final IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> grain1.add(grain2));

        assertEquals(
                "Added goods have differet type (Goods{GoodsType=GRAPES, amount=5}) from current one (Goods{GoodsType=GRAIN, amount=34})",
                exception.getMessage());
    }

    @Test
    void verify_equals() throws Exception {
        assertFalse(Goods.of(GoodsType.GRAIN, 34).equals(null));
        assertFalse(Goods.of(GoodsType.GRAIN, 34)
                .equals(Goods.of(GoodsType.GRAPES, 5)));
        assertFalse(Goods.of(GoodsType.GRAIN, 34)
                .equals(Goods.of(GoodsType.GRAIN, 5)));
        assertTrue(Goods.of(GoodsType.GRAIN, 34)
                .equals(Goods.of(GoodsType.GRAIN, 34)));
        assertTrue(Goods.of(GoodsType.GRAIN, 0)
                .equals(Goods.of(GoodsType.GRAIN, 0)));
    }

}
