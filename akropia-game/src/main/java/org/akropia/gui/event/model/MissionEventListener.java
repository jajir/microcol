package org.akropia.gui.event.model;

import java.util.Optional;

import org.akropia.gui.LocalizationHelper;
import org.akropia.gui.SuccessCuePlayer;
import org.akropia.gui.dialog.DialogMessage;
import org.akropia.gui.dialog.DialogWithMan;
import org.akropia.gui.screen.Screen;
import org.akropia.gui.screen.ShowScreenEvent;
import org.akropia.gui.screen.statistics.HallOfFameDialog;
import org.akropia.gui.util.Listener;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;
import org.akropia.model.HallOfFameEntry;
import org.akropia.model.campaign.EventFinishMission;
import org.akropia.model.campaign.EventShowDialogWithMan;
import org.akropia.model.campaign.EventShowMessages;
import org.akropia.model.campaign.po.HallOfFameDao;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * Class is called from mission instance when mission want's to say something to
 * player. When a scored game ends (R-0193, R-0197), the finished result with
 * its breakdown and place is shown after the end messages and before the
 * campaign screen, so the two windows never overlap. A won mission's first
 * message is accompanied by the success cue (R-0228).
 */
@Listener
public final class MissionEventListener {

    private final I18n i18n;

    private final DialogMessage dialogMessage;

    private final DialogWithMan dialogWithMan;

    private final EventBus eventBus;

    private final GameModelController gameModelController;

    private final ViewUtil viewUtil;

    private final HallOfFameDao hallOfFameDao;

    private final LocalizationHelper localization;

    private final MissionVictoryCue victoryCue;

    @Inject
    MissionEventListener(final I18n i18n, final DialogMessage dialogMessage,
            final DialogWithMan dialogWithMan, final EventBus eventBus,
            final GameModelController gameModelController,
            final ViewUtil viewUtil, final HallOfFameDao hallOfFameDao,
            final LocalizationHelper localization,
            final SuccessCuePlayer successCuePlayer) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.dialogWithMan = Preconditions.checkNotNull(dialogWithMan);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.hallOfFameDao = Preconditions.checkNotNull(hallOfFameDao);
        this.localization = Preconditions.checkNotNull(localization);
        this.victoryCue = new MissionVictoryCue(successCuePlayer);
    }

    @Subscribe
    private void onShowMessages(final EventShowMessages event) {
        Platform.runLater(() -> event.getMessages().forEach(messageKey -> {
            dialogMessage.setText(i18n.get(messageKey));
            dialogMessage.showAndWait();
        }));
    }

    @Subscribe
    private void onShowDialogWithMan(final EventShowDialogWithMan event) {
        Platform.runLater(() -> event.getMessages().forEach(messageKey -> {
            dialogWithMan.setText(i18n.get(messageKey));
            dialogWithMan.showAndWait();
        }));
    }

    @Subscribe
    private void onFinishMission(final EventFinishMission event) {
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
            finished.ifPresent(
                    entry -> new HallOfFameDialog(viewUtil, i18n, hallOfFameDao,
                            localization, Optional.of(entry)).showAndWait());
            goToGameMenu();
        });
    }

    /**
     * @return the frozen result of the game that has just ended, empty for a
     *         mission without a scored result
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
        eventBus.post(new ShowScreenEvent(Screen.CAMPAIGN));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

}
