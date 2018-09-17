package org.microcol.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import mockit.Mocked;

public class ColonyTest {

    private @Mocked Model model;

    private @Mocked Player owner;

    @Test
    public void test_constructor_constructionConsistency() throws Exception {
        Colony colony = makeColony(col -> new ArrayList<Construction>());

        assertNotNull(colony);
    }

    @Test(expected = IllegalStateException.class)
    public void test_constructor_constructionConsistency_sameConstructions() throws Exception {
        Colony colony = makeColony(col -> {
            List<Construction> list = new ArrayList<>();
            list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_SHOP));
            list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_SHOP));
            return list;
        });

        assertNotNull(colony);
        assertFalse(colony.isContainsConstructionByType(ConstructionType.BLACKSMITHS_SHOP));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.BLACKSMITHS_HOUSE));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.DOCK));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.DRYDOCK));
    }

    @Test(expected = IllegalStateException.class)
    public void test_constructor_constructionConsistency_sameConstructionsType() throws Exception {
        Colony colony = makeColony(col -> {
            List<Construction> list = new ArrayList<>();
            list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_SHOP));
            list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_HOUSE));
            return list;
        });

        assertNotNull(colony);
        assertNotNull(colony.getConstructionByType(ConstructionType.BLACKSMITHS_SHOP));
        assertNotNull(colony.getConstructionByType(ConstructionType.BLACKSMITHS_HOUSE));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.DOCK));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.DRYDOCK));
    }

    @Test(expected = IllegalStateException.class)
    public void test_constructor_constructionConsistency_sameConstructionsType_nonProducing()
            throws Exception {
        final Colony colony = makeColony(col -> {
            List<Construction> list = new ArrayList<>();
            list.add(Construction.build(model, col, ConstructionType.DOCK));
            list.add(Construction.build(model, col, ConstructionType.DRYDOCK));
            return list;
        });

        assertNotNull(colony);
        assertNotNull(colony.getConstructionByType(ConstructionType.DOCK));
        assertNotNull(colony.getConstructionByType(ConstructionType.DRYDOCK));
    }

    private Colony makeColony(final Function<Colony, List<Construction>> constructionsBuilder) {
        return new Colony(model, "Prague", owner, Location.of(2, 2), constructionsBuilder,
                new HashMap<String, Integer>(), new ArrayList<>());
    }

}
