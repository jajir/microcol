package org.microcol.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.DoublePredicate;

import org.junit.jupiter.api.Test;
import org.microcol.gui.screen.game.gamepanel.AnimationLock;
import org.microcol.model.Direction;
import org.microcol.model.EuropeanRelationState;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.MovementPoints;
import org.microcol.model.Player;
import org.microcol.model.RivalExpansionObjective;
import org.microcol.model.RivalTradeObjective;
import org.microcol.model.ShipPatrolRules;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.ColonyPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceCargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.model.unit.Ship;

/**
 * Seeded naval fixtures on the free-play map. In that save {@code _} is ocean
 * and the land around Quebec (colony 0 at (17,10)) is grassland, hills and
 * mountains; Quebec has no sea neighbour, so every fixture turns its field slot
 * at (18,9) into ocean. That slot is unworked, so no worker, resource or
 * identity moves. Every square turned to ocean loses its trees, fields, roads
 * and clearings, which an ocean square cannot carry; all other squares keep
 * theirs. Fixtures that need walls turn the surrounding ocean of a fixed window
 * into grassland, so the water that is left is exactly the listed set.
 *
 * Ships start with the movement of a real owner turn, their type's speed, since
 * the cycle is exercised after the turn start. Each battle draw is pinned to
 * the shared quote of the attack the test expects the rival to make, read from
 * the model itself, and so is every other draw that same pass makes. A naval
 * fight draws twice: the first draw below the attacker's win probability wins
 * it, and the second below one half sinks the loser, while a higher draw leaves
 * it damaged for repair. A patrol interception draws once for a step beside an
 * eligible hostile armed ship: below one half it is undetected, below three
 * quarters slowed, otherwise stopped. The naval quote of two ships depends only
 * on their types: a man-of-war against a man-of-war is 36 / (36 + 24) = 0.6, a
 * frigate against one is 24 / (24 + 24) = 0.5, a man-of-war against a
 * merchantman is 36 / 42, and a frigate against a merchantman is 24 / 30.
 */
class RivalNavalMilitaryCycleTest {
    private static final int QUEBEC_ID = 0;
    private static final int MONTREAL_ID = 1;
    private static final int QUEBEC_WORKER_ID = 4;
    private static final int MONTREAL_WORKER_ID = 5;
    private static final int RIVAL_ID = 6;
    private static final int FIRST_NEW_ID = 7;
    private static final int SECOND_NEW_ID = 8;
    private static final int THIRD_NEW_ID = 9;
    private static final int FOURTH_NEW_ID = 10;
    private static final int FIFTH_NEW_ID = 11;
    private static final Location QUEBEC = Location.of(17, 10);
    private static final Location WARSHIP_START = Location.of(18, 9);
    private static final Location PATROL_SEA = Location.of(18, 8);
    /** The carrier of the channel fixtures, walled in on every side but one. */
    private static final Location CARRIER_SQUARE = Location.of(20, 9);
    /** The only sea square beside the walled carrier. */
    private static final Location APPROACH = Location.of(21, 9);
    private static final Location HIDDEN_SEA = Location.of(24, 4);
    /** The window whose ocean the wall fixtures replace with grassland. */
    private static final int WINDOW_FROM_X = 17;
    private static final int WINDOW_TO_X = 24;
    private static final int WINDOW_FROM_Y = 3;
    private static final int WINDOW_TO_Y = 13;
    private static final double EPSILON = 1e-9;
    private static final int PATROL_ROUNDS = 5;
    private static final int PATROL_RADIUS = 5;
    private static final long GAMMA = 0x9E3779B97F4A7C15L;

    private Model model;

    // ------------------------------------------------------------ fixtures

    private static GameModelPo freePlay() {
        return new GameModelPoDao()
                .loadFromClassPath("/maps/free-play.microcol");
    }

    private static UnitPo unitPo(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
    }

    private static ColonyPo colonyPo(final GameModelPo data, final int id) {
        return data.getModel().getColonies().stream()
                .filter(colony -> colony.getId() == id).findFirst()
                .orElseThrow();
    }

    private static PlayerPo playerPo(final GameModelPo data,
            final String name) {
        return data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals(name)).findFirst()
                .orElseThrow();
    }

    /**
     * Turns the squares to ocean. An ocean square cannot carry trees, fields,
     * roads or clearings, so those overlays are removed from exactly these
     * squares and kept everywhere else.
     */
    private static void paintOcean(final GameModelPo data,
            final Set<Location> squares) {
        final WorldMapPo map = data.getModel().getMap();
        final Map<Location, TerrainType> terrain = new HashMap<>(
                map.getTerrainMap());
        squares.forEach(at -> terrain.put(at, TerrainType.OCEAN));
        map.setTerrainType(terrain);
        map.setTrees(without(map.getTreeSet(), squares));
        map.setFields(without(map.getFieldSet(), squares));
        map.setRoads(without(map.getRoadSet(), squares));
        map.setCleared(without(map.getClearedSet(), squares));
    }

    private static Set<Location> without(final Set<Location> base,
            final Set<Location> removed) {
        final Set<Location> out = new HashSet<>(base);
        out.removeAll(removed);
        return out;
    }

    /** Ground the given squares; trees and overlays there stay legal. */
    private static void paintGrass(final GameModelPo data,
            final Set<Location> squares) {
        final Map<Location, TerrainType> terrain = new HashMap<>(
                data.getModel().getMap().getTerrainMap());
        squares.forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        data.getModel().getMap().setTerrainType(terrain);
    }

    /**
     * The window's water becomes exactly the given set: those squares turn to
     * ocean, and every other ocean square of the window turns to grassland.
     * Land squares of the window, such as Quebec's fields, are not touched.
     */
    private static void shape(final GameModelPo data, final Set<Location> sea) {
        paintOcean(data, sea);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        final Set<Location> land = new HashSet<>();
        for (int x = WINDOW_FROM_X; x <= WINDOW_TO_X; x++) {
            for (int y = WINDOW_FROM_Y; y <= WINDOW_TO_Y; y++) {
                final Location at = Location.of(x, y);
                if (!sea.contains(at) && !terrain.containsKey(at)) {
                    land.add(at);
                }
            }
        }
        paintGrass(data, land);
    }

    /** The coastal port's sea square and the open water east of it. */
    private static Set<Location> openSea() {
        final Set<Location> sea = new HashSet<>();
        sea.add(WARSHIP_START);
        sea.add(PATROL_SEA);
        for (int x = 19; x <= 24; x++) {
            for (int y = 4; y <= 16; y++) {
                sea.add(Location.of(x, y));
            }
        }
        return sea;
    }

    /** Quebec's only sea neighbour, with the open water east of it. */
    private static GameModelPo seaPort() {
        final GameModelPo data = freePlay();
        paintOcean(data, openSea());
        unitPo(data, RIVAL_ID).setHoldingGuns(false);
        revealAll(data);
        return data;
    }

    /**
     * Only the port's sea square, the square north of it and one square east of
     * it are water, so a ship in port has a single patrol square to move to,
     * beside the ship that stands east of the port.
     */
    private static GameModelPo tinyPort() {
        final GameModelPo data = freePlay();
        shape(data, Set.of(WARSHIP_START, PATROL_SEA, Location.of(19, 9)));
        unitPo(data, RIVAL_ID).setHoldingGuns(false);
        revealAll(data);
        return data;
    }

    /**
     * A detour whose every square stays within two squares of the carrier, so
     * the carrier is in sight along the whole way. The carrier at (20,9) has
     * land on seven sides and the sea square (21,9) on the east. From the port
     * square (18,9) the only route is (18,8), (19,7), (20,7), (21,7), (22,8),
     * (21,9): six steps, one whole owner turn of a man-of-war, so the attack
     * cannot happen before the second turn. With the approach closed the
     * carrier has no sea neighbour at all.
     */
    private static GameModelPo channelPort(final boolean approachOpen) {
        final GameModelPo data = freePlay();
        final Set<Location> sea = new HashSet<>(
                List.of(WARSHIP_START, PATROL_SEA, Location.of(19, 7),
                        Location.of(20, 7), Location.of(21, 7),
                        Location.of(22, 8), CARRIER_SQUARE, HIDDEN_SEA));
        if (approachOpen) {
            sea.add(APPROACH);
        }
        shape(data, sea);
        unitPo(data, RIVAL_ID).setHoldingGuns(false);
        revealAll(data);
        return data;
    }

    /**
     * France owns no colony: both colonies and their field workers go to the
     * Dutch together, so no colony keeps a worker of the other owner.
     */
    private static GameModelPo noColonyPort() {
        final GameModelPo data = seaPort();
        handOver(data, QUEBEC_ID, QUEBEC_WORKER_ID);
        handOver(data, MONTREAL_ID, MONTREAL_WORKER_ID);
        return data;
    }

    private static void handOver(final GameModelPo data, final int colonyId,
            final int workerId) {
        final ColonyPo colony = colonyPo(data, colonyId);
        colony.setOwnerName("Dutch");
        colony.setLastColonialAffiliation("Dutch");
        unitPo(data, workerId).setOwnerId("Dutch");
    }

    /** France has explored the whole map, so known routes exist everywhere. */
    private static void revealAll(final GameModelPo data) {
        final Set<Location> known = new HashSet<>();
        for (int x = 1; x <= 50; x++) {
            for (int y = 1; y <= 30; y++) {
                known.add(Location.of(x, y));
            }
        }
        playerPo(data, "France").getVisible().setVisibility(known, 50, 30);
    }

    /** A ship of the given kind on the map, built like a saved one. */
    private static void addShip(final GameModelPo data, final int id,
            final UnitType type, final String owner, final Location at) {
        final UnitPo template = unitPo(data, RIVAL_ID);
        final UnitPo ship = new UnitPo();
        ship.setId(id);
        ship.setType(type);
        ship.setOwnerId(owner);
        ship.setAction(template.getAction());
        ship.setMovementThirds(MovementPoints.ofPoints(type.getSpeed()));
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.east);
        ship.setPlaceMap(place);
        ship.setCargo(new CargoPo());
        final List<UnitPo> units = new ArrayList<>(data.getModel().getUnits());
        units.add(ship);
        data.getModel().setUnits(units);
    }

    /** A saved hold with goods of one type; a fresh slot stays empty. */
    private static CargoSlotPo goodsSlot(final GoodsType type,
            final int amount) {
        final CargoSlotPo slot = new CargoSlotPo();
        slot.setGoodsType(type);
        slot.setAmount(amount);
        return slot;
    }

    /** Gives a saved ship the given holds, in order. */
    private static void load(final GameModelPo data, final int id,
            final CargoSlotPo... slots) {
        final CargoPo cargo = new CargoPo();
        cargo.setSlots(List.of(slots));
        unitPo(data, id).setCargo(cargo);
    }

    /**
     * The colonist founder sails in the first hold of the saved ship, the way
     * the free-play save carries its colonists on the galleon.
     */
    private static void boardFounder(final GameModelPo data, final int shipId) {
        final UnitPo founder = unitPo(data, RIVAL_ID);
        founder.setPlaceMap(null);
        founder.setPlaceCargoSlot(new PlaceCargoSlotPo());
        final CargoSlotPo slot = new CargoSlotPo();
        slot.setUnitId(RIVAL_ID);
        load(data, shipId, slot);
    }

    private static void relate(final GameModelPo data,
            final EuropeanRelationState state) {
        data.getModel().getEuropeanRelations().stream()
                .filter(row -> Set.of("Dutch", "France").equals(
                        Set.of(row.getFirstPlayer(), row.getSecondPlayer())))
                .findFirst().orElseThrow().setState(state);
    }

    private static final DoublePredicate UNDETECTED = roll -> ShipPatrolRules
            .draw(roll) == ShipPatrolRules.Outcome.UNDETECTED;
    private static final DoublePredicate STOPPED = roll -> ShipPatrolRules
            .draw(roll) == ShipPatrolRules.Outcome.STOPPED;
    private static final DoublePredicate SINKS = roll -> roll < 0.5;
    private static final DoublePredicate DAMAGES = roll -> roll >= 0.5;

    /**
     * The first saved state whose successive draws satisfy the conditions in
     * order, so every draw a pass makes is known before it is played.
     */
    private static long statePinning(final DoublePredicate... draws) {
        for (long state = 0;; state++) {
            boolean fits = true;
            for (int i = 0; i < draws.length && fits; i++) {
                fits = draws[i]
                        .test(Model.combatRandomAfter(state + i * GAMMA));
            }
            if (fits) {
                return state;
            }
        }
    }

    /** Builds the model with France to move and the saved draw state. */
    private void start(final GameModelPo data) {
        data.getModel().getGameManager().setGameStarted(true);
        data.getModel().getGameManager().setCurrentPlayer("France");
        model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    /** Reveals what the French ship sees from its present square. */
    private void seeFrom(final int shipId) {
        france().revealMapForUnit(model.getUnitById(shipId));
    }

    /**
     * Pins the next fight to the shared quote of the attack from the approach
     * square, rebuilding the model from its own save. The quote is read live,
     * never guessed. The pass first makes the given number of undetected patrol
     * draws, then the two fight draws.
     */
    private void pinBattle(final int attackerId, final Location approach,
            final Location target, final boolean attackerWins,
            final boolean sinks, final int undetectedBefore) {
        seeFrom(attackerId);
        final double share = quote(model.getUnitById(attackerId), approach,
                target);
        final List<DoublePredicate> draws = new ArrayList<>();
        for (int i = 0; i < undetectedBefore; i++) {
            draws.add(UNDETECTED);
        }
        draws.add(attackerWins ? roll -> roll < share : roll -> roll >= share);
        draws.add(sinks ? SINKS : DAMAGES);
        final ModelPo saved = model.save();
        saved.setCombatRandomState(
                statePinning(draws.toArray(new DoublePredicate[0])));
        model = Model.make(saved,
                MissionImpl.getPredefinedGameOverEvaluators());
    }

    private void pinBattle(final int attackerId, final Location approach,
            final Location target, final boolean sinks) {
        pinBattle(attackerId, approach, target, true, sinks, 0);
    }

    // ------------------------------------------------------------ helpers

    private Player france() {
        return model.getPlayerByName("France");
    }

    private Player dutch() {
        return model.getPlayerByName("Dutch");
    }

    private long combatState() {
        return model.save().getCombatRandomState();
    }

    private SimpleAiPlayer newAi(final Model target) {
        return new SimpleAiPlayer(target, target.getPlayerByName("France"),
                mock(AnimationLock.class));
    }

    /** The French owner turn start alone, bounded in time. */
    private void startFrenchTurn() {
        final SimpleAiPlayer ai = newAi(model);
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5),
                    () -> ai.turnStarted());
        } finally {
            ai.stop();
        }
    }

    /**
     * The real robot flow: the turn start, then the ordinary move of each
     * French unit that can attack, which must leave the warships the naval
     * policy owns alone.
     */
    private void playFrenchTurn() {
        final SimpleAiPlayer ai = newAi(model);
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
                ai.turnStarted();
                for (final Unit unit : france().getUnits()) {
                    if (unit.isAtPlaceLocation() && unit.canAttack()) {
                        ai.moveUnit(unit);
                    }
                }
            });
        } finally {
            ai.stop();
        }
    }

    /** Ends the French turn and the Dutch turn that follows it. */
    private void endRoundUntilFrance() {
        do {
            model.getCurrentPlayer().endTurn();
        } while (model.getCurrentPlayer() != france());
    }

    /** The shared attacker win probability of an attack from that square. */
    private static double quote(final Unit attacker, final Location approach,
            final Location target) {
        return attacker.previewAttack(approach, target)
                .flatMap(order -> order.getCombatStrengths())
                .map(strengths -> strengths.getAttackerWinProbability())
                .orElseThrow();
    }

    private boolean isSunk(final int id) {
        return model.tryGetUnitById(id).isEmpty();
    }

    private boolean isDamaged(final int id) {
        return ((Ship) model.getUnitById(id)).isDamaged();
    }

    /** Goods of one type over every hold of a ship. */
    private static int goodsOf(final Ship ship, final GoodsType type) {
        return ship.getCargo().getSlots().stream()
                .flatMap(slot -> slot.getGoods().stream())
                .filter(goods -> goods.getType() == type)
                .mapToInt(Goods::getAmount).sum();
    }

    /** Goods the shared preview of the adjacent attack would move across. */
    private int lootOf(final int attackerId, final Location target) {
        return model.getUnitById(attackerId).prepareAttack(target, false)
                .flatMap(order -> order.getNavalPreview())
                .map(preview -> preview.getPotentialLootIfAttackerWins())
                .orElseThrow().stream().mapToInt(Goods::getAmount).sum();
    }

    /**
     * Steps of the shortest known route from the ship to a sea square beside
     * the target, zero when it already stands beside it, or -1 when none can be
     * reached. This is the model's own route, not a guessed one.
     */
    private int stepsToApproach(final Unit ship, final Location target) {
        int best = -1;
        for (final Location square : target.getNeighbors()) {
            int steps = -1;
            if (square.equals(ship.getLocation())) {
                steps = 0;
            } else if (model.getMap().isValid(square)
                    && ship.isPossibleToMoveAt(square)) {
                steps = ship.getKnownPath(square, false).map(List::size)
                        .orElse(-1);
            }
            if (steps >= 0 && (best < 0 || steps < best)) {
                best = steps;
            }
        }
        return best;
    }

    // ------------------------------------------------------- target choice

    /**
     * The armed Dutch man-of-war stands beside the warship, but the unarmed
     * merchantman two squares away is the priority. The warship's approach step
     * to (19,10) is beside the armed ship, so the model's patrol check draws
     * once before the fight; that draw is pinned undetected, and the two fight
     * draws follow. The merchantman is sunk and the armed ship is untouched.
     */
    @Test
    void unarmedCarrierRanksBeforeAnAdjacentArmedTarget() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MAN_OF_WAR, "Dutch",
                Location.of(19, 9));
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(20, 11));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, Location.of(19, 10), Location.of(20, 11), true,
                true, 1);
        final int dutchThirds = model.getUnitById(SECOND_NEW_ID)
                .getMovementThirds();
        playFrenchTurn();
        assertTrue(isSunk(THIRD_NEW_ID), "The carrier must be the target");
        assertFalse(isSunk(SECOND_NEW_ID));
        assertEquals(Location.of(19, 9),
                model.getUnitById(SECOND_NEW_ID).getLocation());
        assertEquals(dutchThirds,
                model.getUnitById(SECOND_NEW_ID).getMovementThirds());
        assertFalse(isDamaged(SECOND_NEW_ID));
    }

    /**
     * The armed Dutch man-of-war stands beside the warship at exactly the
     * shared threshold, 0.6, so the attack is made and the defender is sunk.
     */
    @Test
    void anArmedTargetAtExactlySixtyPercentIsAttacked() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MAN_OF_WAR, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), true);
        assertEquals(0.6, quote(model.getUnitById(FIRST_NEW_ID), WARSHIP_START,
                Location.of(19, 9)), EPSILON);
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat + 2 * GAMMA, combatState(),
                "One fight draws exactly twice");
        assertTrue(isSunk(SECOND_NEW_ID));
        assertFalse(isDamaged(FIRST_NEW_ID));
    }

    /**
     * A battle the attacker loses is a legal outcome too: at 0.6 the first draw
     * lands above the quote and the second leaves the loser damaged, so the
     * French warship goes to repair and the Dutch ship keeps its place.
     */
    @Test
    void anAttackerThatLosesTheDrawIsDamagedByTheSharedRule() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MAN_OF_WAR, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), false, false,
                0);
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat + 2 * GAMMA, combatState());
        assertFalse(isSunk(SECOND_NEW_ID));
        assertFalse(isDamaged(SECOND_NEW_ID));
        assertTrue(isDamaged(FIRST_NEW_ID),
                "The losing attacker goes to repair");
    }

    /**
     * A frigate beside a man-of-war quotes 0.5, below the threshold, so it
     * never attacks. The only patrol square is the one north of the port, which
     * is also beside the Dutch ship, so the model's own interception check
     * draws once there; the pinned state makes that draw stop the ship. The
     * exact consequence is therefore one draw, no battle, and a frigate that
     * has used its movement beside an untouched Dutch ship.
     */
    @Test
    void anArmedTargetBelowSixtyPercentIsNotAttackedAndOnlyPatrolsIntercepted() {
        final GameModelPo data = tinyPort();
        addShip(data, FIRST_NEW_ID, UnitType.FRIGATE, "France", WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MAN_OF_WAR, "Dutch",
                Location.of(19, 9));
        data.getModel().setCombatRandomState(statePinning(STOPPED));
        start(data);
        model.declareHostilities(france(), dutch());
        seeFrom(FIRST_NEW_ID);
        assertEquals(0, model.getCalendar().getNumberOfPlayedTurns(),
                "The patrol waypoint rotation starts at the first square");
        assertEquals(0.5, quote(model.getUnitById(FIRST_NEW_ID), WARSHIP_START,
                Location.of(19, 9)), EPSILON);
        final long pinned = combatState();
        final int dutchThirds = model.getUnitById(SECOND_NEW_ID)
                .getMovementThirds();
        playFrenchTurn();
        assertEquals(pinned + GAMMA, combatState(),
                "Only the one patrol draw, not a fight's two");
        assertFalse(isSunk(FIRST_NEW_ID));
        assertFalse(isSunk(SECOND_NEW_ID));
        assertFalse(isDamaged(FIRST_NEW_ID));
        assertFalse(isDamaged(SECOND_NEW_ID));
        assertEquals(Location.of(19, 9),
                model.getUnitById(SECOND_NEW_ID).getLocation());
        assertEquals(dutchThirds,
                model.getUnitById(SECOND_NEW_ID).getMovementThirds(),
                "A fight would have spent the defender's movement");
        assertEquals(PATROL_SEA, model.getUnitById(FIRST_NEW_ID).getLocation());
        assertEquals(0, model.getUnitById(FIRST_NEW_ID).getMovementThirds());
    }

    /**
     * Two merchantmen at equal known distance: the lower id (9, east of the
     * warship) is sunk first, and the higher id (10, north-east) survives.
     */
    @Test
    void equalDistanceCarriersRankByTheLowerTargetId() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(20, 9));
        addShip(data, FOURTH_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(20, 7));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, Location.of(19, 8), Location.of(20, 9), true);
        playFrenchTurn();
        assertTrue(isSunk(THIRD_NEW_ID));
        assertFalse(isSunk(FOURTH_NEW_ID));
    }

    // ----------------------------------------------------- peace and allies

    /** Peace bars an ordinary warship: no declaration is made for it. */
    @Test
    void peaceBarsAnOrdinaryWarshipFromAttackingACarrier() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        start(data);
        seeFrom(FIRST_NEW_ID);
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat, combatState());
        assertFalse(isSunk(SECOND_NEW_ID));
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(france(), dutch()));
    }

    /**
     * A privateer attacks a peaceful carrier through the privateer exception:
     * the battle is fought, the sunk carrier is the result, and the relation
     * stays at peace with no war declared.
     */
    @Test
    void privateerAttacksAPeacefulCarrierWithoutDeclaringWar() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.PRIVATEER, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        start(data);
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), true);
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat + 2 * GAMMA, combatState());
        assertTrue(isSunk(SECOND_NEW_ID));
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(france(), dutch()));
    }

    /** An alliance restrains every attack, privateers included. */
    @Test
    void allianceRestrainsEvenAPrivateer() {
        final GameModelPo data = seaPort();
        relate(data, EuropeanRelationState.ALLIANCE);
        addShip(data, FIRST_NEW_ID, UnitType.PRIVATEER, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        start(data);
        seeFrom(FIRST_NEW_ID);
        assertEquals(EuropeanRelationState.ALLIANCE,
                model.getEuropeanRelation(france(), dutch()));
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat, combatState());
        assertFalse(isSunk(SECOND_NEW_ID));
    }

    // ----------------------------------------------------- known and unknown

    /**
     * A carrier beyond the warship's sight is never a target: the warship does
     * not chase it and no battle is fought, and it stays on known sea inside
     * the port band.
     */
    @Test
    void aHiddenCarrierIsIgnoredAndTheWarshipStaysInItsBand() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MERCHANTMAN, "Dutch", HIDDEN_SEA);
        start(data);
        model.declareHostilities(france(), dutch());
        final Unit hidden = model.getUnitById(SECOND_NEW_ID);
        assertFalse(france().isCurrentlyVisible(hidden.getLocation()));
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat, combatState());
        assertEquals(HIDDEN_SEA, hidden.getLocation());
        assertTrue(model.getUnitById(FIRST_NEW_ID).getLocation()
                .getDistanceChebyshev(QUEBEC) <= PATROL_RADIUS);
    }

    /**
     * No target is visible, so the warship patrols known sea within five
     * squares of Quebec for several owner turns, heading for a different band
     * square as the played-turn number advances, and never leaves that band.
     */
    @Test
    void noTargetPatrolsTheKnownSeaWithinFiveSquaresOfThePort() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        start(data);
        final Unit warship = model.getUnitById(FIRST_NEW_ID);
        final long combat = combatState();
        Location previous = WARSHIP_START;
        boolean moved = false;
        for (int round = 0; round < PATROL_ROUNDS; round++) {
            if (round > 0) {
                endRoundUntilFrance();
            }
            playFrenchTurn();
            final Location at = warship.getLocation();
            moved |= !at.equals(previous);
            previous = at;
            assertTrue(at.getDistanceChebyshev(QUEBEC) <= PATROL_RADIUS,
                    "Round " + round + ": left the band");
            assertTrue(model.getMap().getTerrainTypeAt(at).isSea(),
                    "Round " + round + ": left the sea");
        }
        assertTrue(moved, "The warship never patrolled");
        assertEquals(combat, combatState());
    }

    // -------------------------------------------------------- multi-turn

    /**
     * The carrier is walled in, and the model's own route to a square beside it
     * is a full owner turn long, so the first turn can only approach. The test
     * compares the model's route length before and after the turn instead of
     * naming squares: the warship ends the first turn nearer without a fight,
     * and fights the pinned battle on the second turn from beside the carrier.
     */
    @Test
    void multiTurnPursuitReachesTheCarrierAfterItsFirstTurnMoves() {
        final GameModelPo data = channelPort(true);
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                CARRIER_SQUARE);
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, APPROACH, CARRIER_SQUARE, true);
        final Unit warship = model.getUnitById(FIRST_NEW_ID);
        final int stepsBefore = stepsToApproach(warship, CARRIER_SQUARE);
        assertTrue(stepsBefore >= UnitType.MAN_OF_WAR.getSpeed(),
                "The approach must cost a whole turn, was " + stepsBefore);
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat, combatState(), "No battle on the first turn");
        assertFalse(isSunk(THIRD_NEW_ID));
        final int stepsAfter = stepsToApproach(warship, CARRIER_SQUARE);
        assertTrue(stepsAfter >= 0 && stepsAfter < stepsBefore,
                "The first turn must close the distance: " + stepsBefore
                        + " then " + stepsAfter);
        endRoundUntilFrance();
        playFrenchTurn();
        assertNotEquals(combat, combatState(), "A fight on the second turn");
        assertTrue(isSunk(THIRD_NEW_ID));
        assertTrue(warship.getLocation().isNeighbor(CARRIER_SQUARE),
                "The fight is from beside the carrier");
    }

    /**
     * Peace accepted between the two turns stops the pursuit: the warship
     * fights nothing on the second turn and the carrier stays afloat.
     */
    @Test
    void peaceAcceptedMidPursuitStopsTheAttack() {
        final GameModelPo data = channelPort(true);
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                CARRIER_SQUARE);
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, APPROACH, CARRIER_SQUARE, true);
        final int stepsBefore = stepsToApproach(model.getUnitById(FIRST_NEW_ID),
                CARRIER_SQUARE);
        final long combat = combatState();
        playFrenchTurn();
        assertTrue(stepsToApproach(model.getUnitById(FIRST_NEW_ID),
                CARRIER_SQUARE) < stepsBefore);
        model.acceptPeace(france(), dutch());
        endRoundUntilFrance();
        playFrenchTurn();
        assertEquals(combat, combatState());
        assertFalse(isSunk(THIRD_NEW_ID));
        assertEquals(EuropeanRelationState.PEACE,
                model.getEuropeanRelation(france(), dutch()));
    }

    /**
     * The carrier leaves the warship's sight between the turns: the pursuit
     * ends, no battle is drawn, and the carrier keeps the square it moved to.
     */
    @Test
    void vanishedTargetEndsThePursuitWithoutABattle() {
        final GameModelPo data = channelPort(true);
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                CARRIER_SQUARE);
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, APPROACH, CARRIER_SQUARE, true);
        final long combat = combatState();
        playFrenchTurn();
        final Unit carrier = model.getUnitById(THIRD_NEW_ID);
        carrier.placeToLocation(HIDDEN_SEA);
        endRoundUntilFrance();
        assertFalse(france().isCurrentlyVisible(HIDDEN_SEA));
        playFrenchTurn();
        assertEquals(combat, combatState());
        assertEquals(HIDDEN_SEA, carrier.getLocation());
        assertFalse(isSunk(THIRD_NEW_ID));
    }

    /**
     * With the approach closed the carrier has no sea neighbour. The warship
     * finds no legal approach and never attacks, however often the owner turn
     * repeats, and it patrols inside the band instead.
     */
    @Test
    void blockedRouteFindsNoApproachAndNoBattle() {
        final GameModelPo data = channelPort(false);
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                CARRIER_SQUARE);
        start(data);
        model.declareHostilities(france(), dutch());
        seeFrom(FIRST_NEW_ID);
        assertEquals(-1, stepsToApproach(model.getUnitById(FIRST_NEW_ID),
                CARRIER_SQUARE));
        final long combat = combatState();
        final Unit warship = model.getUnitById(FIRST_NEW_ID);
        for (int round = 0; round < 2; round++) {
            if (round > 0) {
                endRoundUntilFrance();
            }
            playFrenchTurn();
            assertEquals(combat, combatState());
            assertTrue(warship.getLocation()
                    .getDistanceChebyshev(QUEBEC) <= PATROL_RADIUS);
        }
        assertFalse(isSunk(THIRD_NEW_ID));
        assertEquals(CARRIER_SQUARE,
                model.getUnitById(THIRD_NEW_ID).getLocation());
    }

    // -------------------------------------------------- reservations

    /**
     * The trade carrier and the expansion carrier are reserved to their own
     * cycles, so the naval policy never claims them; a third warship, which
     * nothing reserves, still fights the adjacent merchantman at the quote.
     */
    @Test
    void reservedTradeAndExpansionCarriersAreNeverClaimed() {
        final GameModelPo data = seaPort();
        final int tradeCarrier = FIRST_NEW_ID;
        final int expansionCarrier = SECOND_NEW_ID;
        final int freeWarship = FIFTH_NEW_ID;
        addShip(data, tradeCarrier, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, expansionCarrier, UnitType.MAN_OF_WAR, "France",
                PATROL_SEA);
        addShip(data, freeWarship, UnitType.MAN_OF_WAR, "France",
                Location.of(19, 10));
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(freeWarship, Location.of(19, 10), Location.of(19, 9), true);
        france().setRivalTradeObjective(new RivalTradeObjective(QUEBEC_ID,
                tradeCarrier, RivalTradeObjective.Stage.TO_EUROPE));
        france().setRivalExpansionObjective(new RivalExpansionObjective(
                QUEBEC_ID, RIVAL_ID, expansionCarrier, Location.of(18, 12),
                RivalExpansionObjective.Stage.TRAVEL));
        final SimpleAiPlayer ai = newAi(model);
        final RivalTradeCycle trade = new RivalTradeCycle(ai);
        final RivalExpansionCycle expansion = new RivalExpansionCycle(ai,
                trade);
        final RivalNavalMilitaryCycle naval = new RivalNavalMilitaryCycle(ai,
                trade, expansion);
        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> naval.advance(Optional.empty()));
        assertFalse(naval.isParticipant(model.getUnitById(tradeCarrier)));
        assertFalse(naval.isParticipant(model.getUnitById(expansionCarrier)));
        assertTrue(naval.isParticipant(model.getUnitById(freeWarship)));
        assertTrue(isSunk(THIRD_NEW_ID));
        assertEquals(WARSHIP_START,
                model.getUnitById(tradeCarrier).getLocation());
        assertEquals(PATROL_SEA,
                model.getUnitById(expansionCarrier).getLocation());
    }

    /**
     * France has no colony, so its founder sails aboard an armed, loaded
     * frigate: that frigate is the first-colony transport. Through the real
     * robot flow the transport must neither fight nor be claimed by the naval
     * policy, even with a merchantman it could sink at 0.8 beside it.
     */
    @Test
    void firstColonyTransportIsPreservedEvenWhenArmedAndLoaded() {
        final GameModelPo data = noColonyPort();
        addShip(data, FIRST_NEW_ID, UnitType.FRIGATE, "France", WARSHIP_START);
        boardFounder(data, FIRST_NEW_ID);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        seeFrom(FIRST_NEW_ID);
        assertTrue(france().getColonies().isEmpty());
        final Ship frigate = (Ship) model.getUnitById(FIRST_NEW_ID);
        assertTrue(frigate.canAttack());
        assertTrue(model.getUnitById(RIVAL_ID).isAtCargoSlot());
        assertEquals(0.8, quote(frigate, WARSHIP_START, Location.of(19, 9)),
                EPSILON);
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat, combatState(),
                "The transport must not fight for the first colony");
        assertFalse(isSunk(THIRD_NEW_ID));
        assertSame(france(), frigate.getOwner());
        assertFalse(frigate.isDamaged());
    }

    /**
     * France has no colony, and a spare armed frigate that carries no founder
     * stands beside a stronger Dutch ship at 0.5. It has no port to patrol
     * from, so it must not move, be intercepted or fight: the old random loop
     * would have attacked it below the threshold.
     */
    @Test
    void spareWarshipWithoutAColonyIsGovernedBelowSixtyPercent() {
        final GameModelPo data = noColonyPort();
        addShip(data, FIRST_NEW_ID, UnitType.FRIGATE, "France", WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MAN_OF_WAR, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        seeFrom(FIRST_NEW_ID);
        assertTrue(france().getColonies().isEmpty());
        assertEquals(0.5, quote(model.getUnitById(FIRST_NEW_ID), WARSHIP_START,
                Location.of(19, 9)), EPSILON);
        final long combat = combatState();
        final int dutchThirds = model.getUnitById(SECOND_NEW_ID)
                .getMovementThirds();
        playFrenchTurn();
        assertEquals(combat, combatState(), "No fight and no patrol draw");
        assertEquals(WARSHIP_START,
                model.getUnitById(FIRST_NEW_ID).getLocation());
        assertEquals(dutchThirds,
                model.getUnitById(SECOND_NEW_ID).getMovementThirds());
        assertFalse(isSunk(FIRST_NEW_ID));
        assertFalse(isSunk(SECOND_NEW_ID));
    }

    /**
     * A spare warship of a rival without a colony is still governed when a
     * target is worth it: a man-of-war sinks the adjacent merchantman at the
     * shared 36 / 42 quote with the pinned draws.
     */
    @Test
    void spareWarshipWithoutAColonyStillHuntsAtTheSharedQuote() {
        final GameModelPo data = noColonyPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), true);
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat + 2 * GAMMA, combatState());
        assertTrue(isSunk(THIRD_NEW_ID));
    }

    // ------------------------------------------------- loaded warships

    /**
     * A frigate with goods aboard is still an armed ship under the naval
     * policy. Beside the Dutch man-of-war it quotes 0.5, so it never attacks
     * and the ordinary unit loop leaves it alone. Its one patrol step beside
     * the Dutch ship is checked by the model's interception, pinned to stop it,
     * so the exact consequence is one draw and unchanged cargo.
     */
    @Test
    void loadedWarshipBelowSixtyPercentNeitherAttacksNorMovesItsCargo() {
        final GameModelPo data = tinyPort();
        addShip(data, FIRST_NEW_ID, UnitType.FRIGATE, "France", WARSHIP_START);
        load(data, FIRST_NEW_ID, goodsSlot(GoodsType.CORN, 2),
                new CargoSlotPo());
        addShip(data, SECOND_NEW_ID, UnitType.MAN_OF_WAR, "Dutch",
                Location.of(19, 9));
        data.getModel().setCombatRandomState(statePinning(STOPPED));
        start(data);
        model.declareHostilities(france(), dutch());
        seeFrom(FIRST_NEW_ID);
        final Ship frigate = (Ship) model.getUnitById(FIRST_NEW_ID);
        assertEquals(0, model.getCalendar().getNumberOfPlayedTurns());
        assertEquals(2, goodsOf(frigate, GoodsType.CORN));
        final long pinned = combatState();
        final int dutchThirds = model.getUnitById(SECOND_NEW_ID)
                .getMovementThirds();
        playFrenchTurn();
        assertEquals(pinned + GAMMA, combatState(),
                "Only the one patrol draw, not a fight's two");
        assertFalse(isSunk(SECOND_NEW_ID));
        assertFalse(frigate.isDamaged());
        assertFalse(isDamaged(SECOND_NEW_ID));
        assertEquals(dutchThirds,
                model.getUnitById(SECOND_NEW_ID).getMovementThirds());
        assertEquals(PATROL_SEA, frigate.getLocation());
        assertEquals(2, goodsOf(frigate, GoodsType.CORN));
    }

    /**
     * A loaded frigate beside a loaded merchantman fights at the shared quote
     * of 24 / 30. The sunk carrier's goods move to the frigate's free hold, as
     * the shared preview planned, and the frigate's own goods stay aboard.
     */
    @Test
    void loadedWarshipWinsALootingBattleAtTheSharedQuote() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.FRIGATE, "France", WARSHIP_START);
        load(data, FIRST_NEW_ID, goodsSlot(GoodsType.CORN, 2),
                new CargoSlotPo());
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        load(data, THIRD_NEW_ID, goodsSlot(GoodsType.CORN, 4));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), true);
        assertEquals(0.8, quote(model.getUnitById(FIRST_NEW_ID), WARSHIP_START,
                Location.of(19, 9)), EPSILON);
        final int loot = lootOf(FIRST_NEW_ID, Location.of(19, 9));
        assertEquals(4, loot);
        final Ship frigate = (Ship) model.getUnitById(FIRST_NEW_ID);
        playFrenchTurn();
        assertTrue(isSunk(THIRD_NEW_ID));
        assertEquals(2 + loot, goodsOf(frigate, GoodsType.CORN));
    }

    /**
     * The same looting fight, but the draws leave the loaded merchantman
     * damaged instead of sunk. The shared rule moves the planned loot at once,
     * before the loser is damaged, so the frigate still gains it and the
     * merchantman goes to repair.
     */
    @Test
    void loadedLoserThatIsOnlyDamagedStillYieldsThePlannedLoot() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.FRIGATE, "France", WARSHIP_START);
        load(data, FIRST_NEW_ID, goodsSlot(GoodsType.CORN, 2),
                new CargoSlotPo());
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        load(data, THIRD_NEW_ID, goodsSlot(GoodsType.CORN, 4));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), false);
        final int loot = lootOf(FIRST_NEW_ID, Location.of(19, 9));
        assertEquals(4, loot);
        final Ship frigate = (Ship) model.getUnitById(FIRST_NEW_ID);
        playFrenchTurn();
        assertFalse(isSunk(THIRD_NEW_ID));
        assertTrue(isDamaged(THIRD_NEW_ID));
        assertEquals(2 + loot, goodsOf(frigate, GoodsType.CORN));
    }

    /**
     * A battle that the draws leave damaged rather than sunk: the merchantman
     * goes to repair, so it is no longer on the map and cannot be attacked
     * again on the next owner turn.
     */
    @Test
    void damagedCarrierIsNotAttackedAgainWhileItIsInRepair() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, THIRD_NEW_ID, UnitType.MERCHANTMAN, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), false);
        playFrenchTurn();
        assertFalse(isSunk(THIRD_NEW_ID));
        assertTrue(isDamaged(THIRD_NEW_ID));
        endRoundUntilFrance();
        final long combat = combatState();
        playFrenchTurn();
        assertEquals(combat, combatState());
    }

    // ----------------------------------------------------------- continue

    /**
     * After the battle is fought and saved, a restored model replans from the
     * live state: the spent warship has no movement, so no second battle or
     * move follows.
     */
    @Test
    void continueDoesNotRepeatTheBattle() {
        final GameModelPo data = seaPort();
        addShip(data, FIRST_NEW_ID, UnitType.MAN_OF_WAR, "France",
                WARSHIP_START);
        addShip(data, SECOND_NEW_ID, UnitType.MAN_OF_WAR, "Dutch",
                Location.of(19, 9));
        start(data);
        model.declareHostilities(france(), dutch());
        pinBattle(FIRST_NEW_ID, WARSHIP_START, Location.of(19, 9), true);
        playFrenchTurn();
        assertTrue(isSunk(SECOND_NEW_ID));
        model = Model.make(model.save(),
                MissionImpl.getPredefinedGameOverEvaluators());
        final long restoredCombat = combatState();
        final Unit restored = model.getUnitById(FIRST_NEW_ID);
        final Location at = restored.getLocation();
        final SimpleAiPlayer ai = newAi(model);
        try {
            assertTimeoutPreemptively(Duration.ofSeconds(5),
                    () -> ai.turnStarted());
        } finally {
            ai.stop();
        }
        assertEquals(restoredCombat, combatState());
        assertEquals(at, restored.getLocation());
    }
}
