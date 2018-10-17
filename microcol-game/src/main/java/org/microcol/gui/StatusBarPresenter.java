package org.microcol.gui;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.Text;
import org.microcol.model.Calendar;
import org.microcol.model.event.GoldWasChangedEvent;
import org.microcol.model.event.RoundStartedEvent;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;
import javafx.scene.control.Label;

@Listener
public final class StatusBarPresenter {

    private final StatusBarView display;

    private final GameModelController gameModelController;

    private final Text text;

    @Inject
    public StatusBarPresenter(final StatusBarView display,
            final StatusBarMessageController statusBarMessageController,
            final ChangeLanguageController changeLanguangeController, final Text text,
            final GameModelController gameModelController) {
        this.display = Preconditions.checkNotNull(display);
        this.text = Preconditions.checkNotNull(text);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        statusBarMessageController.addRunLaterListener(event -> {
            display.getStatusBarDescription().setText(event.getStatusMessage());
        });
        changeLanguangeController.addListener(this::onChangeLanguange);
        display.getLabelEra().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("statusBar.era.description")));
        });
        display.getLabelGold().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("statusBar.gold.description")));
        });
        display.getStatusBarDescription().setOnMouseEntered(event -> {
            statusBarMessageController
                    .fireEvent(new StatusBarMessageEvent(text.get("statusBar.status.description")));
        });
    }

    @Subscribe
    private void onRoundStarted(final RoundStartedEvent event) {
        Platform.runLater(() -> {
            setYearText(display.getLabelEra(), event.getCalendar());
        });
    }
 
    @Subscribe
    private void onGoldChange(final GoldWasChangedEvent event){
        Platform.runLater(() -> {
            setGoldText(display.getLabelGold(), event.getNewValue());
        });        
    }

    @SuppressWarnings("unused")
    private void onChangeLanguange(final ChangeLanguageEvent event) {
        if (gameModelController.isModelReady()) {
            setYearText(display.getLabelEra(), gameModelController.getModel().getCalendar());
            setGoldText(display.getLabelGold(), gameModelController.getHumanPlayer().getGold());
        } else {
            setGoldText(display.getLabelGold(), 0);
        }
        display.getStatusBarDescription().setText("");
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
