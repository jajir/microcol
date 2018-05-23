package org.microcol.gui.turnreport;

import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TurnReportDialog extends AbstractMessageWindow implements TurnReportDialogCallback {

    private final GameModelController gameModelController;

    private final Text text;

    private final VBox turnEventsPanel;

    @Inject
    TurnReportDialog(final ViewUtil viewUtil, final Text text,
            final GameModelController gameModelController) {
        super(viewUtil);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.text = Preconditions.checkNotNull(text);
        getDialog().setTitle(text.get("turnReport.title"));

        final Label labelCaption = new Label(text.get("turnReport.caption"));

        final VBox mainPanel = new VBox();

        final ButtonsBar buttonsBar = new ButtonsBar(text.get("dialog.ok"));
        buttonsBar.getButtonOk().setOnAction(this::onClose);

        turnEventsPanel = new VBox();

        mainPanel.getChildren().addAll(labelCaption, turnEventsPanel, buttonsBar);

        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    @Override
    public void repaint() {
        turnEventsPanel.getChildren().clear();
        gameModelController.getModel()
                .getTurnEventsLocalizedMessages(gameModelController.getCurrentPlayer(),
                        key -> text.get(key))
                .forEach(turnEvent -> turnEventsPanel.getChildren()
                        .add(new TurnEventPanel(turnEvent, text)));
    }

    @SuppressWarnings("unused")
    private void onClose(final ActionEvent event) {
        getDialog().close();
    }

    public void show() {
        repaint();
        getDialog().showAndWait();
    }

}
