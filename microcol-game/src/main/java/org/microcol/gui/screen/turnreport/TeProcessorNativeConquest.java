package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.gui.Loc;
import org.microcol.gui.UnitTypes;
import org.microcol.gui.screen.game.components.StatusBarMessageService;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventNativeConquest;
import org.microcol.model.turnevent.TurnEventNativeConquestLoss;

import com.google.inject.Inject;

/**
 * Display a recorded native conquest and the individual pupil or missionary
 * lost with it; neither is repeated or changes the game (R-0174).
 */
public final class TeProcessorNativeConquest extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorNativeConquest(I18n i18n) {
        super(i18n);
    }

    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventNativeConquest) {
            final TurnEventNativeConquest conquest = (TurnEventNativeConquest) event;
            return new TeItemSimple(getI18n().get(Loc.nativeConquestTurnEvent,
                    getI18n().get(StatusBarMessageService
                            .nationLabel(conquest.getNation()))));
        }
        if (event instanceof TurnEventNativeConquestLoss) {
            final TurnEventNativeConquestLoss loss = (TurnEventNativeConquestLoss) event;
            return new TeItemSimple(getI18n().get(
                    loss.isPupil() ? Loc.nativeConquestLossPupil
                            : Loc.nativeConquestLossMissionary,
                    getI18n().get(UnitTypes.getUnitName(loss.getUnitType())),
                    getI18n().get(StatusBarMessageService
                            .nationLabel(loss.getNation()))));
        }
        return null;
    }
}
