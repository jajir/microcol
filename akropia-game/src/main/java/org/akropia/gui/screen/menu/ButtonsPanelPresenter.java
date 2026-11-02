package org.akropia.gui.screen.menu;

import org.akropia.gui.LocalizationHelper;
import java.util.Optional;

import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.event.QuitGameEvent;
import org.akropia.gui.preferences.GamePreferences;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.statistics.HallOfFameDialog;
import org.akropia.gui.util.Listener;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.campaign.po.HallOfFameDao;

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

    private final ViewUtil viewUtil;

    private final I18n i18n;

    private final HallOfFameDao hallOfFameDao;

    private final LocalizationHelper localization;

    @Inject
    public ButtonsPanelPresenter(final ButtonsPanelView view,
            final PersistingService persistingService,
            final GamePreferences gamePreferences,
            final SecretOption secretOption, final EventBus eventBus,
            final ViewUtil viewUtil, final I18n i18n,
            final HallOfFameDao hallOfFameDao,
            final LocalizationHelper localization) {
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.i18n = Preconditions.checkNotNull(i18n);
        this.hallOfFameDao = Preconditions.checkNotNull(hallOfFameDao);
        this.localization = Preconditions.checkNotNull(localization);
        this.view = Preconditions.checkNotNull(view);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        this.secretOption = Preconditions.checkNotNull(secretOption);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        view.getButtonContinue().setOnAction(this::onGameContinue);
        view.getButtonLoad().setOnAction(this::onLoadWasPressed);
        view.getButtonPlayCampaign().setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.CAMPAIGN)));
        view.getButtonExitAkropia()
                .setOnAction(event -> eventBus.post(new QuitGameEvent()));
        view.getButtonSetting().setOnAction(this::onSetting);
        view.getButtonStartFreeGame().setOnAction(e -> eventBus
                .post(new ShowScreenEvent(Screen.FREE_PLAY_SETUP)));
        view.getButtonHallOfFame().setOnAction(this::onHallOfFame);
        refresh();
    }

    @SuppressWarnings("unused")
    private void onHallOfFame(final ActionEvent event) {
        new HallOfFameDialog(viewUtil, i18n, hallOfFameDao, localization,
                Optional.empty()).showAndWait();
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
