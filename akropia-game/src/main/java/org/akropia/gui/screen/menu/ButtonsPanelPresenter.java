package org.akropia.gui.screen.menu;

import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.event.QuitGameEvent;
import org.akropia.gui.preferences.DeveloperMode;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.util.Listener;

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

    private final EventBus eventBus;

    private final DeveloperMode developerMode;

    @Inject
    public ButtonsPanelPresenter(final ButtonsPanelView view,
            final PersistingService persistingService,
            final GamePreferences gamePreferences,
            final SecretOption secretOption, final EventBus eventBus,
            final DeveloperMode developerMode) {
        this.view = Preconditions.checkNotNull(view);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        this.secretOption = Preconditions.checkNotNull(secretOption);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        view.getButtonContinue().setOnAction(this::onGameContinue);
        view.getButtonLoad().setOnAction(this::onLoadWasPressed);
        view.getButtonExitAkropia()
                .setOnAction(event -> eventBus.post(new QuitGameEvent()));
        view.getButtonSetting().setOnAction(this::onSetting);
        view.getButtonStartFreeGame().setOnAction(e -> eventBus
                .post(new ShowScreenEvent(Screen.FREE_PLAY_SETUP)));

        this.developerMode = Preconditions.checkNotNull(developerMode);
        view.getButtonMapEditor().visibleProperty()
                .bind(developerMode.enabledProperty());
        view.getButtonMapEditor().setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.EDITOR)));
        refresh();
    }

    @SuppressWarnings("unused")
    private void onLoadWasPressed(final ActionEvent event) {
        // The developer load route opens the map editor only in developer
        // mode.
        persistingService.loadFromSavedGames(
                secretOption.isEnabled() && developerMode.isEnabled());
    }

    @SuppressWarnings("unused")
    private void onSetting(final ActionEvent event) {
        eventBus.post(new ShowScreenEvent(Screen.SETTING));
    }

    @SuppressWarnings("unused")
    private void onGameContinue(final ActionEvent actionEvent) {
        persistingService.continueGame();
    }

    public void refresh() {
        view.setContinueEnabled(
                gamePreferences.getGameInProgressSaveFile().isPresent()
                        && persistingService.continueFile().isFile());
        view.setFreeGameEnabled(true);
    }

    public Region getBox() {
        return view.getContent();
    }

}
