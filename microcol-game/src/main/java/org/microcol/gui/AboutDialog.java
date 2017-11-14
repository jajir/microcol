package org.microcol.gui;

import org.microcol.gui.util.AbstractMessageWindow;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * About MicroCol game dialog.
 */
public class AboutDialog extends AbstractMessageWindow {

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param viewUtil
	 *            required tool for centering window on screen
	 * @param text
	 *            required localization helper class
	 */
	public AboutDialog(final ViewUtil viewUtil, final Text text) {
		super(viewUtil);
		VBox root = new VBox();
		init(root);
		getDialog().setTitle("Tato jednotka neumi bojovat");


		final Label label = new Label("<html>About MicroCol<br/>Simple Colonization remake.<br/>...</html>");

		final Button buttonOk = new Button(text.get("dialog.ok"));
		buttonOk.setOnAction(e -> {
			getDialog().close();
		});
		buttonOk.requestFocus();
		root.getChildren().addAll(label, buttonOk);

		getDialog().showAndWait();
	}

}
