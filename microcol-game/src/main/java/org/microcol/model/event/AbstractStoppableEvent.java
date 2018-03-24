package org.microcol.model.event;

import org.microcol.model.Model;

public abstract class AbstractStoppableEvent extends AbstractModelEvent implements StoppableEvent {

    private boolean stopped = false;

    AbstractStoppableEvent(Model model) {
        super(model);
    }

    @Override
    public void stopEventExecution() {
        stopped = true;
    }

    /**
     * @return the stopped
     */
    public boolean isStopped() {
        return stopped;
    }

}
