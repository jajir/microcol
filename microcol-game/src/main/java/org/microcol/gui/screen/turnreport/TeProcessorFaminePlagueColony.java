package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventFaminePlagueColony;

import com.google.inject.Inject;

/**
 * Define function that for input model turn event create front-end object.
 */
public class TeProcessorFaminePlagueColony extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorFaminePlagueColony(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (turnEvent instanceof TurnEventFaminePlagueColony) {
            final TurnEventFaminePlagueColony te = (TurnEventFaminePlagueColony) turnEvent;
            return new TeItemSimple(getMessage(te.getColonyName()));
        }
        return null;
    }

    private String getMessage(final String colonyName) {
        return getI18n().get(TurnEvents.faminePlagueColony, colonyName);
    }

}
