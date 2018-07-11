package org.microcol.gui;

import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.GoldWasChangedController;
import org.microcol.gui.event.model.RoundStartedController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Text;
import org.microcol.model.Calendar;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Label;

public final class StatusBarPresenter {

    private final StatusBarPresenter.Display display;

    private final GameModelController gameModelController;

    private final Text text;

    public interface Display {

        Label getStatusBarDescription();

        Label getLabelEra();

        Label getLabelGold();
    }

    @Inject
    public StatusBarPresenter(final StatusBarPresenter.Display display,
            final StatusBarMessageController statusBarMessageController,
            final RoundStartedController roundStartedController,
            final ChangeLanguageController changeLanguangeController,
            final GoldWasChangedController goldWasChangedController, final Text text,
            final GameModelController gameModelController) {
        this.display = Preconditions.checkNotNull(display);
        this.text = Preconditions.checkNotNull(text);
        this.gameModelController = Preconditions.checkNotNull(gameModelController);
        statusBarMessageController.addRunLaterListener(event -> {
            display.getStatusBarDescription().setText(event.getStatusMessage());
        });
        roundStartedController.addRunLaterListener(event -> {
            setYearText(display.getLabelEra(), event.getCalendar());
        });
        changeLanguangeController.addListener(this::onChangeLanguange);
        goldWasChangedController.addRunLaterListener(event -> {
            setGoldText(display.getLabelGold(), event.getNewValue());
        });
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
