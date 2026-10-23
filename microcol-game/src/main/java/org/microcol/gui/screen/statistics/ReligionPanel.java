package org.microcol.gui.screen.statistics;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.i18n.I18n;
import org.microcol.model.ReligionReport;
import org.microcol.model.ReligionReport.ColonyOutput;
import org.microcol.model.ReligionReport.Immigration;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

/**
 * Religion report (R-0199): the cross output of each colony at the next update,
 * the accumulated crosses toward the next immigrant and the state of the earned
 * immigrant. Every showing rebuilds the figures from the live model; the panel
 * keeps no figure of its own and offers no action that spends crosses.
 */
final class ReligionPanel extends VBox {
    private final GameModelController game;
    private final I18n i18n;
    private final VBox rows = new VBox(8);

    ReligionPanel(final GameModelController game, final I18n i18n) {
        super(12);
        this.game = game;
        this.i18n = i18n;
        setId("religion");
        getStyleClass().add("relations-report");
        final ScrollPane scroll = new ScrollPane(rows);
        scroll.setId("religionScroll");
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(300);
        scroll.setPrefViewportWidth(400);
        getChildren().add(scroll);
    }

    /** Rebuilds the output, progress and state from the model. */
    void showReport() {
        rows.getChildren().clear();
        if (!game.isGameModelReady()) {
            rows.getChildren().add(label("religionUnavailable",
                    i18n.get(ReligionText.unavailable)));
            return;
        }
        final ReligionReport report = game.getModel()
                .getReligionReport(game.getHumanPlayer());
        rows.getChildren()
                .add(label("religionNote", i18n.get(ReligionText.note)));
        rows.getChildren().add(label("religionOutputTitle",
                i18n.get(ReligionText.outputTitle)));
        if (report.getColonies().isEmpty()) {
            rows.getChildren().add(
                    label("religionEmpty", i18n.get(ReligionText.noColonies)));
        }
        for (final ColonyOutput colony : report.getColonies()) {
            rows.getChildren()
                    .add(label("religionColony-" + colony.getColonyId(),
                            i18n.get(ReligionText.colonyRow, colony.getName(),
                                    colony.getCrosses())));
        }
        rows.getChildren().add(label("religionNational", i18n
                .get(ReligionText.nationalRow, report.getNationalOutput())));
        rows.getChildren().add(label("religionProgressTitle",
                i18n.get(ReligionText.progressTitle)));
        rows.getChildren().add(label("religionAccumulated", i18n
                .get(ReligionText.accumulatedRow, report.getAccumulated())));
        rows.getChildren()
                .add(label("religionTarget", i18n.get(ReligionText.targetRow,
                        report.getTarget(), report.getRemaining())));
        rows.getChildren()
                .add(label("religionImmigration",
                        i18n.get(stateText(report.getImmigration()),
                                report.getChoiceOfferCount())));
    }

    private static ReligionText stateText(final Immigration state) {
        switch (state) {
        case ARRIVES_NEXT_TURN:
            return ReligionText.stateArrives;
        case CHOICE_WAITING:
            return ReligionText.stateChoice;
        case BLOCKED_EUROPE:
            return ReligionText.stateBlocked;
        default:
            return ReligionText.stateNone;
        }
    }

    private static Label label(final String id, final String text) {
        final Label label = new Label(text);
        label.setId(id);
        label.setWrapText(true);
        label.setMaxWidth(380);
        return label;
    }
}
