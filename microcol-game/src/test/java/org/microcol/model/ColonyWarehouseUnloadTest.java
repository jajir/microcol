package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ColonyWarehouseUnloadTest {

    private final Colony colony = mock(Colony.class);
    private final Player owner = mock(Player.class);
    private final Unit carrier = mock(Unit.class);
    private final Cargo cargo = mock(Cargo.class);
    private ColonyWarehouse warehouse;
    private CargoSlot source;

    @BeforeEach
    void prepareOwnCargoAtColony() {
        when(colony.isValid()).thenReturn(true);
        when(colony.getWarehouseType())
                .thenReturn(ConstructionType.WAREHOUSE_BASIC);
        when(colony.getOwner()).thenReturn(owner);
        when(colony.getLocation()).thenReturn(Location.of(22, 12));
        when(cargo.getOwner()).thenReturn(carrier);
        when(carrier.getOwner()).thenReturn(owner);
        when(carrier.isAtPlaceLocation()).thenReturn(true);
        when(carrier.getLocation()).thenReturn(Location.of(22, 12));
        when(owner.getUnitsAt(Location.of(22, 12)))
                .thenReturn(List.of(carrier));
        warehouse = new ColonyWarehouse(colony, Map.of("TOBACCO", 99));
        source = new CargoSlot(cargo, Goods.of(GoodsType.TOBACCO, 100));
    }

    @Test
    void foreignCargoIsRejectedWithoutChangingEitherInventory() {
        when(carrier.getOwner()).thenReturn(mock(Player.class));

        assertThrows(IllegalArgumentException.class, () -> warehouse
                .moveToWarehouse(Goods.of(GoodsType.TOBACCO, 1), source));

        assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    static Stream<Arguments> warehouseBoundaries() {
        return GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(type -> type != GoodsType.CORN)
                .flatMap(type -> Stream.of(
                        Arguments.of(type, ConstructionType.WAREHOUSE_BASIC,
                                100),
                        Arguments.of(type, ConstructionType.WAREHOUSE, 200),
                        Arguments.of(type, ConstructionType.WAREHOUSE_EXPANSION,
                                300)));
    }

    @ParameterizedTest(name = "{0} in {1}: capacity {2}")
    @MethodSource("warehouseBoundaries")
    void onlyTheExactAmountThatFitsCanBeTransferred(final GoodsType type,
            final ConstructionType building, final int capacity) {
        when(colony.getWarehouseType()).thenReturn(building);
        warehouse = new ColonyWarehouse(colony,
                Map.of(type.name(), capacity - 1));
        source = new CargoSlot(cargo, Goods.of(type, 100));

        assertEquals(Goods.of(type, 1),
                warehouse.getUnloadableGoods(Goods.of(type, 100), source));
        assertEquals(capacity - 1, warehouse.getGoods(type).getAmount());
        assertEquals(Goods.of(type, 100), source.getGoods().get());

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(Goods.of(type, 2), source));
        assertEquals(capacity - 1, warehouse.getGoods(type).getAmount());
        assertEquals(Goods.of(type, 100), source.getGoods().get());

        warehouse.moveToWarehouse(Goods.of(type, 1), source);

        assertEquals(capacity, warehouse.getGoods(type).getAmount());
        assertEquals(Goods.of(type, 99), source.getGoods().get());
        assertEquals(Goods.of(type, 0),
                warehouse.getUnloadableGoods(Goods.of(type, 99), source));
        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(Goods.of(type, 1), source));
        assertEquals(capacity, warehouse.getGoods(type).getAmount());
        assertEquals(Goods.of(type, 99), source.getGoods().get());
    }

    @Test
    void unloadingAllCargoEmptiesOnlyTheSourceSlot() {
        warehouse = new ColonyWarehouse(colony, Map.of("COTTON", 50));
        source = new CargoSlot(cargo, Goods.of(GoodsType.COTTON, 20));
        final CargoSlot other = new CargoSlot(cargo,
                Goods.of(GoodsType.TOBACCO, 100));

        assertEquals(Goods.of(GoodsType.COTTON, 20), warehouse
                .getUnloadableGoods(Goods.of(GoodsType.COTTON, 20), source));
        warehouse.moveToWarehouse(Goods.of(GoodsType.COTTON, 20), source);

        assertEquals(70, warehouse.getGoods(GoodsType.COTTON).getAmount());
        assertTrue(source.isEmpty());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), other.getGoods().get());
    }

    @Test
    void rejectsZeroWrongTypeAndExcessCargoWithoutMutation() {
        for (final Goods invalid : new Goods[] { Goods.of(GoodsType.TOBACCO, 0),
                Goods.of(GoodsType.COTTON, 1),
                Goods.of(GoodsType.TOBACCO, 101) }) {
            assertThrows(IllegalArgumentException.class,
                    () -> warehouse.getUnloadableGoods(invalid, source));
            assertThrows(IllegalArgumentException.class,
                    () -> warehouse.moveToWarehouse(invalid, source));
            assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
            assertEquals(0, warehouse.getGoods(GoodsType.COTTON).getAmount());
            assertEquals(Goods.of(GoodsType.TOBACCO, 100),
                    source.getGoods().get());
        }
        assertThrows(IllegalArgumentException.class,
                () -> Goods.of(GoodsType.TOBACCO, -1));
    }

    @Test
    void rejectsNonCargoGoods() {
        for (final GoodsType type : new GoodsType[] { GoodsType.BELL,
                GoodsType.CROSS, GoodsType.HAMMERS }) {
            source = new CargoSlot(cargo, Goods.of(type, 1));
            assertThrows(IllegalArgumentException.class, () -> warehouse
                    .getUnloadableGoods(Goods.of(type, 1), source));
            assertThrows(IllegalArgumentException.class,
                    () -> warehouse.moveToWarehouse(Goods.of(type, 1), source));
            assertEquals(0, warehouse.getGoods(type).getAmount());
            assertEquals(Goods.of(type, 1), source.getGoods().get());
        }
    }

    @Test
    void rejectsEmptyAndUnitCargo() {
        source = new CargoSlot(cargo);
        assertInvalidSource();
        assertTrue(source.isEmpty());

        final Unit passenger = mock(Unit.class);
        when(passenger.getOwner()).thenReturn(owner);
        source.store(passenger);
        assertInvalidSource();
        assertSame(passenger, source.getUnit().get());
    }

    @Test
    void rejectsNullInputsWithoutMutation() {
        final Goods goods = Goods.of(GoodsType.TOBACCO, 1);
        assertThrows(NullPointerException.class,
                () -> warehouse.getUnloadableGoods(null, source));
        assertThrows(NullPointerException.class,
                () -> warehouse.getUnloadableGoods(goods, null));
        assertThrows(NullPointerException.class,
                () -> warehouse.moveToWarehouse(null, source));
        assertThrows(NullPointerException.class,
                () -> warehouse.moveToWarehouse(goods, null));
        assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    @Test
    void rejectsRemoteCargo() {
        when(carrier.getLocation()).thenReturn(Location.of(23, 12));
        assertInvalidSource();
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    @Test
    void rejectsOffMapCargoWithoutReadingItsLocation() {
        when(carrier.isAtPlaceLocation()).thenReturn(false);
        clearInvocations(carrier);
        assertInvalidSource();
        verify(carrier, never()).getLocation();
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    @Test
    void repeatsCapacityValidationAfterPreview() {
        final Goods preview = warehouse
                .getUnloadableGoods(Goods.of(GoodsType.TOBACCO, 100), source);
        warehouse.addGoods(Goods.of(GoodsType.TOBACCO, 1));

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(preview, source));

        assertEquals(100, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    @Test
    void repeatsCargoValidationAfterPreview() {
        final Goods preview = warehouse
                .getUnloadableGoods(Goods.of(GoodsType.TOBACCO, 100), source);
        source.removeCargo(Goods.of(GoodsType.TOBACCO, 100));

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(preview, source));

        assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
        assertTrue(source.isEmpty());
    }

    @Test
    void repeatsOwnerAndLocationValidationAfterPreview() {
        final Goods preview = warehouse
                .getUnloadableGoods(Goods.of(GoodsType.TOBACCO, 100), source);
        when(carrier.getOwner()).thenReturn(mock(Player.class));
        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(preview, source));
        when(carrier.getOwner()).thenReturn(owner);
        when(carrier.getLocation()).thenReturn(Location.of(23, 12));
        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(preview, source));
        assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    @Test
    void rejectsDestroyedColonyAfterPreview() {
        final Goods preview = warehouse
                .getUnloadableGoods(Goods.of(GoodsType.TOBACCO, 100), source);
        when(colony.isValid()).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.getUnloadableGoods(preview, source));
        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(preview, source));
        assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    @Test
    void rejectsDestroyedCarrierAfterPreview() {
        final Goods preview = warehouse
                .getUnloadableGoods(Goods.of(GoodsType.TOBACCO, 100), source);
        when(owner.getUnitsAt(Location.of(22, 12))).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class,
                () -> warehouse.getUnloadableGoods(preview, source));
        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(preview, source));
        assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
        assertEquals(Goods.of(GoodsType.TOBACCO, 100), source.getGoods().get());
    }

    private void assertInvalidSource() {
        final Goods goods = Goods.of(GoodsType.TOBACCO, 1);
        assertThrows(IllegalArgumentException.class,
                () -> warehouse.getUnloadableGoods(goods, source));
        assertThrows(IllegalArgumentException.class,
                () -> warehouse.moveToWarehouse(goods, source));
        assertEquals(99, warehouse.getGoods(GoodsType.TOBACCO).getAmount());
    }
}
