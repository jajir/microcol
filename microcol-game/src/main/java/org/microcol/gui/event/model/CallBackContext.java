package org.microcol.gui.event.model;

import org.microcol.gui.DialogMessage;
import org.microcol.gui.mainscreen.Screen;
import org.microcol.gui.mainscreen.ShowScreenEvent;
import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

public final class CallBackContext {

    private final DialogMessage dialogMessage;

    private final EventBus eventBus;

    private final Text text;

    public CallBackContext(final DialogMessage dialogMessage, final EventBus eventBus,
            final Text text) {
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.text = Preconditions.checkNotNull(text);
    }

    public void showMessage(final String... messageKeys) {
        for (final String messageKey : messageKeys) {
            dialogMessage.setText(text.get(messageKey));
            dialogMessage.showAndWait();
        }
    }

    public void goToGameMenu() {
        eventBus.post(new ShowScreenEvent(Screen.GAME_MENU));
    }

}
