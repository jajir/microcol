package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import com.google.inject.Inject;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.screen.europe.Europe;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventGoTo;

/** Translate a recorded go-to pause or cancellation without changing orders. */
public final class TeProcessorGoTo extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorGoTo(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (!(event instanceof TurnEventGoTo)) {
            return null;
        }
        TurnEventGoTo goTo = (TurnEventGoTo) event;
        String unit = getI18n().get(UnitTypes.getUnitName(goTo.getUnitType()));
        Europe key = Europe.valueOf("goTo" + goTo.getKind().name());
        return new TeItemSimple(getI18n().get(key, unit, goTo.getUnitId(),
                goTo.getDestinationName()));
    }
}
