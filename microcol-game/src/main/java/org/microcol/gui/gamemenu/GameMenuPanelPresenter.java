package org.microcol.gui.gamemenu;

import org.microcol.gui.ApplicationController;
import org.microcol.gui.PersistingDialog;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.QuitGameController;
import org.microcol.gui.mainmenu.QuitGameEvent;
import org.microcol.gui.util.GamePreferences;
import org.microcol.gui.util.PersistingTool;
import org.microcol.model.campaign.Campaign;
import org.microcol.model.campaign.CampaignManager;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.event.ActionEvent;
import javafx.scene.layout.VBox;

/**
 * Panel that is visible after game start.
 */
public final class GameMenuPanelPresenter {

    private final GameMenuPanelView view;

    private final GamePreferences gamePreferences;

    private final PersistingTool persistingTool;

    private final GameController gameController;

    private final CampaignManager campaignManager;

    @Inject
    public GameMenuPanelPresenter(final GameMenuPanelView view,
            final ApplicationController applicationController,
            final ChangeLanguageController changeLanguageController,
            final QuitGameController exitGameController, final PersistingDialog persistingDialog,
            final GamePreferences gamePreferences, final PersistingTool persistingTool,
            final GameController gameController, final CampaignManager campaignManager,
            final ShowDefaultCampaignMenuControler showDefaultCampaignMenuControler) {
        this.view = Preconditions.checkNotNull(view);
        this.gamePreferences = Preconditions.checkNotNull(gamePreferences);
        this.persistingTool = Preconditions.checkNotNull(persistingTool);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.campaignManager = Preconditions.checkNotNull(campaignManager);
        view.getButtonContinue().setOnAction(this::onGameContinue);
        view.getButtonLoadSave().setOnAction(event -> persistingDialog.loadModel());
        view.getButtonPlayCampaign().setOnAction(event -> showDefaultCampaignMenuControler
                .fireEvent(new ShowDefaultCampaignMenuEvent()));
        view.getButtonExitMicroCol()
                .setOnAction(event -> exitGameController.fireEvent(new QuitGameEvent()));
        view.getButtonStartFreeGame().setOnAction(e -> applicationController.startNewFreeGame());
        changeLanguageController.addListener(listener -> view.updateLanguage());
        refresh();
    }

    @SuppressWarnings("unused")
    private void onGameContinue(final ActionEvent actionEvent) {
        gameController.startModelFromFile(persistingTool.getAutoSaveFile());
    }

    public void refresh() {
        view.setContinueEnabled(gamePreferences.getGameInProgressSaveFile().isPresent()
                && persistingTool.getAutoSaveFile().exists());
        final Campaign defaultCampain = campaignManager.getDefaultCampain();
        view.setFreeGameEnabled(defaultCampain.isFinished());
    }

    public VBox getBox() {
        return view.getPanel();
    }

}
