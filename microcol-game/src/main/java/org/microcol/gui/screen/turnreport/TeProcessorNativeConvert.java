package org.microcol.gui.screen.turnreport;

import java.util.function.Function;

import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventNativeConvert;

import com.google.inject.Inject;

/** Display a recorded native convert notice without changing the convert. */
public final class TeProcessorNativeConvert extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorNativeConvert(I18n i18n) {
        super(i18n);
    }

    public TeItemSimple apply(TurnEvent event) {
        if (!(event instanceof TurnEventNativeConvert))
            return null;
        final TurnEventNativeConvert notice = (TurnEventNativeConvert) event;
        return new TeItemSimple(getI18n()
                .get(notice.getKind() == TurnEventNativeConvert.Kind.ACQUIRED
                        ? Loc.nativeConvertJoined
                        : Loc.nativeConvertReturned));
    }
}
