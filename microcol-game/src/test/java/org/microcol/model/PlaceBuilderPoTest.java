package org.microcol.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

public class PlaceBuilderPoTest {

	@Tested
	private PlaceBuilderPo placeBuilder;
	
	@Injectable
	private UnitPo unitPo;
	
	@Injectable
	private ModelPo modelPo;
	
	@Injectable
	private Model model;
	
	@Test(expected=IllegalArgumentException.class)
	public void test_build_verify_thatAllBuilderAreCalled(final @Mocked Unit unit) throws Exception {
		new Expectations() {{
			unitPo.getPlaceMap(); result = null;
			unitPo.getPlaceHighSeas(); result = null;
			unitPo.getPlaceEuropePort(); result = null;
		}};		
		
		placeBuilder.build(unit);
		
		new Verifications() {{
			unitPo.getPlaceMap(); times = 1;
			unitPo.getPlaceHighSeas(); times = 1;
			unitPo.getPlaceEuropePort(); times = 1;
		}};		
	}
	
	@Test
	public void test_build_placeMap(final @Mocked Unit unit) throws Exception {
		final PlaceMapPo placeMapPo = new PlaceMapPo();
		placeMapPo.setLocation(Location.of(3, 4));
		new Expectations() {{
			unitPo.getPlaceMap(); result = placeMapPo;
		}};		
		final Place ret = placeBuilder.build(unit);
		
		assertNotNull(ret);
		assertTrue(ret instanceof PlaceLocation);
		PlaceLocation placeLoc = (PlaceLocation)ret;
		assertEquals(Location.of(3, 4), placeLoc.getLocation());
	}

	@Test
	public void test_build_cargo(final @Mocked Unit unit) throws Exception {
		final PlaceMapPo placeMapPo = new PlaceMapPo();
		placeMapPo.setLocation(Location.of(3, 4));
		new Expectations() {{
			unitPo.getPlaceMap(); result = placeMapPo;
		}};		
		final Place ret = placeBuilder.build(unit);
		
		assertNotNull(ret);
		assertTrue(ret instanceof PlaceLocation);
		PlaceLocation placeLoc = (PlaceLocation)ret;
		assertEquals(Location.of(3, 4), placeLoc.getLocation());
	}
	
	public void setup() {
		placeBuilder = new PlaceBuilderPo(unitPo, modelPo, model);
	}
	
	@After
	public void tearDown(){
		placeBuilder = null;
	}
}
