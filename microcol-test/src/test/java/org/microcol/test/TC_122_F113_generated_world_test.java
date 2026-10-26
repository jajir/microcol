package org.microcol.test;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.generation.GeneratedClimate;
import org.microcol.model.generation.GeneratedLandShare;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.FreePlaySetupPage;
import org.microcol.page.GamePage;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A generated New World is chosen through the ordinary setup, previewed,
 * started and continued from disk. Invalid input and cancel leave no game.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_122_F113_generated_world_test extends AbstractMicroColTest {

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), binder -> binder.bind(CursorService.class)
                .toInstance(new CursorServiceNoOpp()));
    }

    @Test
    @Tag("local")
    void TC_01_generated_world_is_previewed_started_and_continued_from_disk()
            throws Exception {
        // Choose the generated world and set every visible parameter.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(4);
        setup.verifyGeneratedSetup(
                "Preview: map 50 × 30 squares, Temperate climate, land 40 %, seed 1.");
        setup.selectClimate(GeneratedClimate.COLD);
        setup.selectLandShare(GeneratedLandShare.HIGH);
        setup.enterSeed("2024");
        setup.verifyPreview(
                "Preview: map 50 × 30 squares, Cold climate, land 55 %, seed 2024.");
        setup.enterName("Anna");
        setup.captureDialog("freePlaySetup", "generated-world-setup.png");

        // Start with the keyboard; the new world becomes the active game.
        final GamePage game = setup.startWithEnter();
        setup.verifyGeneratedWorld(2024, GeneratedClimate.COLD,
                GeneratedLandShare.HIGH, "Anna");
        setup.verifyGeneratedRiversAvoidCoastAndUplands();

        // Continue loads the saved world; it is not generated again.
        game.exitToMenu().verifyMainScreen();
        new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyGeneratedWorld(2024, GeneratedClimate.COLD,
                GeneratedLandShare.HIGH, "Anna");
        setup.verifyGeneratedRiversAvoidCoastAndUplands();
        setup.verifySavedGeneratedWorld(2024);
    }

    @Test
    @Tag("local")
    void TC_02_invalid_seed_and_cancel_start_no_generated_game()
            throws Exception {
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(4);
        setup.enterSeed("1000000000");
        setup.verifySeedRejected("Enter a whole seed from 0 to 999999999.");
        setup.enterSeed("12x");
        setup.verifySeedRejected("Enter a whole seed from 0 to 999999999.");
        setup.enterSeed("42");
        setup.verifyPreview(
                "Preview: map 50 × 30 squares, Temperate climate, land 40 %, seed 42.");

        // Back cancels the setup; no generated game becomes active.
        setup.back().verifyMainScreen();
        setup.verifyNoActiveGame();
    }

    @Test
    @Tag("local")
    void TC_04_cancelled_and_invalid_setup_keep_continue_on_the_same_world()
            throws Exception {
        // Start a generated world, then leave it through the menu.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(4);
        setup.selectClimate(GeneratedClimate.WARM);
        setup.selectLandShare(GeneratedLandShare.LOW);
        setup.enterSeed("7");
        setup.enterName("Eva");
        final GamePage game = setup.startWithEnter();
        setup.verifyGeneratedWorld(7, GeneratedClimate.WARM,
                GeneratedLandShare.LOW, "Eva");
        setup.verifyGeneratedRiversAvoidCoastAndUplands();
        game.exitToMenu().verifyMainScreen();

        // A rejected seed and Back start nothing and replace nothing.
        WelcomePage.of(getContext()).openFreePlaySetup();
        setup.selectMap(4);
        setup.enterSeed("99999999999");
        setup.verifySeedRejected("Enter a whole seed from 0 to 999999999.");
        setup.back().verifyMainScreen();

        // Continue still loads the world that was saved earlier.
        new FreePlayAuditPage(getContext()).continueGame();
        setup.verifyGeneratedWorld(7, GeneratedClimate.WARM,
                GeneratedLandShare.LOW, "Eva");
        setup.verifyGeneratedRiversAvoidCoastAndUplands();
        setup.verifySavedGeneratedWorld(7);
    }

    @Test
    @Tag("local")
    void TC_05_generated_setup_is_localized_in_czech() throws Exception {
        // Select the generated world, cancel and switch the language.
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(4);
        setup.back().verifyMainScreen();
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();

        // The choice, the preview and the seed error follow the language.
        WelcomePage.of(getContext()).openFreePlaySetup();
        setup.verifySelectedMap(4, "Vygenerovaný nový svět");
        setup.verifyPreview(
                "Náhled: mapa 50 × 30 polí, podnebí Mírné, pevnina 40 %, semínko 1.");
        setup.enterSeed("x");
        setup.verifySeedRejected("Zadejte celé semínko od 0 do 999999999.");
        setup.captureDialog("freePlaySetup", "generated-world-czech.png");
        setup.back().verifyMainScreen();
    }

    @Test
    @Tag("local")
    void TC_03_bundled_setup_hides_generated_controls_and_keeps_name_rules()
            throws Exception {
        final FreePlaySetupPage setup = WelcomePage.of(getContext())
                .openFreePlaySetup();
        setup.selectMap(0);
        setup.verifySetup(0);
        setup.enterName("");
        setup.verifyNameAccepted(false);
        setup.back().verifyMainScreen();
    }

}
