package org.microcol.gui.mainmenu;

import org.microcol.gui.event.model.GameModelController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Class just logically separate development menu handling from rest of main
 * menu.
 * <p>
 * Development menu is not localized.
 * </p>
 */
public class MainMenuDevelopment {

	private final Logger logger = LoggerFactory.getLogger(MainMenuDevelopment.class);

	private final Menu developmentMenu;

	@Inject
	public MainMenuDevelopment(final GameModelController gameModelController) {
		developmentMenu = new Menu("Development");

		final CheckMenuItem checkBoxStopAi = new CheckMenuItem("Suspend AI");
		checkBoxStopAi.setAccelerator(
				new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		checkBoxStopAi.selectedProperty().addListener((on, oldValue, newValue) -> {
			boolean isSelected = newValue;
			if (isSelected) {
				new Thread(() -> gameModelController.suspendAi()).start();
				logger.debug("AI was stopped.");
			} else {
				logger.debug("AI was started.");
				new Thread(() -> gameModelController.resumeAi()).start();
			}
		});
		developmentMenu.getItems().add(checkBoxStopAi);

	}

	public Menu getDevelopmentMenu() {
		return developmentMenu;
	}

}
