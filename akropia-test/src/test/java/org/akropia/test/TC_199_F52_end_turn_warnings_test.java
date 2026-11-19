package org.akropia.test;

import java.io.File;
import java.nio.file.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.akropia.model.Location;
import org.akropia.page.*;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import javafx.stage.Stage;

/** R-0398, UC-005/038: review idle colonists before ending a turn. */
@ExtendWith(ApplicationExtension.class)
public class TC_199_F52_end_turn_warnings_test extends AbstractAkropiaTest {
    private static final File SOURCE = new File(
            "src/test/scenarios/F52-fortification.akropia");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), SOURCE);
    }

    @Test
    @Tag("local")
    void TC_01_back_escape_and_open_colony_do_not_end_turn() throws Exception {
        // A colonist with available movement waits outside the buildings.
        prepareWarning("F52-idle-warning.akropia", GoToFixture.COLONY, 3);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final String before = warning.snapshot();
        // Back and Escape retain the entire model and leave commands usable.
        warning.requestTurn();
        warning.verifyWarning(0, "New Amsterdam (1)");
        warning.back();
        warning.verifyUnchanged(before);
        warning.verifyClosed();
        warning.requestTurn();
        warning.escape();
        warning.verifyUnchanged(before);
        warning.verifyClosed();
        // The colony link navigates without assigning work or advancing time.
        warning.requestTurn();
        warning.openColony(0);
        final ColonyScreen colony = ColonyScreen.of(getContext(),
                "New Amsterdam");
        warning.verifyUnchanged(before);
        colony.close();
        warning.verifyClosed();
    }

    @Test
    @Tag("local")
    void TC_02_continue_is_for_one_turn_and_reload_recomputes_warning()
            throws Exception {
        prepareWarning("F52-idle-repeat.akropia", GoToFixture.COLONY, 3);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();
        // The common journey explicitly chooses End turn anyway.
        game.nextTurnAndCloseDialogs();
        warning.verifyTurn(before + 1);
        warning.requestTurn();
        warning.verifyWarning(0, "New Amsterdam (1)");
        warning.back();
        // Continue reloads the position; no saved acknowledgement suppresses
        // it.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        warning.requestTurn();
        warning.verifyWarning(0, "New Amsterdam (1)");
        warning.back();
        warning.verifyTurn(before + 1);
    }

    @Test
    @Tag("local")
    void TC_03_workers_and_passengers_do_not_block_turn() throws Exception {
        // Workers and a passenger retain movement; map units are exhausted.
        prepareWarning("F52-idle-workers.akropia", GoToFixture.EAST, 0);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();
        warning.requestTurn();
        warning.verifyTurnAdvancedWithoutWarning(before);
        warning.verifyClosed();
    }

    @Test
    @Tag("local")
    void TC_04_exhausted_movement_does_not_warn() throws Exception {
        // Load an idle colonist who cannot afford any neighboring square.
        prepareWarning("F52-idle-immobile-0.akropia", GoToFixture.COLONY, 0);
        WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();

        // Request the turn directly: do not automatically accept a warning.
        warning.requestTurn();
        warning.verifyTurnAdvancedWithoutWarning(before);
    }

    @Test
    @Tag("local")
    void TC_05_one_third_movement_does_not_warn() throws Exception {
        // Load an idle colonist who cannot afford any neighboring square.
        prepareWarning("F52-idle-immobile-1.akropia", GoToFixture.COLONY, 1);
        WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();

        // Request the turn directly: do not automatically accept a warning.
        warning.requestTurn();
        warning.verifyTurnAdvancedWithoutWarning(before);
    }

    @Test
    @Tag("local")
    void TC_06_two_thirds_movement_does_not_warn() throws Exception {
        // Load an idle colonist who cannot afford any neighboring square.
        prepareWarning("F52-idle-immobile-2.akropia", GoToFixture.COLONY, 2);
        WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();

        // Request the turn directly: do not automatically accept a warning.
        warning.requestTurn();
        warning.verifyTurnAdvancedWithoutWarning(before);
    }

    @Test
    @Tag("local")
    void TC_07_forest_unit_can_be_selected_without_ending_turn()
            throws Exception {
        // The colonist waits in the forest next to the colony with full
        // movement.
        prepareWarning("F52-idle-forest.akropia", GoToFixture.EAST, 3);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.selectMapUnit(3);
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final String before = warning.snapshot();

        // Back and Escape preserve the map warning and the complete game state.
        warning.requestTurn();
        warning.verifyUnitWarning(GoToFixture.COLONIST_ID,
                "Free Colonist (24, 15)");
        warning.back();
        warning.verifyUnchanged(before);
        warning.requestTurn();
        warning.escape();
        warning.verifyUnchanged(before);

        // Selecting the row returns to that map unit without spending its turn.
        warning.requestTurn();
        warning.selectUnit(GoToFixture.COLONIST_ID);
        warning.verifySelectedUnit(GoToFixture.COLONIST_ID, GoToFixture.EAST);
        warning.verifyFocusedWithoutGameplayChange(before, GoToFixture.EAST);
        warning.verifyClosed();
    }

    @Test
    @Tag("local")
    void TC_08_map_warning_repeats_after_continue_and_reload()
            throws Exception {
        // The original fixture has an idle forest colonist and a movable ship.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final EndTurnWarningsPage warning = new EndTurnWarningsPage(
                getContext());
        final int before = warning.turn();
        warning.requestTurn();
        warning.verifyUnitWarning(3, "Galleon (25, 15)");
        warning.verifyUnitWarning(GoToFixture.COLONIST_ID,
                "Free Colonist (24, 15)");
        warning.back();

        // Confirmation covers one turn only; reloading does not suppress
        // review.
        game.nextTurnAndCloseDialogs();
        warning.verifyTurn(before + 1);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        warning.requestTurn();
        warning.verifyUnitWarning(GoToFixture.COLONIST_ID,
                "Free Colonist (24, 15)");
        warning.back();
        warning.verifyTurn(before + 1);
    }

    private void prepareWarning(final String name, final Location start,
            final int movementThirds) throws Exception {
        // Isolate the subject: the other map units cannot move in this turn.
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(SOURCE.toPath()), JsonObject.class);
        saved.getAsJsonObject("model").getAsJsonArray("units")
                .forEach(value -> {
                    final JsonObject unit = value.getAsJsonObject();
                    if (unit.has("placeMap")) {
                        unit.addProperty("movementThirds", 0);
                    }
                });
        final File source = new File(System.getProperty("user.home"),
                "F52-idle-source.akropia");
        Files.writeString(source.toPath(), saved.toString());
        GoToFixture.prepare(getContext(), source, name, start, movementThirds);
    }

}
