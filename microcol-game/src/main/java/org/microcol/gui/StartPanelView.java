package org.microcol.gui;

import org.microcol.gui.util.Text;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

/**
 * In main area shows basic menu "Start new game".
 */
public class StartPanelView implements StartPanelPresenter.Display {

	private final Text text;
	
	private final Button buttonStartNewGame;

	private final VBox box;

	@Inject
	StartPanelView(final Text text) {
		this.text = Preconditions.checkNotNull(text);
		box = new VBox();
		box.setStyle("-fx-pref-width: 100000; -fx-pref-height: 100000;");
		box.setAlignment(Pos.CENTER);
		buttonStartNewGame = new Button();
		box.getChildren().add(buttonStartNewGame);
		setLocalizedText();
	}

	@Override
	public void updateLanguage() {
		setLocalizedText();
	}
	
	private void setLocalizedText(){
		buttonStartNewGame.setText(text.get("startPanel.buttonNewGame"));
	}

	@Override
	public Button getButtonStartNewGame() {
		return buttonStartNewGame;
	}

	public VBox getBox() {
		return box;
	}

}
