package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.microcol.model.PlaceShipRepair;
import org.microcol.model.unit.Ship;
import javafx.scene.input.KeyCode;

/** Player controls and explicit observations of saved ship repairs. */
public final class ShipRepairPage extends AbstractScreen {
    public ShipRepairPage(TestContext context) {
        super(context);
    }

    public void open() {
        clickOnButtonWithId("shipRepairsButton");
    }

    public void close() {
        clickOnButtonWithId("shipRepairsClose");
    }

    public void escape() {
        getRobot().push(KeyCode.ESCAPE);
    }

    public void verifyProgress(String expectedPhase, int expectedUpdates,
            String expectedText) {
        Ship ship = (Ship) getModel().getUnitById(3);
        assertTrue(ship.isDamaged());
        PlaceShipRepair repair = ship.getRepair().orElseThrow();
        assertEquals(expectedPhase, repair.getPhase().name());
        assertEquals(expectedUpdates, repair.getRemainingOwnerUpdates());
        assertEquals(0, ship.getActionPoints());
        assertFalse(ship.canAttack());
        assertEquals(expectedText, getLabeledById("shipRepair-3").getText());
        assertEquals(expectedText,
                ((javafx.scene.text.Text) getLabeledById("shipRepair-3")
                        .lookup(".text")).getText());
        new AtlanticAuditPage(getContext()).verifyCargo();
    }

    public void verifyComplete(boolean europe, String emptyText) {
        Ship ship = (Ship) getModel().getUnitById(3);
        assertFalse(ship.isDamaged());
        assertEquals(europe, ship.isAtEuropePort());
        if (!europe) {
            assertEquals(
                    getModel().getColonyById(0).orElseThrow().getLocation(),
                    ship.getLocation());
        }
        assertTrue(ship.getActionPoints() > 0);
        assertEquals(emptyText, getLabeledById("shipRepairsEmpty").getText());
        new AtlanticAuditPage(getContext()).verifyCargo();
    }

    public void loadLostDestinationFixture(boolean europeOpen)
            throws Exception {
        Gson gson = new Gson();
        JsonObject saved = gson.fromJson(
                Files.readString(
                        Path.of("src/test/scenarios/F54-ship-repair.microcol")),
                JsonObject.class);
        JsonObject model = saved.getAsJsonObject("model");
        JsonObject repair = model.getAsJsonArray("units").get(0)
                .getAsJsonObject().getAsJsonObject("placeShipRepair");
        repair.addProperty("europeDestination", false);
        repair.addProperty("destinationColonyId", 9999);
        repair.addProperty("phase", "REPAIR");
        repair.addProperty("repairDuration", 3);
        for (var element : model.getAsJsonObject("gameManager")
                .getAsJsonArray("players")) {
            JsonObject player = element.getAsJsonObject();
            if ("Dutch".equals(player.get("name").getAsString())) {
                RebellionJson.set(model, player, !europeOpen);
            }
        }
        Path fixture = Files.createTempFile("ship-repair-lost-destination-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(saved));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    public void verifySunk() {
        assertTrue(getModel().tryGetUnitById(3).isEmpty());
        assertTrue(getModel().tryGetUnitById(4).isEmpty());
    }

    public void loadDrydockFixture() throws Exception {
        Gson gson = new Gson();
        JsonObject saved = gson.fromJson(
                Files.readString(
                        Path.of("src/test/scenarios/F54-ship-repair.microcol")),
                JsonObject.class);
        JsonObject model = saved.getAsJsonObject("model");
        JsonObject repair = model.getAsJsonArray("units").get(0)
                .getAsJsonObject().getAsJsonObject("placeShipRepair");
        repair.addProperty("europeDestination", false);
        repair.addProperty("destinationColonyId", 0);
        repair.addProperty("repairDuration", 3);
        JsonObject drydock = new JsonObject();
        drydock.addProperty("type", "DRYDOCK");
        drydock.add("slots", new JsonArray());
        model.getAsJsonArray("colonies").get(0).getAsJsonObject()
                .getAsJsonArray("constructions").add(drydock);
        Path fixture = Files.createTempFile("ship-repair-drydock-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(saved));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }
}
