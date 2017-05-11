package org.microcol.gui;

import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * About MicroCol game dialog.
 */
public class AboutDialog {

	private final Stage dialog;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param viewUtil
	 *            required tool for centering window on screen
	 * @param text
	 *            required localization helper class
	 */
	public AboutDialog(final ViewUtil viewUtil, final Text text) {
		dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(viewUtil.getParentFrame());
		dialog.setTitle("Tato jednotka neumi bojovat");

		VBox root = new VBox();
		Scene scene = new Scene(root);
		dialog.setScene(scene);

		final Label label = new Label("<html>About MicroCol<br/>Simple Colonization remake.<br/>...</html>");

		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			dialog.close();
		});
		buttonOk.requestFocus();
		root.getChildren().addAll(label, buttonOk);

		dialog.showAndWait();
	}

}
