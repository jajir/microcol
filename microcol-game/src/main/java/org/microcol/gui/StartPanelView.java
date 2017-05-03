package org.microcol.gui;

import org.microcol.gui.util.Localized;

import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class StartPanelView implements StartPanelPresenter.Display, Localized {

	private final Button buttonStartNewGame;

	private final VBox box;

	@Inject
	public StartPanelView() {
		box = new VBox();
		buttonStartNewGame = new Button();
		box.getChildren().add(buttonStartNewGame);

		updateLanguage();
	}

	@Override
	public void updateLanguage() {
		buttonStartNewGame.setText(getText().get("startPanel.buttonNewGame"));
	}

	@Override
	public Button getButtonStartNewGame() {
		return buttonStartNewGame;
	}

	public VBox getBox() {
		return box;
	}

}
