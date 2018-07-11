package org.microcol.gui;

import org.microcol.gui.util.AbstractWarningDialog;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import com.google.inject.Inject;

import javafx.scene.control.Label;

public final class DialogMessage extends AbstractWarningDialog {

    @Inject
    public DialogMessage(final ViewUtil viewUtil, final Text text) {
        super(viewUtil, text, "dialogGameOver.caption");
        getContext().getChildren().add(new Label("prasopes"));
    }

    public void setText(final String text) {
        getContext().getChildren().clear();
        getContext().getChildren().add(new Label(text));
    }

}
