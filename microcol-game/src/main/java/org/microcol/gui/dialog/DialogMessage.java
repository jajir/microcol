package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

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
        getContext().getChildren().add(new Label(text));
    }

}
