package org.microcol.gui.util;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public abstract class AbstractWarningDialog extends AbstractDialog {

	private final VBox context = new VBox();

	public AbstractWarningDialog(final ViewUtil viewUtil, final String buttonOkLabel, final String dialogCaption) {
		super(viewUtil);
		getDialog().setTitle(dialogCaption);

		/**
		 * Buttons
		 */
		final ButtonsBar buttonsBar = new ButtonsBar(buttonOkLabel);
		buttonsBar.getButtonOk().setOnAction(e -> {
			getDialog().close();
		});
		buttonsBar.getButtonOk().requestFocus();

		final VBox root = new VBox();
		root.getChildren().addAll(context, buttonsBar);
		init(root);
	}

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
		this(viewUtil, text.get(KEY_DIALOG_OK), text.get(messageKey));
		context.getChildren().add(new Label(text.get(messageKey)));
	}

	public AbstractWarningDialog(final ViewUtil viewUtil, final Text text, final String caption, final String message) {
		this(viewUtil, text.get(KEY_DIALOG_OK), caption);
		context.getChildren().add(new Label(message));
	}

	public void showAndWait() {
		getDialog().showAndWait();
	}

	/**
	 * @return the context
	 */
	public VBox getContext() {
		return context;
	}

}
