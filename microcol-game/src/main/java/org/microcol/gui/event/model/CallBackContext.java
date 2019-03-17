package org.microcol.gui.event.model;

import org.microcol.gui.dialog.DialogMessage;
import org.microcol.gui.screen.Screen;
import org.microcol.gui.screen.ShowScreenEvent;
import org.microcol.i18n.I18n;
import org.microcol.i18n.MessageKeyResource;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.EventBus;

public final class CallBackContext {

    private final DialogMessage dialogMessage;

    private final EventBus eventBus;

    private final I18n text;

    public CallBackContext(final DialogMessage dialogMessage, final EventBus eventBus,
            final I18n text) {
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.eventBus = Preconditions.checkNotNull(eventBus);
        this.text = Preconditions.checkNotNull(text);
    }

    @SuppressWarnings("unchecked")
    public <T extends Enum<T> & MessageKeyResource> void showMessage(final T... messageKeys) {
        for (final T messageKey : messageKeys) {
            dialogMessage.setText(text.get(messageKey));
            dialogMessage.showAndWait();
        }
    }

    public void goToGameMenu() {
        eventBus.post(new ShowScreenEvent(Screen.MENU));
    }

}
