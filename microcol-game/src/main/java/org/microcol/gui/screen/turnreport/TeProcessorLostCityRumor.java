package org.microcol.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.microcol.i18n.I18n;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventLostCityRumor;

/**
 * Present the recorded result of a lost-city rumor; displaying the report never
 * resolves the rumor again.
 */
public final class TeProcessorLostCityRumor extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorLostCityRumor(I18n i18n) {
        super(i18n);
    }

    @Override
    public TeItemSimple apply(TurnEvent event) {
        if (event instanceof TurnEventLostCityRumor) {
            return new TeItemSimple(text((TurnEventLostCityRumor) event));
        }
        return null;
    }

    private String text(final TurnEventLostCityRumor rumor) {
        switch (rumor.getOutcome()) {
        case GOLD:
            return getI18n().get(RumorText.gold, rumor.getGold());
        case RECRUIT:
            return getI18n().get(RumorText.recruit);
        case EXPLORER_LOST:
            return getI18n().get(RumorText.explorerLost);
        case BURIAL:
            return getI18n()
                    .get(rumor.getNation() == null ? RumorText.burialNoNation
                            : RumorText.burial);
        default:
            return getI18n()
                    .get(rumor.isDeSotoAverted() ? RumorText.deSotoAverted
                            : RumorText.empty);
        }
    }
}
