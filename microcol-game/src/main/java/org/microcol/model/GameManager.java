package org.microcol.model;

import java.util.List;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.Preconditions;

class GameManager {
	private Model model;

	private boolean started;
	private Player currentPlayer;

	GameManager() {
		// Do nothing.
	}

	void setModel(final Model model) {
		this.model = Preconditions.checkNotNull(model);
	}

	boolean isStarted() {
		return started;
	}

	boolean isFinished() {
		return model.getCalendar().isFinished();
	}

	boolean isActive() {
		return started && !isFinished();
	}

	void checkGameActive() {
		if (!isActive()) {
			throw new IllegalStateException("Game must be active.");
		}
	}

	void checkCurrentPlayer(final Player player) {
		Preconditions.checkNotNull(player);

		if (!player.equals(currentPlayer)) {
			throw new IllegalStateException(String.format("This player (%s) is not current player (%s).", player, currentPlayer));
		}
	}

	Player getCurrentPlayer() {
		Preconditions.checkState(started, "Game must be started.");

		return currentPlayer;
	}

	void startGame() {
		Preconditions.checkState(!started, "Game was already started.");

		started = true;
		currentPlayer = model.getPlayers().get(0);
		model.fireGameStarted();
		model.fireRoundStarted();
		currentPlayer.startTurn();
		model.fireTurnStarted(currentPlayer);
	}

	void endTurn() {
		checkGameActive();

		final int index = model.getPlayers().indexOf(currentPlayer);
		if (index < model.getPlayers().size() - 1) {
			currentPlayer = model.getPlayers().get(index + 1);
			currentPlayer.startTurn();
			model.fireTurnStarted(currentPlayer);
		} else {
			model.getCalendar().endRound();
			if (!model.getCalendar().isFinished()) {
				currentPlayer = model.getPlayers().get(0);
				model.fireRoundStarted();
				currentPlayer.startTurn();
				model.fireTurnStarted(currentPlayer);
			} else {
				model.fireGameFinished();
			}
		}
	}

	void save(final String name, final JsonGenerator generator) {
		generator.writeStartObject(name);
		generator.write("started", started);
		if (currentPlayer != null) {
			generator.write("currentPlayer",  currentPlayer.getName());
		} else {
			generator.writeNull("currentPlayer");
		}
		generator.writeEnd();
	}

	static GameManager load(final JsonParser parser, final List<Player> players) {
		parser.next(); // START_OBJECT
		parser.next(); // KEY_NAME
		final boolean started = parser.next() == JsonParser.Event.VALUE_TRUE; // VALUE_TRUE or VALUE_FALSE
		parser.next(); // KEY_NAME
		parser.next(); // VALUE_STRING
		final String currentPlayerName = parser.getString();
		final Player currentPlayer = players.stream()
			.filter(player -> player.getName().equals(currentPlayerName))
			.findAny()
			.orElse(null);
		parser.next(); // END_OBJECT

		final GameManager gameManager = new GameManager();
		gameManager.started = started;
		gameManager.currentPlayer = currentPlayer;

		return gameManager;
	}
}
