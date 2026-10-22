package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Location;
import org.microcol.model.Model;
import org.microcol.model.ShipPatrolRules;
import org.microcol.model.ShipPatrolRules.Outcome;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.NavalInterceptionPage;
import org.microcol.page.ScreenTurnReport;
import org.microcol.page.SettingPage;
import org.microcol.page.WelcomePage;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import javafx.stage.Stage;

/**
 * A Dutch frigate passes a French privateer that is at peace with it. The saved
 * random state is chosen in the prepared save file, so the first step beside
 * the privateer slows the frigate, a step away from it draws nothing, and the
 * next step beside it stops the frigate. The unarmed French galleon beside the
 * route never intercepts. Continue keeps the stop and the stream without
 * redrawing.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_141_F132_naval_interception_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F55-naval-combat.microcol");

    private static final Location START = Location.of(20, 10);

    /** Beside the privateer; the first check slows the frigate. */
    private static final Location SLOWED_STEP = Location.of(20, 9);

    /**
     * Open water two squares from the privateer, the square the frigate started
     * from; no check and no report. The squares west of it are land.
     */
    private static final Location SAFE_STEP = Location.of(20, 10);

    /** Beside the privateer again; the second check stops the frigate. */
    private static final Location STOPPED_STEP = Location.of(19, 9);

    private static final Location PATROL = Location.of(20, 8);

    private static final int PATROL_ID = 10;

    /** Full allowance of the frigate in the fixture, in thirds of a point. */
    private static final int FULL_MOVEMENT = 18;

    /** One ordinary sea step, in thirds of a point. */
    private static final int POINT = 3;

    private static final long GAMMA = 0x9E3779B97F4A7C15L;

    private static final String SLOWED_EN = "A hostile patrol slowed your ship: it has less movement left this turn.";

    private static final String STOPPED_EN = "A hostile patrol stopped your ship for the rest of this turn.";

    private static final String SLOWED_CS = "Nepřátelská hlídka zpomalila vaši loď";

    /** The saved game the Load command opens; each scenario picks its own. */
    private volatile File loadTarget;

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), binder -> {
            binder.bind(CursorService.class)
                    .toInstance(new CursorServiceNoOpp());
            final FileSelectingService selecting = Mockito
                    .mock(FileSelectingService.class);
            Mockito.when(selecting.loadFile(Mockito.any(File.class)))
                    .thenAnswer(invocation -> loadTarget);
            binder.bind(FileSelectingService.class).toInstance(selecting);
        });
    }

    /** Select the frigate that stands alone on the square. */
    private void selectShipAt(final GamePage game, final Location at) {
        game.selectTile(at);
        game.getRightPanelUnit(0).selectUnit();
        game.moveMouseAtLocation(at);
    }

    @Test
    @Tag("local")
    void TC_01_slowdown_safe_step_and_stop_persist_through_continue()
            throws Exception {
        final long state = slowThenStopState();
        loadTarget = prepare(state);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final NavalInterceptionPage naval = new NavalInterceptionPage(
                getContext());
        game.panToTile(START);
        naval.verifyMovementThirds(FULL_MOVEMENT);
        naval.verifyCombatRandomState(state);

        // The first step beside the privateer pays its cost, then loses one
        // point.
        selectShipAt(game, START);
        final ScreenTurnReport slowed = game
                .dragMouseAtLocationAndOpenReport(SLOWED_STEP);
        slowed.verifyThatAtLeastOneEventMessageContains(SLOWED_EN);
        slowed.captureDialog("closeButtonId",
                "naval-interception-slowed-en.png");
        slowed.close();
        game.waitWhileMoving();
        naval.verifyShipAt(SLOWED_STEP);
        naval.verifyMovementThirds(FULL_MOVEMENT - 2 * POINT);
        naval.verifyCombatRandomState(state + GAMMA);

        // A step away from the privateer is an ordinary move: no draw, no
        // report.
        selectShipAt(game, SLOWED_STEP);
        game.dragMouseAtLocation(SAFE_STEP);
        naval.verifyShipAt(SAFE_STEP);
        naval.verifyMovementThirds(FULL_MOVEMENT - 3 * POINT);
        naval.verifyCombatRandomState(state + GAMMA);
        naval.verifyNoReportShown();

        // The next saved draw stops the frigate for the rest of the turn.
        selectShipAt(game, SAFE_STEP);
        final ScreenTurnReport stopped = game
                .dragMouseAtLocationAndOpenReport(STOPPED_STEP);
        stopped.verifyThatAtLeastOneEventMessageContains(STOPPED_EN);
        stopped.captureDialog("closeButtonId",
                "naval-interception-stopped-en.png");
        stopped.close();
        game.waitWhileMoving();
        naval.verifyShipAt(STOPPED_STEP);
        naval.verifyMovementThirds(0);
        naval.verifyCargoIntact();
        naval.verifyCombatRandomState(state + 2 * GAMMA);

        // Continue keeps the stop, the position and the stream; nothing is
        // drawn.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        naval.verifyShipAt(STOPPED_STEP);
        naval.verifyMovementThirds(0);
        naval.verifyCombatRandomState(state + 2 * GAMMA);
        final ScreenTurnReport pending = game.openTurnReport();
        pending.verifyThatAtLeastOneEventMessageContains(STOPPED_EN);
        pending.close();
    }

    @Test
    @Tag("local")
    void TC_02_slowdown_report_is_shown_in_czech() throws Exception {
        // Switch the interface to Czech before the game is loaded.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        loadTarget = prepare(slowThenStopState());
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.panToTile(START);

        // The same real step gives the slowdown report in Czech.
        selectShipAt(game, START);
        final ScreenTurnReport slowed = game
                .dragMouseAtLocationAndOpenReport(SLOWED_STEP);
        slowed.verifyThatAtLeastOneEventMessageContains(SLOWED_CS);
        slowed.captureDialog("closeButtonId",
                "naval-interception-slowed-cs.png");
        slowed.close();
        game.waitWhileMoving();
        new NavalInterceptionPage(getContext()).verifyShipAt(SLOWED_STEP);
    }

    @Test
    @Tag("local")
    void TC_03_unseen_passage_leaves_no_report_and_no_patrol_notice()
            throws Exception {
        final long state = undetectedState();
        loadTarget = prepare(state);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final NavalInterceptionPage naval = new NavalInterceptionPage(
                getContext());
        game.panToTile(START);

        // The privateer passes unseen: the step pays its cost and the stream
        // advances by its one draw, with no notice and no report.
        selectShipAt(game, START);
        game.dragMouseAtLocation(SLOWED_STEP);
        naval.verifyShipAt(SLOWED_STEP);
        naval.verifyMovementThirds(FULL_MOVEMENT - POINT);
        naval.verifyCombatRandomState(state + GAMMA);
        naval.verifyCargoIntact();
        naval.verifyNoReportShown();
        naval.verifyNoPatrolNotice();
    }

    /** The first saved state whose coming draw passes the privateer unseen. */
    private static long undetectedState() {
        for (long state = 0;; state++) {
            if (ShipPatrolRules.draw(
                    Model.combatRandomAfter(state)) == Outcome.UNDETECTED) {
                return state;
            }
        }
    }

    /**
     * The first saved state whose first draw slows the frigate and whose second
     * draw stops it. The second draw follows one ordinary safe step, which
     * draws nothing, so it reads the state advanced by exactly one draw.
     */
    private static long slowThenStopState() {
        for (long state = 0;; state++) {
            if (ShipPatrolRules
                    .draw(Model.combatRandomAfter(state)) == Outcome.SLOWED
                    && ShipPatrolRules.draw(Model.combatRandomAfter(
                            state + GAMMA)) == Outcome.STOPPED) {
                return state;
            }
        }
    }

    /**
     * Writes the fixture with a French privateer beside the route and the
     * chosen saved random state. The file is prepared outside the model; the
     * game opens it like any player save.
     */
    private static File prepare(final long state) throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(FIXTURE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.addProperty("combatRandomState", state);
        model.getAsJsonArray("units").add(privateerAtPeace());
        final File prepared = new File(System.getProperty("user.home"),
                "naval-interception-prepared.microcol");
        Files.writeString(prepared.toPath(), saved.toString());
        return prepared;
    }

    private static JsonObject privateerAtPeace() {
        final JsonObject unit = new JsonObject();
        unit.addProperty("id", PATROL_ID);
        unit.addProperty("type", "PRIVATEER");
        unit.addProperty("ownerId", "France");
        // A privateer holds two goods or passengers; both holds are empty.
        final JsonArray slots = new JsonArray();
        slots.add(new JsonObject());
        slots.add(new JsonObject());
        final JsonObject cargo = new JsonObject();
        cargo.add("slots", slots);
        unit.add("cargo", cargo);
        final JsonObject action = new JsonObject();
        action.addProperty("type", "noAction");
        unit.add("action", action);
        unit.addProperty("movementThirds", 24);
        unit.addProperty("tools", 0);
        unit.addProperty("holdingGuns", false);
        unit.addProperty("mounted", false);
        final JsonObject place = new JsonObject();
        final JsonObject location = new JsonObject();
        location.addProperty("x", PATROL.getX());
        location.addProperty("y", PATROL.getY());
        place.add("location", location);
        place.addProperty("orientation", "east");
        unit.add("placeMap", place);
        return unit;
    }
}
