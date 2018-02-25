package org.microcol.model;

import static org.junit.Assert.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class TerrainTypeTest {

	private final static int NUMBER_OF_TERRAINS = 11;

	/**
	 * It just print out production matrix
	 * 
	 * @throws Exception
	 *             Could throws any exception
	 */
	@Test
	public void test_production_matrix() throws Exception {
		assertEquals(NUMBER_OF_TERRAINS, TerrainType.TERRAINS.size());
	}

	@Test
	public void test_duplicated_terrainType_code() throws Exception {
		Set<String> codes = TerrainType.TERRAINS.stream().map(terrainType -> terrainType.getCode())
				.collect(Collectors.toSet());
		
		assertEquals(NUMBER_OF_TERRAINS, codes.size());
	}

}
