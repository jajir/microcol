package org.microcol.model.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.microcol.model.Location;

import com.google.common.collect.Sets;

/**
 * Tests loading and storing map layers into persistent object.
 */
public class WorldMapPoTest {

    private final static String LINE_C = "column  :1,2,3,4,5";
    private final static String LINE_0 = "row-0001:-,-,-,-,-";
    private final static String LINE_1 = "row-0002:-,t,-,-,-";
    private final static String LINE_2 = "row-0003:-,-,-,-,-";
    private final static String LINE_3 = "row-0004:-,-,t,-,-";
    private final static String LINE_4 = "row-0005:-,-,-,-,t";

    private final static Set<Location> TREE_LOCATIONS = Sets.newHashSet(Location.of(2, 2),
            Location.of(3, 4), Location.of(5, 5));

    private WorldMapPo worldMapPo;

    @Test
    public void test_setTreeSet() throws Exception {
        worldMapPo.setTrees(TREE_LOCATIONS);

        String ret[] = worldMapPo.getTrees();

        assertEquals(6, ret.length);
        assertEquals(LINE_C, ret[0]);
        assertEquals(LINE_0, ret[1]);
        assertEquals(LINE_1, ret[2]);
        assertEquals(LINE_2, ret[3]);
        assertEquals(LINE_3, ret[4]);
        assertEquals(LINE_4, ret[5]);
    }

    @Test
    public void test_getTreeSet() throws Exception {
        worldMapPo.setTrees(new String[] { LINE_C, LINE_0, LINE_1, LINE_2, LINE_3, LINE_4 });

        Set<Location> ret = worldMapPo.getTreeSet();

        assertEquals(3, ret.size());
        TREE_LOCATIONS.forEach(loc -> assertTrue(ret.contains(loc)));
    }

    @BeforeEach
    public void setup() {
        worldMapPo = new WorldMapPo();
        worldMapPo.setMaxX(5);
        worldMapPo.setMaxY(5);
    }

    @AfterEach
    public void tearDown() {
        worldMapPo = null;
    }
}
