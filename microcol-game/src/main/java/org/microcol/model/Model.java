package org.microcol.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public final class Model {
	private final ListenerManager listenerManager;
	private final Calendar calendar;
	private final WorldMap map;
	private final PlayerStore playerStore;
	private final List<Colony> colonies;
	private final UnitStorage unitStorage;
	private final Europe europe;
	private final HighSea highSea;
	private GameManager gameManager;

	Model(final Calendar calendar, final WorldMap map, final List<Player> players, final List<Colony> colonies,
			final List<Unit> units, final List<Unit> unitsInEuropePort) {
		listenerManager = new ListenerManager();

		this.calendar = Preconditions.checkNotNull(calendar);
		this.map = Preconditions.checkNotNull(map);

		this.playerStore = new PlayerStore(players);
		this.colonies = Lists.newArrayList(colonies);
		this.playerStore.getPlayers().forEach(player -> player.setModel(this));
		this.colonies.forEach(colony -> colony.setModel(this));

		unitStorage = new UnitStorage(units);
		unitStorage.getUnits(true).forEach(unit -> unit.setModel(this));

		gameManager = new GameManager(this);

		highSea = new HighSea(this);
		this.europe = new Europe(this);
		unitsInEuropePort.forEach(unit -> unit.placeToEuropePort(europe.getPort()));
		checkUnits();
	}

	/**
	 * Verify that all units are in unit storage and that all units from unit
	 * storage are placed to world.
	 */
	private void checkUnits() {
		/*
		 * It has to be checked. Because of unit could be hold just in colony
		 * field and not in unit storage.
		 */
		colonies.forEach(colony -> {
			colony.getColonyFields().forEach(colonyfield -> {
				if (!colonyfield.isEmpty()) {
					final Unit unit = colonyfield.getUnit();
					Preconditions.checkState(unitStorage.getUnitById(unit.getId()).equals(unit));
				}
			});
			colony.getConstructions().forEach(construction -> {
				construction.getConstructionSlots().forEach(slot -> {
					if (!slot.isEmpty()) {
						final Unit unit = slot.getUnit();
						Preconditions.checkState(unitStorage.getUnitById(unit.getId()).equals(unit));
					}
				});
			});
		});
	}

	Model(final Calendar calendar, final WorldMap map, final ModelPo modelPo, final List<Colony> colonies,
			final UnitStorage unitStorage, final List<Unit> unitsInEuropePort) {
		listenerManager = new ListenerManager();

		this.calendar = Preconditions.checkNotNull(calendar);
		this.map = Preconditions.checkNotNull(map);

		this.playerStore = PlayerStore.makePlayers(this, modelPo);
		
		this.colonies = Lists.newArrayList(colonies);
		modelPo.getColonies().forEach(colonyPo -> {
			final List<Construction> constructions = new ArrayList<>();
			colonyPo.getConstructions().forEach(constructionPo -> {
				final Construction c = Construction.build(constructionPo.getType());
				constructions.add(c);
			});
			final Colony col = new Colony(colonyPo.getOwnerName(), playerStore.getPlayerByName(colonyPo.getName()),
					colonyPo.getLocation(), constructions);
			colonies.add(col);
		});
		
		this.colonies.forEach(colony -> colony.setModel(this));
		this.unitStorage = Preconditions.checkNotNull(unitStorage);

		unitStorage.getUnits(true).forEach(unit -> unit.setModel(this));

		gameManager = new GameManager(this);

		highSea = new HighSea(this);
		this.europe = new Europe(this);
		unitsInEuropePort.forEach(unit -> unit.placeToEuropePort(europe.getPort()));
		checkUnits();
	}

	
	public static Model make(final ModelPo modelPo) {
		final Calendar calendar = Calendar.make(modelPo.getCalendar());
		final WorldMap worldMap = new WorldMap(modelPo);
		
		// TODO JJ finish colonies loading
		final List<Colony> colonies = new ArrayList<>();
		
		// TODO JJ finish units loading
		final List<Unit> units = new ArrayList<>();
		
		// TODO JJ finish loading of ships in Europe port
		final List<Unit> unitsInEuropePort = new ArrayList<>();
		
		final UnitStorage unitStorage = new UnitStorage(units);
		
		Model model =  new Model(calendar, worldMap, modelPo, colonies, unitStorage, unitsInEuropePort);
		
		modelPo.getUnits().forEach(unitPo -> {
			PlaceBuilderModelPo placeBuilderModelPo = new PlaceBuilderModelPo(unitPo, modelPo, model);
			Unit u = new Unit(unitPo, model, placeBuilderModelPo);
			model.unitStorage.getAllUnits().add(u);
		});
		
		return model;
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
		return playerStore.getPlayers();
	}

	public Player getCurrentPlayer() {
		return gameManager.getCurrentPlayer();
	}

	public List<Unit> getAllUnits() {
		return unitStorage.getAllUnits();
	}

	public List<Unit> getUnits() {
		return unitStorage.getUnits(false);
	}

	public List<Unit> getUnits(final boolean includeStored) {
		return unitStorage.getUnits(includeStored);
	}

	public Unit getUnitById(final int id) {
		return unitStorage.getUnitById(id);
	}

	public Map<Location, List<Unit>> getUnitsAt() {
		return unitStorage.getUnitsAt();
	}

	public Map<Location, Colony> getColoniesAt() {
		return colonies.stream().collect(ImmutableMap.toImmutableMap(Colony::getLocation, Function.identity()));
	}

	public Optional<Colony> getColoniesAt(final Location location, final Player owner) {
		Preconditions.checkNotNull(location);
		Preconditions.checkNotNull(owner);
		return colonies.stream().filter(colony -> colony.getOwner().equals(owner))
				.filter(colony -> colony.getLocation().equals(location)).findFirst();
	}

	public Optional<Colony> getColoniesAt(final Location location) {
		Preconditions.checkNotNull(location);
		return colonies.stream().filter(colony -> colony.getLocation().equals(location)).findFirst();
	}

	public List<Colony> getColonies(final Player owner) {
		Preconditions.checkNotNull(owner);
		return colonies.stream().filter(colony -> colony.getOwner().equals(owner))
				.collect(ImmutableList.toImmutableList());
	}

	public List<Unit> getUnitsAt(final Location location) {
		return unitStorage.getUnitsAt(location);
	}

	public ModelPo save(){
		final ModelPo out = new ModelPo();
		map.save(out);
		unitStorage.save(out);
		out.setCalendar(calendar.save());
		out.setPlayers(getSavePlayers());
		out.setColonies(getSaveColonies());
		return out;
	}
	
	private List<PlayerPo> getSavePlayers() {
		//TODO is it necessary? How it's used.
		final List<PlayerPo> out = new ArrayList<PlayerPo>();
		playerStore.getPlayers().forEach(player -> out.add(player.save()));
		return out;
	}
	
	private List<ColonyPo> getSaveColonies() {
		final List<ColonyPo> out = new ArrayList<>();
		colonies.forEach(colony -> out.add(colony.save()));
		return out;
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

	/**
	 * @return the playerStore
	 */
	PlayerStore getPlayerStore() {
		return playerStore;
	}
}
