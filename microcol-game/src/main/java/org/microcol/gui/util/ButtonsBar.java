package org.microcol.gui.util;

import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * Helps manage button part of dialog containing "OK" button.
 *
 */
public final class ButtonsBar extends HBox {

    private final Button buttonOk;

    public ButtonsBar(final I18n i18n) {
        this(i18n.get(Loc.ok));
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
