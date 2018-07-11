package org.microcol.model.event;

import org.microcol.model.Model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

/**
 * All model events should extends from this class. It ensure that all event
 * will have reference to game model.
 */
abstract class AbstractModelEvent {

    private final Model model;

    /**
     * Default constructor.
     *
     * @param model
     *            required game model
     */
    AbstractModelEvent(final Model model) {
        this.model = Preconditions.checkNotNull(model);
    }

    /**
     * Get game model.
     *
     * @return game model
     */
    public Model getModel() {
        return model;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(getClass()).add("model", model).toString();
    }

}
