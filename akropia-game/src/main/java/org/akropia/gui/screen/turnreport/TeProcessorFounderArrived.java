package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventFounderArrived;
import org.akropia.gui.screen.congress.CongressText;

/** Translate an arrival already recorded by the model. */
public final class TeProcessorFounderArrived extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorFounderArrived(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventFounderArrived) {
            TurnEventFounderArrived arrival = (TurnEventFounderArrived) event;
            return new TeItemSimple(getI18n().get(CongressText.founderJoined,
                    CongressText.name(getI18n(), arrival.getFounder())));
        }
        return null;
    }
}
