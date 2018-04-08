package org.microcol.gui.event.model;

import org.microcol.gui.DialogMessage;
import org.microcol.gui.MainPanelPresenter;
import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;

public class CallBackContext {

    private final DialogMessage dialogMessage;

    private final MainPanelPresenter mainPanelPresenter;

    private final Text text;

    public CallBackContext(final DialogMessage dialogMessage,
            final MainPanelPresenter mainPanelPresenter, final Text text) {
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
        this.mainPanelPresenter = Preconditions.checkNotNull(mainPanelPresenter);
        this.text = Preconditions.checkNotNull(text);
    }

    public void showMessage(final String... messageKeys) {
        for (final String messageKey : messageKeys) {
            dialogMessage.setText(text.get(messageKey));
            dialogMessage.showAndWait();
        }
    }

    public void goToGameMenu() {
        mainPanelPresenter.showGameMenu();
    }

}
