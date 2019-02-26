package org.microcol.gui.screen.campaign;

import java.util.function.Consumer;

import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.model.campaign.CampaignNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.scene.control.Button;

/**
 * Panel that is visible after game start.
 */
@Listener
public final class ScreenCampaignPresenter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final GameController gameController;

    private final ScreenCampaignPresenter.Display display;

    private final EventBus eventBus;

    public interface Display {

        void updateLanguage();

        Button getButtonBack();

        void setOnSelectedMission(final Consumer<String> onSelectedMission);

    }

    @Inject
    public ScreenCampaignPresenter(final ScreenCampaignPresenter.Display display,
            final EventBus eventBus, final GameController gameController) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.display = Preconditions.checkNotNull(display);
        this.gameController = Preconditions.checkNotNull(gameController);
        display.getButtonBack()
                .setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.GAME_MENU)));
        display.setOnSelectedMission(this::onSelectedMission);
    }

    @Subscribe
    private void onChangeLanguage(@SuppressWarnings("unused") final ChangeLanguageEvent event) {
        display.updateLanguage();
    }

    private void onSelectedMission(final String missionName) {
        logger.debug("Mission {} was selected to play.", missionName);
        gameController.startCampaignMission(CampaignNames.defaultCampaign, missionName);
        eventBus.post(new ShowScreenEvent(Screen.GAME));
    }

}