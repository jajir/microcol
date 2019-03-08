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
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
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
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
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
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
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
        when(colony.getColonyWarehouse()).thenReturn(warehouse);
        when(warehouse.getGoods(GoodsType.SILK)).thenReturn(Goods.of(GoodsType.SILK, 120));

        slot.storeFromColonyWarehouse(new Goods(GoodsType.SILK, 50), colony);

        verify(warehouse).removeGoods(Goods.of(GoodsType.SILK, 34));
        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertEquals(GoodsType.SILK, slot.getGoods().get().getType());
        assertEquals(100, slot.getGoods().get().getAmount());
    }

    @Test
    void test_removeCargo_null() throws Exception {
        assertThrows(NullPointerException.class, () -> slot.removeCargo(null));
    }

    @Test
    void test_removeCargo_noGoods() throws Exception {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.removeCargo(Goods.of(GoodsType.CIGARS, 56)));

        assertEquals(
                "Cargo slot (CargoSlot{cargoUnit=null, cargoGoods=null}) doesn't contains any good.",
                exception.getMessage());
    }

    @Test
    void test_removeCargo_storedGoodsHaveDifferentType() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.removeCargo(Goods.of(GoodsType.CIGARS, 56)));

        assertEquals(
                "Cargo (CargoSlot{cargoUnit=null, cargoGoods=Goods{GoodsType=CORN, amount=56}}) doesn't contains same type as was transfered (GoodsType{name=CIGARS}).",
                exception.getMessage());
    }

    @Test
    void test_removeCargo_notEnoughtGoodsToRemove() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> slot.removeCargo(Goods.of(GoodsType.CORN, 100)));

        assertEquals(
                "Can't remove more amount (100) than is stored (CargoSlot{cargoUnit=null, cargoGoods=Goods{GoodsType=CORN, amount=56}}).",
                exception.getMessage());
    }

    @Test
    void test_removeCargo_removeAll() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        slot.removeCargo(Goods.of(GoodsType.CORN, 56));

        assertTrue(slot.isEmpty());
        assertFalse(slot.isLoadedGood());
        assertFalse(slot.isLoadedUnit());
    }

    @Test
    void test_removeCargo_removePart() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        slot.removeCargo(Goods.of(GoodsType.CORN, 16));

        assertFalse(slot.isEmpty());
        assertTrue(slot.isLoadedGood());
        assertFalse(slot.isLoadedUnit());
        assertEquals(40, slot.getGoods().get().getAmount());
    }
    
    @Test
    void test_getAvailableCapacity_empty_slot() throws Exception {
        assertEquals(100, slot.getAvailableCapacity());
    }
    
    @Test
    void test_getAvailableCapacity_contains_56_corn() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 56));
        
        assertEquals(44, slot.getAvailableCapacity());
    }
    
    @Test
    void test_getAvailableCapacity_contains_100_corn() throws Exception {
        slot.addGoods(Goods.of(GoodsType.CORN, 100));
        
        assertEquals(0, slot.getAvailableCapacity());
    }
    
    @Test
    void test_getAvailableCapacity_contains_unit() throws Exception {
        final PlaceCargoSlot placeCargoSlot = mock(PlaceCargoSlot.class);
        slot.unsafeStore(placeCargoSlot);
        
        assertEquals(0, slot.getAvailableCapacity());
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
