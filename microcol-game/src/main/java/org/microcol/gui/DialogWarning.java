package org.microcol.gui;

import org.microcol.gui.util.ViewUtil;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogWarning {

	private final Stage dialog;

	/**
	 * Default constructor.
	 * 
	 * @param viewUtil
	 *            required utility class for showing dialog
	 */
	public DialogWarning(final ViewUtil viewUtil) {
		dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(viewUtil.getPrimaryStage());
		dialog.setTitle("Tato jednotka neumi bojovat");

		VBox root = new VBox();
		Scene scene = new Scene(root);
		dialog.setScene(scene);

		final Label label = new Label("Tato jednotka neumi bojovat");

		/**
		 * Buttons
		 */
		final Button buttonFight = new Button("Ok");
		buttonFight.setOnAction(e -> {
			dialog.close();
		});

		buttonFight.requestFocus();
		root.getChildren().addAll(label, buttonFight);

		dialog.showAndWait();
	}
}
