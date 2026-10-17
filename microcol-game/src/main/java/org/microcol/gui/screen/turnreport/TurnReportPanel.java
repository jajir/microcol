package org.microcol.gui.screen.turnreport;

import java.util.Optional;

import org.microcol.gui.Loc;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.GameScreen;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.i18n.I18n;
import org.microcol.model.Colony;
import org.microcol.model.Model;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.control.Button;
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

    private final EventBus eventBus;

    @Inject
    TurnReportPanel(final I18n i18n,
            final GameModelController gameModelController,
            final TurnReportView turnReportView, final TeService teService,
            final EventBus eventBus) {
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.turnReportView = Preconditions.checkNotNull(turnReportView);
        this.teService = Preconditions.checkNotNull(teService);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        turnEventsPanel.getStyleClass().add("turnEventPanel");
        mainPanel.getChildren().addAll(turnEventsPanel);
    }

    @Override
    public void beforeShow() {
        turnEventsPanel.getChildren().clear();
        if (gameModelController.getModel().isTurnEventsMessagesEmpty(
                gameModelController.getHumanPlayer())) {
            turnEventsPanel.getChildren()
                    .add(new Label(i18n.get(Loc.turnReport_noEvents)));
        } else {
            final Model shownModel = gameModelController.getModel();
            teService.getMessages()
                    .forEach(turnEvent -> turnEventsPanel.getChildren()
                            .add(createEventPanel(turnEvent, shownModel)));
        }
    }

    private TurnEventPanel createEventPanel(final TeItem turnEvent,
            final Model shownModel) {
        if (turnEvent instanceof TeItemColony) {
            final int colonyId = ((TeItemColony) turnEvent).getColonyId();
            if (findOwnedColony(shownModel, colonyId).isPresent()) {
                final Button open = new Button(i18n.get(TurnEvents.openColony));
                open.setId("turnEventOpenColony-" + colonyId);
                open.setOnAction(event -> openColony(shownModel, colonyId));
                return new TurnEventPanel(turnEvent, open);
            }
        }
        return new TurnEventPanel(turnEvent);
    }

    private Optional<Colony> findOwnedColony(final Model shownModel,
            final int colonyId) {
        if (!gameModelController.isGameModelReady()
                || gameModelController.getModel() != shownModel) {
            return Optional.empty();
        }
        return shownModel.getColonyById(colonyId).filter(Colony::isValid)
                .filter(colony -> colony.getOwner()
                        .equals(gameModelController.getHumanPlayer()));
    }

    /**
     * Open the colony only while the report still belongs to the current game
     * and the colony is still owned by the human player. A stale report of a
     * replaced game returns to that game's map; a lost colony refreshes the
     * report without its action. Navigation itself spends nothing.
     */
    private void openColony(final Model shownModel, final int colonyId) {
        final Optional<Colony> colony = findOwnedColony(shownModel, colonyId);
        if (colony.isPresent()) {
            eventBus.post(new ShowScreenEvent(Screen.COLONY, colony.get()));
        } else if (!gameModelController.isGameModelReady()) {
            return;
        } else if (gameModelController.getModel() != shownModel) {
            eventBus.post(new ShowScreenEvent(Screen.GAME));
        } else {
            beforeShow();
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
