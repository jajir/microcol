package org.microcol.gui;

import org.microcol.gui.event.KeyController;
import org.microcol.gui.event.StatusBarMessageController;
import org.microcol.gui.event.StatusBarMessageEvent;
import org.microcol.gui.event.model.GameModelController;
import org.microcol.gui.event.model.TurnStartedController;
import org.microcol.gui.gamepanel.TileWasSelectedController;
import org.microcol.gui.gamepanel.TileWasSelectedEvent;
import org.microcol.gui.mainmenu.ChangeLanguageController;
import org.microcol.gui.mainmenu.ChangeLanguageEvent;
import org.microcol.gui.util.Localized;
import org.microcol.model.Location;
import org.microcol.model.Player;
import org.microcol.model.event.TurnStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;

public class RightPanelPresenter implements Localized {
	
	private final Logger logger = LoggerFactory.getLogger(RightPanelPresenter.class);

	public interface Display {

		Button getNextTurnButton();

		void showTile(final TileWasSelectedEvent event);

		GridPane getBox();

		void setOnMovePlayer(Player player);
	}

	private final RightPanelPresenter.Display display;

	private TileWasSelectedEvent lastFocusedTileEvent;

	@Inject
	public RightPanelPresenter(final RightPanelPresenter.Display display,
			final GameModelController gameModelController,
			final KeyController keyController,
			final TileWasSelectedController tileWasSelectedController,
			final ChangeLanguageController changeLanguangeController,
			final StatusBarMessageController statusBarMessageController,
			final TurnStartedController turnStartedController) {
		this.display = Preconditions.checkNotNull(display);
		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		display.getNextTurnButton().setDisable(true);

		display.getNextTurnButton().setOnAction(e -> {
			logger.debug("Next turn button was pressed");
			display.getNextTurnButton().setDisable(true);
			gameModelController.nextTurn();
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
		tileWasSelectedController.addRunLaterListener(this::onFocusedTile);
		turnStartedController.addRunLaterListener(this::onTurnStarted);
	}
	
	private void onTurnStarted(final TurnStartedEvent event){
		logger.debug("Turn started for player {}", event.getPlayer());
		display.setOnMovePlayer(event.getPlayer());
		if (event.getPlayer().isHuman()) {
			display.getNextTurnButton().setDisable(false);
		}		
	}

	private void onLanguageWasChanged(final ChangeLanguageEvent event) {
		display.getNextTurnButton().setText(getText().get("nextTurnButton"));
		display.setOnMovePlayer(event.getModel().getCurrentPlayer());
		display.showTile(lastFocusedTileEvent);
	}

	private void onFocusedTile(final TileWasSelectedEvent event) {
		//TODO isItDifferentTile is not necessary to call, it's done in calling methods
		if (isItDifferentTile(event.getLocation())) {
			lastFocusedTileEvent = event;
			display.showTile(event);
		}
	}

	private boolean isItDifferentTile(final Location tile) {
		return lastFocusedTileEvent == null || !lastFocusedTileEvent.getLocation().equals(tile);
	}

}
