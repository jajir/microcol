package org.microcol.model;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ColonyFieldPo;
import org.microcol.model.store.GeneratedWorldPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.NativeSettlementKnowledgePo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.QueueItemType;
import org.microcol.model.store.UnitPo;
import org.microcol.model.turnevent.TurnEvent;
import org.microcol.model.turnevent.TurnEventNativeConvert;
import org.microcol.model.turnevent.TurnEventNativeTeaching;
import org.microcol.model.turnevent.TurnEventProvider;
import org.microcol.model.turnevent.TurnEventStore;
import org.microcol.model.unit.Ship;
import org.microcol.model.unit.UnitActionNoAction;
import org.microcol.model.unit.UnitActionFortify;
import org.microcol.model.unit.UnitActionBuildRoad;
import org.microcol.model.unit.UnitActionClearForest;
import org.microcol.model.unit.UnitActionPlowField;
import org.microcol.model.unit.UnitActionType;
import org.microcol.model.unit.UnitFactory;
import org.microcol.model.unit.UnitFreeColonist;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitNativeBrave;
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
    /** Last complete round already processed by the Crown-war event. */
    private int lastCrownWarRound;
    private static final int CROWN_WAR_PERIOD = 20;
    /** Settlement attacks by a Spanish attacker gain +50% (R-0174). */
    private static final int SPANISH_SETTLEMENT_ATTACK_BONUS = 50;

    /** Squares around a native settlement in which armed units add alarm. */
    private static final int NATIVE_ARMED_RADIUS = 2;

    /** Local alarm per resident within the settlement's home radius. */
    private static final double NATIVE_RESIDENT_PRESSURE = 0.25;

    /** Local alarm per armed unit within {@link #NATIVE_ARMED_RADIUS}. */
    private static final double NATIVE_ARMED_PRESSURE = 2;

    /** Local alarm per unpaid square a European uses, every owner update. */
    private static final double NATIVE_UNPAID_USE_PRESSURE = 2;

    /** Local alarm charged once when a European first takes unpaid land. */
    private static final double NATIVE_UNPAID_TAKING_ALARM = 5;

    /** Rounds after ending an alliance before war may be declared. */
    public static final int ALLIANCE_WAR_WAIT_ROUNDS = EuropeanRelations.WAR_WAIT_ROUNDS;
    /** Equipment a disbanded colonist returns, in warehouse goods order. */
    private static final List<GoodsType> DISBANDABLE_EQUIPMENT = List
            .of(GoodsType.MUSKET, GoodsType.HORSE, GoodsType.TOOLS);

    /**
     * The value of the saved random stream that follows a saved state. Pure: it
     * advances nothing, so controlled fixtures and tests can choose a state
     * whose coming draws are known.
     *
     * @param savedState saved combat random state
     * @return the next draw in [0, 1) the stream would produce
     */
    public static double combatRandomAfter(final long savedState) {
        long z = savedState + 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        z ^= z >>> 31;
        return (z >>> 11) * 0x1.0p-53;
    }

    double nextCombatRandom() {
        final double draw = combatRandomAfter(combatRandomState);
        combatRandomState += 0x9E3779B97F4A7C15L;
        return draw;
    }

    private final ListenerManager listenerManager;
    private final Calendar calendar;
    private final WorldMap map;
    private final PlayerStore playerStore;
    private final EuropeanRelations europeanRelations;
    private final List<Colony> colonies;
    private final NativeSettlements nativeSettlements;

    /** Land contacts of colonial Europeans with whole native tribes. */
    private final NativeContacts nativeContacts;
    private final NativeRelations nativeRelations;

    /** Paid-incitement orders of native tribes (R-0166). */
    private final NativeIncitements nativeIncitements;

    /** Coastal battery shots taken in the current owner's round (R-0177). */
    private final CoastalBatteryShots coastalBatteryShots;
    private final NativeLandRights nativeLandRights;
    /** Player name to the settlement ids that player has learned by sight. */
    private final Map<String, Set<Integer>> learnedNativeSettlements;
    private final UnitStorage unitStorage;
    private final Europe europe;
    private final HighSea highSea;
    private final GameManager gameManager;
    private Location focusedField;
    private boolean endingTurn;
    private final TurnEventStore turnEventStore;
    private final Statistics statistics;
    private final UnitFactory unitFactory;
    private final TradeRoutes tradeRoutes;
    private final TradeRouteRules tradeRouteRules;
    private final TradeRouteNavigator tradeRouteNavigator;
    /** Settings of a generated New World, or null for a bundled world. */
    private final GeneratedWorldPo generatedWorld;

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
        Preconditions.checkArgument(modelPo.getGeneratedWorld() == null
                || (modelPo.getGeneratedWorld().isComplete() && modelPo
                        .getGeneratedWorld().getSeed().equals(map.getSeed())),
                "Unsupported save: incomplete or mismatched generated world settings");
        generatedWorld = modelPo.getGeneratedWorld();
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
        // Saved rebellion members must name colonies that exist; a member is
        // never rebuilt from ownership (R-0192).
        for (final PlayerPo playerPo : modelPo.getGameManager().getPlayers()) {
            if (playerPo.getRebellionColonyIds() != null) {
                playerPo.getRebellionColonyIds()
                        .forEach(id -> Preconditions.checkArgument(
                                colonyIds.contains(id),
                                "Rebellion refers to an unknown colony id"));
            }
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
        final int savedCrownRound = modelPo.getCrownWarRound() == null ? 0
                : modelPo.getCrownWarRound();
        Preconditions
                .checkArgument(
                        savedCrownRound >= 0
                                && savedCrownRound <= calendar
                                        .getNumberOfPlayedTurns()
                                && savedCrownRound % CROWN_WAR_PERIOD == 0,
                        "Unsupported save: invalid Crown-war round");
        lastCrownWarRound = savedCrownRound;

        this.map = Preconditions.checkNotNull(map);

        this.playerStore = PlayerStore.makePlayers(this, modelPo);
        // Native nations have no European relation; only colonial nations and
        // Crowns form the pairs of the European table.
        europeanRelations = new EuropeanRelations(
                playerStore.getPlayers().stream()
                        .filter(player -> !player.isNative())
                        .collect(java.util.stream.Collectors.toList()),
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
            Preconditions.checkArgument(
                    colonyPo.getLiberty() != null && colonyPo.getLiberty() >= 0
                            && colonyPo.getLastColonialAffiliation() != null,
                    "Unsupported save: missing or invalid colony liberty or affiliation");
            final Player affiliation = playerStore.getPlayers().stream()
                    .filter(player -> player.getName()
                            .equals(colonyPo.getLastColonialAffiliation()))
                    .findAny().orElseThrow(() -> new IllegalArgumentException(
                            "Unsupported save: unknown colony affiliation"));
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
                    }, colonyPo.getColonyWarehouse(), buildingQueue,
                    colonyPo.getLiberty(), affiliation,
                    TownCommons.parse(colonyPo.getTownCommonsGoods()));
            col.restoreExportGoods(colonyPo.getExportGoods());
            colonies.add(col);
        });

        // Settlements are checked against the finished colony list, so a saved
        // settlement cannot take a European colony's square.
        final Set<Location> colonySites = new HashSet<>();
        colonies.forEach(colony -> colonySites.add(colony.getLocation()));
        nativeSettlements = NativeSettlements.make(
                modelPo.getNativeSettlements(),
                location -> map.isValid(location)
                        && !map.getTerrainTypeAt(location).isSea(),
                colonySites);
        learnedNativeSettlements = restoreLearnedNativeSettlements(
                modelPo.getNativeSettlementKnowledge());
        nativeContacts = NativeContacts.make(modelPo.getNativeContacts());
        nativeRelations = NativeRelations.make(nativeSettlements,
                modelPo.getNativeTribalAnger(), modelPo.getNativeLocalAlarm(),
                modelPo.getNativeArmsSales(),
                modelPo.getNativeRelationOutcomeIds());
        nativeIncitements = NativeIncitements
                .make(modelPo.getNativeIncitements());
        coastalBatteryShots = CoastalBatteryShots.make(
                modelPo.getCoastalBatteryShots(), nextColonyId,
                playerStore.getPlayers().stream().map(Player::getName)
                        .collect(Collectors.toSet()));
        nativeLandRights = NativeLandRights.make(
                location -> map.isValid(location)
                        && !map.getTerrainTypeAt(location).isSea(),
                modelPo.getNativeLandRights(), modelPo.getNativeLandTakings());

        for (final Colony colony : colonies) {
            Preconditions.checkArgument(
                    !colonyNames.getProblem(colony.getName(), colony)
                            .isPresent(),
                    "Invalid or duplicate colony name: %s", colony.getName());
            colony.setName(ColonyNames.normalize(colony.getName()));
        }

        unitFactory = new UnitFactory();
        this.unitStorage = Preconditions.checkNotNull(unitStorage);
        // Owners may share a square only as allies or as former allies with
        // saved evidence; nothing else is a lawful mixed-owner placement.
        this.unitStorage.setSharedSquarePolicy(
                (first, second, at) -> first.isColonialNation()
                        && second.isColonialNation()
                        && (europeanRelations.get(first,
                                second) == EuropeanRelationState.ALLIANCE
                                || europeanRelations.isRetainedOverlap(first,
                                        second, at)));

        gameManager = GameManager.make(this, modelPo, gameOverEvaluators,
                playerStore);
        // A human rebel that lost or won ended the game with that same reason;
        // the two saved facts must agree (R-0192, R-0191). Nothing is repaired
        // or guessed.
        for (final Player rebel : playerStore.getPlayers()) {
            if (rebel.isHuman() && rebel.getRebellionLossReason().isPresent()) {
                Preconditions.checkArgument(
                        gameManager.getGameOverResult()
                                .map(GameOverResult::getGameOverReason)
                                .equals(rebel.getRebellionLossReason()),
                        "A lost human rebellion needs its saved game over reason");
            }
            if (rebel.isHuman()
                    && rebel.getRebellionVictoryOrder().isPresent()) {
                Preconditions.checkArgument(
                        gameManager.getGameOverResult()
                                .map(GameOverResult::getGameOverReason)
                                .filter(GameOverEvaluator::isRebellionVictory)
                                .isPresent(),
                        "A won human rebellion needs its saved victory reason");
            }
        }
        // Victory orders are the completion order 1 to n, each taken once, so
        // the next victory always takes the next free order (R-0191).
        final List<Integer> orders = playerStore.getPlayers().stream()
                .map(Player::getRebellionVictoryOrder).flatMap(Optional::stream)
                .sorted().collect(Collectors.toList());
        for (int index = 0; index < orders.size(); index++) {
            Preconditions.checkArgument(orders.get(index) == index + 1,
                    "Rebellion victory orders must be 1 to n");
        }
        // The other direction: a finished game ended by a rebellion names the
        // end that one human rebel recorded itself.
        final Optional<String> rebellionEnd = gameManager.getGameOverResult()
                .map(GameOverResult::getGameOverReason)
                .filter(reason -> GameOverEvaluator.isRebellionLoss(reason)
                        || GameOverEvaluator.isRebellionVictory(reason));
        if (rebellionEnd.isPresent()) {
            Preconditions.checkArgument(
                    playerStore.getPlayers().stream()
                            .anyMatch(rebel -> rebel.isHuman()
                                    && recordsEnd(rebel, rebellionEnd.get())),
                    "A rebellion game over needs its human rebel's saved end");
        }
        // The agreement of the frozen result with the calendar, the rebellion
        // and the terminal reason is validated once, by the game manager
        // (R-0193).

        highSea = new HighSea(this);
        statistics = new Statistics(modelPo, playerStore);
        this.europe = new Europe(this, modelPo.getEurope());
        tradeRouteRules = new TradeRouteRules(this);
        tradeRoutes = new TradeRoutes(this, modelPo);
        tradeRouteNavigator = new TradeRouteNavigator(this, tradeRouteRules);
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

        model.validateSavedNativeBraveHomes();
        model.validateSavedNativeTeaching();
        model.validateSavedNativeMissions();
        model.validateSavedNativeMarkets();
        model.validateSavedNativeTributes();
        model.validateSavedRoyalBindings();
        model.validateSavedCrownSeizure();
        if (modelPo.getUnits().size() != unitStorage.getUnits().size()) {
            throw new MicroColException(String.format(
                    "In source data in %s units by in model is %s units.",
                    modelPo.getUnits().size(), unitStorage.getUnits().size()));
        }

        model.checkUnits();
        model.checkSavedGoToOrders();
        // Units exist only now; a processed visit must name a saved route.
        model.unitStorage.getUnits().forEach(
                unit -> model.tradeRoutes.checkProcessedVisit(model, unit));
        model.unitStorage.getUnits()
                .forEach(unit -> model.tradeRoutes.checkAssignment(unit));
        model.checkSavedBoardingPriorities();
        model.europeanRelations.checkRetainedOverlaps(model::isOverlapLive);
        for (final ColonyPo savedColony : modelPo.getColonies()) {
            final Colony restored = model.getColonyById(savedColony.getId())
                    .orElseThrow();
            restored.checkRestoredLiberty();
            restored.restoreEducationCourses(savedColony.getEducationCourses());
        }
        // Remembered colonies are restored verbatim first; only locations in
        // present sight are then refreshed from permitted observation.
        for (final PlayerPo savedPlayer : modelPo.getGameManager()
                .getPlayers()) {
            final Player restoredPlayer = model
                    .getPlayerByName(savedPlayer.getName());
            restoredPlayer
                    .restoreColonyKnowledge(savedPlayer.getColonySightings());
            restoredPlayer.restoreContacts(savedPlayer.getKnownPowers());
            restoredPlayer
                    .restoreMercenaryOffer(savedPlayer.getMercenaryOffer());
        }
        model.assureDefaultVisibility();
        model.refreshObservations();
        return model;
    }

    /**
     * Let every player record the colonies inside its present sight. Called
     * after colony founding, capture, removal and turn starts; colonies outside
     * present sight keep their remembered snapshot.
     */
    void refreshObservations() {
        playerStore.getPlayers().forEach(Player::observeColonies);
        refreshContacts();
        learnNativeSettlements();
    }

    /**
     * Let every colonial player remember the powers it sees right now. Called
     * after observations and after every unit step; never by a report read.
     */
    void refreshContacts() {
        playerStore.getPlayers().forEach(Player::observeContacts);
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
                                        .getTerrainAt(at).canProduce(
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
                    final Integer workerId = slot.getWorkerId();
                    if (workerId == null) {
                        continue;
                    }
                    Preconditions.checkArgument(jobWorkers.add(workerId),
                            "Saved worker has multiple colony jobs");
                    // A captured colony's building workers belong to its owner.
                    final UnitPo worker = units.get(workerId);
                    Preconditions.checkArgument(
                            worker != null
                                    && colony.getOwnerName()
                                            .equals(worker.getOwnerId())
                                    && worker.getPlaceConstructionSlot() != null
                                    && worker.getPlaceMap() == null
                                    && worker.getPlaceColonyField() == null
                                    && worker.getPlaceCargoSlot() == null
                                    && worker.getPlaceHighSeas() == null
                                    && worker.getPlaceEuropePort() == null,
                            "Invalid saved building worker identity, owner or place");
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

    /**
     * Found a colony without a native land decision, as the computer player
     * does: claimed land it uses is taken unpaid.
     */
    public Colony buildColony(final Player player, final Unit unit,
            final String name) {
        return buildColony(player, unit, name, null, List.of());
    }

    /**
     * Found a colony after the player decided about the native land it uses.
     * Every founding and land condition is checked before the first change: the
     * squares the colony would use must still be the previewed ones, and the
     * chosen purchases must be payable together. Purchases are recorded before
     * the founder starts working, so a paid square is never taken.
     *
     * @param player       required current player
     * @param unit         required founder
     * @param name         required colony name
     * @param expectedLand squares from {@link #getFoundingLand}, or
     *                     <code>null</code> to skip the preview check
     * @param purchases    required unpaid quotes to buy, possibly empty
     * @return the new colony
     */
    public Colony buildColony(final Player player, final Unit unit,
            final String name, final List<Location> expectedLand,
            final List<NativeLandQuote> purchases) {
        checkGameRunning();
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(unit);
        Preconditions.checkNotNull(purchases);
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

        final Colony col = draftColony(player, location,
                ColonyNames.normalize(name));
        final List<ColonyField> fields = col
                .getEmptyFieldsWithMaxCornProduction();
        final List<Location> land = foundingLand(location, fields);
        Preconditions.checkArgument(
                expectedLand == null || expectedLand.equals(land),
                "Native land used by the founding changed");
        final Map<NativeSettlement, Integer> treasuries = checkLandPurchases(
                player, land, purchases);
        final int goldAfter = player.getGold()
                - purchases.stream().mapToInt(NativeLandQuote::getPrice).sum();
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
        if (!purchases.isEmpty()) {
            final EuropeanNation european = nativeEuropean(player);
            purchases.forEach(quote -> nativeLandRights.addPaid(european,
                    quote.getLocation()));
            treasuries.forEach(NativeSettlement::setTreasury);
            player.setGold(goldAfter);
        }
        nextColonyId++;
        colonies.add(col);
        player.recordRebellionColony(col);
        player.activateCongressAfterFirstColony();
        if (fields.isEmpty()) {
            unit.placeToColonyStructureSlot(indoorWork);
        } else {
            unit.placeToColonyField(fields.get(0), GoodsType.CORN);
        }
        recordNativeLandUse(player, location);
        refreshObservations();
        listenerManager.fireColonyWasFounded(this, col);
        // The founding is complete, so it may now end a rebellion (R-0192).
        checkRebellionLoss();
        return col;
    }

    /**
     * Squares a founding by this unit would use now: the colony square, then
     * the field the founder would start working, if any. It uses the same field
     * choice as {@link #buildColony} and changes nothing.
     *
     * @param founder prospective founder
     * @return used squares in that order, or empty when it cannot found now
     */
    public List<Location> getFoundingLand(final Unit founder) {
        if (!canUnitBuildColony(founder)) {
            return List.of();
        }
        final Location location = founder.getLocation();
        return foundingLand(location,
                draftColony(founder.getOwner(), location, "")
                        .getEmptyFieldsWithMaxCornProduction());
    }

    /**
     * Whether the chosen purchases of founding land can be paid together now:
     * every quote is current, names a distinct square the founding would use,
     * and gold and the claimants' treasuries allow the total.
     *
     * @param founder      required founder
     * @param expectedLand squares from {@link #getFoundingLand}
     * @param purchases    required unpaid quotes to buy
     * @return whether {@link #buildColony} would accept these purchases
     */
    public boolean canPayFoundingLand(final Unit founder,
            final List<Location> expectedLand,
            final List<NativeLandQuote> purchases) {
        Preconditions.checkNotNull(purchases);
        if (founder == null || !getFoundingLand(founder).equals(expectedLand)) {
            return false;
        }
        try {
            checkLandPurchases(founder.getOwner(), expectedLand, purchases);
            return true;
        } catch (final IllegalArgumentException rejected) {
            return false;
        }
    }

    /** A colony object for checks and previews; it is not registered. */
    private Colony draftColony(final Player player, final Location location,
            final String name) {
        return new Colony(this, nextColonyId, name, player, location,
                colony -> {
                    final List<Construction> constructions = new ArrayList<>();
                    ConstructionType.NEW_COLONY_CONSTRUCTIONS
                            .forEach(constructionType -> {
                                final Construction c = Construction.build(this,
                                        colony, constructionType);
                                constructions.add(c);
                            });
                    return constructions;
                }, new HashMap<String, Integer>(), new ArrayList<>(), 0L,
                player, TownCommons.chooseSecondCommodity(
                        getMap().getTerrainAt(location)));
    }

    private static List<Location> foundingLand(final Location site,
            final List<ColonyField> fields) {
        return fields.isEmpty() ? List.of(site)
                : List.of(site, fields.get(0).getLocation());
    }

    /**
     * Validate land purchases made together with another action. Each quote
     * must be current and distinct and name one of the used squares; the total
     * must fit the payer's gold and every claimant's treasury.
     *
     * @return treasury after payment for each credited settlement
     */
    private Map<NativeSettlement, Integer> checkLandPurchases(
            final Player player, final List<Location> land,
            final List<NativeLandQuote> purchases) {
        final Set<Location> squares = new HashSet<>();
        final Map<NativeSettlement, Integer> treasuries = new HashMap<>();
        long total = 0;
        for (final NativeLandQuote quote : purchases) {
            Preconditions.checkArgument(
                    quote != null && quote.getPlayer() == player
                            && land.contains(quote.getLocation())
                            && squares.add(quote.getLocation())
                            && canBuyNativeLand(quote),
                    "Native land cannot be bought now");
            total += quote.getPrice();
            final NativeSettlement claimant = quote.getClaimant();
            final long after = (long) treasuries.getOrDefault(claimant,
                    claimant.getTreasury()) + quote.getPrice();
            Preconditions.checkArgument(after <= Integer.MAX_VALUE,
                    "Native treasury cannot take the payment");
            treasuries.put(claimant, (int) after);
        }
        Preconditions.checkArgument(total <= player.getGold(),
                "Not enough gold for the native land");
        return treasuries;
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

    /**
     * @param player required owner
     * @return the owner's saved trade routes in creation order
     */
    public List<TradeRoute> getTradeRoutes(final Player player) {
        Preconditions.checkNotNull(player);
        return tradeRoutes.getRoutes(player);
    }

    /**
     * Stops the owner may add after the given ones, from the last stop or the
     * starting colony. Reads only; nothing moves and no carrier is created.
     *
     * @param owner         required route owner
     * @param kind          required transport kind
     * @param startColonyId starting colony ID
     * @param chosen        required stops already chosen, in order
     * @return legal next stops; empty when none or the route is full
     */
    public List<TradeRouteStop> getTradeRouteStopOptions(final Player owner,
            final TradeRouteKind kind, final int startColonyId,
            final List<TradeRouteStop> chosen) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(kind);
        Preconditions.checkNotNull(chosen);
        final List<TradeRouteStop> options = new ArrayList<>();
        if (owner != getCurrentPlayer() || !owner.isColonialNation()
                || chosen.size() >= TradeRoutes.MAX_STOPS
                || !isOwnedColony(owner, startColonyId)) {
            return options;
        }
        final TradeRouteStop from = chosen.isEmpty()
                ? TradeRouteStop.ofColony(startColonyId)
                : chosen.get(chosen.size() - 1);
        for (final Colony colony : colonies) {
            final TradeRouteStop candidate = TradeRouteStop
                    .ofColony(colony.getId());
            if (colony.getOwner() == owner && !candidate.equals(from)
                    && tradeRouteRules.isLegalLeg(owner, kind, from,
                            candidate)) {
                options.add(candidate);
            }
        }
        final TradeRouteStop europe = TradeRouteStop.europe();
        if (kind == TradeRouteKind.SEA && !europe.equals(from)
                && tradeRouteRules.isLegalLeg(owner, kind, from, europe)) {
            options.add(europe);
        }
        return options;
    }

    /**
     * Checks every rule for a new route without changing anything. Only the
     * current owner may create a route, and every stop must be a live owned
     * colony or Europe, each leg reachable from the one before.
     *
     * @param owner         required route owner
     * @param name          required player-entered name, not yet trimmed
     * @param kind          required transport kind
     * @param startColonyId starting colony ID
     * @param stops         required stops in travel order
     * @return the first problem, or empty when the route may be created
     */
    public Optional<TradeRouteProblem> getTradeRouteProblem(final Player owner,
            final String name, final TradeRouteKind kind,
            final int startColonyId, final List<TradeRouteStop> stops) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(kind);
        Preconditions.checkNotNull(stops);
        if (owner != getCurrentPlayer() || !owner.isColonialNation()) {
            return Optional.of(TradeRouteProblem.NOT_OWNER);
        }
        final Optional<ColonyNameProblem> text = ColonyNames
                .getTextProblem(name);
        if (text.isPresent()) {
            switch (text.get()) {
            case EMPTY:
                return Optional.of(TradeRouteProblem.NAME_EMPTY);
            case TOO_LONG:
                return Optional.of(TradeRouteProblem.NAME_TOO_LONG);
            default:
                return Optional.of(TradeRouteProblem.NAME_INVALID);
            }
        }
        if (tradeRoutes.isNameTaken(owner, ColonyNames.normalize(name))) {
            return Optional.of(TradeRouteProblem.NAME_DUPLICATE);
        }
        if (!tradeRoutes.hasIdCapacity()) {
            return Optional.of(TradeRouteProblem.ID_EXHAUSTED);
        }
        if (!isOwnedColony(owner, startColonyId)) {
            return Optional.of(TradeRouteProblem.START_UNAVAILABLE);
        }
        final Colony start = getColonyById(startColonyId).orElseThrow();
        if (kind == TradeRouteKind.SEA
                && !isOwnedCoastalPort(owner, start.getLocation())) {
            return Optional.of(TradeRouteProblem.START_NOT_PORT);
        }
        if (stops.isEmpty() || stops.size() > TradeRoutes.MAX_STOPS) {
            return Optional.of(TradeRouteProblem.STOP_COUNT);
        }
        TradeRouteStop from = TradeRouteStop.ofColony(startColonyId);
        for (final TradeRouteStop to : stops) {
            if (to == null) {
                return Optional.of(TradeRouteProblem.STOP_UNAVAILABLE);
            }
            if (to.isEurope() && kind == TradeRouteKind.LAND) {
                return Optional.of(TradeRouteProblem.EUROPE_BY_LAND);
            }
            if (!to.isEurope() && !isOwnedColony(owner, to.getColonyId())) {
                return Optional.of(TradeRouteProblem.STOP_UNAVAILABLE);
            }
            if (to.equals(from)) {
                return Optional.of(TradeRouteProblem.SAME_STOP);
            }
            if (!tradeRouteRules.isLegalLeg(owner, kind, from, to)) {
                return Optional.of(TradeRouteProblem.STOP_NOT_REACHABLE);
            }
            from = to;
        }
        return Optional.empty();
    }

    /**
     * Creates a named route after every check of {@link #getTradeRouteProblem}
     * passes. Nothing is changed when it fails.
     *
     * @return the stored route
     * @throws IllegalArgumentException when a rule rejects the route
     */
    public TradeRoute createTradeRoute(final Player owner, final String name,
            final TradeRouteKind kind, final int startColonyId,
            final List<TradeRouteStop> stops) {
        checkGameRunning();
        checkTurnPreparationComplete();
        final Optional<TradeRouteProblem> problem = getTradeRouteProblem(owner,
                name, kind, startColonyId, stops);
        if (problem.isPresent()) {
            throw new IllegalArgumentException(
                    "Trade route cannot be created: " + problem.get());
        }
        // A new route starts with a blank cargo policy at every stop.
        return tradeRoutes.add(owner, name, kind, startColonyId,
                stops.stream()
                        .map(stop -> stop.isEurope() ? TradeRouteStop.europe()
                                : TradeRouteStop.ofColony(stop.getColonyId()))
                        .collect(Collectors.toList()));
    }

    /**
     * Sets the ordered unload and load goods of one stop of a saved route. The
     * route keeps its ID, name, owner, kind and stop order. Both lists are
     * validated before the route is replaced.
     *
     * @param owner     required route owner
     * @param route     required route as currently saved
     * @param stopIndex index of the stop to configure
     * @param unload    required goods to unload, in priority order
     * @param load      required goods to load, in priority order
     * @return the stored replacement route
     * @throws IllegalArgumentException when the route or a list is invalid
     */
    public TradeRoute configureTradeRouteStopCargo(final Player owner,
            final TradeRoute route, final int stopIndex,
            final List<GoodsType> unload, final List<GoodsType> load) {
        checkGameRunning();
        Preconditions.checkNotNull(owner, "Owner is null");
        Preconditions.checkNotNull(route, "Route is null");
        Preconditions.checkArgument(route.getOwner() == owner,
                "Trade route belongs to another player");
        Preconditions.checkArgument(
                tradeRoutes.find(route.getId())
                        .filter(stored -> stored == route).isPresent(),
                "Trade route %s is not current", route.getId());
        Preconditions.checkElementIndex(stopIndex, route.getStops().size(),
                "Trade route stop");
        return tradeRoutes.replaceStop(route, stopIndex,
                route.getStops().get(stopIndex).withCargo(unload, load));
    }

    /**
     * Processes the configured goods of one stop for a carrier that is there
     * now. Unload lists run before load lists, and each list runs top to
     * bottom. Every transfer uses the existing atomic cargo, warehouse and
     * European trade operations, so it moves only the stock, free space and
     * funds that exist at that moment, with the live quote of each hold. All
     * rules that do not depend on earlier transfers are checked before the
     * first one. The result lists every configured entry with the quantity and
     * reason; an empty source, full hold or unaffordable good is a reported
     * no-op. An entry refused part-way keeps its settled quantity in the result
     * and stops the later entries, which are reported as not attempted. The
     * carrier's visit is marked when the first quantity settles, so the same
     * visit cannot be processed again.
     *
     * @param carrier   required carrier, owned by the current player
     * @param route     required route as currently saved
     * @param stopIndex index of the stop the carrier is at
     * @return the ordered entry results, or the reason nothing was processed
     */
    public TradeRouteStopResult processTradeRouteStop(final Unit carrier,
            final TradeRoute route, final int stopIndex) {
        Preconditions.checkNotNull(carrier, "Carrier is null");
        Preconditions.checkNotNull(route, "Route is null");
        final Optional<TradeRouteStopProblem> problem = getTradeRouteStopProblem(
                carrier, route, stopIndex);
        if (problem.isPresent()) {
            return TradeRouteStopResult.refused(problem.get());
        }
        final TradeRouteStop stop = route.getStops().get(stopIndex);
        final TradeRouteVisit visit = new TradeRouteVisit(route.getId(),
                stopIndex, stop.isEurope() ? null : carrier.getLocation());
        return new TradeRouteStopProcessor(this, (UnitWithCargo) carrier, stop,
                visit).run();
    }

    /**
     * Lists the carriers of the player that stand at one stop of a route and
     * are of the route's transport kind, whether or not they may be processed
     * now. The player sees why each one is or is not ready from
     * {@link #getTradeRouteStopProblem}.
     *
     * @param owner     required route owner
     * @param route     required route
     * @param stopIndex index of the stop
     * @return carriers at the stop, ordered by unit ID; empty for a bad index
     */
    public List<UnitWithCargo> getTradeRouteCarriers(final Player owner,
            final TradeRoute route, final int stopIndex) {
        Preconditions.checkNotNull(owner, "Owner is null");
        Preconditions.checkNotNull(route, "Route is null");
        if (stopIndex < 0 || stopIndex >= route.getStops().size()
                || route.getOwner() != owner) {
            return ImmutableList.of();
        }
        final TradeRouteStop stop = route.getStops().get(stopIndex);
        return unitStorage.getUnits().stream()
                .filter(unit -> unit.getOwner() == owner
                        && unit instanceof UnitWithCargo)
                .filter(unit -> isTradeRouteCarrierKind(unit, route.getKind()))
                .filter(unit -> isCarrierAtStop(unit, stop))
                .sorted(Comparator.comparingInt(Unit::getId))
                .map(unit -> (UnitWithCargo) unit)
                .collect(ImmutableList.toImmutableList());
    }

    static boolean isTradeRouteCarrierKind(final Unit carrier,
            final TradeRouteKind kind) {
        return kind == TradeRouteKind.SEA ? carrier.getType().isShip()
                : carrier.getType() == UnitType.WAGON;
    }

    /** Live position check: the carrier is in the colony or Europe port. */
    private boolean isCarrierAtStop(final Unit carrier,
            final TradeRouteStop stop) {
        final boolean ship = carrier.getType().isShip();
        if (stop.isEurope()) {
            return ship && carrier.isAtEuropePort();
        }
        return carrier.isAtPlaceLocation() && getColonyById(stop.getColonyId())
                .filter(colony -> colony.isValid()
                        && colony.getOwner() == carrier.getOwner()
                        && colony.getLocation().equals(carrier.getLocation())
                        && (!ship || colony.getUnitsInPort().contains(carrier)))
                .isPresent();
    }

    /**
     * Live checks before any transfer: the carrier is a current owned,
     * undamaged carrier of the route's kind, the route is the stored one, the
     * carrier is at the stop and has not processed one since it left.
     */
    public Optional<TradeRouteStopProblem> getTradeRouteStopProblem(
            final Unit carrier, final TradeRoute route, final int stopIndex) {
        if (!(carrier instanceof UnitWithCargo) || !isGameRunning()
                || endingTurn || getCurrentPlayer() != carrier.getOwner()
                || unitStorage.tryGetUnitById(carrier.getId())
                        .orElse(null) != carrier
                || (carrier instanceof Ship && ((Ship) carrier).isDamaged())) {
            return Optional.of(TradeRouteStopProblem.CARRIER_UNAVAILABLE);
        }
        final boolean ship = carrier.getType().isShip();
        if (route.getKind() == TradeRouteKind.SEA ? !ship
                : carrier.getType() != UnitType.WAGON) {
            return Optional.of(TradeRouteStopProblem.CARRIER_UNAVAILABLE);
        }
        final Optional<TradeRoute> stored = tradeRoutes.find(route.getId());
        if (stored.isEmpty() || stored.get() != route
                || route.getOwner() != carrier.getOwner()) {
            return Optional.of(TradeRouteStopProblem.ROUTE_UNAVAILABLE);
        }
        if (carrier.getProcessedTradeRouteVisit().isPresent()) {
            return Optional.of(TradeRouteStopProblem.VISIT_ALREADY_PROCESSED);
        }
        if (stopIndex < 0 || stopIndex >= route.getStops().size()) {
            return Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE);
        }
        if (!isCarrierAtStop(carrier, route.getStops().get(stopIndex))) {
            return Optional.of(TradeRouteStopProblem.STOP_UNAVAILABLE);
        }
        try {
            checkTurnPreparationComplete();
        } catch (final TurnPreparationException busy) {
            return Optional.of(TradeRouteStopProblem.TURN_BUSY);
        }
        return Optional.empty();
    }

    /**
     * Saved trade route assignment of a carrier. Reading it never changes it.
     *
     * @param unit required unit
     * @return the assignment, or empty when the unit has none
     */
    public Optional<TradeRouteAssignment> getTradeRouteAssignment(
            final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        return unit instanceof AbstractUnit
                ? ((AbstractUnit) unit).getTradeRouteAssignment()
                : Optional.empty();
    }

    /**
     * The stop an assigned carrier is heading to or standing at now. A carrier
     * that has not started its leg already counts the latest saved route; one
     * on its way still heads to the stop it accepted, even if the route has
     * since been edited. Reading never changes an assignment.
     *
     * @param carrier required unit
     * @return the stop, or empty when the unit has no usable assignment
     */
    public Optional<TradeRouteStop> getTradeRouteHeadingStop(
            final Unit carrier) {
        Preconditions.checkNotNull(carrier, "Carrier is null");
        return getTradeRouteAssignment(carrier)
                .flatMap(assignment -> findTradeRoute(assignment.getRouteId())
                        .filter(route -> assignment.getStopIndex() < route
                                .getStops().size())
                        .map(route -> (TradeRouteNavigator.takesLatestPlan(
                                (AbstractUnit) carrier, assignment)
                                        ? route.getStops()
                                        : assignment.getStops())
                                .get(assignment.getStopIndex())));
    }

    /**
     * Carriers of the owner that currently follow one saved route, wherever
     * they are, so the player sees where each is heading and why it waits.
     * Reading never changes an assignment.
     *
     * @param owner required route owner
     * @param route required route
     * @return the assigned carriers ordered by unit ID
     */
    public List<UnitWithCargo> getTradeRouteAssignedCarriers(final Player owner,
            final TradeRoute route) {
        Preconditions.checkNotNull(owner, "Owner is null");
        Preconditions.checkNotNull(route, "Route is null");
        return unitStorage.getUnits().stream()
                .filter(unit -> unit.getOwner() == owner
                        && unit instanceof UnitWithCargo)
                .filter(unit -> getTradeRouteAssignment(unit).filter(
                        assignment -> assignment.getRouteId() == route.getId())
                        .isPresent())
                .sorted(Comparator.comparingInt(Unit::getId))
                .map(unit -> (UnitWithCargo) unit)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Checks every rule for assigning a carrier to a route from one of its
     * stops, without changing anything. The carrier must belong to the current
     * player and match the route's transport kind, stand on the map or in
     * Europe with no other order, and reach the first stop by the shared rules.
     *
     * @param carrier        required carrier
     * @param route          required route as currently saved
     * @param firstStopIndex index of the stop the carrier heads to first
     * @return the first problem, or empty when the assignment may be made
     */
    public Optional<TradeRouteAssignmentProblem> getTradeRouteAssignmentProblem(
            final Unit carrier, final TradeRoute route,
            final int firstStopIndex) {
        Preconditions.checkNotNull(carrier, "Carrier is null");
        Preconditions.checkNotNull(route, "Route is null");
        if (!isGameRunning() || endingTurn) {
            return Optional.of(TradeRouteAssignmentProblem.TURN_BUSY);
        }
        try {
            checkTurnPreparationComplete();
        } catch (final TurnPreparationException busy) {
            return Optional.of(TradeRouteAssignmentProblem.TURN_BUSY);
        }
        if (getCurrentPlayer() != carrier.getOwner()
                || route.getOwner() != carrier.getOwner()) {
            return Optional.of(TradeRouteAssignmentProblem.NOT_OWNER);
        }
        if (!(carrier instanceof AbstractUnit)
                || !(carrier instanceof UnitWithCargo)
                || !isTradeRouteCarrierKind(carrier, route.getKind())
                || unitStorage.tryGetUnitById(carrier.getId())
                        .orElse(null) != carrier) {
            return Optional.of(TradeRouteAssignmentProblem.NOT_CARRIER);
        }
        final Optional<TradeRoute> stored = tradeRoutes.find(route.getId());
        if (stored.isEmpty() || stored.get() != route) {
            return Optional.of(TradeRouteAssignmentProblem.ROUTE_UNAVAILABLE);
        }
        if (firstStopIndex < 0 || firstStopIndex >= route.getStops().size()) {
            return Optional.of(TradeRouteAssignmentProblem.STOP_UNAVAILABLE);
        }
        final TradeRouteStop stop = route.getStops().get(firstStopIndex);
        if (stop.isEurope() ? !carrier.getOwner().canTradeInEurope()
                : !isOwnedColony(carrier.getOwner(), stop.getColonyId())) {
            return Optional.of(TradeRouteAssignmentProblem.STOP_UNAVAILABLE);
        }
        if (hasGoToOrder(carrier) || !carrier.getAction().isNoAction()
                || !carrier.isAtPlaceLocation() && !carrier.isAtEuropePort()
                || carrier instanceof Ship && ((Ship) carrier).isDamaged()) {
            return Optional.of(TradeRouteAssignmentProblem.CARRIER_BUSY);
        }
        final Location colonyLocation = stop.isEurope() ? null
                : getColonyById(stop.getColonyId()).orElseThrow().getLocation();
        if (!tradeRouteNavigator.canReachStop((AbstractUnit) carrier, stop,
                colonyLocation)) {
            return Optional.of(TradeRouteAssignmentProblem.STOP_NOT_REACHABLE);
        }
        return Optional.empty();
    }

    /**
     * Assigns a carrier to a saved route from a chosen first stop after every
     * check of {@link #getTradeRouteAssignmentProblem} passes. The carrier does
     * not move now; its owner's later turn advances it. A rejected request
     * changes nothing.
     *
     * @param carrier        required carrier
     * @param route          required route as currently saved
     * @param firstStopIndex index of the stop the carrier heads to first
     * @throws IllegalArgumentException when a rule rejects the assignment
     */
    public void assignTradeRoute(final Unit carrier, final TradeRoute route,
            final int firstStopIndex) {
        final Optional<TradeRouteAssignmentProblem> problem = getTradeRouteAssignmentProblem(
                carrier, route, firstStopIndex);
        if (problem.isPresent()) {
            throw new IllegalArgumentException(
                    "Trade route cannot be assigned: " + problem.get());
        }
        ((AbstractUnit) carrier).startTradeRouteAssignment(
                new TradeRouteAssignment(route.getId(), firstStopIndex, null,
                        route.getStops()));
        fireActionWasStarted(carrier);
    }

    /**
     * Checks replacing the identity of one stop of a saved route: the owner is
     * the current player, the route is the stored one and each leg next to the
     * stop stays legal. The stop keeps its place and its cargo policy. A
     * carrier on its way to this stop keeps its current leg; the change reaches
     * it at its next stop. Nothing is changed.
     *
     * @param owner     required route owner
     * @param route     required route as currently saved
     * @param stopIndex index of the stop to replace
     * @param stop      required new identity; its cargo policy is ignored
     * @return the first problem, or empty when the stop may be replaced
     */
    public Optional<TradeRouteProblem> getTradeRouteStopEditProblem(
            final Player owner, final TradeRoute route, final int stopIndex,
            final TradeRouteStop stop) {
        Preconditions.checkNotNull(owner, "Owner is null");
        Preconditions.checkNotNull(route, "Route is null");
        Preconditions.checkNotNull(stop, "Stop is null");
        if (!isGameRunning() || endingTurn || !isTurnPreparationComplete()) {
            return Optional.of(TradeRouteProblem.TURN_BUSY);
        }
        if (owner != getCurrentPlayer() || route.getOwner() != owner) {
            return Optional.of(TradeRouteProblem.NOT_OWNER);
        }
        if (tradeRoutes.find(route.getId()).filter(stored -> stored == route)
                .isEmpty()) {
            return Optional.of(TradeRouteProblem.ROUTE_UNAVAILABLE);
        }
        final List<TradeRouteStop> stops = route.getStops();
        if (stopIndex < 0 || stopIndex >= stops.size()) {
            return Optional.of(TradeRouteProblem.STOP_UNAVAILABLE);
        }
        if (stop.isEurope() && route.getKind() == TradeRouteKind.LAND) {
            return Optional.of(TradeRouteProblem.EUROPE_BY_LAND);
        }
        if (!stop.isEurope() && !isOwnedColony(owner, stop.getColonyId())) {
            return Optional.of(TradeRouteProblem.STOP_UNAVAILABLE);
        }
        final TradeRouteStop from = stopIndex == 0
                ? TradeRouteStop.ofColony(route.getStartColonyId())
                : stops.get(stopIndex - 1);
        final boolean hasNext = stopIndex + 1 < stops.size();
        if (stop.equals(from)
                || hasNext && stop.equals(stops.get(stopIndex + 1))) {
            return Optional.of(TradeRouteProblem.SAME_STOP);
        }
        // A leg to or from a lost colony cannot be judged; it stays visible as
        // a lost stop that the player repairs next, so a route with several
        // lost stops can be mended one stop at a time.
        final boolean fromLost = isLostTradeRouteStop(owner, from);
        final boolean nextLost = hasNext
                && isLostTradeRouteStop(owner, stops.get(stopIndex + 1));
        if (!fromLost
                && !tradeRouteRules.isLegalLeg(owner, route.getKind(), from,
                        stop)
                || hasNext && !nextLost && !tradeRouteRules.isLegalLeg(owner,
                        route.getKind(), stop, stops.get(stopIndex + 1))) {
            return Optional.of(TradeRouteProblem.STOP_NOT_REACHABLE);
        }
        return Optional.empty();
    }

    /**
     * Replaces the identity of one stop after every check of
     * {@link #getTradeRouteStopEditProblem} passes. The route keeps its ID,
     * name, owner, kind, start and stop count; the stop keeps its cargo policy.
     *
     * @return the stored replacement route
     * @throws IllegalArgumentException when a rule rejects the edit
     */
    public TradeRoute editTradeRouteStop(final Player owner,
            final TradeRoute route, final int stopIndex,
            final TradeRouteStop stop) {
        final Optional<TradeRouteProblem> problem = getTradeRouteStopEditProblem(
                owner, route, stopIndex, stop);
        if (problem.isPresent()) {
            throw new IllegalArgumentException(
                    "Trade route stop cannot be edited: " + problem.get());
        }
        final TradeRouteStop current = route.getStops().get(stopIndex);
        final TradeRouteStop replacement = (stop.isEurope()
                ? TradeRouteStop.europe()
                : TradeRouteStop.ofColony(stop.getColonyId()))
                .withCargo(current.getUnloadGoods(), current.getLoadGoods());
        final TradeRoute stored = tradeRoutes.replaceStop(route, stopIndex,
                replacement);
        // A carrier that no longer follows the route and stands where the old
        // stop was keeps no receipt for a stop that is no longer there. A
        // following carrier keeps its receipt, so it cannot repeat the stop.
        for (final Unit unit : new ArrayList<>(owner.getAllUnits())) {
            if (unit instanceof AbstractUnit && getTradeRouteAssignment(unit)
                    .filter(assignment -> assignment.getRouteId() == route
                            .getId())
                    .isEmpty()) {
                ((AbstractUnit) unit).forgetProcessedTradeRouteVisitOf(
                        route.getId(), stopIndex);
            }
        }
        return stored;
    }

    /** Whether a colony stop names a colony the owner no longer holds. */
    private boolean isLostTradeRouteStop(final Player owner,
            final TradeRouteStop stop) {
        return !stop.isEurope() && !isOwnedColony(owner, stop.getColonyId());
    }

    /**
     * Problem with removing one carrier's trade route assignment. The carrier
     * keeps its place, cargo and any voyage; only its standing order goes.
     *
     * @param carrier required unit
     * @return the first problem, or empty when the assignment may be canceled
     */
    public Optional<TradeRouteAssignmentProblem> getTradeRouteCancelProblem(
            final Unit carrier) {
        Preconditions.checkNotNull(carrier, "Carrier is null");
        if (!isGameRunning() || endingTurn || !isTurnPreparationComplete()) {
            return Optional.of(TradeRouteAssignmentProblem.TURN_BUSY);
        }
        if (getCurrentPlayer() != carrier.getOwner()) {
            return Optional.of(TradeRouteAssignmentProblem.NOT_OWNER);
        }
        if (!(carrier instanceof AbstractUnit) || unitStorage
                .tryGetUnitById(carrier.getId()).orElse(null) != carrier) {
            return Optional.of(TradeRouteAssignmentProblem.NOT_CARRIER);
        }
        if (getTradeRouteAssignment(carrier).isEmpty()) {
            return Optional.of(TradeRouteAssignmentProblem.NO_ASSIGNMENT);
        }
        return Optional.empty();
    }

    /**
     * Returns one carrier to manual orders immediately. Other carriers of the
     * route continue; the carrier does not move and no stop is processed.
     *
     * @param carrier required carrier
     * @throws IllegalArgumentException when a rule rejects the cancellation
     */
    public void cancelTradeRouteAssignment(final Unit carrier) {
        final Optional<TradeRouteAssignmentProblem> problem = getTradeRouteCancelProblem(
                carrier);
        if (problem.isPresent()) {
            throw new IllegalArgumentException(
                    "Trade route assignment cannot be canceled: "
                            + problem.get());
        }
        ((AbstractUnit) carrier).clearTradeRouteAssignment();
        fireActionWasStarted(carrier);
    }

    /**
     * Problem with deleting a saved route. Deleting touches no carrier
     * position, cargo, treasury or voyage.
     *
     * @param owner required route owner
     * @param route required route as currently saved
     * @return the first problem, or empty when the route may be deleted
     */
    public Optional<TradeRouteProblem> getTradeRouteDeleteProblem(
            final Player owner, final TradeRoute route) {
        Preconditions.checkNotNull(owner, "Owner is null");
        Preconditions.checkNotNull(route, "Route is null");
        if (!isGameRunning() || endingTurn || !isTurnPreparationComplete()) {
            return Optional.of(TradeRouteProblem.TURN_BUSY);
        }
        if (owner != getCurrentPlayer() || route.getOwner() != owner) {
            return Optional.of(TradeRouteProblem.NOT_OWNER);
        }
        if (tradeRoutes.find(route.getId()).filter(stored -> stored == route)
                .isEmpty()) {
            return Optional.of(TradeRouteProblem.ROUTE_UNAVAILABLE);
        }
        return Optional.empty();
    }

    /**
     * Deletes a saved route. Every carrier of the owner that follows it returns
     * to manual orders where it stands, and a processed visit naming the route
     * is forgotten, so no settled transfer can be repeated for it.
     *
     * @param owner required route owner
     * @param route required route as currently saved
     * @throws IllegalArgumentException when a rule rejects the deletion
     */
    public void deleteTradeRoute(final Player owner, final TradeRoute route) {
        final Optional<TradeRouteProblem> problem = getTradeRouteDeleteProblem(
                owner, route);
        if (problem.isPresent()) {
            throw new IllegalArgumentException(
                    "Trade route cannot be deleted: " + problem.get());
        }
        for (final Unit unit : new ArrayList<>(owner.getAllUnits())) {
            if (!(unit instanceof AbstractUnit)) {
                continue;
            }
            final AbstractUnit carrier = (AbstractUnit) unit;
            carrier.forgetProcessedTradeRouteVisitOf(route.getId());
            if (getTradeRouteAssignment(carrier).filter(
                    assignment -> assignment.getRouteId() == route.getId())
                    .isPresent()) {
                carrier.clearTradeRouteAssignment();
                fireActionWasStarted(carrier);
            }
        }
        tradeRoutes.remove(route);
    }

    /** @return the stored route with this stable ID, if one exists */
    Optional<TradeRoute> findTradeRoute(final int id) {
        return tradeRoutes.find(id);
    }

    /**
     * Advances the trade route assignments of the player whose owner turn has
     * begun. Called only from the turn preparation boundary, after the go-to
     * orders, so save, load and selection never move a carrier.
     *
     * @param player required player whose turn is being prepared
     */
    void advanceTradeRouteAssignments(final Player player) {
        for (final Unit unit : new ArrayList<>(player.getAllUnits())) {
            if (unit instanceof AbstractUnit) {
                tradeRouteNavigator.advance((AbstractUnit) unit);
            }
        }
    }

    /** Cargo slots with room for this goods type, in hold order. */
    static List<CargoSlot> roomForGoods(final UnitWithCargo carrier,
            final GoodsType type) {
        return carrier.getCargo().getSlots().stream()
                .filter(slot -> slot.getAvailableCapacity() > 0)
                .filter(slot -> slot.getGoods()
                        .map(goods -> goods.getType().equals(type))
                        .orElse(true))
                .collect(Collectors.toList());
    }

    /** Cargo slots that hold this goods type, in hold order. */
    static List<CargoSlot> holdingGoods(final UnitWithCargo carrier,
            final GoodsType type) {
        return carrier.getCargo().getSlots().stream()
                .filter(slot -> slot.getGoods()
                        .map(goods -> goods.getType().equals(type))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    private boolean isOwnedColony(final Player owner, final int colonyId) {
        return getColonyById(colonyId)
                .filter(colony -> colony.getOwner() == owner).isPresent();
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
        // A brave is never a European colony founder; its nation's
        // settlements stay distinct from colonies.
        if (founder.getOwner().isNative())
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
        // A native settlement square is never a European colony site.
        if (nativeSettlements.getAt(location).isPresent())
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
        // A native convert never founds a colony (R-0149).
        if (unit instanceof UnitFreeColonist
                && ((UnitFreeColonist) unit).isNativeConvert())
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
        // An ordered unit first needs its go-to order canceled.
        if (hasGoToOrder(unit))
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
                .filter(at -> map.getTerrainAt(at).canProduce(GoodsType.CORN))
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

    /**
     * @param colonialNation a colonial player
     * @return the Crown ruling that nation, empty for any other player
     */
    public Optional<PlayerKing> getKingOf(final Player colonialNation) {
        return playerStore.getKingForPlayer(colonialNation);
    }

    /**
     * Each saved brave's home must name a settlement of the brave's own nation.
     * Unknown or cross-nation homes are rejected, never reassigned.
     */
    void validateSavedNativeBraveHomes() {
        for (final Unit unit : getAllUnits()) {
            if (!(unit instanceof UnitNativeBrave)) {
                continue;
            }
            final UnitNativeBrave brave = (UnitNativeBrave) unit;
            final NativeNation nation = ((PlayerNative) brave.getOwner())
                    .getNativeNation();
            if (brave.getHomeSettlementId().isEmpty()) {
                continue;
            }
            final int homeId = brave.getHomeSettlementId().get();
            final NativeSettlement home = nativeSettlements.getAll().stream()
                    .filter(settlement -> settlement.getId() == homeId)
                    .findAny().orElseThrow(() -> new IllegalArgumentException(
                            "Unsupported save: native brave home is unknown"));
            Preconditions.checkArgument(home.getNation() == nation,
                    "Unsupported save: native brave home belongs to another nation");
        }
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
                UnitType.MAN_OF_WAR, king,
                MovementPoints.ofPoints(UnitType.MAN_OF_WAR.getSpeed()),
                new UnitActionNoAction());
    }

    public VoyageReversalOffer offerVoyageReversal(
            final org.microcol.model.unit.Ship ship) {
        checkVoyageReversalShip(ship);
        checkReversalDestinationOpen(ship, (PlaceHighSea) ship.getPlace());
        return new VoyageReversalOffer(this, ship,
                (PlaceHighSea) ship.getPlace());
    }

    /** A ship cannot be turned toward a home Europe that is closed (R-0187). */
    private void checkReversalDestinationOpen(
            final org.microcol.model.unit.Ship ship,
            final PlaceHighSea voyage) {
        Preconditions.checkState(
                voyage.isTravelToEurope() || ship.getOwner().canTradeInEurope(),
                "Home Europe is closed to %s", ship.getOwner());
    }

    public void reverseVoyage(final VoyageReversalOffer offer) {
        checkCurrentVoyageReversal(offer);
        applyVoyageReversals(List.of(offer));
    }

    /**
     * Validate a reversal offer against the live game without any change.
     *
     * @param offer required offer the player confirmed
     */
    void checkCurrentVoyageReversal(final VoyageReversalOffer offer) {
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
        checkReversalDestinationOpen(offer.ship, voyage);
    }

    /**
     * Reverse already validated voyages by the ordinary rule. Every route turns
     * before any immediate arrival, so an arrival's listeners cannot leave a
     * later route of the same batch unreversed (R-0187 declaration).
     *
     * @param offers offers validated by checkCurrentVoyageReversal with no
     *               change in between
     */
    void applyVoyageReversals(final List<VoyageReversalOffer> offers) {
        offers.forEach(offer -> offer.voyage.reverse());
        for (final VoyageReversalOffer offer : offers) {
            if (offer.ship.getPlace() == offer.voyage
                    && offer.voyage.getRemainigTurns() == 0) {
                offer.ship.tryArriveFromHighSea();
            }
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

    /**
     * Why the carrier may not jettison goods now. A ship at sea or on an
     * Atlantic crossing may discard one goods hold; docks, Europe, land and
     * damaged ships are refused.
     *
     * @param carrier required unit
     * @return the first refused rule, or empty when some goods hold may be
     *         jettisoned
     */
    public Optional<JettisonProblem> getJettisonProblem(final Unit carrier) {
        Preconditions.checkNotNull(carrier, "Carrier is null");
        if (!isGameRunning() || endingTurn || !hasPreparedTurn()) {
            return Optional.of(JettisonProblem.WRONG_PHASE);
        }
        if (getCurrentPlayer() != carrier.getOwner()) {
            return Optional.of(JettisonProblem.NOT_OWNER);
        }
        if (!(carrier instanceof org.microcol.model.unit.Ship) || unitStorage
                .tryGetUnitById(carrier.getId()).orElse(null) != carrier) {
            return Optional.of(JettisonProblem.UNAVAILABLE);
        }
        final org.microcol.model.unit.Ship ship = (org.microcol.model.unit.Ship) carrier;
        if (ship.isDamaged() || !isAtSeaOrTransit(ship)) {
            return Optional.of(JettisonProblem.NOT_AT_SEA);
        }
        if (ship.getCargo().getSlots().stream()
                .noneMatch(slot -> slot.getGoods().isPresent())) {
            return Optional.of(JettisonProblem.NO_GOODS);
        }
        return Optional.empty();
    }

    /**
     * Offer to discard goods from one hold of an owned ship. A refusal throws
     * and changes nothing.
     *
     * @param ship      required ship
     * @param slotIndex zero-based index of the hold
     * @return the terms the player may confirm
     * @throws IllegalStateException    when the ship may not jettison, or the
     *                                  hold has no goods
     * @throws IllegalArgumentException when the hold index is outside the ship
     */
    public JettisonOffer offerJettison(final org.microcol.model.unit.Ship ship,
            final int slotIndex) {
        Preconditions.checkNotNull(ship, "Ship is null");
        final Optional<JettisonProblem> problem = getJettisonProblem(ship);
        Preconditions.checkState(problem.isEmpty(),
                "Jettison is not available: %s", problem.orElse(null));
        Preconditions.checkArgument(
                slotIndex >= 0 && slotIndex < ship.getCargo().getSlots().size(),
                "Hold index %s is outside the ship", slotIndex);
        final Goods held = ship.getCargo().getSlotByIndex(slotIndex).getGoods()
                .orElseThrow(() -> new IllegalStateException(
                        "Hold does not contain goods to jettison"));
        return new JettisonOffer(this, ship, slotIndex, held);
    }

    /**
     * Discard the chosen quantity of the offered goods after the player
     * confirmed it. Only that goods balance changes; the ship, its voyage,
     * passengers, other holds and treasury stay unchanged.
     *
     * @param offer    required confirmed terms
     * @param quantity required amount from one through the offered amount
     * @throws IllegalStateException    when the terms changed or the phase
     *                                  refuses the command
     * @throws IllegalArgumentException when the quantity is outside the offer
     */
    public void jettison(final JettisonOffer offer, final int quantity) {
        Preconditions.checkNotNull(offer, "Jettison offer is null");
        Preconditions.checkArgument(offer.model == this,
                "Jettison offer belongs to another game");
        final Optional<JettisonProblem> problem = getJettisonProblem(
                offer.ship);
        Preconditions.checkState(problem.isEmpty(),
                "Jettison is not available: %s", problem.orElse(null));
        Preconditions.checkState(offer.isCurrent(),
                "Jettison offer is no longer current");
        Preconditions.checkArgument(
                quantity >= 1 && quantity <= offer.goods.getAmount(),
                "Quantity %s is outside 1 to %s", quantity,
                offer.goods.getAmount());
        offer.ship.getCargo().getSlotByIndex(offer.slotIndex)
                .removeCargo(Goods.of(offer.goods.getType(), quantity));
    }

    /** Reads the ship's place once, so a concurrent move cannot throw. */
    private boolean isAtSeaOrTransit(final org.microcol.model.unit.Ship ship) {
        final Place place = ship.getPlace();
        if (place instanceof PlaceHighSea) {
            return true;
        }
        if (!(place instanceof PlaceLocation)) {
            return false;
        }
        final Location at = ((PlaceLocation) place).getLocation();
        return getColonyAt(at).isEmpty()
                && getMap().getTerrainTypeAt(at).isSea();
    }

    private boolean hasPreparedTurn() {
        try {
            checkTurnPreparationComplete();
            return true;
        } catch (final TurnPreparationException busy) {
            return false;
        }
    }

    public boolean isValid(final Location location) {
        return map.isValid(location);
    }

    /**
     * Whether the Crown may send this reserve man-of-war now (R-0186). It asks
     * the same rules as {@link #dispatchRoyalShip(Player, int)} and changes
     * nothing.
     *
     * @param king         required Crown
     * @param shipMemberId identity of the roster member
     * @return whether a dispatch would succeed at this moment
     */
    public boolean canDispatchRoyalShip(final Player king,
            final int shipMemberId) {
        Preconditions.checkNotNull(king, "Crown is null");
        if (!hasPreparedTurn() || !isGameRunning() || getCurrentPlayer() != king
                || !isRoyalDispatchAllowed(king)) {
            return false;
        }
        final RoyalRoster roster = getRoyalRosterOf(king);
        final boolean reserveShip = roster.findMember(shipMemberId).filter(
                member -> member.getFamily() == RoyalRoster.Family.MAN_OF_WAR
                        && member.getState() == RoyalRoster.State.RESERVE)
                .isPresent();
        if (!reserveShip) {
            return false;
        }
        final int passengers = planRoyalPassengers(king, shipMemberId).size();
        return passengers > 0 && unitStorage.canAllocateUnits(passengers + 1)
                && getHighSea().getDefaultArrivalLocation().isPresent();
    }

    /**
     * Send a reserve man-of-war from Europe with every reserve ground unit it
     * can hold, cycling infantry, cavalry and artillery. Each passenger is a
     * real unit bound to its roster member; the roster is the only source.
     * Every rule is checked before anything is created.
     *
     * @param king         required Crown, the current player
     * @param shipMemberId identity of the reserve man-of-war
     * @return the new man-of-war, already on its voyage
     * @throws IllegalStateException    when the Crown or the member may not
     *                                  sail or no ground unit is reserved
     * @throws IllegalArgumentException when the member is unknown
     */
    public UnitWithCargo dispatchRoyalShip(final Player king,
            final int shipMemberId) {
        final RoyalRoster roster = getRoyalRosterOf(king);
        checkRoyalDispatch(king);
        final List<RoyalRoster.Member> passengers = roster.planPassengers(
                UnitType.MAN_OF_WAR.getCargoCapacity(), shipMemberId);
        Preconditions.checkState(!passengers.isEmpty(),
                "No reserve ground unit can sail");
        Preconditions.checkState(
                unitStorage.canAllocateUnits(passengers.size() + 1),
                "No unit IDs remain");
        final UnitWithCargo ship = (UnitWithCargo) createCargoShipForKing(king);
        roster.ship(shipMemberId, ship.getId());
        for (final RoyalRoster.Member passenger : passengers) {
            final Unit soldier = createRoyalPassenger(king,
                    passenger.getFamily(), ship);
            roster.ship(passenger.getId(), soldier.getId());
        }
        return ship;
    }

    /**
     * Whether a surviving carrier waiting in the European port may take the
     * remaining reserve now. It asks the same rules as
     * {@link #reloadRoyalShip(Player, UnitWithCargo)} and changes nothing.
     *
     * @param king required Crown
     * @param ship required man-of-war of this Crown
     * @return whether a reload would succeed at this moment
     */
    public boolean canReloadRoyalShip(final Player king,
            final UnitWithCargo ship) {
        Preconditions.checkNotNull(king, "Crown is null");
        Preconditions.checkNotNull(ship, "Ship is null");
        return getRoyalReloadProblem(king, ship).isEmpty();
    }

    /**
     * Load the next reserve ground units into a carrier that came back to the
     * European port and send it on the ordinary Atlantic crossing. No ship is
     * created and no member is copied: the carrier and every passenger are
     * bound to their roster members. Every rule is checked before anything
     * changes.
     *
     * @param king required Crown, the current player
     * @param ship required man-of-war of this Crown in the European port
     * @throws IllegalStateException when the Crown or the carrier may not act,
     *                               or no ground unit is reserved
     */
    public void reloadRoyalShip(final Player king, final UnitWithCargo ship) {
        Preconditions.checkNotNull(ship, "Ship is null");
        final RoyalRoster roster = getRoyalRosterOf(king);
        checkRoyalDispatch(king);
        final Optional<String> problem = getRoyalReloadProblem(king, ship);
        Preconditions.checkState(problem.isEmpty(),
                "The Crown cannot reload this ship: %s", problem.orElse(null));
        final List<RoyalRoster.Member> passengers = roster
                .planPassengers(ship.getCargo().getSlots().size());
        for (final RoyalRoster.Member passenger : passengers) {
            final Unit soldier = createRoyalPassenger(king,
                    passenger.getFamily(), ship);
            roster.ship(passenger.getId(), soldier.getId());
        }
        roster.setTransportPlan(ship.getId(), Optional.empty(), 0);
        ship.placeToHighSeas(false);
    }

    /**
     * @return why the carrier cannot be reloaded now, or empty when it can
     */
    private Optional<String> getRoyalReloadProblem(final Player king,
            final UnitWithCargo ship) {
        if (!hasPreparedTurn() || !isGameRunning() || getCurrentPlayer() != king
                || !isRoyalDispatchAllowed(king)) {
            return Optional.of("The Crown may not act now");
        }
        final RoyalRoster roster = getRoyalRosterOf(king);
        if (unitStorage.tryGetUnitById(ship.getId()).orElse(null) != ship
                || ship.getOwner() != king
                || ship.getType() != UnitType.MAN_OF_WAR
                || !(ship instanceof Ship)) {
            return Optional.of("Not a live man-of-war of this Crown");
        }
        final Optional<RoyalRoster.Member> member = roster
                .findByUnitId(ship.getId());
        if (member.isEmpty()
                || member.get().getFamily() != RoyalRoster.Family.MAN_OF_WAR) {
            return Optional.of("The ship is not bound to a roster member");
        }
        if (!ship.isAtEuropePort() || ((Ship) ship).isDamaged()
                || !ship.getAction().isNoAction()
                || !ship.getCargo().isEmpty()) {
            return Optional.of("The ship is not an empty carrier in Europe");
        }
        final boolean lane = ((Ship) ship).getDepartureLocation()
                .filter(origin -> map.isValid(origin)
                        && map.getTerrainTypeAt(origin) == TerrainType.HIGH_SEA)
                .isPresent();
        if (!lane) {
            return Optional.of("The ship has no sea lane to return to");
        }
        final int passengers = roster
                .planPassengers(ship.getCargo().getSlots().size()).size();
        if (passengers == 0) {
            return Optional.of("No reserve ground unit remains");
        }
        if (!unitStorage.canAllocateUnits(passengers)) {
            return Optional.of("No unit IDs remain");
        }
        return Optional.empty();
    }

    /**
     * Save the landing target and retry count the Crown chose for its
     * man-of-war on the map (R-0186). The target must be a coastal colony, or
     * none.
     *
     * @param king         required Crown
     * @param ship         required man-of-war of this Crown on the map
     * @param colonyTarget coastal colony the Crown believes rebel, or empty
     * @param blockedTurns unsuccessful turns of the current route
     */
    public void setRoyalTransportPlan(final Player king,
            final UnitWithCargo ship, final Optional<Colony> colonyTarget,
            final int blockedTurns) {
        Preconditions.checkNotNull(ship, "Ship is null");
        final RoyalRoster roster = getRoyalRosterOf(king);
        checkGameRunning();
        checkTurnPreparationComplete();
        checkCurrentPlayer(king);
        Preconditions.checkArgument(ship.getOwner() == king,
                "Ship belongs to another Crown");
        Preconditions.checkState(ship.isAtPlaceLocation(),
                "Only a ship on the map has a transport plan");
        // The Crown chooses from what it knows, which may be out of date, so
        // the owner is not checked; the colony must exist and touch the sea.
        Preconditions.checkArgument(
                colonyTarget.isEmpty()
                        || getColonyById(colonyTarget.get().getId())
                                .orElse(null) == colonyTarget.get()
                                && hasSeaNeighbour(
                                        colonyTarget.get().getLocation()),
                "Transport target must be a coastal colony on the map");
        roster.setTransportPlan(ship.getId(), colonyTarget.map(Colony::getId),
                blockedTurns);
    }

    /**
     * @param king required Crown
     * @return roster of the Crown
     */
    public RoyalRoster getRoyalRosterOf(final Player king) {
        Preconditions.checkNotNull(king, "Crown is null");
        Preconditions.checkArgument(king instanceof PlayerKing,
                "Only a Crown has a royal roster");
        return ((PlayerKing) king).getRoyalRoster();
    }

    private boolean isRoyalDispatchAllowed(final Player king) {
        return king.isKing() && king.isComputer() && ((PlayerKing) king)
                .getWhosKingThisPlayerIs().isDeclaredIndependence();
    }

    private void checkRoyalDispatch(final Player king) {
        checkGameRunning();
        checkTurnPreparationComplete();
        checkCurrentPlayer(king);
        Preconditions.checkState(isRoyalDispatchAllowed(king),
                "Only a computer Crown of a declared nation dispatches");
    }

    private List<RoyalRoster.Member> planRoyalPassengers(final Player king,
            final int shipMemberId) {
        return getRoyalRosterOf(king).planPassengers(
                UnitType.MAN_OF_WAR.getCargoCapacity(), shipMemberId);
    }

    /** Create one royal passenger in the next free hold of the ship. */
    private Unit createRoyalPassenger(final Player king,
            final RoyalRoster.Family family, final UnitWithCargo ship) {
        final CargoSlot cargoSlot = ship.getCargo().getEmptyCargoSlot()
                .orElseThrow(() -> new IllegalStateException(
                        "No free hold for a royal passenger"));
        final UnitType type = family == RoyalRoster.Family.ARTILLERY
                ? UnitType.ARTILLERY
                : UnitType.COLONIST;
        final Unit out = unitStorage.createUnit(
                unit -> new Cargo(unit, type.getCargoCapacity()), this,
                unit -> new PlaceCargoSlot(unit, cargoSlot), type, king,
                MovementPoints.ofPoints(type.getSpeed()),
                new UnitActionNoAction());
        if (out instanceof UnitFreeColonist) {
            final UnitFreeColonist soldier = (UnitFreeColonist) out;
            soldier.setHoldingGuns(true);
            soldier.setMounted(family == RoyalRoster.Family.CAVALRY);
            soldier.assignRoyalRole();
        }
        cargoSlot.store(out.getPlaceCargoSlot());
        return out;
    }

    /**
     * Royal units of a Crown that are destroyed leave the roster as lost
     * (R-0186). Any bound unit is recorded once, and a passenger lost with its
     * ship is recorded by the same recursive removal.
     */
    private void recordRoyalLoss(final Unit unit) {
        if (unit.getOwner().isKing()) {
            ((PlayerKing) unit.getOwner()).getRoyalRoster()
                    .destroyBoundUnit(unit.getId());
        }
    }

    /**
     * A royal unit that another owner takes leaves the roster as captured, not
     * destroyed (R-0186); the unit itself lives on. It is called before the
     * owner changes.
     */
    void recordRoyalCapture(final Unit unit) {
        if (unit.getOwner().isKing()) {
            ((PlayerKing) unit.getOwner()).getRoyalRoster()
                    .captureBoundUnit(unit.getId());
        }
    }

    /**
     * Crown seizure (R-0187) is checked after every carrier binding is
     * restored. A seized unit must belong to a declared nation and stand in its
     * closed home port; a unit of a declared nation in that port must be
     * seized. A seized map unit is refused rather than repaired.
     */
    void validateSavedCrownSeizure() {
        for (final Unit unit : getAllUnits()) {
            final boolean inHomePort = ((AbstractUnit) unit).isInHomeEurope();
            final boolean declared = unit.getOwner().isDeclaredIndependence();
            Preconditions.checkArgument(
                    unit.isCrownSeized() ? inHomePort && declared
                            : !(inHomePort && declared),
                    "Saved Crown seizure of unit %s disagrees with its place",
                    unit.getId());
        }
    }

    /**
     * Saved royal bindings must name a live unit of the Crown with the family
     * of its member. Unbound Crown units, such as authored scenario troops,
     * stay outside the roster.
     */
    private void validateSavedRoyalBindings() {
        for (final Player player : getPlayers()) {
            if (!player.isKing()) {
                continue;
            }
            for (final RoyalRoster.Member member : ((PlayerKing) player)
                    .getRoyalRoster().getMembers()) {
                if (member.getUnitId().isEmpty()) {
                    continue;
                }
                final Unit unit = unitStorage
                        .tryGetUnitById(member.getUnitId().get())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Crown member names a missing unit"));
                Preconditions.checkArgument(unit.getOwner() == player,
                        "Crown member names a unit of another owner");
                Preconditions.checkArgument(
                        matchesRoyalFamily(member.getFamily(), unit),
                        "Crown member names a unit of another family");
            }
        }
    }

    private static boolean matchesRoyalFamily(final RoyalRoster.Family family,
            final Unit unit) {
        switch (family) {
        case MAN_OF_WAR:
            return unit.getType() == UnitType.MAN_OF_WAR;
        case ARTILLERY:
            return unit instanceof UnitArtillery;
        case INFANTRY:
        case CAVALRY:
            return unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit)
                            .getMilitaryRole() == MilitaryRole.ROYAL
                    && ((UnitFreeColonist) unit)
                            .isMounted() == (family == RoyalRoster.Family.CAVALRY);
        default:
            return false;
        }
    }

    private boolean hasSeaNeighbour(final Location location) {
        return location.getNeighbors().stream()
                .anyMatch(neighbour -> map.isValid(neighbour)
                        && map.getTerrainTypeAt(neighbour).isSea());
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
        // No delivery, recruitment or purchase may put a new unit into a
        // closed home port (R-0187).
        Preconditions.checkState(owner.canTradeInEurope(),
                "Home Europe is closed to %s", owner);
        return unitStorage.createUnit(
                unit -> new Cargo(unit, unitType.getCargoCapacity()), this,
                unit -> {
                    if (unitType.isShip()) {
                        return new PlaceEuropePort(unit, europe.getPort());
                    } else {
                        return new PlaceEuropePier(unit);
                    }
                }, unitType, owner,
                MovementPoints.ofPoints(owner.getEffectiveSpeed(unitType)),
                new UnitActionNoAction());
    }

    /** Deliver Jones's one-time frigate to the first reachable rebel port. */
    Optional<Unit> deliverJonesFrigateAtCoastalColony(final Player owner) {
        Preconditions.checkNotNull(owner);
        if (!unitStorage.canAllocateUnit()) {
            return Optional.empty();
        }
        final Set<Location> reachableWater = reachableSea(
                location -> getEnemyUnitsAt(owner, location).isEmpty());
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
                        MovementPoints.ofPoints(
                                owner.getEffectiveSpeed(UnitType.FRIGATE)),
                        new UnitActionNoAction());
        owner.revealMapForUnit(unit);
        return Optional.of(unit);
    }

    /**
     * Sea squares that a ship reaches from the high seas through sea squares
     * that pass the test. A square that fails the test is never entered.
     *
     * @param passable required test of a square, such as "no enemy stands here"
     * @return reachable sea squares, possibly none
     */
    private Set<Location> reachableSea(final Predicate<Location> passable) {
        final Set<Location> reachableWater = new HashSet<>();
        final ArrayDeque<Location> frontier = new ArrayDeque<>();
        for (int x = 0; x <= map.getMaxLocationX(); x++) {
            for (int y = 0; y <= map.getMaxLocationY(); y++) {
                final Location location = Location.of(x, y);
                if (map.isValid(location)
                        && map.getTerrainTypeAt(
                                location) == TerrainType.HIGH_SEA
                        && passable.test(location)
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
                        && passable.test(neighbor)
                        && reachableWater.add(neighbor)) {
                    frontier.add(neighbor);
                }
            }
        }
        return reachableWater;
    }

    /** Men-of-war of one intervention delivery (R-0190). */
    private static final int INTERVENTION_SHIPS = 2;

    /** Continental infantry of one intervention delivery (R-0190). */
    private static final int INTERVENTION_INFANTRY = 4;

    /** Continental cavalry of one intervention delivery (R-0190). */
    private static final int INTERVENTION_CAVALRY = 2;

    /**
     * Deliver the earned foreign intervention of a rebel nation (R-0190): two
     * men-of-war, four Continental infantry and two Continental cavalry, all
     * under rebel control, at the lowest-ID owned coastal colony that the high
     * seas reach. The port, its obstruction and every unit ID are checked
     * before the first unit exists. When any of them prevents the delivery,
     * nothing changes and the caller keeps the delivery pending.
     *
     * The delivered flag and the arrival notice are committed with the units,
     * in the same call, so no caller can mark the delivery separately. The call
     * does not require a completed turn preparation: it is the owner update
     * itself that delivers.
     *
     * @param owner required rebel nation of this game
     * @return the colony that received the forces, or empty while they wait
     * @throws IllegalArgumentException when the owner is not a player of this
     *                                  game or not a declared nation
     * @throws IllegalStateException    when the target is missing, not earned
     *                                  or already delivered
     */
    Optional<Colony> deliverInterventionForces(final Player owner) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkArgument(
                getPlayers().stream().anyMatch(player -> player == owner),
                "Intervention owner belongs to another game");
        Preconditions.checkArgument(
                !owner.isKing() && owner.isDeclaredIndependence(),
                "Only a declared nation receives the intervention");
        final InterventionTarget target = owner.getIntervention()
                .orElseThrow(() -> new IllegalStateException(
                        "No intervention target to deliver"));
        Preconditions.checkState(target.isEarned(),
                "The intervention is not earned");
        Preconditions.checkState(!target.isDelivered(),
                "The intervention is already delivered");
        final Optional<Colony> port = getInterventionPort(owner);
        if (port.isEmpty() || isInterventionBlocked(owner, port.get())
                || !unitStorage.canAllocateUnits(INTERVENTION_SHIPS
                        + INTERVENTION_INFANTRY + INTERVENTION_CAVALRY)) {
            return Optional.empty();
        }
        // Every rule the creation below relies on was checked above: eight free
        // IDs, a rebel owner for the Continental role and origin, and a port
        // square without enemies. New units start without an order, so arming
        // and the role cannot refuse, and the eight units are complete before
        // the flag and the notice record the delivery.
        createInterventionForces(owner, port.get().getLocation());
        target.markDelivered();
        addTurnEvent(
                TurnEventProvider.getInterventionArrival(owner, port.get()));
        return port;
    }

    /**
     * Two men-of-war, four infantry and two cavalry on the square of the port.
     * The caller checks every precondition first; this only creates.
     */
    private void createInterventionForces(final Player owner,
            final Location at) {
        for (int ship = 0; ship < INTERVENTION_SHIPS; ship++) {
            final Unit man = unitStorage
                    .createUnit(
                            unit -> new Cargo(unit,
                                    UnitType.MAN_OF_WAR.getCargoCapacity()),
                            this,
                            unit -> new PlaceLocation(unit, at,
                                    unit.getDefaultOrintation()),
                            UnitType.MAN_OF_WAR, owner,
                            MovementPoints.ofPoints(owner
                                    .getEffectiveSpeed(UnitType.MAN_OF_WAR)),
                            new UnitActionNoAction());
            owner.revealMapForUnit(man);
        }
        for (int soldier = 0; soldier < INTERVENTION_INFANTRY; soldier++) {
            createInterventionSoldier(owner, at, false);
        }
        for (int soldier = 0; soldier < INTERVENTION_CAVALRY; soldier++) {
            createInterventionSoldier(owner, at, true);
        }
    }

    /**
     * A rebel Continental, armed and placed on the square of the port; the
     * mounted ones are the cavalry. Role and origin are set after the unit
     * exists, so a Continental is always a rebel soldier.
     */
    private void createInterventionSoldier(final Player owner,
            final Location at, final boolean cavalry) {
        final Unit created = unitStorage.createUnit(
                unit -> new Cargo(unit, UnitType.COLONIST.getCargoCapacity()),
                this,
                unit -> new PlaceLocation(unit, at,
                        unit.getDefaultOrintation()),
                UnitType.COLONIST, owner,
                MovementPoints.ofPoints(UnitType.COLONIST.getSpeed()),
                new UnitActionNoAction());
        final UnitFreeColonist soldier = (UnitFreeColonist) created;
        soldier.setHoldingGuns(true);
        soldier.setMounted(cavalry);
        soldier.assignMilitaryRole(MilitaryRole.CONTINENTAL);
        soldier.markInterventionDelivered();
        owner.revealMapForUnit(created);
    }

    /**
     * The lowest-ID owned coastal colony that the high seas reach through sea
     * squares. Units standing on the water do not change this choice, so a
     * blockade never moves the forces to a higher ID (R-0190).
     */
    private Optional<Colony> getInterventionPort(final Player owner) {
        return getDeliveryPorts(owner).stream().findFirst();
    }

    /**
     * Owned coastal colonies that the high seas reach through sea squares,
     * lowest identity first. Obstruction is not part of this list; the caller
     * checks it with {@link #isInterventionBlocked(Player, Colony)} (R-0190,
     * R-0169).
     */
    private List<Colony> getDeliveryPorts(final Player owner) {
        final Set<Location> seaFromHighSeas = reachableSea(location -> true);
        return colonies.stream().filter(colony -> colony.getOwner() == owner)
                .filter(colony -> isOwnedCoastalPort(owner,
                        colony.getLocation()))
                .filter(colony -> colony.getLocation().getNeighbors().stream()
                        .anyMatch(seaFromHighSeas::contains))
                .sorted(Comparator.comparingInt(Colony::getId))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * A chosen port is obstructed when an enemy stands on it, or when no sea
     * square beside it can be reached from the high seas without passing an
     * enemy. An enemy far out on a chokepoint therefore blocks the port too,
     * while enemies that leave a route open do not.
     */
    private boolean isInterventionBlocked(final Player owner,
            final Colony port) {
        final Location at = port.getLocation();
        final Set<Location> openSea = reachableSea(
                location -> getEnemyUnitsAt(owner, location).isEmpty());
        return !getEnemyUnitsAt(owner, at).isEmpty()
                || at.getNeighbors().stream().noneMatch(openSea::contains);
    }

    /**
     * Quote the offer of the lowest-identity colonial nation that the owner has
     * contacted and that is at peace with it (R-0169). Nothing is changed. A
     * rebellion that has ended quotes nothing, and so does an empty list.
     *
     * @param owner required colonial nation that would receive the bundle
     * @return the quoted offer, or empty when no issuer is eligible
     */
    Optional<MercenaryOffer> quoteMercenaryOffer(final Player owner) {
        Preconditions.checkNotNull(owner);
        if (owner.isRebellionEnded()) {
            return Optional.empty();
        }
        return getPlayers().stream()
                .filter(issuer -> issuer != owner && issuer.isColonialNation())
                .filter(issuer -> owner.hasContactWith(issuer))
                .filter(issuer -> getEuropeanRelation(owner,
                        issuer) == EuropeanRelationState.PEACE)
                // The stable nation identity is the lowest ID: the order of
                // the saved players must not choose the issuer.
                .min(Comparator
                        .comparingInt((Player issuer) -> EuropeanNation
                                .findByPlayerName(issuer.getName())
                                .map(Enum::ordinal).orElse(Integer.MAX_VALUE))
                        .thenComparing(Player::getName))
                .map(issuer -> new MercenaryOffer(owner, issuer,
                        MercenaryRules.price(getDifficulty())));
    }

    /**
     * The offer the owner may answer now: the game runs, the owner is the
     * current player and the offer has not yet been answered or ended with its
     * turn. Reading it changes nothing.
     *
     * @param owner colonial nation, or null
     * @return the pending offer, or empty
     */
    public Optional<MercenaryOffer> getPendingMercenaryOffer(
            final Player owner) {
        if (owner == null || !getPlayers().contains(owner) || !isGameRunning()
                || getCurrentPlayer() != owner) {
            return Optional.empty();
        }
        return owner.getMercenaryOffer();
    }

    /**
     * Whether a port that the owner may choose is obstructed now (R-0190): an
     * enemy stands on it, or no sea square beside it is open from the high
     * seas. A blocked port still accepts a paid group, which then waits there.
     *
     * @param owner required colonial nation
     * @param port  required owned delivery port of that owner
     * @return whether the group would wait instead of landing now
     */
    public boolean isMercenaryPortBlocked(final Player owner,
            final Colony port) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(port);
        Preconditions.checkArgument(getDeliveryPorts(owner).contains(port),
                "Port is not an owned delivery port");
        return isInterventionBlocked(owner, port);
    }

    /**
     * Owned coastal ports that a paid mercenary group may be sent to, lowest
     * identity first. A port that is only obstructed for the moment stays
     * listed, because its paid group waits there until the route opens (R-0190,
     * R-0169).
     *
     * @param owner required colonial nation
     * @return delivery ports, possibly none
     */
    public List<Colony> getMercenaryDeliveryPorts(final Player owner) {
        Preconditions.checkNotNull(owner);
        return getDeliveryPorts(owner);
    }

    /**
     * Pay for the offered bundle and place it at the chosen port (R-0169). The
     * offer must still be the current one, the issuer and price unchanged, the
     * funds sufficient, the port an owned delivery port, and unit identities
     * available. All of this is checked before any change. An obstructed port
     * keeps the paid group pending rather than charging again later.
     *
     * @param offer required pending offer of the current player
     * @param port  required owned delivery port the owner chose
     * @return whether the soldiers were placed now; false when the route is
     *         obstructed and the paid group waits at the port
     * @throws IllegalStateException when the offer is obsolete, the issuer or
     *                               price changed, funds, port or unit IDs fail
     */
    public boolean acceptMercenaryOffer(final MercenaryOffer offer,
            final Colony port) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(offer);
        Preconditions.checkNotNull(port);
        final Player owner = offer.getOwner();
        Preconditions.checkState(
                getPendingMercenaryOffer(owner).orElse(null) == offer,
                "Mercenary offer is obsolete");
        final MercenaryOffer now = quoteMercenaryOffer(owner).orElseThrow(
                () -> new IllegalStateException("Mercenary offer is obsolete"));
        Preconditions.checkState(
                now.getIssuer() == offer.getIssuer()
                        && now.getPrice() == offer.getPrice(),
                "Mercenary issuer or price changed");
        Preconditions.checkState(owner.getGold() >= offer.getPrice(),
                "Not enough gold for mercenaries");
        Preconditions.checkState(getDeliveryPorts(owner).contains(port),
                "Port is not an owned delivery port");
        Preconditions.checkState(
                unitStorage.canAllocateUnits(MercenaryRules.SOLDIERS),
                "No unit IDs remain");
        final MercenaryDelivery group = new MercenaryDelivery(port.getId(),
                offer.getPrice(), false);
        owner.setGold(owner.getGold() - offer.getPrice());
        owner.addMercenaryDelivery(group);
        owner.clearMercenaryOffer();
        return deliverMercenaryGroup(owner, group);
    }

    /**
     * Decline the offer. Nothing is charged, and the owner may receive future
     * offers (R-0169).
     *
     * @param offer required pending offer of the current player
     */
    public void declineMercenaryOffer(final MercenaryOffer offer) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(offer);
        Preconditions
                .checkState(
                        getPendingMercenaryOffer(offer.getOwner())
                                .orElse(null) == offer,
                        "Mercenary offer is obsolete");
        offer.getOwner().clearMercenaryOffer();
    }

    /**
     * Place a paid group once (R-0169). The chosen port is used while it is
     * still an owned delivery port. A captured choice falls back to the shared
     * lowest-identity port of the intervention rule (R-0190), never to a later
     * unobstructed one. An obstructed destination or unavailable unit
     * identities leave the group pending and change nothing, so nothing is lost
     * or paid twice.
     *
     * @param owner required owner of the group
     * @param group required paid group of that owner
     * @return whether this call placed the group
     */
    boolean deliverMercenaryGroup(final Player owner,
            final MercenaryDelivery group) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(group);
        Preconditions.checkState(!group.isDelivered(),
                "The mercenary group is already delivered");
        final List<Colony> ports = getDeliveryPorts(owner);
        final Optional<Colony> port = ports.stream()
                .filter(colony -> colony.getId() == group.getPortColonyId())
                .findFirst().or(() -> getInterventionPort(owner));
        if (port.isEmpty() || isInterventionBlocked(owner, port.get())
                || !unitStorage.canAllocateUnits(MercenaryRules.SOLDIERS)) {
            return false;
        }
        createMercenarySoldiers(owner, port.get().getLocation());
        group.markDelivered();
        addTurnEvent(TurnEventProvider.getMercenaryArrival(owner, port.get()));
        return true;
    }

    /**
     * The veteran soldiers of one bundle, armed and placed on the port square.
     * The caller checks the port and the unit identities first.
     */
    private void createMercenarySoldiers(final Player owner,
            final Location at) {
        for (int soldier = 0; soldier < MercenaryRules.SOLDIERS; soldier++) {
            final Unit created = unitStorage
                    .createUnit(
                            unit -> new Cargo(unit,
                                    UnitType.VETERAN_SOLDIER
                                            .getCargoCapacity()),
                            this,
                            unit -> new PlaceLocation(unit, at,
                                    unit.getDefaultOrintation()),
                            UnitType.VETERAN_SOLDIER, owner,
                            MovementPoints.ofPoints(owner.getEffectiveSpeed(
                                    UnitType.VETERAN_SOLDIER)),
                            new UnitActionNoAction());
            ((UnitFreeColonist) created).setHoldingGuns(true);
            owner.revealMapForUnit(created);
        }
    }

    /**
     * Tell the interface that the human owner has an offer to answer (R-0169).
     */
    void fireMercenaryOfferRequired(final Player player) {
        listenerManager.fireMercenaryOfferRequired(this, player);
    }

    Unit addUnitOutSideColony(final Colony colony) {
        return unitStorage.createUnit(
                unit -> new Cargo(unit, UnitType.COLONIST.getCargoCapacity()),
                this, unit -> {
                    return new PlaceLocation(unit, colony.getLocation(),
                            unit.getDefaultOrintation());
                }, UnitType.COLONIST, colony.getOwner(),
                MovementPoints.ofPoints(UnitType.COLONIST.getSpeed()),
                new UnitActionNoAction());
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
            // A declared nation's repair still arrives; the closed port seizes
            // the ship there, so cargo is kept rather than sunk (R-0187).
            return owner.canTradeInEurope() || owner.isDeclaredIndependence();
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
     * Whether a ship in Europe could sail home to the given square. The return
     * starts at the recorded sea-lane square, or at the default arrival square
     * when the ship never left. The same known-square sea rules as trade routes
     * decide the answer, so the check reads only and changes nothing.
     *
     * @param ship required ship of its owner, not on the map
     * @param goal required map square to reach
     * @return whether the owner's known map connects the return square to goal
     */
    public boolean isEuropeReturnReachable(
            final org.microcol.model.unit.Ship ship, final Location goal) {
        Preconditions.checkNotNull(ship);
        Preconditions.checkNotNull(goal);
        return ship.getDepartureLocation()
                .or(() -> getHighSea().getDefaultArrivalLocation())
                .filter(origin -> tradeRouteRules.isReachable(ship.getOwner(),
                        TradeRouteKind.SEA, origin, goal))
                .isPresent();
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
        // Native versus European attitudes belong to later requirements; until
        // then a native pair is never a permitted hostility.
        if (first.isNative() || second.isNative()) {
            return false;
        }
        return first != second
                && (first.isKing() || second.isKing() || europeanRelations
                        .get(first, second) == EuropeanRelationState.WAR);
    }

    /**
     * Alliance query. Allies receive reciprocal passage only; this never grants
     * colony ownership, workforce, cargo access or shared sight.
     *
     * @param first  registered participant
     * @param second registered participant
     * @return whether two distinct colonizing players are allied
     */
    public boolean areAllied(final Player first, final Player second) {
        checkRegisteredPlayers(first, second);
        return first != second && first.isColonialNation()
                && second.isColonialNation() && europeanRelations.get(first,
                        second) == EuropeanRelationState.ALLIANCE;
    }

    /**
     * Whether any attack, declaration or privateer exception between these
     * players is forbidden: allies, or former allies during the war wait.
     *
     * @param first  registered participant
     * @param second registered participant
     * @return whether hostile action against each other is restrained
     */
    public boolean isAttackRestrained(final Player first, final Player second) {
        checkRegisteredPlayers(first, second);
        return first != second && first.isColonialNation()
                && second.isColonialNation()
                && (europeanRelations.get(first,
                        second) == EuropeanRelationState.ALLIANCE
                        || europeanRelations.warWaitRemaining(first, second,
                                calendar.getNumberOfPlayedTurns()) > 0);
    }

    /**
     * Rounds before war may be declared after an alliance was ended.
     *
     * @param first  registered colonizing participant
     * @param second registered colonizing participant
     * @return remaining complete rounds, zero when no wait applies
     */
    public int getWarDeclarationWait(final Player first, final Player second) {
        checkRegisteredPlayers(first, second);
        return europeanRelations.warWaitRemaining(first, second,
                calendar.getNumberOfPlayedTurns());
    }

    /** Whether units of both pair members stand on this square. */
    private boolean isOverlapLive(final Set<String> pair, final Location at) {
        final Set<String> owners = new HashSet<>();
        getUnitsAt(at).forEach(unit -> owners.add(unit.getOwner().getName()));
        return owners.containsAll(pair);
    }

    /**
     * Keep lawful overlap evidence before an alliance between the pair ends.
     */
    private void retainSharedSquares(final Player first, final Player second) {
        final List<Location> shared = new ArrayList<>();
        getUnitsAt(first).keySet().forEach(at -> {
            if (!getUnitsAt(second, at).isEmpty()) {
                shared.add(at);
            }
        });
        europeanRelations.retainOverlaps(first, second, shared);
    }

    /**
     * Whether another player's unit at the location blocks this player's
     * movement. Allied units grant passage and so never block.
     */
    boolean isPassageBlockedAt(final Player player, final Location location) {
        return getEnemyUnitsAt(player, location).stream()
                .anyMatch(unit -> !areAllied(player, unit.getOwner()));
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
                && attacker.getOwner().isColonialNation()
                && defender.getOwner().isColonialNation()
                && !isAttackRestrained(attacker.getOwner(), defender.getOwner())
                && attacker.canAttack()
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
        return findContactVisitors(actor, rival, duringPreparation,
                EuropeanRelationState.WAR);
    }

    private List<Unit> findContactVisitors(final Player actor,
            final Player rival, final boolean duringPreparation,
            final EuropeanRelationState required) {
        if ((!duringPreparation && !isGameRunning())
                || (duringPreparation && (!isGameStarted() || isGameFinished()))
                || actor == null || rival == null || getCurrentPlayer() != actor
                || !actor.isColonialNation() || !rival.isColonialNation()
                || !rival.isComputer() || !getPlayers().contains(actor)
                || !getPlayers().contains(rival) || actor == rival
                || europeanRelations.get(actor, rival) != required) {
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
            if (rival == actor || !rival.isColonialNation()
                    || !rival.isComputer()
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

    /**
     * Report military strength: ordinary base of every living armed land unit
     * the owner has, whether or not it can still move this turn. Unarmed
     * colonists and wagons contribute nothing. Tribute and peace prices keep
     * their own rule in {@link #armedLandStrength(Player)}.
     */
    private double armedLandBaseStrength(final Player owner) {
        return getAllUnits().stream()
                .filter(unit -> unit.getOwner() == owner
                        && CombatStrengths.isArmedLand(unit))
                .mapToDouble(CombatStrengths::armedLandBase).sum();
    }

    /**
     * Report naval strength: the canonical ship base of every living armed ship
     * the owner has. Armed means the ship type carries guns, so a ship under
     * repair still counts while it lives; galleons, caravels and merchantmen
     * contribute nothing, and sunk ships are no longer units.
     */
    private double armedNavalBaseStrength(final Player owner) {
        return getAllUnits().stream()
                .filter(unit -> unit.getOwner() == owner && unit.isShip()
                        && unit.getType().canAttack())
                .mapToDouble(unit -> CombatStrengths.shipBase(unit.getType()))
                .sum();
    }

    /**
     * Foreign colonial powers as the viewer may read them. The relation is
     * always listed. Actual aggregates need Jan de Witt in the viewer's
     * Congress and a current contact with the power. Reading changes no state,
     * random draw, gold or knowledge.
     *
     * @param viewer registered colonial nation reading its report
     * @return one row per other colonial power, in registry order
     */
    public List<ForeignPowerComparison> getForeignPowerComparisons(
            final Player viewer) {
        Preconditions.checkNotNull(viewer);
        Preconditions.checkArgument(
                getPlayers().contains(viewer) && viewer.isColonialNation(),
                "Viewer is not a registered colonial nation");
        final boolean deWitt = viewer.getCongressMembers()
                .contains(Founder.JAN_DE_WITT);
        return getPlayers().stream().filter(Player::isColonialNation)
                .filter(power -> power != viewer)
                .map(power -> foreignPowerComparison(viewer, power, deWitt))
                .collect(ImmutableList.toImmutableList());
    }

    private ForeignPowerComparison foreignPowerComparison(final Player viewer,
            final Player power, final boolean deWitt) {
        final EuropeanRelationState relation = getEuropeanRelation(viewer,
                power);
        if (!deWitt) {
            return ForeignPowerComparison.locked(power, relation);
        }
        if (!hasContact(viewer, power)) {
            return ForeignPowerComparison.uncontacted(power, relation);
        }
        return ForeignPowerComparison.revealed(power, relation,
                foreignFigures(power));
    }

    /**
     * Earned contact: the viewer has seen a unit or colony of the power in its
     * present sight (remembered by {@link Player#observeContacts()} when the
     * discovery happened) or knows one of its colonies. Reading it writes no
     * sighting or knowledge.
     */
    private boolean hasContact(final Player viewer, final Player power) {
        return viewer.hasContactWith(power);
    }

    /**
     * Actual aggregates of one power. Population sums its colonies; the average
     * is zero without colonies. Strengths and sales reuse the owners of those
     * rules.
     */
    private ForeignPowerComparison.Figures foreignFigures(final Player power) {
        final List<Colony> colonies = getColonies(power);
        final int population = colonies.stream().mapToInt(Colony::getPopulation)
                .sum();
        final double averageColonySize = colonies.isEmpty() ? 0
                : (double) population / colonies.size();
        return new ForeignPowerComparison.Figures(population, colonies.size(),
                averageColonySize, armedLandBaseStrength(power),
                armedNavalBaseStrength(power),
                getEurope().getRecentSaleRevenue(power.getEuropeanNation()));
    }

    /** Eligible armed or mounted visitors with visible adjacent contact. */
    public List<Unit> getTributeVisitors(final Player actor,
            final Player rival) {
        if (!isGameRunning() || actor == null || rival == null
                || getCurrentPlayer() != actor || !actor.isColonialNation()
                || !rival.isColonialNation() || !rival.isComputer()
                || !getPlayers().contains(actor)
                || !getPlayers().contains(rival) || actor == rival
                || getTributeTurnsRemaining(actor, rival) > 0
                || areAllied(actor, rival)) {
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
                actor != rival && actor.isColonialNation()
                        && rival.isColonialNation(),
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
        if (actor == null || rival == null || actor == rival
                || !actor.isColonialNation() || !rival.isColonialNation()
                || !getPlayers().contains(actor)
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
        if (actor == null || rival == null || actor == rival
                || !actor.isColonialNation() || !rival.isColonialNation()
                || !getPlayers().contains(actor)
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

    /**
     * Rounds an alliance stays unavailable because of a recent confirmed
     * tribute or actual attack/capture between the pair.
     *
     * @param actor registered colonizing player
     * @param rival registered colonizing rival
     * @return remaining complete rounds, zero when the history allows it
     */
    public int getAllianceBlockedRounds(final Player actor,
            final Player rival) {
        checkRegisteredPlayers(actor, rival);
        Preconditions.checkArgument(
                actor != rival && actor.isColonialNation()
                        && rival.isColonialNation(),
                "Invalid alliance participants");
        final int round = calendar.getNumberOfPlayedTurns();
        return Math.max(
                europeanRelations.aggressionTurnsRemaining(actor, rival, round),
                europeanRelations.tributeTurnsRemaining(actor, rival, round));
    }

    /** Eligible on-map colonists adjacent to a visible peaceful AI contact. */
    public List<Unit> getAllianceVisitors(final Player actor,
            final Player rival) {
        if (actor == null || rival == null || actor == rival
                || !actor.isColonialNation() || !rival.isColonialNation()
                || !getPlayers().contains(actor)
                || !getPlayers().contains(rival)
                || getAllianceBlockedRounds(actor, rival) > 0) {
            return List.of();
        }
        return findContactVisitors(actor, rival, false,
                EuropeanRelationState.PEACE);
    }

    private AllianceOffer quoteAlliance(final Unit visitor,
            final Player rival) {
        final Player actor = visitor.getOwner();
        return new AllianceOffer(actor, rival, visitor.getId(),
                AllianceOffer.PRICE, actor.getGold(), rival.getGold());
    }

    /** Create a saved proposal, or reopen the same pair's pending proposal. */
    public AllianceOffer prepareAllianceOffer(final Unit visitor,
            final Player rival) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(rival);
        final Player actor = visitor.getOwner();
        Preconditions.checkState(
                getAllianceVisitors(actor, rival).contains(visitor),
                "Visitor has no eligible alliance contact");
        final Optional<AllianceOffer> pending = europeanRelations
                .getPendingAlliance(actor, rival);
        if (pending.isPresent()) {
            return pending.get();
        }
        Preconditions.checkState(
                (long) rival.getGold()
                        + AllianceOffer.PRICE <= Integer.MAX_VALUE,
                "Alliance price exceeds the rival treasury capacity");
        final AllianceOffer offer = quoteAlliance(visitor, rival);
        europeanRelations.putPendingAlliance(offer);
        return offer;
    }

    public Optional<AllianceOffer> getPendingAllianceOffer(final Player actor,
            final Player rival) {
        if (actor == null || rival == null || actor == rival
                || !actor.isColonialNation() || !rival.isColonialNation()
                || !getPlayers().contains(actor)
                || !getPlayers().contains(rival)) {
            return Optional.empty();
        }
        return europeanRelations.getPendingAlliance(actor, rival);
    }

    /** Cancel a live proposal without charging or changing the relation. */
    public void cancelAllianceOffer(final AllianceOffer offer) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(offer);
        Preconditions.checkState(
                isGameRunning() && getCurrentPlayer() == offer.getActor()
                        && getPendingAllianceOffer(offer.getActor(),
                                offer.getRival()).orElse(null) == offer,
                "Alliance offer is obsolete");
        europeanRelations.clearPendingAlliance(offer);
    }

    /** Revalidate every quoted input, then pay once and form the alliance. */
    public void acceptAllianceOffer(final AllianceOffer offer) {
        checkTurnPreparationComplete();
        Preconditions.checkNotNull(offer);
        final Player actor = offer.getActor();
        final Player rival = offer.getRival();
        Preconditions.checkState(
                isGameRunning() && getCurrentPlayer() == actor
                        && getPendingAllianceOffer(actor, rival)
                                .orElse(null) == offer
                        && europeanRelations.get(actor,
                                rival) == EuropeanRelationState.PEACE,
                "Alliance offer is obsolete");
        final Unit visitor = tryGetUnitById(offer.getVisitorId()).orElseThrow(
                () -> new IllegalStateException("Visitor no longer exists"));
        Preconditions.checkState(
                getAllianceVisitors(actor, rival).contains(visitor),
                "Alliance contact or history changed");
        final AllianceOffer now = quoteAlliance(visitor, rival);
        Preconditions.checkState(
                now.getPrice() == offer.getPrice()
                        && now.getActorGold() == offer.getActorGold()
                        && now.getRivalGold() == offer.getRivalGold(),
                "Alliance offer terms changed");
        Preconditions.checkState(offer.getActorGoldAfter() >= 0,
                "Not enough gold for alliance");
        final int rivalAfter = offer.getRivalGoldAfter();
        actor.setGold(offer.getActorGoldAfter());
        rival.setGold(rivalAfter);
        europeanRelations.transition(actor, rival, EuropeanRelationState.PEACE,
                EuropeanRelationState.ALLIANCE);
    }

    /**
     * Explicitly end an alliance. The pair returns to peace and neither side
     * may declare war or attack until the wait expires.
     *
     * @param actor   current colonizing player
     * @param partner registered ally
     */
    public void terminateAlliance(final Player actor, final Player partner) {
        checkGameRunning();
        checkRegisteredPlayers(actor, partner);
        checkCurrentPlayer(actor);
        europeanRelations.terminateAlliance(actor, partner,
                calendar.getNumberOfPlayedTurns());
        retainSharedSquares(actor, partner);
    }

    /**
     * Plan the relation consequences of an actual attack or capture. Nothing
     * changes until {@link #applyAggression(AggressionPlan)}; allies of a
     * victim must enter war against the attacker, even if they were also allied
     * with it. Mere war declarations are not aggression.
     */
    AggressionPlan planAggression(final Player attacker,
            final Set<Player> victims, final boolean privateerException) {
        checkGameRunning();
        checkRegisteredPlayers(attacker, attacker);
        checkCurrentPlayer(attacker);
        final AggressionPlan plan = new AggressionPlan();
        if (!attacker.isColonialNation()) {
            return plan;
        }
        final List<Player> ordered = victims.stream()
                .sorted(Comparator.comparing(Player::getName))
                .collect(java.util.stream.Collectors.toList());
        for (final Player victim : ordered) {
            checkRegisteredPlayers(attacker, victim);
            if (!victim.isColonialNation() || victim == attacker) {
                continue;
            }
            final EuropeanRelationState state = europeanRelations.get(attacker,
                    victim);
            Preconditions.checkState(state != EuropeanRelationState.ALLIANCE,
                    "Allies cannot attack each other");
            boolean war = state == EuropeanRelationState.WAR;
            if (state == EuropeanRelationState.PEACE && !privateerException) {
                plan.addChange(attacker, victim, state);
                war = true;
            }
            plan.addAggression(attacker, victim);
            if (!war) {
                continue;
            }
            for (final Player ally : getPlayers()) {
                if (ally == attacker || ally == victim
                        || !ally.isColonialNation() || ordered.contains(ally)
                        || europeanRelations.get(ally,
                                victim) != EuropeanRelationState.ALLIANCE) {
                    continue;
                }
                final EuropeanRelationState current = europeanRelations
                        .get(ally, attacker);
                if (current != EuropeanRelationState.WAR) {
                    plan.addChange(ally, attacker, current);
                }
                plan.addDefense(ally, attacker, victim);
            }
        }
        return plan;
    }

    /** Apply a plan atomically; every planned expectation is checked first. */
    void applyAggression(final AggressionPlan plan) {
        Preconditions.checkNotNull(plan);
        Preconditions.checkState(!plan.isApplied(),
                "The aggression was already applied");
        for (final AggressionPlan.Change change : plan.getChanges()) {
            Preconditions.checkState(
                    europeanRelations.get(change.first,
                            change.second) == change.expected,
                    "The diplomatic state changed");
        }
        final int round = calendar.getNumberOfPlayedTurns();
        for (final AggressionPlan.Change change : plan.getChanges()) {
            europeanRelations.transition(change.first, change.second,
                    change.expected, EuropeanRelationState.WAR);
            if (change.expected == EuropeanRelationState.ALLIANCE) {
                retainSharedSquares(change.first, change.second);
            }
        }
        for (final Player[] pair : plan.getAggressions()) {
            europeanRelations.recordAggression(pair[0], pair[1], round);
        }
        for (final AggressionPlan.Defense defense : plan.getDefenses()) {
            if (defense.ally.isHuman()) {
                turnEventStore.add(TurnEventProvider.getAllyDefense(
                        defense.ally, defense.aggressor.getDisplayName(),
                        defense.victim.getDisplayName()));
            }
        }
        plan.markApplied();
    }

    private boolean isCrownWarEligible(final Player first,
            final Player second) {
        return first != second && first.isColonialNation()
                && second.isColonialNation() && !first.isDeclaredIndependence()
                && !second.isDeclaredIndependence()
                && !first.getCongressMembers()
                        .contains(Founder.BENJAMIN_FRANKLIN)
                && !second.getCongressMembers()
                        .contains(Founder.BENJAMIN_FRANKLIN)
                && europeanRelations.get(first,
                        second) != EuropeanRelationState.WAR;
    }

    /**
     * Crown-war event, run once per complete round boundary. Every 20th
     * complete round one random non-warring colonial pair is forced into war;
     * an alliance of that pair ends first. The round is recorded even when no
     * pair qualifies, so a saved game never repeats it.
     */
    void processCrownWar() {
        final int round = calendar.getNumberOfPlayedTurns();
        if (round <= 0 || round % CROWN_WAR_PERIOD != 0
                || round <= lastCrownWarRound) {
            return;
        }
        lastCrownWarRound = round;
        final List<Player> colonists = getPlayers().stream()
                .filter(player -> !player.isKing() && !player.isNative())
                .sorted(Comparator.comparing(Player::getName))
                .collect(java.util.stream.Collectors.toList());
        final List<Player[]> eligible = new ArrayList<>();
        for (int first = 0; first < colonists.size(); first++) {
            for (int second = first + 1; second < colonists.size(); second++) {
                if (isCrownWarEligible(colonists.get(first),
                        colonists.get(second))) {
                    eligible.add(new Player[] { colonists.get(first),
                            colonists.get(second) });
                }
            }
        }
        if (eligible.isEmpty()) {
            return;
        }
        final Player[] selected = eligible
                .get((int) (nextCombatRandom() * eligible.size()));
        Preconditions.checkState(isCrownWarEligible(selected[0], selected[1]),
                "The selected pair is no longer eligible");
        final EuropeanRelationState before = europeanRelations.get(selected[0],
                selected[1]);
        // An existing alliance is ended by the same atomic change to war.
        europeanRelations.transition(selected[0], selected[1], before,
                EuropeanRelationState.WAR);
        final boolean allianceEnded = before == EuropeanRelationState.ALLIANCE;
        if (allianceEnded) {
            retainSharedSquares(selected[0], selected[1]);
        }
        for (int index = 0; index < 2; index++) {
            if (selected[index].isHuman()) {
                turnEventStore.add(TurnEventProvider.getCrownWar(
                        selected[index], selected[1 - index].getDisplayName(),
                        allianceEnded));
            }
        }
    }

    /**
     * Completed player or AI action boundary (R-0192): after an attack, a
     * founding, a completed walk, a resident's exit or a disband. During a turn
     * preparation the same placements are steps of the update, so nothing is
     * judged then; the update's own end in {@link GameManager} judges once.
     */
    void checkRebellionLoss() {
        if (gameManager.isPreparing()) {
            return;
        }
        judgeRebellions();
    }

    /**
     * Judge the declared rebellion of every colonial nation (R-0192, R-0191).
     * Loss is judged first; victory only for a rebel without a loss at this
     * boundary. The first end of a rebel is recorded on that rebel once and is
     * never judged again. A human rebel's loss or victory also ends the game
     * once through the shared game-over result and event; a computer rebel's
     * outcome only stops or completes that nation and never ends the human's
     * game.
     */
    void judgeRebellions() {
        if (!gameManager.isOpen()) {
            return;
        }
        // Spring 1850 ends free play; a rebellion still open then is lost
        // (R-0193), after its own conditions were judged.
        final boolean freePlayEnded = calendar.isScoredPlay()
                && calendar.hasReachedYear(Calendar.YEAR_OF_FREE_PLAY_END);
        for (final Player rebel : getPlayers()) {
            if (rebel.isKing() || rebel.isNative()
                    || !rebel.isDeclaredIndependence()
                    || rebel.isRebellionDecided()) {
                continue;
            }
            Optional<GameOverResult> loss = RebellionLoss.evaluate(this, rebel);
            if (loss.isEmpty() && freePlayEnded) {
                loss = Optional.of(new GameOverResult(
                        GameOverEvaluator.REASON_REBELLION_UNRESOLVED));
            }
            if (loss.isPresent()) {
                rebel.markRebellionLost(loss.get().getGameOverReason());
                if (rebel.isHuman() && gameManager.isOpen()) {
                    gameManager.finishByRebellion(loss.get(),
                            finalScoreOf(rebel));
                }
                continue;
            }
            final Optional<GameOverResult> victory = RebellionVictory
                    .evaluate(this, rebel);
            if (victory.isPresent()) {
                rebel.markRebellionWon(countRebellionVictories() + 1);
                if (rebel.isHuman() && gameManager.isOpen()) {
                    gameManager.finishByRebellion(victory.get(),
                            finalScoreOf(rebel));
                }
            }
        }
    }

    /**
     * The final frozen score of a human rebel's end. Only free play keeps
     * scores (R-0193); a mission's rebellion end freezes nothing.
     *
     * @param rebel required human declared colonial nation whose rebellion has
     *              just ended
     * @return the final result in free play, null otherwise
     */
    private GameResult finalScoreOf(final Player rebel) {
        return calendar.isScoredPlay()
                ? GameResult.freeze(rebel, GameResult.Status.FINAL)
                : null;
    }

    /**
     * @param rebel  required declared colonial nation
     * @param reason required rebellion end reason, a loss or the victory
     * @return whether the nation itself recorded this end
     */
    private static boolean recordsEnd(final Player rebel, final String reason) {
        return GameOverEvaluator.isRebellionVictory(reason)
                ? rebel.getRebellionVictoryOrder().isPresent()
                : rebel.getRebellionLossReason().equals(Optional.of(reason));
    }

    /**
     * @return how many declared rebellions have already been won in this game,
     *         so the next victory takes the next completion order
     */
    private int countRebellionVictories() {
        return Math.toIntExact(getPlayers().stream()
                .filter(player -> player.getRebellionVictoryOrder().isPresent())
                .count());
    }

    /**
     * @return the single terminal result of a finished game, empty while the
     *         game is open; a reloaded finished game keeps the same result and
     *         never announces it again
     */
    public Optional<GameOverResult> getGameOverResult() {
        return gameManager.getGameOverResult();
    }

    /**
     * @return the frozen free-play score of the human nation (R-0193), empty
     *         while no score is frozen; it never changes once taken
     */
    public Optional<GameResult> getFrozenResult() {
        return gameManager.getFrozenResult();
    }

    /**
     * @return the frozen free-play score as a Hall of Fame entry with the
     *         difficulty and the displayed name of the nation (R-0197), empty
     *         while no score is frozen
     */
    public Optional<HallOfFameEntry> getHallOfFameEntry() {
        return getFrozenResult().map(result -> HallOfFameEntry.of(result,
                getPlayerByName(result.getPlayerName()), getDifficulty()));
    }

    /**
     * @return whether spring 1800 froze the score and the game waits for the
     *         player's unscored continuation (R-0193)
     */
    public boolean isAwaitingScoreChoice() {
        return gameManager.isAwaitingScoreChoice();
    }

    /**
     * Labor report of a colonial nation (R-0198): its living people by type,
     * each counted once, with their current places. It is rebuilt from the live
     * state on every call and changes no unit, place or resource.
     *
     * @param owner required colonial nation of this model; the report shows
     *              only its people and rejects a player of another model
     * @return immutable labor report of the current state
     */
    public LaborReport getLaborReport(final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        // Player equality is by name, so only identity proves the owner is
        // this game's own player and not a same-named one of another game.
        Preconditions.checkArgument(
                getPlayers().stream().anyMatch(player -> player == owner),
                "Owner belongs to another model");
        return LaborReport.calculate(this, owner);
    }

    /**
     * Religion report of a colonial nation (R-0199): the cross output of each
     * colony at the next update, the national total, the accumulated crosses
     * and the next immigrant threshold. It is rebuilt from the live state on
     * every call and changes no colony, cross, recruitment or unit.
     *
     * @param owner required colonial nation of this model; the report reads
     *              only its own colonies and rejects a player of another model
     * @return immutable religion report of the current state
     */
    public ReligionReport getReligionReport(final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        // Same-named players of other games compare equal, so only identity
        // proves the owner belongs to this game.
        Preconditions.checkArgument(
                getPlayers().stream().anyMatch(player -> player == owner),
                "Owner belongs to another model");
        return ReligionReport.calculate(owner);
    }

    /**
     * Colony advisor report of a colonial nation (R-0200): each owned colony
     * with its residents, goods of the next update, expected new colonists or
     * famine and active project. It is rebuilt from the live state on every
     * call and changes no colony, good, project or unit.
     *
     * @param owner required colonial nation of this model; the report reads
     *              only its own colonies and rejects a player of another model
     * @return immutable colony advisor report of the current state
     */
    public ColonyAdvisorReport getColonyAdvisorReport(final Player owner) {
        Preconditions.checkNotNull(owner, "Owner is required");
        // Same-named players of other games compare equal, so only identity
        // proves the owner belongs to this game.
        Preconditions.checkArgument(
                getPlayers().stream().anyMatch(player -> player == owner),
                "Owner belongs to another model");
        return ColonyAdvisorReport.calculate(owner);
    }

    /**
     * Continue past spring 1800 without scoring (R-0193). The frozen score
     * stays as it was taken, and the next round starts once.
     *
     * @param player required human nation that owns the frozen score
     * @throws IllegalStateException when the score is not awaiting continuation
     *                               or the nation is not the scored one
     */
    public synchronized void continueWithoutScoring(final Player player) {
        Preconditions.checkState(!endingTurn,
                "Cannot continue while ending a turn.");
        endingTurn = true;
        try {
            gameManager.continueWithoutScoring(player);
        } finally {
            endingTurn = false;
        }
    }

    /**
     * Read the retirement that the human nation may confirm now (R-0194). The
     * read changes nothing; canceling the preview needs no model call.
     *
     * @return the preview of the score that a confirmation would record, empty
     *         when no retirement is offered
     */
    public Optional<RetirementQuote> getRetirementQuote() {
        return gameManager.retirementQuote();
    }

    /**
     * Confirm a previewed retirement (R-0194). A preview that no longer matches
     * the live state, or a repeated confirmation after the game ended, is
     * rejected before any change.
     *
     * @param quote required preview that the player confirmed
     * @throws IllegalStateException when no retirement is offered or the
     *                               preview is stale
     */
    public synchronized void retire(final RetirementQuote quote) {
        Preconditions.checkNotNull(quote, "Retirement preview is required");
        gameManager.retire(quote);
    }

    void fireScoreBoundaryRequired(final Player player) {
        listenerManager.fireScoreBoundaryRequired(this, player);
    }

    private void changeEuropeanRelation(final Player actor, final Player target,
            final EuropeanRelationState expected,
            final EuropeanRelationState next) {
        checkGameRunning();
        checkRegisteredPlayers(actor, target);
        checkCurrentPlayer(actor);
        Preconditions.checkState(
                next != EuropeanRelationState.WAR
                        || europeanRelations.warWaitRemaining(actor, target,
                                calendar.getNumberOfPlayedTurns()) == 0,
                "War cannot be declared while the alliance wait lasts");
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

    /**
     * @return whether the current human nation must answer a Crown tax demand
     *         before any action
     */
    public boolean isAwaitingTaxDecision() {
        return gameManager.isAwaitingTaxDecision();
    }

    void beginTaxChoice(final Player player) {
        gameManager.beginTaxChoice(player);
    }

    void finishTaxChoice(final Player player) {
        gameManager.finishTaxChoice(player);
    }

    void fireTaxDemandRequired(final Player player) {
        listenerManager.fireTaxDemandRequired(this, player);
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

    /**
     * Whether the unit has a standing order: a go-to order or a trade route
     * assignment. Orders that need an idle unit refuse such a unit, so the UI
     * never offers an operation that then throws.
     */
    boolean hasGoToOrder(final Unit unit) {
        return goToOrderOf(unit).isPresent() || unit instanceof AbstractUnit
                && ((AbstractUnit) unit).hasStandingOrder();
    }

    private boolean canChangeSentry(final Unit unit,
            final UnitActionType from) {
        return unit != null && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && !unit.isCrownSeized()
                && (unit.isAtPlaceLocation()
                        || unit.isAtEuropePier() && unit.isStorable())
                && unit.getAction().getType() == from && !hasGoToOrder(unit);
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

    /**
     * Current player's live immigrant waiting on the European pier. Foreign,
     * stale, moved or ordinary units are refused before any change.
     */
    private boolean canChangeBoarding(final Unit unit) {
        return unit != null && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && !unit.isCrownSeized() && unit.isAtEuropePier()
                && unit.isImmigrant();
    }

    public boolean canOptOutOfBoarding(final Unit unit) {
        return canChangeBoarding(unit) && unit.isWillingToBoard();
    }

    /**
     * Withdraw a willing immigrant from the departing ship. The person stays on
     * the pier and also loses any front-of-queue choice.
     *
     * @param unit required waiting immigrant of the current player
     */
    public void optOutOfBoarding(final Unit unit) {
        Preconditions.checkState(canOptOutOfBoarding(unit),
                "Immigrant cannot opt out of boarding");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).setBoardingOptOut(true);
    }

    public boolean canRestoreBoarding(final Unit unit) {
        return canChangeBoarding(unit) && unit.isBoardingOptedOut();
    }

    public void restoreBoarding(final Unit unit) {
        Preconditions.checkState(canRestoreBoarding(unit),
                "Immigrant is not opted out of boarding");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).setBoardingOptOut(false);
    }

    public boolean canPrioritizeBoarding(final Unit unit) {
        return canChangeBoarding(unit) && unit.isWillingToBoard()
                && !unit.isBoardingPriority();
    }

    /**
     * Make a willing immigrant the owner's front-of-queue choice. It replaces
     * any earlier choice of the same owner.
     *
     * @param unit required willing waiting immigrant of the current player
     */
    public void prioritizeBoarding(final Unit unit) {
        Preconditions.checkState(canPrioritizeBoarding(unit),
                "Immigrant cannot lead boarding");
        checkTurnPreparationComplete();
        unit.getOwner().getAllUnits().forEach(
                owned -> ((AbstractUnit) owned).setBoardingPriority(false));
        ((AbstractUnit) unit).setBoardingPriority(true);
    }

    /**
     * Authoritative missionary admission for a live colonist of the current
     * owner. It is free, uses no movement and changes only the missionary role.
     * Accepted places are the owner's European pier and an owned colony tile
     * that has a church or cathedral. Workers in fields or buildings, cargo,
     * sea, foreign or stale units and colonies are refused before any change.
     *
     * @param unit the unit the player or the UI wants to ordain
     * @return the reason ordination is refused, or empty when it may proceed
     */
    public Optional<OrdinationProblem> getOrdinationProblem(final Unit unit) {
        if (!(unit instanceof UnitFreeColonist) || !isGameRunning()
                || endingTurn || getCurrentPlayer() != unit.getOwner()
                || unitStorage.tryGetUnitById(unit.getId())
                        .orElse(null) != unit) {
            return Optional.of(OrdinationProblem.UNAVAILABLE);
        }
        final UnitFreeColonist colonist = (UnitFreeColonist) unit;
        if (colonist.isMissionary()) {
            return Optional.of(OrdinationProblem.ALREADY_MISSIONARY);
        }
        if (colonist.isAtPlaceColonyField()
                || colonist.isAtPlaceConstructionSlot()) {
            return Optional.of(OrdinationProblem.ASSIGNED_WORKER);
        }
        if (colonist.getAction().getType() != UnitActionType.noAction
                || hasGoToOrder(colonist)) {
            return Optional.of(OrdinationProblem.ACTIVE_ORDER);
        }
        if (colonist.isCrownSeized()) {
            return Optional.of(OrdinationProblem.UNAVAILABLE);
        }
        if (colonist.isAtEuropePier()) {
            return Optional.empty();
        }
        if (!colonist.isAtPlaceLocation()) {
            return Optional.of(OrdinationProblem.NOT_IN_EUROPE_OR_COLONY);
        }
        final Optional<Colony> colony = getColonyAt(colonist.getLocation());
        if (colony.isEmpty()) {
            return Optional.of(OrdinationProblem.NOT_IN_EUROPE_OR_COLONY);
        }
        if (!colony.get().isValid()
                || colony.get().getOwner() != colonist.getOwner()) {
            return Optional.of(OrdinationProblem.UNAVAILABLE);
        }
        if (!colony.get().wasConstructionBuilded(ConstructionType.CHURCH)
                && !colony.get()
                        .wasConstructionBuilded(ConstructionType.CATHEDRAL)) {
            return Optional.of(OrdinationProblem.NO_CHURCH);
        }
        return Optional.empty();
    }

    /**
     * Ordain a colonist after the same validation as
     * {@link #getOrdinationProblem}.
     *
     * @throws IllegalStateException when the live state refuses ordination; the
     *                               unit and its state stay unchanged
     */
    public void ordainMissionary(final Unit unit) {
        final Optional<OrdinationProblem> problem = getOrdinationProblem(unit);
        Preconditions.checkState(problem.isEmpty(),
                "Unit cannot be ordained: %s", problem.orElse(null));
        checkTurnPreparationComplete();
        ((UnitFreeColonist) unit).becomeMissionary();
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
                && unit.getMovementThirds() > 0
                && unit.getAction().getType() == UnitActionType.noAction
                && !hasGoToOrder(unit);
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

    /**
     * A live land unit of the current player that stands on the map, is idle
     * and has no go-to order yet may take one.
     */
    private boolean isGoToCandidate(final Unit unit) {
        return unit instanceof AbstractUnit && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.isAtPlaceLocation() && unit.getAction().isNoAction()
                && !hasGoToOrder(unit);
    }

    private static Optional<GoToOrder> goToOrderOf(final Unit unit) {
        return unit instanceof AbstractUnit
                ? ((AbstractUnit) unit).getGoToOrder()
                : Optional.empty();
    }

    /**
     * Whether the unit may take a go-to order to this map square. The square
     * may lie beyond this turn's movement; the route uses the shared step
     * costs.
     *
     * @param unit        required unit
     * @param destination required map square
     * @return {@code true} when a route exists and the order can be issued
     */
    public boolean canIssueGoTo(final Unit unit, final Location destination) {
        // The route reads the unit's place once and is empty for its own
        // square, so a unit that has just left the map gives no route.
        return destination != null && isGoToCandidate(unit)
                && getMap().isValid(destination)
                && ((AbstractUnit) unit).findGoToRoute(destination).isPresent();
    }

    /**
     * Give the unit a go-to order to a map square and advance it along the
     * route as far as its movement allows in this turn. A rejected request
     * changes nothing.
     *
     * @param unit        required unit
     * @param destination required map square
     */
    public void issueGoTo(final Unit unit, final Location destination) {
        Preconditions.checkState(canIssueGoTo(unit, destination),
                "Unit %s cannot go to %s", unit, destination);
        checkTurnPreparationComplete();
        startGoToOrder(unit,
                new GoToOrder(
                        destination, null, String.format("%d, %d",
                                destination.getX(), destination.getY()),
                        false));
    }

    /**
     * Named friendly colonies that the unit may take a go-to order to now.
     *
     * @param unit required unit
     * @return reachable colonies of the unit's owner, empty when no order is
     *         possible
     */
    public List<Colony> getGoToColonies(final Unit unit) {
        if (!isGoToCandidate(unit)) {
            return List.of();
        }
        return getColonies(unit.getOwner()).stream()
                .filter(colony -> canIssueGoTo(unit, colony.getLocation()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Give the unit a go-to order to a named friendly colony. The colony must
     * still be the live colony that the model offers now, so a stale choice is
     * rejected before anything changes.
     *
     * @param unit   required unit
     * @param colony required colony chosen from {@link #getGoToColonies}
     */
    public void issueGoToColony(final Unit unit, final Colony colony) {
        Preconditions.checkNotNull(colony, "Colony is null");
        Preconditions.checkState(getGoToColonies(unit).contains(colony),
                "Colony %s is no longer a go-to destination of unit %s", colony,
                unit);
        checkTurnPreparationComplete();
        startGoToOrder(unit, new GoToOrder(colony.getLocation(), colony.getId(),
                colony.getName(), false));
    }

    private void startGoToOrder(final Unit unit, final GoToOrder order) {
        ((AbstractUnit) unit).startGoTo(order);
        // The order is visible at once; the walk then reports its own steps.
        fireActionWasStarted(unit);
        ((AbstractUnit) unit).advanceGoTo();
    }

    /**
     * Rejects, before the model is used, a saved go-to order whose named colony
     * exists but is not at the saved destination. A colony that is gone or lost
     * is not an error here: the order cancels at its owner's next turn.
     */
    /**
     * A saved front-of-queue choice must be unique for its owner. Saved
     * boarding flags are otherwise checked per unit by the factory.
     */
    private void checkSavedBoardingPriorities() {
        unitStorage.getUnits().stream().filter(Unit::isBoardingPriority)
                .collect(Collectors.groupingBy(Unit::getOwner,
                        Collectors.counting()))
                .values()
                .forEach(count -> Preconditions.checkArgument(count == 1,
                        "Saved boarding priority is not unique"));
    }

    private void checkSavedGoToOrders() {
        for (final Unit unit : unitStorage.getUnits()) {
            goToOrderOf(unit).filter(order -> order.getColonyId() != null)
                    .ifPresent(order -> getColonyById(order.getColonyId())
                            .ifPresent(colony -> Preconditions.checkArgument(
                                    colony.getLocation()
                                            .equals(order.getDestination()),
                                    "Saved go-to colony %s is not at its destination",
                                    colony.getId())));
        }
    }

    /**
     * Destination of the unit's go-to order. Reading it never changes the
     * order.
     *
     * @param unit required unit
     * @return destination square, or empty when the unit has no order
     */
    public Optional<Location> getGoToDestination(final Unit unit) {
        return goToOrderOf(unit).map(GoToOrder::getDestination);
    }

    /**
     * Name of the unit's go-to destination for display.
     *
     * @param unit required unit
     * @return colony name or square coordinates, or empty without an order
     */
    public Optional<String> getGoToDestinationName(final Unit unit) {
        return goToOrderOf(unit).map(GoToOrder::getDestinationName);
    }

    /**
     * @param unit required unit
     * @return whether the unit's go-to order is paused for lack of a route
     */
    public boolean isGoToPaused(final Unit unit) {
        return goToOrderOf(unit).map(GoToOrder::isPaused).orElse(false);
    }

    /**
     * @param unit required unit
     * @return whether the unit may be sent somewhere by a new go-to order; the
     *         destination itself is checked by {@link #canIssueGoTo}
     */
    public boolean canStartGoTo(final Unit unit) {
        return isGoToCandidate(unit);
    }

    public boolean canCancelGoTo(final Unit unit) {
        return unit instanceof AbstractUnit && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && goToOrderOf(unit).isPresent();
    }

    /**
     * Clear the go-to order without moving the unit or restoring its spent
     * movement. Selecting or inspecting a unit never does this.
     *
     * @param unit required unit
     */
    public void cancelGoTo(final Unit unit) {
        Preconditions.checkState(canCancelGoTo(unit),
                "Unit %s has no current go-to order", unit);
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).cancelGoTo();
        fireActionWasStarted(unit);
    }

    /**
     * Authoritative plow admission for the current player's live pioneer.
     *
     * @param unit required unit
     * @return the first reason plowing cannot start, or empty when it can
     */
    public Optional<PlowProblem> getPlowProblem(final Unit unit) {
        if (unit == null || !isGameRunning() || endingTurn
                || getCurrentPlayer() != unit.getOwner()
                || unitStorage.tryGetUnitById(unit.getId()).orElse(null) != unit
                || !unit.isAtPlaceLocation()) {
            return Optional.of(PlowProblem.UNAVAILABLE);
        }
        if (!(unit instanceof UnitFreeColonist)
                || !unit.getType().isCanPlowField()) {
            return Optional.of(PlowProblem.NOT_PIONEER);
        }
        if (((UnitFreeColonist) unit)
                .getTools() < UnitActionPlowField.REQUIRED_TOOLS) {
            return Optional.of(PlowProblem.NOT_ENOUGH_TOOLS);
        }
        if (unit.getAction().getType() != UnitActionType.noAction
                || hasGoToOrder(unit)) {
            return Optional.of(PlowProblem.BUSY);
        }
        if (unit.getMovementThirds() <= 0) {
            return Optional.of(PlowProblem.NO_MOVES);
        }
        final Location at = unit.getLocation();
        if (getColonyAt(at).isPresent()) {
            return Optional.of(PlowProblem.COLONY);
        }
        final Terrain terrain = map.getTerrainAt(at);
        if (!terrain.getTerrainType().isCanHaveField()) {
            return Optional.of(PlowProblem.UNSUITABLE_TERRAIN);
        }
        if (terrain.isHasTrees()) {
            return Optional.of(PlowProblem.FOREST);
        }
        if (terrain.isHasField()) {
            return Optional.of(PlowProblem.ALREADY_PLOWED);
        }
        return Optional.empty();
    }

    /** Whether a current pioneer may start plowing where it stands. */
    public boolean canPlowField(final Unit unit) {
        return getPlowProblem(unit).isEmpty();
    }

    /** Whether this live unit has unfinished plow work that can be dropped. */
    public boolean canCancelPlow(final Unit unit) {
        return unit != null && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.getAction().getType() == UnitActionType.plowField;
    }

    /** Discard plow progress without refunding movement, tools or terrain. */
    public void cancelPlow(final Unit unit) {
        Preconditions.checkState(canCancelPlow(unit),
                "Unit is not doing plow work");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).cancelPlowing();
    }

    /**
     * Authoritative forest-clearing admission for the current player's live
     * pioneer.
     *
     * @param unit required unit
     * @return the first reason clearing cannot start, or empty when it can
     */
    public Optional<ClearForestProblem> getClearForestProblem(final Unit unit) {
        if (unit == null || !isGameRunning() || endingTurn
                || getCurrentPlayer() != unit.getOwner()
                || unitStorage.tryGetUnitById(unit.getId()).orElse(null) != unit
                || !unit.isAtPlaceLocation()) {
            return Optional.of(ClearForestProblem.UNAVAILABLE);
        }
        if (!(unit instanceof UnitFreeColonist)
                || !unit.getType().isCanCutTrees()) {
            return Optional.of(ClearForestProblem.NOT_PIONEER);
        }
        if (((UnitFreeColonist) unit)
                .getTools() < UnitActionClearForest.REQUIRED_TOOLS) {
            return Optional.of(ClearForestProblem.NOT_ENOUGH_TOOLS);
        }
        if (unit.getAction().getType() != UnitActionType.noAction
                || hasGoToOrder(unit)) {
            return Optional.of(ClearForestProblem.BUSY);
        }
        if (unit.getMovementThirds() <= 0) {
            return Optional.of(ClearForestProblem.NO_MOVES);
        }
        final Location at = unit.getLocation();
        if (getColonyAt(at).isPresent()) {
            return Optional.of(ClearForestProblem.COLONY);
        }
        if (!map.getTerrainAt(at).isHasTrees()) {
            return Optional.of(ClearForestProblem.NO_FOREST);
        }
        return Optional.empty();
    }

    /** Whether a current pioneer may start clearing the forest it stands in. */
    public boolean canClearForest(final Unit unit) {
        return getClearForestProblem(unit).isEmpty();
    }

    /**
     * Terms the player must confirm before a pioneer starts clearing.
     *
     * @param unit required unit
     * @return the current terms, or empty when clearing cannot start
     */
    public Optional<ClearForestOffer> getClearForestOffer(final Unit unit) {
        if (!canClearForest(unit)) {
            return Optional.empty();
        }
        return Optional.of(new ClearForestOffer(unit.getId(),
                unit.getLocation(),
                unit.getType() == UnitType.HARDY_PIONEER
                        ? UnitActionClearForest.HARDY_TURNS_TO_CLEAR_FOREST
                        : UnitActionClearForest.REQUIRED_TURNS_TO_CLEAR_FOREST,
                UnitActionClearForest.REQUIRED_TOOLS,
                UnitActionClearForest.LUMBER_YIELD));
    }

    /**
     * Start clearing after the player confirmed the offered terms. Movement is
     * spent now; tools, forest and lumber change only at completion.
     *
     * @param unit      required unit
     * @param confirmed the exact offer the player confirmed
     * @throws IllegalStateException when the unit or terrain no longer allow
     *                               the work or the terms changed
     */
    public void clearForest(final Unit unit, final ClearForestOffer confirmed) {
        Preconditions.checkNotNull(confirmed, "Confirmed offer is required");
        Preconditions.checkState(
                getClearForestOffer(unit).filter(confirmed::equals).isPresent(),
                "Forest clearing offer is no longer valid");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).startClearingForest();
    }

    /**
     * Preview the colony that would receive the lumber if the work finished
     * now: the nearest colony of the owner within two squares, then the lowest
     * colony ID. Distance counts diagonal steps as one. Completion resolves the
     * recipient again, so the answer can change before then.
     *
     * @param owner required owner of the pioneer
     * @param at    forested square
     * @return the colony that would receive the lumber, if any
     */
    public Optional<Colony> getForestClearingRecipient(final Player owner,
            final Location at) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(at);
        final Comparator<Colony> nearestThenId = Comparator
                .comparingInt((Colony colony) -> colony.getLocation()
                        .getDistanceChebyshev(at))
                .thenComparingInt(Colony::getId);
        return getColonies(owner).stream()
                .filter(colony -> colony.getLocation().getDistanceChebyshev(
                        at) <= UnitActionClearForest.RECIPIENT_RADIUS)
                .min(nearestThenId);
    }

    /** Whether this live unit has unfinished clearing that can be dropped. */
    public boolean canCancelClearForest(final Unit unit) {
        return unit != null && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.getAction().getType() == UnitActionType.clearForest;
    }

    /** Discard clearing progress without refunding movement or tools. */
    public void cancelClearForest(final Unit unit) {
        Preconditions.checkState(canCancelClearForest(unit),
                "Unit is not clearing a forest");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).cancelClearingForest();
    }

    /**
     * Authoritative road admission for the current player's live pioneer.
     *
     * @param unit required unit
     * @return the first reason building cannot start, or empty when it can
     */
    public Optional<BuildRoadProblem> getBuildRoadProblem(final Unit unit) {
        if (unit == null || !isGameRunning() || endingTurn
                || getCurrentPlayer() != unit.getOwner()
                || unitStorage.tryGetUnitById(unit.getId()).orElse(null) != unit
                || !unit.isAtPlaceLocation()) {
            return Optional.of(BuildRoadProblem.UNAVAILABLE);
        }
        if (!(unit instanceof UnitFreeColonist)
                || !unit.getType().isCanBuildRoad()) {
            return Optional.of(BuildRoadProblem.NOT_PIONEER);
        }
        if (((UnitFreeColonist) unit)
                .getTools() < UnitActionBuildRoad.REQUIRED_TOOLS) {
            return Optional.of(BuildRoadProblem.NOT_ENOUGH_TOOLS);
        }
        if (unit.getAction().getType() != UnitActionType.noAction
                || hasGoToOrder(unit)) {
            return Optional.of(BuildRoadProblem.BUSY);
        }
        if (unit.getMovementThirds() <= 0) {
            return Optional.of(BuildRoadProblem.NO_MOVES);
        }
        final Location at = unit.getLocation();
        if (!map.getTerrainTypeAt(at).isLand()) {
            return Optional.of(BuildRoadProblem.SEA);
        }
        if (map.hasRoadAt(at)) {
            return Optional.of(BuildRoadProblem.ALREADY_ROAD);
        }
        return Optional.empty();
    }

    /** Whether a current pioneer may start building a road where it stands. */
    public boolean canBuildRoad(final Unit unit) {
        return getBuildRoadProblem(unit).isEmpty();
    }

    /**
     * Start building a road. Movement is spent now; tools and the road change
     * only at completion.
     *
     * @param unit required unit
     * @throws IllegalStateException when the unit or square no longer allow the
     *                               work
     */
    public void buildRoad(final Unit unit) {
        Preconditions.checkState(canBuildRoad(unit),
                "Unit cannot build road: %s",
                getBuildRoadProblem(unit).orElse(null));
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).startBuildingRoad();
    }

    /** Whether this live unit has unfinished road work that can be dropped. */
    public boolean canCancelBuildRoad(final Unit unit) {
        return unit != null && isGameRunning() && !endingTurn
                && getCurrentPlayer() == unit.getOwner()
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && unit.getAction().getType() == UnitActionType.buildRoad;
    }

    /** Discard road progress without refunding movement or tools. */
    public void cancelBuildRoad(final Unit unit) {
        Preconditions.checkState(canCancelBuildRoad(unit),
                "Unit is not building a road");
        checkTurnPreparationComplete();
        ((AbstractUnit) unit).cancelBuildingRoad();
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
        if (unit == null) {
            return false;
        }
        // One place read: a unit departing on the moving thread is refused
        // instead of failing a second location query.
        final Place place = unit.getPlace();
        return isGameRunning() && unit.getOwner() == getCurrentPlayer()
                && getPlayers().stream()
                        .anyMatch(player -> player == unit.getOwner())
                && unitStorage.tryGetUnitById(unit.getId()).orElse(null) == unit
                && place instanceof PlaceLocation
                && unit.getMovementThirds() > 0
                && ((PlaceLocation) place).getLocation() != null
                && map.isValid(((PlaceLocation) place).getLocation())
                && unit.getAction().isNoAction() && !hasGoToOrder(unit);
    }

    public void skipUnitTurn(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        Preconditions.checkState(canSkipUnitTurn(unit),
                "Unit turn cannot be skipped");
        unit.setActionType(UnitActionType.noAction);
    }

    /**
     * Why the given unit cannot be disbanded now. The same check runs again
     * before the removal.
     *
     * @param unit unit the player selected, may be null
     * @return the first reason, or empty when the unit may be disbanded
     */
    public Optional<DisbandProblem> getDisbandProblem(final Unit unit) {
        if (unit == null || unitStorage.tryGetUnitById(unit.getId())
                .orElse(null) != unit) {
            return Optional.of(DisbandProblem.UNAVAILABLE);
        }
        if (!isGameRunning() || endingTurn) {
            return Optional.of(DisbandProblem.WRONG_PHASE);
        }
        if (getCurrentPlayer() != unit.getOwner()) {
            return Optional.of(DisbandProblem.NOT_OWNER);
        }
        // A Crown-seized unit is inaccessible to its owner, so it cannot be
        // disbanded either (R-0187).
        if (unit.isCrownSeized()) {
            return Optional.of(DisbandProblem.UNAVAILABLE);
        }
        final Place place = unit.getPlace();
        if (place instanceof PlaceHighSea) {
            return Optional.of(DisbandProblem.VOYAGE);
        }
        if (place instanceof PlaceShipRepair) {
            return Optional.of(DisbandProblem.IN_REPAIR);
        }
        if (place instanceof PlaceCargoSlot) {
            return Optional.of(DisbandProblem.ABOARD_CARRIER);
        }
        if (unit.canHoldCargo()
                && !((UnitWithCargo) unit).getCargo().isEmpty()) {
            return Optional.of(DisbandProblem.LOADED_CARRIER);
        }
        final Optional<Colony> residence = residenceOf(place);
        if (residence.isPresent()) {
            // The equipment goes only to a live colony of the same owner.
            if (residence.get().getOwner() != unit.getOwner()
                    || !residence.get().isValid()) {
                return Optional.of(DisbandProblem.RECEIVING_COLONY_INVALID);
            }
            // Removing the last resident would destroy the colony and its
            // warehouse as a side effect, so it is refused.
            if (residence.get().getUnitsInsideColony().size() <= 1) {
                return Optional.of(DisbandProblem.LAST_COLONY_RESIDENT);
            }
            if (!unit.canLeaveColony()) {
                return Optional.of(DisbandProblem.MINIMUM_RESIDENTS);
            }
        }
        return Optional.empty();
    }

    /**
     * Terms the player must confirm before a unit is disbanded: the equipment
     * it carries, the part the colony keeps and the part that is lost.
     *
     * @param unit unit the player selected, may be null
     * @return the current terms, or empty when disbanding is not allowed
     */
    public Optional<DisbandOffer> getDisbandOffer(final Unit unit) {
        if (getDisbandProblem(unit).isPresent()) {
            return Optional.empty();
        }
        final Optional<Colony> receiving = receivingColony(unit);
        final Map<GoodsType, Integer> carried = unit instanceof UnitFreeColonist
                ? ((UnitFreeColonist) unit).getCarriedEquipment()
                : ImmutableMap.of(GoodsType.MUSKET, 0, GoodsType.HORSE, 0,
                        GoodsType.TOOLS, 0);
        final Map<GoodsType, Integer> retained = new HashMap<>();
        final Map<GoodsType, Integer> lost = new HashMap<>();
        final Map<GoodsType, Integer> capacity = new HashMap<>();
        for (final GoodsType type : DISBANDABLE_EQUIPMENT) {
            final int amount = carried.get(type);
            final int kept = receiving
                    .map(colony -> Math.min(amount,
                            colony.getWarehouse().getFreeStorage(type)))
                    .orElse(0);
            retained.put(type, kept);
            lost.put(type, amount - kept);
            receiving.ifPresent(colony -> capacity.put(type, colony
                    .getWarehouse().getStorageCapacity(type).getAmount()));
        }
        return Optional.of(new DisbandOffer(unit.getId(), unit.getPlace(),
                receiving.orElse(null), carried, retained, lost, capacity));
    }

    /**
     * Remove a unit the player chose to disband. Retained equipment goes to the
     * receiving colony, the unit leaves its place and the registry once. No
     * order runs, no turn advances and no voyage is settled. A rejected call
     * changes nothing.
     *
     * @param unit               unit the player selected
     * @param confirmed          exact terms the player confirmed
     * @param excessAcknowledged whether the player accepted losing the excess
     * @throws IllegalStateException when the unit, phase, terms or the
     *                               acknowledgement do not match the offer
     */
    public void disband(final Unit unit, final DisbandOffer confirmed,
            final boolean excessAcknowledged) {
        Preconditions.checkNotNull(confirmed, "Confirmed offer is required");
        Preconditions.checkState(
                getDisbandOffer(unit).filter(confirmed::equals).isPresent(),
                "Disband offer is no longer valid");
        Preconditions.checkState(
                excessAcknowledged || !confirmed.hasExcessLoss(),
                "Excess warehouse loss must be acknowledged");
        final Optional<Colony> receiving = confirmed.getReceivingColony();
        unit.getPlace().destroy();
        receiving.ifPresent(colony -> DISBANDABLE_EQUIPMENT.forEach(type -> {
            final int kept = confirmed.getRetained(type);
            if (kept > 0) {
                colony.getWarehouse().addGoods(Goods.of(type, kept));
            }
        }));
        destroyUnit(unit);
        // The unit, its place and its equipment are settled, so a colony that
        // lost its last resident may now end the rebellion (R-0192).
        checkRebellionLoss();
    }

    /**
     * Colony that takes a unit's equipment: the colony of its resident place,
     * or an owned colony on its own square. Other places have none.
     */
    private Optional<Colony> receivingColony(final Unit unit) {
        final Place place = unit.getPlace();
        if (place instanceof PlaceLocation) {
            return getColonyAt(unit.getLocation())
                    .filter(colony -> colony.getOwner() == unit.getOwner()
                            && colony.isValid());
        }
        return residenceOf(place);
    }

    private static Optional<Colony> residenceOf(final Place place) {
        if (place instanceof PlaceColonyField) {
            return Optional.of(((PlaceColonyField) place).getColony());
        }
        if (place instanceof PlaceConstructionSlot) {
            return Optional.of(((PlaceConstructionSlot) place).getColony());
        }
        return Optional.empty();
    }

    public Optional<Unit> getFirstSelectableUnitAt(final Location location) {
        return unitStorage.getFirstSelectableUnitAt(getCurrentPlayer(),
                location);
    }

    public List<Unit> getMoveableUnitAtOwnedBy(final Location location,
            final Player player) {
        return unitStorage.getUnitsAt(location).stream()
                .filter(unit -> unit.getMovementThirds() > 0
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
        out.setGeneratedWorld(generatedWorld);
        out.setNextColonyId(nextColonyId);
        tradeRoutes.save(out);
        map.save(out);
        unitStorage.save(out);
        out.setCalendar(calendar.save());
        gameManager.save(out);
        playerStore.save(out);
        out.setEuropeanRelations(europeanRelations.save(this::isOverlapLive));
        out.setEurope(europe.save());
        // Zero means no Crown-war round has been processed yet.
        out.setCrownWarRound(lastCrownWarRound == 0 ? null : lastCrownWarRound);
        out.setColonies(getSaveColonies());
        out.setNativeSettlements(
                nativeSettlements.toPo(this::isOpenNativeExpedition));
        out.setNativeSettlementKnowledge(saveLearnedNativeSettlements());
        out.setNativeTribalAnger(nativeRelations.saveTribalAnger());
        out.setNativeLocalAlarm(nativeRelations.saveLocalAlarm());
        out.setNativeArmsSales(nativeRelations.saveArmsSales());
        out.setNativeRelationOutcomeIds(nativeRelations.saveAppliedOutcomes());
        out.setNativeIncitements(nativeIncitements.save());
        out.setCoastalBatteryShots(coastalBatteryShots.save());
        out.setNativeLandRights(nativeLandRights.savePaid());
        out.setNativeLandTakings(nativeLandRights.saveTaken());
        out.setNativeContacts(nativeContacts.save());
        out.setFocusedField(focusedField);
        out.setTurnEvents(turnEventStore.save());
        out.setStatistics(statistics.save());
        return out;
    }

    /**
     * @return every native settlement; for the model's own checks and saving
     *         only, not for player-facing views
     */
    List<NativeSettlement> getNativeSettlements() {
        return nativeSettlements.getAll();
    }

    /**
     * Remembered native settlement identity, not current sight: settlements
     * whose nation, class and capital status the player has learned. Identity
     * is learned only by present sight and stays known under fog, after leaving
     * sight and across save and Continue. Explored terrain alone teaches
     * nothing.
     *
     * @param player required player observing the map
     * @return learned settlements in saved order
     */
    public List<NativeSettlement> getNativeSettlementsKnownTo(
            final Player player) {
        Preconditions.checkNotNull(player);
        final Set<Integer> learned = learnedNativeSettlements
                .getOrDefault(player.getName(), Set.of());
        final List<NativeSettlement> out = new ArrayList<>();
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            if (learned.contains(settlement.getId())) {
                out.add(settlement);
            }
        }
        return out;
    }

    /**
     * @param player   required player observing the map
     * @param location required square
     * @return remembered identity of the settlement on the square, not current
     *         sight: present only if the player has learned it by sight, even
     *         when the square is now under fog
     */
    public Optional<NativeSettlement> getNativeSettlementKnownTo(
            final Player player, final Location location) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(location);
        final Set<Integer> learned = learnedNativeSettlements
                .getOrDefault(player.getName(), Set.of());
        return nativeSettlements.getAt(location)
                .filter(settlement -> learned.contains(settlement.getId()));
    }

    /**
     * Home of a native brave as far as the player has learned it. A brave's
     * nation is public once the brave is seen, but its home settlement is only
     * reported after the player has learned that settlement by sight, so a
     * hidden home never leaks through a visible brave.
     *
     * @param player required player observing the map
     * @param brave  required registered unit
     * @return the learned home, empty for a homeless survivor, an unlearned
     *         home or a unit that is not a native brave
     */
    public Optional<NativeSettlement> getNativeBraveHomeKnownTo(
            final Player player, final Unit brave) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(brave);
        if (!(brave instanceof UnitNativeBrave)) {
            return Optional.empty();
        }
        final Set<Integer> learned = learnedNativeSettlements
                .getOrDefault(player.getName(), Set.of());
        return ((UnitNativeBrave) brave).getHomeSettlementId()
                .filter(learned::contains)
                .flatMap(id -> nativeSettlements.getAll().stream()
                        .filter(settlement -> settlement.getId() == id)
                        .findAny());
    }

    /**
     * Home of a native brave as its own nation knows it. Unlike
     * {@link #getNativeBraveHomeKnownTo} no learned knowledge is needed,
     * because the nation's own turn acts on its own settlements.
     *
     * @param brave required native brave
     * @return the home settlement, empty for a homeless survivor
     */
    public Optional<NativeSettlement> getNativeBraveHome(
            final UnitNativeBrave brave) {
        Preconditions.checkNotNull(brave);
        return brave.getHomeSettlementId()
                .map(id -> nativeSettlements.getAll().stream()
                        .filter(settlement -> settlement.getId() == id)
                        .findAny().orElseThrow(() -> new IllegalStateException(
                                "Native brave home is missing")));
    }

    /**
     * Attitude of a native brave's nation toward a colonial European. A brave
     * with a home reads that settlement's hostility; a homeless survivor reads
     * the tribal anger of its nation alone. Pure read, so it advances nothing.
     *
     * @param brave  required native brave
     * @param player required colonial player
     * @return content, uneasy, angry or hostile
     */
    public NativeHostility getNativeBraveAttitude(final UnitNativeBrave brave,
            final Player player) {
        Preconditions.checkNotNull(brave);
        final EuropeanNation european = nativeEuropean(player);
        return getNativeBraveHome(brave)
                .map(home -> nativeRelations.getHostility(home, european))
                .orElseGet(() -> NativeHostility.of(nativeRelations
                        .getTribalAnger(brave.getNation(), european)));
    }

    /**
     * Hostility of a native settlement toward a colonial European, read from
     * the larger of tribal anger and local alarm. Only the model computes it.
     *
     * @param player     required colonial player
     * @param settlement required native settlement
     * @return content, uneasy, angry or hostile
     */
    public NativeHostility getNativeHostility(final Player player,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(settlement);
        return nativeRelations.getHostility(settlement, nativeEuropean(player));
    }

    /**
     * Actions a visitor may choose at a native settlement, each with its limit
     * (R-0157). Pure read: it changes no unit, relation, cargo, treasury, visit
     * or random state, so cancelling the chooser has nothing to undo. An empty
     * list means the visitor cannot enter the settlement now: it is not the
     * current colonial player's registered unit on the map, not adjacent, or
     * the player has not learned the settlement.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @return one choice per action, or none when entry is not legal now
     */
    public List<NativeVisitChoice> getNativeVisitChoices(final Unit visitor,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        if (!isEntryAllowed(visitor, settlement)) {
            return List.of();
        }
        return NativeVisitRules.choices(visitor,
                getNativeHostility(visitor.getOwner(), settlement),
                settlement.getTeachingPupilId().isPresent(),
                settlement.getMissionaryId().isPresent(),
                isRivalMission(visitor, settlement),
                isTradeOpen(visitor, settlement),
                !getNativeIncitementTargets(visitor, settlement).isEmpty(),
                getNativeConquestQuote(visitor, settlement).isPresent(),
                NativeTributeRules.isCooling(
                        settlement.findTributeTurn(
                                nativeEuropean(visitor.getOwner())),
                        calendar.getNumberOfPlayedTurns()),
                getNativeTributeQuote(visitor, settlement).isPresent());
    }

    /**
     * Terms of the chief visit a scout may confirm now (R-0158). Pure read: it
     * draws nothing, pays nothing and records nothing, so cancelling changes
     * nothing. A nation that has met the chief here before gets the information
     * again, so its terms are a repeat with no risk.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @return the terms, empty when the chief is not available to the visitor
     */
    public Optional<NativeChiefPreview> getNativeChiefPreview(
            final Unit visitor, final NativeSettlement settlement) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        if (!isChiefAvailable(visitor, settlement)) {
            return Optional.empty();
        }
        final Player owner = visitor.getOwner();
        final boolean repeat = settlement
                .findChiefEncounter(nativeEuropean(owner)).isPresent();
        return Optional
                .of(NativeChiefPreview.of(getNativeHostility(owner, settlement),
                        visitor.getType() == UnitType.SEASONED_SCOUT, repeat));
    }

    /**
     * Visit the native chief with the terms the player confirmed (R-0158). The
     * first visit of a nation draws once from the persisted random state, then
     * applies the outcome: information discloses this settlement, gold moves up
     * to its cap from its treasury to the player, and death removes the scout.
     * The outcome is recorded for the nation, so later visits repeat only the
     * information. Every check and the overflow test run before the draw.
     *
     * @param visitor    required scout adjacent to the settlement
     * @param settlement required native settlement
     * @param shown      required terms the player was shown and confirmed
     * @return the actual result of the visit
     * @throws IllegalStateException when the chief is not available now or the
     *                               shown terms went stale
     */
    public NativeChiefResult visitNativeChief(final Unit visitor,
            final NativeSettlement settlement, final NativeChiefPreview shown) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        final Optional<NativeChiefPreview> current = getNativeChiefPreview(
                visitor, settlement);
        Preconditions.checkState(
                current.isPresent() && current.get().equals(shown),
                "Native chief terms changed");
        final Player owner = visitor.getOwner();
        final EuropeanNation european = nativeEuropean(owner);
        if (settlement.findChiefEncounter(european).isPresent()) {
            // Repeating the information reveals the same land again, no draw.
            // The nation keeps the information it has now been given, while
            // its first outcome stays as drawn.
            owner.revealLandAround(settlement.getLocation());
            settlement.recordChiefInformation(european);
            return NativeChiefResult.repeated(chiefInformation(settlement));
        }
        // The most gold this settlement can pay must fit the purse before the
        // draw, so no refused result has to restore the random state.
        final int maxGold = NativeChiefRules
                .goldAmount(settlement.getTreasury(), settlement.isCapital());
        Preconditions.checkState(owner.getGold() <= Integer.MAX_VALUE - maxGold,
                "Native chief gold would overflow");
        final NativeChiefOutcome outcome = NativeChiefRules.draw(
                shown.getHostility(), shown.isSeasoned(), nextCombatRandom());
        final int gold = outcome == NativeChiefOutcome.GOLD ? maxGold : 0;
        switch (outcome) {
        case GOLD:
            settlement.setTreasury(settlement.getTreasury() - gold);
            owner.setGold(owner.getGold() + gold);
            break;
        case DEATH:
            // A dead scout keeps no go-to order and no square.
            ((AbstractUnit) visitor).discardGoToOrder();
            visitor.getPlace().destroy();
            destroyUnit(visitor);
            break;
        default:
            break;
        }
        settlement.recordChiefEncounter(european, outcome);
        final Optional<NativeChiefResult.Information> informed = outcome == NativeChiefOutcome.INFORMATION
                ? Optional.of(chiefInformation(settlement))
                : Optional.empty();
        if (outcome == NativeChiefOutcome.INFORMATION) {
            owner.revealLandAround(settlement.getLocation());
        }
        return NativeChiefResult.first(outcome, gold, informed);
    }

    /**
     * Terms of the tribute demand a visitor may confirm now (R-0162). Pure
     * read: nothing is drawn, moved, paid or recorded, so cancelling changes
     * nothing. The visitor must have a demanding role, stand beside the learned
     * settlement on turn, and its European must have passed the ten-turn wait
     * here. Any unit on the square must be an actual native brave of the
     * settlement; with none, the defense is zero and the demand is accepted.
     * The force is the base strength of the visitor and of its own soldiers,
     * dragoons and scouts on the eight squares around the settlement, each
     * counted once; the defense is the strength of the defenders with the
     * settlement's defense bonus, from the same battle strength rules as an
     * attack.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @return the terms, empty when no demand is legal now
     */
    public Optional<NativeTributeQuote> getNativeTributeQuote(
            final Unit visitor, final NativeSettlement settlement) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        if (!isEntryAllowed(visitor, settlement)
                || !NativeVisitRules.canDemandTribute(visitor)) {
            return Optional.empty();
        }
        final Player owner = visitor.getOwner();
        final EuropeanNation european = nativeEuropean(owner);
        final int turn = calendar.getNumberOfPlayedTurns();
        if (NativeTributeRules.isCooling(settlement.findTributeTurn(european),
                turn)) {
            return Optional.empty();
        }
        // A square holding anything but the settlement's own braves is refused.
        // An empty square is not refused: a demand is not an attack, so with no
        // defenders the defense is zero and the demand is accepted.
        final List<Unit> onSquare = getUnitsAt(settlement.getLocation());
        if (onSquare.stream()
                .anyMatch(unit -> !isNativeDefender(unit, settlement))) {
            return Optional.empty();
        }
        double defense = 0;
        final List<Integer> defenders = new ArrayList<>();
        for (final Unit defender : onSquare.stream()
                .sorted(Comparator.comparingInt(Unit::getId))
                .collect(Collectors.toList())) {
            final Optional<CombatStrengths> strengths = CombatStrengths
                    .forSettlementBattle(this, visitor, defender,
                            settlement.getDefenseBonusPercent(), 0);
            if (strengths.isEmpty()) {
                return Optional.empty();
            }
            defense += strengths.get().getDefender().getTotal();
            defenders.add(defender.getId());
        }
        final List<Unit> forceUnits = tributeForceUnits(visitor, settlement);
        final double force = forceUnits.stream()
                .mapToDouble(CombatStrengths::tributeBase).sum();
        return Optional.of(new NativeTributeQuote(settlement, visitor.getId(),
                forceUnits.stream().map(Unit::getId)
                        .collect(Collectors.toList()),
                force, defenders, defense, settlement.getTreasury(),
                nativeRelations.getTribalAnger(settlement.getNation(),
                        european),
                nativeRelations.getLocalAlarm(settlement, european), turn));
    }

    /**
     * Units that add force to a tribute demand (R-0162): the visitor and the
     * soldiers, dragoons and scouts of its own owner on the eight squares
     * around the settlement. Each unit is listed once, by identity. Units
     * inside a carrier, allied or foreign units, artillery and distant units
     * are not among them.
     */
    private List<Unit> tributeForceUnits(final Unit visitor,
            final NativeSettlement settlement) {
        final Set<Unit> units = new LinkedHashSet<>();
        units.add(visitor);
        for (final Location next : settlement.getLocation().getNeighbors()) {
            if (!isValid(next)) {
                continue;
            }
            getUnitsAt(next).stream()
                    .filter(unit -> unit.getOwner() == visitor.getOwner())
                    .filter(NativeVisitRules::canDemandTribute)
                    .forEach(units::add);
        }
        return units.stream().sorted(Comparator.comparingInt(Unit::getId))
                .collect(Collectors.toList());
    }

    /**
     * Demand tribute with the terms the player confirmed (R-0162). The shown
     * terms must equal the live ones before the first change, which includes
     * the treasury, the wait and the relation values. Nothing is drawn. A paid
     * demand moves the gold from the treasury to the player; a refused one
     * moves none. Both record this European's demand here and raise the tribe's
     * anger and the settlement's alarm once, through the shared relation
     * outcome.
     *
     * @param visitor    required unit that confirmed the demand
     * @param settlement required native settlement
     * @param shown      required terms the player was shown and confirmed
     * @return the actual result of the demand
     * @throws IllegalStateException when the demand is not legal now or the
     *                               shown terms went stale
     */
    public NativeTributeResult demandNativeTribute(final Unit visitor,
            final NativeSettlement settlement, final NativeTributeQuote shown) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        final Optional<NativeTributeQuote> current = getNativeTributeQuote(
                visitor, settlement);
        Preconditions.checkState(
                current.isPresent() && current.get().equals(shown),
                "Native tribute terms changed");
        final Player owner = visitor.getOwner();
        final EuropeanNation european = nativeEuropean(owner);
        final int payment = shown.getPayment();
        Preconditions.checkState(owner.getGold() <= Integer.MAX_VALUE - payment,
                "Native tribute gold would overflow");
        // Every check has passed; only the changes below remain.
        if (payment > 0) {
            settlement.setTreasury(settlement.getTreasury() - payment);
            owner.setGold(owner.getGold() + payment);
        }
        settlement.recordTribute(european, calendar.getNumberOfPlayedTurns());
        applyNativeRelationOutcome(owner, NativeRelationOutcome
                .tribute(nativeRelations.nextOutcomeId(), settlement));
        return NativeTributeResult.of(shown.isAccepted(), payment);
    }

    /**
     * Saved tribute demands cannot be from a later turn than the saved calendar
     * (R-0162); a loaded game never advances the turn or repeats a demand.
     */
    void validateSavedNativeTributes() {
        final int turn = calendar.getNumberOfPlayedTurns();
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            for (final int demand : settlement.getTributeTurns().values()) {
                Preconditions.checkArgument(demand <= turn,
                        "Unsupported save: native tribute turn is in the future");
            }
        }
    }

    /**
     * Whether the visitor may confirm the chief now: the chooser must offer it,
     * which needs a scout that passes the shared entry gate. Pure read.
     */
    private boolean isChiefAvailable(final Unit visitor,
            final NativeSettlement settlement) {
        return getNativeVisitChoices(visitor, settlement).stream().anyMatch(
                choice -> choice.getAction() == NativeVisitAction.SCOUT_CHIEF
                        && choice
                                .getLimit() == NativeVisitChoice.Limit.AVAILABLE);
    }

    /**
     * What a chief tells about a settlement: its own offered and wanted goods
     * and its teachable skill. The land within two squares is revealed on the
     * player's map separately, so it is not listed here.
     */
    private NativeChiefResult.Information chiefInformation(
            final NativeSettlement settlement) {
        return new NativeChiefResult.Information(settlement.getOfferedGoods(),
                settlement.getWantedGoods(), settlement.getTeachableSkill());
    }

    /**
     * Gate shared by every native visit: the running game, the visitor's
     * colonial owner on turn, the visitor itself registered on the map beside
     * the settlement, and the settlement learned by that owner. Pure read.
     *
     * @param visitor    required unit
     * @param settlement required native settlement
     * @return whether the visitor may enter the settlement now
     */
    private boolean isEntryAllowed(final Unit visitor,
            final NativeSettlement settlement) {
        final Player owner = visitor.getOwner();
        return isGameRunning() && owner != null && getCurrentPlayer() == owner
                && owner.isColonialNation() && getPlayers().contains(owner)
                && tryGetUnitById(visitor.getId()).orElse(null) == visitor
                && visitor.isAtPlaceLocation()
                && visitor.getLocation().isNeighbor(settlement.getLocation())
                && getNativeSettlementKnownTo(owner, settlement.getLocation())
                        .filter(known -> known == settlement).isPresent();
    }

    /**
     * Whether a land introduction has made a native tribe know a colonial
     * European nation (R-0159). Pure read.
     *
     * @param european required colonial European nation
     * @param nation   required native tribe
     * @return whether the tribe is in contact with that nation
     */
    public boolean isNativeContacted(final EuropeanNation european,
            final NativeNation nation) {
        Preconditions.checkNotNull(european);
        Preconditions.checkNotNull(nation);
        return nativeContacts.isContacted(european, nation);
    }

    /**
     * Market view of a visitor at a native settlement (R-0159). Pure read: it
     * changes no unit, contact, expedition, stock, treasury or random state.
     * The stock, wanted goods and treasury appear only when the visitor is
     * admitted.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @return the view, empty when entry is not legal now
     */
    public Optional<NativeMarketView> getNativeMarketView(final Unit visitor,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        if (!isEntryAllowed(visitor, settlement)) {
            return Optional.empty();
        }
        final EuropeanNation european = nativeEuropean(visitor.getOwner());
        final boolean contacted = nativeContacts.isContacted(european,
                settlement.getNation());
        final NativeMarketView.Admission admission = admissionOf(visitor,
                settlement, contacted);
        final boolean open = settlement.findTrade(european)
                .flatMap(state -> state.openExpedition(visitor)).isPresent();
        return Optional.of(new NativeMarketView(contacted,
                !contacted && !visitor.isShip(), admission, open,
                admission == NativeMarketView.Admission.ADMITTED
                        ? marketOf(visitor, settlement)
                        : null,
                purchaseOf(visitor, settlement)));
    }

    /**
     * Introduce a colonial nation to a native tribe by land (R-0159). The unit
     * stays where it is: no cargo, gold, movement or random state changes, and
     * repeating an introduction reuses the same contact.
     *
     * @param introducer required land unit adjacent to the settlement
     * @param settlement required native settlement
     * @throws IllegalStateException when the visitor is not an eligible land
     *                               unit
     */
    public void introduceNativeContact(final Unit introducer,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(introducer);
        Preconditions.checkNotNull(settlement);
        checkTurnPreparationComplete();
        Preconditions.checkState(isEntryAllowed(introducer, settlement),
                "Native introduction needs an adjacent unit of the current player");
        Preconditions.checkState(!introducer.getType().isShip(),
                "A ship cannot introduce its nation");
        nativeContacts.introduce(nativeEuropean(introducer.getOwner()),
                settlement.getNation());
    }

    /**
     * Open or reuse the trading expedition of a carrier at a native settlement
     * (R-0159). The admission must pass now. An expedition that is already open
     * for this carrier at this square is reused, so reopening never starts a
     * new one; any other start records the carrier, its square and its place.
     *
     * @param carrier    required wagon or ship adjacent to the settlement
     * @param settlement required native settlement
     * @throws IllegalStateException when the market is not open to the carrier
     */
    public void enterNativeMarket(final Unit carrier,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        checkTurnPreparationComplete();
        Preconditions.checkState(isEntryAllowed(carrier, settlement),
                "Native market needs an adjacent unit of the current player");
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        Preconditions.checkState(admissionOf(carrier, settlement,
                nativeContacts.isContacted(european, settlement
                        .getNation())) == NativeMarketView.Admission.ADMITTED,
                "Native market is not open to this unit");
        final NativeTradeState trade = settlement.tradeFor(european);
        if (trade.openExpedition(carrier).isEmpty()) {
            trade.startExpedition(carrier);
        }
    }

    /**
     * Whether a village refuses to take this good from the carrier now, because
     * the expedition began right after the village received it from an earlier
     * carrier (R-0159). Only the goods the village buys from the player are
     * meant: the offered stock the player buys (R-0161) is not involved.
     * Muskets are never refused. Pure read.
     *
     * @param carrier    required unit
     * @param settlement required native settlement
     * @param goods      required goods the carrier would deliver
     * @return whether the repeated goods are refused
     */
    public boolean isRepeatedReceivedGoods(final Unit carrier,
            final NativeSettlement settlement, final GoodsType goods) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(goods);
        if (!isEntryAllowed(carrier, settlement)) {
            return false;
        }
        return settlement.findTrade(nativeEuropean(carrier.getOwner()))
                .flatMap(state -> state.openExpedition(carrier))
                .map(expedition -> NativeMarketRules.repeatsPreviousGood(
                        settlement.getSettlementClass(),
                        expedition.getBlockedGoods().orElse(null), goods))
                .orElse(false);
    }

    /**
     * Record that the village received goods the carrier delivers, in its open
     * expedition (R-0159). Shared with the transaction of R-0160, which moves
     * the goods and gold itself; this changes no stock, gold or cargo.
     *
     * @param carrier    required unit with an open expedition and the goods
     * @param settlement required native settlement
     * @param goods      required goods delivered by the carrier
     * @throws IllegalStateException when the delivery is not allowed now
     */
    void recordNativeGoodsReceived(final Unit carrier,
            final NativeSettlement settlement, final GoodsType goods) {
        Preconditions.checkNotNull(goods);
        checkTurnPreparationComplete();
        Preconditions.checkState(isEntryAllowed(carrier, settlement),
                "Native delivery needs an adjacent unit of the current player");
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        Preconditions.checkState(admissionOf(carrier, settlement,
                nativeContacts.isContacted(european, settlement
                        .getNation())) == NativeMarketView.Admission.ADMITTED,
                "Native market is not open to this unit");
        final NativeTradeState trade = settlement.findTrade(european)
                .filter(state -> state.openExpedition(carrier).isPresent())
                .orElseThrow(() -> new IllegalStateException(
                        "Native delivery needs an open expedition"));
        Preconditions.checkState(carries(carrier, goods),
                "The carrier does not hold these goods");
        Preconditions.checkState(
                !isRepeatedReceivedGoods(carrier, settlement, goods),
                "The village refuses the same goods on consecutive expeditions");
        trade.recordReceived(goods);
    }

    /**
     * Terms of one sale or gift of carried goods to a native settlement
     * (R-0160). Pure read: no goods, gold, treasury, expedition, outcome or
     * random state changes. The market must be open to the carrier now, the
     * goods must be tradable, carried and not refused by the village, and the
     * quantity must not exceed {@link #getNativeMaxSaleQuantity}.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param goods      required tradable goods
     * @param quantity   required pieces
     * @param gift       whether the goods are given without gold
     * @return the terms, empty when the sale or gift is not legal now
     */
    public Optional<NativeSaleQuote> getNativeSaleQuote(final Unit carrier,
            final NativeSettlement settlement, final GoodsType goods,
            final int quantity, final boolean gift) {
        Preconditions.checkNotNull(goods);
        if (quantity <= 0 || quantity > getNativeMaxSaleQuantity(carrier,
                settlement, goods, gift)) {
            return Optional.empty();
        }
        return Optional
                .of(saleQuote(carrier, settlement, goods, quantity, gift));
    }

    /**
     * Most pieces of carried goods that may be sold or given now (R-0160): the
     * carried amount for a gift, and for a sale also the amount the
     * settlement's treasury pays at the current terms. Zero when the market is
     * not open to the carrier, the village refuses these goods, or the goods
     * are not tradable. Pure read.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param goods      required goods
     * @param gift       whether the goods are given without gold
     * @return the largest legal quantity, zero when none is legal
     */
    public int getNativeMaxSaleQuantity(final Unit carrier,
            final NativeSettlement settlement, final GoodsType goods,
            final boolean gift) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(goods);
        final Optional<NativeExpedition> expedition = openSale(carrier,
                settlement);
        if (expedition.isEmpty() || !goods.isCanBeBought()
                || isRepeatedReceivedGoods(carrier, settlement, goods)) {
            return 0;
        }
        // A trade whose transaction revision is exhausted offers nothing: no
        // further expedition, haggle or sale could be counted.
        if (settlement.findTrade(nativeEuropean(carrier.getOwner()))
                .filter(NativeTradeState::canAdvanceRevision).isEmpty()) {
            return 0;
        }
        // The village can count only so many more pieces of these goods: the
        // limit applies to a gift and a sale alike, before any change.
        final int limit = Math.min(cargoAmount(carrier, goods),
                settlement.getReceiveRoom(goods));
        if (gift) {
            return limit;
        }
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        return NativeMarketRules.maxSaleQuantity(limit,
                settlement.getTreasury(), europeBid(goods, european),
                settlement.getWantedGoods().contains(goods),
                carrier.getType().isShip(), haggleOf(expedition.get(), goods,
                        false) == NativeSaleQuote.Haggle.ACCEPTED);
    }

    /**
     * The one haggle of a carrier's expedition (R-0160): one request for +10%
     * receipts on the complete quote, succeeding with 50% at content and 25% at
     * uneasy natives. The shown offer must still be the live offer, with its
     * haggle still open, and the raised price must still be payable; only then
     * is the saved random state drawn. Every refusal happens before that draw,
     * so it changes neither the state nor the random state. The result is saved
     * with the expedition, so reopening, saving and continuing never rerolls. A
     * failed haggle keeps the original offer; the haggle is spent either way.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param shown      required sale offer the player saw, with its haggle
     *                   open
     * @return whether the haggle succeeded
     * @throws NativeTermsChangedException when the shown offer no longer holds
     * @throws IllegalStateException       when the raised price cannot be paid
     */
    public boolean haggleNativeSale(final Unit carrier,
            final NativeSettlement settlement, final NativeSaleQuote shown) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        Preconditions.checkArgument(!shown.isGift(),
                "A gift cannot be haggled");
        final NativeSaleQuote live = getNativeSaleQuote(carrier, settlement,
                shown.getGoods(), shown.getQuantity(), false)
                .filter(current -> current.equals(shown))
                .orElseThrow(() -> new NativeTermsChangedException(
                        "Native terms changed or the market closed; nothing was haggled"));
        Preconditions.checkState(
                live.getHaggle() == NativeSaleQuote.Haggle.OFFERED,
                "Native haggle was already used in this expedition");
        Preconditions
                .checkState(
                        NativeMarketRules.saleTotal(live.getQuantity(),
                                live.getEuropeBid(), live
                                        .isWanted(),
                                live.isShip(), true) <= live.getTreasury(),
                        "The natives cannot pay the haggled price; nothing was drawn");
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        final NativeExpedition expedition = openSale(carrier, settlement)
                .orElseThrow(() -> new IllegalStateException(
                        "Native trade is not open to this unit"));
        final NativeTradeState trade = settlement.findTrade(european)
                .orElseThrow();
        // Checked before the draw, so a revision overflow never consumes the
        // random state.
        Preconditions.checkState(trade.canAdvanceRevision(),
                "The native trade revision is exhausted; nothing was drawn");
        final boolean accepted = NativeMarketRules
                .haggleSucceeds(live.getHostility(), nextCombatRandom());
        expedition.recordHaggle(shown.getGoods(), accepted,
                shown.getQuantity());
        trade.advanceRevision();
        return accepted;
    }

    /**
     * Sell carried goods to a native settlement at the terms the player was
     * shown (R-0160). Every check runs before the first change: the quote must
     * still match, the village must accept the goods, the outcome IDs must be
     * usable and the gold must fit. Then the goods leave the carrier, the gold
     * moves once, the treasury pays, the village records the goods and the
     * trade and arms outcomes apply.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param offered    required sale quote the player confirmed
     * @throws NativeTermsChangedException when the terms changed; nothing
     *                                     traded
     * @throws IllegalStateException       when the sale is not legal now
     */
    public void sellToNative(final Unit carrier,
            final NativeSettlement settlement, final NativeSaleQuote offered) {
        Preconditions.checkNotNull(offered);
        Preconditions.checkArgument(!offered.isGift(),
                "A gift pays no gold; use giftToNative");
        commitNativeTrade(carrier, settlement, offered);
    }

    /**
     * Give carried goods to a native settlement without gold (R-0160). The same
     * preflight as a sale applies, but the treasury is not checked, and the
     * outcome is the proportional gift reduction. Muskets and horses given away
     * are not sold, so they add no arms tribal anger.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param offered    required gift terms the player confirmed
     * @throws NativeTermsChangedException when the terms changed; nothing given
     * @throws IllegalStateException       when the gift is not legal now
     */
    public void giftToNative(final Unit carrier,
            final NativeSettlement settlement, final NativeSaleQuote offered) {
        Preconditions.checkNotNull(offered);
        Preconditions.checkArgument(offered.isGift(),
                "A sale pays gold; use sellToNative");
        commitNativeTrade(carrier, settlement, offered);
    }

    private void commitNativeTrade(final Unit carrier,
            final NativeSettlement settlement, final NativeSaleQuote offered) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        checkTurnPreparationComplete();
        final GoodsType goods = offered.getGoods();
        final NativeSaleQuote live = getNativeSaleQuote(carrier, settlement,
                goods, offered.getQuantity(), offered.isGift())
                .filter(current -> current.equals(offered))
                .orElseThrow(() -> new NativeTermsChangedException(
                        "Native terms changed or the market closed; nothing was traded"));
        final Player owner = carrier.getOwner();
        final EuropeanNation european = nativeEuropean(owner);
        // Taken before any change: a sale may empty the carrier, and the open
        // expedition then needs no goods to open the second stage (R-0161).
        final NativeExpedition expedition = openSale(carrier, settlement)
                .orElseThrow();
        // Both IDs are above every applied one; the sale's arms outcome is
        // applied after the trade, so its ID is the next one.
        final long tradeId = nativeRelations.nextOutcomeId();
        final NativeRelationOutcome trade = offered.isGift()
                ? NativeRelationOutcome.gift(tradeId, settlement,
                        offered.getQuantity())
                : NativeRelationOutcome.trade(tradeId, settlement);
        final boolean arms = !offered.isGift()
                && (goods.equals(GoodsType.MUSKET)
                        || goods.equals(GoodsType.HORSE));
        final NativeRelationOutcome armsSale = arms
                ? NativeRelationOutcome.armsSale(tradeId + 1,
                        settlement.getNation(), offered.getQuantity())
                : null;
        // Every check runs before the first change: the village's room, the
        // relation IDs, the gold and the village's refusal of repeated goods.
        Preconditions.checkState(
                settlement.getReceiveRoom(goods) >= offered.getQuantity(),
                "The village cannot count these goods; nothing was traded");
        Preconditions.checkState(
                settlement.findTrade(european).orElseThrow()
                        .canAdvanceRevision(),
                "The native trade revision is exhausted; nothing was traded");
        Preconditions.checkState(
                nativeRelations.canApply(european, trade) && (armsSale == null
                        || nativeRelations.canApply(european, armsSale)),
                "Native relation outcome cannot be applied; nothing was traded");
        Preconditions.checkState(addsFit(owner.getGold(), live.getGold()),
                "Gold would exceed its limit; nothing was traded");
        Preconditions.checkState(
                !isRepeatedReceivedGoods(carrier, settlement, goods),
                "The village refuses the same goods on consecutive expeditions");
        // From here on each change is infallible: the checks above and the
        // village's own delivery check have passed.
        recordNativeGoodsReceived(carrier, settlement, goods);
        final NativeTradeState tradeState = settlement.findTrade(european)
                .orElseThrow();
        tradeState.advanceRevision();
        removeCargo(carrier, goods, offered.getQuantity());
        owner.setGold(owner.getGold() + live.getGold());
        settlement.setTreasury(settlement.getTreasury() - live.getGold());
        settlement.addReceived(goods, offered.getQuantity());
        applyNativeRelationOutcome(owner, trade);
        if (armsSale != null) {
            applyNativeRelationOutcome(owner, armsSale);
        }
        // The confirmed sale or gift opens the second stage of this expedition;
        // the request result is only drawn when the player asks for it.
        expedition.openPurchase(tradeState.getRevision());
    }

    /**
     * Terms of the second stage (R-0161): one offered good bought from the
     * settlement after a confirmed sale or gift of this expedition. Pure read:
     * no goods, gold, stock, treasury, expedition or random state changes. The
     * stage must still be pending, the market must admit the carrier apart from
     * its goods, and the quantity must not exceed
     * {@link #getNativeMaxPurchaseQuantity}.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param goods      required goods the settlement offers
     * @param quantity   required pieces
     * @return the terms, empty when the purchase is not legal now
     */
    public Optional<NativePurchaseQuote> getNativePurchaseQuote(
            final Unit carrier, final NativeSettlement settlement,
            final GoodsType goods, final int quantity) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(goods);
        if (quantity <= 0 || quantity > getNativeMaxPurchaseQuantity(carrier,
                settlement, goods)) {
            return Optional.empty();
        }
        return Optional.of(purchaseQuote(carrier, settlement, goods, quantity));
    }

    /**
     * Most pieces of an offered good that the second stage may buy now
     * (R-0161): the stock, the room the carrier has for the goods, and the gold
     * that the buyer and the native treasury can take at the current request
     * result. Zero when the stage is not open, the goods are not offered, or
     * the transaction revision is exhausted. Pure read.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param goods      required goods
     * @return the largest legal quantity, zero when none is legal
     */
    public int getNativeMaxPurchaseQuantity(final Unit carrier,
            final NativeSettlement settlement, final GoodsType goods) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(goods);
        final Optional<NativeExpedition> expedition = openPurchase(carrier,
                settlement);
        if (expedition.isEmpty()) {
            return 0;
        }
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        final int limit = Math.min(settlement.getStock(goods),
                cargoRoom(carrier, goods));
        // The treasury receives the gold, so its integer room limits the sale
        // as the buyer's own gold does.
        final int payable = Math.min(carrier.getOwner().getGold(),
                Integer.MAX_VALUE - settlement.getTreasury());
        return NativeMarketRules.maxPurchaseQuantity(limit, payable,
                askPrice(goods, european), expedition.get()
                        .getPurchaseHaggle() == NativeSaleQuote.Haggle.ACCEPTED);
    }

    /**
     * The one request of the second stage (R-0161): once per purchase stage it
     * asks for 10% off the complete purchase quote, succeeding with 50% at
     * content and 25% at uneasy natives. The shown quote must still be the live
     * one, with its request still open. Every refusal happens before the draw,
     * so it changes neither the state nor the random state. The result is kept
     * with the stage, so reopening, changing the quantity or goods, and
     * Save/Continue never reroll; a failed request keeps the original price.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param shown      required purchase quote the player saw, with its
     *                   request open
     * @return whether the request succeeded
     * @throws NativeTermsChangedException when the shown quote no longer holds
     * @throws IllegalStateException       when the request was already used
     */
    public boolean haggleNativePurchase(final Unit carrier,
            final NativeSettlement settlement,
            final NativePurchaseQuote shown) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        final NativePurchaseQuote live = getNativePurchaseQuote(carrier,
                settlement, shown.getGoods(), shown.getQuantity())
                .filter(current -> current.equals(shown))
                .orElseThrow(() -> new NativeTermsChangedException(
                        "Native terms changed or the purchase closed; nothing was haggled"));
        Preconditions.checkState(
                live.getHaggle() == NativeSaleQuote.Haggle.OFFERED,
                "Native purchase request was already used in this stage");
        final NativeExpedition expedition = openPurchase(carrier, settlement)
                .orElseThrow();
        // The revision was checked by openPurchase, before the draw.
        final NativeTradeState trade = settlement
                .findTrade(nativeEuropean(carrier.getOwner())).orElseThrow();
        final boolean accepted = NativeMarketRules
                .haggleSucceeds(live.getHostility(), nextCombatRandom());
        expedition.recordPurchaseHaggle(accepted, shown.getGoods(),
                shown.getQuantity());
        trade.advanceRevision();
        return accepted;
    }

    /**
     * Buy offered goods from the settlement at the terms the player confirmed
     * (R-0161). Every check runs before the first change: the quote must still
     * match, the carrier must have room for the goods, the buyer must pay the
     * gold and the native treasury must hold it. Then the stock leaves the
     * settlement, the cargo takes the goods through its own placement, the gold
     * moves once, and the stage closes. A refused purchase changes nothing.
     *
     * @param carrier    required wagon or ship beside the settlement
     * @param settlement required native settlement
     * @param offered    required purchase quote the player confirmed
     * @throws NativeTermsChangedException when the terms changed; nothing
     *                                     bought
     * @throws IllegalStateException       when the purchase is not legal now
     */
    public void buyFromNative(final Unit carrier,
            final NativeSettlement settlement,
            final NativePurchaseQuote offered) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(offered);
        checkTurnPreparationComplete();
        final GoodsType goods = offered.getGoods();
        final NativePurchaseQuote live = getNativePurchaseQuote(carrier,
                settlement, goods, offered.getQuantity())
                .filter(current -> current.equals(offered))
                .orElseThrow(() -> new NativeTermsChangedException(
                        "Native terms changed or the purchase closed; nothing was bought"));
        final Player owner = carrier.getOwner();
        final NativeExpedition expedition = openPurchase(carrier, settlement)
                .orElseThrow();
        final NativeTradeState trade = settlement
                .findTrade(nativeEuropean(owner)).orElseThrow();
        // Every check runs before the first change.
        Preconditions.checkState(
                cargoRoom(carrier, goods) >= offered.getQuantity(),
                "The carrier cannot hold these goods; nothing was bought");
        Preconditions.checkState(
                addsFit(settlement.getTreasury(), live.getGold()),
                "Native treasury would exceed its limit; nothing was bought");
        Preconditions.checkState(owner.getGold() >= live.getGold(),
                "Not enough gold for these goods; nothing was bought");
        // From here on each change is infallible: the checks above have passed,
        // and the cargo placement refuses nothing that its room allows.
        owner.setGold(owner.getGold() - live.getGold());
        settlement.setTreasury(settlement.getTreasury() + live.getGold());
        settlement.setStock(goods,
                settlement.getStock(goods) - offered.getQuantity());
        storeGoods(carrier, goods, offered.getQuantity());
        expedition.closePurchase();
        trade.advanceRevision();
    }

    /**
     * Decline the pending second stage (R-0161). Nothing is bought, and the
     * stage does not reopen without a new confirmed sale or gift. Closing the
     * chooser instead keeps the stage pending, and it is never rerolled.
     *
     * @param carrier    required unit beside the settlement
     * @param settlement required native settlement
     * @throws IllegalStateException when no purchase is pending for this unit
     */
    public void rejectNativePurchase(final Unit carrier,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(carrier);
        Preconditions.checkNotNull(settlement);
        checkTurnPreparationComplete();
        pendingPurchase(carrier, settlement)
                .orElseThrow(() -> new IllegalStateException(
                        "No native purchase is pending for this unit"))
                .closePurchase();
    }

    /**
     * @return the pending second stage of the carrier's open expedition, after
     *         the shared entry gate; the goods and the relation gates do not
     *         apply, so a pending stage can always be declined. Pure read.
     */
    private Optional<NativeExpedition> pendingPurchase(final Unit carrier,
            final NativeSettlement settlement) {
        if (!isEntryAllowed(carrier, settlement)) {
            return Optional.empty();
        }
        return settlement.findTrade(nativeEuropean(carrier.getOwner()))
                .flatMap(state -> state.openExpedition(carrier))
                .filter(expedition -> expedition.getPurchaseRevision()
                        .isPresent());
    }

    /**
     * @return the pending second stage when its purchase may be confirmed now:
     *         the admission gates other than the goods, an undamaged carrier
     *         and a trade whose revision can still count one more transaction
     */
    private Optional<NativeExpedition> openPurchase(final Unit carrier,
            final NativeSettlement settlement) {
        final Optional<NativeExpedition> pending = pendingPurchase(carrier,
                settlement);
        if (pending.isEmpty()) {
            return Optional.empty();
        }
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        if (admissionOf(carrier, settlement,
                nativeContacts.isContacted(european, settlement.getNation()),
                false) != NativeMarketView.Admission.ADMITTED) {
            return Optional.empty();
        }
        if (carrier instanceof Ship && ((Ship) carrier).isDamaged()) {
            return Optional.empty();
        }
        if (settlement.findTrade(european)
                .filter(NativeTradeState::canAdvanceRevision).isEmpty()) {
            return Optional.empty();
        }
        return pending;
    }

    /**
     * @return whether the market is open to the visitor now: admitted with its
     *         goods, or a purchase stage can be confirmed (R-0161). Pure read.
     */
    private boolean isTradeOpen(final Unit visitor,
            final NativeSettlement settlement) {
        final boolean contacted = nativeContacts.isContacted(
                nativeEuropean(visitor.getOwner()), settlement.getNation());
        return admissionOf(visitor, settlement,
                contacted) == NativeMarketView.Admission.ADMITTED
                || openPurchase(visitor, settlement).isPresent();
    }

    /**
     * @return the purchase view of a pending stage, or null when none is
     *         pending
     */
    private NativeMarketView.Purchase purchaseOf(final Unit visitor,
            final NativeSettlement settlement) {
        final Optional<NativeExpedition> pending = pendingPurchase(visitor,
                settlement);
        if (pending.isEmpty()) {
            return null;
        }
        final EuropeanNation european = nativeEuropean(visitor.getOwner());
        final List<NativeMarketView.PurchaseOffer> offers = new ArrayList<>();
        for (final GoodsType goods : settlement.getOfferedGoods()) {
            offers.add(new NativeMarketView.PurchaseOffer(goods,
                    settlement.getStock(goods), askPrice(goods, european)));
        }
        return new NativeMarketView.Purchase(offers,
                pending.get().getPurchaseHaggle(),
                openPurchase(visitor, settlement).isPresent(),
                pending.get().getPurchaseGoods().orElse(null),
                pending.get().getPurchaseQuantity().orElse(0));
    }

    private NativePurchaseQuote purchaseQuote(final Unit carrier,
            final NativeSettlement settlement, final GoodsType goods,
            final int quantity) {
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        final NativeExpedition expedition = openPurchase(carrier, settlement)
                .orElseThrow(() -> new IllegalStateException(
                        "Native purchase is not open to this unit"));
        final NativeSaleQuote.Haggle haggle = expedition.getPurchaseHaggle();
        final int ask = askPrice(goods, european);
        return new NativePurchaseQuote(carrier.getId(),
                settlement.getLocation(), goods, quantity,
                NativeMarketRules.purchaseTotal(quantity, ask,
                        haggle == NativeSaleQuote.Haggle.ACCEPTED),
                haggle, settlement.getStock(goods), settlement.getTreasury(),
                carrier.getOwner().getGold(), cargoRoom(carrier, goods), ask,
                nativeRelations.getHostility(settlement, european),
                settlement.findTrade(european).orElseThrow().getRevision(),
                expedition.getPurchaseRevision().orElseThrow());
    }

    /**
     * @return Europe ask of one piece of the goods, the price the natives
     *         charge for a purchase; no tax applies
     */
    private int askPrice(final GoodsType goods, final EuropeanNation european) {
        return getEurope().getGoodsTradeForType(goods, european).getBuyPrice();
    }

    /**
     * @return pieces of one goods type that the carrier's slots can still take:
     *         the room of empty slots and of slots already holding that type;
     *         zero for every other slot and for a passenger's slot. Pure read.
     */
    private static int cargoRoom(final Unit carrier, final GoodsType goods) {
        if (!(carrier instanceof UnitWithCargo)) {
            return 0;
        }
        int room = 0;
        for (final CargoSlot slot : ((UnitWithCargo) carrier).getCargo()
                .getSlots()) {
            if (slot.getGoods().map(held -> held.getType().equals(goods))
                    .orElse(slot.isEmpty())) {
                room += slot.getAvailableCapacity();
            }
        }
        return room;
    }

    /**
     * Put goods into the carrier through the cargo's own placement: the slots
     * already holding this type first, then the empty ones, in slot order. The
     * caller has checked the room.
     */
    private static void storeGoods(final Unit carrier, final GoodsType goods,
            final int amount) {
        int left = amount;
        final List<CargoSlot> slots = ((UnitWithCargo) carrier).getCargo()
                .getSlots();
        for (final CargoSlot slot : slots) {
            if (slot.getGoods().map(held -> held.getType().equals(goods))
                    .orElse(false)) {
                left = addToSlot(slot, goods, left);
            }
        }
        for (final CargoSlot slot : slots) {
            if (slot.isEmpty()) {
                left = addToSlot(slot, goods, left);
            }
        }
    }

    /**
     * @return pieces still to store after this slot took what it can
     */
    private static int addToSlot(final CargoSlot slot, final GoodsType goods,
            final int left) {
        final int taken = Math.min(left, slot.getAvailableCapacity());
        if (taken > 0) {
            slot.addGoods(Goods.of(goods, taken));
        }
        return left - taken;
    }

    /**
     * @return the open expedition when the market is open to this carrier for
     *         sale or gift now: a legal visit, admission, an undamaged carrier
     *         and an expedition open at its square; empty otherwise
     */
    private Optional<NativeExpedition> openSale(final Unit carrier,
            final NativeSettlement settlement) {
        if (!isEntryAllowed(carrier, settlement)) {
            return Optional.empty();
        }
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        if (admissionOf(carrier, settlement,
                nativeContacts.isContacted(european, settlement
                        .getNation())) != NativeMarketView.Admission.ADMITTED) {
            return Optional.empty();
        }
        if (carrier instanceof Ship && ((Ship) carrier).isDamaged()) {
            return Optional.empty();
        }
        return settlement.findTrade(european)
                .flatMap(state -> state.openExpedition(carrier));
    }

    private NativeSaleQuote saleQuote(final Unit carrier,
            final NativeSettlement settlement, final GoodsType goods,
            final int quantity, final boolean gift) {
        final EuropeanNation european = nativeEuropean(carrier.getOwner());
        final NativeExpedition expedition = openSale(carrier, settlement)
                .orElseThrow(() -> new IllegalStateException(
                        "Native trade is not open to this unit"));
        final NativeSaleQuote.Haggle haggle = haggleOf(expedition, goods, gift);
        final int bid = europeBid(goods, european);
        final boolean wanted = settlement.getWantedGoods().contains(goods);
        final boolean ship = carrier.getType().isShip();
        final int gold = gift ? 0
                : NativeMarketRules.saleTotal(quantity, bid, wanted, ship,
                        haggle == NativeSaleQuote.Haggle.ACCEPTED);
        final NativeTradeState trade = settlement.findTrade(european)
                .orElseThrow();
        return new NativeSaleQuote(carrier.getId(), settlement.getLocation(),
                goods, quantity, gift, gold, haggle,
                cargoAmount(carrier, goods), settlement.getTreasury(), bid,
                wanted, ship,
                nativeRelations.getHostility(settlement, european),
                settlement.getReceiveRoom(goods), trade.getRevision());
    }

    /** @return the haggle state of goods in this expedition */
    private static NativeSaleQuote.Haggle haggleOf(
            final NativeExpedition expedition, final GoodsType goods,
            final boolean gift) {
        if (gift) {
            return NativeSaleQuote.Haggle.UNAVAILABLE;
        }
        final Optional<GoodsType> haggled = expedition.getHaggledGoods();
        if (haggled.isEmpty()) {
            return NativeSaleQuote.Haggle.OFFERED;
        }
        if (!haggled.get().equals(goods)) {
            return NativeSaleQuote.Haggle.UNAVAILABLE;
        }
        return expedition.isHaggleAccepted() ? NativeSaleQuote.Haggle.ACCEPTED
                : NativeSaleQuote.Haggle.REFUSED;
    }

    /**
     * @return Europe bid of the goods in the nation's market, the price the
     *         settlement's base is taken from
     */
    private int europeBid(final GoodsType goods,
            final EuropeanNation european) {
        return getEurope().getGoodsTradeForType(goods, european).getSellPrice();
    }

    /**
     * @return pieces of one good that the unit holds in its goods slots
     */
    private static int cargoAmount(final Unit unit, final GoodsType goods) {
        if (!(unit instanceof UnitWithCargo)) {
            return 0;
        }
        return ((UnitWithCargo) unit).getCargo().getSlots().stream()
                .flatMap(slot -> slot.getGoods()
                        .filter(held -> held.getType().equals(goods)).stream())
                .mapToInt(Goods::getAmount).sum();
    }

    /**
     * Take exactly this many pieces from the goods slots of the carrier, in
     * slot order. The caller has checked that they are carried.
     */
    private static void removeCargo(final Unit carrier, final GoodsType goods,
            final int amount) {
        int left = amount;
        for (final CargoSlot slot : ((UnitWithCargo) carrier).getCargo()
                .getSlots()) {
            final Optional<Goods> held = slot.getGoods();
            if (left > 0 && held.isPresent()
                    && held.get().getType().equals(goods)) {
                final int taken = Math.min(left, held.get().getAmount());
                slot.removeCargo(Goods.of(goods, taken));
                left -= taken;
            }
        }
    }

    /**
     * Terms of a sale to an adjacent foreign colony (R-0167): the carrier gives
     * the goods and the colony owner pays their Europe bid, with no visitor
     * tax. Pure read.
     *
     * @param visitor required wagon or ship beside the colony
     * @param host    required foreign colony
     * @param goods   required goods the carrier gives
     * @return the quote; its problem says why the sale is not legal now
     */
    public ForeignColonyTradeQuote quoteForeignColonySale(final Unit visitor,
            final Colony host, final Goods goods) {
        Preconditions.checkNotNull(goods);
        return quoteForeignColony(visitor, host, goods, null);
    }

    /**
     * Terms of a barter with an adjacent foreign colony (R-0167): one incoming
     * commodity for one outgoing commodity, valued at the colony owner's Europe
     * bid and ask. The net gold is paid by the side that gains the difference.
     * Pure read.
     *
     * @param visitor  required wagon or ship beside the colony
     * @param host     required foreign colony
     * @param incoming required goods the carrier gives
     * @param outgoing required goods the carrier receives
     * @return the quote; its problem says why the barter is not legal now
     */
    public ForeignColonyTradeQuote quoteForeignColonyBarter(final Unit visitor,
            final Colony host, final Goods incoming, final Goods outgoing) {
        Preconditions.checkNotNull(incoming);
        Preconditions.checkNotNull(outgoing);
        return quoteForeignColony(visitor, host, incoming, outgoing);
    }

    /**
     * Access to trade with a foreign colony: the running game, the current
     * colonial owner, an adjacent visible colony of another colonial nation,
     * Jan de Witt in the visitor's Congress, peace or alliance, and a loaded
     * undamaged carrier. Rebellion does not affect this; Europe closure is not
     * part of it. Pure read.
     *
     * @param visitor required unit
     * @param host    required colony
     * @return the first reason access is refused, or NONE when it is open
     */
    public ForeignColonyTradeProblem getForeignColonyTradeAccess(
            final Unit visitor, final Colony host) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(host);
        return foreignColonyAccess(visitor, host);
    }

    /**
     * Most pieces of one commodity that a foreign trade may give. The limit is
     * the carried amount and the colony's free storage; the outgoing side uses
     * {@link #getForeignColonyReceiveLimit}, because its room depends on the
     * goods given in the same barter. Zero when access is refused. Pure read.
     *
     * @param visitor  required unit
     * @param host     required colony
     * @param goods    required commodity
     * @param incoming whether the carrier gives the goods; the stock of the
     *                 outgoing side is returned for {@code false}
     * @return the largest legal quantity, zero when none is legal
     */
    public int getForeignColonyTradeLimit(final Unit visitor, final Colony host,
            final GoodsType goods, final boolean incoming) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(host);
        Preconditions.checkNotNull(goods);
        if (foreignColonyAccess(visitor,
                host) != ForeignColonyTradeProblem.NONE) {
            return 0;
        }
        if (incoming) {
            return Math.min(cargoAmount(visitor, goods),
                    host.getWarehouse().getFreeStorage(goods));
        }
        return host.getWarehouse().getGoods(goods).getAmount();
    }

    /**
     * Most pieces of an outgoing commodity that a barter may hand over, given
     * the incoming goods of the same barter (R-0167). The carrier room counts
     * the holds that the given goods free; the colony stock limits the rest.
     * Zero when access is refused. Pure read.
     *
     * @param visitor  required unit
     * @param host     required colony
     * @param given    required incoming goods and their amount
     * @param received required outgoing commodity
     * @return the largest legal quantity, zero when none is legal
     */
    public int getForeignColonyReceiveLimit(final Unit visitor,
            final Colony host, final Goods given, final GoodsType received) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(host);
        Preconditions.checkNotNull(given);
        Preconditions.checkNotNull(received);
        if (foreignColonyAccess(visitor,
                host) != ForeignColonyTradeProblem.NONE) {
            return 0;
        }
        return Math.min(host.getWarehouse().getGoods(received).getAmount(),
                roomAfterGiving(visitor, given.getType(), given.getAmount(),
                        received));
    }

    /**
     * Pieces of a commodity in the colony warehouse (R-0167). Pure read.
     *
     * @param host  required colony
     * @param goods required commodity
     * @return the stock, zero when the warehouse holds none
     */
    public int getForeignColonyStock(final Colony host, final GoodsType goods) {
        Preconditions.checkNotNull(host);
        Preconditions.checkNotNull(goods);
        return host.getWarehouse().getGoods(goods).getAmount();
    }

    /**
     * Trade with an adjacent foreign colony on the terms the player confirmed
     * (R-0167). The live quote must equal the confirmed one and be legal; every
     * check runs before the first change. Then the incoming goods leave the
     * carrier for the colony warehouse, the outgoing goods enter the carrier,
     * and the net gold moves once between the two treasuries. The colony is
     * never entered or captured. A repeated confirmation fails, because the
     * carrier and treasuries have already changed.
     *
     * @param visitor required wagon or ship beside the colony
     * @param host    required foreign colony
     * @param offered required terms the player confirmed
     * @throws ForeignColonyTermsChangedException when the terms no longer hold;
     *                                            nothing was traded
     */
    public void tradeWithForeignColony(final Unit visitor, final Colony host,
            final ForeignColonyTradeQuote offered) {
        Preconditions.checkNotNull(offered);
        checkTurnPreparationComplete();
        final ForeignColonyTradeQuote live = quoteForeignColony(visitor, host,
                offered.getIncoming(), offered.getOutgoing());
        if (!live.isLegal() || !live.equals(offered)) {
            throw new ForeignColonyTermsChangedException(
                    "Foreign colony terms changed; nothing was traded");
        }
        final Goods incoming = live.getIncoming();
        final Goods outgoing = live.getOutgoing();
        final Player owner = visitor.getOwner();
        final Player hostOwner = host.getOwner();
        removeCargo(visitor, incoming.getType(), incoming.getAmount());
        host.getWarehouse().addGoods(incoming);
        if (outgoing != null) {
            host.getWarehouse().removeGoods(outgoing);
            storeGoods(visitor, outgoing.getType(), outgoing.getAmount());
        }
        owner.setGold(owner.getGold() + live.getNetGold());
        hostOwner.setGold(hostOwner.getGold() - live.getNetGold());
    }

    /**
     * Free room for the received goods once the given goods have left the
     * carrier (R-0167). The slots are walked in the order the removal empties
     * them, so the count matches the placement that follows. Unit-held and
     * reserved holds, such as a treasure train's six holds, never count. Partly
     * emptied slots still hold the given goods and count nothing. Pure read:
     * the carrier is not changed.
     */
    private static int roomAfterGiving(final Unit carrier,
            final GoodsType given, final int amount, final GoodsType received) {
        int left = amount;
        int room = 0;
        for (final CargoSlot slot : ((UnitWithCargo) carrier).getCargo()
                .getSlots()) {
            if (slot.isLoadedUnit() || slot.isReservedHold()) {
                continue;
            }
            final Optional<Goods> held = slot.getGoods();
            if (held.isEmpty()) {
                room += CargoSlot.MAX_CARGO_SLOT_CAPACITY;
            } else if (held.get().getType().equals(given)) {
                final int taken = Math.min(left, held.get().getAmount());
                left -= taken;
                if (taken == held.get().getAmount()) {
                    room += CargoSlot.MAX_CARGO_SLOT_CAPACITY;
                }
            } else if (held.get().getType().equals(received)) {
                room += CargoSlot.MAX_CARGO_SLOT_CAPACITY
                        - held.get().getAmount();
            }
        }
        return room;
    }

    /**
     * Quote one sale or barter. The access comes first; refused access returns
     * the quote with that problem and zero values. Otherwise the goods, the
     * warehouse, the carrier room and both treasuries are compared before the
     * net gold is accepted.
     */
    private ForeignColonyTradeQuote quoteForeignColony(final Unit visitor,
            final Colony host, final Goods incoming, final Goods outgoing) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(host);
        Preconditions.checkNotNull(incoming);
        final ForeignColonyTradeProblem access = foreignColonyAccess(visitor,
                host);
        if (access != ForeignColonyTradeProblem.NONE) {
            return new ForeignColonyTradeQuote(visitor.getId(), host.getId(),
                    incoming, outgoing, access, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        }
        final Player owner = visitor.getOwner();
        final Player hostOwner = host.getOwner();
        final EuropeanNation european = hostOwner.getEuropeanNation();
        final int bid = getEurope()
                .getGoodsTradeForType(incoming.getType(), european)
                .getSellPrice();
        final int ask = outgoing == null ? 0
                : getEurope().getGoodsTradeForType(outgoing.getType(), european)
                        .getBuyPrice();
        final int carried = cargoAmount(visitor, incoming.getType());
        final int hostFree = host.getWarehouse()
                .getFreeStorage(incoming.getType());
        final int hostStock = outgoing == null ? 0
                : host.getWarehouse().getGoods(outgoing.getType()).getAmount();
        final int room = outgoing == null ? 0
                : roomAfterGiving(visitor, incoming.getType(),
                        incoming.getAmount(), outgoing.getType());
        // Positive when the colony owner pays the visitor; never includes tax.
        final long net = (long) incoming.getAmount() * bid
                - (outgoing == null ? 0L : (long) outgoing.getAmount() * ask);
        final ForeignColonyTradeProblem problem = foreignColonyProblem(incoming,
                outgoing, carried, hostFree, hostStock, room, owner.getGold(),
                hostOwner.getGold(), net);
        return new ForeignColonyTradeQuote(visitor.getId(), host.getId(),
                incoming, outgoing, problem, bid, ask,
                problem == ForeignColonyTradeProblem.NONE ? (int) net : 0,
                owner.getGold(), hostOwner.getGold(), carried, hostFree,
                hostStock, room);
    }

    private ForeignColonyTradeProblem foreignColonyAccess(final Unit visitor,
            final Colony host) {
        final Player owner = visitor.getOwner();
        final Player hostOwner = host.getOwner();
        if (!isGameRunning() || !(visitor instanceof UnitWithCargo)
                || getCurrentPlayer() != owner || !owner.isColonialNation()
                || !hostOwner.isColonialNation() || owner == hostOwner
                || !getPlayers().contains(owner)
                || !getPlayers().contains(hostOwner)
                || tryGetUnitById(visitor.getId()).orElse(null) != visitor
                || !visitor.isAtPlaceLocation()
                || getColonyAt(host.getLocation()).orElse(null) != host
                || !visitor.getLocation().isNeighbor(host.getLocation())
                || !owner.isVisible(host.getLocation())) {
            return ForeignColonyTradeProblem.NO_ACCESS;
        }
        if (!owner.getCongressMembers().contains(Founder.JAN_DE_WITT)) {
            return ForeignColonyTradeProblem.FOUNDER_ABSENT;
        }
        final EuropeanRelationState relation = getEuropeanRelation(owner,
                hostOwner);
        if (relation != EuropeanRelationState.PEACE
                && relation != EuropeanRelationState.ALLIANCE) {
            return ForeignColonyTradeProblem.AT_WAR;
        }
        if (visitor instanceof Ship && ((Ship) visitor).isDamaged()) {
            return ForeignColonyTradeProblem.DAMAGED_CARRIER;
        }
        if (!hasGoodsAboard((UnitWithCargo) visitor)) {
            return ForeignColonyTradeProblem.EMPTY_CARGO;
        }
        return ForeignColonyTradeProblem.NONE;
    }

    private static ForeignColonyTradeProblem foreignColonyProblem(
            final Goods incoming, final Goods outgoing, final int carried,
            final int hostFree, final int hostStock, final int room,
            final int visitorGold, final int hostGold, final long net) {
        if (incoming.getAmount() <= 0
                || outgoing != null && outgoing.getAmount() <= 0) {
            return ForeignColonyTradeProblem.INVALID_QUANTITY;
        }
        if (outgoing != null && outgoing.getType().equals(incoming.getType())) {
            return ForeignColonyTradeProblem.SAME_GOODS;
        }
        if (carried < incoming.getAmount()) {
            return ForeignColonyTradeProblem.INVALID_QUANTITY;
        }
        if (hostFree < incoming.getAmount()) {
            return ForeignColonyTradeProblem.HOST_CAPACITY;
        }
        if (outgoing != null && hostStock < outgoing.getAmount()) {
            return ForeignColonyTradeProblem.HOST_STOCK;
        }
        // The room already counts the slots that the incoming goods free.
        if (outgoing != null && room < outgoing.getAmount()) {
            return ForeignColonyTradeProblem.CARRIER_CAPACITY;
        }
        if (net > hostGold) {
            return ForeignColonyTradeProblem.HOST_GOLD;
        }
        if (-net > visitorGold) {
            return ForeignColonyTradeProblem.VISITOR_GOLD;
        }
        if (hostGold - net > Integer.MAX_VALUE) {
            return ForeignColonyTradeProblem.HOST_GOLD;
        }
        if (visitorGold + net > Integer.MAX_VALUE) {
            return ForeignColonyTradeProblem.VISITOR_GOLD;
        }
        return ForeignColonyTradeProblem.NONE;
    }

    private static boolean hasGoodsAboard(final UnitWithCargo carrier) {
        return carrier.getCargo().getSlots().stream()
                .anyMatch(slot -> slot.getGoods().isPresent());
    }

    private static boolean addsFit(final int gold, final int received) {
        try {
            Math.addExact(gold, received);
            return true;
        } catch (final ArithmeticException overflow) {
            return false;
        }
    }

    /**
     * @param unit  required unit
     * @param goods required goods type
     * @return whether a goods slot of the unit holds that goods type
     */
    private static boolean carries(final Unit unit, final GoodsType goods) {
        return unit instanceof UnitWithCargo
                && ((UnitWithCargo) unit).getCargo().getSlots().stream()
                        .anyMatch(slot -> slot.getGoods()
                                .filter(held -> held.getType().equals(goods))
                                .isPresent());
    }

    /**
     * Whether an expedition is open for its carrier now. Pure read, used when
     * the settlements are saved: a carrier that left and came back is not open.
     */
    private boolean isOpenNativeExpedition(final NativeExpedition expedition) {
        return tryGetUnitById(expedition.getCarrierId())
                .filter(expedition::isOpenFor).isPresent();
    }

    /**
     * Admission gates in their fixed order: the carrier role, prior contact,
     * loaded goods, a coastal ship, then the attitude. Pure read.
     */
    private NativeMarketView.Admission admissionOf(final Unit visitor,
            final NativeSettlement settlement, final boolean contacted) {
        return admissionOf(visitor, settlement, contacted, true);
    }

    /**
     * Admission gates of {@link #admissionOf(Unit, NativeSettlement, boolean)}.
     * A second stage (R-0161) passes with the goods gate skipped, since the
     * carrier may have sold everything; every other gate still applies.
     *
     * @param goodsRequired whether the carrier must hold loaded goods now
     */
    private NativeMarketView.Admission admissionOf(final Unit visitor,
            final NativeSettlement settlement, final boolean contacted,
            final boolean goodsRequired) {
        final boolean ship = visitor.getType().isShip();
        if (!ship && visitor.getType() != UnitType.WAGON) {
            return NativeMarketView.Admission.NOT_A_TRADER;
        }
        if (!contacted) {
            return NativeMarketView.Admission.NO_CONTACT;
        }
        if (goodsRequired && !hasLoadedGoods(visitor)) {
            return NativeMarketView.Admission.NO_GOODS;
        }
        if (ship && !isCoastalTrade(visitor, settlement)) {
            return NativeMarketView.Admission.SHIP_NOT_COASTAL;
        }
        final NativeHostility hostility = getNativeHostility(visitor.getOwner(),
                settlement);
        if (hostility == NativeHostility.ANGRY
                || hostility == NativeHostility.HOSTILE) {
            return NativeMarketView.Admission.RELATION_TENSE;
        }
        if (ship && hostility == NativeHostility.UNEASY) {
            return NativeMarketView.Admission.SHIP_RELATION_UNEASY;
        }
        return NativeMarketView.Admission.ADMITTED;
    }

    /**
     * @param unit required unit
     * @return whether it carries at least one goods slot; passengers alone do
     *         not
     */
    private static boolean hasLoadedGoods(final Unit unit) {
        return unit instanceof UnitWithCargo
                && ((UnitWithCargo) unit).getCargo().getSlots().stream()
                        .anyMatch(CargoSlot::isLoadedGood);
    }

    /**
     * A ship trades only from water beside a settlement that has water beside
     * it.
     */
    private boolean isCoastalTrade(final Unit ship,
            final NativeSettlement settlement) {
        return getMap().getTerrainTypeAt(ship.getLocation()).isSea()
                && settlement.getLocation().getNeighbors().stream()
                        .anyMatch(at -> getMap().isValid(at)
                                && getMap().getTerrainTypeAt(at).isSea());
    }

    private NativeMarketView.Market marketOf(final Unit visitor,
            final NativeSettlement settlement) {
        final List<NativeMarketView.Offer> offers = new ArrayList<>();
        for (final GoodsType goods : settlement.getOfferedGoods()) {
            offers.add(new NativeMarketView.Offer(goods,
                    settlement.getStock(goods)));
        }
        final List<GoodsType> tradable = new ArrayList<>(
                GoodsType.BUYABLE_GOOD_TYPES);
        tradable.sort(Comparator.comparing(GoodsType::name));
        final List<NativeMarketView.Carried> carried = new ArrayList<>();
        for (final GoodsType goods : tradable) {
            final int amount = cargoAmount(visitor, goods);
            if (amount > 0) {
                carried.add(new NativeMarketView.Carried(goods, amount));
            }
        }
        // The offer a drawn haggle was made for is kept with the open
        // expedition, so a reopened chooser shows the same offer.
        final Optional<NativeExpedition> open = settlement
                .findTrade(nativeEuropean(visitor.getOwner()))
                .flatMap(state -> state.openExpedition(visitor));
        return new NativeMarketView.Market(offers, settlement.getWantedGoods(),
                settlement.getTreasury(), carried,
                open.flatMap(NativeExpedition::getHaggledGoods).orElse(null),
                open.flatMap(NativeExpedition::getHaggledQuantity).orElse(0));
    }

    /**
     * Restore the expeditions of a loaded save (R-0159). An expedition whose
     * carrier is gone, or no longer stands on its anchor, has already ended and
     * is dropped. A carrier of another nation is refused.
     */
    void validateSavedNativeMarkets() {
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            for (final Map.Entry<EuropeanNation, NativeTradeState> entry : settlement
                    .getTrades().entrySet()) {
                final NativeTradeState state = entry.getValue();
                for (final NativeExpedition expedition : List
                        .copyOf(state.getExpeditions().values())) {
                    final Optional<Unit> carrier = tryGetUnitById(
                            expedition.getCarrierId());
                    if (carrier.isPresent()) {
                        Preconditions.checkArgument(
                                nativeNationOf(carrier.get().getOwner())
                                        .equals(Optional.of(entry.getKey())),
                                "Unsupported save: native expedition belongs to another nation");
                        Preconditions.checkArgument(
                                carrier.get().getType() == UnitType.WAGON
                                        || carrier.get().getType().isShip(),
                                "Unsupported save: native expedition carrier is not a wagon or ship");
                    }
                    if (carrier.isEmpty() || !carrier.get().isAtPlaceLocation()
                            || !carrier.get().getLocation()
                                    .equals(expedition.getAnchor())) {
                        state.removeExpedition(expedition.getCarrierId());
                    } else {
                        expedition.bindPlace(carrier.get().getPlace());
                    }
                }
            }
        }
    }

    /**
     * One owner update of a native nation: its settlements gain treasury and
     * restock their goods once. Called from the nation's own preparation only.
     *
     * @param nation required native nation whose update has run
     */
    void updateNativeMarkets(final NativeNation nation) {
        Preconditions.checkNotNull(nation);
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            if (settlement.getNation() == nation) {
                settlement.applyOwnerUpdate();
            }
        }
    }

    /**
     * Start learning the settlement's offered skill (R-0148). The visit chooser
     * must offer teaching now, and the offer must still be the settlement's
     * current skill. The colonist leaves the map for the native course at once,
     * with its identity and equipment; the skill comes after the required owner
     * updates. Every check runs before the first change.
     *
     * @param visitor      required colonist adjacent to the settlement
     * @param settlement   required native settlement
     * @param offeredSkill required skill the chooser displayed
     * @throws IllegalStateException when teaching is not available now or the
     *                               offer went stale
     */
    public void startNativeTeaching(final Unit visitor,
            final NativeSettlement settlement, final UnitType offeredSkill) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(offeredSkill);
        checkTurnPreparationComplete();
        final boolean available = getNativeVisitChoices(visitor, settlement)
                .stream()
                .anyMatch(choice -> choice
                        .getAction() == NativeVisitAction.TEACHING
                        && choice
                                .getLimit() == NativeVisitChoice.Limit.AVAILABLE);
        Preconditions.checkState(available,
                "Native teaching is not available for this visitor");
        Preconditions.checkState(settlement.getTeachableSkill() == offeredSkill,
                "Native teaching offer changed");
        Preconditions.checkState(
                nativeTeachingRoleAllows(visitor)
                        && visitor.isAtPlaceLocation(),
                "Native teaching needs an unskilled colonist on the map");
        ((AbstractUnit) visitor).beginNativeCourse(settlement.getId());
        settlement.startTeaching(visitor.getId());
    }

    /**
     * Cancel the native course of a colonist by its own choice (R-0148). The
     * colonist returns to a legal adjacent square and keeps its identity, but
     * no skill is granted and the progress is discarded.
     *
     * @param pupil required colonist learning at a native settlement
     * @throws IllegalStateException when the course is not the current player's
     *                               or no legal square is free to return to
     */
    public void cancelNativeTeaching(final Unit pupil) {
        Preconditions.checkNotNull(pupil);
        checkTurnPreparationComplete();
        final NativeSettlement settlement = getNativeTeachingSettlement(pupil)
                .orElseThrow(() -> new IllegalStateException(
                        "Unit is not in a native course"));
        Preconditions.checkState(
                isGameRunning() && getCurrentPlayer() == pupil.getOwner(),
                "Native teaching is unavailable on this turn");
        final Location square = findNativeReleaseSquare(pupil, settlement)
                .orElseThrow(() -> new IllegalStateException(
                        "No free square to leave the native settlement"));
        releaseNativePupil(pupil, square);
        settlement.clearTeaching();
    }

    /**
     * @param settlement required native settlement
     * @return the course the settlement holds, empty when its place is free
     */
    public Optional<NativeTeachingCourse> getNativeTeachingCourse(
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(settlement);
        return settlement.getTeachingPupilId()
                .map(pupilId -> new NativeTeachingCourse(pupilId,
                        settlement.getTeachableSkill(),
                        settlement.getTeachingUpdates(),
                        settlement.getTeachingStage().orElseThrow()));
    }

    /**
     * Whether a finished or canceled colonist, or a learner who gives up, has a
     * legal square to leave the settlement now. Pure read; the chooser and the
     * settlement details use it before they offer a leave.
     *
     * @param pupil required unit
     * @return whether a square is free to leave to
     */
    public boolean hasNativeTeachingExit(final Unit pupil) {
        Preconditions.checkNotNull(pupil);
        return getNativeTeachingSettlement(pupil).flatMap(
                settlement -> findNativeReleaseSquare(pupil, settlement))
                .isPresent();
    }

    /**
     * @param pupil required unit
     * @return the settlement whose course holds this unit, if any
     */
    public Optional<NativeSettlement> getNativeTeachingSettlement(
            final Unit pupil) {
        Preconditions.checkNotNull(pupil);
        if (!pupil.isAtPlaceNativeCourse()) {
            return Optional.empty();
        }
        final int settlementId = ((PlaceNativeCourse) pupil.getPlace())
                .getSettlementId();
        return nativeSettlements.getAll().stream()
                .filter(settlement -> settlement.getId() == settlementId)
                .findAny();
    }

    /**
     * @param visitor required unit
     * @return whether the unit's type may learn a native skill: an unskilled
     *         free colonist or an indentured servant; criminals and specialists
     *         may not
     */
    private static boolean nativeTeachingRoleAllows(final Unit visitor) {
        return visitor.getType() == UnitType.COLONIST
                || visitor.getType() == UnitType.INDENTURED_SERVANT;
    }

    /**
     * First legal square around the settlement, in the fixed direction order. A
     * square is legal for the pupil's type when ordinary entry would be
     * allowed, so it never takes a hostile or blocked square.
     *
     * @param pupil      required pupil
     * @param settlement required native settlement
     * @return the release square, empty when every neighbour is blocked
     */
    private Optional<Location> findNativeReleaseSquare(final Unit pupil,
            final NativeSettlement settlement) {
        for (final Direction direction : Direction.values()) {
            final Location square = settlement.getLocation()
                    .add(direction.getVector());
            if (MovementPolicy.isPossibleToEnter(this, pupil.getType(), false,
                    pupil.getOwner(), square, false)) {
                return Optional.of(square);
            }
        }
        return Optional.empty();
    }

    /**
     * Put the pupil back on the map at a square already validated by
     * {@link #findNativeReleaseSquare}; the course stays with the caller.
     */
    private static void releaseNativePupil(final Unit pupil,
            final Location square) {
        pupil.placeToLocation(square);
    }

    /**
     * Terms of an attack on a native settlement that the visitor may confirm
     * now (R-0174). Pure read: nothing is drawn, moved, paid or recorded, so
     * cancelling the confirmation changes nothing. The attacker must be an
     * armed soldier, dragoon, scout or artillery on turn with its movement
     * unspent; the settlement must be known and adjacent; and every unit on its
     * square must be an actual native brave of its nation. Spain's bonus is
     * named here and applies to this settlement attack only.
     *
     * @param attacker   required unit that would attack
     * @param settlement required native settlement
     * @return the terms, empty when no attack is legal now
     */
    public Optional<NativeConquestQuote> getNativeConquestQuote(
            final Unit attacker, final NativeSettlement settlement) {
        Preconditions.checkNotNull(attacker);
        Preconditions.checkNotNull(settlement);
        if (!isEntryAllowed(attacker, settlement) || !isConquestRole(attacker)
                || !isReadyForAttack(attacker)) {
            return Optional.empty();
        }
        final Player owner = attacker.getOwner();
        final Location at = settlement.getLocation();
        if (!owner.isVisible(at) || !owner.isCurrentlyVisible(at) || !attacker
                .getType().canMoveAtTerrain(map.getTerrainTypeAt(at))) {
            return Optional.empty();
        }
        final List<Unit> onSquare = getUnitsAt(at);
        if (onSquare.isEmpty() || onSquare.stream()
                .anyMatch(unit -> !isNativeDefender(unit, settlement))) {
            return Optional.empty();
        }
        final int defenseBonus = settlement.getDefenseBonusPercent();
        final int spanishBonus = settlementAttackBonus(owner);
        final Map<Integer, CombatStrengths> battles = new HashMap<>();
        for (final Unit defender : onSquare) {
            final Optional<CombatStrengths> strengths = CombatStrengths
                    .forSettlementBattle(this, attacker, defender, defenseBonus,
                            spanishBonus);
            if (strengths.isEmpty()) {
                return Optional.empty();
            }
            battles.put(defender.getId(), strengths.get());
        }
        // Strongest defender first, as the battle selection of the attacker.
        final List<Unit> ordered = onSquare.stream()
                .sorted(Comparator
                        .comparingDouble((Unit defender) -> battles
                                .get(defender.getId()).getDefender().getTotal())
                        .reversed().thenComparingInt(Unit::getId))
                .collect(Collectors.toList());
        final List<CombatStrengths> frozen = ordered.stream()
                .map(defender -> battles.get(defender.getId()))
                .collect(Collectors.toList());
        return Optional.of(new NativeConquestQuote(settlement, attacker,
                ordered, frozen, defenseBonus, spanishBonus,
                getNativeHostility(owner, settlement)));
    }

    /**
     * Attack a native settlement with the terms the player confirmed (R-0174).
     * The live terms must equal the shown ones before the first draw. Each
     * actual defender then fights once, strongest first; a lost battle ends the
     * attack and the settlement stays. Overcoming every defender conquers it
     * exactly once: the attacker enters its square, the relations take the
     * destruction raise, and the settlement with its claim, map interaction and
     * learned identity is retired. Attacks that fought at all raise relations.
     * A conquest then makes its single treasure draw and, when a train is left,
     * creates it on the square in this same call (R-0175); a loss draws
     * nothing.
     *
     * @param attacker   required unit that confirmed the attack
     * @param settlement required native settlement
     * @param shown      required terms the player was shown and confirmed
     * @return the actual result of the attack
     * @throws IllegalStateException when the attack is not legal now or the
     *                               shown terms went stale
     */
    public NativeConquestResult attackNativeSettlement(final Unit attacker,
            final NativeSettlement settlement,
            final NativeConquestQuote shown) {
        Preconditions.checkNotNull(attacker);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        final Optional<NativeConquestQuote> current = getNativeConquestQuote(
                attacker, settlement);
        Preconditions.checkState(
                current.isPresent() && current.get().equals(shown),
                "Native attack terms changed");
        // A conquest may create its treasure train, so the unit ID is checked
        // before the first draw; a refused attack then changes nothing.
        Preconditions.checkState(unitStorage.canAllocateUnit(),
                "No unit IDs remain");
        final Player owner = attacker.getOwner();
        // The destruction count is checked here too, before any battle, so a
        // full count refuses the attack with nothing changed (R-0196).
        owner.checkNativeSettlementDestroyedCount();
        final AbstractUnit unit = (AbstractUnit) attacker;
        // Everything the aftermath needs is resolved before the first draw, so
        // a broken course reference refuses the attack with nothing changed.
        final List<Unit> courseUnits = nativeCourseUnits(settlement);
        // The battles use the strengths the player confirmed. A promotion won
        // in an earlier battle is the ordinary per-battle rule and does not
        // change the terms of the later battles of this one attack.
        int defeated = 0;
        boolean attackerLost = false;
        MilitaryPromotion promotion = null;
        for (int i = 0; i < shown.getDefenders().size() && !attackerLost; i++) {
            final AbstractUnit.NativeBattle battle = unit.fightNativeDefender(
                    shown.getDefenders().get(i), shown.getBattles().get(i));
            if (battle.isWon()) {
                defeated++;
            } else {
                attackerLost = true;
            }
            if (promotion == null) {
                promotion = battle.getPromotion();
            }
        }
        if (attackerLost) {
            applyNativeRelationOutcome(owner,
                    NativeRelationOutcome.settlementAttack(
                            nativeRelations.nextOutcomeId(), settlement,
                            false));
            // A promotion won earlier stays with a survivor, who keeps it
            // after losing its equipment; a destroyed unit has none to show.
            return new NativeConquestResult(false, defeated, true,
                    isUnitInGame(attacker) ? promotion : null, List.of(), null);
        }
        // Every defender is overcome: the destruction raise applies once, with
        // the capital factor, while the settlement is still registered.
        applyNativeRelationOutcome(owner,
                NativeRelationOutcome.settlementAttack(
                        nativeRelations.nextOutcomeId(), settlement, true));
        unit.occupyNativeSquare(settlement.getLocation());
        final List<NativeConquestResult.Loss> losses = conquerNativeSettlement(
                settlement, courseUnits);
        // One durable record of the conquest, then the single treasure draw of
        // this attack (R-0175). It follows every battle draw and is taken only
        // here, so a lost or refused attack never takes it.
        getTurnEventStore().add(TurnEventProvider.getNativeConquest(owner,
                settlement, defeated));
        // The conqueror's own count is durable too; turn events are cleared at
        // the end of the turn, so the score cannot read them later (R-0196).
        owner.markNativeSettlementDestroyed();
        final Location at = settlement.getLocation();
        final Optional<Integer> treasure = NativeTreasureRules.value(
                settlement.isCapital(),
                owner.getCongressMembers().contains(Founder.HERNAN_CORTES),
                this::nextCombatRandom);
        if (treasure.isPresent()) {
            final Unit train = unitStorage.createTreasure(this,
                    created -> new PlaceLocation(created, at,
                            created.getDefaultOrintation()),
                    owner, treasure.get());
            // Announced like any unit placed on a square, so the map shows it.
            fireUnitMovedToLocation(train);
        }
        return new NativeConquestResult(true, defeated, false, promotion,
                losses, treasure.orElse(null));
    }

    /**
     * The pupil and the missionary of a settlement, resolved and checked
     * against their places. A missing or misplaced unit means the saved state
     * is inconsistent and the conquest is refused before it changes anything.
     */
    private List<Unit> nativeCourseUnits(final NativeSettlement settlement) {
        final List<Unit> out = new ArrayList<>();
        if (settlement.getTeachingPupilId().isPresent()) {
            final Unit pupil = tryGetUnitById(
                    settlement.getTeachingPupilId().get())
                    .filter(unit -> isLearningAt(unit, settlement))
                    .orElseThrow(() -> new IllegalStateException(
                            "Native course unit is missing"));
            out.add(pupil);
        }
        if (settlement.getMissionaryId().isPresent()) {
            final Unit missionary = tryGetUnitById(
                    settlement.getMissionaryId().get())
                    .filter(unit -> isStationedAt(unit, settlement))
                    .orElseThrow(() -> new IllegalStateException(
                            "Native course unit is missing"));
            out.add(missionary);
        }
        return out;
    }

    /**
     * Retire a conquered settlement once (R-0174). The settlement leaves the
     * registry, so its claim, map interaction and learned identity end with it,
     * and no later load can recreate it. Its braves are rehomed, and its pupil
     * and missionary are released to the nearest legal square, or lost when
     * none exists.
     *
     * @param settlement  required settlement whose last defender was overcome
     * @param courseUnits required pupil and missionary resolved beforehand,
     *                    each held by this settlement
     * @return the pupil and missionary that found no legal square and died,
     *         each also recorded as a saved turn event of its owner
     */
    private List<NativeConquestResult.Loss> conquerNativeSettlement(
            final NativeSettlement settlement, final List<Unit> courseUnits) {
        final Location at = settlement.getLocation();
        // Dependents are settled while the settlement is still registered and
        // none of these steps can fail, so the removal below is the last
        // change and never leaves a half-conquered settlement.
        final List<NativeConquestResult.Loss> losses = new ArrayList<>();
        for (final Unit unit : courseUnits) {
            final boolean pupil = unit.isAtPlaceNativeCourse();
            final UnitType type = unit.getType();
            final Player unitOwner = unit.getOwner();
            final int unitId = unit.getId();
            if (!releaseCourseUnit(unit, at)) {
                final NativeConquestResult.Loss loss = new NativeConquestResult.Loss(
                        unitId, type, unitOwner.getName(), pupil);
                losses.add(loss);
                getTurnEventStore().add(TurnEventProvider.getNativeConquestLoss(
                        unitOwner, settlement, unitId, type, pupil));
            }
        }
        rehomeNativeBraves(settlement);
        nativeSettlements.remove(settlement);
        nativeRelations.removeSettlement(settlement);
        learnedNativeSettlements.values()
                .forEach(ids -> ids.remove(settlement.getId()));
        return losses;
    }

    /**
     * Braves that were homed here are rehomed to the nearest surviving
     * settlement of their nation, ties by stable ID; without one they stay
     * living as homeless braves.
     */
    private void rehomeNativeBraves(final NativeSettlement settlement) {
        for (final Unit unit : getAllUnits()) {
            if (!(unit instanceof UnitNativeBrave)) {
                continue;
            }
            final UnitNativeBrave brave = (UnitNativeBrave) unit;
            if (brave.getHomeSettlementId()
                    .filter(id -> id == settlement.getId()).isEmpty()) {
                continue;
            }
            final Location from = brave.isAtPlaceLocation()
                    ? brave.getLocation()
                    : settlement.getLocation();
            brave.rehome(nearestNativeSettlementOf(brave.getNation(), from,
                    settlement).map(NativeSettlement::getId).orElse(null));
        }
    }

    /**
     * Nearest settlement of the nation to a square other than the conquered
     * one, which is still registered while its dependents are settled.
     */
    private Optional<NativeSettlement> nearestNativeSettlementOf(
            final NativeNation nation, final Location from,
            final NativeSettlement conquered) {
        return nativeSettlements.getAll().stream()
                .filter(settlement -> settlement != conquered
                        && settlement.getNation() == nation)
                .min(Comparator.comparingInt(
                        (NativeSettlement settlement) -> settlement
                                .getLocation().getDistanceChebyshev(from))
                        .thenComparingInt(NativeSettlement::getId));
    }

    /**
     * Put a pupil or missionary of a conquered settlement on its nearest legal
     * square, or destroy it when no square is legal.
     *
     * @return whether the unit survived
     */
    private boolean releaseCourseUnit(final Unit unit,
            final Location settlementAt) {
        final Optional<Location> square = findNearestLegalSquare(unit,
                settlementAt);
        if (square.isPresent()) {
            releaseNativePupil(unit, square.get());
            return true;
        }
        unit.getPlace().destroy();
        destroyUnit(unit);
        return false;
    }

    /**
     * Nearest legal square for the unit anywhere on the map, by Chebyshev
     * distance from the settlement; inside one distance the lower X and then
     * the lower Y wins, so the choice is fixed. Release is an evacuation, not a
     * walk, so no path is needed. A square is legal when the shared movement
     * policy lets the unit's owner enter it: land for its type, no foreign unit
     * or foreign colony that is not an ally, and no native settlement, so the
     * conquered square itself, still registered and held by the attacker, is
     * never chosen.
     *
     * @param unit   required unit to release
     * @param center required square of the settlement
     * @return the square, empty when no square of the map is legal
     */
    private Optional<Location> findNearestLegalSquare(final Unit unit,
            final Location center) {
        final int farthest = Math.max(map.getMaxLocationX(),
                map.getMaxLocationY());
        for (int distance = 1; distance <= farthest; distance++) {
            final List<Location> ring = new ArrayList<>();
            for (int dx = -distance; dx <= distance; dx++) {
                for (int dy = -distance; dy <= distance; dy++) {
                    final Location square = Location.of(center.getX() + dx,
                            center.getY() + dy);
                    if (Math.max(Math.abs(dx), Math.abs(dy)) == distance
                            && map.isValid(square)) {
                        ring.add(square);
                    }
                }
            }
            ring.sort(Comparator.comparingInt(Location::getX)
                    .thenComparingInt(Location::getY));
            for (final Location square : ring) {
                if (MovementPolicy.isPossibleToEnter(this, unit.getType(),
                        false, unit.getOwner(), square, false)) {
                    return Optional.of(square);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Attack bonus of Spain against a native settlement, in percent. It is
     * asked only by the settlement quote, never for a brave on open ground or a
     * European colony, and no other nation or the Crown has it.
     *
     * @param attacker required owner of the attacking unit
     * @return 50 for Spain, otherwise 0
     */
    static int settlementAttackBonus(final Player attacker) {
        return nativeNationOf(attacker)
                .filter(nation -> nation == EuropeanNation.SPANISH).isPresent()
                        ? SPANISH_SETTLEMENT_ATTACK_BONUS
                        : 0;
    }

    /**
     * Roles that may fight a settlement: artillery, and colonists that are
     * scouts or hold guns (soldiers and dragoons). A colonist that cannot
     * produce a battle strength is refused by the shared resolver instead.
     */
    private static boolean isConquestRole(final Unit attacker) {
        if (attacker instanceof UnitArtillery) {
            return true;
        }
        return attacker instanceof UnitFreeColonist
                && (((UnitFreeColonist) attacker).isScout()
                        || ((UnitFreeColonist) attacker).isHoldingGuns());
    }

    /** Same turn-state gate as an ordinary attack: points, action and order. */
    private static boolean isReadyForAttack(final Unit attacker) {
        return attacker.getActionPoints() > 0
                && attacker.getAction().isNoAction()
                && !((AbstractUnit) attacker).hasStandingOrder();
    }

    private static boolean isNativeDefender(final Unit unit,
            final NativeSettlement settlement) {
        return unit instanceof UnitNativeBrave && ((UnitNativeBrave) unit)
                .getNation() == settlement.getNation()
                && unit.isAtPlaceLocation();
    }

    /**
     * Owner update of a colonial player (R-0148), called once per completed
     * update after its relations. A tense settlement cancels the course with
     * feedback; otherwise each update counts and the fourth grants the skill to
     * the same colonist. A blocked release waits without losing the colonist.
     *
     * @param owner required colonial player whose update completed
     */
    void advanceNativeTeaching(final Player owner) {
        Preconditions.checkNotNull(owner);
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            final Optional<Integer> pupilId = settlement.getTeachingPupilId();
            if (pupilId.isEmpty()) {
                continue;
            }
            final Unit pupil = tryGetUnitById(pupilId.get()).orElseThrow(
                    () -> new IllegalStateException("Native pupil is missing"));
            if (pupil.getOwner() != owner) {
                continue;
            }
            final NativeHostility hostility = nativeRelations
                    .getHostility(settlement, nativeEuropean(owner));
            final boolean tense = hostility == NativeHostility.ANGRY
                    || hostility == NativeHostility.HOSTILE;
            if (settlement.getTeachingStage()
                    .orElseThrow() == NativeTeachingCourse.Stage.LEARNING) {
                if (tense) {
                    // Hostility ends the learning now: no further update
                    // counts.
                    settlement.cancelTeaching();
                    addTurnEvent(TurnEventProvider.getNativeTeaching(owner,
                            TurnEventNativeTeaching.Kind.CANCELLED_HOSTILITY,
                            settlement.getTeachableSkill()));
                } else {
                    settlement.addTeachingUpdate();
                    if (settlement
                            .getTeachingUpdates() == NativeTeachingCourse.REQUIRED_UPDATES) {
                        // The grant happens once, here, whether or not the
                        // colonist can leave yet.
                        ((UnitFreeColonist) pupil).completeEducation(
                                settlement.getTeachableSkill());
                        settlement.completeTeaching();
                        addTurnEvent(TurnEventProvider.getNativeTeaching(owner,
                                TurnEventNativeTeaching.Kind.COMPLETED,
                                settlement.getTeachableSkill()));
                    }
                }
            }
            // A finished or canceled colonist waits safely and leaves as soon
            // as
            // a legal square is free; leaving never grants anything again.
            if (settlement.getTeachingStage()
                    .orElseThrow() != NativeTeachingCourse.Stage.LEARNING) {
                findNativeReleaseSquare(pupil, settlement).ifPresent(square -> {
                    releaseNativePupil(pupil, square);
                    settlement.clearTeaching();
                });
            }
        }
    }

    /**
     * A saved pupil fits its stage: a learner or a canceled one is still an
     * unskilled colonist or servant, while a finished one already holds the
     * granted skill.
     */
    private static boolean pupilFitsStage(final Unit pupil,
            final NativeSettlement settlement) {
        if (settlement.getTeachingStage()
                .orElseThrow() == NativeTeachingCourse.Stage.COMPLETED) {
            return pupil.getType() == settlement.getTeachableSkill();
        }
        return nativeTeachingRoleAllows(pupil);
    }

    /**
     * Check saved native courses against the units after loading (R-0148): each
     * pupil is a registered colonist of a colonial owner on its settlement's
     * course, and each native-course unit has exactly one course.
     */
    void validateSavedNativeTeaching() {
        final Set<Integer> pupils = new HashSet<>();
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            settlement.getTeachingPupilId().ifPresent(pupilId -> {
                Preconditions.checkArgument(pupils.add(pupilId),
                        "Unsupported save: a native pupil is in two courses");
                final Unit pupil = tryGetUnitById(pupilId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unsupported save: missing native pupil"));
                Preconditions.checkArgument(
                        pupil.getOwner().isColonialNation()
                                && pupilFitsStage(pupil, settlement)
                                && getNativeTeachingSettlement(pupil)
                                        .filter(found -> found == settlement)
                                        .isPresent(),
                        "Unsupported save: native pupil does not match its course");
            });
        }
        for (final Unit unit : unitStorage.getUnits()) {
            Preconditions.checkArgument(
                    !unit.isAtPlaceNativeCourse()
                            || pupils.contains(unit.getId()),
                    "Unsupported save: native course without a settlement");
        }
    }

    /**
     * Terms of the mission a missionary may establish now (R-0164). Pure read:
     * it changes no unit, relation, mission, treasury or random state, so
     * cancelling the confirmation has nothing to undo. Expertise is read live,
     * so Jean de Brebeuf's membership counts at once.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @return the terms, empty when no mission may be established now
     */
    public Optional<NativeMissionView> getNativeMissionPreview(
            final Unit visitor, final NativeSettlement settlement) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        if (!isMissionAvailable(visitor, settlement)) {
            return Optional.empty();
        }
        return Optional.of(missionView(visitor, settlement, 0));
    }

    /**
     * Establish the mission of a missionary beside a native settlement
     * (R-0164). The missionary leaves the map for the settlement's mission at
     * once, with its identity, owner and role; the settlement then holds that
     * mission. The shown terms and every gate run before the first change, so a
     * refused or stale confirmation leaves the missionary and the settlement
     * unchanged.
     *
     * @param missionary required missionary adjacent to the settlement
     * @param settlement required native settlement
     * @param shown      required terms the player was shown and confirmed
     * @throws IllegalStateException when the mission is not available now or
     *                               the shown terms went stale
     */
    public void establishNativeMission(final Unit missionary,
            final NativeSettlement settlement, final NativeMissionView shown) {
        Preconditions.checkNotNull(missionary);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        final Optional<NativeMissionView> current = getNativeMissionPreview(
                missionary, settlement);
        Preconditions.checkState(
                current.isPresent() && current.get().equals(shown),
                "Native mission terms changed");
        ((AbstractUnit) missionary).beginNativeMission(settlement.getId());
        settlement.establishMission(missionary.getId());
    }

    /**
     * Terms of the contest a visiting missionary may confirm against the rival
     * mission of a settlement (R-0165). Pure read: it changes no unit,
     * relation, mission, treasury or random state, so cancelling the
     * confirmation has nothing to undo. Expertise and hostility are read live,
     * so Jean de Brebeuf's membership and the current attitudes count at once.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @return the terms, empty when no rival mission may be contested now
     */
    public Optional<NativeMissionContestView> getNativeMissionContestPreview(
            final Unit visitor, final NativeSettlement settlement) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        if (!isMissionContestAvailable(visitor, settlement)) {
            return Optional.empty();
        }
        return Optional.of(contestView(visitor, settlement));
    }

    /**
     * Challenge the rival mission of a settlement (R-0165). Every check and the
     * terms comparison run before the one draw from the persisted random state,
     * so a refused contest changes neither missionary, the mission, its clock
     * nor the random state. A success removes only the resident, then moves the
     * visitor into the mission with its clock at zero. A failure removes only
     * the visitor, and the resident keeps the mission and its clock. The
     * confirmed contest changes the units at once, so Save and Continue cannot
     * replay it.
     *
     * @param visitor    required missionary beside the settlement
     * @param settlement required native settlement
     * @param shown      required terms the player was shown and confirmed
     * @return the actual outcome of the contest
     * @throws IllegalStateException when the contest is not available now or
     *                               the shown terms went stale
     */
    public NativeMissionContestOutcome contestNativeMission(final Unit visitor,
            final NativeSettlement settlement,
            final NativeMissionContestView shown) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        final Optional<NativeMissionContestView> current = getNativeMissionContestPreview(
                visitor, settlement);
        Preconditions.checkState(
                current.isPresent() && current.get().equals(shown),
                "Native mission contest terms changed");
        final Unit resident = residentOf(settlement);
        final NativeMissionContestOutcome outcome = NativeContestRules
                .outcome(shown.getChallengerChance(), nextCombatRandom());
        if (outcome == NativeMissionContestOutcome.SUCCESS) {
            // The resident's place is released by its cleanup, not by combat.
            settlement.clearMission();
            resident.getPlace().destroy();
            destroyUnit(resident);
            ((AbstractUnit) visitor).beginNativeMission(settlement.getId());
            settlement.establishMission(visitor.getId());
        } else {
            // A dead missionary keeps no go-to order and no square.
            ((AbstractUnit) visitor).discardGoToOrder();
            visitor.getPlace().destroy();
            destroyUnit(visitor);
        }
        return outcome;
    }

    /**
     * European powers a paid incitement may select now (R-0166): every other
     * colonial player of this game, never the visitor's own nation, the Crown
     * or a nation absent from the game. Pure read.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @return the selectable targets in European order, empty when the visitor
     *         cannot incite now
     */
    public List<EuropeanNation> getNativeIncitementTargets(final Unit visitor,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        final List<EuropeanNation> targets = new ArrayList<>();
        if (!isIncitementEntry(visitor, settlement)) {
            return targets;
        }
        for (final EuropeanNation target : EuropeanNation.values()) {
            if (isIncitementTarget(visitor, target)) {
                targets.add(target);
            }
        }
        return targets;
    }

    /**
     * Terms of the paid incitement a visiting missionary may confirm against a
     * selected European power (R-0166). Pure read: it changes no unit,
     * relation, order, gold or random state, so cancelling the confirmation has
     * nothing to undo. The price, expertise and mission count are read live.
     *
     * @param visitor    required unit that would enter the settlement
     * @param settlement required native settlement
     * @param target     required selected European power
     * @return the terms, empty when the incitement is not available now
     */
    public Optional<NativeIncitementQuote> getNativeIncitementQuote(
            final Unit visitor, final NativeSettlement settlement,
            final EuropeanNation target) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(target);
        if (!isIncitementEntry(visitor, settlement)
                || !isIncitementTarget(visitor, target)) {
            return Optional.empty();
        }
        return Optional.of(incitementQuote(visitor, settlement, target));
    }

    /**
     * Confirm a paid incitement (R-0166). Every check runs before the first
     * change. Then the price is paid and the one saved draw decides the result,
     * whether it succeeds or fails. A success raises the tribe's anger toward
     * the target to at least 75 and replaces its order with a fresh twenty-turn
     * one; a failure leaves the previous order and anger unchanged.
     *
     * @param visitor    required missionary beside the settlement
     * @param settlement required native settlement
     * @param shown      required terms the player was shown and confirmed
     * @return the actual outcome of the incitement
     * @throws IllegalStateException when the incitement is not available now,
     *                               the shown terms went stale or the visitor's
     *                               owner cannot pay the price
     */
    public NativeIncitementOutcome incite(final Unit visitor,
            final NativeSettlement settlement,
            final NativeIncitementQuote shown) {
        Preconditions.checkNotNull(visitor);
        Preconditions.checkNotNull(settlement);
        Preconditions.checkNotNull(shown);
        checkTurnPreparationComplete();
        final Optional<NativeIncitementQuote> current = getNativeIncitementQuote(
                visitor, settlement, shown.getTarget());
        Preconditions.checkState(
                current.isPresent() && current.get().equals(shown),
                "Native incitement terms changed");
        final Player owner = visitor.getOwner();
        Preconditions.checkState(owner.getGold() >= shown.getPrice(),
                "Not enough gold for native incitement");
        final NativeIncitementOutcome outcome = NativeIncitementRules
                .outcome(shown.getChance(), nextCombatRandom());
        owner.setGold(owner.getGold() - shown.getPrice());
        if (outcome == NativeIncitementOutcome.SUCCESS) {
            nativeRelations.raiseTribalAngerTo(settlement.getNation(),
                    shown.getTarget(), NativeIncitementRules.TARGET_ANGER);
            nativeIncitements.install(settlement.getNation(),
                    shown.getTarget());
        }
        return outcome;
    }

    /**
     * The order of a native tribe that pursues a European power (R-0166). Pure
     * read: opening it advances no clock.
     *
     * @param nation required native tribe
     * @return the active order, empty when the tribe has none
     */
    public Optional<NativeIncitementOrder> getNativeIncitementOrder(
            final NativeNation nation) {
        Preconditions.checkNotNull(nation);
        return nativeIncitements.find(nation);
    }

    /**
     * Drop a tribe's order when its target no longer exists in the game, or
     * when the tribe's anger toward the target is below 75 (R-0166). Run once
     * at the start of the native turn, before the native AI acts.
     *
     * @param nation required native tribe whose turn has started
     */
    void checkNativeIncitement(final NativeNation nation) {
        Preconditions.checkNotNull(nation);
        nativeIncitements.find(nation).ifPresent(order -> {
            final EuropeanNation target = order.getTarget();
            if (!isLiveIncitedTarget(target)
                    || nativeRelations.getTribalAnger(nation,
                            target) < NativeIncitementRules.TARGET_ANGER) {
                nativeIncitements.clear(nation);
            }
        });
    }

    /**
     * One native action turn of a tribe is over, so its order loses one turn
     * (R-0166). Called once per native turn, never by a load or a Continue.
     *
     * @param nation required native tribe whose turn has ended
     */
    void advanceNativeIncitement(final NativeNation nation) {
        Preconditions.checkNotNull(nation);
        nativeIncitements.endTurn(nation);
    }

    /**
     * Whether the visitor may incite now: the shared entry gate and a
     * missionary role. The chooser needs no separate limit, because the price
     * and target are the only conditions. Pure read.
     */
    private boolean isIncitementEntry(final Unit visitor,
            final NativeSettlement settlement) {
        return isEntryAllowed(visitor, settlement)
                && visitor instanceof UnitFreeColonist
                && ((UnitFreeColonist) visitor).isMissionary();
    }

    /**
     * Whether a European power may be selected as target from this visitor
     * (R-0166): another colonial player that exists in this game. Pure read.
     */
    private boolean isIncitementTarget(final Unit visitor,
            final EuropeanNation target) {
        return target != nativeEuropean(visitor.getOwner())
                && isLiveIncitedTarget(target);
    }

    /**
     * @param target required European power
     * @return whether a colonial player of that power is in the game now
     */
    private boolean isLiveIncitedTarget(final EuropeanNation target) {
        return getPlayers().stream()
                .anyMatch(player -> player.isColonialNation()
                        && nativeNationOf(player).equals(Optional.of(target)));
    }

    /**
     * @param visitor    required missionary beside the settlement
     * @param settlement required native settlement
     * @param target     required selected European power
     * @return the terms read live from the visitor, the attitude, the purse and
     *         the current order revision; pure
     */
    private NativeIncitementQuote incitementQuote(final Unit visitor,
            final NativeSettlement settlement, final EuropeanNation target) {
        final Player owner = visitor.getOwner();
        final double hostility = nativeRelations.getHostilityValue(settlement,
                nativeEuropean(owner));
        final int missions = ownMissionsInTribe(owner, settlement.getNation());
        final boolean expert = missionaryExpert(visitor);
        return new NativeIncitementQuote(visitor.getId(), settlement.getId(),
                settlement.getNation(), target, hostility, missions, expert,
                NativeIncitementRules.price(hostility, missions),
                owner.getGold(), NativeIncitementRules.chance(expert),
                nativeIncitements.revision());
    }

    /**
     * @param owner  required colonial player
     * @param nation required native tribe
     * @return how many settlements of the tribe hold a mission of this owner
     */
    private int ownMissionsInTribe(final Player owner,
            final NativeNation nation) {
        return (int) nativeSettlements.getAll().stream()
                .filter(settlement -> settlement.getNation() == nation)
                .filter(settlement -> settlement.getMissionaryId()
                        .flatMap(missionaryId -> tryGetUnitById(missionaryId))
                        .map(missionary -> missionary.getOwner() == owner)
                        .orElse(false))
                .count();
    }

    /**
     * Whether the settlement's mission belongs to a nation other than the
     * visitor's (R-0165). Pure read.
     */
    private boolean isRivalMission(final Unit visitor,
            final NativeSettlement settlement) {
        return settlement.getMissionaryId()
                .flatMap(missionaryId -> tryGetUnitById(missionaryId))
                .map(resident -> resident.getOwner() != visitor.getOwner())
                .orElse(false);
    }

    /**
     * Whether the visitor may challenge the settlement's rival mission now: the
     * chooser must offer the contest, which needs a missionary that passes the
     * shared entry gate. Pure read.
     */
    private boolean isMissionContestAvailable(final Unit visitor,
            final NativeSettlement settlement) {
        return getNativeVisitChoices(visitor, settlement).stream().anyMatch(
                choice -> choice.getAction() == NativeVisitAction.MISSION
                        && choice
                                .getLimit() == NativeVisitChoice.Limit.CONTEST_AVAILABLE);
    }

    /**
     * @param visitor    required visitor beside a rival mission
     * @param settlement required native settlement holding that mission
     * @return the contest terms read live from both missionaries and the
     *         attitudes; pure
     */
    private NativeMissionContestView contestView(final Unit visitor,
            final NativeSettlement settlement) {
        final Unit resident = residentOf(settlement);
        final Player residentOwner = resident.getOwner();
        final EuropeanNation holder = nativeEuropean(residentOwner);
        final double visitorHostility = nativeRelations.getHostilityValue(
                settlement, nativeEuropean(visitor.getOwner()));
        final double residentHostility = nativeRelations
                .getHostilityValue(settlement, holder);
        final boolean visitorExpert = missionaryExpert(visitor);
        final boolean residentExpert = missionaryExpert(resident);
        final double challengerChance = NativeContestRules.challengerChance(
                NativeContestRules.strength(visitorExpert, visitorHostility),
                NativeContestRules.strength(residentExpert, residentHostility));
        return new NativeMissionContestView(visitor.getId(), settlement.getId(),
                resident.getId(), holder, residentOwner.getDisplayName(),
                visitorExpert, residentExpert, visitorHostility,
                residentHostility, challengerChance,
                settlement.getMissionRevision());
    }

    /**
     * @param settlement required settlement that holds a mission
     * @return the resident missionary of that mission
     */
    private Unit residentOf(final NativeSettlement settlement) {
        return tryGetUnitById(settlement.getMissionaryId().orElseThrow())
                .orElseThrow(() -> new IllegalStateException(
                        "Native mission missionary is missing"));
    }

    /**
     * What a player reads about the mission of a learned native settlement
     * (R-0164): its owner, the expertise of its missionary, the owner's current
     * attitude and the counted progress. The missionary stays stationed here,
     * so the mission is shown even when it is not on the map. Pure read.
     *
     * @param player     required player
     * @param settlement required native settlement
     * @return the view, empty without a mission or when the player has not
     *         learned the settlement
     */
    public Optional<NativeMissionView> getNativeMissionView(final Player player,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(settlement);
        final Set<Integer> learned = learnedNativeSettlements
                .getOrDefault(player.getName(), Set.of());
        if (!learned.contains(settlement.getId())) {
            return Optional.empty();
        }
        return settlement.getMissionaryId()
                .flatMap(missionaryId -> tryGetUnitById(missionaryId))
                .filter(Unit::isAtPlaceNativeMission)
                .map(missionary -> missionView(missionary, settlement,
                        settlement.getMissionUpdates()));
    }

    /**
     * Whether the missionary may establish a mission now: the chooser must
     * offer it, which needs a missionary that passes the shared entry gate.
     * Pure read.
     */
    private boolean isMissionAvailable(final Unit visitor,
            final NativeSettlement settlement) {
        return getNativeVisitChoices(visitor, settlement).stream().anyMatch(
                choice -> choice.getAction() == NativeVisitAction.MISSION
                        && choice
                                .getLimit() == NativeVisitChoice.Limit.AVAILABLE);
    }

    private NativeMissionView missionView(final Unit missionary,
            final NativeSettlement settlement, final int completedUpdates) {
        final Player owner = missionary.getOwner();
        final EuropeanNation european = nativeEuropean(owner);
        return new NativeMissionView(european, owner.getDisplayName(),
                missionaryExpert(missionary),
                nativeRelations.getHostility(settlement, european),
                completedUpdates);
    }

    /**
     * @param missionary required registered missionary
     * @return whether it acts as an expert now: a Jesuit, or any missionary
     *         while its owner holds Jean de Brebeuf
     */
    private static boolean missionaryExpert(final Unit missionary) {
        return ((UnitFreeColonist) missionary)
                .hasEffectiveMissionaryExpertise();
    }

    /**
     * One update of a colonial European's missions, run once from its own turn
     * preparation before its relations (R-0164). Each mission the owner holds
     * lowers the owner's local alarm by 2, or by 4 with an expert missionary,
     * and adds 1 to the local alarm of each rival nation that the tribe knows
     * by land introduction. The clock reads the attitude at the entry of this
     * update, before its own reduction: only a hostile update (alarm or anger
     * of 75 or more, the larger of both) pauses it, while an angry one (50 up
     * to 75) still counts. The fifth nonhostile update of a cycle ends the
     * cycle and is returned for the conversion of R-0149; nothing is banked and
     * nothing is converted here.
     *
     * @param owner required player whose update is starting
     * @return settlements whose mission reached its fifth nonhostile update now
     */
    List<NativeSettlement> advanceNativeMissions(final Player owner) {
        Preconditions.checkNotNull(owner);
        final List<NativeSettlement> fifth = new ArrayList<>();
        final Optional<EuropeanNation> nation = nativeNationOf(owner);
        if (nation.isEmpty()) {
            return fifth;
        }
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            final Optional<Integer> missionaryId = settlement.getMissionaryId();
            if (missionaryId.isEmpty()) {
                continue;
            }
            final Unit missionary = tryGetUnitById(missionaryId.get())
                    .orElseThrow(() -> new IllegalStateException(
                            "Native mission missionary is missing"));
            if (missionary.getOwner() != owner) {
                continue;
            }
            final NativeHostility entry = nativeRelations
                    .getHostility(settlement, nation.get());
            // Only the hostile band pauses the clock (accepted R-0164 rule).
            final boolean nonhostile = entry != NativeHostility.HOSTILE;
            nativeRelations.lowerLocalAlarm(settlement, nation.get(),
                    missionaryExpert(missionary)
                            ? NativeRelations.MISSION_EXPERT_REDUCTION
                            : NativeRelations.MISSION_ORDINARY_REDUCTION);
            raiseContactedRivals(settlement, owner);
            if (nonhostile && settlement.addMissionUpdate()) {
                fifth.add(settlement);
            }
        }
        return fifth;
    }

    /**
     * Local alarm of each rival colonial nation that the tribe knows through a
     * land introduction (R-0164): one raise each, with that rival's own
     * national and founder factor. The owner, uncontacted nations and nations
     * that are not players of this game get nothing.
     *
     * @param settlement required settlement of the mission
     * @param owner      required owner of the mission
     */
    private void raiseContactedRivals(final NativeSettlement settlement,
            final Player owner) {
        for (final Player rival : playerStore.getPlayers()) {
            final Optional<EuropeanNation> rivalNation = nativeNationOf(rival);
            if (rival == owner || rivalNation.isEmpty() || !nativeContacts
                    .isContacted(rivalNation.get(), settlement.getNation())) {
                continue;
            }
            nativeRelations.addLocalAlarm(settlement, rivalNation.get(),
                    NativeRelations.MISSION_RIVAL_RAISE,
                    nativeLocalFactor(rivalNation.get(), rival));
        }
    }

    /**
     * Count the owner updates a native convert of this owner spends unassigned
     * (R-0149). A convert working a colony field or a building pauses the count
     * without resetting it. The eighth unassigned update returns the convert to
     * its tribe once. Only assignment to a field or building pauses the count;
     * a convert on the map, in cargo, in transit, in Europe, in a native course
     * or stationed at a mission is unassigned and counts. Called after the
     * unit-update phase, so a convert delivered in this preparation starts at
     * zero.
     *
     * @param owner required colonial player whose update is starting
     */
    void advanceNativeConverts(final Player owner) {
        Preconditions.checkNotNull(owner);
        for (final Unit unit : List.copyOf(unitStorage.getUnits())) {
            if (unit.getOwner() != owner || !(unit instanceof UnitFreeColonist)
                    || unit.isAtPlaceColonyField()
                    || unit.isAtPlaceConstructionSlot()) {
                continue;
            }
            final UnitFreeColonist convert = (UnitFreeColonist) unit;
            if (convert.isNativeConvert()
                    && convert.addConvertUnassignedUpdate()) {
                returnConvertToTribe(convert);
                addTurnEvent(TurnEventProvider.getNativeConvert(owner,
                        TurnEventNativeConvert.Kind.RETURNED));
            }
        }
    }

    /**
     * Las Casas's one-time join (R-0182): every native convert the owner holds
     * now becomes an ordinary free colonist. Each person keeps its identity,
     * place, cargo, job, equipment and resources; only the affiliation and the
     * lifetime count end, so its restrictions and expiry cease. Converts that
     * other owners hold, free colonists and converts acquired later are not
     * touched. Nothing is created, and the notice is stored once per join.
     *
     * @param owner required player whose founder joins
     */
    void freeNativeConverts(final Player owner) {
        Preconditions.checkNotNull(owner);
        // Collect first: a list of owned converts is the whole preflight.
        // Ending
        // the affiliation only lifts restrictions, so no current job needs a
        // new
        // place before this change.
        final List<UnitFreeColonist> converts = new ArrayList<>();
        for (final Unit unit : unitStorage.getUnits()) {
            if (unit.getOwner() == owner && unit instanceof UnitFreeColonist
                    && ((UnitFreeColonist) unit).isNativeConvert()) {
                converts.add((UnitFreeColonist) unit);
            }
        }
        if (converts.isEmpty()) {
            return;
        }
        converts.forEach(UnitFreeColonist::releaseNativeConvert);
        addTurnEvent(TurnEventProvider.getNativeConvert(owner,
                TurnEventNativeConvert.Kind.FREED));
    }

    /**
     * Remove an expired convert through the shared cleanup: settlement
     * references to the person are cleared first, then its place releases the
     * cargo slot, and the registry drops it once. No passenger slot or course
     * keeps a reference to the removed identity.
     */
    private void returnConvertToTribe(final UnitFreeColonist convert) {
        getNativeTeachingSettlement(convert)
                .ifPresent(NativeSettlement::clearTeaching);
        if (convert.isAtPlaceNativeMission()) {
            nativeSettlements.getAll().stream()
                    .filter(settlement -> settlement.getMissionaryId()
                            .equals(Optional.of(convert.getId())))
                    .forEach(NativeSettlement::clearMission);
        }
        convert.getPlace().destroy();
        destroyUnit(convert);
    }

    /**
     * Resolve the fifth-update boundaries of the owner's missions (R-0149).
     * Each boundary draws the saved combat randomness exactly once. A success
     * creates at most one convert at the nearest owned colony reachable by
     * land, then the stable colony identity. With no reachable colony or no
     * free unit identity, the success is skipped without banking it. Combat
     * never converts.
     *
     * @param owner required colonial player whose update is starting
     * @param fifth required settlements whose missions reached the fifth update
     */
    void resolveNativeConversions(final Player owner,
            final List<NativeSettlement> fifth) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(fifth);
        for (final NativeSettlement settlement : fifth) {
            final Unit missionary = tryGetUnitById(
                    settlement.getMissionaryId().orElseThrow())
                    .orElseThrow(() -> new IllegalStateException(
                            "Native mission missionary is missing"));
            final double chance = NativeConversion.chance(
                    missionaryExpert(missionary), owner.getCongressMembers()
                            .contains(Founder.JUAN_DE_SEPULVEDA));
            if (nextCombatRandom() >= chance) {
                continue;
            }
            final Optional<Colony> destination = nearestReachableOwnedColony(
                    settlement.getLocation(), owner);
            if (destination.isEmpty() || !unitStorage.canAllocateUnit()) {
                continue;
            }
            final Unit convert = addUnitOutSideColony(destination.get());
            ((UnitFreeColonist) convert)
                    .becomeNativeConvert(settlement.getNation());
            addTurnEvent(TurnEventProvider.getNativeConvert(owner,
                    TurnEventNativeConvert.Kind.ACQUIRED));
        }
    }

    /**
     * Owned colony nearest to a square by legal land route (R-0149): each step
     * must be one the owner's colonist may enter by the shared movement rule,
     * so sea, foreign settlements and colonies, hostile colonies and squares
     * kept by foreign units are never crossed. The distance counts steps of
     * that route, not straight-line geometry. Ties go to the lower colony
     * identity.
     *
     * @param from  required land square the search starts from
     * @param owner required owner of the colonies
     * @return the colony, empty when no owned colony is reachable by land
     */
    private Optional<Colony> nearestReachableOwnedColony(final Location from,
            final Player owner) {
        final Map<Location, Integer> distance = new HashMap<>();
        final ArrayDeque<Location> frontier = new ArrayDeque<>();
        distance.put(from, 0);
        frontier.add(from);
        while (!frontier.isEmpty()) {
            final Location at = frontier.removeFirst();
            for (final Location next : at.getNeighbors()) {
                if (!distance.containsKey(next)
                        && MovementPolicy.isPossibleToEnter(this,
                                UnitType.COLONIST, false, owner, next, false)) {
                    distance.put(next, distance.get(at) + 1);
                    frontier.add(next);
                }
            }
        }
        return colonies.stream()
                .filter(colony -> colony.getOwner() == owner && colony.isValid()
                        && distance.containsKey(colony.getLocation()))
                .min(Comparator.comparingInt(
                        (Colony colony) -> distance.get(colony.getLocation()))
                        .thenComparingInt(Colony::getId));
    }

    /**
     * Check saved native missions against the units after loading (R-0164):
     * each held missionary is a registered missionary of a colonial owner that
     * is stationed at this settlement, and each stationed missionary belongs to
     * the mission of one settlement.
     */
    void validateSavedNativeMissions() {
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            settlement.getMissionaryId().ifPresent(missionaryId -> {
                final Unit missionary = tryGetUnitById(missionaryId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unsupported save: missing native missionary"));
                Preconditions.checkArgument(
                        missionary instanceof UnitFreeColonist
                                && ((UnitFreeColonist) missionary)
                                        .isMissionary()
                                && missionary.getOwner().isColonialNation()
                                && isStationedAt(missionary, settlement),
                        "Unsupported save: native mission does not match its missionary");
            });
        }
        for (final Unit unit : unitStorage.getUnits()) {
            Preconditions.checkArgument(
                    !unit.isAtPlaceNativeMission() || nativeSettlements.getAll()
                            .stream()
                            .anyMatch(settlement -> settlement.getMissionaryId()
                                    .equals(Optional.of(unit.getId()))),
                    "Unsupported save: native mission without a settlement");
        }
    }

    /** Whether the pupil is in the course of exactly this settlement. */
    private static boolean isLearningAt(final Unit pupil,
            final NativeSettlement settlement) {
        return pupil.isAtPlaceNativeCourse()
                && ((PlaceNativeCourse) pupil.getPlace())
                        .getSettlementId() == settlement.getId();
    }

    private static boolean isStationedAt(final Unit missionary,
            final NativeSettlement settlement) {
        return missionary.isAtPlaceNativeMission()
                && ((PlaceNativeMission) missionary.getPlace())
                        .getSettlementId() == settlement.getId();
    }

    /**
     * Exact tribal anger of a native nation toward a colonial European. Only
     * the model and tests read the number; player interfaces show hostility of
     * a settlement the player has learned.
     *
     * @param player required colonial player
     * @param nation required native nation
     * @return anger in 0..100, fractions included
     */
    public double getNativeTribalAnger(final Player player,
            final NativeNation nation) {
        Preconditions.checkNotNull(nation);
        return nativeRelations.getTribalAnger(nation, nativeEuropean(player));
    }

    /**
     * Exact local alarm of a settlement toward a colonial European. Same
     * reading rule as {@link #getNativeTribalAnger}.
     *
     * @param player     required colonial player
     * @param settlement required native settlement
     * @return alarm in 0..100, fractions included
     */
    public double getNativeLocalAlarm(final Player player,
            final NativeSettlement settlement) {
        Preconditions.checkNotNull(settlement);
        return nativeRelations.getLocalAlarm(settlement,
                nativeEuropean(player));
    }

    /**
     * What the player may read about a learned native settlement: attitude,
     * local alarm change, and a rival provoker only from visible sources. Pure
     * read; opening it advances no relation. The settlement must be learned by
     * the player, and only a colonial European has a view.
     *
     * @param player     required player
     * @param settlement required native settlement
     * @return the view, empty for a king or a settlement the player has not
     *         learned
     */
    public Optional<NativeRelationView> getNativeRelationView(
            final Player player, final NativeSettlement settlement) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(settlement);
        if (nativeNationOf(player).isEmpty()) {
            return Optional.empty();
        }
        final Set<Integer> learned = learnedNativeSettlements
                .getOrDefault(player.getName(), Set.of());
        if (!learned.contains(settlement.getId())) {
            return Optional.empty();
        }
        final EuropeanNation european = nativeEuropean(player);
        return Optional.of(NativeRelationView.of(
                nativeRelations.getHostility(settlement, european),
                nativeRelations.getLocalAlarmChange(settlement, european),
                visibleNativeProvoker(settlement, player)));
    }

    /**
     * Consolidated native advisor of one player (R-0201). A nation is known by
     * a land contact (R-0159) or by a learned settlement; each learned
     * settlement carries its relation view and, once the chief has told it, the
     * goods and skill of that visit. Pure read: it draws nothing, changes no
     * relation and records no sighting, so reopening it repeats the same facts.
     *
     * @param player required player
     * @return the report, empty for a king or a scripted actor
     */
    public NativeAdvisorReport getNativeAdvisorReport(final Player player) {
        Preconditions.checkNotNull(player);
        if (nativeNationOf(player).isEmpty()) {
            return NativeAdvisorReport.of(List.of(), List.of());
        }
        final EuropeanNation european = nativeEuropean(player);
        final List<NativeSettlement> learned = getNativeSettlementsKnownTo(
                player);
        final List<NativeAdvisorReport.Settlement> rows = new ArrayList<>();
        for (final NativeSettlement settlement : learned) {
            rows.add(NativeAdvisorReport.Settlement.of(settlement,
                    getNativeRelationView(player, settlement).orElseThrow(),
                    learnedChiefFacts(settlement, european)));
        }
        final List<NativeNation> nations = new ArrayList<>();
        for (final NativeNation nation : NativeNation.values()) {
            final boolean learnedHere = learned.stream()
                    .anyMatch(settlement -> settlement.getNation() == nation);
            if (learnedHere || isNativeContacted(european, nation)) {
                nations.add(nation);
            }
        }
        return NativeAdvisorReport.of(nations, rows);
    }

    /**
     * Chief information the nation has learned here (R-0158, R-0201). It is
     * known from the first information outcome, or from a confirmed repeat
     * visit after a gold or death outcome, which records the disclosure. It is
     * never derived from the map: explored land alone teaches nothing.
     */
    private Optional<NativeChiefResult.Information> learnedChiefFacts(
            final NativeSettlement settlement, final EuropeanNation european) {
        return settlement.isChiefInformed(european)
                ? Optional.of(chiefInformation(settlement))
                : Optional.empty();
    }

    /**
     * Start the alarm observation of a colonial European at the start of its
     * own turn, before congress settles. A Crown or a scripted actor has none.
     *
     * @param european required player whose turn is starting
     */
    void observeNativeRelations(final Player european) {
        Preconditions.checkNotNull(european);
        nativeNationOf(european)
                .ifPresent(nation -> nativeRelations.observeLocalAlarm(nation));
    }

    /**
     * One peaceful update of a colonial European, run once from its own turn
     * preparation. A Crown or a scripted actor without a nation has none.
     *
     * @param european required player whose turn has completed preparation
     */
    void updateNativeRelations(final Player european) {
        Preconditions.checkNotNull(european);
        final Optional<EuropeanNation> nation = nativeNationOf(european);
        if (nation.isEmpty()) {
            return;
        }
        final Map<Integer, Double> pressure = new HashMap<>();
        for (final NativeSettlement settlement : nativeSettlements.getAll()) {
            pressure.put(settlement.getId(),
                    nativeLocalPressure(settlement, european));
        }
        nativeRelations.applyEuropeanUpdate(nation.get(), pressure,
                nativeLocalFactor(nation.get(), european));
    }

    /**
     * Direct tribal anger from a later trigger; the national and founder
     * factors never apply to it.
     *
     * @param nation required native nation
     * @param owner  required colonial player
     * @param amount finite nonnegative increase
     */
    void addNativeTribalAnger(final NativeNation nation, final Player owner,
            final double amount) {
        Preconditions.checkNotNull(nation);
        nativeRelations.addTribalAnger(nation, nativeEuropean(owner), amount);
    }

    /**
     * Local pressure of a settlement from a later trigger, reduced by the
     * national and founder factors of its European.
     *
     * @param settlement required native settlement
     * @param owner      required colonial player
     * @param amount     finite nonnegative increase before the factors
     */
    void addNativeLocalAlarm(final NativeSettlement settlement,
            final Player owner, final double amount) {
        Preconditions.checkNotNull(settlement);
        final EuropeanNation european = nativeEuropean(owner);
        nativeRelations.addLocalAlarm(settlement, european, amount,
                nativeLocalFactor(european, owner));
    }

    /**
     * Clear a colonial European's positive native relations once, at the
     * arrival of Pocahontas in its Congress.
     *
     * @param owner required colonial player that has just received her
     */
    void clearNativeRelationsAfterPocahontas(final Player owner) {
        nativeRelations.clearEuropean(nativeEuropean(owner));
    }

    /**
     * Apply one completed native-relations outcome of a colonial European,
     * exactly once. The owner must be colonial; the outcome's references and
     * amounts are checked before any native relation changes.
     *
     * @param owner   required colonial player whose action has just completed
     * @param outcome required completed outcome, created at that commit
     */
    void applyNativeRelationOutcome(final Player owner,
            final NativeRelationOutcome outcome) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(outcome);
        final EuropeanNation european = nativeEuropean(owner);
        nativeRelations.applyOutcome(european, outcome,
                nativeLocalFactor(european, owner));
    }

    /**
     * Plan the native relation effect of a colonial attack on a native brave.
     * Nothing changes: the outcome gets its origin ID here and is applied once
     * by {@link #applyNativeRelationOutcome(Player, NativeRelationOutcome)}
     * when the battle commits.
     *
     * @param owner required colonial attacker
     * @param brave required native brave under attack
     * @return outcome of the attack, not yet applied
     */
    NativeRelationOutcome planNativeUnitAttack(final Player owner,
            final Unit brave) {
        Preconditions.checkArgument(owner.isColonialNation(),
                "Only a colonial European attacks a native brave");
        Preconditions.checkArgument(brave instanceof UnitNativeBrave,
                "Only a native brave has a native attack effect");
        final UnitNativeBrave nativeBrave = (UnitNativeBrave) brave;
        final NativeSettlement home = nativeBrave.getHomeSettlementId()
                .flatMap(id -> nativeSettlements.getAll().stream()
                        .filter(settlement -> settlement.getId() == id)
                        .findAny())
                .orElse(null);
        return NativeRelationOutcome.unitAttack(nativeRelations.nextOutcomeId(),
                nativeBrave.getNation(), home);
    }

    /**
     * Resolve the coastal batteries beside one legal ship step (R-0177). Every
     * eligible battery, in stable colony-ID order, fires at most once at this
     * ship in its owner's round. Eligible means an owned coastal colony with a
     * fort or fortress beside the new sea square, at war with the ship's owner.
     * A hit sends the ship to repair through the ordinary damage path and ends
     * the remaining shots; a miss leaves the ship on its course.
     *
     * @param ship required ship that has just completed a step
     * @param at   required sea square of that step
     */
    void resolveCoastalBatteries(final Ship ship, final Location at) {
        Preconditions.checkNotNull(ship);
        Preconditions.checkNotNull(at);
        final List<Colony> batteries = getColonies().stream()
                .filter(colony -> isCoastalBatteryFor(colony, ship, at))
                .sorted(Comparator.comparingInt(Colony::getId))
                .collect(Collectors.toList());
        for (final Colony colony : batteries) {
            // A hit that sent the ship to repair ends every remaining shot.
            if (!ship.isAtPlaceLocation() || !isUnitInGame(ship)) {
                return;
            }
            fireCoastalBattery(colony, ship);
        }
    }

    private boolean isCoastalBatteryFor(final Colony colony, final Ship ship,
            final Location at) {
        return colony.getLocation().isNeighbor(at)
                && getMap().getTerrainTypeAt(at).isSea()
                && (colony.wasConstructionBuilded(ConstructionType.FORT)
                        || colony.wasConstructionBuilded(
                                ConstructionType.FORTRESS))
                && isOwnedCoastalPort(colony.getOwner(), colony.getLocation())
                && areHostile(colony.getOwner(), ship.getOwner())
                && !coastalBatteryShots.contains(colony, ship);
    }

    /**
     * One battery shot: a single draw decides the hit against the ship's naval
     * defense. The shot is recorded and noticed before a hit is applied through
     * the ordinary ship damage path, which chooses the repair destination or
     * sinks the ship when none exists.
     */
    private void fireCoastalBattery(final Colony colony, final Ship ship) {
        final boolean hit = CombatStrengths
                .forCoastalBattery(this, colony, ship)
                .attackerWins(nextCombatRandom());
        coastalBatteryShots.record(colony, ship);
        getTurnEventStore().add(TurnEventProvider.getCoastalBattery(
                ship.getOwner(), colony.getId(), ship.getId(), hit));
        if (hit) {
            damageShip(ship);
        }
    }

    /**
     * The colony owner has begun its own turn preparation, so its batteries may
     * fire at ships again (R-0177). Other owners' records are unchanged.
     *
     * @param owner required player whose turn is starting
     */
    void renewCoastalBatteryShots(final Player owner) {
        Preconditions.checkNotNull(owner);
        coastalBatteryShots.renew(owner.getName());
    }

    /**
     * Check one legal ship step against the strongest eligible patrol beside
     * the new square. The random stream is drawn once, and only when a patrol
     * qualifies. The check lowers the ship's remaining movement and nothing
     * else: it starts no battle and changes no cargo, gold or relation. A
     * slowdown or stop is recorded for the ship's owner; an undetected passage
     * leaves no trace of the patrol.
     *
     * @param mover required ship that has just completed a step
     * @param at    required square of that step
     */
    void resolveShipPatrol(final Ship mover, final Location at) {
        Preconditions.checkNotNull(mover);
        Preconditions.checkNotNull(at);
        final Optional<Ship> patrol = findPatrol(mover, at);
        if (patrol.isEmpty()) {
            return;
        }
        final int before = mover.getMovementThirds();
        final int after = ShipPatrolRules.remainingAfter(
                ShipPatrolRules.draw(nextCombatRandom()), before);
        mover.setMovementThirds(after);
        if (after < before) {
            getTurnEventStore().add(TurnEventProvider.getPatrolInterception(
                    mover.getOwner(), patrol.get().getId(), after == 0));
        }
    }

    /**
     * The patrol that checks a step onto this square, or empty. Any live armed
     * ship of another owner beside the square qualifies when its owner is at
     * war with the mover's owner. A privateer also qualifies at peace when it
     * could legally attack the mover; other armed ships never do. The strongest
     * attacker wins, and the lowest stable ID breaks a tie.
     *
     * @param mover required ship that has just stepped
     * @param at    required square of that step
     * @return the chosen patrol, or empty when no ship qualifies
     */
    Optional<Ship> findPatrol(final Ship mover, final Location at) {
        return at.getNeighbors().stream()
                .flatMap(neighbor -> getUnitsAt(neighbor).stream())
                .filter(unit -> unit instanceof Ship && unit != mover)
                .map(Ship.class::cast)
                .filter(patrol -> isEligiblePatrol(patrol, mover))
                .min(Comparator
                        .<Ship>comparingDouble(
                                patrol -> -attackStrength(patrol, mover))
                        .thenComparingInt(Unit::getId));
    }

    private boolean isEligiblePatrol(final Ship patrol, final Ship mover) {
        if (patrol.getOwner() == mover.getOwner() || !patrol.canAttack()
                || !patrol.isAtPlaceLocation()
                || !patrol.getType().getAttackableUnitType()
                        .contains(mover.getType())
                || CombatStrengths.forBattle(this, patrol, mover).isEmpty()) {
            return false;
        }
        return areHostile(patrol.getOwner(), mover.getOwner())
                || patrol.getType() == UnitType.PRIVATEER
                        && isPrivateerNavalException(patrol, mover);
    }

    private double attackStrength(final Ship patrol, final Ship mover) {
        return CombatStrengths.forBattle(this, patrol, mover)
                .map(strengths -> strengths.getAttacker().getTotal())
                .orElse(0.0);
    }

    /**
     * Resolve a lost-city rumor once, after a legal scout step onto its square.
     * The entry is validated before anything changes. Then the marker is
     * consumed, the outcome is drawn from the persisted random state and
     * applied in this same action, and one turn notice records the actual
     * result. Any other entry leaves the model and its random state unchanged.
     *
     * @param explorer required unit that has just stepped onto the square
     * @param at       required square of that step
     */
    void resolveLostCityRumor(final Unit explorer, final Location at) {
        Preconditions.checkNotNull(explorer);
        Preconditions.checkNotNull(at);
        if (!isLostCityRumorEntry(explorer, at)) {
            return;
        }
        final Player owner = explorer.getOwner();
        // Every random draw happens before the first change. The commits below
        // are infallible once canCommitLostCityRumor accepted the drawn result;
        // a refused result restores the random state, so nothing changed.
        final long randomBefore = combatRandomState;
        final LostCityRumorRules.Outcome drawn = LostCityRumorRules.draw(
                explorer.getType() == UnitType.SEASONED_SCOUT,
                nextCombatRandom());
        final LostCityRumorRules.Outcome outcome = LostCityRumorRules
                .withDeSoto(drawn, owner.getCongressMembers()
                        .contains(Founder.HERNANDO_DE_SOTO));
        final int gold = outcome == LostCityRumorRules.Outcome.GOLD
                ? LostCityRumorRules.gold(nextCombatRandom())
                : 0;
        final Optional<NativeSettlement> nearest = outcome == LostCityRumorRules.Outcome.BURIAL
                ? nearestNativeSettlement(at)
                : Optional.empty();
        final NativeNation nation = nearest.map(NativeSettlement::getNation)
                .orElse(null);
        if (!canCommitLostCityRumor(owner, outcome, gold,
                nearest.isPresent())) {
            combatRandomState = randomBefore;
            return;
        }
        map.removeLostCityRumor(at);
        if (outcome == LostCityRumorRules.Outcome.GOLD) {
            owner.setGold(owner.getGold() + gold);
        } else if (outcome == LostCityRumorRules.Outcome.RECRUIT) {
            addFreeColonistAt(owner, at);
        } else if (outcome == LostCityRumorRules.Outcome.EXPLORER_LOST) {
            // A lost explorer keeps no go-to order and no square.
            ((AbstractUnit) explorer).discardGoToOrder();
            explorer.getPlace().destroy();
            destroyUnit(explorer);
        } else if (nearest.isPresent()) {
            applyNativeRelationOutcome(owner, NativeRelationOutcome
                    .burial(nativeRelations.nextOutcomeId(), nation));
        }
        getTurnEventStore().add(TurnEventProvider.getLostCityRumor(owner,
                outcome, explorer.getId(), gold, nation, outcome != drawn));
    }

    /**
     * A scout that stands on the square after its own step, belonging to a
     * colonial player, resolves the unresolved rumor there once. Anything else,
     * including a unit that is not live in this game, is not an entry and
     * changes nothing.
     */
    private boolean isLostCityRumorEntry(final Unit explorer,
            final Location at) {
        return explorer instanceof UnitFreeColonist
                && ((UnitFreeColonist) explorer).isScout()
                && isUnitInGame(explorer) && explorer.isAtPlaceLocation()
                && explorer.getLocation().equals(at)
                && explorer.getOwner().isColonialNation()
                && map.hasLostCityRumorAt(at);
    }

    /**
     * Whether the drawn result can be applied completely. Only the result that
     * needs a resource checks it: gold must fit the treasury, a recruit needs a
     * free unit ID and a burial needs a European whose anger can change. The
     * other results never depend on those.
     */
    private boolean canCommitLostCityRumor(final Player owner,
            final LostCityRumorRules.Outcome outcome, final int gold,
            final boolean angersNative) {
        switch (outcome) {
        case GOLD:
            return owner.getGold() <= Integer.MAX_VALUE - gold;
        case RECRUIT:
            return unitStorage.canAllocateUnit();
        case BURIAL:
            return !angersNative || nativeNationOf(owner).isPresent();
        default:
            return true;
        }
    }

    /**
     * @param unit required unit
     * @return whether this exact unit is still registered in the game, so a
     *         unit lost in the middle of a path is never moved again
     */
    public boolean isUnitInGame(final Unit unit) {
        Preconditions.checkNotNull(unit);
        return unitStorage.tryGetUnitById(unit.getId())
                .filter(found -> found == unit).isPresent();
    }

    /**
     * Nearest native settlement by Chebyshev distance, among every settlement.
     * A distance tie is decided uniformly with one draw from the persisted
     * random state; no settlement at all means no nation is affected.
     */
    private Optional<NativeSettlement> nearestNativeSettlement(
            final Location at) {
        final List<NativeSettlement> settlements = nativeSettlements.getAll();
        if (settlements.isEmpty()) {
            return Optional.empty();
        }
        final int nearest = settlements.stream().mapToInt(
                settlement -> settlement.getLocation().getDistanceChebyshev(at))
                .min().getAsInt();
        final List<NativeSettlement> tied = settlements.stream()
                .filter(settlement -> settlement.getLocation()
                        .getDistanceChebyshev(at) == nearest)
                .collect(Collectors.toList());
        if (tied.size() == 1) {
            return Optional.of(tied.get(0));
        }
        return Optional.of(tied.get((int) (nextCombatRandom() * tied.size())));
    }

    /**
     * Recruit: one free colonist on the explored square, owned by the owner.
     */
    private void addFreeColonistAt(final Player owner, final Location at) {
        unitStorage.createUnit(
                unit -> new Cargo(unit, UnitType.COLONIST.getCargoCapacity()),
                this,
                unit -> new PlaceLocation(unit, at,
                        unit.getDefaultOrintation()),
                UnitType.COLONIST, owner,
                MovementPoints.ofPoints(UnitType.COLONIST.getSpeed()),
                new UnitActionNoAction());
    }

    /**
     * Quote one native-claimed square for a colonial European. The player must
     * have explored the square, and must have learned the claim owner;
     * otherwise nothing about the claim is revealed.
     *
     * @param player required colonial player
     * @param at     required square
     * @return quote, or empty when the square is not a known claimed land
     *         square
     */
    public Optional<NativeLandQuote> quoteNativeLand(final Player player,
            final Location at) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(at);
        final Optional<EuropeanNation> nation = nativeNationOf(player);
        if (nation.isEmpty() || !map.isValid(at)
                || map.getTerrainTypeAt(at).isSea() || !player.isVisible(at)) {
            return Optional.empty();
        }
        final Optional<NativeSettlement> claimant = nativeSettlements
                .getClaimOwner(at);
        if (claimant.isEmpty() || getNativeSettlementKnownTo(player,
                claimant.get().getLocation()).isEmpty()) {
            return Optional.empty();
        }
        final NativeLandStatus status;
        if (nativeLandRights.isPaid(nation.get(), at)) {
            status = NativeLandStatus.PAID;
        } else if (nativeLandWaived(player)) {
            status = NativeLandStatus.WAIVED;
        } else {
            status = NativeLandStatus.UNPAID;
        }
        final int price = status == NativeLandStatus.UNPAID
                ? nativeLandPrice(claimant.get())
                : 0;
        return Optional.of(
                new NativeLandQuote(player, at, claimant.get(), status, price));
    }

    /**
     * Whether a quote can be paid now. Every live fact is read again: running
     * game, current player, the same claim owner, the same unpaid status and
     * price, and enough gold. A stale quote is refused, never repriced.
     *
     * @param quote quote from {@link #quoteNativeLand}, possibly stale
     * @return whether {@link #buyNativeLand} would charge it
     */
    public boolean canBuyNativeLand(final NativeLandQuote quote) {
        if (quote == null || !isGameRunning()
                || getCurrentPlayer() != quote.getPlayer()
                || quote.getStatus() != NativeLandStatus.UNPAID) {
            return false;
        }
        final Player player = quote.getPlayer();
        final Optional<NativeLandQuote> live = quoteNativeLand(player,
                quote.getLocation());
        return live.isPresent()
                && live.get().getStatus() == NativeLandStatus.UNPAID
                && live.get().getClaimant().getId() == quote.getClaimant()
                        .getId()
                && live.get().getPrice() == quote.getPrice()
                && player.getGold() >= quote.getPrice() && quote.getClaimant()
                        .getTreasury() <= Integer.MAX_VALUE - quote.getPrice();
    }

    /**
     * Pay for one claimed square. The right is recorded and the claim owner's
     * treasury grows before the European's gold changes, so every gold listener
     * sees the finished result. A refused quote changes neither side.
     *
     * @param quote required quote that {@link #canBuyNativeLand} accepts
     */
    public void buyNativeLand(final NativeLandQuote quote) {
        checkTurnPreparationComplete();
        Preconditions.checkArgument(canBuyNativeLand(quote),
                "Native land cannot be bought now");
        final Player player = quote.getPlayer();
        // Every arithmetic result is computed before the first mutation, so an
        // overflow leaves the right, treasury and gold unchanged.
        final int treasuryAfter = Math
                .addExact(quote.getClaimant().getTreasury(), quote.getPrice());
        final int goldAfter = Math.subtractExact(player.getGold(),
                quote.getPrice());
        nativeLandRights.addPaid(nativeEuropean(player), quote.getLocation());
        quote.getClaimant().setTreasury(treasuryAfter);
        player.setGold(goldAfter);
    }

    /**
     * Record a successful use of a square by a colonial European: founding, a
     * worker placed in a field, or a completed plow. The first unpaid use of a
     * claimed square adds the one-time taking to its claim owner's local alarm,
     * once per European and square. Paid squares, Minuit's waiver, unclaimed
     * squares and repeated use change nothing.
     *
     * @param owner required player that used the square
     * @param at    required square
     */
    public void recordNativeLandUse(final Player owner, final Location at) {
        Preconditions.checkNotNull(owner);
        Preconditions.checkNotNull(at);
        final Optional<EuropeanNation> nation = nativeNationOf(owner);
        if (nation.isEmpty() || nativeLandWaived(owner)) {
            return;
        }
        final Optional<NativeSettlement> claimant = nativeSettlements
                .getClaimOwner(at);
        if (claimant.isEmpty() || nativeLandRights.isPaid(nation.get(), at)
                || nativeLandRights.isTaken(nation.get(), at)) {
            return;
        }
        nativeLandRights.addTaken(nation.get(), at);
        addNativeLocalAlarm(claimant.get(), owner, NATIVE_UNPAID_TAKING_ALARM);
    }

    /**
     * Gold for one claimed square: the accepted profile factor of the base
     * price, doubled when the claim belongs to a capital. Factors are whole
     * percents, so the rounded-up price is exact.
     */
    private int nativeLandPrice(final NativeSettlement claimant) {
        final int percent;
        switch (getDifficulty()) {
        case DISCOVERER:
            percent = 75;
            break;
        case EXPLORER:
            percent = 90;
            break;
        case CONQUISTADOR:
            percent = 100;
            break;
        case GOVERNOR:
            percent = 115;
            break;
        case VICEROY:
            percent = 130;
            break;
        default:
            throw new IllegalStateException("Unknown difficulty");
        }
        return claimant.isCapital() ? 2 * percent : percent;
    }

    /**
     * Peter Minuit's membership waives future land payments and unpaid-land
     * alarm. Earlier payments are not refunded.
     */
    private static boolean nativeLandWaived(final Player player) {
        return player.getCongressMembers().contains(Founder.PETER_MINUIT);
    }

    private static Optional<EuropeanNation> nativeNationOf(
            final Player player) {
        return player.isKing() ? Optional.empty()
                : EuropeanNation.findByPlayerName(player.getName());
    }

    private static EuropeanNation nativeEuropean(final Player player) {
        Preconditions.checkNotNull(player);
        return nativeNationOf(player)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Native relations need a colonial European"));
    }

    /**
     * Multiplier of positive local alarm: France halves it, and Pocahontas
     * halves it again while she is a member. Direct tribal anger is not
     * multiplied.
     */
    private static double nativeLocalFactor(final EuropeanNation european,
            final Player owner) {
        double factor = 1;
        if (european == EuropeanNation.FRENCH) {
            factor *= 0.5;
        }
        if (owner.getCongressMembers().contains(Founder.POCAHONTAS)) {
            factor *= 0.5;
        }
        return factor;
    }

    /**
     * Positive local inputs of one settlement from one European's own units and
     * colonies. Each unit is placed once, by the square it stands on or the
     * colony it works in; cargo and units outside the map count nothing. A
     * resident within the home radius adds 0.25, and an armed unit within two
     * squares adds 2 on top, so an armed colonist can count for both. Each
     * colony within the home radius adds 3 with a fortress or 2 with a fort.
     * Unpaid claimed land in use adds its own input, see
     * {@link #nativeUnpaidUsePressure}.
     */
    private double nativeLocalPressure(final NativeSettlement settlement,
            final Player owner) {
        return nativeSourcePressure(settlement, owner, square -> true)
                + nativeUnpaidUsePressure(settlement, owner);
    }

    /**
     * Visible pressure sources of one European, the units and colonies counted
     * by {@link #nativeLocalPressure} without unpaid land. Only sources on a
     * square accepted by {@code seen} count, so a rival's pressure is read from
     * what the viewer can currently see.
     */
    private double nativeSourcePressure(final NativeSettlement settlement,
            final Player owner, final Predicate<Location> seen) {
        final Location home = settlement.getLocation();
        final int radius = settlement.getSettlementClass().getHomeRadius();
        double pressure = 0;
        for (final Unit unit : owner.getAllUnits()) {
            final Optional<Location> square = nativeResidentSquare(unit, owner);
            if (square.isEmpty() || !seen.test(square.get())) {
                continue;
            }
            final int distance = home.getDistanceChebyshev(square.get());
            if (isNativeResident(unit) && distance <= radius) {
                pressure += NATIVE_RESIDENT_PRESSURE;
            }
            if (isNativeArmed(unit) && distance <= NATIVE_ARMED_RADIUS) {
                pressure += NATIVE_ARMED_PRESSURE;
            }
        }
        for (final Colony colony : colonies) {
            if (colony.getOwner() == owner && seen.test(colony.getLocation())
                    && home.getDistanceChebyshev(
                            colony.getLocation()) <= radius) {
                if (colony.wasConstructionBuilded(ConstructionType.FORTRESS)) {
                    pressure += 3;
                } else if (colony
                        .wasConstructionBuilded(ConstructionType.FORT)) {
                    pressure += 2;
                }
            }
        }
        return pressure;
    }

    /**
     * The rival European whose currently visible sources press hardest on the
     * settlement. Unpaid land is never counted, since its use is hidden. Ties
     * keep the earlier player in the saved order; no visible source means none.
     */
    private Optional<Player> visibleNativeProvoker(
            final NativeSettlement settlement, final Player viewer) {
        Player provoker = null;
        double strongest = 0;
        for (final Player rival : playerStore.getPlayers()) {
            if (rival == viewer || nativeNationOf(rival).isEmpty()) {
                continue;
            }
            final double visible = nativeSourcePressure(settlement, rival,
                    viewer::isCurrentlyVisible);
            if (visible > strongest) {
                strongest = visible;
                provoker = rival;
            }
        }
        return Optional.ofNullable(provoker);
    }

    /**
     * Unpaid land in use by one European, counted once per square: each owned
     * colony square and each field with a worker. Squares with a paid right and
     * every square while Peter Minuit is a member add nothing. Only the claim
     * owner of a square receives its pressure.
     */
    private double nativeUnpaidUsePressure(final NativeSettlement settlement,
            final Player owner) {
        if (nativeLandWaived(owner)) {
            return 0;
        }
        final EuropeanNation european = nativeEuropean(owner);
        // A square taken unpaid stays in use for pressure until it is paid or
        // Minuit waives it, even when only a road or plow remains there. The
        // set deduplicates it with colony squares and worked fields.
        final Set<Location> used = new HashSet<>(
                nativeLandRights.takenBy(european));
        for (final Colony colony : colonies) {
            if (colony.getOwner() == owner) {
                used.add(colony.getLocation());
                colony.getColonyFields().stream()
                        .filter(ColonyField::isNotEmpty)
                        .forEach(field -> used.add(field.getLocation()));
            }
        }
        final long unpaid = used.stream()
                .filter(at -> !nativeLandRights.isPaid(european, at))
                .filter(at -> nativeSettlements.getClaimOwner(at)
                        .orElse(null) == settlement)
                .count();
        return unpaid * NATIVE_UNPAID_USE_PRESSURE;
    }

    /**
     * A resident is any land unit of the owner except a wagon; ships are not
     * residents. Residency does not depend on armament, so an armed colonist
     * within the home radius counts both as a resident and as armed.
     */
    private static boolean isNativeResident(final Unit unit) {
        return !unit.isShip() && unit.getType() != UnitType.WAGON;
    }

    /**
     * Armed land units are artillery and colonists holding guns, as in combat.
     * An armed ship, one that can attack, also counts as an armed unit.
     */
    private static boolean isNativeArmed(final Unit unit) {
        return CombatStrengths.isArmedLand(unit)
                || unit.isShip() && unit.canAttack();
    }

    private Optional<Location> nativeResidentSquare(final Unit unit,
            final Player owner) {
        if (unit.isAtPlaceLocation()) {
            return Optional.of(unit.getLocation());
        }
        return colonies.stream()
                .filter(colony -> colony.getOwner() == owner
                        && colony.getUnitsInsideColony().contains(unit))
                .map(Colony::getLocation).findFirst();
    }

    /**
     * Restore which native settlements each player has learned. The list is
     * required in the current schema; an empty list explicitly means nothing
     * has been learned yet and a missing one is rejected. Every entry must name
     * a known player and known settlements, without duplicates.
     */
    private Map<String, Set<Integer>> restoreLearnedNativeSettlements(
            final List<NativeSettlementKnowledgePo> saved) {
        Preconditions.checkArgument(saved != null,
                "Unsupported save: missing native settlement knowledge");
        final Map<String, Set<Integer>> out = new HashMap<>();
        for (final NativeSettlementKnowledgePo entry : saved) {
            Preconditions.checkArgument(
                    entry != null && entry.getPlayerName() != null
                            && entry.getSettlementIds() != null,
                    "Unsupported save: incomplete native settlement knowledge");
            Preconditions.checkArgument(
                    playerStore.getPlayers().stream()
                            .anyMatch(player -> player.getName()
                                    .equals(entry.getPlayerName())),
                    "Unsupported save: unknown player in native settlement knowledge");
            final Set<Integer> ids = new HashSet<>();
            for (final Integer id : entry.getSettlementIds()) {
                Preconditions.checkArgument(
                        id != null && ids.add(id)
                                && nativeSettlements.getAll().stream()
                                        .anyMatch(s -> s.getId() == id),
                        "Unsupported save: unknown or duplicate learned native settlement");
            }
            Preconditions.checkArgument(
                    out.put(entry.getPlayerName(), ids) == null,
                    "Unsupported save: duplicate native settlement knowledge");
        }
        return out;
    }

    /**
     * Record native settlements inside each player's present sight. Only sight
     * teaches identity; a square that was merely explored does not.
     */
    void learnNativeSettlements() {
        for (final Player player : playerStore.getPlayers()) {
            final Set<Integer> learned = learnedNativeSettlements
                    .computeIfAbsent(player.getName(), name -> new HashSet<>());
            for (final NativeSettlement settlement : nativeSettlements
                    .getAll()) {
                if (player.isCurrentlyVisible(settlement.getLocation())) {
                    learned.add(settlement.getId());
                }
            }
        }
    }

    private List<NativeSettlementKnowledgePo> saveLearnedNativeSettlements() {
        final List<NativeSettlementKnowledgePo> out = new ArrayList<>();
        for (final Player player : playerStore.getPlayers()) {
            final Set<Integer> learned = learnedNativeSettlements
                    .get(player.getName());
            if (learned != null && !learned.isEmpty()) {
                final List<Integer> ids = new ArrayList<>(learned);
                ids.sort(null);
                final NativeSettlementKnowledgePo po = new NativeSettlementKnowledgePo();
                po.setPlayerName(player.getName());
                po.setSettlementIds(ids);
                out.add(po);
            }
        }
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
        Preconditions.checkState(!hasGoToOrder(unit),
                "Cancel the go-to order before moving manually");
        listenerManager.fireActionStarted(this);
        try {
            // Reject the route before movement listeners disable UI controls.
            if (unit.isAtPlaceLocation()) {
                unit.verifyPathIsLegal(path.getLocations());
                Preconditions.checkState(
                        unit.canAffordPath(path.getLocations()),
                        "Unit %s cannot afford the path", unit);
            }
            if (fireUnitMoveStarted(unit, path)) {
                boolean intercepted = false;
                for (final Location loc : path.getLocations()) {
                    /*
                     * Unit could move to high seas, so could be out of map, or
                     * be lost on a step to a lost-city rumor. Neither moves
                     * again.
                     */
                    if (!unit.isAtPlaceLocation() || !isUnitInGame(unit)) {
                        break;
                    }
                    // Once any patrol on this route took more movement than its
                    // step cost, the walk ends at the first step it can no
                    // longer
                    // pay. Without one, an unaffordable step is not skipped;
                    // moveOneStep refuses it.
                    if (intercepted && !unit.canAffordStepTo(loc)) {
                        break;
                    }
                    final int expected = Math.max(0, unit.getMovementThirds()
                            - unit.getEntryCostThirds(unit.getLocation(), loc));
                    unit.moveOneStep(loc);
                    intercepted |= unit.isAtPlaceLocation()
                            && unit.getMovementThirds() < expected;
                }
                fireUnitMovedFinished(unit, path);
                // The walk is complete, so a resident who left a colony may now
                // end it; no step of the walk is judged (R-0192).
                checkRebellionLoss();
            }
        } finally {
            listenerManager.fireActionEnded(this);
        }
    }

    boolean fireUnitMoveStarted(final Unit unit, final Path path) {
        return listenerManager.fireUnitMoveStarted(this, unit, path);
    }

    void fireUnitMovedFinished(final Unit unit, final Path path) {
        listenerManager.fireUnitMovedFinished(this, unit, path);
        learnNativeSettlements();
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
        Preconditions.checkState(!hasGoToOrder(unit),
                "Cancel the go-to order before moving manually");
        if (listenerManager.fireUnitMoveStarted(this, unit, path)) {
            for (final Location loc : path.getLocations()) {
                /*
                 * Check if unit is at place location is reasonable, because
                 * unit could in first step conquer city a be placed inside
                 * city. The first step it cannot pay ends the walk.
                 */
                if (!unit.isAtPlaceLocation() || !isUnitInGame(unit)
                        || !unit.canAffordStepTo(loc)) {
                    break;
                }
                unit.moveOneStep(loc);
            }
            listenerManager.fireUnitMovedFinished(this, unit, path);
            learnNativeSettlements();
            // The walk is complete; judged once, never between its steps.
            checkRebellionLoss();
        }
    }

    public synchronized void startGame() {
        Preconditions.checkState(!endingTurn,
                "Cannot start or end game while ending a turn.");
        if (gameManager.isRunning() || gameManager.isAwaitingImmigrationChoice()
                || gameManager.isAwaitingTaxDecision()
                || gameManager.isAwaitingScoreChoice()) {
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

    /** @return whether the owner's turn preparation has finished for reads */
    private boolean isTurnPreparationComplete() {
        try {
            checkTurnPreparationComplete();
            return true;
        } catch (final TurnPreparationException busy) {
            return false;
        }
    }

    void checkCurrentPlayer(final Player player) {
        gameManager.checkCurrentPlayer(player);
    }

    /**
     * Current legal terms: own galleon in Europe, or royal pickup at an owned
     * coastal colony.
     */
    public Optional<TreasureRedemptionQuote> quoteTreasureRedemption(
            final Unit unit, final Colony colony) {
        if (!(unit instanceof org.microcol.model.unit.UnitTreasure)
                || !isGameRunning() || getCurrentPlayer() != unit.getOwner()
                || !treasureRegistered(unit)
                || !unit.getOwner().canTradeInEurope()) {
            return Optional.empty();
        }
        final org.microcol.model.unit.UnitTreasure train = (org.microcol.model.unit.UnitTreasure) unit;
        if (train.getPickupColonyId() != null) {
            return Optional.empty();
        }
        final boolean eligible;
        if (colony == null) {
            eligible = train.isAtCargoSlot()
                    && train.getPlaceCargoSlot().getOwnerUnit()
                            .getType() == UnitType.GALLEON
                    && train.getPlaceCargoSlot().getOwnerUnit()
                            .getOwner() == train.getOwner()
                    && train.getPlaceCargoSlot().getOwnerUnit()
                            .isAtEuropePort();
        } else {
            eligible = isTreasureAtPort(train, colony);
        }
        if (!eligible) {
            return Optional.empty();
        }
        final TreasureRedemptionQuote quote = new TreasureRedemptionQuote(train,
                colony);
        return (long) quote.getNet()
                + train.getOwner().getGold() <= Integer.MAX_VALUE
                        ? Optional.of(quote)
                        : Optional.empty();
    }

    private boolean treasureRegistered(final Unit unit) {
        return tryGetUnitById(unit.getId()).orElse(null) == unit
                && getPlayers().contains(unit.getOwner())
                && unit.getOwner().isColonialNation();
    }

    private boolean isTreasureAtPort(final Unit train, final Colony colony) {
        return getColonyAt(colony.getLocation()).orElse(null) == colony
                && colony.getOwner() == train.getOwner()
                && train.isAtPlaceLocation()
                && train.getLocation().equals(colony.getLocation())
                && isOwnedCoastalPort(train.getOwner(), colony.getLocation());
    }

    /**
     * Revalidate all displayed terms before any removal, payment or
     * reservation.
     */
    public void redeemTreasure(final TreasureRedemptionQuote offered) {
        Preconditions.checkNotNull(offered);
        checkTurnPreparationComplete();
        final TreasureRedemptionQuote live = quoteTreasureRedemption(
                offered.train, offered.colony)
                .orElseThrow(() -> new IllegalStateException(
                        "Treasure redemption is no longer available"));
        Preconditions.checkState(
                live.owner == offered.owner && live.taxRate == offered.taxRate
                        && live.gold == offered.gold
                        && live.getNet() == offered.getNet(),
                "Treasure proceeds changed");
        if (live.isRoyalPickup()) {
            live.train.setPickup(live.colony.getId(), live.getNet());
        } else {
            payTreasure(live.train, live.getNet());
        }
    }

    private void payTreasure(final org.microcol.model.unit.UnitTreasure train,
            final int net) {
        final Player owner = train.getOwner();
        final int gold = Math.addExact(owner.getGold(), net);
        destroyUnit(train);
        owner.setGold(gold);
    }

    /** Once per owner update; opening a save never collects a pending train. */
    void collectRoyalTreasure(final Player owner) {
        for (final Unit unit : ImmutableList.copyOf(owner.getAllUnits())) {
            if (!(unit instanceof org.microcol.model.unit.UnitTreasure)) {
                continue;
            }
            final org.microcol.model.unit.UnitTreasure train = (org.microcol.model.unit.UnitTreasure) unit;
            if (train.getPickupColonyId() == null) {
                continue;
            }
            final Colony colony = getColonyById(train.getPickupColonyId())
                    .orElse(null);
            final int net = train.getPickupNet();
            final boolean eligible = treasureRegistered(train)
                    && owner.canTradeInEurope() && colony != null
                    && isTreasureAtPort(train, colony)
                    && (long) owner.getGold() + net <= Integer.MAX_VALUE;
            train.setPickup(null, null);
            if (eligible) {
                payTreasure(train, net);
            }
        }
    }

    void destroyUnit(final Unit unit) {
        Preconditions.checkNotNull(unit, "Unit is null");
        // A passenger still boarded releases its holds once, so no hold keeps
        // naming a removed unit; a ship destroyed first has already done so.
        if (unit.isAtCargoSlot() && unit.getPlaceCargoSlot().getCargoSlot()
                .getUnit().orElse(null) == unit) {
            unit.getPlace().destroy();
        }
        if (unit.canHoldCargo()) {
            final UnitWithCargo unitWithCargo = (UnitWithCargo) unit;
            unitWithCargo.getCargo().getSlots().stream().forEach(cargoSlot -> {
                if (cargoSlot.isLoadedUnit()) {
                    final Unit u = cargoSlot.getUnit().get();
                    destroyUnit(u);
                }
            });
        }
        recordRoyalLoss(unit);
        unitStorage.remove(unit);
    }

    public void sellGoods(final CargoSlot cargoSlot, final Goods goods) {
        cargoSlot.sellAndEmpty(goods);
        listenerManager.fireGoodsWasSoldInEurope(this, goods);
    }

    /**
     * Sell only when the price still matches the quote the player confirmed.
     */
    public void sellGoods(final CargoSlot cargoSlot, final Goods goods,
            final EuropeTradeQuote offered) {
        cargoSlot.sellAndEmpty(goods,
                Preconditions.checkNotNull(offered, "Offered quote is null"));
        listenerManager.fireGoodsWasSoldInEurope(this, goods);
    }

    /** Goods that quick loading never moves, even when they are in stock. */
    private static final List<GoodsType> QUICK_LOAD_EXCLUDED = ImmutableList
            .of(GoodsType.HORSE, GoodsType.TOOLS, GoodsType.MUSKET);

    /**
     * Choose the one quick load or unload for a carrier at its own colony. Only
     * the model ranks goods and selects a hold; a refusal explains the reason
     * and changes nothing.
     *
     * @param carrier required unit, or {@code null} when none is selected
     * @param action  required direction of the transfer
     * @return the offer to confirm, or the reason the action is unavailable
     */
    public QuickCargoChoice getQuickCargoChoice(final Unit carrier,
            final QuickCargoAction action) {
        Preconditions.checkNotNull(action, "Quick cargo action is null");
        if (!canUseQuickCargo(carrier)) {
            return QuickCargoChoice.refused(QuickCargoProblem.UNAVAILABLE);
        }
        final UnitWithCargo cargoCarrier = (UnitWithCargo) carrier;
        final Optional<Colony> colony = getColonyAt(carrier.getLocation())
                .filter(candidate -> candidate.isValid()
                        && candidate.getOwner() == carrier.getOwner()
                        && candidate.getUnitsInPort().contains(carrier));
        if (colony.isEmpty()) {
            return QuickCargoChoice.refused(QuickCargoProblem.UNAVAILABLE);
        }
        return action == QuickCargoAction.LOAD
                ? planQuickLoad(cargoCarrier, colony.get())
                : planQuickUnload(cargoCarrier, colony.get());
    }

    /**
     * Perform a quick load or unload after the player confirmed its offer. The
     * live choice must still equal the confirmed terms; only then the existing
     * carrier and warehouse operations move the goods, and they validate again.
     * No treasury changes.
     *
     * @param confirmed the exact offer the player confirmed
     * @throws IllegalStateException when the terms changed or the phase forbids
     *                               the transfer
     */
    public void confirmQuickCargo(final QuickCargoOffer confirmed) {
        Preconditions.checkNotNull(confirmed, "Confirmed offer is required");
        Preconditions.checkState(
                getQuickCargoChoice(confirmed.getCarrier(),
                        confirmed.getAction()).getOffer()
                        .filter(confirmed::equals).isPresent(),
                "Quick cargo offer is no longer valid");
        final CargoSlot hold = confirmed.getCarrier().getCargo()
                .getSlotByIndex(confirmed.getSlotIndex());
        if (confirmed.getAction() == QuickCargoAction.LOAD) {
            hold.storeFromColonyWarehouse(confirmed.getGoods(),
                    confirmed.getColony());
        } else {
            confirmed.getColony().getWarehouse()
                    .moveToWarehouse(confirmed.getGoods(), hold);
        }
    }

    /** Live phase, owner and placement checks shared by both directions. */
    private boolean canUseQuickCargo(final Unit carrier) {
        if (!(carrier instanceof UnitWithCargo) || !isGameRunning()
                || endingTurn || getCurrentPlayer() != carrier.getOwner()
                || unitStorage.tryGetUnitById(carrier.getId())
                        .orElse(null) != carrier
                || !carrier.isAtPlaceLocation()) {
            return false;
        }
        if (carrier instanceof Ship && ((Ship) carrier).isDamaged()) {
            return false;
        }
        try {
            checkTurnPreparationComplete();
            return true;
        } catch (final TurnPreparationException busy) {
            return false;
        }
    }

    private QuickCargoChoice planQuickLoad(final UnitWithCargo carrier,
            final Colony colony) {
        final ColonyWarehouse warehouse = colony.getWarehouse();
        final List<GoodsType> stocked = GoodsType.BUYABLE_GOOD_TYPES.stream()
                .filter(type -> !QUICK_LOAD_EXCLUDED.contains(type))
                .filter(type -> warehouse.getGoods(type).getAmount() > 0)
                .sorted(Comparator.comparing(GoodsType::name))
                .collect(ImmutableList.toImmutableList());
        if (stocked.isEmpty()) {
            return QuickCargoChoice.refused(QuickCargoProblem.NO_STOCK);
        }
        // Goods are sorted by name, so a tie keeps the lower goods ID.
        Optional<EuropeTradeQuote> best = Optional.empty();
        for (final GoodsType type : stocked) {
            if (compatibleHold(carrier, type).isPresent()) {
                final EuropeTradeQuote quote = carrier.getOwner()
                        .quoteEuropeTrade(Goods.of(type, 1), false);
                if (best.isEmpty()
                        || quote.getTotal() > best.get().getTotal()) {
                    best = Optional.of(quote);
                }
            }
        }
        if (best.isEmpty()) {
            return QuickCargoChoice.refused(QuickCargoProblem.NO_FREE_HOLD);
        }
        final GoodsType type = best.get().getGoods().getType();
        final CargoSlot hold = compatibleHold(carrier, type).orElseThrow();
        final int stock = warehouse.getGoods(type).getAmount();
        final int holdStock = hold.getGoods().map(Goods::getAmount).orElse(0);
        return QuickCargoChoice.of(new QuickCargoOffer(carrier, colony,
                QuickCargoAction.LOAD, carrier.getLocation(), hold.getIndex(),
                Goods.of(type, Math.min(stock, hold.getAvailableCapacity())),
                stock, warehouse.getFreeStorage(type), holdStock,
                hold.getAvailableCapacity(), best.get()));
    }

    private QuickCargoChoice planQuickUnload(final UnitWithCargo carrier,
            final Colony colony) {
        final Optional<CargoSlot> hold = carrier.getCargo().getSlots().stream()
                .filter(slot -> slot.getGoods().isPresent()).findFirst();
        if (hold.isEmpty()) {
            return QuickCargoChoice.refused(QuickCargoProblem.NO_GOODS);
        }
        final ColonyWarehouse warehouse = colony.getWarehouse();
        final Goods held = hold.get().getGoods().get();
        final Goods transfer;
        try {
            transfer = warehouse.getUnloadableGoods(held, hold.get());
        } catch (final IllegalArgumentException rejected) {
            // The warehouse accepts only cargo goods; anything else is refused.
            return QuickCargoChoice.refused(QuickCargoProblem.UNAVAILABLE);
        }
        if (transfer.isZero()) {
            return QuickCargoChoice.refused(QuickCargoProblem.WAREHOUSE_FULL);
        }
        return QuickCargoChoice.of(new QuickCargoOffer(carrier, colony,
                QuickCargoAction.UNLOAD, carrier.getLocation(),
                hold.get().getIndex(), transfer,
                warehouse.getGoods(held.getType()).getAmount(),
                warehouse.getFreeStorage(held.getType()), held.getAmount(),
                hold.get().getAvailableCapacity(), null));
    }

    /**
     * @return the partly filled hold of this goods with the lowest index, or
     *         else the lowest empty hold; passenger holds never qualify
     */
    private static Optional<CargoSlot> compatibleHold(
            final UnitWithCargo carrier, final GoodsType type) {
        return carrier.getCargo().getSlots().stream()
                .filter(slot -> slot.getAvailableCapacity() > 0)
                .filter(slot -> slot.getGoods()
                        .map(goods -> goods.getType().equals(type))
                        .orElse(true))
                .min(Comparator
                        .comparing((CargoSlot slot) -> slot.isEmpty() ? 1 : 0)
                        .thenComparingInt(CargoSlot::getIndex));
    }

    void fireGameStarted() {
        listenerManager.fireGameStarted(this);
    }

    void fireRoundStarted() {
        listenerManager.fireRoundStarted(this, calendar);
    }

    void fireTurnStarted(final Player player, final boolean isFreshStart) {
        refreshObservations();
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
            final Unit defeated, final boolean wasDestroyed,
            final MilitaryPromotion promotion,
            final ColonyBattleOutcome colonyOutcome) {
        listenerManager.fireUnitAttacked(this, attacker, defender, defeated,
                wasDestroyed, promotion, colonyOutcome);
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
        // Destruction ends membership; a capture never does (R-0192).
        getPlayers().forEach(
                player -> player.forgetRebellionColony(colony.getId()));
        colony.getOwner().forgetColony(colony.getLocation());
        refreshObservations();
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
