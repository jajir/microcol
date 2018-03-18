package org.microcol.gui.event.model;

import org.microcol.gui.DialogMessage;
import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

/**
 * Class is called from mission instance when mission want's to say something to
 * player.
 *
 *
 * TODO change it interface and move to campaign package.
 */
public class MissionCallBack {

    private final Text text;

    private final DialogMessage dialogMessage;

    @Inject
    MissionCallBack(final Text text, final DialogMessage dialogMessage) {
        this.text = Preconditions.checkNotNull(text);
        this.dialogMessage = Preconditions.checkNotNull(dialogMessage);
    }

    public void showMessage(final String messageKey) {
        dialogMessage.setText(text.get(messageKey));
        dialogMessage.showAndWait();
    }

}
