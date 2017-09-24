package org.microcol.model.store;

import java.io.FileNotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.microcol.model.WorldMap;

public class WorldMapDaoTest {

	private WorldMapDao dao;
	
	//TODO finish test
	
	// @Test
	// public void simple_write() throws FileNotFoundException {
	// WorldMap worldMap = new WorldMap("/maps/test-map-simple-test.txt");
	//
	// dao.save(worldMap, "target/pok.json");
	// }

	@Test
	public void simple_read() throws FileNotFoundException {
		WorldMap worldMap = dao.load("/maps/test1.json");

		dao.save(worldMap, "target/pok2.json");
	}

	@Before
	public void setup() {
		dao = new WorldMapDao();
	}

	@After
	public void tearDown() {
		dao = null;
	}

}
