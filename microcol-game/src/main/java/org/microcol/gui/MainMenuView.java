package org.microcol.gui;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class MainMenuView extends JMenuBar implements MainMenuPresenter.Display {

	/**
	 * Default serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private final Text text;

	private final JMenu menuGame;

	private final JMenuItem menuItemNewGame;

	private final JMenuItem menuItemSameGame;

	private final JMenuItem menuItemLoadGame;

	private final JMenuItem menuItemQuitGame;

	private final JMenuItem menuItemAbout;

	private final JMenu menuView;

	private final JMenuItem menuItemCenterView;

	private final JMenu menuUnit;

	private final JMenuItem menuItemMove;

	private final JMenu menuPrefereces;

	private final JMenu menuLanguage;

	private final JRadioButtonMenuItem rbMenuItemlanguageEn;

	private final JRadioButtonMenuItem rbMenuItemlanguageCz;

	private final JMenuItem menuItemVolume;

	private final JMenuItem menuItemAnimationSpeed;

	private final JCheckBoxMenuItem menuItemShowGrid;

	private final JMenu menuHelp;

	@Inject
	public MainMenuView(final GamePreferences gamePreferences, final Text text, final MainMenuDevelopment mainMenuDevelopment) {
		this.text = Preconditions.checkNotNull(text);
		menuGame = new JMenu();

		menuItemNewGame = new JMenuItem();
		menuItemNewGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
		menuItemNewGame.setEnabled(true);
		menuGame.add(menuItemNewGame);
		
		menuItemLoadGame = new JMenuItem();
		menuItemLoadGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
		menuItemLoadGame.setEnabled(true);
		menuGame.add(menuItemLoadGame);

		menuItemSameGame = new JMenuItem();
		menuItemSameGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		menuItemSameGame.setEnabled(true);
		menuGame.add(menuItemSameGame);

		menuItemQuitGame = new JMenuItem();
		menuItemQuitGame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
		if (!gamePreferences.isOSX()) {
			menuGame.add(menuItemQuitGame);
		}
		add(menuGame);
		
		/**
		 * Menu view
		 * 
		 */
		menuView = new JMenu();
		menuItemCenterView = new JMenuItem();
		menuItemCenterView.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0));
		menuItemCenterView.setEnabled(false);
		menuView.add(menuItemCenterView);
		add(menuView);

		/**
		 * Menu unit
		 * 
		 */
		menuUnit = new JMenu();
		menuItemMove = new JMenuItem();
		menuItemMove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, 0));
		menuItemMove.setEnabled(false);
		menuUnit.add(menuItemMove);
		add(menuUnit);

		/**
		 * Preferences
		 * 
		 */
		menuPrefereces = new JMenu();
		rbMenuItemlanguageEn = new JRadioButtonMenuItem();
		rbMenuItemlanguageEn.setSelected(gamePreferences.getLanguage().equals(Language.en));
		rbMenuItemlanguageCz = new JRadioButtonMenuItem();
		rbMenuItemlanguageCz.setSelected(gamePreferences.getLanguage().equals(Language.cz));
		final ButtonGroup groupLanguage = new ButtonGroup();
		groupLanguage.add(rbMenuItemlanguageEn);
		groupLanguage.add(rbMenuItemlanguageCz);
		menuLanguage = new JMenu();
		menuLanguage.add(rbMenuItemlanguageEn);
		menuLanguage.add(rbMenuItemlanguageCz);
		menuPrefereces.add(menuLanguage);

		menuItemVolume = new JMenuItem();
		menuItemVolume.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
		menuPrefereces.add(menuItemVolume);

		menuItemAnimationSpeed= new JMenuItem();
		menuItemAnimationSpeed.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
		menuPrefereces.add(menuItemAnimationSpeed);

		menuItemShowGrid = new JCheckBoxMenuItem();
		menuItemShowGrid.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, 0));
		menuItemShowGrid.setSelected(gamePreferences.isGridShown());
		menuPrefereces.add(menuItemShowGrid);

		add(menuPrefereces);

		/**
		 * Help
		 */
		menuHelp = new JMenu();
		menuItemAbout = new JMenuItem();
		menuHelp.add(menuItemAbout);

		if(gamePreferences.isDevelopment()){
			add(mainMenuDevelopment.getDevelopmentMenu());
		}
		
		if (!gamePreferences.isOSX()) {
			add(menuHelp);
		}
		updateLanguage();
	}

	@Override
	public void updateLanguage() {
		/**
		 * Game
		 */
		menuGame.setText(text.get("mainMenu.game"));
		menuItemNewGame.setText(text.get("mainMenu.game.newGame"));
		menuItemSameGame.setText(text.get("mainMenu.game.saveGame"));
		menuItemLoadGame.setText(text.get("mainMenu.game.loadGame"));
		menuItemQuitGame.setText(text.get("mainMenu.game.quitGame"));

		/**
		 * View
		 */
		menuView.setText(text.get("mainMenu.view"));
		menuItemCenterView.setText(text.get("mainMenu.view.center"));

		/**
		 * Unit
		 */
		menuUnit.setText(text.get("mainMenu.unit"));
		menuItemMove.setText(text.get("mainMenu.unit.move"));

		/**
		 * Preferences
		 */
		menuPrefereces.setText(text.get("mainMenu.preferences"));
		menuLanguage.setText(text.get("mainMenu.preferences.language"));
		rbMenuItemlanguageEn.setText(text.get("mainMenu.preferences.language.en"));
		rbMenuItemlanguageCz.setText(text.get("mainMenu.preferences.language.cz"));
		menuItemVolume.setText(text.get("mainMenu.preferences.volume"));
		menuItemAnimationSpeed.setText(text.get("mainMenu.preferences.animationSpeed"));
		menuItemShowGrid.setText(text.get("mainMenu.preferences.showGrid"));

		/**
		 * Help & About
		 */
		menuHelp.setText(text.get("mainMenu.help"));
		menuItemAbout.setText(text.get("mainMenu.help.about"));
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

	@Override
	public JMenuItem getMenuItemVolume() {
		return menuItemVolume;
	}

	@Override
	public JCheckBoxMenuItem getMenuItemShowGrid() {
		return menuItemShowGrid;
	}

	@Override
	public JMenuItem getMenuItemMove() {
		return menuItemMove;
	}

	@Override
	public JMenuItem getMenuItemCenterView() {
		return menuItemCenterView;
	}

	@Override
	public JMenuItem getMenuItemAnimationSpeed() {
		return menuItemAnimationSpeed;
	}
}
