package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.akropia.i18n.I18n;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventDiplomacy;
import org.akropia.gui.screen.statistics.Stats;

/** Present an already recorded diplomatic change; reading never changes it. */
public final class TeProcessorDiplomacy extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorDiplomacy(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventDiplomacy) {
            TurnEventDiplomacy notice = (TurnEventDiplomacy) event;
            switch (notice.getKind()) {
            case CROWN_WAR:
                return new TeItemSimple(getI18n().get(Stats.turnCrownWar,
                        notice.getRivalName()));
            case CROWN_WAR_ALLIANCE_ENDED:
                return new TeItemSimple(getI18n().get(
                        Stats.turnCrownWarAlliance, notice.getRivalName()));
            default:
                return new TeItemSimple(getI18n().get(Stats.turnAllyDefense,
                        notice.getRivalName(), notice.getVictimName()));
            }
        }
        return null;
    }
}
