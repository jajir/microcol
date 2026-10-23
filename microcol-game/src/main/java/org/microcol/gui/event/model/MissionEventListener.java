package org.microcol.gui.event.model;

import java.util.Optional;

import org.microcol.gui.LocalizationHelper;
import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.dialog.DialogWithMan;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.screen.statistics.HallOfFameDialog;
import org.microcol.gui.util.Listener;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;
import org.microcol.model.HallOfFameEntry;
import org.microcol.model.campaign.EventFinishMission;
import org.microcol.model.campaign.EventShowDialogWithMan;
import org.microcol.model.campaign.EventShowMessages;
import org.microcol.model.campaign.po.HallOfFameDao;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * Class is called from mission instance when mission want's to say something to
 * player. When a scored game ends (R-0193, R-0197), the finished result with
 * its breakdown and place is shown after the end messages and before the
 * campaign screen, so the two windows never overlap.
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

    @Inject
    MissionEventListener(final I18n i18n, final DialogMessage dialogMessage,
            final DialogWithMan dialogWithMan, final EventBus eventBus,
            final GameModelController gameModelController,
            final ViewUtil viewUtil, final HallOfFameDao hallOfFameDao,
            final LocalizationHelper localization) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.dialogWithMan = Preconditions.checkNotNull(dialogWithMan);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.gameModelController = Preconditions
                .checkNotNull(gameModelController);
        this.viewUtil = Preconditions.checkNotNull(viewUtil);
        this.hallOfFameDao = Preconditions.checkNotNull(hallOfFameDao);
        this.localization = Preconditions.checkNotNull(localization);
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
        Platform.runLater(() -> {
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

    private void goToGameMenu() {
        eventBus.post(new ShowScreenEvent(Screen.CAMPAIGN));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

}
