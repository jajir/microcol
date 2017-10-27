package org.microcol.gui;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.model.GameModelController;

import com.google.inject.Inject;

import javafx.scene.control.Button;

/**
 * Panel that is visible after game start.
 */
public class StartPanelPresenter {

	public interface Display {

		void updateLanguage();

		Button getButtonStartNewGame();

	}

	@Inject
	public StartPanelPresenter(final StartPanelPresenter.Display display, final GameModelController gameController,
			final ChangeLanguageController changeLanguageController) {
		display.getButtonStartNewGame().setOnAction(e -> gameController.startNewGame());
		changeLanguageController.addListener(listener -> display.updateLanguage());
	}

}
