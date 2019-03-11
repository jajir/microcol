package org.microcol.model;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColonyConstructorTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Model model = mock(Model.class);

    private final Player owner = mock(Player.class);

    @Test
    public void test_constructor_constructionConsistency() throws Exception {
        Colony colony = makeColony(col -> new ArrayList<Construction>());

        assertNotNull(colony);
    }

    @Test
    public void show_construction_in_new_colony() throws Exception {
        ConstructionType.NEW_COLONY_CONSTRUCTIONS.forEach(type -> {
            logger.info(type.name());
        });
    }

    @Test
    public void test_constructor_default_buildings() throws Exception {
        final Colony colony = makeColony(col -> {
            return ConstructionType.NEW_COLONY_CONSTRUCTIONS.stream()
                    .map(type -> Construction.build(model, col, type)).collect(Collectors.toList());
        });

        assertNotNull(colony);
        assertNotNull(colony.getConstructionByType(ConstructionType.TOWN_HALL));
        assertNotNull(colony.getConstructionByType(ConstructionType.CARPENTERS_STAND));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.DOCK));
        assertFalse(colony.isContainsConstructionByType(ConstructionType.DRYDOCK));
    }

    @Test
    public void test_constructor_constructionConsistency_sameConstructions() throws Exception {
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            makeColony(col -> {
                List<Construction> list = new ArrayList<>();
                list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_SHOP));
                list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_SHOP));
                return list;
            });
        });

        assertEquals("Construction type 'BLACKSMITHS_SHOP' is duplicated", exception.getMessage());
    }

    @Test
    public void test_constructor_constructionConsistency_sameConstructionsType() throws Exception {
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            makeColony(col -> {
                List<Construction> list = new ArrayList<>();
                list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_SHOP));
                list.add(Construction.build(model, col, ConstructionType.BLACKSMITHS_HOUSE));
                return list;
            });
        });

        assertEquals("Good type type 'TOOLS' is prodecen in more than one building",
                exception.getMessage());
    }

    @Test
    public void test_constructor_constructionConsistency_sameConstructionsType_nonProducing()
            throws Exception {
        final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            makeColony(col -> {
                List<Construction> list = new ArrayList<>();
                list.add(Construction.build(model, col, ConstructionType.DOCK));
                list.add(Construction.build(model, col, ConstructionType.DRYDOCK));
                return list;
            });
        });

        assertEquals(
                "There is building type 'ConstructionType{name=DOCK}' and it's upgrade 'Construction{name=DRYDOCK}'",
                exception.getMessage());
    }

    private Colony makeColony(final Function<Colony, List<Construction>> constructionsBuilder) {
        return new Colony(model, "Prague", owner, Location.of(2, 2), constructionsBuilder,
                new HashMap<String, Integer>(), new ArrayList<>());
    }

}
