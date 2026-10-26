package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Difficulty;
import org.microcol.model.EuropeanNation;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.FreePlaySetupPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/** Start both real maps and preserve the existing game when leaving setup. */
@ExtendWith(ApplicationExtension.class)
public class TC_24_F15_free_play_setup_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F15-free-play-audit.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_original_world_and_french_opponent_survive_continue()
            throws Exception {
        // A fresh profile opens setup without completing the campaign.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.verifySetup(0);
        setup.verifyDifficultySelection(Difficulty.CONQUISTADOR,
                "Conquistador");
        setup.verifyNoActiveGame();
        setup.enterName("  Jan Dvořák  ");
        setup.captureDialog("freePlaySetup", "free-play-original.png");
        GamePage game = setup.start();
        setup.verifyWorld(134, 17, "Jan Dvořák");

        // France participates in the turn and retains its ownership after
        // saving.
        game.nextTurnAndCloseDialogs();
        game.exitToMenu().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyWorld(134, 17, "Jan Dvořák");
    }

    @Test
    @Tag("local")
    void TC_02_two_islands_and_fixed_nation_survive_continue()
            throws Exception {
        // Authored maps keep the Dutch human participant.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(1);
        setup.verifyNationSelection(EuropeanNation.DUTCH, false);
        setup.enterName("Éva");
        setup.verifySetup(1);
        setup.captureDialog("freePlaySetup", "free-play-two-islands.png");
        GamePage game = setup.startWithEnter();
        setup.verifyWorld(135, 13, "Éva");

        // Continue must load this selected world, not the original resource.
        game.nextTurnAndCloseDialogs();
        game.exitToMenu().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyWorld(135, 13, "Éva");
    }

    @Test
    @Tag("local")
    void TC_03_back_and_escape_preserve_saved_game_for_both_maps()
            throws Exception {
        // Save an existing position before exploring replacement-game setup.
        WelcomePage.of(getContext()).loadGame().exitToMenu();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        byte[] saved = audit.readAutosave();
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(0);
        setup.selectDifficulty(Difficulty.DISCOVERER);
        setup.back().verifyMainScreen();
        audit.verifyAutosaveUnchanged(saved);

        // Escape from the other selection likewise does not create a world.
        setup = WelcomePage.of(getContext()).openFreePlaySetup();
        setup.selectMap(1);
        setup.selectDifficulty(Difficulty.VICEROY);
        setup.escape().verifyMainScreen();
        setup.verifyNoActiveGame();
        audit.verifyAutosaveUnchanged(saved);
        GamePage game = audit.continueGame();
        setup.verifyGameDifficulty(Difficulty.CONQUISTADOR, "Conquistador");
        game.openEuroperPort().verifyGold(10000);
    }

    @Test
    @Tag("local")
    void TC_04_name_boundaries_prevent_invalid_start() throws Exception {
        // Invalid drafts explain the problem and cannot start the game.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.enterName("");
        setup.verifyNameAccepted(false);
        setup.enterName("Ž".repeat(65));
        setup.verifyNameAccepted(false);
        setup.verifyNoActiveGame();

        // Exactly 64 Unicode characters are accepted and persisted on the
        // player.
        setup.enterName("Ž".repeat(64));
        setup.verifyNameAccepted(true);
        setup.start();
        setup.verifyWorld(134, 17, "Ž".repeat(64));
    }

    @Test
    @Tag("local")
    void TC_05_colony_menu_excludes_french_cities() throws Exception {
        // The new expedition owns no colonies; French settlements are not
        // choices.
        WelcomePage.of(getContext()).startFreeGame();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        audit.press(javafx.scene.input.KeyCode.O);
        audit.verifyColonyMenu("No colonies yet");
        audit.press(javafx.scene.input.KeyCode.ESCAPE);
        GamePage.of(getContext());
        new FreePlaySetupPage(getContext()).verifyWorld(134, 17, "Dutch");
    }

    @Test
    @Tag("local")
    void TC_06_czech_setup_explains_fixed_nation_and_can_be_cancelled()
            throws Exception {
        // Czech players see the fixed nation and reversible setup.
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(1);
        setup.verifySetupLanguage("Začít hru",
                "V tomto scénáři hrajete za Nizozemsko.");
        setup.verifyDifficultySelection(Difficulty.CONQUISTADOR, "Dobyvatel");
        setup.captureDialog("freePlaySetup", "free-play-czech.png");
        setup.escape().verifyMainScreen();
        setup.verifyNoActiveGame();
    }

    @Test
    @Tag("local")
    void TC_07_all_difficulties_persist_through_continue() throws Exception {
        final String[] names = { "Discoverer", "Explorer", "Conquistador",
                "Governor", "Viceroy" };
        for (final Difficulty difficulty : Difficulty.values()) {
            // Choose the level through the actual setup control and start.
            final FreePlaySetupPage setup = WelcomePage.of(getContext())
                    .openFreePlaySetup();
            setup.selectDifficulty(difficulty);
            setup.verifyDifficultySelection(difficulty,
                    names[difficulty.ordinal()]);
            GamePage game = setup.start();
            setup.verifyGameDifficulty(difficulty, names[difficulty.ordinal()]);

            // The menu recovery file stores the same identity, then Continue
            // restores the visible level and the model's rule-facing value.
            game.exitToMenu();
            setup.verifySavedDifficulty(difficulty);
            game = new FreePlayAuditPage(getContext()).continueGame();
            setup.verifyGameDifficulty(difficulty, names[difficulty.ordinal()]);
            game.exitToMenu();
        }
    }

    @Test
    @Tag("local")
    void TC_08_invalid_difficulty_preserves_game_and_save() throws Exception {
        // Establish a valid continuation before choosing an invalid file.
        WelcomePage.of(getContext()).loadGame().exitToMenu();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final byte[] continuation = audit.readAutosave();

        for (final String invalid : new String[] { null, "IMPOSSIBLE" }) {
            // Missing and unknown identities are rejected without guessing.
            audit.prepareInvalidDifficultySave(invalid);
            audit.attemptInvalidSavedGame(false);
            audit.verifyInvalidSavedGameMessage(
                    "The saved game contains invalid data and could not be loaded. The current game and save file were not changed.");
            org.microcol.page.DialogMessagePage.of(getContext()).close();
            audit.verifyRejectedLoadPreservedGameAndFile();
            audit.verifyAutosaveUnchanged(continuation);
            WelcomePage.of(getContext()).verifyMainScreen();
        }
    }

    @Test
    @Tag("local")
    void TC_09_language_change_preserves_selected_difficulty()
            throws Exception {
        // Select a nondefault profile, then change the application language.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectDifficulty(Difficulty.GOVERNOR);
        setup.back();
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();

        // Reopening translates the choice without resetting its identity.
        setup = WelcomePage.of(getContext()).openFreePlaySetup();
        setup.verifyDifficultySelection(Difficulty.GOVERNOR, "Guvernér");
        setup.start();
        setup.verifyGameDifficulty(Difficulty.GOVERNOR, "Guvernér");
        setup.captureDialog("rightPanel", "difficulty-czech-game.png");
    }

    @Test
    @Tag("local")
    void TC_10_reference_expeditions_start_and_survive_continue()
            throws Exception {
        // Each selected nation gets its own expedition and survives Continue.
        FreePlaySetupPage setup = null;
        for (final EuropeanNation nation : EuropeanNation.values()) {
            setup = WelcomePage.of(getContext()).openFreePlaySetup();
            setup.selectMap(3);
            setup.verifySelectedMap(3, "Reference expedition (four nations)");
            setup.selectNation(nation);
            setup.verifyNationSelection(nation, true);
            setup.enterName("  Jan Výprava  ");
            GamePage game = setup.start();
            setup.verifyReferenceExpedition(nation, "Jan Výprava", 500);
            setup.verifyGameDifficulty(Difficulty.CONQUISTADOR, "Conquistador");
            final java.util.List<Integer> units = setup.referenceUnitIds();
            if (nation == EuropeanNation.DUTCH) {
                setup.captureDialog("gameDifficulty",
                        "reference-expedition.png");
            }
            game.exitToMenu();
            game = new FreePlayAuditPage(getContext()).continueGame();
            setup.verifyReferenceExpedition(nation, "Jan Výprava", 500);
            setup.verifyReferenceUnitIds(units);
            game.exitToMenu();
        }

        // Cancelling a selected draft leaves the last recovery save intact.
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        byte[] saved = audit.readAutosave();
        setup = WelcomePage.of(getContext()).openFreePlaySetup();
        setup.selectMap(3);
        setup.selectNation(EuropeanNation.FRENCH);
        setup.verifyNationSelection(EuropeanNation.FRENCH, true);
        setup.selectMap(0);
        setup.verifyNationSelection(EuropeanNation.DUTCH, false);
        setup.selectMap(3);
        setup.verifyNationSelection(EuropeanNation.DUTCH, true);
        setup.selectNation(EuropeanNation.SPANISH);
        setup.back();
        audit.verifyAutosaveUnchanged(saved);
    }

    @Test
    @Tag("local")
    void TC_11_czech_reference_profile_uses_selected_difficulty()
            throws Exception {
        // The reference option has a Czech label and uses the selected treasury
        // profile.
        org.microcol.page.SettingPage settings = WelcomePage.of(getContext())
                .openSetting();
        settings.setCzechLanguage();
        FreePlaySetupPage setup = settings.goBack().openFreePlaySetup();
        setup.selectMap(3);
        setup.verifySelectedMap(3, "Výpravy čtyř národů");
        setup.verifySetupLanguage("Začít hru",
                "Vyberte národ, za který budete hrát.");
        setup.verifyNationSelection(EuropeanNation.DUTCH, true);
        setup.selectDifficulty(Difficulty.DISCOVERER);
        setup.enterName("Česká výprava");
        setup.captureDialog("freePlaySetup", "reference-setup-czech.png");
        setup.start();
        setup.verifyReferenceExpedition(EuropeanNation.DUTCH, "Česká výprava",
                1000);
        setup.verifyGameDifficulty(Difficulty.DISCOVERER, "Objevitel");
    }

    @Test
    @Tag("local")
    void TC_12_original_world_river_landing_and_colony_survive_continue()
            throws Exception {
        // Start the actual authored world; inland features are initially
        // hidden.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(0);
        GamePage game = setup.start();
        org.microcol.page.FogPage fog = new org.microcol.page.FogPage(
                getContext());
        org.microcol.page.FreePlayAuditPage audit = new org.microcol.page.FreePlayAuditPage(
                getContext());
        org.microcol.model.Location mouth = org.microcol.model.Location.of(24,
                13);
        fog.verifyExplored(mouth, false);

        // Sail west along the ordinary sea approach, paying movement each turn.
        game.nextTurnAndCloseDialogs();
        game.selectTile(org.microcol.model.Location.of(45, 15));
        game.getRightPanelUnit(0).selectUnit();
        fog.move(org.microcol.model.Location.of(45, 15),
                org.microcol.model.Location.of(40, 15), 3);
        audit.press(javafx.scene.input.KeyCode.C);
        game.nextTurnAndCloseDialogs();
        game.selectTile(org.microcol.model.Location.of(40, 15));
        game.getRightPanelUnit(0).selectUnit();
        fog.move(org.microcol.model.Location.of(40, 15),
                org.microcol.model.Location.of(35, 15), 3);
        audit.press(javafx.scene.input.KeyCode.C);
        game.nextTurnAndCloseDialogs();
        game.selectTile(org.microcol.model.Location.of(35, 15));
        game.getRightPanelUnit(0).selectUnit();
        fog.move(org.microcol.model.Location.of(35, 15),
                org.microcol.model.Location.of(30, 15), 3);
        audit.press(javafx.scene.input.KeyCode.C);
        game.nextTurnAndCloseDialogs();
        org.microcol.model.Location sea = org.microcol.model.Location.of(26,
                14);
        org.microcol.model.Location land = org.microcol.model.Location.of(25,
                14);
        game.selectTile(org.microcol.model.Location.of(30, 15));
        game.getRightPanelUnit(0).selectUnit();
        fog.move(org.microcol.model.Location.of(30, 15), sea, 3);
        audit.press(javafx.scene.input.KeyCode.C);
        fog.verifyExplored(mouth, true);
        setup.verifyOriginalWorldLandscape();

        // Choose a passenger, land and found a colony using the normal dialog.
        game.nextTurnAndCloseDialogs();
        game.moveMouseAtLocation(sea);
        game.dragMouseAtLocation(land);
        org.microcol.page.PassengerTransferPage passengers = new org.microcol.page.PassengerTransferPage(
                getContext());
        passengers.choose(0);
        passengers.confirm();
        game.waitForNumberOfUnitsAt(land, 1);

        // Walk inland to reveal both river squares through ordinary
        // exploration.
        game.nextTurnAndCloseDialogs();
        game.selectTile(land);
        game.getRightPanelUnit(0).selectUnit();
        final org.microcol.model.Location inland = org.microcol.model.Location
                .of(24, 14);
        game.moveMouseAtLocation(land);
        game.dragMouseAtLocation(inland);
        game.waitForNumberOfUnitsAt(inland, 1);
        fog.verifyExplored(org.microcol.model.Location.of(23, 13), true);
        new org.microcol.page.RiverPage(getContext()).verifyRiverWidth(mouth,
                org.microcol.model.RiverSize.LARGE,
                org.microcol.model.Location.of(23, 13));
        fog.saveMapEvidence("R-0263-river-approach.png");

        // Return to the coastal founding site after inspecting the river.
        game.nextTurnAndCloseDialogs();
        game.selectTile(inland);
        game.getRightPanelUnit(0).selectUnit();
        game.moveMouseAtLocation(inland);
        game.dragMouseAtLocation(land);
        game.waitForNumberOfUnitsAt(land, 1);
        // Landing spends the colonist's movement; found on the next turn.
        game.nextTurnAndCloseDialogs();
        game.selectTile(land);
        game.getRightPanelUnit(0).selectUnit();
        org.microcol.page.ColonyNameDialog naming = game.foundColony();
        naming.enterName("River Haven");
        naming.save();
        org.microcol.page.ColonyScreen.of(getContext(), "River Haven").close();

        // Continue restores both authored features and the player's new colony.
        game.exitToMenu();
        audit.continueGame();
        fog.verifyExplored(mouth, true);
        final org.microcol.page.ColonyScreen colony = game.openColonyAt(land,
                "River Haven");
        colony.verifyColonyName("River Haven");
        colony.close();
        setup.verifyOriginalWorldLandscape();

        // Sail north and inspect the freshwater lake behind its land shore.
        game.nextTurnAndCloseDialogs();
        game.selectTile(sea);
        game.getRightPanelUnit(0).selectUnit();
        final org.microcol.model.Location northernSea = org.microcol.model.Location
                .of(26, 10);
        fog.move(sea, northernSea, 3);
        audit.press(javafx.scene.input.KeyCode.C);
        game.nextTurnAndCloseDialogs();
        game.selectTile(northernSea);
        game.getRightPanelUnit(0).selectUnit();
        final org.microcol.model.Location lakeApproach = org.microcol.model.Location
                .of(21, 8);
        fog.move(northernSea, lakeApproach, 3);
        audit.press(javafx.scene.input.KeyCode.C);
        game.nextTurnAndCloseDialogs();
        game.selectTile(lakeApproach);
        game.getRightPanelUnit(0).selectUnit();
        final org.microcol.model.Location lake = org.microcol.model.Location
                .of(19, 8);
        game.moveMouseAtLocation(lakeApproach);
        game.dragMouseAtLocation(lake);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        org.microcol.page.DialogMessagePage rejection = org.microcol.page.DialogMessagePage
                .of(getContext());
        rejection.verifyDialogTextContains("not possible to move unit here");
        rejection.close();
        audit.press(javafx.scene.input.KeyCode.C);
        setup.verifyShipOnWater(3, lakeApproach,
                org.microcol.model.TerrainType.OCEAN);
        setup.verifyLakeSeparatedFromSea(lake);
        fog.saveMapEvidence("R-0302-enclosed-lake.png");

        // The blocked lake entry and Continue preserve the ship and landscape.
        game.exitToMenu();
        audit.continueGame();
        setup.verifyShipOnWater(3, lakeApproach,
                org.microcol.model.TerrainType.OCEAN);
        setup.verifyLakeSeparatedFromSea(lake);
        setup.verifyOriginalWorldLandscape();
    }
}
