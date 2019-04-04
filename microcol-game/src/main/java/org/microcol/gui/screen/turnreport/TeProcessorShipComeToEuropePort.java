package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventShipComeToEuropePort;

import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorShipComeToEuropePort extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorShipComeToEuropePort(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventShipComeToEuropePort) {
            return new TeItemSimple(getMessage());
        }
        return null;
    }

    private String getMessage() {
        return getI18n().get(TurnEvents.shipComeToEuropePort);
    }

}
