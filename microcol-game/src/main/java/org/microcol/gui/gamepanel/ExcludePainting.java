package org.microcol.gui.gamepanel;

import java.util.HashSet;
import java.util.Set;

import org.microcol.model.Unit;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Class holds information about object what should not be drawn. For example
 * when move animation is executed unit should not be drawn at target location.
 * <p>
 * When it will be necessary to exclude other object type it should be here.
 * </p>
 */
public final class ExcludePainting {

    /**
     * List of units that will not be animated.
     */
    private final Set<Unit> excludeUnits;

    @Inject
    public ExcludePainting() {
        excludeUnits = new HashSet<>();
    }

    public void excludeUnit(final Unit unit) {
        excludeUnits.add(Preconditions.checkNotNull(unit));
    }

    public void includeUnit(final Unit unit) {
        excludeUnits.remove(Preconditions.checkNotNull(unit));
    }

    public boolean isUnitIncluded(final Unit unit) {
        return !excludeUnits.contains(Preconditions.checkNotNull(unit));
    }

}
