package org.microcol.gui.util;

import org.microcol.gui.Dialog;
import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * Show No/Yes buttons at the bottom of dialog window.
 */
public final class ButtonsBarYesNo extends HBox {

    private final Button buttonYes;

    private final Button buttonNo;

    public ButtonsBarYesNo(final I18n i18n) {
        this(i18n.get(Loc.ok), i18n.get(Dialog.cancel));
    }

    public ButtonsBarYesNo(final String buttonYesLabel, final String buttonNoLabel) {
        buttonYes = new Button(buttonYesLabel);
        buttonYes.requestFocus();
        buttonYes.setId("buttonYes");

        buttonNo = new Button(buttonNoLabel);
        buttonNo.requestFocus();
        buttonNo.setId("buttonNo");

        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        setId("buttonPane");
        getChildren().addAll(buttonNo, spacer, buttonYes);
    }

    public Button getButtonYes() {
        return buttonYes;
    }

    public Button getButtonNo() {
        return buttonNo;
    }

}
