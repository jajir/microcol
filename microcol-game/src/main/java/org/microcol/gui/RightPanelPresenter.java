package org.microcol.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.microcol.model.GameController;

import com.google.inject.Inject;

public class RightPanelPresenter implements Localized {

	public interface Display {
		JButton getNextTurnButton();

		JLabel getTextLabel();
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

		focusedTileController.addNextTurnListener(tile -> {
			if (tile.getUnits().isEmpty()) {
				display.getTextLabel().setText("empty unit");
			} else {
				final StringBuilder buff = new StringBuilder();
				buff.append("<html>");
				tile.getUnits().forEach(unit -> {
					buff.append(unit.toString());
					buff.append("<br />");
					buff.append("<br />");
				});
				buff.append("</html>");
				display.getTextLabel().setText(buff.toString());
			}
		});

		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		languangeController.addLanguageListener(event -> {
			display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		});
	}

}
