package org.microcol.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;

import org.microcol.gui.model.GameController;
import org.microcol.gui.model.Tile;

import com.google.inject.Inject;

public class RightPanelPresenter implements Localized {

	public interface Display {
		JButton getNextTurnButton();

		void showTile(final Tile tile);
	}

	@Inject
	public RightPanelPresenter(final RightPanelPresenter.Display display, final GameController gameController,
			final KeyController keyController, final FocusedTileController focusedTileController,
			final LanguangeController languangeController) {

		display.getNextTurnButton().addActionListener(e -> {
			gameController.getWorld().nextTurn();
		});

		display.getNextTurnButton().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent keyEvent) {
				keyController.fireKeyWasPressed(keyEvent);
			}
		});

		focusedTileController.addFocusedTileListener(tile -> {
			display.showTile(tile);
		});

		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		languangeController.addLanguageListener(event -> {
			display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		});
	}

}
