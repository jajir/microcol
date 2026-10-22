package org.microcol.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventPatrolInterception;

/**
 * Present the recorded slowdown or stop of a ship by a hostile patrol.
 * Displaying the report never checks the patrol again.
 */
public final class TeProcessorPatrolInterception extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorPatrolInterception(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventPatrolInterception) {
            final TurnEventPatrolInterception interception = (TurnEventPatrolInterception) event;
            return new TeItemSimple(
                    getI18n().get(interception.isStopped() ? PatrolText.stopped
                            : PatrolText.slowed));
        }
        return null;
    }
}
