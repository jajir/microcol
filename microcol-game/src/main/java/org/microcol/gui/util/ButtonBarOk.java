package org.microcol.gui.util;

import org.microcol.gui.Loc;
import org.microcol.i18n.I18n;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Component containing one button. By default with text 'Ok'.
 */
public class ButtonBarOk implements JavaFxComponent {

    public static final String BUTTON_OK_ID = "buttonOk";

    private final HBox mainBox = new HBox();

    private final Button buttonOk;

    public ButtonBarOk(final I18n i18n) {
        this(i18n.get(Loc.ok));
    }

    public ButtonBarOk(final String buttonOkLabel) {
        buttonOk = new Button(buttonOkLabel);
        buttonOk.requestFocus();
        buttonOk.setId(BUTTON_OK_ID);
        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        mainBox.setId("buttonPane");
        mainBox.getChildren().addAll(spacer, buttonOk);
    }

    public Button getButtonOk() {
        return buttonOk;
    }

    @Override
    public Region getContent() {
        return mainBox;
    }

}
