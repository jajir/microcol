package org.microcol.model;

import static org.mockito.Mockito.mock;

/**
 * Abstract test for construction tests. Prepare slots and colony mocks.
 */
public abstract class AbstractConstructionTest {

    protected final ConstructionSlot slot1 = mock(ConstructionSlot.class);

    protected final ConstructionSlot slot2 = mock(ConstructionSlot.class);

    protected final ConstructionSlot slot3 = mock(ConstructionSlot.class);

    protected final Colony colony = mock(Colony.class);

    protected Construction construction;

}
