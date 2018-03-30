package org.microcol.gui;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.microcol.gui.util.ClipboardWritter;
import org.microcol.model.CargoSlot;
import org.microcol.model.GoodsAmount;
import org.microcol.model.GoodType;
import org.microcol.model.Unit;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class ClipboardWritterTest {

	private @Mocked Dragboard db;
	
	@Test
	public void test_addUnit(final @Mocked Unit unit) throws Exception {
		new Expectations() {{
			unit.getId(); result = 12;
		}};
		
		ClipboardWritter.make(db).addUnit(unit).build();
		
		new Verifications() {{
			ClipboardContent tested;
			db.setContent(tested = withCapture());
			final String stored = tested.getString();
			assertEquals("Unit,12", stored);
	    }};
	}
	
	@Test
	public void test_addGood(final @Mocked GoodsAmount good) throws Exception {
		new Expectations() {{
			good.getAmount(); result = 100;
			good.getGoodType(); result = GoodType.COTTON;
		}};
		
		ClipboardWritter.make(db).addGoodAmount(good).build();
		
		new Verifications() {{
			ClipboardContent tested;
			db.setContent(tested = withCapture());
			final String stored = tested.getString();
			assertEquals("Goods,COTTON,100", stored);
	    }};
	}
	
	@Test(expected = IllegalStateException.class)
	public void test_addUnit_fromEuropePortPier_transferFrom_is_called_after_transfer(final @Mocked Unit unit)
			throws Exception {
		ClipboardWritter.make(db).addUnit(unit).addTransferFromEuropePortPier().build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_addGood_fromEuropePortPier(final @Mocked GoodsAmount good) throws Exception {
		ClipboardWritter.make(db).addTransferFromEuropePortPier().addGoodAmount(good).build();
	}
	
	@Test
	public void test_addUnit_fromEuropePortPier(final @Mocked Unit unit) throws Exception {
		new Expectations() {{
			unit.getId(); result = 12;
		}};
		
		ClipboardWritter.make(db).addTransferFromEuropePortPier().addUnit(unit).build();
		
		new Verifications() {{
			ClipboardContent tested;
			db.setContent(tested = withCapture());
			final String stored = tested.getString();
			assertEquals("Unit,12,FromEuropePortPier", stored);
	    }};
	}
	
	@Test
	public void test_addGood_fromCargoUnit(final @Mocked GoodsAmount good, final @Mocked Unit unit,
			final @Mocked CargoSlot cargoSlot) throws Exception {
		new Expectations() {{
			good.getAmount(); result = 100;
			good.getGoodType(); result = GoodType.COTTON;
			unit.getId(); result = 45;
			cargoSlot.getIndex(); result = 2;
		}};
		
		ClipboardWritter.make(db).addTransferFromUnit(unit, cargoSlot).addGoodAmount(good).build();
		
		new Verifications() {{
			ClipboardContent tested;
			db.setContent(tested = withCapture());
			final String stored = tested.getString();
			assertEquals("Goods,COTTON,100,FromUnit,45,2", stored);
	    }};
	}
	
	@Test
	public void test_addGood_fromEuropeShop(final @Mocked GoodsAmount good) throws Exception {
		new Expectations() {{
			good.getAmount(); result = 100;
			good.getGoodType(); result = GoodType.COTTON;
		}};
		
		ClipboardWritter.make(db).addTransferFromEuropeShop().addGoodAmount(good).build();
		
		new Verifications() {{
			ClipboardContent tested;
			db.setContent(tested = withCapture());
			final String stored = tested.getString();
			assertEquals("Goods,COTTON,100,FromEuropeShop", stored);
	    }};
	}
	
	
	@Test(expected = IllegalStateException.class)
	public void test_addBoth(final @Mocked Unit unit, final @Mocked GoodsAmount good) throws Exception {
		
		ClipboardWritter.make(db).addUnit(unit).addGoodAmount(good).build();
	}
	
}
