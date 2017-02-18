package org.microcol.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.microcol.gui.Text.Language;

import com.google.inject.Inject;

public class MainMenuView extends JMenuBar implements MainMenuPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final JMenuItem menuItemNewGame;

	private final JMenuItem menuItemSameGame;

	private final JMenuItem menuItemLoadGame;

	private final JMenuItem menuItemQuitGame;

	private final JMenuItem menuItemAbout;

	private final JRadioButtonMenuItem rbMenuItemlanguageEn;

	private final JRadioButtonMenuItem rbMenuItemlanguageCz;

	@Inject
	public MainMenuView(final GamePreferences gamePreferences) {
		JMenu menuGame = new JMenu();
		menuGame.setText("Game");

		menuItemNewGame = new JMenuItem();
		menuItemNewGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		menuItemNewGame.setText("New game");
		menuItemNewGame.setEnabled(false);
		menuGame.add(menuItemNewGame);

		menuItemSameGame = new JMenuItem();
		menuItemSameGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		menuItemSameGame.setText("Save game");
		menuItemSameGame.setEnabled(false);
		menuGame.add(menuItemSameGame);

		menuItemLoadGame = new JMenuItem();
		menuItemLoadGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		menuItemLoadGame.setText("Load game");
		menuItemLoadGame.setEnabled(false);
		menuGame.add(menuItemLoadGame);

		menuItemQuitGame = new JMenuItem();
		menuItemQuitGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		menuItemQuitGame.setText("Quit MicroCol");
		if (!gamePreferences.isOSX()) {
			menuGame.add(menuItemQuitGame);
		}
		add(menuGame);

		/**
		 * Preferences
		 * 
		 */
		final JMenu menuPrefereces = new JMenu();
		menuPrefereces.setText("Preferences");
		rbMenuItemlanguageEn = new JRadioButtonMenuItem("English", gamePreferences.getLanguage().equals(Language.en));
		rbMenuItemlanguageCz = new JRadioButtonMenuItem("Czech", gamePreferences.getLanguage().equals(Language.cz));
		final ButtonGroup groupLanguage = new ButtonGroup();
		groupLanguage.add(rbMenuItemlanguageEn);
		groupLanguage.add(rbMenuItemlanguageCz);
		final JMenu menuLanguage = new JMenu("Language");
		menuLanguage.add(rbMenuItemlanguageEn);
		menuLanguage.add(rbMenuItemlanguageCz);
		menuPrefereces.add(menuLanguage);
		add(menuPrefereces);

		/**
		 * Help
		 */
		JMenu menuHelp = new JMenu();
		menuHelp.setText("Help");
		menuItemAbout = new JMenuItem();
		menuItemAbout.setText("About");
		menuHelp.add(menuItemAbout);

		if (!gamePreferences.isOSX()) {
			add(menuHelp);
		}
	}

	@Override
	public JMenuItem getMenuItemNewGame() {
		return menuItemNewGame;
	}

	@Override
	public JMenuItem getMenuItemQuitGame() {
		return menuItemQuitGame;
	}

	@Override
	public JMenuItem getMenuItemSameGame() {
		return menuItemSameGame;
	}

	@Override
	public JMenuItem getMenuItemLoadGame() {
		return menuItemLoadGame;
	}

	@Override
	public JMenuItem getMenuItemAbout() {
		return menuItemAbout;
	}

	@Override
	public JRadioButtonMenuItem getRbMenuItemlanguageEn() {
		return rbMenuItemlanguageEn;
	}

	@Override
	public JRadioButtonMenuItem getRbMenuItemlanguageCz() {
		return rbMenuItemlanguageCz;
	}

}
