package org.microcol.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public final class Model {
	private final ListenerManager listenerManager;
	private final Calendar calendar;
	private final WorldMap map;
	private final List<Player> players;
	private final List<Town> towns;
	private final UnitStorage unitStorage;
	private final Europe europe;
	private final HighSea highSea;
	private GameManager gameManager;

	Model(final Calendar calendar, final WorldMap map, final List<Player> players, final List<Town> towns,
			final List<Unit> units, final Europe europe, final List<PlaceHighSea> highSeaUnits) {
		listenerManager = new ListenerManager();

		this.calendar = Preconditions.checkNotNull(calendar);
		this.map = Preconditions.checkNotNull(map);

		this.players = ImmutableList.copyOf(players);
		this.towns = Lists.newArrayList(towns);
		Preconditions.checkArgument(!this.players.isEmpty(), "There must be at least one player.");
		checkPlayerNames(this.players);
		this.players.forEach(player -> player.setModel(this));
		this.towns.forEach(town -> town.setModel(this));

		unitStorage = new UnitStorage(units);
		unitStorage.getUnits(true).forEach(unit -> unit.setModel(this));

		gameManager = new GameManager();
		gameManager.setModel(this);

		highSea = new HighSea(highSeaUnits);
		this.europe = Preconditions.checkNotNull(europe);
	}

	private void checkPlayerNames(final List<Player> players) {
		Set<String> names = new HashSet<>();
		players.forEach(player -> {
			if (!names.add(player.getName())) {
				throw new IllegalArgumentException(String.format("Duplicate player name (%s).", player.getName()));
			}
		});
	}

	public boolean isGameStarted() {
		return gameManager.isStarted();
	}

	public boolean isGameRunning() {
		return gameManager.isRunning();
	}

	public boolean isGameFinished() {
		return gameManager.isFinished();
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

	public List<Unit> getUnits() {
		return unitStorage.getUnits(false);
	}

	public List<Unit> getUnits(final boolean includeStored) {
		return unitStorage.getUnits(includeStored);
	}

	public Map<Location, List<Unit>> getUnitsAt() {
		return unitStorage.getUnitsAt();
	}

	public Map<Location, Town> getTownsAt() {
		return towns.stream().collect(ImmutableMap.toImmutableMap(Town::getLocation, Function.identity()));
	}

	public Optional<Town> getTownsAt(final Location location, final Player owner) {
		Preconditions.checkNotNull(location);
		Preconditions.checkNotNull(owner);
		return towns.stream().filter(town -> town.getOwner().equals(owner))
				.filter(town -> town.getLocation().equals(location)).findFirst();
	}

	public List<Unit> getUnitsAt(final Location location) {
		return unitStorage.getUnitsAt(location);
	}

	public void save(final String name, final JsonGenerator generator) {
		generator.writeStartObject(name);
		calendar.save("calendar", generator);
		map.save("map", generator);
		generator.writeStartArray("players");
		players.forEach(player -> player.save(generator));
		generator.writeEnd();
		unitStorage.save(generator);
		gameManager.save("game", generator);
		generator.writeEnd();
	}

	public static Model load(final JsonParser parser) {
		parser.next(); // START_OBJECT
		parser.next(); // KEY_NAME
		final Calendar calendar = Calendar.load(parser);
		parser.next(); // KEY_NAME
		final WorldMap map = WorldMap.load(parser);
		parser.next(); // KEY_NAME
		parser.next(); // START_ARRAY
		final List<Player> players = new ArrayList<>();
		Player player = null;
		while ((player = Player.load(parser)) != null) {
			players.add(player);
		}
		parser.next(); // KEY_NAME
		final List<Unit> units = UnitStorage.load(parser, players);
		parser.next(); // KEY_NAME
		final GameManager gameManager = GameManager.load(parser, players);
		parser.next(); // END_OBJECT

		final List<Town> towns = TownsStorage.load(parser, players);

		final Model model = new Model(calendar, map, players, towns, units, new Europe(Lists.newArrayList()), Lists.newArrayList());
		gameManager.setModel(model);
		model.gameManager = gameManager;

		return model;
	}

	List<Unit> getUnits(final Player player, final boolean includeStored) {
		return unitStorage.getUnits(player, includeStored);
	}

	Map<Location, List<Unit>> getUnitsAt(final Player player) {
		return unitStorage.getUnitsAt(player);
	}

	List<Unit> getUnitsAt(final Player player, final Location location) {
		return unitStorage.getUnitsAt(player, location);
	}

	List<Unit> getEnemyUnits(final Player player, final boolean includeStored) {
		return unitStorage.getEnemyUnits(player, includeStored);
	}

	Map<Location, List<Unit>> getEnemyUnitsAt(final Player player) {
		return unitStorage.getEnemyUnitsAt(player);
	}

	List<Unit> getEnemyUnitsAt(final Player player, final Location location) {
		return unitStorage.getEnemyUnitsAt(player, location);
	}

	public void startGame() {
		gameManager.startGame();
	}

	public void endTurn() {
		gameManager.endTurn();
	}

	void checkGameRunning() {
		gameManager.checkGameRunning();
	}

	void checkCurrentPlayer(final Player player) {
		gameManager.checkCurrentPlayer(player);
	}

	void destroyUnit(final Unit unit) {
		unitStorage.remove(unit);
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

	void fireUnitMoved(final Unit unit, final Location start, final Path path) {
		listenerManager.fireUnitMoved(this, unit, start, path);
	}

	void fireUnitAttacked(final Unit attacker, final Unit defender, final Unit destroyed) {
		listenerManager.fireUnitAttacked(this, attacker, defender, destroyed);
	}

	void fireUnitStored(final Unit unit, final CargoSlot slot) {
		listenerManager.fireUnitStored(this, unit, slot);
	}

	void fireGoldWasChanged(final Player player, final int oldValue, final int newValue) {
		listenerManager.fireGoldWasChanged(this, player, oldValue, newValue);
	}

	void fireGameFinished() {
		listenerManager.fireGameFinished(this);
	}

	// TODO JKA Temporary solution
	public void requestDebug(final List<Location> locations) {
		listenerManager.fireDebugRequested(this, locations);
	}

	public Europe getEurope() {
		return europe;
	}

	public HighSea getHighSea() {
		return highSea;
	}
}
