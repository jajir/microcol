package org.akropia.gui.screen.turnreport;

import java.util.function.Function;
import com.google.inject.Inject;
import org.akropia.gui.UnitTypes;
import org.akropia.gui.screen.colony.EducationText;
import org.akropia.i18n.I18n;
import org.akropia.model.UnitType;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.model.turnevent.TurnEventEducationCompleted;

/** Display recorded education without advancing or recalculating training. */
public final class TeProcessorEducation extends AbstractTeProcessor
        implements Function<TurnEvent, TeItemSimple> {
    @Inject
    TeProcessorEducation(I18n i18n) {
        super(i18n);
    }

    public TeItemSimple apply(TurnEvent event) {
        if (!(event instanceof TurnEventEducationCompleted))
            return null;
        TurnEventEducationCompleted step = (TurnEventEducationCompleted) event;
        return new TeItemColony(
                getI18n().get(
                        step.isWorkExperience() ? EducationText.learned
                                : EducationText.graduated,
                        step.getColonyName(), step.getStudentId(),
                        name(step.getBeforeType(), step.wasVeteran()),
                        name(step.getAfterType(), step.isVeteran())),
                step.getColonyId());
    }

    private String name(UnitType type, boolean veteran) {
        return getI18n().get(UnitTypes.getUnitName(type))
                + (veteran ? " (" + getI18n().get(EducationText.veteran) + ")"
                        : "");
    }
}
