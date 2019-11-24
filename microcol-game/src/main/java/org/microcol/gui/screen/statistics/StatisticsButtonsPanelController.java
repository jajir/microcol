package org.microcol.gui.screen.statistics;

import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class StatisticsButtonsPanelController {

    @Inject
    public StatisticsButtonsPanelController(final StatisticsButtonsPanel statisticsButtonsPanel,
            final EventBus eventBus) {
        statisticsButtonsPanel.getButtonClose()
                .setOnAction(event -> eventBus.post(new ShowScreenEvent(Screen.GAME)));
    }

}
