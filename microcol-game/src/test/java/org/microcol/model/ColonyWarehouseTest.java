package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ColonyWarehouseTest {

    private final Colony colony = mock(Colony.class);

    private ColonyWarehouse warehouse;

    private Map<String, Integer> initialGoods;

    @Test
    void test_addGoodsWithThrowingAway_goods_is_null() throws Exception {
        assertThrows(NullPointerException.class,
                () -> warehouse.addGoodsWithThrowingAway(null, null));
    }

    @Test
    void test_addGoodsWithThrowingAway_add_2_ore() throws Exception {
        final Goods ret = warehouse.addGoodsWithThrowingAway(Goods.of(GoodsType.ORE, 2),
                thrownAway -> fail());

        assertNotNull(ret);
        assertEquals(GoodsType.ORE, ret.getType());
        assertEquals(0, ret.getAmount());
    }

    @Test
    void test_addGoodsWithThrowingAway_add_3_ore() throws Exception {
        final Goods ret = warehouse.addGoodsWithThrowingAway(Goods.of(GoodsType.ORE, 3),
                thrownAway -> fail());

        assertNotNull(ret);
        assertEquals(GoodsType.ORE, ret.getType());
        assertEquals(0, ret.getAmount());
    }

    @Test
    void test_addGoodsWithThrowingAway_add_4_ore() throws Exception {
        final AtomicBoolean wasCalled = new AtomicBoolean(false);
        final Goods ret = warehouse.addGoodsWithThrowingAway(Goods.of(GoodsType.ORE, 4),
                thrownAway -> {
                    wasCalled.set(true);
                });

        assertTrue(wasCalled.get());
        assertNotNull(ret);
        assertEquals(GoodsType.ORE, ret.getType());
        assertEquals(1, ret.getAmount());
    }

    @Test
    void test_addGoodsWithThrowingAway_add_5_ore() throws Exception {
        final Goods ret = warehouse.addGoodsWithThrowingAway(Goods.of(GoodsType.ORE, 5), null);

        assertNotNull(ret);
        assertEquals(GoodsType.ORE, ret.getType());
        assertEquals(2, ret.getAmount());
    }

    @Test
    void test_addGoodsWithThrowingAway_add_10_cigars() throws Exception {
        final Goods ret = warehouse.addGoodsWithThrowingAway(Goods.of(GoodsType.CIGARS, 10), null);

        assertNotNull(ret);
        assertEquals(GoodsType.CIGARS, ret.getType());
        assertEquals(10, ret.getAmount());
    }

    @Test
    void test_add_24_corn() throws Exception {
        warehouse.addGoods(Goods.of(GoodsType.CORN, 24));

        assertEquals(24, warehouse.getGoods(GoodsType.CORN).getAmount());
    }

    @Test
    void test_add_140_cotton() throws Exception {
        warehouse.addGoods(Goods.of(GoodsType.COTTON, 140));

        assertEquals(290, warehouse.getGoods(GoodsType.COTTON).getAmount());
    }

    @Test
    void test_add_null_goods() throws Exception {
        assertThrows(NullPointerException.class, () -> warehouse.addGoods(null));
    }

    @Test
    void test_remove_null_goods() throws Exception {
        assertThrows(NullPointerException.class, () -> warehouse.removeGoods(null));
    }

    @Test
    void test_remove_10_cotton() throws Exception {
        warehouse.removeGoods(Goods.of(GoodsType.COTTON, 10));

        assertEquals(140, warehouse.getGoods(GoodsType.COTTON).getAmount());
    }

    @Test
    void test_remove_150_cotton() throws Exception {
        warehouse.removeGoods(Goods.of(GoodsType.COTTON, 150));

        assertEquals(0, warehouse.getGoods(GoodsType.COTTON).getAmount());
    }

    @Test
    void test_remove_0_corn() throws Exception {
        warehouse.removeGoods(Goods.of(GoodsType.CORN, 0));

        assertEquals(0, warehouse.getGoods(GoodsType.CORN).getAmount());
    }

    @Test
    void test_remove_200_cotton() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> warehouse.removeGoods(Goods.of(GoodsType.COTTON, 200)));

        assertEquals(
                "Can't remove 200 of GoodsType{name=COTTON}, because in warehouse is currently just 150 of GoodsType{name=COTTON}",
                exception.getMessage());
        assertEquals(150, warehouse.getGoods(GoodsType.COTTON).getAmount());
    }

    @Test
    void test_setGoodsToZero_corn() throws Exception {
        warehouse.setGoodsToZero(GoodsType.CORN);

        assertEquals(0, warehouse.getGoods(GoodsType.CORN).getAmount());
    }

    @Test
    void test_setGoodsToZero_cotton() throws Exception {
        warehouse.setGoodsToZero(GoodsType.COTTON);

        assertEquals(0, warehouse.getGoods(GoodsType.COTTON).getAmount());
    }

    @Test
    void test_setGoodsToZero_null_goodsType() throws Exception {
        assertThrows(NullPointerException.class, () -> warehouse.setGoodsToZero(null));
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> dataStorageLimit() {
        return Stream.of(
                arguments(GoodsType.BELL, Integer.MAX_VALUE, ConstructionType.WAREHOUSE_BASIC),
                arguments(GoodsType.BELL, Integer.MAX_VALUE, ConstructionType.WAREHOUSE_EXPANSION),
                arguments(GoodsType.CORN, 200, ConstructionType.WAREHOUSE_BASIC),
                arguments(GoodsType.CORN, 200, ConstructionType.WAREHOUSE),
                arguments(GoodsType.CORN, 200, ConstructionType.WAREHOUSE_EXPANSION),
                arguments(GoodsType.COTTON, 100, ConstructionType.WAREHOUSE_BASIC),
                arguments(GoodsType.COTTON, 200, ConstructionType.WAREHOUSE),
                arguments(GoodsType.COTTON, 300, ConstructionType.WAREHOUSE_EXPANSION),
                arguments(GoodsType.ORE, 100, ConstructionType.WAREHOUSE_BASIC),
                arguments(GoodsType.ORE, 200, ConstructionType.WAREHOUSE),
                arguments(GoodsType.ORE, 300, ConstructionType.WAREHOUSE_EXPANSION),
                arguments(GoodsType.CROSS, Integer.MAX_VALUE, ConstructionType.WAREHOUSE_BASIC));
    }

    @ParameterizedTest(name = "{index}: Storage limit for goods {0} is {1} in {2} ")
    @MethodSource("dataStorageLimit")
    void test_getStorageCapacity(final GoodsType goodsType, final Integer limit,
            final ConstructionType constructionType) throws Exception {
        final Integer realLimit = warehouse.getStorageCapacity(goodsType, constructionType);

        assertEquals(limit, realLimit,
                String.format("Expected limit was %s but really is %s", limit, realLimit));
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> dataTransgerableGoods() {
        return Stream.of(arguments(GoodsType.BELL, Integer.MAX_VALUE, 0),
                arguments(GoodsType.CORN, 200, 0), arguments(GoodsType.CORN, 0, 0),
                arguments(GoodsType.COTTON, 200, 150), arguments(GoodsType.COTTON, 0, 0),
                arguments(GoodsType.COTTON, 12, 12));
    }

    @ParameterizedTest(name = "{index}: Transferable for goods {0} with limit {1} is {2}")
    @MethodSource("dataTransgerableGoods")
    void test_getTransferableGoods(final GoodsType goodsType, final int limit,
            final int expectedAmount) {
        final Goods ret = warehouse.getTransferableGoods(goodsType, limit);

        assertEquals(goodsType, ret.getType());
        assertEquals(expectedAmount, ret.getAmount());
    }

    @BeforeEach
    private void beforeEach() {
        initialGoods = new HashMap<>();
        initialGoods.put(GoodsType.COTTON.name(), 150);
        initialGoods.put(GoodsType.ORE.name(), 297);
        initialGoods.put(GoodsType.CIGARS.name(), 300);
        when(colony.getWarehouseType()).thenReturn(ConstructionType.WAREHOUSE_EXPANSION);
        warehouse = new ColonyWarehouse(colony, initialGoods);
    }

    @AfterEach
    private void afterEach() {
        initialGoods = null;
        warehouse = null;
    }

}
