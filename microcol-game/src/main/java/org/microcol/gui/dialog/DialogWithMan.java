package org.microcol.gui.dialog;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.ViewUtil;
import org.microcol.i18n.I18n;

import com.google.inject.Inject;

import javafx.scene.control.Label;

/**
 * Class is used for showing
 */
public final class DialogWithMan extends AbstractWarningDialog {

    @Inject
    public DialogWithMan(final ViewUtil viewUtil, final I18n i18n) {
        super(viewUtil, i18n, Dialog.gameOver_caption);
        getContext().getStyleClass().add("dialog-with-man");
    }

    public void setText(final String text) {
        getContext().getChildren().clear();
        final Label label = new Label(text);
        label.setWrapText(true);
        getContext().getChildren().add(label);
    }

}
