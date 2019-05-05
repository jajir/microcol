package org.microcol.gui.screen.turnreport;

import org.microcol.gui.Loc;
import org.microcol.gui.MainStageBuilder;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.ButtonBarOk;
import org.microcol.i18n.I18n;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public final class TurnReportPanel implements GameScreen {

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    private final TurnReportView turnReportView;

    private final VBox mainPanel = new VBox();

    private final ButtonBarOk buttonsBar = new ButtonBarOk();

    private final VBox turnEventsPanel;

    private final TeService teService;

    @Inject
    TurnReportPanel(final I18n i18n, final EventBus eventBus,
            final GameModelController gameModelController, final TurnReportView turnReportView,
            final TeService teService) {
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.turnReportView = Preconditions.checkNotNull(turnReportView);
        this.teService = Preconditions.checkNotNull(teService);

        buttonsBar.getButtonOk().setOnAction(this::onClose);

        turnEventsPanel = new VBox();

        mainPanel.getChildren().addAll(turnEventsPanel, buttonsBar.getContent());

        mainPanel.getStylesheets().add(MainStageBuilder.STYLE_SHEET_MICROCOL);
    }

    @SuppressWarnings("unused")
    private void onClose(final ActionEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.GAME));
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
        buttonsBar.setButtonText(i18n.get(TurnEvents.buttonBack));
    }
}
