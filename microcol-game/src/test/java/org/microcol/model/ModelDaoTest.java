package org.microcol.model;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.model.store.ModelDao;

import com.google.gson.JsonSyntaxException;

public class ModelDaoTest {
	
	private ModelDao dao;
	
	@Test(expected = JsonSyntaxException.class)
	public void test_invalid_file_format() {
		dao.loadPredefinedModel("/maps/test-map-invalid-0x0.json");
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_load_missingFile() {
		dao.loadPredefinedModel("/maps/test-map-missing-0x0.json");
	}

	@Test(expected = NullPointerException.class)
	public void testValidLocationNull() {
		WorldMap map = dao.loadPredefinedWorldMap("/maps/test-map-ocean-10x10.json");

		map.isValid((Location) null);
	}

	@Test(expected = NullPointerException.class)
	public void testValidPathNull() {
		WorldMap map = dao.loadPredefinedWorldMap("/maps/test-map-ocean-10x10.json");

		map.isValid((Path) null);
	}
	
	@Before
	public void before() {
		dao = new ModelDao();
	}

	@After
	public void after(){
		dao = null;
	}
}
