package org.microcol.model;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Abstract test for construction tests. Prepare slots and colony mocks.
 */
public abstract class AbstractConstructionTest {

    protected final ConstructionSlot slot1 = mock(ConstructionSlot.class);

    protected final ConstructionSlot slot2 = mock(ConstructionSlot.class);

    protected final ConstructionSlot slot3 = mock(ConstructionSlot.class);

    protected final Colony colony = mock(Colony.class);

    protected final Model model = mock(Model.class);

    protected Construction construction;

    /** Mocked colonies report no political production change. */
    protected AbstractConstructionTest() {
        when(colony.getPoliticalProduction())
                .thenReturn(PoliticalProduction.NONE);
    }

}
