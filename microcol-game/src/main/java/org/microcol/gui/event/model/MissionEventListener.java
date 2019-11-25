package org.microcol.gui.event.model;

import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.dialog.DialogWithMan;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.gui.util.Listener;
import org.microcol.i18n.I18n;
import org.microcol.model.campaign.EventFinishMission;
import org.microcol.model.campaign.EventShowDialogWithMan;
import org.microcol.model.campaign.EventShowMessages;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import javafx.application.Platform;

/**
 * Class is called from mission instance when mission want's to say something to
 * player.
 */
@Listener
public final class MissionEventListener {

    private final I18n i18n;

    private final DialogMessage dialogMessage;

    private final DialogWithMan dialogWithMan;

    private final EventBus eventBus;

    @Inject
    MissionEventListener(final I18n i18n, final DialogMessage dialogMessage,
            final DialogWithMan dialogWithMan, final EventBus eventBus) {
        this.i18n = Preconditions.checkNotNull(i18n);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.dialogWithMan = Preconditions.checkNotNull(dialogWithMan);
        this.eventBus = Preconditions.checkNotNull(eventBus);
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
        Platform.runLater(() -> {
            event.getMessages().forEach(messageKey -> {
                dialogMessage.setText(i18n.get(messageKey));
                dialogMessage.showAndWait();
            });
            goToGameMenu();
        });
    }

    private void goToGameMenu() {
        eventBus.post(new ShowScreenEvent(Screen.CAMPAIGN));
    }

    public EventBus getEventBus() {
        return eventBus;
    }

}
