package org.akropia.model.event;

import org.akropia.model.Colony;
import org.akropia.model.Model;
import org.akropia.model.Unit;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

public class ColonyWasCapturedEvent extends AbstractModelEvent {

    private final Unit capturingUnit;
    private final Colony capturedColony;

    public ColonyWasCapturedEvent(final Model model, final Unit capturingUnit,
            final Colony capturedColony) {
        super(model);
        this.capturingUnit = Preconditions.checkNotNull(capturingUnit);
        this.capturedColony = Preconditions.checkNotNull(capturedColony);
    }

    /**
     * @return the capturingUnit
     */
    public Unit getCapturingUnit() {
        return capturingUnit;
    }

    /**
     * @return the capturedColony
     */
    public Colony getCapturedColony() {
        return capturedColony;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(ColonyWasCapturedEvent.class)
                .add("model", getModel()).add("capturingUnit", capturingUnit)
                .add("capturedColony", capturedColony).toString();
    }

}
