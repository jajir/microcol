package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public final class Model {
	
	private final ColonyNames colonyNames;
	private final ListenerManager listenerManager;
	private final Calendar calendar;
	private final WorldMap map;
	private final PlayerStore playerStore;
	private final List<Colony> colonies;
	private final UnitStorage unitStorage;
	private final Europe europe;
	private final HighSea highSea;
	private final GameManager gameManager;

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

	Model(final Calendar calendar, final WorldMap map, final ModelPo modelPo, 
			final UnitStorage unitStorage, final List<Unit> unitsInEuropePort) {
		Preconditions.checkNotNull(modelPo);
		listenerManager = new ListenerManager();

		this.calendar = Preconditions.checkNotNull(calendar);
		this.map = Preconditions.checkNotNull(map);

		this.playerStore = PlayerStore.makePlayers(this, modelPo);
		
		colonyNames = new ColonyNames(this);
		
		this.colonies = Lists.newArrayList();
		modelPo.getColonies().forEach(colonyPo -> {
			final Colony col = new Colony(this, colonyPo.getName(), playerStore.getPlayerByName(colonyPo.getOwnerName()),
					colonyPo.getLocation(), colony -> {
						final List<Construction> constructions = new ArrayList<>();
						colonyPo.getConstructions().forEach(constructionPo -> {
							final Construction c = Construction.build(colony, constructionPo.getType());
							constructions.add(c);
						});
						return constructions;
					}, colonyPo.getColonyWarehouse());
			colonies.add(col);
		});
		
		this.unitStorage = Preconditions.checkNotNull(unitStorage);

		gameManager = new GameManager(this);

		highSea = new HighSea(this);
		this.europe = new Europe(this);
		unitsInEuropePort.forEach(unit -> unit.placeToEuropePort(europe.getPort()));
		checkUnits();
	}

	
	public static Model make(final ModelPo modelPo) {
		final Calendar calendar = Calendar.make(modelPo.getCalendar());
		final WorldMap worldMap = new WorldMap(modelPo);
		
		// TODO JJ finish units loading
		final List<Unit> units = new ArrayList<>();
		
		// TODO JJ finish loading of ships in Europe port
		final List<Unit> unitsInEuropePort = new ArrayList<>();
		
		final UnitStorage unitStorage = new UnitStorage(units);
		
		Model model =  new Model(calendar, worldMap, modelPo, unitStorage, unitsInEuropePort);
		
		modelPo.getUnits().forEach(unitPo -> {
			if (!model.tryGetUnitById(unitPo.getId()).isPresent()) {
				model.createUnit(model, modelPo, unitPo);
			}
		});
		
		return model;
	}
	
	public void buildColony(final Player player, final Unit unit){
		//TODO move method to colony store
		Preconditions.checkNotNull(player);
		Preconditions.checkNotNull(unit);
		Preconditions.checkArgument(unit.isAtPlaceLocation(), "Unit (%s) have to be on map", unit);
		Preconditions.checkArgument(!unit.getType().isShip(), "Ship Unit (%s) can't found city", unit);
		Preconditions.checkArgument(!unit.getType().canHoldCargo(), "Unit (%s) that transport cargo, can't found city",
				unit);
		final Location location = unit.getLocation();
		final Colony col = new Colony(this, colonyNames.getNewColonyName(player), player, location, colony -> {
			final List<Construction> constructions = new ArrayList<>();
			ConstructionType.NEW_COLONY_CONSTRUCTIONS.forEach(constructionType -> {
				final Construction c = Construction.build(colony, constructionType);
				constructions.add(c);
			});
			return constructions;
		}, new HashMap<String, Integer>());
		colonies.add(col);
		col.placeUnitToProduceFood(unit);
	}
	
	Unit createUnit(final Model model, final ModelPo modelPo, final UnitPo unitPo) {
		final Unit out = Unit.make(model, modelPo, unitPo);
		model.unitStorage.addUnit(out);
		return out;
	}
	
	/**
	 * Create cargo ship for king and put it to high seas in direction to
	 * colonies.
	 * 
	 * @param king
	 *            required king player
	 * @return created unit
	 */
	public Unit createCargoShipForKing(final Player king){
		Preconditions.checkNotNull(king);
		Preconditions.checkNotNull(king.isComputer(), "king have to be computer player");
		final Unit out = new Unit(unit -> new Cargo(unit, UnitType.GALLEON.getSpeed()), this, IdManager.nextId(),
				unit -> new PlaceHighSea(unit, false, 3), UnitType.GALLEON, king, UnitType.GALLEON.getSpeed());
		unitStorage.addUnit(out);
		return out;
	}

	public boolean isValid(final Path path) {
		return map.isValid(path);
	}

	/**
	 * Create new royal expedition force unit and place it to cargo ship.
	 * 
	 * @param king
	 *            required king
	 * @param loadUnitToShip
	 *            required ship that will hold cargo
	 * @return created unit
	 */
	public Unit createRoyalExpeditionForceUnit(final Player king, final Unit loadUnitToShip){
		Preconditions.checkNotNull(king);
		Preconditions.checkNotNull(king.isComputer(), "king have to be computer player");
		Preconditions.checkArgument(loadUnitToShip.getCargo().getEmptyCargoSlot().isPresent(),"Ship (%s) for cargo doesn't have any free slot for expedition force unit.",loadUnitToShip);
		CargoSlot cargoSlot = loadUnitToShip.getCargo().getEmptyCargoSlot().get();
		final Unit out = new Unit(unit -> new Cargo(unit, UnitType.COLONIST.getSpeed()), this, IdManager.nextId(),
				unit -> new PlaceCargoSlot(unit, cargoSlot), UnitType.COLONIST, king, UnitType.COLONIST.getSpeed());
		unitStorage.addUnit(out);
		return out;
	}
	
	void addUnitToPlayer(final UnitType unitType, final Player owner){
		unitStorage.addUnitToPlayer(unitType, owner, this);
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
		return ImmutableList.copyOf(playerStore.getPlayers());
	}

	public Player getCurrentPlayer() {
		return gameManager.getCurrentPlayer();
	}

	public List<Unit> getAllUnits() {
		return unitStorage.getUnits();
	}

	public Unit getUnitById(final int id) {
		return unitStorage.getUnitById(id);
	}
	
	public Optional<Unit> tryGetUnitById(final int id){
		return unitStorage.tryGetUnitById(id);
	}

	public Map<Location, List<Unit>> getUnitsAt() {
		return unitStorage.getUnitsAt();
	}

	public Map<Location, Colony> getColoniesAt() {
		return colonies.stream().collect(ImmutableMap.toImmutableMap(Colony::getLocation, Function.identity()));
	}

	/**
	 * Get colony owned by player at some location.
	 * 
	 * @param location
	 *            required location
	 * @param owner
	 *            required player
	 * @return optional colony object
	 */
	public Optional<Colony> getColoniesAt(final Location location, final Player owner) {
		Preconditions.checkNotNull(location);
		Preconditions.checkNotNull(owner);
		return colonies.stream().filter(colony -> colony.getOwner().equals(owner))
				.filter(colony -> colony.getLocation().equals(location)).findFirst();
	}
	
	public Unit getNextUnitForCurrentUser(final Unit currentUnit){
		Preconditions.checkState(getCurrentPlayer().equals(currentUnit.getOwner()),
				"current unit (%s) doest belongs to user that is on turn (%s)", currentUnit, getCurrentPlayer());
		return unitStorage.getNextUnitForCurrentUser(getCurrentPlayer(), currentUnit);
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
		return playerStore.getPlayers().stream().map(player -> player.save()).collect(ImmutableList.toImmutableList());
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
	
	void fireColonyWasCaptured(final Model model, final Unit capturingUnit, final Colony capturedColony) {
		listenerManager.fireColonyWasCaptured(model, capturingUnit, capturedColony);
	}

	void fireGameFinished(final GameOverResult gameOverResult) {
		listenerManager.fireGameFinished(this, gameOverResult);
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
	
	public Player getPlayerByName(final String playerName){
		return playerStore.getPlayerByName(playerName);
	}

	/**
	 * @return the playerStore
	 */
	PlayerStore getPlayerStore() {
		return playerStore;
	}
	
	void destroyColony(final Colony colony){
		Preconditions.checkNotNull(colony);
		colonies.remove(colony);
	}
	
	boolean isExists(final Colony colony){
		Preconditions.checkNotNull(colony);
		return colonies.contains(colony);
	}
	
}
