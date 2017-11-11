package org.microcol.gui;

import org.microcol.gui.event.ChangeLanguageController;

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
	public StartPanelPresenter(final StartPanelPresenter.Display display,
			final ApplicationController applicationController,
			final ChangeLanguageController changeLanguageController) {
		display.getButtonStartNewGame().setOnAction(e -> applicationController.startNewDefaultGame());
		changeLanguageController.addListener(listener -> display.updateLanguage());
	}

}
