package org.microcol.gui;

import javax.swing.JButton;

import org.microcol.gui.event.ChangeLanguageController;

import com.google.inject.Inject;

public class StartPanelPresenter {

	public interface Display {

		void updateLanguage();

		JButton getButtonStartNewGame();

	}

	@Inject
	public StartPanelPresenter(final StartPanelPresenter.Display display,final ApplicationController applicationController,
			final ChangeLanguageController changeLanguageController) {
		display.getButtonStartNewGame().addActionListener(e->applicationController.startNewGame());
		changeLanguageController.addListener(listener -> display.updateLanguage());
	}

}
