package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.microcol.model.store.CalendarPo;

public class CalendarStoreTest {

	private Calendar cal = new Calendar(100, 999, 138);
	
	
	@Test
	public void test_save() throws Exception {
		CalendarPo data = cal.save();
		
		assertEquals(100, data.getStartYear());
		assertEquals(999, data.getEndYear());
		assertEquals(138, data.getCurrentYear());
	}
	
	@Test
	public void test_make() throws Exception {
		Calendar tst = Calendar.make(cal.save());
		
		assertEquals(100, tst.getStartYear());
		assertEquals(999, tst.getEndYear());
		assertEquals(138, tst.getCurrentYear());
	}

}
