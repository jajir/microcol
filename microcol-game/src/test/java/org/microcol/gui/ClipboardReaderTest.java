package org.microcol.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.microcol.gui.util.ClipboardReader;
import org.microcol.model.GoodType;
import org.microcol.model.Model;
import org.microcol.model.Unit;

import javafx.scene.input.Dragboard;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;

@RunWith(JMockit.class)
public class ClipboardReaderTest {

	private @Mocked Model model;

	private @Mocked Dragboard db;

	@Test
	public void test_readUnit(final @Mocked Unit unit) throws Exception {
		new Expectations() {{
			db.getString(); result = "Unit,45";
			model.getUnitById(45); result = unit;
		}};
		
		ClipboardReader.make(model, db).readUnit(u -> {
			assertNotNull(u);
			assertSame(unit, u);
		});
	}

	@Test(expected = IllegalStateException.class)
	public void test_readUnit_missing_unitId(final @Mocked Unit unit) throws Exception {
		new Expectations() {{
			db.getString(); result = "Unit";
		}};
		
		ClipboardReader.make(model, db).readUnit(u -> {
			assertNotNull(u);
			assertSame(unit, u);
		});
	}

	@Test(expected = IllegalStateException.class)
	public void test_readUnit_invalid_keyword(final @Mocked Unit unit) throws Exception {
		new Expectations() {{
			db.getString(); result = "UniT,76";
		}};
		
		ClipboardReader.make(model, db).readUnit(u -> {
			assertNotNull(u);
			assertSame(unit, u);
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_readUnit_unitId_not_a_number(final @Mocked Unit unit) throws Exception {
		new Expectations() {{
			db.getString(); result = "Unit,blee";
		}};
		
		ClipboardReader.make(model, db).readUnit(u -> {
			assertNotNull(u);
			assertSame(unit, u);
		});
	}

	@Test(expected = NullPointerException.class)
	public void test_make_missing_keyword() throws Exception {
		new Expectations() {{
			db.getString(); result = null;
		}};
		
		ClipboardReader.make(model, db);
	}

	@Test(expected = IllegalStateException.class)
	public void test_make_empty_keyword() throws Exception {
		new Expectations() {{
			db.getString(); result = "";
		}};
		
		ClipboardReader.make(model, db);
	}

	@Test
	public void test_readGoods() throws Exception {
		new Expectations() {{
			db.getString(); result = "Goods,COTTON,75";
		}};
		
		ClipboardReader.make(model, db).readGood(goods -> {
			assertNotNull(goods);
			assertEquals(GoodType.COTTON, goods.getGoodType());
			assertEquals(75, goods.getAmmount());
		});
	}

	@Test(expected = IllegalStateException.class)
	public void test_readGoods_invalid_length() throws Exception {
		new Expectations() {{
			db.getString(); result = "Goods,COTTON";
		}};
		
		ClipboardReader.make(model, db).readGood(goods -> {
			assertNotNull(goods);
			assertEquals(GoodType.COTTON, goods.getGoodType());
			assertEquals(75, goods.getAmmount());
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_readGoods_ammount_is_not_a_number() throws Exception {
		new Expectations() {{
			db.getString(); result = "Goods,COTTON,blee";
		}};
		
		ClipboardReader.make(model, db).readGood(goods -> {
			assertNotNull(goods);
			assertEquals(GoodType.COTTON, goods.getGoodType());
			assertEquals(75, goods.getAmmount());
		});
	}


	@Test(expected = IllegalArgumentException.class)
	public void test_readGoods_invalid_goodType() throws Exception {
		new Expectations() {{
			db.getString(); result = "Goods,GOLD,45";
		}};
		
		ClipboardReader.make(model, db).readGood(goods -> {
			assertNotNull(goods);
			assertEquals(GoodType.COTTON, goods.getGoodType());
			assertEquals(75, goods.getAmmount());
		});
	}
	

	@Test(expected = IllegalStateException.class)
	public void test_readGoods_invalid_keyword() throws Exception {
		new Expectations() {{
			db.getString(); result = "GooDS,COTTON,67";
		}};
		
		ClipboardReader.make(model, db).readGood(goods -> {
			assertNotNull(goods);
			assertEquals(GoodType.COTTON, goods.getGoodType());
			assertEquals(75, goods.getAmmount());
		});
	}

	
}
