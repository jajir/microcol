package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventInterventionArrival;

import com.google.inject.Inject;

/**
 * Present an already recorded arrival of the foreign intervention (R-0190);
 * displaying the report never changes the delivery.
 */
public final class TeProcessorInterventionArrival extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorInterventionArrival(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventInterventionArrival) {
            TurnEventInterventionArrival arrival = (TurnEventInterventionArrival) event;
            return new TeItemSimple(getI18n().get(
                    TurnEvents.interventionArrived, arrival.getColonyName()));
        }
        return null;
    }
}
