package org.akropia.gui.util;

import org.akropia.gui.util.ActionButtons;
import org.akropia.gui.image.ButtonIcon;
import org.akropia.gui.Loc;
import org.akropia.i18n.I18n;

import javafx.scene.control.Button;
import javafx.scene.layout.Region;

/**
 * Component containing one button. By default with text 'Ok'.
 */
public class ButtonBarOk implements JavaFxComponent {

    public static final String BUTTON_OK_ID = "buttonOk";

    private final ModalActions mainBox;

    private final Button buttonOk;

    /**
     * Created button bat with untitled button.
     */
    public ButtonBarOk() {
        this("[not set]");
    }

    public ButtonBarOk(final I18n i18n) {
        this(i18n.get(Loc.ok));
    }

    public ButtonBarOk(final String buttonOkLabel) {
        buttonOk = ActionButtons.button(ButtonIcon.CONFIRM, buttonOkLabel);
        buttonOk.requestFocus();
        buttonOk.setId(BUTTON_OK_ID);
        mainBox = new ModalActions(buttonOk);
        mainBox.setId("buttonPane");
    }

    public void setButtonText(final String text) {
        buttonOk.setText(text);
    }

    public Button getButtonOk() {
        return buttonOk;
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

}
