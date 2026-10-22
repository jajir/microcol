package org.microcol.test;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.gui.FileSelectingService;
import org.microcol.gui.screen.game.gamepanel.CursorService;
import org.microcol.mock.CursorServiceNoOpp;
import org.microcol.model.Location;
import org.microcol.model.Model;
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
 * A Dutch frigate sails past the French colony Quebec, which has a fort and is
 * at war with the Dutch. The fixture makes the coast explicit: the three sea
 * squares beside and before the port are written into the saved tiles. The
 * first step beside the port is a battery shot; a miss lets the frigate sail on
 * with no repeated shot, even after Continue, and a hit sends it to repair with
 * its cargo and no patrol draw.
 */
@ExtendWith(ApplicationExtension.class)
public class TC_142_F133_coastal_battery_test extends AbstractMicroColTest {

    private static final File FIXTURE = new File(
            "src/test/scenarios/F55-naval-combat.microcol");

    /** The Dutch frigate starts on open sea, two squares from the port. */
    private static final Location START = Location.of(20, 10);

    /** Open sea that is not beside the port; the frigate passes it first. */
    private static final Location PASSAGE = Location.of(19, 10);

    /** Sea beside the port; the first battery shot is taken here. */
    private static final Location APPROACH = Location.of(18, 10);

    /** Sea beside the port again; a second step never repeats the shot. */
    private static final Location PORT_PATROL = Location.of(18, 9);

    /** Full allowance of the frigate in the fixture, in thirds of a point. */
    private static final int FULL_MOVEMENT = 18;

    /** One ordinary sea step, in thirds of a point. */
    private static final int POINT = 3;

    /**
     * Fort strength 4 against the frigate's defense 16 gives a 1/5 hit chance.
     */
    private static final double HIT_CHANCE = 4.0 / 20.0;

    private static final long GAMMA = 0x9E3779B97F4A7C15L;

    private static final String MISS_EN = "A coastal battery fired at your ship and missed.";

    private static final String HIT_EN = "A coastal battery hit and damaged your ship.";

    private static final String HIT_CS = "Pobřežní baterie zasáhla a poškodila vaši loď";

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
    void TC_01_miss_continues_passage_and_no_repeated_shot_after_continue()
            throws Exception {
        final long state = missState();
        loadTarget = prepare(state);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        final NavalInterceptionPage naval = new NavalInterceptionPage(
                getContext());
        game.panToTile(START);
        naval.verifyMovementThirds(FULL_MOVEMENT);
        naval.verifyCombatRandomState(state);

        // The passage square is not beside the port; the approach is, and its
        // one battery shot misses. The report shows the miss at once.
        selectShipAt(game, START);
        final ScreenTurnReport missed = game
                .dragMouseAtLocationAndOpenReport(APPROACH);
        missed.verifyThatAtLeastOneEventMessageContains(MISS_EN);
        missed.captureDialog("closeButtonId", "coastal-battery-miss-en.png");
        missed.close();
        game.waitWhileMoving();
        naval.verifyShipAt(APPROACH);
        naval.verifyMovementThirds(FULL_MOVEMENT - 2 * POINT);
        naval.verifyCombatRandomState(state + GAMMA);
        naval.verifyCoastalBatteryNoticeCount(1);

        // The next step is still beside the port: the battery has fired at
        // this ship this round, so the step draws nothing and opens no report.
        selectShipAt(game, APPROACH);
        game.dragMouseAtLocation(PORT_PATROL);
        naval.verifyShipAt(PORT_PATROL);
        naval.verifyMovementThirds(FULL_MOVEMENT - 3 * POINT);
        naval.verifyCombatRandomState(state + GAMMA);
        naval.verifyNoReportShown();
        naval.verifyCoastalBatteryNoticeCount(1);

        // Continue keeps the position, the stream and the one notice. A step
        // back beside the port repeats nothing.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        naval.verifyShipAt(PORT_PATROL);
        naval.verifyCombatRandomState(state + GAMMA);
        naval.verifyCoastalBatteryNoticeCount(1);
        selectShipAt(game, PORT_PATROL);
        game.dragMouseAtLocation(APPROACH);
        naval.verifyShipAt(APPROACH);
        naval.verifyMovementThirds(FULL_MOVEMENT - 4 * POINT);
        naval.verifyCombatRandomState(state + GAMMA);
        naval.verifyNoReportShown();
        naval.verifyCoastalBatteryNoticeCount(1);
    }

    @Test
    @Tag("local")
    void TC_02_hit_sends_ship_to_repair_with_cargo_and_visible_notice()
            throws Exception {
        final long state = hitState();
        loadTarget = prepare(state);
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        final NavalInterceptionPage naval = new NavalInterceptionPage(
                getContext());
        game.panToTile(START);

        // The approach square is the battery's shot: it hits, the report says
        // the ship was damaged and the frigate leaves the map for repair.
        selectShipAt(game, START);
        final ScreenTurnReport hit = game
                .dragMouseAtLocationAndOpenReport(APPROACH);
        hit.verifyThatAtLeastOneEventMessageContains(HIT_EN);
        hit.captureDialog("closeButtonId", "coastal-battery-hit-en.png");
        hit.close();
        game.waitWhileMoving();
        naval.verifyShipDamagedOffMap();
        naval.verifyCombatRandomState(state + GAMMA);
        naval.verifyCargoIntact();
        naval.verifyCoastalBatteryNoticeCount(1);
    }

    @Test
    @Tag("local")
    void TC_03_hit_notice_is_shown_in_czech() throws Exception {
        // Switch the interface to Czech before the game is loaded.
        final SettingPage settings = WelcomePage.of(getContext()).openSetting();
        settings.setCzechLanguage();
        settings.goBack();
        loadTarget = prepare(hitState());
        final GamePage game = WelcomePage.of(getContext()).loadGame();
        game.panToTile(START);

        // The same real hit gives the forced repair notice in Czech.
        selectShipAt(game, START);
        final ScreenTurnReport hit = game
                .dragMouseAtLocationAndOpenReport(APPROACH);
        hit.verifyThatAtLeastOneEventMessageContains(HIT_CS);
        hit.captureDialog("closeButtonId", "coastal-battery-hit-cs.png");
        hit.close();
        game.waitWhileMoving();
        new NavalInterceptionPage(getContext()).verifyShipDamagedOffMap();
    }

    /** The first saved state whose coming draw misses the frigate. */
    private static long missState() {
        for (long state = 0;; state++) {
            if (Model.combatRandomAfter(state) >= HIT_CHANCE) {
                return state;
            }
        }
    }

    /** The first saved state whose coming draw hits the frigate. */
    private static long hitState() {
        for (long state = 0;; state++) {
            if (Model.combatRandomAfter(state) < HIT_CHANCE) {
                return state;
            }
        }
    }

    /**
     * Writes the fixture with the chosen saved random state, the explicit sea
     * squares, the war with France and a fort on Quebec. The file is prepared
     * outside the model; the game opens it like any player save.
     */
    private static File prepare(final long state) throws Exception {
        final JsonObject saved = new Gson()
                .fromJson(Files.readString(FIXTURE.toPath()), JsonObject.class);
        final JsonObject model = saved.getAsJsonObject("model");
        model.addProperty("combatRandomState", state);
        makeSea(model, List.of(PASSAGE, APPROACH, PORT_PATROL));
        declareWarWithFrance(model);
        addFortToQuebec(model);
        final File prepared = new File(System.getProperty("user.home"),
                "coastal-battery-prepared.microcol");
        Files.writeString(prepared.toPath(), saved.toString());
        return prepared;
    }

    /**
     * Writes sea into the saved terrain and removes any tree there, one square
     * at a time. Rows are labelled row-YYYY and each cell is a column, counted
     * from 1.
     */
    private static void makeSea(final JsonObject model,
            final List<Location> squares) {
        final JsonObject map = model.getAsJsonObject("map");
        for (final String layer : List.of("tiles", "trees")) {
            final JsonArray rows = map.getAsJsonArray(layer);
            for (int i = 0; i < rows.size(); i++) {
                String row = rows.get(i).getAsString();
                for (final Location at : squares) {
                    final String label = String.format("row-%04d:", at.getY());
                    if (row.startsWith(label)) {
                        final String[] cells = row.substring(label.length())
                                .split(",");
                        cells[at.getX() - 1] = "_";
                        row = label + String.join(",", cells);
                    }
                }
                rows.set(i, new JsonPrimitive(row));
            }
        }
    }

    private static void declareWarWithFrance(final JsonObject model) {
        for (final JsonElement element : model
                .getAsJsonArray("europeanRelations")) {
            final JsonObject relation = element.getAsJsonObject();
            if (Set.of(relation.get("firstPlayer").getAsString(),
                    relation.get("secondPlayer").getAsString())
                    .equals(Set.of("Dutch", "France"))) {
                relation.addProperty("state", "WAR");
            }
        }
    }

    /** Quebec, the French colony on the fixture's port square, by its name. */
    private static void addFortToQuebec(final JsonObject model) {
        final JsonObject fort = new JsonObject();
        fort.addProperty("type", "FORT");
        fort.add("slots", new JsonArray());
        for (final JsonElement element : model.getAsJsonArray("colonies")) {
            final JsonObject colony = element.getAsJsonObject();
            if (colony.get("name").getAsString().equals("Quebec")) {
                colony.getAsJsonArray("constructions").add(fort);
                return;
            }
        }
        throw new IllegalStateException("Fixture has no colony named Quebec");
    }
}
