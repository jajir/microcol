package org.microcol.model;

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
        final Goods corn = Goods.of(GoodsType.CORN);

        assertEquals(GoodsType.CORN, corn.getType());
        assertEquals(0, corn.getAmount());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 10, 100, 200, 10000 })
    void verify_parametrized_amount_of_corn(final int amount) throws Exception {
        final Goods corn = Goods.of(GoodsType.CORN, amount);

        assertEquals(GoodsType.CORN, corn.getType());
        assertEquals(amount, corn.getAmount());
        if (amount == 0) {
            assertTrue(corn.isZero());
            assertFalse(corn.isNotZero());
        } else {
            assertFalse(corn.isZero());
            assertTrue(corn.isNotZero());
        }
    }

    @Test
    void verify_of_minus_10_corns() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Goods.of(GoodsType.CORN, -10));

        assertEquals("Amount (-10) can't be less than zero", exception.getMessage());
    }

    static Stream<Arguments> substractData() {
        return Stream.of(arguments(0, 0, 0), arguments(1, 1, 0), arguments(37, 17, 20),
                arguments(100, 100, 0));
    }

    @ParameterizedTest(name = "{index}: Corn {0} minus {1} = {2}")
    @MethodSource("substractData")
    void verify_substract(int item1, int item2, int item) throws Exception {
        final Goods corn1 = Goods.of(GoodsType.CORN, item1);
        final Goods corn2 = Goods.of(GoodsType.CORN, item2);

        final Goods corn = corn1.substract(corn2);
        assertEquals(item, corn.getAmount());
    }

    @Test
    void verify_that_different_goods_cant_be_substract() throws Exception {
        final Goods corn1 = Goods.of(GoodsType.CORN, 34);
        final Goods corn2 = Goods.of(GoodsType.TOBACCO, 5);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> corn1.substract(corn2));

        assertEquals(
                "Substracted goods have differet type (Goods{GoodsType=TOBACCO, amount=5}) from current one (Goods{GoodsType=CORN, amount=34})",
                exception.getMessage());
    }

    static Stream<Arguments> addData() {
        return Stream.of(arguments(0, 0, 0), arguments(1, 1, 2), arguments(37, 17, 54),
                arguments(100, 100, 200));
    }

    @ParameterizedTest(name = "{index}: Corn {0} plus {1} = {2}")
    @MethodSource("addData")
    void verify_add(int item1, int item2, int item) throws Exception {
        final Goods corn1 = Goods.of(GoodsType.CORN, item1);
        final Goods corn2 = Goods.of(GoodsType.CORN, item2);

        final Goods corn = corn1.add(corn2);
        assertEquals(item, corn.getAmount());
    }

    @Test
    void verify_that_different_goods_cant_be_add() throws Exception {
        final Goods corn1 = Goods.of(GoodsType.CORN, 34);
        final Goods corn2 = Goods.of(GoodsType.TOBACCO, 5);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> corn1.add(corn2));

        assertEquals(
                "Added goods have differet type (Goods{GoodsType=TOBACCO, amount=5}) from current one (Goods{GoodsType=CORN, amount=34})",
                exception.getMessage());
    }

    @Test
    void verify_equals() throws Exception {
        assertFalse(Goods.of(GoodsType.CORN, 34).equals(null));
        assertFalse(Goods.of(GoodsType.CORN, 34).equals(Goods.of(GoodsType.TOBACCO, 5)));
        assertFalse(Goods.of(GoodsType.CORN, 34).equals(Goods.of(GoodsType.CORN, 5)));
        assertTrue(Goods.of(GoodsType.CORN, 34).equals(Goods.of(GoodsType.CORN, 34)));
        assertTrue(Goods.of(GoodsType.CORN, 0).equals(Goods.of(GoodsType.CORN, 0)));
    }

}
