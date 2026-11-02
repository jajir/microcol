package org.akropia.gui.screen.turnreport;

import java.util.function.Function;

import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventNewUnitInColony;

import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorNewUnitInColony extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorNewUnitInColony(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventNewUnitInColony) {
            final TurnEventNewUnitInColony te = (TurnEventNewUnitInColony) turnEvent;
            return new TeItemColony(getMessage(te.getColonyName()),
                    te.getColonyId());
        }
        return null;
    }

    private String getMessage(final String colonyName) {
        return getI18n().get(TurnEvents.newUnitIsInColony, colonyName);
    }

}
