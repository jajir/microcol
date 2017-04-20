package org.microcol.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.microcol.gui.event.ChangeLanguageController;
import org.microcol.gui.event.ChangeLanguageEvent;
import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.model.Location;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class RightPanelPresenter implements Localized {

	public interface Display {

		JButton getNextTurnButton();

		void showTile(final FocusedTileEvent event);

		JPanel getRightPanel();

		void setOnMovePlayer(Player player);
	}

	private final RightPanelPresenter.Display display;

	private FocusedTileEvent lastFocusedTileEvent;

	@Inject
	public RightPanelPresenter(final RightPanelPresenter.Display display, final GameController gameController,
			final KeyController keyController, final FocusedTileController focusedTileController,
			final ChangeLanguageController changeLanguangeController,
			final StatusBarMessageController statusBarMessageController,
			final TurnStartedController turnStartedController) {
		this.display = Preconditions.checkNotNull(display);
		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		display.getNextTurnButton().setEnabled(false);

		display.getNextTurnButton().addActionListener(e -> {
			display.getNextTurnButton().setEnabled(false);
			gameController.nextTurn();
		});

		display.getNextTurnButton().addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent keyEvent) {
				keyController.fireEvent(keyEvent);
			}
		});

		display.getNextTurnButton().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				statusBarMessageController
						.fireEvent(new StatusBarMessageEvent(getText().get("nextTurnButton.desctiption")));
			}
		});

		display.getRightPanel().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				statusBarMessageController
						.fireEvent(new StatusBarMessageEvent(getText().get("rightPanel.description")));
			}
		});

		changeLanguangeController.addListener(this::onLanguageWasChanged);
		focusedTileController.addListener(this::onFocusedTile);
		turnStartedController.addListener(event -> {
			display.setOnMovePlayer(event.getPlayer());
			if (event.getPlayer().isHuman()) {
				display.getNextTurnButton().setEnabled(true);
			}
		});
	}

	private void onLanguageWasChanged(final ChangeLanguageEvent event) {
		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		display.setOnMovePlayer(event.getModel().getCurrentPlayer());
		display.showTile(lastFocusedTileEvent);
	}

	private void onFocusedTile(final FocusedTileEvent event) {
		if (isItDifferentTile(event.getLocation())) {
			lastFocusedTileEvent = event;
			display.showTile(event);
		}
	}

	private boolean isItDifferentTile(final Location tile) {
		return lastFocusedTileEvent == null || !lastFocusedTileEvent.equals(tile);
	}

}
