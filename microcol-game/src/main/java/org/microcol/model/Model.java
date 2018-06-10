package org.microcol.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.microcol.gui.MicroColException;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventStore;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * Game model.
 * 
 * TODO when move is in progress block call of another active operation.
 */
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
    private Location focusedField;
    private final TurnEventStore turnEventStore;
    private final Statistics statistics;

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
                        Preconditions
                                .checkState(unitStorage.getUnitById(unit.getId()).equals(unit));
                    }
                });
            });
        });
    }

    Model(final Calendar calendar, final WorldMap map, final ModelPo modelPo,
            final UnitStorage unitStorage,
            final List<Function<Model, GameOverResult>> gameOverEvaluators) {
        Preconditions.checkNotNull(modelPo);
        listenerManager = new ListenerManager();
        this.focusedField = modelPo.getFocusedField();

        this.calendar = Preconditions.checkNotNull(calendar);
        this.map = Preconditions.checkNotNull(map);

        this.playerStore = PlayerStore.makePlayers(this, modelPo);
        turnEventStore = new TurnEventStore(modelPo, playerStore);

        colonyNames = new ColonyNames(this);

        this.colonies = Lists.newArrayList();
        modelPo.getColonies().forEach(colonyPo -> {
            final Colony col = new Colony(this, colonyPo.getName(),
                    playerStore.getPlayerByName(colonyPo.getOwnerName()), colonyPo.getLocation(),
                    colony -> {
                        final List<Construction> constructions = new ArrayList<>();
                        colonyPo.getConstructions().forEach(constructionPo -> {
                            final Construction c = Construction.build(colony,
                                    constructionPo.getType());
                            constructions.add(c);
                        });
                        return constructions;
                    }, colonyPo.getColonyWarehouse());
            colonies.add(col);
        });

        this.unitStorage = Preconditions.checkNotNull(unitStorage);

        gameManager = GameManager.make(this, modelPo, gameOverEvaluators, playerStore);

        highSea = new HighSea(this);
        statistics = new Statistics(modelPo, playerStore);
        this.europe = new Europe(this);
    }

    /**
     * For each unit owned by human on assure that visible are is really
     * revealed. It allows to not-define correct visible area in save files.
     */
    private void assureDefaultVisibility() {
        unitStorage.getUnits().stream()
                .filter(unit -> unit.getOwner().isHuman() && unit.isAtPlaceLocation())
                .forEach(unit -> unit.getOwner().makeVisibleMapForUnit(unit));
    }

    public static Model make(final ModelPo modelPo,
            final List<Function<Model, GameOverResult>> gameOverEvaluators) {
        final Calendar calendar = Calendar.make(modelPo.getCalendar());
        final WorldMap worldMap = new WorldMap(modelPo);
        final UnitStorage unitStorage = new UnitStorage(IdManager.makeFromModelPo(modelPo));

        final Model model = new Model(calendar, worldMap, modelPo, unitStorage, gameOverEvaluators);

        /*
         * First are loaded units which can hold cargo than which can be held in
         * cargo.
         */
        modelPo.getUnits().stream().filter(unitPo -> unitPo.getType().canHoldCargo())
                .forEach(unitPo -> {
                    if (model.tryGetUnitById(unitPo.getId()).isPresent()) {
                        throw new MicroColException(
                                String.format("unit with id %s was alredy loaded", unitPo.getId()));
                    } else {
                        model.createUnit(model, modelPo, unitPo);
                    }
                });
        modelPo.getUnits().stream().filter(unitPo -> !unitPo.getType().canHoldCargo())
                .forEach(unitPo -> {
                    if (model.tryGetUnitById(unitPo.getId()).isPresent()) {
                        throw new MicroColException(
                                String.format("unit with id %s was alredy loaded", unitPo.getId()));
                    } else {
                        model.createUnit(model, modelPo, unitPo);
                    }
                });

        if (modelPo.getUnits().size() != unitStorage.getUnits().size()) {
            throw new MicroColException(
                    String.format("In source data in %s units by in model is %s units.",
                            modelPo.getUnits().size(), unitStorage.getUnits().size()));
        }

        model.checkUnits();
        model.assureDefaultVisibility();
        return model;
    }

    public void buildColony(final Player player, final Unit unit) {
        // TODO move method to colony store
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(unit);
        Preconditions.checkArgument(unit.isAtPlaceLocation(), "Unit (%s) have to be on map", unit);
        Preconditions.checkArgument(!unit.getType().isShip(), "Ship Unit (%s) can't found city",
                unit);
        Preconditions.checkArgument(!unit.getType().canHoldCargo(),
                "Unit (%s) that transport cargo, can't found city", unit);
        final Location location = unit.getLocation();
        final Colony col = new Colony(this, colonyNames.getNewColonyName(player), player, location,
                colony -> {
                    final List<Construction> constructions = new ArrayList<>();
                    ConstructionType.NEW_COLONY_CONSTRUCTIONS.forEach(constructionType -> {
                        final Construction c = Construction.build(colony, constructionType);
                        constructions.add(c);
                    });
                    return constructions;
                }, new HashMap<String, Integer>());
        colonies.add(col);
        col.placeUnitToProduceFood(unit);
        listenerManager.fireColonyWasFounded(this, col);
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
    public Unit createCargoShipForKing(final Player king) {
        Preconditions.checkNotNull(king);
        Preconditions.checkNotNull(king.isComputer(), "king have to be computer player");
        return unitStorage.createUnit(unit -> new Cargo(unit, UnitType.GALLEON.getSpeed()), this,
                unit -> new PlaceHighSea(unit, false, 3), UnitType.GALLEON, king,
                UnitType.GALLEON.getSpeed());
    }

    public List<TurnEvent> getLocalizedMessages(final Player player,
            final Function<String, String> messageProvider) {
        return turnEventStore.getLocalizedMessages(player, messageProvider);
    }

    public boolean isValid(final Path path) {
        return map.isValid(path);
    }

    public boolean isValid(final Location location) {
        return map.isValid(location);
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
    public Unit createRoyalExpeditionForceUnit(final Player king, final Unit loadUnitToShip) {
        Preconditions.checkNotNull(king);
        Preconditions.checkNotNull(king.isComputer(), "king have to be computer player");
        Preconditions.checkArgument(loadUnitToShip.getCargo().getEmptyCargoSlot().isPresent(),
                "Ship (%s) for cargo doesn't have any free slot for expedition force unit.",
                loadUnitToShip);
        CargoSlot cargoSlot = loadUnitToShip.getCargo().getEmptyCargoSlot().get();
        return unitStorage.createUnit(unit -> new Cargo(unit, UnitType.COLONIST.getSpeed()), this,
                unit -> new PlaceCargoSlot(unit, cargoSlot), UnitType.COLONIST, king,
                UnitType.COLONIST.getSpeed());
    }

    /**
     * Add unit to player and place it to Europe.
     *
     * @param unitType
     *            required unit type
     * @param owner
     *            required unit owner
     */
    void addUnitInEurope(final UnitType unitType, final Player owner) {
        unitStorage.createUnit(unit -> new Cargo(unit, unitType.getCargoCapacity()), this, unit -> {
            if (unitType.isShip()) {
                return new PlaceEuropePort(unit, europe.getPort());
            } else {
                return new PlaceEuropePier(unit);
            }
        }, unitType, owner, unitType.getSpeed());
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

    public void addListener(final ModelListener listener) {
        listenerManager.addListener(listener);
    }

    public void removeListener(final ModelListener listener) {
        listenerManager.removeListener(listener);
    }

    /**
     * Prepare model to be removed from memory.
     */
    public void stop() {
        listenerManager.removeAllListeners();
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

    public Optional<Unit> tryGetUnitById(final int id) {
        return unitStorage.tryGetUnitById(id);
    }

    public Map<Location, List<Unit>> getUnitsAt() {
        return unitStorage.getUnitsAt();
    }

    public Map<Location, Colony> getColoniesAt() {
        return colonies.stream()
                .collect(ImmutableMap.toImmutableMap(Colony::getLocation, Function.identity()));
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

    public Optional<Unit> getNextUnitForCurrentUser(final Unit currentUnit) {
        Preconditions.checkNotNull(currentUnit);
        Preconditions.checkState(getCurrentPlayer().equals(currentUnit.getOwner()),
                "current unit (%s) doest belongs to user that is on turn (%s)", currentUnit,
                getCurrentPlayer());
        return unitStorage.getNextUnitForCurrentUser(getCurrentPlayer(), currentUnit);
    }

    public Optional<Unit> getFirstSelectableUnit() {
        return unitStorage.getFirstSelectableUnit(getCurrentPlayer());
    }

    public Optional<Unit> getFirstSelectableUnitAt(final Location location) {
        return unitStorage.getFirstSelectableUnitAt(getCurrentPlayer(), location);
    }

    public Optional<Colony> getColonyAt(final Location location) {
        Preconditions.checkNotNull(location);
        return colonies.stream().filter(colony -> colony.getLocation().equals(location))
                .findFirst();
    }

    public List<Colony> getColonies(final Player owner) {
        Preconditions.checkNotNull(owner);
        return colonies.stream().filter(colony -> colony.getOwner().equals(owner))
                .collect(ImmutableList.toImmutableList());
    }

    public List<Unit> getUnitsAt(final Location location) {
        return unitStorage.getUnitsAt(location);
    }

    public ModelPo save() {
        final ModelPo out = new ModelPo();
        map.save(out);
        unitStorage.save(out);
        out.setCalendar(calendar.save());
        //TODO move to same method to game manager
        out.getGameManager().setPlayers(getSavePlayers());
        out.getGameManager().setGameStarted(gameManager.isStarted());
        out.getGameManager().setCurrentPlayer(gameManager.getCurrentPlayer().getName());
        out.setColonies(getSaveColonies());
        out.setFocusedField(focusedField);
        out.setTurnEvents(turnEventStore.save());
        out.setStatistics(statistics.save());
        return out;
    }

    private List<PlayerPo> getSavePlayers() {
        return playerStore.getPlayers().stream().map(player -> player.save())
                .collect(ImmutableList.toImmutableList());
    }

    private List<ColonyPo> getSaveColonies() {
        final List<ColonyPo> out = new ArrayList<>();
        colonies.forEach(colony -> out.add(colony.save()));
        return out;
    }

    /**
     * Get list of units owned by given player.
     *
     * @param player
     *            required player's object
     * @param includeStored
     *            if it's <code>true</code> than list will contains all units
     *            holds in cargo in colonies and units in Europe port. When it's
     *            <code>false</code> than result contain just unit visible on
     *            map.
     * @return return list of units
     */
    List<Unit> getUnitsOwnedBy(final Player player, final boolean includeStored) {
        return unitStorage.getUnitsOwnedBy(player, includeStored);
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

    /**
     * Move selected unit on defined path.
     * <p>
     * Unit have to be on map. Path have to be accessible for unit.
     * </p>
     * 
     * @param unit
     *            required moving unit
     * @param path
     *            required path
     * @throws IllegalStateException
     *             It's thrown when unit doesn't have enough action points to
     *             move along whole given path.
     */
    public void moveUnit(final Unit unit, final Path path) {
        if (listenerManager.fireUnitMoveStarted(this, unit, path)) {
            path.getLocations().forEach(loc -> {
                unit.moveOneStep(loc);
            });
            listenerManager.fireUnitMovedFinished(this, unit, path);
        }
    }

    /**
     * Move selected unit on defined path. Unit will walk along path as far as
     * it will be possible. How far unit move depends on terrain and number of
     * available action points.
     * <p>
     * Unit have to be on map. Path have to available for unit.
     * </p>
     * 
     * @param unit
     *            required moving unit
     * @param path
     *            required path
     */
    public void moveUnitAsFarAsPossible(final Unit unit, final Path path) {
        if (listenerManager.fireUnitMoveStarted(this, unit, path)) {
            path.getLocations().forEach(loc -> {
                if (unit.getAvailableMoves() > 0) {
                    unit.moveOneStep(loc);
                }
            });
            listenerManager.fireUnitMovedFinished(this, unit, path);
        }
    }

    public void startGame() {
        if (gameManager.isRunning()) {
            gameManager.continueGame();
        } else {
            gameManager.startGame();
        }
    }

    public void endTurn() {
        if (listenerManager.fireBeforeEndTurn(this)) {
            gameManager.endTurn();
        }
    }

    void checkGameRunning() {
        gameManager.checkGameRunning();
    }

    void checkCurrentPlayer(final Player player) {
        gameManager.checkCurrentPlayer(player);
    }

    void destroyUnit(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        unit.getCargo().getSlots().stream().forEach(cargoSlot -> {
            if (cargoSlot.isLoadedUnit()) {
                final Unit u = cargoSlot.getUnit().get();
                destroyUnit(u);
            }
        });
        unitStorage.remove(unit);
    }

    public void sellGoods(final CargoSlot cargoSlot, final GoodsAmount goodsAmount) {
        cargoSlot.sellAndEmpty(goodsAmount);
        listenerManager.fireGoodsWasSoldInEurope(this, goodsAmount);
    }

    void fireGameStarted() {
        listenerManager.fireGameStarted(this);
    }

    void fireRoundStarted() {
        listenerManager.fireRoundStarted(this, calendar);
    }

    void fireTurnStarted(final Player player, final boolean isFreshStart) {
        listenerManager.fireTurnStarted(this, player, isFreshStart);
    }

    void fireUnitMovedStep(final Unit unit, final Location start, final Location end) {
        listenerManager.fireUnitMovedStep(this, unit, start, end);
    }

    void fireUnitMovedToHighSeas(final Unit unit) {
        listenerManager.fireUnitMovedToHighSeas(this, unit);
    }

    void fireUnitAttacked(final Unit attacker, final Unit defender, final Unit destroyed) {
        listenerManager.fireUnitAttacked(this, attacker, defender, destroyed);
    }

    void fireUnitEmbarked(final Unit unit, final CargoSlot slot) {
        listenerManager.fireUnitEmbarked(this, unit, slot);
    }

    void fireGoldWasChanged(final Player player, final int oldValue, final int newValue) {
        listenerManager.fireGoldWasChanged(this, player, oldValue, newValue);
    }

    void fireIndependenceWasDeclared(final Player whoDecalareIt) {
        listenerManager.fireIndependenceWasDeclared(this, whoDecalareIt);
    }

    boolean fireBeforeDeclaringIndependence(final Player whoDecalareIt) {
        return listenerManager.fireBeforeDeclaringIndependence(this, whoDecalareIt);
    }

    void fireColonyWasCaptured(final Model model, final Unit capturingUnit,
            final Colony capturedColony) {
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

    public Player getPlayerByName(final String playerName) {
        return playerStore.getPlayerByName(playerName);
    }

    /**
     * @return the playerStore
     */
    PlayerStore getPlayerStore() {
        return playerStore;
    }

    void destroyColony(final Colony colony) {
        Preconditions.checkNotNull(colony);
        colonies.remove(colony);
    }

    boolean isExists(final Colony colony) {
        Preconditions.checkNotNull(colony);
        return colonies.contains(colony);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode()).toString();
    }

    /**
     * @return the focusedField
     */
    public Location getFocusedField() {
        return focusedField;
    }

    /**
     * @param focusedField
     *            the focusedField to set
     */
    public void setFocusedField(Location focusedField) {
        this.focusedField = focusedField;
    }

    /**
     * @return the turnEventStore
     */
    TurnEventStore getTurnEventStore() {
        return turnEventStore;
    }

    public List<TurnEvent> getTurnEventsLocalizedMessages(final Player player,
            final Function<String, String> messageProvider) {
        return turnEventStore.getLocalizedMessages(player, messageProvider);
    }

    public boolean isTurnEventsMessagesEmpty(final Player player) {
        return turnEventStore.getForPlayer(player).isEmpty();
    }

    /**
     * @return the statistics
     */
    public Statistics getStatistics() {
        return statistics;
    }

}
