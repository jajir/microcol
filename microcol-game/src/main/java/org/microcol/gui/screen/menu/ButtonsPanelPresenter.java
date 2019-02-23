package org.microcol.gui.screen.menu;

import org.microcol.gui.dialog.ApplicationController;
import org.microcol.gui.dialog.PersistingDialog;
import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.event.QuitGameEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.preferences.GamePreferences;
import org.microcol.gui.screen.setting.ShowDefaultCampaignMenuEvent;
import org.microcol.gui.screen.setting.ShowGameSettingEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.PersistingTool;
import org.microcol.i18n.I18n;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.layout.Region;

/**
 * Panel that is visible after game start.
 */
@Listener
public final class ButtonsPanelPresenter {

    private final ButtonsPanelView view;

    private final GamePreferences gamePreferences;

    private final PersistingTool persistingTool;

    private final GameController gameController;

    private final CampaignManager campaignManager;

    private final I18n i18n;

    private final EventBus eventBus;

    @Inject
    public ButtonsPanelPresenter(final ButtonsPanelView view,
            final ApplicationController applicationController,
            final PersistingDialog persistingDialog, final GamePreferences gamePreferences,
            final PersistingTool persistingTool, final GameController gameController,
            final CampaignManager campaignManager, final EventBus eventBus, final I18n i18n) {
        this.view = Preconditions.checkNotNull(view);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.i18n = Preconditions.checkNotNull(i18n);
        view.getButtonContinue().setOnAction(this::onGameContinue);
        view.getButtonLoadSave().setOnAction(event -> persistingDialog.loadModel());
        view.getButtonPlayCampaign()
                .setOnAction(event -> eventBus.post(new ShowDefaultCampaignMenuEvent()));
        view.getButtonExitMicroCol().setOnAction(event -> eventBus.post(new QuitGameEvent()));
        view.getButtonSetting().setOnAction(this::onSetting);
        view.getButtonStartFreeGame().setOnAction(e -> applicationController.startNewFreeGame());
        refresh();
    }

    @Subscribe
    private void onChangeLanguageEvent(
            @SuppressWarnings("unused") final ChangeLanguageEvent event) {
        view.updateLanguage(i18n);
    }

    @SuppressWarnings("unused")
    private void onSetting(final ActionEvent event) {
        eventBus.post(new ShowGameSettingEvent());
    }

    @SuppressWarnings("unused")
    private void onGameContinue(final ActionEvent actionEvent) {
        gameController.loadModelFromFile(persistingTool.getAutoSaveFile());
    }

    public void refresh() {
        view.setContinueEnabled(gamePreferences.getGameInProgressSaveFile().isPresent()
                && persistingTool.getAutoSaveFile().exists());
        final Campaign defaultCampain = campaignManager.getDefaultCampain();
        view.setFreeGameEnabled(defaultCampain.isFinished());
    }

    public Region getBox() {
        return view.getContent();
    }

}
