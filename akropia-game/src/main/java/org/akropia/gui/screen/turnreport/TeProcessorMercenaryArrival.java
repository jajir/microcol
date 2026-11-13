package org.akropia.gui.screen.turnreport;

import java.util.function.Function;

import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventMercenaryArrival;

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
            return new TeItemColony(getI18n().get(TurnEvents.mercenaryArrived,
                    arrival.getColonyName()), arrival.getColonyId());
        }
        return null;
    }
}
