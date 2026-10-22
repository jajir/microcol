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
import java.util.stream.Collectors;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Colony;
import org.microcol.model.Direction;
import org.microcol.model.Goods;
import org.microcol.model.GoodsType;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.CargoPo;
import org.microcol.model.store.CargoSlotPo;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.PlayerPo;
import org.microcol.model.store.UnitPo;
import org.microcol.model.store.WorldMapPo;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.RivalColonyPage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Rival multi-colony scheduling through forty real France owner turns. France
 * holds two established colonies in one walled basin: Rouen at (21,2) with its
 * founder and three crew, and Lyon at (15,3) with its founder and two crew.
 * Each colony keeps 50 tools, and each has food for far more than five turns,
 * so both meet the reserves a further colony needs. Two ships wait in Rouen's
 * port: the first is the supply carrier, the second one is free for the
 * expansion. The whole map is known to France.
 *
 * A mountain wall splits the basin into a northern part with both colonies and
 * a southern part with the only legal site, a coastal grassland block. The wall
 * has a single sea square, the channel at (20,12). The Dutch ship waits south
 * of it at (20,14). The Dutch hold a colony as well, so the game does not end
 * by the no-colony rule.
 *
 * France releases a founder from Rouen once its crew work the fields and sails
 * toward the site. When the carrier has left the port and is still short of the
 * channel, the human closes it with the Dutch ship through the ordinary map
 * controls. France keeps the same objective and founder aboard and retries each
 * turn. During the block the saved game is continued from disk and nothing
 * changes. Then the human clears the channel, the carrier passes and the
 * founder founds the third colony exactly once. The scenario runs all forty
 * owner turns. Both established colonies must survive them without starving,
 * keep their tools and their crews, and the new colony's own empty tool store
 * must keep France from preparing a fourth.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_147_F138_rival_multi_colony_test extends AbstractMicroColTest {

    private static final Location COLONY_A = Location.of(21, 2);
    private static final Location COLONY_B = Location.of(15, 3);
    /** The one sea square of the wall between the basin parts. */
    private static final Location CHANNEL = Location.of(20, 12);
    /** Where the Dutch ship waits, south of the channel. */
    private static final Location WAITING = Location.of(20, 14);
    /** West edge of the 3x3 grassland block that holds the legal site. */
    private static final Location SITE_BLOCK = Location.of(23, 15);
    /**
     * Land beside the Dutch ship's former place, so the human keeps a colony.
     */
    private static final Location DUTCH_SITE = Location.of(31, 15);
    /** Land beside Rouen: five corn fields. */
    private static final List<Location> FIELDS_A = List.of(Location.of(22, 1),
            Location.of(22, 2), Location.of(22, 3), Location.of(21, 3),
            Location.of(20, 3));
    /** Land beside Lyon: five corn fields. */
    private static final List<Location> FIELDS_B = List.of(Location.of(14, 2),
            Location.of(14, 3), Location.of(14, 4), Location.of(15, 4),
            Location.of(16, 4));
    private static final int COLONY_A_ID = 0;
    private static final int COLONY_B_ID = 1;
    private static final List<Integer> ESTABLISHED = List.of(COLONY_A_ID,
            COLONY_B_ID);
    private static final int FOUNDER_ID = 4;
    private static final int SHIP_ID = 5;
    private static final int EXPANSION_SHIP_ID = 9;
    private static final int B_FOUNDER_ID = 10;
    private static final int DUTCH_SETTLER_ID = 0;
    private static final int DUTCH_SHIP_ID = 3;
    /** Rouen's three crew, who reach the colony square. */
    private static final List<Integer> A_CREW = List.of(6, 7, 8);
    /** Lyon's two crew, who reach its colony square. */
    private static final List<Integer> B_CREW = List.of(11, 12);
    /** Every original French colonist; any other one must be a birth. */
    private static final Set<Integer> CREW = Set.of(FOUNDER_ID, 6, 7, 8,
            B_FOUNDER_ID, 11, 12);
    private static final int FOOD_A = 100;
    private static final int FOOD_B = 60;
    private static final int TOOLS = 50;
    /** The reference horizon: forty real France owner turns. */
    private static final int ROUNDS = 40;
    /** France turns the channel stays closed before the human clears it. */
    private static final int BLOCKED_TURNS = 3;
    /** The blocked turn after which the saved game is continued from disk. */
    private static final int CONTINUE_AFTER = 2;

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

    /**
     * Scoped reference, prepared as detached saved data: a mountain-walled
     * basin with open water inside, split by one wall with a single channel
     * square. Rouen and Lyon are founded through the model with their crews on
     * the colony squares, the ships wait in Rouen's port, and the Dutch ship
     * waits south of the channel. The whole map is known to France.
     */
    private static File multiColonyFixture() throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        final Map<Location, TerrainType> terrain = new HashMap<>(
                data.getModel().getMap().getTerrainMap());
        for (int x = 13; x <= 27; x++) {
            for (int y = 1; y <= 17; y++) {
                terrain.put(Location.of(x, y), TerrainType.MOUNTAIN);
            }
        }
        for (int x = 14; x <= 26; x++) {
            for (int y = 1; y <= 16; y++) {
                terrain.put(Location.of(x, y), TerrainType.OCEAN);
            }
        }
        for (int x = 14; x <= 26; x++) {
            terrain.put(Location.of(x, CHANNEL.getY()), TerrainType.MOUNTAIN);
        }
        terrain.put(CHANNEL, TerrainType.OCEAN);
        terrain.put(COLONY_A, TerrainType.GRASSLAND);
        terrain.put(COLONY_B, TerrainType.GRASSLAND);
        for (final Location field : FIELDS_A) {
            terrain.put(field, TerrainType.GRASSLAND);
        }
        for (final Location field : FIELDS_B) {
            terrain.put(field, TerrainType.GRASSLAND);
        }
        for (int x = SITE_BLOCK.getX(); x <= SITE_BLOCK.getX() + 2; x++) {
            for (int y = SITE_BLOCK.getY() - 1; y <= SITE_BLOCK.getY()
                    + 1; y++) {
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
        for (int x = DUTCH_SITE.getX(); x <= DUTCH_SITE.getX() + 1; x++) {
            for (int y = DUTCH_SITE.getY() - 1; y <= DUTCH_SITE.getY()
                    + 1; y++) {
                terrain.put(Location.of(x, y), TerrainType.GRASSLAND);
            }
        }
        data.getModel().getMap().setTerrainType(terrain);
        clearFeatures(data.getModel().getMap());
        final Set<Location> known = new HashSet<>();
        for (int x = 1; x <= 50; x++) {
            for (int y = 1; y <= 30; y++) {
                known.add(Location.of(x, y));
            }
        }
        final PlayerPo rival = data.getModel().getGameManager().getPlayers()
                .stream().filter(player -> player.getName().equals("France"))
                .findFirst().orElseThrow();
        rival.getVisible().setVisibility(known, 50, 30);
        // The human sees the basin too, so the channel and the waiting square
        // can be clicked with the ordinary map controls.
        data.getModel().getGameManager().getPlayers().stream()
                .filter(player -> player.getName().equals("Dutch")).findFirst()
                .orElseThrow().getVisible().setVisibility(known, 50, 30);
        final List<UnitPo> units = new ArrayList<>(data.getModel().getUnits());
        final UnitPo template = units.stream()
                .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                .orElseThrow();
        units.add(extraShip(template, EXPANSION_SHIP_ID, Location.of(20, 2)));
        for (final int id : A_CREW) {
            units.add(colonist(template, id, COLONY_A));
        }
        units.add(colonist(template, B_FOUNDER_ID, COLONY_B));
        for (final int id : B_CREW) {
            units.add(colonist(template, id, COLONY_B));
        }
        data.getModel().setUnits(units);
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            model.startGame();
            final Player france = model.getPlayerByName("France");
            if (model.getCurrentPlayer() != france) {
                model.getCurrentPlayer().endTurn();
            }
            final Unit founderA = model.getUnitById(FOUNDER_ID);
            founderA.placeToLocation(COLONY_A);
            model.buildColony(france, founderA,
                    model.suggestColonyName(Set.of()));
            final Unit founderB = model.getUnitById(B_FOUNDER_ID);
            founderB.placeToLocation(COLONY_B);
            model.buildColony(france, founderB,
                    model.suggestColonyName(colonyNames(model)));
            model.getUnitById(SHIP_ID).placeToLocation(COLONY_A);
            model.getUnitById(EXPANSION_SHIP_ID).placeToLocation(COLONY_A);
            stock(model, COLONY_A_ID, FOOD_A);
            stock(model, COLONY_B_ID, FOOD_B);
            france.endTurn();

            // On the human turn the Dutch settle beside their own ship, then
            // the Dutch ship takes its waiting place south of the channel.
            final Player dutch = model.getPlayerByName("Dutch");
            for (int guard = 0; guard < 4
                    && model.getCurrentPlayer() != dutch; guard++) {
                model.getCurrentPlayer().endTurn();
            }
            if (model.getCurrentPlayer() != dutch) {
                throw new IllegalStateException("The human turn never came");
            }
            final Unit dutchSettler = model.getUnitById(DUTCH_SETTLER_ID);
            dutchSettler.placeToLocation(DUTCH_SITE);
            dutchSettler.startTurn();
            model.buildColony(dutch, dutchSettler,
                    model.suggestColonyName(colonyNames(model)));
            model.getUnitById(DUTCH_SHIP_ID).placeToLocation(WAITING);
            data.setModel(model.save());
        } finally {
            model.stop();
        }
        final Path file = Files.createDirectories(Path.of("target", "fixtures"))
                .resolve("F138-rival-multi-colony.microcol");
        dao.saveToFile(file.toString(), data);
        return file.toFile();
    }

    private static Set<String> colonyNames(final Model model) {
        return model.getColonies().stream().map(Colony::getName)
                .collect(Collectors.toSet());
    }

    /** Corn and the tool target in a colony warehouse, through the model. */
    private static void stock(final Model model, final int colonyId,
            final int corn) {
        final Colony colony = model.getColonyById(colonyId).orElseThrow();
        colony.getWarehouse().addGoods(Goods.of(GoodsType.CORN, corn));
        colony.getWarehouse().addGoods(Goods.of(GoodsType.TOOLS, TOOLS));
    }

    /**
     * Removes trees, rivers, resources, mineral deposits, fields, roads and
     * clearings from the whole synthetic map. Ocean and mountain cannot hold
     * them, and the painted land would otherwise keep features from the
     * original map.
     */
    private static void clearFeatures(final WorldMapPo map) {
        map.setTrees(Set.of());
        map.setRivers(Map.of());
        map.setRiverSegments(List.of());
        map.setResources(Map.of());
        map.setMineralReserves(Map.of());
        map.setFields(Set.of());
        map.setRoads(Set.of());
        map.setCleared(Set.of());
    }

    /** A second French galleon at sea beside Rouen's port. */
    private static UnitPo extraShip(final UnitPo template, final int id,
            final Location at) {
        final UnitPo extra = new UnitPo();
        extra.setId(id);
        extra.setType(UnitType.GALLEON);
        extra.setOwnerId("France");
        extra.setAction(template.getAction());
        extra.setMovementThirds(0);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        extra.setPlaceMap(place);
        final CargoPo cargo = new CargoPo();
        for (int index = 0; index < template.getCargo().getSlots()
                .size(); index++) {
            cargo.getSlots().add(new CargoSlotPo());
        }
        extra.setCargo(cargo);
        return extra;
    }

    /** A French colonist standing on a colony square, not yet working. */
    private static UnitPo colonist(final UnitPo template, final int id,
            final Location at) {
        final UnitPo crew = new UnitPo();
        crew.setId(id);
        crew.setType(UnitType.COLONIST);
        crew.setOwnerId("France");
        crew.setAction(template.getAction());
        crew.setWorkUpdates(0);
        crew.setMissionary(false);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(at);
        place.setOrientation(Direction.west);
        crew.setPlaceMap(place);
        crew.setAvailableMoves(1);
        return crew;
    }

    @Test
    @Tag("local")
    void TC_01_rival_keeps_two_colonies_alive_and_founds_a_third()
            throws Exception {
        loadTarget = multiColonyFixture();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.observeRivalBirths();
        final int start = rival.readRound();
        rival.verifyMultiColonyReady(ESTABLISHED, List.of(COLONY_A, COLONY_B),
                DUTCH_SHIP_ID, WAITING, TOOLS);

        boolean blocked = false;
        boolean recovered = false;
        boolean continued = false;
        int blockedTurns = 0;
        // The model picks the nearest legal coastal square of the southern
        // basin; the scenario reads it from the saved objective.
        Location site = null;
        for (int turn = 0; turn < ROUNDS; turn++) {
            final int next = rival.readRound() + 1;
            rival.nextTurnAcceptingTaxDemands();
            rival.waitForHumanRound(next);
            rival.verifyMultiColonyTurn("Round " + next, ESTABLISHED, CREW,
                    TOOLS);
            if (site == null) {
                site = rival.readRivalExpansionSite();
            }
            if (!blocked && rival.isRivalFounderSailingBefore(FOUNDER_ID,
                    EXPANSION_SHIP_ID, CHANNEL.getY())) {
                // The carrier is under way and still north of the wall: the
                // human closes the channel with the Dutch ship. Panning to the
                // channel first keeps both squares on the screen.
                game.panToTile(CHANNEL);
                game.panToTile(WAITING);
                game.selectTile(WAITING);
                game.dragMouseAtLocation(CHANNEL);
                blocked = true;
            } else if (blocked && !recovered) {
                rival.verifyRivalTransportBlocked("Blocked round " + next,
                        FOUNDER_ID, EXPANSION_SHIP_ID, CHANNEL.getY(),
                        DUTCH_SHIP_ID, CHANNEL);
                blockedTurns++;
                if (!continued && blockedTurns == CONTINUE_AFTER) {
                    // Continue from disk during the block keeps the same
                    // objective, founder and carrier place.
                    final String before = rival.describeRivalExpansion(
                            FOUNDER_ID, EXPANSION_SHIP_ID);
                    game.exitToMenu();
                    game = new FreePlayAuditPage(getContext()).continueGame();
                    rival.observeRivalBirths();
                    rival.waitForHumanRound(next);
                    rival.verifyRivalExpansionUnchanged(before, FOUNDER_ID,
                            EXPANSION_SHIP_ID);
                    continued = true;
                }
                if (blockedTurns == BLOCKED_TURNS) {
                    // The human clears the channel again.
                    game.panToTile(WAITING);
                    game.panToTile(CHANNEL);
                    game.selectTile(CHANNEL);
                    game.dragMouseAtLocation(WAITING);
                    recovered = true;
                }
            }
        }
        rival.verifyRivalThirdColony(COLONY_A_ID, COLONY_B_ID, site, FOUNDER_ID,
                CREW, start, ROUNDS, TOOLS, blocked, recovered, continued);
    }
}
