package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;
import org.microcol.model.store.CalendarPo;

public class CalendarStoreTest {


    @Test
    public void test_save() throws Exception {
	final Calendar cal = new Calendar(100, 999, 0);
        CalendarPo data = cal.save();

        assertEquals(100, data.getStartYear());
        assertEquals(999, data.getEndYear());
    }

    @Test
    public void test_make() throws Exception {
	final Calendar cal = new Calendar(100, 999, 0);
        Calendar tst = Calendar.make(cal.save());

        assertEquals(100, tst.getStartYear());
        assertEquals(999, tst.getEndYear());
        assertEquals(100, tst.getCurrentYear());
    }
    
    @Test
    public void testCreation() throws Exception {
	final Calendar cal = new Calendar(1630, 1800, 0);
	
        assertEquals(1630, cal.getStartYear());
        assertEquals(1800, cal.getEndYear());
        assertEquals(1630, cal.getCurrentYear());
        assertTrue(cal.getCurrentSeason().isPresent());
        assertEquals(Calendar.Season.spring, cal.getCurrentSeason().get());
    }

}
