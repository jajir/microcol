package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventFishermanLost;
import com.google.inject.Inject;

public final class TeProcessorFishermanLost extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    public TeProcessorFishermanLost(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent event) {
        if (!(event instanceof TurnEventFishermanLost)) {
            return null;
        }
        final TurnEventFishermanLost lost = (TurnEventFishermanLost) event;
        return new TeItemColony(getI18n().get(
                lost.getReason() == TurnEventFishermanLost.Reason.NO_CARGO_SPACE
                        ? TurnEvents.fishermanLostCargo
                        : TurnEvents.fishermanLostLand,
                lost.getUnitId(), lost.getColonyName(),
                lost.getLocation().getX(), lost.getLocation().getY()),
                lost.getColonyId());
    }
}
