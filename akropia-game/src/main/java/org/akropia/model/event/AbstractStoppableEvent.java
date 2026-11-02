package org.akropia.model.event;

import org.akropia.model.Model;

abstract class AbstractStoppableEvent extends AbstractModelEvent
        implements StoppableEvent {

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
