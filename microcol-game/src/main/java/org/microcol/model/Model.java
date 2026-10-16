package org.microcol.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.turnevent.TurnEventStore;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFactory;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitWithCargo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Game model.
 */
public class Model {

    private final Difficulty difficulty;
    private final ColonyNames colonyNames;
    private int nextColonyId;
    private final ListenerManager listenerManager;
    private final Calendar calendar;
    private final WorldMap map;
    private final PlayerStore playerStore;
    private final EuropeanRelations europeanRelations;
    private final List<Colony> colonies;
    private final UnitStorage unitStorage;
    private final Europe europe;
    private final HighSea highSea;
    private final GameManager gameManager;
    private Location focusedField;
    private boolean endingTurn;
    private final TurnEventStore turnEventStore;
    private final Statistics statistics;
    private final UnitFactory unitFactory;

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
                if (colonyfield.getUnit().isPresent()) {
                    final Unit unit = colonyfield.getUnit().get();
                    Preconditions.checkState(
                            unitStorage.getUnitById(unit.getId()).equals(unit));
                }
            });
            colony.getConstructions().forEach(construction -> {
                construction.getConstructionSlots().forEach(slot -> {
                    if (!slot.isEmpty()) {
                        final Unit unit = slot.getUnit();
                        Preconditions.checkState(unitStorage
                                .getUnitById(unit.getId()).equals(unit));
                    }
                });
            });
        });
    }

    Model(final Calendar calendar, final WorldMap map, final ModelPo modelPo,
            final UnitStorage unitStorage,
            final List<Function<Model, GameOverResult>> gameOverEvaluators) {
        Preconditions.checkNotNull(modelPo);
        Preconditions.checkArgument(modelPo.getDifficulty() != null,
                "Unsupported save: missing or invalid difficulty");
        difficulty = modelPo.getDifficulty();
        Preconditions.checkArgument(
                modelPo.getNextColonyId() != null
                        && modelPo.getNextColonyId() >= 0,
                "Unsupported save: missing or invalid nextColonyId");
        nextColonyId = modelPo.getNextColonyId();
        final Set<Integer> colonyIds = new HashSet<>();
        for (final ColonyPo colonyPo : modelPo.getColonies()) {
            Preconditions.checkArgument(
                    colonyPo.getId() != null && colonyPo.getId() >= 0
                            && colonyPo.getId() < nextColonyId
                            && colonyIds.add(colonyPo.getId()),
                    "Invalid or duplicate colony id");
        }
        modelPo.getTurnEvents().forEach(event -> {
            if (event.getColonyId() != null) {
                Preconditions.checkArgument(
                        event.getColonyId() >= 0
                                && event.getColonyId() < nextColonyId,
                        "Report refers to an unallocated colony id");
            }
        });
        listenerManager = new ListenerManager();
        this.focusedField = modelPo.getFocusedField();

        this.calendar = Preconditions.checkNotNull(calendar);
        this.map = Preconditions.checkNotNull(map);

        this.playerStore = PlayerStore.makePlayers(this, modelPo);
        europeanRelations = new EuropeanRelations(playerStore.getPlayers(),
                modelPo.getEuropeanRelations());
        turnEventStore = new TurnEventStore(modelPo, playerStore);

        colonyNames = new ColonyNames(this);

        this.colonies = new ArrayList<>();
        modelPo.getColonies().forEach(colonyPo -> {
            final List<ColonyBuildingItemProgress<?>> buildingQueue = new ArrayList<>();
            final Set<Integer> queueIds = new HashSet<>();
            Preconditions.checkArgument(colonyPo.getBuildingQueue() != null,
                    "Missing construction queue");
            colonyPo.getBuildingQueue().forEach(itemPo -> {
                Preconditions.checkArgument(
                        itemPo != null && itemPo.getType() != null
                                && itemPo.getId() != null && itemPo.getId() >= 0
                                && queueIds.add(itemPo.getId())
                                && itemPo.getBuildHammers() != null
                                && itemPo.getBuildHammers() >= 0,
                        "Invalid construction queue identity, kind or progress");
                if (itemPo.getType() == QueueItemType.CONSTRUCTION) {
                    final ConstructionType type = itemPo.getConstructionType();
                    Preconditions
                            .checkArgument(
                                    type != null
                                            && ConstructionType.ALL
                                                    .contains(type)
                                            && type.canBeConstructed()
                                            && itemPo.getUnitType() == null,
                                    "Invalid construction project type");
                    Preconditions.checkArgument(buildingQueue.stream().filter(
                            entry -> entry instanceof ColonyBuildingItemProgressConstruction)
                            .map(entry -> ((ColonyBuildingItemProgressConstruction) entry)
                                    .getConstructionType())
                            .noneMatch(queued -> type.getUpgradeChain()
                                    .contains(queued)
                                    || queued.getUpgradeChain().contains(type)),
                            "Duplicate or incompatible construction projects");
                    ColonyBuildingItemProgressConstruction item = new ColonyBuildingItemProgressConstruction(
                            new ColonyBuildingItemConstruction(
                                    itemPo.getConstructionType()),
                            itemPo.getId());
                    item.setBuildHammers(itemPo.getBuildHammers());
                    buildingQueue.add(item);
                } else if (itemPo.getType() == QueueItemType.UNIT) {
                    Preconditions.checkArgument(
                            itemPo.getUnitType() != null
                                    && UnitType.UNIT_TYPES
                                            .contains(itemPo.getUnitType())
                                    && itemPo.getUnitType().canBeBuildInColony()
                                    && itemPo.getConstructionType() == null,
                            "Invalid unit project type");
                    ColonyBuildingItemProgressUnit item = new ColonyBuildingItemProgressUnit(
                            new ColonyBuildingItemUnit(itemPo.getUnitType()),
                            itemPo.getId());
                    item.setBuildHammers(itemPo.getBuildHammers());
                    buildingQueue.add(item);
                } else {
                    throw new IllegalArgumentException(
                            "Unknown construction queue kind");
                }
            });
            final Colony col = new Colony(this, colonyPo.getId(),
                    colonyPo.getName(),
                    playerStore.getPlayerByName(colonyPo.getOwnerName()),
                    colonyPo.getLocation(), colony -> {
                        final List<Construction> constructions = new ArrayList<>();
                        colonyPo.getConstructions().forEach(constructionPo -> {
                            final Construction c = Construction.build(this,
                                    colony, constructionPo.getType());
                            constructions.add(c);
                        });
                        return constructions;
                    }, colonyPo.getColonyWarehouse(), buildingQueue);
            colonies.add(col);
        });

        for (final Colony colony : colonies) {
            Preconditions.checkArgument(
                    !colonyNames.getProblem(colony.getName(), colony)
                            .isPresent(),
                    "Invalid or duplicate colony name: %s", colony.getName());
            colony.setName(ColonyNames.normalize(colony.getName()));
        }

        unitFactory = new UnitFactory();
        this.unitStorage = Preconditions.checkNotNull(unitStorage);

        gameManager = GameManager.make(this, modelPo, gameOverEvaluators,
                playerStore);

        highSea = new HighSea(this);
        statistics = new Statistics(modelPo, playerStore);
        this.europe = new Europe(this);
    }

    /**
     * Each on-map unit reveals its owner's starting surroundings, including
     * computer players. Saved exploration outside that radius is preserved.
     */
    private void assureDefaultVisibility() {
        unitStorage.getUnits().stream().filter(Unit::isAtPlaceLocation)
                .forEach(unit -> unit.getOwner().revealMapForUnit(unit));
    }

    public static Model make(final ModelPo modelPo,
            final List<Function<Model, GameOverResult>> gameOverEvaluators) {
        Preconditions.checkNotNull(modelPo, "Persistent model is null");
        Preconditions.checkNotNull(gameOverEvaluators,
                "Game over evaluators are null");
        final Calendar calendar = Calendar.make(modelPo.getCalendar());
        final WorldMap worldMap = new WorldMap(modelPo);
        validateSavedColonyFields(modelPo, worldMap);
        final UnitStorage unitStorage = new UnitStorage(
                IdManager.makeFromModelPo(modelPo));

        final Model model = new Model(calendar, worldMap, modelPo, unitStorage,
                gameOverEvaluators);

        /*
         * First are loaded units which can hold cargo than which can be held in
         * cargo.
         */
        modelPo.getUnits().stream()
                .filter(unitPo -> unitPo.getType().canHoldCargo())
                .forEach(unitPo -> {
                    if (model.tryGetUnitById(unitPo.getId()).isPresent()) {
                        throw new MicroColException(String.format(
                                "unit with id %s was alredy loaded",
                                unitPo.getId()));
                    } else {
                        model.createUnit(modelPo, unitPo);
                    }
                });
        modelPo.getUnits().stream()
                .filter(unitPo -> !unitPo.getType().canHoldCargo())
                .forEach(unitPo -> {
                    if (model.tryGetUnitById(unitPo.getId()).isPresent()) {
                        throw new MicroColException(String.format(
                                "unit with id %s was alredy loaded",
                                unitPo.getId()));
                    } else {
                        model.createUnit(modelPo, unitPo);
                    }
                });

        if (modelPo.getUnits().size() != unitStorage.getUnits().size()) {
            throw new MicroColException(String.format(
                    "In source data in %s units by in model is %s units.",
                    modelPo.getUnits().size(), unitStorage.getUnits().size()));
        }

        model.checkUnits();
        model.assureDefaultVisibility();
        return model;
    }

    private static void validateSavedColonyFields(final ModelPo data,
            final WorldMap map) {
        Preconditions.checkArgument(
                data.getUnits() != null && data.getColonies() != null,
                "Missing saved units or colonies");
        final Map<Integer, UnitPo> units = new HashMap<>();
        for (final UnitPo unit : data.getUnits()) {
            Preconditions
                    .checkArgument(
                            unit != null && unit.getId() != null
                                    && units.putIfAbsent(unit.getId(),
                                            unit) == null,
                            "Missing or duplicate saved unit identity");
        }
        final Set<Location> occupiedSquares = new HashSet<>();
        final Set<Integer> jobWorkers = new HashSet<>();
        final Set<Integer> outdoorWorkers = new HashSet<>();
        for (final ColonyPo colony : data.getColonies()) {
            Preconditions.checkArgument(
                    colony != null && colony.getLocation() != null
                            && colony.getOwnerName() != null
                            && colony.getColonyFields() != null
                            && colony.getConstructions() != null,
                    "Incomplete saved colony assignments");
            final Set<Location> directions = new HashSet<>();
            for (final ColonyFieldPo field : colony.getColonyFields()) {
                Preconditions.checkArgument(
                        field != null
                                && Direction.getVectors()
                                        .contains(field.getDirection())
                                && directions.add(field.getDirection()),
                        "Invalid or duplicate saved field direction");
                final Integer workerId = field.getWorkerId();
                if (workerId == null) {
                    Preconditions.checkArgument(
                            field.getProducedGoodsType() == null,
                            "Empty saved field has production");
                    continue;
                }
                Preconditions.checkArgument(jobWorkers.add(workerId),
                        "Saved worker has multiple colony jobs");
                final UnitPo worker = units.get(workerId);
                Preconditions
                        .checkArgument(
                                worker != null && colony
                                        .getOwnerName()
                                        .equals(worker.getOwnerId())
                                        && (worker
                                                .getType() == UnitType.COLONIST
                                                || UnitType.EUROPEAN_SPECIALISTS
                                                        .contains(worker
                                                                .getType())),
                                "Invalid saved field worker identity, owner or role");
                Preconditions.checkArgument(
                        worker.getPlaceColonyField() != null
                                && worker.getPlaceMap() == null
                                && worker.getPlaceConstructionSlot() == null
                                && worker.getPlaceCargoSlot() == null
                                && worker.getPlaceHighSeas() == null
                                && worker.getPlaceEuropePort() == null,
                        "Saved field worker has a conflicting place");
                final Location at = colony.getLocation()
                        .add(field.getDirection());
                Preconditions.checkArgument(
                        field.getProducedGoodsType() != null && map.isValid(at)
                                && map.getTerrainTypeAt(at).canProduce(
                                        field.getProducedGoodsType()),
                        "Invalid saved field crop or location");
                Preconditions.checkArgument(occupiedSquares.add(at),
                        "Multiple colonies work the same saved world square");
                outdoorWorkers.add(workerId);
            }
            for (final var construction : colony.getConstructions()) {
                Preconditions.checkArgument(
                        construction != null && construction.getSlots() != null,
                        "Missing saved construction slots");
                for (final var slot : construction.getSlots()) {
                    Preconditions.checkArgument(slot != null,
                            "Missing saved construction slot");
                    Preconditions.checkArgument(
                            slot.getWorkerId() == null
                                    || jobWorkers.add(slot.getWorkerId()),
                            "Saved worker has multiple colony jobs");
                }
            }
        }
        for (final UnitPo unit : units.values()) {
            Preconditions.checkArgument(
                    unit.getPlaceColonyField() == null
                            || outdoorWorkers.contains(unit.getId()),
                    "Saved outdoor worker has no field assignment");
        }
    }

    public Colony buildColony(final Player player, final Unit unit,
            final String name) {
        checkGameRunning();
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(unit);
        Preconditions.checkArgument(
                player == getCurrentPlayer() && unit.getOwner() == player,
                "Only the current owner may found a colony");
        Preconditions.checkArgument(canUnitBuildColony(unit),
                "This unit cannot found a colony here");
        Preconditions.checkArgument(
                !colonyNames.getProblem(name, null).isPresent(),
                "Invalid colony name");
        Preconditions.checkState(nextColonyId < Integer.MAX_VALUE,
                "Colony IDs exhausted");
        final Location location = unit.getLocation();

        final Colony col = new Colony(this, nextColonyId,
                ColonyNames.normalize(name), player, location, colony -> {
                    final List<Construction> constructions = new ArrayList<>();
                    ConstructionType.NEW_COLONY_CONSTRUCTIONS
                            .forEach(constructionType -> {
                                final Construction c = Construction.build(this,
                                        colony, constructionType);
                                constructions.add(c);
                            });
                    return constructions;
                }, new HashMap<String, Integer>(), new ArrayList<>());
        final List<ColonyField> fields = col
                .getEmptyFieldsWithMaxCornProduction();
        final ConstructionSlot indoorWork = col
                .getConstructionByType(ConstructionType.TOWN_HALL).getSlotAt(0);
        Preconditions.checkState(indoorWork.isEmpty(),
                "Founding workplace is occupied");
        if (unit instanceof UnitFreeColonist) {
            final UnitFreeColonist colonist = (UnitFreeColonist) unit;
            final Map<GoodsType, Integer> returnedEquipment = ImmutableMap.of(
                    GoodsType.MUSKET,
                    colonist.isHoldingGuns()
                            ? UnitFreeColonist.REQUIRED_MUSKETS_FOR_ARMED_UNIT
                            : 0,
                    GoodsType.HORSE,
                    colonist.isMounted()
                            ? UnitFreeColonist.REQUIRED_HORSES_FOR_MOUNTED_UNIT
                            : 0,
                    GoodsType.TOOLS, colonist.getTools());
            returnedEquipment
                    .forEach((type, amount) -> Preconditions.checkState(
                            amount >= 0 && amount <= col.getWarehouse()
                                    .getStorageCapacity(type).getAmount(),
                            "Founder equipment does not fit in the new colony"));
        }
        nextColonyId++;
        colonies.add(col);
        if (fields.isEmpty()) {
            unit.placeToColonyStructureSlot(indoorWork);
        } else {
            unit.placeToColonyField(fields.get(0), GoodsType.CORN);
        }
        listenerManager.fireColonyWasFounded(this, col);
        return col;
    }

    public Optional<ColonyNameProblem> getColonyNameProblem(final String name,
            final Colony colony) {
        return colonyNames.getProblem(name, colony);
    }

    public String suggestColonyName(final Set<String> excluded) {
        return colonyNames.suggest(excluded);
    }

    public boolean canRenameColony(final Player player, final Colony colony) {
        return colony != null && colonies.contains(colony)
                && player == getCurrentPlayer() && colony.getOwner() == player;
    }

    public void renameColony(final Player player, final Colony colony,
            final String name) {
        checkTurnPreparationComplete();
        Preconditions.checkArgument(canRenameColony(player, colony),
                "Colony cannot be renamed");
        Preconditions.checkArgument(
                !colonyNames.getProblem(name, colony).isPresent(),
                "Invalid colony name");
        colony.setName(ColonyNames.normalize(name));
    }

    public List<Colony> getColonies() {
        return ImmutableList.copyOf(colonies);
    }

    public Optional<Colony> getColonyById(final int id) {
        return colonies.stream().filter(colony -> colony.getId() == id)
                .findFirst();
    }

    public boolean isColonyInNeighbourhood(final Location location) {
        return colonies.stream()
                .filter(col -> col.getLocation().isNeighbor(location)).findAny()
                .isPresent();
    }

    /**
     * Checks a prospective site without requiring the founder to be there yet.
     * Food policy, equipment capacity, turn and movement are separate checks.
     *
     * @param founder  registered prospective founder
     * @param location prospective colony position
     * @return whether the site permits founding
     */
    public boolean canFoundColonyAt(final Unit founder,
            final Location location) {
        if (founder == null || location == null)
            return false;
        if (!unitStorage.getUnits().stream().anyMatch(u -> u == founder))
            return false;
        if (!founder.getType().canBuildColony())
            return false;
        if (!map.isValid(location))
            return false;
        final TerrainType terrain = map.getTerrainTypeAt(location);
        if (terrain.isSea() || terrain == TerrainType.MOUNTAIN)
            return false;
        if (getColonyAt(location).isPresent())
            return false;
        if (isColonyInNeighbourhood(location))
            return false;
        if (getUnitsAt(location).stream()
                .anyMatch(u -> u.getOwner() != founder.getOwner()))
            return false;
        return true;
    }

    /**
     * Checks live founding permission at the unit's current position.
     *
     * @param unit prospective founder
     * @return whether the current player can found with this unit now
     */
    public boolean canUnitBuildColony(final Unit unit) {
        if (unit == null)
            return false;
        if (!isGameRunning())
            return false;
        if (unit.getOwner() != getCurrentPlayer())
            return false;
        if (!unit.isAtPlaceLocation())
            return false;
        final Location loc = unit.getLocation();
        if (loc == null || !map.isValid(loc))
            return false;
        if (unit.getActionPoints() <= 0)
            return false;
        if (!canFoundColonyAt(unit, loc))
            return false;
        return true;
    }

    /**
     * Read-only food opportunities around a prospective settlement.
     *
     * @param owner prospective colony owner
     * @param site  prospective colony position
     * @return valid, unworked fields that can produce corn
     */
    public List<Location> getAvailableColonyFoodFields(final Player owner,
            final Location site) {
        Preconditions.checkNotNull(site);
        return site.getNeighbors().stream()
                .filter(at -> isColonyFieldAvailable(owner, at))
                .filter(at -> map.getTerrainTypeAt(at)
                        .canProduce(GoodsType.CORN))
                .collect(ImmutableList.toImmutableList());
    }

    boolean isColonyFieldAvailable(final Player owner, final Location at) {
        return owner != null
                && getPlayers().stream().anyMatch(player -> player == owner)
                && map.isValid(at) && getColonyAt(at).isEmpty()
                && getUnitsAt(at).stream()
                        .noneMatch(unit -> unit.getOwner() != owner)
                && !isColonyFieldWorked(at);
    }

    boolean isColonyFieldWorked(final Location at) {
        return colonies.stream()
                .flatMap(colony -> colony.getColonyFields().stream())
                .anyMatch(field -> field.isNotEmpty()
                        && field.getLocation().equals(at));
    }

    public int getKingsTaxForPlayer(final Player subduedPlayer) {
        return playerStore.getKingsTaxForPlayer(subduedPlayer);
    }

    Unit createUnit(final ModelPo modelPo, final UnitPo unitPo) {
        final Unit unit = unitFactory.createUnit(this, modelPo, unitPo);
        unitStorage.addUnit(unit);
        return unit;
    }

    /**
     * Create cargo ship for king and put it to high seas in direction to
     * colonies.
     *
     * @param king required king player
     * @return created unit
     */
    public UnitWithCargo createCargoShipForKing(final Player king) {
        Preconditions.checkNotNull(king);
        Preconditions.checkArgument(
                getPlayers().stream().anyMatch(player -> player == king)
                        && king.isKing() && king.isComputer(),
                "Expected a registered computer king");
        getHighSea().getDefaultArrivalLocation()
                .orElseThrow(() -> new IllegalStateException(
                        "No eastern sea-lane arrival is available"));
        return (UnitWithCargo) unitStorage.createUnit(
                unit -> new Cargo(unit, UnitType.MAN_OF_WAR.getCargoCapacity()),
                this,
                unit -> ((AbstractUnit) unit).createVoyage(false,
                        Direction.east),
                UnitType.MAN_OF_WAR, king, UnitType.MAN_OF_WAR.getSpeed(),
                new UnitActionNoAction());
    }

    public VoyageReversalOffer offerVoyageReversal(
            final org.microcol.model.unit.Ship ship) {
        checkVoyageReversalShip(ship);
        return new VoyageReversalOffer(this, ship,
                (PlaceHighSea) ship.getPlace());
    }

    public void reverseVoyage(final VoyageReversalOffer offer) {
        Preconditions.checkNotNull(offer);
        Preconditions.checkArgument(offer.model == this,
                "Voyage offer belongs to another game");
        checkVoyageReversalShip(offer.ship);
        final PlaceHighSea voyage = (PlaceHighSea) offer.ship.getPlace();
        Preconditions.checkState(
                offer.owner == offer.ship.getOwner() && offer.voyage == voyage
                        && offer.revision == voyage.getRevision()
                        && offer.position == voyage.getPosition()
                        && offer.toEurope == voyage.isTravelToEurope(),
                "Voyage offer is no longer current");
        voyage.reverse();
        if (voyage.getRemainigTurns() == 0) {
            offer.ship.tryArriveFromHighSea();
        }
    }

    private void checkVoyageReversalShip(
            final org.microcol.model.unit.Ship ship) {
        Preconditions.checkNotNull(ship);
        checkGameRunning();
        checkTurnPreparationComplete();
        checkCurrentPlayer(ship.getOwner());
        Preconditions.checkState(
                tryGetUnitById(ship.getId()).orElse(null) == ship
                        && ship.isAtHighSea(),
                "Ship is not a registered in-transit unit");
    }

    public boolean isValid(final Location location) {
        return map.isValid(location);
    }

    /**
     * Create new royal expedition force unit and place it to cargo ship.
     *
     * @param king           required king
     * @param loadUnitToShip required ship that will hold cargo
     * @return created unit
     */
    public Unit createRoyalExpeditionForceUnit(final Player king,
            final UnitWithCargo loadUnitToShip) {
        Preconditions.checkNotNull(king);
        Preconditions.checkNotNull(king.isComputer(),
                "king have to be computer player");
        Preconditions.checkArgument(
                loadUnitToShip.getCargo().getEmptyCargoSlot().isPresent(),
                "Ship (%s) for cargo doesn't have any free slot for expedition force unit.",
                loadUnitToShip);
        final CargoSlot cargoSlot = loadUnitToShip.getCargo()
                .getEmptyCargoSlot().get();
        final Unit out = unitStorage.createUnit(
                unit -> new Cargo(unit, UnitType.COLONIST.getCargoCapacity()),
                this, unit -> new PlaceCargoSlot(unit, cargoSlot),
                UnitType.COLONIST, king, UnitType.COLONIST.getSpeed(),
                new UnitActionNoAction());
        ((UnitFreeColonist) out).setHoldingGuns(true);
        cargoSlot.store(out.getPlaceCargoSlot());
        return out;
    }

    /**
     * Add unit to player and place it to Europe.
     *
     * @param unitType required unit type
     * @param owner    required unit owner
     */
    void addUnitInEurope(final UnitType unitType, final Player owner) {
        unitStorage.createUnit(
                unit -> new Cargo(unit, unitType.getCargoCapacity()), this,
                unit -> {
                    if (unitType.isShip()) {
                        return new PlaceEuropePort(unit, europe.getPort());
                    } else {
                        return new PlaceEuropePier(unit);
                    }
                }, unitType, owner, unitType.getSpeed(),
                new UnitActionNoAction());
    }

    Unit addUnitOutSideColony(final Colony colony) {
        return unitStorage.createUnit(
                unit -> new Cargo(unit, UnitType.COLONIST.getCargoCapacity()),
                this, unit -> {
                    return new PlaceLocation(unit, colony.getLocation(),
                            unit.getDefaultOrintation());
                }, UnitType.COLONIST, colony.getOwner(),
                UnitType.COLONIST.getSpeed(), new UnitActionNoAction());
    }

    /**
     * A port uses eight-direction water adjacency, without an
     * ocean-connectivity rule.
     *
     * @param player   required port owner
     * @param location required colony square
     * @return whether this is the player's coastal colony
     */
    public boolean isOwnedCoastalPort(final Player player,
            final Location location) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(location);
        return getMap().isValid(location) && getColonyAt(location)
                .filter(colony -> colony.getOwner() == player).isPresent()
                && location.getNeighbors().stream().filter(getMap()::isValid)
                        .anyMatch(neighbor -> getMap()
                                .getTerrainTypeAt(neighbor).isSea());
    }

    public EuropeanRelationState getEuropeanRelation(final Player first,
            final Player second) {
        return europeanRelations.get(first, second);
    }

    /**
     * Diplomatic permission; Crown actions retain the existing rebellion path.
     *
     * @param first  registered participant
     * @param second registered participant
     * @return whether the distinct participants may engage in hostilities
     */
    public boolean areHostile(final Player first, final Player second) {
        checkRegisteredPlayers(first, second);
        return first != second
                && (first.isKing() || second.isKing() || europeanRelations
                        .get(first, second) == EuropeanRelationState.WAR);
    }

    private void checkRegisteredPlayers(final Player first,
            final Player second) {
        Preconditions.checkArgument(
                first != null && second != null
                        && playerStore.getPlayers().stream()
                                .anyMatch(p -> p == first)
                        && playerStore.getPlayers().stream()
                                .anyMatch(p -> p == second),
                "Unregistered relation participant");
    }

    public void declareHostilities(final Player actor, final Player target) {
        changeEuropeanRelation(actor, target, EuropeanRelationState.PEACE,
                EuropeanRelationState.WAR);
    }

    /**
     * Records a treaty already accepted by both parties, not a peace offer.
     *
     * @param actor  current colonizing player accepting the treaty
     * @param target registered rival currently at war with the actor
     */
    public void acceptPeace(final Player actor, final Player target) {
        changeEuropeanRelation(actor, target, EuropeanRelationState.WAR,
                EuropeanRelationState.PEACE);
    }

    private void changeEuropeanRelation(final Player actor, final Player target,
            final EuropeanRelationState expected,
            final EuropeanRelationState next) {
        checkGameRunning();
        checkRegisteredPlayers(actor, target);
        checkCurrentPlayer(actor);
        europeanRelations.transition(actor, target, expected, next);
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
        gameManager.stop();
        listenerManager.fireGameStopped(this);
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

    private boolean canChangeSentry(final Unit unit,
            final UnitActionType from) {
        return unit != null && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && (unit.isAtPlaceLocation()
                        || unit.isAtEuropePier() && unit.isStorable())
                && unit.getAction().getType() == from;
    }

    public boolean canSetSentry(final Unit unit) {
        return canChangeSentry(unit, UnitActionType.noAction);
    }

    public void setSentry(final Unit unit) {
        Preconditions.checkState(canSetSentry(unit),
                "Unit cannot enter sentry");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).setWaitingOrder(UnitActionType.sentry);
    }

    public boolean canActivateSentry(final Unit unit) {
        return canChangeSentry(unit, UnitActionType.sentry);
    }

    public void activateSentry(final Unit unit) {
        Preconditions.checkState(canActivateSentry(unit),
                "Unit is not a current sentry");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).setWaitingOrder(UnitActionType.noAction);
    }

    void wakeSentriesForMovement(final Unit mover, final Location destination) {
        for (final Unit unit : unitStorage.getUnits()) {
            if (unit.getOwner() != mover.getOwner() && unit.isAtPlaceLocation()
                    && unit.getAction().getType() == UnitActionType.sentry
                    && unit.getLocation().isNeighbor(destination)
                    && unit.getVisibleLocations().contains(destination)) {
                ((AbstractUnit) unit).wakeSentry();
            }
        }
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
        return colonies.stream().collect(ImmutableMap
                .toImmutableMap(Colony::getLocation, Function.identity()));
    }

    /**
     * Get colony owned by player at some location.
     *
     * @param location required location
     * @param owner    required player
     * @return optional colony object
     */
    public Optional<Colony> getColoniesAt(final Location location,
            final Player owner) {
        Preconditions.checkNotNull(location);
        Preconditions.checkNotNull(owner);
        return colonies.stream()
                .filter(colony -> colony.getOwner().equals(owner))
                .filter(colony -> colony.getLocation().equals(location))
                .findFirst();
    }

    public Optional<Unit> getNextUnitForCurrentPlayer(final Unit currentUnit) {
        Preconditions.checkNotNull(currentUnit);
        Preconditions.checkState(
                getCurrentPlayer().equals(currentUnit.getOwner()),
                "current unit (%s) doest belongs to user that is on turn (%s)",
                currentUnit, getCurrentPlayer());
        return unitStorage.getNextUnitForPlayer(getCurrentPlayer(),
                currentUnit);
    }

    public Optional<Unit> getFirstSelectableUnit() {
        return unitStorage.getFirstSelectableUnit(getCurrentPlayer());
    }

    public boolean canSkipUnitTurn(final Unit unit) {
        return unit != null && isGameRunning()
                && unit.getOwner() == getCurrentPlayer()
                && getPlayers().stream()
                        .anyMatch(player -> player == unit.getOwner())
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.isAtPlaceLocation() && unit.getActionPoints() > 0
                && unit.getLocation() != null && map.isValid(unit.getLocation())
                && unit.getAction().isNoAction();
    }

    public void skipUnitTurn(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        Preconditions.checkState(canSkipUnitTurn(unit),
                "Unit turn cannot be skipped");
        unit.setActionType(UnitActionType.noAction);
    }

    public Optional<Unit> getFirstSelectableUnitAt(final Location location) {
        return unitStorage.getFirstSelectableUnitAt(getCurrentPlayer(),
                location);
    }

    public List<Unit> getMoveableUnitAtOwnedBy(final Location location,
            final Player player) {
        return unitStorage.getUnitsAt(location).stream()
                .filter(unit -> unit.getActionPoints() > 0
                        && unit.getOwner().equals(player))
                .collect(ImmutableList.toImmutableList());
    }

    public Optional<Colony> getColonyAt(final Location location) {
        Preconditions.checkNotNull(location);
        return colonies.stream()
                .filter(colony -> colony.getLocation().equals(location))
                .findFirst();
    }

    public List<Colony> getColonies(final Player owner) {
        Preconditions.checkNotNull(owner);
        return colonies.stream()
                .filter(colony -> colony.getOwner().equals(owner))
                .collect(ImmutableList.toImmutableList());
    }

    public Optional<Colony> getColonyByName(final String colonyName) {
        Preconditions.checkNotNull(colonyName);
        return colonies.stream()
                .filter(colony -> colonyName.equalsIgnoreCase(colony.getName()))
                .findAny();
    }

    public List<Unit> getUnitsAt(final Location location) {
        return unitStorage.getUnitsAt(location);
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public synchronized ModelPo save() {
        gameManager.checkTurnPreparationComplete();
        final ModelPo out = new ModelPo();
        out.setDifficulty(difficulty);
        out.setNextColonyId(nextColonyId);
        map.save(out);
        unitStorage.save(out);
        out.setCalendar(calendar.save());
        gameManager.save(out);
        playerStore.save(out);
        out.setEuropeanRelations(europeanRelations.save());
        out.setColonies(getSaveColonies());
        out.setFocusedField(focusedField);
        out.setTurnEvents(turnEventStore.save());
        out.setStatistics(statistics.save());
        return out;
    }

    private List<ColonyPo> getSaveColonies() {
        final List<ColonyPo> out = new ArrayList<>();
        colonies.forEach(colony -> out.add(colony.save()));
        return out;
    }

    /**
     * Get list of units owned by given player.
     *
     * @param player        required player's object
     * @param includeStored if it's <code>true</code> than list will contains
     *                      all units holds in cargo in colonies and units in
     *                      Europe port. When it's <code>false</code> than
     *                      result contain just unit visible on map.
     * @return return list of units
     */
    List<Unit> getUnitsOwnedBy(final Player player,
            final boolean includeStored) {
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
     * @param unit required moving unit
     * @param path required path
     * @throws IllegalStateException It's thrown when unit doesn't have enough
     *                               action points to move along whole given
     *                               path.
     */
    public void moveUnit(final Unit unit, final Path path) {
        checkTurnPreparationComplete();
        listenerManager.fireActionStarted(this);
        if (fireUnitMoveStarted(unit, path)) {
            path.getLocations().forEach(loc -> {
                /*
                 * Unit could move to high seas. So could be out of map.
                 */
                if (unit.isAtPlaceLocation()) {
                    unit.moveOneStep(loc);
                }
            });
            fireUnitMovedFinished(unit, path);
        }
        listenerManager.fireActionEnded(this);
    }

    boolean fireUnitMoveStarted(final Unit unit, final Path path) {
        return listenerManager.fireUnitMoveStarted(this, unit, path);
    }

    void fireUnitMovedFinished(final Unit unit, final Path path) {
        listenerManager.fireUnitMovedFinished(this, unit, path);
    }

    void fireNewUnitWasBorn(final Unit unit) {
        listenerManager.fireNewUnitWasBorn(this, unit);
    }

    void fireActionWasStarted(final Unit unit) {
        listenerManager.fireActionWasStarted(this, unit);
    }

    /**
     * Move selected unit on defined path. Unit will walk along path as far as
     * it will be possible. How far unit move depends on terrain and number of
     * available action points.
     * <p>
     * Unit have to be on map. Path have to available for unit.
     * </p>
     *
     * @param unit required moving unit
     * @param path required path
     */
    public void moveUnitAsFarAsPossible(final Unit unit, final Path path) {
        checkTurnPreparationComplete();
        if (listenerManager.fireUnitMoveStarted(this, unit, path)) {
            path.getLocations().forEach(loc -> {
                /*
                 * Check if unit is at place location is reasonable, because
                 * unit could in first step conquer city a be placed inside
                 * city.
                 */
                if (unit.isAtPlaceLocation() && unit.getActionPoints() > 0) {
                    unit.moveOneStep(loc);
                }
            });
            listenerManager.fireUnitMovedFinished(this, unit, path);
        }
    }

    public synchronized void startGame() {
        Preconditions.checkState(!endingTurn,
                "Cannot start or end game while ending a turn.");
        if (gameManager.isRunning()) {
            gameManager.continueGame();
        } else {
            gameManager.startGame();
        }
    }

    public synchronized void endTurn() {
        Preconditions.checkState(!endingTurn,
                "Cannot start or end game while ending a turn.");
        checkGameRunning();
        gameManager.checkCurrentPlayer(getCurrentPlayer());
        endingTurn = true;
        try {
            if (listenerManager.fireBeforeEndTurn(this)) {
                gameManager.endTurn();
            }
        } finally {
            endingTurn = false;
        }
    }

    void checkGameRunning() {
        gameManager.checkGameRunning();
    }

    void checkTurnPreparationComplete() {
        gameManager.checkTurnPreparationComplete();
    }

    void checkCurrentPlayer(final Player player) {
        gameManager.checkCurrentPlayer(player);
    }

    void destroyUnit(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        if (unit.canHoldCargo()) {
            final UnitWithCargo unitWithCargo = (UnitWithCargo) unit;
            unitWithCargo.getCargo().getSlots().stream().forEach(cargoSlot -> {
                if (cargoSlot.isLoadedUnit()) {
                    final Unit u = cargoSlot.getUnit().get();
                    destroyUnit(u);
                }
            });
        }
        unitStorage.remove(unit);
    }

    public void sellGoods(final CargoSlot cargoSlot, final Goods goods) {
        cargoSlot.sellAndEmpty(goods);
        listenerManager.fireGoodsWasSoldInEurope(this, goods);
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

    void fireTurnFinished(final Player player) {
        listenerManager.fireTurnFinished(this, player);
    }

    void fireUnitMovedStepStarted(final Unit unit, final Location start,
            final Location end, final Direction orientation) {
        listenerManager.fireUnitMovedStepStarted(this, unit, start, end,
                orientation);
    }

    void fireUnitMovedStepFinished(final Unit unit, final Location start,
            final Location end) {
        listenerManager.fireUnitMovedStepFinished(this, unit, start, end);
    }

    void fireUnitMovedToHighSeas(final Unit unit) {
        listenerManager.fireUnitMovedToHighSeas(this, unit);
    }

    public void fireUnitArrivedToColonies(final Ship ship) {
        listenerManager.fireUnitArrivedToColonies(this, ship);
    }

    void fireUnitMovedToColonyField(final Unit unit) {
        listenerManager.fireUnitMovedToColonyField(this, unit);
    }

    void fireUnitMovedToConstruction(final Unit unit) {
        listenerManager.fireUnitMovedToConstruction(this, unit);
    }

    void fireUnitMovedToLocation(final Unit unit) {
        listenerManager.fireUnitMovedToLocation(this, unit);
    }

    void fireUnitAttacked(final Unit attacker, final Unit defender,
            final Unit destroyed) {
        listenerManager.fireUnitAttacked(this, attacker, defender, destroyed);
    }

    void fireUnitEmbarked(final Unit unit, final CargoSlot slot) {
        listenerManager.fireUnitEmbarked(this, unit, slot);
    }

    void fireGoldWasChanged(final Player player, final int oldValue,
            final int newValue) {
        listenerManager.fireGoldWasChanged(this, player, oldValue, newValue);
    }

    void fireIndependenceWasDeclared(final Player whoDecalareIt) {
        listenerManager.fireIndependenceWasDeclared(this, whoDecalareIt);
    }

    boolean fireBeforeDeclaringIndependence(final Player whoDecalareIt) {
        return listenerManager.fireBeforeDeclaringIndependence(this,
                whoDecalareIt);
    }

    void fireColonyWasCaptured(final Model model, final Unit capturingUnit,
            final Colony capturedColony) {
        listenerManager.fireColonyWasCaptured(model, capturingUnit,
                capturedColony);
    }

    void fireGameFinished(final GameOverResult gameOverResult) {
        listenerManager.fireGameFinished(this, gameOverResult);
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
    public PlayerStore getPlayerStore() {
        return playerStore;
    }

    void destroyColony(final Colony colony) {
        Preconditions.checkNotNull(colony);
        if (!colonies.contains(colony)) {
            return;
        }
        int lostShips = 0;
        for (final Unit unit : new ArrayList<>(
                getUnitsAt(colony.getLocation()))) {
            if (!(unit instanceof Ship)
                    || unit.getOwner() != colony.getOwner()) {
                continue;
            }
            final Ship ship = (Ship) unit;
            final Optional<Location> destination = colony.getLocation()
                    .getNeighbors().stream().filter(map::isValid)
                    .filter(target -> ship.getType()
                            .canMoveAtTerrain(map.getTerrainTypeAt(target)))
                    .filter(target -> getEnemyUnitsAt(ship.getOwner(), target)
                            .isEmpty())
                    .sorted(Comparator.comparingInt(Location::getX)
                            .thenComparingInt(Location::getY))
                    .findFirst();
            if (destination.isPresent()) {
                ship.placeToLocation(destination.get());
            } else {
                destroyUnit(ship);
                lostShips++;
            }
        }
        getTurnEventStore().add(TurnEventProvider
                .getColonyWasDestroyed(colony.getOwner(), colony, lostShips));
        colonies.remove(colony);
    }

    boolean isExists(final Colony colony) {
        Preconditions.checkNotNull(colony);
        return colonies.contains(colony);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("hashcode", hashCode())
                .toString();
    }

    /**
     * @return the focusedField
     */
    public Location getFocusedField() {
        return focusedField;
    }

    /**
     * @param focusedField the focusedField to set
     */
    public void setFocusedField(Location focusedField) {
        this.focusedField = focusedField;
    }

    /**
     * @return the turnEventStore
     */
    public TurnEventStore getTurnEventStore() {
        return turnEventStore;
    }

    public void addTurnEvent(final TurnEvent turnEvent) {
        getTurnEventStore().add(turnEvent);
    }

    public List<TurnEvent> getTurnEventsLocalizedMessages(final Player player) {
        return turnEventStore.getLocalizedMessages(player);
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

    /**
     * @return the unitStorage
     */
    UnitStorage getUnitStorage() {
        return unitStorage;
    }

}
