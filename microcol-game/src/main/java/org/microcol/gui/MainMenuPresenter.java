package org.microcol.gui;

import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import com.google.inject.Inject;

public class MainMenuPresenter {

	public interface Display {
		JMenuItem getMenuItemNewGame();

		JMenuItem getMenuItemLoadGame();

		JMenuItem getMenuItemSameGame();

		JMenuItem getMenuItemQuitGame();

		JMenuItem getMenuItemAbout();

		JRadioButtonMenuItem getRbMenuItemlanguageEn();

		JRadioButtonMenuItem getRbMenuItemlanguageCz();
	}

	@Inject
	public MainMenuPresenter(final MainMenuPresenter.Display display, final GameEventController gameEventController,
			final GamePreferences gamePreferences, final ChangeLanguageController languangeController) {
		display.getMenuItemNewGame().addActionListener(actionEvent -> {

		});
		if (!gamePreferences.isOSX()) {
			display.getMenuItemQuitGame().addActionListener(actionEvent -> {
				gameEventController.fireGameExit();
			});
			display.getMenuItemAbout().addActionListener(actionEvent -> {
				gameEventController.fireAboutGameEvent();
			});
		}
		display.getRbMenuItemlanguageCz().addActionListener(actionEvent -> {
			languangeController.fireLanguageWasChangedEvent(Text.Language.cz);
		});
		display.getRbMenuItemlanguageEn().addActionListener(actionEvent -> {
			languangeController.fireLanguageWasChangedEvent(Text.Language.en);
		});
	}

}
