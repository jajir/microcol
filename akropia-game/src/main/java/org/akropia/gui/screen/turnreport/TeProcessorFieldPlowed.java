package org.akropia.gui.screen.turnreport;

import java.util.function.Function;

import org.akropia.gui.UnitTypes;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventFieldPlowed;

import com.google.inject.Inject;

/** Display the recorded finished plowing without reading the current world. */
public final class TeProcessorFieldPlowed extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {

    @Inject
    TeProcessorFieldPlowed(final I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(final TurnEvent turnEvent) {
        if (!(turnEvent instanceof TurnEventFieldPlowed)) {
            return null;
        }
        final TurnEventFieldPlowed plowed = (TurnEventFieldPlowed) turnEvent;
        return new TeItemSimple(getI18n().get(TurnEvents.fieldPlowed,
                getI18n().get(UnitTypes.getUnitName(plowed.getUnitType())),
                plowed.getUnitId(), plowed.getLocation().getX(),
                plowed.getLocation().getY()));
    }

}
