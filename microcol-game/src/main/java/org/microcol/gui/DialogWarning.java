package org.microcol.gui;

import org.microcol.gui.util.AbstractDialog;
import org.microcol.gui.util.ViewUtil;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class DialogWarning extends AbstractDialog {

	/**
	 * Default constructor.
	 * 
	 * @param viewUtil
	 *            required utility class for showing dialog
	 */
	public DialogWarning(final ViewUtil viewUtil) {
		super(viewUtil);
		getDialog().setTitle("Tato jednotka neumi bojovat");

		VBox root = new VBox();
		init(root);

		final Label label = new Label("Tato jednotka neumi bojovat");

		/**
		 * Buttons
		 */
		final Button buttonFight = new Button("Ok");
		buttonFight.setOnAction(e -> {
			getDialog().close();
		});

		buttonFight.requestFocus();
		root.getChildren().addAll(label, buttonFight);

		getDialog().showAndWait();
	}
}
