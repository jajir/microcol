package org.microcol.gui;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
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

		final JMenuItem menuItemStartAi = new JMenuItem("Start AI");
		menuItemStartAi.setEnabled(true);
		menuItemStartAi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		menuItemStartAi.addActionListener(event -> {
			gameController.getAiEngine().resume();
			logger.debug("AI was stopped.");
		});
		developmentMenu.add(menuItemStartAi);

		final JMenuItem menuItemStopAi = new JMenuItem("Stop AI");
		menuItemStopAi.setEnabled(true);
		menuItemStopAi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		menuItemStopAi.addActionListener(event -> {
			gameController.getAiEngine().suspend();
			logger.debug("AI was started.");
		});
		developmentMenu.add(menuItemStopAi);

	}

	public JMenu getDevelopmentMenu() {
		return developmentMenu;
	}

}
