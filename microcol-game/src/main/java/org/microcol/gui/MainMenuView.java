package org.microcol.gui;

import org.microcol.gui.event.model.GameController;
import org.microcol.gui.util.PersistentSrevice;
import org.microcol.gui.util.Text;
import org.microcol.gui.util.Text.Language;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import de.codecentric.centerdevice.MenuToolkit;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class MainMenuView implements MainMenuPresenter.Display {

	private final Text text;

	private final MenuBar menuBar;

	private final Menu menuGame;

	private final MenuItem menuItemNewGame;

	private final MenuItem menuItemSameGame;

	private final MenuItem menuItemLoadGame;

	private final Menu menuNewScenario;
	
	private final MenuItem menuItemQuitGame;

	private final MenuItem menuItemAbout;

	private final MenuItem menuItemColonizopedia;

	private final Menu menuView;

	private final MenuItem menuItemCenterView;

	private final MenuItem menuItemEurope;

	private final Menu menuUnit;

	private final MenuItem menuItemMove;

	private final Menu menuPrefereces;

	private final Menu menuLanguage;

	private final RadioMenuItem rbMenuItemlanguageEn;

	private final RadioMenuItem rbMenuItemlanguageCz;

	private final MenuItem menuItemVolume;

	private final MenuItem menuItemAnimationSpeed;

	private final CheckMenuItem menuItemShowGrid;

	private final CheckMenuItem menuItemShowFightAdvisor;

	private final Menu menuHelp;

	@Inject
	public MainMenuView(final GamePreferences gamePreferences, final Text text,
			final MainMenuDevelopment mainMenuDevelopment, final PersistentSrevice persistentSrevice,
			final GameController gameController) {
		this.text = Preconditions.checkNotNull(text);

		/**
		 * Game menu
		 */
		menuItemNewGame = new MenuItem();
		menuItemNewGame.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
		menuItemNewGame.disableProperty().setValue(true);
		
		menuNewScenario = new Menu();
		persistentSrevice.getScenarios().forEach(scenario->{
			final MenuItem menuItem = new MenuItem(scenario.getName());
			menuItem.setOnAction(event->{
				gameController.startScenario(scenario.getFileName());
			});
			menuNewScenario.getItems().add(menuItem);
		});

		menuItemLoadGame = new MenuItem();
		menuItemLoadGame.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
		menuItemLoadGame.disableProperty().setValue(true);

		menuItemSameGame = new MenuItem();
		menuItemSameGame.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		menuItemSameGame.disableProperty().setValue(true);

		menuItemQuitGame = new MenuItem();
		menuItemQuitGame.setAccelerator(new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN));

		/**
		 * Menu view
		 * 
		 */
		menuItemCenterView = new MenuItem();
		menuItemCenterView.setAccelerator(new KeyCodeCombination(KeyCode.C));
		menuItemCenterView.disableProperty().setValue(false);

		menuItemEurope = new MenuItem();
		menuItemEurope.setAccelerator(new KeyCodeCombination(KeyCode.E));
		menuItemEurope.disableProperty().setValue(false);

		/**
		 * Menu unit
		 * 
		 */
		menuItemMove = new MenuItem();
		menuItemMove.setAccelerator(new KeyCodeCombination(KeyCode.M));
		menuItemMove.disableProperty().setValue(true);

		/**
		 * Preferences
		 * 
		 */
		final ToggleGroup groupLanguage = new ToggleGroup();
		rbMenuItemlanguageEn = new RadioMenuItem();
		rbMenuItemlanguageEn.setSelected(gamePreferences.getLanguage().equals(Language.en));
		rbMenuItemlanguageEn.setToggleGroup(groupLanguage);
		rbMenuItemlanguageCz = new RadioMenuItem();
		rbMenuItemlanguageCz.setSelected(gamePreferences.getLanguage().equals(Language.cz));
		rbMenuItemlanguageCz.setToggleGroup(groupLanguage);

		menuLanguage = new Menu();
		menuLanguage.getItems().addAll(rbMenuItemlanguageEn, rbMenuItemlanguageCz);

		menuItemVolume = new MenuItem();
		menuItemVolume.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));

		menuItemAnimationSpeed = new MenuItem();
		menuItemAnimationSpeed.setAccelerator(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN));

		menuItemShowGrid = new CheckMenuItem();
		menuItemShowGrid.setAccelerator(new KeyCodeCombination(KeyCode.G));
		menuItemShowGrid.setSelected(gamePreferences.isGridShown());

		menuItemShowFightAdvisor = new CheckMenuItem();
		menuItemShowFightAdvisor.setSelected(gamePreferences.getShowFightAdvisorProperty().get());
		gamePreferences.getShowFightAdvisorProperty().bindBidirectional(menuItemShowFightAdvisor.selectedProperty());

		/**
		 * menu
		 */
		menuGame = new Menu();
		menuGame.getItems().addAll(menuItemNewGame, menuNewScenario, menuItemLoadGame, menuItemSameGame, menuItemQuitGame);
		menuView = new Menu();
		menuView.getItems().addAll(menuItemCenterView, menuItemEurope);
		menuUnit = new Menu();
		menuUnit.getItems().addAll(menuItemMove);
		menuPrefereces = new Menu();
		menuPrefereces.getItems().addAll(menuLanguage, menuItemVolume, menuItemAnimationSpeed, menuItemShowGrid,
				menuItemShowFightAdvisor);

		menuBar = new MenuBar();
		menuBar.getMenus().addAll(menuGame, menuView, menuUnit, menuPrefereces);
		if (gamePreferences.isDevelopment()) {
			menuBar.getMenus().add(mainMenuDevelopment.getDevelopmentMenu());
		}
		/**
		 * Following command will use apple system menu. Side effect is that
		 * menu accelerators like 'm' stop work.
		 * 
		 * XXX JJ it's probably bug.
		 */
		// menuBar.useSystemMenuBarProperty().set(true);

		/**
		 * Help
		 */
		menuHelp = new Menu();
		if (gamePreferences.isOSX()) {
			/**
			 * Display menus in apple application menu.
			 */
			// Get the toolkit
			MenuToolkit tk = MenuToolkit.toolkit();
			// Create the default Application menu
			Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("MicroCol");
			// Update the existing Application menu
			tk.setApplicationMenu(defaultApplicationMenu);

			menuItemAbout = defaultApplicationMenu.getItems().get(0);
		} else {
			menuItemAbout = new MenuItem();
			menuHelp.getItems().addAll(menuItemAbout);
		}
		menuItemColonizopedia = new MenuItem();
		menuItemColonizopedia.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
		menuItemColonizopedia.disableProperty().setValue(false);
		menuHelp.getItems().addAll(menuItemColonizopedia);

		menuBar.getMenus().add(menuHelp);

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
		menuNewScenario.setText(text.get("mainMenu.game.newScenario"));
		menuItemQuitGame.setText(text.get("mainMenu.game.quitGame"));

		/**
		 * View
		 */
		menuView.setText(text.get("mainMenu.view"));
		menuItemCenterView.setText(text.get("mainMenu.view.center"));
		menuItemEurope.setText(text.get("mainMenu.view.europe"));

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
		menuItemShowFightAdvisor.setText(text.get("mainMenu.preferences.showFightAdvisor"));

		/**
		 * Help & About
		 */
		menuHelp.setText(text.get("mainMenu.help"));
		menuItemAbout.setText(text.get("mainMenu.help.about"));
		menuItemColonizopedia.setText(text.get("mainMenu.help.colonizopedia"));
	}

	@Override
	public MenuItem getMenuItemNewGame() {
		return menuItemNewGame;
	}

	@Override
	public MenuItem getMenuItemQuitGame() {
		return menuItemQuitGame;
	}

	@Override
	public MenuItem getMenuItemSameGame() {
		return menuItemSameGame;
	}

	@Override
	public MenuItem getMenuItemLoadGame() {
		return menuItemLoadGame;
	}

	@Override
	public MenuItem getMenuItemAbout() {
		return menuItemAbout;
	}

	@Override
	public RadioMenuItem getRbMenuItemlanguageEn() {
		return rbMenuItemlanguageEn;
	}

	@Override
	public RadioMenuItem getRbMenuItemlanguageCz() {
		return rbMenuItemlanguageCz;
	}

	@Override
	public MenuItem getMenuItemVolume() {
		return menuItemVolume;
	}

	@Override
	public CheckMenuItem getMenuItemShowGrid() {
		return menuItemShowGrid;
	}

	@Override
	public MenuItem getMenuItemMove() {
		return menuItemMove;
	}

	@Override
	public MenuItem getMenuItemCenterView() {
		return menuItemCenterView;
	}

	@Override
	public MenuItem getMenuItemAnimationSpeed() {
		return menuItemAnimationSpeed;
	}

	@Override
	public MenuItem getMenuItemEurope() {
		return menuItemEurope;
	}

	public MenuBar getMenuBar() {
		return menuBar;
	}

	@Override
	public MenuItem getMenuItemColonizopedia() {
		return menuItemColonizopedia;
	}
}
