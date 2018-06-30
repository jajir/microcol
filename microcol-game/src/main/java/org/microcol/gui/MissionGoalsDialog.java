package org.microcol.gui;

import java.util.List;
import java.util.function.Function;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;
import org.microcol.model.Calendar;
import org.microcol.model.TurnPlayerStatistics;
import org.microcol.model.campaign.MissionGoals;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class MissionGoalsDialog extends AbstractMessageWindow {

    private final GameModelController gameModelController;

    private final Text text;

    private final VBox goalsPanel;

    @Inject
    MissionGoalsDialog(final ViewUtil viewUtil, final Text text,
            final GameModelController gameModelController) {
        super(viewUtil);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.text = Preconditions.checkNotNull(text);
        setTitle(text.get("missionGoals.title"));

        final Label labelCaption = new Label(text.get("missionGoals.caption"));

        final VBox mainPanel = new VBox();

        final ButtonsBar buttonsBar = new ButtonsBar(text.get("dialog.ok"));
        buttonsBar.getButtonOk().setOnAction(this::onClose);

        goalsPanel = new VBox();

        mainPanel.getChildren().addAll(labelCaption, goalsPanel, buttonsBar);

        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    public void repaint() {
        goalsPanel.getChildren().clear();
        final MissionGoals goals = gameModelController.getModelMission().getMission().getGoals();
        goals.getGoals().forEach(goal -> {
            String str = text.get(goal.getDescriptionKey()) + " ";
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
