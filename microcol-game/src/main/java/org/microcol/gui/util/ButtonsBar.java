package org.microcol.gui.util;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * Helps manage button part of dialog containing "OK" button.
 *
 */
public class ButtonsBar extends HBox {

    private final Button buttonOk;

    public ButtonsBar(final Text text) {
        this(text.get(AbstractMessageWindow.KEY_DIALOG_OK));
    }

    public ButtonsBar(final String buttonOkLabel) {
        buttonOk = new Button(buttonOkLabel);
        buttonOk.requestFocus();
        buttonOk.setId("buttonOk");
        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        setId("buttonPane");
        getChildren().addAll(spacer, buttonOk);
    }

    public Button getButtonOk() {
        return buttonOk;
    }

}
