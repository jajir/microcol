package org.akropia.gui.screen.goals;

import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GoalsButtonsPanelController {

    @Inject
    public GoalsButtonsPanelController(
            final GoalsButtonsPanel statisticsButtonsPanel,
            final EventBus eventBus) {
        statisticsButtonsPanel.getButtonClose().setOnAction(
                event -> eventBus.post(new ShowScreenEvent(Screen.GAME)));
    }

}
