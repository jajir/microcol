package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventColonyWasLost;

import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorColonyWasLost extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorColonyWasLost(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventColonyWasLost) {
            final TurnEventColonyWasLost te = (TurnEventColonyWasLost) turnEvent;
            return new TeItemSimple(getMessage(te.getColonyName()));
        }
        return null;
    }

    private String getMessage(final String colonyName) {
        return getI18n().get(TurnEvents.colonyWasLost, colonyName);
    }

}
