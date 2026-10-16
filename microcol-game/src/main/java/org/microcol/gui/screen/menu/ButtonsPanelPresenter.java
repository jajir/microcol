package org.microcol.gui.screen.menu;

import org.microcol.gui.dialog.PersistingService;
import org.microcol.gui.event.QuitGameEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.campaign.po.PersistingTool;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.layout.Region;

/**
 * Panel that is visible after game start.
 */
@Listener
public final class ButtonsPanelPresenter {

    private final ButtonsPanelView view;

    private final SecretOption secretOption;

    private final GamePreferences gamePreferences;

    private final PersistingService persistingService;

    private final PersistingTool persistingTool;

    private final GameController gameController;

    private final EventBus eventBus;

    @Inject
    public ButtonsPanelPresenter(final ButtonsPanelView view,
            final PersistingService persistingService,
            final GamePreferences gamePreferences,
            final PersistingTool persistingTool,
            final GameController gameController,
            final SecretOption secretOption, final EventBus eventBus) {
        this.view = Preconditions.checkNotNull(view);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.secretOption = Preconditions.checkNotNull(secretOption);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        view.getButtonContinue().setOnAction(this::onGameContinue);
        view.getButtonLoad().setOnAction(this::onLoadWasPressed);
        view.getButtonPlayCampaign().setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.CAMPAIGN)));
        view.getButtonExitMicroCol()
                .setOnAction(event -> eventBus.post(new QuitGameEvent()));
        view.getButtonSetting().setOnAction(this::onSetting);
        view.getButtonStartFreeGame().setOnAction(e -> eventBus
                .post(new ShowScreenEvent(Screen.FREE_PLAY_SETUP)));
        refresh();
    }

    @SuppressWarnings("unused")
    private void onLoadWasPressed(final ActionEvent event) {
        persistingService.loadFromSavedGames(secretOption.isEnabled());
    }

    @SuppressWarnings("unused")
    private void onSetting(final ActionEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.SETTING));
    }

    @SuppressWarnings("unused")
    private void onGameContinue(final ActionEvent actionEvent) {
        if (gameController
                .loadModelFromFile(persistingTool.getAutoSaveFile())) {
            eventBus.post(new ShowScreenEvent(Screen.GAME));
        }
    }

    public void refresh() {
        view.setContinueEnabled(
                gamePreferences.getGameInProgressSaveFile().isPresent()
                        && persistingTool.getAutoSaveFile().exists());
        view.setFreeGameEnabled(true);
    }

    public Region getBox() {
        return view.getContent();
    }

}
