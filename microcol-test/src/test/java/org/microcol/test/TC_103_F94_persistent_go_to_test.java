package org.microcol.test;

import java.io.File;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.*;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/**
 * Persistent go-to orders (UC-013, UC-016): map and named destinations over
 * several owner turns, explicit cancel and Escape, the paused state with its
 * reason, and Save/Continue in the middle of a journey. The colonist walks
 * along row 15 between the east end (24, 15) next to New Amsterdam and the west
 * end (20, 15); the fixture prepares the squares and the colonist before
 * loading.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_103_F94_persistent_go_to_test extends AbstractMicroColTest {
    private static final File SOURCE = new File(
            "src/test/scenarios/F52-fortification.microcol");

    private static final int COLONIST = GoToFixture.COLONIST_ID;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_map_order_walks_four_owner_turns_and_arrives() throws Exception {
        GoToFixture.prepare(getContext(), SOURCE, "F94-go-to-map.microcol",
                GoToFixture.EAST, 3);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        GoToPage goTo = new GoToPage(getContext());
        game.panToTile(GoToFixture.WEST);
        // The colonist stands at the east end with a full allowance and no
        // order.
        game.selectTile(GoToFixture.EAST);
        goTo.verifyButton("Go to", true);
        goTo.verifyColonyButton(true, true);
        goTo.verifyNoStatus();
        goTo.verifyMoveButtonShown(true);
        // Choose Go to, then the far square: the order shows its target and
        // spends this turn's allowance on the first square only.
        goTo.startOrder();
        game.selectTile(GoToFixture.WEST);
        goTo.waitUntilUnitIsAt(COLONIST, Location.of(23, 15));
        goTo.verifyDestination(COLONIST, GoToFixture.WEST, false);
        goTo.verifyMovement(COLONIST, 0);
        goTo.verifyStatus("Going to 20, 15");
        goTo.verifyButton("Cancel go-to", true);
        goTo.verifyColonyButton(false, false);
        goTo.verifyMoveButtonShown(false);
        // Inspecting another unit and coming back does not cancel the order.
        game.selectTile(Location.of(25, 15));
        goTo.verifyNoStatus();
        game.selectTile(Location.of(23, 15));
        goTo.verifyDestination(COLONIST, GoToFixture.WEST, false);
        goTo.verifyStatus("Going to 20, 15");
        // Each owner turn walks one more square toward the same target.
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, Location.of(22, 15));
        game.selectTile(Location.of(22, 15));
        goTo.verifyDestination(COLONIST, GoToFixture.WEST, false);
        goTo.verifyStatus("Going to 20, 15");
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, Location.of(21, 15));
        // Arrival ends the order and gives manual control back.
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, GoToFixture.WEST);
        goTo.verifyNoDestination(COLONIST);
        goTo.verifyMovement(COLONIST, 0);
        game.selectTile(Location.of(21, 15));
        game.selectTile(GoToFixture.WEST);
        goTo.verifyNoStatus();
        goTo.verifyButton("Go to", true);
        // The following owner turn leaves the colonist where it arrived.
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, GoToFixture.WEST);
        goTo.verifyMovement(COLONIST, 3);
    }

    @Test
    @Tag("local")
    void TC_02_escape_changes_nothing_and_cancel_keeps_spent_movement()
            throws Exception {
        GoToFixture.prepare(getContext(), SOURCE, "F94-go-to-cancel.microcol",
                GoToFixture.EAST, 3);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        GoToPage goTo = new GoToPage(getContext());
        game.panToTile(GoToFixture.WEST);
        game.selectTile(GoToFixture.EAST);
        goTo.verifyButton("Go to", true);
        // Choosing Go to and pressing Escape leaves the colonist unchanged.
        goTo.startOrder();
        goTo.cancelPickWithEscape();
        goTo.verifyLocation(COLONIST, GoToFixture.EAST);
        goTo.verifyNoDestination(COLONIST);
        goTo.verifyMovement(COLONIST, 3);
        goTo.verifyNoStatus();
        goTo.verifyButton("Go to", true);
        // Choose a destination, then cancel the order by hand.
        goTo.startOrder();
        game.selectTile(GoToFixture.WEST);
        goTo.waitUntilUnitIsAt(COLONIST, Location.of(23, 15));
        goTo.verifyMovement(COLONIST, 0);
        goTo.verifyStatus("Going to 20, 15");
        goTo.cancelOrder();
        // Cancel neither moves the colonist nor gives its movement back.
        goTo.verifyNoDestination(COLONIST);
        goTo.verifyLocation(COLONIST, Location.of(23, 15));
        goTo.verifyMovement(COLONIST, 0);
        goTo.verifyNoStatus();
        goTo.verifyButton("Go to", true);
        // With no order left the next owner turn does not move the colonist.
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, Location.of(23, 15));
        goTo.verifyMovement(COLONIST, 3);
        goTo.verifyNoDestination(COLONIST);
    }

    @Test
    @Tag("local")
    void TC_03_named_colony_is_chosen_from_the_list_and_reached_in_four_turns()
            throws Exception {
        GoToFixture.prepare(getContext(), SOURCE, "F94-go-to-colony.microcol",
                GoToFixture.WEST, 3);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        GoToPage goTo = new GoToPage(getContext());
        game.panToTile(GoToFixture.WEST);
        game.selectTile(GoToFixture.WEST);
        goTo.verifyColonyButton(true, true);
        // The chooser lists the model's friendly colonies; Cancel changes
        // nothing.
        goTo.openColonyChoice();
        goTo.verifyColonyChoices("New Amsterdam");
        goTo.cancelColonyChoice();
        goTo.verifyNoDestination(COLONIST);
        goTo.verifyLocation(COLONIST, GoToFixture.WEST);
        goTo.verifyMovement(COLONIST, 3);
        // Choose the colony: the colonist walks its first square at once.
        goTo.openColonyChoice();
        goTo.chooseColony(0);
        goTo.confirmColonyChoice();
        goTo.waitUntilUnitIsAt(COLONIST, Location.of(21, 15));
        goTo.verifyDestination(COLONIST, GoToFixture.COLONY, false);
        goTo.verifyStatus("Going to New Amsterdam");
        goTo.verifyColonyButton(false, false);
        // The next owner turns continue to the colony and arrive in it.
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, Location.of(22, 15));
        goTo.verifyDestination(COLONIST, GoToFixture.COLONY, false);
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, Location.of(23, 15));
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, GoToFixture.COLONY);
        goTo.verifyNoDestination(COLONIST);
    }

    @Test
    @Tag("local")
    void TC_04_continue_in_the_middle_of_a_journey_keeps_the_order_unmoved()
            throws Exception {
        GoToFixture.prepare(getContext(), SOURCE, "F94-go-to-save.microcol",
                GoToFixture.EAST, 3);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        GoToPage goTo = new GoToPage(getContext());
        game.panToTile(GoToFixture.WEST);
        game.selectTile(GoToFixture.EAST);
        goTo.startOrder();
        game.selectTile(GoToFixture.WEST);
        goTo.waitUntilUnitIsAt(COLONIST, Location.of(23, 15));
        goTo.verifyDestination(COLONIST, GoToFixture.WEST, false);
        // Leaving to the menu saves the game; Continue restores it.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        // Loading neither walked the colonist nor gave back its movement.
        goTo.verifyLocation(COLONIST, Location.of(23, 15));
        goTo.verifyMovement(COLONIST, 0);
        goTo.verifyDestination(COLONIST, GoToFixture.WEST, false);
        game.panToTile(GoToFixture.WEST);
        game.selectTile(Location.of(23, 15));
        goTo.verifyStatus("Going to 20, 15");
        goTo.verifyButton("Cancel go-to", true);
        // The order is still followed on the next owner turn.
        game.nextTurnAndCloseDialogs();
        goTo.verifyLocation(COLONIST, Location.of(22, 15));
        goTo.verifyDestination(COLONIST, GoToFixture.WEST, false);
    }

    @Test
    @Tag("local")
    void TC_05_unreachable_destination_pauses_with_a_reason_and_can_be_canceled()
            throws Exception {
        // The saved order leads to a grass islet in the sea that no land unit
        // can reach, as a game saved earlier in the journey would carry it.
        GoToFixture.prepareOrdered(getContext(), SOURCE,
                "F94-go-to-paused.microcol", GoToFixture.EAST, 3,
                GoToFixture.ISLET, "26, 15", false);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        GoToPage goTo = new GoToPage(getContext());
        game.selectTile(GoToFixture.EAST);
        // Loading did not move the colonist, and its order is active.
        goTo.verifyLocation(COLONIST, GoToFixture.EAST);
        goTo.verifyMovement(COLONIST, 3);
        goTo.verifyDestination(COLONIST, GoToFixture.ISLET, false);
        goTo.verifyStatus("Going to 26, 15");
        goTo.verifyMoveButtonShown(false);
        // Moving by hand is refused with the reason; the order stays.
        game.dragMouseAtLocationWithFeedback(Location.of(23, 15),
                "Going to 26, 15");
        goTo.verifyLocation(COLONIST, GoToFixture.EAST);
        goTo.verifyDestination(COLONIST, GoToFixture.ISLET, false);
        // The next owner turn finds no legal route: the order pauses, keeps its
        // destination and reports why.
        Optional<ScreenTurnReport> report = game.nextTurn();
        goTo.verifyPauseNotice(report, "26, 15");
        goTo.closeReport(report);
        game.selectTile(GoToFixture.EAST);
        goTo.verifyLocation(COLONIST, GoToFixture.EAST);
        goTo.verifyDestination(COLONIST, GoToFixture.ISLET, true);
        goTo.verifyStatus("Paused, no route to 26, 15");
        goTo.verifyButton("Cancel go-to", true);
        // Continue keeps the paused order.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        goTo.verifyDestination(COLONIST, GoToFixture.ISLET, true);
        goTo.verifyLocation(COLONIST, GoToFixture.EAST);
        game.selectTile(GoToFixture.EAST);
        goTo.verifyStatus("Paused, no route to 26, 15");
        // The next owner turn rechecks the route without reporting it again.
        report = game.nextTurn();
        goTo.verifyNoPauseNotice(report);
        goTo.closeReport(report);
        goTo.verifyDestination(COLONIST, GoToFixture.ISLET, true);
        goTo.verifyLocation(COLONIST, GoToFixture.EAST);
        // The player cancels the paused order and takes the colonist back.
        game.selectTile(Location.of(23, 15));
        game.selectTile(GoToFixture.EAST);
        goTo.cancelOrder();
        goTo.verifyNoDestination(COLONIST);
        goTo.verifyNoStatus();
        goTo.verifyButton("Go to", true);
        goTo.verifyMoveButtonShown(true);
    }
}
