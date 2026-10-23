package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.gui.screen.congress.CongressText;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventIntervention;

import com.google.inject.Inject;

/**
 * Present an already recorded intervention notice; displaying the report never
 * changes the target or the earning.
 */
public final class TeProcessorIntervention extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorIntervention(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventIntervention) {
            TurnEventIntervention notice = (TurnEventIntervention) event;
            return new TeItemSimple(getI18n().get(
                    notice.isEarned() ? CongressText.interventionEarned
                            : CongressText.interventionAnnounced,
                    notice.getTarget()));
        }
        return null;
    }
}
