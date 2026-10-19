package org.microcol.test;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.microcol.model.Location;
import org.microcol.page.FreePlayAuditPage;
import org.microcol.page.ColonyScreen;
import org.microcol.page.EuropePortScreen;
import org.microcol.page.GamePage;
import org.microcol.page.PassengerTransferPage;
import org.microcol.page.WelcomePage;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import javafx.stage.Stage;

@ExtendWith(ApplicationExtension.class)
public class TC_29_F21_passenger_transfer_test extends AbstractMicroColTest {
    private static final Location WATER = Location.of(25, 15);
    private static final Location SHORE = Location.of(24, 15);

    @Start
    private void start(Stage stage) throws Exception {
        initialize(stage, getClass(),
                new File("src/test/scenarios/F21-passenger-transfer.microcol"));
    }

    @Test
    @Tag("local")
    void TC_01_choose_one_passenger_cancel_and_continue() throws Exception {
        // Open a landing choice without moving the ship or either passenger.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.verifyChoice(4, true);
        passengers.verifyChoice(5, false);
        passengers.captureDialog("passenger-confirm",
                "passenger-selection.png");
        passengers.cancel();
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        passengers.verifyShip(WATER, 6);

        // Escape also cancels a changed choice, and reopening resets selection.
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.choose(5);
        passengers.escape();
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.verifyChoice(4, true);
        passengers.choose(5);
        passengers.confirm();
        game.waitForNumberOfUnitsAt(SHORE, 1);
        passengers.verifyAshore(5, SHORE, 4, 0);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyShip(WATER, 6);

        // Continue preserves the separate places; the selected rider boards
        // again.
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        passengers.verifyAshore(5, SHORE, 4, 0);
        passengers.verifyAboard(4, 1, 1, 80);
        game.moveMouseAtLocation(SHORE);
        game.dragMouseAtLocation(WATER);
        game.waitForNumberOfUnitsAt(SHORE, 0);
        passengers.verifyAboard(5, 2, 3, 0);
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyShip(WATER, 6);

        // Land the equipped pioneer separately and retain tools after Continue.
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.choose(4);
        passengers.confirm();
        game.waitForNumberOfUnitsAt(SHORE, 1);
        passengers.verifyAshore(4, SHORE, 0, 80);
        passengers.verifyAboard(5, 2, 3, 0);
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        passengers.verifyAshore(4, SHORE, 0, 80);
        passengers.verifyAboard(5, 2, 3, 0);

        // Even the sole remaining passenger requires an explicit choice.
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.verifyChoice(5, true);
        passengers.verifyChoiceCount(1);
        passengers.cancel();
        passengers.verifyAshore(4, SHORE, 0, 80);
        passengers.verifyAboard(5, 2, 3, 0);
    }

    @Test
    @Tag("local")
    void TC_02_changed_shore_rejects_confirmation_without_unloading()
            throws Exception {
        // A legal choice is open when another unit occupies the shore.
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        game.moveMouseAtLocation(WATER);
        game.dragMouseAtLocation(SHORE);
        passengers.choose(4);
        WaitForAsyncUtils.asyncFx(
                () -> getModel().getUnitById(1000).placeToLocation(SHORE))
                .get();
        passengers.confirm();

        // The model rejects the stale choice and the player sees the reason.
        passengers.verifyRejection();
        passengers.closeRejection();
        passengers.verifyAboard(4, 1, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        passengers.verifyShip(WATER, 6);
    }

    @Test
    @Tag("local")
    void TC_03_zero_move_passenger_uses_owned_colony_port_and_keeps_equipment()
            throws Exception {
        // Load a coastal port with an exhausted pioneer still aboard.
        loadPortFixture(false);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        Location port = Location.of(24, 16);
        ColonyScreen colony = game.openColonyAt(port, "New Amsterdam");
        colony.selectUnitFromPort(0);
        passengers.disembarkInPort(4, 1, false);
        passengers.verifyAshore(4, port, 0, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        passengers.verifyShip(port, 6);

        // Occupied goods and passenger holds reject the drop without losing
        // gear.
        passengers.dragPassengerToSlot(4, 0, false);
        passengers.verifyAshore(4, port, 0, 80);
        passengers.dragPassengerToSlot(4, 2, false);
        passengers.verifyAshore(4, port, 0, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        passengers.embarkInPort(4, 1, false);
        passengers.verifyAboard(4, 1, 0, 80);

        // Continue preserves the local transfer and permits unloading again at
        // zero.
        game = colony.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        passengers.verifyAboard(4, 1, 0, 80);
        colony = game.openColonyAt(port, "New Amsterdam");
        colony.selectUnitFromPort(0);
        passengers.disembarkInPort(4, 1, false);
        passengers.verifyAshore(4, port, 0, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        colony.close();
    }

    @Test
    @Tag("local")
    void TC_04_zero_move_passenger_uses_european_pier_and_free_hold()
            throws Exception {
        // The same exhausted pioneer can use the pier without spending
        // movement.
        loadPortFixture(true);
        EuropePortScreen europe = WelcomePage.of(getContext()).loadGame()
                .openEuroperPort();
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        europe.selectUnitFromPort(0);
        passengers.disembarkInPort(4, 1, true);
        passengers.verifyOnPier(4, 0, 80);
        passengers.verifyAboard(5, 2, 5, 0);

        // An occupied hold rejects boarding; an empty hold retains the same
        // person.
        passengers.dragPassengerToSlot(4, 2, true);
        passengers.verifyOnPier(4, 0, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        passengers.embarkInPort(4, 1, true);
        passengers.verifyAboard(4, 1, 0, 80);
        GamePage game = europe.close();
        game.exitToMenu();
        game = new FreePlayAuditPage(getContext()).continueGame();
        passengers.verifyAboard(4, 1, 0, 80);
        europe = game.openEuroperPort();
        europe.selectUnitFromPort(0);
        passengers.disembarkInPort(4, 1, true);
        passengers.verifyOnPier(4, 0, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        europe.close();
    }

    @Test
    @Tag("local")
    void TC_05_foreign_ship_does_not_offer_passenger_boarding()
            throws Exception {
        // Prepare an owned pioneer on shore next to a foreign empty ship.
        com.google.gson.JsonObject saved = readFixture();
        for (com.google.gson.JsonElement entry : saved.getAsJsonObject("model")
                .getAsJsonArray("units")) {
            com.google.gson.JsonObject unit = entry.getAsJsonObject();
            int id = unit.get("id").getAsInt();
            if (id == 3) {
                unit.remove("placeMap");
                com.google.gson.JsonObject port = new com.google.gson.JsonObject();
                port.addProperty("isOnPier", false);
                unit.add("placeEuropePort", port);
                unit.getAsJsonObject("cargo").getAsJsonArray("slots").set(1,
                        new com.google.gson.JsonObject());
            } else if (id == 4) {
                unit.remove("placeCargoSlot");
                com.google.gson.JsonObject place = new com.google.gson.JsonObject();
                com.google.gson.JsonObject location = new com.google.gson.JsonObject();
                location.addProperty("x", SHORE.getX());
                location.addProperty("y", SHORE.getY());
                place.add("location", location);
                place.addProperty("orientation", "east");
                unit.add("placeMap", place);
            } else if (id == 1000) {
                unit.addProperty("type", "GALLEON");
                unit.remove("workUpdates");
                unit.remove("missionary");
                unit.remove("workSkill");
                unit.addProperty("holdingGuns", false);
                unit.getAsJsonObject("placeMap").getAsJsonObject("location")
                        .addProperty("x", WATER.getX());
                unit.getAsJsonObject("placeMap").getAsJsonObject("location")
                        .addProperty("y", WATER.getY());
                com.google.gson.JsonArray slots = new com.google.gson.JsonArray();
                for (int n = 0; n < 5; n++)
                    slots.add(new com.google.gson.JsonObject());
                unit.getAsJsonObject("cargo").add("slots", slots);
            }
        }
        useFixture(saved);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        passengers.verifyAshore(4, SHORE, 1, 80);
        passengers.verifyForeignShip(1000, "Dutch's King", WATER);

        // Attempt the normal boarding gesture; it must not move either unit.
        game.moveMouseAtLocation(SHORE);
        game.dragMouseAtLocation(WATER);
        passengers.verifyMovementRejected();
        passengers.closeRejection();
        passengers.verifyAshore(4, SHORE, 1, 80);
        passengers.verifyForeignShip(1000, "Dutch's King", WATER);
        passengers.verifyAboard(5, 2, 5, 0);
    }

    @Test
    @Tag("local")
    void TC_06_full_ship_rejects_boarding_without_changing_any_hold()
            throws Exception {
        // Put the equipped pioneer on shore and fill every hold of the own
        // ship.
        com.google.gson.JsonObject saved = readFixture();
        for (com.google.gson.JsonElement entry : saved.getAsJsonObject("model")
                .getAsJsonArray("units")) {
            com.google.gson.JsonObject unit = entry.getAsJsonObject();
            if (unit.get("id").getAsInt() == 4) {
                unit.remove("placeCargoSlot");
                com.google.gson.JsonObject place = new com.google.gson.JsonObject();
                com.google.gson.JsonObject location = new com.google.gson.JsonObject();
                location.addProperty("x", SHORE.getX());
                location.addProperty("y", SHORE.getY());
                place.add("location", location);
                place.addProperty("orientation", "east");
                unit.add("placeMap", place);
            } else if (unit.get("id").getAsInt() == 3) {
                com.google.gson.JsonArray slots = unit.getAsJsonObject("cargo")
                        .getAsJsonArray("slots");
                slots.add(new com.google.gson.JsonObject());
                for (int index : new int[] { 1, 3, 4, 5 }) {
                    com.google.gson.JsonObject goods = new com.google.gson.JsonObject();
                    goods.addProperty("goodsType", "TOOLS");
                    goods.addProperty("amount", 100);
                    slots.set(index, goods);
                }
            }
        }
        useFixture(saved);
        GamePage game = WelcomePage.of(getContext()).loadGame();
        PassengerTransferPage passengers = new PassengerTransferPage(
                getContext());
        passengers.verifyFullShip(3);
        game.moveMouseAtLocation(SHORE);
        game.dragMouseAtLocation(WATER);
        passengers.verifyMovementRejected();
        passengers.closeRejection();
        passengers.verifyAshore(4, SHORE, 1, 80);
        passengers.verifyAboard(5, 2, 5, 0);
        passengers.verifyShip(WATER, 6);
        passengers.verifyFullShip(3);
        passengers.verifyCargoGoods(3, 1, org.microcol.model.Goods
                .of(org.microcol.model.GoodsType.TOOLS, 100));
        passengers.verifyCargoGoods(3, 3, org.microcol.model.Goods
                .of(org.microcol.model.GoodsType.TOOLS, 100));
        passengers.verifyCargoGoods(3, 4, org.microcol.model.Goods
                .of(org.microcol.model.GoodsType.TOOLS, 100));
        passengers.verifyCargoGoods(3, 5, org.microcol.model.Goods
                .of(org.microcol.model.GoodsType.TOOLS, 100));
    }

    private com.google.gson.JsonObject readFixture() throws Exception {
        return new com.google.gson.Gson().fromJson(
                java.nio.file.Files.readString(java.nio.file.Path.of(
                        "src/test/scenarios/F21-passenger-transfer.microcol")),
                com.google.gson.JsonObject.class);
    }

    private void loadPortFixture(boolean europe) throws Exception {
        com.google.gson.JsonObject saved = readFixture();
        for (com.google.gson.JsonElement entry : saved.getAsJsonObject("model")
                .getAsJsonArray("units")) {
            com.google.gson.JsonObject unit = entry.getAsJsonObject();
            if (unit.get("id").getAsInt() == 4) {
                unit.addProperty("movementThirds", 0);
            }
            if (unit.get("id").getAsInt() == 3) {
                if (europe) {
                    unit.remove("placeMap");
                    com.google.gson.JsonObject port = new com.google.gson.JsonObject();
                    port.addProperty("isOnPier", false);
                    unit.add("placeEuropePort", port);
                } else {
                    com.google.gson.JsonObject location = unit
                            .getAsJsonObject("placeMap")
                            .getAsJsonObject("location");
                    location.addProperty("x", 24);
                    location.addProperty("y", 16);
                }
            }
        }
        useFixture(saved);
    }

    private void useFixture(com.google.gson.JsonObject saved) throws Exception {
        File fixture = new File(System.getProperty("user.home"),
                "passenger-port.microcol");
        java.nio.file.Files.writeString(fixture.toPath(), saved.toString());
        org.mockito.Mockito
                .when(getContext()
                        .getClassFromGuice(
                                org.microcol.gui.FileSelectingService.class)
                        .loadFile(org.mockito.ArgumentMatchers.any(File.class)))
                .thenReturn(fixture);
    }

}
