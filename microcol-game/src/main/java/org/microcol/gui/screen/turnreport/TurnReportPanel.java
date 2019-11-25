package org.microcol.gui.screen.turnreport;

import org.microcol.gui.Loc;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.GameScreen;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class TurnReportPanel implements GameScreen {

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final TurnReportView turnReportView;

    private final VBox mainPanel = new VBox();

    private final VBox turnEventsPanel = new VBox();

    private final TeService teService;

    @Inject
    TurnReportPanel(final I18n i18n, final GameModelController gameModelController,
            final TurnReportView turnReportView, final TeService teService) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.turnReportView = Preconditions.checkNotNull(turnReportView);
        this.teService = Preconditions.checkNotNull(teService);
        turnEventsPanel.getStyleClass().add("turnEventPanel");
        mainPanel.getChildren().addAll(turnEventsPanel);
    }

    @Override
    public void beforeShow() {
        turnEventsPanel.getChildren().clear();
        if (gameModelController.getModel()
                .isTurnEventsMessagesEmpty(gameModelController.getHumanPlayer())) {
            turnEventsPanel.getChildren().add(new Label(i18n.get(Loc.turnReport_noEvents)));
        } else {
            teService.getMessages().forEach(
                    turnEvent -> turnEventsPanel.getChildren().add(new TurnEventPanel(turnEvent)));
        }
    }

    @Override
    public void beforeHide() {
        // intentionally do nothing
    }

    @Override
    public Region getContent() {
        return mainPanel;
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        turnReportView.setTitle(i18n.get(Loc.turnReport_title));
    }
}
