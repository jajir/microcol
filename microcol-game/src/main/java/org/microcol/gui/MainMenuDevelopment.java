package org.microcol.gui;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.google.inject.Inject;

/**
 * Class just logically separate development menu handling from rest of main
 * menu.
 * <p>
 * Development menu is not localized.
 * </p>
 */
public class MainMenuDevelopment {

	private final JMenu developmentMenu;

	@Inject
	public MainMenuDevelopment() {
		developmentMenu = new JMenu("Development");

		final JMenuItem menuItemStartAi = new JMenuItem("Start AI");
		menuItemStartAi.setEnabled(true);
		menuItemStartAi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		menuItemStartAi.addActionListener(event -> {
			System.out.println("Start");
		});
		developmentMenu.add(menuItemStartAi);

		final JMenuItem menuItemStopAi = new JMenuItem("Stop AI");
		menuItemStopAi.setEnabled(true);
		menuItemStopAi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK + KeyEvent.SHIFT_MASK));
		menuItemStopAi.addActionListener(event -> {
			System.out.println("Stop");
		});
		developmentMenu.add(menuItemStopAi);

	}

	public JMenu getDevelopmentMenu() {
		return developmentMenu;
	}

}
