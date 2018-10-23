package org.microcol.gui.gamemenu;

import java.util.function.Consumer;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.model.campaign.CampaignNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.control.Button;

/**
 * Panel that is visible after game start.
 */
public final class CampaignMenuPanelPresenter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final GameController gameController;

    private final EventBus eventBus;

    public interface Display {

        void updateLanguage();

        Button getButtonBack();

        void setOnSelectedMission(final Consumer<String> onSelectedMission);

    }

    @Inject
    public CampaignMenuPanelPresenter(final CampaignMenuPanelPresenter.Display display,
            final ChangeLanguageController changeLanguageController, final EventBus eventBus,
            final GameController gameController) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameController = Preconditions.checkNotNull(gameController);
        changeLanguageController.addListener(listener -> display.updateLanguage());
        display.getButtonBack().setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.GAME_MENU)));
        display.setOnSelectedMission(this::onSelectedMission);
    }

    private void onSelectedMission(final String missionName) {
        logger.debug("Mission {} was selected to play.", missionName);
        gameController.startCampaignMission(CampaignNames.defaultCampaign, missionName);
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

}
