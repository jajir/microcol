package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Difficulty;
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
    void TC_02_two_islands_and_mock_nation_survive_continue() throws Exception {
        // Map selection is real; the clearly marked nation preview is not.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(1);
        setup.selectNationPreview(1);
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
    void TC_06_czech_setup_explains_preview_and_can_be_cancelled()
            throws Exception {
        // Czech players see the same honest preview and reversible setup.
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(1);
        setup.verifySetupLanguage("Začít hru",
                "Pouze náhled. Hrát budete za Nizozemsko.");
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
        // Select the new reference profile while retaining authored-world
        // choices.
        FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(3);
        setup.verifySelectedMap(3, "Reference expedition (four nations)");
        setup.enterName("  Jan Výprava  ");
        GamePage game = setup.start();
        setup.verifyReferenceExpedition("Jan Výprava", 500);
        setup.verifyGameDifficulty(Difficulty.CONQUISTADOR, "Conquistador");
        java.util.List<Integer> units = setup.referenceUnitIds();
        setup.captureDialog("gameDifficulty", "reference-expedition.png");

        // Continue restores the same participants and cargo rather than
        // creating another expedition.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyReferenceExpedition("Jan Výprava", 500);
        setup.verifyReferenceUnitIds(units);

        // Leaving another reference setup keeps the saved continuation
        // unchanged.
        game.exitToMenu();
        FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        byte[] saved = audit.readAutosave();
        setup = WelcomePage.of(getContext()).openFreePlaySetup();
        setup.selectMap(3);
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
        setup.selectDifficulty(Difficulty.DISCOVERER);
        setup.enterName("Česká výprava");
        setup.captureDialog("freePlaySetup", "reference-setup-czech.png");
        setup.start();
        setup.verifyReferenceExpedition("Česká výprava", 1000);
        setup.verifyGameDifficulty(Difficulty.DISCOVERER, "Objevitel");
    }
}
