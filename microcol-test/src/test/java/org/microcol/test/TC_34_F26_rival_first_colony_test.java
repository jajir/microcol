package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
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

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
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
                Location.of(29, 12), 4, 5);

        // Continue preserves the actual new fleet, not either settled world.
        game.exitToMenu().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        rival.waitForHumanRound(0);
        rival.verifyStartingExpeditions("Anna", Location.of(29, 15),
                Location.of(29, 12), 4, 5);
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
                "Pouze náhled. Hrát budete za Nizozemsko.");
        setup.enterName("Jiří");
        setup.captureDialog("freePlaySetup", "rival-expedition-czech.png");
        setup.startWithEnter();
        final RivalColonyPage rival = new RivalColonyPage(getContext());
        rival.waitForHumanRound(0);
        rival.verifyStartingExpeditions("Jiří", Location.of(29, 15),
                Location.of(29, 12), 4, 5);
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
        rival.verifyRivalTravel(0, Location.of(29, 12), true, null);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        rival.verifyRivalTravel(0, Location.of(29, 12), true, null);
        rival.verifyHumanExpeditionUnchanged(humanFleet, 2108);

        // Two more sea steps permit landing, which spends the founder's point.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(1);
        rival.verifyRivalTravel(1, Location.of(27, 14), false,
                Location.of(26, 15));

        // The third rival turn founds with the original settler at the visible
        // coast.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(2);
        rival.verifyRivalColonyViable(2, 0, Location.of(26, 15));
        game.selectTile(Location.of(26, 15));
        game.verifyThatStatusBarContains("colony New Amsterdam");
        game.captureDialog("nextTurn", "rival-first-colony-map.png");

        // Food sustains that resident; the first-colony objective does not
        // expand again.
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(3);
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(4);
        game.nextTurnAndCloseDialogs();
        rival.waitForHumanRound(5);
        rival.verifyRivalColonyViable(5, 0, Location.of(26, 15));
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
