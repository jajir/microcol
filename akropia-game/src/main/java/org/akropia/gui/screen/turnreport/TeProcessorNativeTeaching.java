package org.akropia.gui.screen.turnreport;

import java.util.function.Function;

import org.akropia.gui.Loc;
import org.akropia.gui.UnitTypes;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventNativeTeaching;

import com.google.inject.Inject;

/** Display a recorded native course outcome without advancing the course. */
public final class TeProcessorNativeTeaching extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorNativeTeaching(I18n i18n) {
        super(i18n);
    }

    public TeItemSimple apply(TurnEvent event) {
        if (!(event instanceof TurnEventNativeTeaching))
            return null;
        TurnEventNativeTeaching notice = (TurnEventNativeTeaching) event;
        final String skill = getI18n()
                .get(UnitTypes.getUnitName(notice.getSkill()));
        return new TeItemSimple(getI18n()
                .get(notice.getKind() == TurnEventNativeTeaching.Kind.COMPLETED
                        ? Loc.nativeTeachingDone
                        : Loc.nativeTeachingHostility, skill));
    }
}
