package org.akropia.gui.screen.editor;

import java.util.stream.Collectors;

import org.akropia.gui.LocalizationHelper;
import org.akropia.i18n.I18n;
import org.akropia.model.UnitType;
import org.akropia.model.generation.ScenarioDraft;
import org.akropia.model.generation.ScenarioDraftException;
import org.akropia.model.generation.ScenarioProblem;
import org.akropia.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Localized editor feedback: rejected edits and launch problems with their
 * squares, and unit names.
 */
@Singleton
class EditorMessages {

    private final I18n i18n;

    private final LocalizationHelper localization;

    @Inject
    EditorMessages(final I18n i18n, final LocalizationHelper localization) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.localization = Preconditions.checkNotNull(localization);
    }

    String get(final EditorText text, final Object... arguments) {
        return i18n.get(text, arguments);
    }

    /**
     * @return every problem with its square, in order
     */
    String describe(final ScenarioDraftException rejected) {
        return rejected.getProblems().stream().map(this::describe)
                .collect(Collectors.joining(" "));
    }

    String describe(final ScenarioProblem problem) {
        String out = i18n.get(EditorText.of(problem.getReason()),
                ScenarioDraft.MIN_SIDE, ScenarioDraft.MAX_SIDE);
        if (problem.getDetail().isPresent() && problem
                .getReason() != ScenarioProblem.Reason.DIMENSION_NOT_NUMBER
                && problem
                        .getReason() != ScenarioProblem.Reason.DIMENSION_OUT_OF_RANGE) {
            out = i18n.get(EditorText.problemDetail, out,
                    problem.getDetail().get());
        }
        if (problem.getLocation().isPresent()) {
            out = i18n.get(EditorText.problemAt, out,
                    problem.getLocation().get().getX(),
                    problem.getLocation().get().getY());
        }
        return out;
    }

    String unitType(final UnitType type) {
        return localization.getUnitName(type);
    }

    String unit(final UnitPo unit) {
        return i18n.get(EditorText.unitLabel, unitType(unit.getType()),
                unit.getId());
    }

}
