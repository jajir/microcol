package org.microcol.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mockit.Mocked;

public class TownTest {

	
	@Test
	public void test_constructor_constructionConsistency(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		Town town = new Town("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(town);
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructions(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		list.add(new Construction(ConstructionType.BLACKSMITHS_SHOP));
		list.add(new Construction(ConstructionType.BLACKSMITHS_SHOP));
		Town town = new Town("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(town);
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructionsType(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		list.add(new Construction(ConstructionType.BLACKSMITHS_SHOP));
		list.add(new Construction(ConstructionType.BLACKSMITHS_HOUSE));
		Town town = new Town("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(town);
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructionsType_nonProducing(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		list.add(new Construction(ConstructionType.DOCK));
		list.add(new Construction(ConstructionType.DRYDOCK));
		Town town = new Town("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(town);
	}

}
