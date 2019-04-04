package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventFamineWillPlagueColony;

import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorFamineWillPlagueColony extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorFamineWillPlagueColony(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventFamineWillPlagueColony) {
            final TurnEventFamineWillPlagueColony te = (TurnEventFamineWillPlagueColony) turnEvent;
            return new TeItemSimple(getMessage(te.getColonyName()));
        }
        return null;
    }

    private String getMessage(final String colonyName) {
        return getI18n().get(TurnEvents.famineWillPlagueColony, colonyName);
    }

}
