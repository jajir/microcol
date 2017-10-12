package org.microcol.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import mockit.Mocked;

public class ColonyTest {

	
	@Test
	public void test_constructor_constructionConsistency(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		Colony colony = new Colony("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(colony);
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructions(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		list.add(Construction.build(ConstructionType.BLACKSMITHS_SHOP));
		list.add(Construction.build(ConstructionType.BLACKSMITHS_SHOP));
		Colony colony = new Colony("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(colony);
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructionsType(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		list.add(Construction.build(ConstructionType.BLACKSMITHS_SHOP));
		list.add(Construction.build(ConstructionType.BLACKSMITHS_HOUSE));
		Colony colony = new Colony("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(colony);
	}
	
	@Test(expected=IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructionsType_nonProducing(final @Mocked Player owner) throws Exception {
		List<Construction> list = new ArrayList<>();
		list.add(Construction.build(ConstructionType.DOCK));
		list.add(Construction.build(ConstructionType.DRYDOCK));
		Colony colony = new Colony("Prague", owner, Location.of(2, 2), list);
		
		assertNotNull(colony);
	}

}
