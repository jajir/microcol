package org.microcol.gui.screen.turnreport;

import org.microcol.gui.Loc;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.dialog.Dialog;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.ButtonsBar;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public final class TurnReportDialog extends AbstractMessageWindow
        implements TurnReportDialogCallback {

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final VBox turnEventsPanel;
    
    private final TeService teService;

    @Inject
    TurnReportDialog(final ViewUtil viewUtil, final I18n i18n,
            final GameModelController gameModelController, final TeService teService) {
        super(viewUtil, i18n);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.teService = Preconditions.checkNotNull(teService);
        setTitle(i18n.get(Loc.turnReport_title));

        final Label labelCaption = new Label(i18n.get(Loc.turnReport_caption));

        final VBox mainPanel = new VBox();

        final ButtonsBar buttonsBar = new ButtonsBar(i18n.get(Dialog.ok));
        buttonsBar.getButtonOk().setOnAction(this::onClose);

        turnEventsPanel = new VBox();

        mainPanel.getChildren().addAll(labelCaption, turnEventsPanel, buttonsBar);

        init(mainPanel);
        getScene().getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    @Override
    public void repaint() {
        turnEventsPanel.getChildren().clear();
        if (gameModelController.getModel()
                .isTurnEventsMessagesEmpty(gameModelController.getCurrentPlayer())) {
            turnEventsPanel.getChildren().add(new Label(i18n.get(Loc.turnReport_noEvents)));
        } else {
            teService.getMessages().forEach(turnEvent -> turnEventsPanel.getChildren()
                    .add(new TurnEventPanel(turnEvent)));
        }
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
