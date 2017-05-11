package org.microcol.gui;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import org.microcol.gui.util.Text;
import org.microcol.gui.util.ViewUtil;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class NewGameDialog {

	private final Stage dialog;

	/**
	 * Constructor when parentFrame is not available.
	 * 
	 * @param viewUtil
	 *            required tool for centering window on screen
	 * @param text
	 *            required localization helper class
	 */
	public NewGameDialog(final ViewUtil viewUtil, final Text text) {
		dialog = new Stage();
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(viewUtil.getParentFrame());
		dialog.setTitle(text.get("newGameDialog.title"));

		GridPane root = new GridPane();
		root.setAlignment(Pos.CENTER);
		root.setHgap(10);
		root.setVgap(10);
		root.setPadding(new Insets(25, 25, 25, 25));

		Scene scene = new Scene(root);
		dialog.setScene(scene);

		// Y=0
		final Label labelSelectMap = new Label(text.get("newGameDialog.selectMap"));
		root.add(labelSelectMap, 0, 0);

		final ComboBox<String> comboBoxSelectMap = new ComboBox<>();
		comboBoxSelectMap.setItems(FXCollections.observableArrayList(getMaps()));
		if (getMaps().length > 0) {
			comboBoxSelectMap.setValue(getMaps()[0]);
		}
		comboBoxSelectMap.setEditable(false);
		root.add(comboBoxSelectMap, 1, 0);

		// Y=1
		final Button buttonCancel = new Button(text.get("newGameDialog.cancel"));
		buttonCancel.setOnAction(e -> {
			dialog.close();
		});
		root.add(buttonCancel, 0, 1);

		final Button buttonStartGame = new Button(text.get("newGameDialog.startGame"));
		buttonStartGame.setOnAction(e -> {
			// TODO JJ selected map should be used.
			// final String selectedMap = (String)
			// comboBoxSelectMap.getSelectedItem();
			dialog.close();
		});
		buttonStartGame.requestFocus();
		root.add(buttonStartGame, 1, 1);

		dialog.showAndWait();
	}

	private String[] getMaps() {
		try {
			final URL resource = ClassLoader.getSystemClassLoader().getResource("maps");
			File directory = new File(resource.toURI());
			String[] out = new String[directory.listFiles().length];
			int i = 0;
			for (final File f : directory.listFiles()) {
				out[i] = f.getName();
				i++;
			}
			return out;
		} catch (URISyntaxException e) {
			throw new MicroColException(e.getMessage(), e);
		}
	}

}
