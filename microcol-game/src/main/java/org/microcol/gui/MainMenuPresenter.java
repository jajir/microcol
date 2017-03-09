package org.microcol.gui;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.GameEventController;
import org.microcol.gui.event.ShowGridController;
import org.microcol.gui.event.VolumeChangeController;

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

		void updateLanguage();

		JMenuItem getMenuItemVolume();

		JCheckBoxMenuItem getMenuItemShowGrid();

		JMenuItem getMenuItemMove();
	}

	@Inject
	public MainMenuPresenter(final MainMenuPresenter.Display display, final GameEventController gameEventController,
			final GamePreferences gamePreferences, final ChangeLanguageController languangeController, final Text text,
			final ViewUtil viewUtil, final VolumeChangeController volumeChangeController,
			final ShowGridController showGridController, final FocusedTileController focusedTileController) {
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
		display.getMenuItemVolume().addActionListener(actionEvent -> {
			PreferencesVolume preferencesVolume = new PreferencesVolume(viewUtil, text, volumeChangeController,
					gamePreferences.getVolume());
			preferencesVolume.setVisible(true);
		});
		display.getMenuItemShowGrid().addActionListener(
				ectionEvent -> showGridController.fireShowGridEvent(display.getMenuItemShowGrid().isSelected()));

		languangeController.addLanguageListener(event -> {
			display.updateLanguage();
		});
		focusedTileController.addFocusedTileListener(event -> {
			if (event.isTileContainsMovebleUnit()) {
				display.getMenuItemMove().setEnabled(true);
			} else {
				display.getMenuItemMove().setEnabled(false);
			}
		});
	}

}
