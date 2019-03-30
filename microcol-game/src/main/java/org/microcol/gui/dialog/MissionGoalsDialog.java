package org.microcol.gui.dialog;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.campaign.MissionGoals;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class MissionGoalsDialog extends AbstractMessageWindow {

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final VBox goalsPanel;

    @Inject
    MissionGoalsDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController gameModelController) {
        super(viewUtil, i18n);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        setTitle(i18n.get((Dialog.missionGoals_title)));

        final Label labelCaption = new Label(i18n.get(Dialog.missionGoals_caption));

        final VBox mainPanel = new VBox();

        final ButtonsBar buttonsBar = new ButtonsBar(i18n.get(Dialog.ok));
        buttonsBar.getButtonOk().setOnAction(this::onClose);

        goalsPanel = new VBox();

        mainPanel.getChildren().addAll(labelCaption, goalsPanel, buttonsBar);

        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    public void repaint() {
        goalsPanel.getChildren().clear();
        final MissionGoals goals = gameModelController.getGameModel().getMissionGoals();
        goals.getGoals().forEach(goal -> {
            String str = i18n.get(goal.getDescriptionKey()) + " ";
            if (goal.isFinished()) {
                str += "Done";
            } else {
                str += "Not done";
            }
            final Label label = new Label(str);
            goalsPanel.getChildren().add(label);
        });
    }

    @SuppressWarnings("unused")
    private void onClose(final ActionEvent event) {
        close();
    }

    public void show() {
        repaint();
        showAndWait();
    }

}
