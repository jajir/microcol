package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.gui.screen.colony.buildingqueue.ConstructionText;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventProjectCompleted;

import com.google.inject.Inject;

/**
 * Display a finished colony project with the colony action, so the reader can
 * go to the colony that finished it.
 */
public final class TeProcessorProjectCompleted extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorProjectCompleted(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent event) {
        if (event instanceof TurnEventProjectCompleted) {
            final TurnEventProjectCompleted completed = (TurnEventProjectCompleted) event;
            return new TeItemColony(
                    getI18n().get(TurnEvents.projectCompleted,
                            completed.getColonyName(),
                            ConstructionText.name(getI18n(),
                                    completed.getProject())),
                    completed.getColonyId());
        }
        return null;
    }

}
