package org.akropia.model.generation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.akropia.model.Calendar;
import org.akropia.model.Difficulty;
import org.akropia.model.Direction;
import org.akropia.model.Europe;
import org.akropia.model.EuropeanNation;
import org.akropia.model.Location;
import org.akropia.model.MineralReserve;
import org.akropia.model.Model;
import org.akropia.model.NativeNation;
import org.akropia.model.NativeRelations;
import org.akropia.model.ResourceOverlay;
import org.akropia.model.RiverSize;
import org.akropia.model.RoyalRoster;
import org.akropia.model.TerrainType;
import org.akropia.model.UnitType;
import org.akropia.model.WorldMap;
import org.akropia.model.campaign.MissionImpl;
import org.akropia.model.campaign.ReferenceExpedition;
import org.akropia.model.campaign.po.GameModelPo;
import org.akropia.model.campaign.po.GameModelPoDao;
import org.akropia.model.generation.ScenarioProblem.Reason;
import org.akropia.model.store.CargoSlotPo;
import org.akropia.model.store.ModelPo;
import org.akropia.model.store.NativeLocalAlarmPo;
import org.akropia.model.store.NativeSettlementPo;
import org.akropia.model.store.PlaceCargoSlotPo;
import org.akropia.model.store.PlaceMapPo;
import org.akropia.model.store.PlayerPo;
import org.akropia.model.store.RiverSegmentPo;
import org.akropia.model.store.UnitPo;
import org.akropia.model.store.VisibilityPo;
import org.akropia.model.store.WorldMapPo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

/**
 * Authored scenario being edited in the developer map editor (R-0435): the
 * world, the native settlements, the player's nation and the initial
 * expedition, kept in the scenario schema of the bundled maps.
 * <p>
 * Every edit is applied to a copy and checked with the game's own map rules
 * before it replaces the draft, so a rejected edit changes nothing. A draft
 * only has to be structurally sound to be saved and reopened;
 * {@link #playableScenario()} separately checks that the game can start it and
 * returns it unchanged otherwise. Starting a scenario never adds a generated
 * expedition, other nations or native braves.
 * </p>
 */
public final class ScenarioDraft {

    /** Smallest supported width and height in squares. */
    public static final int MIN_SIDE = 10;

    /**
     * Largest supported width and height in squares. The largest bundled world
     * is 100 × 80 squares; the bound keeps an authored world within the scale
     * of the game's tested worlds, map views and saves.
     */
    public static final int MAX_SIDE = 100;

    /** Unit types an authored European expedition may contain. */
    public static final List<UnitType> EXPEDITION_TYPES = UnitType.UNIT_TYPES
            .stream()
            .filter(type -> type != UnitType.BRAVE
                    && type != UnitType.TREASURE_TRAIN)
            .collect(ImmutableList.toImmutableList());

    private static final String CAMPAIGN = "freePlay";

    private static final String MISSION = "freePlay";

    private static final int STARTING_GOLD = 1000;

    private static final GameModelPoDao COPIES = new GameModelPoDao();

    private GameModelPo scenario;

    private ScenarioDraft(final GameModelPo scenario) {
        this.scenario = scenario;
    }

    /**
     * @param text width or height as typed
     * @return the side in squares
     * @throws ScenarioDraftException when it is no whole number or outside the
     *                                supported bounds
     */
    public static int parseSide(final String text) {
        final int side;
        try {
            side = Integer.parseInt(text == null ? "" : text.strip());
        } catch (NumberFormatException invalid) {
            throw new ScenarioDraftException(new ScenarioProblem(
                    Reason.DIMENSION_NOT_NUMBER, null, text));
        }
        checkSide(side);
        return side;
    }

    private static void checkSide(final int side) {
        if (side < MIN_SIDE || side > MAX_SIDE) {
            throw new ScenarioDraftException(new ScenarioProblem(
                    Reason.DIMENSION_OUT_OF_RANGE, null, String.valueOf(side)));
        }
    }

    /**
     * A new world covered by ocean, without settlements or units. The bounds
     * are checked before the world is allocated.
     *
     * @param width  width in squares
     * @param height height in squares
     * @return new draft for the Dutch nation
     * @throws ScenarioDraftException when a side is outside the bounds
     */
    public static ScenarioDraft ocean(final int width, final int height) {
        checkSide(width);
        checkSide(height);
        final ModelPo model = new ModelPo();
        model.setCombatRandomState(0L);
        model.setDifficulty(Difficulty.CONQUISTADOR);
        model.setNextColonyId(0);
        model.setNextTradeRouteId(0);
        model.setTradeRoutes(new ArrayList<>());
        model.setEuropeanRelations(new ArrayList<>());
        model.setNativeSettlements(new ArrayList<>());
        model.setNativeSettlementKnowledge(new ArrayList<>());
        model.setNativeContacts(new ArrayList<>());
        model.setNativeTribalAnger(NativeRelations.freshTribalAnger());
        model.setNativeLocalAlarm(
                NativeRelations.freshLocalAlarm(model.getNativeSettlements()));
        model.setNativeLandRights(new ArrayList<>());
        model.setNativeLandTakings(new ArrayList<>());
        model.getCalendar().setStartYear(1490);
        model.getCalendar().setEndYear(Calendar.YEAR_OF_FREE_PLAY_END);
        model.getCalendar().setScoredPlay(true);
        final WorldMapPo map = new WorldMapPo();
        map.setMaxX(width);
        map.setMaxY(height);
        map.setSeed(0);
        map.setTerrainType(new HashMap<>());
        final VisibilityPo visibility = new VisibilityPo();
        visibility.setVisibility(Set.of(), width, height);
        map.setVisibility(visibility);
        model.setMap(map);
        model.setEurope(Europe.initialPo());
        final String owner = EuropeanNation.DUTCH.getPlayerName();
        model.getGameManager().getPlayers().add(ReferenceExpedition
                .newPlayer(owner, false, STARTING_GOLD, null, width, height));
        final PlayerPo crown = ReferenceExpedition.newPlayer(crownName(owner),
                true, 0, owner, width, height);
        crown.setRoyalRoster(RoyalRoster.initial(Difficulty.CONQUISTADOR));
        model.getGameManager().getPlayers().add(crown);
        model.getGameManager().setCurrentPlayer(owner);
        final GameModelPo out = new GameModelPo();
        out.setCampaignName(CAMPAIGN);
        out.setMissionName(MISSION);
        out.setModel(model);
        return new ScenarioDraft(out);
    }

    /**
     * Open an authored scenario for editing. Only its structure is checked; it
     * need not be playable. The source is not changed.
     *
     * @param source required scenario read from a file
     * @return draft of an independent copy
     * @throws ScenarioDraftException when the scenario is structurally unsound
     */
    public static ScenarioDraft of(final GameModelPo source) {
        Preconditions.checkNotNull(source);
        final GameModelPo copy;
        try {
            copy = COPIES.copyOf(source);
        } catch (RuntimeException invalid) {
            throw structure(invalid.getMessage());
        }
        checkStructure(copy);
        return new ScenarioDraft(copy);
    }

    private static ScenarioDraftException structure(final String detail) {
        return new ScenarioDraftException(
                new ScenarioProblem(Reason.STRUCTURE, null, detail));
    }

    private static void checkStructure(final GameModelPo source) {
        if (source.getCampaignName() == null || source.getMissionName() == null
                || source.getModel() == null
                || source.getModel().getMap() == null
                || source.getModel().getGameManager() == null
                || source.getModel().getGameManager().getPlayers() == null
                || source.getModel().getUnits() == null
                || source.getModel().getColonies() == null
                || source.getModel().getNativeSettlements() == null) {
            throw structure("Incomplete scenario");
        }
        final ModelPo model = source.getModel();
        final WorldMapPo map = model.getMap();
        if (map.getMaxX() < MIN_SIDE || map.getMaxX() > MAX_SIDE
                || map.getMaxY() < MIN_SIDE || map.getMaxY() > MAX_SIDE) {
            throw new ScenarioDraftException(
                    new ScenarioProblem(Reason.DIMENSION_OUT_OF_RANGE, null,
                            map.getMaxX() + " × " + map.getMaxY()));
        }
        try {
            new WorldMap(model);
        } catch (RuntimeException invalid) {
            throw structure(invalid.getMessage());
        }
        final Set<String> names = new HashSet<>();
        for (final PlayerPo player : model.getGameManager().getPlayers()) {
            if (player == null || player.getName() == null
                    || player.getVisible() == null
                    || !names.add(player.getName())) {
                throw structure("Invalid player");
            }
        }
        final List<PlayerPo> humans = model.getGameManager().getPlayers()
                .stream().filter(ScenarioDraft::isColonialHuman)
                .collect(Collectors.toList());
        if (humans.size() != 1 || EuropeanNation
                .findByPlayerName(humans.get(0).getName()).isEmpty()) {
            throw structure("Scenario needs one human European nation");
        }
        final Set<Integer> ids = new HashSet<>();
        for (final UnitPo unit : model.getUnits()) {
            if (unit == null || unit.getId() == null || unit.getType() == null
                    || unit.getOwnerId() == null || unit.getCargo() == null
                    || unit.getCargo().getSlots() == null
                    || !names.contains(unit.getOwnerId())
                    || !ids.add(unit.getId())) {
                throw structure("Invalid unit");
            }
            if (unit.getPlaceMap() != null
                    && !isOnMap(map, unit.getPlaceMap().getLocation())) {
                throw new ScenarioDraftException(
                        new ScenarioProblem(Reason.OUTSIDE_MAP,
                                unit.getPlaceMap().getLocation(), null));
            }
        }
        final Set<Integer> settlementIds = new HashSet<>();
        final Set<Location> sites = new HashSet<>();
        for (final NativeSettlementPo settlement : model
                .getNativeSettlements()) {
            if (settlement == null || settlement.getId() == null
                    || settlement.getNation() == null
                    || settlement.getLocation() == null
                    || !settlementIds.add(settlement.getId())
                    || !sites.add(settlement.getLocation())) {
                throw structure("Invalid native settlement");
            }
            if (!isOnMap(map, settlement.getLocation())) {
                throw new ScenarioDraftException(new ScenarioProblem(
                        Reason.OUTSIDE_MAP, settlement.getLocation(), null));
            }
        }
    }

    private static boolean isColonialHuman(final PlayerPo player) {
        return !player.isComputer() && player.getWhosKingThisPlayerIs() == null
                && player.getNativeNation() == null;
    }

    private static boolean isOnMap(final WorldMapPo map,
            final Location location) {
        return location != null && location.getX() >= Location.MAP_MIN_X
                && location.getY() >= Location.MAP_MIN_Y
                && location.getX() <= map.getMaxX()
                && location.getY() <= map.getMaxY();
    }

    private static String crownName(final String owner) {
        return owner + "'s King";
    }

    /**
     * @return independent copy in the scenario schema, for saving
     */
    public GameModelPo toScenario() {
        return COPIES.copyOf(scenario);
    }

    public int getWidth() {
        return map().getMaxX();
    }

    public int getHeight() {
        return map().getMaxY();
    }

    private ModelPo model() {
        return scenario.getModel();
    }

    private WorldMapPo map() {
        return model().getMap();
    }

    /**
     * @param location required square on the map
     * @return whether the square lies on the map
     */
    public boolean contains(final Location location) {
        return isOnMap(map(), location);
    }

    /**
     * @return the draft's world under the game's map rules, for its preview
     */
    public WorldMap getWorldMap() {
        return new WorldMap(model());
    }

    public Map<Location, TerrainType> getTerrainMap() {
        return map().getTerrainMap();
    }

    public Set<Location> getForests() {
        return map().getTreeSet();
    }

    public Set<Location> getFields() {
        return map().getFieldSet();
    }

    public Map<Location, RiverSize> getRivers() {
        return map().getRiverMap();
    }

    public List<RiverSegmentPo> getRiverSegments() {
        return ImmutableList.copyOf(map().getRiverSegments());
    }

    public Map<Location, ResourceOverlay> getResources() {
        return map().getResourceMap();
    }

    /**
     * @return squares of the colonies an opened scenario already holds; the
     *         editor does not author colonies
     */
    public Set<Location> getColonyLocations() {
        return model().getColonies().stream()
                .map(colony -> colony.getLocation())
                .collect(Collectors.toSet());
    }

    public List<NativeSettlementPo> getSettlements() {
        return ImmutableList.copyOf(model().getNativeSettlements());
    }

    public Optional<NativeSettlementPo> getSettlementAt(
            final Location location) {
        return model().getNativeSettlements().stream()
                .filter(settlement -> settlement.getLocation().equals(location))
                .findFirst();
    }

    /**
     * @return the nation the player controls
     */
    public EuropeanNation getNation() {
        return EuropeanNation.fromPlayerName(human(model()).getName());
    }

    /**
     * @return units standing on the map square, in identity order
     */
    public List<UnitPo> getUnitsAt(final Location location) {
        return model().getUnits().stream()
                .filter(unit -> unit.getPlaceMap() != null
                        && location.equals(unit.getPlaceMap().getLocation()))
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * @return units carried by the ship, in cargo order
     */
    public List<UnitPo> getPassengers(final int shipId) {
        return findUnit(model(), shipId)
                .map(ship -> ship.getCargo().getSlots().stream()
                        .filter(slot -> slot.getUnitId() != null)
                        .map(slot -> findUnit(model(), slot.getUnitId()))
                        .flatMap(Optional::stream)
                        .collect(ImmutableList.<UnitPo>toImmutableList()))
                .orElse(ImmutableList.of());
    }

    /**
     * Paint terrain. Forests, fields, rivers and bonuses that the new terrain
     * cannot carry are removed with it; a settlement or unit standing there
     * must still fit, otherwise nothing changes.
     *
     * @param location required square
     * @param terrain  required terrain
     * @throws ScenarioDraftException when the change is refused
     */
    public void setTerrain(final Location location, final TerrainType terrain) {
        Preconditions.checkNotNull(terrain);
        edit(location, model -> {
            final WorldMapPo map = model.getMap();
            final Map<Location, TerrainType> types = map.getTerrainMap();
            types.put(location, terrain);
            map.setTerrainType(types);
            if (terrain.isSea()) {
                removeFrom(map.getTreeSet(), location, map::setTrees);
                removeFrom(map.getFieldSet(), location, map::setFields);
                removeFrom(map.getRoadSet(), location, map::setRoads);
                removeFrom(map.getRumorSet(), location, map::setRumors);
                removeRiver(map, location);
            }
            final Map<Location, ResourceOverlay> resources = map
                    .getResourceMap();
            final ResourceOverlay resource = resources.getOrDefault(location,
                    ResourceOverlay.NONE);
            if (!resource.canBeOn(terrain,
                    map.getRiverMap().getOrDefault(location, RiverSize.NONE))) {
                setResource(map, location, ResourceOverlay.NONE);
            }
            if (hasColony(model, location) || getSettlementIn(model, location)
                    .isPresent()
                    && !GeneratedNatives.isSettlementSite(map, location)) {
                throw problem(Reason.OCCUPIED, location);
            }
            for (final UnitPo unit : unitsIn(model, location)) {
                if (!unit.getType().canMoveAtTerrain(terrain)) {
                    throw problem(Reason.OCCUPIED, location);
                }
            }
        });
    }

    /**
     * @throws ScenarioDraftException when the square cannot carry a forest
     */
    public void setForest(final Location location, final boolean forest) {
        edit(location, model -> {
            final WorldMapPo map = model.getMap();
            requireLand(map, location, forest);
            final Set<Location> trees = map.getTreeSet();
            toggle(trees, location, forest);
            map.setTrees(trees);
        });
    }

    /**
     * @throws ScenarioDraftException when the square cannot carry a field
     */
    public void setField(final Location location, final boolean field) {
        edit(location, model -> {
            final WorldMapPo map = model.getMap();
            requireLand(map, location, field);
            final Set<Location> fields = map.getFieldSet();
            toggle(fields, location, field);
            map.setFields(fields);
        });
    }

    /**
     * Set or remove the river of a square. A new river joins every neighbouring
     * river square; a removed one leaves its course.
     *
     * @throws ScenarioDraftException when the square cannot carry a river
     */
    public void setRiver(final Location location, final RiverSize size) {
        Preconditions.checkNotNull(size);
        edit(location, model -> {
            final WorldMapPo map = model.getMap();
            if (size == RiverSize.NONE) {
                removeRiver(map, location);
                return;
            }
            requireLand(map, location, true);
            if (getSettlementIn(model, location).isPresent()) {
                throw problem(Reason.OCCUPIED, location);
            }
            final Map<Location, RiverSize> rivers = map.getRiverMap();
            final boolean added = rivers.put(location, size) == null;
            map.setRivers(rivers);
            if (added) {
                final List<RiverSegmentPo> segments = new ArrayList<>(
                        map.getRiverSegments());
                for (final Location neighbour : location.getNeighbors()) {
                    if (rivers.containsKey(neighbour)) {
                        segments.add(new RiverSegmentPo(location, neighbour));
                    }
                }
                map.setRiverSegments(segments);
            }
        });
    }

    /**
     * @throws ScenarioDraftException when the square cannot carry the bonus
     */
    public void setResource(final Location location,
            final ResourceOverlay resource) {
        Preconditions.checkNotNull(resource);
        edit(location, model -> {
            final WorldMapPo map = model.getMap();
            if (resource != ResourceOverlay.NONE
                    && !resource.canBeOn(map.getTerrainMap()
                            .getOrDefault(location, TerrainType.OCEAN))) {
                throw problem(
                        resource == ResourceOverlay.FISHERY ? Reason.GEOGRAPHY
                                : Reason.NEEDS_LAND,
                        location);
            }
            setResource(map, location, resource);
        });
    }

    /**
     * Place a native settlement with the defaults of a generated one: the
     * nation's settlement class, its treasury and a market and teaching skill
     * chosen from its surroundings.
     *
     * @throws ScenarioDraftException when the site or capital is refused
     */
    public void placeSettlement(final Location location,
            final NativeNation nation, final boolean capital) {
        Preconditions.checkNotNull(nation);
        edit(location, model -> {
            if (getSettlementIn(model, location).isPresent()
                    || hasColony(model, location)) {
                throw problem(Reason.OCCUPIED, location);
            }
            final int id = model.getNativeSettlements().stream()
                    .mapToInt(NativeSettlementPo::getId).max().orElse(-1) + 1;
            putSettlement(model, location, nation, capital, id, null);
        });
    }

    /**
     * Change the tribe, capital and teaching skill of a settlement. Its
     * identity, market, treasury, missions and history stay; braves of the
     * settlement follow it to the new tribe. A treasury still at the default of
     * its rank follows a changed rank.
     *
     * @param skill optional teaching skill, the default when empty
     * @throws ScenarioDraftException when the change is refused
     */
    public void updateSettlement(final Location location,
            final NativeNation nation, final boolean capital,
            final UnitType skill) {
        Preconditions.checkNotNull(nation);
        Preconditions.checkArgument(
                skill == null
                        || UnitType.NATIVE_TEACHABLE_SKILLS.contains(skill),
                "Not a native teaching skill");
        edit(location, model -> {
            final NativeSettlementPo old = getSettlementIn(model, location)
                    .orElseThrow(
                            () -> problem(Reason.SETTLEMENT_SITE, location));
            if (capital && model.getNativeSettlements().stream()
                    .anyMatch(other -> other != old
                            && other.getNation() == nation
                            && Boolean.TRUE.equals(other.getCapital()))) {
                throw problem(Reason.SECOND_CAPITAL, location);
            }
            final boolean wasCapital = Boolean.TRUE.equals(old.getCapital());
            final NativeSettlementPo defaults = GeneratedNatives.settlement(
                    old.getId(), old.getNation(), wasCapital, location);
            if (wasCapital != capital
                    && defaults.getTreasury().equals(old.getTreasury())) {
                old.setTreasury(GeneratedNatives
                        .settlement(old.getId(), nation, capital, location)
                        .getTreasury());
            }
            final NativeNation before = old.getNation();
            old.setNation(nation);
            old.setSettlementClass(nation.getSettlementClass());
            old.setCapital(capital);
            if (skill != null) {
                old.setTeachableSkill(skill.name());
            }
            for (final UnitPo unit : model.getUnits()) {
                if (old.getId().equals(unit.getNativeHomeSettlementId())
                        && before.name().equals(unit.getOwnerId())) {
                    unit.setOwnerId(nation.name());
                }
            }
            syncNatives(model);
        });
    }

    /**
     * Remove the settlement standing on the square, with the braves whose home
     * it is and every record of it.
     */
    public void removeSettlement(final Location location) {
        edit(location, model -> {
            getSettlementIn(model, location).ifPresent(settlement -> {
                final Integer id = settlement.getId();
                model.getNativeSettlements().remove(settlement);
                model.getUnits().removeIf(
                        unit -> id.equals(unit.getNativeHomeSettlementId()));
                if (model.getNativeSettlementKnowledge() != null) {
                    model.getNativeSettlementKnowledge().forEach(
                            known -> known.getSettlementIds().remove(id));
                }
            });
            syncNatives(model);
        });
    }

    private void putSettlement(final ModelPo model, final Location location,
            final NativeNation nation, final boolean capital, final int id,
            final UnitType skill) {
        if (!GeneratedNatives.isSettlementSite(model.getMap(), location)) {
            throw problem(Reason.SETTLEMENT_SITE, location);
        }
        if (!unitsIn(model, location).isEmpty()) {
            throw problem(Reason.OCCUPIED, location);
        }
        if (capital && model.getNativeSettlements().stream()
                .anyMatch(other -> other.getNation() == nation
                        && Boolean.TRUE.equals(other.getCapital()))) {
            throw problem(Reason.SECOND_CAPITAL, location);
        }
        final NativeSettlementPo settlement = GeneratedNatives.settlement(id,
                nation, capital, location);
        GeneratedNatives.furnish(model.getMap(), settlement, new Random(id));
        if (skill != null) {
            settlement.setTeachableSkill(skill.name());
        }
        model.getNativeSettlements().add(settlement);
        syncNatives(model);
    }

    /**
     * Keep the native participants that have settlements or units, and the
     * local alarm of every settlement: existing values stay, a new settlement
     * starts calm and a removed one leaves the table.
     */
    private static void syncNatives(final ModelPo model) {
        final Set<NativeNation> present = model.getNativeSettlements().stream()
                .map(NativeSettlementPo::getNation).collect(Collectors.toSet());
        final Set<String> owners = model.getUnits().stream()
                .map(UnitPo::getOwnerId).collect(Collectors.toSet());
        final List<PlayerPo> players = model.getGameManager().getPlayers();
        final Set<NativeNation> leaving = players.stream()
                .map(PlayerPo::getNativeNation)
                .filter(nation -> nation != null && !present.contains(nation)
                        && !owners.contains(nation.name()))
                .collect(Collectors.toSet());
        players.removeIf(player -> leaving.contains(player.getNativeNation()));
        if (model.getNativeContacts() != null) {
            model.getNativeContacts()
                    .removeIf(contact -> leaving.contains(contact.getNation()));
        }
        for (final NativeNation nation : NativeNation.values()) {
            if (present.contains(nation) && players.stream()
                    .noneMatch(player -> player.getNativeNation() == nation)) {
                players.add(GeneratedNatives.player(nation,
                        model.getMap().getMaxX(), model.getMap().getMaxY()));
            }
        }
        final Set<Integer> ids = model.getNativeSettlements().stream()
                .map(NativeSettlementPo::getId).collect(Collectors.toSet());
        final List<NativeLocalAlarmPo> alarm = new ArrayList<>(
                model.getNativeLocalAlarm() == null ? List.of()
                        : model.getNativeLocalAlarm());
        alarm.removeIf(entry -> !ids.contains(entry.getSettlementId()));
        final Set<Integer> known = alarm.stream()
                .map(NativeLocalAlarmPo::getSettlementId)
                .collect(Collectors.toSet());
        alarm.addAll(NativeRelations.freshLocalAlarm(model
                .getNativeSettlements().stream()
                .filter(settlement -> !known.contains(settlement.getId()))
                .collect(Collectors.toList())));
        model.setNativeLocalAlarm(alarm);
    }

    /**
     * Choose the European nation the player controls. The expedition keeps its
     * units, positions and passengers, and the natives keep their relations,
     * which move to the new nation. A scenario with colonies or another
     * colonial nation keeps its nation, because their records name it.
     *
     * @throws ScenarioDraftException when another participant is that nation or
     *                                the scenario fixes the nation
     */
    public void setNation(final EuropeanNation nation) {
        Preconditions.checkNotNull(nation);
        final ModelPo current = model();
        final String before = human(current).getName();
        final String after = nation.getPlayerName();
        if (before.equals(after)) {
            return;
        }
        apply(model -> {
            if (model.getGameManager().getPlayers().stream()
                    .anyMatch(player -> player.getName().equals(after)
                            || player.getName().equals(crownName(after)))) {
                throw problem(Reason.NATION_TAKEN, null);
            }
            if (!model.getColonies().isEmpty()
                    || model.getGameManager().getPlayers().stream()
                            .anyMatch(player -> player.getNativeNation() == null
                                    && player.getWhosKingThisPlayerIs() == null
                                    && !player.getName().equals(before))) {
                throw problem(Reason.NATION_FIXED, null);
            }
            final EuropeanNation from = EuropeanNation.fromPlayerName(before);
            if (model.getNativeLocalAlarm() != null) {
                model.getNativeLocalAlarm().forEach(entry -> entry
                        .setEuropean(swap(entry.getEuropean(), from, nation)));
            }
            if (model.getNativeTribalAnger() != null) {
                model.getNativeTribalAnger().forEach(entry -> entry
                        .setEuropean(swap(entry.getEuropean(), from, nation)));
            }
            if (model.getNativeContacts() != null) {
                model.getNativeContacts().forEach(entry -> entry
                        .setEuropean(swap(entry.getEuropean(), from, nation)));
            }
            if (model.getNativeSettlementKnowledge() != null) {
                model.getNativeSettlementKnowledge().forEach(entry -> {
                    if (before.equals(entry.getPlayerName())) {
                        entry.setPlayerName(after);
                    }
                });
            }
            for (final PlayerPo player : model.getGameManager().getPlayers()) {
                if (player.getName().equals(before)) {
                    player.setName(after);
                } else if (player.getName().equals(crownName(before))) {
                    player.setName(crownName(after));
                }
                if (before.equals(player.getWhosKingThisPlayerIs())) {
                    player.setWhosKingThisPlayerIs(after);
                }
            }
            for (final UnitPo unit : model.getUnits()) {
                if (before.equals(unit.getOwnerId())) {
                    unit.setOwnerId(after);
                } else if (crownName(before).equals(unit.getOwnerId())) {
                    unit.setOwnerId(crownName(after));
                }
            }
            if (model.getEuropeanRelations() != null) {
                model.getEuropeanRelations().forEach(relation -> {
                    if (before.equals(relation.getFirstPlayer())) {
                        relation.setFirstPlayer(after);
                    }
                    if (before.equals(relation.getSecondPlayer())) {
                        relation.setSecondPlayer(after);
                    }
                });
            }
            if (before.equals(model.getGameManager().getCurrentPlayer())) {
                model.getGameManager().setCurrentPlayer(after);
            }
        });
    }

    /**
     * Place a new unit of the player's expedition on the map.
     *
     * @return identity of the new unit
     * @throws ScenarioDraftException when the type or terrain is refused
     */
    public int addUnit(final UnitType type, final Location location) {
        Preconditions.checkNotNull(type);
        final int id = nextUnitId();
        edit(location, model -> {
            requireExpeditionType(type, location);
            final TerrainType terrain = model.getMap().getTerrainMap()
                    .getOrDefault(location, TerrainType.OCEAN);
            if (!type.canMoveAtTerrain(terrain)) {
                throw problem(Reason.UNIT_TERRAIN, location);
            }
            final String owner = human(model).getName();
            if (getSettlementIn(model, location).isPresent()
                    || hasColony(model, location)
                    || unitsIn(model, location).stream().anyMatch(
                            other -> !owner.equals(other.getOwnerId()))) {
                throw problem(Reason.OCCUPIED, location);
            }
            final UnitPo unit = ReferenceExpedition.newUnit(id,
                    human(model).getName(), type);
            final PlaceMapPo place = new PlaceMapPo();
            place.setLocation(location);
            place.setOrientation(Direction.west);
            unit.setPlaceMap(place);
            model.getUnits().add(unit);
        });
        return id;
    }

    /**
     * Put a new unit of the player's expedition aboard a ship.
     *
     * @return identity of the new passenger
     * @throws ScenarioDraftException when the ship cannot carry it
     */
    public int addPassenger(final int shipId, final UnitType type) {
        Preconditions.checkNotNull(type);
        final int id = nextUnitId();
        apply(model -> {
            final UnitPo ship = findUnit(model, shipId)
                    .orElseThrow(() -> problem(Reason.NOT_A_SHIP, null));
            final Location at = ship.getPlaceMap() == null ? null
                    : ship.getPlaceMap().getLocation();
            if (!ship.getType().isShip()) {
                throw problem(Reason.NOT_A_SHIP, at);
            }
            requireExpeditionType(type, at);
            if (!type.isStorable()) {
                throw problem(Reason.UNIT_TYPE, at);
            }
            int used = 0;
            for (final CargoSlotPo slot : ship.getCargo().getSlots()) {
                used += slot.getUnitId() == null ? 1
                        : findUnit(model, slot.getUnitId())
                                .map(unit -> unit.getType().getRequiredHolds())
                                .orElse(1);
            }
            if (used + type.getRequiredHolds() > ship.getType()
                    .getCargoCapacity()) {
                throw problem(Reason.SHIP_FULL, at);
            }
            final UnitPo passenger = ReferenceExpedition.newUnit(id,
                    ship.getOwnerId(), type);
            passenger.setPlaceCargoSlot(new PlaceCargoSlotPo());
            final CargoSlotPo slot = new CargoSlotPo();
            slot.setUnitId(id);
            ship.getCargo().getSlots().add(slot);
            model.getUnits().add(passenger);
        });
        return id;
    }

    /**
     * Remove a unit; a ship leaves with its passengers and a passenger leaves
     * its ship.
     */
    public void removeUnit(final int unitId) {
        apply(model -> {
            final Optional<UnitPo> found = findUnit(model, unitId);
            if (found.isEmpty()) {
                return;
            }
            for (final UnitPo passenger : getPassengersIn(model, unitId)) {
                model.getUnits().remove(passenger);
            }
            model.getUnits().remove(found.get());
            for (final UnitPo carrier : model.getUnits()) {
                carrier.getCargo().getSlots().removeIf(slot -> Integer
                        .valueOf(unitId).equals(slot.getUnitId()));
            }
        });
    }

    private List<UnitPo> getPassengersIn(final ModelPo model,
            final int shipId) {
        return findUnit(model, shipId)
                .map(ship -> ship.getCargo().getSlots().stream()
                        .filter(slot -> slot.getUnitId() != null)
                        .map(slot -> findUnit(model, slot.getUnitId()))
                        .flatMap(Optional::stream).collect(Collectors.toList()))
                .orElse(List.of());
    }

    private static void requireExpeditionType(final UnitType type,
            final Location location) {
        if (!EXPEDITION_TYPES.contains(type)) {
            throw problem(Reason.UNIT_TYPE, location);
        }
    }

    private int nextUnitId() {
        return model().getUnits().stream().mapToInt(UnitPo::getId).max()
                .orElse(0) + 1;
    }

    /**
     * @return every reason why the game cannot start this draft, empty when it
     *         can
     */
    public List<ScenarioProblem> getPlayabilityProblems() {
        final List<ScenarioProblem> out = new ArrayList<>();
        final ModelPo model = model();
        final String owner = human(model).getName();
        if (model.getUnits().stream()
                .noneMatch(unit -> owner.equals(unit.getOwnerId()))) {
            out.add(new ScenarioProblem(Reason.NO_EXPEDITION, null, null));
        }
        for (final NativeNation nation : NativeNation.values()) {
            final List<NativeSettlementPo> settlements = model
                    .getNativeSettlements().stream()
                    .filter(settlement -> settlement.getNation() == nation)
                    .collect(Collectors.toList());
            if (!settlements.isEmpty()
                    && settlements.stream().noneMatch(settlement -> Boolean.TRUE
                            .equals(settlement.getCapital()))) {
                out.add(new ScenarioProblem(Reason.NO_CAPITAL,
                        settlements.get(0).getLocation(),
                        nation.getDisplayName()));
            }
        }
        if (out.isEmpty()) {
            try {
                Model.make(assemble().getModel(),
                        MissionImpl.getPredefinedGameOverEvaluators());
            } catch (RuntimeException rejected) {
                out.add(new ScenarioProblem(Reason.GAME_REJECTED, null,
                        rejected.getMessage()));
            }
        }
        return out;
    }

    /**
     * @return the scenario the game starts: the authored world, settlements,
     *         nation and expedition, focused on the expedition
     * @throws ScenarioDraftException listing every reason when the game cannot
     *                                start it
     */
    public GameModelPo playableScenario() {
        final List<ScenarioProblem> problems = getPlayabilityProblems();
        if (!problems.isEmpty()) {
            throw new ScenarioDraftException(problems);
        }
        return assemble();
    }

    private GameModelPo assemble() {
        final GameModelPo out = toScenario();
        final ModelPo model = out.getModel();
        final String owner = human(model).getName();
        model.getUnits().stream()
                .filter(unit -> owner.equals(unit.getOwnerId())
                        && unit.getPlaceMap() != null)
                .findFirst().ifPresent(unit -> model
                        .setFocusedField(unit.getPlaceMap().getLocation()));
        return out;
    }

    private static PlayerPo human(final ModelPo model) {
        return model.getGameManager().getPlayers().stream()
                .filter(ScenarioDraft::isColonialHuman).findFirst()
                .orElseThrow();
    }

    private static Optional<UnitPo> findUnit(final ModelPo model,
            final int id) {
        return model.getUnits().stream().filter(unit -> unit.getId() == id)
                .findFirst();
    }

    private static Optional<NativeSettlementPo> getSettlementIn(
            final ModelPo model, final Location location) {
        return model.getNativeSettlements().stream()
                .filter(settlement -> settlement.getLocation().equals(location))
                .findFirst();
    }

    private static boolean hasColony(final ModelPo model,
            final Location location) {
        return model.getColonies().stream()
                .anyMatch(colony -> location.equals(colony.getLocation()));
    }

    private static EuropeanNation swap(final EuropeanNation value,
            final EuropeanNation from, final EuropeanNation to) {
        if (value == from) {
            return to;
        }
        return value == to ? from : value;
    }

    private static List<UnitPo> unitsIn(final ModelPo model,
            final Location location) {
        return model.getUnits().stream()
                .filter(unit -> unit.getPlaceMap() != null
                        && location.equals(unit.getPlaceMap().getLocation()))
                .collect(Collectors.toList());
    }

    private static void requireLand(final WorldMapPo map,
            final Location location, final boolean adding) {
        if (adding && !map.getTerrainMap()
                .getOrDefault(location, TerrainType.OCEAN).isLand()) {
            throw problem(Reason.NEEDS_LAND, location);
        }
    }

    private static void toggle(final Set<Location> set, final Location location,
            final boolean present) {
        if (present) {
            set.add(location);
        } else {
            set.remove(location);
        }
    }

    private static void removeFrom(final Set<Location> set,
            final Location location, final Consumer<Set<Location>> store) {
        if (set.remove(location)) {
            store.accept(set);
        }
    }

    private static void removeRiver(final WorldMapPo map,
            final Location location) {
        final Map<Location, RiverSize> rivers = map.getRiverMap();
        if (rivers.remove(location) != null) {
            map.setRivers(rivers);
            map.setRiverSegments(map.getRiverSegments().stream()
                    .filter(segment -> !segment.getFrom().equals(location)
                            && !segment.getTo().equals(location))
                    .collect(Collectors.toList()));
        }
    }

    private static void setResource(final WorldMapPo map,
            final Location location, final ResourceOverlay resource) {
        final Map<Location, ResourceOverlay> resources = map.getResourceMap();
        final Map<Location, MineralReserve> reserves = map
                .getMineralReserveMap();
        if (resource == ResourceOverlay.NONE) {
            resources.remove(location);
        } else {
            resources.put(location, resource);
        }
        reserves.remove(location);
        if (resource.isFinite()) {
            reserves.put(location, MineralReserve.initial(resource));
        }
        map.setResources(resources);
        map.setMineralReserves(reserves);
    }

    private static ScenarioDraftException problem(final Reason reason,
            final Location location) {
        return new ScenarioDraftException(
                new ScenarioProblem(reason, location, null));
    }

    private void edit(final Location location, final Consumer<ModelPo> change) {
        Preconditions.checkNotNull(location);
        if (!contains(location)) {
            throw problem(Reason.OUTSIDE_MAP, location);
        }
        apply(model -> {
            change.accept(model);
            try {
                new WorldMap(model);
            } catch (ScenarioDraftException rejected) {
                throw rejected;
            } catch (RuntimeException rejected) {
                throw new ScenarioDraftException(new ScenarioProblem(
                        Reason.GEOGRAPHY, location, rejected.getMessage()));
            }
        });
    }

    /**
     * Apply a change to a copy and keep it only when the whole change succeeds.
     */
    private void apply(final Consumer<ModelPo> change) {
        final GameModelPo next = COPIES.copyOf(scenario);
        change.accept(next.getModel());
        scenario = next;
    }

}
