package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;

import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class CargoSlotTest {

	@Tested
	private CargoSlot slot;

	@Injectable
	private Cargo cargo;

	@Test
	public void test_store_goods() throws Exception {
		slot.store(new GoodAmmount(GoodType.CORN, 100));
	}

	@Test(expected = IllegalStateException.class)
	public void test_store_goods_anotherOne() throws Exception {
		slot.store(new GoodAmmount(GoodType.CORN, 100));

		slot.store(new GoodAmmount(GoodType.CORN, 100));
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

}
