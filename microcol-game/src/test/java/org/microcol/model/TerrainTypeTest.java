package org.microcol.model;

import static org.junit.Assert.*;

import org.junit.Test;

public class TerrainTypeTest {

	/**
	 * It just print out production matrix
	 * @throws Exception
	 */
	@Test
	public void test_production_matrix() throws Exception {
		assertEquals(11, TerrainType.TERRAINS.size());
	}

}
