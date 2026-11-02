package org.akropia.gui.screen.campaign;

import java.util.function.Consumer;

import org.akropia.gui.dialog.PersistingService;
import org.akropia.gui.event.model.GameController;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.util.Listener;
import org.akropia.model.campaign.CampaignNames;
import org.akropia.model.campaign.Default_missionNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import javafx.scene.control.Button;

/**
 * Panel that is visible after game start.
 */
@Listener
public final class CampaignPresenter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final GameController gameController;

    private final EventBus eventBus;

    private final PersistingService persistingService;

    public interface Display {

        Button getButtonBack();

        void setOnSelectedMission(final Consumer<String> onSelectedMission);

    }

    @Inject
    public CampaignPresenter(final CampaignPresenter.Display display,
            final EventBus eventBus, final GameController gameController,
            final PersistingService persistingService) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameController = Preconditions.checkNotNull(gameController);
        this.persistingService = Preconditions.checkNotNull(persistingService);
        display.getButtonBack().setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.MENU)));
        display.setOnSelectedMission(this::onSelectedMission);
    }

    private void onSelectedMission(final String missionName) {
        logger.debug("Mission {} was selected to play.", missionName);
        final Default_missionNames name = Default_missionNames
                .valueOf(missionName);
        gameController.startCampaignMission(CampaignNames.defaultCampaign,
                name);
        eventBus.post(new ShowScreenEvent(Screen.GAME));
        persistingService.checkpointNewGame();
    }

}
