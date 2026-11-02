package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventCoastalBattery;

/**
 * Present the recorded result of a coastal battery shot. A hit reports only the
 * damage; any repair or sinking is reported by the ship repair notices.
 * Displaying the report never fires again.
 */
public final class TeProcessorCoastalBattery extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorCoastalBattery(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventCoastalBattery) {
            final TurnEventCoastalBattery shot = (TurnEventCoastalBattery) event;
            return new TeItemSimple(
                    getI18n().get(shot.isHit() ? CoastalBatteryText.hit
                            : CoastalBatteryText.miss));
        }
        return null;
    }
}
