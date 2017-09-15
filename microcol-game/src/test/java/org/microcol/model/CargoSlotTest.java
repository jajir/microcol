package org.microcol.model;

import static org.junit.Assert.assertSame;

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

	@Test
	public void test_storeFromCargoSlot_(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		new Expectations() {{
			cargo.getOwner(); result = ownerUnit;
			ownerUnit.getOwner(); result = player;
			sourceCargoSlot.getGoods(); result = Optional.of(new GoodAmount(GoodType.CORN, 100));
		}};
		
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 100),sourceCargoSlot);
		
		new Verifications() {{
			sourceCargoSlot.empty();
		}};
	}

	@Test(expected = IllegalStateException.class)
	public void test_storeFromCargoSlot_not_same_owner(final @Injectable CargoSlot sourceCargoSlot) throws Exception {
		slot.storeFromCargoSlot(new GoodAmount(GoodType.CORN, 100), sourceCargoSlot);
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
