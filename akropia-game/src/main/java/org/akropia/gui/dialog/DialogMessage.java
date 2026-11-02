package org.akropia.gui.dialog;

import org.akropia.gui.util.AbstractWarningDialog;
import org.akropia.gui.util.ViewUtil;
import org.akropia.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.control.Label;

/**
 * Class is used for showing
 */
public final class DialogMessage extends AbstractWarningDialog {

    @Inject
    public DialogMessage(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.gameOver_caption);
    }

    public void setText(final String text) {
        getContext().getChildren().clear();
        final Label message = new Label(text);
        message.setWrapText(true);
        message.setPrefWidth(500);
        message.setMaxWidth(500);
        getContext().getChildren().add(message);
    }

}
