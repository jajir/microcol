package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventPeaceOffer;
import org.akropia.gui.screen.statistics.PeaceText;

/**
 * Present an already recorded offer; displaying the report never negotiates.
 */
public final class TeProcessorPeaceOffer extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorPeaceOffer(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventPeaceOffer) {
            TurnEventPeaceOffer offer = (TurnEventPeaceOffer) event;
            return new TeItemSimple(getI18n().get(PeaceText.turnOffer,
                    offer.getRivalName(), offer.getPrice()));
        }
        return null;
    }
}
