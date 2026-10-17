package org.microcol.model;

import java.util.ArrayList;
import java.util.ArrayDeque;
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
import org.microcol.model.unit.UnitActionFortify;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFactory;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitArtillery;
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
    private long combatRandomState;

    double nextCombatRandom() {
        combatRandomState += 0x9E3779B97F4A7C15L;
        long z = combatRandomState;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }

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
        Preconditions.checkArgument(modelPo.getCombatRandomState() != null,
                "Unsupported save: missing combat random state");
        combatRandomState = modelPo.getCombatRandomState();
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
                modelPo.getEuropeanRelations(),
                calendar.getNumberOfPlayedTurns());
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
                                && queueIds.add(itemPo.getId()),
                        "Invalid construction queue identity or kind");
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
            col.restoreExportGoods(colonyPo.getExportGoods());
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
        for (final ColonyPo savedColony : modelPo.getColonies()) {
            model.getColonyById(savedColony.getId()).orElseThrow()
                    .restoreEducationCourses(savedColony.getEducationCourses());
        }
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
                Preconditions.checkArgument(worker != null
                        && colony.getOwnerName().equals(worker.getOwnerId())
                        && (worker.getType() == UnitType.COLONIST || worker
                                .getType() == UnitType.INDENTURED_SERVANT
                                || worker.getType() == UnitType.PETTY_CRIMINAL
                                || UnitType.EUROPEAN_SPECIALISTS
                                        .contains(worker.getType())
                                || UnitType.SCHOOL_ONLY_SPECIALISTS
                                        .contains(worker.getType())),
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
                        field.getProducedGoodsType() != null
                                && map.isValid(at) && map
                                        .getTerrainTypeAt(at).canProduce(
                                                field.getProducedGoodsType())
                                && (!map.getTerrainTypeAt(at).isSea() || colony
                                        .getConstructions().stream().anyMatch(
                                                construction -> construction != null
                                                        && (construction
                                                                .getType() == ConstructionType.DOCK
                                                                || construction
                                                                        .getType() == ConstructionType.DRYDOCK
                                                                || construction
                                                                        .getType() == ConstructionType.SHIPYARD))),
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
        player.activateCongressAfterFirstColony();
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
    boolean canAddUnitInEurope() {
        return unitStorage.canAllocateUnit();
    }

    Unit addUnitInEurope(final UnitType unitType, final Player owner) {
        Preconditions.checkState(unitStorage.canAllocateUnit(),
                "No unit IDs remain");
        return unitStorage.createUnit(
                unit -> new Cargo(unit, unitType.getCargoCapacity()), this,
                unit -> {
                    if (unitType.isShip()) {
                        return new PlaceEuropePort(unit, europe.getPort());
                    } else {
                        return new PlaceEuropePier(unit);
                    }
                }, unitType, owner, owner.getEffectiveSpeed(unitType),
                new UnitActionNoAction());
    }

    /** Deliver Jones's one-time frigate to the first reachable rebel port. */
    Optional<Unit> deliverJonesFrigateAtCoastalColony(final Player owner) {
        Preconditions.checkNotNull(owner);
        if (!unitStorage.canAllocateUnit()) {
            return Optional.empty();
        }
        final Set<Location> reachableWater = new HashSet<>();
        final ArrayDeque<Location> frontier = new ArrayDeque<>();
        for (int x = 0; x <= map.getMaxLocationX(); x++) {
            for (int y = 0; y <= map.getMaxLocationY(); y++) {
                final Location location = Location.of(x, y);
                if (map.isValid(location)
                        && map.getTerrainTypeAt(
                                location) == TerrainType.HIGH_SEA
                        && getEnemyUnitsAt(owner, location).isEmpty()
                        && reachableWater.add(location)) {
                    frontier.add(location);
                }
            }
        }
        while (!frontier.isEmpty()) {
            for (final Location neighbor : frontier.removeFirst()
                    .getNeighbors()) {
                if (map.isValid(neighbor)
                        && map.getTerrainTypeAt(neighbor).isSea()
                        && getEnemyUnitsAt(owner, neighbor).isEmpty()
                        && reachableWater.add(neighbor)) {
                    frontier.add(neighbor);
                }
            }
        }
        final Optional<Colony> destination = colonies.stream()
                .filter(colony -> colony.getOwner() == owner)
                .filter(colony -> getEnemyUnitsAt(owner, colony.getLocation())
                        .isEmpty())
                .filter(colony -> colony.getLocation().getNeighbors().stream()
                        .anyMatch(reachableWater::contains))
                .min(Comparator.comparingInt(Colony::getId));
        if (destination.isEmpty()) {
            return Optional.empty();
        }
        final Location location = destination.get().getLocation();
        final Unit unit = unitStorage
                .createUnit(
                        created -> new Cargo(
                                created, UnitType.FRIGATE.getCargoCapacity()),
                        this,
                        created -> new PlaceLocation(created, location,
                                created.getDefaultOrintation()),
                        UnitType.FRIGATE, owner,
                        owner.getEffectiveSpeed(UnitType.FRIGATE),
                        new UnitActionNoAction());
        owner.revealMapForUnit(unit);
        return Optional.of(unit);
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

    private void finishShipRepair(final Ship ship,
            final PlaceShipRepair repair) {
        addShipRepairEvent(ship,
                org.microcol.model.turnevent.TurnEventShipRepair.Kind.COMPLETED,
                repair);
        if (repair.isEuropeDestination()) {
            ((AbstractUnit) ship).completeShipRepair(getEurope().getPort());
        } else {
            final Colony colony = getColonyById(repair.getDestinationColonyId())
                    .orElseThrow();
            ((AbstractUnit) ship).completeShipRepair(colony.getLocation());
        }
    }

    private void addShipRepairEvent(final Ship ship,
            final org.microcol.model.turnevent.TurnEventShipRepair.Kind kind,
            final PlaceShipRepair repair) {
        final String destinationName = repair.isEuropeDestination() ? "Europe"
                : getColonyById(repair.getDestinationColonyId())
                        .map(Colony::getName).orElse("Lost repair port");
        final List<Goods> goods = new ArrayList<>();
        int passengers = 0;
        if (kind == org.microcol.model.turnevent.TurnEventShipRepair.Kind.SUNK) {
            for (final CargoSlot slot : ship.getCargo().getSlots()) {
                if (slot.isLoadedUnit()) {
                    passengers++;
                }
                slot.getGoods().ifPresent(goods::add);
            }
        }
        getTurnEventStore()
                .add(new org.microcol.model.turnevent.TurnEventShipRepair(
                        ship.getOwner().getName(), kind, ship.getId(),
                        ship.getType(), destinationName,
                        repair.isEuropeDestination(), passengers, goods));
    }

    /**
     * Finds the closest reachable owned repair yard, or an open European port.
     */
    public Optional<ShipRepairDestination> planShipRepair(final Ship ship) {
        Preconditions.checkNotNull(ship);
        Preconditions.checkState(
                tryGetUnitById(ship.getId()).orElse(null) == ship,
                "Ship is no longer registered");
        Preconditions.checkState(ship.isAtPlaceLocation() || ship.isDamaged(),
                "Ship has no water repair origin");
        final Location origin = ship.isDamaged()
                ? ship.getRepair().get().getOrigin()
                : ship.getLocation();
        return chooseShipRepair(ship.getOwner(), origin);
    }

    private Optional<ShipRepairDestination> chooseShipRepair(final Player owner,
            final Location origin) {
        Preconditions.checkArgument(
                getMap().isValid(origin)
                        && (getMap().getTerrainTypeAt(origin).isSea() || origin
                                .getNeighbors().stream()
                                .filter(getMap()::isValid)
                                .anyMatch(next -> getMap()
                                        .getTerrainTypeAt(next).isSea())),
                "Repair origin must be sea or owned port");
        final java.util.ArrayDeque<Location> queue = new java.util.ArrayDeque<>();
        final java.util.Map<Location, Integer> distances = new java.util.HashMap<>();
        if (getMap().getTerrainTypeAt(origin).isSea()) {
            queue.add(origin);
            distances.put(origin, 0);
        } else {
            for (final Location neighbor : origin.getNeighbors()) {
                if (getMap().isValid(neighbor)
                        && getMap().getTerrainTypeAt(neighbor).isSea()
                        && owner.getEnemyUnitsAt(neighbor).isEmpty()) {
                    queue.add(neighbor);
                    distances.put(neighbor, 1);
                }
            }
        }
        ShipRepairDestination best = getColonyAt(origin).filter(
                colony -> colony.getOwner() == owner && isRepairYard(colony))
                .map(colony -> ShipRepairDestination.colony(colony, 0))
                .orElse(null);
        while (!queue.isEmpty()) {
            final Location current = queue.remove();
            for (final Location next : current.getNeighbors()) {
                if (!getMap().isValid(next)) {
                    continue;
                }
                final Optional<Colony> port = getColonyAt(next);
                if (port.isPresent()) {
                    final Colony colony = port.get();
                    if (colony.getOwner() == owner && isRepairYard(colony)) {
                        final int distance = distances.get(current) + 1;
                        final ShipRepairDestination candidate = ShipRepairDestination
                                .colony(colony, distance);
                        if (best == null
                                || candidate.getSeaDistance() < best
                                        .getSeaDistance()
                                || candidate.getSeaDistance() == best
                                        .getSeaDistance()
                                        && candidate.getColonyId() < best
                                                .getColonyId()) {
                            best = candidate;
                        }
                    }
                }
                if (getMap().getTerrainTypeAt(next).isSea()
                        && owner.getEnemyUnitsAt(next).isEmpty()
                        && !distances.containsKey(next)) {
                    distances.put(next, distances.get(current) + 1);
                    queue.add(next);
                }
            }
        }
        if (best != null) {
            return Optional.of(best);
        }
        return owner.canTradeInEurope()
                ? Optional.of(ShipRepairDestination.europe())
                : Optional.empty();
    }

    private boolean isRepairYard(final Colony colony) {
        return isOwnedCoastalPort(colony.getOwner(), colony.getLocation())
                && (colony.wasConstructionBuilded(ConstructionType.DRYDOCK)
                        || colony.wasConstructionBuilded(
                                ConstructionType.SHIPYARD));
    }

    private boolean isRepairDestinationValid(final Player owner,
            final PlaceShipRepair repair) {
        if (repair.isEuropeDestination()) {
            return owner.canTradeInEurope();
        }
        return getColonyById(repair.getDestinationColonyId())
                .filter(colony -> colony.getOwner() == owner
                        && isRepairYard(colony) && canReachRepairYard(owner,
                                repair.getOrigin(), colony.getLocation()))
                .isPresent();
    }

    private boolean canReachRepairYard(final Player owner,
            final Location origin, final Location yard) {
        if (origin.equals(yard)) {
            return true;
        }
        final java.util.ArrayDeque<Location> queue = new java.util.ArrayDeque<>();
        final java.util.Set<Location> seen = new java.util.HashSet<>();
        if (getMap().getTerrainTypeAt(origin).isSea()) {
            queue.add(origin);
            seen.add(origin);
        } else {
            for (final Location next : origin.getNeighbors()) {
                if (getMap().isValid(next)
                        && getMap().getTerrainTypeAt(next).isSea()
                        && owner.getEnemyUnitsAt(next).isEmpty()
                        && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        while (!queue.isEmpty()) {
            for (final Location next : queue.remove().getNeighbors()) {
                if (!getMap().isValid(next)) {
                    continue;
                }
                if (next.equals(yard)) {
                    return true;
                }
                if (getMap().getTerrainTypeAt(next).isSea()
                        && owner.getEnemyUnitsAt(next).isEmpty()
                        && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return false;
    }

    /** Damages a registered map ship after the caller has resolved combat. */
    public void damageShip(final Ship ship) {
        Preconditions.checkNotNull(ship);
        checkGameRunning();
        Preconditions.checkState(
                tryGetUnitById(ship.getId()).orElse(null) == ship
                        && ship.isAtPlaceLocation() && !ship.isDamaged(),
                "Ship cannot enter repair");
        final Location origin = ship.getLocation();
        Preconditions.checkArgument(
                getMap().getTerrainTypeAt(origin).isSea()
                        || isOwnedCoastalPort(ship.getOwner(), origin),
                "Damage transition requires sea or owned coastal port");
        final Optional<ShipRepairDestination> destination = chooseShipRepair(
                ship.getOwner(), origin);
        if (destination.isPresent()) {
            ((AbstractUnit) ship).beginShipRepair(
                    new PlaceShipRepair(ship, origin, destination.get()));
        } else {
            final List<Goods> goods = new ArrayList<>();
            int passengers = 0;
            for (final CargoSlot slot : ship.getCargo().getSlots()) {
                if (slot.isLoadedUnit()) {
                    passengers++;
                }
                slot.getGoods().ifPresent(goods::add);
            }
            getTurnEventStore()
                    .add(new org.microcol.model.turnevent.TurnEventShipRepair(
                            ship.getOwner().getName(),
                            org.microcol.model.turnevent.TurnEventShipRepair.Kind.SUNK,
                            ship.getId(), ship.getType(), "No repair port",
                            false, passengers, goods));
            destroyUnit(ship);
        }
    }

    /**
     * Advances only the current owner's damaged ship during turn preparation.
     */
    public void advanceShipRepair(final Ship ship) {
        Preconditions.checkState(gameManager.isPreparing()
                && getCurrentPlayer() == ship.getOwner()
                && tryGetUnitById(ship.getId()).orElse(null) == ship,
                "Repair advances only during the owner's turn preparation");
        final PlaceShipRepair repair = ship.getRepair().orElseThrow(
                () -> new IllegalStateException("Ship is not damaged"));
        if (isRepairDestinationValid(ship.getOwner(), repair)) {
            if (repair.advance()) {
                finishShipRepair(ship, repair);
            } else if (repair.getPhase() == PlaceShipRepair.Phase.REPAIR
                    && repair.getRemainingPhaseUpdates() == repair
                            .getRepairDuration()) {
                addShipRepairEvent(ship,
                        org.microcol.model.turnevent.TurnEventShipRepair.Kind.ARRIVED,
                        repair);
            }
            return;
        }
        final Optional<ShipRepairDestination> destination = chooseShipRepair(
                ship.getOwner(), repair.getOrigin());
        if (destination.isEmpty()) {
            addShipRepairEvent(ship,
                    org.microcol.model.turnevent.TurnEventShipRepair.Kind.SUNK,
                    repair);
            destroyUnit(ship);
            return;
        }
        repair.reroute(destination.get());
        addShipRepairEvent(ship,
                org.microcol.model.turnevent.TurnEventShipRepair.Kind.REROUTED,
                repair);
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

    /**
     * Whether this ship pair may fight without changing its European treaty.
     * Ordinary attack placement, movement and visibility rules still apply.
     *
     * @param attacker registered attacking unit
     * @param defender registered foreign target
     * @return whether the privateer naval exception applies
     */
    public boolean isPrivateerNavalException(final Unit attacker,
            final Unit defender) {
        return attacker != null && defender != null && attacker != defender
                && tryGetUnitById(attacker.getId()).orElse(null) == attacker
                && tryGetUnitById(defender.getId()).orElse(null) == defender
                && attacker.isShip() && defender.isShip()
                && attacker.getOwner() != defender.getOwner()
                && !attacker.getOwner().isKing()
                && !defender.getOwner().isKing() && attacker.canAttack()
                && attacker.getType().getAttackableUnitType()
                        .contains(defender.getType())
                && (attacker.getType() == UnitType.PRIVATEER
                        || defender.getType() == UnitType.PRIVATEER);
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

    /** Eligible on-map colonists adjacent to a visible rival contact. */
    public List<Unit> getPeaceVisitors(final Player actor, final Player rival) {
        return findPeaceVisitors(actor, rival, false);
    }

    private List<Unit> findPeaceVisitors(final Player actor, final Player rival,
            final boolean duringPreparation) {
        if ((!duringPreparation && !isGameRunning())
                || (duringPreparation && (!isGameStarted() || isGameFinished()))
                || actor == null || rival == null || getCurrentPlayer() != actor
                || actor.isKing() || rival.isKing() || !rival.isComputer()
                || !getPlayers().contains(actor)
                || !getPlayers().contains(rival) || actor == rival
                || europeanRelations.get(actor,
                        rival) != EuropeanRelationState.WAR) {
            return List.of();
        }
        return getAllUnits().stream()
                .filter(unit -> unit.getOwner() == actor
                        && unit instanceof UnitFreeColonist
                        && unit.isAtPlaceLocation())
                .filter(unit -> hasVisiblePeaceContact(unit, rival))
                .sorted(Comparator.comparingInt(Unit::getId))
                .collect(ImmutableList.toImmutableList());
    }

    /** Prepare new Franklin offers after an ordinary owner update. */
    void prepareFranklinPeaceOffers(final Player actor) {
        Preconditions.checkState(
                actor != null && getCurrentPlayer() == actor && isGameStarted()
                        && !isGameFinished()
                        && actor.getCongressMembers()
                                .contains(Founder.BENJAMIN_FRANKLIN),
                "Franklin peace prompt is unavailable");
        final List<PeaceOffer> newOffers = new ArrayList<>();
        for (final Player rival : getPlayers()) {
            if (rival == actor || rival.isKing() || !rival.isComputer()
                    || europeanRelations.get(actor,
                            rival) != EuropeanRelationState.WAR
                    || europeanRelations.hasPendingPeace(actor, rival)) {
                continue;
            }
            final List<Unit> visitors = findPeaceVisitors(actor, rival, true);
            if (visitors.isEmpty()) {
                continue;
            }
            final PeaceOffer offer = quotePeace(visitors.get(0), rival);
            newOffers.add(offer);
        }
        for (final PeaceOffer offer : newOffers) {
            europeanRelations.putPending(offer);
            turnEventStore.add(TurnEventProvider.getPeaceOffer(actor,
                    offer.getRival().getDisplayName(), offer.getPrice()));
        }
    }

    private boolean hasVisiblePeaceContact(final Unit visitor,
            final Player rival) {
        final Location location = visitor.getLocation();
        return getAllUnits().stream()
                .anyMatch(other -> other.getOwner() == rival
                        && other.isAtPlaceLocation()
                        && location.isNeighbor(other.getLocation())
                        && visitor.getOwner().isVisible(other.getLocation()))
                || colonies.stream()
                        .anyMatch(colony -> colony.getOwner() == rival
                                && location.isNeighbor(colony.getLocation())
                                && visitor.getOwner()
                                        .isVisible(colony.getLocation()));
    }

    private double armedLandStrength(final Player owner) {
        return getAllUnits().stream()
                .filter(unit -> unit.getOwner() == owner
                        && !unit.getType().isShip() && unit.canAttack())
                .mapToDouble(Unit::getMilitaryStrenght).sum();
    }

    /** Eligible armed or mounted visitors with visible adjacent contact. */
    public List<Unit> getTributeVisitors(final Player actor,
            final Player rival) {
        if (!isGameRunning() || actor == null || rival == null
                || getCurrentPlayer() != actor || actor.isKing()
                || rival.isKing() || !rival.isComputer()
                || !getPlayers().contains(actor)
                || !getPlayers().contains(rival) || actor == rival
                || getTributeTurnsRemaining(actor, rival) > 0) {
            return List.of();
        }
        return getAllUnits().stream()
                .filter(unit -> unit.getOwner() == actor
                        && unit instanceof UnitFreeColonist
                        && unit.isAtPlaceLocation())
                .filter(unit -> unit.getType() == UnitType.SEASONED_SCOUT
                        || ((UnitFreeColonist) unit).isHoldingGuns()
                        || ((UnitFreeColonist) unit).isMounted())
                .filter(unit -> hasVisiblePeaceContact(unit, rival))
                .sorted(Comparator.comparingInt(Unit::getId))
                .collect(ImmutableList.toImmutableList());
    }

    /** Remaining pair-level cooldown rounds, zero when eligible again. */
    public int getTributeTurnsRemaining(final Player actor,
            final Player rival) {
        checkRegisteredPlayers(actor, rival);
        Preconditions.checkArgument(
                actor != rival && !actor.isKing() && !rival.isKing(),
                "Invalid tribute participants");
        return europeanRelations.tributeTurnsRemaining(actor, rival,
                calendar.getNumberOfPlayedTurns());
    }

    private TributeDemand quoteTribute(final Unit visitor, final Player rival) {
        final Player actor = visitor.getOwner();
        final double actorStrength = armedLandStrength(actor);
        final double rivalStrength = armedLandStrength(rival);
        final boolean accepted = actorStrength >= 2d * rivalStrength;
        final int amount = accepted ? Math.min(200, rival.getGold()) : 0;
        return new TributeDemand(actor, rival, visitor.getId(), amount,
                actor.getGold(), rival.getGold(), actorStrength, rivalStrength,
                calendar.getNumberOfPlayedTurns(),
                europeanRelations.get(actor, rival), accepted);
    }

    /** Freeze one demand for later confirmation or cancellation. */
    public TributeDemand prepareTributeDemand(final Unit visitor,
            final Player rival) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(rival);
        final Player actor = visitor.getOwner();
        Preconditions.checkState(
                getTributeVisitors(actor, rival).contains(visitor),
                "Visitor has no eligible tribute contact");
        final Optional<TributeDemand> pending = europeanRelations
                .getPendingTribute(actor, rival);
        if (pending.isPresent()) {
            return pending.get();
        }
        final TributeDemand demand = quoteTribute(visitor, rival);
        Preconditions.checkState(
                (long) demand.getActorGold()
                        + demand.getAmount() <= Integer.MAX_VALUE,
                "Tribute exceeds treasury capacity");
        europeanRelations.putPendingTribute(demand);
        return demand;
    }

    public Optional<TributeDemand> getPendingTributeDemand(final Player actor,
            final Player rival) {
        if (actor == null || rival == null || actor == rival || actor.isKing()
                || rival.isKing() || !getPlayers().contains(actor)
                || !getPlayers().contains(rival)) {
            return Optional.empty();
        }
        return europeanRelations.getPendingTribute(actor, rival);
    }

    /** Discard a pending demand without starting a cooldown. */
    public void cancelTributeDemand(final TributeDemand demand) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(demand);
        Preconditions.checkState(
                isGameRunning() && getCurrentPlayer() == demand.getActor()
                        && getPendingTributeDemand(demand.getActor(),
                                demand.getRival()).orElse(null) == demand,
                "Tribute demand is obsolete");
        europeanRelations.clearPendingTribute(demand);
    }

    /** Confirm reviewed terms once; returns the amount actually received. */
    public int confirmTributeDemand(final TributeDemand demand) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(demand);
        final Player actor = demand.getActor();
        final Player rival = demand.getRival();
        Preconditions.checkState(
                isGameRunning() && getCurrentPlayer() == actor
                        && getPendingTributeDemand(actor, rival)
                                .orElse(null) == demand
                        && getTributeTurnsRemaining(actor, rival) == 0,
                "Tribute demand is obsolete");
        final Unit visitor = tryGetUnitById(demand.getVisitorId()).orElseThrow(
                () -> new IllegalStateException("Visitor no longer exists"));
        Preconditions.checkState(
                getTributeVisitors(actor, rival).contains(visitor),
                "Tribute contact was lost");
        final TributeDemand now = quoteTribute(visitor, rival);
        Preconditions.checkState(
                now.getQuotedRound() == demand.getQuotedRound()
                        && now.getRelation() == demand.getRelation()
                        && now.getActorGold() == demand.getActorGold()
                        && now.getRivalGold() == demand.getRivalGold()
                        && Double.compare(now.getActorStrength(),
                                demand.getActorStrength()) == 0
                        && Double.compare(now.getRivalStrength(),
                                demand.getRivalStrength()) == 0
                        && now.isAccepted() == demand.isAccepted()
                        && now.getAmount() == demand.getAmount(),
                "Tribute terms changed");
        Preconditions.checkState(
                (long) actor.getGold()
                        + demand.getAmount() <= Integer.MAX_VALUE,
                "Tribute exceeds treasury capacity");
        final int actorAfter = demand.getActorGoldAfter();
        final int rivalAfter = demand.getRivalGoldAfter();
        if (demand.getAmount() > 0) {
            actor.setGold(actorAfter);
            rival.setGold(rivalAfter);
        }
        europeanRelations.confirmTribute(demand,
                calendar.getNumberOfPlayedTurns());
        return demand.getAmount();
    }

    private PeaceOffer quotePeace(final Unit visitor, final Player rival) {
        final Player actor = visitor.getOwner();
        final double actorStrength = armedLandStrength(actor);
        final double rivalStrength = armedLandStrength(rival);
        final double ratio = Math.max(1d,
                rivalStrength / Math.max(1d, actorStrength));
        final double raw = Math.ceil(200d * ratio);
        Preconditions.checkState(
                Double.isFinite(raw) && raw <= Integer.MAX_VALUE,
                "Peace price exceeds supported range");
        final boolean franklin = actor.getCongressMembers()
                .contains(Founder.BENJAMIN_FRANKLIN);
        final int normal = (int) raw;
        final int price = franklin ? normal / 2 + normal % 2 : normal;
        return new PeaceOffer(actor, rival, visitor.getId(), price,
                actorStrength, rivalStrength, actor.getGold(), rival.getGold(),
                franklin);
    }

    /** Create a saved proposal, or reopen the same pair's pending proposal. */
    public PeaceOffer preparePeaceOffer(final Unit visitor,
            final Player rival) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(rival);
        final Player actor = visitor.getOwner();
        Preconditions.checkState(
                getPeaceVisitors(actor, rival).contains(visitor),
                "Visitor has no eligible peace contact");
        final Optional<PeaceOffer> pending = europeanRelations.getPending(actor,
                rival);
        if (pending.isPresent()) {
            return pending.get();
        }
        final PeaceOffer offer = quotePeace(visitor, rival);
        europeanRelations.putPending(offer);
        return offer;
    }

    public Optional<PeaceOffer> getPendingPeaceOffer(final Player actor,
            final Player rival) {
        if (actor == null || rival == null || actor == rival || actor.isKing()
                || rival.isKing() || !getPlayers().contains(actor)
                || !getPlayers().contains(rival)) {
            return Optional.empty();
        }
        return europeanRelations.getPending(actor, rival);
    }

    /** Cancel a live proposal without changing the war. */
    public void cancelPeaceOffer(final PeaceOffer offer) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(offer);
        Preconditions.checkState(
                isGameRunning() && getCurrentPlayer() == offer.getActor()
                        && getPendingPeaceOffer(offer.getActor(),
                                offer.getRival()).orElse(null) == offer,
                "Peace offer is obsolete");
        europeanRelations.clearPending(offer);
    }

    /** Revalidate every quoted input, then pay once and establish peace. */
    public void acceptPeaceOffer(final PeaceOffer offer) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(offer);
        final Player actor = offer.getActor();
        final Player rival = offer.getRival();
        Preconditions.checkState(
                isGameRunning() && getCurrentPlayer() == actor
                        && getPendingPeaceOffer(actor, rival)
                                .orElse(null) == offer
                        && europeanRelations.get(actor,
                                rival) == EuropeanRelationState.WAR,
                "Peace offer is obsolete");
        final Unit visitor = tryGetUnitById(offer.getVisitorId()).orElseThrow(
                () -> new IllegalStateException("Visitor no longer exists"));
        Preconditions.checkState(
                getPeaceVisitors(actor, rival).contains(visitor),
                "Peace contact was lost");
        final PeaceOffer now = quotePeace(visitor, rival);
        Preconditions.checkState(
                now.getPrice() == offer.getPrice()
                        && Double.compare(now.getActorStrength(),
                                offer.getActorStrength()) == 0
                        && Double.compare(now.getRivalStrength(),
                                offer.getRivalStrength()) == 0
                        && now.getActorGold() == offer.getActorGold()
                        && now.getRivalGold() == offer.getRivalGold()
                        && now.hasFranklin() == offer.hasFranklin(),
                "Peace offer terms changed");
        Preconditions.checkState(offer.getActorGoldAfter() >= 0,
                "Not enough gold for peace");
        final int rivalAfter = offer.getRivalGoldAfter();
        actor.setGold(offer.getActorGoldAfter());
        rival.setGold(rivalAfter);
        europeanRelations.transition(actor, rival, EuropeanRelationState.WAR,
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

    /** @return whether the current human owner must choose a free immigrant */
    public boolean isAwaitingImmigrationChoice() {
        return gameManager.isAwaitingImmigrationChoice();
    }

    void finishImmigrationChoice(final Player player) {
        gameManager.finishImmigrationChoice(player);
    }

    void beginImmigrationChoice(final Player player) {
        gameManager.beginImmigrationChoice(player);
    }

    void continueImmigrationChoice() {
        gameManager.continueImmigrationChoice();
    }

    void fireImmigrationChoiceRequired(final Player player) {
        listenerManager.fireImmigrationChoiceRequired(this, player);
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

    /** Whether a current armed land unit can begin fortifying. */
    public boolean canFortify(final Unit unit) {
        return (unit instanceof UnitFreeColonist
                || unit instanceof UnitArtillery) && isGameRunning()
                && !endingTurn && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.isAtPlaceLocation()
                && map.getTerrainTypeAt(unit.getLocation()).isLand()
                && (unit instanceof UnitArtillery
                        || ((UnitFreeColonist) unit).isHoldingGuns())
                && unit.getActionPoints() > 0
                && unit.getAction().getType() == UnitActionType.noAction;
    }

    /** Spend this turn's movement and record a pending fortification. */
    public void fortify(final Unit unit) {
        Preconditions.checkState(canFortify(unit), "Unit cannot fortify");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).startFortifying();
    }

    public boolean canCancelFortify(final Unit unit) {
        return unit != null && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.getAction().getType() == UnitActionType.fortify;
    }

    /** Clear either stage without refunding movement. */
    public void cancelFortify(final Unit unit) {
        Preconditions.checkState(canCancelFortify(unit),
                "Unit is not a current fortification order");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).cancelFortifying();
    }

    /** Whether this live unit's order has crossed its owner-turn boundary. */
    public boolean isFortified(final Unit unit) {
        return unit != null
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.getAction() instanceof UnitActionFortify
                && ((UnitActionFortify) unit.getAction()).isFortified();
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
        gameManager.checkSaveBoundary();
        final ModelPo out = new ModelPo();
        out.setCombatRandomState(combatRandomState);
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
        if (gameManager.isRunning()
                || gameManager.isAwaitingImmigrationChoice()) {
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
            final Unit defeated, final boolean wasDestroyed) {
        listenerManager.fireUnitAttacked(this, attacker, defender, defeated,
                wasDestroyed);
    }

    void fireUnitAttacked(final Unit attacker, final Unit defender,
            final Unit defeated, final boolean wasDestroyed,
            final MilitaryPromotion promotion) {
        listenerManager.fireUnitAttacked(this, attacker, defender, defeated,
                wasDestroyed, promotion);
    }

    void fireUnitAttacked(final Unit attacker, final Unit defender,
            final Unit defeated, final NavalCombatOutcome outcome) {
        listenerManager.fireUnitAttacked(this, attacker, defender, defeated,
                outcome);
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
