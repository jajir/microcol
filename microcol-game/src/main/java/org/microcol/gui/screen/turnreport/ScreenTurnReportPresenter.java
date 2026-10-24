package org.microcol.gui.screen.turnreport;

import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

@Singleton
public class ScreenTurnReportPresenter {

    private final EventBus eventBus;

    private final EuropeOpeningRequest europeOpeningRequest;

    @Inject
    ScreenTurnReportPresenter(final ScreenTurnReport screenTurnReport,
            final EventBus eventBus,
            final EuropeOpeningRequest europeOpeningRequest) {
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.europeOpeningRequest = Preconditions
                .checkNotNull(europeOpeningRequest);
        screenTurnReport.getContent().setOnKeyPressed(this::onKeyPressed);
    }

    private void onKeyPressed(final KeyEvent event) {
        /**
         * Escape closes the report like its Close button does.
         */
        if (KeyCode.ESCAPE == event.getCode()) {
            eventBus.post(new ShowScreenEvent(
                    europeOpeningRequest.takeScreenAfterReport()));
        }
    }

}
