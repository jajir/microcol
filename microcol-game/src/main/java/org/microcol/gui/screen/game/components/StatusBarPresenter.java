package org.microcol.gui.screen.game.components;

import org.microcol.gui.Loc;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.screen.game.components.StatusBarMessageEvent.Source;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.RoundStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Label;

@Listener
final class StatusBarPresenter implements UpdatableLanguage {

    private final Logger logger = LoggerFactory.getLogger(StatusBarPresenter.class);

    private final GameModelController gameModelController;

    private final I18n i18n;

    private final EventBus eventBus;

    private StatusBarView view;

    private Source showEventsFromSource;

    @Inject
    public StatusBarPresenter(final EventBus eventBus, final I18n text,
            final GameModelController gameModelController) {
        this.i18n = Preconditions.checkNotNull(text);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
    }

    @Subscribe
    private void onStatusBarMessageChange(final StatusBarMessageEvent event) {
        Preconditions.checkNotNull(showEventsFromSource,
                "It's not defined which events should be shown in status bar.");
        if (showEventsFromSource == event.getSource()) {
            logger.debug("Setting text at status bar '{}'", event.getStatusMessage());
            view.getStatusBarDescription().setText(event.getStatusMessage());
        }
    }

    void setStatusBarView(final StatusBarView statusBarView) {
        this.view = Preconditions.checkNotNull(statusBarView);
        statusBarView.getLabelEra().setOnMouseEntered(event -> {
            Preconditions.checkNotNull(showEventsFromSource,
                    "It's not defined which events should be shown in status bar.");
            eventBus.post(new StatusBarMessageEvent(i18n.get(Loc.statusBar_era_description),
                    showEventsFromSource));
        });
        statusBarView.getLabelGold().setOnMouseEntered(event -> {
            Preconditions.checkNotNull(showEventsFromSource,
                    "It's not defined which events should be shown in status bar.");
            eventBus.post(new StatusBarMessageEvent(i18n.get(Loc.statusBar_gold_description),
                    showEventsFromSource));
        });
        statusBarView.getStatusBarDescription().setOnMouseEntered(event -> {
            Preconditions.checkNotNull(showEventsFromSource,
                    "It's not defined which events should be shown in status bar.");
            eventBus.post(new StatusBarMessageEvent(i18n.get(Loc.statusBar_status_description),
                    showEventsFromSource));
        });
    }

    @Subscribe
    private void onRoundStarted(final RoundStartedEvent event) {
        Platform.runLater(() -> {
            setYearText(view.getLabelEra(), event.getCalendar());
        });
    }

    @Subscribe
    private void onGoldChange(final GoldWasChangedEvent event) {
        Platform.runLater(() -> {
            setGoldText(event.getNewValue());
        });
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        if (gameModelController.isGameModelReady()) {
            setYearText(view.getLabelEra(), gameModelController.getModel().getCalendar());
            setGoldText(gameModelController.getHumanPlayer().getGold());
        } else {
            setGoldText(0);
        }
        view.getStatusBarDescription().setText("");
    }

    private void setYearText(final Label labelEra, final Calendar calendar) {
        Preconditions.checkNotNull(labelEra);
        Preconditions.checkNotNull(calendar);
        String date = i18n.get(Loc.statusBar_era) + " " + calendar.getCurrentYear() + " AD";
        if (calendar.getCurrentSeason().isPresent()) {
            date += " " + i18n.get(Loc.getTerrainDescription(calendar.getCurrentSeason().get()));
        }
        labelEra.setText(date);
    }

    private void setGoldText(final int gold) {
        getLabelGold().setText(i18n.get(Loc.statusBar_gold) + " " + gold);
    }

    /**
     * @param showEventsFromSource
     *            the showEventsFromSource to set
     */
    void setShowEventsFromSource(final Source showEventsFromSource) {
        this.showEventsFromSource = Preconditions.checkNotNull(showEventsFromSource);
    }

    private Label getLabelGold() {
        return Preconditions.checkNotNull(view.getLabelGold(), "Label gold in null");
    }

}
