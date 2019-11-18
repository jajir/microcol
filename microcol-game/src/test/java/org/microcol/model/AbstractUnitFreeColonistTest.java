package org.microcol.model;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitFrigate;

public abstract class AbstractUnitFreeColonistTest {

    protected Unit unit;

    protected Model model = mock(Model.class);

    protected Player owner = mock(Player.class);

    protected WorldMap worldMap = mock(WorldMap.class);

    protected Cargo cargo = mock(Cargo.class);

    protected void makeColonist(final Model model, final int id, final Place place,
            final Player owner, final int availableMoves) {
        unit = new UnitFreeColonist(model, id, unit -> place, owner, availableMoves,
                new UnitActionNoAction(), 0, false, false);
    }

    protected UnitFrigate makeFrigate(final Model model, final int id, final Place place,
            final Player owner, final int availableMoves) {
        return new UnitFrigate(unit -> cargo, model, id, unit -> place, owner, availableMoves,
                new UnitActionNoAction());
    }

    @AfterEach
    public void tearDown() {
        unit = null;
    }

}
