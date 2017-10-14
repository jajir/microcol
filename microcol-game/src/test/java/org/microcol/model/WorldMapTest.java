package org.microcol.model;

import org.junit.Test;
import org.microcol.model.store.WorldMapDao;

import com.google.gson.JsonSyntaxException;

public class WorldMapTest {
	
	@Test(expected = JsonSyntaxException.class)
	public void test_invalid_file_format() {
		WorldMapDao dao = new WorldMapDao();
		dao.loadMap("/maps/test-map-invalid-0x0.json");
	}

	@Test(expected = IllegalArgumentException.class)
	public void test_load_missingFile() {
		WorldMapDao dao = new WorldMapDao();
		dao.loadMap("/maps/test-map-missing-0x0.json");
	}

	@Test(expected = NullPointerException.class)
	public void testValidLocationNull() {
		WorldMapDao dao = new WorldMapDao();
		WorldMap map = dao.loadMap("/maps/test-map-ocean-10x10.json");

		map.isValid((Location) null);
	}

	@Test(expected = NullPointerException.class)
	public void testValidPathNull() {
		WorldMapDao dao = new WorldMapDao();
		WorldMap map = dao.loadMap("/maps/test-map-ocean-10x10.json");

		map.isValid((Path) null);
	}
}
