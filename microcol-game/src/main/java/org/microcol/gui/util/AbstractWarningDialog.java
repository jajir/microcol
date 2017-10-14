package org.microcol.gui.util;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public abstract class AbstractWarningDialog extends AbstractDialog {

	/**
	 * Default constructor.
	 * 
	 * @param viewUtil
	 *            required utility class for showing dialog
	 * @param text
	 *            required localization tool
	 * @param messageKey
	 *            required message key
	 */
	public AbstractWarningDialog(final ViewUtil viewUtil, final Text text, final String messageKey) {
		super(viewUtil);
		getDialog().setTitle(text.get(messageKey));

		final VBox root = new VBox();
		init(root);

		final Label label = new Label(text.get(messageKey));

		/**
		 * Buttons
		 */
		final Button buttonFight = new Button(text.get(KEY_DIALOG_OK));
		buttonFight.setOnAction(e -> {
			getDialog().close();
		});

		buttonFight.requestFocus();
		root.getChildren().addAll(label, buttonFight);

		getDialog().showAndWait();
	}
}
