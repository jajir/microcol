package org.microcol.model;

import static org.junit.Assert.*;

import java.util.Optional;

import org.junit.Test;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

public class CargoSlotTest {

	@Tested
	private CargoSlot slot;

	@Injectable
	private Cargo cargo;
	
	private @Mocked Player player;
	
	private @Mocked Unit ownerUnit;

	@Test(expected = NullPointerException.class)
	public void test_storeFromCargoSlot_null_goodAmount(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		slot.storeFromCargoSlot(null, sourceCargoSlot);
	}

	@Test(expected = NullPointerException.class)
	public void test_storeFromCargoSlot_null_sourceCargoSlot() throws Exception {
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 100), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromCargoSlot_not_same_owner(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		new Expectations() {{
			cargo.getOwner(); result = ownerUnit;
			ownerUnit.getOwner(); result = player;
			sourceCargoSlot.getOwnerPlayer(); result = null; times = 2;
		}};
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 100), sourceCargoSlot);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromCargoSlot_already_occupied_by_unit(final @Injectable CargoSlot sourceCargoSlot, final @Injectable Unit unit) throws Exception {
		slot.store(unit);
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 100), sourceCargoSlot);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromCargoSlot_source_have_to_contain_goods(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		new Expectations() {{
			cargo.getOwner(); result = ownerUnit;
			ownerUnit.getOwner(); result = player;
			sourceCargoSlot.getOwnerPlayer(); result = player; times = 2;
			sourceCargoSlot.getGoods(); result = Optional.empty();
		}};
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 13), sourceCargoSlot);		
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromCargoSlot_transferred_goodType_isDifferent_from_source(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		new Expectations() {{
			cargo.getOwner(); result = ownerUnit;
			ownerUnit.getOwner(); result = player;
			sourceCargoSlot.getOwnerPlayer(); result = player; times = 2;
			sourceCargoSlot.getGoods(); result = Optional.of(new GoodAmount(GoodType.CIGARS, 13));
		}};
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 13), sourceCargoSlot);		
	}
	
	@Test
	public void test_storeFromCargoSlot_transfer_all(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		new Expectations() {{
			cargo.getOwner(); result = ownerUnit;
			ownerUnit.getOwner(); result = player;
			sourceCargoSlot.getOwnerPlayer(); result = player; times = 2;
			sourceCargoSlot.getGoods(); result = Optional.of(new GoodAmount(GoodType.CORN, 13));
		}};
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 13), sourceCargoSlot);		
		
		assertFalse(slot.isEmpty());
		assertTrue(slot.isLoadedGood());
		assertEquals(GoodType.CORN, slot.getGoods().get().getGoodType());
		assertEquals(13, slot.getGoods().get().getAmount());
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromCargoSlot_transferred_goodType_isDifferent_from_alreadyStored(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		slot = new CargoSlot(cargo, new GoodAmount(GoodType.CIGARS, 13));
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 13), sourceCargoSlot);		
	}
	
	@Test
	public void test_storeFromCargoSlot_transfered_good_is_added_to_already_stored_1(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		slot = new CargoSlot(cargo, new GoodAmount(GoodType.CORN, 30));
		new Expectations() {{
			cargo.getOwner(); result = ownerUnit;
			ownerUnit.getOwner(); result = player;
			sourceCargoSlot.getOwnerPlayer(); result = player; times = 2;
			sourceCargoSlot.getGoods(); result = Optional.of(new GoodAmount(GoodType.CORN, 13));
		}};
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 13), sourceCargoSlot);		
		
		assertFalse(slot.isEmpty());
		assertTrue(slot.isLoadedGood());
		assertEquals(GoodType.CORN, slot.getGoods().get().getGoodType());
		assertEquals(43, slot.getGoods().get().getAmount());
		
		new Verifications() {{
			sourceCargoSlot.removeGoodsAmount(13);
		}};
	}

	@Test
	public void test_storeFromCargoSlot_transfered_good_is_added_to_already_stored_2(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		slot = new CargoSlot(cargo, new GoodAmount(GoodType.CORN, 30));
		new Expectations() {{
			cargo.getOwner(); result = ownerUnit;
			ownerUnit.getOwner(); result = player;
			sourceCargoSlot.getOwnerPlayer(); result = player; times = 2;
			sourceCargoSlot.getGoods(); result = Optional.of(new GoodAmount(GoodType.CORN, 13));
		}};
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 80), sourceCargoSlot);		
		
		assertFalse(slot.isEmpty());
		assertTrue(slot.isLoadedGood());
		assertEquals(GoodType.CORN, slot.getGoods().get().getGoodType());
		assertEquals(43, slot.getGoods().get().getAmount());
		
		new Verifications() {{
			sourceCargoSlot.removeGoodsAmount(13);
		}};
	}

	@Test
	public void test_store_unit(@Mocked Unit unit) throws Exception {
		slot.store(unit);

		assertSame(unit, slot.getUnit().get());
	}

	@Test(expected = IllegalStateException.class)
	public void test_store_unit_another_one(@Mocked Unit unit) throws Exception {
		slot.store(unit);

		slot.store(unit);
	}

	@Test(expected = NullPointerException.class)
	public void test_storeFromColonyWarehouse_good_ammount_cant_be_null(final @Mocked Colony colony) throws Exception {
		slot.storeFromColonyWarehouse(null, colony);
	}

	@Test(expected = NullPointerException.class)
	public void test_storeFromColonyWarehouse_colony_cant_be_null() throws Exception {
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 10), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromColonyWarehouse_colony_have_different_owner(final @Mocked Colony colony) throws Exception {
		new Expectations() {{
			colony.getOwner(); result = null;
		}};
		
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 10), colony);
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromColonyWarehouse_is_allready_occupied_by_unit(final @Mocked Colony colony, final @Mocked Unit unit) throws Exception {
		slot.store(unit);
		
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 10), colony);
	}

	@Test()
	public void test_storeFromColonyWarehouse_warehouse_limit_transferring_goods_amount(final @Mocked Colony colony, final @Mocked ColonyWarehouse warehouse) throws Exception {
		new Expectations() {{
			colony.getOwner(); result = player;
			colony.getColonyWarehouse(); result = warehouse;
			warehouse.getGoodAmmount(GoodType.SILK); result = 9;
		}};
		
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 10), colony);
		
		assertFalse(slot.isEmpty());
		assertTrue(slot.isLoadedGood());
		assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
		assertEquals(9, slot.getGoods().get().getAmount());
	}

	@Test
	public void test_storeFromColonyWarehouse(final @Mocked Colony colony, final @Mocked ColonyWarehouse warehouse) throws Exception {
		new Expectations() {{
			colony.getOwner(); result = player;
			colony.getColonyWarehouse(); result = warehouse;
			warehouse.getGoodAmmount(GoodType.SILK); result = 120;
			warehouse.removeFromWarehouse(GoodType.SILK, 10); times = 1;
		}};
		
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 10), colony);
		
		assertFalse(slot.isEmpty());
		assertTrue(slot.isLoadedGood());
		assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
		assertEquals(10, slot.getGoods().get().getAmount());
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_storeFromColonyWarehouse_transferred_goodType_isDifferent_from_alreadyStored(final @Mocked Colony colony) throws Exception {
		slot = new CargoSlot(cargo, new GoodAmount(GoodType.ORE, 22));
		
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 10), colony);
	}
	
	@Test
	public void test_storeFromColonyWarehouse_transfered_good_is_added_to_already_stored(final @Mocked Colony colony, final @Mocked ColonyWarehouse warehouse) throws Exception {
		slot = new CargoSlot(cargo, new GoodAmount(GoodType.SILK, 22));
		new Expectations() {{
			colony.getOwner(); result = player;
			colony.getColonyWarehouse(); result = warehouse;
			warehouse.getGoodAmmount(GoodType.SILK); result = 120;
			warehouse.removeFromWarehouse(GoodType.SILK, 10); times = 1;
		}};
		
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 10), colony);
		
		assertFalse(slot.isEmpty());
		assertTrue(slot.isLoadedGood());
		assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
		assertEquals(32, slot.getGoods().get().getAmount());
	}
	
	@Test
	public void test_storeFromColonyWarehouse_transfered_good_is_added_to_already_stored_2(final @Mocked Colony colony, final @Mocked ColonyWarehouse warehouse) throws Exception {
		slot = new CargoSlot(cargo, new GoodAmount(GoodType.SILK, 66));
		new Expectations() {{
			colony.getOwner(); result = player;
			colony.getColonyWarehouse(); result = warehouse;
			warehouse.getGoodAmmount(GoodType.SILK); result = 120;
			warehouse.removeFromWarehouse(GoodType.SILK, 34); times = 1;
		}};
		
		slot.storeFromColonyWarehouse(new GoodAmount(GoodType.SILK, 50), colony);
		
		assertFalse(slot.isEmpty());
		assertTrue(slot.isLoadedGood());
		assertEquals(GoodType.SILK, slot.getGoods().get().getGoodType());
		assertEquals(100, slot.getGoods().get().getAmount());
	}
	
}
