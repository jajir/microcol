package org.microcol.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.microcol.model.store.VisibilityPo;

class VisibilityTest {
    @Test
    void leavingAndReloadingRetainsExplorationWithoutRevealingOtherCells() {
        final Location first = Location.of(3, 3);
        final Location second = Location.of(8, 8);
        final Location unknown = Location.of(5, 5);
        final Visibility visibility = new Visibility(new HashSet<>());
        final Unit unit = mock(Unit.class);
        when(unit.getVisibleLocations()).thenReturn(List.of(first),
                List.of(second));
        visibility.makeVisibleMapForUnit(unit);
        visibility.makeVisibleMapForUnit(unit);
        final VisibilityPo saved = new VisibilityPo();
        visibility.store(saved, 10, 10);
        final Visibility restored = new Visibility(saved.getVisibilitySet());
        assertTrue(restored.isVisible(first));
        assertTrue(restored.isVisible(second));
        assertFalse(restored.isVisible(unknown));
    }
}
