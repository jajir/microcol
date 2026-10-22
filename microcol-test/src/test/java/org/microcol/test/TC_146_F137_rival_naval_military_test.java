package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Direction;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.MovementPoints;
import org.microcol.model.TerrainType;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.ModelPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NavalBattlePage;
import org.microcol.page.RivalNavalPage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Rival naval military on the free-play map, through real French and Dutch
 * owner turns. In that save the sea is the {@code _} terrain and Quebec (17,10)
 * has no sea neighbour, so each fixture turns its unworked field slot at (18,9)
 * into ocean, where the French man-of-war starts. Every other ocean square of
 * the window around the port becomes grassland, so the water left is exactly
 * the listed set, and the squares turned to ocean lose the trees, fields, roads
 * and clearings that an ocean square cannot carry.
 *
 * The Dutch merchantman sits at (20,9) with land on seven sides and the sea
 * square (21,9) on the east. The only route from the port is six steps long,
 * and a man-of-war has six movement points, so the warship uses its whole first
 * turn to arrive beside the carrier and can fight only on the second turn. The
 * whole route stays within two squares of the carrier, so it remains in sight.
 *
 * Three scenarios use different saved positions. At war the warship pursues the
 * carrier, fights it on the second turn and sinks it, within ten rounds, and
 * Save and Continue keeps that outcome with no second battle. At peace an
 * ordinary warship never attacks, and its carrier keeps its square for every
 * round. With no carrier the warship patrols known sea within five squares of
 * Quebec and stays there.
 *
 * The saved battle draws are pinned to the shared quote of the carrier attack,
 * read from a detached copy of the game with France as the acting player, so
 * the sinking is the one the model rates.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_146_F137_rival_naval_military_test
        extends AbstractMicroColTest {

    /**
     * The armed French colonist, disarmed so the warship is the only attacker.
     */
    private static final int RIVAL_ID = 6;
    private static final int WARSHIP_ID = 7;
    private static final int CARRIER_ID = 8;
    private static final Location QUEBEC = Location.of(17, 10);
    private static final Location WARSHIP_START = Location.of(18, 9);
    private static final Location CARRIER_START = Location.of(20, 9);
    /** The only sea square beside the walled carrier. */
    private static final Location APPROACH = Location.of(21, 9);
    private static final int WINDOW_FROM_X = 17;
    private static final int WINDOW_TO_X = 24;
    private static final int WINDOW_FROM_Y = 3;
    private static final int WINDOW_TO_Y = 13;
    private static final long GAMMA = 0x9E3779B97F4A7C15L;
    /** The encounter must come within this many French turns. */
    private static final int ENCOUNTER_ROUNDS = 10;
    /** Rounds at peace; the carrier must keep its square in each of them. */
    private static final int PEACE_ROUNDS = 3;
    /** Rounds with no visible carrier; the warship must stay in its band. */
    private static final int PATROL_ROUNDS = 5;

    /** The saved game the Load command opens; this scenario swaps it. */
    private volatile File loadTarget;

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), binder -> {
            binder.bind(CursorService.class)
                    .toInstance(new CursorServiceNoOpp());
            final FileSelectingService selecting = Mockito
                    .mock(FileSelectingService.class);
            Mockito.when(selecting.loadFile(Mockito.any(File.class)))
                    .thenAnswer(invocation -> loadTarget);
            binder.bind(FileSelectingService.class).toInstance(selecting);
        });
    }

    private static UnitPo unitPo(final GameModelPo data, final int id) {
        return data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == id).findFirst().orElseThrow();
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

    /**
     * The window's water becomes exactly the given set: those squares turn to
     * ocean, and every other ocean square of the window turns to grassland.
     * Land squares of the window, such as Quebec's fields, are not touched.
     */
    private static void shape(final GameModelPo data, final Set<Location> sea) {
        paintOcean(data, sea);
        final Map<Location, TerrainType> terrain = new HashMap<>(
                data.getModel().getMap().getTerrainMap());
        for (int x = WINDOW_FROM_X; x <= WINDOW_TO_X; x++) {
            for (int y = WINDOW_FROM_Y; y <= WINDOW_TO_Y; y++) {
                final Location at = Location.of(x, y);
                if (!sea.contains(at) && !terrain.containsKey(at)) {
                    terrain.put(at, TerrainType.GRASSLAND);
                }
            }
        }
        data.getModel().getMap().setTerrainType(terrain);
    }

    /**
     * The port square, the six-step detour around the carrier's walls and the
     * carrier's own square. See the class comment for the route.
     */
    private static Set<Location> channelSea() {
        return new HashSet<>(List.of(WARSHIP_START, Location.of(18, 8),
                Location.of(19, 7), Location.of(20, 7), Location.of(21, 7),
                Location.of(22, 8), APPROACH, CARRIER_START));
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

    /**
     * The first saved state whose first draw is below the win share and whose
     * second draw is below one half, so the carrier attack is won and the loser
     * is sunk. No other draw happens before it in this scenario.
     */
    private static long sinkingState(final double winShare) {
        for (long state = 0;; state++) {
            if (Model.combatRandomAfter(state) < winShare
                    && Model.combatRandomAfter(state + GAMMA) < 0.5) {
                return state;
            }
        }
    }

    /**
     * The shared quote of the carrier attack from the east approach, read from
     * a detached copy with France as the acting player, so the live game is
     * never changed.
     */
    private static double carrierQuote(final Model model) {
        final ModelPo copySave = model.save();
        copySave.getGameManager().setCurrentPlayer("France");
        final Model copy = Model.make(copySave,
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            return copy.getUnitById(WARSHIP_ID)
                    .previewAttack(APPROACH, CARRIER_START)
                    .flatMap(order -> order.getCombatStrengths())
                    .map(strengths -> strengths.getAttackerWinProbability())
                    .orElseThrow();
        } finally {
            copy.stop();
        }
    }

    /**
     * Saves a free-play game at the start of the human turn. The French warship
     * is on its square and, when asked, the Dutch carrier is walled in at
     * (20,9). The nations are at war when asked, otherwise at peace.
     */
    private static File saveFixture(final String name, final boolean war,
            final boolean withCarrier) throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play.microcol");
        shape(data, channelSea());
        revealAll(data);
        // The saved armed colonist would fight on land; the warship is the only
        // attacker in these scenarios.
        unitPo(data, RIVAL_ID).setHoldingGuns(false);
        addShip(data, WARSHIP_ID, UnitType.MAN_OF_WAR, "France", WARSHIP_START);
        if (withCarrier) {
            addShip(data, CARRIER_ID, UnitType.MERCHANTMAN, "Dutch",
                    CARRIER_START);
        }
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            model.startGame();
            for (int guard = 0; guard < 4 && model.getCurrentPlayer() != model
                    .getPlayerByName("Dutch"); guard++) {
                model.getCurrentPlayer().endTurn();
            }
            if (model.getCurrentPlayer() != model.getPlayerByName("Dutch")) {
                throw new IllegalStateException("The human turn never came");
            }
            if (war) {
                model.declareHostilities(model.getPlayerByName("Dutch"),
                        model.getPlayerByName("France"));
            }
            final boolean battle = war && withCarrier;
            // A battle, if one comes, is pinned to the shared quote of the
            // carrier attack. Scenarios without one keep their saved state.
            final double share = battle ? carrierQuote(model) : 0.0;
            data.setModel(model.save());
            if (battle) {
                data.getModel().setCombatRandomState(sinkingState(share));
            }
        } finally {
            model.stop();
        }
        final Path file = Files.createDirectories(Path.of("target", "fixtures"))
                .resolve(name);
        dao.saveToFile(file.toString(), data);
        return file.toFile();
    }

    @Test
    @Tag("local")
    void TC_01_warship_pursues_visible_carrier_and_sinks_it_on_its_second_turn()
            throws Exception {
        loadTarget = saveFixture("F137-encounter.microcol", true, true);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalNavalPage rival = new RivalNavalPage(getContext());
        rival.verifyEncounterReady(WARSHIP_ID, WARSHIP_START, CARRIER_ID,
                CARRIER_START);
        // The model's own route to a square beside the carrier is a whole turn
        // long, so the first turn can only approach.
        final int stepsBefore = rival.readApproachSteps(WARSHIP_ID,
                CARRIER_START);
        rival.verifyApproachNeedsAWholeTurn(WARSHIP_ID, stepsBefore);
        boolean fought = false;
        int fightTurn = -1;
        for (int turn = 0; turn < ENCOUNTER_ROUNDS && !fought; turn++) {
            final int next = rival.readRound() + 1;
            final String combatBefore = rival.readCombatState();
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyEncounterTurn("Round " + next, WARSHIP_ID, CARRIER_ID);
            fought = rival.isBattleFought(combatBefore);
            if (fought) {
                fightTurn = turn;
            } else if (turn == 0) {
                rival.verifyFirstTurnApproach("Round " + next, WARSHIP_ID,
                        CARRIER_ID, CARRIER_START, stepsBefore);
            }
        }
        // The shared quote wins the pinned draw and the second draw sinks the
        // carrier, so it is removed and the warship keeps its French flag.
        rival.verifyEncounterBattle(WARSHIP_ID, CARRIER_ID, fought,
                ENCOUNTER_ROUNDS);
        rival.verifyFoughtAfterApproach(fightTurn);
        // The sunk carrier was the human's, so after the fight animation the
        // game reports the loss in the naval outcome window; the player reads
        // it and confirms it before leaving the game.
        final NavalBattlePage naval = new NavalBattlePage(getContext());
        naval.verifyOutcomeText("#" + CARRIER_ID + " sank.",
                "ship #" + WARSHIP_ID + ", captured:");
        naval.closeOutcome();
        final String before = rival.readCombatState();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.waitForHumanRound(rival.readRound());
        rival.verifyContinueUnchanged(before, CARRIER_ID);
    }

    @Test
    @Tag("local")
    void TC_02_peaceful_warship_never_attacks_the_visible_carrier()
            throws Exception {
        loadTarget = saveFixture("F137-peace.microcol", false, true);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalNavalPage rival = new RivalNavalPage(getContext());
        rival.verifyPeaceReady(WARSHIP_ID, WARSHIP_START, CARRIER_ID,
                CARRIER_START);
        for (int turn = 0; turn < PEACE_ROUNDS; turn++) {
            final int next = rival.readRound() + 1;
            final String combatBefore = rival.readCombatState();
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyPeaceTurn("Round " + next, WARSHIP_ID, CARRIER_ID,
                    CARRIER_START, combatBefore);
        }
    }

    @Test
    @Tag("local")
    void TC_03_warship_without_a_carrier_patrols_within_five_squares_of_the_port()
            throws Exception {
        loadTarget = saveFixture("F137-patrol.microcol", true, false);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalNavalPage rival = new RivalNavalPage(getContext());
        rival.verifyPatrolReady(WARSHIP_ID, WARSHIP_START);
        Location previous = rival.readLocation(WARSHIP_ID);
        boolean moved = false;
        for (int turn = 0; turn < PATROL_ROUNDS; turn++) {
            final int next = rival.readRound() + 1;
            final String combatBefore = rival.readCombatState();
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyPatrolTurn("Round " + next, WARSHIP_ID, QUEBEC,
                    combatBefore);
            final Location at = rival.readLocation(WARSHIP_ID);
            moved |= !at.equals(previous);
            previous = at;
        }
        rival.verifyPatrolled(moved);
    }
}
