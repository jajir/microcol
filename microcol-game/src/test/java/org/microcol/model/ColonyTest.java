package org.microcol.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import mockit.Mocked;

public class ColonyTest {

	@Mocked
	Model model;

	@Test
	public void test_constructor_constructionConsistency(final @Mocked Player owner) throws Exception {
		Colony colony = new Colony(model, "Prague", owner, Location.of(2, 2), col -> new ArrayList<Construction>(),
				new HashMap<String, Integer>());

		assertNotNull(colony);
	}

	@Test(expected = IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructions(final @Mocked Player owner)
			throws Exception {
		Colony colony = new Colony(model, "Prague", owner, Location.of(2, 2), col -> {
			List<Construction> list = new ArrayList<>();
			list.add(Construction.build(col, ConstructionType.BLACKSMITHS_SHOP));
			list.add(Construction.build(col, ConstructionType.BLACKSMITHS_SHOP));
			return list;
		}, new HashMap<String, Integer>());

		assertNotNull(colony);
	}

	@Test(expected = IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructionsType(final @Mocked Player owner)
			throws Exception {
		Colony colony = new Colony(model, "Prague", owner, Location.of(2, 2), col -> {
			List<Construction> list = new ArrayList<>();
			list.add(Construction.build(col, ConstructionType.BLACKSMITHS_SHOP));
			list.add(Construction.build(col, ConstructionType.BLACKSMITHS_HOUSE));
			return list;
		}, new HashMap<String, Integer>());

		assertNotNull(colony);
	}

	@Test(expected = IllegalStateException.class)
	public void test_constructor_constructionConsistency_sameConstructionsType_nonProducing(final @Mocked Player owner)
			throws Exception {
		Colony colony = new Colony(model, "Prague", owner, Location.of(2, 2), col -> {
			List<Construction> list = new ArrayList<>();
			list.add(Construction.build(col, ConstructionType.DOCK));
			list.add(Construction.build(col, ConstructionType.DRYDOCK));
			return list;
		}, new HashMap<String, Integer>());

		assertNotNull(colony);
	}

}
