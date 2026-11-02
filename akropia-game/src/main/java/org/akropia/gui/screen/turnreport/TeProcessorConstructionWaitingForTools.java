package org.akropia.gui.screen.turnreport;

import java.util.function.Function;

import org.akropia.gui.screen.colony.buildingqueue.ConstructionText;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventConstructionWaitingForTools;

import com.google.inject.Inject;

/** Display the recorded tools shortage without reading the current colony. */
public final class TeProcessorConstructionWaitingForTools extends
        AbstractTeProcessor implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorConstructionWaitingForTools(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent event) {
        if (event instanceof TurnEventConstructionWaitingForTools) {
            final TurnEventConstructionWaitingForTools waiting = (TurnEventConstructionWaitingForTools) event;
            return new TeItemColony(
                    getI18n().get(TurnEvents.constructionWaitingForTools,
                            waiting.getColonyName(),
                            ConstructionText.name(getI18n(),
                                    waiting.getProject()),
                            waiting.getMissingTools()),
                    waiting.getColonyId());
        }
        return null;
    }

}
