package org.microcol.gui.screen.turnreport;

import org.microcol.gui.screen.ShowScreenEvent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TurnReportButtonsPanelController {

    @Inject
    public TurnReportButtonsPanelController(
            final TurnReportButtonsPanel statisticsButtonsPanel,
            final EventBus eventBus,
            final EuropeOpeningRequest europeOpeningRequest) {
        statisticsButtonsPanel.getButtonClose()
                .setOnAction(event -> eventBus.post(new ShowScreenEvent(
                        europeOpeningRequest.takeScreenAfterReport())));
    }

}
