package org.microcol.gui.util;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * Show No/Yes buttons at the bottom of dialog window.
 */
public class ButtonsBarYesNo extends HBox {

	private final Button buttonYes;

	private final Button buttonNo;

	public ButtonsBarYesNo(final Text text) {
		this(text.get(AbstractMessageWindow.KEY_DIALOG_OK), text.get(AbstractMessageWindow.KEY_DIALOG_CANCEL));
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
