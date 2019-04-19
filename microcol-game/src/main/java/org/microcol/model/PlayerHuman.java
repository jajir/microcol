package org.microcol.model;

import java.util.Map;
import java.util.Set;

/**
 * Human player class.
 */
public class PlayerHuman extends Player {

    protected PlayerHuman(final String name, final boolean computer, final int initialGold,
            final Model model, final boolean declaredIndependence,
            final Map<String, Object> extraData, Set<Location> visible) {
        super(name, computer, initialGold, model, declaredIndependence, extraData, visible);
    }
}
