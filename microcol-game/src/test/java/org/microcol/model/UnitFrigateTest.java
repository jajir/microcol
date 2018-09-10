package org.microcol.model;

import static org.junit.Assert.*;

import java.util.List;
import java.util.function.Function;

import org.junit.Before;
import org.junit.Test;
import org.microcol.model.unit.UnitAction;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFrigate;

import com.google.common.base.Preconditions;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

public class UnitFrigateTest {

    @Tested
    private UnitFrigate unit;

    @Injectable
    private Function<Unit, Cargo> cargoProvider;

    @Injectable
    private Model model;

    @Injectable(value = "4")
    private Integer id;

    @Injectable
    private Function<Unit, Place> placeBuilder;

    @Injectable
    private Player owner;

    @Injectable(value = "3")
    private int availableMoves;

    @Injectable
    private UnitAction unitAction = new UnitActionNoAction();

    @Mocked
    private PlaceLocation placeMap;

    @Mocked
    private Cargo cargo;

    @Test
    public void test_verifyThatUnitCouldBePlacedIntoHighseas() throws Exception {
        unit.placeToHighSeas(true);

        assertTrue(unit.isAtHighSea());
    }

    @Test(expected = IllegalStateException.class)
    public void test_placeToEuropePortPier() throws Exception {
        unit.placeToEuropePortPier();
    }

    @Test
    public void test_visibleArea_visibility_1() throws Exception {
        new Expectations() {{
                placeMap.getLocation(); result = Location.of(30, 30);
            }};

        final List<Location> visible = unit.getVisibleLocations();

        Preconditions.checkNotNull(visible);
        assertEquals(121, visible.size());
    }

    @Before
    public void setup() {
        /*
         * Following expectations will be used for unit constructor
         */
        new Expectations() {{
                cargoProvider.apply((Unit) any); result = cargo;
                placeBuilder.apply((Unit) any); result = placeMap;
            }};
    }

}