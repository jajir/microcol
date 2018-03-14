package org.microcol.gui;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ExitGameController;
import org.microcol.gui.mainmenu.ExitGameEvent;
import org.microcol.gui.util.PersistingTool;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;

/**
 * Panel that is visible after game start.
 */
public class StartPanelPresenter {

    private final StartPanelView view;

    private final GamePreferences gamePreferences;

    private final PersistingTool persistingTool;

    private final GameController gameController;

    private final MainFramePresenter mainFramePresenter;

    private final CampaignManager campaignManager;

    @Inject
    public StartPanelPresenter(final StartPanelView view,
            final ApplicationController applicationController,
            final ChangeLanguageController changeLanguageController,
            final ExitGameController exitGameController,
            final MainFramePresenter mainFramePresenter, final PersistingDialog persistingDialog,
            final GamePreferences gamePreferences, final PersistingTool persistingTool,
            final GameController gameController, final CampaignManager campaignManager) {
        this.view = Preconditions.checkNotNull(view);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.mainFramePresenter = Preconditions.checkNotNull(mainFramePresenter);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        view.getButtonContinue().setOnAction(this::onGameContinue);
        view.getButtonLoadSave().setOnAction(event -> persistingDialog.loadModel());
        view.getButtonPlayCampaign().setOnAction(
                event -> mainFramePresenter.showPanel(MainFramePresenter.CAMPAIGN_PANEL));
        view.getButtonExitMicroCol()
                .setOnAction(event -> exitGameController.fireEvent(new ExitGameEvent()));
        view.getButtonStartFreeGame().setOnAction(e -> applicationController.startNewDefaultGame());
        changeLanguageController.addListener(listener -> view.updateLanguage());
        refresh();
    }

    @SuppressWarnings("unused")
    private void onGameContinue(final ActionEvent actionEvent) {
        gameController.loadModelFromFile(persistingTool.getAutoSaveFile());
        mainFramePresenter.showPanel(MainFramePresenter.MAIN_GAME_PANEL);
    }

    void refresh() {
        view.setContinueEnabled(gamePreferences.getGameInProgressSaveFile().isPresent()
                && persistingTool.getAutoSaveFile().exists());
        final Campaign defaultCampain = campaignManager.getDefaultCampain();
        view.setFreeGameEnabled(defaultCampain.isFinished());
    }

}
