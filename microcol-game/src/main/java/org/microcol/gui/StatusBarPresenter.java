package org.microcol.gui;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.UpdatableLanguage;
import org.microcol.i18n.I18n;
import org.microcol.model.Calendar;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.RoundStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Label;

@Listener
public final class StatusBarPresenter implements UpdatableLanguage {

    private final GameModelController gameModelController;

    private final Text text;

    private final StatusBarMessageController statusBarMessageController;

    private StatusBarView statusBarView;

    @Inject
    public StatusBarPresenter(final StatusBarMessageController statusBarMessageController,
            final Text text, final GameModelController gameModelController) {
        this.text = Preconditions.checkNotNull(text);
        this.statusBarMessageController = Preconditions.checkNotNull(statusBarMessageController);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        statusBarMessageController.addRunLaterListener(event -> {
            statusBarView.getStatusBarDescription().setText(event.getStatusMessage());
        });
    }

    void setStatusBarView(final StatusBarView statusBarView) {
        this.statusBarView = Preconditions.checkNotNull(statusBarView);
        statusBarView.getLabelEra().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("statusBar.era.description")));
        });
        statusBarView.getLabelGold().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("statusBar.gold.description")));
        });
        statusBarView.getStatusBarDescription().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("statusBar.status.description")));
        });
    }

    @Subscribe
    private void onRoundStarted(final RoundStartedEvent event) {
        Platform.runLater(() -> {
            setYearText(statusBarView.getLabelEra(), event.getCalendar());
        });
    }

    @Subscribe
    private void onGoldChange(final GoldWasChangedEvent event) {
        Platform.runLater(() -> {
            setGoldText(statusBarView.getLabelGold(), event.getNewValue());
        });
    }

    @Override
    public void updateLanguage(final I18n i18n) {
        if (gameModelController.isModelReady()) {
            setYearText(statusBarView.getLabelEra(), gameModelController.getModel().getCalendar());
            setGoldText(statusBarView.getLabelGold(),
                    gameModelController.getHumanPlayer().getGold());
        } else {
            setGoldText(statusBarView.getLabelGold(), 0);
        }
        statusBarView.getStatusBarDescription().setText("");
    }

    private void setYearText(final Label labelEra, final Calendar calendar) {
        Preconditions.checkNotNull(labelEra);
        Preconditions.checkNotNull(calendar);
        String date = text.get("statusBar.era") + " " + calendar.getCurrentYear() + " AD";
        if (calendar.getCurrentSeason().isPresent()) {
            date += " "
                    + text.get("statusBar.season." + calendar.getCurrentSeason().get().getKey());
        }
        labelEra.setText(date);
    }

    private void setGoldText(final Label labelGold, final int gold) {
        Preconditions.checkNotNull(labelGold);
        labelGold.setText(text.get("statusBar.gold") + " " + gold);
    }

}
