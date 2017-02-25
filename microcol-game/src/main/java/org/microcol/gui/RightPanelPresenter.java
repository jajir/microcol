package org.microcol.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.model.GameController;
import org.microcol.gui.model.Tile;

import com.google.inject.Inject;

public class RightPanelPresenter implements Localized {

	public interface Display {
		JButton getNextTurnButton();

		void showTile(final Tile tile);

		JPanel getRightPanel();
	}

	private Tile focusedTile;

	@Inject
	public RightPanelPresenter(final RightPanelPresenter.Display display, final GameController gameController,
			final KeyController keyController, final FocusedTileController focusedTileController,
			final ChangeLanguageController languangeController,
			final StatusBarMessageController statusBarMessageController) {

		display.getNextTurnButton().addActionListener(e -> {
			gameController.nextTurn();
		});

		display.getNextTurnButton().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent keyEvent) {
				keyController.fireKeyWasPressed(keyEvent);
			}
		});

		focusedTileController.addFocusedTileListener(tile -> {
			if (isItDifferentTile(tile)) {
				focusedTile = tile;
				display.showTile(tile);
			}
		});

		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		languangeController.addLanguageListener(event -> {
			display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		});

		display.getNextTurnButton().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				statusBarMessageController
						.fireStatusMessageWasChangedEvent(getText().get("nextTurnButton.desctiption"));
			}
		});

		display.getRightPanel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				statusBarMessageController.fireStatusMessageWasChangedEvent(getText().get("rightPanel.description"));
			}
		});
	}

	private boolean isItDifferentTile(final Tile tile) {
		return focusedTile == null || (focusedTile != null && !focusedTile.equals(tile));
	}

}
