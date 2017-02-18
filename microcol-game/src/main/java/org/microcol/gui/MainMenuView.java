package org.microcol.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MainMenuView extends JMenuBar implements MainMenuPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final JMenuItem menuItemNewGame;

	private final JMenuItem menuItemSameGame;

	private final JMenuItem menuItemLoadGame;

	private final JMenuItem menuItemQuitGame;

	public MainMenuView() {
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
		menuGame.add(menuItemQuitGame);

		add(menuGame);
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

}
