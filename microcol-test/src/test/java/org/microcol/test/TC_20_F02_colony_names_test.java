package org.microcol.test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.SelectedUnitManager;
import org.microcol.model.Location;
import org.microcol.model.Unit;
import org.microcol.page.ColonyNameDialog;
import org.microcol.page.ColonyScreen;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * Founding and renaming preserve identity and only commit confirmed valid
 * names.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_20_F02_colony_names_test extends AbstractMicroColTest {

    private static final File testFileName = new File(
            "src/test/scenarios/F02-founding-colony.microcol");
    private static final Location FOUNDING_LOCATION = Location.of(19, 11);
    private static final Location DELFT_LOCATION = Location.of(22, 12);

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_cancel_and_escape_leave_founder_and_map_unchanged()
            throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final Unit founder = getContext()
                .getClassFromGuice(SelectedUnitManager.class).getSelectedUnit()
                .orElseThrow();

        // Suggestions and draft edits do not create a colony or consume an ID.
        final ColonyNameDialog dialog = game.foundColony();
        dialog.verifyDraft("Leiden");
        dialog.generate();
        dialog.verifyDraft("Rotterdam");
        dialog.generate();
        dialog.verifyDraft("Utrecht");
        dialog.enterName("Unbuilt town");
        dialog.verifyFoundingUnchanged(founder, FOUNDING_LOCATION, 1, 3, 3);
        dialog.cancel();
        dialog.verifyFoundingUnchanged(founder, FOUNDING_LOCATION, 1, 3, 3);

        // Escape has the same cancellation contract, and suggestions restart.
        final ColonyNameDialog reopened = game.foundColony();
        reopened.verifyDraft("Leiden");
        reopened.escape();
        reopened.verifyFoundingUnchanged(founder, FOUNDING_LOCATION, 1, 3, 3);
    }

    @Test
    @Tag("local")
    void TC_02_found_unicode_name_and_reload_same_identity() throws Exception {
        final File savedFile = new File(System.getProperty("user.home"),
                "colony-name.microcol");
        final FileSelectingService files = getContext()
                .getClassFromGuice(FileSelectingService.class);
        when(files.saveFile(any(File.class), anyString()))
                .thenReturn(savedFile);
        when(files.loadFile(any(File.class))).thenReturn(testFileName,
                savedFile);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final Unit founder = getContext()
                .getClassFromGuice(SelectedUnitManager.class).getSelectedUnit()
                .orElseThrow();

        // The entered Unicode name is trimmed only when confirmed.
        final ColonyNameDialog dialog = game.foundColony();
        dialog.enterName("  Nová Praha 🌊  ");
        dialog.verifyDraft("  Nová Praha 🌊  ");
        dialog.confirmWithEnter();
        dialog.verifyFounded(founder, 3, "Nová Praha 🌊", FOUNDING_LOCATION, 4);
        final ColonyScreen colony = ColonyScreen.of(getContext(),
                "Nová Praha 🌊");
        colony.verifyNumberOfUnitsOnFields(1);

        // Save and reload preserve the name, ID, owner and assigned founder.
        final GamePage afterFounding = colony.close();
        afterFounding.saveGame();
        afterFounding.verifySavedGame(savedFile);
        final ColonyScreen restored = afterFounding.exitToMenu().loadGame()
                .openColonyAt(FOUNDING_LOCATION, "Nová Praha 🌊");
        new ColonyNameDialog(getContext()).verifyFounded(
                getContext().getModel().getUnitById(founder.getId()), 3,
                "Nová Praha 🌊", FOUNDING_LOCATION, 4);
        restored.verifyNumberOfUnitsOnFields(1);
    }

    @Test
    @Tag("local")
    void TC_03_duplicate_name_is_red_and_rename_is_explicit() throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyScreen colony = game.openColonyAt(DELFT_LOCATION, "Delft");

        // A duplicate is visibly rejected without changing the stored name.
        final ColonyNameDialog dialog = colony.rename();
        dialog.enterName(" new AMSTERDAM ");
        dialog.verifyValidation(
                "A colony with this name already exists. Choose another name.",
                true);
        dialog.verifyStoredName(1, "Delft");
        dialog.captureDialog("colonyNameError", "duplicate-name.png");
        dialog.generate();
        dialog.verifyDraft("Leiden");
        dialog.verifyValidation("", false);
        dialog.cancel();
        colony.verifyColonyName("Delft");

        // A valid edited name is saved on the same colony, without extra
        // confirmation.
        final ColonyNameDialog rename = colony.rename();
        rename.enterName("  Přístav svobody  ");
        rename.save();
        rename.verifyStoredName(1, "Přístav svobody");
        colony.verifyColonyName("Přístav svobody");
    }

    @Test
    @Tag("local")
    void TC_04_blank_and_overlong_names_cannot_be_saved() throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final ColonyNameDialog dialog = game.foundColony();

        // Empty and overlong input remain editable but cannot be committed.
        dialog.enterName("   ");
        dialog.verifyValidation("Enter a colony name.", true);
        dialog.enterName("A".repeat(65));
        dialog.verifyValidation("Use at most 64 characters.", true);
        dialog.enterName("A".repeat(64));
        dialog.verifyValidation("", false);
        dialog.save();
        ColonyScreen.of(getContext(), "A".repeat(64)).close();

        // Founding on the occupied tile remains unavailable.
        game.verifyThatBuildColonyButtonIsHidden();
        getContext().getRobot().push(javafx.scene.input.KeyCode.B);
        org.testfx.util.WaitForAsyncUtils.waitForFxEvents();
        game.verifyThatStatusBarContains(
                "Select a colonist with movement remaining");
    }

    @Test
    @Tag("local")
    void TC_05_changed_founder_rejects_stale_confirmation() throws Exception {
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final Unit founder = getContext()
                .getClassFromGuice(SelectedUnitManager.class).getSelectedUnit()
                .orElseThrow();
        final ColonyNameDialog dialog = game.foundColony();

        // Simulate a concurrent move while the naming decision is open.
        getContext().getRobot()
                .interact(() -> founder.placeToLocation(Location.of(18, 11)));
        dialog.save();
        dialog.verifyValidation(
                "The colony or founding unit changed. Nothing was changed. Cancel and try again.",
                false);
        dialog.verifyFoundingUnchanged(founder, Location.of(18, 11), 1, 3, 3);
        dialog.cancel();
    }
}
