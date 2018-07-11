package org.microcol.gui.mainmenu;

import org.microcol.gui.util.GamePreferences;
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

public final class MainMenuView {

    private final Text text;

    private final MenuBar menuBar;

    private final Menu menuGame;

    private final MenuItem menuItemDeclareIndependence;

    private final MenuItem menuItemExitGame;

    private final MenuItem menuItemNewGame;

    private final MenuItem menuItemSaveGame;

    private final MenuItem menuItemLoadGame;

    private final MenuItem menuItemQuitGame;

    private final MenuItem menuItemAbout;

    private final MenuItem menuItemColonizopedia;

    private final Menu menuView;

    private final MenuItem menuItemCenterView;

    private final MenuItem menuItemEurope;

    private final MenuItem menuItemTurnReport;

    private final MenuItem menuItemGoals;

    private final MenuItem menuItemStatistics;

    private final Menu menuUnit;

    private final MenuItem menuItemMove;

    private final MenuItem menuItemPlowField;

    private final MenuItem menuItemBuildColony;

    private final MenuItem menuItemNextUnit;

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
	    final MainMenuDevelopment mainMenuDevelopment) {
	this.text = Preconditions.checkNotNull(text);

	/**
	 * Game menu
	 */
	menuItemDeclareIndependence = new MenuItem();
	menuItemDeclareIndependence.disableProperty().setValue(false);

	menuItemExitGame = new MenuItem();
	menuItemExitGame.disableProperty().setValue(false);

	menuItemNewGame = new MenuItem();
	menuItemNewGame.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
	menuItemNewGame.disableProperty().setValue(false);

	menuItemLoadGame = new MenuItem();
	menuItemLoadGame.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
	menuItemLoadGame.disableProperty().setValue(false);

	menuItemSaveGame = new MenuItem();
	menuItemSaveGame.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
	menuItemSaveGame.disableProperty().setValue(false);

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

	menuItemTurnReport = new MenuItem();
	menuItemTurnReport.setAccelerator(new KeyCodeCombination(KeyCode.T));
	menuItemTurnReport.disableProperty().setValue(false);

	menuItemStatistics = new MenuItem();
	menuItemStatistics.setAccelerator(new KeyCodeCombination(KeyCode.A));
	menuItemStatistics.disableProperty().setValue(false);

	menuItemGoals = new MenuItem();
	menuItemGoals.setAccelerator(new KeyCodeCombination(KeyCode.G));
	menuItemGoals.disableProperty().setValue(false);

	/**
	 * Menu unit
	 * 
	 */
	menuItemMove = new MenuItem();
	menuItemMove.setAccelerator(new KeyCodeCombination(KeyCode.M));
	menuItemMove.disableProperty().setValue(true);

	menuItemPlowField = new MenuItem();
	menuItemPlowField.setAccelerator(new KeyCodeCombination(KeyCode.P));
	menuItemPlowField.disableProperty().setValue(true);

	menuItemBuildColony = new MenuItem();
	menuItemBuildColony.setAccelerator(new KeyCodeCombination(KeyCode.B));
	menuItemBuildColony.disableProperty().setValue(true);

	menuItemNextUnit = new MenuItem();
	menuItemNextUnit.setAccelerator(new KeyCodeCombination(KeyCode.TAB));
	menuItemNextUnit.disableProperty().setValue(false);

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
	menuItemShowGrid.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
	menuItemShowGrid.setSelected(gamePreferences.isGridShown());

	menuItemShowFightAdvisor = new CheckMenuItem();
	menuItemShowFightAdvisor.setSelected(gamePreferences.getShowFightAdvisorProperty().get());
	gamePreferences.getShowFightAdvisorProperty().bindBidirectional(menuItemShowFightAdvisor.selectedProperty());

	/**
	 * menu
	 */
	menuGame = new Menu();
	menuGame.getItems().addAll(menuItemDeclareIndependence, menuItemExitGame, menuItemNewGame, menuItemLoadGame,
		menuItemSaveGame, menuItemQuitGame);
	menuView = new Menu();
	menuView.getItems().addAll(menuItemCenterView, menuItemEurope, menuItemTurnReport, menuItemStatistics,
		menuItemGoals);
	menuUnit = new Menu();
	menuUnit.getItems().addAll(menuItemMove, menuItemBuildColony, menuItemPlowField, menuItemNextUnit);
	menuPrefereces = new Menu();
	menuPrefereces.getItems().addAll(menuLanguage, menuItemVolume, menuItemAnimationSpeed, menuItemShowGrid,
		menuItemShowFightAdvisor);

	menuBar = new MenuBar();
	menuBar.getMenus().addAll(menuGame, menuView, menuUnit, menuPrefereces);
	if (gamePreferences.isDevelopment()) {
	    menuBar.getMenus().add(mainMenuDevelopment.getDevelopmentMenu());
	}

	menuItemColonizopedia = new MenuItem();
	menuItemColonizopedia.setAccelerator(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN));
	menuItemColonizopedia.disableProperty().setValue(false);

	/**
	 * Help
	 */
	menuHelp = new Menu();
	if (gamePreferences.isDevelopment()) {
	    menuHelp.getItems().addAll(menuItemColonizopedia);
	}

	if (gamePreferences.isOSX()) {
	    /**
	     * Following command will use apple system menu. Side effect is that menu
	     * accelerators like 'm' stop work.
	     * 
	     * TODO JJ it's probably bug.
	     */
	    // menuBar.useSystemMenuBarProperty().set(true);
	    final MenuToolkit tk = MenuToolkit.toolkit();
	    // Create the default Application menu
	    if (tk == null) {
		menuItemAbout = new MenuItem();
		menuHelp.getItems().addAll(menuItemAbout);
		menuBar.getMenus().add(menuHelp);
	    } else {
		Menu defaultApplicationMenu = tk.createDefaultApplicationMenu("MicroCol");
		// Update the existing Application menu
		tk.setApplicationMenu(defaultApplicationMenu);
		menuItemAbout = defaultApplicationMenu.getItems().get(0);
		if (gamePreferences.isDevelopment()) {
		    menuBar.getMenus().add(menuHelp);
		}
	    }
	} else {
	    menuItemAbout = new MenuItem();
	    menuHelp.getItems().addAll(menuItemAbout);
	    menuBar.getMenus().add(menuHelp);
	}
	updateLanguage();
    }

    public void updateLanguage() {
	/**
	 * Game
	 */
	menuGame.setText(text.get("mainMenu.game"));
	menuItemDeclareIndependence.setText(text.get("mainMenu.game.declareIndependence"));
	menuItemNewGame.setText(text.get("mainMenu.game.newGame"));
	menuItemExitGame.setText(text.get("mainMenu.game.exitGame"));
	menuItemSaveGame.setText(text.get("mainMenu.game.saveGame"));
	menuItemLoadGame.setText(text.get("mainMenu.game.loadGame"));
	menuItemQuitGame.setText(text.get("mainMenu.game.quitGame"));

	/**
	 * View
	 */
	menuView.setText(text.get("mainMenu.view"));
	menuItemCenterView.setText(text.get("mainMenu.view.center"));
	menuItemEurope.setText(text.get("mainMenu.view.europe"));
	menuItemTurnReport.setText(text.get("mainMenu.view.turnReport"));
	menuItemStatistics.setText(text.get("mainMenu.view.statistics"));
	menuItemGoals.setText(text.get("mainMenu.view.menuItemGoals"));

	/**
	 * Unit
	 */
	menuUnit.setText(text.get("mainMenu.unit"));
	menuItemMove.setText(text.get("mainMenu.unit.move"));
	menuItemBuildColony.setText(text.get("mainMenu.unit.buildColony"));
	menuItemNextUnit.setText(text.get("mainMenu.unit.nextUnit"));
	menuItemPlowField.setText(text.get("mainMenu.unit.plowFied"));

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

    public MenuItem getMenuItemNewGame() {
	return menuItemNewGame;
    }

    public MenuItem getMenuItemQuitGame() {
	return menuItemQuitGame;
    }

    public MenuItem getMenuItemSaveGame() {
	return menuItemSaveGame;
    }

    public MenuItem getMenuItemLoadGame() {
	return menuItemLoadGame;
    }

    public MenuItem getMenuItemAbout() {
	return menuItemAbout;
    }

    public RadioMenuItem getRbMenuItemlanguageEn() {
	return rbMenuItemlanguageEn;
    }

    public RadioMenuItem getRbMenuItemlanguageCz() {
	return rbMenuItemlanguageCz;
    }

    public MenuItem getMenuItemVolume() {
	return menuItemVolume;
    }

    public CheckMenuItem getMenuItemShowGrid() {
	return menuItemShowGrid;
    }

    public MenuItem getMenuItemMove() {
	return menuItemMove;
    }

    public MenuItem getMenuItemCenterView() {
	return menuItemCenterView;
    }

    public MenuItem getMenuItemAnimationSpeed() {
	return menuItemAnimationSpeed;
    }

    public MenuItem getMenuItemEurope() {
	return menuItemEurope;
    }

    public MenuBar getMenuBar() {
	return menuBar;
    }

    public MenuItem getMenuItemColonizopedia() {
	return menuItemColonizopedia;
    }

    /**
     * @return the menuItemDeclareIndependence
     */
    public MenuItem getMenuItemDeclareIndependence() {
	return menuItemDeclareIndependence;
    }

    /**
     * @return the menuItemBuildColony
     */
    public MenuItem getMenuItemBuildColony() {
	return menuItemBuildColony;
    }

    /**
     * @return the menuItemNextUnit
     */
    public MenuItem getMenuItemNextUnit() {
	return menuItemNextUnit;
    }

    /**
     * @return the menuItemExitGame
     */
    public MenuItem getMenuItemExitGame() {
	return menuItemExitGame;
    }

    /**
     * @return the menuItemTurnReport
     */
    public MenuItem getMenuItemTurnReport() {
	return menuItemTurnReport;
    }

    /**
     * @return the menuItemStatistics
     */
    public MenuItem getMenuItemStatistics() {
	return menuItemStatistics;
    }

    /**
     * @return the menuItemGoals
     */
    public MenuItem getMenuItemGoals() {
	return menuItemGoals;
    }

    public MenuItem getMenuItemPlowField() {
	return menuItemPlowField;
    }

}
