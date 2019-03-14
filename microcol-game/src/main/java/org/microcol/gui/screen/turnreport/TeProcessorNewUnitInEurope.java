package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventNewUnitInEurope;

import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorNewUnitInEurope extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorNewUnitInEurope(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventNewUnitInEurope) {
            return new TeItemSimple(getMessage());
        }
        return null;
    }

    private String getMessage() {
        return getI18n().get(TurnEvents.newUnitIsInEurope);
    }

}
