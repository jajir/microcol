package org.akropia.gui.event.model;

import java.util.Optional;

import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.SuccessCuePlayer;
import org.akropia.gui.dialog.DialogMessage;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.statistics.HallOfFameDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.HallOfFameEntry;
import org.akropia.model.game.EventGameFinished;
import org.akropia.model.game.EventShowMessages;
import org.akropia.model.game.po.HallOfFameDao;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;

/** Presents game outcomes, then returns to the main menu. */
public final class GameEventListener {

    private final I18n i18n;

    private final DialogMessage dialogMessage;

    private final EventBus eventBus;

    private final GameModelController gameModelController;

    private final ViewUtil viewUtil;

    private final HallOfFameDao hallOfFameDao;

    private final LocalizationHelper localization;

    private final GameVictoryCue victoryCue;

    @Inject
    GameEventListener(final I18n i18n, final DialogMessage dialogMessage,
            final EventBus eventBus,
            final GameModelController gameModelController,
            final ViewUtil viewUtil, final HallOfFameDao hallOfFameDao,
            final LocalizationHelper localization,
            final SuccessCuePlayer successCuePlayer) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.hallOfFameDao = Preconditions.checkNotNull(hallOfFameDao);
        this.localization = Preconditions.checkNotNull(localization);
        this.victoryCue = new GameVictoryCue(successCuePlayer);
    }

    @Subscribe
    private void onShowMessages(final EventShowMessages event) {
        Platform.runLater(() -> event.getMessages().forEach(messageKey -> {
            dialogMessage.setText(i18n.get(messageKey));
            dialogMessage.showAndWait();
        }));
    }

    @Subscribe
    private void onGameFinished(final EventGameFinished event) {
        // Captured now, while the finished game is still the current one: the
        // window later shows this snapshot even after the game is left.
        final Optional<HallOfFameEntry> finished = finishedResult();
        final Optional<String> playthrough = finishedPlaythrough();
        Platform.runLater(() -> {
            playthrough.ifPresent(id -> victoryCue
                    .onFinishMessageShown(event.isVictory(), id));
            event.getMessages().forEach(messageKey -> {
                dialogMessage.setText(i18n.get(messageKey));
                dialogMessage.showAndWait();
            });
            if (finished.isPresent()) {
                HallOfFameDialog result = new HallOfFameDialog(viewUtil, i18n,
                        hallOfFameDao, localization, finished,
                        gameModelController.getModel()
                                .canContinueAfterVictory());
                result.showAndWait();
                if (result.isContinueRequested()) {
                    gameModelController.getModel().continueAfterVictory();
                    eventBus.post(new ShowScreenEvent(Screen.GAME));
                    return;
                }
            }
            goToGameMenu();
        });
    }

    /**
     * @return the frozen result of the game that has just ended, empty for a
     *         scenario without a scored result
     */
    private Optional<HallOfFameEntry> finishedResult() {
        return gameModelController.isGameModelReady()
                ? gameModelController.getModel().getHallOfFameEntry()
                : Optional.empty();
    }

    /**
     * @return identity of the game that has just ended, empty without a game
     */
    private Optional<String> finishedPlaythrough() {
        return gameModelController.isGameModelReady()
                ? Optional.of(
                        gameModelController.getGameModel().getPlaythroughId())
                : Optional.empty();
    }

    private void goToGameMenu() {
        eventBus.post(new ShowScreenEvent(Screen.MENU));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

}
