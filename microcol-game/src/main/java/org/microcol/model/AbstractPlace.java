package org.microcol.model;

import com.google.common.base.Preconditions;

/**
 * Provide reference to placed unit.
 */
abstract class AbstractPlace implements Place {

    private final Unit unit;

    AbstractPlace(final Unit unit) {
        this.unit = Preconditions.checkNotNull(unit);
    }

    @Override
    public Unit getUnit() {
        return unit;
    }

    @Override
    public void destroy() {
        // default empty implementation.
    }

    /**
     * Default implementation just delegate to {@link #destroy()}.
     */
    @Override
    public void destroySimple() {
        destroy();
    }

}
