package org.microcol.gui;

import javax.swing.JButton;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.model.GameController;

import com.google.inject.Inject;

/**
 * Panel that is visible after game start.
 */
public class StartPanelPresenter {

	public interface Display {

		void updateLanguage();

		JButton getButtonStartNewGame();

	}

	@Inject
	public StartPanelPresenter(final StartPanelPresenter.Display display, final GameController gameController,
			final ChangeLanguageController changeLanguageController) {
		display.getButtonStartNewGame().addActionListener(e -> gameController.startNewGame());
		changeLanguageController.addListener(listener -> display.updateLanguage());
	}

}
