package org.microcol.page;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import org.microcol.model.Location;
import org.microcol.model.UnitType;
import org.microcol.model.unit.UnitArtillery;
import org.microcol.model.unit.UnitWithCargo;
import org.testfx.util.WaitForAsyncUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Artillery actions and explicit observations across existing player screens.
 */
public final class ArtilleryPage extends AbstractScreen {
    public ArtilleryPage(TestContext context) {
        super(context);
    }

    public void buy() {
        new ExpertPurchasePage(getContext()).revealSpecialist("ARTILLERY");
        clickOnButtonWithId("buy-unit-ARTILLERY");
    }

    public void prepareNextBattle() {
        getRobot().interact(() -> {
            ((org.microcol.model.AbstractUnit) getModel().getUnitById(0))
                    .setActionPoints(1);
            ((org.microcol.model.AbstractUnit) getModel().getUnitById(6))
                    .setActionPoints(1);
        });
    }

    public void loadConstructionFixture(boolean armory) throws Exception {
        Gson gson = new Gson();
        JsonObject game = gson.fromJson(Files.readString(
                new File("src/test/scenarios/F46-rush-construction.microcol")
                        .toPath()),
                JsonObject.class);
        JsonObject colony = game.getAsJsonObject("model")
                .getAsJsonArray("colonies").get(0).getAsJsonObject();
        if (armory) {
            JsonObject building = new JsonObject();
            building.addProperty("type", "ARMORY");
            JsonArray slots = new JsonArray();
            for (int i = 0; i < 3; i++)
                slots.add(new JsonObject());
            building.add("slots", slots);
            colony.getAsJsonArray("constructions").add(building);
        }
        colony.getAsJsonObject("colonyWarehouse").addProperty("HAMMERS", 200);
        colony.getAsJsonObject("colonyWarehouse").addProperty("TOOLS", 100);
        Path fixture = Files.createTempFile("artillery-construction-",
                ".microcol");
        try {
            Files.writeString(fixture, gson.toJson(game));
            new AttackPage(getContext()).replaceGame(fixture.toFile());
        } finally {
            Files.deleteIfExists(fixture);
        }
    }

    public void verifyPurchase(int count, int gold, int nextPrice) {
        org.microcol.model.Player owner = getModel().getPlayerByName("Dutch");
        assertEquals(count, owner.save().getUnitPurchaseCounts()
                .getOrDefault("ARTILLERY", 0));
        assertEquals(gold, owner.getGold());
        assertEquals(nextPrice, owner.getUnitPurchasePrice(UnitType.ARTILLERY));
        assertEquals(1000, owner.getUnitPurchasePrice(UnitType.CARAVEL));
    }

    public void verifyCargo(int unitId, int shipId, int slot) {
        UnitArtillery gun = (UnitArtillery) getModel().getUnitById(unitId);
        UnitWithCargo ship = (UnitWithCargo) getModel().getUnitById(shipId);
        assertTrue(gun.isAtCargoSlot());
        assertFalse(gun.isDamaged());
        assertSame(gun,
                ship.getCargo().getSlots().get(slot).getUnit().orElseThrow());
        assertEquals(1, ship.getCargo().getSlots().stream()
                .filter(s -> s.getUnit().orElse(null) == gun).count());
    }

    public void verifyDamaged(int unitId, Location at, int moves)
            throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS,
                () -> ((UnitArtillery) getModel().getUnitById(unitId))
                        .isDamaged());
        GamePage.of(getContext()).waitWhileMoving();
        UnitArtillery gun = (UnitArtillery) getModel().getUnitById(unitId);
        assertEquals(at, gun.getLocation());
        assertEquals(moves, gun.getActionPoints());
        assertEquals("Dutch", gun.getOwner().getName());
        org.microcol.gui.image.ImageProvider images = getContext()
                .getClassFromGuice(org.microcol.gui.image.ImageProvider.class);
        assertSame(images.getImage("artillery-damaged.png"),
                images.getUnitImage(gun));
    }

    public void verifyDestroyed(int id) throws Exception {
        WaitForAsyncUtils.waitFor(10, TimeUnit.SECONDS, () -> getModel()
                .getAllUnits().stream().noneMatch(u -> u.getId() == id));
        GamePage.of(getContext()).waitWhileMoving();
        assertTrue(getModel().getAllUnits().stream()
                .noneMatch(u -> u.getId() == id));
        assertEquals(0, getModel().getUnitById(6).getActionPoints());
    }

    public void verifyFeedback(String text) throws Exception {
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS,
                () -> WaitForAsyncUtils.asyncFx(() -> getListOfNodes(".label")
                        .stream()
                        .filter(javafx.scene.control.Label.class::isInstance)
                        .map(n -> ((javafx.scene.control.Label) n).getText())
                        .anyMatch(value -> value.contains(text))).get());
        assertTrue(getListOfNodes(".label").stream()
                .filter(javafx.scene.control.Label.class::isInstance)
                .map(n -> ((javafx.scene.control.Label) n).getText())
                .anyMatch(value -> value.contains(text)));
    }
}
