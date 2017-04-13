package org.microcol.gui;

import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import org.microcol.gui.event.GameController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

/**
 * Class just logically separate development menu handling from rest of main
 * menu.
 * <p>
 * Development menu is not localized.
 * </p>
 */
public class MainMenuDevelopment {

	private final Logger logger = LoggerFactory.getLogger(MainMenuDevelopment.class);

	private final JMenu developmentMenu;

	@Inject
	public MainMenuDevelopment(final GameController gameController) {
		developmentMenu = new JMenu("Development");

		final JCheckBoxMenuItem checkBoxStopAi = new JCheckBoxMenuItem("Suspend AI");
		checkBoxStopAi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		checkBoxStopAi.addActionListener(event -> {
			final AbstractButton aButton = (AbstractButton) event.getSource();
			if (aButton.getModel().isSelected()) {
				new Thread(() -> gameController.getAiEngine().resume()).start();
				logger.debug("AI was stopped.");
			} else {
				new Thread(() -> gameController.getAiEngine().suspend()).start();
				logger.debug("AI was started.");
			}
		});
		developmentMenu.add(checkBoxStopAi);

	}

	public JMenu getDevelopmentMenu() {
		return developmentMenu;
	}

}
