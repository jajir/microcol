package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NationalTurnPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import com.google.gson.JsonElement;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_36_F28_national_turn_order_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F28-national-turn-order.microcol");
    private static final List<String> ORDER = List.of("Dutch", "England Crown",
            "France Crown", "Spain Crown", "Dutch's King", "England", "France",
            "Spain");

    @Start
    private void start(final Stage stage) throws Exception {
        Files.deleteIfExists(Path.of(System.getProperty("user.home"),
                ".microcol", "autosave.microcol"));
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_complete_national_round_and_resume_without_preparing_twice()
            throws Exception {
        // Load a shuffled roster at the human's already prepared action
        // boundary.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final NationalTurnPage turns = new NationalTurnPage(getContext());
        audit.verifyDisplayedDate("Year 1508 AD");
        audit.verifyCalendarAndGold(18, 10000);

        // End the human turn, let all present Crowns/nations act, and return
        // once.
        turns.observeTurnOrder();
        game.nextTurnAndCloseDialogs();
        turns.verifyTurnOrder(ORDER);
        audit.verifyDisplayedDate("Year 1509 AD");
        audit.verifyCalendarAndGold(19, 10000);
        final JsonElement prepared = turns.readTurnState();

        // Save the prepared position and Continue twice; no earlier phase
        // repeats.
        game.exitToMenu().verifyMainScreen();
        game = audit.continueGame();
        turns.verifyTurnState(prepared);
        game.exitToMenu().verifyMainScreen();
        game = audit.continueGame();
        turns.verifyTurnState(prepared);
        audit.verifyDisplayedDate("Year 1509 AD");

        // Another complete round follows the same order and advances only once.
        turns.observeTurnOrder();
        game.nextTurnAndCloseDialogs();
        turns.verifyTurnOrder(ORDER);
        audit.verifyDisplayedDate("Year 1510 AD");
        audit.verifyCalendarAndGold(20, 10000);
    }

    @Test
    @Tag("local")
    void TC_02_preparation_blocks_map_commands_and_duplicate_end_turn()
            throws Exception {
        // Select the ship before the human turn finishes.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final NationalTurnPage turns = new NationalTurnPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        game.selectTile(org.microcol.model.Location.of(25, 16));
        game.getRightPanelUnit(0).selectUnit();
        final JsonElement ship = turns.readUnitState(3);
        turns.observeTurnOrder();
        game.verifyThatTileIsVisible(org.microcol.model.Location.of(26, 16));
        turns.pauseBeforeNextHumanFinish();

        // Hold preparation at a real lifecycle notification while the UI stays
        // responsive.
        turns.clickNextTurnWithoutWaiting();
        try {
            turns.verifyPreparationGate();
            audit.press(javafx.scene.input.KeyCode.M);
            turns.clickMapTile(org.microcol.model.Location.of(26, 16));
            turns.clickNextTurnWithoutWaiting();
            turns.verifyUnitAndMoveMode(3, ship, false);
        } finally {
            turns.releasePreparation();
        }

        // Releasing the update returns control after exactly one national
        // round.
        turns.verifyTurnOrder(ORDER);
        audit.verifyCalendarAndGold(19, 10000);
        audit.verifyDisplayedDate("Year 1509 AD");
    }

    @Test
    @Tag("local")
    void TC_03_failed_preparation_keeps_recovery_save_and_menu_continue_works()
            throws Exception {
        // Establish a real recovery file at the completed action boundary.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final NationalTurnPage turns = new NationalTurnPage(getContext());
        game.exitToMenu().verifyMainScreen();
        game = audit.continueGame();
        final byte[] checkpoint = audit.readAutosave();
        final JsonElement before = turns.readTurnState();

        // Inject a backend failure and use the ordinary exit/recovery controls.
        turns.failNextPreparation();
        turns.verifyFailedPreparation();
        turns.clickNextTurnWithoutWaiting();
        turns.exitFailedGame();
        turns.verifyRecoveryMessage(
                "The turn could not finish. No saved game was overwritten. Continue or load a saved game to play again.");
        org.microcol.page.DialogMessagePage.of(getContext()).close();
        WelcomePage.of(getContext()).verifyMainScreen();
        audit.verifyAutosaveUnchanged(checkpoint);

        // Continue loads the unchanged checkpoint; the next real round
        // succeeds.
        game = audit.continueGame();
        turns.verifyTurnState(before);
        turns.observeTurnOrder();
        game.nextTurnAndCloseDialogs();
        turns.verifyTurnOrder(ORDER);
        audit.verifyCalendarAndGold(19, 10000);
    }

}
