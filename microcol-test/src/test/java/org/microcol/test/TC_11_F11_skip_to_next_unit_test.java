package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.GamePage;
import org.microcol.page.UnitTurnOrdersPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.NationalTurnPage;
import javafx.scene.input.KeyCode;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_11_F11_skip_to_next_unit_test extends AbstractMicroColTest {

    private final static Location COLONY_LOCATION = Location.of(22, 12);

    private final static File testFileName = new File(
            "src/test/scenarios/F11-ship_to_next_unit.microcol");

    @Start
    private void start(final Stage primaryStage) throws Exception {
        initialize(primaryStage, this.getClass(), testFileName);
    }

    @Test
    @Tag("local")
    void TC_01_embark_disembark() throws Exception {
        // open MicroCol and load defined game
        GamePage gamePage = WelcomePage.of(getContext()).loadGame();

        // verify that there in colony are 3 units
        gamePage.verifyNumberOfUnitsAt(COLONY_LOCATION, 3);

        // verify that first unit is selected
        gamePage.verifyThatUnitIsSelected(0);

        gamePage.pressTab();

        // verify that second unit is selected
        gamePage.verifyThatUnitIsSelected(1);

        gamePage.pressTab();

        // verify that third unit is selected
        gamePage.verifyThatUnitIsSelected(2);

        gamePage.pressTab();

        // verify that first unit is selected
        gamePage.verifyThatUnitIsSelected(0);
    }

    @Test
    @Tag("local")
    void TC_02_wait_skip_save_resume_and_restore_movement() throws Exception {
        // Load three eligible units; inspect visible labels, shortcuts and
        // layout.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final UnitTurnOrdersPage orders = new UnitTurnOrdersPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        orders.captureDialog("waitUnit", "unit-turn-orders.png");
        orders.verifyLabelsAndLayout();
        orders.verifyOrdersAvailable(true, true);
        orders.verifySelectedUnit(3);

        // Wait cycles without spending movement, including wraparound.
        orders.waitForAnotherUnit();
        orders.verifySelectedUnit(24);
        orders.waitForAnotherUnit();
        orders.verifySelectedUnit(25);
        audit.press(KeyCode.TAB);
        orders.verifySelectedUnit(3);
        orders.verifyMovement(3, 1, 1, 197);

        // Space skips exactly once even while the Skip button keeps focus.
        orders.skipUnitTurn();
        orders.verifySelectedUnit(24);
        orders.verifyMovement(0, 1, 1, 197);
        audit.press(KeyCode.SPACE);
        orders.verifySelectedUnit(25);
        orders.verifyMovement(0, 0, 1, 197);
        audit.press(KeyCode.SPACE);
        orders.verifySelectedUnit(null);
        orders.verifyOrdersAvailable(false, false);
        audit.press(KeyCode.TAB);
        audit.press(KeyCode.SPACE);
        orders.verifySelectedUnit(null);
        orders.verifyMovement(0, 0, 0, 197);

        // Manual inspection is allowed, but it must not reactivate a spent
        // unit.
        game.getRightPanelUnit(0).selectUnit();
        orders.verifySelectedUnit(3);
        orders.verifyOrdersAvailable(false, false);
        audit.press(KeyCode.SPACE);
        orders.verifyRejectionMessage(
                "This unit cannot skip now. Select an available unit.");
        orders.verifySelectedUnit(null);
        orders.verifyMovement(0, 0, 0, 197);
        game.exitToMenu().verifyMainScreen();
        final byte[] saved = audit.readAutosave();
        game = audit.continueGame();
        orders.verifyMovement(0, 0, 0, 197);
        orders.verifyOrdersAvailable(false, false);
        audit.verifyAutosaveUnchanged(saved);
        game.exitToMenu().verifyMainScreen();
        game = audit.continueGame();
        orders.verifyMovement(0, 0, 0, 197);

        // Only the next normal owner turn restores movement.
        game.nextTurnAndCloseDialogs();
        orders.verifyMovement(6, 1, 1, 198);
        orders.verifyOrdersAvailable(true, true);
    }

    @Test
    @Tag("local")
    void TC_03_cancel_preview_and_preserve_cycle_position() throws Exception {
        // Wait cancels an unconfirmed route without moving the ship.
        WelcomePage.of(getContext()).loadGame();
        final UnitTurnOrdersPage orders = new UnitTurnOrdersPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final NationalTurnPage turns = new NationalTurnPage(getContext());
        final com.google.gson.JsonElement ship = turns.readUnitState(3);
        audit.press(KeyCode.M);
        turns.verifyUnitAndMoveMode(3, ship, true);
        orders.waitForAnotherUnit();
        orders.verifySelectedUnit(24);
        turns.verifyUnitAndMoveMode(3, ship, false);

        // Skip the middle unit; the next unit must precede wrapping to the
        // ship.
        audit.press(KeyCode.M);
        audit.press(KeyCode.SPACE);
        orders.verifySelectedUnit(25);
        orders.verifyMovement(3, 0, 1, 197);
        turns.verifyUnitAndMoveMode(3, ship, false);
        orders.skipUnitTurn();
        orders.verifySelectedUnit(3);

        // Wait with one eligible unit stays on it; Skip then clears selection.
        orders.waitForAnotherUnit();
        orders.verifySelectedUnit(3);
        orders.verifyMovement(3, 0, 0, 197);
        orders.skipUnitTurn();
        orders.verifySelectedUnit(null);
        orders.verifyMovement(0, 0, 0, 197);
    }

    @Test
    @Tag("local")
    void TC_04_preparation_rejects_wait_and_skip_input() throws Exception {
        // Pause a real turn transition while keyboard and toolbar remain
        // visible.
        WelcomePage.of(getContext()).loadGame();
        final UnitTurnOrdersPage orders = new UnitTurnOrdersPage(getContext());
        final FreePlayAuditPage audit = new FreePlayAuditPage(getContext());
        final NationalTurnPage turns = new NationalTurnPage(getContext());
        turns.observeTurnOrder();
        turns.pauseBeforeNextHumanFinish();
        final com.google.gson.JsonElement ship = turns.readUnitState(3);
        turns.clickNextTurnWithoutWaiting();
        try {
            turns.verifyPreparationGate();
            orders.verifyOrdersAvailable(false, false);
            audit.press(KeyCode.TAB);
            audit.press(KeyCode.SPACE);
            orders.waitForAnotherUnit();
            orders.skipUnitTurn();
            orders.verifySelectedUnit(3);
            turns.verifyUnitAndMoveMode(3, ship, false);
        } finally {
            turns.releasePreparation();
        }

        // The completed owner turn restores normal commands exactly once.
        turns.verifyTurnOrder(java.util.List.of("Dutch", "Dutch's King"));
        orders.verifyMovement(6, 1, 1, 198);
        audit.closeWithEscape();
        orders.verifyOrdersAvailable(true, true);
    }

    @Test
    @Tag("local")
    void TC_05_unit_context_keeps_global_controls_and_keyboard_orders()
            throws Exception {
        // Inspect the game-wide toolbar with a ship selected.
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final UnitTurnOrdersPage orders = new UnitTurnOrdersPage(getContext());
        orders.verifyGlobalToolbar();
        orders.verifyMovement(3, 1, 1, 197);

        // Open the same unit menu from the keyboard and cancel without an
        // order.
        orders.openActionsWithKeyboard();
        orders.verifyMenuVisible(true);
        orders.verifyContextCommands(false);
        orders.captureDialog("waitUnit", "r0270-ship-context.png");
        orders.dismissActions();
        orders.verifyMenuVisible(false);
        orders.verifyMovement(3, 1, 1, 197);

        // Arrow and Enter activate Move; Escape cancels the destination
        // preview.
        final NationalTurnPage turns = new NationalTurnPage(getContext());
        final com.google.gson.JsonElement ship = turns.readUnitState(3);
        orders.openActionsWithKeyboard();
        orders.moveWithMenuKeyboard();
        turns.verifyUnitAndMoveMode(3, ship, true);
        orders.dismissActions();
        turns.verifyUnitAndMoveMode(3, ship, false);

        // A contextual Wait activated with Space runs once, without skipping
        // movement.
        orders.openActionsWithRightClick();
        orders.verifyMenuVisible(true);
        orders.waitWithMenuKeyboard();
        orders.verifySelectedUnit(24);
        orders.verifyMenuVisible(false);
        orders.verifyMovement(3, 1, 1, 197);

        // A colonist inside Delft cannot found another colony; selection closes
        // its menu.
        orders.openActionsWithRightClick();
        orders.verifyContextCommands(false);
        orders.captureDialog("waitUnit", "r0270-colonist-context.png");
        game.getRightPanelUnit(2).selectUnit();
        orders.verifySelectedUnit(25);
        orders.verifyMenuVisible(false);
        orders.verifyMovement(3, 1, 1, 197);
        orders.verifyGlobalToolbar();
    }

    @Test
    @Tag("local")
    void TC_06_czech_context_at_small_window_and_empty_selection()
            throws Exception {
        // Open the same game in Czech at the supported small window size.
        final org.microcol.page.SettingPage settings = WelcomePage
                .of(getContext()).openSetting();
        settings.setCzechLanguage();
        final GamePage game = settings.goBack().loadGame();
        final UnitTurnOrdersPage orders = new UnitTurnOrdersPage(getContext());
        orders.resizeWindow(1000, 700);
        orders.openActionsWithKeyboard();
        orders.verifyLocalizedMenu("Akce", "Čekat (Tab)",
                "Vynechat tah jednotky (Mezerník)");
        orders.captureDialog("waitUnit", "r0270-context-cs-1000.png");
        orders.dismissActions();
        orders.verifyMovement(3, 1, 1, 197);

        // The larger layout keeps the same commands, and an empty square has
        // none.
        orders.resizeWindow(1280, 850);
        orders.openActionsWithRightClick();
        orders.verifyLocalizedMenu("Akce", "Čekat (Tab)",
                "Vynechat tah jednotky (Mezerník)");
        orders.dismissActions();
        game.selectTile(Location.of(21, 12));
        orders.verifyNoUnitActions();
        orders.verifyMovement(3, 1, 1, 197);
    }

}
