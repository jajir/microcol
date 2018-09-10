package org.microcol.model;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import mockit.Expectations;
import mockit.Mocked;

public class UnitColonistTestInCargo extends AbstractUnitFreeColonistTest {

    @Mocked
    private PlaceCargoSlot placeCargoSlot;

    @Mocked
    private Cargo cargo;

    @Test
    public void test_placeToEuropePortPier() throws Exception {
        new Expectations() {{
                placeCargoSlot.isOwnerAtEuropePort(); result = true;
            }};

        unit.placeToEuropePortPier();

        assertTrue(unit.isAtEuropePier());
    }

    @Test(expected = IllegalStateException.class)
    public void test_placeToEuropePortPier_holding_unit_is_not_in_europe_port() throws Exception {
        new Expectations() {{
                placeCargoSlot.isOwnerAtEuropePort();
                result = false;
            }};
        unit.placeToEuropePortPier();
    }

    @Before
    public void setup() {
        makeColonist(model, 4, placeCargoSlot, owner, 3);
    }

}
