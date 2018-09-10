package org.microcol.model;

import org.junit.After;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;

import mockit.Mocked;

public abstract class AbstractUnitFreeColonistTest {

    protected Unit unit;

    @Mocked
    protected Model model;

    @Mocked
    protected Player owner;

    @Mocked
    protected WorldMap worldMap;

    protected void makeColonist(final Model model, final int id, final Place place,
            final Player owner, final int availableMoves) {
        unit = new UnitFreeColonist(model, id, unit -> place, owner, availableMoves,
                new UnitActionNoAction(), 0, false, false);
    }

    @After
    public void tearDown() {
        unit = null;
    }

}
