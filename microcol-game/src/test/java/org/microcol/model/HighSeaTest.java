package org.microcol.model;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class HighSeaTest {

	@Tested
	private HighSea highSea;

	@Injectable
	private Model model;

	@Test
	public void test_getHighSeasAll(@Mocked Unit unit1, @Mocked PlaceHighSea placeHighSea1) throws Exception {
		new Expectations() {{
			model.getAllUnits(); result=Lists.newArrayList(unit1);
			unit1.getPlace(); result=placeHighSea1;
		}};
		List<PlaceHighSea> ret = highSea.getHighSeasAll();

		assertNotNull(ret);
		assertEquals(1, ret.size());
	}

	@Test
	public void test_getHighSeasAll_two(@Mocked Unit unit1, @Mocked PlaceHighSea placeHighSea1,
			@Mocked Unit unit2, @Mocked PlaceHighSea placeHighSea2) throws Exception {
		new Expectations() {{
			model.getAllUnits(); result=Lists.newArrayList(unit1, unit2);
			unit1.getPlace(); result=placeHighSea1;
			unit2.getPlace(); result=placeHighSea2;
		}};
		List<PlaceHighSea> ret = highSea.getHighSeasAll();

		assertNotNull(ret);
		assertEquals(2, ret.size());
	}

}
