package org.akropia.test;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.akropia.gui.screen.game.gamepanel.CursorService;
import org.akropia.mock.CursorServiceNoOpp;
import org.akropia.model.generation.GeneratedClimate;
import org.akropia.model.generation.GeneratedLandShare;
import org.akropia.page.FreePlayAuditPage;
import org.akropia.page.FreePlaySetupPage;
import org.akropia.page.GamePage;
import org.akropia.page.SettingPage;
import org.akropia.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A generated New World is chosen through the ordinary setup, previewed,
 * started and continued from disk. Invalid input and cancel leave no game.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_122_F113_generated_world_test extends AbstractAkropiaTest {

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".akropia", "autosave.akropia"));
        initialize(stage, getClass(), binder -> binder.bind(CursorService.class)
                .toInstance(new CursorServiceNoOpp()));
    }

    @Test
    @Tag("local")
    void TC_01_generated_world_is_previewed_started_and_continued_from_disk()
            throws Exception {
        // Configure the default generated world and inspect both wizard steps.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openGeneratedWorldSetup();
        setup.verifyWizardStep(false);
        setup.verifyWizardChoices();
        setup.selectSize(org.akropia.model.generation.GeneratedMapSize.MEDIUM);
        setup.selectLayout(
                org.akropia.model.generation.GeneratedLandLayout.ARCHIPELAGO);
        setup.enterSeed("Nový svět 2024!");
        setup.captureDialog("freePlaySetup", "wizard-world.png");
        setup.nextStep();
        setup.verifyWizardStep(true);
        setup.verifyPlayerName("");
        setup.verifyPreview("Medium · 120 × 80 · Archipelago");
        setup.enterName("Anna");
        setup.selectNation(org.akropia.model.EuropeanNation.ENGLISH);
        setup.verifyPlayerName("Anna");
        // Returning to the world retains the exact seed and player draft.
        setup.worldStep();
        setup.verifyWizardStep(false);
        setup.verifySeed("Nový svět 2024!");
        setup.nextStep();
        setup.verifyPlayerName("Anna");
        setup.captureDialog("freePlaySetup", "wizard-player.png");
        // Start with the keyboard and load the same saved world through
        // Continue.
        final GamePage game = setup.startWithEnter();
        setup.verifyWizardWorld("Nový svět 2024!",
                org.akropia.model.generation.GeneratedMapSize.MEDIUM,
                org.akropia.model.generation.GeneratedLandLayout.ARCHIPELAGO,
                "Anna");
        setup.verifyGeneratedRiversAvoidCoastAndUplands();
        game.exitToMenu().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyWizardWorld("Nový svět 2024!",
                org.akropia.model.generation.GeneratedMapSize.MEDIUM,
                org.akropia.model.generation.GeneratedLandLayout.ARCHIPELAGO,
                "Anna");
        setup.verifySavedWizardSeed("Nový svět 2024!");
    }

    @Test
    @Tag("local")
    void TC_02_invalid_seed_and_cancel_start_no_generated_game()
            throws Exception {
        // Blank and overlong seeds cannot advance; text seeds and a fresh
        // random seed can.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openGeneratedWorldSetup();
        setup.enterSeed("");
        setup.verifySeedRejected("Enter a seed of 1–64 characters.");
        setup.enterSeed("x".repeat(65));
        setup.verifySeedRejected("Enter a seed of 1–64 characters.");
        setup.enterSeed("x".repeat(64));
        setup.nextStep();
        setup.verifyWizardStep(true);
        setup.worldStep();
        setup.newSeed();
        setup.verifyFreshSeed("x".repeat(64));
        setup.back().verifyMainScreen();
        setup.verifyNoActiveGame();
    }

    @Test
    @Tag("local")
    void TC_03_bundled_setup_hides_generated_controls_and_keeps_name_rules()
            throws Exception {
        // Authored scenarios keep their nation restrictions and independent
        // name validation.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.verifySetup(0);
        setup.verifyGeneratedControlsHidden();
        setup.enterName("");
        setup.verifyNameAccepted(false);
        setup.back().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_04_cancelled_and_invalid_setup_keep_continue_on_the_same_world()
            throws Exception {
        // Generate a single landmass, then abandon an invalid replacement.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openGeneratedWorldSetup();
        setup.selectLayout(
                org.akropia.model.generation.GeneratedLandLayout.ONE_LANDMASS);
        setup.enterSeed("7");
        setup.enterName("Eva");
        final GamePage game = setup.startWithEnter();
        setup.verifyWizardWorld("7",
                org.akropia.model.generation.GeneratedMapSize.SMALL,
                org.akropia.model.generation.GeneratedLandLayout.ONE_LANDMASS,
                "Eva");
        game.exitToMenu().verifyMainScreen();
        WelcomePage.of(getContext()).openGeneratedWorldSetup();
        setup.verifyFreshSeed("7");
        setup.enterSeed("");
        setup.verifySeedRejected("Enter a seed of 1–64 characters.");
        setup.back().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyWizardWorld("7",
                org.akropia.model.generation.GeneratedMapSize.SMALL,
                org.akropia.model.generation.GeneratedLandLayout.ONE_LANDMASS,
                "Eva");
        setup.verifySavedWizardSeed("7");
    }

    @Test
    @Tag("local")
    void TC_05_generated_setup_is_localized_in_czech() throws Exception {
        // The world and player steps use the selected language.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openGeneratedWorldSetup();
        setup.verifySelectedMap(4, "Vygenerované nové pobřeží");
        setup.enterSeed("");
        setup.verifySeedRejected("Zadejte seed o délce 1–64 znaků.");
        setup.enterSeed("svět");
        setup.nextStep();
        setup.verifyPreview("Malá · 90 × 60 · Velké ostrovy");
        setup.captureDialog("freePlaySetup", "wizard-czech.png");
        setup.back().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_06_huge_island_world_starts_and_completes_a_turn()
            throws Exception {
        // The largest selectable map must support real play, including computer
        // turns.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openGeneratedWorldSetup();
        setup.selectSize(org.akropia.model.generation.GeneratedMapSize.HUGE);
        setup.selectLayout(
                org.akropia.model.generation.GeneratedLandLayout.BIG_ISLANDS);
        setup.enterSeed("Huge islands");
        setup.enterName("Alex");
        final GamePage game = setup.startWithEnter();
        setup.verifyWizardWorld("Huge islands",
                org.akropia.model.generation.GeneratedMapSize.HUGE,
                org.akropia.model.generation.GeneratedLandLayout.BIG_ISLANDS,
                "Alex");
        game.nextTurnAndCloseDialogs();
        new FreePlayAuditPage(getContext()).verifyCurrentPlayerIsHuman();
        game.exitToMenu().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyWizardWorld("Huge islands",
                org.akropia.model.generation.GeneratedMapSize.HUGE,
                org.akropia.model.generation.GeneratedLandLayout.BIG_ISLANDS,
                "Alex");
    }
}
