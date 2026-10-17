package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
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
import org.microcol.model.Player;
import org.microcol.model.TerrainType;
import org.microcol.model.Unit;
import org.microcol.model.UnitType;
import org.microcol.model.campaign.MissionImpl;
import org.microcol.model.campaign.po.GameModelPo;
import org.microcol.model.campaign.po.GameModelPoDao;
import org.microcol.model.store.PlaceMapPo;
import org.microcol.model.store.UnitPo;
import org.mockito.Mockito;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.FreePlaySetupPage;
import org.microcol.page.GamePage;
import org.microcol.page.RivalColonyPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** The expedition is selected through the ordinary new-game setup. */
@ExtendWith(ApplicationExtension.class)
public class TC_34_F26_rival_first_colony_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F26-first-colony-boundaries.microcol");

    private static final Location SITE = Location.of(21, 2);

    /** The saved game the Load command opens; one scenario swaps it. */
    private volatile File loadTarget = FIXTURE;

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
     * Scoped reference: the expedition's own founder founds the colony through
     * the model at the usual site, two ordinary colonists stand beside it and
     * the surrounding land is grassland. No stock is added.
     */
    private static File spareWorkerFixture() throws Exception {
        final GameModelPoDao dao = new GameModelPoDao();
        final GameModelPo data = dao
                .loadFromClassPath("/maps/free-play-rival-expedition.microcol");
        final UnitPo founder = data.getModel().getUnits().stream()
                .filter(unit -> unit.getId() == 4).findFirst().orElseThrow();
        founder.setAvailableMoves(1);
        final List<UnitPo> units = new ArrayList<>(data.getModel().getUnits());
        for (int id = 6; id <= 7; id++) {
            final UnitPo spare = new UnitPo();
            spare.setId(id);
            spare.setType(UnitType.COLONIST);
            spare.setOwnerId("France");
            spare.setAction(founder.getAction());
            spare.setWorkUpdates(0);
            final PlaceMapPo place = new PlaceMapPo();
            place.setLocation(SITE);
            place.setOrientation(Direction.west);
            spare.setPlaceMap(place);
            spare.setAvailableMoves(1);
            units.add(spare);
        }
        data.getModel().setUnits(units);
        final Map<Location, TerrainType> terrain = data.getModel().getMap()
                .getTerrainMap();
        SITE.getNeighbors().stream().filter(
                at -> terrain.containsKey(at) && terrain.get(at).isLand())
                .forEach(at -> terrain.put(at, TerrainType.GRASSLAND));
        data.getModel().getMap().setTerrainType(terrain);
        data.getModel().getMap().setTrees(Collections.emptySet());

        // France founds with the ordinary model operation, then yields.
        final Model model = Model.make(data.getModel(),
                MissionImpl.getPredefinedGameOverEvaluators());
        try {
            model.startGame();
            final Player france = model.getPlayerByName("France");
            if (model.getCurrentPlayer() != france) {
                model.getCurrentPlayer().endTurn();
            }
            final Unit settler = model.getUnitById(4);
            settler.placeToLocation(SITE);
            model.buildColony(france, settler,
                    model.suggestColonyName(Set.of()));
            france.endTurn();
            data.setModel(model.save());
        } finally {
            model.stop();
        }
        final Path file = Files.createDirectories(Path.of("target", "fixtures"))
                .resolve("F26-spare-workers.microcol");
        dao.saveToFile(file.toString(), data);
        return file.toFile();
    }

    @Test
    @Tag("local")
    void TC_01_select_expedition_and_continue_before_first_turn()
            throws Exception {
        // Choose the new world through the keyboard-operated map selector.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(2);
        setup.verifySetup(2);
        setup.verifySelectedMap(2, "Rival expedition");
        setup.enterName("Anna");
        setup.captureDialog("freePlaySetup", "rival-expedition-setup.png");
        final GamePage game = setup.startWithEnter();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.waitForHumanRound(0);
        rival.verifyStartingExpeditions("Anna", Location.of(29, 15),
                Location.of(29, 4), 4, 5, false);

        // Continue preserves the actual new fleet, not either settled world.
        game.exitToMenu().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        rival.waitForHumanRound(0);
        rival.verifyStartingExpeditions("Anna", Location.of(29, 15),
                Location.of(29, 4), 4, 5, false);
    }

    @Test
    @Tag("local")
    void TC_02_expedition_selection_survives_czech_language_refresh()
            throws Exception {
        // Select the expedition, cancel setup and change the UI language.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(2);
        setup.back().verifyMainScreen();
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();

        // Reopening keeps the third choice and starts its real world.
        WelcomePage.of(getContext()).openFreePlaySetup();
        setup.verifySelectedMap(2, "Soupeřova výprava");
        setup.verifySetupLanguage("Začít hru",
                "V tomto scénáři hrajete za Nizozemsko.");
        setup.enterName("Jiří");
        setup.captureDialog("freePlaySetup", "rival-expedition-czech.png");
        setup.startWithEnter();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.waitForHumanRound(0);
        rival.verifyStartingExpeditions("Jiří", Location.of(29, 15),
                Location.of(29, 4), 4, 5, false);
    }

    @Test
    @Tag("local")
    void TC_03_found_at_map_edge_and_continue_with_the_same_resident()
            throws Exception {
        // An ordinary colonist may settle legal land at the map edge.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.selectTile(Location.of(1, 15));
        game.getRightPanelUnit(0).selectUnit();
        final org.microcol.page.ColonyNameDialog name = game.foundColony();
        name.enterName("Edge harbour");
        name.confirmWithEnter();
        final org.microcol.page.ColonyScreen colony = org.microcol.page.ColonyScreen
                .of(getContext(), "Edge harbour");
        final RivalColonyPage page = new RivalColonyPage(getContext());
        page.verifyFoundedResident(0, 0, "Edge harbour", Location.of(1, 15),
                true);
        colony.captureDialog("colonyName", "founding-map-edge.png");

        // Off-map work is refused; a valid field still accepts the same drag.
        colony.verifyFieldWorker(0, Location.of(1, 14), 4);
        colony.dragFieldWorkerTo(0, org.microcol.model.Direction.west);
        colony.verifyFieldWorker(0, Location.of(1, 14), 4);
        colony.dragFieldWorkerTo(0, org.microcol.model.Direction.east);
        colony.verifyFieldWorker(0, Location.of(2, 15), 4);

        // Saving and Continue preserve the colony and its original founder.
        colony.close().exitToMenu();
        new FreePlayAuditPage(getContext()).continueGame();
        page.verifyFoundedResident(0, 0, "Edge harbour", Location.of(1, 15),
                true);
    }

    @Test
    @Tag("local")
    void TC_04_mountain_is_unavailable_and_keyboard_explains_rejection()
            throws Exception {
        // The shared rule hides founding on a mountain before any dialog.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.selectTile(Location.of(4, 15));
        game.getRightPanelUnit(0).selectUnit();
        game.verifyThatBuildColonyButtonIsHidden();

        // The keyboard command also rejects with a terrain explanation.
        new FreePlayAuditPage(getContext()).press(javafx.scene.input.KeyCode.B);
        game.verifyThatStatusBarContains("outside mountains");
        new RivalColonyPage(getContext()).verifyFoundingRejected(1,
                Location.of(4, 15), 0, 0);
    }

    @Test
    @Tag("local")
    void TC_05_unproductive_land_uses_an_indoor_workplace() throws Exception {
        // Food quality guides the rival, but does not ban a human settlement.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.selectTile(Location.of(8, 15));
        game.getRightPanelUnit(0).selectUnit();
        final org.microcol.page.ColonyNameDialog name = game.foundColony();
        name.enterName("Arctic station");
        name.confirmWithEnter();
        final org.microcol.page.ColonyScreen colony = org.microcol.page.ColonyScreen
                .of(getContext(), "Arctic station");
        new RivalColonyPage(getContext()).verifyFoundedResident(0, 2,
                "Arctic station", Location.of(8, 15), false);
        colony.captureDialog("colonyName", "founding-unproductive-land.png");
    }

    @Test
    @Tag("local")
    void TC_06_rival_explores_lands_and_founds_after_continue_during_travel()
            throws Exception {
        // Start the ordinary expedition world, keeping the human fleet
        // untouched.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(2);
        setup.enterName("Anna");
        GamePage game = setup.startWithEnter();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.waitForHumanRound(0);
        final String humanFleet = rival.captureHumanExpedition();

        // France acts before the first Dutch turn; Continue preserves that
        // journey.
        rival.verifyRivalTravel(0, Location.of(29, 4), true, null);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.verifyRivalTravel(0, Location.of(29, 4), true, null);
        rival.verifyHumanExpeditionUnchanged(humanFleet, 2108);

        // The rival sails toward a northern shore beyond Dutch sight.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(1);
        rival.verifyRivalTravel(1, Location.of(23, 4), true, null);

        // Landing spends the founder's point; founding follows next turn.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(2);
        rival.verifyRivalTravel(2, Location.of(21, 3), false,
                Location.of(21, 2));
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(3);
        rival.verifyRivalColonyViable(3, 0, Location.of(21, 2));
        rival.verifyRivalColonyUndiscovered(Location.of(21, 2));
        game.panToTile(Location.of(21, 2));
        game.captureDialog("nextTurn",
                "rival-first-colony-before-selection.png");
        game.selectTile(Location.of(21, 2));
        game.verifyThatStatusBarContains("This area is still hidden");
        game.captureDialog("nextTurn", "rival-first-colony-map.png");

        // Food sustains that resident; the first-colony objective does not
        // expand again.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(4);
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(5);
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(6);
        rival.verifyRivalColonyViable(6, 0, Location.of(21, 2));
        rival.verifyRivalColonyUndiscovered(Location.of(21, 2));
        rival.verifyHumanExpeditionUnchanged(humanFleet, 2108);
    }

    @Test
    @Tag("local")
    void TC_08_rival_colony_keeps_producing_food_for_ten_turns_after_founding()
            throws Exception {
        // Start the ordinary expedition and let France found its colony.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(2);
        setup.enterName("Anna");
        GamePage game = setup.startWithEnter();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.waitForHumanRound(0);
        for (int round = 1; round <= 3; round++) {
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(round);
        }
        rival.verifyRivalColonyViable(3, 0, Location.of(21, 2));
        final String humanFleet = rival.captureHumanExpedition();
        final int foodAtFounding = rival.readRivalFood(0);

        // Five turns later the settlement is still fed by its one resident.
        for (int round = 4; round <= 8; round++) {
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(round);
        }
        rival.verifyRivalColonyKeepsProducing(8, 0, Location.of(21, 2),
                foodAtFounding);
        final int foodMidway = rival.readRivalFood(0);

        // Continue while the settlement is active keeps its stock and workers.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.verifyRivalColonyKeepsProducing(8, 0, Location.of(21, 2),
                foodMidway);

        // Five more turns return control each time and keep producing.
        for (int round = 9; round <= 13; round++) {
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(round);
        }
        rival.verifyRivalColonyKeepsProducing(13, 0, Location.of(21, 2),
                foodMidway);
        rival.verifyHumanExpeditionUnchanged(humanFleet, 2108);
    }

    @Test
    @Tag("local")
    void TC_09_spare_workers_make_food_and_lumber_for_ten_turns_after_founding()
            throws Exception {
        // Open the scoped save: France's colony has its founder and two idle
        // colonists.
        loadTarget = spareWorkerFixture();
        final GamePage game0 = WelcomePage.of(getContext()).loadGame();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        final int start = rival.readRound();
        rival.verifyScopedColonyWithIdleWorkers(0, SITE, 4, 6, 2);
        final String humanFleet = rival.captureHumanExpedition();

        // Each real France turn returns control; its workers feed and cut.
        GamePage game = game0;
        for (int turn = 1; turn <= 5; turn++) {
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(start + turn);
        }
        rival.verifyRivalWorkforceProducing(start + 5, 0, 4, 3, 1, 1);
        final int foodMidway = rival.readRivalFood(0);
        final int lumberMidway = rival.readRivalLumber(0);

        // Continue while the settlement is active keeps workers and stocks.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.verifyRivalWorkforceProducing(start + 5, 0, 4, 3, foodMidway,
                lumberMidway);

        // Five more turns keep growing both stocks, and the human is untouched.
        for (int turn = 6; turn <= 10; turn++) {
            game.nextTurnAndCloseDialogs();
            rival.waitForHumanRound(start + turn);
        }
        rival.verifyRivalWorkforceProducing(start + 10, 0, 4, 3, foodMidway,
                lumberMidway + 1);
        rival.verifyHumanExpeditionUnchanged(humanFleet, 2108);
    }

    @Test
    @Tag("local")
    void TC_07_enclosed_rival_returns_control_without_a_colony()
            throws Exception {
        // The boundary fixture encloses the rival ship with impassable
        // mountains.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        final String humanFleet = rival.captureHumanExpedition();
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(1);
        rival.verifyRivalTravel(1, Location.of(35, 10), true, null);
        rival.verifyHumanExpeditionUnchanged(humanFleet, 2108);

        // A later retry is also bounded and the ordinary next-turn control
        // remains usable.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(2);
        rival.verifyRivalTravel(2, Location.of(35, 10), true, null);
        rival.verifyHumanExpeditionUnchanged(humanFleet, 2108);
    }

}
