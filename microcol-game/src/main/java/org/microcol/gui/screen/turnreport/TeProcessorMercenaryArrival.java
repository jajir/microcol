package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventMercenaryArrival;

import com.google.inject.Inject;

/**
 * Present an already recorded arrival of a paid mercenary group (R-0169);
 * displaying the report never changes the delivery.
 */
public final class TeProcessorMercenaryArrival extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorMercenaryArrival(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventMercenaryArrival) {
            TurnEventMercenaryArrival arrival = (TurnEventMercenaryArrival) event;
            return new TeItemSimple(getI18n().get(TurnEvents.mercenaryArrived,
                    arrival.getColonyName()));
        }
        return null;
    }
}
