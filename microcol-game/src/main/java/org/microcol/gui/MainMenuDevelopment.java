package org.microcol.gui;

import javax.swing.AbstractButton;

import org.microcol.gui.event.model.GameController;
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
	public MainMenuDevelopment(final GameController gameController) {
		developmentMenu = new Menu("Development");

		final CheckMenuItem checkBoxStopAi = new CheckMenuItem("Suspend AI");
		checkBoxStopAi.setAccelerator(
				new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		checkBoxStopAi.setOnAction(event -> {
			final AbstractButton aButton = (AbstractButton) event.getSource();
			if (aButton.getModel().isSelected()) {
				new Thread(() -> gameController.getAiEngine().resume()).start();
				logger.debug("AI was stopped.");
			} else {
				new Thread(() -> gameController.getAiEngine().suspend()).start();
				logger.debug("AI was started.");
			}
		});
		developmentMenu.getItems().add(checkBoxStopAi);

	}

	public Menu getDevelopmentMenu() {
		return developmentMenu;
	}

}
