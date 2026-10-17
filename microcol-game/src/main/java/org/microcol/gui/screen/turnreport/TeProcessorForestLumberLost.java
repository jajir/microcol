package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventForestLumberLost;

import com.google.inject.Inject;

/** Display the recorded lumber loss without reading the current world. */
public final class TeProcessorForestLumberLost extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorForestLumberLost(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (!(turnEvent instanceof TurnEventForestLumberLost)) {
            return null;
        }
        final TurnEventForestLumberLost lost = (TurnEventForestLumberLost) turnEvent;
        final int x = lost.getLocation().getX();
        final int y = lost.getLocation().getY();
        final int amount = lost.getGoods().getAmount();
        if (lost.getColonyId().isPresent()) {
            return new TeItemColony(
                    getI18n().get(TurnEvents.forestLumberLostFull, x, y, amount,
                            lost.getColonyName().orElseThrow()),
                    lost.getColonyId().get());
        }
        return new TeItemSimple(getI18n()
                .get(TurnEvents.forestLumberLostNoColony, x, y, amount));
    }

}
