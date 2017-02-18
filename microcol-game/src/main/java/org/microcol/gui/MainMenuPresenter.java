package org.microcol.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import com.google.inject.Inject;

public class MainMenuPresenter {

	public interface Display {
		JMenuItem getMenuItemNewGame();

		JMenuItem getMenuItemLoadGame();

		JMenuItem getMenuItemSameGame();

		JMenuItem getMenuItemQuitGame();

		JMenuItem getMenuItemAbout();
	}

	@Inject
	public MainMenuPresenter(final MainMenuPresenter.Display display, final GameEventController gameEventController,
			final GamePreferences gamePreferences) {
		display.getMenuItemNewGame().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {

			}
		});
		if (!gamePreferences.isOSX()) {
			display.getMenuItemQuitGame().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					gameEventController.fireGameExit();
				}
			});
			display.getMenuItemAbout().addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent evt) {
					gameEventController.fireAboutGameEvent();
				}
			});
		}
	}

}
