package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CargoSlotTest {

    private CargoSlot slot;

    private final Cargo cargo = mock(Cargo.class);

    private final Player player = mock(Player.class);

    private final Unit ownerUnit = mock(Unit.class);

    private final CargoSlot sourceCargoSlot = mock(CargoSlot.class);

    private final Unit unit = mock(Unit.class);

    private final Colony colony = mock(Colony.class);

    private final ColonyWarehouse warehouse = mock(ColonyWarehouse.class);

    @Test
    public void test_storeFromCargoSlot_null_goodAmount() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            slot.storeFromCargoSlot(null, sourceCargoSlot);
        });
    }

    @Test()
    public void test_storeFromCargoSlot_null_sourceCargoSlot() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 100), null);
        });
    }

    @Test()
    public void test_storeFromCargoSlot_not_same_owner() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(null);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 100), sourceCargoSlot);
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
                    slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 100), sourceCargoSlot);
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
                    slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 13), sourceCargoSlot);
                });

        assertTrue(exception.getMessage().contains("Source cargo slot doesn't contains any good"),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test()
    public void test_storeFromCargoSlot_transferred_goodType_isDifferent_from_source()
            throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods())
                .thenReturn(Optional.of(new GoodsAmount(GoodType.CIGARS, 13)));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 13), sourceCargoSlot);
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
        when(sourceCargoSlot.getGoods())
                .thenReturn(Optional.of(new GoodsAmount(GoodType.CORN, 13)));

        slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 13), sourceCargoSlot);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodType.CORN, slot.getGoods().get().getGoodType());
        assertEquals(13, slot.getGoods().get().getAmount());
    }

    @Test()
    public void test_storeFromCargoSlot_transferred_goodType_isDifferent_from_alreadyStored()
            throws Exception {
        slot = new CargoSlot(cargo, new GoodsAmount(GoodType.CIGARS, 13));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 13), sourceCargoSlot);
                });

        assertTrue(
                exception.getMessage()
                        .contains("Tranfered cargo is diffrent type this is stored in slot."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test
    public void test_storeFromCargoSlot_transfered_good_is_added_to_already_stored_1()
            throws Exception {
        slot = new CargoSlot(cargo, new GoodsAmount(GoodType.CORN, 30));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods())
                .thenReturn(Optional.of(new GoodsAmount(GoodType.CORN, 13)));

        slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 13), sourceCargoSlot);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodType.CORN, slot.getGoods().get().getGoodType());
        assertEquals(43, slot.getGoods().get().getAmount());

        verify(sourceCargoSlot).removeGoodsAmount(13);
    }

    @Test
    public void test_storeFromCargoSlot_transfered_good_is_added_to_already_stored_2()
            throws Exception {
        slot = new CargoSlot(cargo, new GoodsAmount(GoodType.CORN, 30));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(sourceCargoSlot.getOwnerPlayer()).thenReturn(player);
        when(sourceCargoSlot.getGoods())
                .thenReturn(Optional.of(new GoodsAmount(GoodType.CORN, 13)));

        slot.storeFromCargoSlot(new GoodsAmount(GoodType.CORN, 80), sourceCargoSlot);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodType.CORN, slot.getGoods().get().getGoodType());
        assertEquals(43, slot.getGoods().get().getAmount());

        verify(sourceCargoSlot).removeGoodsAmount(13);
    }

    @Test
    public void test_store_unit() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(unit.getOwner()).thenReturn(player);

        slot.store(unit);

        assertSame(unit, slot.getUnit().get());
    }

    @Test()
    public void test_store_unit_another_one() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(unit.getOwner()).thenReturn(player);

        slot.store(unit);

        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            slot.store(unit);
        });

        assertTrue(exception.getMessage().contains("is already loaded."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
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
            slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 10), null);
        });
    }

    @Test()
    public void test_storeFromColonyWarehouse_colony_have_different_owner() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(null);

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 10), colony);
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
                    slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 10), colony);
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
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoodAmmount(GoodType.SILK)).thenReturn(9);

        slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 10), colony);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
        assertEquals(9, slot.getGoods().get().getAmount());
    }

    @Test
    public void test_storeFromColonyWarehouse() throws Exception {
        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoodAmmount(GoodType.SILK)).thenReturn(120);

        slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 10), colony);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
        assertEquals(10, slot.getGoods().get().getAmount());

        verify(warehouse).removeFromWarehouse(GoodType.SILK, 10);
    }

    @Test()
    public void test_storeFromColonyWarehouse_transferred_goodType_isDifferent_from_alreadyStored()
            throws Exception {
        slot = new CargoSlot(cargo, new GoodsAmount(GoodType.ORE, 22));

        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> {
                    slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 10), colony);
                });

        assertTrue(
                exception.getMessage()
                        .contains("Tranfered cargo is diffrent type this is stored in slot."),
                String.format("Invalid exception message '%s'", exception.getMessage()));
    }

    @Test
    public void test_storeFromColonyWarehouse_transfered_good_is_added_to_already_stored()
            throws Exception {
        slot = new CargoSlot(cargo, new GoodsAmount(GoodType.SILK, 22));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoodAmmount(GoodType.SILK)).thenReturn(120);

        slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 10), colony);

        verify(warehouse).removeFromWarehouse(GoodType.SILK, 10);

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
        assertEquals(32, slot.getGoods().get().getAmount());
    }

    @Test
    public void test_storeFromColonyWarehouse_transfered_good_is_added_to_already_stored_2()
            throws Exception {
        slot = new CargoSlot(cargo, new GoodsAmount(GoodType.SILK, 66));

        when(cargo.getOwner()).thenReturn(ownerUnit);
        when(ownerUnit.getOwner()).thenReturn(player);
        when(colony.getOwner()).thenReturn(player);
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoodAmmount(GoodType.SILK)).thenReturn(120);

        slot.storeFromColonyWarehouse(new GoodsAmount(GoodType.SILK, 50), colony);

        verify(warehouse).removeFromWarehouse(GoodType.SILK, 34);
        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
        assertEquals(100, slot.getGoods().get().getAmount());
    }

    @BeforeEach
    public void startUp() {
        slot = new CargoSlot(cargo);
    }

    @AfterEach
    public void tearDown() {
        slot = null;
    }

}
