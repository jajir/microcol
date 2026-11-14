package org.akropia.gui.util;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.Loc;
import org.akropia.gui.dialog.Dialog;
import org.akropia.i18n.I18n;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;

/**
 * Show No/Yes buttons at the bottom of dialog window.
 */
public final class ButtonsBarYesNo implements JavaFxComponent {

    private final ModalActions mainBox;

    private final Button buttonYes;

    private final Button buttonNo;

    public ButtonsBarYesNo(final I18n i18n) {
        this(i18n.get(Loc.ok), i18n.get(Dialog.cancel));
    }

    public ButtonsBarYesNo(final String buttonYesLabel,
            final String buttonNoLabel) {
        buttonYes = ActionButtons.button(ButtonIcon.CONFIRM, buttonYesLabel);

        buttonYes.setId("buttonYes");

        buttonNo = ActionButtons.button(ButtonIcon.CANCEL, buttonNoLabel);
        buttonNo.requestFocus();
        buttonNo.setId("buttonNo");

        buttonNo.setCancelButton(true);
        mainBox = new ModalActions(buttonYes, buttonNo);
        mainBox.setId("buttonPane");
    }

    public Button getButtonYes() {
        return buttonYes;
    }

    public Button getButtonNo() {
        return buttonNo;
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

}
