package org.microcol.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.json.stream.JsonGenerator;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

public class Model {
	private final ListenerManager listenerManager;
	private final Calendar calendar;
	private final WorldMap map;
	private final ImmutableList<Player> players;
	private final ShipStorage shipStorage;
	private final GameManager gameManager;

	Model(final Calendar calendar, final WorldMap map, final List<Player> players, final List<Ship> ships) {
		listenerManager = new ListenerManager();

		this.calendar = Preconditions.checkNotNull(calendar);
		this.map = Preconditions.checkNotNull(map);

		this.players = ImmutableList.copyOf(players);
		Preconditions.checkArgument(!this.players.isEmpty(), "There must be at least one player.");
		checkPlayerNames(this.players);
		this.players.forEach(player -> player.setModel(this));

		shipStorage = new ShipStorage(ships);
		shipStorage.getShips().forEach(ship -> ship.setModel(this));

		gameManager = new GameManager(this);
	}

	private void checkPlayerNames(final List<Player> players) {
		Set<String> names = new HashSet<>();
		players.forEach(player -> {
			if (!names.add(player.getName())) {
				throw new IllegalArgumentException(String.format("Duplicate player name (%s).", player.getName()));
			}
		});
	}

	public void addListener(ModelListener listener) {
		listenerManager.addListener(listener);
	}

	public void removeListener(ModelListener listener) {
		listenerManager.removeListener(listener);
	}

	public Calendar getCalendar() {
		return calendar;
	}

	public WorldMap getMap() {
		return map;
	}

	public List<Player> getPlayers() {
		return players;
	}

	public Player getCurrentPlayer() {
		return gameManager.getCurrentPlayer();
	}

	public List<Ship> getShips() {
		return shipStorage.getShips();
	}

	public Map<Location, List<Ship>> getShipsAt() {
		return shipStorage.getShipsAt();
	}

	public List<Ship> getShipsAt(final Location location) {
		return shipStorage.getShipsAt(location);
	}

	void save(final JsonGenerator generator) {
		generator.writeStartObject();
		calendar.save("calendar", generator);
		map.save("map", generator);
		generator.writeStartArray("players");
		players.forEach(player -> player.save(generator));
		generator.writeEnd();
		shipStorage.save(generator);
		gameManager.save("game", generator);
		generator.writeEnd();
	}

	List<Ship> getShips(final Player player) {
		return shipStorage.getShips(player);
	}

	Map<Location, List<Ship>> getShipsAt(final Player player) {
		return shipStorage.getShipsAt(player);
	}

	List<Ship> getShipsAt(final Player player, final Location location) {
		return shipStorage.getShipsAt(player, location);
	}

	List<Ship> getEnemyShips(final Player player) {
		return shipStorage.getEnemyShips(player);
	}

	Map<Location, List<Ship>> getEnemyShipsAt(final Player player) {
		return shipStorage.getEnemyShipsAt(player);
	}

	List<Ship> getEnemyShipsAt(final Player player, final Location location) {
		return shipStorage.getEnemyShipsAt(player, location);
	}

	public void startGame() {
		gameManager.startGame();
	}

	public void endTurn() {
		gameManager.endTurn();
	}

	void checkGameActive() {
		gameManager.checkGameActive();
	}

	void checkCurrentPlayer(final Player player) {
		gameManager.checkCurrentPlayer(player);
	}

	void destroyShip(final Ship ship) {
		shipStorage.remove(ship);
	}

	void fireGameStarted() {
		listenerManager.fireGameStarted(this);
	}

	void fireRoundStarted() {
		listenerManager.fireRoundStarted(this, calendar);
	}

	void fireTurnStarted(final Player player) {
		listenerManager.fireTurnStarted(this, player);
	}

	void fireShipMoved(final Ship ship, final Location start, final Path path) {
		listenerManager.fireShipMoved(this, ship, start, path);
	}

	void fireShipAttacked(final Ship attacker, final Ship defender, final Ship destroyed) {
		listenerManager.fireShipAttacked(this, attacker, defender, destroyed);
	}

	void fireGameFinished() {
		listenerManager.fireGameFinished(this);
	}

	public void requestDebug(final List<Location> locations) {
		listenerManager.fireDebugRequested(this, locations);
	}
}
