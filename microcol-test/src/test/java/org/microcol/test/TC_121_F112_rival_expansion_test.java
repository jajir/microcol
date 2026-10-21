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
 * Rival second-colony expansion through real French owner turns. France holds
 * one colony at (21,2) with four colonists: the founder and three crew who
 * reach the colony square. Two ships wait in its port. The first ship is the
 * colony's supply carrier, and the second one is free for the expansion. The
 * whole map is known to France, and a coastal grassland site lies on the open
 * sea lane south of the colony. The Dutch hold a colony as well, so the game
 * does not end by the no-colony rule.
 *
 * On the first France turn the crew still stand on the colony square, so the
 * reserves refuse a release; the second ship is held in the port, and the crew
 * take the corn fields. With no trees there is no lumber work, so every
 * resident grows food and the lowest ID, the founder, is the spare released on
 * the next turn. It boards the second ship and sails to the site. Save and
 * Continue midway keeps the same objective and placement. The founder founds
 * exactly one colony within twenty France turns. The source colony keeps the
 * crew minus the founder, with its food reserve. Every original colonist
 * survives, and any other colonist must be a recorded colony birth.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_121_F112_rival_expansion_test extends AbstractMicroColTest {

    private static final Location COLONY = Location.of(21, 2);
    /**
     * Coastal grassland about eleven sea squares south of the colony, so the
     * voyage lasts several France turns and has a midway state to resume.
     */
    private static final Location SITE_BLOCK = Location.of(23, 13);
    /** Land beside the Dutch ship, so the human keeps a colony. */
    private static final Location DUTCH_SITE = Location.of(31, 15);
    /** Land beside the colony: five corn fields, one for each colonist. */
    private static final List<Location> FIELDS = List.of(Location.of(22, 1),
            Location.of(22, 2), Location.of(22, 3), Location.of(21, 3),
            Location.of(20, 3));
    private static final int COLONY_ID = 0;
    private static final int FOUNDER_ID = 4;
    private static final int SHIP_ID = 5;
    private static final int SECOND_SHIP_ID = 9;
    private static final int DUTCH_SETTLER_ID = 0;
    private static final int FIRST_CREW_ID = 6;
    private static final int CREW_COUNT = 3;
    /** Corn in stock: more than five turns of the remaining residents eat. */
    private static final int FOOD = 60;
    /** Crew who reach the colony square: the founder and three colonists. */
    private static final Set<Integer> CREW = Set.of(FOUNDER_ID, 6, 7, 8);
    /** Finite bound for the whole expansion, in France owner turns. */
    private static final int MAX_ROUNDS = 20;

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
     * Scoped reference: France founds its colony at the coast, its colonists
     * stand on the colony square, and both ships wait in its port. The sea
     * around the colony and the site is open, so the expansion needs no
     * exploration. The save records the state before the first France turn.
     */
    private static File expansionFixture() throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        final Map<Location, TerrainType> terrain = new HashMap<>(
                data.getModel().getMap().getTerrainMap());
        for (int x = 14; x <= 26; x++) {
            for (int y = 1; y <= 16; y++) {
                terrain.put(Location.of(x, y), TerrainType.OCEAN);
            }
        }
        terrain.put(COLONY, TerrainType.GRASSLAND);
        // Corn fields beside the colony, so the crew can work it from the
        // start.
        for (final Location field : FIELDS) {
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
        final List<UnitPo> units = new ArrayList<>(data.getModel().getUnits());
        final UnitPo template = units.stream()
                .filter(unit -> unit.getId() == SHIP_ID).findFirst()
                .orElseThrow();
        units.add(extraShip(template, SECOND_SHIP_ID, Location.of(20, 2)));
        for (int id = FIRST_CREW_ID; id < FIRST_CREW_ID + CREW_COUNT; id++) {
            units.add(colonist(template, id));
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
            final Unit settler = model.getUnitById(FOUNDER_ID);
            settler.placeToLocation(COLONY);
            model.buildColony(france, settler,
                    model.suggestColonyName(Set.of()));
            model.getUnitById(SHIP_ID).placeToLocation(COLONY);
            model.getUnitById(SECOND_SHIP_ID).placeToLocation(COLONY);
            model.getColonyById(COLONY_ID).orElseThrow().getWarehouse()
                    .addGoods(Goods.of(GoodsType.CORN, FOOD));
            france.endTurn();

            // On the human turn the Dutch settle beside their own ship.
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
                    model.suggestColonyName(model.getColonies().stream()
                            .map(Colony::getName).collect(Collectors.toSet())));
            data.setModel(model.save());
        } finally {
            model.stop();
        }
        final Path file = Files.createDirectories(Path.of("target", "fixtures"))
                .resolve("F112-rival-expansion.microcol");
        dao.saveToFile(file.toString(), data);
        return file.toFile();
    }

    /**
     * Removes trees, rivers, resources, mineral deposits, fields, roads and
     * clearings from the whole synthetic map. Ocean cannot hold them, and the
     * painted land would otherwise keep features from the original map.
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

    /** A second French galleon at sea beside the colony port. */
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

    /** A French colonist standing on the colony square, not yet working. */
    private static UnitPo colonist(final UnitPo template, final int id) {
        final UnitPo crew = new UnitPo();
        crew.setId(id);
        crew.setType(UnitType.COLONIST);
        crew.setOwnerId("France");
        crew.setAction(template.getAction());
        crew.setWorkUpdates(0);
        crew.setMissionary(false);
        final PlaceMapPo place = new PlaceMapPo();
        place.setLocation(COLONY);
        place.setOrientation(Direction.west);
        crew.setPlaceMap(place);
        crew.setAvailableMoves(1);
        return crew;
    }

    @Test
    @Tag("local")
    void TC_01_rival_expands_to_a_second_colony_and_survives_continue()
            throws Exception {
        loadTarget = expansionFixture();
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.observeRivalBirths();
        final int start = rival.readRound();
        rival.verifyRivalExpansionReady(COLONY_ID, COLONY, SHIP_ID,
                SECOND_SHIP_ID, FOUNDER_ID);

        boolean released = false;
        boolean continuedMidJourney = false;
        // The model picks the nearest legal coastal square; the scenario reads
        // it from the saved objective instead of assuming one square.
        Location site = null;
        for (int turn = 0; turn < MAX_ROUNDS
                && !rival.isRivalColonyFounded(); turn++) {
            final int next = rival.readRound() + 1;
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(next);
            rival.verifyRivalExpansionJourney("Round " + next, COLONY_ID,
                    FOUNDER_ID, SECOND_SHIP_ID, CREW);
            if (site == null) {
                site = rival.readRivalExpansionSite();
            }
            if (!released
                    && rival.isRivalFounderReleased(COLONY_ID, FOUNDER_ID)) {
                // The reserves are checked once, right after the release.
                rival.verifyRivalSourceAfterRelease(COLONY_ID, FOUNDER_ID,
                        CREW);
                released = true;
            }
            if (!continuedMidJourney
                    && rival.isRivalFounderAwayFrom(FOUNDER_ID, COLONY)) {
                // Save and Continue midway keep the same objective and place.
                final String before = rival.describeRivalExpansion(FOUNDER_ID,
                        SECOND_SHIP_ID);
                game.exitToMenu();
                game = new FreePlayAuditPage(getContext()).continueGame();
                rival.observeRivalBirths();
                rival.waitForHumanRound(next);
                rival.verifyRivalExpansionUnchanged(before, FOUNDER_ID,
                        SECOND_SHIP_ID);
                continuedMidJourney = true;
            }
        }
        rival.verifyRivalSecondColony(COLONY_ID, site, FOUNDER_ID, CREW, start,
                continuedMidJourney);
    }
}
