package org.microcol.gui.dialog;

import java.util.Optional;

import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.OrdinationProblem;
import org.microcol.model.Unit;

import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Confirm a missionary ordination. The model decides admission before the
 * dialog shows the confirmation and again before the change.
 */
public final class DialogOrdainMissionary extends AbstractMessageWindow {

    private final GameModelController gameModelController;

    private final Label caption;
    private final Label summary;
    private final Label message;
    private final Button confirm;
    private final Button cancel;

    private final I18n i18n;

    private Unit unit;
    private boolean ordained;

    @Inject
    public DialogOrdainMissionary(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController gameModelController) {
        super(viewUtil, i18n);
        this.i18n = i18n;
        this.gameModelController = gameModelController;

        this.caption = new Label();
        this.summary = new Label();
        this.message = new Label();
        this.confirm = new Button();
        this.cancel = new Button();

        final VBox root = new VBox(12);
        root.setPrefWidth(500);
        root.setId("missionaryDialog");

        caption.setId("missionaryCaption");
        summary.setId("missionarySummary");
        message.setId("missionaryMessage");
        confirm.setId("missionaryConfirm");
        cancel.setId("missionaryCancel");

        final HBox buttons = new HBox(8, confirm, cancel);
        buttons.setId("missionaryButtons");

        root.getChildren().addAll(caption, summary, message, buttons);

        caption.setWrapText(true);
        summary.setWrapText(true);
        summary.setMinHeight(80);
        message.setWrapText(true);
        message.setMinHeight(60);

        confirm.setDefaultButton(true);
        confirm.setOnAction(e -> handleConfirm());
        cancel.setCancelButton(true);
        cancel.setOnAction(e -> close());

        init(root);
    }

    /**
     * Show the ordination choice for one colonist.
     *
     * @param unit the colonist the player selected
     * @return whether this dialog ordained the colonist
     */
    public boolean show(final Unit unit) {
        this.unit = unit;
        this.ordained = false;

        caption.setText(i18n.get(OrdinationMsg.ordainTitle));
        summary.setText(i18n.get(OrdinationMsg.ordainSummary));
        confirm.setText(i18n.get(OrdinationMsg.ordainConfirm));
        cancel.setText(i18n.get(OrdinationMsg.ordainCancel));
        refresh();

        Platform.runLater(() -> {
            if (confirm.isDisabled()) {
                cancel.requestFocus();
            } else {
                confirm.requestFocus();
            }
        });

        showAndWait();
        return ordained;
    }

    private void refresh() {
        final Optional<OrdinationProblem> problem = gameModelController
                .getModel().getOrdinationProblem(unit);
        confirm.setDisable(problem.isPresent());
        message.setText(problem
                .map(p -> i18n.get(OrdinationMsg.valueOf("ordain" + p.name())))
                .orElse(""));
    }

    private void handleConfirm() {
        if (gameModelController.getModel().getOrdinationProblem(unit)
                .isPresent()) {
            refresh();
            return;
        }
        try {
            gameModelController.getModel().ordainMissionary(unit);
            ordained = true;
            confirm.setDisable(true);
            cancel.setText(i18n.get(OrdinationMsg.ordainClose));
            message.setText(i18n.get(OrdinationMsg.ordainDone));
        } catch (IllegalStateException rejected) {
            refresh();
            if (!confirm.isDisabled()) {
                message.setText(i18n.get(OrdinationMsg.ordainChanged));
            }
        }
    }
}
