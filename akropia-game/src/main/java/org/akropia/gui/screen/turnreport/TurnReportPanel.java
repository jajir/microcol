package org.akropia.gui.screen.turnreport;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;
import org.akropia.model.Location;
import org.akropia.model.turnevent.TurnEvent;
import org.akropia.gui.event.CenterViewEvent;
import org.akropia.gui.screen.game.gamepanel.SelectedTileManager;
import org.akropia.gui.screen.game.gamepanel.ScrollToFocusedTile;

import org.akropia.gui.Loc;
import org.akropia.gui.event.model.GameModelController;
import org.akropia.gui.screen.GameScreen;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.i18n.I18n;
import org.akropia.model.Colony;
import org.akropia.model.Model;

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

    private final EuropeOpeningRequest europeOpeningRequest;

    private final SelectedTileManager selectedTileManager;
    private Model previousModel;
    private List<TurnEvent> previousEvents = List.of();

    @Inject
    TurnReportPanel(final I18n i18n,
            final GameModelController gameModelController,
            final TurnReportView turnReportView, final TeService teService,
            final EventBus eventBus,
            final EuropeOpeningRequest europeOpeningRequest,
            final SelectedTileManager selectedTileManager) {
        this.selectedTileManager = selectedTileManager;
        this.europeOpeningRequest = Preconditions
                .checkNotNull(europeOpeningRequest);
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
        final Model current = gameModelController.getModel();
        final List<TurnEvent> events = current.getTurnEventsLocalizedMessages(
                gameModelController.getHumanPlayer());
        if (current != previousModel || !events.equals(previousEvents)) {
            turnReportView.resetScrollPosition();
        }
        previousModel = current;
        previousEvents = List.copyOf(events);
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

    private TurnEventPanel createEventPanel(final TeItemSimple turnEvent,
            final Model shownModel) {
        final List<Button> actions = new ArrayList<>();
        Optional<Location> location = turnEvent.getLocation();
        if (turnEvent instanceof TeItemColony) {
            final int colonyId = ((TeItemColony) turnEvent).getColonyId();
            if (location.isEmpty()) {
                location = shownModel.getColonyById(colonyId)
                        .filter(Colony::isValid)
                        .filter(colony -> gameModelController.getHumanPlayer()
                                .isVisible(colony.getLocation()))
                        .map(Colony::getLocation);
            }
            if (findOwnedColony(shownModel, colonyId).isPresent()) {
                final Button open = ActionButtons.button(ButtonIcon.COLONIES,
                        i18n.get(TurnEvents.openColony));
                open.setId("turnEventOpenColony-" + colonyId);
                open.setOnAction(event -> openColony(shownModel, colonyId));
                actions.add(open);
            }
        }
        if (turnEvent instanceof TeItemEurope) {
            final Button open = ActionButtons.button(ButtonIcon.EUROPE,
                    i18n.get(TurnEvents.openEurope));
            open.setId("turnEventOpenEurope");
            open.setOnAction(event -> openEurope(shownModel));
            actions.add(open);
        }
        location.filter(shownModel.getMap()::isValid).ifPresent(at -> {
            final Button map = ActionButtons.button(ButtonIcon.CENTER,
                    i18n.get(TurnEvents.showOnMap));
            map.setId("turnEventShowOnMap-" + at.getX() + "-" + at.getY());
            map.setOnAction(event -> openMap(shownModel, at));
            actions.add(map);
        });
        return new TurnEventPanel(turnEvent, actions.toArray(new Button[0]));
    }

    /**
     * Open Europe only while the report still belongs to the current game. A
     * stale report of a replaced game returns to that game's map instead.
     * Navigation itself spends nothing.
     */
    private void openEurope(final Model shownModel) {
        if (!gameModelController.isGameModelReady()) {
            return;
        }
        if (gameModelController.getModel() != shownModel) {
            eventBus.post(new ShowScreenEvent(Screen.GAME));
        } else {
            eventBus.post(new ShowScreenEvent(Screen.EUROPE));
        }
    }

    private void openMap(final Model shownModel, final Location at) {
        if (!gameModelController.isGameModelReady()) {
            return;
        }
        if (gameModelController.getModel() == shownModel
                && shownModel.getMap().isValid(at)) {
            selectedTileManager.setSelectedTile(at,
                    ScrollToFocusedTile.smoothScroll);
            eventBus.post(new ShowScreenEvent(Screen.GAME));
            // Center even if this tile was already selected before the report.
            eventBus.post(new CenterViewEvent());
        } else {
            eventBus.post(new ShowScreenEvent(Screen.GAME));
        }
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

    /**
     * Leaving the report by any route (Close, Escape, Open actions) ends the
     * pending Europe request of this turn, so reopening it cannot replay it.
     */
    @Override
    public void beforeHide() {
        europeOpeningRequest.clear();
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
