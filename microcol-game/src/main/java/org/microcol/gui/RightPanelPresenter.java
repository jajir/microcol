package org.microcol.gui;

import org.microcol.gui.event.FocusedTileController;
import org.microcol.gui.event.FocusedTileEvent;
import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Localized;
import org.microcol.model.Location;
import org.microcol.model.Player;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class RightPanelPresenter implements Localized {

	public interface Display {

		Button getNextTurnButton();

		void showTile(final FocusedTileEvent event);

		GridPane getBox();

		void setOnMovePlayer(Player player);
	}

	private final RightPanelPresenter.Display display;

	private FocusedTileEvent lastFocusedTileEvent;

	@Inject
	public RightPanelPresenter(final RightPanelPresenter.Display display, final GameModelController gameController,
			final KeyController keyController, final FocusedTileController focusedTileController,
			final ChangeLanguageController changeLanguangeController,
			final StatusBarMessageController statusBarMessageController,
			final TurnStartedController turnStartedController) {
		this.display = Preconditions.checkNotNull(display);
		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		display.getNextTurnButton().setDisable(true);

		display.getNextTurnButton().setOnAction(e -> {
			display.getNextTurnButton().setDisable(true);
			gameController.nextTurn();
		});

		display.getNextTurnButton().setOnKeyPressed(e -> {
			keyController.fireEvent(e);
		});

		display.getNextTurnButton().setOnMouseEntered(event -> {
			statusBarMessageController
					.fireEvent(new StatusBarMessageEvent(getText().get("nextTurnButton.desctiption")));
		});

		display.getBox().setOnMouseEntered(e -> {
			statusBarMessageController.fireEvent(new StatusBarMessageEvent(getText().get("rightPanel.description")));
		});

		changeLanguangeController.addListener(this::onLanguageWasChanged);
		focusedTileController.addListener(this::onFocusedTile);
		turnStartedController.addListener(event -> {
			display.setOnMovePlayer(event.getPlayer());
			if (event.getPlayer().isHuman()) {
				display.getNextTurnButton().setDisable(false);
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
