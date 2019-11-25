package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CargoSlotTest {

    private final static String NOT_ENOUGH_GOLD = "not enough gold";

    private CargoSlot slot;

    private final Cargo cargo = mock(Cargo.class);

    private final Player player = mock(Player.class);

    private final Unit ownerUnit = mock(Unit.class);

    private final CargoSlot sourceCargoSlot = mock(CargoSlot.class);

    private final Unit unit = mock(Unit.class);

    private final Colony colony = mock(Colony.class);

    private final ColonyWarehouse warehouse = mock(ColonyWarehouse.class);

    @Test
    public void test_storeFromCargoSlot_null_goods() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            slot.storeFromCargoSlot(null, sourceCargoSlot);
        });
    }

    @Test()
    public void test_storeFromCargoSlot_null_sourceCargoSlot() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 100), null);
        });
    }

    @Test()
    public void test_storeFromCargoSlot_not_same_owner() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(null);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 100), sourceCargoSlot);
                });

        assertTrue(
                exception.getMessage().contains(
                        "Source cargo slot and target cargo slots doesn't belongs to same user"),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test()
    public void test_storeFromCargoSlot_already_occupied_by_unit() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(unit.getOwner()).thenReturn(player);

        slot.store(unit);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 100), sourceCargoSlot);
                });

        assertTrue(
                exception.getMessage()
                        .contains("Attempt to move cargo to slot occupied with unit."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test()
    public void test_storeFromCargoSlot_source_have_to_contain_goods() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods()).thenReturn(Optional.empty());

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 13), sourceCargoSlot);
                });

        assertTrue(exception.getMessage().contains("Source cargo slot doesn't contains any good"),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test()
    public void test_storeFromCargoSlot_transferred_goodsType_isDifferent_from_source()
            throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods()).thenReturn(Optional.of(new Goods(GoodsType.CIGARS, 13)));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 13), sourceCargoSlot);
                });

        assertTrue(
                exception.getMessage()
                        .contains("Source cargo slot contains diffrent good than was transfered."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test
    public void test_storeFromCargoSlot_transfer_all() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods()).thenReturn(Optional.of(new Goods(GoodsType.CORN, 13)));

        slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 13), sourceCargoSlot);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.CORN, slot.getGoods().get().getType());
        assertEquals(13, slot.getGoods().get().getAmount());
    }

    @Test()
    public void test_storeFromCargoSlot_transferred_goodsType_isDifferent_from_alreadyStored()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 13));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 13), sourceCargoSlot);
                });

        assertTrue(
                exception.getMessage()
                        .contains("Tranfered cargo is diffrent type this is stored in slot."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test
    public void test_storeFromCargoSlot_transfered_good_is_added_to_already_stored_1()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CORN, 30));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods()).thenReturn(Optional.of(new Goods(GoodsType.CORN, 13)));

        slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 13), sourceCargoSlot);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.CORN, slot.getGoods().get().getType());
        assertEquals(43, slot.getGoods().get().getAmount());

        verify(sourceCargoSlot).removeCargo(Goods.of(GoodsType.CORN, 13));
    }

    @Test
    public void test_storeFromCargoSlot_transfered_good_is_added_to_already_stored_2()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CORN, 30));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods()).thenReturn(Optional.of(new Goods(GoodsType.CORN, 13)));

        slot.storeFromCargoSlot(new Goods(GoodsType.CORN, 80), sourceCargoSlot);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.CORN, slot.getGoods().get().getType());
        assertEquals(43, slot.getGoods().get().getAmount());

        verify(sourceCargoSlot).removeCargo(Goods.of(GoodsType.CORN, 13));
    }

    @Test
    public void test_store_unit() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(unit.getOwner()).thenReturn(player);

        slot.store(unit);

        assertSame(unit, slot.getUnit().get());
        assertEquals(0, slot.getAvailableCapacity());
    }

    @Test()
    public void test_store_unit_another_one() throws Exception {
        loadUnit(unit);

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            slot.store(unit);
        });

        assertTrue(exception.getMessage().contains("is already loaded."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    /**
     * When exception is throws by unit.placeToCargoSlot than verify that slot
     * is still empty.
     * 
     * @throws Exception generic exception
     */
    @Test()
    public void test_store_exception_consistency() throws Exception {
        assertTrue(slot.isEmpty());
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(unit.getOwner()).thenReturn(player);

        doThrow(IllegalArgumentException.class).when(unit).placeToCargoSlot(any());

        assertThrows(IllegalArgumentException.class, () -> {
            slot.store(unit);
        });

        assertTrue(slot.isEmpty());
    }

    @Test()
    public void test_storeFromColonyWarehouse_good_ammount_cant_be_null() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            slot.storeFromColonyWarehouse(null, colony);
        });
    }

    @Test()
    public void test_storeFromColonyWarehouse_colony_cant_be_null() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 10), null);
        });
    }

    @Test()
    public void test_storeFromColonyWarehouse_colony_have_different_owner() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(null);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 10), colony);
                });

        assertTrue(exception.getMessage().contains(
                "Source colony warehouse and target cargo slots doesn't belongs to same user"),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test()
    public void test_storeFromColonyWarehouse_is_allready_occupied_by_unit() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(unit.getOwner()).thenReturn(player);

        slot.store(unit);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 10), colony);
                });

        assertTrue(
                exception.getMessage()
                        .contains("Attempt to move cargo to slot occupied with unit."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test()
    public void test_storeFromColonyWarehouse_warehouse_limit_transferring_goods_amount()
            throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoods(GoodsType.SILK)).thenReturn(Goods.of(GoodsType.SILK, 9));

        slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 10), colony);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.SILK, slot.getGoods().get().getType());
        assertEquals(9, slot.getGoods().get().getAmount());
    }

    @Test
    public void test_storeFromColonyWarehouse() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoods(GoodsType.SILK)).thenReturn(Goods.of(GoodsType.SILK, 120));

        slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 10), colony);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.SILK, slot.getGoods().get().getType());
        assertEquals(10, slot.getGoods().get().getAmount());

        verify(warehouse).removeGoods(Goods.of(GoodsType.SILK, 10));
    }

    @Test()
    public void test_storeFromColonyWarehouse_transferred_goodsType_isDifferent_from_alreadyStored()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.ORE, 22));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 10), colony);
                });

        assertTrue(
                exception.getMessage()
                        .contains("Tranfered cargo is diffrent type this is stored in slot."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test
    public void test_storeFromColonyWarehouse_transfered_good_is_added_to_already_stored()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.SILK, 22));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoods(GoodsType.SILK)).thenReturn(Goods.of(GoodsType.SILK, 120));

        slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 10), colony);

        verify(warehouse).removeGoods(Goods.of(GoodsType.SILK, 10));

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.SILK, slot.getGoods().get().getType());
        assertEquals(32, slot.getGoods().get().getAmount());
    }

    @Test
    public void test_storeFromColonyWarehouse_transfered_good_is_added_to_already_stored_2()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.SILK, 66));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoods(GoodsType.SILK)).thenReturn(Goods.of(GoodsType.SILK, 120));

        slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 50), colony);

        verify(warehouse).removeGoods(Goods.of(GoodsType.SILK, 34));
        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.SILK, slot.getGoods().get().getType());
        assertEquals(100, slot.getGoods().get().getAmount());
    }

    @Test
    public void test_removeCargo_null() throws Exception {
        assertThrows(NullPointerException.class, () -> slot.removeCargo(null));
    }

    @Test
    public void test_removeCargo_noGoods() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.removeCargo(Goods.of(GoodsType.CIGARS, 56)));

        assertEquals(
                "Cargo slot (CargoSlot{cargoUnit=null, cargoGoods=null}) doesn't contains any good.",
                exception.getMessage());
    }

    @Test
    public void test_removeCargo_storedGoodsHaveDifferentType() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.removeCargo(Goods.of(GoodsType.CIGARS, 56)));

        assertEquals(
                "Cargo (CargoSlot{cargoUnit=null, cargoGoods=Goods{GoodsType=CORN, amount=56}}) doesn't contains same type as was transfered (GoodsType{name=CIGARS}).",
                exception.getMessage());
    }

    @Test
    public void test_removeCargo_notEnoughtGoodsToRemove() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.removeCargo(Goods.of(GoodsType.CORN, 100)));

        assertEquals(
                "Can't remove more amount (100) than is stored (CargoSlot{cargoUnit=null, cargoGoods=Goods{GoodsType=CORN, amount=56}}).",
                exception.getMessage());
    }

    @Test
    public void test_removeCargo_removeAll() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        slot.removeCargo(Goods.of(GoodsType.CORN, 56));

        assertTrue(slot.isEmpty());
        assertFalse(slot.isLoadedGood());
        assertFalse(slot.isLoadedUnit());
    }

    @Test
    public void test_removeCargo_removePart() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        slot.removeCargo(Goods.of(GoodsType.CORN, 16));

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertFalse(slot.isLoadedUnit());
        assertEquals(40, slot.getGoods().get().getAmount());
    }

    @Test
    public void test_sellAndEmpty_goods_is_null() throws Exception {
        assertThrows(NullPointerException.class, () -> slot.sellAndEmpty(null));
    }

    @Test
    public void test_sellAndEmpty_slot_is_empty() throws Exception {
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> slot.sellAndEmpty(Goods.of(GoodsType.CORN, 56)));

        assertEquals("Cargo slot (CargoSlot{cargoUnit=null, cargoGoods=null}) is already empty.",
                exception.getMessage());
    }

    @Test
    public void test_sellAndEmpty_unit_is_loaded() throws Exception {
        loadUnit(unit);
        final IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> slot.sellAndEmpty(Goods.of(GoodsType.CORN, 56)));

        assertEquals(
                "Cargo slot (CargoSlot{cargoUnit=PlaceCargoSlot{unit id=0, ownerName=null}, cargoGoods=null}) doesn't contains goods.",
                exception.getMessage());
    }

    @Test
    public void test_sellAndEmpty_selling_different_goods_type_from_stored() throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 20));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.sellAndEmpty(Goods.of(GoodsType.CORN, 56)));

        assertEquals(
                "Cargo slot (CargoSlot{cargoUnit=null, cargoGoods=Goods{GoodsType=CIGARS, amount=20}}) doesn't contains correct goods type.",
                exception.getMessage());
    }

    @Test
    public void test_sellAndEmpty_selling_more_goods_than_is_stored() throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 10));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.sellAndEmpty(Goods.of(GoodsType.CIGARS, 20)));

        assertEquals(
                "Attempt to sell more goods Goods{GoodsType=CIGARS, amount=20} than is stored in CargoSlot{cargoUnit=null, cargoGoods=Goods{GoodsType=CIGARS, amount=10}}",
                exception.getMessage());
    }

    @Test
    public void test_sellAndEmpty_sell_all_goods() throws Exception {
        final Goods goods = new Goods(GoodsType.CIGARS, 10);
        slot = new CargoSlot(cargo, goods);
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);

        slot.sellAndEmpty(goods);

        assertTrue(slot.isEmpty());
        verify(player).sellGoods(goods);
    }

    @Test
    public void test_sellAndEmpty_sell_part_of_goods() throws Exception {
        final Goods goods = new Goods(GoodsType.CIGARS, 10);
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 20));
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);

        slot.sellAndEmpty(goods);

        assertTrue(slot.isLoadedGood());
        assertEquals(10, slot.getGoods().get().getAmount());
        verify(player).sellGoods(new Goods(GoodsType.CIGARS, 10));
    }

    @Test
    public void test_getAvailableCapacity_empty_slot() throws Exception {
        assertEquals(100, slot.getAvailableCapacity());
    }

    @Test
    public void test_getAvailableCapacity_contains_56_corn() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));

        assertEquals(44, slot.getAvailableCapacity());
    }

    @Test
    public void test_getAvailableCapacity_contains_100_corn() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 100));

        assertEquals(0, slot.getAvailableCapacity());
    }

    @Test
    public void test_buyAndStoreFromEuropePort_empty() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);

        final Goods ret = slot.buyAndStoreFromEuropePort(goods);

        assertEquals(goods, ret);
        assertEquals(goods, slot.getGoods().get());
        verify(player).buyGoods(goods);
    }

    @Test
    public void test_buyAndStoreFromEuropePort_empty_not_enough_gold() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);
        doThrow(new NotEnoughtGoldException(NOT_ENOUGH_GOLD)).when(player).buyGoods(goods);

        final NotEnoughtGoldException ret = assertThrows(NotEnoughtGoldException.class,
                () -> slot.buyAndStoreFromEuropePort(goods));

        assertTrue(slot.isEmpty());
        assertEquals(NOT_ENOUGH_GOLD, ret.getMessage());
    }

    @Test
    public void test_buyAndStoreFromEuropePort_loaded_with_20_cigars() throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 20));
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);

        final Goods ret = slot.buyAndStoreFromEuropePort(goods);

        assertEquals(goods, ret);
        assertEquals(goods.setAmount(54), slot.getGoods().get());
        verify(player).buyGoods(goods);
    }

    @Test
    public void test_buyAndStoreFromEuropePort_loaded_with_20_cigars_not_enough_gold()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 20));
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);
        doThrow(new NotEnoughtGoldException(NOT_ENOUGH_GOLD)).when(player).buyGoods(goods);

        final NotEnoughtGoldException ret = assertThrows(NotEnoughtGoldException.class,
                () -> slot.buyAndStoreFromEuropePort(goods));

        assertEquals(NOT_ENOUGH_GOLD, ret.getMessage());
        assertEquals(new Goods(GoodsType.CIGARS, 20), slot.getGoods().get());
    }

    @Test
    public void test_buyAndStoreFromEuropePort_loaded_with_100_cigars() throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 100));
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);

        final Goods ret = slot.buyAndStoreFromEuropePort(goods);

        assertEquals(goods.setAmount(0), ret);
        assertEquals(goods.setAmount(100), slot.getGoods().get());
    }

    @Test
    public void test_buyAndStoreFromEuropePort_loaded_with_80_cigars() throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 80));
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);

        final Goods ret = slot.buyAndStoreFromEuropePort(goods);

        assertEquals(goods.setAmount(20), ret);
        assertEquals(goods.setAmount(100), slot.getGoods().get());
        verify(player).buyGoods(goods.setAmount(20));
    }

    @Test
    public void test_buyAndStoreFromEuropePort_loaded_with_80_cigars_not_enough_gold()
            throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CIGARS, 80));
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);
        doThrow(new NotEnoughtGoldException(NOT_ENOUGH_GOLD)).when(player)
                .buyGoods(goods.setAmount(20));

        final NotEnoughtGoldException ret = assertThrows(NotEnoughtGoldException.class,
                () -> slot.buyAndStoreFromEuropePort(goods));

        assertEquals(NOT_ENOUGH_GOLD, ret.getMessage());
        assertEquals(new Goods(GoodsType.CIGARS, 80), slot.getGoods().get());
    }

    @Test
    public void test_buyAndStoreFromEuropePort_loaded_with_20_corn() throws Exception {
        slot = new CargoSlot(cargo, new Goods(GoodsType.CORN, 20));
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);

        final IllegalArgumentException ret = assertThrows(IllegalArgumentException.class,
                () -> slot.buyAndStoreFromEuropePort(goods));

        assertTrue(ret.getMessage().contains("Tranfered cargo is diffrent type this is stored in"));
    }

    @Test
    public void test_buyAndStoreFromEuropePort_loaded_with_unit() throws Exception {
        loadUnit(unit);
        final Goods goods = new Goods(GoodsType.CIGARS, 34);

        final IllegalArgumentException ret = assertThrows(IllegalArgumentException.class,
                () -> slot.buyAndStoreFromEuropePort(goods));

        assertEquals("Attempt to move cargo to slot occupied with unit.", ret.getMessage());
    }

    private void loadUnit(final Unit unit) {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(unit.getOwner()).thenReturn(player);

        slot.store(unit);
    }

    @BeforeEach
    private void beforeEach() {
        slot = new CargoSlot(cargo);
    }

    @AfterEach
    private void afterEach() {
        slot = null;
    }

}
