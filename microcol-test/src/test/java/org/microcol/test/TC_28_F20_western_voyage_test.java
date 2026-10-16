package org.microcol.test;

import java.io.File;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.AtlanticAuditPage;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.FogPage;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.GamePage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;
import javafx.stage.Stage;

/** Sail out of the western lane and wait for the exact return square. */
@ExtendWith(ApplicationExtension.class)
public class TC_28_F20_western_voyage_test extends AbstractMicroColTest {
    private static final File FIXTURE = new File(
            "src/test/scenarios/F20-western-voyage.microcol");

    @Start
    private void start(final Stage stage) throws Exception {
        initialize(stage, getClass(), FIXTURE);
    }

    @Test
    @Tag("local")
    void TC_01_western_departure_waits_for_its_recorded_arrival()
            throws Exception {
        // Entry, travel along the lane and an inward step remain ordinary
        // moves.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        AtlanticAuditPage audit = new AtlanticAuditPage(getContext());
        game.moveMouseAtLocation(Location.of(3, 16));
        game.dragMouseAtLocation(Location.of(2, 16));
        audit.verifyReturnedTo(Location.of(2, 16));
        game.moveMouseAtLocation(Location.of(2, 16));
        game.dragMouseAtLocation(Location.of(2, 17));
        audit.verifyReturnedTo(Location.of(2, 17));
        game.moveMouseAtLocation(Location.of(2, 17));
        game.dragMouseAtLocation(Location.of(3, 17));
        audit.verifyReturnedTo(Location.of(3, 17));
        game.moveMouseAtLocation(Location.of(3, 17));
        game.dragMouseAtLocation(Location.of(2, 17));
        audit.verifyMovement(2);

        // Return an outward drag to its origin before releasing: no departure.
        FogPage preview = new FogPage(getContext());
        preview.preview(Location.of(2, 17), Location.of(1, 17));
        preview.cancelPreview();
        audit.verifyReturnedTo(Location.of(2, 17));
        audit.verifyMovement(2);
        audit.verifyCargo();

        // Confirm the outward gesture and retain its sampled duration.
        game.moveMouseAtLocation(Location.of(2, 17));
        game.dragMouseAtLocation(Location.of(1, 17));
        final int outwardDuration = audit.verifyVoyageSample(true, true);
        final int outwardWeather = audit.getVoyageWeather();
        audit.verifyMovement(1);
        EuropePortScreen europe = game.openEuroperPort();
        audit.verifyVoyageText("Turns remaining: " + outwardDuration);
        game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        audit.verifyVoyageMetadata(outwardWeather, outwardDuration,
                outwardDuration, true);
        for (int remaining = outwardDuration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            if (remaining > 0) {
                audit.verifyVoyageMetadata(outwardWeather, outwardDuration,
                        remaining, true);
            }
        }
        audit.verifyInEurope();
        audit.verifyCargo();

        // Another ship occupies the original square during the return crossing.
        europe = game.openEuroperPort();
        europe.selectUnitFromPort(0);
        audit.sendShipBack();
        final int returnDuration = audit.verifyVoyageSample(false, true);
        final int returnWeather = audit.getVoyageWeather();
        audit.verifyVoyageText("Turns remaining: " + returnDuration);
        WaitForAsyncUtils.asyncFx(() -> getModel().getUnitById(1000)
                .placeToLocation(Location.of(2, 17))).get();
        game = europe.close();
        for (int remaining = returnDuration - 1; remaining >= 0; remaining--) {
            game.nextTurnAndCloseDialogs();
            audit.verifyVoyageMetadata(returnWeather, returnDuration, remaining,
                    false);
        }
        audit.verifyCargo();
        europe = game.openEuroperPort();
        audit.verifyVoyageText("Waiting: the arrival square is occupied.");
        audit.captureDialog("voyage-remaining-3",
                "western-arrival-waiting.png");
        game = europe.close();

        // Continue preserves the wait; further turns never make it negative.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        audit.verifyVoyageMetadata(returnWeather, returnDuration, 0, false);
        game.nextTurnAndCloseDialogs();
        audit.verifyVoyageMetadata(returnWeather, returnDuration, 0, false);
        audit.verifyCargo();

        // Once that concurrent obstruction leaves, the next owner turn arrives.
        WaitForAsyncUtils.asyncFx(() -> getModel().getUnitById(1000)
                .placeToLocation(Location.of(2, 19))).get();
        game.nextTurnAndCloseDialogs();
        audit.verifyReturnedTo(Location.of(2, 17));
        audit.verifyCargo();
    }

    @Test
    @Tag("local")
    void TC_02_missing_arrival_profile_keeps_ship_and_cargo_in_port()
            throws Exception {
        // Prepare a reference map without sea lanes and a ship awaiting its
        // first crossing.
        com.google.gson.JsonObject saved = new com.google.gson.Gson().fromJson(
                java.nio.file.Files.readString(FIXTURE.toPath()),
                com.google.gson.JsonObject.class);
        com.google.gson.JsonObject model = saved.getAsJsonObject("model");
        com.google.gson.JsonArray tiles = model.getAsJsonObject("map")
                .getAsJsonArray("tiles");
        for (int row = 1; row < tiles.size(); row++) {
            tiles.set(row, new com.google.gson.JsonPrimitive(
                    tiles.get(row).getAsString().replace('~', '_')));
        }
        for (com.google.gson.JsonElement element : model
                .getAsJsonArray("units")) {
            com.google.gson.JsonObject unit = element.getAsJsonObject();
            if (unit.get("id").getAsInt() == 3) {
                unit.remove("placeMap");
                unit.add("placeEuropePort", new com.google.gson.JsonObject());
            }
        }
        File noLane = new File(System.getProperty("user.home"),
                "no-sea-lane.microcol");
        java.nio.file.Files.writeString(noLane.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(noLane);

        // The actual Europe drag explains why it cannot leave and preserves its
        // cargo.
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        europe.selectUnitFromPort(0);
        AtlanticAuditPage audit = new AtlanticAuditPage(getContext());
        audit.attemptSendShipBack();
        audit.verifyDepartureRejection(
                "This ship cannot depart. It must be in your European port during your turn, with an available sea-lane destination.");
        audit.verifyInEurope();
        audit.verifyCargo();
        audit.captureDialog("buttonOk", "voyage-no-destination.png");
        audit.closeRejection();
        europe.verifyNumberOfShipsInPort(1);
    }
}
