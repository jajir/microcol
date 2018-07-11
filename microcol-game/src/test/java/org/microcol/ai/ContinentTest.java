package org.microcol.ai;

import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.microcol.model.Colony;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.store.WorldMapPo;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;

/**
 * Verify that finding closest enemy city to attack works.
 */
public class ContinentTest {
    
    private static final Location CITY_1 = Location.of(22, 12);
    
    private static final Location CITY_2 = Location.of(23, 17);

    private static final String[] trees = new String[] {
            "column  :1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0,1,2,3,4,5,6,7,8,9,0",
            "row-0001:t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,a",
            "row-0002:~,_,_,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,_,_,_,t,_,_,_,_,t,_,_,_,_,t,_,_,_,t,_,_,_,t,t",
            "row-0003:~,_,_,_,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0004:~,_,_,_,_,_,t,_,_,_,_,_,_,_,_,_,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0005:~,_,_,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0006:~,_,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0007:~,_,_,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0008:~,_,_,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0009:~,_,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0010:~,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0011:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0012:~,_,_,_,_,_,_,_,_,_,_,_,t,t,_,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0013:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0014:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0015:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0016:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0017:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0018:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0019:~,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0020:~,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0021:~,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0022:~,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0023:~,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0024:~,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0025:~,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,t,t,t,t,t,t,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0026:~,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0027:~,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,~",
            "row-0028:~,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,t,_,_,_,_,_,_,_,t,_,_,_,_,_,_,_,t,_,_,_,_,_,_,_,t,_,_,_,_,_,~",
            "row-0029:~,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,_,t,t,t,_,_,_,_,_,t,t,t,_,_,_,_,_,t,t,t,_,_,_,_,_,t,t,t,_,_,_,_,~",
            "row-0030:t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,t,a" };

    @Tested
    private Continent continent;

    @Mocked
    protected Model model;

    @Mocked
    protected Player enemyPlayer;

    @Mocked
    protected Colony enemyColony;

    @Test
    public void test_verify_that_cities_are_at_continent() throws Exception {
        assertTrue(continent.contains(CITY_1));
        assertTrue(continent.contains(CITY_2));
    }
    
    @Test
    public void test_findClosestCityToAttack_location_in_not_atContinent() throws Exception {
        final Optional<Location> oLoc = continent.getClosesEnemyCityToAttack(Location.of(1, 1));

        Assert.assertNotNull(oLoc);
        Assert.assertFalse(oLoc.isPresent());
    }

    @Test
    public void test_findClosestCityToAttack_no_enemy_cities() throws Exception {
        final Optional<Location> oLoc = continent.getClosesEnemyCityToAttack(Location.of(22, 13));

        Assert.assertNotNull(oLoc);
        Assert.assertFalse(oLoc.isPresent());
    }

    @Test
    public void test_findClosestCityToAttack_one_city() throws Exception {
        new Expectations() {{
            model.getColoniesAt(CITY_1, enemyPlayer); result = Optional.of(enemyColony);
        }};
        
        final Optional<Location> oLoc = continent.getClosesEnemyCityToAttack(Location.of(22, 13));
        Assert.assertNotNull(oLoc);
        Assert.assertTrue(oLoc.isPresent());
        Assert.assertEquals(CITY_1, oLoc.get());
    }

    @Test
    public void test_findClosestCityToAttack_one_city_23_14() throws Exception {
        new Expectations() {{
            model.getColoniesAt(CITY_1, enemyPlayer); result = Optional.of(enemyColony);
            model.getColoniesAt(CITY_2, enemyPlayer); result = Optional.of(enemyColony);
        }};
        final Optional<Location> oLoc = continent.getClosesEnemyCityToAttack(Location.of(23, 14));
        
        Assert.assertNotNull(oLoc);
        Assert.assertTrue(oLoc.isPresent());
        Assert.assertEquals(CITY_1, oLoc.get());
    }
    
    @Test
    public void test_findClosestCityToAttack_one_city_22_13() throws Exception {
        new Expectations() {{
            model.getColoniesAt(CITY_1, enemyPlayer); result = Optional.of(enemyColony);
            model.getColoniesAt(CITY_2, enemyPlayer); result = Optional.of(enemyColony);
        }};
        final Optional<Location> oLoc = continent.getClosesEnemyCityToAttack(Location.of(22, 13));
        
        Assert.assertNotNull(oLoc);
        Assert.assertTrue(oLoc.isPresent());
        Assert.assertEquals(CITY_1, oLoc.get());
    }

    @Before
    public void before() {
        continent = new Continent(model, enemyPlayer);
        /**
         * Following code is little hack. It use property of world map where data are in
         * JSON stored as array of strings but in code are used as list of location.
         * Translation from trees is used her for continent.
         */
        final WorldMapPo po = new WorldMapPo();
        po.setTrees(trees);
        po.getTreeSet().forEach(loc -> continent.add(loc));
    }
    
    @After
    public void after() {
        continent = null;
    }

}
